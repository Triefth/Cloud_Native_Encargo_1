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
  Lock,
  User,
  AlertTriangle,
  CheckCircle2
} from 'lucide-react';

export default function LoginPage({ onLogin, onDemoLogin, onDirectLogin, bffStatus, authReady }) {
  const [username, setUsername] = useState('medico.rural@telemedicina.cl');
  const [password, setPassword] = useState('Med123456!');
  const [role, setRole] = useState('MEDICO');
  
  const [errorMessage, setErrorMessage] = useState('');
  const [touched, setTouched] = useState({ username: false, password: false });

  const handleDirectSubmit = (e) => {
    e.preventDefault();
    setTouched({ username: true, password: true });

    const trimmedUser = username.trim();
    const trimmedPass = password.trim();

    if (!trimmedUser || !trimmedPass) {
      setErrorMessage('El usuario y la contraseña no pueden estar en blanco. Por favor complete ambos campos.');
      return;
    }

    setErrorMessage('');
    onDirectLogin(trimmedUser, trimmedPass, role);
  };

  const handleDemoSelect = (demoEmail, demoRole) => {
    setUsername(demoEmail);
    setPassword('DemoPass2026!');
    setRole(demoRole);
    setErrorMessage('');
    onDemoLogin(demoEmail, demoRole);
  };

  const demoRoles = [
    {
      role: 'MEDICO',
      email: 'medico.rural@telemedicina.cl',
      title: 'Médico Rural',
      desc: 'Acceso a atención de teleconsultas, recetas y fichas clínicas.',
      color: 'var(--accent-teal)',
      badgeClass: 'badge-info'
    },
    {
      role: 'PACIENTE',
      email: 'paciente.rural@telemedicina.cl',
      title: 'Paciente Rural',
      desc: 'Consulta de citas programadas, atenciones y notificaciones.',
      color: 'var(--accent-emerald)',
      badgeClass: 'badge-success'
    },
    {
      role: 'ADMIN',
      email: 'admin.salud@telemedicina.cl',
      title: 'Administrador de Salud',
      desc: 'Gestión de clínicas rurales, métricas de resiliencia y reportes.',
      color: 'var(--accent-purple)',
      badgeClass: 'badge-warning'
    }
  ];

  const microservices = [
    { name: 'BFF Gateway (8080)', icon: ShieldCheck, status: bffStatus },
    { name: 'Agenda Citas', icon: Activity, status: 'online' },
    { name: 'Teleconsulta CPaaS', icon: Video, status: 'online' },
    { name: 'Fichas Médicas', icon: FileText, status: 'online' },
    { name: 'Pacientes & Médicos', icon: Users, status: 'online' },
    { name: 'Clínicas Rurales', icon: Building2, status: 'online' },
  ];

  const usernameIsEmpty = touched.username && !username.trim();
  const passwordIsEmpty = touched.password && !password.trim();

  return (
    <div className="login-container">
      <div className="login-wrapper">
        
        {/* Header / Branding */}
        <div className="login-header">
          <div className="login-logo-glow">
            <Stethoscope size={42} color="#fff" />
          </div>
          <h1 className="login-title text-gradient">Telemedicina Rural</h1>
          <p className="login-subtitle">
            Plataforma Cloud Native Desacoplada • DSY1107 Desarrollo Cloud Native I
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
          
          {/* Form Card: Direct Credential Login */}
          <div className="glass-card login-card primary-card">
            <div className="card-badge">
              <Sparkles size={14} /> Autenticación Principal
            </div>

            <h2 className="card-title">
              <KeyRound size={26} style={{ color: 'var(--accent-teal)' }} />
              Iniciar Sesión
            </h2>
            <p className="card-desc">
              Ingrese sus credenciales de usuario y contraseña para acceder a la plataforma.
            </p>

            {/* Error Banner */}
            {errorMessage && (
              <div className="alert-box alert-danger" style={{ marginTop: '16px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                <AlertTriangle size={20} style={{ flexShrink: 0 }} />
                <span>{errorMessage}</span>
              </div>
            )}

            <form onSubmit={handleDirectSubmit} style={{ marginTop: '20px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
              
              {/* Usuario Field */}
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">
                  <User size={14} style={{ display: 'inline', marginRight: '4px' }} />
                  Usuario / Correo
                </label>
                <input 
                  type="text"
                  className={`form-input ${usernameIsEmpty ? 'input-error' : ''}`}
                  value={username}
                  onChange={(e) => {
                    setUsername(e.target.value);
                    if (errorMessage) setErrorMessage('');
                  }}
                  onBlur={() => setTouched({ ...touched, username: true })}
                  placeholder="ej: usuario@telemedicina.cl"
                />
                {usernameIsEmpty && (
                  <span className="error-text">El usuario no puede estar en blanco</span>
                )}
              </div>

              {/* Password Field */}
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">
                  <Lock size={14} style={{ display: 'inline', marginRight: '4px' }} />
                  Contraseña
                </label>
                <input 
                  type="password"
                  className={`form-input ${passwordIsEmpty ? 'input-error' : ''}`}
                  value={password}
                  onChange={(e) => {
                    setPassword(e.target.value);
                    if (errorMessage) setErrorMessage('');
                  }}
                  onBlur={() => setTouched({ ...touched, password: true })}
                  placeholder="••••••••••••"
                />
                {passwordIsEmpty && (
                  <span className="error-text">La contraseña no puede estar en blanco</span>
                )}
              </div>

              {/* Role Selection */}
              <div className="form-group" style={{ marginBottom: 0 }}>
                <label className="form-label">Rol de Usuario</label>
                <select 
                  className="form-select"
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
                >
                  <option value="MEDICO">Médico Rural (Atenciones & Fichas)</option>
                  <option value="PACIENTE">Paciente Rural (Consultas & Citas)</option>
                  <option value="ADMIN">Administrador de Salud (Gestión & Reportes)</option>
                </select>
              </div>

              {/* Submit Button */}
              <button 
                type="submit"
                className="btn btn-primary btn-login-main"
                style={{ marginTop: '8px' }}
              >
                Ingresar a la Plataforma
                <ArrowRight size={18} />
              </button>
            </form>

            <div style={{ marginTop: '20px', paddingTop: '16px', borderTop: '1px solid var(--border-color)' }}>
              <button 
                className="btn btn-secondary btn-sm"
                style={{ width: '100%' }}
                onClick={onLogin}
                disabled={!authReady}
              >
                <ShieldCheck size={16} />
                O Iniciar Sesión con Microsoft Entra ID (SSO)
              </button>
            </div>
          </div>

          {/* Card 2: Quick Demo Roles Access */}
          <div className="glass-card login-card">
            <h2 className="card-title">
              <UserCheck size={24} style={{ color: 'var(--accent-emerald)' }} />
              Acceso Rápido por Rol
            </h2>
            <p className="card-desc">
              Seleccione un perfil para pre-cargar usuario y contraseña para pruebas inmediatas.
            </p>

            {/* Demo Roles selection */}
            <div className="demo-roles-container" style={{ marginTop: '16px' }}>
              {demoRoles.map((item) => (
                <div 
                  key={item.role}
                  className="demo-role-card"
                  onClick={() => handleDemoSelect(item.email, item.role)}
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

            <div style={{ marginTop: 'auto', paddingTop: '16px', borderTop: '1px dashed var(--border-color)', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.78rem', color: 'var(--text-muted)' }}>
              <CheckCircle2 size={15} style={{ color: 'var(--accent-emerald)', flexShrink: 0 }} />
              <span>Redirección automática al Inicio del Usuario Conectado tras ingresar.</span>
            </div>
          </div>

        </div>

        {/* Microservices Footer Preview */}
        <div className="login-services-preview">
          <h3 className="preview-title">Arquitectura Cloud Native • Microservicios Habilitados</h3>
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
