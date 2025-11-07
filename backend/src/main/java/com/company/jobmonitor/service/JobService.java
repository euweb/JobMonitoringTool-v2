package com.company.jobmonitor.service;

import com.company.jobmonitor.dto.JobDto;
import com.company.jobmonitor.dto.JobExecutionDto;
import com.company.jobmonitor.entity.Job;
import com.company.jobmonitor.entity.JobExecution;
import com.company.jobmonitor.entity.User;
import com.company.jobmonitor.repository.JobExecutionRepository;
import com.company.jobmonitor.repository.JobRepository;
import com.company.jobmonitor.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service class for job management operations.
 *
 * <p>This service provides comprehensive job management functionality including:
 *
 * <ul>
 *   <li>Job creation and configuration
 *   <li>Job execution tracking and monitoring
 *   <li>Job scheduling and status management
 *   <li>Performance analytics and statistics
 *   <li>Job search and filtering operations
 * </ul>
 *
 * <p>All operations are performed within transactional contexts to ensure data consistency. Caching
 * is implemented for frequently accessed job information.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 2.0
 */
@Service
@Transactional
public class JobService {

  private final JobRepository jobRepository;
  private final JobExecutionRepository jobExecutionRepository;
  private final UserRepository userRepository;

  /**
   * Constructs a JobService with the specified dependencies.
   *
   * @param jobRepository the repository for job data access operations
   * @param jobExecutionRepository the repository for job execution data access
   * @param userRepository the repository for user data access operations
   */
  public JobService(
      JobRepository jobRepository,
      JobExecutionRepository jobExecutionRepository,
      UserRepository userRepository) {
    this.jobRepository = jobRepository;
    this.jobExecutionRepository = jobExecutionRepository;
    this.userRepository = userRepository;
  }

  /**
   * Retrieves all jobs with pagination and caching.
   *
   * @param pageable pagination information
   * @return page of jobs as DTOs
   */
  @Cacheable("jobs")
  @Transactional(readOnly = true)
  public Page<JobDto> getAllJobs(Pageable pageable) {
    return jobRepository.findAll(pageable).map(this::convertToJobDtoWithStats);
  }

  /**
   * Retrieves a job by its unique identifier with statistics.
   *
   * @param id the unique identifier of the job
   * @return optional containing the job DTO if found
   */
  @Cacheable(value = "jobs", key = "#id")
  @Transactional(readOnly = true)
  public Optional<JobDto> getJobById(Integer id) {
    return jobRepository.findById(id).map(this::convertToJobDtoWithStats);
  }

  /**
   * Creates a new job.
   *
   * @param jobDto the job data to create
   * @param createdByUsername the username of the user creating the job
   * @return the created job as a DTO
   * @throws RuntimeException if the user is not found or job name already exists
   */
  @CacheEvict(value = "jobs", allEntries = true)
  public JobDto createJob(JobDto jobDto, String createdByUsername) {
    // Check if job name already exists
    if (jobRepository.existsByNameIgnoreCase(jobDto.getName())) {
      throw new RuntimeException("Job with name '" + jobDto.getName() + "' already exists");
    }

    // Find the user creating the job
    User creator =
        userRepository
            .findByUsername(createdByUsername)
            .orElseThrow(() -> new RuntimeException("User not found: " + createdByUsername));

    // Convert DTO to entity
    Job job = jobDto.toEntity();
    job.setCreatedBy(creator);
    job.setLastModifiedBy(creator);

    // Set next execution time if it's a scheduled job
    if (job.isScheduled() && job.getCronExpression() != null) {
      // TODO: Calculate next execution time based on cron expression
      // For now, set to next hour as placeholder
      job.setNextExecutionAt(LocalDateTime.now().plusHours(1));
    }

    // Save the job
    Job savedJob = jobRepository.save(job);
    return convertToJobDtoWithStats(savedJob);
  }

  /**
   * Updates an existing job.
   *
   * @param id the ID of the job to update
   * @param jobDto the updated job data
   * @param modifiedByUsername the username of the user modifying the job
   * @return the updated job as a DTO
   * @throws RuntimeException if the job is not found or name conflict occurs
   */
  @CacheEvict(value = "jobs", allEntries = true)
  public JobDto updateJob(Integer id, JobDto jobDto, String modifiedByUsername) {
    Job job =
        jobRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

    // Check for name conflicts (excluding current job)
    if (!job.getName().equalsIgnoreCase(jobDto.getName())
        && jobRepository.existsByNameIgnoreCase(jobDto.getName())) {
      throw new RuntimeException("Job with name '" + jobDto.getName() + "' already exists");
    }

    // Find the user modifying the job
    User modifier =
        userRepository
            .findByUsername(modifiedByUsername)
            .orElseThrow(() -> new RuntimeException("User not found: " + modifiedByUsername));

    // Update job properties
    job.setName(jobDto.getName());
    job.setDescription(jobDto.getDescription());
    job.setJobType(jobDto.getJobType());
    job.setStatus(jobDto.getStatus());
    job.setPriority(jobDto.getPriority());
    job.setCronExpression(jobDto.getCronExpression());
    job.setCommand(jobDto.getCommand());
    job.setWorkingDirectory(jobDto.getWorkingDirectory());
    job.setEnvironmentVariables(jobDto.getEnvironmentVariables());
    job.setTimeoutMinutes(jobDto.getTimeoutMinutes());
    job.setMaxRetries(jobDto.getMaxRetries());
    job.setRetryDelayMinutes(jobDto.getRetryDelayMinutes());
    job.setNotificationEmail(jobDto.getNotificationEmail());
    job.setNotifyOnSuccess(jobDto.getNotifyOnSuccess());
    job.setNotifyOnFailure(jobDto.getNotifyOnFailure());
    job.setLastModifiedBy(modifier);

    // Update next execution time if cron expression changed
    if (job.isScheduled() && job.getCronExpression() != null) {
      // TODO: Recalculate next execution time
      job.setNextExecutionAt(LocalDateTime.now().plusHours(1));
    }

    Job savedJob = jobRepository.save(job);
    return convertToJobDtoWithStats(savedJob);
  }

  /**
   * Deletes a job by its ID.
   *
   * @param id the ID of the job to delete
   * @throws RuntimeException if the job is not found
   */
  @CacheEvict(value = "jobs", allEntries = true)
  public void deleteJob(Integer id) {
    Job job =
        jobRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Job not found with id: " + id));

    jobRepository.delete(job);
  }

  /**
   * Triggers a manual execution of a job.
   *
   * @param jobId the ID of the job to execute
   * @param triggeredByUsername the username of the user triggering the execution
   * @return the created job execution DTO
   * @throws RuntimeException if the job is not found or not active
   */
  public JobExecutionDto triggerJobExecution(Integer jobId, String triggeredByUsername) {
    Job job =
        jobRepository
            .findById(jobId)
            .orElseThrow(() -> new RuntimeException("Job not found with id: " + jobId));

    if (!job.isActive()) {
      throw new RuntimeException("Cannot execute inactive job: " + job.getName());
    }

    // Find the user triggering the execution
    User triggeredBy =
        userRepository
            .findByUsername(triggeredByUsername)
            .orElseThrow(() -> new RuntimeException("User not found: " + triggeredByUsername));

    // Create new execution record
    JobExecution execution = new JobExecution(job, JobExecution.TriggerType.MANUAL, triggeredBy);
    execution.setExecutionNumber(getNextExecutionNumber(jobId));

    // TODO: Implement actual job execution logic here
    // For now, simulate a quick execution
    execution.start();
    execution.completeSuccess(0, "Manual execution completed successfully");

    // Save execution
    JobExecution savedExecution = jobExecutionRepository.save(execution);

    // Update job's last executed time
    job.setLastExecutedAt(execution.getStartedAt());
    jobRepository.save(job);

    return JobExecutionDto.from(savedExecution);
  }

  /**
   * Gets executions for a specific job with pagination.
   *
   * @param jobId the ID of the job
   * @param pageable pagination information
   * @return page of job execution DTOs
   */
  @Transactional(readOnly = true)
  public Page<JobExecutionDto> getJobExecutions(Integer jobId, Pageable pageable) {
    return jobExecutionRepository.findByJobId(jobId, pageable).map(JobExecutionDto::from);
  }

  /**
   * Gets recent executions across all jobs.
   *
   * @param pageable pagination information
   * @return page of recent job execution DTOs
   */
  @Transactional(readOnly = true)
  public Page<JobExecutionDto> getRecentExecutions(Pageable pageable) {
    return jobExecutionRepository.findRecentExecutions(pageable).map(JobExecutionDto::summary);
  }

  /**
   * Searches jobs with multiple filters.
   *
   * @param name name filter (can be null)
   * @param status status filter (can be null)
   * @param jobType type filter (can be null)
   * @param priority priority filter (can be null)
   * @param createdById creator user ID filter (can be null)
   * @param pageable pagination information
   * @return page of filtered job DTOs
   */
  @Transactional(readOnly = true)
  public Page<JobDto> searchJobs(
      String name,
      Job.JobStatus status,
      Job.JobType jobType,
      Job.JobPriority priority,
      Integer createdById,
      Pageable pageable) {
    return jobRepository
        .findJobsWithFilters(name, status, jobType, priority, createdById, pageable)
        .map(this::convertToJobDtoWithStats);
  }

  /**
   * Gets jobs created by a specific user.
   *
   * @param userId the ID of the user
   * @param pageable pagination information
   * @return page of job DTOs created by the user
   */
  @Transactional(readOnly = true)
  public Page<JobDto> getJobsByUser(Integer userId, Pageable pageable) {
    return jobRepository.findByCreatedById(userId, pageable).map(this::convertToJobDtoWithStats);
  }

  /**
   * Gets dashboard statistics for jobs.
   *
   * @return object array with job statistics
   */
  @Transactional(readOnly = true)
  public Object[] getJobStatistics() {
    LocalDateTime since = LocalDateTime.now().minusDays(30);
    return jobRepository.getJobStatistics(since);
  }

  /**
   * Gets jobs that are due for execution.
   *
   * @return list of jobs that need to be executed
   */
  @Transactional(readOnly = true)
  public List<JobDto> getJobsDueForExecution() {
    LocalDateTime now = LocalDateTime.now();
    return jobRepository.findJobsDueForExecution(now).stream()
        .map(JobDto::from)
        .collect(Collectors.toList());
  }

  /**
   * Converts a Job entity to JobDto with execution statistics.
   *
   * @param job the Job entity to convert
   * @return JobDto with statistics included
   */
  private JobDto convertToJobDtoWithStats(Job job) {
    // Get execution statistics
    Object[] stats = jobExecutionRepository.getExecutionStatistics(job.getId());

    // Handle the case when stats array might be null or empty
    Long totalExecutions = 0L;
    Long successfulExecutions = 0L;
    Long failedExecutions = 0L;
    Double averageDuration = null;

    if (stats != null && stats.length >= 4) {
      totalExecutions = stats[0] != null ? ((Number) stats[0]).longValue() : 0L;
      successfulExecutions = stats[1] != null ? ((Number) stats[1]).longValue() : 0L;
      failedExecutions = stats[2] != null ? ((Number) stats[2]).longValue() : 0L;
      averageDuration = stats[3] != null ? ((Number) stats[3]).doubleValue() : null;
    }

    return JobDto.fromWithStats(
        job, totalExecutions, successfulExecutions, failedExecutions, averageDuration);
  }

  /**
   * Gets the next execution number for a job.
   *
   * @param jobId the ID of the job
   * @return the next execution number
   */
  private Integer getNextExecutionNumber(Integer jobId) {
    return jobExecutionRepository
        .findFirstByJobIdOrderByStartedAtDesc(jobId)
        .map(
            execution ->
                execution.getExecutionNumber() != null ? execution.getExecutionNumber() + 1 : 1)
        .orElse(1);
  }
}
