import React from "react";
import { Box, Typography, Grid, Card, CardContent, Chip } from "@mui/material";

const Dashboard: React.FC = () => {
  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Job Monitoring Dashboard
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Total Jobs
              </Typography>
              <Typography variant="h5" component="div">
                12
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Running Jobs
              </Typography>
              <Typography variant="h5" component="div">
                3
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Failed Jobs
              </Typography>
              <Typography variant="h5" component="div" color="error">
                1
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} sm={6} md={3}>
          <Card>
            <CardContent>
              <Typography color="textSecondary" gutterBottom>
                Success Rate
              </Typography>
              <Typography variant="h5" component="div" color="success.main">
                92%
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Recent Job Executions
              </Typography>
              <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                {[
                  {
                    name: "ETL Daily Sales",
                    status: "SUCCESS",
                    time: "2 minutes ago",
                  },
                  {
                    name: "Data Validation",
                    status: "SUCCESS",
                    time: "5 minutes ago",
                  },
                  {
                    name: "Report Generator",
                    status: "RUNNING",
                    time: "10 minutes ago",
                  },
                  {
                    name: "Archive Cleanup",
                    status: "FAILED",
                    time: "1 hour ago",
                  },
                ].map((job, index) => (
                  <Box
                    key={index}
                    sx={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "center",
                    }}
                  >
                    <Typography>{job.name}</Typography>
                    <Box sx={{ display: "flex", gap: 1, alignItems: "center" }}>
                      <Chip
                        label={job.status}
                        color={
                          job.status === "SUCCESS"
                            ? "success"
                            : job.status === "RUNNING"
                              ? "info"
                              : "error"
                        }
                        size="small"
                      />
                      <Typography variant="body2" color="textSecondary">
                        {job.time}
                      </Typography>
                    </Box>
                  </Box>
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default Dashboard;
