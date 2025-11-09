/**
 * Imported Jobs Execution Page
 *
 * Displays all job executions imported from CSV files with:
 * - Pagination
 * - Column-based filtering
 * - Status indicators
 * - Execution details
 * - Job chain visualization
 */

import React, { useState, useCallback, useMemo } from "react";
import {
  Box,
  Typography,
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
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Paper,
  Button,
  Stack,
  Collapse,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from "@mui/material";
import {
  PlayArrow as RunningIcon,
  CheckCircle as SuccessIcon,
  Error as ErrorIcon,
  Queue as QueueIcon,
  Refresh as RefreshIcon,
  Download as ImportIcon,
  Favorite as FavoriteIcon,
  FavoriteBorder as FavoriteBorderIcon,
  ExpandMore as ExpandMoreIcon,
  ExpandLess as ExpandLessIcon,
  Timeline as ChainIcon,
  AccountTree as AccountTreeIcon,
} from "@mui/icons-material";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  ImportedJobExecution,
  ExecutionFilters,
  importedJobService,
} from "@/services/importedJobService";

/**
 * Status color and icon mapping
 */
const getStatusInfo = (status: string | undefined) => {
  switch (status) {
    case "DONE":
      return {
        color: "success" as const,
        icon: <SuccessIcon />,
        label: "Completed",
      };
    case "STRT":
      return {
        color: "info" as const,
        icon: <RunningIcon />,
        label: "Running",
      };
    case "FAIL":
      return { color: "error" as const, icon: <ErrorIcon />, label: "Failed" };
    case "QUEU":
      return {
        color: "warning" as const,
        icon: <QueueIcon />,
        label: "Queued",
      };
    default:
      return {
        color: "default" as const,
        icon: undefined,
        label: status || "Unknown",
      };
  }
};

/**
 * Format duration from seconds to human readable
 */
const formatDuration = (seconds?: number): string => {
  if (!seconds) return "-";
  if (seconds < 60) return `${seconds}s`;
  if (seconds < 3600) return `${Math.floor(seconds / 60)}m ${seconds % 60}s`;
  return `${Math.floor(seconds / 3600)}h ${Math.floor((seconds % 3600) / 60)}m`;
};

/**
 * Format timestamp to local date/time
 */
const formatTimestamp = (timestamp?: string): string => {
  if (!timestamp) return "-";
  return new Date(timestamp).toLocaleString();
};

/**
 * Main ImportedJobsPage component
 */
const ImportedJobsPage: React.FC = () => {
  // State management
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(25);
  const [filters, setFilters] = useState<ExecutionFilters>({});
  const [expandedRows, setExpandedRows] = useState<Set<number>>(new Set());
  const [favorites, setFavorites] = useState<Set<string>>(new Set());
  const [jobChainData, setJobChainData] = useState<any>(null);
  const [showJobChain, setShowJobChain] = useState(false);

  const queryClient = useQueryClient();

  // Fetch executions with pagination and filters
  const {
    data: executionsPage,
    isLoading,
    error,
    refetch,
  } = useQuery({
    queryKey: ["imported-executions", page, rowsPerPage, filters],
    queryFn: () => importedJobService.getExecutions(page, rowsPerPage, filters),
    staleTime: 30000, // 30 seconds
  });

  // Fetch running jobs for quick stats
  const { data: runningJobs } = useQuery({
    queryKey: ["running-jobs"],
    queryFn: () => importedJobService.getRunningJobs(),
    refetchInterval: 10000, // 10 seconds
  });

  // Fetch recent failures for quick stats
  const { data: recentFailures } = useQuery({
    queryKey: ["recent-failures"],
    queryFn: () => importedJobService.getRecentFailures(),
    refetchInterval: 30000, // 30 seconds
  });

  // Fetch user's favorite jobs
  const { data: favoritesData } = useQuery({
    queryKey: ["user-favorites"],
    queryFn: () => importedJobService.getFavorites(),
  });

  // Update favorites state when data changes
  React.useEffect(() => {
    if (favoritesData) {
      console.log("Favorites data loaded:", favoritesData);
      const favoriteJobNames = new Set(
        favoritesData.map((fav) => fav.favorite?.jobName).filter(Boolean) || [],
      );
      console.log("Extracted job names:", Array.from(favoriteJobNames));
      setFavorites(favoriteJobNames);
    }
  }, [favoritesData]);

  // Manual import trigger
  const importMutation = useMutation({
    mutationFn: () => importedJobService.triggerImport(),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["imported-executions"] });
      queryClient.invalidateQueries({ queryKey: ["running-jobs"] });
      queryClient.invalidateQueries({ queryKey: ["recent-failures"] });
    },
  });

  // Favoriten-Mutations
  const addToFavoritesMutation = useMutation({
    mutationFn: (jobName: string) => importedJobService.addToFavorites(jobName),
    onSuccess: (data, jobName) => {
      setFavorites((prev) => new Set(prev).add(jobName));
      queryClient.invalidateQueries({ queryKey: ["user-favorites"] });
    },
    onError: (error) => {
      console.error("Failed to add to favorites:", error);
      // TODO: Show error message to user
    },
  });

  const removeFromFavoritesMutation = useMutation({
    mutationFn: (jobName: string) =>
      importedJobService.removeFromFavorites(jobName),
    onSuccess: (data, jobName) => {
      setFavorites((prev) => {
        const newFavorites = new Set(prev);
        newFavorites.delete(jobName);
        return newFavorites;
      });
      queryClient.invalidateQueries({ queryKey: ["user-favorites"] });
    },
    onError: (error) => {
      console.error("Failed to remove from favorites:", error);
      // TODO: Show error message to user
    },
  });

  // Handle pagination
  const handlePageChange = useCallback((event: unknown, newPage: number) => {
    setPage(newPage);
  }, []);

  const handleRowsPerPageChange = useCallback(
    (event: React.ChangeEvent<HTMLInputElement>) => {
      setRowsPerPage(parseInt(event.target.value, 10));
      setPage(0);
    },
    [],
  );

  // Handle filters
  const handleFilterChange = useCallback(
    (field: keyof ExecutionFilters, value: string) => {
      setFilters((prev) => ({
        ...prev,
        [field]: value || undefined,
      }));
      setPage(0); // Reset to first page when filtering
    },
    [],
  );

  // Clear all filters
  const clearFilters = useCallback(() => {
    setFilters({});
    setPage(0);
  }, []);

  // Toggle row expansion
  const toggleRowExpansion = useCallback((executionId: number) => {
    setExpandedRows((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(executionId)) {
        newSet.delete(executionId);
      } else {
        newSet.add(executionId);
      }
      return newSet;
    });
  }, []);

  // Handle favorite toggle
  const handleFavoriteToggle = useCallback(
    (jobName: string, event: React.MouseEvent) => {
      event.stopPropagation(); // Prevent row expansion

      if (favorites.has(jobName)) {
        removeFromFavoritesMutation.mutate(jobName);
      } else {
        addToFavoritesMutation.mutate(jobName);
      }
    },
    [favorites, addToFavoritesMutation, removeFromFavoritesMutation],
  );

  // Handle job chain view
  const handleJobChainView = useCallback(
    async (executionId: number, event: React.MouseEvent) => {
      event.stopPropagation(); // Prevent row expansion

      try {
        const chainData = await importedJobService.getJobChain(executionId);
        setJobChainData(chainData);
        setShowJobChain(true);
      } catch (error) {
        console.error("Failed to fetch job chain:", error);
        // TODO: Show error message to user
      }
    },
    [],
  );

  // Get unique values for filter dropdowns
  const uniqueValues = useMemo(() => {
    if (!executionsPage?.content) return {};

    const content = executionsPage.content;
    return {
      statuses: [...new Set(content.map((ex) => ex.status).filter(Boolean))],
      jobTypes: [...new Set(content.map((ex) => ex.jobType).filter(Boolean))],
      hosts: [...new Set(content.map((ex) => ex.host).filter(Boolean))],
      submitters: [
        ...new Set(content.map((ex) => ex.submittedBy).filter(Boolean)),
      ],
    };
  }, [executionsPage?.content]);

  if (error) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="error">
          Failed to load imported jobs:{" "}
          {error instanceof Error ? error.message : "Unknown error"}
        </Alert>
      </Box>
    );
  }

  return (
    <Box sx={{ p: 3 }}>
      {/* Header */}
      <Box
        sx={{
          mb: 3,
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <Typography variant="h4" component="h1">
          Imported Job Executions
        </Typography>
        <Stack direction="row" spacing={2}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={() => refetch()}
            disabled={isLoading}
          >
            Refresh
          </Button>
          <Button
            variant="contained"
            startIcon={<ImportIcon />}
            onClick={() => importMutation.mutate()}
            disabled={importMutation.isPending}
          >
            Trigger Import
          </Button>
        </Stack>
      </Box>

      {/* Quick Stats */}
      <Grid container spacing={3} sx={{ mb: 3 }}>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" color="info.main">
                Running Jobs
              </Typography>
              <Typography variant="h4">{runningJobs?.length || 0}</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" color="error.main">
                Recent Failures
              </Typography>
              <Typography variant="h4">
                {recentFailures?.length || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" color="text.secondary">
                Total Executions
              </Typography>
              <Typography variant="h4">
                {executionsPage?.totalElements || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} md={3}>
          <Card>
            <CardContent>
              <Typography variant="h6" color="text.secondary">
                Pages
              </Typography>
              <Typography variant="h4">
                {executionsPage?.totalPages || 0}
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Filters */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" sx={{ mb: 2 }}>
            Filters
          </Typography>
          <Grid container spacing={2}>
            <Grid item xs={12} md={2}>
              <TextField
                fullWidth
                label="Job Name"
                value={filters.jobName || ""}
                onChange={(e) => handleFilterChange("jobName", e.target.value)}
                size="small"
              />
            </Grid>
            <Grid item xs={12} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>Status</InputLabel>
                <Select
                  value={filters.status || ""}
                  label="Status"
                  onChange={(e) => handleFilterChange("status", e.target.value)}
                >
                  <MenuItem value="">All</MenuItem>
                  {uniqueValues.statuses?.map((status) => (
                    <MenuItem key={status} value={status}>
                      {status}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>Job Type</InputLabel>
                <Select
                  value={filters.jobType || ""}
                  label="Job Type"
                  onChange={(e) =>
                    handleFilterChange("jobType", e.target.value)
                  }
                >
                  <MenuItem value="">All</MenuItem>
                  {uniqueValues.jobTypes?.map((type) => (
                    <MenuItem key={type} value={type}>
                      {type}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>Host</InputLabel>
                <Select
                  value={filters.host || ""}
                  label="Host"
                  onChange={(e) => handleFilterChange("host", e.target.value)}
                >
                  <MenuItem value="">All</MenuItem>
                  {uniqueValues.hosts?.map((host) => (
                    <MenuItem key={host} value={host}>
                      {host}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={2}>
              <FormControl fullWidth size="small">
                <InputLabel>Submitter</InputLabel>
                <Select
                  value={filters.submittedBy || ""}
                  label="Submitter"
                  onChange={(e) =>
                    handleFilterChange("submittedBy", e.target.value)
                  }
                >
                  <MenuItem value="">All</MenuItem>
                  {uniqueValues.submitters?.map((submitter) => (
                    <MenuItem key={submitter} value={submitter}>
                      {submitter}
                    </MenuItem>
                  ))}
                </Select>
              </FormControl>
            </Grid>
            <Grid item xs={12} md={2}>
              <Button
                fullWidth
                variant="outlined"
                onClick={clearFilters}
                size="small"
              >
                Clear Filters
              </Button>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      {/* Executions Table */}
      <Card>
        <CardContent sx={{ p: 0 }}>
          <TableContainer component={Paper} elevation={0}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell></TableCell>
                  <TableCell>Execution ID</TableCell>
                  <TableCell>Job Name</TableCell>
                  <TableCell>Type</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Submitted</TableCell>
                  <TableCell>Started</TableCell>
                  <TableCell>Duration</TableCell>
                  <TableCell>Host</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {isLoading ? (
                  <TableRow>
                    <TableCell colSpan={10} align="center">
                      <CircularProgress />
                    </TableCell>
                  </TableRow>
                ) : executionsPage?.content?.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={10} align="center">
                      No executions found
                    </TableCell>
                  </TableRow>
                ) : (
                  executionsPage?.content?.map((execution) => {
                    const statusInfo = getStatusInfo(execution.status);
                    const isExpanded = expandedRows.has(execution.executionId);

                    return (
                      <React.Fragment key={execution.executionId}>
                        <TableRow hover>
                          <TableCell>
                            <IconButton
                              size="small"
                              onClick={() =>
                                toggleRowExpansion(execution.executionId)
                              }
                            >
                              {isExpanded ? (
                                <ExpandLessIcon />
                              ) : (
                                <ExpandMoreIcon />
                              )}
                            </IconButton>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2" fontWeight="medium">
                              {execution.executionId}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {execution.jobName}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={execution.jobType}
                              size="small"
                              variant="outlined"
                            />
                          </TableCell>
                          <TableCell>
                            <Chip
                              label={statusInfo.label}
                              color={statusInfo.color}
                              {...(statusInfo.icon && {
                                icon: statusInfo.icon,
                              })}
                              size="small"
                            />
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {formatTimestamp(execution.submittedAt)}
                            </Typography>
                            <Typography
                              variant="caption"
                              color="text.secondary"
                            >
                              by {execution.submittedBy}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {formatTimestamp(execution.startedAt)}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {formatDuration(execution.durationSeconds)}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Typography variant="body2">
                              {execution.host}
                            </Typography>
                          </TableCell>
                          <TableCell>
                            <Stack direction="row" spacing={1}>
                              {execution.parentExecutionId && (
                                <Tooltip title="View Job Chain">
                                  <IconButton
                                    size="small"
                                    onClick={(e) =>
                                      handleJobChainView(
                                        execution.executionId,
                                        e,
                                      )
                                    }
                                  >
                                    <ChainIcon fontSize="small" />
                                  </IconButton>
                                </Tooltip>
                              )}
                              <Tooltip
                                title={
                                  favorites.has(execution.jobName)
                                    ? "Remove from Favorites"
                                    : "Add to Favorites"
                                }
                              >
                                <IconButton
                                  size="small"
                                  onClick={(e) =>
                                    handleFavoriteToggle(execution.jobName, e)
                                  }
                                  color={
                                    favorites.has(execution.jobName)
                                      ? "error"
                                      : "default"
                                  }
                                >
                                  {favorites.has(execution.jobName) ? (
                                    <FavoriteIcon fontSize="small" />
                                  ) : (
                                    <FavoriteBorderIcon fontSize="small" />
                                  )}
                                </IconButton>
                              </Tooltip>
                            </Stack>
                          </TableCell>
                        </TableRow>

                        {/* Expanded Row Details */}
                        <TableRow>
                          <TableCell colSpan={10} sx={{ p: 0 }}>
                            <Collapse
                              in={isExpanded}
                              timeout="auto"
                              unmountOnExit
                            >
                              <Box sx={{ p: 2, backgroundColor: "grey.50" }}>
                                <Grid container spacing={2}>
                                  <Grid item xs={12} md={6}>
                                    <Typography
                                      variant="subtitle2"
                                      gutterBottom
                                    >
                                      Script Details
                                    </Typography>
                                    <Typography
                                      variant="body2"
                                      color="text.secondary"
                                    >
                                      <strong>Script:</strong>{" "}
                                      {execution.scriptPath || "N/A"}
                                    </Typography>
                                    <Typography
                                      variant="body2"
                                      color="text.secondary"
                                    >
                                      <strong>Priority:</strong>{" "}
                                      {execution.priority || "N/A"}
                                    </Typography>
                                    <Typography
                                      variant="body2"
                                      color="text.secondary"
                                    >
                                      <strong>Strategy:</strong>{" "}
                                      {execution.strategy || "N/A"}
                                    </Typography>
                                  </Grid>
                                  <Grid item xs={12} md={6}>
                                    <Typography
                                      variant="subtitle2"
                                      gutterBottom
                                    >
                                      Import Information
                                    </Typography>
                                    <Typography
                                      variant="body2"
                                      color="text.secondary"
                                    >
                                      <strong>Source File:</strong>{" "}
                                      {execution.csvSourceFile}
                                    </Typography>
                                    <Typography
                                      variant="body2"
                                      color="text.secondary"
                                    >
                                      <strong>Import Time:</strong>{" "}
                                      {formatTimestamp(
                                        execution.importTimestamp,
                                      )}
                                    </Typography>
                                    {execution.parentExecutionId && (
                                      <Typography
                                        variant="body2"
                                        color="text.secondary"
                                      >
                                        <strong>Parent Execution:</strong>{" "}
                                        {execution.parentExecutionId}
                                      </Typography>
                                    )}
                                  </Grid>
                                  {execution.endedAt && (
                                    <Grid item xs={12}>
                                      <Typography
                                        variant="body2"
                                        color="text.secondary"
                                      >
                                        <strong>Completed:</strong>{" "}
                                        {formatTimestamp(execution.endedAt)}
                                      </Typography>
                                    </Grid>
                                  )}
                                </Grid>
                              </Box>
                            </Collapse>
                          </TableCell>
                        </TableRow>
                      </React.Fragment>
                    );
                  })
                )}
              </TableBody>
            </Table>
          </TableContainer>

          {/* Pagination */}
          {executionsPage && (
            <TablePagination
              component="div"
              count={executionsPage.totalElements}
              page={page}
              onPageChange={handlePageChange}
              rowsPerPage={rowsPerPage}
              onRowsPerPageChange={handleRowsPerPageChange}
              rowsPerPageOptions={[10, 25, 50, 100]}
            />
          )}
        </CardContent>
      </Card>

      {/* Import Status */}
      {importMutation.isSuccess && (
        <Alert severity="success" sx={{ mt: 2 }}>
          Import completed: {importMutation.data.imported} executions imported
        </Alert>
      )}

      {importMutation.isError && (
        <Alert severity="error" sx={{ mt: 2 }}>
          Import failed:{" "}
          {importMutation.error instanceof Error
            ? importMutation.error.message
            : "Unknown error"}
        </Alert>
      )}

      {/* Job Chain Dialog */}
      <Dialog
        open={showJobChain}
        onClose={() => setShowJobChain(false)}
        maxWidth="lg"
        fullWidth
      >
        <DialogTitle>
          <Box display="flex" alignItems="center" gap={1}>
            <AccountTreeIcon />
            Job Chain Visualization
          </Box>
        </DialogTitle>
        <DialogContent>
          {jobChainData ? (
            <JobChainVisualization
              rootExecution={jobChainData.rootExecution}
              chainTree={jobChainData.chainTree}
            />
          ) : (
            <Box display="flex" justifyContent="center" p={4}>
              <CircularProgress />
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowJobChain(false)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

/**
 * Job Chain Visualization Component
 */
interface JobChainVisualizationProps {
  rootExecution: ImportedJobExecution;
  chainTree: Array<{
    execution: ImportedJobExecution;
    children: Array<any>;
  }>;
}

const JobChainVisualization: React.FC<JobChainVisualizationProps> = ({
  rootExecution,
  chainTree,
}) => {
  const renderJobNode = (
    execution: ImportedJobExecution,
    level: number = 0,
  ) => {
    const statusInfo = getStatusInfo(execution.status);

    return (
      <Card
        key={execution.executionId}
        variant="outlined"
        sx={{
          mb: 1,
          ml: level * 4,
          borderLeft: level > 0 ? "3px solid #e0e0e0" : "none",
        }}
      >
        <CardContent sx={{ py: 2 }}>
          <Box
            display="flex"
            alignItems="center"
            justifyContent="space-between"
          >
            <Box flex={1}>
              <Box display="flex" alignItems="center" gap={1} mb={1}>
                <Typography variant="h6" component="div">
                  {execution.jobName}
                </Typography>
                <Chip
                  icon={statusInfo.icon}
                  label={statusInfo.label}
                  color={statusInfo.color}
                  size="small"
                />
              </Box>

              <Grid container spacing={2}>
                <Grid item xs={6} md={3}>
                  <Typography variant="caption" color="text.secondary">
                    Execution ID
                  </Typography>
                  <Typography variant="body2" fontWeight="medium">
                    {execution.executionId}
                  </Typography>
                </Grid>

                <Grid item xs={6} md={3}>
                  <Typography variant="caption" color="text.secondary">
                    Job Type
                  </Typography>
                  <Typography variant="body2" fontWeight="medium">
                    {execution.jobType || "N/A"}
                  </Typography>
                </Grid>

                <Grid item xs={6} md={3}>
                  <Typography variant="caption" color="text.secondary">
                    Started At
                  </Typography>
                  <Typography variant="body2" fontWeight="medium">
                    {execution.startedAt
                      ? new Date(execution.startedAt).toLocaleString()
                      : "Not started"}
                  </Typography>
                </Grid>

                <Grid item xs={6} md={3}>
                  <Typography variant="caption" color="text.secondary">
                    Duration
                  </Typography>
                  <Typography variant="body2" fontWeight="medium">
                    {execution.durationSeconds
                      ? `${execution.durationSeconds}s`
                      : "N/A"}
                  </Typography>
                </Grid>

                {execution.host && (
                  <Grid item xs={6} md={3}>
                    <Typography variant="caption" color="text.secondary">
                      Host
                    </Typography>
                    <Typography variant="body2" fontWeight="medium">
                      {execution.host}
                    </Typography>
                  </Grid>
                )}

                {execution.submittedBy && (
                  <Grid item xs={6} md={3}>
                    <Typography variant="caption" color="text.secondary">
                      Submitted By
                    </Typography>
                    <Typography variant="body2" fontWeight="medium">
                      {execution.submittedBy}
                    </Typography>
                  </Grid>
                )}
              </Grid>
            </Box>
          </Box>
        </CardContent>
      </Card>
    );
  };

  const renderChainTree = (nodes: Array<any>, level: number = 1) => {
    return nodes.map((node, index) => (
      <Box key={`${node.execution.executionId}-${index}`}>
        {renderJobNode(node.execution, level)}
        {node.children && node.children.length > 0 && (
          <Box>{renderChainTree(node.children, level + 1)}</Box>
        )}
      </Box>
    ));
  };

  return (
    <Box>
      {/* Root Job */}
      <Box mb={3}>
        <Typography variant="h5" gutterBottom>
          Root Job Chain
        </Typography>
        {renderJobNode(rootExecution, 0)}
      </Box>

      {/* Child Jobs */}
      {chainTree && chainTree.length > 0 && (
        <Box>
          <Typography variant="h5" gutterBottom>
            Child Jobs
          </Typography>
          {renderChainTree(chainTree, 1)}
        </Box>
      )}

      {/* Summary */}
      <Box mt={3} p={2} bgcolor="grey.50" borderRadius={1}>
        <Typography variant="h6" gutterBottom>
          Chain Summary
        </Typography>
        <Grid container spacing={2}>
          <Grid item xs={6} md={3}>
            <Typography variant="caption" color="text.secondary">
              Total Jobs
            </Typography>
            <Typography variant="h6">
              {1 + (chainTree ? chainTree.length : 0)}
            </Typography>
          </Grid>
          <Grid item xs={6} md={3}>
            <Typography variant="caption" color="text.secondary">
              Root Job
            </Typography>
            <Typography variant="body2" fontWeight="medium">
              {rootExecution.jobName}
            </Typography>
          </Grid>
          <Grid item xs={6} md={3}>
            <Typography variant="caption" color="text.secondary">
              Chain Status
            </Typography>
            <Chip
              icon={getStatusInfo(rootExecution.status).icon}
              label={getStatusInfo(rootExecution.status).label}
              color={getStatusInfo(rootExecution.status).color}
              size="small"
            />
          </Grid>
        </Grid>
      </Box>
    </Box>
  );
};

export default ImportedJobsPage;
