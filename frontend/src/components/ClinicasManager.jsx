import React, { useState, useEffect } from 'react';
import { clinicasApi } from '../services/api';
import { Building2, Settings, Plus, Globe, Key, ShieldAlert, RefreshCw } from 'lucide-react';

export default function ClinicasManager() {
  const [clinicas, setClinicas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [showConfigModal, setShowConfigModal] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Form crear clínica
  const [nombre, setNombre] = useState('Posta Rural Alicahue');
  const [comuna, setComuna] = useState('Cabildo');
  const [region, setRegion] = useState('Valparaíso');

  // Form EHR config
  const [apiUrl, setApiUrl] = useState('https://ehr.posta-alicahue.cl/api/v1');
  const [apiKey, setApiKey] = useState('EHR_SECRET_KEY_88392');

  const loadClinicas = async () => {
    setLoading(true);
    const res = await clinicasApi.getAll();
    setLoading(false);

    if (!res.error) {
      setClinicas(res.data || []);
    } else {
      setClinicas([
        { id: 1, nombre: 'Posta Rural Petorca Centro', comuna: 'Petorca', region: 'Valparaíso', configuracionEhr: { apiUrl: 'https://ehr.petorca.cl/api', apiKey: 'sec_84920' } },
        { id: 2, nombre: 'Centro de Salud Familiar Putaendo Rural', comuna: 'Putaendo', region: 'Valparaíso', configuracionEhr: { apiUrl: 'https://cesfam.putaendo.cl/ehr', apiKey: 'sec_11029' } }
      ]);
    }
  };

  useEffect(() => {
    loadClinicas();
  }, []);

  const handleCrearClinica = async (e) => {
    e.preventDefault();
    setLoading(true);
    const res = await clinicasApi.create({ nombre, comuna, region });
    setLoading(false);
    if (!res.error) {
      setShowCreateModal(false);
      loadClinicas();
    } else {
      alert(`Error: ${res.message}`);
    }
  };

  const handleGuardarEhr = async (e) => {
    e.preventDefault();
    if (!showConfigModal) return;
    setLoading(true);
    const res = await clinicasApi.updateEhrConfig(showConfigModal.id, { apiUrl, apiKey });
    setLoading(false);
    if (!res.error) {
      setShowConfigModal(null);
      loadClinicas();
    } else {
      alert(`Error: ${res.message}`);
    }
  };

  return (
    <div>
      <div className="section-header">
        <div>
          <h2 className="section-title">
            <Building2 size={28} style={{ color: 'var(--accent-teal)' }} />
            Gestor de Clínicas Rurales & Integración EHR
          </h2>
          <p className="section-subtitle">
            Microservicio <code>clinicas-service</code> (Puerto 8087). Catálogo de centros de salud afiliados y configuración de credenciales API para software de ficha clínica externo.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={loadClinicas} disabled={loading}>
            <RefreshCw size={16} className={loading ? 'pulsing' : ''} />
            Actualizar
          </button>
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
            <Plus size={16} /> Agregar Nueva Clínica
          </button>
        </div>
      </div>

      <div className="grid-3">
        {clinicas.map((c) => (
          <div key={c.id} className="glass-card" style={{ borderTop: '4px solid var(--accent-teal)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
              <h3 style={{ fontSize: '1.15rem' }}>{c.nombre}</h3>
              <span className="badge badge-info">{c.comuna}</span>
            </div>

            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '16px' }}>
              Región: {c.region}
            </p>

            <div style={{ background: 'rgba(0,0,0,0.2)', padding: '12px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.8rem', color: 'var(--accent-teal)', marginBottom: '4px' }}>
                <Globe size={14} /> Endpoint Ficha Médica:
              </div>
              <p style={{ fontSize: '0.75rem', fontFamily: 'monospace', wordBreak: 'break-all', color: 'var(--text-primary)' }}>
                {c.configuracionEhr?.apiUrl || 'No configurado'}
              </p>
            </div>

            <button 
              className="btn btn-secondary btn-sm" 
              style={{ width: '100%' }}
              onClick={() => {
                setShowConfigModal(c);
                setApiUrl(c.configuracionEhr?.apiUrl || '');
                setApiKey(c.configuracionEhr?.apiKey || '');
              }}
            >
              <Settings size={14} /> Configurar Credenciales API EHR
            </button>
          </div>
        ))}
      </div>

      {/* Modal Crear Clínica */}
      {showCreateModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(5px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div className="glass-card" style={{ maxWidth: '450px', width: '100%', margin: '20px' }}>
            <h3 style={{ marginBottom: '16px' }}>Registrar Nueva Clínica Rural</h3>
            <form onSubmit={handleCrearClinica}>
              <div className="form-group">
                <label className="form-label">Nombre del Centro de Salud</label>
                <input className="form-input" value={nombre} onChange={(e) => setNombre(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Comuna</label>
                <input className="form-input" value={comuna} onChange={(e) => setComuna(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Región</label>
                <input className="form-input" value={region} onChange={(e) => setRegion(e.target.value)} required />
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>Cancelar</button>
                <button type="submit" className="btn btn-primary">Guardar Clínica</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal EHR Config */}
      {showConfigModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(5px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div className="glass-card" style={{ maxWidth: '500px', width: '100%', margin: '20px' }}>
            <h3 style={{ marginBottom: '16px' }}>Configuración EHR - {showConfigModal.nombre}</h3>
            <form onSubmit={handleGuardarEhr}>
              <div className="form-group">
                <label className="form-label">URL Endpoint API Externa</label>
                <input className="form-input" value={apiUrl} onChange={(e) => setApiUrl(e.target.value)} required placeholder="https://api.clinica.cl/v1" />
              </div>
              <div className="form-group">
                <label className="form-label">API Key / Token Secreto</label>
                <input type="password" className="form-input" value={apiKey} onChange={(e) => setApiKey(e.target.value)} required />
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowConfigModal(null)}>Cancelar</button>
                <button type="submit" className="btn btn-primary">Guardar Credenciales</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
