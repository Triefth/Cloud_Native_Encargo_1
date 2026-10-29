// API Client for Telemedicina Rural BFF Gateway

const gatewayUrl = import.meta.env.VITE_API_GATEWAY_URL;
const BASE_URL = gatewayUrl ? (gatewayUrl.replace(/\/$/, '') + '/api/bff') : '/api/bff';

export const getAuthToken = () => {
  return localStorage.getItem('bff_jwt_token') || '';
};

export const setAuthToken = (token) => {
  if (token) {
    localStorage.setItem('bff_jwt_token', token);
  } else {
    localStorage.removeItem('bff_jwt_token');
  }
};

export const apiRequest = async (endpoint, options = {}) => {
  const token = getAuthToken();
  
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const timeoutMs = options.timeout || 8000;
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), timeoutMs);

  const config = {
    ...options,
    headers,
    signal: controller.signal,
  };

  try {
    const response = await fetch(`${BASE_URL}${endpoint}`, config);
    clearTimeout(timeoutId);
    const data = await response.json().catch(() => ({}));

    if (!response.ok) {
      return {
        error: true,
        status: response.status,
        message: data.message || data.error || 'Error en la petición',
        details: data.details || null,
        data,
      };
    }

    return { error: false, status: response.status, data };
  } catch (err) {
    clearTimeout(timeoutId);
    return {
      error: true,
      status: 503,
      message: 'No se pudo conectar con el BFF Gateway (http://localhost:8080)',
      details: err.message,
    };
  }
};

// Authentication Services
export const authApi = {
  status: () => apiRequest('/auth/status'),
  validateToken: () => apiRequest('/auth/me'),
};

// Citas Service
export const citasApi = {
  getAll: () => apiRequest('/citas'),
  getById: (id) => apiRequest(`/citas/${id}`),
  getByPaciente: (rut) => apiRequest(`/citas/paciente/${rut}`),
  getByMedico: (rut) => apiRequest(`/citas/medico/${rut}`),
  create: (cita) =>
    apiRequest('/citas', {
      method: 'POST',
      body: JSON.stringify(cita),
    }),
  confirm: (id) =>
    apiRequest(`/citas/${id}/confirmar`, { method: 'PUT' }),
  cancel: (id) =>
    apiRequest(`/citas/${id}/cancelar`, { method: 'PUT' }),
  reschedule: (id, nuevaFechaHora) =>
    apiRequest(`/citas/${id}/reprogramar`, {
      method: 'PUT',
      body: JSON.stringify({ nuevaFechaHora }),
    }),
};

// Consultas Service
export const consultasApi = {
  getAll: () => apiRequest('/consultas'),
  getByCitaId: (citaId) => apiRequest(`/consultas/cita/${citaId}`),
  getByPaciente: (rut) => apiRequest(`/consultas/paciente/${rut}`),
  start: (citaId, rutPaciente, rutMedico) =>
    apiRequest('/consultas/iniciar', {
      method: 'POST',
      body: JSON.stringify({ citaId, rutPaciente, rutMedico }),
    }),
  finish: (id, diagnosticoPreliminar, indicacionesMedicas, duracionMinutos) =>
    apiRequest(`/consultas/${id}/finalizar`, {
      method: 'PUT',
      body: JSON.stringify({ diagnosticoPreliminar, indicacionesMedicas, duracionMinutos }),
    }),
};

// Fichas Service
export const fichasApi = {
  getAll: () => apiRequest('/fichas'),
  getByRut: (rut) => apiRequest(`/fichas/paciente/${rut}`),
  create: (ficha) =>
    apiRequest('/fichas', {
      method: 'POST',
      body: JSON.stringify(ficha),
    }),
  addAtencion: (rut, consultaId, nombreMedico, especialidad, resumenAtencion) =>
    apiRequest(`/fichas/paciente/${rut}/atencion`, {
      method: 'POST',
      body: JSON.stringify({ consultaId, nombreMedico, especialidad, resumenAtencion }),
    }),
};

// Usuarios Service
export const usuariosApi = {
  getPacientes: () => apiRequest('/usuarios/pacientes'),
  getPacienteByRut: (rut) => apiRequest(`/usuarios/pacientes/${rut}`),
  createPaciente: (paciente) =>
    apiRequest('/usuarios/pacientes', {
      method: 'POST',
      body: JSON.stringify(paciente),
    }),
  getMedicos: () => apiRequest('/usuarios/medicos'),
  getMedicoByRut: (rut) => apiRequest(`/usuarios/medicos/${rut}`),
  getMedicosByEspecialidad: (especialidad) =>
    apiRequest(`/usuarios/medicos/especialidad/${especialidad}`),
  createMedico: (medico) =>
    apiRequest('/usuarios/medicos', {
      method: 'POST',
      body: JSON.stringify(medico),
    }),
};

// Notificaciones Service
export const notificacionesApi = {
  getAll: () => apiRequest('/notificaciones'),
  getByPaciente: (rut) => apiRequest(`/notificaciones/paciente/${rut}`),
  sendReminder: (citaId, rutPaciente, tipo, mensaje) =>
    apiRequest('/notificaciones/recordatorio', {
      method: 'POST',
      body: JSON.stringify({ citaId, rutPaciente, tipo, mensaje }),
    }),
  markRead: (id) =>
    apiRequest(`/notificaciones/${id}/lectura`, { method: 'PUT' }),
};

// Clinicas Service
export const clinicasApi = {
  getAll: () => apiRequest('/clinicas'),
  getById: (id) => apiRequest(`/clinicas/${id}`),
  create: (clinica) =>
    apiRequest('/clinicas', {
      method: 'POST',
      body: JSON.stringify(clinica),
    }),
  updateEhrConfig: (id, ehrConfig) =>
    apiRequest(`/clinicas/${id}/configuracion-ehr`, {
      method: 'PUT',
      body: JSON.stringify(ehrConfig),
    }),
};

// Reportes Service
export const reportesApi = {
  getAll: () => apiRequest('/reportes'),
  getResumen: () => apiRequest('/reportes/resumen'),
  getByModulo: (modulo) => apiRequest(`/reportes/modulo/${modulo}`),
  createEvent: (reporte) =>
    apiRequest('/reportes/evento', {
      method: 'POST',
      body: JSON.stringify(reporte),
    }),
};

// Kafka Topics & Partitions Service (Guía 2)
export const kafkaApi = {
  send: (message, key) => {
    const params = new URLSearchParams({ message });
    if (key) params.append('key', key);
    return apiRequest(`/kafka/send?${params.toString()}`);
  },
  getMessages: () => apiRequest('/kafka/messages'),
  clearMessages: () => apiRequest('/kafka/messages', { method: 'DELETE' }),
  health: () => apiRequest('/kafka/health'),
};

// Banco & Kafka Streams Pipeline Service (Guía 1)
export const bancoApi = {
  sendTransaccion: (transaccion) =>
    apiRequest('/banco/transaccion', {
      method: 'POST',
      body: JSON.stringify(transaccion),
    }),
  getAlertas: () => apiRequest('/banco/alertas'),
  clearAlertas: () => apiRequest('/banco/alertas', { method: 'DELETE' }),
  getTransacciones: () => apiRequest('/banco/transacciones'),
  health: () => apiRequest('/banco/health'),
};

// Tuning, Pruebas de Carga & Consumer Lag (PDF 2 y PDF 3)
export const tuningApi = {
  startLoadTest: (count = 10000) =>
    apiRequest(`/tuning/load?count=${count}`, {
      method: 'POST',
      timeout: 30000,
    }),
  getMetrics: () => apiRequest('/tuning/metrics'),
  setConsumerDelay: (delayMs) =>
    apiRequest(`/tuning/consumer/delay?delayMs=${delayMs}`, { method: 'POST' }),
  togglePause: (pause) =>
    apiRequest(`/tuning/consumer/pause${pause !== undefined ? `?pause=${pause}` : ''}`, { method: 'POST' }),
  reset: () => apiRequest('/tuning/reset', { method: 'POST' }),
};

// Manejo de Errores, DLT, Idempotencia y Observabilidad (PDF 1)
export const resilienceApi = {
  sendTransaction: (tx) =>
    apiRequest('/resilience/send', {
      method: 'POST',
      body: JSON.stringify(tx),
    }),
  simulateValidationError: (type = 'monto_negativo') =>
    apiRequest(`/resilience/simulate-validation-error?type=${type}`, { method: 'POST' }),
  simulatePoisonPill: () =>
    apiRequest('/resilience/simulate-poison-pill', { method: 'POST' }),
  simulateDuplicate: (customId) =>
    apiRequest(`/resilience/simulate-duplicate${customId ? `?customId=${encodeURIComponent(customId)}` : ''}`, { method: 'POST' }),
  simulateTransientError: () =>
    apiRequest('/resilience/simulate-transient-error', { method: 'POST' }),
  simulatePermanentError: () =>
    apiRequest('/resilience/simulate-permanent-error', { method: 'POST' }),
  getDltRecords: () => apiRequest('/resilience/dlt'),
  reprocessDlt: (id) =>
    apiRequest(`/resilience/dlt/reprocess/${id}`, { method: 'POST' }),
  clearDlt: () => apiRequest('/resilience/dlt', { method: 'DELETE' }),
  getAuditLogs: () => apiRequest('/resilience/audit-logs'),
  getProcessedTransactions: () => apiRequest('/resilience/processed'),
  getProcessedKeys: () => apiRequest('/resilience/processed-keys'),
  reset: () => apiRequest('/resilience/reset', { method: 'POST' }),
};


