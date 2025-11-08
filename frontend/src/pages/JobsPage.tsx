/**
 * Jobs List Page for Phase 2 Job Monitoring
 *
 * Displays a comprehensive list of all jobs with their status, statistics,
 * and management actions. Features include filtering, searching, and real-time updates.
 */

import React, { useState, useMemo } from "react";
import {
  Box,
  Typography,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  IconButton,
  Tooltip,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from "@mui/material";
import {
  Add as AddIcon,
  PlayArrow as PlayIcon,
  Stop as StopIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Schedule as ScheduleIcon,
  CheckCircle as SuccessIcon,
  Error as ErrorIcon,
  Pause as PauseIcon,
  Refresh as RefreshIcon,
  Favorite as FavoriteIcon,
  Notifications as NotificationsIcon,
  NotificationsActive as NotificationsActiveIcon,
} from "@mui/icons-material";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { jobService } from "@/services/jobService";
import { importedJobService } from "@/services/importedJobService";
import type { Job, JobStatus, ExecutionStatus } from "@/types/job";

/**
 * Status color mapping for job status chips
 */
const getStatusColor = (status: ExecutionStatus | JobStatus) => {
  switch (status) {
    case "ACTIVE":
    case "SUCCESS":
      return "success";
    case "FAILED":
    case "CANCELLED":
      return "error";
    case "RUNNING":
      return "info";
    case "QUEUED":
    case "RETRYING":
      return "warning";
    case "INACTIVE":
    case "DELETED":
      return "default";
    default:
      return "default";
  }
};

/**
 * Format execution duration for display
 */
const formatDuration = (durationSeconds?: number) => {
  if (durationSeconds === undefined || durationSeconds === null) return "N/A";

  const seconds = Math.floor(durationSeconds);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);

  if (hours > 0) {
    return `${hours}h ${minutes % 60}m ${seconds % 60}s`;
  } else if (minutes > 0) {
    return `${minutes}m ${seconds % 60}s`;
  } else {
    return `${seconds}s`;
  }
};

/**
 * Individual job card component
 */
interface JobCardProps {
  job: Job;
  onExecute: (job: Job) => void;
  onEdit: (job: Job) => void;
  onDelete: (job: Job) => void;
  onToggleEnabled: (job: Job) => void;
}

const JobCard: React.FC<JobCardProps> = ({
  job,
  onExecute,
  onEdit,
  onDelete,
  onToggleEnabled,
}) => {
  const navigate = useNavigate();

  return (
    <Card sx={{ height: "100%" }}>
      <CardContent>
        {/* Job Header */}
        <Box
          display="flex"
          justifyContent="space-between"
          alignItems="flex-start"
          mb={2}
        >
          <Box flex={1}>
            <Typography
              variant="h6"
              sx={{ cursor: "pointer" }}
              onClick={() => navigate(`/jobs/${job.id}`)}
            >
              {job.name}
            </Typography>
            <Typography variant="body2" color="text.secondary" noWrap>
              {job.description || "No description"}
            </Typography>
          </Box>
          <Chip
            label={job.status}
            color={getStatusColor(job.status) as any}
            size="small"
          />
        </Box>

        {/* Job Statistics */}
        <Grid container spacing={1} mb={2}>
          <Grid item xs={6}>
            <Typography variant="caption" color="text.secondary">
              Success Rate
            </Typography>
            <Typography variant="body2" fontWeight="bold">
              {Math.round(job.successRate)}%
            </Typography>
          </Grid>
          <Grid item xs={6}>
            <Typography variant="caption" color="text.secondary">
              Total Runs
            </Typography>
            <Typography variant="body2" fontWeight="bold">
              {job.totalExecutions}
            </Typography>
          </Grid>
        </Grid>

        {/* Last Execution Info */}
        {job.lastExecutionStatus && (
          <Box display="flex" alignItems="center" gap={1} mb={2}>
            <Chip
              label={job.lastExecutionStatus}
              color={getStatusColor(job.lastExecutionStatus) as any}
              size="small"
              icon={
                job.lastExecutionStatus === "SUCCESS" ? (
                  <SuccessIcon />
                ) : job.lastExecutionStatus === "FAILED" ? (
                  <ErrorIcon />
                ) : undefined
              }
            />
            <Typography variant="caption" color="text.secondary">
              {formatDuration(job.lastExecutionDuration)}
            </Typography>
          </Box>
        )}

        {/* Next/Last Execution */}
        <Box mb={2}>
          {job.nextExecution && (
            <Typography variant="caption" color="text.secondary">
              Next: {new Date(job.nextExecution).toLocaleString()}
            </Typography>
          )}
        </Box>

        {/* Action Buttons */}
        <Box display="flex" justifyContent="space-between">
          <Box>
            <Tooltip title="Execute Job">
              <IconButton
                size="small"
                color="primary"
                onClick={() => onExecute(job)}
                disabled={!job.isEnabled}
              >
                <PlayIcon />
              </IconButton>
            </Tooltip>
            <Tooltip title="Edit Job">
              <IconButton size="small" onClick={() => onEdit(job)}>
                <EditIcon />
              </IconButton>
            </Tooltip>
          </Box>
          <Box>
            <Tooltip title={job.isEnabled ? "Disable Job" : "Enable Job"}>
              <IconButton
                size="small"
                color={job.isEnabled ? "warning" : "success"}
                onClick={() => onToggleEnabled(job)}
              >
                {job.isEnabled ? <PauseIcon /> : <PlayIcon />}
              </IconButton>
            </Tooltip>
            <Tooltip title="Delete Job">
              <IconButton
                size="small"
                color="error"
                onClick={() => onDelete(job)}
              >
                <DeleteIcon />
              </IconButton>
            </Tooltip>
          </Box>
        </Box>
      </CardContent>
    </Card>
  );
};

/**
 * Main Jobs page component
 */
const JobsPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // State for filtering and pagination
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<string>("");
  const [page, setPage] = useState(0);
  const [deleteDialogJob, setDeleteDialogJob] = useState<Job | null>(null);

  // Fetch jobs with filters
  const {
    data: jobsData,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["jobs", page, search, statusFilter],
    queryFn: () =>
      jobService.getJobs(
        page,
        20,
        statusFilter || undefined,
        search || undefined,
      ),
    refetchInterval: 30000, // Refresh every 30 seconds
  });

  // Fetch favorite jobs
  const { data: favoriteJobs } = useQuery({
    queryKey: ["user-favorites"],
    queryFn: () => importedJobService.getFavorites(),
    refetchInterval: 60000, // Refresh every minute
  });

  // Favorite jobs already come with latestExecution from backend
  const favoriteJobsWithStatus = useMemo(() => {
    if (!favoriteJobs || favoriteJobs.length === 0) return [];

    return favoriteJobs.map((favoriteData) => {
      // Backend returns: { favorite: JobFavorite, latestExecution: ImportedJobExecution, jobSummary: Map }
      const favorite = favoriteData.favorite;
      const latestExecution = favoriteData.latestExecution;
      const jobSummary = favoriteData.jobSummary;

      return {
        jobName: favorite.jobName,
        notifyOnFailure: favorite.notifyOnFailure,
        notifyOnSuccess: favorite.notifyOnSuccess,
        notifyOnStart: favorite.notifyOnStart,
        latestExecution: latestExecution,
        totalExecutions: jobSummary?.totalExecutions || 0,
        successCount: jobSummary?.successfulExecutions || 0,
        failureCount: jobSummary?.failedExecutions || 0,
      };
    });
  }, [favoriteJobs]);

  // Execute job mutation
  const executeJobMutation = useMutation({
    mutationFn: (job: Job) => jobService.executeJob({ jobId: job.id }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["jobs"] });
    },
  });

  // Toggle job enabled mutation
  const toggleEnabledMutation = useMutation({
    mutationFn: ({ job, enabled }: { job: Job; enabled: boolean }) =>
      jobService.toggleJobEnabled(job.id, enabled),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["jobs"] });
    },
  });

  // Delete job mutation
  const deleteJobMutation = useMutation({
    mutationFn: (jobId: number) => jobService.deleteJob(jobId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["jobs"] });
      setDeleteDialogJob(null);
    },
  });

  // Update favorite notification settings
  const updateFavoriteSettingsMutation = useMutation({
    mutationFn: ({
      jobName,
      notifyOnFailure,
      notifyOnSuccess,
      notifyOnStart,
    }: {
      jobName: string;
      notifyOnFailure?: boolean;
      notifyOnSuccess?: boolean;
      notifyOnStart?: boolean;
    }) =>
      importedJobService.updateFavoriteSettings(jobName, {
        notifyOnFailure,
        notifyOnSuccess,
        notifyOnStart,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-favorites"] });
    },
  });

  // Filtered jobs
  const jobs = jobsData?.jobs || [];

  // Event handlers
  const handleExecuteJob = (job: Job) => {
    executeJobMutation.mutate(job);
  };

  const handleEditJob = (job: Job) => {
    navigate(`/jobs/${job.id}/edit`);
  };

  const handleDeleteJob = (job: Job) => {
    setDeleteDialogJob(job);
  };

  const confirmDelete = () => {
    if (deleteDialogJob) {
      deleteJobMutation.mutate(deleteDialogJob.id);
    }
  };

  const handleToggleEnabled = (job: Job) => {
    toggleEnabledMutation.mutate({ job, enabled: !job.isEnabled });
  };

  const handleRefresh = () => {
    queryClient.invalidateQueries({ queryKey: ["jobs"] });
  };

  if (error) {
    return (
      <Alert severity="error" sx={{ mt: 2 }}>
        Failed to load jobs: {(error as Error).message}
      </Alert>
    );
  }

  return (
    <Box>
      {/* Page Header */}
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        mb={3}
      >
        <Typography variant="h4" component="h1">
          Job Monitoring
        </Typography>
        <Box display="flex" gap={2}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={handleRefresh}
          >
            Refresh
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate("/jobs/new")}
          >
            Create Job
          </Button>
        </Box>
      </Box>

      {/* Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Grid container spacing={2}>
            <Grid item xs={12} md={6}>
              <TextField
                fullWidth
                label="Search Jobs"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                placeholder="Search by job name or description..."
              />
            </Grid>
            <Grid item xs={12} md={3}>
              <FormControl fullWidth>
                <InputLabel>Status</InputLabel>
                <Select
                  value={statusFilter}
                  label="Status"
                  onChange={(e) => setStatusFilter(e.target.value)}
                >
                  <MenuItem value="">All Statuses</MenuItem>
                  <MenuItem value="ACTIVE">Active</MenuItem>
                  <MenuItem value="DISABLED">Disabled</MenuItem>
                  <MenuItem value="ARCHIVED">Archived</MenuItem>
                </Select>
              </FormControl>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Favorite Jobs Section */}
      {favoriteJobsWithStatus && favoriteJobsWithStatus.length > 0 && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
            <Box display="flex" alignItems="center" mb={2}>
              <FavoriteIcon color="error" sx={{ mr: 1 }} />
              <Typography variant="h6">Favorite Jobs</Typography>
            </Box>
            <Grid container spacing={2}>
              {favoriteJobsWithStatus.map((favorite) => (
                <Grid item xs={12} md={6} key={favorite.jobName}>
                  <Card variant="outlined">
                    <CardContent>
                      <Box
                        display="flex"
                        justifyContent="space-between"
                        alignItems="start"
                      >
                        <Box flex={1}>
                          <Typography variant="subtitle1" fontWeight="medium">
                            {favorite.jobName}
                          </Typography>
                          {favorite.latestExecution && (
                            <Box mt={1}>
                              <Chip
                                label={
                                  favorite.latestExecution.status || "Unknown"
                                }
                                color={
                                  getStatusColor(
                                    favorite.latestExecution.status,
                                  ) as any
                                }
                                size="small"
                                sx={{ mr: 1 }}
                              />
                              <Typography
                                variant="caption"
                                color="text.secondary"
                                display="block"
                              >
                                Start:{" "}
                                {favorite.latestExecution.startedAt
                                  ? new Date(
                                      favorite.latestExecution.startedAt,
                                    ).toLocaleString()
                                  : "Nicht gestartet"}
                              </Typography>
                              <Typography
                                variant="caption"
                                color="text.secondary"
                                display="block"
                              >
                                Ende:{" "}
                                {favorite.latestExecution.endedAt
                                  ? new Date(
                                      favorite.latestExecution.endedAt,
                                    ).toLocaleString()
                                  : "Nicht beendet"}
                              </Typography>
                              <Typography
                                variant="caption"
                                color="text.secondary"
                                display="block"
                              >
                                Laufzeit:{" "}
                                {formatDuration(
                                  favorite.latestExecution.durationSeconds,
                                )}
                              </Typography>
                            </Box>
                          )}
                          <Typography
                            variant="body2"
                            color="text.secondary"
                            sx={{ mt: 1 }}
                          >
                            Executions: {favorite.totalExecutions} | Success:{" "}
                            {favorite.successCount} | Failed:{" "}
                            {favorite.failureCount}
                          </Typography>
                        </Box>
                        <Box display="flex" flexDirection="column" gap={1}>
                          <Tooltip
                            title={
                              favorite.notifyOnFailure
                                ? "Failure notifications enabled"
                                : "Enable failure notifications"
                            }
                          >
                            <IconButton
                              size="small"
                              color={
                                favorite.notifyOnFailure ? "error" : "default"
                              }
                              onClick={() =>
                                updateFavoriteSettingsMutation.mutate({
                                  jobName: favorite.jobName,
                                  notifyOnFailure: !favorite.notifyOnFailure,
                                  notifyOnSuccess: favorite.notifyOnSuccess,
                                  notifyOnStart: favorite.notifyOnStart,
                                })
                              }
                            >
                              {favorite.notifyOnFailure ? (
                                <NotificationsActiveIcon />
                              ) : (
                                <NotificationsIcon />
                              )}
                            </IconButton>
                          </Tooltip>
                        </Box>
                      </Box>
                    </CardContent>
                  </Card>
                </Grid>
              ))}
            </Grid>
          </CardContent>
        </Card>
      )}

      {/* Jobs Grid */}
      {isLoading ? (
        <Box display="flex" justifyContent="center" py={4}>
          <CircularProgress />
        </Box>
      ) : (
        <Grid container spacing={3}>
          {jobs.map((job) => (
            <Grid item xs={12} md={6} lg={4} key={job.id}>
              <JobCard
                job={job}
                onExecute={handleExecuteJob}
                onEdit={handleEditJob}
                onDelete={handleDeleteJob}
                onToggleEnabled={handleToggleEnabled}
              />
            </Grid>
          ))}
        </Grid>
      )}

      {/* No Jobs Message */}
      {!isLoading && jobs.length === 0 && (
        <Box textAlign="center" py={8}>
          <ScheduleIcon sx={{ fontSize: 64, color: "text.secondary", mb: 2 }} />
          <Typography variant="h6" color="text.secondary" gutterBottom>
            No jobs found
          </Typography>
          <Typography variant="body2" color="text.secondary" mb={3}>
            {search || statusFilter
              ? "Try adjusting your filters"
              : "Create your first job to get started"}
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => navigate("/jobs/new")}
          >
            Create Job
          </Button>
        </Box>
      )}

      {/* Delete Confirmation Dialog */}
      <Dialog open={!!deleteDialogJob} onClose={() => setDeleteDialogJob(null)}>
        <DialogTitle>Delete Job</DialogTitle>
        <DialogContent>
          <Typography>
            Are you sure you want to delete "{deleteDialogJob?.name}"? This
            action cannot be undone.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogJob(null)}>Cancel</Button>
          <Button
            onClick={confirmDelete}
            color="error"
            variant="contained"
            disabled={deleteJobMutation.isPending}
          >
            {deleteJobMutation.isPending ? "Deleting..." : "Delete"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default JobsPage;
