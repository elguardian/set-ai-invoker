package org.jboss.set.agent.invoker.gh;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;

/**
 * Minimal GitHub Contents API client (read + create/update a single file).
 */
final class GithubApi {

    private static final String API_BASE = "https://api.github.com";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(30))
        .build();

    private final String token;

    GithubApi() {
        this.token = System.getenv("GITHUB_TOKEN");
    }

    /** Parsed coordinates from a github.com URI. */
    record Coords(String owner, String repo, String branch, String path) {
        static Coords from(URI uri) {
            // Accepts: https://github.com/owner/repo/blob/branch/path...
            //      or: https://github.com/owner/repo/path...  (branch defaults to main)
            String[] parts = uri.getPath().replaceFirst("^/", "").split("/", -1);
            if (parts.length < 3) {
                throw new IllegalArgumentException("Cannot parse GitHub URI: " + uri);
            }
            String owner = parts[0];
            String repo  = parts[1];
            String branch;
            String filePath;
            if (parts.length > 3 && "blob".equals(parts[2])) {
                branch   = parts[3];
                filePath = String.join("/", Arrays.copyOfRange(parts, 4, parts.length));
            } else {
                branch   = "main";
                filePath = String.join("/", Arrays.copyOfRange(parts, 2, parts.length));
            }
            return new Coords(owner, repo, branch, filePath);
        }
    }

    /** Result of a file fetch: decoded content + blob SHA (null if file is new). */
    record FileContent(byte[] content, String sha) {}

    /**
     * Downloads the file at {@code coords}. Returns {@code null} if the file does not yet exist.
     */
    FileContent get(Coords coords) throws Exception {
        String url = API_BASE + "/repos/" + coords.owner() + "/" + coords.repo()
            + "/contents/" + coords.path() + "?ref=" + coords.branch();

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "token " + token)
            .header("Accept", "application/vnd.github+json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .timeout(Duration.ofSeconds(30))
            .GET()
            .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() == 404) return null;
        if (resp.statusCode() != 200) {
            throw new RuntimeException("GitHub GET returned " + resp.statusCode() + " for " + url);
        }

        JsonNode node = MAPPER.readTree(resp.body());
        String encoded = node.get("content").asText().replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(encoded);
        String sha     = node.get("sha").asText();
        return new FileContent(decoded, sha);
    }

    /**
     * Creates or updates the file at {@code coords} with {@code content}.
     * Pass {@code currentSha = null} to create a new file.
     */
    void put(Coords coords, byte[] content, String currentSha, String commitMessage) throws Exception {
        String url = API_BASE + "/repos/" + coords.owner() + "/" + coords.repo()
            + "/contents/" + coords.path();

        ObjectNode body = MAPPER.createObjectNode();
        body.put("message", commitMessage);
        body.put("content", Base64.getEncoder().encodeToString(content));
        body.put("branch", coords.branch());
        if (currentSha != null) {
            body.put("sha", currentSha);
        }

        HttpRequest req = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Authorization", "token " + token)
            .header("Accept", "application/vnd.github+json")
            .header("Content-Type", "application/json")
            .header("X-GitHub-Api-Version", "2022-11-28")
            .timeout(Duration.ofSeconds(30))
            .PUT(HttpRequest.BodyPublishers.ofString(
                MAPPER.writeValueAsString(body), StandardCharsets.UTF_8))
            .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());

        if (resp.statusCode() != 200 && resp.statusCode() != 201) {
            throw new RuntimeException(
                "GitHub PUT returned " + resp.statusCode() + " for " + url
                + ": " + resp.body());
        }
    }
}
