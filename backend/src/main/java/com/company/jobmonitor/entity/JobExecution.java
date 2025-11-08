package com.company.jobmonitor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Entity representing a single execution instance of a job.
 *
 * <p>JobExecution tracks the complete lifecycle of a job execution including:
 *
 * <ul>
 *   <li>Execution timing (start, end, duration)
 *   <li>Execution status and results
 *   <li>Output and error logs
 *   <li>Exit codes and failure reasons
 *   <li>Resource usage metrics
 * </ul>
 *
 * <p>This entity provides comprehensive auditing and monitoring capabilities for job executions,
 * enabling detailed analysis and troubleshooting.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 2.0
 */
@Entity
@Table(name = "job_executions")
public class JobExecution {

  /** Execution status enumeration defining the current state of job execution. */
  public enum ExecutionStatus {
    /** Execution is queued and waiting to start */
    QUEUED,
    /** Execution is currently running */
    RUNNING,
    /** Execution completed successfully */
    SUCCESS,
    /** Execution failed with errors */
    FAILED,
    /** Execution was cancelled by user or system */
    CANCELLED,
    /** Execution timed out */
    TIMEOUT,
    /** Execution is being retried */
    RETRYING
  }

  /** Trigger type enumeration defining how the execution was initiated. */
  public enum TriggerType {
    /** Triggered by scheduled cron expression */
    SCHEDULED,
    /** Triggered manually by user */
    MANUAL,
    /** Triggered by external system/API call */
    API,
    /** Triggered as a retry attempt */
    RETRY,
    /** Triggered by job dependency completion */
    DEPENDENCY
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id", nullable = false)
  private Job job;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ExecutionStatus status;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "trigger_type", nullable = false)
  private TriggerType triggerType;

  @Column(name = "execution_number")
  private Integer executionNumber;

  @Column(name = "retry_attempt")
  private Integer retryAttempt = 0;

  @NotNull
  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "ended_at")
  private LocalDateTime endedAt;

  @Column(name = "duration_ms")
  private Integer durationMs;

  @Column(name = "exit_code")
  private Integer exitCode;

  @Column(name = "output", columnDefinition = "TEXT")
  private String output;

  @Column(name = "error_output", columnDefinition = "TEXT")
  private String errorOutput;

  @Column(name = "failure_reason")
  private String failureReason;

  @Column(name = "host_name")
  private String hostName;

  @Column(name = "process_id")
  private Integer processId;

  @Column(name = "memory_usage_mb")
  private Double memoryUsageMb;

  @Column(name = "cpu_usage_percent")
  private Double cpuUsagePercent;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "triggered_by")
  private User triggeredBy;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  // Constructors
  public JobExecution() {
    this.createdAt = LocalDateTime.now();
    this.startedAt = LocalDateTime.now();
    this.status = ExecutionStatus.QUEUED;
  }

  public JobExecution(Job job, TriggerType triggerType) {
    this();
    this.job = job;
    this.triggerType = triggerType;
  }

  public JobExecution(Job job, TriggerType triggerType, User triggeredBy) {
    this(job, triggerType);
    this.triggeredBy = triggeredBy;
  }

  // Lifecycle methods
  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Business methods

  /** Starts the execution by setting status to RUNNING and recording start time. */
  public void start() {
    this.status = ExecutionStatus.RUNNING;
    this.startedAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Completes the execution successfully.
   *
   * @param exitCode the exit code of the process
   * @param output the standard output of the execution
   */
  public void completeSuccess(Integer exitCode, String output) {
    this.status = ExecutionStatus.SUCCESS;
    this.endedAt = LocalDateTime.now();
    this.exitCode = exitCode;
    this.output = output;
    this.durationMs = calculateDuration();
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Marks the execution as failed.
   *
   * @param exitCode the exit code of the process
   * @param errorOutput the error output of the execution
   * @param failureReason the reason for failure
   */
  public void completeFailed(Integer exitCode, String errorOutput, String failureReason) {
    this.status = ExecutionStatus.FAILED;
    this.endedAt = LocalDateTime.now();
    this.exitCode = exitCode;
    this.errorOutput = errorOutput;
    this.failureReason = failureReason;
    this.durationMs = calculateDuration();
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Marks the execution as cancelled.
   *
   * @param reason the reason for cancellation
   */
  public void cancel(String reason) {
    this.status = ExecutionStatus.CANCELLED;
    this.endedAt = LocalDateTime.now();
    this.failureReason = reason;
    this.durationMs = calculateDuration();
    this.updatedAt = LocalDateTime.now();
  }

  /** Marks the execution as timed out. */
  public void timeout() {
    this.status = ExecutionStatus.TIMEOUT;
    this.endedAt = LocalDateTime.now();
    this.failureReason = "Execution timed out";
    this.durationMs = calculateDuration();
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Marks the execution for retry.
   *
   * @param retryAttempt the current retry attempt number
   */
  public void retry(Integer retryAttempt) {
    this.status = ExecutionStatus.RETRYING;
    this.retryAttempt = retryAttempt;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Calculates execution duration in milliseconds.
   *
   * @return duration in milliseconds, or null if execution hasn't ended
   */
  private Integer calculateDuration() {
    if (startedAt != null && endedAt != null) {
      return (int) Duration.between(startedAt, endedAt).toMillis();
    }
    return null;
  }

  /**
   * Gets the execution duration as a Duration object.
   *
   * @return Duration object or null if execution hasn't ended
   */
  public Duration getDuration() {
    if (durationMs != null) {
      return Duration.ofMillis(durationMs);
    }
    return calculateDuration() != null ? Duration.ofMillis(calculateDuration()) : null;
  }

  /**
   * Gets a human-readable duration string.
   *
   * @return formatted duration string (e.g., "2m 30s", "1h 15m")
   */
  public String getFormattedDuration() {
    Duration duration = getDuration();
    if (duration == null) {
      return "N/A";
    }

    long hours = duration.toHours();
    long minutes = duration.toMinutesPart();
    long seconds = duration.toSecondsPart();

    if (hours > 0) {
      return String.format("%dh %dm %ds", hours, minutes, seconds);
    } else if (minutes > 0) {
      return String.format("%dm %ds", minutes, seconds);
    } else {
      return String.format("%ds", seconds);
    }
  }

  /**
   * Checks if the execution is currently running.
   *
   * @return true if status is RUNNING, false otherwise
   */
  public boolean isRunning() {
    return ExecutionStatus.RUNNING.equals(this.status);
  }

  /**
   * Checks if the execution has completed (success or failure).
   *
   * @return true if execution has ended, false otherwise
   */
  public boolean isCompleted() {
    return endedAt != null;
  }

  /**
   * Checks if the execution was successful.
   *
   * @return true if status is SUCCESS, false otherwise
   */
  public boolean isSuccessful() {
    return ExecutionStatus.SUCCESS.equals(this.status);
  }

  /**
   * Checks if the execution failed.
   *
   * @return true if status is FAILED, false otherwise
   */
  public boolean isFailed() {
    return ExecutionStatus.FAILED.equals(this.status);
  }

  // Getters and Setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Job getJob() {
    return job;
  }

  public void setJob(Job job) {
    this.job = job;
  }

  public ExecutionStatus getStatus() {
    return status;
  }

  public void setStatus(ExecutionStatus status) {
    this.status = status;
  }

  public TriggerType getTriggerType() {
    return triggerType;
  }

  public void setTriggerType(TriggerType triggerType) {
    this.triggerType = triggerType;
  }

  public Integer getExecutionNumber() {
    return executionNumber;
  }

  public void setExecutionNumber(Integer executionNumber) {
    this.executionNumber = executionNumber;
  }

  public Integer getRetryAttempt() {
    return retryAttempt;
  }

  public void setRetryAttempt(Integer retryAttempt) {
    this.retryAttempt = retryAttempt;
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

  public Integer getDurationMs() {
    return durationMs;
  }

  public void setDurationMs(Integer durationMs) {
    this.durationMs = durationMs;
  }

  public Integer getExitCode() {
    return exitCode;
  }

  public void setExitCode(Integer exitCode) {
    this.exitCode = exitCode;
  }

  public String getOutput() {
    return output;
  }

  public void setOutput(String output) {
    this.output = output;
  }

  public String getErrorOutput() {
    return errorOutput;
  }

  public void setErrorOutput(String errorOutput) {
    this.errorOutput = errorOutput;
  }

  public String getFailureReason() {
    return failureReason;
  }

  public void setFailureReason(String failureReason) {
    this.failureReason = failureReason;
  }

  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  public Integer getProcessId() {
    return processId;
  }

  public void setProcessId(Integer processId) {
    this.processId = processId;
  }

  public Double getMemoryUsageMb() {
    return memoryUsageMb;
  }

  public void setMemoryUsageMb(Double memoryUsageMb) {
    this.memoryUsageMb = memoryUsageMb;
  }

  public Double getCpuUsagePercent() {
    return cpuUsagePercent;
  }

  public void setCpuUsagePercent(Double cpuUsagePercent) {
    this.cpuUsagePercent = cpuUsagePercent;
  }

  public User getTriggeredBy() {
    return triggeredBy;
  }

  public void setTriggeredBy(User triggeredBy) {
    this.triggeredBy = triggeredBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  @Override
  public String toString() {
    return "JobExecution{"
        + "id="
        + id
        + ", jobId="
        + (job != null ? job.getId() : null)
        + ", status="
        + status
        + ", triggerType="
        + triggerType
        + ", startedAt="
        + startedAt
        + ", endedAt="
        + endedAt
        + ", durationMs="
        + durationMs
        + '}';
  }
}
