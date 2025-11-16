import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import { Box } from "@mui/material";
import { useAuthStore } from "@/store/authStore";
import Layout from "./components/Layout";

// Pages
const LoginPage = React.lazy(() => import("./pages/LoginPage"));
const Dashboard = React.lazy(() => import("./pages/Dashboard"));
const AdminPanel = React.lazy(() => import("./pages/AdminPanel"));
const UserSettings = React.lazy(() => import("./pages/UserSettings"));
const JobsPage = React.lazy(() => import("./pages/JobsPage"));
const ImportedJobsPage = React.lazy(() => import("./pages/ImportedJobsPage"));

const App: React.FC = () => {
  const { isAuthenticated } = useAuthStore();

  if (!isAuthenticated) {
    return (
      <React.Suspense fallback={<div>Loading...</div>}>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </React.Suspense>
    );
  }

  return (
    <Layout>
      <React.Suspense fallback={<Box p={3}>Loading...</Box>}>
        <Routes>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/jobs" element={<JobsPage />} />
          <Route path="/imported-jobs" element={<ImportedJobsPage />} />
          <Route path="/admin" element={<AdminPanel />} />
          <Route path="/settings" element={<UserSettings />} />
          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </React.Suspense>
    </Layout>
  );
};

export default App;
