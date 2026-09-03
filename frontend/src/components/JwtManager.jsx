import React, { useState } from 'react';
import { authApi, setAuthToken } from '../services/api';
import { ShieldCheck, Key, CheckCircle, AlertTriangle, Copy, RefreshCw } from 'lucide-react';

export default function JwtManager({ activeToken, setActiveToken }) {
  const [user, setUser] = useState('medico.rural@telemedicina.cl');
  const [role, setRole] = useState('MEDICO');
  const [loading, setLoading] = useState(false);
  const [validationResult, setValidationResult] = useState(null);
  const [copied, setCopied] = useState(false);
  const [message, setMessage] = useState('');

  const handleGenerateToken = async () => {
    setLoading(true);
    setMessage('');
    const res = await authApi.getDevToken(user, role);
    setLoading(false);

    if (res.error) {
      setMessage(`Error: ${res.message}`);
      return;
    }

    const token = res.data.token;
    setAuthToken(token);
    setActiveToken(token);
    setMessage('¡Token Dev JWT generado y guardado en localStorage!');
    handleValidate(token);
  };

  const handleValidate = async (tokenToTest = activeToken) => {
    if (!tokenToTest) {
      setValidationResult(null);
      return;
    }
    setLoading(true);
    const res = await authApi.validateToken(tokenToTest);
    setLoading(false);
    if (!res.error) {
      setValidationResult(res.data);
    } else {
      setValidationResult(res.data || { valid: false, message: res.message });
    }
  };

  const handleCopy = () => {
    if (activeToken) {
      navigator.clipboard.writeText(activeToken);
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleClearToken = () => {
    setAuthToken('');
    setActiveToken('');
    setValidationResult(null);
    setMessage('Token eliminado.');
  };

  return (
    <div>
      <div className="section-header">
        <div>
          <h2 className="section-title">
            <ShieldCheck size={28} style={{ color: 'var(--accent-teal)' }} />
            Autenticación & Validación JWT (Azure AD / MSAL)
          </h2>
          <p className="section-subtitle">
            El microservicio <strong>bff-service</strong> intercepta todas las peticiones con <code>JwtValidationFilter</code> para validar el emisor (iss), la audiencia (aud), la fecha de expiración (exp) y la firma HMAC-SHA256.
          </p>
        </div>
      </div>

      <div className="grid-2">
        {/* Generador de Token Dev */}
        <div className="glass-card">
          <h3 style={{ fontSize: '1.1rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Key size={20} style={{ color: 'var(--accent-blue)' }} />
            Generador de Token Dev
          </h3>

          <div className="form-group">
            <label className="form-label">Usuario / Correo MSAL</label>
            <input
              type="text"
              className="form-input"
              value={user}
              onChange={(e) => setUser(e.target.value)}
              placeholder="ej: medico.rural@telemedicina.cl"
            />
          </div>

          <div className="form-group">
            <label className="form-label">Rol de Usuario</label>
            <select
              className="form-select"
              value={role}
              onChange={(e) => setRole(e.target.value)}
            >
              <option value="MEDICO">MEDICO (Acceso a teleconsultas y fichas)</option>
              <option value="PACIENTE">PACIENTE (Acceso a mis citas y atenciones)</option>
              <option value="ADMIN">ADMIN (Acceso total a clínicas y reportes)</option>
            </select>
          </div>

          <div style={{ display: 'flex', gap: '10px', marginTop: '20px' }}>
            <button className="btn btn-primary" onClick={handleGenerateToken} disabled={loading}>
              <RefreshCw size={16} className={loading ? 'pulsing' : ''} />
              {loading ? 'Generando...' : 'Generar Token JWT'}
            </button>

            {activeToken && (
              <button className="btn btn-danger btn-sm" onClick={handleClearToken}>
                Limpiar Token
              </button>
            )}
          </div>

          {message && (
            <p style={{ marginTop: '14px', fontSize: '0.85rem', color: message.startsWith('Error') ? 'var(--accent-rose)' : 'var(--accent-emerald)' }}>
              {message}
            </p>
          )}
        </div>

        {/* Inspección y Validación de Token */}
        <div className="glass-card">
          <h3 style={{ fontSize: '1.1rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <CheckCircle size={20} style={{ color: 'var(--accent-emerald)' }} />
            Inspección y Validación de Token
          </h3>

          {activeToken ? (
            <div>
              <div className="form-group">
                <label className="form-label">Token Bearer JWT Activo</label>
                <div style={{ display: 'flex', gap: '8px' }}>
                  <textarea
                    className="form-textarea"
                    rows={3}
                    readOnly
                    value={activeToken}
                    style={{ fontSize: '0.75rem', fontFamily: 'monospace' }}
                  />
                  <button className="btn btn-secondary" onClick={handleCopy} title="Copiar Token">
                    <Copy size={16} />
                    {copied ? 'Copiado!' : ''}
                  </button>
                </div>
              </div>

              <div style={{ marginTop: '16px' }}>
                <button className="btn btn-secondary btn-sm" onClick={() => handleValidate(activeToken)}>
                  Validar Firma & Claims con BFF
                </button>
              </div>

              {validationResult && (
                <div style={{ marginTop: '16px', padding: '12px', background: 'rgba(0, 0, 0, 0.3)', borderRadius: 'var(--radius-sm)', border: '1px solid var(--border-color)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                    <span className={`badge ${validationResult.valid ? 'badge-success' : 'badge-danger'}`}>
                      {validationResult.valid ? 'VÁLIDO' : 'INVÁLIDO'}
                    </span>
                    <span style={{ fontSize: '0.8rem', color: 'var(--text-secondary)' }}>
                      Firma HMAC-SHA256 Verificada
                    </span>
                  </div>

                  <pre style={{ fontSize: '0.75rem', color: '#93c5fd', whiteSpace: 'pre-wrap', wordBreak: 'break-all' }}>
                    {JSON.stringify(validationResult, null, 2)}
                  </pre>
                </div>
              )}
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '30px 10px', color: 'var(--text-muted)' }}>
              <AlertTriangle size={36} style={{ marginBottom: '10px', opacity: 0.6 }} />
              <p style={{ fontSize: '0.9rem' }}>No hay un token JWT activo en la sesión.</p>
              <p style={{ fontSize: '0.8rem', marginTop: '4px' }}>Haz clic en "Generar Token JWT" para simular un token de Azure AD.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
