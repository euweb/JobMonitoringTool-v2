package com.company.jobmonitor.repository;

import com.company.jobmonitor.entity.Job;
import com.company.jobmonitor.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Job entity operations.
 *
 * <p>Provides comprehensive data access methods for job management including:
 *
 * <ul>
 *   <li>Basic CRUD operations
 *   <li>Job searching and filtering
 *   <li>Status-based queries
 *   <li>Scheduling-related operations
 *   <li>User-specific job queries
 * </ul>
 *
 * <p>All methods support pagination and sorting for efficient data handling in the user interface.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 2.0
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Integer> {

  /**
   * Finds all jobs created by a specific user.
   *
   * @param user the user who created the jobs
   * @param pageable pagination information
   * @return page of jobs created by the user
   */
  Page<Job> findByCreatedBy(User user, Pageable pageable);

  /**
   * Finds all jobs created by a specific user.
   *
   * @param userId the ID of the user who created the jobs
   * @param pageable pagination information
   * @return page of jobs created by the user
   */
  Page<Job> findByCreatedById(Integer userId, Pageable pageable);

  /**
   * Finds all jobs with a specific status.
   *
   * @param status the job status to filter by
   * @param pageable pagination information
   * @return page of jobs with the specified status
   */
  Page<Job> findByStatus(Job.JobStatus status, Pageable pageable);

  /**
   * Finds all jobs with a specific type.
   *
   * @param jobType the job type to filter by
   * @param pageable pagination information
   * @return page of jobs with the specified type
   */
  Page<Job> findByJobType(Job.JobType jobType, Pageable pageable);

  /**
   * Finds all jobs with a specific priority.
   *
   * @param priority the job priority to filter by
   * @param pageable pagination information
   * @return page of jobs with the specified priority
   */
  Page<Job> findByPriority(Job.JobPriority priority, Pageable pageable);

  /**
   * Finds jobs by name containing the specified text (case-insensitive).
   *
   * @param name the text to search for in job names
   * @param pageable pagination information
   * @return page of jobs with names containing the search text
   */
  Page<Job> findByNameContainingIgnoreCase(String name, Pageable pageable);

  /**
   * Finds jobs by description containing the specified text (case-insensitive).
   *
   * @param description the text to search for in job descriptions
   * @param pageable pagination information
   * @return page of jobs with descriptions containing the search text
   */
  Page<Job> findByDescriptionContainingIgnoreCase(String description, Pageable pageable);

  /**
   * Finds jobs that need to be executed (next execution time has passed).
   *
   * @param currentTime the current timestamp
   * @return list of jobs that are due for execution
   */
  @Query(
      "SELECT j FROM Job j WHERE j.status = 'ACTIVE' "
          + "AND j.nextExecutionAt IS NOT NULL "
          + "AND j.nextExecutionAt <= :currentTime")
  List<Job> findJobsDueForExecution(@Param("currentTime") LocalDateTime currentTime);

  /**
   * Finds all active scheduled jobs.
   *
   * @return list of active scheduled jobs
   */
  @Query("SELECT j FROM Job j WHERE j.status = 'ACTIVE' AND j.jobType = 'SCHEDULED'")
  List<Job> findActiveScheduledJobs();

  /**
   * Finds jobs that haven't been executed in the specified time period.
   *
   * @param cutoffTime the cutoff time for last execution
   * @return list of jobs that haven't run recently
   */
  @Query(
      "SELECT j FROM Job j WHERE j.status = 'ACTIVE' "
          + "AND (j.lastExecutedAt IS NULL OR j.lastExecutedAt < :cutoffTime)")
  List<Job> findJobsNotExecutedSince(@Param("cutoffTime") LocalDateTime cutoffTime);

  /**
   * Finds jobs with failed executions in the last specified period.
   *
   * @param since the time period to check for failures
   * @return list of jobs with recent failures
   */
  @Query(
      "SELECT DISTINCT j FROM Job j JOIN j.executions e "
          + "WHERE e.status = 'FAILED' AND e.startedAt >= :since")
  List<Job> findJobsWithRecentFailures(@Param("since") LocalDateTime since);

  /**
   * Finds the most recently created jobs.
   *
   * @param pageable pagination information
   * @return page of recently created jobs
   */
  @Query("SELECT j FROM Job j ORDER BY j.createdAt DESC")
  Page<Job> findRecentJobs(Pageable pageable);

  /**
   * Finds the most recently executed jobs.
   *
   * @param pageable pagination information
   * @return page of recently executed jobs
   */
  @Query("SELECT j FROM Job j WHERE j.lastExecutedAt IS NOT NULL ORDER BY j.lastExecutedAt DESC")
  Page<Job> findRecentlyExecutedJobs(Pageable pageable);

  /**
   * Counts jobs by status.
   *
   * @param status the job status to count
   * @return number of jobs with the specified status
   */
  long countByStatus(Job.JobStatus status);

  /**
   * Counts jobs by type.
   *
   * @param jobType the job type to count
   * @return number of jobs with the specified type
   */
  long countByJobType(Job.JobType jobType);

  /**
   * Counts jobs created by a specific user.
   *
   * @param userId the ID of the user who created the jobs
   * @return number of jobs created by the user
   */
  long countByCreatedById(Integer userId);

  /**
   * Finds jobs created within a specific date range.
   *
   * @param startDate the start of the date range
   * @param endDate the end of the date range
   * @param pageable pagination information
   * @return page of jobs created within the date range
   */
  Page<Job> findByCreatedAtBetween(
      LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

  /**
   * Checks if a job name already exists (case-insensitive).
   *
   * @param name the job name to check
   * @return true if a job with this name exists, false otherwise
   */
  boolean existsByNameIgnoreCase(String name);

  /**
   * Finds a job by name (case-insensitive).
   *
   * @param name the job name to search for
   * @return optional containing the job if found
   */
  Optional<Job> findByNameIgnoreCase(String name);

  /**
   * Complex search query for jobs with multiple filters.
   *
   * @param name the name to search for (can be null)
   * @param status the status to filter by (can be null)
   * @param jobType the job type to filter by (can be null)
   * @param priority the priority to filter by (can be null)
   * @param createdById the creator user ID (can be null)
   * @param pageable pagination information
   * @return page of jobs matching the search criteria
   */
  @Query(
      "SELECT j FROM Job j WHERE "
          + "(:name IS NULL OR LOWER(j.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND "
          + "(:status IS NULL OR j.status = :status) AND "
          + "(:jobType IS NULL OR j.jobType = :jobType) AND "
          + "(:priority IS NULL OR j.priority = :priority) AND "
          + "(:createdById IS NULL OR j.createdBy.id = :createdById)")
  Page<Job> findJobsWithFilters(
      @Param("name") String name,
      @Param("status") Job.JobStatus status,
      @Param("jobType") Job.JobType jobType,
      @Param("priority") Job.JobPriority priority,
      @Param("createdById") Integer createdById,
      Pageable pageable);

  /**
   * Gets job statistics for dashboard.
   *
   * @return array containing [total_jobs, active_jobs, failed_jobs, scheduled_jobs]
   */
  @Query(
      "SELECT COUNT(j), SUM(CASE WHEN j.status = 'ACTIVE' THEN 1 ELSE 0 END), SUM(CASE WHEN EXISTS"
          + " (SELECT 1 FROM JobExecution e WHERE e.job = j AND e.status = 'FAILED' AND e.startedAt"
          + " >= :since) THEN 1 ELSE 0 END), SUM(CASE WHEN j.jobType = 'SCHEDULED' AND j.status ="
          + " 'ACTIVE' THEN 1 ELSE 0 END) FROM Job j")
  Object[] getJobStatistics(@Param("since") LocalDateTime since);
}
