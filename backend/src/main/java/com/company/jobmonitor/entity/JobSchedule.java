package com.company.jobmonitor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Entity representing the scheduling configuration for a job.
 *
 * <p>JobSchedule contains all scheduling-related information including:
 *
 * <ul>
 *   <li>Cron expressions for time-based scheduling
 *   <li>Timezone information for accurate scheduling
 *   <li>Execution windows and constraints
 *   <li>Next execution calculation
 *   <li>Scheduling status and controls
 * </ul>
 *
 * <p>This entity works with the Job entity to provide comprehensive scheduling capabilities for the
 * job monitoring system.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 2.0
 */
@Entity
@Table(name = "job_schedules")
public class JobSchedule {

  /** Schedule status enumeration defining the current state of the schedule. */
  public enum ScheduleStatus {
    /** Schedule is active and will trigger executions */
    ACTIVE,
    /** Schedule is temporarily paused */
    PAUSED,
    /** Schedule is permanently disabled */
    DISABLED,
    /** Schedule has expired and won't trigger anymore */
    EXPIRED
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotNull
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "job_id", nullable = false)
  private Job job;

  @NotNull
  @Column(name = "cron_expression", nullable = false)
  private String cronExpression;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ScheduleStatus status = ScheduleStatus.ACTIVE;

  @Column(name = "timezone")
  private String timezone = "UTC";

  @Column(name = "start_date")
  private LocalDateTime startDate;

  @Column(name = "end_date")
  private LocalDateTime endDate;

  @Column(name = "next_execution_at")
  private LocalDateTime nextExecutionAt;

  @Column(name = "last_execution_at")
  private LocalDateTime lastExecutionAt;

  @Column(name = "execution_count")
  private Integer executionCount = 0;

  @Column(name = "max_executions")
  private Integer maxExecutions;

  @Column(name = "allow_concurrent")
  private Boolean allowConcurrent = false;

  @Column(name = "misfire_policy")
  private String misfirePolicy = "DO_NOTHING";

  @Column(name = "description")
  private String description;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_modified_by")
  private User lastModifiedBy;

  // Constructors
  public JobSchedule() {
    this.createdAt = LocalDateTime.now();
  }

  public JobSchedule(Job job, String cronExpression) {
    this();
    this.job = job;
    this.cronExpression = cronExpression;
  }

  public JobSchedule(Job job, String cronExpression, String timezone) {
    this(job, cronExpression);
    this.timezone = timezone;
  }

  // Lifecycle methods
  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }

  // Business methods

  /**
   * Checks if the schedule is currently active and can trigger executions.
   *
   * @return true if status is ACTIVE and within execution window
   */
  public boolean isActive() {
    if (!ScheduleStatus.ACTIVE.equals(this.status)) {
      return false;
    }

    LocalDateTime now = LocalDateTime.now();

    // Check start date constraint
    if (startDate != null && now.isBefore(startDate)) {
      return false;
    }

    // Check end date constraint
    if (endDate != null && now.isAfter(endDate)) {
      return false;
    }

    // Check max executions constraint
    if (maxExecutions != null && executionCount >= maxExecutions) {
      return false;
    }

    return true;
  }

  /**
   * Checks if the schedule has expired.
   *
   * @return true if the schedule is past its end date or max executions
   */
  public boolean isExpired() {
    LocalDateTime now = LocalDateTime.now();

    // Check end date
    if (endDate != null && now.isAfter(endDate)) {
      return true;
    }

    // Check max executions
    if (maxExecutions != null && executionCount >= maxExecutions) {
      return true;
    }

    return false;
  }

  /**
   * Checks if the schedule is due for execution.
   *
   * @return true if the next execution time has passed
   */
  public boolean isDue() {
    if (nextExecutionAt == null) {
      return false;
    }

    return LocalDateTime.now().isAfter(nextExecutionAt)
        || LocalDateTime.now().isEqual(nextExecutionAt);
  }

  /** Increments the execution count and updates last execution time. */
  public void recordExecution() {
    this.lastExecutionAt = LocalDateTime.now();
    this.executionCount++;
    this.updatedAt = LocalDateTime.now();

    // Check if max executions reached
    if (maxExecutions != null && executionCount >= maxExecutions) {
      this.status = ScheduleStatus.EXPIRED;
    }
  }

  /** Pauses the schedule. */
  public void pause() {
    if (this.status == ScheduleStatus.ACTIVE) {
      this.status = ScheduleStatus.PAUSED;
      this.updatedAt = LocalDateTime.now();
    }
  }

  /** Resumes the schedule from paused state. */
  public void resume() {
    if (this.status == ScheduleStatus.PAUSED) {
      this.status = ScheduleStatus.ACTIVE;
      this.updatedAt = LocalDateTime.now();
    }
  }

  /** Disables the schedule permanently. */
  public void disable() {
    this.status = ScheduleStatus.DISABLED;
    this.updatedAt = LocalDateTime.now();
  }

  /**
   * Gets the timezone as a ZoneId object.
   *
   * @return ZoneId object for the configured timezone
   */
  public ZoneId getZoneId() {
    try {
      return ZoneId.of(timezone);
    } catch (Exception e) {
      return ZoneId.of("UTC");
    }
  }

  /**
   * Gets a human-readable description of the cron expression.
   *
   * @return formatted description of the schedule
   */
  public String getCronDescription() {
    // This could be enhanced with a cron parser library
    // For now, return a basic description
    return "Cron: " + cronExpression + " (" + timezone + ")";
  }

  /**
   * Calculates the remaining executions if max executions is set.
   *
   * @return remaining executions or null if unlimited
   */
  public Integer getRemainingExecutions() {
    if (maxExecutions == null) {
      return null;
    }

    return Math.max(0, maxExecutions - executionCount);
  }

  /**
   * Checks if concurrent executions are allowed.
   *
   * @return true if concurrent executions are permitted
   */
  public boolean allowsConcurrentExecution() {
    return Boolean.TRUE.equals(allowConcurrent);
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

  public String getCronExpression() {
    return cronExpression;
  }

  public void setCronExpression(String cronExpression) {
    this.cronExpression = cronExpression;
  }

  public ScheduleStatus getStatus() {
    return status;
  }

  public void setStatus(ScheduleStatus status) {
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

  public User getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(User createdBy) {
    this.createdBy = createdBy;
  }

  public User getLastModifiedBy() {
    return lastModifiedBy;
  }

  public void setLastModifiedBy(User lastModifiedBy) {
    this.lastModifiedBy = lastModifiedBy;
  }

  @Override
  public String toString() {
    return "JobSchedule{"
        + "id="
        + id
        + ", jobId="
        + (job != null ? job.getId() : null)
        + ", cronExpression='"
        + cronExpression
        + '\''
        + ", status="
        + status
        + ", timezone='"
        + timezone
        + '\''
        + ", nextExecutionAt="
        + nextExecutionAt
        + ", executionCount="
        + executionCount
        + '}';
  }
}
