package io.setaicompanion.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.setaicompanion.collector.CollectorConfig;
import io.setaicompanion.collector.EventCollector;
import io.setaicompanion.collector.EventsCollected;
import io.setaicompanion.jira.dto.Issue;
import io.setaicompanion.jira.dto.SearchResponse;
import io.setaicompanion.model.ApplicationEvent;
import io.setaicompanion.model.JiraIssueEvent;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class JiraCollector implements EventCollector {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final DateTimeFormatter JQL_FORMAT =
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm").withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter JIRA_UPDATED_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();

    @Override
    public String getType() {
        return "jira";
    }

    @Override
    public List<String> getFilterKeysSupported() {
        return List.of("project");
    }

    @Override
    public String filterHelp() {
        return "project=<KEY>  (e.g. project=JBEAP)";
    }

    @Override
    public EventsCollected collect(CollectorConfig config, String checkpoint) throws Exception {
        String baseUrl = config.url().replaceAll("/+$", "");
        Instant since = parseCheckpoint(checkpoint);
        Instant now   = Instant.now();

        List<String> projects = config.filter().newStringValue("project").multiple().build().get();

        StringBuilder jql = new StringBuilder("updated >= \"" + JQL_FORMAT.format(since) + "\"");
        if (!projects.isEmpty()) {
            jql.append(" AND project in (").append(String.join(",", projects)).append(")");
        }
        jql.append(" AND statusCategory != Done");
        jql.append(" ORDER BY updated ASC");

        String body = MAPPER.writeValueAsString(Map.of(
            "jql",        jql.toString(),
            "fields",     List.of("summary", "status", "priority", "assignee", "reporter", "updated"),
            "maxResults", 100
        ));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/rest/api/3/search/jql"))
            .header("Authorization", buildAuthHeader(config))
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(30))
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Log.LOG.apiError(response.statusCode(), baseUrl);
            return new EventsCollected(null, List.of());
        }

        SearchResponse search = MAPPER.readValue(response.body(), SearchResponse.class);
        if (search.getIssues() == null || search.getIssues().isEmpty()) {
            Log.LOG.noNewIssues();
            return new EventsCollected(now.toString(), List.of());
        }

        List<ApplicationEvent> events = new ArrayList<>();
        for (Issue issue : search.getIssues()) {
            Issue.Fields f = issue.getFields();
            Instant updatedAt = parseUpdated(f.getUpdated());
            events.add(new JiraIssueEvent(
                UUID.randomUUID().toString(),
                updatedAt,
                issue.getKey(),
                f.getSummary(),
                f.getStatus()   != null ? f.getStatus().getName()          : null,
                f.getAssignee() != null ? f.getAssignee().getDisplayName() : null,
                f.getPriority() != null ? f.getPriority().getName()        : null,
                f.getReporter() != null ? f.getReporter().getDisplayName() : null
            ));
            Log.LOG.issueCollected(issue.getKey(), f.getSummary());
        }

        Log.LOG.collectionComplete(events.size(), baseUrl);
        return new EventsCollected(now.toString(), events);
    }

    private Instant parseCheckpoint(String checkpoint) {
        if (checkpoint == null) return Instant.now().minus(24, ChronoUnit.HOURS);
        try {
            return Instant.parse(checkpoint);
        } catch (DateTimeParseException e) {
            return Instant.now().minus(24, ChronoUnit.HOURS);
        }
    }

    private Instant parseUpdated(String updated) {
        if (updated == null) return Instant.now();
        try {
            return JIRA_UPDATED_FORMAT.parse(updated, Instant::from);
        } catch (DateTimeParseException e) {
            Log.LOG.timestampParseError(updated);
            return Instant.now();
        }
    }

    private String buildAuthHeader(CollectorConfig config) {
        if (config.apiToken() != null && !config.apiToken().isBlank()) {
            if (config.user() != null && !config.user().isBlank()) {
                String credentials = config.user() + ":" + config.apiToken();
                return "Basic " + java.util.Base64.getEncoder()
                    .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
            }
            return "Bearer " + config.apiToken();
        }
        if (config.user() != null && config.password() != null) {
            String credentials = config.user() + ":" + config.password();
            return "Basic " + java.util.Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        }
        return "";
    }
}
