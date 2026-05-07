package io.setaicompanion.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.setaicompanion.collector.CollectorConfig;
import io.setaicompanion.collector.EventCollector;
import io.setaicompanion.collector.EventsCollected;
import io.setaicompanion.collector.Filter;
import io.setaicompanion.collector.FilterOperator;
import io.setaicompanion.github.dto.GitHubEvent;
import io.setaicompanion.github.dto.GitHubEventPayload;
import io.setaicompanion.github.dto.GitHubPullRequest;
import io.setaicompanion.model.ApplicationEvent;
import io.setaicompanion.model.PullRequestEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitHubCollector implements EventCollector {

    private static final ObjectMapper MAPPER  = new ObjectMapper();
    private static final Pattern      NEXT_LINK = Pattern.compile("<([^>]+)>;\\s*rel=\"next\"");

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();

    @Override
    public String getType() {
        return "github";
    }

    @Override
    public List<String> getFilterKeysSupported() {
        return List.of("eventType", "action");
    }

    @Override
    public EventsCollected collect(CollectorConfig config, String checkpoint) throws Exception {
        String repoApiUrl = config.url().replaceAll("/+$", "");
        RepoCoords coords  = parseRepoCoords(repoApiUrl);
        String repoKey     = coords.owner() + "/" + coords.repo();

        Checkpoint cp = Checkpoint.parse(checkpoint);

        // ── First page — use ETag for 304 optimisation ────────────────────────
        HttpRequest.Builder firstReq = HttpRequest.newBuilder()
            .uri(URI.create(repoApiUrl + "?per_page=100"))
            .header("Authorization", basicAuth(config.apiToken()))
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .timeout(Duration.ofSeconds(30))
            .GET();

        if (cp.etag != null) {
            firstReq.header("If-None-Match", cp.etag);
        }

        HttpResponse<String> first = http.send(firstReq.build(),
            HttpResponse.BodyHandlers.ofString());

        if (first.statusCode() == 304) {
            Log.LOG.noChanges(repoKey);
            return new EventsCollected(null, List.of());
        }

        if (first.statusCode() != 200) {
            Log.LOG.apiError(first.statusCode(), repoKey);
            return new EventsCollected(null, List.of());
        }

        String nextEtag = first.headers().firstValue("ETag").orElse(null);

        // ── Paginate until we reach an already-seen event ID ──────────────────
        List<ApplicationEvent> events = new ArrayList<>();
        long maxId = cp.lastId;
        boolean done = false;

        HttpResponse<String> page = first;
        while (!done) {
            GitHubEvent[] ghEvents = MAPPER.readValue(page.body(), GitHubEvent[].class);

            for (GitHubEvent ghEvent : ghEvents) {
                long eventId = Long.parseLong(ghEvent.getId());
                if (eventId <= cp.lastId) {
                    done = true;
                    break;
                }
                if (eventId > maxId) maxId = eventId;

                GitHubEventPayload payload = ghEvent.getPayload();
                if (payload == null || payload.getPullRequest() == null) continue;

                GitHubPullRequest pr        = payload.getPullRequest();
                String            action    = payload.getAction();
                Instant           timestamp = ghEvent.getCreatedAt() != null
                    ? Instant.parse(ghEvent.getCreatedAt()) : Instant.now();

                PullRequestEvent prEvent = new PullRequestEvent(
                    ghEvent.getId(), timestamp,
                    ghEvent.getType(),
                    coords.owner(), coords.repo(),
                    pr.getNumber(), pr.getUrl(), action);

                if (!matchesFilters(prEvent, config.filter())) continue;

                Log.LOG.prDetected(pr.getNumber(), repoKey, action,
                    ghEvent.getActor() != null ? ghEvent.getActor().getLogin() : "unknown");
                events.add(prEvent);
            }

            if (done) break;

            Optional<String> nextUrl = nextLink(page);
            if (nextUrl.isEmpty()) break;

            HttpRequest nextReq = HttpRequest.newBuilder()
                .uri(URI.create(nextUrl.get()))
                .header("Authorization", basicAuth(config.apiToken()))
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

            page = http.send(nextReq, HttpResponse.BodyHandlers.ofString());
            if (page.statusCode() != 200) {
                Log.LOG.apiError(page.statusCode(), repoKey);
                break;
            }
        }

        Log.LOG.collectionComplete(events.size(), repoKey);
        return new EventsCollected(new Checkpoint(nextEtag, maxId).serialize(), events);
    }

    private static boolean matchesFilters(PullRequestEvent event, io.setaicompanion.collector.Filters filters) {
        for (Filter filter : filters.all()) {
            String fieldValue = switch (filter.key()) {
                case "eventType" -> event.eventType();
                case "action"    -> event.action();
                default          -> null;
            };
            if (fieldValue == null) continue;
            boolean matches = filter.operator() == FilterOperator.EQ && fieldValue.equalsIgnoreCase(filter.value());
            if (!matches) return false;
        }
        return true;
    }

    private static String basicAuth(String token) {
        return Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
    }

    private Optional<String> nextLink(HttpResponse<?> response) {
        return response.headers().firstValue("Link")
            .flatMap(header -> {
                Matcher m = NEXT_LINK.matcher(header);
                return m.find() ? Optional.of(m.group(1)) : Optional.empty();
            });
    }

    private RepoCoords parseRepoCoords(String url) {
        String[] segments = URI.create(url).getPath().replaceAll("^/+", "").split("/");
        for (int i = 0; i < segments.length - 2; i++) {
            if ("repos".equals(segments[i])) {
                return new RepoCoords(segments[i + 1], segments[i + 2]);
            }
        }
        throw new IllegalArgumentException("URL does not contain /repos/{owner}/{repo}: " + url);
    }

    private record RepoCoords(String owner, String repo) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Checkpoint {
        public String etag;
        public long   lastId;

        Checkpoint() {}

        Checkpoint(String etag, long lastId) {
            this.etag   = etag;
            this.lastId = lastId;
        }

        static Checkpoint parse(String raw) {
            if (raw == null) return new Checkpoint();
            try {
                return MAPPER.readValue(raw, Checkpoint.class);
            } catch (Exception e) {
                Log.LOG.checkpointParseError(e.getMessage());
                return new Checkpoint();
            }
        }

        String serialize() {
            try {
                return MAPPER.writeValueAsString(this);
            } catch (Exception e) {
                Log.LOG.checkpointSerialiseError(e.getMessage());
                return null;
            }
        }
    }
}
