package io.setaicompanion.jira;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.setaicompanion.collector.CollectorConfig;
import io.setaicompanion.collector.EventCollector;
import io.setaicompanion.collector.EventsCollected;
import io.setaicompanion.jira.dto.AuditRecord;
import io.setaicompanion.jira.dto.AuditRecordsResponse;
import io.setaicompanion.jira.dto.ChangedValue;
import io.setaicompanion.model.ApplicationEvent;
import io.setaicompanion.model.JiraEvent;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class JiraCollector implements EventCollector {

    private static final Set<String> TRACKED_FIELDS = Set.of("pm_ack", "dev_ack", "qe_ack");
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
    public EventsCollected collect(CollectorConfig config, String checkpoint) throws Exception {
        String baseUrl = config.url().replaceAll("/+$", "");

        long sinceMs = parseCheckpoint(checkpoint);

        String since = URLEncoder.encode(String.valueOf(sinceMs), StandardCharsets.UTF_8);
        String apiUrl = baseUrl + "/rest/api/2/auditing/record?since=" + since + "&limit=200";

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(apiUrl))
            .header("Authorization", buildAuthHeader(config))
            .header("Accept", "application/json")
            .timeout(Duration.ofSeconds(30))
            .GET()
            .build();

        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            Log.LOG.apiError(response.statusCode(), baseUrl);
            return new EventsCollected(null, List.of());
        }

        AuditRecordsResponse audit = MAPPER.readValue(response.body(), AuditRecordsResponse.class);
        if (audit.getRecords() == null || audit.getRecords().isEmpty()) {
            Log.LOG.noNewRecords();
            return new EventsCollected(null, List.of());
        }

        List<String> projectKeys = config.filter().newStringValue("project").multiple().build().get();

        List<ApplicationEvent> events = new ArrayList<>();
        long latestMs = sinceMs;

        for (AuditRecord record : audit.getRecords()) {
            long recordMs = parseMs(record.getCreated());
            if (recordMs > latestMs) latestMs = recordMs;
            events.addAll(processRecord(record, projectKeys));
        }

        for (ApplicationEvent event : events) {
            JiraEvent jira = (JiraEvent) event;
            Log.LOG.fieldChangeDetected(jira.issueKey(), jira.fieldName(),
                jira.oldValue(), jira.newValue(), jira.changedBy());
        }

        Log.LOG.collectionComplete(events.size(), baseUrl);
        return new EventsCollected(String.valueOf(latestMs + 1), events);
    }

    private long parseCheckpoint(String checkpoint) {
        if (checkpoint == null) return System.currentTimeMillis() - TimeUnit.HOURS.toMillis(24);
        try {
            return Long.parseLong(checkpoint);
        } catch (NumberFormatException e) {
            return 0L;
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

    private List<ApplicationEvent> processRecord(AuditRecord record, List<String> projectKeys) {
        if (record.getObjectItem() == null || !"Issue".equals(record.getObjectItem().getTypeName())) {
            return List.of();
        }
        if (record.getChangedValues() == null || record.getChangedValues().isEmpty()) {
            return List.of();
        }

        String issueKey = record.getObjectItem().getName();

        if (!projectKeys.isEmpty()) {
            String project = issueKey.contains("-") ? issueKey.split("-")[0] : "";
            if (!projectKeys.contains(project)) {
                return List.of();
            }
        }

        Instant ts = Instant.ofEpochMilli(parseMs(record.getCreated()));
        List<ApplicationEvent> events = new ArrayList<>();

        for (ChangedValue change : record.getChangedValues()) {
            if (!TRACKED_FIELDS.contains(change.getFieldName())) continue;
            events.add(new JiraEvent(
                UUID.randomUUID().toString(),
                ts,
                issueKey,
                change.getFieldName(),
                change.getChangedFrom(),
                change.getChangedTo(),
                record.author()
            ));
        }
        return events;
    }

    private long parseMs(String created) {
        if (created == null) return System.currentTimeMillis();
        try {
            return OffsetDateTime.parse(created).toInstant().toEpochMilli();
        } catch (DateTimeParseException e) {
            Log.LOG.timestampParseError(created);
            return System.currentTimeMillis();
        }
    }
}
