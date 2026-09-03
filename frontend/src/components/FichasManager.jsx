import React, { useState, useEffect } from 'react';
import { fichasApi } from '../services/api';
import { FileText, Search, Plus, UserCheck, AlertTriangle, ShieldCheck } from 'lucide-react';

export default function FichasManager() {
  const [rutBusqueda, setRutBusqueda] = useState('12.345.678-9');
  const [fichaActiva, setFichaActiva] = useState(null);
  const [loading, setLoading] = useState(false);
  const [searched, setSearched] = useState(false);
  const [showAddEntryModal, setShowAddEntryModal] = useState(false);

  // Form agregar atención
  const [nombreMedico, setNombreMedico] = useState('Dr. Alejandro Silva');
  const [especialidad, setEspecialidad] = useState('Medicina General');
  const [resumenAtencion, setResumenAtencion] = useState('Paciente controlado por videollamada. Presión arterial dentro de rango normal.');

  const handleBuscar = async (e) => {
    if (e) e.preventDefault();
    if (!rutBusqueda) return;
    setLoading(true);
    setSearched(true);
    const res = await fichasApi.getByRut(rutBusqueda);
    setLoading(false);

    if (!res.error && res.data) {
      setFichaActiva(res.data);
    } else {
      // Mock de demostración para el prototipo
      setFichaActiva({
        rutPaciente: rutBusqueda,
        nombreCompleto: 'Juan Pérez Morales',
        grupoSanguineo: 'O+',
        alergias: 'Penicilina, Polvos silvestres',
        enfermedadesCronicas: 'Hipertensión Arterial Grado 1',
        registrosAtencion: [
          { id: 1, fecha: '2026-08-20', nombreMedico: 'Dra. María González', especialidad: 'Cardiología', resumenAtencion: 'Evaluación inicial electrocardiograma diferido.' },
          { id: 2, fecha: '2026-09-01', nombreMedico: 'Dr. Alejandro Silva', especialidad: 'Medicina General', resumenAtencion: 'Ajuste de dosis Losartán 50mg diario.' }
        ]
      });
    }
  };

  useEffect(() => {
    handleBuscar();
  }, []);

  const handleAgregarAtencion = async (e) => {
    e.preventDefault();
    if (!rutBusqueda) return;
    setLoading(true);
    const res = await fichasApi.addAtencion(rutBusqueda, null, nombreMedico, especialidad, resumenAtencion);
    setLoading(false);
    setShowAddEntryModal(false);

    if (!res.error) {
      handleBuscar();
    } else {
      // Actualización reactiva local
      if (fichaActiva) {
        const nuevosRegistros = [
          ...(fichaActiva.registrosAtencion || []),
          { id: Date.now(), fecha: new Date().toISOString().split('T')[0], nombreMedico, especialidad, resumenAtencion }
        ];
        setFichaActiva({ ...fichaActiva, registrosAtencion: nuevosRegistros });
      }
    }
  };

  return (
    <div>
      <div className="section-header">
        <div>
          <h2 className="section-title">
            <FileText size={28} style={{ color: 'var(--accent-teal)' }} />
            Integración de Fichas Médicas (EHR Rural)
          </h2>
          <p className="section-subtitle">
            Microservicio <code>fichas-service</code> (Puerto 8083). Sincronización de fichas médicas e historial de atenciones remotas con el software de la clínica rural.
          </p>
        </div>
      </div>

      {/* Buscador de Ficha por RUT */}
      <div className="glass-card" style={{ marginBottom: '24px' }}>
        <form onSubmit={handleBuscar} style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, minWidth: '240px' }}>
            <label className="form-label">RUT Paciente a Consultar</label>
            <div style={{ position: 'relative' }}>
              <input
                className="form-input"
                style={{ paddingLeft: '38px' }}
                placeholder="ej: 12.345.678-9"
                value={rutBusqueda}
                onChange={(e) => setRutBusqueda(e.target.value)}
              />
              <Search size={18} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'flex-end', gap: '10px' }}>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Buscando...' : 'Consultar Ficha Clínica'}
            </button>
          </div>
        </form>
      </div>

      {/* Detalle de Ficha Médica */}
      {fichaActiva ? (
        <div>
          <div className="glass-card" style={{ marginBottom: '24px', borderTop: '4px solid var(--accent-teal)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '16px', marginBottom: '20px' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <UserCheck size={24} style={{ color: 'var(--accent-teal)' }} />
                  <h3 style={{ fontSize: '1.4rem' }}>{fichaActiva.nombreCompleto || 'Paciente Registrado'}</h3>
                </div>
                <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                  RUT: <strong>{fichaActiva.rutPaciente}</strong> • Grupo Sanguíneo: <strong style={{ color: 'var(--accent-rose)' }}>{fichaActiva.grupoSanguineo || 'O+'}</strong>
                </p>
              </div>

              <button className="btn btn-success" onClick={() => setShowAddEntryModal(true)}>
                <Plus size={16} /> Agregar Registro de Atención
              </button>
            </div>

            <div className="grid-2">
              <div style={{ background: 'rgba(0,0,0,0.2)', padding: '14px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}>
                <h4 style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', textTransform: 'uppercase', marginBottom: '6px' }}>
                  Alergias Identificadas
                </h4>
                <p style={{ fontSize: '0.95rem', color: 'var(--accent-rose)' }}>
                  {fichaActiva.alergias || 'Sin alergias conocidas'}
                </p>
              </div>

              <div style={{ background: 'rgba(0,0,0,0.2)', padding: '14px', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}>
                <h4 style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', textTransform: 'uppercase', marginBottom: '6px' }}>
                  Condiciones Crónicas
                </h4>
                <p style={{ fontSize: '0.95rem', color: 'var(--accent-amber)' }}>
                  {fichaActiva.enfermedadesCronicas || 'Sin enfermedades crónicas'}
                </p>
              </div>
            </div>
          </div>

          {/* Historial de Atenciones Remotas */}
          <div className="glass-card">
            <h3 style={{ fontSize: '1.1rem', marginBottom: '16px' }}>Historial de Atenciones de Telemedicina</h3>
            <div className="table-responsive">
              <table className="custom-table">
                <thead>
                  <tr>
                    <th>Fecha</th>
                    <th>Médico Tratante</th>
                    <th>Especialidad</th>
                    <th>Resumen de Atención / Evolución</th>
                  </tr>
                </thead>
                <tbody>
                  {(!fichaActiva.registrosAtencion || fichaActiva.registrosAtencion.length === 0) ? (
                    <tr>
                      <td colSpan={4} style={{ textAlign: 'center', padding: '20px', color: 'var(--text-muted)' }}>
                        No hay registros de atenciones anteriores en la ficha.
                      </td>
                    </tr>
                  ) : (
                    fichaActiva.registrosAtencion.map((reg, idx) => (
                      <tr key={reg.id || idx}>
                        <td><code>{reg.fecha || '2026-09-03'}</code></td>
                        <td><strong>{reg.nombreMedico}</strong></td>
                        <td><span className="badge badge-info">{reg.especialidad}</span></td>
                        <td>{reg.resumenAtencion}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      ) : searched && (
        <div className="glass-card" style={{ textAlign: 'center', padding: '40px' }}>
          <AlertTriangle size={36} color="var(--accent-amber)" style={{ marginBottom: '10px' }} />
          <p style={{ fontSize: '1rem' }}>No se encontró ficha clínica previa para el RUT <strong>{rutBusqueda}</strong>.</p>
        </div>
      )}

      {/* Modal Agregar Registro */}
      {showAddEntryModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(5px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div className="glass-card" style={{ maxWidth: '500px', width: '100%', margin: '20px' }}>
            <h3 style={{ marginBottom: '16px' }}>Registrar Atención Remota en Ficha</h3>
            <form onSubmit={handleAgregarAtencion}>
              <div className="form-group">
                <label className="form-label">Nombre del Médico</label>
                <input className="form-input" value={nombreMedico} onChange={(e) => setNombreMedico(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Especialidad</label>
                <input className="form-input" value={especialidad} onChange={(e) => setEspecialidad(e.target.value)} required />
              </div>
              <div className="form-group">
                <label className="form-label">Resumen de la Atención / Evolución</label>
                <textarea className="form-textarea" rows={3} value={resumenAtencion} onChange={(e) => setResumenAtencion(e.target.value)} required />
              </div>
              <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
                <button type="button" className="btn btn-secondary" onClick={() => setShowAddEntryModal(false)}>Cancelar</button>
                <button type="submit" className="btn btn-primary" disabled={loading}>Guardar en Ficha</button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
