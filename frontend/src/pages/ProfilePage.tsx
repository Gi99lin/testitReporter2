// @ts-nocheck
import React, { useState } from 'react';
import {
  Box,
  Typography,
  Paper,
  TextField,
  Button,
  Grid,
  Alert,
  Divider,
  CircularProgress,
} from '@mui/material';
import { useAuth } from '../context/AuthContext';
import { userApi } from '../services/api';

const ProfilePage: React.FC = () => {
  const { user, updateTestItToken } = useAuth();
  
  const [username, setUsername] = useState(user?.username || '');
  const [email, setEmail] = useState(user?.email || '');
  const [token, setToken] = useState(user?.testitToken || '');
  
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const handleUpdateProfile = async () => {
    if (!username || !email) {
      setError('Имя пользователя и email обязательны');
      return;
    }
    
    try {
      setLoading(true);
      setError('');
      setSuccess('');
      
      await userApi.updateCurrentUser({
        username,
        email,
      });
      
      setSuccess('Профиль успешно обновлен');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось обновить профиль');
    } finally {
      setLoading(false);
    }
  };
  
  const handleUpdateToken = async () => {
    try {
      setLoading(true);
      setError('');
      setSuccess('');
      
      // Обновляем только токен TestIT в базе данных, не трогая JWT токен авторизации
      await userApi.updateToken(token);
      
      // Используем специальный метод для обновления только TestIT токена
      updateTestItToken(token);
      
      setSuccess('Токен TestIT успешно обновлен');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось обновить токен');
    } finally {
      setLoading(false);
    }
  };
  
  if (!user) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
        <CircularProgress />
      </Box>
    );
  }
  
  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Профиль пользователя
      </Typography>
      
      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}
      
      {success && (
        <Alert severity="success" sx={{ mb: 3 }}>
          {success}
        </Alert>
      )}
      
      <Paper sx={{ p: 3, mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          Основная информация
        </Typography>
        
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Имя пользователя"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              margin="normal"
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              margin="normal"
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Роль"
              value={user.role}
              margin="normal"
              disabled
            />
          </Grid>
        </Grid>
        
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            variant="contained"
            onClick={handleUpdateProfile}
            disabled={loading}
          >
            {loading ? 'Сохранение...' : 'Сохранить изменения'}
          </Button>
        </Box>
      </Paper>
      
      <Paper sx={{ p: 3 }}>
        <Typography variant="h6" gutterBottom>
          Токен TestIT
        </Typography>
        
        <Typography variant="body2" color="text.secondary" paragraph>
          Токен используется для доступа к API TestIT при ручном сборе статистики.
        </Typography>
        
        <TextField
          fullWidth
          label="Токен TestIT"
          value={token}
          onChange={(e) => setToken(e.target.value)}
          margin="normal"
          type="password"
        />
        
        <Box sx={{ mt: 2, display: 'flex', justifyContent: 'flex-end' }}>
          <Button
            variant="contained"
            onClick={handleUpdateToken}
            disabled={loading}
          >
            {loading ? 'Сохранение...' : 'Обновить токен'}
          </Button>
        </Box>
      </Paper>
    </Box>
  );
};

export default ProfilePage;
