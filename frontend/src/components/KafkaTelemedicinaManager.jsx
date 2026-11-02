import React, { useState, useEffect } from 'react';
import {
  Activity,
  AlertTriangle,
  Calendar,
  RefreshCw,
  Send,
  Trash2,
  PlusCircle,
  Layers,
  Bell,
  ArrowRight,
  Server,
  Terminal,
  CheckCircle2,
  Stethoscope
} from 'lucide-react';
import { telemedicinaKafkaApi } from '../services/api';

const EVENT_TYPES = [
  { value: 'CITA_CREADA', label: 'Cita Creada', color: 'var(--accent-blue)' },
  { value: 'CITA_CONFIRMADA', label: 'Cita Confirmada', color: 'var(--accent-emerald)' },
  { value: 'CITA_REPROGRAMADA', label: 'Cita Reprogramada', color: 'var(--accent-amber)' },
  { value: 'CITA_CANCELADA', label: 'Cita Cancelada', color: 'var(--accent-rose)' },
];

export default function KafkaTelemedicinaManager() {
  const [activeTab, setActiveTab] = useState('flujo'); // 'flujo' | 'admin'
  const [clusterStatus, setClusterStatus] = useState('verificando');
  const [eventos, setEventos] = useState([]);
  const [alertas, setAlertas] = useState([]);
  const [response, setResponse] = useState(null);

  // Formulario de simulación
  const [eventType, setEventType] = useState('CITA_CREADA');
  const [rutPaciente, setRutPaciente] = useState('12.345.678-9');
  const [nombrePaciente, setNombrePaciente] = useState('María González');
  const [citaId, setCitaId] = useState(1001);
  const [motivo, setMotivo] = useState('Control de hipertensión');

  // Admin
  const [topics, setTopics] = useState([]);
  const [consumerGroups, setConsumerGroups] = useState([]);
  const [newTopic, setNewTopic] = useState({ name: '', partitions: 3, replicas: 1, retentionMs: '604800000', cleanupPolicy: 'delete' });

  const checkHealth = async () => {
    const res = await telemedicinaKafkaApi.health();
    setClusterStatus(res.error ? 'offline' : 'online');
  };

  const loadData = async () => {
    const [evRes, alRes] = await Promise.all([
      telemedicinaKafkaApi.getEventos(),
      telemedicinaKafkaApi.getAlertas(),
    ]);
    if (!evRes.error && Array.isArray(evRes.data)) setEventos(evRes.data);
    if (!alRes.error && Array.isArray(alRes.data)) setAlertas(alRes.data);
  };

  const loadAdmin = async () => {
    const [tRes, cgRes] = await Promise.all([
      telemedicinaKafkaApi.listTopics(),
      telemedicinaKafkaApi.listConsumerGroups(),
    ]);
    if (!tRes.error && tRes.data?.topics) setTopics(tRes.data.topics);
    if (!cgRes.error && cgRes.data?.consumerGroups) setConsumerGroups(cgRes.data.consumerGroups);
  };

  useEffect(() => {
    checkHealth();
    loadData();
    const interval = setInterval(() => {
      loadData();
      if (activeTab === 'admin') loadAdmin();
    }, 3500);
    return () => clearInterval(interval);
  }, [activeTab]);

  useEffect(() => {
    if (activeTab === 'admin') loadAdmin();
  }, [activeTab]);

  const handleSimular = async () => {
    setResponse(null);
    const evento = {
      eventType,
      citaId: Number(citaId),
      rutPaciente,
      nombrePaciente,
      rutMedico: '11.111.111-1',
      nombreMedico: 'Dr. Pérez',
      especialidad: 'Medicina General',
      estado: eventType.replace('CITA_', ''),
      fechaHora: '2026-11-05T10:30:00',
      motivoConsulta: motivo,
      clinicaRural: 'Clínica Rural San Pedro',
    };
    const res = await telemedicinaKafkaApi.simularEvento(evento);
    if (!res.error) {
      setResponse({ success: true, message: `Evento ${eventType} publicado en telemedicina.citas.eventos` });
      setTimeout(loadData, 700);
    } else {
      setResponse({ success: false, message: res.message || 'Error al publicar el evento' });
    }
  };

  const handleClearEventos = async () => {
    await telemedicinaKafkaApi.clearEventos();
    setEventos([]);
  };

  const handleClearAlertas = async () => {
    await telemedicinaKafkaApi.clearAlertas();
    setAlertas([]);
  };

  const handleCreateTopic = async () => {
    if (!newTopic.name) return;
    const res = await telemedicinaKafkaApi.createTopic(newTopic);
    if (!res.error) {
      setNewTopic({ name: '', partitions: 3, replicas: 1, retentionMs: '604800000', cleanupPolicy: 'delete' });
      loadAdmin();
    }
  };

  const handleDeleteTopic = async (name) => {
    await telemedicinaKafkaApi.deleteTopic(name);
    loadAdmin();
  };

  const severityBadge = (s) => {
    if (s === 'ALTA') return 'badge-danger';
    if (s === 'MEDIA') return 'badge-warning';
    return 'badge-info';
  };

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      {/* Header */}
      <div className="glass-card" style={{
        background: 'linear-gradient(135deg, rgba(10, 20, 40, 0.95) 0%, rgba(20, 35, 60, 0.9) 100%)',
        borderLeft: '4px solid var(--accent-teal)',
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px', flexWrap: 'wrap' }}>
              <span className="badge badge-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                <Stethoscope size={14} /> Kafka · Telemedicina (EP3)
              </span>
              <span className={`badge ${clusterStatus === 'online' ? 'badge-success' : 'badge-warning'}`}>
                <span className={`status-dot ${clusterStatus === 'online' ? 'online' : 'offline'}`} />
                {clusterStatus === 'online' ? 'Cluster ACTIVO (9092 / 8888)' : 'DESCONECTADO'}
              </span>
            </div>
            <h1 style={{ fontSize: '1.7rem', fontWeight: 800, margin: 0 }} className="text-gradient">
              Streaming de Citas → Alertas Clínicas
            </h1>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '6px', maxWidth: '880px' }}>
              Dos casos de uso sobre Apache Kafka: <strong>ciclo de vida de citas</strong> (<code>telemedicina.citas.eventos</code>) y
              <strong> alertas clínicas en tiempo real</strong> (<code>telemedicina.alertas</code>), con productores, consumidores,
              consumer groups, DLT y administración de tópicos.
            </p>
          </div>
          <button onClick={() => { checkHealth(); loadData(); }} className="btn btn-secondary" style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
            <RefreshCw size={16} /> Refrescar
          </button>
        </div>

        {/* Sub-tabs */}
        <div style={{ display: 'flex', gap: '10px', marginTop: '22px', borderTop: '1px solid var(--border-color)', paddingTop: '16px', flexWrap: 'wrap' }}>
          <button onClick={() => setActiveTab('flujo')} className={`btn ${activeTab === 'flujo' ? 'btn-primary' : 'btn-secondary'}`}>
            <Activity size={16} /> Flujo Citas → Alertas
          </button>
          <button onClick={() => setActiveTab('admin')} className={`btn ${activeTab === 'admin' ? 'btn-primary' : 'btn-secondary'}`}>
            <Server size={16} /> Administrar Tópicos & Consumer Groups
          </button>
        </div>
      </div>

      {activeTab === 'flujo' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          {/* Arquitectura */}
          <div className="glass-card">
            <h3 style={{ fontSize: '1.15rem', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Layers size={18} color="var(--accent-teal)" /> Arquitectura de los 2 casos de uso
            </h3>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px', alignItems: 'center' }}>
              <div style={{ textAlign: 'center', padding: '12px', background: 'rgba(59,130,246,0.1)', borderRadius: '8px', border: '1px solid rgba(59,130,246,0.3)' }}>
                <div style={{ fontSize: '0.72rem', color: 'var(--accent-blue)', fontWeight: 700, textTransform: 'uppercase' }}>Productor</div>
                <div style={{ fontWeight: 700, marginTop: '4px' }}>citas-service</div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', marginTop: '4px' }}>Publica CitaEventoDTO</div>
              </div>
              <ArrowRight size={20} color="var(--text-muted)" style={{ justifySelf: 'center' }} />
              <div style={{ textAlign: 'center', padding: '12px', background: 'rgba(16,185,129,0.1)', borderRadius: '8px', border: '1px solid rgba(16,185,129,0.3)' }}>
                <div style={{ fontSize: '0.72rem', color: 'var(--accent-emerald)', fontWeight: 700, textTransform: 'uppercase' }}>Tópico A</div>
                <div style={{ fontWeight: 700, marginTop: '4px' }}>telemedicina.citas.eventos</div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', marginTop: '4px' }}>3 particiones · consumer group citas-group</div>
              </div>
              <ArrowRight size={20} color="var(--text-muted)" style={{ justifySelf: 'center' }} />
              <div style={{ textAlign: 'center', padding: '12px', background: 'rgba(245,158,11,0.1)', borderRadius: '8px', border: '1px solid rgba(245,158,11,0.3)' }}>
                <div style={{ fontSize: '0.72rem', color: 'var(--accent-amber)', fontWeight: 700, textTransform: 'uppercase' }}>Consumidor/Productor</div>
                <div style={{ fontWeight: 700, marginTop: '4px' }}>kafka-service</div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', marginTop: '4px' }}>Detecta cancelación/reprogramación → AlertaClinicaDTO</div>
              </div>
              <ArrowRight size={20} color="var(--text-muted)" style={{ justifySelf: 'center' }} />
              <div style={{ textAlign: 'center', padding: '12px', background: 'rgba(244,63,94,0.1)', borderRadius: '8px', border: '1px solid rgba(244,63,94,0.3)' }}>
                <div style={{ fontSize: '0.72rem', color: 'var(--accent-rose)', fontWeight: 700, textTransform: 'uppercase' }}>Tópico B</div>
                <div style={{ fontWeight: 700, marginTop: '4px' }}>telemedicina.alertas</div>
                <div style={{ fontSize: '0.78rem', color: 'var(--text-secondary)', marginTop: '4px' }}>consumer group alertas-group</div>
              </div>
            </div>
          </div>

          {/* Simulador + Lista de alertas */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '24px' }}>
            {/* Simulador */}
            <div className="glass-card">
              <h3 style={{ fontSize: '1.15rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Send size={18} color="var(--accent-teal)" /> Simular evento de cita (Productor)
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Tipo de evento:</label>
                  <select className="form-input" value={eventType} onChange={(e) => setEventType(e.target.value)} style={{ width: '100%' }}>
                    {EVENT_TYPES.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
                  </select>
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>RUT paciente:</label>
                    <input className="form-input" value={rutPaciente} onChange={(e) => setRutPaciente(e.target.value)} style={{ width: '100%' }} />
                  </div>
                  <div>
                    <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Cita ID:</label>
                    <input type="number" className="form-input" value={citaId} onChange={(e) => setCitaId(e.target.value)} style={{ width: '100%' }} />
                  </div>
                </div>
                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Nombre paciente:</label>
                  <input className="form-input" value={nombrePaciente} onChange={(e) => setNombrePaciente(e.target.value)} style={{ width: '100%' }} />
                </div>
                <div>
                  <label style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>Motivo:</label>
                  <input className="form-input" value={motivo} onChange={(e) => setMotivo(e.target.value)} style={{ width: '100%' }} />
                </div>
                <button onClick={handleSimular} className="btn btn-primary" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
                  <Send size={16} /> Publicar evento en Kafka
                </button>
                {response && (
                  <div className={`badge ${response.success ? 'badge-success' : 'badge-danger'}`} style={{ padding: '10px 14px', borderRadius: '8px' }}>
                    {response.message}
                  </div>
                )}
              </div>
            </div>

            {/* Alertas */}
            <div className="glass-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                <h3 style={{ fontSize: '1.15rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Bell size={18} color="var(--accent-rose)" /> Alertas clínicas (telemedicina.alertas)
                </h3>
                {alertas.length > 0 && <button onClick={handleClearAlertas} className="btn btn-secondary btn-sm">Limpiar</button>}
              </div>
              {alertas.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '30px 20px', color: 'var(--text-secondary)' }}>
                  <CheckCircle2 size={34} color="var(--accent-emerald)" style={{ margin: '0 auto 12px auto', display: 'block' }} />
                  <p style={{ fontWeight: 600 }}>Sin alertas registradas</p>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Publica una cita CANCELADA o REPROGRAMADA para generar una alerta.</p>
                </div>
              ) : (
                <div style={{ overflowX: 'auto', maxHeight: '330px' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                    <thead>
                      <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', textAlign: 'left' }}>
                        <th style={{ padding: '8px' }}>Tipo</th>
                        <th style={{ padding: '8px' }}>Severidad</th>
                        <th style={{ padding: '8px' }}>Paciente</th>
                        <th style={{ padding: '8px' }}>Mensaje</th>
                      </tr>
                    </thead>
                    <tbody>
                      {alertas.map((a, i) => (
                        <tr key={i} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                          <td style={{ padding: '8px', fontWeight: 600 }}>{a.tipo}</td>
                          <td style={{ padding: '8px' }}><span className={`badge ${severityBadge(a.severidad)}`}>{a.severidad}</span></td>
                          <td style={{ padding: '8px' }}>{a.nombrePaciente || a.rutPaciente}</td>
                          <td style={{ padding: '8px', color: 'var(--text-secondary)' }}>{a.mensaje}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </div>

          {/* Eventos recibidos */}
          <div className="glass-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <h3 style={{ fontSize: '1.15rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Calendar size={18} color="var(--accent-teal)" /> Eventos de citas consumidos (telemedicina.citas.eventos)
              </h3>
              {eventos.length > 0 && <button onClick={handleClearEventos} className="btn btn-secondary btn-sm">Limpiar</button>}
            </div>
            {eventos.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '26px 20px', color: 'var(--text-secondary)' }}>
                <Terminal size={30} color="var(--text-muted)" style={{ margin: '0 auto 10px auto', display: 'block' }} />
                <p style={{ fontSize: '0.85rem' }}>El consumer group <code>telemedicina-citas-group</code> aún no ha recibido eventos.</p>
              </div>
            ) : (
              <div style={{ overflowX: 'auto', maxHeight: '300px' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', textAlign: 'left' }}>
                      <th style={{ padding: '8px' }}>Tipo</th>
                      <th style={{ padding: '8px' }}>Cita ID</th>
                      <th style={{ padding: '8px' }}>Paciente</th>
                      <th style={{ padding: '8px' }}>Estado</th>
                      <th style={{ padding: '8px' }}>Timestamp</th>
                    </tr>
                  </thead>
                  <tbody>
                    {eventos.map((e, i) => (
                      <tr key={i} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                        <td style={{ padding: '8px', fontWeight: 600 }}>{e.eventType}</td>
                        <td style={{ padding: '8px', fontFamily: 'monospace' }}>{e.citaId}</td>
                        <td style={{ padding: '8px' }}>{e.nombrePaciente || e.rutPaciente}</td>
                        <td style={{ padding: '8px' }}><span className="badge badge-info">{e.estado}</span></td>
                        <td style={{ padding: '8px', color: 'var(--text-muted)', fontSize: '0.78rem' }}>{e.timestamp}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'admin' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
          {/* Admin tópicos */}
          <div className="glass-card">
            <h3 style={{ fontSize: '1.15rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Server size={18} color="var(--accent-teal)" /> Administración de Tópicos
            </h3>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '16px' }}>
              <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr', gap: '8px' }}>
                <input className="form-input" placeholder="nombre-tópico" value={newTopic.name} onChange={(e) => setNewTopic({ ...newTopic, name: e.target.value })} />
                <input type="number" className="form-input" placeholder="particiones" value={newTopic.partitions} onChange={(e) => setNewTopic({ ...newTopic, partitions: Number(e.target.value) })} />
                <input type="number" className="form-input" placeholder="réplicas" value={newTopic.replicas} onChange={(e) => setNewTopic({ ...newTopic, replicas: Number(e.target.value) })} />
              </div>
              <button onClick={handleCreateTopic} disabled={!newTopic.name} className="btn btn-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
                <PlusCircle size={16} /> Crear tópico
              </button>
            </div>
            <div style={{ overflowX: 'auto', maxHeight: '320px' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', textAlign: 'left' }}>
                    <th style={{ padding: '8px' }}>Tópico</th>
                    <th style={{ padding: '8px' }}></th>
                  </tr>
                </thead>
                <tbody>
                  {topics.map((t) => (
                    <tr key={t} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                      <td style={{ padding: '8px', fontFamily: 'monospace', color: 'var(--accent-teal)' }}>{t}</td>
                      <td style={{ padding: '8px', textAlign: 'right' }}>
                        <button onClick={() => handleDeleteTopic(t)} className="btn btn-secondary btn-sm" title="Eliminar tópico">
                          <Trash2 size={14} />
                        </button>
                      </td>
                    </tr>
                  ))}
                  {topics.length === 0 && <tr><td colSpan={2} style={{ padding: '16px', color: 'var(--text-muted)' }}>Sin tópicos visibles (verifica que el cluster esté activo).</td></tr>}
                </tbody>
              </table>
            </div>
          </div>

          {/* Consumer groups */}
          <div className="glass-card">
            <h3 style={{ fontSize: '1.15rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Activity size={18} color="var(--accent-teal)" /> Consumer Groups
            </h3>
            <div style={{ overflowX: 'auto', maxHeight: '320px' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', textAlign: 'left' }}>
                    <th style={{ padding: '8px' }}>Group ID</th>
                    <th style={{ padding: '8px' }}>Tipo</th>
                  </tr>
                </thead>
                <tbody>
                  {consumerGroups.map((g) => (
                    <tr key={g.groupId} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                      <td style={{ padding: '8px', fontFamily: 'monospace', color: '#fff' }}>{g.groupId}</td>
                      <td style={{ padding: '8px' }}><span className="badge badge-info">{g.simpleConsumerGroup ? 'simple' : 'consumer'}</span></td>
                    </tr>
                  ))}
                  {consumerGroups.length === 0 && <tr><td colSpan={2} style={{ padding: '16px', color: 'var(--text-muted)' }}>Sin consumer groups activos.</td></tr>}
                </tbody>
              </table>
            </div>
            <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '12px' }}>
              <AlertTriangle size={13} style={{ verticalAlign: '-2px' }} /> Los groups <code>telemedicina-citas-group</code> y <code>telemedicina-alertas-group</code> aparecen tras consumir el primer evento.
            </p>
          </div>
        </div>
      )}
    </div>
  );
}
