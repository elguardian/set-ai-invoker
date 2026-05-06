package io.setaicompanion.github;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.setaicompanion.collector.CollectorConfig;
import io.setaicompanion.collector.EventCollector;
import io.setaicompanion.collector.EventsCollected;
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
import java.util.UUID;

public class GitHubCollector implements EventCollector {

    private static final String API_BASE = "https://api.github.com";
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
        RepoCoords coords = parseRepoUrl(config.url());
        String repoKey = coords.owner() + "/" + coords.repo();

        String apiUrl = API_BASE + "/repos/" + repoKey + "/pulls?state=open&per_page=100";

        HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
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

        GitHubPullRequest[] prs = MAPPER.readValue(response.body(), GitHubPullRequest[].class);
        List<ApplicationEvent> events = new ArrayList<>();

        for (GitHubPullRequest pr : prs) {
            PullRequestEvent event = new PullRequestEvent(
                UUID.randomUUID().toString(),
                Instant.now(),
                coords.owner(),
                coords.repo(),
                pr.getNumber(),
                pr.getTitle(),
                pr.getUser() != null ? pr.getUser().getLogin() : "unknown",
                pr.getHtmlUrl(),
                pr.getBody()
            );
            Log.LOG.prDetected(pr.getNumber(), repoKey, pr.getTitle(),
                pr.getUser() != null ? pr.getUser().getLogin() : "unknown");
            events.add(event);
        }

        Log.LOG.collectionComplete(events.size(), repoKey);
        return new EventsCollected(nextEtag, events);
    }

    private RepoCoords parseRepoUrl(String url) {
        String path = url.contains("://") ? URI.create(url).getPath() : "/" + url;
        String[] parts = path.replaceAll("^/+", "").split("/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Cannot parse GitHub repo URL: " + url);
        }
        return new RepoCoords(parts[0], parts[1].replaceAll("\\.git$", ""));
    }

    private record RepoCoords(String owner, String repo) {}
}
