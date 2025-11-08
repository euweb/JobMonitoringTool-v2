package com.company.jobmonitor.service;

import com.company.jobmonitor.dto.PagedResponse;
import com.company.jobmonitor.entity.ImportedJobExecution;
import com.company.jobmonitor.entity.JobFavorite;
import com.company.jobmonitor.repository.ImportedJobExecutionRepository;
import com.company.jobmonitor.repository.JobFavoriteRepository;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Service for managing imported jobs and job chains */
@Service
public class ImportedJobService {

  private static final Logger logger = LoggerFactory.getLogger(ImportedJobService.class);

  @Autowired private ImportedJobExecutionRepository executionRepository;

  @Autowired private JobFavoriteRepository favoriteRepository;

  /** Get all distinct jobs with their latest execution */
  public Page<Map<String, Object>> getAllJobs(Pageable pageable) {
    List<ImportedJobExecution> latestExecutions = executionRepository.findLatestExecutionPerJob();

    List<Map<String, Object>> jobSummaries =
        latestExecutions.stream()
            .map(this::createJobSummary)
            .sorted((a, b) -> ((String) a.get("jobName")).compareTo((String) b.get("jobName")))
            .collect(Collectors.toList());

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), jobSummaries.size());
    List<Map<String, Object>> pageContent = jobSummaries.subList(start, end);

    return new PageImpl<>(pageContent, pageable, jobSummaries.size());
  }

  /** Get job details with execution history */
  public Map<String, Object> getJobDetails(String jobName) {
    List<ImportedJobExecution> executions =
        executionRepository.findByJobNameOrderByExecutionIdDesc(jobName);

    if (executions.isEmpty()) {
      return null;
    }

    ImportedJobExecution latest = executions.get(0);
    Object[] stats = executionRepository.getJobStatistics(jobName);

    Map<String, Object> jobDetails = new HashMap<>();
    jobDetails.put("jobName", jobName);
    jobDetails.put("jobType", latest.getJobType());
    jobDetails.put("scriptPath", latest.getScriptPath());
    jobDetails.put("latestStatus", latest.getStatus());
    jobDetails.put("latestExecution", latest);
    jobDetails.put("executions", executions);
    jobDetails.put("statistics", parseStatistics(stats));

    // Check for job chains
    List<ImportedJobExecution> childExecutions =
        executionRepository.findByParentExecutionIdOrderByExecutionIdDesc(latest.getExecutionId());
    jobDetails.put("hasChildren", !childExecutions.isEmpty());
    jobDetails.put("childExecutions", childExecutions);

    if (latest.getParentExecutionId() != null) {
      ImportedJobExecution parent =
          executionRepository.findById(latest.getParentExecutionId()).orElse(null);
      jobDetails.put("parentExecution", parent);
    }

    return jobDetails;
  }

  /** Get execution details */
  public ImportedJobExecution getExecutionDetails(Long executionId) {
    return executionRepository.findById(executionId).orElse(null);
  }

  /** Get currently running jobs */
  public List<ImportedJobExecution> getRunningJobs() {
    return executionRepository.findRunningExecutions();
  }

  /** Get recent failures (last 24 hours) */
  public List<ImportedJobExecution> getRecentFailures() {
    LocalDateTime since = LocalDateTime.now().minusHours(24);
    return executionRepository.findRecentFailures(since);
  }

  /** Get job chain for a specific execution */
  public Map<String, Object> getJobChain(Long executionId) {
    ImportedJobExecution execution = executionRepository.findById(executionId).orElse(null);
    if (execution == null) {
      return null;
    }

    Map<String, Object> chain = new HashMap<>();

    // Find root parent
    ImportedJobExecution root = findRootParent(execution);
    chain.put("rootExecution", root);

    // Build chain tree
    chain.put("chainTree", buildChainTree(root));

    return chain;
  }

  /** Add job to favorites */
  public JobFavorite addToFavorites(String jobName, Integer userId) {
    if (favoriteRepository.existsByUserIdAndJobName(userId, jobName)) {
      throw new IllegalArgumentException("Job is already in favorites");
    }

    JobFavorite favorite = new JobFavorite(jobName, userId);
    return favoriteRepository.save(favorite);
  }

  /** Remove job from favorites */
  public void removeFromFavorites(String jobName, Integer userId) {
    favoriteRepository
        .findByUserIdAndJobName(userId, jobName)
        .ifPresent(favoriteRepository::delete);
  }

  /** Get user's favorite jobs */
  public List<Map<String, Object>> getFavoriteJobs(Integer userId) {
    List<JobFavorite> favorites = favoriteRepository.findByUserIdOrderByJobName(userId);

    return favorites.stream()
        .map(
            favorite -> {
              Map<String, Object> favoriteInfo = new HashMap<>();
              favoriteInfo.put("favorite", favorite);

              // Get latest execution for this job
              List<ImportedJobExecution> executions =
                  executionRepository.findByJobNameOrderByExecutionIdDesc(favorite.getJobName());
              if (!executions.isEmpty()) {
                favoriteInfo.put("latestExecution", executions.get(0));
                favoriteInfo.put("jobSummary", createJobSummary(executions.get(0)));
              }

              return favoriteInfo;
            })
        .collect(Collectors.toList());
  }

  /** Check if job is favorited by user */
  public boolean isJobFavorited(String jobName, Integer userId) {
    return favoriteRepository.existsByUserIdAndJobName(userId, jobName);
  }

  /** Update favorite notification settings */
  public JobFavorite updateFavoriteSettings(
      String jobName,
      Integer userId,
      Boolean notifyOnFailure,
      Boolean notifyOnSuccess,
      Boolean notifyOnStart) {
    JobFavorite favorite =
        favoriteRepository
            .findByUserIdAndJobName(userId, jobName)
            .orElseThrow(() -> new IllegalArgumentException("Job is not in favorites"));

    if (notifyOnFailure != null) favorite.setNotifyOnFailure(notifyOnFailure);
    if (notifyOnSuccess != null) favorite.setNotifyOnSuccess(notifyOnSuccess);
    if (notifyOnStart != null) favorite.setNotifyOnStart(notifyOnStart);

    return favoriteRepository.save(favorite);
  }

  // Helper methods

  private Map<String, Object> createJobSummary(ImportedJobExecution execution) {
    Map<String, Object> summary = new HashMap<>();
    summary.put("jobName", execution.getJobName());
    summary.put("jobType", execution.getJobType());
    summary.put("scriptPath", execution.getScriptPath());
    summary.put("latestStatus", execution.getStatus());
    summary.put("latestSubmission", execution.getSubmittedAt());
    summary.put("latestExecution", execution);
    summary.put("isRunning", execution.isRunning());
    summary.put("isFailed", execution.isFailed());
    summary.put("isPartOfChain", execution.isPartOfChain());

    // Get statistics
    Object[] stats = executionRepository.getJobStatistics(execution.getJobName());
    summary.put("statistics", parseStatistics(stats));

    return summary;
  }

  private Map<String, Object> parseStatistics(Object[] stats) {
    Map<String, Object> statistics = new HashMap<>();

    if (stats != null && stats.length >= 4) {
      statistics.put("totalExecutions", stats[0] != null ? ((Number) stats[0]).longValue() : 0L);
      statistics.put(
          "successfulExecutions", stats[1] != null ? ((Number) stats[1]).longValue() : 0L);
      statistics.put("failedExecutions", stats[2] != null ? ((Number) stats[2]).longValue() : 0L);
      statistics.put(
          "averageDurationSeconds", stats[3] != null ? ((Number) stats[3]).doubleValue() : 0.0);
    } else {
      statistics.put("totalExecutions", 0L);
      statistics.put("successfulExecutions", 0L);
      statistics.put("failedExecutions", 0L);
      statistics.put("averageDurationSeconds", 0.0);
    }

    return statistics;
  }

  private ImportedJobExecution findRootParent(ImportedJobExecution execution) {
    if (execution.getParentExecutionId() == null) {
      return execution;
    }

    ImportedJobExecution parent =
        executionRepository.findById(execution.getParentExecutionId()).orElse(null);
    if (parent != null) {
      return findRootParent(parent);
    }

    return execution;
  }

  private List<Map<String, Object>> buildChainTree(ImportedJobExecution parent) {
    List<ImportedJobExecution> children =
        executionRepository.findByParentExecutionIdOrderByExecutionIdDesc(parent.getExecutionId());

    return children.stream()
        .map(
            child -> {
              Map<String, Object> node = new HashMap<>();
              node.put("execution", child);
              node.put("children", buildChainTree(child));
              return node;
            })
        .collect(Collectors.toList());
  }

  /** Get all executions with filtering and pagination */
  public PagedResponse<ImportedJobExecution> getExecutions(
      int page,
      int size,
      String jobName,
      String status,
      String jobType,
      String host,
      String submittedBy) {

    org.springframework.data.domain.Pageable pageable =
        org.springframework.data.domain.PageRequest.of(page, size);

    Page<ImportedJobExecution> pageResult =
        executionRepository.findExecutionsWithFilters(
            jobName, status, jobType, host, submittedBy, pageable);

    return new PagedResponse<>(
        pageResult.getContent(),
        pageResult.getNumber(),
        pageResult.getSize(),
        pageResult.getTotalElements(),
        pageResult.getTotalPages(),
        pageResult.isFirst(),
        pageResult.isLast());
  }

  /** Get filter options for dropdowns */
  public Map<String, List<String>> getFilterOptions() {
    Map<String, List<String>> filters = new HashMap<>();
    filters.put("statuses", executionRepository.findDistinctStatuses());
    filters.put("jobTypes", executionRepository.findDistinctJobTypes());
    filters.put("hosts", executionRepository.findDistinctHosts());
    filters.put("submitters", executionRepository.findDistinctSubmitters());
    return filters;
  }
}
