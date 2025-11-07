import React from "react";
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  Chip,
  CircularProgress,
  Alert,
  Avatar,
} from "@mui/material";
import {
  People as PeopleIcon,
  CheckCircle as CheckCircleIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
} from "@mui/icons-material";
import { useQuery } from "@tanstack/react-query";
import { useAuthStore } from "@/store/authStore";
import { adminService } from "@/services/adminService";

const Dashboard: React.FC = () => {
  const { user } = useAuthStore();
  const isAdmin = user?.role === "ADMIN";

  // Fetch admin stats if user is admin
  const {
    data: adminStats,
    isLoading: statsLoading,
    error: statsError,
  } = useQuery({
    queryKey: ["admin-stats"],
    queryFn: () => adminService.getAdminStats(),
    enabled: isAdmin,
    refetchInterval: 30000, // Refresh every 30 seconds
  });

  // Fetch user list if admin
  const {
    data: users,
    isLoading: usersLoading,
    error: usersError,
  } = useQuery({
    queryKey: ["admin-users"],
    queryFn: () => adminService.getAllUsers(),
    enabled: isAdmin,
    refetchInterval: 60000, // Refresh every minute
  });

  const enabledUsers = users?.filter((u) => u.enabled) || [];
  const disabledUsers = users?.filter((u) => !u.enabled) || [];
  const adminUsers = users?.filter((u) => u.role === "ADMIN") || [];

  return (
    <Box>
      <Typography variant="h4" gutterBottom>
        Welcome, {user?.firstName} {user?.lastName}!
      </Typography>

      <Typography variant="h5" gutterBottom sx={{ mt: 2, mb: 3 }}>
        {isAdmin ? "Administrator Dashboard" : "User Dashboard"}
      </Typography>

      {/* Admin Statistics */}
      {isAdmin && (
        <>
          {statsLoading && (
            <Box sx={{ display: "flex", justifyContent: "center", my: 3 }}>
              <CircularProgress />
            </Box>
          )}

          {statsError && (
            <Alert severity="error" sx={{ mb: 3 }}>
              Failed to load dashboard statistics
            </Alert>
          )}

          <Grid container spacing={3}>
            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    <Avatar sx={{ bgcolor: "primary.main" }}>
                      <PeopleIcon />
                    </Avatar>
                    <Box>
                      <Typography color="textSecondary" gutterBottom>
                        Total Users
                      </Typography>
                      <Typography variant="h5" component="div">
                        {adminStats?.totalUsers || users?.length || 0}
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    <Avatar sx={{ bgcolor: "success.main" }}>
                      <CheckCircleIcon />
                    </Avatar>
                    <Box>
                      <Typography color="textSecondary" gutterBottom>
                        Active Users
                      </Typography>
                      <Typography variant="h5" component="div">
                        {enabledUsers.length}
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    <Avatar sx={{ bgcolor: "warning.main" }}>
                      <ErrorIcon />
                    </Avatar>
                    <Box>
                      <Typography color="textSecondary" gutterBottom>
                        Disabled Users
                      </Typography>
                      <Typography
                        variant="h5"
                        component="div"
                        color="warning.main"
                      >
                        {disabledUsers.length}
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12} sm={6} md={3}>
              <Card>
                <CardContent>
                  <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                    <Avatar sx={{ bgcolor: "info.main" }}>
                      <InfoIcon />
                    </Avatar>
                    <Box>
                      <Typography color="textSecondary" gutterBottom>
                        Administrators
                      </Typography>
                      <Typography
                        variant="h5"
                        component="div"
                        color="info.main"
                      >
                        {adminUsers.length}
                      </Typography>
                    </Box>
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            <Grid item xs={12}>
              <Card>
                <CardContent>
                  <Typography variant="h6" gutterBottom>
                    System Status
                  </Typography>
                  <Box sx={{ display: "flex", alignItems: "center", gap: 2 }}>
                    <Chip
                      label={adminStats?.systemStatus || "Operational"}
                      color="success"
                      icon={<CheckCircleIcon />}
                    />
                    <Typography variant="body2" color="text.secondary">
                      All systems running normally
                    </Typography>
                  </Box>
                </CardContent>
              </Card>
            </Grid>

            {/* Recent Users */}
            {users && users.length > 0 && (
              <Grid item xs={12}>
                <Card>
                  <CardContent>
                    <Typography variant="h6" gutterBottom>
                      Recent Users
                    </Typography>
                    <Box
                      sx={{ display: "flex", flexDirection: "column", gap: 2 }}
                    >
                      {users.slice(0, 5).map((user) => (
                        <Box
                          key={user.id}
                          sx={{
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                            p: 2,
                            borderRadius: 1,
                            bgcolor: "background.paper",
                            border: "1px solid",
                            borderColor: "divider",
                          }}
                        >
                          <Box>
                            <Typography variant="subtitle1">
                              {user.firstName} {user.lastName}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              @{user.username} • {user.email}
                            </Typography>
                          </Box>
                          <Box sx={{ display: "flex", gap: 1 }}>
                            <Chip
                              label={user.role}
                              color={
                                user.role === "ADMIN" ? "primary" : "default"
                              }
                              size="small"
                            />
                            <Chip
                              label={user.enabled ? "Active" : "Disabled"}
                              color={user.enabled ? "success" : "warning"}
                              size="small"
                            />
                          </Box>
                        </Box>
                      ))}
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            )}
          </Grid>
        </>
      )}

      {/* User Dashboard */}
      {!isAdmin && (
        <Grid container spacing={3}>
          <Grid item xs={12} sm={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  Your Profile
                </Typography>
                <Box sx={{ display: "flex", flexDirection: "column", gap: 1 }}>
                  <Typography variant="body1">
                    <strong>Username:</strong> {user?.username}
                  </Typography>
                  <Typography variant="body1">
                    <strong>Email:</strong> {user?.email}
                  </Typography>
                  <Typography variant="body1">
                    <strong>Role:</strong> {user?.role}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>

          <Grid item xs={12} sm={6}>
            <Card>
              <CardContent>
                <Typography variant="h6" gutterBottom>
                  System Information
                </Typography>
                <Box sx={{ display: "flex", flexDirection: "column", gap: 1 }}>
                  <Chip
                    label="System Operational"
                    color="success"
                    icon={<CheckCircleIcon />}
                  />
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mt: 1 }}
                  >
                    Job monitoring system is running normally
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        </Grid>
      )}
    </Box>
  );
};

export default Dashboard;
