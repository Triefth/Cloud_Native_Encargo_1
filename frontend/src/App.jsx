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
import Login from './components/Login';
import { getAuthToken, setAuthToken, authApi } from './services/api';

export default function App() {
  const [activeTab, setActiveTab] = useState('health');
  const [bffStatus, setBffStatus] = useState('offline');
  const [activeToken, setActiveToken] = useState(getAuthToken());

  // Ping BFF gateway health
  const checkBffHealth = async () => {
    const res = await authApi.validateToken('ping');
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

  if (!activeToken) {
    return <Login onLogin={setActiveToken} />;
  }

  return (
    <div className="app-container">
      <Navbar 
        activeTab={activeTab} 
        setActiveTab={setActiveTab} 
        bffStatus={bffStatus} 
        activeToken={activeToken} 
          onLogout={() => {
            setAuthToken('');
            setActiveToken('');
          }} 
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
