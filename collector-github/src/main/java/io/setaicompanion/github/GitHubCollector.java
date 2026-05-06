package io.setaicompanion.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.setaicompanion.collector.CollectorConfig;
import io.setaicompanion.collector.EventCollector;
import io.setaicompanion.collector.EventsCollected;
import io.setaicompanion.github.dto.GitHubEvent;
import io.setaicompanion.github.dto.GitHubEventPayload;
import io.setaicompanion.github.dto.GitHubPullRequest;
import io.setaicompanion.model.ApplicationEvent;
import io.setaicompanion.model.PullRequestEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class GitHubCollector implements EventCollector {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();

    @Override
    public String getType() {
        return "github";
    }

    @Override
    public EventsCollected collect(CollectorConfig config, String checkpoint) throws Exception {
        String repoApiUrl = config.url().replaceAll("/+$", "");
        RepoCoords coords = parseRepoCoords(repoApiUrl);
        String repoKey = coords.owner() + "/" + coords.repo();

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
            .uri(URI.create(repoApiUrl + "/events?per_page=100"))
            .header("Authorization", "token " + config.apiToken())
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .timeout(Duration.ofSeconds(30))
            .GET();

        if (checkpoint != null) {
            reqBuilder.header("If-None-Match", checkpoint);
        }

        HttpResponse<String> response = http.send(reqBuilder.build(),
            HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 304) {
            Log.LOG.noChanges(repoKey);
            return new EventsCollected(null, List.of());
        }

        if (response.statusCode() != 200) {
            Log.LOG.apiError(response.statusCode(), repoKey);
            return new EventsCollected(null, List.of());
        }

        String nextEtag = response.headers().firstValue("ETag").orElse(null);

        GitHubEvent[] ghEvents = MAPPER.readValue(response.body(), GitHubEvent[].class);
        List<ApplicationEvent> events = new ArrayList<>();

        for (GitHubEvent ghEvent : ghEvents) {
            if (!"PullRequestEvent".equals(ghEvent.getType())) continue;

            GitHubEventPayload payload = ghEvent.getPayload();
            if (payload == null || payload.getPullRequest() == null) continue;

            GitHubPullRequest pr = payload.getPullRequest();
            String author = ghEvent.getActor() != null ? ghEvent.getActor().getLogin() : "unknown";
            Instant timestamp = ghEvent.getCreatedAt() != null
                ? Instant.parse(ghEvent.getCreatedAt())
                : Instant.now();

            Log.LOG.prDetected(pr.getNumber(), repoKey, payload.getAction(), author);
            events.add(new PullRequestEvent(
                ghEvent.getId(),
                timestamp,
                coords.owner(),
                coords.repo(),
                pr.getNumber(),
                pr.getTitle(),
                author,
                pr.getHtmlUrl(),
                pr.getBody()
            ));
        }

        Log.LOG.collectionComplete(events.size(), repoKey);
        return new EventsCollected(nextEtag, events);
    }

    /** Extracts owner and repo from a GitHub-compatible repo API URL containing {@code /repos/{owner}/{repo}}. */
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
}
