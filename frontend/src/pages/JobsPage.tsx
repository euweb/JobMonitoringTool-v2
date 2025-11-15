/**
 * Jobs List Page for Phase 2 Job Monitoring
 *
 * Displays a comprehensive list of all jobs with their status, statistics,
 * and management actions. Features include filtering, searching, and real-time updates.
 */

import React, { useMemo } from "react";
import {
  Box,
  Typography,
  Card,
  CardContent,
  Chip,
  Grid,
  IconButton,
  Tooltip,
} from "@mui/material";
import {
  Favorite as FavoriteIcon,
  Notifications as NotificationsIcon,
  NotificationsActive as NotificationsActiveIcon,
} from "@mui/icons-material";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { importedJobService } from "@/services/importedJobService";
import type { JobStatus, ExecutionStatus } from "@/types/job";
import { getStatusInfo } from "./ImportedJobsPage";

/**
 * Status color mapping for job status chips
 */
const getStatusColor = (status: ExecutionStatus | JobStatus) => {
  return getStatusInfo(status).color;
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
 * Main Jobs page component
 */
const JobsPage: React.FC = () => {
  const navigate = useNavigate();
  const queryClient = useQueryClient();

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

  return (
    <Box>
      {/* Favorite Jobs Section */}
      <Box display="flex" alignItems="center" mb={2}>
        <FavoriteIcon color="error" sx={{ mr: 1 }} />
        <Typography variant="h6">Favorite Jobs</Typography>
      </Box>
      {favoriteJobsWithStatus && favoriteJobsWithStatus.length > 0 && (
        <Card sx={{ mb: 3 }}>
          <CardContent>
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
    </Box>
  );
};

export default JobsPage;
