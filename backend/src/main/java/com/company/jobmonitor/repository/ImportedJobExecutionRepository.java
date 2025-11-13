package com.company.jobmonitor.repository;

import com.company.jobmonitor.entity.ImportedJobExecution;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ImportedJobExecutionRepository extends JpaRepository<ImportedJobExecution, Long> {

  /** Find execution by execution ID */
  ImportedJobExecution findByExecutionId(Long executionId);

  /** Find all executions for a specific job name, ordered by execution ID desc */
  List<ImportedJobExecution> findByJobNameOrderByExecutionIdDesc(String jobName);

  /** Find latest execution for each job name */
  @Query(
      "SELECT e FROM ImportedJobExecution e WHERE e.executionId = (SELECT MAX(e2.executionId) FROM"
          + " ImportedJobExecution e2 WHERE e2.jobName = e.jobName)")
  List<ImportedJobExecution> findLatestExecutionPerJob();

  /** Find all distinct job names */
  @Query("SELECT DISTINCT e.jobName FROM ImportedJobExecution e ORDER BY e.jobName")
  List<String> findDistinctJobNames();

  /** Find executions by status */
  List<ImportedJobExecution> findByStatusOrderByExecutionIdDesc(String status);

  /** Find currently running jobs */
  @Query("SELECT e FROM ImportedJobExecution e WHERE e.status = 'STRT' ORDER BY e.startedAt DESC")
  List<ImportedJobExecution> findRunningExecutions();

  /** Find failed jobs in the last 24 hours */
  @Query(
      "SELECT e FROM ImportedJobExecution e WHERE e.status = 'FAIL' AND e.endedAt >= :since ORDER"
          + " BY e.endedAt DESC")
  List<ImportedJobExecution> findRecentFailures(@Param("since") LocalDateTime since);

  /** Find executions that are part of job chains */
  @Query(
      "SELECT e FROM ImportedJobExecution e WHERE e.parentExecutionId IS NOT NULL ORDER BY"
          + " e.executionId DESC")
  List<ImportedJobExecution> findChainedExecutions();

  /** Find child executions for a parent execution ID */
  List<ImportedJobExecution> findByParentExecutionIdOrderByExecutionIdDesc(Long parentExecutionId);

  /** Find executions by job name, status and import timestamp for notifications */
  List<ImportedJobExecution> findByJobNameAndStatusAndImportTimestampAfter(
      String jobName, String status, LocalDateTime after);

  /** Get execution statistics for a job */
  @Query(
      "SELECT COUNT(e), SUM(CASE WHEN e.status = 'DONE' THEN 1 ELSE 0 END), SUM(CASE WHEN e.status"
          + " = 'FAIL' THEN 1 ELSE 0 END), AVG(CASE WHEN e.status = 'DONE' AND e.durationSeconds IS"
          + " NOT NULL THEN e.durationSeconds ELSE NULL END) FROM ImportedJobExecution e WHERE"
          + " e.jobName = :jobName")
  Object[] getJobStatistics(@Param("jobName") String jobName);

  /** Find executions imported after a specific timestamp (for delta processing) */
  List<ImportedJobExecution> findByImportTimestampAfterOrderByExecutionIdDesc(
      LocalDateTime importTimestamp);

  /** Find latest execution timestamp for a job (to track new executions) */
  @Query("SELECT MAX(e.submittedAt) FROM ImportedJobExecution e WHERE e.jobName = :jobName")
  LocalDateTime findLatestSubmissionTime(@Param("jobName") String jobName);

  /** Check if execution ID already exists (prevent duplicates) */
  boolean existsByExecutionId(Long executionId);

  /** Find executions with filtering and pagination support using @Query */
  @Query(
      "SELECT e FROM ImportedJobExecution e WHERE (:jobName IS NULL OR LOWER(e.jobName) LIKE"
          + " LOWER(CONCAT('%', :jobName, '%'))) AND (:status IS NULL OR e.status = :status) AND"
          + " (:jobType IS NULL OR LOWER(e.jobType) LIKE LOWER(CONCAT('%', :jobType, '%'))) AND"
          + " (:host IS NULL OR LOWER(e.host) LIKE LOWER(CONCAT('%', :host, '%'))) AND"
          + " (:submittedBy IS NULL OR LOWER(e.submittedBy) LIKE LOWER(CONCAT('%', :submittedBy,"
          + " '%'))) AND (:submittedAfter IS NULL OR e.submittedAt >= CAST(:submittedAfter AS"
          + " timestamp)) AND (:submittedBefore IS NULL OR e.submittedAt <= CAST(:submittedBefore"
          + " AS timestamp)) AND (:startedAfter IS NULL OR e.startedAt >= CAST(:startedAfter AS"
          + " timestamp)) AND (:startedBefore IS NULL OR e.startedAt <= CAST(:startedBefore AS"
          + " timestamp)) AND (:endedAfter IS NULL OR e.endedAt >= CAST(:endedAfter AS timestamp))"
          + " AND (:endedBefore IS NULL OR e.endedAt <= CAST(:endedBefore AS timestamp)) ORDER BY"
          + " e.executionId DESC")
  org.springframework.data.domain.Page<ImportedJobExecution> findExecutionsWithFilters(
      @Param("jobName") String jobName,
      @Param("status") String status,
      @Param("jobType") String jobType,
      @Param("host") String host,
      @Param("submittedBy") String submittedBy,
      @Param("submittedAfter") String submittedAfter,
      @Param("submittedBefore") String submittedBefore,
      @Param("startedAfter") String startedAfter,
      @Param("startedBefore") String startedBefore,
      @Param("endedAfter") String endedAfter,
      @Param("endedBefore") String endedBefore,
      org.springframework.data.domain.Pageable pageable);

  /** Get distinct values for filter dropdowns */
  @Query(
      "SELECT DISTINCT e.status FROM ImportedJobExecution e WHERE e.status IS NOT NULL ORDER BY"
          + " e.status")
  List<String> findDistinctStatuses();

  @Query(
      "SELECT DISTINCT e.jobType FROM ImportedJobExecution e WHERE e.jobType IS NOT NULL ORDER BY"
          + " e.jobType")
  List<String> findDistinctJobTypes();

  @Query(
      "SELECT DISTINCT e.host FROM ImportedJobExecution e WHERE e.host IS NOT NULL ORDER BY e.host")
  List<String> findDistinctHosts();

  @Query(
      "SELECT DISTINCT e.submittedBy FROM ImportedJobExecution e WHERE e.submittedBy IS NOT NULL"
          + " ORDER BY e.submittedBy")
  List<String> findDistinctSubmitters();
}
