package com.company.jobmonitor.dto;

import com.company.jobmonitor.entity.JobSchedule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for JobSchedule entity.
 *
 * <p>This DTO represents the scheduling configuration for a job including cron expressions,
 * timezone settings, execution constraints, and scheduling status.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 2.0
 */
@Schema(description = "Job scheduling configuration")
public class JobScheduleDto {

  @Schema(description = "Unique identifier for the schedule", example = "1")
  private Integer id;

  @Schema(description = "ID of the associated job", example = "1")
  private Integer jobId;

  @Schema(description = "Cron expression for scheduling", example = "0 2 * * *")
  @NotBlank(message = "Cron expression is required")
  private String cronExpression;

  @Schema(
      description = "Human-readable description of the cron schedule",
      example = "Daily at 2:00 AM")
  private String cronDescription;

  @Schema(
      description = "Current schedule status",
      example = "ACTIVE",
      allowableValues = {"ACTIVE", "PAUSED", "DISABLED", "EXPIRED"})
  @NotNull(message = "Schedule status is required")
  private JobSchedule.ScheduleStatus status;

  @Schema(description = "Timezone for the schedule", example = "UTC", defaultValue = "UTC")
  private String timezone;

  @Schema(description = "Start date for the schedule (optional)", example = "2024-01-01T00:00:00")
  private LocalDateTime startDate;

  @Schema(description = "End date for the schedule (optional)", example = "2024-12-31T23:59:59")
  private LocalDateTime endDate;

  @Schema(description = "Next calculated execution time", example = "2024-01-16T02:00:00")
  private LocalDateTime nextExecutionAt;

  @Schema(description = "Last actual execution time", example = "2024-01-15T02:00:00")
  private LocalDateTime lastExecutionAt;

  @Schema(description = "Total number of executions performed", example = "157")
  private Integer executionCount;

  @Schema(description = "Maximum number of executions allowed (optional)", example = "1000")
  private Integer maxExecutions;

  @Schema(description = "Remaining executions if max is set", example = "843")
  private Integer remainingExecutions;

  @Schema(
      description = "Whether concurrent executions are allowed",
      example = "false",
      defaultValue = "false")
  private Boolean allowConcurrent;

  @Schema(
      description = "Policy for handling missed executions",
      example = "DO_NOTHING",
      allowableValues = {"DO_NOTHING", "RUN_IMMEDIATELY", "SKIP_TO_NEXT"})
  private String misfirePolicy;

  @Schema(description = "Description of the schedule purpose")
  private String description;

  @Schema(description = "User who created the schedule")
  private UserDto createdBy;

  @Schema(description = "User who last modified the schedule")
  private UserDto lastModifiedBy;

  @Schema(description = "Timestamp when schedule was created", example = "2024-01-01T10:00:00")
  private LocalDateTime createdAt;

  @Schema(description = "Timestamp when schedule was last updated", example = "2024-01-15T14:30:00")
  private LocalDateTime updatedAt;

  // Derived fields for UI convenience
  @Schema(description = "Whether the schedule is currently active", example = "true")
  private Boolean isActive;

  @Schema(description = "Whether the schedule has expired", example = "false")
  private Boolean isExpired;

  @Schema(description = "Whether the schedule is due for execution", example = "true")
  private Boolean isDue;

  // Constructors
  public JobScheduleDto() {}

  /**
   * Creates a JobScheduleDto from a JobSchedule entity.
   *
   * @param schedule the JobSchedule entity to convert
   * @return JobScheduleDto representation of the schedule
   */
  public static JobScheduleDto from(JobSchedule schedule) {
    JobScheduleDto dto = new JobScheduleDto();
    dto.setId(schedule.getId());
    dto.setCronExpression(schedule.getCronExpression());
    dto.setCronDescription(schedule.getCronDescription());
    dto.setStatus(schedule.getStatus());
    dto.setTimezone(schedule.getTimezone());
    dto.setStartDate(schedule.getStartDate());
    dto.setEndDate(schedule.getEndDate());
    dto.setNextExecutionAt(schedule.getNextExecutionAt());
    dto.setLastExecutionAt(schedule.getLastExecutionAt());
    dto.setExecutionCount(schedule.getExecutionCount());
    dto.setMaxExecutions(schedule.getMaxExecutions());
    dto.setRemainingExecutions(schedule.getRemainingExecutions());
    dto.setAllowConcurrent(schedule.getAllowConcurrent());
    dto.setMisfirePolicy(schedule.getMisfirePolicy());
    dto.setDescription(schedule.getDescription());
    dto.setCreatedAt(schedule.getCreatedAt());
    dto.setUpdatedAt(schedule.getUpdatedAt());

    // Set job information
    if (schedule.getJob() != null) {
      dto.setJobId(schedule.getJob().getId());
    }

    // Set user information
    if (schedule.getCreatedBy() != null) {
      dto.setCreatedBy(UserDto.from(schedule.getCreatedBy()));
    }
    if (schedule.getLastModifiedBy() != null) {
      dto.setLastModifiedBy(UserDto.from(schedule.getLastModifiedBy()));
    }

    // Set derived fields
    dto.setIsActive(schedule.isActive());
    dto.setIsExpired(schedule.isExpired());
    dto.setIsDue(schedule.isDue());

    return dto;
  }

  /**
   * Creates a summary DTO with minimal information.
   *
   * @param schedule the JobSchedule entity
   * @return JobScheduleDto with summary information
   */
  public static JobScheduleDto summary(JobSchedule schedule) {
    JobScheduleDto dto = new JobScheduleDto();
    dto.setId(schedule.getId());
    dto.setCronExpression(schedule.getCronExpression());
    dto.setCronDescription(schedule.getCronDescription());
    dto.setStatus(schedule.getStatus());
    dto.setTimezone(schedule.getTimezone());
    dto.setNextExecutionAt(schedule.getNextExecutionAt());
    dto.setExecutionCount(schedule.getExecutionCount());
    dto.setIsActive(schedule.isActive());
    dto.setIsDue(schedule.isDue());

    if (schedule.getJob() != null) {
      dto.setJobId(schedule.getJob().getId());
    }

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

  public String getCronExpression() {
    return cronExpression;
  }

  public void setCronExpression(String cronExpression) {
    this.cronExpression = cronExpression;
  }

  public String getCronDescription() {
    return cronDescription;
  }

  public void setCronDescription(String cronDescription) {
    this.cronDescription = cronDescription;
  }

  public JobSchedule.ScheduleStatus getStatus() {
    return status;
  }

  public void setStatus(JobSchedule.ScheduleStatus status) {
    this.status = status;
  }

  public String getTimezone() {
    return timezone;
  }

  public void setTimezone(String timezone) {
    this.timezone = timezone;
  }

  public LocalDateTime getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDateTime startDate) {
    this.startDate = startDate;
  }

  public LocalDateTime getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDateTime endDate) {
    this.endDate = endDate;
  }

  public LocalDateTime getNextExecutionAt() {
    return nextExecutionAt;
  }

  public void setNextExecutionAt(LocalDateTime nextExecutionAt) {
    this.nextExecutionAt = nextExecutionAt;
  }

  public LocalDateTime getLastExecutionAt() {
    return lastExecutionAt;
  }

  public void setLastExecutionAt(LocalDateTime lastExecutionAt) {
    this.lastExecutionAt = lastExecutionAt;
  }

  public Integer getExecutionCount() {
    return executionCount;
  }

  public void setExecutionCount(Integer executionCount) {
    this.executionCount = executionCount;
  }

  public Integer getMaxExecutions() {
    return maxExecutions;
  }

  public void setMaxExecutions(Integer maxExecutions) {
    this.maxExecutions = maxExecutions;
  }

  public Integer getRemainingExecutions() {
    return remainingExecutions;
  }

  public void setRemainingExecutions(Integer remainingExecutions) {
    this.remainingExecutions = remainingExecutions;
  }

  public Boolean getAllowConcurrent() {
    return allowConcurrent;
  }

  public void setAllowConcurrent(Boolean allowConcurrent) {
    this.allowConcurrent = allowConcurrent;
  }

  public String getMisfirePolicy() {
    return misfirePolicy;
  }

  public void setMisfirePolicy(String misfirePolicy) {
    this.misfirePolicy = misfirePolicy;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
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

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Boolean getIsActive() {
    return isActive;
  }

  public void setIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  public Boolean getIsExpired() {
    return isExpired;
  }

  public void setIsExpired(Boolean isExpired) {
    this.isExpired = isExpired;
  }

  public Boolean getIsDue() {
    return isDue;
  }

  public void setIsDue(Boolean isDue) {
    this.isDue = isDue;
  }
}
