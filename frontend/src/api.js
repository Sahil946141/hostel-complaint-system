import axios from 'axios';
import API_BASE_URL from './config';

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor — attach JWT token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor — handle 401 (token expired)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

// ── Auth APIs ──────────────────────────────────────
export const authAPI = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

// ── Complaint APIs ─────────────────────────────────
export const complaintAPI = {
  create: (data) => api.post('/complaints', data),
  getAll: () => api.get('/complaints'),
  getMy: () => api.get('/complaints/my'),
  getById: (id) => api.get(`/complaints/${id}`),
  updateStatus: (id, data) => api.put(`/complaints/${id}/status`, data),
  getByStatus: (status) => api.get(`/complaints/status/${status}`),
};

// ── Admin APIs ─────────────────────────────────────
export const adminAPI = {
  getDashboard: () => api.get('/admin/dashboard'),
  getAllStaff: () => api.get('/admin/staff'),
  getAllUsers: () => api.get('/admin/users'),
};

export default api;
