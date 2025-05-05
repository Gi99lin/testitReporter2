// @ts-nocheck
import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Box,
  Typography,
  Grid,
  Card,
  CardContent,
  CardActions,
  Button,
  Divider,
  CircularProgress,
  Alert,
} from '@mui/material';
import {
  Assessment as AssessmentIcon,
  Add as AddIcon,
} from '@mui/icons-material';
import { useAuth } from '../context/AuthContext';
import { projectApi } from '../services/api';

interface Project {
  id: number;
  name: string;
  description: string;
  status: string;
  visible: boolean;
}

const DashboardPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchProjects = async () => {
      try {
        const response = await projectApi.getVisibleProjects();
        setProjects(response.data.data);
        setLoading(false);
      } catch (err: any) {
        setError(err.response?.data?.message || 'Не удалось загрузить проекты');
        setLoading(false);
      }
    };

    fetchProjects();
  }, []);

  const handleProjectClick = (projectId: number) => {
    navigate(`/projects/${projectId}`);
  };

  const handleAddProject = () => {
    navigate('/projects');
  };

  if (loading) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Typography variant="h4" component="h1">
          Панель управления
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleAddProject}
        >
          Добавить проект
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      <Box sx={{ mb: 4 }}>
        <Typography variant="h5" component="h2" gutterBottom>
          Добро пожаловать, {user?.username}!
        </Typography>
        <Typography variant="body1">
          Здесь представлен обзор ваших проектов TestIT и их статистика.
        </Typography>
      </Box>

      <Divider sx={{ mb: 4 }} />

      <Typography variant="h5" component="h2" gutterBottom>
        Ваши проекты
      </Typography>

      {projects.length === 0 ? (
        <Box sx={{ textAlign: 'center', py: 4 }}>
          <AssessmentIcon sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h6" color="text.secondary" gutterBottom>
            Проекты не найдены
          </Typography>
          <Typography variant="body1" color="text.secondary" paragraph>
            У вас пока нет проектов. Добавьте проект, чтобы начать отслеживать статистику.
          </Typography>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={handleAddProject}
          >
            Добавить проект
          </Button>
        </Box>
      ) : (
        <Grid container spacing={3}>
          {projects.map((project) => (
            <Grid item xs={12} sm={6} md={4} key={project.id}>
              <Card>
                <CardContent>
                  <Typography variant="h6" component="div" noWrap>
                    {project.name}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 1.5 }}>
                    Статус: {project.status}
                  </Typography>
                  <Typography variant="body2" color="text.secondary" sx={{ height: 60, overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {project.description || 'Описание отсутствует'}
                  </Typography>
                </CardContent>
                <CardActions>
                  <Button size="small" onClick={() => handleProjectClick(project.id)}>
                    Просмотр статистики
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}
    </Box>
  );
};

export default DashboardPage;
