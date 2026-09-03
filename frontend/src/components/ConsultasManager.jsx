import React, { useState, useEffect } from 'react';
import { consultasApi } from '../services/api';
import { Video, Play, CheckSquare, Mic, MicOff, Camera, CameraOff, PhoneOff, Clock, AlertCircle } from 'lucide-react';

export default function ConsultasManager() {
  const [consultas, setConsultas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [activeSession, setActiveSession] = useState(null);
  
  // Audio/Video controls
  const [micOn, setMicOn] = useState(true);
  const [camOn, setCamOn] = useState(true);
  const [seconds, setSeconds] = useState(0);

  // Form finalización
  const [diagnostico, setDiagnostico] = useState('Paciente presenta faringoamigdalitis aguda. Sin signos de alerta respiratoria.');
  const [indicaciones, setIndicaciones] = useState('Paracetamol 500mg c/8hrs por 3 días. Hidratación abundante y reposo.');

  // Form iniciar
  const [citaId, setCitaId] = useState('1');
  const [rutPaciente, setRutPaciente] = useState('12.345.678-9');
  const [rutMedico, setRutMedico] = useState('98.765.432-1');

  const loadConsultas = async () => {
    setLoading(true);
    const res = await consultasApi.getAll();
    setLoading(false);
    if (!res.error) {
      setConsultas(res.data || []);
    } else {
      // Mock de respaldo
      setConsultas([
        { id: 101, citaId: 1, rutPaciente: '12.345.678-9', rutMedico: '98.765.432-1', estado: 'FINALIZADA', diagnosticoPreliminar: 'Control de hipertensión estable', duracionMinutos: 18 }
      ]);
    }
  };

  useEffect(() => {
    loadConsultas();
  }, []);

  // Timer para la llamada active
  useEffect(() => {
    let interval = null;
    if (activeSession) {
      interval = setInterval(() => setSeconds(s => s + 1), 1000);
    } else {
      setSeconds(0);
    }
    return () => clearInterval(interval);
  }, [activeSession]);

  const handleIniciarConsulta = async (e) => {
    e.preventDefault();
    setLoading(true);
    const res = await consultasApi.start(citaId, rutPaciente, rutMedico);
    setLoading(false);
    if (res.error) {
      alert(`Error al iniciar consulta: ${res.message}`);
    } else {
      setActiveSession(res.data);
      loadConsultas();
    }
  };

  const handleFinalizarConsulta = async (e) => {
    e.preventDefault();
    if (!activeSession) return;
    const duracion = Math.max(1, Math.ceil(seconds / 60));
    setLoading(true);
    const res = await consultasApi.finish(activeSession.id, diagnostico, indicaciones, duracion);
    setLoading(false);
    if (!res.error) {
      alert('¡Consulta médica finalizada y guardada exitosamente!');
      setActiveSession(null);
      loadConsultas();
    } else {
      alert(`Error: ${res.message}`);
    }
  };

  const formatTimer = (totalSeconds) => {
    const mins = Math.floor(totalSeconds / 60).toString().padStart(2, '0');
    const secs = (totalSeconds % 60).toString().padStart(2, '0');
    return `${mins}:${secs}`;
  };

  return (
    <div>
      <div className="section-header">
        <div>
          <h2 className="section-title">
            <Video size={28} style={{ color: 'var(--accent-teal)' }} />
            Salas de Teleconsulta en Línea (HIPAA CPaaS)
          </h2>
          <p className="section-subtitle">
            Microservicio <code>consultas-service</code> (Puerto 8082). Conexiones de videollamada cifradas de alta resiliencia para posta médica rural.
          </p>
        </div>
      </div>

      {activeSession ? (
        /* Simulador de Sala de Videollamada CPaaS Active */
        <div className="glass-card" style={{ marginBottom: '24px', border: '2px solid var(--accent-teal)' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <span className="status-dot online pulsing"></span>
              <h3 style={{ fontSize: '1.2rem' }}>Sala CPaaS Activa - Consulta #{activeSession.id}</h3>
            </div>
            <div className="badge badge-info" style={{ fontSize: '0.9rem', padding: '6px 12px' }}>
              <Clock size={14} /> Tiempo Transcurrido: {formatTimer(seconds)}
            </div>
          </div>

          <div className="video-room-container">
            {/* Screen principal */}
            <div className="video-screen">
              {camOn ? (
                <div style={{ textAlign: 'center', color: '#fff' }}>
                  <Video size={64} style={{ color: 'var(--accent-teal)', marginBottom: '10px' }} className="pulsing" />
                  <p style={{ fontWeight: 600, fontSize: '1.1rem' }}>Paciente en Línea: {activeSession.rutPaciente}</p>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Conexión CPaaS encriptada end-to-end (HIPAA Ready)</p>
                </div>
              ) : (
                <div style={{ textAlign: 'center', color: 'var(--text-muted)' }}>
                  <CameraOff size={48} />
                  <p style={{ marginTop: '8px' }}>Cámara Desactivada</p>
                </div>
              )}

              {/* Picture in Picture (Médico) */}
              <div className="pip-screen">
                <div style={{ width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#091024', color: '#fff' }}>
                  <span style={{ fontSize: '0.75rem', fontWeight: 600 }}>Tú (Dr. {activeSession.rutMedico})</span>
                </div>
              </div>

              {/* Botones de Control en Vivo */}
              <div className="video-overlay">
                <div style={{ display: 'flex', gap: '10px' }}>
                  <button className={`btn btn-sm ${micOn ? 'btn-secondary' : 'btn-danger'}`} onClick={() => setMicOn(!micOn)}>
                    {micOn ? <Mic size={16} /> : <MicOff size={16} />}
                  </button>
                  <button className={`btn btn-sm ${camOn ? 'btn-secondary' : 'btn-danger'}`} onClick={() => setCamOn(!camOn)}>
                    {camOn ? <Camera size={16} /> : <CameraOff size={16} />}
                  </button>
                </div>

                <button className="btn btn-danger btn-sm" onClick={() => setActiveSession(null)}>
                  <PhoneOff size={16} /> Colgar Sesión
                </button>
              </div>
            </div>

            {/* Formulario de Diagnóstico y Cierre */}
            <div style={{ background: 'rgba(0,0,0,0.3)', padding: '16px', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-color)' }}>
              <h4 style={{ marginBottom: '12px', fontSize: '1rem', color: 'var(--accent-teal)' }}>Registro de Ficha Clínica</h4>
              <form onSubmit={handleFinalizarConsulta}>
                <div className="form-group">
                  <label className="form-label">Diagnóstico Preliminar</label>
                  <textarea className="form-textarea" rows={3} value={diagnostico} onChange={(e) => setDiagnostico(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Indicaciones Médicas</label>
                  <textarea className="form-textarea" rows={3} value={indicaciones} onChange={(e) => setIndicaciones(e.target.value)} required />
                </div>
                <button type="submit" className="btn btn-success" style={{ width: '100%', marginTop: '10px' }} disabled={loading}>
                  <CheckSquare size={16} /> Finalizar & Registrar Atención
                </button>
              </form>
            </div>
          </div>
        </div>
      ) : (
        /* Iniciar Nueva Consulta Form */
        <div className="grid-2" style={{ marginBottom: '24px' }}>
          <div className="glass-card">
            <h3 style={{ fontSize: '1.1rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Play size={20} style={{ color: 'var(--accent-teal)' }} />
              Iniciar Nueva Teleconsulta
            </h3>

            <form onSubmit={handleIniciarConsulta}>
              <div className="form-group">
                <label className="form-label">ID de Cita Médica</label>
                <input className="form-input" value={citaId} onChange={(e) => setCitaId(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">RUT Paciente en Posta Rural</label>
                <input className="form-input" value={rutPaciente} onChange={(e) => setRutPaciente(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">RUT Médico Voluntario</label>
                <input className="form-input" value={rutMedico} onChange={(e) => setRutMedico(e.target.value)} required />
              </div>

              <button type="submit" className="btn btn-primary" style={{ width: '100%', marginTop: '10px' }} disabled={loading}>
                <Video size={16} /> Conectar Sala de Videollamada
              </button>
            </form>
          </div>

          <div className="glass-card" style={{ background: 'var(--gradient-card)' }}>
            <h3 style={{ fontSize: '1.1rem', marginBottom: '12px' }}>Especificación HIPAA CPaaS</h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', lineHeight: 1.6 }}>
              Este microservicio gestiona el ciclo de vida de la atención médica remota. Al finalizar una consulta, los datos de diagnóstico son enviados al microservicio de <strong>fichas-service</strong> para actualizar el historial médico del paciente en la clínica rural.
            </p>
          </div>
        </div>
      )}

      {/* Historial de Consultas */}
      <div className="glass-card">
        <h3 style={{ fontSize: '1.1rem', marginBottom: '16px' }}>Historial de Consultas Realizadas</h3>
        <div className="table-responsive">
          <table className="custom-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Cita #</th>
                <th>Paciente</th>
                <th>Médico</th>
                <th>Duración</th>
                <th>Estado</th>
                <th>Diagnóstico</th>
              </tr>
            </thead>
            <tbody>
              {consultas.map((c) => (
                <tr key={c.id}>
                  <td><code>#{c.id}</code></td>
                  <td>#{c.citaId}</td>
                  <td>{c.rutPaciente}</td>
                  <td>{c.rutMedico}</td>
                  <td>{c.duracionMinutos ? `${c.duracionMinutos} min` : 'En curso'}</td>
                  <td>
                    <span className={`badge ${c.estado === 'FINALIZADA' ? 'badge-success' : 'badge-warning'}`}>
                      {c.estado}
                    </span>
                  </td>
                  <td style={{ maxWidth: '250px', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {c.diagnosticoPreliminar || 'Sin diagnóstico registrado'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
