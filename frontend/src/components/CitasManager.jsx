import React, { useState, useEffect } from 'react';
import { citasApi } from '../services/api';
import { Calendar, Plus, Check, X, Clock, User, AlertCircle, RefreshCw } from 'lucide-react';

export default function CitasManager() {
  const [citas, setCitas] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showRescheduleModal, setShowRescheduleModal] = useState(null);

  // Form state
  const [rutPaciente, setRutPaciente] = useState('12.345.678-9');
  const [rutMedico, setRutMedico] = useState('98.765.432-1');
  const [especialidad, setEspecialidad] = useState('Medicina General');
  const [fechaHora, setFechaHora] = useState('2026-09-10T10:00');
  const [motivo, setMotivo] = useState('Control de hipertensión en clínica rural');

  // Reschedule form
  const [nuevaFechaHora, setNuevaFechaHora] = useState('');

  const loadCitas = async () => {
    setLoading(true);
    setError('');
    const res = await citasApi.getAll();
    setLoading(false);

    if (res.error) {
      setError(res.message);
      // Mock de respaldo si el microservicio está apagado para demostración UI
      setCitas([
        { id: 1, rutPaciente: '12.345.678-9', rutMedico: '98.765.432-1', especialidad: 'Medicina General', fechaHora: '2026-09-05T10:30:00', estado: 'PROGRAMADA', motivoConsulta: 'Chequeo preventivo en posta rural' },
        { id: 2, rutPaciente: '11.222.333-4', rutMedico: '98.765.432-1', especialidad: 'Pediatría', fechaHora: '2026-09-06T12:00:00', estado: 'CONFIRMADA', motivoConsulta: 'Teleconsulta por resfrío común' }
      ]);
    } else {
      setCitas(res.data || []);
    }
  };

  useEffect(() => {
    loadCitas();
  }, []);

  const handleCrearCita = async (e) => {
    e.preventDefault();
    setLoading(true);
    const nueva = { rutPaciente, rutMedico, especialidad, fechaHora, motivoConsulta: motivo, estado: 'PROGRAMADA' };
    const res = await citasApi.create(nueva);
    setLoading(false);

    if (res.error) {
      alert(`Error al agendar cita: ${res.message}`);
    } else {
      setShowCreateModal(false);
      loadCitas();
    }
  };

  const handleConfirmar = async (id) => {
    const res = await citasApi.confirm(id);
    if (!res.error) loadCitas();
    else alert(`Error: ${res.message}`);
  };

  const handleCancelar = async (id) => {
    const res = await citasApi.cancel(id);
    if (!res.error) loadCitas();
    else alert(`Error: ${res.message}`);
  };

  const handleReprogramar = async (e) => {
    e.preventDefault();
    if (!showRescheduleModal || !nuevaFechaHora) return;
    const res = await citasApi.reschedule(showRescheduleModal.id, nuevaFechaHora);
    if (!res.error) {
      setShowRescheduleModal(null);
      loadCitas();
    } else {
      alert(`Error: ${res.message}`);
    }
  };

  return (
    <div>
      <div className="section-header">
        <div>
          <h2 className="section-title">
            <Calendar size={28} style={{ color: 'var(--accent-teal)' }} />
            Agenda de Citas Médicas Remotas
          </h2>
          <p className="section-subtitle">
            Microservicio <code>citas-service</code> (Puerto 8081). Gestión de agendamiento, confirmaciones y reprogramación.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={loadCitas} disabled={loading}>
            <RefreshCw size={16} className={loading ? 'pulsing' : ''} />
            Actualizar
          </button>
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
            <Plus size={16} />
            Agendar Nueva Cita
          </button>
        </div>
      </div>

      {error && (
        <div style={{ marginBottom: '20px', padding: '12px', background: 'rgba(244, 63, 94, 0.1)', border: '1px solid var(--accent-rose)', borderRadius: 'var(--radius-sm)', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <AlertCircle size={20} color="var(--accent-rose)" />
          <span style={{ fontSize: '0.9rem', color: 'var(--accent-rose)' }}>{error} (Mostrando datos locales de demostración)</span>
        </div>
      )}

      {/* Tabla de Citas */}
      <div className="glass-card">
        <div className="table-responsive">
          <table className="custom-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Paciente (RUT)</th>
                <th>Médico (RUT)</th>
                <th>Especialidad</th>
                <th>Fecha y Hora</th>
                <th>Estado</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {citas.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '24px', color: 'var(--text-muted)' }}>
                    No se encontraron citas agendadas.
                  </td>
                </tr>
              ) : (
                citas.map((c) => (
                  <tr key={c.id}>
                    <td><code>#{c.id}</code></td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <User size={14} style={{ color: 'var(--accent-teal)' }} />
                        {c.rutPaciente}
                      </div>
                    </td>
                    <td>{c.rutMedico}</td>
                    <td>{c.especialidad}</td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.85rem' }}>
                        <Clock size={14} style={{ color: 'var(--text-secondary)' }} />
                        {c.fechaHora?.replace('T', ' ')}
                      </div>
                    </td>
                    <td>
                      <span className={`badge ${
                        c.estado === 'CONFIRMADA' ? 'badge-success' :
                        c.estado === 'CANCELADA' ? 'badge-danger' : 'badge-warning'
                      }`}>
                        {c.estado}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', gap: '6px' }}>
                        {c.estado === 'PROGRAMADA' && (
                          <button className="btn btn-success btn-sm" onClick={() => handleConfirmar(c.id)} title="Confirmar Cita">
                            <Check size={14} /> Confirmar
                          </button>
                        )}
                        {c.estado !== 'CANCELADA' && (
                          <button className="btn btn-danger btn-sm" onClick={() => handleCancelar(c.id)} title="Cancelar Cita">
                            <X size={14} /> Cancelar
                          </button>
                        )}
                        <button className="btn btn-secondary btn-sm" onClick={() => { setShowRescheduleModal(c); setNuevaFechaHora(c.fechaHora); }}>
                          Reprogramar
                        </button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal Crear Cita */}
      {showCreateModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(5px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div className="glass-card" style={{ maxWidth: '500px', width: '100%', margin: '20px' }}>
            <h3 style={{ marginBottom: '16px' }}>Agendar Nueva Cita Telemedicina</h3>
            <form onSubmit={handleCrearCita}>
              <div className="form-group">
                <label className="form-label">RUT Paciente</label>
                <input className="form-input" value={rutPaciente} onChange={(e) => setRutPaciente(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">RUT Médico Voluntario</label>
                <input className="form-input" value={rutMedico} onChange={(e) => setRutMedico(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Especialidad</label>
                <select className="form-select" value={especialidad} onChange={(e) => setEspecialidad(e.target.value)}>
                  <option value="Medicina General">Medicina General</option>
                  <option value="Pediatría">Pediatría</option>
                  <option value="Dermatología">Dermatología</option>
                  <option value="Cardiología">Cardiología</option>
                </select>
              </div>
              <div className="form-group">
                <label className="form-label">Fecha y Hora</label>
                <input type="datetime-local" className="form-input" value={fechaHora} onChange={(e) => setFechaHora(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Motivo de Consulta</label>
                <textarea className="form-textarea" rows={2} value={motivo} onChange={(e) => setMotivo(e.target.value)} required />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>Cancelar</button>
                <button type="submit" className="btn btn-primary" disabled={loading}>Guardar Cita</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal Reprogramar */}
      {showRescheduleModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(5px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div className="glass-card" style={{ maxWidth: '400px', width: '100%', margin: '20px' }}>
            <h3 style={{ marginBottom: '16px' }}>Reprogramar Cita #{showRescheduleModal.id}</h3>
            <form onSubmit={handleReprogramar}>
              <div className="form-group">
                <label className="form-label">Nueva Fecha y Hora</label>
                <input type="datetime-local" className="form-input" value={nuevaFechaHora} onChange={(e) => setNuevaFechaHora(e.target.value)} required />
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowRescheduleModal(null)}>Cancelar</button>
                <button type="submit" className="btn btn-primary">Confirmar Cambio</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
