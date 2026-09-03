import React from 'react';
import { 
  Activity, 
  Calendar, 
  Video, 
  FileText, 
  Users, 
  Bell, 
  Building2, 
  BarChart3, 
  ShieldCheck, 
  Stethoscope 
} from 'lucide-react';

export default function Navbar({ activeTab, setActiveTab, bffStatus, activeToken }) {
  const navItems = [
    { id: 'health', label: 'Resiliencia & Salud', icon: Activity },
    { id: 'jwt', label: 'Dev JWT MSAL', icon: ShieldCheck },
    { id: 'citas', label: 'Agenda Citas', icon: Calendar },
    { id: 'consultas', label: 'Teleconsulta CPaaS', icon: Video },
    { id: 'fichas', label: 'Fichas Médicas', icon: FileText },
    { id: 'usuarios', label: 'Pacientes & Médicos', icon: Users },
    { id: 'notificaciones', label: 'Notificaciones', icon: Bell },
    { id: 'clinicas', label: 'Clínicas Rurales', icon: Building2 },
    { id: 'reportes', label: 'Reportes Operativos', icon: BarChart3 },
  ];

  return (
    <header className="glass-card" style={{ borderRadius: 0, padding: '16px 24px', borderTop: 0, borderLeft: 0, borderRight: 0 }}>
      <div style={{ maxWidth: '1400px', margin: '0 auto', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
        
        {/* Brand */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
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
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div className="badge badge-info" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span className={`status-dot ${bffStatus === 'online' ? 'online' : 'offline'}`}></span>
            BFF Gateway (8080): {bffStatus === 'online' ? 'ONLINE' : 'DESCONECTADO'}
          </div>

          <div className={`badge ${activeToken ? 'badge-success' : 'badge-warning'}`}>
            <ShieldCheck size={14} />
            {activeToken ? 'Token JWT Activo' : 'Sin Token JWT'}
          </div>
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
