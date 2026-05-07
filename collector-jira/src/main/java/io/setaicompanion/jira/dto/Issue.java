package io.setaicompanion.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

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
        // pm_ack, dev_ack (devel_ack), qe_ack (qa_ack) — option objects with "value"
        @JsonProperty("customfield_10884") private ValueObject pmAck;
        @JsonProperty("customfield_10887") private ValueObject devAck;
        @JsonProperty("customfield_10883") private ValueObject qeAck;
        // target_release — version object with "name"
        @JsonProperty("customfield_10886") private NamedObject targetRelease;
        // affected versions — standard field, array of version objects with "name"
        private List<NamedObject> versions;

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
        public ValueObject getPmAck() { return pmAck; }
        public void setPmAck(ValueObject pmAck) { this.pmAck = pmAck; }
        public ValueObject getDevAck() { return devAck; }
        public void setDevAck(ValueObject devAck) { this.devAck = devAck; }
        public ValueObject getQeAck() { return qeAck; }
        public void setQeAck(ValueObject qeAck) { this.qeAck = qeAck; }
        public NamedObject getTargetRelease() { return targetRelease; }
        public void setTargetRelease(NamedObject targetRelease) { this.targetRelease = targetRelease; }
        public List<NamedObject> getVersions() { return versions; }
        public void setVersions(List<NamedObject> versions) { this.versions = versions; }
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ValueObject {
        private String value;
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}
