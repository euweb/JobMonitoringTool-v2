package com.company.jobmonitor.controller;

import com.company.jobmonitor.dto.PagedResponse;
import com.company.jobmonitor.entity.ImportedJobExecution;
import com.company.jobmonitor.entity.JobFavorite;
import com.company.jobmonitor.security.UserPrincipal;
import com.company.jobmonitor.service.CsvImportService;
import com.company.jobmonitor.service.ImportedJobService;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Controller for managing imported jobs and executions */
@RestController
@RequestMapping("/api/imported-jobs")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class ImportedJobController {

  @Autowired private ImportedJobService importedJobService;

  @Autowired private CsvImportService csvImportService;

  /** Get all jobs with pagination */
  @GetMapping
  public ResponseEntity<Page<Map<String, Object>>> getAllJobs(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<Map<String, Object>> jobs = importedJobService.getAllJobs(pageable);
    return ResponseEntity.ok(jobs);
  }

  /** Get job details by name */
  @GetMapping("/{jobName}")
  public ResponseEntity<Map<String, Object>> getJobDetails(@PathVariable String jobName) {
    Map<String, Object> jobDetails = importedJobService.getJobDetails(jobName);

    if (jobDetails == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(jobDetails);
  }

  /** Get all executions with filtering and pagination */
  @GetMapping("/executions")
  public ResponseEntity<PagedResponse<ImportedJobExecution>> getExecutions(
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size,
      @RequestParam(required = false) String jobName,
      @RequestParam(required = false) String status,
      @RequestParam(required = false) String jobType,
      @RequestParam(required = false) String host,
      @RequestParam(required = false) String submittedBy) {

    PagedResponse<ImportedJobExecution> executions =
        importedJobService.getExecutions(page, size, jobName, status, jobType, host, submittedBy);

    return ResponseEntity.ok(executions);
  }

  /** Get filter options for dropdowns */
  @GetMapping("/executions/filters")
  public ResponseEntity<Map<String, List<String>>> getFilterOptions() {
    Map<String, List<String>> filters = importedJobService.getFilterOptions();
    return ResponseEntity.ok(filters);
  }

  /** Get execution details by ID */
  @GetMapping("/executions/{executionId}")
  public ResponseEntity<ImportedJobExecution> getExecutionDetails(@PathVariable Long executionId) {
    ImportedJobExecution execution = importedJobService.getExecutionDetails(executionId);

    if (execution == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(execution);
  }

  /** Get currently running jobs */
  @GetMapping("/running")
  public ResponseEntity<List<ImportedJobExecution>> getRunningJobs() {
    List<ImportedJobExecution> runningJobs = importedJobService.getRunningJobs();
    return ResponseEntity.ok(runningJobs);
  }

  /** Get recent failures */
  @GetMapping("/failures")
  public ResponseEntity<List<ImportedJobExecution>> getRecentFailures() {
    List<ImportedJobExecution> failures = importedJobService.getRecentFailures();
    return ResponseEntity.ok(failures);
  }

  /** Get job chain for an execution */
  @GetMapping("/executions/{executionId}/chain")
  public ResponseEntity<Map<String, Object>> getJobChain(@PathVariable Long executionId) {
    Map<String, Object> chain = importedJobService.getJobChain(executionId);

    if (chain == null) {
      return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(chain);
  }

  /** Add job to favorites */
  @PostMapping("/{jobName}/favorite")
  public ResponseEntity<JobFavorite> addToFavorites(
      @PathVariable String jobName, @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      JobFavorite favorite = importedJobService.addToFavorites(jobName, userPrincipal.getId());
      return ResponseEntity.ok(favorite);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /** Remove job from favorites */
  @DeleteMapping("/{jobName}/favorite")
  public ResponseEntity<Void> removeFromFavorites(
      @PathVariable String jobName, @AuthenticationPrincipal UserPrincipal userPrincipal) {

    importedJobService.removeFromFavorites(jobName, userPrincipal.getId());
    return ResponseEntity.ok().build();
  }

  /** Get user's favorite jobs */
  @GetMapping("/favorites")
  public ResponseEntity<List<Map<String, Object>>> getFavoriteJobs(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {
    List<Map<String, Object>> favorites = importedJobService.getFavoriteJobs(userPrincipal.getId());
    return ResponseEntity.ok(favorites);
  }

  /** Check if job is favorited */
  @GetMapping("/{jobName}/favorite/status")
  public ResponseEntity<Map<String, Boolean>> getFavoriteStatus(
      @PathVariable String jobName, @AuthenticationPrincipal UserPrincipal userPrincipal) {

    boolean isFavorited = importedJobService.isJobFavorited(jobName, userPrincipal.getId());
    return ResponseEntity.ok(Map.of("isFavorited", isFavorited));
  }

  /** Update favorite notification settings */
  @PutMapping("/{jobName}/favorite/settings")
  public ResponseEntity<JobFavorite> updateFavoriteSettings(
      @PathVariable String jobName,
      @RequestBody Map<String, Boolean> settings,
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    try {
      JobFavorite updatedFavorite =
          importedJobService.updateFavoriteSettings(
              jobName,
              userPrincipal.getId(),
              settings.get("notifyOnFailure"),
              settings.get("notifyOnSuccess"),
              settings.get("notifyOnStart"));
      return ResponseEntity.ok(updatedFavorite);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  /** Trigger manual CSV import (admin only) */
  @PostMapping("/import")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, Object>> triggerImport() {
    try {
      int imported = csvImportService.importAllCsvFiles();
      return ResponseEntity.ok(
          Map.of("imported", imported, "message", "Import completed successfully"));
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
          .body(Map.of("error", "Import failed: " + e.getMessage()));
    }
  }

  /** Get import configuration (admin only) */
  @GetMapping("/import/config")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Map<String, String>> getImportConfig() {
    return ResponseEntity.ok(
        Map.of(
            "importDirectory", csvImportService.getImportDirectory(),
            "processedDirectory", csvImportService.getProcessedDirectory()));
  }
}
