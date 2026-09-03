import React, { useState, useEffect } from 'react';
import { usuariosApi } from '../services/api';
import { Users, UserPlus, Stethoscope, Mail, Phone, MapPin, RefreshCw } from 'lucide-react';

export default function UsuariosManager() {
  const [subTab, setSubTab] = useState('pacientes'); // 'pacientes' | 'medicos'
  const [pacientes, setPacientes] = useState([]);
  const [medicos, setMedicos] = useState([]);
  const [loading, setLoading] = useState(false);
  const [filtroEspecialidad, setFiltroEspecialidad] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Form paciente
  const [pacRut, setPacRut] = useState('');
  const [pacNombre, setPacNombre] = useState('');
  const [pacEmail, setPacEmail] = useState('');
  const [pacTelefono, setPacTelefono] = useState('');
  const [pacComuna, setPacComuna] = useState('Petorca');

  // Form medico
  const [medRut, setMedRut] = useState('');
  const [medNombre, setMedNombre] = useState('');
  const [medEmail, setMedEmail] = useState('');
  const [medEspecialidad, setMedEspecialidad] = useState('Medicina General');

  const loadData = async () => {
    setLoading(true);
    if (subTab === 'pacientes') {
      const res = await usuariosApi.getPacientes();
      if (!res.error) {
        setPacientes(res.data || []);
      } else {
        setPacientes([
          { id: 1, rut: '12.345.678-9', nombreCompleto: 'Juan Pérez Morales', email: 'juan.perez@gmail.com', telefono: '+56912345678', comuna: 'Petorca' },
          { id: 2, rut: '11.222.333-4', nombreCompleto: 'María Soto Silva', email: 'maria.soto@gmail.com', telefono: '+56987654321', comuna: 'Putaendo' }
        ]);
      }
    } else {
      let res;
      if (filtroEspecialidad) {
        res = await usuariosApi.getMedicosByEspecialidad(filtroEspecialidad);
      } else {
        res = await usuariosApi.getMedicos();
      }
      if (!res.error) {
        setMedicos(res.data || []);
      } else {
        setMedicos([
          { id: 1, rut: '98.765.432-1', nombreCompleto: 'Dr. Alejandro Silva', email: 'a.silva@telemedicina.cl', especialidad: 'Medicina General', disponible: true },
          { id: 2, rut: '87.654.321-0', nombreCompleto: 'Dra. María González', email: 'm.gonzalez@telemedicina.cl', especialidad: 'Cardiología', disponible: true }
        ]);
      }
    }
    setLoading(false);
  };

  useEffect(() => {
    loadData();
  }, [subTab, filtroEspecialidad]);

  const handleCrearPaciente = async (e) => {
    e.preventDefault();
    setLoading(true);
    const res = await usuariosApi.createPaciente({ rut: pacRut, nombreCompleto: pacNombre, email: pacEmail, telefono: pacTelefono, comuna: pacComuna });
    setLoading(false);
    if (!res.error) {
      setShowCreateModal(false);
      loadData();
    } else {
      alert(`Error: ${res.message}`);
    }
  };

  const handleCrearMedico = async (e) => {
    e.preventDefault();
    setLoading(true);
    const res = await usuariosApi.createMedico({ rut: medRut, nombreCompleto: medNombre, email: medEmail, especialidad: medEspecialidad, disponible: true });
    setLoading(false);
    if (!res.error) {
      setShowCreateModal(false);
      loadData();
    } else {
      alert(`Error: ${res.message}`);
    }
  };

  return (
    <div>
      <div className="section-header">
        <div>
          <h2 className="section-title">
            <Users size={28} style={{ color: 'var(--accent-teal)' }} />
            Maestro de Usuarios (Pacientes & Médicos Voluntarios)
          </h2>
          <p className="section-subtitle">
            Microservicio <code>usuarios-service</code> (Puerto 8086). Fuente única de verdad para la autenticación y catálogos de atención.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '10px' }}>
          <button className="btn btn-secondary" onClick={loadData} disabled={loading}>
            <RefreshCw size={16} className={loading ? 'pulsing' : ''} />
            Actualizar
          </button>
          <button className="btn btn-primary" onClick={() => setShowCreateModal(true)}>
            <UserPlus size={16} />
            {subTab === 'pacientes' ? 'Registrar Nuevo Paciente' : 'Registrar Nuevo Médico'}
          </button>
        </div>
      </div>

      {/* Sub tabs Pacientes / Médicos */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
        <button
          className={`btn ${subTab === 'pacientes' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setSubTab('pacientes')}
        >
          <Users size={16} /> Catálogo de Pacientes
        </button>
        <button
          className={`btn ${subTab === 'medicos' ? 'btn-primary' : 'btn-secondary'}`}
          onClick={() => setSubTab('medicos')}
        >
          <Stethoscope size={16} /> Médicos Voluntarios
        </button>
      </div>

      {subTab === 'medicos' && (
        <div className="glass-card" style={{ marginBottom: '20px', padding: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <label className="form-label" style={{ margin: 0 }}>Filtrar por Especialidad:</label>
            <select
              className="form-select"
              style={{ maxWidth: '240px' }}
              value={filtroEspecialidad}
              onChange={(e) => setFiltroEspecialidad(e.target.value)}
            >
              <option value="">Todas las Especialidades</option>
              <option value="Medicina General">Medicina General</option>
              <option value="Pediatría">Pediatría</option>
              <option value="Cardiología">Cardiología</option>
              <option value="Dermatología">Dermatología</option>
            </select>
          </div>
        </div>
      )}

      {/* Vista Pacientes */}
      {subTab === 'pacientes' ? (
        <div className="glass-card">
          <div className="table-responsive">
            <table className="custom-table">
              <thead>
                <tr>
                  <th>RUT</th>
                  <th>Nombre Completo</th>
                  <th>Correo Electrónico</th>
                  <th>Teléfono</th>
                  <th>Comuna / Localidad</th>
                </tr>
              </thead>
              <tbody>
                {pacientes.map((p) => (
                  <tr key={p.rut || p.id}>
                    <td><code>{p.rut}</code></td>
                    <td><strong>{p.nombreCompleto}</strong></td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Mail size={14} color="var(--text-secondary)" />
                        {p.email}
                      </div>
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <Phone size={14} color="var(--text-secondary)" />
                        {p.telefono}
                      </div>
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <MapPin size={14} color="var(--accent-teal)" />
                        {p.comuna || 'Petorca'}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        /* Vista Médicos */
        <div className="grid-3">
          {medicos.map((m) => (
            <div key={m.rut || m.id} className="glass-card" style={{ borderTop: '4px solid var(--accent-indigo)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                <h4 style={{ fontSize: '1.1rem' }}>{m.nombreCompleto}</h4>
                <span className="badge badge-success">Disponible</span>
              </div>
              <p style={{ fontSize: '0.85rem', color: 'var(--accent-teal)', marginBottom: '8px', fontWeight: 600 }}>
                Especialidad: {m.especialidad}
              </p>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '4px' }}>
                RUT: <code>{m.rut}</code>
              </p>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                Email: {m.email}
              </p>
            </div>
          ))}
        </div>
      )}

      {/* Modal Crear Paciente/Médico */}
      {showCreateModal && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(5px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000
        }}>
          <div className="glass-card" style={{ maxWidth: '500px', width: '100%', margin: '20px' }}>
            <h3 style={{ marginBottom: '16px' }}>
              {subTab === 'pacientes' ? 'Registrar Nuevo Paciente' : 'Registrar Nuevo Médico Voluntario'}
            </h3>

            {subTab === 'pacientes' ? (
              <form onSubmit={handleCrearPaciente}>
                <div className="form-group">
                  <label className="form-label">RUT Paciente</label>
                  <input className="form-input" value={pacRut} onChange={(e) => setPacRut(e.target.value)} required placeholder="12.345.678-9" />
                </div>
                <div className="form-group">
                  <label className="form-label">Nombre Completo</label>
                  <input className="form-input" value={pacNombre} onChange={(e) => setPacNombre(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Correo Electrónico</label>
                  <input type="email" className="form-input" value={pacEmail} onChange={(e) => setPacEmail(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Teléfono de Contacto</label>
                  <input className="form-input" value={pacTelefono} onChange={(e) => setPacTelefono(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Comuna / Localidad Rural</label>
                  <input className="form-input" value={pacComuna} onChange={(e) => setPacComuna(e.target.value)} required />
                </div>
                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
                  <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>Cancelar</button>
                  <button type="submit" className="btn btn-primary">Guardar Paciente</button>
                </div>
              </form>
            ) : (
              <form onSubmit={handleCrearMedico}>
                <div className="form-group">
                  <label className="form-label">RUT Médico</label>
                  <input className="form-input" value={medRut} onChange={(e) => setMedRut(e.target.value)} required placeholder="98.765.432-1" />
                </div>
                <div className="form-group">
                  <label className="form-label">Nombre Completo</label>
                  <input className="form-input" value={medNombre} onChange={(e) => setMedNombre(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Correo Electrónico</label>
                  <input type="email" className="form-input" value={medEmail} onChange={(e) => setMedEmail(e.target.value)} required />
                </div>
                <div className="form-group">
                  <label className="form-label">Especialidad</label>
                  <select className="form-select" value={medEspecialidad} onChange={(e) => setMedEspecialidad(e.target.value)}>
                    <option value="Medicina General">Medicina General</option>
                    <option value="Pediatría">Pediatría</option>
                    <option value="Cardiología">Cardiología</option>
                    <option value="Dermatología">Dermatología</option>
                  </select>
                </div>
                <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px', marginTop: '20px' }}>
                  <button type="button" className="btn btn-secondary" onClick={() => setShowCreateModal(false)}>Cancelar</button>
                  <button type="submit" className="btn btn-primary">Guardar Médico</button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
