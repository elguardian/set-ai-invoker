package org.jboss.set.agent.invoker.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubEvent {

    private String id;
    private String type;
    private GitHubUser actor;
    private GitHubEventPayload payload;

    @JsonProperty("created_at")
    private String createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public GitHubUser getActor() { return actor; }
    public void setActor(GitHubUser actor) { this.actor = actor; }

    public GitHubEventPayload getPayload() { return payload; }
    public void setPayload(GitHubEventPayload payload) { this.payload = payload; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
