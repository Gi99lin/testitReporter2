// @ts-nocheck
import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Box } from '@mui/material';
import { useAuth } from './context/AuthContext';

// Layout components
import Layout from './components/Layout';

// Page components
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import ProjectsPage from './pages/ProjectsPage';
import ProjectDetailsPage from './pages/ProjectDetailsPage';
import ProfilePage from './pages/ProfilePage';
import AdminPage from './pages/AdminPage';
import NotFoundPage from './pages/NotFoundPage';

// Protected route component
const ProtectedRoute: React.FC<{ 
  element: React.ReactNode; 
  requireAdmin?: boolean;
}> = ({ element, requireAdmin = false }) => {
  const { isAuthenticated, isAdmin } = useAuth();

  if (!isAuthenticated) {
    return <Navigate to="/login" />;
  }

  if (requireAdmin && !isAdmin) {
    return <Navigate to="/dashboard" />;
  }

  return <>{element}</>;
};

const App: React.FC = () => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<LoginPage />} />
        
        {/* Protected routes */}
        <Route path="/" element={<Layout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={
            <ProtectedRoute element={<DashboardPage />} />
          } />
          <Route path="projects" element={
            <ProtectedRoute element={<ProjectsPage />} />
          } />
          <Route path="projects/:id" element={
            <ProtectedRoute element={<ProjectDetailsPage />} />
          } />
          <Route path="profile" element={
            <ProtectedRoute element={<ProfilePage />} />
          } />
          <Route path="admin" element={
            <ProtectedRoute element={<AdminPage />} requireAdmin={true} />
          } />
        </Route>
        
        {/* Not found route */}
        <Route path="*" element={<NotFoundPage />} />
      </Routes>
    </Box>
  );
};

export default App;
