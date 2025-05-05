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
  TextField,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  CircularProgress,
  Alert,
} from '@mui/material';
import { Add as AddIcon } from '@mui/icons-material';
import { projectApi } from '../services/api';
import { useAuth } from '../context/AuthContext';

interface Project {
  id: number;
  name: string;
  description: string;
  status: string;
  visible: boolean;
  testitId: string;
}

const ProjectsPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [projects, setProjects] = useState<Project[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [openDialog, setOpenDialog] = useState(false);
  const [testitId, setTestitId] = useState('');
  const [addingProject, setAddingProject] = useState(false);
  const [confirmDeleteDialog, setConfirmDeleteDialog] = useState(false);
  const [projectToDelete, setProjectToDelete] = useState<number | null>(null);

  useEffect(() => {
    fetchProjects();
  }, []);

  const fetchProjects = async () => {
    try {
      setLoading(true);
      const response = await projectApi.getAllUserProjects();
      setProjects(response.data.data);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось загрузить проекты');
    } finally {
      setLoading(false);
    }
  };

  const handleOpenDialog = () => {
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setTestitId('');
  };

  const handleAddProject = async () => {
    if (!testitId) {
      return;
    }

    try {
      setAddingProject(true);
      await projectApi.addProjectFromTestIt(testitId);
      await fetchProjects();
      handleCloseDialog();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось добавить проект');
    } finally {
      setAddingProject(false);
    }
  };

  const handleProjectClick = (projectId: number) => {
    navigate(`/projects/${projectId}`);
  };

  const handleToggleVisibility = async (projectId: number, visible: boolean) => {
    try {
      await projectApi.updateProjectVisibility(projectId, !visible);
      await fetchProjects();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось изменить видимость проекта');
    }
  };

  const handleOpenConfirmDeleteDialog = (projectId: number) => {
    setProjectToDelete(projectId);
    setConfirmDeleteDialog(true);
  };

  const handleCloseConfirmDeleteDialog = () => {
    setConfirmDeleteDialog(false);
    setProjectToDelete(null);
  };

  const handleRemoveProject = async (projectId: number) => {
    // Для админов показываем диалог подтверждения, так как они полностью удаляют проект из системы
    if (user && user.role === 'ADMIN') {
      handleOpenConfirmDeleteDialog(projectId);
    } else {
      // Для обычных пользователей удаляем проект без подтверждения, так как он только скрывается
      try {
        console.log('User is removing project from their list');
        await projectApi.removeProjectFromUser(projectId);
        await fetchProjects();
      } catch (err: any) {
        console.error('Error removing project:', err);
        setError(err.response?.data?.message || 'Не удалось удалить проект');
      }
    }
  };

  const handleConfirmDelete = async () => {
    if (projectToDelete === null) return;
    
    try {
      console.log('Admin is deleting project completely from the system');
      await projectApi.deleteProject(projectToDelete);
      await fetchProjects();
      handleCloseConfirmDeleteDialog();
    } catch (err: any) {
      console.error('Error deleting project:', err);
      setError(err.response?.data?.message || 'Не удалось удалить проект');
      handleCloseConfirmDeleteDialog();
    }
  };

  if (loading && projects.length === 0) {
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
          Проекты
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleOpenDialog}
        >
          Добавить проект
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

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
                  {project.description || 'Нет описания'}
                </Typography>
              </CardContent>
              <CardActions>
                <Button size="small" onClick={() => handleProjectClick(project.id)}>
                  Статистика
                </Button>
                <Button size="small" onClick={() => handleToggleVisibility(project.id, project.visible)}>
                  {project.visible ? 'Скрыть' : 'Показать'}
                </Button>
                <Button size="small" color="error" onClick={() => handleRemoveProject(project.id)}>
                  Удалить
                </Button>
              </CardActions>
            </Card>
          </Grid>
        ))}
      </Grid>

      <Dialog open={openDialog} onClose={handleCloseDialog}>
        <DialogTitle>Добавить проект из TestIT</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Введите ID проекта из TestIT для добавления его в систему.
          </DialogContentText>
          <TextField
            autoFocus
            margin="dense"
            id="testitId"
            label="ID проекта TestIT"
            type="text"
            fullWidth
            variant="outlined"
            value={testitId}
            onChange={(e) => setTestitId(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Отмена</Button>
          <Button onClick={handleAddProject} disabled={!testitId || addingProject}>
            {addingProject ? 'Добавление...' : 'Добавить'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Диалог подтверждения удаления проекта */}
      <Dialog
        open={confirmDeleteDialog}
        onClose={handleCloseConfirmDeleteDialog}
      >
        <DialogTitle>Подтверждение удаления</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Вы уверены, что хотите полностью удалить этот проект из системы? 
            Это действие нельзя отменить, и все данные проекта будут потеряны.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseConfirmDeleteDialog}>Отмена</Button>
          <Button onClick={handleConfirmDelete} color="error">
            Удалить
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default ProjectsPage;
