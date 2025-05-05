// @ts-nocheck
import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import {
  Box,
  Typography,
  Paper,
  Grid,
  Button,
  CircularProgress,
  Alert,
  TextField,
  Divider,
} from '@mui/material';
import { DatePicker } from '@mui/x-date-pickers/DatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';
import { ru } from 'date-fns/locale';
import { format, subMonths } from 'date-fns';
import { Pie } from 'react-chartjs-2';
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js';
import { projectApi, statisticsApi } from '../services/api';

// Регистрируем компоненты Chart.js
ChartJS.register(ArcElement, Tooltip, Legend);

interface Project {
  id: number;
  name: string;
  description: string;
  status: string;
}

interface UserStatistics {
  userId: string;
  username: string;
  createdCount: number;
  modifiedCount: number;
  passedCount: number;
  failedCount: number;
}

interface Statistics {
  projectId: number;
  projectName: string;
  startDate: string;
  endDate: string;
  userStatistics: UserStatistics[];
  totalCreatedCount: number;
  totalModifiedCount: number;
  totalPassedCount: number;
  totalFailedCount: number;
}

const ProjectDetailsPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const projectId = parseInt(id || '0');
  
  const [project, setProject] = useState<Project | null>(null);
  const [statistics, setStatistics] = useState<Statistics | null>(null);
  const [startDate, setStartDate] = useState<Date>(subMonths(new Date(), 1));
  const [endDate, setEndDate] = useState<Date>(new Date());
  
  const [loading, setLoading] = useState(true);
  const [collecting, setCollecting] = useState(false);
  const [error, setError] = useState('');
  
  useEffect(() => {
    if (projectId) {
      fetchProject();
      fetchStatistics();
    }
  }, [projectId]);
  
  const fetchProject = async () => {
    try {
      const response = await projectApi.getProjectById(projectId);
      setProject(response.data.data);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось загрузить проект');
    }
  };
  
  const fetchStatistics = async () => {
    try {
      setLoading(true);
      const formattedStartDate = format(startDate, 'yyyy-MM-dd');
      const formattedEndDate = format(endDate, 'yyyy-MM-dd');
      
      const response = await statisticsApi.getProjectStatistics(
        projectId,
        formattedStartDate,
        formattedEndDate
      );
      
      setStatistics(response.data.data);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось загрузить статистику');
    } finally {
      setLoading(false);
    }
  };
  
  const handleCollectStatistics = async () => {
    try {
      setCollecting(true);
      const formattedStartDate = format(startDate, 'yyyy-MM-dd');
      const formattedEndDate = format(endDate, 'yyyy-MM-dd');
      
      await statisticsApi.collectProjectStatistics(
        projectId,
        formattedStartDate,
        formattedEndDate
      );
      
      // Ждем немного, чтобы статистика успела собраться
      setTimeout(() => {
        fetchStatistics();
        setCollecting(false);
      }, 3000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось собрать статистику');
      setCollecting(false);
    }
  };
  
  const handleDateChange = () => {
    fetchStatistics();
  };
  
  if (loading && !statistics) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
        <CircularProgress />
      </Box>
    );
  }
  
  // Данные для графиков
  const createdData = {
    labels: statistics?.userStatistics.map(user => user.username) || [],
    datasets: [
      {
        label: 'Создано тест-кейсов',
        data: statistics?.userStatistics.map(user => user.createdCount) || [],
        backgroundColor: [
          'rgba(255, 99, 132, 0.6)',
          'rgba(54, 162, 235, 0.6)',
          'rgba(255, 206, 86, 0.6)',
          'rgba(75, 192, 192, 0.6)',
          'rgba(153, 102, 255, 0.6)',
        ],
        borderWidth: 1,
      },
    ],
  };
  
  const modifiedData = {
    labels: statistics?.userStatistics.map(user => user.username) || [],
    datasets: [
      {
        label: 'Изменено тест-кейсов',
        data: statistics?.userStatistics.map(user => user.modifiedCount) || [],
        backgroundColor: [
          'rgba(54, 162, 235, 0.6)',
          'rgba(255, 99, 132, 0.6)',
          'rgba(255, 206, 86, 0.6)',
          'rgba(75, 192, 192, 0.6)',
          'rgba(153, 102, 255, 0.6)',
        ],
        borderWidth: 1,
      },
    ],
  };
  
  const passedData = {
    labels: statistics?.userStatistics.map(user => user.username) || [],
    datasets: [
      {
        label: 'Успешно пройдено тестов',
        data: statistics?.userStatistics.map(user => user.passedCount) || [],
        backgroundColor: [
          'rgba(75, 192, 192, 0.6)',
          'rgba(54, 162, 235, 0.6)',
          'rgba(255, 206, 86, 0.6)',
          'rgba(255, 99, 132, 0.6)',
          'rgba(153, 102, 255, 0.6)',
        ],
        borderWidth: 1,
      },
    ],
  };
  
  const failedData = {
    labels: statistics?.userStatistics.map(user => user.username) || [],
    datasets: [
      {
        label: 'Не пройдено тестов',
        data: statistics?.userStatistics.map(user => user.failedCount) || [],
        backgroundColor: [
          'rgba(255, 206, 86, 0.6)',
          'rgba(54, 162, 235, 0.6)',
          'rgba(75, 192, 192, 0.6)',
          'rgba(255, 99, 132, 0.6)',
          'rgba(153, 102, 255, 0.6)',
        ],
        borderWidth: 1,
      },
    ],
  };
  
  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        {project?.name || 'Проект'}
      </Typography>
      
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      <Paper sx={{ p: 3, mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          Период статистики
        </Typography>
        
        <LocalizationProvider dateAdapter={AdapterDateFns} adapterLocale={ru}>
          <Grid container spacing={2} alignItems="center">
            <Grid item xs={12} sm={5} md={4}>
              <DatePicker
                label="Начальная дата"
                value={startDate}
                onChange={(newValue) => {
                  if (newValue) {
                    setStartDate(newValue);
                  }
                }}
                slotProps={{ textField: { fullWidth: true, margin: 'normal' } }}
              />
            </Grid>
            <Grid item xs={12} sm={5} md={4}>
              <DatePicker
                label="Конечная дата"
                value={endDate}
                onChange={(newValue) => {
                  if (newValue) {
                    setEndDate(newValue);
                  }
                }}
                slotProps={{ textField: { fullWidth: true, margin: 'normal' } }}
              />
            </Grid>
            <Grid item xs={12} sm={2} md={2}>
              <Button
                variant="outlined"
                onClick={handleDateChange}
                sx={{ mt: 2 }}
                fullWidth
              >
                Применить
              </Button>
            </Grid>
            <Grid item xs={12} sm={12} md={2}>
              <Button
                variant="contained"
                onClick={handleCollectStatistics}
                disabled={collecting}
                sx={{ mt: 2 }}
                fullWidth
              >
                {collecting ? 'Сбор...' : 'Обновить данные'}
              </Button>
            </Grid>
          </Grid>
        </LocalizationProvider>
      </Paper>
      
      {statistics && (
        <>
          <Typography variant="h5" gutterBottom>
            Статистика за период {format(startDate, 'dd.MM.yyyy')} - {format(endDate, 'dd.MM.yyyy')}
          </Typography>
          
          <Grid container spacing={3}>
            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 3, height: '100%' }}>
                <Typography variant="h6" gutterBottom>
                  Создано тест-кейсов: {statistics.totalCreatedCount}
                </Typography>
                <Box sx={{ height: 300 }}>
                  <Pie data={createdData} options={{ maintainAspectRatio: false }} />
                </Box>
              </Paper>
            </Grid>
            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 3, height: '100%' }}>
                <Typography variant="h6" gutterBottom>
                  Изменено тест-кейсов: {statistics.totalModifiedCount}
                </Typography>
                <Box sx={{ height: 300 }}>
                  <Pie data={modifiedData} options={{ maintainAspectRatio: false }} />
                </Box>
              </Paper>
            </Grid>
            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 3, height: '100%' }}>
                <Typography variant="h6" gutterBottom>
                  Успешно пройдено тестов: {statistics.totalPassedCount}
                </Typography>
                <Box sx={{ height: 300 }}>
                  <Pie data={passedData} options={{ maintainAspectRatio: false }} />
                </Box>
              </Paper>
            </Grid>
            <Grid item xs={12} md={6}>
              <Paper sx={{ p: 3, height: '100%' }}>
                <Typography variant="h6" gutterBottom>
                  Не пройдено тестов: {statistics.totalFailedCount}
                </Typography>
                <Box sx={{ height: 300 }}>
                  <Pie data={failedData} options={{ maintainAspectRatio: false }} />
                </Box>
              </Paper>
            </Grid>
          </Grid>
          
          <Paper sx={{ p: 3, mt: 4 }}>
            <Typography variant="h6" gutterBottom>
              Детальная статистика по пользователям
            </Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={3}>
                <Typography variant="subtitle1" fontWeight="bold">
                  Пользователь
                </Typography>
              </Grid>
              <Grid item xs={2}>
                <Typography variant="subtitle1" fontWeight="bold">
                  Создано
                </Typography>
              </Grid>
              <Grid item xs={2}>
                <Typography variant="subtitle1" fontWeight="bold">
                  Изменено
                </Typography>
              </Grid>
              <Grid item xs={2}>
                <Typography variant="subtitle1" fontWeight="bold">
                  Успешно
                </Typography>
              </Grid>
              <Grid item xs={2}>
                <Typography variant="subtitle1" fontWeight="bold">
                  Не успешно
                </Typography>
              </Grid>
            </Grid>
            
            <Divider sx={{ my: 2 }} />
            
            {statistics.userStatistics.map((user, index) => (
              <React.Fragment key={user.userId}>
                <Grid container spacing={2}>
                  <Grid item xs={3}>
                    <Typography variant="body1">
                      {user.username}
                    </Typography>
                  </Grid>
                  <Grid item xs={2}>
                    <Typography variant="body1">
                      {user.createdCount}
                    </Typography>
                  </Grid>
                  <Grid item xs={2}>
                    <Typography variant="body1">
                      {user.modifiedCount}
                    </Typography>
                  </Grid>
                  <Grid item xs={2}>
                    <Typography variant="body1">
                      {user.passedCount}
                    </Typography>
                  </Grid>
                  <Grid item xs={2}>
                    <Typography variant="body1">
                      {user.failedCount}
                    </Typography>
                  </Grid>
                </Grid>
                {index < statistics.userStatistics.length - 1 && (
                  <Divider sx={{ my: 2 }} />
                )}
              </React.Fragment>
            ))}
          </Paper>
        </>
      )}
    </Box>
  );
};

export default ProjectDetailsPage;
