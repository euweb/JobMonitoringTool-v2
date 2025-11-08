package com.company.jobmonitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Imported Job Execution from CSV files. Each CSV row represents one execution instance. */
@Entity
@Table(name = "imported_job_executions")
public class ImportedJobExecution {

  @Id
  @Column(name = "execution_id")
  private Long executionId; // CSV ID column

  @Column(name = "job_type", length = 50)
  private String jobType; // CSV Typ column (PE, SCRIPT, UOW)

  @Column(name = "job_name", nullable = false, length = 200)
  private String jobName; // CSV Name column

  @Column(name = "script_path", length = 500)
  private String scriptPath; // CSV Script column

  @Column(name = "priority")
  private Integer priority; // CSV Prio column

  @Column(name = "strategy")
  private Integer strategy; // CSV Strat. column

  @Column(name = "status", length = 20)
  private String status; // CSV Status column (QUEU, DONE, STRT, FAIL)

  @Column(name = "submitted_by", length = 100)
  private String submittedBy; // CSV von column

  @Column(name = "submitted_at")
  private LocalDateTime submittedAt; // CSV am column

  @Column(name = "started_at")
  private LocalDateTime startedAt; // CSV gestartet column

  @Column(name = "ended_at")
  private LocalDateTime endedAt; // CSV beendet column

  @Column(name = "host", length = 100)
  private String host; // CSV auf column

  @Column(name = "parent_execution_id")
  private Long parentExecutionId; // CSV parent column

  @Column(name = "duration_seconds")
  private Long durationSeconds; // CSV Laufzeit (s) column

  @Column(name = "import_timestamp")
  private LocalDateTime importTimestamp; // When this record was imported

  @Column(name = "csv_source_file", length = 500)
  private String csvSourceFile; // Which CSV file this came from

  // Constructors
  public ImportedJobExecution() {
    this.importTimestamp = LocalDateTime.now();
  }

  // Getters and Setters
  public Long getExecutionId() {
    return executionId;
  }

  public void setExecutionId(Long executionId) {
    this.executionId = executionId;
  }

  public String getJobType() {
    return jobType;
  }

  public void setJobType(String jobType) {
    this.jobType = jobType;
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public String getScriptPath() {
    return scriptPath;
  }

  public void setScriptPath(String scriptPath) {
    this.scriptPath = scriptPath;
  }

  public Integer getPriority() {
    return priority;
  }

  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  public Integer getStrategy() {
    return strategy;
  }

  public void setStrategy(Integer strategy) {
    this.strategy = strategy;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getSubmittedBy() {
    return submittedBy;
  }

  public void setSubmittedBy(String submittedBy) {
    this.submittedBy = submittedBy;
  }

  public LocalDateTime getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(LocalDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }

  public LocalDateTime getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(LocalDateTime startedAt) {
    this.startedAt = startedAt;
  }

  public LocalDateTime getEndedAt() {
    return endedAt;
  }

  public void setEndedAt(LocalDateTime endedAt) {
    this.endedAt = endedAt;
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public Long getParentExecutionId() {
    return parentExecutionId;
  }

  public void setParentExecutionId(Long parentExecutionId) {
    this.parentExecutionId = parentExecutionId;
  }

  public Long getDurationSeconds() {
    return durationSeconds;
  }

  public void setDurationSeconds(Long durationSeconds) {
    this.durationSeconds = durationSeconds;
  }

  public LocalDateTime getImportTimestamp() {
    return importTimestamp;
  }

  public void setImportTimestamp(LocalDateTime importTimestamp) {
    this.importTimestamp = importTimestamp;
  }

  public String getCsvSourceFile() {
    return csvSourceFile;
  }

  public void setCsvSourceFile(String csvSourceFile) {
    this.csvSourceFile = csvSourceFile;
  }

  /** Helper method to check if execution is part of a job chain */
  public boolean isPartOfChain() {
    return parentExecutionId != null && parentExecutionId > 0;
  }

  /** Helper method to check if execution is currently running */
  public boolean isRunning() {
    return "STRT".equals(status);
  }

  /** Helper method to check if execution failed */
  public boolean isFailed() {
    return "FAIL".equals(status);
  }

  /** Helper method to check if execution completed successfully */
  public boolean isCompleted() {
    return "DONE".equals(status);
  }

  /** Helper method to check if execution is queued */
  public boolean isQueued() {
    return "QUEU".equals(status);
  }
}
