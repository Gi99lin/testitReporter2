// @ts-nocheck
import React, { useEffect, useState } from 'react';
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
  Tabs,
  Tab,
  List,
  ListItem,
  ListItemText,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from '@mui/material';
import {
  Delete as DeleteIcon,
  Edit as EditIcon,
  Add as AddIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material';
import { adminApi, userApi, statisticsApi } from '../services/api';
import { useAuth } from '../context/AuthContext';
import { format } from 'date-fns';

interface TabPanelProps {
  children?: React.ReactNode;
  index: number;
  value: number;
}

function TabPanel(props: TabPanelProps) {
  const { children, value, index, ...other } = props;

  return (
    <div
      role="tabpanel"
      hidden={value !== index}
      id={`admin-tabpanel-${index}`}
      aria-labelledby={`admin-tab-${index}`}
      {...other}
    >
      {value === index && (
        <Box sx={{ p: 3 }}>
          {children}
        </Box>
      )}
    </div>
  );
}

interface GlobalSetting {
  key: string;
  value: string;
  description: string;
}

interface User {
  id: number;
  username: string;
  email: string;
  role: string;
}

const AdminPage: React.FC = () => {
  const { user } = useAuth();
  const [tabValue, setTabValue] = useState(0);
  
  // Состояние для настроек
  const [settings, setSettings] = useState<GlobalSetting[]>([]);
  const [loadingSettings, setLoadingSettings] = useState(true);
  const [editingSetting, setEditingSetting] = useState<GlobalSetting | null>(null);
  const [newSetting, setNewSetting] = useState<GlobalSetting>({ key: '', value: '', description: '' });
  const [openSettingDialog, setOpenSettingDialog] = useState(false);
  
  // Состояние для пользователей
  const [users, setUsers] = useState<User[]>([]);
  const [loadingUsers, setLoadingUsers] = useState(true);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [newUser, setNewUser] = useState<any>({ username: '', email: '', password: '', role: 'USER' });
  const [openUserDialog, setOpenUserDialog] = useState(false);
  
  // Состояние для сбора статистики
  const [collectingStats, setCollectingStats] = useState(false);
  const [statsStartDate, setStatsStartDate] = useState<string>(format(new Date(new Date().setDate(1)), 'yyyy-MM-dd'));
  const [statsEndDate, setStatsEndDate] = useState<string>(format(new Date(), 'yyyy-MM-dd'));
  
  // Состояние для обновления имен пользователей
  const [updatingUsernames, setUpdatingUsernames] = useState(false);
  
  // Общее состояние
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  useEffect(() => {
    if (tabValue === 0) {
      fetchSettings();
    } else if (tabValue === 1) {
      fetchUsers();
    }
  }, [tabValue]);
  
  const handleTabChange = (event: React.SyntheticEvent, newValue: number) => {
    setTabValue(newValue);
  };
  
  // Функции для настроек
  const fetchSettings = async () => {
    try {
      setLoadingSettings(true);
      const response = await adminApi.getAllSettings();
      setSettings(response.data.data);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось загрузить настройки');
    } finally {
      setLoadingSettings(false);
    }
  };
  
  const handleOpenSettingDialog = (setting?: GlobalSetting) => {
    if (setting) {
      setEditingSetting(setting);
    } else {
      setNewSetting({ key: '', value: '', description: '' });
    }
    setOpenSettingDialog(true);
  };
  
  const handleCloseSettingDialog = () => {
    setOpenSettingDialog(false);
    setEditingSetting(null);
  };
  
  const handleSaveSetting = async () => {
    try {
      if (editingSetting) {
        await adminApi.updateSetting(
          editingSetting.key, 
          editingSetting.value, 
          editingSetting.description
        );
        setSuccess('Настройка успешно обновлена');
      } else {
        await adminApi.createOrUpdateSetting(
          newSetting.key, 
          newSetting.value, 
          newSetting.description
        );
        setSuccess('Настройка успешно создана');
      }
      
      handleCloseSettingDialog();
      fetchSettings();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось сохранить настройку');
    }
  };
  
  const handleUpdateGlobalToken = async (token: string) => {
    try {
      await adminApi.updateGlobalToken(token);
      setSuccess('Глобальный токен успешно обновлен');
      fetchSettings();
      
      // Не обновляем JWT токен авторизации при обновлении глобального токена TestIT
      // Это предотвращает деавторизацию пользователя
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось обновить глобальный токен');
    }
  };
  
  const handleUpdateApiSchedule = async (cron: string) => {
    try {
      await adminApi.updateApiSchedule(cron);
      setSuccess('Расписание API успешно обновлено');
      fetchSettings();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось обновить расписание API');
    }
  };
  
  const handleUpdateApiBaseUrl = async (url: string) => {
    try {
      await adminApi.updateApiBaseUrl(url);
      setSuccess('Базовый URL API успешно обновлен');
      fetchSettings();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось обновить базовый URL API');
    }
  };
  
  const handleUpdateTestItCookies = async (cookies: string) => {
    try {
      await adminApi.updateTestItCookies(cookies);
      setSuccess('Cookies для TestIT API успешно обновлены');
      fetchSettings();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось обновить cookies для TestIT API');
    }
  };
  
  const handleToggleUseTestItCookies = async (use: boolean) => {
    try {
      await adminApi.toggleUseTestItCookies(use);
      setSuccess(`Использование cookies для TestIT API ${use ? 'включено' : 'выключено'}`);
      fetchSettings();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось изменить настройку использования cookies');
    }
  };
  
  // Функции для пользователей
  const fetchUsers = async () => {
    try {
      setLoadingUsers(true);
      const response = await userApi.getAllUsers();
      setUsers(response.data.data);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось загрузить пользователей');
    } finally {
      setLoadingUsers(false);
    }
  };
  
  const handleOpenUserDialog = (user?: User) => {
    if (user) {
      setEditingUser(user);
    } else {
      setNewUser({ username: '', email: '', password: '', role: 'USER' });
    }
    setOpenUserDialog(true);
  };
  
  const handleCloseUserDialog = () => {
    setOpenUserDialog(false);
    setEditingUser(null);
  };
  
  const handleSaveUser = async () => {
    try {
      if (editingUser) {
        await userApi.updateUser(editingUser.id, {
          username: editingUser.username,
          email: editingUser.email,
          role: editingUser.role,
        });
        setSuccess('Пользователь успешно обновлен');
      } else {
        await userApi.createUser(newUser);
        setSuccess('Пользователь успешно создан');
      }
      
      handleCloseUserDialog();
      fetchUsers();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось сохранить пользователя');
    }
  };
  
  const handleDeleteUser = async (userId: number) => {
    try {
      await userApi.deleteUser(userId);
      setSuccess('Пользователь успешно удален');
      fetchUsers();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось удалить пользователя');
    }
  };
  
  // Функции для сбора статистики
  const handleCollectAllStatistics = async () => {
    try {
      setCollectingStats(true);
      await statisticsApi.collectAllProjectsStatistics(statsStartDate, statsEndDate);
      setSuccess('Сбор статистики запущен для всех проектов');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось запустить сбор статистики');
    } finally {
      setCollectingStats(false);
    }
  };
  
  // Функция для обновления имен пользователей
  const handleUpdateAllUsernames = async () => {
    try {
      setUpdatingUsernames(true);
      await adminApi.updateAllUsernames();
      setSuccess('Обновление имен пользователей успешно запущено');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Не удалось запустить обновление имен пользователей');
    } finally {
      setUpdatingUsernames(false);
    }
  };
  
  if (!user || user.role !== 'ADMIN') {
    return (
      <Box>
        <Typography variant="h4" component="h1" gutterBottom>
          Доступ запрещен
        </Typography>
        <Alert severity="error">
          У вас нет прав для доступа к этой странице.
        </Alert>
      </Box>
    );
  }
  
  return (
    <Box>
      <Typography variant="h4" component="h1" gutterBottom>
        Панель администратора
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
      
      <Paper sx={{ width: '100%', mb: 4 }}>
        <Tabs value={tabValue} onChange={handleTabChange} aria-label="admin tabs">
          <Tab label="Настройки" id="admin-tab-0" aria-controls="admin-tabpanel-0" />
          <Tab label="Пользователи" id="admin-tab-1" aria-controls="admin-tabpanel-1" />
          <Tab label="Сбор статистики" id="admin-tab-2" aria-controls="admin-tabpanel-2" />
        </Tabs>
        
        {/* Вкладка настроек */}
        <TabPanel value={tabValue} index={0}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Глобальные настройки</Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => handleOpenSettingDialog()}
            >
              Добавить настройку
            </Button>
          </Box>
          
          {loadingSettings ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : (
            <List>
              {settings.map((setting) => (
                <React.Fragment key={setting.key}>
                  <ListItem
                    secondaryAction={
                      <IconButton edge="end" aria-label="edit" onClick={() => handleOpenSettingDialog(setting)}>
                        <EditIcon />
                      </IconButton>
                    }
                  >
                    <ListItemText
                      primary={setting.key}
                      secondary={
                        <>
                          <Typography component="span" variant="body2" color="text.primary">
                            {setting.value}
                          </Typography>
                          <br />
                          {setting.description}
                        </>
                      }
                    />
                  </ListItem>
                  <Divider />
                </React.Fragment>
              ))}
            </List>
          )}
          
          <Box sx={{ mt: 4 }}>
            <Typography variant="h6" gutterBottom>
              Быстрые настройки
            </Typography>
            
            <Grid container spacing={2}>
              <Grid item xs={12} md={4}>
                <Paper sx={{ p: 2 }}>
                  <Typography variant="subtitle1" gutterBottom>
                    Глобальный токен TestIT
                  </Typography>
                  <TextField
                    fullWidth
                    label="Токен"
                    type="password"
                    margin="normal"
                    value={settings.find(s => s.key === 'GLOBAL_TESTIT_TOKEN')?.value || ''}
                    onChange={(e) => {
                      const newSettings = [...settings];
                      const index = newSettings.findIndex(s => s.key === 'GLOBAL_TESTIT_TOKEN');
                      if (index !== -1) {
                        newSettings[index] = { ...newSettings[index], value: e.target.value };
                        setSettings(newSettings);
                      }
                    }}
                  />
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={() => handleUpdateGlobalToken(settings.find(s => s.key === 'GLOBAL_TESTIT_TOKEN')?.value || '')}
                  >
                    Обновить токен
                  </Button>
                </Paper>
              </Grid>
              
              <Grid item xs={12} md={4}>
                <Paper sx={{ p: 2 }}>
                  <Typography variant="subtitle1" gutterBottom>
                    Расписание сбора статистики
                  </Typography>
                  <TextField
                    fullWidth
                    label="Cron-выражение"
                    margin="normal"
                    value={settings.find(s => s.key === 'API_SCHEDULE_CRON')?.value || ''}
                    onChange={(e) => {
                      const newSettings = [...settings];
                      const index = newSettings.findIndex(s => s.key === 'API_SCHEDULE_CRON');
                      if (index !== -1) {
                        newSettings[index] = { ...newSettings[index], value: e.target.value };
                        setSettings(newSettings);
                      }
                    }}
                  />
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={() => handleUpdateApiSchedule(settings.find(s => s.key === 'API_SCHEDULE_CRON')?.value || '')}
                  >
                    Обновить расписание
                  </Button>
                </Paper>
              </Grid>
              
              <Grid item xs={12} md={4}>
                <Paper sx={{ p: 2 }}>
                  <Typography variant="subtitle1" gutterBottom>
                    Базовый URL API TestIT
                  </Typography>
                  <TextField
                    fullWidth
                    label="URL"
                    margin="normal"
                    value={settings.find(s => s.key === 'API_BASE_URL')?.value || ''}
                    onChange={(e) => {
                      const newSettings = [...settings];
                      const index = newSettings.findIndex(s => s.key === 'API_BASE_URL');
                      if (index !== -1) {
                        newSettings[index] = { ...newSettings[index], value: e.target.value };
                        setSettings(newSettings);
                      }
                    }}
                  />
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={() => handleUpdateApiBaseUrl(settings.find(s => s.key === 'API_BASE_URL')?.value || '')}
                  >
                    Обновить URL
                  </Button>
                </Paper>
              </Grid>
              
              <Grid item xs={12} md={6}>
                <Paper sx={{ p: 2 }}>
                  <Typography variant="subtitle1" gutterBottom>
                    Cookies для TestIT API
                  </Typography>
                  <TextField
                    fullWidth
                    label="Cookies"
                    margin="normal"
                    multiline
                    rows={4}
                    value={settings.find(s => s.key === 'TESTIT_COOKIES')?.value || ''}
                    onChange={(e) => {
                      const newSettings = [...settings];
                      const index = newSettings.findIndex(s => s.key === 'TESTIT_COOKIES');
                      if (index !== -1) {
                        newSettings[index] = { ...newSettings[index], value: e.target.value };
                        setSettings(newSettings);
                      }
                    }}
                  />
                  <Button
                    variant="contained"
                    fullWidth
                    onClick={() => handleUpdateTestItCookies(settings.find(s => s.key === 'TESTIT_COOKIES')?.value || '')}
                    sx={{ mb: 2 }}
                  >
                    Обновить Cookies
                  </Button>
                  
                  <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mt: 2 }}>
                    <Typography variant="body1">
                      Использовать Cookies для TestIT API
                    </Typography>
                    <Button
                      variant="contained"
                      color={settings.find(s => s.key === 'USE_TESTIT_COOKIES')?.value === 'true' ? 'success' : 'error'}
                      onClick={() => handleToggleUseTestItCookies(settings.find(s => s.key === 'USE_TESTIT_COOKIES')?.value !== 'true')}
                    >
                      {settings.find(s => s.key === 'USE_TESTIT_COOKIES')?.value === 'true' ? 'Включено' : 'Выключено'}
                    </Button>
                  </Box>
                </Paper>
              </Grid>
            </Grid>
          </Box>
        </TabPanel>
        
        {/* Вкладка пользователей */}
        <TabPanel value={tabValue} index={1}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
            <Typography variant="h6">Управление пользователями</Typography>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => handleOpenUserDialog()}
            >
              Добавить пользователя
            </Button>
          </Box>
          
          {loadingUsers ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', p: 3 }}>
              <CircularProgress />
            </Box>
          ) : (
            <List>
              {users.map((user) => (
                <React.Fragment key={user.id}>
                  <ListItem
                    secondaryAction={
                      <>
                        <IconButton edge="end" aria-label="edit" onClick={() => handleOpenUserDialog(user)} sx={{ mr: 1 }}>
                          <EditIcon />
                        </IconButton>
                        <IconButton edge="end" aria-label="delete" onClick={() => handleDeleteUser(user.id)}>
                          <DeleteIcon />
                        </IconButton>
                      </>
                    }
                  >
                    <ListItemText
                      primary={user.username}
                      secondary={
                        <>
                          <Typography component="span" variant="body2" color="text.primary">
                            {user.email}
                          </Typography>
                          <br />
                          Роль: {user.role}
                        </>
                      }
                    />
                  </ListItem>
                  <Divider />
                </React.Fragment>
              ))}
            </List>
          )}
        </TabPanel>
        
        {/* Вкладка сбора статистики */}
        <TabPanel value={tabValue} index={2}>
          <Typography variant="h6" gutterBottom>
            Сбор статистики для всех проектов
          </Typography>
          
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="body1" paragraph>
              Запустите сбор статистики для всех проектов в системе. Это может занять некоторое время.
            </Typography>
            
            <Grid container spacing={2} alignItems="center">
              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  label="Начальная дата"
                  type="date"
                  value={statsStartDate}
                  onChange={(e) => setStatsStartDate(e.target.value)}
                  InputLabelProps={{ shrink: true }}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <TextField
                  fullWidth
                  label="Конечная дата"
                  type="date"
                  value={statsEndDate}
                  onChange={(e) => setStatsEndDate(e.target.value)}
                  InputLabelProps={{ shrink: true }}
                  margin="normal"
                />
              </Grid>
              <Grid item xs={12} sm={4}>
                <Button
                  variant="contained"
                  startIcon={<RefreshIcon />}
                  onClick={handleCollectAllStatistics}
                  disabled={collectingStats}
                  sx={{ mt: 2 }}
                  fullWidth
                >
                  {collectingStats ? 'Сбор...' : 'Запустить сбор'}
                </Button>
              </Grid>
            </Grid>
          </Paper>
          
          <Typography variant="h6" gutterBottom>
            Обновление имен пользователей
          </Typography>
          
          <Paper sx={{ p: 3 }}>
            <Typography variant="body1" paragraph>
              Обновите имена пользователей из TestIT для всех записей статистики. Это поможет отображать реальные имена пользователей вместо идентификаторов.
            </Typography>
            
            <Button
              variant="contained"
              color="primary"
              startIcon={<RefreshIcon />}
              onClick={handleUpdateAllUsernames}
              disabled={updatingUsernames}
              fullWidth
            >
              {updatingUsernames ? 'Обновление...' : 'Обновить имена пользователей'}
            </Button>
          </Paper>
        </TabPanel>
      </Paper>
      
      {/* Диалог настройки */}
      <Dialog open={openSettingDialog} onClose={handleCloseSettingDialog}>
        <DialogTitle>{editingSetting ? 'Редактировать настройку' : 'Добавить настройку'}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Введите данные настройки:
          </DialogContentText>
          <TextField
            autoFocus
            margin="dense"
            label="Ключ"
            fullWidth
            variant="outlined"
            value={editingSetting ? editingSetting.key : newSetting.key}
            onChange={(e) => {
              if (editingSetting) {
                setEditingSetting({ ...editingSetting, key: e.target.value });
              } else {
                setNewSetting({ ...newSetting, key: e.target.value });
              }
            }}
            disabled={!!editingSetting}
          />
          <TextField
            margin="dense"
            label="Значение"
            fullWidth
            variant="outlined"
            value={editingSetting ? editingSetting.value : newSetting.value}
            onChange={(e) => {
              if (editingSetting) {
                setEditingSetting({ ...editingSetting, value: e.target.value });
              } else {
                setNewSetting({ ...newSetting, value: e.target.value });
              }
            }}
          />
          <TextField
            margin="dense"
            label="Описание"
            fullWidth
            variant="outlined"
            value={editingSetting ? editingSetting.description : newSetting.description}
            onChange={(e) => {
              if (editingSetting) {
                setEditingSetting({ ...editingSetting, description: e.target.value });
              } else {
                setNewSetting({ ...newSetting, description: e.target.value });
              }
            }}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseSettingDialog}>Отмена</Button>
          <Button onClick={handleSaveSetting}>Сохранить</Button>
        </DialogActions>
      </Dialog>
      
      {/* Диалог пользователя */}
      <Dialog open={openUserDialog} onClose={handleCloseUserDialog}>
        <DialogTitle>{editingUser ? 'Редактировать пользователя' : 'Добавить пользователя'}</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Введите данные пользователя:
          </DialogContentText>
          <TextField
            autoFocus
            margin="dense"
            label="Имя пользователя"
            fullWidth
            variant="outlined"
            value={editingUser ? editingUser.username : newUser.username}
            onChange={(e) => {
              if (editingUser) {
                setEditingUser({ ...editingUser, username: e.target.value });
              } else {
                setNewUser({ ...newUser, username: e.target.value });
              }
            }}
          />
          <TextField
            margin="dense"
            label="Email"
            type="email"
            fullWidth
            variant="outlined"
            value={editingUser ? editingUser.email : newUser.email}
            onChange={(e) => {
              if (editingUser) {
                setEditingUser({ ...editingUser, email: e.target.value });
              } else {
                setNewUser({ ...newUser, email: e.target.value });
              }
            }}
          />
          {!editingUser && (
            <TextField
              margin="dense"
              label="Пароль"
              type="password"
              fullWidth
              variant="outlined"
              value={newUser.password}
              onChange={(e) => setNewUser({ ...newUser, password: e.target.value })}
            />
          )}
          <TextField
            margin="dense"
            label="Роль"
            select
            fullWidth
            variant="outlined"
            value={editingUser ? editingUser.role : newUser.role}
            onChange={(e) => {
              if (editingUser) {
                setEditingUser({ ...editingUser, role: e.target.value });
              } else {
                setNewUser({ ...newUser, role: e.target.value });
              }
            }}
            SelectProps={{
              native: true,
            }}
          >
            <option value="USER">Пользователь</option>
            <option value="ADMIN">Администратор</option>
          </TextField>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseUserDialog}>Отмена</Button>
          <Button onClick={handleSaveUser}>Сохранить</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default AdminPage;
