package com.company.jobmonitor.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Job Favorite - marks jobs that the user wants to be notified about */
@Entity
@Table(name = "job_favorites")
public class JobFavorite {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "job_name", nullable = false, length = 200)
  private String jobName;

  @Column(name = "user_id", nullable = false)
  private Integer userId;

  @Column(name = "created_at")
  private LocalDateTime createdAt;

  @Column(name = "notify_on_failure", columnDefinition = "BOOLEAN DEFAULT TRUE")
  private Boolean notifyOnFailure = true;

  @Column(name = "notify_on_success", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private Boolean notifyOnSuccess = false;

  @Column(name = "notify_on_start", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private Boolean notifyOnStart = false;

  @Column(name = "last_notified_execution_id")
  private Long lastNotifiedExecutionId;

  // Constructors
  public JobFavorite() {
    this.createdAt = LocalDateTime.now();
  }

  public JobFavorite(String jobName, Integer userId) {
    this();
    this.jobName = jobName;
    this.userId = userId;
  }

  // Getters and Setters
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Boolean getNotifyOnFailure() {
    return notifyOnFailure;
  }

  public void setNotifyOnFailure(Boolean notifyOnFailure) {
    this.notifyOnFailure = notifyOnFailure;
  }

  public Boolean getNotifyOnSuccess() {
    return notifyOnSuccess;
  }

  public void setNotifyOnSuccess(Boolean notifyOnSuccess) {
    this.notifyOnSuccess = notifyOnSuccess;
  }

  public Boolean getNotifyOnStart() {
    return notifyOnStart;
  }

  public void setNotifyOnStart(Boolean notifyOnStart) {
    this.notifyOnStart = notifyOnStart;
  }

  public Long getLastNotifiedExecutionId() {
    return lastNotifiedExecutionId;
  }

  public void setLastNotifiedExecutionId(Long lastNotifiedExecutionId) {
    this.lastNotifiedExecutionId = lastNotifiedExecutionId;
  }
}
