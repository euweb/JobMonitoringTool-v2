import React, { useState, useEffect } from "react";
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Chip,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormControlLabel,
  Switch,
  CircularProgress,
  Alert,
  Snackbar,
} from "@mui/material";
import {
  AdminPanelSettings as AdminIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Add as AddIcon,
} from "@mui/icons-material";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  adminService,
  User,
  CreateUserRequest,
  UpdateUserRequest,
} from "@/services/adminService";

interface UserDialogProps {
  open: boolean;
  onClose: () => void;
  user?: User | null;
  mode: "create" | "edit";
}

const UserDialog: React.FC<UserDialogProps> = ({
  open,
  onClose,
  user,
  mode,
}) => {
  const [formData, setFormData] = useState({
    username: user?.username || "",
    email: user?.email || "",
    firstName: user?.firstName || "",
    lastName: user?.lastName || "",
    password: "",
    role: (user?.role || "USER") as "ADMIN" | "USER",
    enabled: user?.enabled ?? true,
  });

  // Update formData when user changes
  useEffect(() => {
    if (user) {
      setFormData({
        username: user.username || "",
        email: user.email || "",
        firstName: user.firstName || "",
        lastName: user.lastName || "",
        password: "",
        role: user.role as "ADMIN" | "USER",
        enabled: user.enabled ?? true,
      });
    } else {
      // Reset form for create mode
      setFormData({
        username: "",
        email: "",
        firstName: "",
        lastName: "",
        password: "",
        role: "USER",
        enabled: true,
      });
    }
  }, [user, mode]);

  const queryClient = useQueryClient();

  const createMutation = useMutation({
    mutationFn: (data: CreateUserRequest) => adminService.createUser(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-users"] });
      onClose();
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateUserRequest }) =>
      adminService.updateUser(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-users"] });
      onClose();
    },
  });

  const handleSubmit = () => {
    if (mode === "create") {
      createMutation.mutate(formData);
    } else if (user) {
      const { password, ...updateData } = formData;
      const finalData = password ? formData : updateData;
      updateMutation.mutate({ id: user.id, data: finalData });
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>
        {mode === "create" ? "Create New User" : "Edit User"}
      </DialogTitle>
      <DialogContent>
        <Box sx={{ pt: 1, display: "flex", flexDirection: "column", gap: 2 }}>
          <TextField
            label="Username"
            value={formData.username}
            onChange={(e) =>
              setFormData({ ...formData, username: e.target.value })
            }
            fullWidth
            required
          />
          <TextField
            label="Email"
            type="email"
            value={formData.email}
            onChange={(e) =>
              setFormData({ ...formData, email: e.target.value })
            }
            fullWidth
            required
          />
          <TextField
            label="First Name"
            value={formData.firstName}
            onChange={(e) =>
              setFormData({ ...formData, firstName: e.target.value })
            }
            fullWidth
            required
          />
          <TextField
            label="Last Name"
            value={formData.lastName}
            onChange={(e) =>
              setFormData({ ...formData, lastName: e.target.value })
            }
            fullWidth
            required
          />
          <TextField
            label={
              mode === "create"
                ? "Password"
                : "New Password (leave empty to keep current)"
            }
            type="password"
            value={formData.password}
            onChange={(e) =>
              setFormData({ ...formData, password: e.target.value })
            }
            fullWidth
            required={mode === "create"}
          />
          <FormControl fullWidth>
            <InputLabel>Role</InputLabel>
            <Select
              value={formData.role}
              label="Role"
              onChange={(e) =>
                setFormData({
                  ...formData,
                  role: e.target.value as "ADMIN" | "USER",
                })
              }
            >
              <MenuItem value="USER">User</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
            </Select>
          </FormControl>
          <FormControlLabel
            control={
              <Switch
                checked={formData.enabled}
                onChange={(e) =>
                  setFormData({ ...formData, enabled: e.target.checked })
                }
              />
            }
            label="Enabled"
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button
          onClick={handleSubmit}
          variant="contained"
          disabled={createMutation.isPending || updateMutation.isPending}
        >
          {createMutation.isPending || updateMutation.isPending ? (
            <CircularProgress size={20} />
          ) : mode === "create" ? (
            "Create"
          ) : (
            "Update"
          )}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

const AdminPanel: React.FC = () => {
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [dialogMode, setDialogMode] = useState<"create" | "edit">("create");
  const [snackbar, setSnackbar] = useState({
    open: false,
    message: "",
    severity: "success" as "success" | "error",
  });

  const queryClient = useQueryClient();

  // Fetch users
  const {
    data: users,
    isLoading,
    error,
  } = useQuery({
    queryKey: ["admin-users"],
    queryFn: () => adminService.getAllUsers(),
    refetchInterval: 30000,
  });

  // Delete user mutation
  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminService.deleteUser(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-users"] });
      setSnackbar({
        open: true,
        message: "User deleted successfully",
        severity: "success",
      });
    },
    onError: () => {
      setSnackbar({
        open: true,
        message: "Failed to delete user",
        severity: "error",
      });
    },
  });

  const handleCreateUser = () => {
    setSelectedUser(null);
    setDialogMode("create");
    setDialogOpen(true);
  };

  const handleEditUser = (user: User) => {
    setSelectedUser(user);
    setDialogMode("edit");
    setDialogOpen(true);
  };

  const handleDeleteUser = (user: User) => {
    if (
      window.confirm(`Are you sure you want to delete user ${user.username}?`)
    ) {
      deleteMutation.mutate(user.id);
    }
  };

  return (
    <Box>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 3,
        }}
      >
        <Typography
          variant="h4"
          sx={{ display: "flex", alignItems: "center", gap: 1 }}
        >
          <AdminIcon />
          Admin Panel
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleCreateUser}
        >
          Add User
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          Failed to load users
        </Alert>
      )}

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            User Management
          </Typography>

          {isLoading ? (
            <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
              <CircularProgress />
            </Box>
          ) : users && users.length > 0 ? (
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Username</TableCell>
                    <TableCell>Name</TableCell>
                    <TableCell>Email</TableCell>
                    <TableCell>Role</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {users.map((user) => (
                    <TableRow key={user.id}>
                      <TableCell>
                        <Typography variant="body2" fontWeight="medium">
                          {user.username}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        {user.firstName} {user.lastName}
                      </TableCell>
                      <TableCell>{user.email}</TableCell>
                      <TableCell>
                        <Chip
                          label={user.role}
                          color={user.role === "ADMIN" ? "primary" : "default"}
                          size="small"
                        />
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={user.enabled ? "Active" : "Disabled"}
                          color={user.enabled ? "success" : "warning"}
                          size="small"
                        />
                      </TableCell>
                      <TableCell align="right">
                        <IconButton
                          onClick={() => handleEditUser(user)}
                          color="primary"
                          size="small"
                        >
                          <EditIcon />
                        </IconButton>
                        <IconButton
                          onClick={() => handleDeleteUser(user)}
                          color="error"
                          size="small"
                          disabled={deleteMutation.isPending}
                        >
                          <DeleteIcon />
                        </IconButton>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          ) : (
            <Typography variant="body2" color="text.secondary">
              No users found
            </Typography>
          )}
        </CardContent>
      </Card>

      <UserDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        user={selectedUser}
        mode={dialogMode}
      />

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

export default AdminPanel;
