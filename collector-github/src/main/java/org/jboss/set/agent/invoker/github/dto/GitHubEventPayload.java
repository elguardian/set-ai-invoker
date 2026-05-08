package org.jboss.set.agent.invoker.github.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubEventPayload {

    private String action;
    private int number;

    @JsonProperty("pull_request")
    private GitHubPullRequest pullRequest;

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public int getNumber() { return number; }
    public void setNumber(int number) { this.number = number; }

    public GitHubPullRequest getPullRequest() { return pullRequest; }
    public void setPullRequest(GitHubPullRequest pullRequest) { this.pullRequest = pullRequest; }
}
