import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import ServicesHealthMonitor from './components/ServicesHealthMonitor';
import JwtManager from './components/JwtManager';
import CitasManager from './components/CitasManager';
import ConsultasManager from './components/ConsultasManager';
import FichasManager from './components/FichasManager';
import UsuariosManager from './components/UsuariosManager';
import NotificacionesManager from './components/NotificacionesManager';
import ClinicasManager from './components/ClinicasManager';
import ReportesDashboard from './components/ReportesDashboard';
import LoginPage from './components/LoginPage';
import { getAuthToken, authApi } from './services/api';
import { initializeAuth, login, logout } from './services/auth';

export default function App() {
  const [activeTab, setActiveTab] = useState('health');
  const [bffStatus, setBffStatus] = useState('offline');
  const [activeToken, setActiveToken] = useState(getAuthToken());
  const [authReady, setAuthReady] = useState(false);

  useEffect(() => {
    initializeAuth()
      .then((token) => {
        if (token) {
          localStorage.setItem('bff_jwt_token', token);
          setActiveToken(token);
        }
      })
      .finally(() => setAuthReady(true));
  }, []);

  const handleLogin = async () => {
    try {
      const token = await login();
      if (token) {
        localStorage.setItem('bff_jwt_token', token);
        setActiveToken(token);
      }
    } catch (err) {
      console.error('Error al iniciar sesión MSAL:', err);
    }
  };

  const handleDemoLogin = (email, role) => {
    const header = btoa(JSON.stringify({ alg: 'HS256', typ: 'JWT' }));
    const payload = btoa(JSON.stringify({
      sub: email,
      preferred_username: email,
      name: email.split('@')[0].replace('.', ' '),
      roles: [role],
      iss: 'https://login.microsoftonline.com/telemedicina-rural',
      aud: 'api://telemedicina-bff',
      exp: Math.floor(Date.now() / 1000) + 86400
    }));
    const signature = 'demo_signature_' + Date.now();
    const demoToken = `${header}.${payload}.${signature}`;

    localStorage.setItem('bff_jwt_token', demoToken);
    setActiveToken(demoToken);
  };

  const handleLogout = async () => {
    try {
      await logout();
    } catch (e) {
      // Ignore MSAL logout error if logged in via demo
    }
    localStorage.removeItem('bff_jwt_token');
    setActiveToken('');
  };

  // Ping BFF gateway health
  const checkBffHealth = async () => {
    const res = await authApi.status();
    // If BFF responds (even with valid=false for dummy token), it's online
    if (res.status !== 503 && res.status !== 0) {
      setBffStatus('online');
    } else {
      setBffStatus('offline');
    }
  };

  useEffect(() => {
    checkBffHealth();
    const interval = setInterval(checkBffHealth, 15000);
    return () => clearInterval(interval);
  }, []);

  const isAuthenticated = Boolean(activeToken);

  if (!isAuthenticated) {
    return (
      <LoginPage 
        onLogin={handleLogin} 
        onDemoLogin={handleDemoLogin} 
        bffStatus={bffStatus} 
        authReady={authReady} 
      />
    );
  }

  return (
    <div className="app-container">
      <Navbar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab} 
        bffStatus={bffStatus} 
        activeToken={activeToken} 
        authReady={authReady}
        onLogin={handleLogin}
        onLogout={handleLogout}
      />

      <main className="main-content">
        {activeTab === 'health' && <ServicesHealthMonitor />}
        {activeTab === 'jwt' && <JwtManager activeToken={activeToken} setActiveToken={setActiveToken} />}
        {activeTab === 'citas' && <CitasManager />}
        {activeTab === 'consultas' && <ConsultasManager />}
        {activeTab === 'fichas' && <FichasManager />}
        {activeTab === 'usuarios' && <UsuariosManager />}
        {activeTab === 'notificaciones' && <NotificacionesManager />}
        {activeTab === 'clinicas' && <ClinicasManager />}
        {activeTab === 'reportes' && <ReportesDashboard />}
      </main>

      <footer style={{
        textAlign: 'center',
        padding: '20px',
        borderTop: '1px solid var(--border-color)',
        color: 'var(--text-muted)',
        fontSize: '0.8rem',
        background: 'var(--bg-secondary)'
      }}>
        Plataforma de Telemedicina Rural • DSY1107 Desarrollo Cloud Native I • 8 Microservicios + BFF Gateway
      </footer>
    </div>
  );
}

