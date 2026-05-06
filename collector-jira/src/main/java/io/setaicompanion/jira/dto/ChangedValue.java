package io.setaicompanion.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangedValue {
    private String fieldName;
    private String changedFrom;
    private String changedTo;

    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }

    public String getChangedFrom() { return changedFrom; }
    public void setChangedFrom(String changedFrom) { this.changedFrom = changedFrom; }

    public String getChangedTo() { return changedTo; }
    public void setChangedTo(String changedTo) { this.changedTo = changedTo; }
}
