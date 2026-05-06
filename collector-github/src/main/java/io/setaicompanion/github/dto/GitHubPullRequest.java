package io.setaicompanion.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPullRequest {

    private long id;
    private int number;
    private String title;
    private String body;
    private String state;

    @JsonProperty("html_url")
    private String htmlUrl;

    private GitHubUser user;

    @JsonProperty("updated_at")
    private String updatedAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getHtmlUrl() { return htmlUrl; }
    public void setHtmlUrl(String htmlUrl) { this.htmlUrl = htmlUrl; }

    public GitHubUser getUser() { return user; }
    public void setUser(GitHubUser user) { this.user = user; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
