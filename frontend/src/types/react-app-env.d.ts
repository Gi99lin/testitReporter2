/// <reference types="react-scripts" />

declare global {
  namespace JSX {
    interface Element {}
    interface IntrinsicElements {
      [elemName: string]: any;
    }
    interface ElementClass {
      render: any;
    }
  }
}

declare module 'react/jsx-runtime' {
  export const jsx: any;
  export const jsxs: any;
  export const Fragment: any;
}

declare module 'react' {
  export type ReactNode = 
    | React.ReactElement
    | string
    | number
    | boolean
    | null
    | undefined;
  
  export interface ReactElement<P = any, T = any> {}
  
  export type FC<P = {}> = FunctionComponent<P>;
  export interface FunctionComponent<P = {}> {
    (props: P): ReactElement | null;
  }
  
  export function useState<T>(initialState: T | (() => T)): [T, (newState: T) => void];
  export function useEffect(effect: () => void | (() => void), deps?: any[]): void;
  export function useContext<T>(context: React.Context<T>): T;
  export function createContext<T>(defaultValue: T): React.Context<T>;
  
  export interface Context<T> {
    Provider: Provider<T>;
    Consumer: Consumer<T>;
  }
  
  export interface Provider<T> {}
  export interface Consumer<T> {}
  
  export type MouseEvent<T = Element> = any;
  export type FormEvent<T = Element> = any;
  export type SyntheticEvent<T = Element> = any;
  
  export default {
    useState,
    useEffect,
    useContext,
    createContext
  };
}

declare module 'react-dom/client' {
  export function createRoot(container: Element | DocumentFragment): {
    render(element: any): void;
  };
  
  export default {
    createRoot
  };
}

declare module 'react-router-dom' {
  export function useNavigate(): (path: string) => void;
  export function useLocation(): { pathname: string };
  export function Link(props: any): any;
  export function Navigate(props: any): any;
  export function Routes(props: any): any;
  export function Route(props: any): any;
  export function Outlet(): any;
  export function BrowserRouter(props: any): any;
  
  export default {
    useNavigate,
    useLocation,
    Link,
    Navigate,
    Routes,
    Route,
    Outlet,
    BrowserRouter
  };
}

declare module 'web-vitals' {
  export type ReportHandler = (metric: {
    name: string;
    delta: number;
    id: string;
  }) => void;

  export function getCLS(onReport: ReportHandler): void;
  export function getFID(onReport: ReportHandler): void;
  export function getFCP(onReport: ReportHandler): void;
  export function getLCP(onReport: ReportHandler): void;
  export function getTTFB(onReport: ReportHandler): void;
  
  export default {
    getCLS,
    getFID,
    getFCP,
    getLCP,
    getTTFB
  };
}

declare module 'axios' {
  export interface AxiosRequestConfig {
    baseURL?: string;
    headers?: Record<string, string>;
    params?: any;
  }

  export interface AxiosResponse<T = any> {
    data: T;
    status: number;
    statusText: string;
    headers: Record<string, string>;
    config: AxiosRequestConfig;
  }

  export interface AxiosError<T = any> extends Error {
    config: AxiosRequestConfig;
    code?: string;
    request?: any;
    response?: AxiosResponse<T>;
  }

  export interface AxiosInstance {
    (config: AxiosRequestConfig): Promise<AxiosResponse>;
    (url: string, config?: AxiosRequestConfig): Promise<AxiosResponse>;
    defaults: AxiosRequestConfig;
    interceptors: {
      request: any;
      response: any;
    };
    get<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
    post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
    put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
    delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<AxiosResponse<T>>;
  }

  export function create(config?: AxiosRequestConfig): AxiosInstance;
  
  export default {
    create
  };
}

declare module '@mui/material' {
  export const Tabs: any;
  export const Tab: any;
  export const Dialog: any;
  export const DialogTitle: any;
  export const DialogContent: any;
  export const DialogContentText: any;
  export const DialogActions: any;
  export const Box: any;
  export const Typography: any;
  export const Grid: any;
  export const Card: any;
  export const CardContent: any;
  export const CardActions: any;
  export const Button: any;
  export const Divider: any;
  export const CircularProgress: any;
  export const Alert: any;
  export const Avatar: any;
  export const TextField: any;
  export const Link: any;
  export const Container: any;
  export const Paper: any;
  export const AppBar: any;
  export const Toolbar: any;
  export const IconButton: any;
  export const List: any;
  export const ListItem: any;
  export const ListItemButton: any;
  export const ListItemIcon: any;
  export const ListItemText: any;
  export const Drawer: any;
  export const CssBaseline: any;
  export const Menu: any;
  export const MenuItem: any;
}

declare module '@mui/material/styles' {
  export function createTheme(options: any): any;
  export const ThemeProvider: any;
}

declare module '@mui/icons-material' {
  export const Delete: any;
  export const Edit: any;
  export const Refresh: any;
  export const Menu: any;
  export const Dashboard: any;
  export const List: any;
  export const Person: any;
  export const Settings: any;
  export const Logout: any;
  export const LockOutlined: any;
  export const PersonAdd: any;
  export const Assessment: any;
  export const Add: any;
  export const Error: any;
}

declare module 'formik' {
  export function useFormik(options: any): any;
}

declare module 'yup' {
  export function object(schema: any): any;
  export function string(): any;
  export function ref(path: string): any;
}

declare module 'chart.js' {
  export const Chart: any;
  export const ArcElement: any;
  export const Tooltip: any;
  export const Legend: any;
  
  export function register(...components: any[]): void;
}

declare module 'react-chartjs-2' {
  export const Pie: any;
  export const Bar: any;
  export const Line: any;
}

declare module 'date-fns' {
  export function format(date: Date, format: string): string;
  export function subMonths(date: Date, amount: number): Date;
}

declare module 'date-fns/locale' {
  export const ru: any;
}

declare module '@mui/x-date-pickers/DatePicker' {
  export const DatePicker: any;
}

declare module '@mui/x-date-pickers/LocalizationProvider' {
  export const LocalizationProvider: any;
}

declare module '@mui/x-date-pickers/AdapterDateFns' {
  export const AdapterDateFns: any;
}
