/**
 * Job Form Page for Phase 2 Job Monitoring
 *
 * Handles both creating new jobs and editing existing jobs.
 */

import React, { useState, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useForm, Controller } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  Box,
  Typography,
  Button,
  Card,
  CardContent,
  Grid,
  TextField,
  FormControl,
  FormControlLabel,
  FormLabel,
  RadioGroup,
  Radio,
  Switch,
  Alert,
  CircularProgress,
  IconButton,
  Tooltip,
  Chip,
  MenuItem,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  Divider,
  InputAdornment,
} from "@mui/material";
import {
  ArrowBack as BackIcon,
  Save as SaveIcon,
  Help as HelpIcon,
  ExpandMore as ExpandMoreIcon,
  Add as AddIcon,
  Remove as RemoveIcon,
} from "@mui/icons-material";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { jobService } from "@/services/jobService";
import type {
  Job,
  CreateJobRequest,
  UpdateJobRequest,
  TriggerType,
} from "@/types/job";

/**
 * Job form validation schema
 */
const jobFormSchema = z.object({
  name: z.string().min(1, "Job name is required").max(255, "Name too long"),
  description: z.string().optional(),
  command: z.string().min(1, "Command is required"),
  arguments: z.string().optional(),
  workingDirectory: z.string().optional(),
  environmentVariables: z.string().optional(),
  isEnabled: z.boolean(),
  priority: z.enum(["LOW", "NORMAL", "HIGH", "CRITICAL"]),
  maxRetries: z.number().min(0).max(10),
  timeoutSeconds: z.number().min(1).max(86400), // Max 1 day
  triggerType: z.enum(["MANUAL", "SCHEDULED", "EVENT"]),
  cronExpression: z.string().optional(),
  tags: z.string().optional(),
});

type JobFormData = z.infer<typeof jobFormSchema>;

/**
 * Default form values
 */
const defaultValues: JobFormData = {
  name: "",
  description: "",
  command: "",
  arguments: "",
  workingDirectory: "",
  environmentVariables: "",
  isEnabled: true,
  priority: "NORMAL",
  maxRetries: 3,
  timeoutSeconds: 3600, // 1 hour
  triggerType: "MANUAL",
  cronExpression: "",
  tags: "",
};

/**
 * Job Form component
 */
const JobFormPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const jobId = id ? parseInt(id) : null;
  const isEditing = !!jobId;

  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    control,
    handleSubmit,
    reset,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<JobFormData>({
    resolver: zodResolver(jobFormSchema),
    defaultValues,
  });

  const triggerType = watch("triggerType");

  // Fetch job for editing
  const {
    data: job,
    isLoading: jobLoading,
    error: jobError,
  } = useQuery({
    queryKey: ["job", jobId],
    queryFn: () => jobService.getJob(jobId!),
    enabled: !!jobId,
  });

  // Initialize form with job data when editing
  useEffect(() => {
    if (job && isEditing) {
      reset({
        name: job.name,
        description: job.description || "",
        command: job.command,
        arguments: job.arguments || "",
        workingDirectory: job.workingDirectory || "",
        environmentVariables: job.environmentVariables || "",
        isEnabled: job.isEnabled,
        priority: job.priority,
        maxRetries: job.maxRetries,
        timeoutSeconds: job.timeoutSeconds,
        triggerType: job.triggerType,
        cronExpression: job.cronExpression || "",
        tags: job.tags?.join(", ") || "",
      });
    }
  }, [job, isEditing, reset]);

  // Create/Update job mutations
  const createJobMutation = useMutation({
    mutationFn: (data: CreateJobRequest) => jobService.createJob(data),
    onSuccess: (newJob) => {
      queryClient.invalidateQueries({ queryKey: ["jobs"] });
      navigate(`/jobs/${newJob.id}`);
    },
    onError: (error: Error) => {
      setSubmitError(error.message);
    },
  });

  const updateJobMutation = useMutation({
    mutationFn: (data: UpdateJobRequest) => jobService.updateJob(jobId!, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["job", jobId] });
      queryClient.invalidateQueries({ queryKey: ["jobs"] });
      navigate(`/jobs/${jobId}`);
    },
    onError: (error: Error) => {
      setSubmitError(error.message);
    },
  });

  const onSubmit = async (data: JobFormData) => {
    setSubmitError(null);

    const jobData: CreateJobRequest = {
      name: data.name,
      description: data.description || undefined,
      command: data.command,
      arguments: data.arguments || undefined,
      workingDirectory: data.workingDirectory || undefined,
      environmentVariables: data.environmentVariables || undefined,
      isEnabled: data.isEnabled,
      priority: data.priority,
      maxRetries: data.maxRetries,
      timeoutSeconds: data.timeoutSeconds,
      triggerType: data.triggerType,
      cronExpression:
        data.triggerType === "SCHEDULED" ? data.cronExpression : undefined,
      tags: data.tags
        ? data.tags
            .split(",")
            .map((tag) => tag.trim())
            .filter((tag) => tag)
        : undefined,
    };

    if (isEditing) {
      const updateData: UpdateJobRequest = {
        id: jobId!,
        ...jobData,
      };
      updateJobMutation.mutate(updateData);
    } else {
      createJobMutation.mutate(jobData);
    }
  };
  if (isEditing && jobError) {
    return (
      <Alert severity="error" sx={{ mt: 2 }}>
        Failed to load job: {(jobError as Error).message}
      </Alert>
    );
  }

  if (isEditing && jobLoading) {
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
        <IconButton onClick={() => navigate(-1)} sx={{ mr: 2 }}>
          <BackIcon />
        </IconButton>
        <Typography variant="h4" component="h1">
          {isEditing ? `Edit Job: ${job?.name}` : "Create New Job"}
        </Typography>
      </Box>

      {/* Form */}
      <form onSubmit={handleSubmit(onSubmit)}>
        <Grid container spacing={3}>
          {/* Basic Information */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Basic Information
                </Typography>

                <Grid container spacing={2}>
                  <Grid item xs={12} md={6}>
                    <Controller
                      name="name"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Job Name"
                          fullWidth
                          required
                          error={!!errors.name}
                          helperText={errors.name?.message}
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <Controller
                      name="priority"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Priority"
                          select
                          fullWidth
                          error={!!errors.priority}
                          helperText={
                            errors.priority?.message || "Job execution priority"
                          }
                        >
                          <MenuItem value="LOW">Low</MenuItem>
                          <MenuItem value="NORMAL">Normal</MenuItem>
                          <MenuItem value="HIGH">High</MenuItem>
                          <MenuItem value="CRITICAL">Critical</MenuItem>
                        </TextField>
                      )}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Controller
                      name="description"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Description"
                          fullWidth
                          multiline
                          rows={2}
                          error={!!errors.description}
                          helperText={errors.description?.message}
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Controller
                      name="tags"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Tags"
                          fullWidth
                          placeholder="tag1, tag2, tag3"
                          helperText="Comma-separated tags for organizing jobs"
                        />
                      )}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>

          {/* Command Configuration */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Command Configuration
                </Typography>

                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <Controller
                      name="command"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Command"
                          fullWidth
                          required
                          placeholder="/bin/bash"
                          error={!!errors.command}
                          helperText={errors.command?.message}
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Controller
                      name="arguments"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Arguments"
                          fullWidth
                          placeholder="-c 'echo Hello World'"
                          helperText="Command line arguments"
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Controller
                      name="workingDirectory"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Working Directory"
                          fullWidth
                          placeholder="/path/to/directory"
                          helperText="Directory to execute the command in"
                        />
                      )}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>

          {/* Execution Settings */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Execution Settings
                </Typography>

                <Grid container spacing={2} alignItems="center">
                  <Grid item xs={12} md={6}>
                    <Controller
                      name="maxRetries"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Max Retries"
                          type="number"
                          fullWidth
                          inputProps={{ min: 0, max: 10 }}
                          error={!!errors.maxRetries}
                          helperText={errors.maxRetries?.message}
                          onChange={(e) =>
                            field.onChange(parseInt(e.target.value))
                          }
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12} md={6}>
                    <Controller
                      name="timeoutSeconds"
                      control={control}
                      render={({ field }) => (
                        <TextField
                          {...field}
                          label="Timeout (seconds)"
                          type="number"
                          fullWidth
                          inputProps={{ min: 1, max: 86400 }}
                          error={!!errors.timeoutSeconds}
                          helperText={errors.timeoutSeconds?.message}
                          onChange={(e) =>
                            field.onChange(parseInt(e.target.value))
                          }
                        />
                      )}
                    />
                  </Grid>

                  <Grid item xs={12}>
                    <Controller
                      name="isEnabled"
                      control={control}
                      render={({ field }) => (
                        <FormControlLabel
                          control={<Switch {...field} checked={field.value} />}
                          label="Enable Job"
                        />
                      )}
                    />
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
          </Grid>

          {/* Trigger Configuration */}
          <Grid item xs={12}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Trigger Configuration
                </Typography>

                <Grid container spacing={2}>
                  <Grid item xs={12}>
                    <Controller
                      name="triggerType"
                      control={control}
                      render={({ field }) => (
                        <FormControl>
                          <FormLabel>Trigger Type</FormLabel>
                          <RadioGroup {...field} row>
                            <FormControlLabel
                              value="MANUAL"
                              control={<Radio />}
                              label="Manual"
                            />
                            <FormControlLabel
                              value="SCHEDULED"
                              control={<Radio />}
                              label="Scheduled"
                            />
                            <FormControlLabel
                              value="EVENT"
                              control={<Radio />}
                              label="Event-driven"
                            />
                          </RadioGroup>
                        </FormControl>
                      )}
                    />
                  </Grid>

                  {triggerType === "SCHEDULED" && (
                    <Grid item xs={12}>
                      <Controller
                        name="cronExpression"
                        control={control}
                        render={({ field }) => (
                          <TextField
                            {...field}
                            label="Cron Expression"
                            fullWidth
                            placeholder="0 0 12 * * ?"
                            helperText="Cron expression for scheduling (required for scheduled jobs)"
                            InputProps={{
                              endAdornment: (
                                <InputAdornment position="end">
                                  <Tooltip title="Cron format: second minute hour day month weekday">
                                    <IconButton>
                                      <HelpIcon />
                                    </IconButton>
                                  </Tooltip>
                                </InputAdornment>
                              ),
                            }}
                          />
                        )}
                      />
                    </Grid>
                  )}
                </Grid>
              </CardContent>
            </Card>
          </Grid>

          {/* Environment Variables */}
          <Grid item xs={12}>
            <Accordion>
              <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                <Typography variant="h6">
                  Environment Variables (Optional)
                </Typography>
              </AccordionSummary>
              <AccordionDetails>
                <Controller
                  name="environmentVariables"
                  control={control}
                  render={({ field }) => (
                    <TextField
                      {...field}
                      label="Environment Variables"
                      fullWidth
                      multiline
                      rows={6}
                      placeholder="KEY1=value1&#10;KEY2=value2&#10;KEY3=value3"
                      helperText="One variable per line in KEY=VALUE format"
                    />
                  )}
                />
              </AccordionDetails>
            </Accordion>
          </Grid>

          {/* Submit Error */}
          {submitError && (
            <Grid item xs={12}>
              <Alert severity="error">{submitError}</Alert>
            </Grid>
          )}

          {/* Actions */}
          <Grid item xs={12}>
            <Box display="flex" gap={2} justifyContent="flex-end">
              <Button
                variant="outlined"
                onClick={() => navigate(-1)}
                disabled={isSubmitting}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                variant="contained"
                startIcon={
                  isSubmitting ? <CircularProgress size={20} /> : <SaveIcon />
                }
                disabled={isSubmitting}
              >
                {isSubmitting
                  ? "Saving..."
                  : isEditing
                    ? "Update Job"
                    : "Create Job"}
              </Button>
            </Box>
          </Grid>
        </Grid>
      </form>
    </Box>
  );
};

export default JobFormPage;
