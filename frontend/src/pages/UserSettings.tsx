import React, { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Card,
  CardContent,
  TextField,
  Button,
  Grid,
  CircularProgress,
  Alert,
  Snackbar,
} from "@mui/material";
import { Settings as SettingsIcon } from "@mui/icons-material";
import { useAuthStore } from "@/store/authStore";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  userService,
  UpdateProfileRequest,
  ChangePasswordRequest,
} from "@/services/userService";

const UserSettings: React.FC = () => {
  const { user } = useAuthStore();
  const queryClient = useQueryClient();

  const [profileData, setProfileData] = useState({
    email: "",
    firstName: "",
    lastName: "",
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: "",
  });

  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  // Fetch current user profile
  const { data: userProfile, isLoading: profileLoading } = useQuery({
    queryKey: ["user-profile"],
    queryFn: () => userService.getUserProfile(),
    enabled: !!user,
  });

  // Update profile whenever user profile data changes
  useEffect(() => {
    if (userProfile) {
      setProfileData({
        email: userProfile.email || "",
        firstName: userProfile.firstName || "",
        lastName: userProfile.lastName || "",
      });
    }
  }, [userProfile]);

  // Profile update mutation
  const updateProfileMutation = useMutation({
    mutationFn: (data: UpdateProfileRequest) =>
      userService.updateUserProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["user-profile"] });
      queryClient.invalidateQueries({ queryKey: ["current-user"] });
      setSnackbar({
        open: true,
        message: "Profile updated successfully",
        severity: "success",
      });
    },
    onError: () => {
      setSnackbar({
        open: true,
        message: "Failed to update profile",
        severity: "error",
      });
    },
  });

  // Password change mutation
  const changePasswordMutation = useMutation({
    mutationFn: (data: ChangePasswordRequest) =>
      userService.changePassword(data),
    onSuccess: () => {
      setPasswordData({
        currentPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
      setSnackbar({
        open: true,
        message: "Password changed successfully",
        severity: "success",
      });
    },
    onError: () => {
      setSnackbar({
        open: true,
        message: "Failed to change password",
        severity: "error",
      });
    },
  });

  const handleProfileSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateProfileMutation.mutate(profileData);
  };

  const handlePasswordSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      setSnackbar({
        open: true,
        message: "New passwords do not match",
        severity: "error",
      });
      return;
    }
    changePasswordMutation.mutate({
      currentPassword: passwordData.currentPassword,
      newPassword: passwordData.newPassword,
    });
  };

  return (
    <Box>
      <Typography
        variant="h4"
        gutterBottom
        sx={{ display: "flex", alignItems: "center", gap: 1 }}
      >
        <SettingsIcon />
        User Settings
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Profile Information
              </Typography>
              {profileLoading ? (
                <Box sx={{ display: "flex", justifyContent: "center", py: 3 }}>
                  <CircularProgress />
                </Box>
              ) : (
                <Box
                  component="form"
                  onSubmit={handleProfileSubmit}
                  sx={{ "& .MuiTextField-root": { mb: 2 } }}
                >
                  <TextField
                    fullWidth
                    label="Username"
                    value={user?.username || ""}
                    disabled
                    helperText="Username cannot be changed"
                  />
                  <TextField
                    fullWidth
                    label="Email"
                    type="email"
                    value={profileData.email}
                    onChange={(e) =>
                      setProfileData({ ...profileData, email: e.target.value })
                    }
                    required
                  />
                  <TextField
                    fullWidth
                    label="First Name"
                    value={profileData.firstName}
                    onChange={(e) =>
                      setProfileData({
                        ...profileData,
                        firstName: e.target.value,
                      })
                    }
                    required
                  />
                  <TextField
                    fullWidth
                    label="Last Name"
                    value={profileData.lastName}
                    onChange={(e) =>
                      setProfileData({
                        ...profileData,
                        lastName: e.target.value,
                      })
                    }
                    required
                  />
                  <Button
                    type="submit"
                    variant="contained"
                    color="primary"
                    disabled={updateProfileMutation.isPending}
                    fullWidth
                  >
                    {updateProfileMutation.isPending ? (
                      <CircularProgress size={20} />
                    ) : (
                      "Save Changes"
                    )}
                  </Button>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" gutterBottom>
                Change Password
              </Typography>
              <Box
                component="form"
                onSubmit={handlePasswordSubmit}
                sx={{ "& .MuiTextField-root": { mb: 2 } }}
              >
                <TextField
                  fullWidth
                  type="password"
                  label="Current Password"
                  value={passwordData.currentPassword}
                  onChange={(e) =>
                    setPasswordData({
                      ...passwordData,
                      currentPassword: e.target.value,
                    })
                  }
                  required
                />
                <TextField
                  fullWidth
                  type="password"
                  label="New Password"
                  value={passwordData.newPassword}
                  onChange={(e) =>
                    setPasswordData({
                      ...passwordData,
                      newPassword: e.target.value,
                    })
                  }
                  required
                />
                <TextField
                  fullWidth
                  type="password"
                  label="Confirm New Password"
                  value={passwordData.confirmPassword}
                  onChange={(e) =>
                    setPasswordData({
                      ...passwordData,
                      confirmPassword: e.target.value,
                    })
                  }
                  required
                />
                <Button
                  type="submit"
                  variant="contained"
                  color="primary"
                  disabled={changePasswordMutation.isPending}
                  fullWidth
                >
                  {changePasswordMutation.isPending ? (
                    <CircularProgress size={20} />
                  ) : (
                    "Change Password"
                  )}
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={() => setSnackbar({ ...snackbar, open: false })}
      >
        <Alert severity={snackbar.severity}>{snackbar.message}</Alert>
      </Snackbar>
    </Box>
  );
};

export default UserSettings;
