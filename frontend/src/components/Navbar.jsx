import React from 'react';
import { 
  Home,
  Activity, 
  Calendar, 
  Video, 
  FileText, 
  Users, 
  Bell, 
  Building2, 
  BarChart3, 
  ShieldCheck, 
  Stethoscope,
  User
} from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab, bffStatus, activeToken, authReady, onLogin, onLogout }) {
  let userLabel = '';
  if (activeToken) {
    try {
      const parts = activeToken.split('.');
      if (parts.length === 3) {
        const payload = JSON.parse(atob(parts[1]));
        userLabel = payload.name || payload.preferred_username || payload.sub || '';
      }
    } catch (e) {
      // ignore
    }
  }

  const navItems = [
    { id: 'inicio', label: 'Inicio', icon: Home },
    { id: 'citas', label: 'Agenda Citas', icon: Calendar },
    { id: 'consultas', label: 'Teleconsulta CPaaS', icon: Video },
    { id: 'fichas', label: 'Fichas Médicas', icon: FileText },
    { id: 'usuarios', label: 'Pacientes & Médicos', icon: Users },
    { id: 'notificaciones', label: 'Notificaciones', icon: Bell },
    { id: 'clinicas', label: 'Clínicas Rurales', icon: Building2 },
    { id: 'reportes', label: 'Reportes Operativos', icon: BarChart3 },
    { id: 'health', label: 'Resiliencia & Salud', icon: Activity },
    { id: 'jwt', label: 'Dev JWT MSAL', icon: ShieldCheck },
  ];

  return (
    <header className="glass-card" style={{ borderRadius: 0, padding: '16px 24px', borderTop: 0, borderLeft: 0, borderRight: 0 }}>
      <div style={{ maxWidth: '1400px', margin: '0 auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        
        {/* Brand */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }} onClick={() => setActiveTab('inicio')}>
          <div style={{ 
            width: '42px', 
            height: '42px', 
            borderRadius: '12px', 
            background: 'var(--gradient-brand)', 
            display: 'flex', 
            alignItems: 'center', 
            justifyContent: 'center',
            boxShadow: 'var(--shadow-glow)'
          }}>
            <Stethoscope size={24} color="#fff" />
          </div>
          <div>
            <h1 style={{ fontSize: '1.25rem', fontWeight: 800 }} className="text-gradient">
              Telemedicina Rural
            </h1>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
              DSY1107 • Plataforma Cloud Native desacoplada
            </p>
          </div>
        </div>

        {/* Status Indicators */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px', flexWrap: 'wrap' }}>
          <div className="badge badge-info" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span className={`status-dot ${bffStatus === 'online' ? 'online' : 'offline'}`}></span>
            BFF Gateway (8080): {bffStatus === 'online' ? 'ONLINE' : 'DESCONECTADO'}
          </div>

          {userLabel && (
            <div className="badge badge-success" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <User size={13} />
              <span>{userLabel}</span>
            </div>
          )}

          <div className={`badge ${activeToken ? 'badge-success' : 'badge-warning'}`}>
            <ShieldCheck size={14} />
            {activeToken ? 'JWT Activo' : 'Sin Token'}
          </div>

          {activeToken ? (
            <button className="btn btn-secondary btn-sm" onClick={onLogout}>Cerrar sesión</button>
          ) : (
            <button className="btn btn-primary btn-sm" onClick={onLogin} disabled={!authReady}>Iniciar sesión</button>
          )}
        </div>
      </div>

      {/* Tabs */}
      <nav style={{ maxWidth: '1400px', margin: '16px auto 0 auto', display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '4px' }}>
        {navItems.map((item) => {
          const Icon = item.icon;
          const isActive = activeTab === item.id;
          return (
            <button
              key={item.id}
              onClick={() => setActiveTab(item.id)}
              className="btn"
              style={{
                background: isActive ? 'var(--gradient-brand)' : 'rgba(255, 255, 255, 0.05)',
                color: isActive ? '#ffffff' : 'var(--text-secondary)',
                border: isActive ? 'none' : '1px solid var(--border-color)',
                fontSize: '0.85rem',
                padding: '8px 14px',
                borderRadius: 'var(--radius-sm)'
              }}
            >
              <Icon size={16} />
              {item.label}
            </button>
          );
        })}
      </nav>
    </header>
  );
}
