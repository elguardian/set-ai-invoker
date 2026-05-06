package io.setaicompanion.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditRecord {
    private long id;
    private String summary;
    private String created;
    private String authorKey;
    private String authorAccountId;
    private ObjectItem objectItem;
    private List<ChangedValue> changedValues;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getCreated() { return created; }
    public void setCreated(String created) { this.created = created; }

    public String getAuthorKey() { return authorKey; }
    public void setAuthorKey(String authorKey) { this.authorKey = authorKey; }

    public String getAuthorAccountId() { return authorAccountId; }
    public void setAuthorAccountId(String authorAccountId) { this.authorAccountId = authorAccountId; }

    public ObjectItem getObjectItem() { return objectItem; }
    public void setObjectItem(ObjectItem objectItem) { this.objectItem = objectItem; }

    public List<ChangedValue> getChangedValues() { return changedValues; }
    public void setChangedValues(List<ChangedValue> changedValues) { this.changedValues = changedValues; }

    public String author() {
        return authorKey != null ? authorKey : (authorAccountId != null ? authorAccountId : "unknown");
    }
}
