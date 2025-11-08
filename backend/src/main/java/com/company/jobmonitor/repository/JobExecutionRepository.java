package com.company.jobmonitor.repository;

import com.company.jobmonitor.entity.Job;
import com.company.jobmonitor.entity.JobExecution;
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
 * Repository interface for JobExecution entity operations.
 *
 * <p>Provides comprehensive data access methods for job execution tracking including:
 *
 * <ul>
 *   <li>Execution history and status tracking
 *   <li>Performance metrics and statistics
 *   <li>Failure analysis and reporting
 *   <li>Execution filtering and search
 *   <li>Real-time execution monitoring
 * </ul>
 *
 * <p>Supports complex queries for analytics and reporting features.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 2.0
 */
@Repository
public interface JobExecutionRepository extends JpaRepository<JobExecution, Integer> {

  /**
   * Finds all executions for a specific job.
   *
   * @param job the job to get executions for
   * @param pageable pagination information
   * @return page of executions for the job
   */
  Page<JobExecution> findByJob(Job job, Pageable pageable);

  /**
   * Finds all executions for a specific job by job ID.
   *
   * @param jobId the ID of the job
   * @param pageable pagination information
   * @return page of executions for the job
   */
  Page<JobExecution> findByJobId(Integer jobId, Pageable pageable);

  /**
   * Finds all executions with a specific status.
   *
   * @param status the execution status to filter by
   * @param pageable pagination information
   * @return page of executions with the specified status
   */
  Page<JobExecution> findByStatus(JobExecution.ExecutionStatus status, Pageable pageable);

  /**
   * Finds all executions triggered by a specific user.
   *
   * @param user the user who triggered the executions
   * @param pageable pagination information
   * @return page of executions triggered by the user
   */
  Page<JobExecution> findByTriggeredBy(User user, Pageable pageable);

  /**
   * Finds all executions with a specific trigger type.
   *
   * @param triggerType the trigger type to filter by
   * @param pageable pagination information
   * @return page of executions with the specified trigger type
   */
  Page<JobExecution> findByTriggerType(JobExecution.TriggerType triggerType, Pageable pageable);

  /**
   * Finds the latest execution for a specific job.
   *
   * @param job the job to get the latest execution for
   * @return optional containing the latest execution
   */
  Optional<JobExecution> findFirstByJobOrderByStartedAtDesc(Job job);

  /**
   * Finds the latest execution for a specific job by job ID.
   *
   * @param jobId the ID of the job
   * @return optional containing the latest execution
   */
  Optional<JobExecution> findFirstByJobIdOrderByStartedAtDesc(Integer jobId);

  /**
   * Finds currently running executions.
   *
   * @return list of executions with RUNNING status
   */
  List<JobExecution> findByStatusOrderByStartedAtDesc(JobExecution.ExecutionStatus status);

  /**
   * Finds running executions for a specific job.
   *
   * @param jobId the ID of the job
   * @return list of running executions for the job
   */
  @Query("SELECT e FROM JobExecution e WHERE e.job.id = :jobId AND e.status = 'RUNNING'")
  List<JobExecution> findRunningExecutionsForJob(@Param("jobId") Integer jobId);

  /**
   * Finds failed executions within a time range.
   *
   * @param startTime the start of the time range
   * @param endTime the end of the time range
   * @param pageable pagination information
   * @return page of failed executions in the time range
   */
  @Query(
      "SELECT e FROM JobExecution e WHERE e.status = 'FAILED' "
          + "AND e.startedAt BETWEEN :startTime AND :endTime "
          + "ORDER BY e.startedAt DESC")
  Page<JobExecution> findFailedExecutionsBetween(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      Pageable pageable);

  /**
   * Finds successful executions within a time range.
   *
   * @param startTime the start of the time range
   * @param endTime the end of the time range
   * @param pageable pagination information
   * @return page of successful executions in the time range
   */
  @Query(
      "SELECT e FROM JobExecution e WHERE e.status = 'SUCCESS' "
          + "AND e.startedAt BETWEEN :startTime AND :endTime "
          + "ORDER BY e.startedAt DESC")
  Page<JobExecution> findSuccessfulExecutionsBetween(
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      Pageable pageable);

  /**
   * Finds executions within a time range for a specific job.
   *
   * @param jobId the ID of the job
   * @param startTime the start of the time range
   * @param endTime the end of the time range
   * @param pageable pagination information
   * @return page of executions for the job in the time range
   */
  Page<JobExecution> findByJobIdAndStartedAtBetween(
      Integer jobId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

  /**
   * Counts executions by status for a specific job.
   *
   * @param jobId the ID of the job
   * @param status the execution status
   * @return number of executions with the specified status for the job
   */
  long countByJobIdAndStatus(Integer jobId, JobExecution.ExecutionStatus status);

  /**
   * Counts executions by status.
   *
   * @param status the execution status
   * @return number of executions with the specified status
   */
  long countByStatus(JobExecution.ExecutionStatus status);

  /**
   * Counts executions for a specific job within a time range.
   *
   * @param jobId the ID of the job
   * @param startTime the start of the time range
   * @param endTime the end of the time range
   * @return number of executions for the job in the time range
   */
  long countByJobIdAndStartedAtBetween(
      Integer jobId, LocalDateTime startTime, LocalDateTime endTime);

  /**
   * Gets average execution duration for a specific job.
   *
   * @param jobId the ID of the job
   * @return average duration in milliseconds
   */
  @Query(
      "SELECT AVG(e.durationMs) FROM JobExecution e WHERE e.job.id = :jobId "
          + "AND e.status = 'SUCCESS' AND e.durationMs IS NOT NULL")
  Optional<Double> getAverageExecutionDuration(@Param("jobId") Integer jobId);

  /**
   * Gets execution statistics for a specific job.
   *
   * @param jobId the ID of the job
   * @return array containing [total, success, failed, avg_duration_ms]
   */
  @Query(
      "SELECT COUNT(e), SUM(CASE WHEN e.status = 'SUCCESS' THEN 1 ELSE 0 END), SUM(CASE WHEN"
          + " e.status = 'FAILED' THEN 1 ELSE 0 END), AVG(CASE WHEN e.status = 'SUCCESS' AND"
          + " e.durationMs IS NOT NULL THEN e.durationMs ELSE NULL END) FROM JobExecution e WHERE"
          + " e.job.id = :jobId")
  Object[] getExecutionStatistics(@Param("jobId") Integer jobId);

  /**
   * Gets recent execution trends for dashboard.
   *
   * @param hours the number of hours to look back
   * @return list of execution counts grouped by hour
   */
  @Query(
      "SELECT "
          + "DATE_FORMAT(e.startedAt, '%Y-%m-%d %H:00:00') as hour, "
          + "COUNT(e) as count, "
          + "SUM(CASE WHEN e.status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, "
          + "SUM(CASE WHEN e.status = 'FAILED' THEN 1 ELSE 0 END) as failed_count "
          + "FROM JobExecution e "
          + "WHERE e.startedAt >= :since "
          + "GROUP BY DATE_FORMAT(e.startedAt, '%Y-%m-%d %H:00:00') "
          + "ORDER BY hour")
  List<Object[]> getExecutionTrends(@Param("since") LocalDateTime since);

  /**
   * Finds the longest running executions.
   *
   * @param pageable pagination information
   * @return page of executions ordered by duration descending
   */
  @Query(
      "SELECT e FROM JobExecution e WHERE e.status = 'SUCCESS' "
          + "AND e.durationMs IS NOT NULL ORDER BY e.durationMs DESC")
  Page<JobExecution> findLongestRunningExecutions(Pageable pageable);

  /**
   * Finds executions that exceeded the timeout.
   *
   * @param pageable pagination information
   * @return page of timed out executions
   */
  Page<JobExecution> findByStatusOrderByStartedAtDesc(
      JobExecution.ExecutionStatus status, Pageable pageable);

  /**
   * Finds recent executions across all jobs.
   *
   * @param pageable pagination information
   * @return page of recent executions
   */
  @Query("SELECT e FROM JobExecution e ORDER BY e.startedAt DESC")
  Page<JobExecution> findRecentExecutions(Pageable pageable);

  /**
   * Complex search for executions with multiple filters.
   *
   * @param jobId the job ID to filter by (can be null)
   * @param status the status to filter by (can be null)
   * @param triggerType the trigger type to filter by (can be null)
   * @param startTime the start time for filtering (can be null)
   * @param endTime the end time for filtering (can be null)
   * @param pageable pagination information
   * @return page of executions matching the search criteria
   */
  @Query(
      "SELECT e FROM JobExecution e WHERE "
          + "(:jobId IS NULL OR e.job.id = :jobId) AND "
          + "(:status IS NULL OR e.status = :status) AND "
          + "(:triggerType IS NULL OR e.triggerType = :triggerType) AND "
          + "(:startTime IS NULL OR e.startedAt >= :startTime) AND "
          + "(:endTime IS NULL OR e.startedAt <= :endTime) "
          + "ORDER BY e.startedAt DESC")
  Page<JobExecution> findExecutionsWithFilters(
      @Param("jobId") Integer jobId,
      @Param("status") JobExecution.ExecutionStatus status,
      @Param("triggerType") JobExecution.TriggerType triggerType,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime,
      Pageable pageable);

  /**
   * Deletes old executions to maintain database performance.
   *
   * @param cutoffDate executions before this date will be deleted
   * @return number of deleted executions
   */
  @Query("DELETE FROM JobExecution e WHERE e.startedAt < :cutoffDate")
  int deleteExecutionsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
}
