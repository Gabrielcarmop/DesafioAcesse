import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 30000,
});

// ── Lotes ──────────────────────────────────────────────────────────────────
export const uploadLote = (file, onProgress) => {
  const form = new FormData();
  form.append('file', file);
  return api.post('/lotes', form, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (e) => {
      const pct = Math.round((e.loaded * 100) / e.total);
      onProgress?.(pct);
    },
  });
};

export const getLotes = (page = 0, size = 10) =>
  api.get('/lotes', { params: { page, size } });

export const getLoteStatus = (id) =>
  api.get(`/lotes/${id}/status`);

// ── Leads ──────────────────────────────────────────────────────────────────
export const getLeads = (params) =>
  api.get('/leads', { params });

// ── Dashboard ──────────────────────────────────────────────────────────────
export const getDashboard = () =>
  api.get('/dashboard');

export default api;

export const getLoteChunks = (id) =>
  api.get(`/lotes/${id}/chunks`);
