package io.setaicompanion.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuditRecordsResponse {
    private int total;
    private List<AuditRecord> records;

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public List<AuditRecord> getRecords() { return records; }
    public void setRecords(List<AuditRecord> records) { this.records = records; }
}
