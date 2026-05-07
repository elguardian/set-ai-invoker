package io.setaicompanion.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubPullRequest {

    private int number;
    private String title;
    private String body;

    private String url;

    private GitHubUser user;

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public GitHubUser getUser() { return user; }
    public void setUser(GitHubUser user) { this.user = user; }
}
