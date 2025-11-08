/**
 * Job Monitoring Types for Phase 2
 *
 * TypeScript types matching the backend DTOs for Jobs, Executions, and Schedules.
 */

// Job execution status enum
export type ExecutionStatus =
  | "QUEUED"
  | "RUNNING"
  | "SUCCESS"
  | "FAILED"
  | "CANCELLED"
  | "TIMEOUT"
  | "RETRYING";

// Job execution trigger type enum
export type TriggerType = "MANUAL" | "SCHEDULED" | "EVENT";

// Job status enum
export type JobStatus = "ACTIVE" | "INACTIVE" | "PAUSED" | "DELETED";

// Job priority enum matching backend
export type JobPriority = "LOW" | "NORMAL" | "HIGH" | "CRITICAL";

/**
 * Represents a single job execution instance
 */
export interface JobExecution {
  id: number;
  jobId: number;
  jobName: string;
  status: ExecutionStatus;
  triggerType: TriggerType;
  executionNumber: number;
  retryAttempt: number;
  startedAt?: string; // ISO datetime string
  endedAt?: string; // ISO datetime string
  durationMs?: number;
  formattedDuration?: string;
  exitCode?: number;
  output?: string;
  errorOutput?: string;
  failureReason?: string;
  hostName?: string;
  processId?: number;
  memoryUsageMb?: number;
  cpuUsagePercent?: number;
  createdAt: string; // ISO datetime string
  updatedAt: string; // ISO datetime string
}

/**
 * Represents a scheduled job
 */
export interface Job {
  id: number;
  name: string;
  description?: string;
  command: string;
  arguments?: string;
  workingDirectory?: string;
  environmentVariables?: string; // JSON string
  status: JobStatus;
  priority: JobPriority;
  maxRetries: number;
  timeoutSeconds: number;
  triggerType: TriggerType;
  cronExpression?: string;
  isEnabled: boolean;
  tags?: string[];

  // Statistics
  totalExecutions: number;
  successfulExecutions: number;
  failedExecutions: number;
  successRate: number;
  averageDuration?: number;
  lastExecutionDuration?: number;
  lastExecutionStatus?: ExecutionStatus;

  // Metadata
  createdBy: string;
  createdAt: string; // ISO datetime string
  updatedAt: string; // ISO datetime string
  nextExecution?: string; // ISO datetime string
  lastExecution?: string; // ISO datetime string
}

/**
 * Job schedule information
 */
export interface JobSchedule {
  id: number;
  jobId: number;
  cronExpression: string;
  timezone: string;
  description?: string;
  isEnabled: boolean;
  nextExecution?: string; // ISO datetime string
  lastExecution?: string; // ISO datetime string
  executionCount: number;
  createdAt: string; // ISO datetime string
  updatedAt: string; // ISO datetime string
}

/**
 * Request payload for creating/updating jobs
 */
export interface CreateJobRequest {
  name: string;
  description?: string;
  command: string;
  arguments?: string;
  workingDirectory?: string;
  environmentVariables?: string; // JSON string
  priority: JobPriority;
  maxRetries: number;
  timeoutSeconds: number;
  triggerType: TriggerType;
  cronExpression?: string;
  isEnabled: boolean;
  tags?: string[];
}

/**
 * Request payload for updating job
 */
export interface UpdateJobRequest {
  id: number;
  name?: string;
  description?: string;
  command?: string;
  arguments?: string;
  workingDirectory?: string;
  environmentVariables?: string; // JSON string
  priority?: JobPriority;
  maxRetries?: number;
  timeoutSeconds?: number;
  triggerType?: TriggerType;
  cronExpression?: string;
  isEnabled?: boolean;
  tags?: string[];
}

/**
 * Request payload for manual job execution
 */
export interface ExecuteJobRequest {
  jobId: number;
  arguments?: string;
  environmentVariables?: string; // JSON string
}

/**
 * Job execution statistics
 */
export interface JobStats {
  totalJobs: number;
  activeJobs: number;
  runningExecutions: number;
  failedExecutionsToday: number;
  successfulExecutionsToday: number;
  averageExecutionTime: number;
}

/**
 * Paginated job list response
 */
export interface JobListResponse {
  jobs: Job[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}

/**
 * Paginated job execution list response
 */
export interface JobExecutionListResponse {
  executions: JobExecution[];
  total: number;
  page: number;
  pageSize: number;
  totalPages: number;
}
