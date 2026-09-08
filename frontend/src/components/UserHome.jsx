import React from 'react';
import { 
  User, 
  ShieldCheck, 
  Calendar, 
  Video, 
  FileText, 
  Bell, 
  BarChart3, 
  Activity, 
  Clock, 
  Building2, 
  CheckCircle, 
  ArrowRight,
  Stethoscope,
  Key
} from 'lucide-react';

export default function UserHome({ activeToken, onNavigate, bffStatus }) {
  // Decode user email and role from token if possible
  let userInfo = {
    username: 'Usuario Autenticado',
    email: 'usuario@telemedicina.cl',
    role: 'MEDICO',
    iss: 'BFF Gateway Telemedicina'
  };

  if (activeToken) {
    try {
      const parts = activeToken.split('.');
      if (parts.length === 3) {
        const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/');
        const jsonPayload = decodeURIComponent(atob(base64).split('').map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)).join(''));
        const payload = JSON.parse(jsonPayload);
        userInfo.username = payload.name || payload.preferred_username || payload.sub || userInfo.username;
        userInfo.email = payload.preferred_username || payload.sub || userInfo.email;
        if (payload.roles && payload.roles.length > 0) {
          userInfo.role = payload.roles[0];
        }
        if (payload.iss) {
          userInfo.iss = payload.iss;
        }
      }
    } catch (e) {
      console.warn('No se pudo decodificar el token para el perfil de inicio:', e);
    }
  }

  const roleBadgeColor = {
    MEDICO: 'badge-info',
    PACIENTE: 'badge-success',
    ADMIN: 'badge-warning'
  }[userInfo.role] || 'badge-info';

  const quickActions = [
    { 
      id: 'citas', 
      title: 'Agenda de Citas', 
      desc: 'Gestión y programación de atenciones médicas rurales.', 
      icon: Calendar,
      color: 'var(--accent-teal)'
    },
    { 
      id: 'consultas', 
      title: 'Teleconsulta CPaaS', 
      desc: 'Sala de atención de video en vivo y emisión de recetas.', 
      icon: Video,
      color: 'var(--accent-blue)'
    },
    { 
      id: 'fichas', 
      title: 'Fichas Médicas', 
      desc: 'Historial clínico unificado y registros de salud.', 
      icon: FileText,
      color: 'var(--accent-emerald)'
    },
    { 
      id: 'notificaciones', 
      title: 'Notificaciones', 
      desc: 'Recordatorios SMS/Email y avisos de pacientes.', 
      icon: Bell,
      color: 'var(--accent-amber)'
    },
    { 
      id: 'reportes', 
      title: 'Reportes Operativos', 
      desc: 'Métricas de resiliencia y actividad de la red rural.', 
      icon: BarChart3,
      color: 'var(--accent-purple)'
    },
    { 
      id: 'clinicas', 
      title: 'Clínicas Rurales', 
      desc: 'Directorio de postas y centros de atención remota.', 
      icon: Building2,
      color: 'var(--accent-indigo)'
    }
  ];

  return (
    <div className="user-home-container" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* User Welcome Card Header */}
      <div className="glass-card welcome-banner" style={{
        background: 'linear-gradient(135deg, rgba(0, 210, 255, 0.12) 0%, rgba(59, 130, 246, 0.08) 50%, rgba(19, 31, 61, 0.9) 100%)',
        border: '1px solid rgba(0, 210, 255, 0.3)',
        position: 'relative',
        overflow: 'hidden'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '20px' }}>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{
              width: '64px',
              height: '64px',
              borderRadius: '18px',
              background: 'var(--gradient-brand)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: 'var(--shadow-glow)'
            }}>
              <User size={32} color="#fff" />
            </div>

            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <h2 style={{ fontSize: '1.6rem', fontWeight: 800 }}>
                  ¡Bienvenido, <span className="text-gradient">{userInfo.username}</span>!
                </h2>
                <span className={`badge ${roleBadgeColor}`}>
                  {userInfo.role}
                </span>
              </div>
              <p style={{ color: 'var(--text-secondary)', marginTop: '4px', fontSize: '0.92rem' }}>
                Conectado como <code style={{ color: 'var(--accent-teal)' }}>{userInfo.email}</code> • Plataforma Telemedicina Rural
              </p>
            </div>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '8px' }}>
            <div className="badge badge-success" style={{ padding: '6px 14px', fontSize: '0.82rem' }}>
              <CheckCircle size={14} /> Sesión Activa
            </div>
            <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Clock size={13} /> {new Date().toLocaleDateString('es-CL', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
            </span>
          </div>

        </div>
      </div>

      {/* Quick Summary Grid */}
      <div className="grid-4">
        
        <div className="glass-card" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Estado Gateway</span>
            <div className={`status-dot ${bffStatus === 'online' ? 'online' : 'offline'}`} />
          </div>
          <h3 style={{ fontSize: '1.4rem', color: bffStatus === 'online' ? '#34d399' : '#f87171' }}>
            {bffStatus === 'online' ? 'BFF Online' : 'Offline'}
          </h3>
          <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>Puerto 8080 • API Gateway</p>
        </div>

        <div className="glass-card" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Autenticación</span>
            <ShieldCheck size={20} style={{ color: 'var(--accent-teal)' }} />
          </div>
          <h3 style={{ fontSize: '1.4rem', color: '#fff' }}>JWT Validado</h3>
          <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>HMAC / OAuth2 Signature</p>
        </div>

        <div className="glass-card" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Microservicios</span>
            <Activity size={20} style={{ color: 'var(--accent-emerald)' }} />
          </div>
          <h3 style={{ fontSize: '1.4rem', color: '#fff' }}>8 Operativos</h3>
          <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>Resiliencia Cloud Native</p>
        </div>

        <div className="glass-card" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Rol Conectado</span>
            <Stethoscope size={20} style={{ color: 'var(--accent-purple)' }} />
          </div>
          <h3 style={{ fontSize: '1.4rem', color: '#fff' }}>{userInfo.role}</h3>
          <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>Permisos configurados</p>
        </div>

      </div>

      {/* Modules Quick Access Section */}
      <div>
        <div style={{ marginBottom: '16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <div>
            <h3 style={{ fontSize: '1.25rem' }}>Módulos Principales de la Plataforma</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Selecciona una opción para administrar la atención de telemedicina</p>
          </div>
        </div>

        <div className="grid-3">
          {quickActions.map((action) => {
            const Icon = action.icon;
            return (
              <div 
                key={action.id}
                className="glass-card module-card"
                onClick={() => onNavigate(action.id)}
                style={{ 
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  justify: 'space-between',
                  padding: '24px',
                  transition: 'all 0.25s ease'
                }}
              >
                <div>
                  <div style={{ 
                    width: '44px', 
                    height: '44px', 
                    borderRadius: '12px', 
                    background: 'rgba(255, 255, 255, 0.05)', 
                    border: '1px solid var(--border-color)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    marginBottom: '16px'
                  }}>
                    <Icon size={24} style={{ color: action.color }} />
                  </div>
                  <h4 style={{ fontSize: '1.1rem', marginBottom: '6px', color: '#fff' }}>{action.title}</h4>
                  <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.4 }}>{action.desc}</p>
                </div>

                <div style={{ 
                  marginTop: '20px', 
                  display: 'flex', 
                  alignItems: 'center', 
                  gap: '6px', 
                  fontSize: '0.85rem', 
                  fontWeight: 600,
                  color: action.color 
                }}>
                  Ingresar a módulo
                  <ArrowRight size={16} />
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Session Details Footer */}
      <div className="glass-card" style={{ padding: '20px' }}>
        <h4 style={{ fontSize: '0.95rem', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Key size={16} style={{ color: 'var(--accent-teal)' }} />
          Detalles de Autenticación de Usuario
        </h4>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '12px', fontSize: '0.82rem', color: 'var(--text-secondary)' }}>
          <div><strong>Usuario:</strong> {userInfo.username}</div>
          <div><strong>Correo:</strong> {userInfo.email}</div>
          <div><strong>Rol Activo:</strong> {userInfo.role}</div>
          <div><strong>Emisor (iss):</strong> {userInfo.iss}</div>
        </div>
      </div>

    </div>
  );
}
