package com.company.jobmonitor.controller;

import com.company.jobmonitor.dto.JobExecutionDto;
import com.company.jobmonitor.entity.JobExecution;
import com.company.jobmonitor.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for job execution monitoring and management.
 *
 * <p>This controller provides endpoints for monitoring job executions including:
 *
 * <ul>
 *   <li>Execution history and details
 *   <li>Execution status tracking
 *   <li>Performance metrics and analytics
 *   <li>Execution filtering and search
 * </ul>
 *
 * <p>All endpoints require authentication. Users can view all executions while admin users have
 * additional management capabilities.
 *
 * @author Job Monitor Development Team
 * @version 1.0
 * @since 2.0
 */
@RestController
@RequestMapping("/api/executions")
@Tag(name = "Job Executions", description = "Job execution monitoring and analysis operations")
@SecurityRequirement(name = "bearerAuth")
public class JobExecutionController {

  private final JobService jobService;

  /**
   * Constructs a JobExecutionController with the specified job service.
   *
   * @param jobService the service for job management operations
   */
  public JobExecutionController(JobService jobService) {
    this.jobService = jobService;
  }

  /**
   * Retrieves all job executions with pagination and filtering.
   *
   * @param page the page number (0-based)
   * @param size the page size
   * @param sortBy the field to sort by
   * @param sortDir the sort direction (asc/desc)
   * @param status optional execution status filter
   * @param jobId optional job ID filter
   * @param triggeredBy optional triggerer username filter
   * @param startedAfter optional start time filter (after this date)
   * @param startedBefore optional start time filter (before this date)
   * @return page of job execution DTOs
   */
  @GetMapping
  @Operation(
      summary = "Get all job executions",
      description =
          "Retrieves a paginated list of job executions with optional filtering and sorting"
              + " capabilities")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Executions retrieved successfully",
        content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required"),
    @ApiResponse(responseCode = "403", description = "Access denied")
  })
  public ResponseEntity<Page<JobExecutionDto>> getAllExecutions(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20")
          int size,
      @Parameter(description = "Sort field", example = "startedAt")
          @RequestParam(defaultValue = "startedAt")
          String sortBy,
      @Parameter(description = "Sort direction", example = "desc")
          @RequestParam(defaultValue = "desc")
          String sortDir,
      @Parameter(description = "Filter by execution status") @RequestParam(required = false)
          JobExecution.ExecutionStatus status,
      @Parameter(description = "Filter by job ID") @RequestParam(required = false) Long jobId,
      @Parameter(description = "Filter by triggerer username") @RequestParam(required = false)
          String triggeredBy,
      @Parameter(description = "Filter executions started after this date")
          @RequestParam(required = false)
          LocalDateTime startedAfter,
      @Parameter(description = "Filter executions started before this date")
          @RequestParam(required = false)
          LocalDateTime startedBefore) {
    Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    // TODO: Implement filtering in JobService
    // For now, return all recent executions
    Page<JobExecutionDto> executions = jobService.getRecentExecutions(pageable);
    return ResponseEntity.ok(executions);
  }

  /**
   * Retrieves a specific job execution by its ID.
   *
   * @param id the execution ID
   * @return the job execution DTO
   */
  @GetMapping("/{id}")
  @Operation(
      summary = "Get job execution by ID",
      description = "Retrieves detailed information about a specific job execution")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Execution retrieved successfully",
        content = @Content(schema = @Schema(implementation = JobExecutionDto.class))),
    @ApiResponse(responseCode = "404", description = "Execution not found"),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  public ResponseEntity<JobExecutionDto> getExecutionById(
      @Parameter(description = "Execution ID", example = "1") @PathVariable Integer id) {
    // TODO: Implement getExecutionById in JobService
    // For now, return not found
    return ResponseEntity.notFound().build();
  }

  /**
   * Retrieves execution statistics for analytics.
   *
   * @param days the number of days to include in statistics (default: 30)
   * @return execution statistics object
   */
  @GetMapping("/statistics")
  @Operation(
      summary = "Get execution statistics",
      description = "Retrieves aggregate statistics about job executions for analytics dashboard")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  public ResponseEntity<Object[]> getExecutionStatistics(
      @Parameter(description = "Number of days to include in statistics", example = "30")
          @RequestParam(defaultValue = "30")
          int days) {
    // TODO: Implement execution statistics in JobService
    // For now, return basic job statistics
    Object[] statistics = jobService.getJobStatistics();
    return ResponseEntity.ok(statistics);
  }

  /**
   * Retrieves currently running executions.
   *
   * @return list of running job execution DTOs
   */
  @GetMapping("/running")
  @Operation(
      summary = "Get currently running executions",
      description = "Retrieves all job executions that are currently in progress")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Running executions retrieved successfully",
        content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  public ResponseEntity<Page<JobExecutionDto>> getRunningExecutions(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20")
          int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
    // TODO: Implement getRunningExecutions in JobService
    // For now, return recent executions
    Page<JobExecutionDto> executions = jobService.getRecentExecutions(pageable);
    return ResponseEntity.ok(executions);
  }

  /**
   * Retrieves failed executions for troubleshooting.
   *
   * @param page the page number
   * @param size the page size
   * @param days the number of days to look back (default: 7)
   * @return page of failed job execution DTOs
   */
  @GetMapping("/failed")
  @Operation(
      summary = "Get failed executions",
      description = "Retrieves job executions that have failed for troubleshooting purposes")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "Failed executions retrieved successfully",
        content = @Content(schema = @Schema(implementation = Page.class))),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  public ResponseEntity<Page<JobExecutionDto>> getFailedExecutions(
      @Parameter(description = "Page number (0-based)", example = "0")
          @RequestParam(defaultValue = "0")
          int page,
      @Parameter(description = "Page size", example = "20") @RequestParam(defaultValue = "20")
          int size,
      @Parameter(description = "Number of days to look back", example = "7")
          @RequestParam(defaultValue = "7")
          int days) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "startedAt"));
    // TODO: Implement getFailedExecutions in JobService
    // For now, return recent executions
    Page<JobExecutionDto> executions = jobService.getRecentExecutions(pageable);
    return ResponseEntity.ok(executions);
  }

  /**
   * Retrieves execution performance trends.
   *
   * @param jobId optional job ID filter
   * @param days the number of days to include (default: 30)
   * @return performance trend data
   */
  @GetMapping("/performance")
  @Operation(
      summary = "Get execution performance trends",
      description = "Retrieves performance metrics and trends for job executions over time")
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Performance data retrieved successfully"),
    @ApiResponse(responseCode = "401", description = "Authentication required")
  })
  public ResponseEntity<Object> getExecutionPerformance(
      @Parameter(description = "Optional job ID filter") @RequestParam(required = false) Long jobId,
      @Parameter(description = "Number of days to include", example = "30")
          @RequestParam(defaultValue = "30")
          int days) {
    // TODO: Implement performance analytics in JobService
    // For now, return placeholder data
    return ResponseEntity.ok("Performance data not yet implemented");
  }
}
