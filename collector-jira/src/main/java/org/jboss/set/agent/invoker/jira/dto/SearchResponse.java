package org.jboss.set.agent.invoker.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchResponse {
    private int total;
    private List<Issue> issues;

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public List<Issue> getIssues() { return issues; }
    public void setIssues(List<Issue> issues) { this.issues = issues; }
}
