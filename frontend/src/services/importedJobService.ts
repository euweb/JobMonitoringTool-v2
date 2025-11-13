/**
 * Service for managing imported job executions from CSV files
 *
 * This service provides methods to interact with the imported jobs API endpoints,
 * including fetching job executions with pagination, filtering, and managing favorites.
 */

import { apiClient } from "./apiClient";

/**
 * Imported Job Execution interface based on backend entity
 */
export interface ImportedJobExecution {
  executionId: number;
  jobType?: string;
  jobName: string;
  scriptPath?: string;
  priority?: number;
  strategy?: number;
  status?: string;
  submittedBy?: string;
  submittedAt?: string;
  startedAt?: string;
  endedAt?: string;
  host?: string;
  parentExecutionId?: number;
  durationSeconds?: number;
  importTimestamp?: string;
  csvSourceFile?: string;
}

/**
 * Job summary with aggregated statistics
 */
export interface JobSummary {
  jobName: string;
  latestStatus: string;
  totalExecutions: number;
  successfulExecutions: number;
  failedExecutions: number;
  runningExecutions: number;
  avgDurationSeconds?: number;
  lastExecution?: ImportedJobExecution;
}

/**
 * Paginated response for job executions
 */
export interface PagedResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

/**
 * Filter parameters for job executions
 */
export interface ExecutionFilters {
  jobName?: string;
  status?: string;
  jobType?: string;
  host?: string;
  submittedBy?: string;
  submittedAfter?: string;
  submittedBefore?: string;
  startedAfter?: string;
  startedBefore?: string;
  endedAfter?: string;
  endedBefore?: string;
}

/**
 * Service class for imported job operations
 */
export class ImportedJobService {
  /**
   * Get paginated list of job executions with optional filters
   */
  async getExecutions(
    page: number = 0,
    size: number = 20,
    filters?: ExecutionFilters,
  ): Promise<PagedResponse<ImportedJobExecution>> {
    const params: Record<string, string | number> = {
      page: page,
      size: size,
    };

    if (filters?.jobName) params.jobName = filters.jobName;
    if (filters?.status) params.status = filters.status;
    if (filters?.jobType) params.jobType = filters.jobType;
    if (filters?.host) params.host = filters.host;
    if (filters?.submittedBy) params.submittedBy = filters.submittedBy;
    if (filters?.submittedAfter) params.submittedAfter = filters.submittedAfter;
    if (filters?.submittedBefore)
      params.submittedBefore = filters.submittedBefore;
    if (filters?.startedAfter) params.startedAfter = filters.startedAfter;
    if (filters?.startedBefore) params.startedBefore = filters.startedBefore;
    if (filters?.endedAfter) params.endedAfter = filters.endedAfter;
    if (filters?.endedBefore) params.endedBefore = filters.endedBefore;

    const response = await apiClient.get("/imported-jobs/executions", {
      params,
    });
    return response.data;
  }

  /**
   * Get filter options for dropdowns
   */
  async getFilterOptions(): Promise<{
    statuses: string[];
    jobTypes: string[];
    hosts: string[];
    submitters: string[];
  }> {
    const response = await apiClient.get("/imported-jobs/executions/filters");
    return response.data;
  }

  /**
   * Get job summaries with pagination
   */
  async getJobs(
    page: number = 0,
    size: number = 20,
  ): Promise<PagedResponse<JobSummary>> {
    const response = await apiClient.get("/imported-jobs", {
      params: { page, size },
    });
    return response.data;
  }

  /**
   * Get single job execution by ID
   */
  async getExecutionById(executionId: number): Promise<ImportedJobExecution> {
    const response = await apiClient.get(
      `/imported-jobs/executions/${executionId}`,
    );
    return response.data;
  }

  /**
   * Get job details by name
   */
  async getJobByName(jobName: string): Promise<JobSummary> {
    const response = await apiClient.get(`/imported-jobs/${jobName}`);
    return response.data;
  }

  /**
   * Get currently running jobs
   */
  async getRunningJobs(): Promise<ImportedJobExecution[]> {
    const response = await apiClient.get("/imported-jobs/running");
    return response.data;
  }

  /**
   * Get recent failures
   */
  async getRecentFailures(): Promise<ImportedJobExecution[]> {
    const response = await apiClient.get("/imported-jobs/failures");
    return response.data;
  }

  /**
   * Get job chain for an execution
   */
  async getJobChain(executionId: number): Promise<any> {
    const response = await apiClient.get(
      `/imported-jobs/executions/${executionId}/chain`,
    );
    return response.data;
  }

  /**
   * Add job to favorites
   */
  async addToFavorites(jobName: string): Promise<any> {
    const response = await apiClient.post(`/imported-jobs/${jobName}/favorite`);
    return response.data;
  }

  /**
   * Remove job from favorites
   */
  async removeFromFavorites(jobName: string): Promise<void> {
    await apiClient.delete(`/imported-jobs/${jobName}/favorite`);
  }

  /**
   * Get user's favorite jobs
   */
  async getFavorites(): Promise<any[]> {
    const response = await apiClient.get("/imported-jobs/favorites");
    return response.data;
  }

  /**
   * Update favorite notification settings
   */
  async updateFavoriteSettings(
    jobName: string,
    settings: {
      notifyOnFailure?: boolean;
      notifyOnSuccess?: boolean;
      notifyOnStart?: boolean;
    },
  ): Promise<any> {
    const response = await apiClient.put(
      `/imported-jobs/${jobName}/favorite/settings`,
      settings,
    );
    return response.data;
  }

  /**
   * Trigger manual CSV import
   */
  async triggerImport(): Promise<{ imported: number; message: string }> {
    const response = await apiClient.post("/imported-jobs/import");
    return response.data;
  }

  /**
   * Get import statistics
   */
  async getImportStats(): Promise<any> {
    const response = await apiClient.get("/imported-jobs/import/stats");
    return response.data;
  }
}

/**
 * Singleton instance of ImportedJobService
 */
export const importedJobService = new ImportedJobService();
