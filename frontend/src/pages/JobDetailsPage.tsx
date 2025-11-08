/**
 * Job Details Page for Phase 2 Job Monitoring
 *
 * Displays detailed information about a single job including execution history,
 * statistics, logs, and management actions.
 */

import React, { useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import {
  Box,
  Typography,
  Button,
  Card,
  CardContent,
  Chip,
  Grid,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
  CircularProgress,
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemText,
  Divider,
  LinearProgress,
  Accordion,
  AccordionSummary,
  AccordionDetails,
} from "@mui/material";
import {
  ArrowBack as BackIcon,
  PlayArrow as PlayIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Visibility as ViewIcon,
  Cancel as CancelIcon,
  Refresh as RefreshIcon,
  ExpandMore as ExpandMoreIcon,
  Schedule as ScheduleIcon,
  Computer as ComputerIcon,
  Timeline as TimelineIcon,
} from "@mui/icons-material";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { jobService } from "@/services/jobService";
import type { Job, JobExecution, ExecutionStatus } from "@/types/job";

/**
 * Status color mapping
 */
const getStatusColor = (status: ExecutionStatus) => {
  switch (status) {
    case "SUCCESS":
      return "success";
    case "FAILED":
    case "CANCELLED":
    case "TIMEOUT":
      return "error";
    case "RUNNING":
      return "info";
    case "QUEUED":
    case "RETRYING":
      return "warning";
    default:
      return "default";
  }
};

/**
 * Format date for display
 */
const formatDate = (dateStr: string) => {
  return new Date(dateStr).toLocaleString();
};

/**
 * Format duration for display
 */
const formatDuration = (durationMs?: number) => {
  if (!durationMs) return "N/A";

  const seconds = Math.floor(durationMs / 1000);
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
 * Execution output dialog component
 */
interface ExecutionOutputDialogProps {
  execution: JobExecution | null;
  open: boolean;
  onClose: () => void;
}

const ExecutionOutputDialog: React.FC<ExecutionOutputDialogProps> = ({
  execution,
  open,
  onClose,
}) => {
  const { data: output, isLoading } = useQuery({
    queryKey: ["executionOutput", execution?.id],
    queryFn: () =>
      execution ? jobService.getExecutionOutput(execution.id) : null,
    enabled: !!execution && open,
  });

  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        Execution Output - {execution?.jobName} #{execution?.executionNumber}
      </DialogTitle>
      <DialogContent>
        {isLoading ? (
          <CircularProgress />
        ) : (
          <Box>
            {output?.output && (
              <Accordion defaultExpanded>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography variant="h6">Standard Output</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Paper
                    variant="outlined"
                    sx={{
                      p: 2,
                      bgcolor: "grey.50",
                      fontFamily: "monospace",
                      fontSize: "0.875rem",
                      whiteSpace: "pre-wrap",
                      maxHeight: "300px",
                      overflow: "auto",
                    }}
                  >
                    {output.output}
                  </Paper>
                </AccordionDetails>
              </Accordion>
            )}

            {output?.errorOutput && (
              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography variant="h6">Error Output</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <Paper
                    variant="outlined"
                    sx={{
                      p: 2,
                      bgcolor: "error.light",
                      color: "error.contrastText",
                      fontFamily: "monospace",
                      fontSize: "0.875rem",
                      whiteSpace: "pre-wrap",
                      maxHeight: "300px",
                      overflow: "auto",
                    }}
                  >
                    {output.errorOutput}
                  </Paper>
                </AccordionDetails>
              </Accordion>
            )}
          </Box>
        )}
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Close</Button>
      </DialogActions>
    </Dialog>
  );
};

/**
 * Tab panel component
 */
interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => {
  return (
    <div hidden={value !== index}>
      {value === index && <Box sx={{ pt: 3 }}>{children}</Box>}
    </div>
  );
};

/**
 * Main Job Details page component
 */
const JobDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const [tabValue, setTabValue] = useState(0);
  const [selectedExecution, setSelectedExecution] =
    useState<JobExecution | null>(null);
  const [outputDialogOpen, setOutputDialogOpen] = useState(false);

  const jobId = parseInt(id || "0");

  // Fetch job details
  const {
    data: job,
    isLoading: jobLoading,
    error: jobError,
  } = useQuery({
    queryKey: ["job", jobId],
    queryFn: () => jobService.getJob(jobId),
    enabled: !!jobId,
  });

  // Fetch job executions
  const { data: executionsData, isLoading: executionsLoading } = useQuery({
    queryKey: ["jobExecutions", jobId],
    queryFn: () => jobService.getJobExecutions(jobId),
    enabled: !!jobId,
    refetchInterval: 10000, // Refresh every 10 seconds
  });

  // Execute job mutation
  const executeJobMutation = useMutation({
    mutationFn: () => jobService.executeJob({ jobId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["job", jobId] });
      queryClient.invalidateQueries({ queryKey: ["jobExecutions", jobId] });
    },
  });

  // Cancel execution mutation
  const cancelExecutionMutation = useMutation({
    mutationFn: (executionId: number) =>
      jobService.cancelExecution(executionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["jobExecutions", jobId] });
    },
  });

  const executions = executionsData?.executions || [];

  const handleExecuteJob = () => {
    executeJobMutation.mutate();
  };

  const handleEditJob = () => {
    navigate(`/jobs/${jobId}/edit`);
  };

  const handleViewOutput = (execution: JobExecution) => {
    setSelectedExecution(execution);
    setOutputDialogOpen(true);
  };

  const handleCancelExecution = (execution: JobExecution) => {
    cancelExecutionMutation.mutate(execution.id);
  };

  const handleRefresh = () => {
    queryClient.invalidateQueries({ queryKey: ["job", jobId] });
    queryClient.invalidateQueries({ queryKey: ["jobExecutions", jobId] });
  };

  if (jobError) {
    return (
      <Alert severity="error" sx={{ mt: 2 }}>
        Failed to load job details: {(jobError as Error).message}
      </Alert>
    );
  }

  if (jobLoading || !job) {
    return (
      <Box display="flex" justifyContent="center" py={4}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      {/* Header */}
      <Box display="flex" alignItems="center" mb={3}>
        <IconButton onClick={() => navigate("/jobs")} sx={{ mr: 2 }}>
          <BackIcon />
        </IconButton>
        <Box flex={1}>
          <Typography variant="h4" component="h1">
            {job.name}
          </Typography>
          <Typography variant="subtitle1" color="text.secondary">
            {job.description || "No description"}
          </Typography>
        </Box>
        <Box display="flex" gap={2}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={handleRefresh}
          >
            Refresh
          </Button>
          <Button
            variant="outlined"
            startIcon={<EditIcon />}
            onClick={handleEditJob}
          >
            Edit
          </Button>
          <Button
            variant="contained"
            startIcon={<PlayIcon />}
            onClick={handleExecuteJob}
            disabled={!job.isEnabled || executeJobMutation.isPending}
          >
            {executeJobMutation.isPending ? "Starting..." : "Execute"}
          </Button>
        </Box>
      </Box>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: "divider", mb: 3 }}>
        <Tabs
          value={tabValue}
          onChange={(_, newValue) => setTabValue(newValue)}
        >
          <Tab label="Overview" icon={<TimelineIcon />} iconPosition="start" />
          <Tab
            label="Execution History"
            icon={<ScheduleIcon />}
            iconPosition="start"
          />
          <Tab
            label="Configuration"
            icon={<ComputerIcon />}
            iconPosition="start"
          />
        </Tabs>
      </Box>

      {/* Overview Tab */}
      <TabPanel value={tabValue} index={0}>
        <Grid container spacing={3}>
          {/* Job Status */}
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Job Status
                </Typography>
                <Box display="flex" alignItems="center" gap={2} mb={2}>
                  <Chip
                    label={job.status}
                    color={job.status === "ACTIVE" ? "success" : "default"}
                  />
                  <Chip
                    label={job.isEnabled ? "Enabled" : "Disabled"}
                    color={job.isEnabled ? "success" : "warning"}
                  />
                </Box>
                <Typography variant="body2" color="text.secondary">
                  Priority: {job.priority}
                </Typography>
              </CardContent>
            </Card>
          </Grid>

          {/* Statistics */}
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Statistics
                </Typography>
                <List dense>
                  <ListItem>
                    <ListItemText
                      primary="Total Executions"
                      secondary={job.totalExecutions}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemText
                      primary="Success Rate"
                      secondary={`${Math.round(job.successRate)}%`}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemText
                      primary="Average Duration"
                      secondary={formatDuration(job.averageDuration)}
                    />
                  </ListItem>
                </List>
              </CardContent>
            </Card>
          </Grid>

          {/* Last Execution */}
          <Grid item xs={12} md={4}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Last Execution
                </Typography>
                {job.lastExecution ? (
                  <Box>
                    <Box display="flex" alignItems="center" gap={1} mb={1}>
                      <Chip
                        label={job.lastExecutionStatus}
                        color={getStatusColor(job.lastExecutionStatus!) as any}
                        size="small"
                      />
                    </Box>
                    <Typography variant="body2" color="text.secondary">
                      {formatDate(job.lastExecution)}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      Duration: {formatDuration(job.lastExecutionDuration)}
                    </Typography>
                  </Box>
                ) : (
                  <Typography color="text.secondary">
                    No executions yet
                  </Typography>
                )}
              </CardContent>
            </Card>
          </Grid>

          {/* Next Execution */}
          {job.nextExecution && (
            <Grid item xs={12}>
              <Alert severity="info">
                Next scheduled execution: {formatDate(job.nextExecution)}
              </Alert>
            </Grid>
          )}
        </Grid>
      </TabPanel>

      {/* Execution History Tab */}
      <TabPanel value={tabValue} index={1}>
        {executionsLoading ? (
          <Box display="flex" justifyContent="center" py={4}>
            <CircularProgress />
          </Box>
        ) : (
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>#</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Started</TableCell>
                  <TableCell>Duration</TableCell>
                  <TableCell>Trigger</TableCell>
                  <TableCell>Exit Code</TableCell>
                  <TableCell>Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {executions.map((execution) => (
                  <TableRow key={execution.id}>
                    <TableCell>#{execution.executionNumber}</TableCell>
                    <TableCell>
                      <Chip
                        label={execution.status}
                        color={getStatusColor(execution.status) as any}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>
                      {execution.startedAt
                        ? formatDate(execution.startedAt)
                        : "N/A"}
                    </TableCell>
                    <TableCell>
                      {formatDuration(execution.durationMs)}
                    </TableCell>
                    <TableCell>{execution.triggerType}</TableCell>
                    <TableCell>{execution.exitCode ?? "N/A"}</TableCell>
                    <TableCell>
                      <Tooltip title="View Output">
                        <IconButton
                          size="small"
                          onClick={() => handleViewOutput(execution)}
                        >
                          <ViewIcon />
                        </IconButton>
                      </Tooltip>
                      {execution.status === "RUNNING" && (
                        <Tooltip title="Cancel">
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => handleCancelExecution(execution)}
                            disabled={cancelExecutionMutation.isPending}
                          >
                            <CancelIcon />
                          </IconButton>
                        </Tooltip>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        )}

        {!executionsLoading && executions.length === 0 && (
          <Box textAlign="center" py={8}>
            <ScheduleIcon
              sx={{ fontSize: 64, color: "text.secondary", mb: 2 }}
            />
            <Typography variant="h6" color="text.secondary" gutterBottom>
              No executions yet
            </Typography>
            <Typography variant="body2" color="text.secondary" mb={3}>
              This job hasn't been executed yet
            </Typography>
            <Button
              variant="contained"
              startIcon={<PlayIcon />}
              onClick={handleExecuteJob}
              disabled={!job.isEnabled}
            >
              Execute Now
            </Button>
          </Box>
        )}
      </TabPanel>

      {/* Configuration Tab */}
      <TabPanel value={tabValue} index={2}>
        <Grid container spacing={3}>
          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Command Configuration
                </Typography>
                <List>
                  <ListItem>
                    <ListItemText primary="Command" secondary={job.command} />
                  </ListItem>
                  {job.arguments && (
                    <ListItem>
                      <ListItemText
                        primary="Arguments"
                        secondary={job.arguments}
                      />
                    </ListItem>
                  )}
                  {job.workingDirectory && (
                    <ListItem>
                      <ListItemText
                        primary="Working Directory"
                        secondary={job.workingDirectory}
                      />
                    </ListItem>
                  )}
                </List>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} md={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Execution Settings
                </Typography>
                <List>
                  <ListItem>
                    <ListItemText
                      primary="Max Retries"
                      secondary={job.maxRetries}
                    />
                  </ListItem>
                  <ListItem>
                    <ListItemText
                      primary="Timeout"
                      secondary={`${job.timeoutSeconds}s`}
                    />
                  </ListItem>
                  {job.cronExpression && (
                    <ListItem>
                      <ListItemText
                        primary="Cron Expression"
                        secondary={job.cronExpression}
                      />
                    </ListItem>
                  )}
                </List>
              </CardContent>
            </Card>
          </Grid>

          {job.environmentVariables && (
            <Grid item xs={12}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    Environment Variables
                  </Typography>
                  <Paper
                    variant="outlined"
                    sx={{
                      p: 2,
                      bgcolor: "grey.50",
                      fontFamily: "monospace",
                      fontSize: "0.875rem",
                      whiteSpace: "pre-wrap",
                    }}
                  >
                    {job.environmentVariables}
                  </Paper>
                </CardContent>
              </Card>
            </Grid>
          )}
        </Grid>
      </TabPanel>

      {/* Execution Output Dialog */}
      <ExecutionOutputDialog
        execution={selectedExecution}
        open={outputDialogOpen}
        onClose={() => setOutputDialogOpen(false)}
      />
    </Box>
  );
};

export default JobDetailsPage;
