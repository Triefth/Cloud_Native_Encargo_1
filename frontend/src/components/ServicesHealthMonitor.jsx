import React, { useState, useEffect } from 'react';
import { apiRequest } from '../services/api';
import { Activity, Server, RefreshCw, CheckCircle, AlertTriangle, ShieldCheck } from 'lucide-react';

const SERVICES_LIST = [
  { id: 'bff', name: 'BFF / API Gateway', port: 8080, endpoint: '/auth/validate-token', desc: 'Validación JWT Azure AD y Enrutamiento' },
  { id: 'citas', name: 'Agenda de Citas', port: 8081, endpoint: '/citas', desc: 'Programación y gestión de citas médicas' },
  { id: 'consultas', name: 'Consultas en Línea', port: 8082, endpoint: '/consultas', desc: 'Salas de videollamadas CPaaS HIPAA' },
  { id: 'fichas', name: 'Fichas Médicas', port: 8083, endpoint: '/fichas', desc: 'Historial y registros médicos sincronizados' },
  { id: 'notificaciones', name: 'Notificaciones', port: 8084, endpoint: '/notificaciones', desc: 'Alertas SMS/WhatsApp/Email anti no-show' },
  { id: 'reportes', name: 'Informes Operativos', port: 8085, endpoint: '/reportes/resumen', desc: 'Métricas de disponibilidad y latencia' },
  { id: 'usuarios', name: 'Maestro de Usuarios', port: 8086, endpoint: '/usuarios/pacientes', desc: 'Directorio de Pacientes y Médicos' },
  { id: 'clinicas', name: 'Clínicas Rurales', port: 8087, endpoint: '/clinicas', desc: 'Catálogo de clínicas y credenciales EHR' },
];

export default function ServicesHealthMonitor() {
  const [healthStatus, setHealthStatus] = useState({});
  const [loading, setLoading] = useState(false);
  const [lastCheck, setLastCheck] = useState(null);

  const checkHealth = async () => {
    setLoading(true);
    const newStatus = {};

    for (const service of SERVICES_LIST) {
      const startTime = performance.now();
      let res;
      if (service.id === 'bff') {
        res = await apiRequest('/auth/validate-token', { method: 'POST', body: JSON.stringify({ token: 'test' }) });
      } else {
        res = await apiRequest(service.endpoint);
      }
      const endTime = performance.now();
      const latency = Math.round(endTime - startTime);

      // Si el status es 503 o falla la conexión, indicamos indisponibilidad controlada
      const isUp = !res.error || (res.status !== 503 && res.status !== 0);

      newStatus[service.id] = {
        online: isUp,
        status: res.status || 503,
        latency,
        message: res.message || 'Operativo',
        fallback: res.status === 503,
      };
    }

    setHealthStatus(newStatus);
    setLoading(false);
    setLastCheck(new Date().toLocaleTimeString());
  };

  useEffect(() => {
    checkHealth();
  }, []);

  const totalServices = SERVICES_LIST.length;
  const onlineCount = Object.values(healthStatus).filter(s => s.online).length;

  return (
    <div>
      <div className="section-header">
        <div>
          <h2 className="section-title">
            <Activity size={28} style={{ color: 'var(--accent-teal)' }} />
            Monitor de Salud & Resiliencia de Microservicios
          </h2>
          <p className="section-subtitle">
            Demostración del principio de desacoplamiento: Si un microservicio falla o se detiene, el BFF responde con un código <strong>503 Service Unavailable</strong> controlado y los demás microservicios continúan operando normalmente.
          </p>
        </div>

        <button className="btn btn-secondary" onClick={checkHealth} disabled={loading}>
          <RefreshCw size={16} className={loading ? 'pulsing' : ''} />
          {loading ? 'Comprobando...' : 'Recomprobar Salud'}
        </button>
      </div>

      {/* Global Status Banner */}
      <div className="glass-card" style={{ marginBottom: '24px', background: 'var(--gradient-card)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{
              width: '50px',
              height: '50px',
              borderRadius: 'var(--radius-md)',
              background: onlineCount === totalServices ? 'rgba(16, 185, 129, 0.15)' : 'rgba(245, 158, 11, 0.15)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              border: `1px solid ${onlineCount === totalServices ? '#10b981' : '#f59e0b'}`
            }}>
              <Server size={28} style={{ color: onlineCount === totalServices ? '#10b981' : '#f59e0b' }} />
            </div>
            <div>
              <h3 style={{ fontSize: '1.2rem' }}>
                {onlineCount} / {totalServices} Microservicios Activos
              </h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>
                Última verificación: {lastCheck || 'Cargando...'}
              </p>
            </div>
          </div>

          <div style={{ display: 'flex', gap: '12px' }}>
            <span className="badge badge-success">
              <CheckCircle size={14} /> Tolerante a Fallos
            </span>
            <span className="badge badge-info">
              <ShieldCheck size={14} /> BFF Gateway Activo
            </span>
          </div>
        </div>
      </div>

      {/* Services Grid */}
      <div className="grid-4">
        {SERVICES_LIST.map((service) => {
          const statusInfo = healthStatus[service.id] || { online: false, status: '...', latency: 0 };
          const isOnline = statusInfo.online;

          return (
            <div 
              key={service.id} 
              className="glass-card"
              style={{
                borderLeft: `4px solid ${isOnline ? 'var(--accent-emerald)' : 'var(--accent-rose)'}`,
                padding: '20px'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                <h4 style={{ fontSize: '1.05rem' }}>{service.name}</h4>
                <span className={`status-dot ${isOnline ? 'online' : 'offline'}`}></span>
              </div>

              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '16px' }}>
                {service.desc}
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.8rem', paddingTop: '12px', borderTop: '1px solid var(--border-color)' }}>
                <span style={{ color: 'var(--text-muted)' }}>Puerto: <code>:{service.port}</code></span>
                <span className={`badge ${isOnline ? 'badge-success' : 'badge-danger'}`}>
                  {isOnline ? `${statusInfo.latency}ms` : '503 Fallback'}
                </span>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
