package com.company.jobmonitor.dto;

import com.company.jobmonitor.entity.Job;
import com.company.jobmonitor.entity.JobExecution;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Job entity.
 *
 * <p>This DTO provides a complete representation of job information for API communication including
 * job configuration, scheduling details, execution statistics, and metadata.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 2.0
 */
@Schema(description = "Job monitoring configuration and metadata")
public class JobDto {

  @Schema(description = "Unique identifier for the job", example = "1")
  private Integer id;

  @Schema(description = "Human-readable name for the job", example = "Daily Backup")
  @NotBlank(message = "Job name is required")
  @Size(max = 100, message = "Job name cannot exceed 100 characters")
  private String name;

  @Schema(
      description = "Detailed description of the job purpose",
      example = "Performs daily backup of critical application data")
  @Size(max = 500, message = "Description cannot exceed 500 characters")
  private String description;

  @Schema(
      description = "Type of job execution",
      example = "SCHEDULED",
      allowableValues = {"SCHEDULED", "ON_DEMAND", "TRIGGERED"})
  @NotNull(message = "Job type is required")
  private Job.JobType jobType;

  @Schema(
      description = "Current status of the job",
      example = "ACTIVE",
      allowableValues = {"ACTIVE", "INACTIVE", "PAUSED", "DELETED"})
  @NotNull(message = "Job status is required")
  private Job.JobStatus status;

  @Schema(
      description = "Execution priority level",
      example = "NORMAL",
      allowableValues = {"LOW", "NORMAL", "HIGH", "CRITICAL"})
  @NotNull(message = "Job priority is required")
  private Job.JobPriority priority;

  @Schema(description = "Cron expression for scheduled jobs", example = "0 2 * * *")
  private String cronExpression;

  @Schema(description = "Command to execute", example = "backup_script.sh")
  private String command;

  @Schema(description = "Working directory for command execution", example = "/opt/backups")
  private String workingDirectory;

  @Schema(
      description = "Environment variables as key=value pairs (JSON format)",
      example = "{\"DATABASE_URL\":\"jdbc:sqlite:data.db\",\"LOG_LEVEL\":\"INFO\"}")
  private String environmentVariables;

  @Schema(description = "Timeout in minutes for job execution", example = "60")
  private Integer timeoutMinutes;

  @Schema(description = "Maximum number of retry attempts", example = "3")
  private Integer maxRetries;

  @Schema(description = "Delay between retry attempts in minutes", example = "5")
  private Integer retryDelayMinutes;

  @Schema(description = "Email address for notifications", example = "admin@company.com")
  private String notificationEmail;

  @Schema(description = "Send notification on successful execution", example = "false")
  private Boolean notifyOnSuccess;

  @Schema(description = "Send notification on failed execution", example = "true")
  private Boolean notifyOnFailure;

  @Schema(description = "User who created this job")
  private UserDto createdBy;

  @Schema(description = "User who last modified this job")
  private UserDto lastModifiedBy;

  @Schema(description = "Timestamp when job was created", example = "2024-01-15T10:30:00")
  private LocalDateTime createdAt;

  @Schema(description = "Timestamp when job was last modified", example = "2024-01-15T15:45:00")
  private LocalDateTime lastModifiedAt;

  @Schema(description = "Timestamp of last execution", example = "2024-01-15T02:00:00")
  private LocalDateTime lastExecutedAt;

  @Schema(description = "Timestamp of next scheduled execution", example = "2024-01-16T02:00:00")
  private LocalDateTime nextExecutionAt;

  @Schema(description = "Recent execution history")
  private List<JobExecutionDto> recentExecutions;

  @Schema(description = "Job scheduling configuration")
  private JobScheduleDto schedule;

  // Execution statistics
  @Schema(description = "Total number of executions", example = "157")
  private Long totalExecutions;

  @Schema(description = "Number of successful executions", example = "143")
  private Long successfulExecutions;

  @Schema(description = "Number of failed executions", example = "14")
  private Long failedExecutions;

  @Schema(description = "Success rate as percentage", example = "91.08")
  private Double successRate;

  @Schema(description = "Average execution duration in milliseconds", example = "45000")
  private Double averageDuration;

  @Schema(description = "Duration of last execution in milliseconds", example = "42000")
  private Integer lastExecutionDuration;

  @Schema(
      description = "Status of last execution",
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
  private JobExecution.ExecutionStatus lastExecutionStatus;

  // Constructors
  public JobDto() {}

  /**
   * Creates a JobDto from a Job entity.
   *
   * @param job the Job entity to convert
   * @return JobDto representation of the job
   */
  public static JobDto from(Job job) {
    JobDto dto = new JobDto();
    dto.setId(job.getId());
    dto.setName(job.getName());
    dto.setDescription(job.getDescription());
    dto.setJobType(job.getJobType());
    dto.setStatus(job.getStatus());
    dto.setPriority(job.getPriority());
    dto.setCronExpression(job.getCronExpression());
    dto.setCommand(job.getCommand());
    dto.setWorkingDirectory(job.getWorkingDirectory());
    dto.setEnvironmentVariables(job.getEnvironmentVariables());
    dto.setTimeoutMinutes(job.getTimeoutMinutes());
    dto.setMaxRetries(job.getMaxRetries());
    dto.setRetryDelayMinutes(job.getRetryDelayMinutes());
    dto.setNotificationEmail(job.getNotificationEmail());
    dto.setNotifyOnSuccess(job.getNotifyOnSuccess());
    dto.setNotifyOnFailure(job.getNotifyOnFailure());
    dto.setCreatedAt(job.getCreatedAt());
    dto.setLastModifiedAt(job.getLastModifiedAt());
    dto.setLastExecutedAt(job.getLastExecutedAt());
    dto.setNextExecutionAt(job.getNextExecutionAt());

    // Set user information
    if (job.getCreatedBy() != null) {
      dto.setCreatedBy(UserDto.from(job.getCreatedBy()));
    }
    if (job.getLastModifiedBy() != null) {
      dto.setLastModifiedBy(UserDto.from(job.getLastModifiedBy()));
    }

    // Set schedule information
    if (job.getSchedule() != null) {
      dto.setSchedule(JobScheduleDto.from(job.getSchedule()));
    }

    return dto;
  }

  /**
   * Creates a JobDto with execution statistics.
   *
   * @param job the Job entity
   * @param totalExecutions total number of executions
   * @param successfulExecutions number of successful executions
   * @param failedExecutions number of failed executions
   * @param averageDuration average execution duration
   * @return JobDto with statistics
   */
  public static JobDto fromWithStats(
      Job job,
      Long totalExecutions,
      Long successfulExecutions,
      Long failedExecutions,
      Double averageDuration) {
    JobDto dto = from(job);
    dto.setTotalExecutions(totalExecutions);
    dto.setSuccessfulExecutions(successfulExecutions);
    dto.setFailedExecutions(failedExecutions);
    dto.setAverageDuration(averageDuration);

    // Calculate success rate
    if (totalExecutions > 0) {
      dto.setSuccessRate(
          (successfulExecutions.doubleValue() / totalExecutions.doubleValue()) * 100.0);
    } else {
      dto.setSuccessRate(0.0);
    }

    // Set last execution information
    JobExecution latestExecution = job.getLatestExecution();
    if (latestExecution != null) {
      dto.setLastExecutionStatus(latestExecution.getStatus());
      dto.setLastExecutionDuration(latestExecution.getDurationMs());
    }

    return dto;
  }

  /**
   * Converts this DTO to a Job entity.
   *
   * @return Job entity representation
   */
  public Job toEntity() {
    Job job = new Job();
    job.setId(this.id);
    job.setName(this.name);
    job.setDescription(this.description);
    job.setJobType(this.jobType);
    job.setStatus(this.status);
    job.setPriority(this.priority);
    job.setCronExpression(this.cronExpression);
    job.setCommand(this.command);
    job.setWorkingDirectory(this.workingDirectory);
    job.setEnvironmentVariables(this.environmentVariables);
    job.setTimeoutMinutes(this.timeoutMinutes);
    job.setMaxRetries(this.maxRetries);
    job.setRetryDelayMinutes(this.retryDelayMinutes);
    job.setNotificationEmail(this.notificationEmail);
    job.setNotifyOnSuccess(this.notifyOnSuccess);
    job.setNotifyOnFailure(this.notifyOnFailure);
    job.setNextExecutionAt(this.nextExecutionAt);

    return job;
  }

  // Getters and Setters
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Job.JobType getJobType() {
    return jobType;
  }

  public void setJobType(Job.JobType jobType) {
    this.jobType = jobType;
  }

  public Job.JobStatus getStatus() {
    return status;
  }

  public void setStatus(Job.JobStatus status) {
    this.status = status;
  }

  public Job.JobPriority getPriority() {
    return priority;
  }

  public void setPriority(Job.JobPriority priority) {
    this.priority = priority;
  }

  public String getCronExpression() {
    return cronExpression;
  }

  public void setCronExpression(String cronExpression) {
    this.cronExpression = cronExpression;
  }

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  public String getEnvironmentVariables() {
    return environmentVariables;
  }

  public void setEnvironmentVariables(String environmentVariables) {
    this.environmentVariables = environmentVariables;
  }

  public Integer getTimeoutMinutes() {
    return timeoutMinutes;
  }

  public void setTimeoutMinutes(Integer timeoutMinutes) {
    this.timeoutMinutes = timeoutMinutes;
  }

  public Integer getMaxRetries() {
    return maxRetries;
  }

  public void setMaxRetries(Integer maxRetries) {
    this.maxRetries = maxRetries;
  }

  public Integer getRetryDelayMinutes() {
    return retryDelayMinutes;
  }

  public void setRetryDelayMinutes(Integer retryDelayMinutes) {
    this.retryDelayMinutes = retryDelayMinutes;
  }

  public String getNotificationEmail() {
    return notificationEmail;
  }

  public void setNotificationEmail(String notificationEmail) {
    this.notificationEmail = notificationEmail;
  }

  public Boolean getNotifyOnSuccess() {
    return notifyOnSuccess;
  }

  public void setNotifyOnSuccess(Boolean notifyOnSuccess) {
    this.notifyOnSuccess = notifyOnSuccess;
  }

  public Boolean getNotifyOnFailure() {
    return notifyOnFailure;
  }

  public void setNotifyOnFailure(Boolean notifyOnFailure) {
    this.notifyOnFailure = notifyOnFailure;
  }

  public UserDto getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(UserDto createdBy) {
    this.createdBy = createdBy;
  }

  public UserDto getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(UserDto lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getLastModifiedAt() {
    return lastModifiedAt;
  }

  public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
    this.lastModifiedAt = lastModifiedAt;
  }

  public LocalDateTime getLastExecutedAt() {
    return lastExecutedAt;
  }

  public void setLastExecutedAt(LocalDateTime lastExecutedAt) {
    this.lastExecutedAt = lastExecutedAt;
  }

  public LocalDateTime getNextExecutionAt() {
    return nextExecutionAt;
  }

  public void setNextExecutionAt(LocalDateTime nextExecutionAt) {
    this.nextExecutionAt = nextExecutionAt;
  }

  public List<JobExecutionDto> getRecentExecutions() {
    return recentExecutions;
  }

  public void setRecentExecutions(List<JobExecutionDto> recentExecutions) {
    this.recentExecutions = recentExecutions;
  }

  public JobScheduleDto getSchedule() {
    return schedule;
  }

  public void setSchedule(JobScheduleDto schedule) {
    this.schedule = schedule;
  }

  public Long getTotalExecutions() {
    return totalExecutions;
  }

  public void setTotalExecutions(Long totalExecutions) {
    this.totalExecutions = totalExecutions;
  }

  public Long getSuccessfulExecutions() {
    return successfulExecutions;
  }

  public void setSuccessfulExecutions(Long successfulExecutions) {
    this.successfulExecutions = successfulExecutions;
  }

  public Long getFailedExecutions() {
    return failedExecutions;
  }

  public void setFailedExecutions(Long failedExecutions) {
    this.failedExecutions = failedExecutions;
  }

  public Double getSuccessRate() {
    return successRate;
  }

  public void setSuccessRate(Double successRate) {
    this.successRate = successRate;
  }

  public Double getAverageDuration() {
    return averageDuration;
  }

  public void setAverageDuration(Double averageDuration) {
    this.averageDuration = averageDuration;
  }

  public Integer getLastExecutionDuration() {
    return lastExecutionDuration;
  }

  public void setLastExecutionDuration(Integer lastExecutionDuration) {
    this.lastExecutionDuration = lastExecutionDuration;
  }

  public JobExecution.ExecutionStatus getLastExecutionStatus() {
    return lastExecutionStatus;
  }

  public void setLastExecutionStatus(JobExecution.ExecutionStatus lastExecutionStatus) {
    this.lastExecutionStatus = lastExecutionStatus;
  }
}
