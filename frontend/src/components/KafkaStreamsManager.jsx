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
  Server,
  Gauge,
  Zap,
  TrendingDown,
  Bug,
  ShieldCheck,
  AlertOctagon,
  Activity,
  Sliders,
  Pause,
  Clock,
  Flame,
  Check,
  XCircle,
  Copy
} from 'lucide-react';
import { kafkaApi, bancoApi, tuningApi, resilienceApi } from '../services/api';

export default function KafkaStreamsManager() {
  const [activeTab, setActiveTab] = useState('pipeline'); // 'pipeline' | 'particiones' | 'tuning' | 'resiliencia' | 'cloud'
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

  // States for Guía 3: Tuning, Pruebas de Carga & Consumer Lag (PDF 2 y PDF 3)
  const [loadCount, setLoadCount] = useState(10000);
  const [runningLoadTest, setRunningLoadTest] = useState(false);
  const [loadTestResult, setLoadTestResult] = useState(null);
  const [tuningMetrics, setTuningMetrics] = useState(null);
  const [consumerDelay, setConsumerDelay] = useState(10);
  const [updatingDelay, setUpdatingDelay] = useState(false);

  // States for Guía 4: Manejo de Errores, Resiliencia & DLT (PDF 1)
  const [dltRecords, setDltRecords] = useState([]);
  const [resilienceAuditLogs, setResilienceAuditLogs] = useState([]);
  const [processedKeys, setProcessedKeys] = useState({});
  const [runningResilienceAction, setRunningResilienceAction] = useState(false);
  const [resilienceResponse, setResilienceResponse] = useState(null);
  const [customDuplicateId, setCustomDuplicateId] = useState('orden-vip-777');

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
    const [msgsRes, alertsRes, txsRes, tuningRes, dltRes, auditRes, keysRes] = await Promise.all([
      kafkaApi.getMessages(),
      bancoApi.getAlertas(),
      bancoApi.getTransacciones(),
      tuningApi.getMetrics(),
      resilienceApi.getDltRecords(),
      resilienceApi.getAuditLogs(),
      resilienceApi.getProcessedKeys()
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
    if (!tuningRes.error && tuningRes.data) {
      setTuningMetrics(tuningRes.data);
      if (tuningRes.data.consumerDelayMs !== undefined) {
        setConsumerDelay(tuningRes.data.consumerDelayMs);
      }
    }
    if (!dltRes.error && Array.isArray(dltRes.data)) {
      setDltRecords(dltRes.data);
    }
    if (!auditRes.error && Array.isArray(auditRes.data)) {
      setResilienceAuditLogs(auditRes.data);
    }
    if (!keysRes.error && keysRes.data) {
      setProcessedKeys(keysRes.data);
    }
  };

  useEffect(() => {
    checkHealth();
    loadData();
    // Polling regular cada 3 segundos; si está en la pestaña de tuning, refrescar cada 1.5s para ver el lag en vivo
    const intervalTime = activeTab === 'tuning' ? 1500 : 3500;
    const interval = setInterval(() => {
      loadData();
    }, intervalTime);
    return () => clearInterval(interval);
  }, [activeTab]);

  // --- ACCIONES GUÍA 3: TUNING & CONSUMER LAG (PDF 2 y 3) ---
  const handleStartLoadTest = async (countOverride) => {
    const count = countOverride || loadCount;
    setRunningLoadTest(true);
    setLoadTestResult(null);

    const res = await tuningApi.startLoadTest(count);
    setRunningLoadTest(false);

    if (!res.error && res.data) {
      setLoadTestResult({
        success: true,
        message: res.data.message || `Carga de ${count.toLocaleString()} mensajes disparada exitosamente.`,
        data: res.data
      });
      loadData();
    } else {
      setLoadTestResult({
        success: false,
        message: res.message || 'Error al ejecutar la prueba de carga.'
      });
    }
  };

  const handleUpdateConsumerDelay = async (newDelay) => {
    setUpdatingDelay(true);
    const res = await tuningApi.setConsumerDelay(newDelay);
    setUpdatingDelay(false);
    if (!res.error) {
      setConsumerDelay(newDelay);
      loadData();
    }
  };

  const handleToggleConsumerPause = async () => {
    const currentPaused = tuningMetrics?.isConsumerPaused ?? false;
    await tuningApi.togglePause(!currentPaused);
    loadData();
  };

  const handleResetTuning = async () => {
    await tuningApi.reset();
    setLoadTestResult(null);
    loadData();
  };

  // --- ACCIONES GUÍA 4: MANEJO DE ERRORES, DLT & IDEMPOTENCIA (PDF 1) ---
  const handleSimulateValidationError = async (type) => {
    setRunningResilienceAction(true);
    setResilienceResponse(null);
    const res = await resilienceApi.simulateValidationError(type);
    setRunningResilienceAction(false);
    if (!res.error) {
      setResilienceResponse({
        success: true,
        title: 'Validación de Payload (JSR-380)',
        message: res.data.description || 'Fallo de validación enviado a Kafka.',
        type: 'validation'
      });
      setTimeout(loadData, 800);
    } else {
      setResilienceResponse({ success: false, message: res.message });
    }
  };

  const handleSimulatePoisonPill = async () => {
    setRunningResilienceAction(true);
    setResilienceResponse(null);
    const res = await resilienceApi.simulatePoisonPill();
    setRunningResilienceAction(false);
    if (!res.error) {
      setResilienceResponse({
        success: true,
        title: 'Poison Pill (Error de Deserialización)',
        message: res.data.description || 'Poison Pill enviada sin colapsar el consumidor.',
        type: 'poison'
      });
      setTimeout(loadData, 800);
    } else {
      setResilienceResponse({ success: false, message: res.message });
    }
  };

  const handleSimulateDuplicate = async () => {
    setRunningResilienceAction(true);
    setResilienceResponse(null);
    const res = await resilienceApi.simulateDuplicate(customDuplicateId);
    setRunningResilienceAction(false);
    if (!res.error) {
      setResilienceResponse({
        success: true,
        title: 'Idempotencia del Consumidor (Check-then-Act)',
        message: res.data.description || 'Mensajes duplicados enviados.',
        type: 'idempotency'
      });
      setTimeout(loadData, 800);
    } else {
      setResilienceResponse({ success: false, message: res.message });
    }
  };

  const handleSimulateTransient = async () => {
    setRunningResilienceAction(true);
    setResilienceResponse(null);
    const res = await resilienceApi.simulateTransientError();
    setRunningResilienceAction(false);
    if (!res.error) {
      setResilienceResponse({
        success: true,
        title: 'Control de Reintentos (Exponential Backoff)',
        message: res.data.description || 'Fallo transitorio simulado. Reintentos en curso...',
        type: 'retry'
      });
      setTimeout(loadData, 1200);
    } else {
      setResilienceResponse({ success: false, message: res.message });
    }
  };

  const handleSimulatePermanent = async () => {
    setRunningResilienceAction(true);
    setResilienceResponse(null);
    const res = await resilienceApi.simulatePermanentError();
    setRunningResilienceAction(false);
    if (!res.error) {
      setResilienceResponse({
        success: true,
        title: 'Error Permanente -> Desvío a DLT',
        message: res.data.description || 'Error permanente enviado. Redirigido a banco-resiliente.DLT.',
        type: 'dlt'
      });
      setTimeout(loadData, 1500);
    } else {
      setResilienceResponse({ success: false, message: res.message });
    }
  };

  const handleReprocessDlt = async (id) => {
    const res = await resilienceApi.reprocessDlt(id);
    if (!res.error) {
      setResilienceResponse({
        success: true,
        title: 'Herramienta de Reprocesamiento DLT',
        message: `Mensaje re-inyectado al topic principal banco-resiliente con ID: ${res.data.newMessageId || id}`,
        type: 'reprocess'
      });
      loadData();
    }
  };

  const handleClearDlt = async () => {
    await resilienceApi.clearDlt();
    loadData();
  };

  const handleResetResilience = async () => {
    await resilienceApi.reset();
    setResilienceResponse(null);
    loadData();
  };

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
        <div style={{ display: 'flex', gap: '10px', marginTop: '24px', borderTop: '1px solid var(--border-color)', paddingTop: '16px', flexWrap: 'wrap' }}>
          <button
            onClick={() => setActiveTab('pipeline')}
            className={`btn ${activeTab === 'pipeline' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
          >
            <ShieldAlert size={16} /> Guía 1: Streams & Fraude
          </button>
          <button
            onClick={() => setActiveTab('particiones')}
            className={`btn ${activeTab === 'particiones' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
          >
            <Layers size={16} /> Guía 2: Particiones & Offsets
          </button>
          <button
            onClick={() => setActiveTab('tuning')}
            className={`btn ${activeTab === 'tuning' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
          >
            <Gauge size={16} /> Guía 3: Tuning & Consumer Lag
          </button>
          <button
            onClick={() => setActiveTab('resiliencia')}
            className={`btn ${activeTab === 'resiliencia' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
          >
            <Bug size={16} /> Guía 4: Resiliencia, DLT & Errores
          </button>
          <button
            onClick={() => setActiveTab('cloud')}
            className={`btn ${activeTab === 'cloud' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}
          >
            <Server size={16} /> Monitoreo Cloud & Kafka UI
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

      {/* --- PESTAÑA 3: GUÍA 3 - TUNING, PRUEBAS DE CARGA & CONSUMER LAG --- */}
      {activeTab === 'tuning' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Tarjeta de Especificaciones de Tuning Aplicadas */}
          <div className="glass-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', flexWrap: 'wrap', gap: '12px' }}>
              <h3 style={{ fontSize: '1.2rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Gauge size={20} color="var(--accent-teal)" /> Arquitectura de Alto Rendimiento & Tuning Kafka (PDF 2 y 3)
              </h3>
              <button onClick={handleResetTuning} className="btn btn-secondary btn-sm" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                <RotateCcw size={14} /> Reiniciar Métricas
              </button>
            </div>

            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6, marginBottom: '16px' }}>
              Optimizaciones aplicadas al productor y broker para maximizar throughput y minimizar la latencia de red bajo cargas masivas (Duoc UC 2025):
            </p>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '14px' }}>
              <div style={{ padding: '14px', background: 'rgba(59, 130, 246, 0.08)', borderRadius: '8px', border: '1px solid rgba(59, 130, 246, 0.25)' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--accent-blue)', fontWeight: 700, textTransform: 'uppercase' }}>1. Batch Size</div>
                <div style={{ fontSize: '1.15rem', fontWeight: 800, marginTop: '4px', color: '#fff' }}>32 KB</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '4px' }}>Doble del default (16KB) para agrupar miles de registros por llamada I/O.</div>
              </div>

              <div style={{ padding: '14px', background: 'rgba(16, 185, 129, 0.08)', borderRadius: '8px', border: '1px solid rgba(16, 185, 129, 0.25)' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--accent-emerald)', fontWeight: 700, textTransform: 'uppercase' }}>2. Linger Time</div>
                <div style={{ fontSize: '1.15rem', fontWeight: 800, marginTop: '4px', color: '#fff' }}>20 ms</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '4px' }}>Pausa artificial para llenar los buffers antes de transmitir por red.</div>
              </div>

              <div style={{ padding: '14px', background: 'rgba(245, 158, 11, 0.08)', borderRadius: '8px', border: '1px solid rgba(245, 158, 11, 0.25)' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--accent-amber)', fontWeight: 700, textTransform: 'uppercase' }}>3. Compresión</div>
                <div style={{ fontSize: '1.15rem', fontWeight: 800, marginTop: '4px', color: '#fff' }}>Snappy</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '4px' }}>Balance óptimo de compresión rápida con mínimo consumo de CPU.</div>
              </div>

              <div style={{ padding: '14px', background: 'rgba(236, 72, 153, 0.08)', borderRadius: '8px', border: '1px solid rgba(236, 72, 153, 0.25)' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--accent-rose)', fontWeight: 700, textTransform: 'uppercase' }}>4. Retención Broker</div>
                <div style={{ fontSize: '1.15rem', fontWeight: 800, marginTop: '4px', color: '#fff' }}>5 Min / 50 MB</div>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', marginTop: '4px' }}>Limpieza activa cada 60s en <code>docker-compose</code> para evitar saturar disco.</div>
              </div>
            </div>
          </div>

          {/* Panel de Control: Disparador de Carga Masiva */}
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(360px, 1fr))', gap: '24px' }}>
            
            <div className="glass-card">
              <h3 style={{ fontSize: '1.15rem', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Zap size={18} color="var(--accent-teal)" /> Generador de Carga Masiva (Parallel Stream)
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '16px' }}>
                Envía ráfagas de miles de mensajes por segundo en paralelo hacia el topic <code>sensor-topic</code> (3 particiones, 5 keys de sensores).
              </p>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                <div>
                  <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)', display: 'block', marginBottom: '6px' }}>
                    Cantidad de Mensajes a Producir:
                  </label>
                  <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                    {[1000, 5000, 10000, 25000].map(cnt => (
                      <button
                        key={cnt}
                        onClick={() => setLoadCount(cnt)}
                        className={`btn ${loadCount === cnt ? 'btn-primary' : 'btn-secondary'} btn-sm`}
                        style={{ fontSize: '0.8rem', fontWeight: 700 }}
                      >
                        {cnt.toLocaleString()} msgs
                      </button>
                    ))}
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                  <input
                    type="number"
                    min="100"
                    max="100000"
                    value={loadCount}
                    onChange={(e) => setLoadCount(parseInt(e.target.value) || 1000)}
                    style={{
                      background: 'rgba(255, 255, 255, 0.05)',
                      border: '1px solid var(--border-color)',
                      borderRadius: '6px',
                      padding: '10px 14px',
                      color: '#fff',
                      width: '160px',
                      fontSize: '0.9rem'
                    }}
                  />
                  <button
                    onClick={() => handleStartLoadTest()}
                    disabled={runningLoadTest}
                    className="btn btn-primary"
                    style={{ flex: 1, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                  >
                    {runningLoadTest ? (
                      <>
                        <RefreshCw size={16} className="spin" /> Disparando Carga...
                      </>
                    ) : (
                      <>
                        <Play size={16} /> Disparar Carga ({loadCount.toLocaleString()})
                      </>
                    )}
                  </button>
                </div>

                {loadTestResult && (
                  <div style={{
                    padding: '12px 16px',
                    borderRadius: '8px',
                    background: loadTestResult.success ? 'rgba(16, 185, 129, 0.1)' : 'rgba(244, 63, 94, 0.1)',
                    border: `1px solid ${loadTestResult.success ? 'rgba(16, 185, 129, 0.3)' : 'rgba(244, 63, 94, 0.3)'}`,
                    color: loadTestResult.success ? '#34d399' : '#f87171',
                    fontSize: '0.85rem'
                  }}>
                    <div style={{ fontWeight: 700 }}>{loadTestResult.message}</div>
                    {loadTestResult.data && (
                      <div style={{ fontSize: '0.8rem', marginTop: '4px', opacity: 0.9 }}>
                        ⏱ Duración: <strong>{loadTestResult.data.durationMs} ms</strong> | ⚡ Throughput: <strong>{loadTestResult.data.throughputMsgSec?.toLocaleString()} msg/seg</strong>
                      </div>
                    )}
                  </div>
                )}
              </div>
            </div>

            {/* Panel de Control: Slow Consumer & Simulación de Lag */}
            <div className="glass-card">
              <h3 style={{ fontSize: '1.15rem', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Clock size={18} color="var(--accent-amber)" /> Consumidor Pesado & Simulación de Consumer Lag
              </h3>
              <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '16px' }}>
                El <code>SlowConsumer</code> simula procesamiento intensivo (IA/BD). Ajusta su retardo para forzar o aliviar el <em>Consumer Lag</em>.
              </p>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
                <div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                    <label style={{ fontSize: '0.85rem', fontWeight: 600, color: 'var(--text-secondary)' }}>
                      Retardo Artificial por Mensaje:
                    </label>
                    <span className="badge badge-warning" style={{ fontWeight: 700 }}>
                      {consumerDelay} ms / msg (máx {consumerDelay > 0 ? Math.round(1000 / consumerDelay) : '∞'} msg/s)
                    </span>
                  </div>

                  <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap', marginBottom: '10px' }}>
                    {[
                      { label: '0 ms (Drenar Rápido)', val: 0 },
                      { label: '5 ms (Rápido)', val: 5 },
                      { label: '10 ms (Estándar Guía)', val: 10 },
                      { label: '30 ms (Lag Alto)', val: 30 }
                    ].map(opt => (
                      <button
                        key={opt.val}
                        onClick={() => handleUpdateConsumerDelay(opt.val)}
                        disabled={updatingDelay}
                        className={`btn ${consumerDelay === opt.val ? 'btn-primary' : 'btn-secondary'} btn-sm`}
                        style={{ fontSize: '0.75rem' }}
                      >
                        {opt.label}
                      </button>
                    ))}
                  </div>

                  <input
                    type="range"
                    min="0"
                    max="50"
                    step="1"
                    value={consumerDelay}
                    onChange={(e) => handleUpdateConsumerDelay(parseInt(e.target.value))}
                    style={{ width: '100%', cursor: 'pointer' }}
                  />
                </div>

                <div style={{ display: 'flex', gap: '12px', alignItems: 'center', paddingTop: '8px', borderTop: '1px solid var(--border-color)' }}>
                  <button
                    onClick={handleToggleConsumerPause}
                    className={`btn ${tuningMetrics?.isConsumerPaused ? 'btn-success' : 'btn-danger'}`}
                    style={{ flex: 1, display: 'inline-flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}
                  >
                    {tuningMetrics?.isConsumerPaused ? (
                      <>
                        <Play size={16} /> Reanudar Consumidor
                      </>
                    ) : (
                      <>
                        <Pause size={16} /> Pausar Consumidor (Acumular Lag)
                      </>
                    )}
                  </button>

                  <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                    Estado:{' '}
                    <strong style={{ color: tuningMetrics?.isConsumerPaused ? 'var(--accent-rose)' : 'var(--accent-emerald)' }}>
                      {tuningMetrics?.isConsumerPaused ? 'PAUSADO' : 'ACTIVO'}
                    </strong>
                  </div>
                </div>
              </div>
            </div>

          </div>

          {/* Métricas en Tiempo Real: Tacómetro de Consumer Lag */}
          <div className="glass-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', flexWrap: 'wrap', gap: '10px' }}>
              <h3 style={{ fontSize: '1.15rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <TrendingDown size={18} color="var(--accent-rose)" /> Monitor de Consumer Lag en Vivo
              </h3>
              <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                Topic: <code>sensor-topic</code> | Grupo: <code>analytics-group</code> (3 Particiones)
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px', marginBottom: '20px' }}>
              
              <div style={{ padding: '16px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: 600 }}>Total Producidos (LogEndOffset)</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--accent-blue)', margin: '6px 0' }}>
                  {(tuningMetrics?.totalProduced || 0).toLocaleString()}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Mensajes emitidos por el generador</div>
              </div>

              <div style={{ padding: '16px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: 600 }}>Total Consumidos (CurrentOffset)</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--accent-emerald)', margin: '6px 0' }}>
                  {(tuningMetrics?.totalConsumed || 0).toLocaleString()}
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Procesados por SlowConsumer</div>
              </div>

              <div style={{
                padding: '16px',
                background: (tuningMetrics?.consumerLag || 0) > 1000 ? 'rgba(244, 63, 94, 0.12)' : (tuningMetrics?.consumerLag || 0) > 0 ? 'rgba(245, 158, 11, 0.1)' : 'rgba(16, 185, 129, 0.08)',
                borderRadius: '8px',
                border: `1px solid ${(tuningMetrics?.consumerLag || 0) > 1000 ? 'rgba(244, 63, 94, 0.35)' : 'var(--border-color)'}`
              }}>
                <div style={{ fontSize: '0.75rem', color: (tuningMetrics?.consumerLag || 0) > 500 ? 'var(--accent-rose)' : 'var(--text-secondary)', fontWeight: 700 }}>
                  CONSUMER LAG (ATRASO)
                </div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: (tuningMetrics?.consumerLag || 0) > 500 ? 'var(--accent-rose)' : '#fff', margin: '6px 0' }}>
                  {(tuningMetrics?.consumerLag || 0).toLocaleString()} <span style={{ fontSize: '0.85rem', fontWeight: 500 }}>msgs</span>
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  {(tuningMetrics?.consumerLag || 0) === 0 ? '✅ Consumo al día (Sin atraso)' : '⚠️ Mensajes en cola esperando ser consumidos'}
                </div>
              </div>

              <div style={{ padding: '16px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', fontWeight: 600 }}>Último Throughput Medido</div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--accent-teal)', margin: '6px 0' }}>
                  {(tuningMetrics?.lastThroughput || 0).toLocaleString()} <span style={{ fontSize: '0.8rem', fontWeight: 500 }}>msg/s</span>
                </div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Velocidad de inyección batching Snappy</div>
              </div>

            </div>

            {/* Muestreo de Mensajes de Sensores Procesados */}
            <h4 style={{ fontSize: '0.95rem', fontWeight: 700, color: 'var(--text-secondary)', marginBottom: '10px' }}>
              Muestreo de Mensajes Consumidos en sensor-topic (Partición & Offset):
            </h4>

            {(!tuningMetrics?.sampleRecords || tuningMetrics.sampleRecords.length === 0) ? (
              <div style={{ padding: '24px', textAlign: 'center', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                No hay registros aún. Dispara una prueba de carga arriba para ver la distribución y el consumo.
              </div>
            ) : (
              <div style={{ overflowX: 'auto', maxHeight: '240px' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', textAlign: 'left' }}>
                      <th style={{ padding: '8px 10px' }}>Partición</th>
                      <th style={{ padding: '8px 10px' }}>Offset</th>
                      <th style={{ padding: '8px 10px' }}>Key del Sensor</th>
                      <th style={{ padding: '8px 10px' }}>Timestamp</th>
                    </tr>
                  </thead>
                  <tbody>
                    {tuningMetrics.sampleRecords.map((s, idx) => (
                      <tr key={idx} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                        <td style={{ padding: '8px 10px' }}>
                          <span className={`badge ${s.partition === 0 ? 'badge-primary' : s.partition === 1 ? 'badge-info' : 'badge-success'}`}>
                            P-{s.partition}
                          </span>
                        </td>
                        <td style={{ padding: '8px 10px', fontFamily: 'monospace' }}>#{s.offset}</td>
                        <td style={{ padding: '8px 10px' }}>
                          <span className="badge badge-warning" style={{ fontFamily: 'monospace' }}>{s.key}</span>
                        </td>
                        <td style={{ padding: '8px 10px', color: 'var(--text-muted)', fontSize: '0.8rem' }}>
                          {new Date(s.timestamp).toLocaleTimeString()}
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

      {/* --- PESTAÑA 4: GUÍA 4 - RESILIENCIA, DLT, IDEMPOTENCIA Y ERRORES (PDF 1) --- */}
      {activeTab === 'resiliencia' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Header Teórico de Resiliencia Duoc UC */}
          <div className="glass-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px', flexWrap: 'wrap', gap: '12px' }}>
              <h3 style={{ fontSize: '1.2rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <ShieldCheck size={20} color="var(--accent-emerald)" /> Arquitectura Resiliente en Event Streaming (Duoc UC - PDF 1)
              </h3>
              <button onClick={handleResetResilience} className="btn btn-secondary btn-sm" style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
                <RotateCcw size={14} /> Reiniciar Simulación
              </button>
            </div>

            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6, marginBottom: '16px' }}>
              Manejo integral de fallas en streaming de eventos mediante 5 patrones esenciales de resiliencia:
            </p>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '12px' }}>
              <div style={{ padding: '12px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--accent-rose)', fontWeight: 700, fontSize: '0.85rem' }}>1. ErrorHandlingDeserializer</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                  Atrapa <em>Poison Pills</em> (bytes corruptos) evitando que el consumidor se bloquee en un bucle infinito.
                </div>
              </div>

              <div style={{ padding: '12px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--accent-amber)', fontWeight: 700, fontSize: '0.85rem' }}>2. JSR-380 Bean Validation</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                  Valida @Min, @NotBlank y @Email en el payload antes de ejecutar lógica de negocio.
                </div>
              </div>

              <div style={{ padding: '12px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--accent-blue)', fontWeight: 700, fontSize: '0.85rem' }}>3. Exponential Backoff</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                  Previene <em>Retry Storms</em> espaciando los reintentos (1s, 2s, 4s...) ante caídas transitorias de BD.
                </div>
              </div>

              <div style={{ padding: '12px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--accent-teal)', fontWeight: 700, fontSize: '0.85rem' }}>4. Sala de Emergencias (.DLT)</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                  Desvía mensajes fallidos con <em>Header Enrichment</em> (stacktrace, causa original y Correlation ID).
                </div>
              </div>

              <div style={{ padding: '12px', background: 'rgba(255,255,255,0.03)', borderRadius: '8px', border: '1px solid var(--border-color)' }}>
                <div style={{ color: 'var(--accent-purple)', fontWeight: 700, fontSize: '0.85rem' }}>5. Idempotencia Check-then-Act</div>
                <div style={{ fontSize: '0.75rem', color: 'var(--text-secondary)', marginTop: '4px' }}>
                  Registro de <code>message_id</code> para descartar duplicados y evitar cobros dobles en la red.
                </div>
              </div>
            </div>
          </div>

          {/* Consola Interactiva de Simulaciones de Fallos */}
          <div className="glass-card">
            <h3 style={{ fontSize: '1.15rem', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Play size={18} color="var(--accent-teal)" /> Consola Interactiva de Simulaciones Didácticas
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '18px' }}>
              Haz clic en cualquiera de los escenarios para disparar eventos hacia Kafka y observar cómo el consumidor reacciona en tiempo real:
            </p>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '14px' }}>
              
              {/* Escenario 1: Poison Pill */}
              <div style={{ padding: '16px', background: 'rgba(244, 63, 94, 0.06)', borderRadius: '8px', border: '1px solid rgba(244, 63, 94, 0.25)' }}>
                <div style={{ fontWeight: 700, color: 'var(--accent-rose)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <AlertOctagon size={16} /> 1. Poison Pill (Payload Corrupto)
                </div>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '8px 0 12px 0' }}>
                  Envía bytes ilegibles no serializables. ErrorHandlingDeserializer lo intercepta sin congelar la partición.
                </p>
                <button
                  onClick={handleSimulatePoisonPill}
                  disabled={runningResilienceAction}
                  className="btn btn-secondary btn-sm"
                  style={{ width: '100%', borderColor: 'rgba(244, 63, 94, 0.4)' }}
                >
                  Simular Poison Pill
                </button>
              </div>

              {/* Escenario 2: Validación JSR-380 */}
              <div style={{ padding: '16px', background: 'rgba(245, 158, 11, 0.06)', borderRadius: '8px', border: '1px solid rgba(245, 158, 11, 0.25)' }}>
                <div style={{ fontWeight: 700, color: 'var(--accent-amber)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <ShieldAlert size={16} /> 2. Fallo de Validación (JSR-380)
                </div>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '8px 0 12px 0' }}>
                  Transacción con monto negativo (-$500). Falla <code>@Min(1)</code> y va directo a DLT sin reintentos inútiles.
                </p>
                <button
                  onClick={() => handleSimulateValidationError('monto_negativo')}
                  disabled={runningResilienceAction}
                  className="btn btn-secondary btn-sm"
                  style={{ width: '100%', borderColor: 'rgba(245, 158, 11, 0.4)' }}
                >
                  Simular Monto Negativo
                </button>
              </div>

              {/* Escenario 3: Idempotencia / Deduplicación */}
              <div style={{ padding: '16px', background: 'rgba(168, 85, 247, 0.06)', borderRadius: '8px', border: '1px solid rgba(168, 85, 247, 0.25)' }}>
                <div style={{ fontWeight: 700, color: 'var(--accent-purple)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Copy size={16} /> 3. Duplicado (Idempotencia)
                </div>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '8px 0 8px 0' }}>
                  Envía el mismo mensaje 2 veces. El consumidor detecta el <code>message_id</code> existente y descarta el duplicado.
                </p>
                <div style={{ display: 'flex', gap: '6px', marginBottom: '8px' }}>
                  <input
                    type="text"
                    value={customDuplicateId}
                    onChange={(e) => setCustomDuplicateId(e.target.value)}
                    placeholder="ID Orden"
                    style={{
                      background: 'rgba(255,255,255,0.05)',
                      border: '1px solid var(--border-color)',
                      borderRadius: '4px',
                      padding: '4px 8px',
                      color: '#fff',
                      fontSize: '0.75rem',
                      width: '100%'
                    }}
                  />
                </div>
                <button
                  onClick={handleSimulateDuplicate}
                  disabled={runningResilienceAction}
                  className="btn btn-secondary btn-sm"
                  style={{ width: '100%', borderColor: 'rgba(168, 85, 247, 0.4)' }}
                >
                  Enviar Mensaje Duplicado
                </button>
              </div>

              {/* Escenario 4: Error Transitorio */}
              <div style={{ padding: '16px', background: 'rgba(59, 130, 246, 0.06)', borderRadius: '8px', border: '1px solid rgba(59, 130, 246, 0.25)' }}>
                <div style={{ fontWeight: 700, color: 'var(--accent-blue)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <RotateCcw size={16} /> 4. Fallo Transitorio (Reintentos)
                </div>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '8px 0 12px 0' }}>
                  Falla temporalmente en los 2 primeros intentos y se recupera con éxito en el 3ero con backoff exponencial.
                </p>
                <button
                  onClick={handleSimulateTransient}
                  disabled={runningResilienceAction}
                  className="btn btn-secondary btn-sm"
                  style={{ width: '100%', borderColor: 'rgba(59, 130, 246, 0.4)' }}
                >
                  Simular Fallo Temporal
                </button>
              </div>

              {/* Escenario 5: Error Permanente */}
              <div style={{ padding: '16px', background: 'rgba(236, 72, 153, 0.06)', borderRadius: '8px', border: '1px solid rgba(236, 72, 153, 0.25)' }}>
                <div style={{ fontWeight: 700, color: 'var(--accent-rose)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Flame size={16} /> 5. Error Permanente (A DLT)
                </div>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-secondary)', margin: '8px 0 12px 0' }}>
                  Falla definitiva de lógica. Agota reintentos y es desviado automáticamente a la "Sala de Emergencias" (.DLT).
                </p>
                <button
                  onClick={handleSimulatePermanent}
                  disabled={runningResilienceAction}
                  className="btn btn-secondary btn-sm"
                  style={{ width: '100%', borderColor: 'rgba(236, 72, 153, 0.4)' }}
                >
                  Simular Fallo Definitivo
                </button>
              </div>

            </div>

            {/* Banner de Feedback de la Simulación */}
            {resilienceResponse && (
              <div style={{
                marginTop: '16px',
                padding: '14px 18px',
                borderRadius: '8px',
                background: resilienceResponse.success ? 'rgba(16, 185, 129, 0.1)' : 'rgba(244, 63, 94, 0.1)',
                border: `1px solid ${resilienceResponse.success ? 'rgba(16, 185, 129, 0.3)' : 'rgba(244, 63, 94, 0.3)'}`,
                color: '#fff',
                fontSize: '0.85rem'
              }}>
                <div style={{ fontWeight: 700, color: resilienceResponse.success ? 'var(--accent-emerald)' : 'var(--accent-rose)', marginBottom: '4px' }}>
                  {resilienceResponse.title || 'Resultado de la Simulación'}
                </div>
                <div>{resilienceResponse.message}</div>
              </div>
            )}
          </div>

          {/* Sala de Emergencias (Dead Letter Topic - DLT) */}
          <div className="glass-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px', flexWrap: 'wrap', gap: '10px' }}>
              <h3 style={{ fontSize: '1.15rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Bug size={18} color="var(--accent-rose)" /> Sala de Emergencias de Mensajes (Topic: banco-resiliente.DLT)
              </h3>
              <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                <span className="badge badge-danger">
                  {dltRecords.length} en cuarentena
                </span>
                {dltRecords.length > 0 && (
                  <button onClick={handleClearDlt} className="btn btn-secondary btn-sm" style={{ fontSize: '0.75rem' }}>
                    Vaciar Sala
                  </button>
                )}
              </div>
            </div>

            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '14px' }}>
              Mensajes desviados automáticamente por el <code>DeadLetterPublishingRecoverer</code>. Contienen metadatos de excepción inyectados en los headers (Header Enrichment).
            </p>

            {dltRecords.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '36px 20px', color: 'var(--text-secondary)' }}>
                <CheckCircle2 size={36} color="var(--accent-emerald)" style={{ margin: '0 auto 12px auto', display: 'block' }} />
                <p style={{ fontWeight: 600 }}>La Sala de Emergencias está limpia</p>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                  Dispara un fallo permanente o de validación arriba para enviar un mensaje al DLT.
                </p>
              </div>
            ) : (
              <div style={{ overflowX: 'auto', maxHeight: '420px' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', textAlign: 'left' }}>
                      <th style={{ padding: '10px' }}>Ubicación</th>
                      <th style={{ padding: '10px' }}>Correlation ID</th>
                      <th style={{ padding: '10px' }}>Causa de Fallo</th>
                      <th style={{ padding: '10px' }}>Payload</th>
                      <th style={{ padding: '10px' }}>Acción</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dltRecords.map((r) => (
                      <tr key={r.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.05)' }}>
                        <td style={{ padding: '10px' }}>
                          <span className="badge badge-danger" style={{ fontWeight: 700 }}>
                            P-{r.partition} #{r.offset}
                          </span>
                          <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                            {r.originalTopic}
                          </div>
                        </td>
                        <td style={{ padding: '10px', fontFamily: 'monospace', fontSize: '0.8rem' }}>
                          {r.correlationId ? (
                            <span className="badge badge-info">{r.correlationId}</span>
                          ) : (
                            <span style={{ color: 'var(--text-muted)' }}>(sin correlation-id)</span>
                          )}
                        </td>
                        <td style={{ padding: '10px', maxWidth: '300px' }}>
                          <div style={{ color: 'var(--accent-rose)', fontWeight: 600, fontSize: '0.8rem' }}>
                            {r.exceptionMessage}
                          </div>
                          {r.exceptionStacktrace && (
                            <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', fontFamily: 'monospace', marginTop: '4px' }}>
                              {r.exceptionStacktrace.substring(0, 100)}...
                            </div>
                          )}
                        </td>
                        <td style={{ padding: '10px', maxWidth: '240px', fontFamily: 'monospace', fontSize: '0.75rem', color: 'var(--text-secondary)' }}>
                          {r.payload ? (r.payload.length > 70 ? r.payload.substring(0, 70) + '...' : r.payload) : '(vacío)'}
                        </td>
                        <td style={{ padding: '10px' }}>
                          <button
                            onClick={() => handleReprocessDlt(r.id)}
                            className="btn btn-primary btn-sm"
                            style={{ fontSize: '0.75rem', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
                            title="Herramienta de reprocesamiento (Re-inyectar al topic principal)"
                          >
                            <RefreshCw size={12} /> Reprocesar
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          {/* Registro de Auditoría de Idempotencia y Reintentos */}
          <div className="glass-card">
            <h3 style={{ fontSize: '1.15rem', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Activity size={18} color="var(--accent-blue)" /> Auditoría de Idempotencia & Control de Reintentos (MDC Logs)
            </h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.85rem', marginBottom: '14px' }}>
              Trazabilidad en tiempo real de eventos procesados, descartes por duplicidad e intentos con Exponential Backoff:
            </p>

            {resilienceAuditLogs.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '24px', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                Sin eventos de auditoría registrados aún.
              </div>
            ) : (
              <div style={{ overflowX: 'auto', maxHeight: '280px' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '0.85rem' }}>
                  <thead>
                    <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-secondary)', textAlign: 'left' }}>
                      <th style={{ padding: '8px 10px' }}>Evento</th>
                      <th style={{ padding: '8px 10px' }}>Trace ID (MDC)</th>
                      <th style={{ padding: '8px 10px' }}>Message ID</th>
                      <th style={{ padding: '8px 10px' }}>Detalles de Ejecución</th>
                      <th style={{ padding: '8px 10px' }}>Hora</th>
                    </tr>
                  </thead>
                  <tbody>
                    {resilienceAuditLogs.map((log) => (
                      <tr key={log.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                        <td style={{ padding: '8px 10px' }}>
                          <span className={`badge ${
                            log.status === 'PROCESSED_SUCCESS' ? 'badge-success' :
                            log.status === 'IDEMPOTENT_IGNORED' ? 'badge-warning' :
                            log.status === 'RETRY_ATTEMPT' ? 'badge-info' :
                            log.status === 'RETRY_SUCCESS' ? 'badge-success' : 'badge-danger'
                          }`} style={{ fontWeight: 700, fontSize: '0.75rem' }}>
                            {log.status === 'IDEMPOTENT_IGNORED' ? '⚠️ DUPLICADO' :
                             log.status === 'PROCESSED_SUCCESS' ? '✅ ÉXITO' :
                             log.status === 'RETRY_ATTEMPT' ? '🔄 REINTENTO' :
                             log.status === 'RETRY_SUCCESS' ? '✨ RECUPERADO' : '💀 FALLO'}
                          </span>
                        </td>
                        <td style={{ padding: '8px 10px', fontFamily: 'monospace', fontSize: '0.75rem', color: 'var(--accent-teal)' }}>
                          {log.traceId}
                        </td>
                        <td style={{ padding: '8px 10px', fontFamily: 'monospace', fontSize: '0.75rem' }}>
                          {log.messageId}
                        </td>
                        <td style={{ padding: '8px 10px', color: 'var(--text-secondary)', fontSize: '0.8rem' }}>
                          {log.details}
                        </td>
                        <td style={{ padding: '8px 10px', color: 'var(--text-muted)', fontSize: '0.75rem' }}>
                          {new Date(log.timestamp).toLocaleTimeString()}
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

      {/* --- PESTAÑA 5: VERIFICACIÓN & KAFKA UI CLOUD --- */}
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
