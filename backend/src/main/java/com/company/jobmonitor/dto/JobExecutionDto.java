package com.company.jobmonitor.dto;

import com.company.jobmonitor.entity.JobExecution;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for JobExecution entity.
 *
 * <p>This DTO represents a single execution instance of a job including timing information,
 * execution results, and performance metrics.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 2.0
 */
@Schema(description = "Job execution instance with results and metrics")
public class JobExecutionDto {

  @Schema(description = "Unique identifier for the execution", example = "1")
  private Integer id;

  @Schema(description = "ID of the associated job", example = "1")
  private Integer jobId;

  @Schema(description = "Name of the associated job", example = "Daily Backup")
  private String jobName;

  @Schema(
      description = "Current execution status",
      example = "SUCCESS",
      allowableValues = {
        "QUEUED",
        "RUNNING",
        "SUCCESS",
        "FAILED",
        "CANCELLED",
        "TIMEOUT",
        "RETRYING"
      })
  private JobExecution.ExecutionStatus status;

  @Schema(
      description = "How the execution was triggered",
      example = "SCHEDULED",
      allowableValues = {"SCHEDULED", "MANUAL", "API", "RETRY", "DEPENDENCY"})
  private JobExecution.TriggerType triggerType;

  @Schema(description = "Sequential execution number for the job", example = "157")
  private Integer executionNumber;

  @Schema(description = "Retry attempt number (0 for first attempt)", example = "0")
  private Integer retryAttempt;

  @Schema(description = "Timestamp when execution started", example = "2024-01-15T02:00:00")
  private LocalDateTime startedAt;

  @Schema(description = "Timestamp when execution ended", example = "2024-01-15T02:15:30")
  private LocalDateTime endedAt;

  @Schema(description = "Execution duration in milliseconds", example = "930000")
  private Integer durationMs;

  @Schema(description = "Human-readable duration", example = "15m 30s")
  private String formattedDuration;

  @Schema(description = "Process exit code", example = "0")
  private Integer exitCode;

  @Schema(description = "Standard output from the execution")
  private String output;

  @Schema(description = "Error output from the execution")
  private String errorOutput;

  @Schema(description = "Reason for failure if execution failed")
  private String failureReason;

  @Schema(description = "Host where the execution ran", example = "server-01")
  private String hostName;

  @Schema(description = "Process ID of the execution", example = "12345")
  private Integer processId;

  @Schema(description = "Memory usage in MB", example = "128.5")
  private Double memoryUsageMb;

  @Schema(description = "CPU usage percentage", example = "15.2")
  private Double cpuUsagePercent;

  @Schema(description = "User who triggered the execution")
  private UserDto triggeredBy;

  @Schema(
      description = "Timestamp when execution record was created",
      example = "2024-01-15T01:59:58")
  private LocalDateTime createdAt;

  @Schema(
      description = "Timestamp when execution record was last updated",
      example = "2024-01-15T02:15:30")
  private LocalDateTime updatedAt;

  // Derived fields for UI convenience
  @Schema(description = "Whether the execution is currently running", example = "false")
  private Boolean isRunning;

  @Schema(description = "Whether the execution has completed", example = "true")
  private Boolean isCompleted;

  @Schema(description = "Whether the execution was successful", example = "true")
  private Boolean isSuccessful;

  @Schema(description = "Whether the execution failed", example = "false")
  private Boolean isFailed;

  // Constructors
  public JobExecutionDto() {}

  /**
   * Creates a JobExecutionDto from a JobExecution entity.
   *
   * @param execution the JobExecution entity to convert
   * @return JobExecutionDto representation of the execution
   */
  public static JobExecutionDto from(JobExecution execution) {
    JobExecutionDto dto = new JobExecutionDto();
    dto.setId(execution.getId());
    dto.setStatus(execution.getStatus());
    dto.setTriggerType(execution.getTriggerType());
    dto.setExecutionNumber(execution.getExecutionNumber());
    dto.setRetryAttempt(execution.getRetryAttempt());
    dto.setStartedAt(execution.getStartedAt());
    dto.setEndedAt(execution.getEndedAt());
    dto.setDurationMs(execution.getDurationMs());
    dto.setFormattedDuration(execution.getFormattedDuration());
    dto.setExitCode(execution.getExitCode());
    dto.setOutput(execution.getOutput());
    dto.setErrorOutput(execution.getErrorOutput());
    dto.setFailureReason(execution.getFailureReason());
    dto.setHostName(execution.getHostName());
    dto.setProcessId(execution.getProcessId());
    dto.setMemoryUsageMb(execution.getMemoryUsageMb());
    dto.setCpuUsagePercent(execution.getCpuUsagePercent());
    dto.setCreatedAt(execution.getCreatedAt());
    dto.setUpdatedAt(execution.getUpdatedAt());

    // Set job information
    if (execution.getJob() != null) {
      dto.setJobId(execution.getJob().getId());
      dto.setJobName(execution.getJob().getName());
    }

    // Set user information
    if (execution.getTriggeredBy() != null) {
      dto.setTriggeredBy(UserDto.from(execution.getTriggeredBy()));
    }

    // Set derived fields
    dto.setIsRunning(execution.isRunning());
    dto.setIsCompleted(execution.isCompleted());
    dto.setIsSuccessful(execution.isSuccessful());
    dto.setIsFailed(execution.isFailed());

    return dto;
  }

  /**
   * Creates a summary DTO with minimal information for list views.
   *
   * @param execution the JobExecution entity
   * @return JobExecutionDto with summary information
   */
  public static JobExecutionDto summary(JobExecution execution) {
    JobExecutionDto dto = new JobExecutionDto();
    dto.setId(execution.getId());
    dto.setStatus(execution.getStatus());
    dto.setTriggerType(execution.getTriggerType());
    dto.setStartedAt(execution.getStartedAt());
    dto.setEndedAt(execution.getEndedAt());
    dto.setDurationMs(execution.getDurationMs());
    dto.setFormattedDuration(execution.getFormattedDuration());
    dto.setExitCode(execution.getExitCode());
    dto.setFailureReason(execution.getFailureReason());

    if (execution.getJob() != null) {
      dto.setJobId(execution.getJob().getId());
      dto.setJobName(execution.getJob().getName());
    }

    // Set derived fields
    dto.setIsRunning(execution.isRunning());
    dto.setIsCompleted(execution.isCompleted());
    dto.setIsSuccessful(execution.isSuccessful());
    dto.setIsFailed(execution.isFailed());

    return dto;
  }

  // Getters and Setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Integer getJobId() {
    return jobId;
  }

  public void setJobId(Integer jobId) {
    this.jobId = jobId;
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public JobExecution.ExecutionStatus getStatus() {
    return status;
  }

  public void setStatus(JobExecution.ExecutionStatus status) {
    this.status = status;
  }

  public JobExecution.TriggerType getTriggerType() {
    return triggerType;
  }

  public void setTriggerType(JobExecution.TriggerType triggerType) {
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

  public String getFormattedDuration() {
    return formattedDuration;
  }

  public void setFormattedDuration(String formattedDuration) {
    this.formattedDuration = formattedDuration;
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

  public UserDto getTriggeredBy() {
    return triggeredBy;
  }

  public void setTriggeredBy(UserDto triggeredBy) {
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

  public Boolean getIsRunning() {
    return isRunning;
  }

  public void setIsRunning(Boolean isRunning) {
    this.isRunning = isRunning;
  }

  public Boolean getIsCompleted() {
    return isCompleted;
  }

  public void setIsCompleted(Boolean isCompleted) {
    this.isCompleted = isCompleted;
  }

  public Boolean getIsSuccessful() {
    return isSuccessful;
  }

  public void setIsSuccessful(Boolean isSuccessful) {
    this.isSuccessful = isSuccessful;
  }

  public Boolean getIsFailed() {
    return isFailed;
  }

  public void setIsFailed(Boolean isFailed) {
    this.isFailed = isFailed;
  }
}
