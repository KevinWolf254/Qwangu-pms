import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: 'http://localhost:3001/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json',
  },
});

axiosInstance.interceptors.request.use((config) => {
  if (!config.url?.endsWith('/v1/sign-in')) {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
}, (error) => {
  return Promise.reject(error);
});

export default axiosInstance;
