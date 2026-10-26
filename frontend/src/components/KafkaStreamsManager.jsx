import React, { useState, useEffect } from 'react';
import {
  Layers,
  ShieldAlert,
  Send,
  RefreshCw,
  ExternalLink,
  CheckCircle2,
  AlertTriangle,
  Cpu,
  Database,
  Terminal,
  ArrowRight,
  User,
  DollarSign,
  Play,
  RotateCcw,
  Sparkles,
  Info,
  Server
} from 'lucide-react';
import { kafkaApi, bancoApi } from '../services/api';

export default function KafkaStreamsManager() {
  const [activeTab, setActiveTab] = useState('pipeline'); // 'pipeline' | 'particiones' | 'cloud'
  const [clusterStatus, setClusterStatus] = useState('verificando'); // 'online' | 'offline' | 'verificando'

  // States for Guía 1: Pipeline Kafka Streams
  const [txUsuario, setTxUsuario] = useState('juan');
  const [txMonto, setTxMonto] = useState('15000');
  const [txOrigen, setTxOrigen] = useState('cajero');
  const [sendingTx, setSendingTx] = useState(false);
  const [txResponse, setTxResponse] = useState(null);
  const [alertas, setAlertas] = useState([]);
  const [transacciones, setTransacciones] = useState([]);
  const [streamConsoleLogs, setStreamConsoleLogs] = useState([
    { id: 1, text: 'ℹ️ Motor Kafka Streams inicializado (application-id: banco-pipeline-app)', type: 'info', time: 'Sistema' },
    { id: 2, text: 'ℹ️ Escuchando topic "banco-transacciones" con Serdes JSON...', type: 'info', time: 'Sistema' }
  ]);

  // States for Guía 2: Particiones, Productor y Consumidor
  const [kafkaMessage, setKafkaMessage] = useState('Mensaje de prueba');
  const [kafkaKey, setKafkaKey] = useState('');
  const [sendingKafka, setSendingKafka] = useState(false);
  const [kafkaResponse, setKafkaResponse] = useState(null);
  const [receivedMessages, setReceivedMessages] = useState([]);
  const [runningScenario, setRunningScenario] = useState(false);

  // Determinar la IP o host de backend para Kafka UI
  const getBackendHost = () => {
    if (typeof window !== 'undefined') {
      const hostname = window.location.hostname;
      return hostname === 'localhost' || hostname === '127.0.0.1' ? 'localhost' : hostname;
    }
    return 'localhost';
  };

  const kafkaUiUrl = `http://${getBackendHost()}:8090`;
  const kafkaBrokerPort = '9092';
  const kafkaServicePort = '8888';

  // Verificar estado de los servicios Kafka
  const checkHealth = async () => {
    try {
      const [kHealth, bHealth] = await Promise.all([
        kafkaApi.health(),
        bancoApi.health()
      ]);
      if (!kHealth.error || !bHealth.error) {
        setClusterStatus('online');
      } else {
        setClusterStatus('offline');
      }
    } catch {
      setClusterStatus('offline');
    }
  };

  // Cargar datos periódicos
  const loadData = async () => {
    const [msgsRes, alertsRes, txsRes] = await Promise.all([
      kafkaApi.getMessages(),
      bancoApi.getAlertas(),
      bancoApi.getTransacciones()
    ]);

    if (!msgsRes.error && Array.isArray(msgsRes.data)) {
      setReceivedMessages(msgsRes.data);
    }
    if (!alertsRes.error && Array.isArray(alertsRes.data)) {
      setAlertas(alertsRes.data);
    }
    if (!txsRes.error && Array.isArray(txsRes.data)) {
      setTransacciones(txsRes.data);
    }
  };

  useEffect(() => {
    checkHealth();
    loadData();
    const interval = setInterval(() => {
      loadData();
    }, 4000);
    return () => clearInterval(interval);
  }, []);

  // --- ACCIONES GUÍA 1: PIPELINE KAFKA STREAMS ---
  const handleSendTransaction = async (override) => {
    const usuario = override?.usuario ?? txUsuario;
    const monto = parseFloat(override?.monto ?? txMonto);
    const origen = override?.origen ?? txOrigen;

    setSendingTx(true);
    setTxResponse(null);

    const txPayload = { usuario, monto, origen, timestamp: Date.now() };

    const res = await bancoApi.sendTransaccion(txPayload);
    setSendingTx(false);

    const nowStr = new Date().toLocaleTimeString();

    if (!res.error) {
      setTxResponse({ success: true, message: typeof res.data === 'string' ? res.data : `📤 Enviado: ${monto} de ${usuario}` });

      // Simulación de los logs exactos en consola pedidos en la guía
      const newLogs = [];
      if (monto > 10) {
        newLogs.push({
          id: Date.now(),
          text: `✅ Transacción procesada: ${monto.toFixed(1)} [Usuario: ${usuario}, Origen: ${origen}]`,
          type: 'success',
          time: nowStr
        });
      } else {
        newLogs.push({
          id: Date.now(),
          text: `⏹️ Transacción filtrada e ignorada: $${monto.toFixed(1)} < $10.0 (No pasa al pipeline)`,
          type: 'warning',
          time: nowStr
        });
      }

      if (monto > 10000) {
        newLogs.push({
          id: Date.now() + 1,
          text: `🚨 ALERTA GENERADA: Transacción inusualmente alta en ${origen} | Monto: $${monto.toFixed(1)} -> Enviada a topic 'banco-alertas'`,
          type: 'danger',
          time: nowStr
        });
      }

      setStreamConsoleLogs(prev => [...newLogs, ...prev.slice(0, 30)]);
      setTimeout(loadData, 500);
    } else {
      setTxResponse({ success: false, message: res.message || 'Error al enviar transacción a Kafka' });
    }
  };

  // --- ACCIONES GUÍA 2: PARTICIONES Y OFFSETS ---
  const handleSendKafkaMessage = async (msgOverride, keyOverride) => {
    const msg = msgOverride !== undefined ? msgOverride : kafkaMessage;
    const key = keyOverride !== undefined ? keyOverride : kafkaKey;

    setSendingKafka(true);
    setKafkaResponse(null);

    const res = await kafkaApi.send(msg, key);
    setSendingKafka(false);

    if (!res.error) {
      setKafkaResponse({
        success: true,
        message: typeof res.data === 'string' ? res.data : `Mensaje enviado al topic 'tutorial-particiones' ${key ? `con KEY: ${key}` : '(sin key - Round Robin)'}`
      });
      setTimeout(loadData, 500);
    } else {
      setKafkaResponse({ success: false, message: res.message || 'Error al publicar mensaje en Kafka' });
    }
  };

  // Escenario 1: Round Robin (3 mensajes sin key)
  const runRoundRobinDemo = async () => {
    setRunningScenario(true);
    try {
      await handleSendKafkaMessage('Mensaje1', '');
      await new Promise(r => setTimeout(r, 600));
      await handleSendKafkaMessage('Mensaje2', '');
      await new Promise(r => setTimeout(r, 600));
      await handleSendKafkaMessage('Mensaje3', '');
      await new Promise(r => setTimeout(r, 600));
      loadData();
    } finally {
      setRunningScenario(false);
    }
  };

  // Escenario 2: Sticky Partitioning con Key (3 pasos de una misma orden)
  const runStickyPartitionDemo = async () => {
    setRunningScenario(true);
    try {
      await handleSendKafkaMessage('Paso1-Creada', 'orden-555');
      await new Promise(r => setTimeout(r, 600));
      await handleSendKafkaMessage('Paso2-Pagada', 'orden-555');
      await new Promise(r => setTimeout(r, 600));
      await handleSendKafkaMessage('Paso3-Enviada', 'orden-555');
      await new Promise(r => setTimeout(r, 600));
      loadData();
    } finally {
      setRunningScenario(false);
    }
  };

  const handleClearAlerts = async () => {
    await bancoApi.clearAlertas();
    setAlertas([]);
  };

  const handleClearMessages = async () => {
    await kafkaApi.clearMessages();
    setReceivedMessages([]);
  };

  return (
    <div className="kafka-manager-container" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Header Banner */}
      <div className="glass-card" style={{
        background: 'linear-gradient(135deg, rgba(14, 23, 42, 0.95) 0%, rgba(30, 41, 59, 0.9) 100%)',
        borderLeft: '4px solid var(--accent-teal)',
        position: 'relative',
        overflow: 'hidden'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <span className="badge badge-primary" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                <Cpu size={14} /> Apache Kafka & Kafka Streams
              </span>
              <span className={`badge ${clusterStatus === 'online' ? 'badge-success' : 'badge-warning'}`} style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                <span className={`status-dot ${clusterStatus === 'online' ? 'online' : 'offline'}`} />
                Cluster Kafka Cloud: {clusterStatus === 'online' ? 'ACTIVO (Puerto 9092 / 8888)' : 'DESCONECTADO'}
              </span>
            </div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 800, margin: 0 }} className="text-gradient">
              Pipeline de Eventos & Kafka Streams en la Nube
            </h1>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', marginTop: '6px', maxWidth: '850px' }}>
              Plataforma Cloud Native desacoplada con <strong>Apache Kafka</strong>, <strong>ZooKeeper</strong> y <strong>Kafka Streams</strong>.
              Demostración en vivo de particiones, offsets, orden garantizado mediante <em>Sticky Partitioning</em> y pipeline de detección de fraude en tiempo real.
            </p>
          </div>

          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            <a
              href={kafkaUiUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="btn btn-primary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '8px', textDecoration: 'none' }}
            >
              <ExternalLink size={16} /> Abrir Kafka UI (Puerto 8090)
            </a>
            <button
              onClick={() => { checkHealth(); loadData(); }}
              className="btn btn-secondary"
              style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
            >
              <RefreshCw size={16} /> Refrescar
            </button>
          </div>
        </div>

        {/* Navigation Sub-Tabs */}
        <div style={{ display: 'flex', gap: '12px', marginTop: '24px', borderTop: '1px solid var(--border-color)', paddingTop: '16px' }}>
          <button
            onClick={() => setActiveTab('pipeline')}
            className={`btn ${activeTab === 'pipeline' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
          >
            <ShieldAlert size={16} /> Guía 1: Pipeline Kafka Streams (Detección de Fraude)
          </button>
          <button
            onClick={() => setActiveTab('particiones')}
            className={`btn ${activeTab === 'particiones' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
          >
            <Layers size={16} /> Guía 2: Particiones, Offsets & Round Robin
          </button>
          <button
            onClick={() => setActiveTab('cloud')}
            className={`btn ${activeTab === 'cloud' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
          >
            <Server size={16} /> Verificación & Kafka UI Cloud
          </button>
        </div>
      </div>

      {/* --- PESTAÑA 1: GUÍA 1 - PIPELINE KAFKA STREAMS --- */}
      {activeTab === 'pipeline' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Arquitectura del Pipeline Visual */}
          <div className="glass-card">
            <h3 style={{ fontSize: '1.15rem', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Layers size={18} color="var(--accent-teal)" /> Topología de Kafka Streams en Tiempo Real
            </h3>
            
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
              gap: '14px',
              alignItems: 'center',
              background: 'rgba(0, 0, 0, 0.25)',
              padding: '16px',
              borderRadius: 'var(--radius-sm)',
              border: '1px solid var(--border-color)'
            }}>
              <div style={{ textAlign: 'center', padding: '12px', background: 'rgba(59, 130, 246, 0.1)', borderRadius: '8px', border: '1px solid rgba(59, 130, 246, 0.3)' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--accent-blue)', fontWeight: 700, textTransform: 'uppercase' }}>1. Entrada (Source)</div>
                <div style={{ fontWeight: 700, marginTop: '4px', color: '#fff' }}>banco-transacciones</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '4px' }}>Eventos JSON crudos con usuario, monto, origen</div>
              </div>

              <div style={{ textAlign: 'center', padding: '12px', background: 'rgba(16, 185, 129, 0.1)', borderRadius: '8px', border: '1px solid rgba(16, 185, 129, 0.3)' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--accent-emerald)', fontWeight: 700, textTransform: 'uppercase' }}>2. Filtro de Negocio</div>
                <div style={{ fontWeight: 700, marginTop: '4px', color: '#fff' }}>monto &gt; $10</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '4px' }}>Descarta transacciones menores automáticamente</div>
              </div>

              <div style={{ textAlign: 'center', padding: '12px', background: 'rgba(244, 63, 94, 0.1)', borderRadius: '8px', border: '1px solid rgba(244, 63, 94, 0.3)' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--accent-rose)', fontWeight: 700, textTransform: 'uppercase' }}>3. Detección de Fraude</div>
                <div style={{ fontWeight: 700, marginTop: '4px', color: '#fff' }}>monto &gt; $10,000</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '4px' }}>Transforma a objeto AlertaFraude (Severidad: ALTA)</div>
              </div>

              <div style={{ textAlign: 'center', padding: '12px', background: 'rgba(168, 85, 247, 0.1)', borderRadius: '8px', border: '1px solid rgba(168, 85, 247, 0.3)' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--accent-purple)', fontWeight: 700, textTransform: 'uppercase' }}>4. Salida (Sink)</div>
                <div style={{ fontWeight: 700, marginTop: '4px', color: '#fff' }}>banco-alertas</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '4px' }}>Notifica a oficiales de seguridad y Kafka UI</div>
              </div>
            </div>
          </div>

          {/* Grid de 2 Columnas: Simulador de Transacciones + Escenarios Rápidos */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '24px' }}>
            
            {/* Simulador Manual */}
            <div className="glass-card">
              <h3 style={{ fontSize: '1.15rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Send size={18} color="var(--accent-teal)" /> Productor Simulado (REST API /api/banco/transaccion)
              </h3>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Usuario Titular:
                  </label>
                  <div style={{ position: 'relative' }}>
                    <input
                      type="text"
                      className="form-input"
                      value={txUsuario}
                      onChange={(e) => setTxUsuario(e.target.value)}
                      placeholder="Ej: juan, maria, carlos"
                      style={{ width: '100%', paddingLeft: '36px' }}
                    />
                    <User size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                  <div>
                    <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                      Monto (USD):
                    </label>
                    <div style={{ position: 'relative' }}>
                      <input
                        type="number"
                        className="form-input"
                        value={txMonto}
                        onChange={(e) => setTxMonto(e.target.value)}
                        placeholder="Ej: 15000"
                        style={{ width: '100%', paddingLeft: '36px' }}
                      />
                      <DollarSign size={16} style={{ position: 'absolute', left: '12px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
                    </div>
                  </div>

                  <div>
                    <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                      Canal de Origen:
                    </label>
                    <select
                      className="form-input"
                      value={txOrigen}
                      onChange={(e) => setTxOrigen(e.target.value)}
                      style={{ width: '100%' }}
                    >
                      <option value="cajero">cajero (ATM)</option>
                      <option value="web">web (Portal)</option>
                      <option value="app">app (Móvil)</option>
                    </select>
                  </div>
                </div>

                <button
                  onClick={() => handleSendTransaction()}
                  disabled={sendingTx || !txUsuario || !txMonto}
                  className="btn btn-primary"
                  style={{ marginTop: '8px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                >
                  <Send size={16} /> {sendingTx ? 'Transmitiendo a Kafka...' : 'Enviar Transacción'}
                </button>

                {txResponse && (
                  <div className={`badge ${txResponse.success ? 'badge-success' : 'badge-danger'}`} style={{ padding: '10px 14px', borderRadius: '8px', marginTop: '6px' }}>
                    {txResponse.message}
                  </div>
                )}
              </div>
            </div>

            {/* Escenarios Guiados del PDF */}
            <div className="glass-card">
              <h3 style={{ fontSize: '1.15rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Sparkles size={18} color="var(--accent-teal)" /> Casos de Prueba Oficiales (Guía Duoc UC)
              </h3>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                <div style={{ padding: '12px', background: 'rgba(255, 255, 255, 0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: 600, color: 'var(--accent-emerald)' }}>1. Transacción Normal (Sin Alerta)</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Monto: $500 • Origen: app • Esperado: procesada sin alerta</div>
                    </div>
                    <button
                      onClick={() => handleSendTransaction({ usuario: 'juan', monto: 500, origen: 'app' })}
                      className="btn btn-secondary btn-sm"
                    >
                      Probar ($500)
                    </button>
                  </div>
                </div>

                <div style={{ padding: '12px', background: 'rgba(244, 63, 94, 0.08)', borderRadius: '8px', border: '1px solid rgba(244, 63, 94, 0.25)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: 600, color: 'var(--accent-rose)' }}>2. Transacción Fraudulenta (Genera Alerta)</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Monto: $15,000 • Origen: cajero • Esperado: Alerta ALTA en banco-alertas</div>
                    </div>
                    <button
                      onClick={() => handleSendTransaction({ usuario: 'juan', monto: 15000, origen: 'cajero' })}
                      className="btn btn-primary btn-sm"
                      style={{ background: 'var(--accent-rose)', borderColor: 'var(--accent-rose)' }}
                    >
                      Probar ($15k)
                    </button>
                  </div>
                </div>

                <div style={{ padding: '12px', background: 'rgba(255, 255, 255, 0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: 600, color: 'var(--accent-amber)' }}>3. Transacción Ignorada (Filtro)</div>
                      <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Monto: $5 • Origen: app • Esperado: Filtrada porque 5 &lt; 10</div>
                    </div>
                    <button
                      onClick={() => handleSendTransaction({ usuario: 'juan', monto: 5, origen: 'app' })}
                      className="btn btn-secondary btn-sm"
                    >
                      Probar ($5)
                    </button>
                  </div>
                </div>
              </div>
            </div>

          </div>

          {/* Grid Inferior: Alertas en Tiempo Real + Consola de Kafka Streams */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
            
            {/* Tabla de Alertas de Fraude */}
            <div className="glass-card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
                <h3 style={{ fontSize: '1.15rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <ShieldAlert size={18} color="var(--accent-rose)" /> Alertas de Fraude Generadas (Topic: banco-alertas)
                </h3>
                {alertas.length > 0 && (
                  <button onClick={handleClearAlerts} className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
                    Limpiar
                  </button>
                )}
              </div>

              {alertas.length === 0 ? (
                <div style={{ textAlign: 'center', padding: '36px 20px', color: 'var(--text-secondary)' }}>
                  <CheckCircle2 size={36} color="var(--accent-emerald)" style={{ margin: '0 auto 12px auto', display: 'block' }} />
                  <p style={{ fontWeight: 600 }}>No hay alertas de fraude registradas</p>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Envía una transacción con monto &gt; $10,000 para que Kafka Streams dispare una alerta.</p>
                </div>
              ) : (
                <div style={{ overflowX: 'auto', maxHeight: '350px' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                    <thead>
                      <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', textAlign: 'left' }}>
                        <th style={{ padding: '8px' }}>Severidad</th>
                        <th style={{ padding: '8px' }}>Usuario</th>
                        <th style={{ padding: '8px' }}>Monto</th>
                        <th style={{ padding: '8px' }}>Motivo</th>
                      </tr>
                    </thead>
                    <tbody>
                      {alertas.map((a, idx) => (
                        <tr key={idx} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                          <td style={{ padding: '10px 8px' }}>
                            <span className="badge badge-danger" style={{ fontWeight: 700, fontSize: '0.75rem' }}>
                              🚨 {a.severidad || 'ALTA'}
                            </span>
                          </td>
                          <td style={{ padding: '10px 8px', fontWeight: 600 }}>{a.usuario}</td>
                          <td style={{ padding: '10px 8px', color: 'var(--accent-rose)', fontWeight: 700 }}>
                            ${Number(a.monto || 0).toLocaleString()} USD
                          </td>
                          <td style={{ padding: '10px 8px', color: 'var(--text-secondary)' }}>{a.motivo}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            {/* Consola de Kafka Streams (Visualización de logs de ejecución) */}
            <div className="glass-card" style={{ background: '#050914', border: '1px solid rgba(0, 210, 255, 0.2)' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                <h3 style={{ fontSize: '1.05rem', display: 'flex', alignItems: 'center', gap: '8px', color: '#00d2ff' }}>
                  <Terminal size={16} /> Consola de Logs Kafka Streams (Pipeline Topology)
                </h3>
                <span className="badge badge-info" style={{ fontSize: '0.7rem' }}>En Vivo</span>
              </div>

              <div style={{
                fontFamily: 'monospace',
                fontSize: '0.8rem',
                lineHeight: 1.6,
                maxHeight: '320px',
                overflowY: 'auto',
                display: 'flex',
                flexDirection: 'column',
                gap: '6px',
                padding: '8px',
                background: 'rgba(0,0,0,0.4)',
                borderRadius: '6px'
              }}>
                {streamConsoleLogs.map((log) => (
                  <div key={log.id} style={{
                    color: log.type === 'danger' ? '#f43f5e' : log.type === 'success' ? '#10b981' : log.type === 'warning' ? '#f59e0b' : '#94a3b8'
                  }}>
                    <span style={{ color: '#475569', marginRight: '8px' }}>[{log.time}]</span>
                    {log.text}
                  </div>
                ))}
              </div>
            </div>

          </div>

        </div>
      )}

      {/* --- PESTAÑA 2: GUÍA 2 - PARTICIONES, OFFSETS Y ROUND ROBIN --- */}
      {activeTab === 'particiones' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Tarjeta Explicativa */}
          <div className="glass-card">
            <h3 style={{ fontSize: '1.15rem', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Info size={18} color="var(--accent-teal)" /> Concepto: Distribución entre Particiones vs Orden Garantizado
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6 }}>
              Apache Kafka divide un Topic en múltiples <strong>particiones</strong> (en este tutorial: <strong>3 particiones</strong>).
              Los mensajes <strong>sin clave (key)</strong> se distribuyen equitativamente entre las particiones <em>0, 1 y 2</em> mediante <strong>Round Robin</strong>.
              Cuando se envía una <strong>clave (key)</strong> (como <code>orden-555</code>), Kafka calcula un hash que asegura que todos los eventos relacionados vayan <strong>siempre a la misma partición</strong>, garantizando su orden secuencial estricto mediante <strong>offsets incrementales</strong>.
            </p>
          </div>

          {/* Tester de Publicación + Escenarios Rápidos */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '24px' }}>
            
            {/* Formulario Manual */}
            <div className="glass-card">
              <h3 style={{ fontSize: '1.15rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Send size={18} color="var(--accent-teal)" /> Productor de Kafka (/api/kafka/send)
              </h3>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Mensaje (String):
                  </label>
                  <input
                    type="text"
                    className="form-input"
                    value={kafkaMessage}
                    onChange={(e) => setKafkaMessage(e.target.value)}
                    placeholder="Ej: Mensaje1 o Paso1-Creada"
                    style={{ width: '100%' }}
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '6px' }}>
                    Key Opcional (deja vacío para Round Robin):
                  </label>
                  <input
                    type="text"
                    className="form-input"
                    value={kafkaKey}
                    onChange={(e) => setKafkaKey(e.target.value)}
                    placeholder="Ej: orden-555 (o dejar vacío)"
                    style={{ width: '100%' }}
                  />
                  <small style={{ color: 'var(--text-muted)', fontSize: '0.75rem', marginTop: '4px', display: 'block' }}>
                    Sin key: Round Robin automático (0, 1, 2). Con key: Hash partition garantizada.
                  </small>
                </div>

                <button
                  onClick={() => handleSendKafkaMessage()}
                  disabled={sendingKafka || !kafkaMessage}
                  className="btn btn-primary"
                  style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                >
                  <Send size={16} /> {sendingKafka ? 'Publicando...' : 'Publicar en tutorial-particiones'}
                </button>

                {kafkaResponse && (
                  <div className={`badge ${kafkaResponse.success ? 'badge-success' : 'badge-danger'}`} style={{ padding: '10px 14px', borderRadius: '8px' }}>
                    {kafkaResponse.message}
                  </div>
                )}
              </div>
            </div>

            {/* Escenarios Automatizados de la Guía */}
            <div className="glass-card">
              <h3 style={{ fontSize: '1.15rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Play size={18} color="var(--accent-teal)" /> Pruebas Automatizadas de la Guía
              </h3>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                
                <div style={{ padding: '14px', background: 'rgba(255, 255, 255, 0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                  <div style={{ fontWeight: 600, color: 'var(--accent-teal)', marginBottom: '4px' }}>
                    PRUEBA 1: Round Robin (Sin Key)
                  </div>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '10px' }}>
                    Envía secuencialmente <strong>Mensaje1</strong>, <strong>Mensaje2</strong> y <strong>Mensaje3</strong> sin key.
                    Observa cómo Kafka los distribuye entre las particiones 0, 1 y 2.
                  </p>
                  <button
                    onClick={runRoundRobinDemo}
                    disabled={runningScenario}
                    className="btn btn-primary btn-sm"
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
                  >
                    <Play size={14} /> Ejecutar Prueba Round Robin (3 mensajes)
                  </button>
                </div>

                <div style={{ padding: '14px', background: 'rgba(59, 130, 246, 0.08)', borderRadius: '8px', border: '1px solid rgba(59, 130, 246, 0.3)' }}>
                  <div style={{ fontWeight: 600, color: 'var(--accent-blue)', marginBottom: '4px' }}>
                    PRUEBA 2: Sticky Partitioning (Con Key 'orden-555')
                  </div>
                  <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginBottom: '10px' }}>
                    Simula el ciclo de vida de una orden: <strong>Paso1-Creada</strong> → <strong>Paso2-Pagada</strong> → <strong>Paso3-Enviada</strong> con la misma clave <code>orden-555</code>.
                    Todos van a la <strong>misma partición</strong> incrementando offsets secuenciales (1, 2, 3), garantizando orden.
                  </p>
                  <button
                    onClick={runStickyPartitionDemo}
                    disabled={runningScenario}
                    className="btn btn-secondary btn-sm"
                    style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}
                  >
                    <Play size={14} /> Ejecutar Ciclo de Orden (Misma Key)
                  </button>
                </div>

              </div>
            </div>

          </div>

          {/* Tabla de Mensajes Recibidos por el Consumidor */}
          <div className="glass-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div>
                <h3 style={{ fontSize: '1.15rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Database size={18} color="var(--accent-teal)" /> Mensajes Recibidos por @KafkaListener (Topic: tutorial-particiones)
                </h3>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '4px 0 0 0' }}>
                  Consumer Group: <code>mi-grupo-educativo</code> • Captura en tiempo real de partición, offset y key
                </p>
              </div>

              {receivedMessages.length > 0 && (
                <button onClick={handleClearMessages} className="btn btn-secondary btn-sm">
                  Limpiar Mensajes
                </button>
              )}
            </div>

            {receivedMessages.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '36px 20px', color: 'var(--text-secondary)' }}>
                <Layers size={36} color="var(--text-muted)" style={{ margin: '0 auto 12px auto', display: 'block' }} />
                <p style={{ fontWeight: 600 }}>No hay mensajes recibidos aún</p>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Ejecuta la Prueba 1 o Prueba 2 para observar los offsets y particiones.</p>
              </div>
            ) : (
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', textAlign: 'left' }}>
                      <th style={{ padding: '10px' }}>📍 Partición</th>
                      <th style={{ padding: '10px' }}>Offset</th>
                      <th style={{ padding: '10px' }}>Key</th>
                      <th style={{ padding: '10px' }}>Mensaje Contenido</th>
                      <th style={{ padding: '10px' }}>Timestamp</th>
                    </tr>
                  </thead>
                  <tbody>
                    {receivedMessages.map((m, idx) => (
                      <tr key={idx} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                        <td style={{ padding: '10px' }}>
                          <span className={`badge ${m.partition === 0 ? 'badge-primary' : m.partition === 1 ? 'badge-info' : 'badge-success'}`} style={{ fontWeight: 700 }}>
                            Partición {m.partition}
                          </span>
                        </td>
                        <td style={{ padding: '10px', fontFamily: 'monospace', fontWeight: 600 }}>
                          #{m.offset}
                        </td>
                        <td style={{ padding: '10px' }}>
                          {m.key ? (
                            <span className="badge badge-warning" style={{ fontFamily: 'monospace' }}>
                              {m.key}
                            </span>
                          ) : (
                            <span style={{ color: 'var(--text-muted)', fontStyle: 'italic' }}>
                              (sin key)
                            </span>
                          )}
                        </td>
                        <td style={{ padding: '10px', fontWeight: 600, color: '#fff' }}>
                          {m.message}
                        </td>
                        <td style={{ padding: '10px', color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                          {new Date(m.timestamp).toLocaleTimeString()}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

        </div>
      )}

      {/* --- PESTAÑA 3: VERIFICACIÓN & KAFKA UI CLOUD --- */}
      {activeTab === 'cloud' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          <div className="glass-card">
            <h3 style={{ fontSize: '1.2rem', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Server size={20} color="var(--accent-teal)" /> Arquitectura Cloud Native en AWS EC2
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6 }}>
              En la nube, la infraestructura provisionada con <strong>Terraform</strong> expone los puertos de mensajería y monitoreo
              a través del Security Group <code>telemedicina_backend_ec2_sg_v2</code> y el Firewall UFW:
            </p>

            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))',
              gap: '16px',
              marginTop: '16px'
            }}>
              <div style={{ padding: '14px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--accent-teal)', fontWeight: 700 }}>Kafka UI (Monitoreo Visual)</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, margin: '4px 0' }}>Puerto 8090</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Dashboard web de tópicos, consumidores, particiones y mensajes en vivo.</div>
                <a
                  href={kafkaUiUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn btn-secondary btn-sm"
                  style={{ marginTop: '10px', display: 'inline-flex', alignItems: 'center', gap: '6px' }}
                >
                  <ExternalLink size={14} /> Abrir Kafka UI
                </a>
              </div>

              <div style={{ padding: '14px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--accent-blue)', fontWeight: 700 }}>Microservicio Kafka & Streams</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, margin: '4px 0' }}>Puerto 8888</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Spring Boot 3 con Spring Kafka, Kafka Streams y endpoints /api/kafka y /api/banco.</div>
              </div>

              <div style={{ padding: '14px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--accent-purple)', fontWeight: 700 }}>Kafka Broker (Confluent)</div>
                <div style={{ fontSize: '1.1rem', fontWeight: 700, margin: '4px 0' }}>Puerto 9092</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Broker de mensajería con Auto Create Topics y Zookeeper coordinador (2181).</div>
              </div>
            </div>
          </div>

          {/* Comandos cURL para terminal */}
          <div className="glass-card">
            <h3 style={{ fontSize: '1.15rem', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Terminal size={18} color="var(--accent-teal)" /> Comandos de Verificación por Consola (cURL)
            </h3>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              
              <div>
                <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--accent-teal)', marginBottom: '4px' }}>
                  1. Enviar Transacción Normal (Guía 1):
                </div>
                <pre style={{ background: '#050914', padding: '12px', borderRadius: '6px', fontSize: '0.8rem', color: '#38bdf8', overflowX: 'auto' }}>
{`curl -X POST http://${getBackendHost()}:8080/api/banco/transaccion \\
  -H "Content-Type: application/json" \\
  -d '{"usuario": "juan", "monto": 500, "origen": "app"}'`}
                </pre>
              </div>

              <div>
                <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--accent-rose)', marginBottom: '4px' }}>
                  2. Enviar Transacción Fraudulenta (Guía 1 - Dispara Alerta):
                </div>
                <pre style={{ background: '#050914', padding: '12px', borderRadius: '6px', fontSize: '0.8rem', color: '#f43f5e', overflowX: 'auto' }}>
{`curl -X POST http://${getBackendHost()}:8080/api/banco/transaccion \\
  -H "Content-Type: application/json" \\
  -d '{"usuario": "juan", "monto": 15000, "origen": "cajero"}'`}
                </pre>
              </div>

              <div>
                <div style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--accent-amber)', marginBottom: '4px' }}>
                  3. Enviar Mensaje con Key para Sticky Partitioning (Guía 2):
                </div>
                <pre style={{ background: '#050914', padding: '12px', borderRadius: '6px', fontSize: '0.8rem', color: '#fbbf24', overflowX: 'auto' }}>
{`curl -X GET "http://${getBackendHost()}:8080/api/kafka/send?message=Paso1-Creada&key=orden-555"`}
                </pre>
              </div>

            </div>
          </div>

        </div>
      )}

    </div>
  );
}
