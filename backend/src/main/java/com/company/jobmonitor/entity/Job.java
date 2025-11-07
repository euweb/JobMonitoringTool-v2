package com.company.jobmonitor.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing a monitored job in the system.
 *
 * <p>A Job represents a scheduled or on-demand task that needs to be monitored. It contains
 * configuration information, scheduling details, and maintains a history of all executions through
 * the JobExecution relationship.
 *
 * <p>Jobs can have different types (SCHEDULED, ON_DEMAND, TRIGGERED) and statuses (ACTIVE,
 * INACTIVE, PAUSED) to support various monitoring scenarios.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 2.0
 */
@Entity
@Table(name = "job_monitor_jobs")
public class Job {

  /** Job type enumeration defining how the job is triggered. */
  public enum JobType {
    /** Job runs on a schedule (cron expression) */
    SCHEDULED,
    /** Job runs manually on demand */
    ON_DEMAND,
    /** Job runs based on external triggers */
    TRIGGERED
  }

  /** Job status enumeration defining the current state of the job. */
  public enum JobStatus {
    /** Job is active and can be executed */
    ACTIVE,
    /** Job is inactive and won't be executed */
    INACTIVE,
    /** Job is temporarily paused */
    PAUSED,
    /** Job has been deleted (soft delete) */
    DELETED
  }

  /** Job priority levels for execution ordering. */
  public enum JobPriority {
    LOW(1),
    NORMAL(5),
    HIGH(8),
    CRITICAL(10);

    private final int level;

    JobPriority(int level) {
      this.level = level;
    }

    public int getLevel() {
      return level;
    }
  }

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @NotBlank(message = "Job name is required")
  @Size(max = 100, message = "Job name cannot exceed 100 characters")
  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Size(max = 500, message = "Description cannot exceed 500 characters")
  @Column(name = "description", length = 500)
  private String description;

  @NotNull(message = "Job type is required")
  @Enumerated(EnumType.STRING)
  @Column(name = "job_type", nullable = false)
  private JobType jobType;

  @NotNull(message = "Job status is required")
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private JobStatus status = JobStatus.ACTIVE;

  @NotNull(message = "Job priority is required")
  @Enumerated(EnumType.STRING)
  @Column(name = "priority", nullable = false)
  private JobPriority priority = JobPriority.NORMAL;

  @Column(name = "cron_expression")
  private String cronExpression;

  @Column(name = "command", length = 1000)
  private String command;

  @Column(name = "working_directory")
  private String workingDirectory;

  @Column(name = "environment_variables", columnDefinition = "TEXT")
  private String environmentVariables;

  @Column(name = "timeout_minutes")
  private Integer timeoutMinutes;

  @Column(name = "max_retries")
  private Integer maxRetries = 0;

  @Column(name = "retry_delay_minutes")
  private Integer retryDelayMinutes = 5;

  @Column(name = "notification_email")
  private String notificationEmail;

  @Column(name = "notify_on_success")
  private Boolean notifyOnSuccess = false;

  @Column(name = "notify_on_failure")
  private Boolean notifyOnFailure = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by")
  private User createdBy;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "last_modified_by")
  private User lastModifiedBy;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "last_modified_at")
  private LocalDateTime lastModifiedAt;

  @Column(name = "last_executed_at")
  private LocalDateTime lastExecutedAt;

  @Column(name = "next_execution_at")
  private LocalDateTime nextExecutionAt;

  @OneToMany(
      mappedBy = "job",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<JobExecution> executions = new ArrayList<>();

  @OneToOne(
      mappedBy = "job",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private JobSchedule schedule;

  // Constructors
  public Job() {
    this.createdAt = LocalDateTime.now();
  }

  public Job(String name, String description, JobType jobType) {
    this();
    this.name = name;
    this.description = description;
    this.jobType = jobType;
  }

  // Lifecycle methods
  @PrePersist
  protected void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
    lastModifiedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    lastModifiedAt = LocalDateTime.now();
  }

  // Business methods

  /**
   * Checks if the job is currently active and can be executed.
   *
   * @return true if the job status is ACTIVE, false otherwise
   */
  public boolean isActive() {
    return JobStatus.ACTIVE.equals(this.status);
  }

  /**
   * Checks if the job is scheduled type.
   *
   * @return true if job type is SCHEDULED, false otherwise
   */
  public boolean isScheduled() {
    return JobType.SCHEDULED.equals(this.jobType);
  }

  /**
   * Checks if the job has retry configuration.
   *
   * @return true if maxRetries is greater than 0, false otherwise
   */
  public boolean hasRetryConfiguration() {
    return maxRetries != null && maxRetries > 0;
  }

  /**
   * Adds a job execution to the history.
   *
   * @param execution the job execution to add
   */
  public void addExecution(JobExecution execution) {
    executions.add(execution);
    execution.setJob(this);
    this.lastExecutedAt = LocalDateTime.now();
  }

  /**
   * Gets the most recent job execution.
   *
   * @return the latest job execution or null if no executions exist
   */
  public JobExecution getLatestExecution() {
    return executions.stream()
        .max((e1, e2) -> e1.getStartedAt().compareTo(e2.getStartedAt()))
        .orElse(null);
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

  public JobType getJobType() {
    return jobType;
  }

  public void setJobType(JobType jobType) {
    this.jobType = jobType;
  }

  public JobStatus getStatus() {
    return status;
  }

  public void setStatus(JobStatus status) {
    this.status = status;
  }

  public JobPriority getPriority() {
    return priority;
  }

  public void setPriority(JobPriority priority) {
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

  public List<JobExecution> getExecutions() {
    return executions;
  }

  public void setExecutions(List<JobExecution> executions) {
    this.executions = executions;
  }

  public JobSchedule getSchedule() {
    return schedule;
  }

  public void setSchedule(JobSchedule schedule) {
    this.schedule = schedule;
    if (schedule != null) {
      schedule.setJob(this);
    }
  }

  @Override
  public String toString() {
    return "Job{"
        + "id="
        + id
        + ", name='"
        + name
        + '\''
        + ", jobType="
        + jobType
        + ", status="
        + status
        + ", priority="
        + priority
        + ", createdAt="
        + createdAt
        + '}';
  }
}
