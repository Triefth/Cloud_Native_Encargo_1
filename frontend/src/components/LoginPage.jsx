import React, { useState } from 'react';
import { 
  Stethoscope, 
  ShieldCheck, 
  UserCheck, 
  KeyRound, 
  Activity, 
  Video, 
  FileText, 
  Users, 
  Building2, 
  ArrowRight,
  Sparkles,
  Lock
} from 'lucide-react';

export default function LoginPage({ onLogin, onDemoLogin, bffStatus, authReady }) {
  const [customEmail, setCustomEmail] = useState('medico.rural@telemedicina.cl');
  const [customRole, setCustomRole] = useState('MEDICO');

  const handleCustomDemoSubmit = (e) => {
    e.preventDefault();
    onDemoLogin(customEmail, customRole);
  };

  const demoRoles = [
    {
      role: 'MEDICO',
      email: 'medico.rural@telemedicina.cl',
      title: 'Médico Rural',
      desc: 'Acceso a atención de teleconsultas, emisión de recetas y fichas clínicas.',
      color: 'var(--accent-teal)',
      badgeClass: 'badge-info'
    },
    {
      role: 'PACIENTE',
      email: 'paciente.rural@telemedicina.cl',
      title: 'Paciente Rural',
      desc: 'Consulta de citas programadas, historial de atenciones y notificaciones.',
      color: 'var(--accent-emerald)',
      badgeClass: 'badge-success'
    },
    {
      role: 'ADMIN',
      email: 'admin.salud@telemedicina.cl',
      title: 'Administrador de Salud',
      desc: 'Gestión de clínicas rurales, métricas de resiliencia y reportes globales.',
      color: 'var(--accent-purple)',
      badgeClass: 'badge-warning'
    }
  ];

  const microservices = [
    { name: 'BFF Gateway', icon: ShieldCheck, status: bffStatus },
    { name: 'Agenda Citas', icon: Activity, status: 'online' },
    { name: 'Teleconsulta CPaaS', icon: Video, status: 'online' },
    { name: 'Fichas Médicas', icon: FileText, status: 'online' },
    { name: 'Pacientes & Médicos', icon: Users, status: 'online' },
    { name: 'Clínicas Rurales', icon: Building2, status: 'online' },
  ];

  return (
    <div className="login-container">
      <div className="login-wrapper">
        
        {/* Header / Branding */}
        <div className="login-header">
          <div className="login-logo-glow">
            <Stethoscope size={38} color="#fff" />
          </div>
          <h1 className="login-title text-gradient">Telemedicina Rural</h1>
          <p className="login-subtitle">
            Plataforma Cloud Native desacoplada • DSY1107 Desarrollo Cloud Native I
          </p>
          <div className="login-status-bar">
            <span className="badge badge-info" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
              <span className={`status-dot ${bffStatus === 'online' ? 'online' : 'offline'}`}></span>
              BFF Gateway (Port 8080): {bffStatus === 'online' ? 'ONLINE' : 'DESCONECTADO'}
            </span>
          </div>
        </div>

        {/* Login Cards Grid */}
        <div className="login-grid">
          
          {/* Card 1: Microsoft Entra ID */}
          <div className="glass-card login-card primary-card">
            <div className="card-badge">
              <Sparkles size={14} /> Recomendado Producción
            </div>
            <h2 className="card-title">
              <ShieldCheck size={24} style={{ color: 'var(--accent-teal)' }} />
              Microsoft Entra ID
            </h2>
            <p className="card-desc">
              Autenticación empresarial con OAuth2 / OIDC. Obtén un token Bearer JWT validado con firma HMAC por el microservicio <code>bff-service</code>.
            </p>

            <div style={{ marginTop: '24px', display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <button 
                className="btn btn-primary btn-login-main"
                onClick={onLogin}
                disabled={!authReady}
              >
                <KeyRound size={20} />
                Iniciar sesión con Microsoft
                <ArrowRight size={18} />
              </button>
              <span className="login-hint">
                <Lock size={13} /> Token JWT administrado por MSAL Browser SDK
              </span>
            </div>
          </div>

          {/* Card 2: Quick Demo Access */}
          <div className="glass-card login-card">
            <h2 className="card-title">
              <UserCheck size={24} style={{ color: 'var(--accent-emerald)' }} />
              Acceso Rápido Demo
            </h2>
            <p className="card-desc">
              Acceso directo inmediato para evaluación del encargo sin requerir credenciales de Microsoft Entra ID.
            </p>

            {/* Demo Roles selection */}
            <div className="demo-roles-container">
              {demoRoles.map((item) => (
                <div 
                  key={item.role}
                  className="demo-role-card"
                  onClick={() => onDemoLogin(item.email, item.role)}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <span className={`badge ${item.badgeClass}`}>{item.role}</span>
                    <ArrowRight size={14} style={{ color: item.color }} />
                  </div>
                  <h4 style={{ fontSize: '0.95rem', marginTop: '6px', color: '#fff' }}>{item.title}</h4>
                  <p style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', marginTop: '2px' }}>{item.desc}</p>
                </div>
              ))}
            </div>

            {/* Custom Email Form */}
            <form onSubmit={handleCustomDemoSubmit} className="demo-custom-form">
              <div style={{ display: 'flex', gap: '8px' }}>
                <input 
                  type="email"
                  className="form-input"
                  style={{ fontSize: '0.85rem' }}
                  value={customEmail}
                  onChange={(e) => setCustomEmail(e.target.value)}
                  placeholder="correo@telemedicina.cl"
                  required
                />
                <select 
                  className="form-select"
                  style={{ width: '130px', fontSize: '0.85rem' }}
                  value={customRole}
                  onChange={(e) => setCustomRole(e.target.value)}
                >
                  <option value="MEDICO">MÉDICO</option>
                  <option value="PACIENTE">PACIENTE</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </div>
              <button type="submit" className="btn btn-secondary btn-sm" style={{ width: '100%', marginTop: '8px' }}>
                Entrar con correo personalizado
              </button>
            </form>
          </div>

        </div>

        {/* Microservices Footer Preview */}
        <div className="login-services-preview">
          <h3 className="preview-title">Arquitectura de Microservicios Habilitada</h3>
          <div className="services-pills-grid">
            {microservices.map((srv, idx) => {
              const IconComp = srv.icon;
              return (
                <div key={idx} className="service-pill">
                  <IconComp size={16} style={{ color: 'var(--accent-teal)' }} />
                  <span>{srv.name}</span>
                </div>
              );
            })}
          </div>
        </div>

      </div>
    </div>
  );
}
