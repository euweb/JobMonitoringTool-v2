/**
 * Job Service for Phase 2 Job Monitoring
 *
 * Service layer for job management API calls including CRUD operations,
 * execution management, and job statistics.
 */

import {
  Job,
  JobExecution,
  JobListResponse,
  JobExecutionListResponse,
  CreateJobRequest,
  UpdateJobRequest,
  ExecuteJobRequest,
  JobStats,
} from "@/types/job";
import { apiClient } from "./apiClient";

export class JobService {
  /**
   * Get paginated list of jobs
   */
  static async getJobs(
    page: number = 0,
    size: number = 20,
    status?: string,
    search?: string,
  ): Promise<JobListResponse> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    if (status) params.append("status", status);
    if (search) params.append("search", search);

    const response = await apiClient.get(`/jobs?${params}`);
    return response.data;
  }

  /**
   * Get single job by ID
   */
  static async getJob(id: number): Promise<Job> {
    const response = await apiClient.get(`/jobs/${id}`);
    return response.data;
  }

  /**
   * Create new job
   */
  static async createJob(job: CreateJobRequest): Promise<Job> {
    const response = await apiClient.post("/jobs", job);
    return response.data;
  }

  /**
   * Update existing job
   */
  static async updateJob(id: number, job: UpdateJobRequest): Promise<Job> {
    const response = await apiClient.put(`/jobs/${id}`, job);
    return response.data;
  }

  /**
   * Delete job
   */
  static async deleteJob(id: number): Promise<void> {
    await apiClient.delete(`/jobs/${id}`);
  }

  /**
   * Enable/disable job
   */
  static async toggleJobEnabled(id: number, enabled: boolean): Promise<Job> {
    const response = await apiClient.patch(`/jobs/${id}/toggle`, { enabled });
    return response.data;
  }

  /**
   * Execute job manually
   */
  static async executeJob(request: ExecuteJobRequest): Promise<JobExecution> {
    const response = await apiClient.post(
      `/jobs/${request.jobId}/execute`,
      request,
    );
    return response.data;
  }

  /**
   * Get job execution history
   */
  static async getJobExecutions(
    jobId: number,
    page: number = 0,
    size: number = 20,
  ): Promise<JobExecutionListResponse> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    const response = await apiClient.get(`/jobs/${jobId}/executions?${params}`);
    return response.data;
  }

  /**
   * Get single job execution
   */
  static async getJobExecution(executionId: number): Promise<JobExecution> {
    const response = await apiClient.get(`/executions/${executionId}`);
    return response.data;
  }

  /**
   * Cancel running job execution
   */
  static async cancelExecution(executionId: number): Promise<void> {
    await apiClient.post(`/executions/${executionId}/cancel`);
  }

  /**
   * Get all job executions (across all jobs)
   */
  static async getAllExecutions(
    page: number = 0,
    size: number = 20,
    status?: string,
  ): Promise<JobExecutionListResponse> {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });

    if (status) params.append("status", status);

    const response = await apiClient.get(`/executions?${params}`);
    return response.data;
  }

  /**
   * Get job statistics
   */
  static async getJobStats(): Promise<JobStats> {
    const response = await apiClient.get("/jobs/stats");
    return response.data;
  }

  /**
   * Get execution output/logs
   */
  static async getExecutionOutput(executionId: number): Promise<{
    output: string;
    errorOutput: string;
  }> {
    const response = await apiClient.get(`/executions/${executionId}/output`);
    return response.data;
  }
}

export const jobService = JobService;
