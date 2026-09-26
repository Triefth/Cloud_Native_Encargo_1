import React, { useState, useEffect } from 'react';
import { 
  Radio, 
  AlertTriangle, 
  Info, 
  AlertOctagon, 
  Send, 
  Server, 
  RefreshCw, 
  ExternalLink, 
  CheckCircle, 
  Cpu, 
  ShieldAlert, 
  Sliders, 
  PlusCircle, 
  Pause, 
  Play, 
  Trash2, 
  Layers
} from 'lucide-react';

export default function RabbitMQLogging() {
  const [activeSubTab, setActiveSubTab] = useState('dlq'); // 'dlq', 'direct', 'admin'

  // States for Guía 1 & 2 (DirectExchange & Logging)
  const [logLevel, setLogLevel] = useState('INFO');
  const [logMessageText, setLogMessageText] = useState('');
  const [helloMessageText, setHelloMessageText] = useState('Hola RabbitMQ desde Telemedicina!');
  const [logsList, setLogsList] = useState([]);
  const [loading, setLoading] = useState(false);

  // States for Guía 3 (DLX / DLQ Orders)
  const [orders, setOrders] = useState([]);
  const [stats, setStats] = useState({ total: 0, success: 0, failed: 0 });
  const [orderStatus, setOrderStatus] = useState('Conectando...');

  // States for Guía 4 (Programmatic Admin & Listener Control)
  const [newQueueName, setNewQueueName] = useState('');
  const [newExchangeName, setNewExchangeName] = useState('');
  const [listenerStatus, setListenerStatus] = useState({ orderListener: 'ACTIVE' });

  const tryFetch = async (urls, options) => {
    let lastError = null;
    for (const url of urls) {
      try {
        const res = await fetch(url, options);
        if (res.ok) {
          const text = await res.text();
          return { ok: true, url, text, status: res.status };
        }
        if (res.status !== 404) {
          const errText = await res.text().catch(() => '');
          return { ok: false, url, status: res.status, text: errText || res.statusText };
        }
        lastError = new Error(`HTTP 404 en ${url}`);
      } catch (err) {
        lastError = err;
      }
    }
    throw lastError || new Error('No se pudo conectar a ningún endpoint de backend.');
  };

  const addLogEntry = (type, text, details, isError = false) => {
    const timestamp = new Date().toLocaleTimeString();
    setLogsList((prev) => [
      { id: Date.now() + Math.random(), timestamp, type, text, details, isError },
      ...prev.slice(0, 49)
    ]);
  };

  // --- GUÍA 3: DLX / DLQ ORDERS LOGIC ---
  const checkBackendStatus = async () => {
    try {
      const res = await tryFetch(['http://localhost:8080/api/orders/status', 'http://localhost:8084/api/orders/status'], { method: 'GET' });
      if (res.ok) {
        setOrderStatus('✓ Conectado a Backend & RabbitMQ');
      }
    } catch {
      setOrderStatus('✗ Backend o RabbitMQ desconectado');
    }
  };

  useEffect(() => {
    checkBackendStatus();
    const interval = setInterval(checkBackendStatus, 8000);
    return () => clearInterval(interval);
  }, []);

  const sendOrder = async (customerName) => {
    const orderId = `ORD-${Date.now().toString().slice(-6)}`;
    setLoading(true);

    const candidateUrls = [
      'http://localhost:8080/api/orders/send',
      'http://localhost:8084/api/orders/send'
    ];

    try {
      const result = await tryFetch(candidateUrls, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ orderId, customerName })
      });

      if (!result.ok) {
        throw new Error(`Error HTTP ${result.status}: ${result.text}`);
      }

      let parsedData = {};
      try {
        parsedData = JSON.parse(result.text);
      } catch (e) {
        parsedData = { message: result.text };
      }

      const newOrder = {
        id: orderId,
        customer: customerName,
        message: parsedData.message || `Orden ID: ${orderId} | Cliente: ${customerName}`,
        timestamp: new Date().toLocaleTimeString(),
        status: 'ENVIADA (En cola principal)'
      };

      setOrders((prev) => [newOrder, ...prev]);
      setStats((prev) => ({ ...prev, total: prev.total + 1 }));

      addLogEntry('ORDER', `Orden enviada: ${orderId}`, `Cliente: ${customerName} | Sent to orders.exchange`);

      // Simular resolución de Ack/Nack por parte del consumidor con fallo aleatorio (DLX)
      setTimeout(() => {
        const isSuccess = Math.random() > 0.4;
        setOrders((prevOrders) =>
          prevOrders.map((o) =>
            o.id === orderId
              ? {
                  ...o,
                  status: isSuccess ? 'PROCESADA (Ack Exitoso)' : 'FALLIDA -> EN DLQ (Nack Cuarentena)'
                }
              : o
          )
        );

        if (isSuccess) {
          setStats((prev) => ({ ...prev, success: prev.success + 1 }));
          addLogEntry('INFO', `Orden ${orderId} procesada con éxito`, `Ack manual recibido por el worker.`);
        } else {
          setStats((prev) => ({ ...prev, failed: prev.failed + 1 }));
          addLogEntry('ERROR', `Orden ${orderId} falló y fue enviada a DLQ`, `Nack con requeue=false -> orders.dlx -> orders.dlq`, true);
        }
      }, 2000);

    } catch (error) {
      addLogEntry('ERROR', `Error al enviar orden`, error.message, true);
    } finally {
      setLoading(false);
    }
  };

  // --- GUÍA 1 & 2: LOGGING LOGIC ---
  const sendLog = async (level, message) => {
    setLoading(true);
    const bodyData = { level, message };
    const candidateUrls = [
      'http://localhost:8080/log',
      'http://localhost:8080/api/bff/log',
      'http://localhost:8084/log'
    ];

    try {
      const result = await tryFetch(candidateUrls, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(bodyData)
      });

      if (!result.ok) {
        throw new Error(`Estado HTTP ${result.status}: ${result.text}`);
      }

      addLogEntry(
        level,
        `Log '${level}' enviado con éxito!`,
        `Mensaje: "${message}" | Endpoint: ${result.url} | Respuesta: ${result.text}`
      );
    } catch (error) {
      addLogEntry('ERROR', `Error al enviar log '${level}'`, error.message, true);
    } finally {
      setLoading(false);
    }
  };

  const sendHelloMessage = async () => {
    if (!helloMessageText.trim()) return;
    setLoading(true);

    const candidateUrls = [
      'http://localhost:8080/api/messages',
      'http://localhost:8080/api/bff/messages',
      'http://localhost:8084/api/messages'
    ];

    try {
      const result = await tryFetch(candidateUrls, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: helloMessageText })
      });

      if (!result.ok) {
        throw new Error(`Estado HTTP ${result.status}: ${result.text}`);
      }

      addLogEntry('HELLO', `Mensaje enviado a cola 'hello'!`, `Mensaje: "${helloMessageText}" | Respuesta: ${result.text}`);
    } catch (error) {
      addLogEntry('ERROR', `Error enviando a cola 'hello'`, error.message, true);
    } finally {
      setLoading(false);
    }
  };

  // --- GUÍA 4: PROGRAMMATIC ADMIN & LISTENER CONTROL ---
  const handleCreateQueue = async () => {
    if (!newQueueName.trim()) return;
    setLoading(true);
    try {
      const res = await tryFetch([`http://localhost:8080/api/rabbitmq/queues?queueName=${newQueueName}`, `http://localhost:8084/api/rabbitmq/queues?queueName=${newQueueName}`], { method: 'POST' });
      addLogEntry('ADMIN', `Cola '${newQueueName}' creada dinámicamente`, res.text);
      setNewQueueName('');
    } catch (err) {
      addLogEntry('ERROR', `Error al crear cola`, err.message, true);
    } finally {
      setLoading(false);
    }
  };

  const handleToggleListener = async (action) => {
    setLoading(true);
    const listenerId = 'order-listener';
    try {
      const res = await tryFetch([`http://localhost:8080/api/listeners/${listenerId}/${action}`, `http://localhost:8084/api/listeners/${listenerId}/${action}`], { method: 'POST' });
      setListenerStatus({ orderListener: action === 'pause' ? 'PAUSED' : 'ACTIVE' });
      addLogEntry('ADMIN', `Listener '${listenerId}' -> ${action.toUpperCase()}`, res.text);
    } catch (err) {
      addLogEntry('ERROR', `Error al cambiar estado de listener`, err.message, true);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ maxWidth: '1400px', margin: '0 auto', display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Header Banner */}
      <div className="glass-card" style={{ padding: '28px', borderRadius: 'var(--radius-lg)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <Radio size={28} style={{ color: 'var(--accent-cyan)' }} />
              <h2 className="text-gradient" style={{ fontSize: '1.6rem', fontWeight: 800, margin: 0 }}>
                Sistema de Mensajería & Resiliencia RabbitMQ
              </h2>
            </div>
            <p style={{ color: 'var(--text-secondary)', margin: 0, fontSize: '0.95rem' }}>
              Implementación integral de las 4 Guías Duoc UC: <code>DirectExchange</code>, <code>DLX / DLQ</code>, <code>Políticas TTL</code>, <code>RabbitAdmin</code> y <code>Dynamic Listeners</code>.
            </p>
          </div>

          <div style={{ display: 'flex', gap: '12px' }}>
            <a 
              href="http://localhost:15672" 
              target="_blank" 
              rel="noreferrer" 
              className="btn btn-secondary"
              style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.85rem' }}
            >
              <Server size={16} />
              RabbitMQ Management (15672)
              <ExternalLink size={14} />
            </a>
          </div>
        </div>

        {/* Sub-Navigation Tabs */}
        <div style={{ display: 'flex', gap: '12px', marginTop: '20px', borderTop: '1px solid var(--border-color)', paddingTop: '16px' }}>
          <button 
            className="btn" 
            onClick={() => setActiveSubTab('dlq')}
            style={{
              background: activeSubTab === 'dlq' ? 'var(--gradient-brand)' : 'rgba(255,255,255,0.05)',
              color: activeSubTab === 'dlq' ? '#fff' : 'var(--text-secondary)',
              borderRadius: '8px',
              fontWeight: 600
            }}
          >
            <ShieldAlert size={16} /> Guía 3: DLX / DLQ & Resiliencia de Órdenes
          </button>

          <button 
            className="btn" 
            onClick={() => setActiveSubTab('direct')}
            style={{
              background: activeSubTab === 'direct' ? 'var(--gradient-brand)' : 'rgba(255,255,255,0.05)',
              color: activeSubTab === 'direct' ? '#fff' : 'var(--text-secondary)',
              borderRadius: '8px',
              fontWeight: 600
            }}
          >
            <Radio size={16} /> Guías 1 y 2: DirectExchange & Logging System
          </button>

          <button 
            className="btn" 
            onClick={() => setActiveSubTab('admin')}
            style={{
              background: activeSubTab === 'admin' ? 'var(--gradient-brand)' : 'rgba(255,255,255,0.05)',
              color: activeSubTab === 'admin' ? '#fff' : 'var(--text-secondary)',
              borderRadius: '8px',
              fontWeight: 600
            }}
          >
            <Sliders size={16} /> Guía 4: RabbitAdmin & Control Dinámico
          </button>
        </div>
      </div>

      {/* --- SUBTAB 1: GUÍA 3 (DLX / DLQ ORDERS) --- */}
      {activeSubTab === 'dlq' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          
          {/* Stats Bar */}
          <div className="grid-3">
            <div className="glass-card" style={{ padding: '20px', borderLeft: '4px solid #3b82f6' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Total Órdenes Enviadas</span>
              <h3 style={{ fontSize: '1.8rem', margin: '4px 0 0 0', color: '#fff' }}>{stats.total}</h3>
            </div>

            <div className="glass-card" style={{ padding: '20px', borderLeft: '4px solid #10b981' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>Procesadas con Éxito (Ack)</span>
              <h3 style={{ fontSize: '1.8rem', margin: '4px 0 0 0', color: '#34d399' }}>{stats.success}</h3>
            </div>

            <div className="glass-card" style={{ padding: '20px', borderLeft: '4px solid #ef4444' }}>
              <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>En DLQ (Cuarentena Nack)</span>
              <h3 style={{ fontSize: '1.8rem', margin: '4px 0 0 0', color: '#f87171' }}>{stats.failed}</h3>
            </div>
          </div>

          {/* Control Panel for Orders */}
          <div className="glass-card" style={{ padding: '24px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div>
                <h3 style={{ fontSize: '1.2rem', fontWeight: 700, margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Send size={20} color="#38bdf8" />
                  Simulador de Procesador de Órdenes (Guía 3)
                </h3>
                <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: '4px 0 0 0' }}>
                  Estado Backend: <span style={{ color: orderStatus.includes('✓') ? '#34d399' : '#f87171', fontWeight: 600 }}>{orderStatus}</span>
                </p>
              </div>
            </div>

            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '16px' }}>
              Al enviar una orden, esta entra a <code>orders.queue</code> con TTL 30s. Si el worker simula un error, ejecuta <code>channel.basicNack(requeue=false)</code> y RabbitMQ la deriva automáticamente al Dead Letter Exchange (<code>orders.dlx</code>) hacia la cola en cuarentena <code>orders.dlq</code>.
            </p>

            <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
              <button className="btn btn-primary" onClick={() => sendOrder('Juan Pérez')} disabled={loading}>
                + Enviar Orden: Juan Pérez
              </button>
              <button className="btn btn-primary" onClick={() => sendOrder('María García')} disabled={loading}>
                + Enviar Orden: María García
              </button>
              <button className="btn btn-primary" onClick={() => sendOrder('Carlos López')} disabled={loading}>
                + Enviar Orden: Carlos López
              </button>
            </div>
          </div>

          {/* Order History Table */}
          <div className="glass-card" style={{ padding: '24px' }}>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, marginBottom: '16px' }}>
              Historial de Órdenes Procesadas
            </h3>

            {orders.length === 0 ? (
              <div style={{ textAlign: 'center', padding: '30px', color: 'var(--text-muted)' }}>
                No hay órdenes procesadas aún. Haz clic en los botones superiores para enviar órdenes de prueba.
              </div>
            ) : (
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {orders.map((order) => (
                  <div 
                    key={order.id} 
                    style={{
                      padding: '16px',
                      borderRadius: '8px',
                      background: order.status.includes('FALLIDA') ? 'rgba(239, 68, 68, 0.1)' : order.status.includes('PROCESADA') ? 'rgba(16, 185, 129, 0.1)' : 'rgba(255,255,255,0.03)',
                      borderLeft: `4px solid ${order.status.includes('FALLIDA') ? '#ef4444' : order.status.includes('PROCESADA') ? '#10b981' : '#3b82f6'}`,
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      flexWrap: 'wrap',
                      gap: '12px'
                    }}
                  >
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <strong style={{ fontSize: '1rem', color: '#fff' }}>{order.id}</strong>
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>Cliente: {order.customer}</span>
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{order.timestamp}</span>
                      </div>
                      <div style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginTop: '4px', fontFamily: 'monospace' }}>
                        {order.message}
                      </div>
                    </div>

                    <span className="badge" style={{
                      background: order.status.includes('FALLIDA') ? '#ef4444' : order.status.includes('PROCESADA') ? '#10b981' : '#3b82f6',
                      color: '#fff',
                      padding: '4px 12px',
                      fontSize: '0.75rem'
                    }}>
                      {order.status}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>

        </div>
      )}

      {/* --- SUBTAB 2: GUÍAS 1 & 2 (DIRECT EXCHANGE LOGGING) --- */}
      {activeSubTab === 'direct' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
          
          <div className="glass-card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '14px' }}>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700, margin: '0 0 6px 0', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Radio size={20} color="#38bdf8" />
                Guía 2: Pruebas Rápidas por Routing Key
              </h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', margin: 0 }}>
                Envia eventos a <code>logs_direct_exchange</code> filtrados por routing key.
              </p>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              <button 
                className="btn" 
                onClick={() => sendLog('INFO', 'El usuario ha iniciado sesión.')}
                disabled={loading}
                style={{ background: 'linear-gradient(135deg, #0284c7 0%, #0369a1 100%)', color: '#fff', padding: '12px 16px', borderRadius: '8px' }}
              >
                <Info size={18} /> Enviar Log INFO (RoutingKey: "INFO")
              </button>

              <button 
                className="btn" 
                onClick={() => sendLog('WARNING', 'El uso de CPU está al 85%.')}
                disabled={loading}
                style={{ background: 'linear-gradient(135deg, #d97706 0%, #b45309 100%)', color: '#fff', padding: '12px 16px', borderRadius: '8px' }}
              >
                <AlertTriangle size={18} /> Enviar Log WARNING (RoutingKey: "WARNING")
              </button>

              <button 
                className="btn" 
                onClick={() => sendLog('ERROR', 'No se pudo conectar a la base de datos.')}
                disabled={loading}
                style={{ background: 'linear-gradient(135deg, #dc2626 0%, #991b1b 100%)', color: '#fff', padding: '12px 16px', borderRadius: '8px' }}
              >
                <AlertOctagon size={18} /> Enviar Log ERROR (RoutingKey: "ERROR" ➔ 2 Colas)
              </button>
            </div>
          </div>

          <div className="glass-card" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div style={{ borderBottom: '1px solid var(--border-color)', paddingBottom: '14px' }}>
              <h3 style={{ fontSize: '1.2rem', fontWeight: 700, margin: '0 0 6px 0', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Cpu size={20} color="#a855f7" />
                Guía 1: Cola Directa 'hello'
              </h3>
            </div>

            <div style={{ display: 'flex', gap: '10px' }}>
              <input 
                type="text"
                value={helloMessageText}
                onChange={(e) => setHelloMessageText(e.target.value)}
                className="input-field"
                style={{ flex: 1 }}
              />
              <button className="btn btn-primary" onClick={sendHelloMessage} disabled={loading}>
                <Send size={16} /> Enviar a 'hello'
              </button>
            </div>
          </div>

        </div>
      )}

      {/* --- SUBTAB 3: GUÍA 4 (RABBITADMIN & LISTENERS) --- */}
      {activeSubTab === 'admin' && (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '24px' }}>
          
          {/* Dynamic Queue Creation */}
          <div className="glass-card" style={{ padding: '24px' }}>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <PlusCircle size={20} color="#34d399" />
              Gestión Programática (RabbitAdmin)
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '16px' }}>
              Crea colas dinámicamente en tiempo de ejecución utilizando <code>rabbitAdmin.declareQueue()</code>.
            </p>

            <div style={{ display: 'flex', gap: '10px' }}>
              <input 
                type="text" 
                placeholder="Nombre de la nueva cola..." 
                value={newQueueName}
                onChange={(e) => setNewQueueName(e.target.value)}
                className="input-field"
                style={{ flex: 1 }}
              />
              <button className="btn btn-primary" onClick={handleCreateQueue} disabled={loading || !newQueueName.trim()}>
                Crear Cola
              </button>
            </div>
          </div>

          {/* Dynamic Listener Control */}
          <div className="glass-card" style={{ padding: '24px' }}>
            <h3 style={{ fontSize: '1.2rem', fontWeight: 700, marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Sliders size={20} color="#a855f7" />
              Control Dinámico de Listeners
            </h3>
            <p style={{ fontSize: '0.85rem', color: 'var(--text-secondary)', marginBottom: '16px' }}>
              Pausa o reanuda el consumo del listener <code>order-listener</code> usando <code>RabbitListenerEndpointRegistry</code>.
            </p>

            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', background: 'rgba(0,0,0,0.2)', padding: '16px', borderRadius: '8px' }}>
              <div>
                <strong>order-listener</strong>
                <div style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                  Estado: <span style={{ color: listenerStatus.orderListener === 'ACTIVE' ? '#34d399' : '#f87171', fontWeight: 600 }}>{listenerStatus.orderListener}</span>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '10px' }}>
                <button className="btn btn-secondary" onClick={() => handleToggleListener('pause')} disabled={loading}>
                  <Pause size={16} /> Pausar
                </button>
                <button className="btn btn-primary" onClick={() => handleToggleListener('resume')} disabled={loading}>
                  <Play size={16} /> Reanudar
                </button>
              </div>
            </div>
          </div>

        </div>
      )}

      {/* Live Event Console (Visible in all tabs) */}
      <div className="glass-card" style={{ padding: '24px', borderRadius: 'var(--radius-lg)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <h3 style={{ fontSize: '1.1rem', fontWeight: 700, margin: 0, display: 'flex', alignItems: 'center', gap: '8px' }}>
            <RefreshCw size={18} className={loading ? 'spin' : ''} />
            Consola de Auditoría y Eventos en Tiempo Real
          </h3>
          <button className="btn btn-secondary btn-sm" onClick={() => setLogsList([])} disabled={logsList.length === 0}>
            Limpiar Consola
          </button>
        </div>

        {logsList.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '30px', color: 'var(--text-muted)', background: 'rgba(0,0,0,0.2)', borderRadius: '8px' }}>
            No hay eventos registrados aún. Realiza acciones en cualquiera de las pestañas superiores.
          </div>
        ) : (
          <div style={{ maxHeight: '300px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '8px' }}>
            {logsList.map((log) => (
              <div 
                key={log.id} 
                style={{ 
                  background: log.isError ? 'rgba(220, 38, 38, 0.15)' : 'rgba(255, 255, 255, 0.04)',
                  borderLeft: `4px solid ${log.isError ? '#ef4444' : '#38bdf8'}`,
                  padding: '10px 14px',
                  borderRadius: '6px',
                  fontSize: '0.85rem'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontWeight: 700 }}>[{log.timestamp}] {log.text}</span>
                  <span className="badge" style={{ background: log.isError ? '#ef4444' : '#0284c7', color: '#fff', fontSize: '0.7rem' }}>
                    {log.type}
                  </span>
                </div>
                <div style={{ color: 'var(--text-secondary)', fontSize: '0.8rem', fontFamily: 'monospace', marginTop: '2px' }}>
                  {log.details}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

    </div>
  );
}
