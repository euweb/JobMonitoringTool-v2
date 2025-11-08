import React from "react";
import {
  AppBar,
  Box,
  Drawer,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
  Avatar,
  Menu,
  MenuItem,
  Divider,
} from "@mui/material";
import {
  Menu as MenuIcon,
  Dashboard as DashboardIcon,
  AdminPanelSettings as AdminIcon,
  Settings as SettingsIcon,
  Logout as LogoutIcon,
  Work as JobIcon,
  CloudDownload as ImportIcon,
} from "@mui/icons-material";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuthStore } from "@/store/authStore";

/** Constant defining the drawer width in pixels */
const DRAWER_WIDTH = 240;

/**
 * Props interface for the Layout component.
 */
interface LayoutProps {
  /** Child components to render within the layout content area */
  children: React.ReactNode;
}

/**
 * Main application layout component providing navigation and user interface structure.
 *
 * This component creates a responsive Material-UI layout with:
 * - Fixed app bar with user profile menu
 * - Collapsible side navigation drawer
 * - Role-based menu items (Admin panel only for ADMIN users)
 * - User profile menu with logout functionality
 * - Responsive design that adapts to mobile and desktop screens
 *
 * The layout automatically highlights the current page in the navigation
 * and provides consistent spacing and styling across all pages.
 *
 * @param props - Component props
 * @returns JSX element representing the complete application layout
 *
 * @example
 * ```tsx
 * // Wrap page content with Layout
 * <Layout>
 *   <YourPageContent />
 * </Layout>
 * ```
 */
const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [drawerOpen, setDrawerOpen] = React.useState(false);
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout } = useAuthStore();

  /**
   * Toggles the navigation drawer open/closed state.
   */
  const handleDrawerToggle = () => {
    setDrawerOpen(!drawerOpen);
  };

  /**
   * Opens the user profile menu.
   *
   * @param event - Mouse event from clicking the profile avatar
   */
  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  /**
   * Closes the user profile menu.
   */
  const handleProfileMenuClose = () => {
    setAnchorEl(null);
  };

  /**
   * Handles user logout by clearing auth state and closing profile menu.
   */
  const handleLogout = () => {
    logout();
    handleProfileMenuClose();
  };

  /**
   * Navigation menu items with role-based filtering.
   * Admin panel item is only shown to users with ADMIN role.
   */
  const menuItems = [
    { text: "Dashboard", icon: <DashboardIcon />, path: "/dashboard" },
    { text: "Jobs", icon: <JobIcon />, path: "/jobs" },
    { text: "Imported Jobs", icon: <ImportIcon />, path: "/imported-jobs" },
    ...(user?.role === "ADMIN"
      ? [{ text: "Admin Panel", icon: <AdminIcon />, path: "/admin" }]
      : []),
    { text: "Settings", icon: <SettingsIcon />, path: "/settings" },
  ];

  /**
   * Drawer content containing navigation items.
   * Highlights the currently active page based on route path.
   */
  const drawer = (
    <Box>
      <Toolbar>
        <Typography variant="h6" noWrap component="div">
          Job Monitor
        </Typography>
      </Toolbar>
      <Divider />
      <List>
        {menuItems.map((item) => (
          <ListItem
            key={item.path}
            button
            selected={
              location.pathname === item.path ||
              location.pathname.startsWith(item.path)
            }
            onClick={() => navigate(item.path)}
          >
            <ListItemIcon>{item.icon}</ListItemIcon>
            <ListItemText primary={item.text} />
          </ListItem>
        ))}
      </List>
    </Box>
  );

  return (
    <Box sx={{ display: "flex" }}>
      {/* App Bar */}
      <AppBar
        position="fixed"
        sx={{
          width: { sm: `calc(100% - ${DRAWER_WIDTH}px)` },
          ml: { sm: `${DRAWER_WIDTH}px` },
        }}
      >
        <Toolbar>
          <IconButton
            color="inherit"
            aria-label="open drawer"
            edge="start"
            onClick={handleDrawerToggle}
            sx={{ mr: 2, display: { sm: "none" } }}
          >
            <MenuIcon />
          </IconButton>
          <Typography variant="h6" noWrap component="div" sx={{ flexGrow: 1 }}>
            Job Monitoring Tool
          </Typography>

          {/* User Profile */}
          <IconButton onClick={handleProfileMenuOpen} size="small">
            <Avatar sx={{ width: 32, height: 32 }}>
              {user?.firstName?.[0]}
              {user?.lastName?.[0]}
            </Avatar>
          </IconButton>

          <Menu
            anchorEl={anchorEl}
            open={Boolean(anchorEl)}
            onClose={handleProfileMenuClose}
          >
            <MenuItem disabled>
              <Typography variant="body2">
                {user?.fullName} ({user?.role})
              </Typography>
            </MenuItem>
            <Divider />
            <MenuItem
              onClick={() => {
                navigate("/settings");
                handleProfileMenuClose();
              }}
            >
              <ListItemIcon>
                <SettingsIcon fontSize="small" />
              </ListItemIcon>
              Settings
            </MenuItem>
            <MenuItem onClick={handleLogout}>
              <ListItemIcon>
                <LogoutIcon fontSize="small" />
              </ListItemIcon>
              Logout
            </MenuItem>
          </Menu>
        </Toolbar>
      </AppBar>

      {/* Navigation Drawer */}
      <Box
        component="nav"
        sx={{ width: { sm: DRAWER_WIDTH }, flexShrink: { sm: 0 } }}
      >
        <Drawer
          variant="temporary"
          open={drawerOpen}
          onClose={handleDrawerToggle}
          ModalProps={{ keepMounted: true }}
          sx={{
            display: { xs: "block", sm: "none" },
            "& .MuiDrawer-paper": {
              boxSizing: "border-box",
              width: DRAWER_WIDTH,
            },
          }}
        >
          {drawer}
        </Drawer>
        <Drawer
          variant="permanent"
          sx={{
            display: { xs: "none", sm: "block" },
            "& .MuiDrawer-paper": {
              boxSizing: "border-box",
              width: DRAWER_WIDTH,
            },
          }}
          open
        >
          {drawer}
        </Drawer>
      </Box>

      {/* Main Content */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          p: 3,
          width: { sm: `calc(100% - ${DRAWER_WIDTH}px)` },
        }}
      >
        <Toolbar />
        {children}
      </Box>
    </Box>
  );
};

export default Layout;
