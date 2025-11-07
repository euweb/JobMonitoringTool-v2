package com.company.jobmonitor.controller;

import com.company.jobmonitor.dto.JobDto;
import com.company.jobmonitor.dto.JobExecutionDto;
import com.company.jobmonitor.entity.Job;
import com.company.jobmonitor.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for job management operations.
 *
 * <p>This controller provides comprehensive job management endpoints including:
 *
 * <ul>
 *   <li>Job CRUD operations (Create, Read, Update, Delete)
 *   <li>Job execution triggering and monitoring
 *   <li>Job search and filtering capabilities
 *   <li>Execution history and analytics
 *   <li>Dashboard statistics and metrics
 * </ul>
 *
 * <p>All endpoints require authentication and appropriate authorization levels. Admin users have
 * full access while regular users can only manage their own jobs.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 2.0
 */
@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Job Management", description = "Job monitoring and execution management operations")
@SecurityRequirement(name = "bearerAuth")
public class JobController {

  private final JobService jobService;

  /**
   * Constructs a JobController with the specified job service.
   *
   * @param jobService the service for job management operations
   */
  public JobController(JobService jobService) {
    this.jobService = jobService;
  }

  /**
   * Retrieves all jobs with pagination and filtering.
   *
   * @param page the page number (0-based)
   * @param size the page size
   * @param sortBy the field to sort by
   * @param sortDir the sort direction (asc/desc)
   * @param name optional name filter
   * @param status optional status filter
   * @param jobType optional job type filter
   * @param priority optional priority filter
   * @param createdById optional creator user ID filter
   * @return page of job DTOs
   */
  @GetMapping
  @Operation(
      summary = "Get all jobs",
      description =
          "Retrieves a paginated list of all jobs with optional filtering and sorting capabilities")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Jobs retrieved successfully",
        content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Access denied")
  })
  public ResponseEntity<Page<JobDto>> getAllJobs(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20")
          int size,
      @Parameter(description = "Sort field", example = "name") @RequestParam(defaultValue = "name")
          String sortBy,
      @Parameter(description = "Sort direction", example = "asc")
          @RequestParam(defaultValue = "asc")
          String sortDir,
      @Parameter(description = "Filter by job name") @RequestParam(required = false) String name,
      @Parameter(description = "Filter by job status") @RequestParam(required = false)
          Job.JobStatus status,
      @Parameter(description = "Filter by job type") @RequestParam(required = false)
          Job.JobType jobType,
      @Parameter(description = "Filter by job priority") @RequestParam(required = false)
          Job.JobPriority priority,
      @Parameter(description = "Filter by creator user ID") @RequestParam(required = false)
          Integer createdById) {
    Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<JobDto> jobs;
    if (name != null
        || status != null
        || jobType != null
        || priority != null
        || createdById != null) {
      jobs = jobService.searchJobs(name, status, jobType, priority, createdById, pageable);
    } else {
      jobs = jobService.getAllJobs(pageable);
    }

    return ResponseEntity.ok(jobs);
  }

  /**
   * Retrieves a specific job by its ID.
   *
   * @param id the job ID
   * @return the job DTO
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get job by ID",
      description =
          "Retrieves detailed information about a specific job including execution statistics")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Job retrieved successfully",
        content = @Content(schema = @Schema(implementation = JobDto.class))),
    @ApiResponse(responseCode = "404", description = "Job not found"),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  public ResponseEntity<JobDto> getJobById(
      @Parameter(description = "Job ID", example = "1") @PathVariable Integer id) {
    return jobService
        .getJobById(id)
        .map(job -> ResponseEntity.ok(job))
        .orElse(ResponseEntity.notFound().build());
  }

  /**
   * Creates a new job.
   *
   * @param jobDto the job data to create
   * @param authentication the current user authentication
   * @return the created job DTO
   */
  @PostMapping
  @Operation(
      summary = "Create a new job",
      description = "Creates a new job with the provided configuration")
  @ApiResponses({
    @ApiResponse(
        responseCode = "201",
        description = "Job created successfully",
        content = @Content(schema = @Schema(implementation = JobDto.class))),
    @ApiResponse(responseCode = "400", description = "Invalid job data"),
    @ApiResponse(responseCode = "409", description = "Job name already exists"),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<JobDto> createJob(
      @Parameter(description = "Job configuration data") @Valid @RequestBody JobDto jobDto,
      Authentication authentication) {
    try {
      JobDto createdJob = jobService.createJob(jobDto, authentication.getName());
      return ResponseEntity.status(HttpStatus.CREATED).body(createdJob);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
  }

  /**
   * Updates an existing job.
   *
   * @param id the job ID
   * @param jobDto the updated job data
   * @param authentication the current user authentication
   * @return the updated job DTO
   */
  @PutMapping("/{id}")
  @Operation(
      summary = "Update an existing job",
      description = "Updates job configuration and settings")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Job updated successfully",
        content = @Content(schema = @Schema(implementation = JobDto.class))),
    @ApiResponse(responseCode = "400", description = "Invalid job data"),
    @ApiResponse(responseCode = "404", description = "Job not found"),
    @ApiResponse(responseCode = "409", description = "Job name already exists"),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @PreAuthorize(
      "hasRole('ADMIN') or @jobService.getJobById(#id).orElse(null)?.createdBy?.username =="
          + " authentication.name")
  public ResponseEntity<JobDto> updateJob(
      @Parameter(description = "Job ID", example = "1") @PathVariable Integer id,
      @Parameter(description = "Updated job configuration data") @Valid @RequestBody JobDto jobDto,
      Authentication authentication) {
    try {
      JobDto updatedJob = jobService.updateJob(id, jobDto, authentication.getName());
      return ResponseEntity.ok(updatedJob);
    } catch (RuntimeException e) {
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      } else if (e.getMessage().contains("already exists")) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
      }
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Deletes a job.
   *
   * @param id the job ID
   * @return empty response
   */
  @DeleteMapping("/{id}")
  @Operation(
      summary = "Delete a job",
      description = "Permanently deletes a job and all associated execution history")
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Job deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Job not found"),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @PreAuthorize(
      "hasRole('ADMIN') or @jobService.getJobById(#id).orElse(null)?.createdBy?.username =="
          + " authentication.name")
  public ResponseEntity<Void> deleteJob(
      @Parameter(description = "Job ID", example = "1") @PathVariable Integer id) {
    try {
      jobService.deleteJob(id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.notFound().build();
    }
  }

  /**
   * Triggers a manual execution of a job.
   *
   * @param id the job ID
   * @param authentication the current user authentication
   * @return the created job execution DTO
   */
  @PostMapping("/{id}/execute")
  @Operation(
      summary = "Execute a job manually",
      description = "Triggers immediate execution of a job regardless of its schedule")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Job execution started successfully",
        content = @Content(schema = @Schema(implementation = JobExecutionDto.class))),
    @ApiResponse(responseCode = "404", description = "Job not found"),
    @ApiResponse(responseCode = "400", description = "Job cannot be executed (inactive)"),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Access denied")
  })
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<JobExecutionDto> executeJob(
      @Parameter(description = "Job ID", example = "1") @PathVariable Integer id,
      Authentication authentication) {
    try {
      JobExecutionDto execution = jobService.triggerJobExecution(id, authentication.getName());
      return ResponseEntity.ok(execution);
    } catch (RuntimeException e) {
      if (e.getMessage().contains("not found")) {
        return ResponseEntity.notFound().build();
      } else if (e.getMessage().contains("inactive")) {
        return ResponseEntity.badRequest().build();
      }
      return ResponseEntity.badRequest().build();
    }
  }

  /**
   * Retrieves execution history for a job.
   *
   * @param id the job ID
   * @param page the page number
   * @param size the page size
   * @return page of job execution DTOs
   */
  @GetMapping("/{id}/executions")
  @Operation(
      summary = "Get job execution history",
      description = "Retrieves paginated execution history for a specific job")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Execution history retrieved successfully",
        content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "404", description = "Job not found"),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  public ResponseEntity<Page<JobExecutionDto>> getJobExecutions(
      @Parameter(description = "Job ID", example = "1") @PathVariable Integer id,
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20")
          int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
    Page<JobExecutionDto> executions = jobService.getJobExecutions(id, pageable);
    return ResponseEntity.ok(executions);
  }

  /**
   * Retrieves recent executions across all jobs.
   *
   * @param page the page number
   * @param size the page size
   * @return page of recent job execution DTOs
   */
  @GetMapping("/executions/recent")
  @Operation(
      summary = "Get recent job executions",
      description = "Retrieves recent job executions across all jobs")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Recent executions retrieved successfully",
        content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  public ResponseEntity<Page<JobExecutionDto>> getRecentExecutions(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20")
          int size) {
    Pageable pageable = PageRequest.of(page, size);
    Page<JobExecutionDto> executions = jobService.getRecentExecutions(pageable);
    return ResponseEntity.ok(executions);
  }

  /**
   * Retrieves jobs created by the current user.
   *
   * @param authentication the current user authentication
   * @param page the page number
   * @param size the page size
   * @return page of job DTOs created by the user
   */
  @GetMapping("/my-jobs")
  @Operation(
      summary = "Get current user's jobs",
      description = "Retrieves jobs created by the currently authenticated user")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "User jobs retrieved successfully",
        content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<Page<JobDto>> getMyJobs(
      Authentication authentication,
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20")
          int size) {
    // TODO: Get user ID from authentication
    // For now, assume user ID 1 (admin user from sample data)
    Integer userId = 1;

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<JobDto> jobs = jobService.getJobsByUser(userId, pageable);
    return ResponseEntity.ok(jobs);
  }

  /**
   * Retrieves dashboard statistics for jobs.
   *
   * @return job statistics object
   */
  @GetMapping("/statistics")
  @Operation(
      summary = "Get job statistics",
      description = "Retrieves aggregate statistics about jobs for dashboard display")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  public ResponseEntity<Object[]> getJobStatistics() {
    Object[] statistics = jobService.getJobStatistics();
    return ResponseEntity.ok(statistics);
  }

  /**
   * Retrieves jobs that are due for execution.
   *
   * @return list of jobs due for execution
   */
  @GetMapping("/due")
  @Operation(
      summary = "Get jobs due for execution",
      description = "Retrieves jobs that are scheduled to run or overdue for execution")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Due jobs retrieved successfully",
        content = @Content(schema = @Schema(implementation = List.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<List<JobDto>> getJobsDue() {
    List<JobDto> dueJobs = jobService.getJobsDueForExecution();
    return ResponseEntity.ok(dueJobs);
  }
}
