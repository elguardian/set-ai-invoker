package io.setaicompanion.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Issue {
    private String key;
    private Fields fields;

    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public Fields getFields() { return fields; }
    public void setFields(Fields fields) { this.fields = fields; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        private String summary;
        private String updated;
        private NamedObject status;
        private NamedObject priority;
        private DisplayNameObject assignee;
        private DisplayNameObject reporter;

        public String getSummary() { return summary; }
        public void setSummary(String summary) { this.summary = summary; }
        public String getUpdated() { return updated; }
        public void setUpdated(String updated) { this.updated = updated; }
        public NamedObject getStatus() { return status; }
        public void setStatus(NamedObject status) { this.status = status; }
        public NamedObject getPriority() { return priority; }
        public void setPriority(NamedObject priority) { this.priority = priority; }
        public DisplayNameObject getAssignee() { return assignee; }
        public void setAssignee(DisplayNameObject assignee) { this.assignee = assignee; }
        public DisplayNameObject getReporter() { return reporter; }
        public void setReporter(DisplayNameObject reporter) { this.reporter = reporter; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class NamedObject {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DisplayNameObject {
        private String displayName;
        public String getDisplayName() { return displayName; }
        public void setDisplayName(String displayName) { this.displayName = displayName; }
    }
}
