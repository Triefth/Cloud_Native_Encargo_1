import React, { useState } from 'react';
import { Activity, ArrowRight, LockKeyhole, ShieldCheck, Stethoscope } from 'lucide-react';
import { authApi, setAuthToken } from '../services/api';

export default function Login({ onLogin }) {
  const [user, setUser] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');

    if (!user.trim() || !password) {
      setError('Ingresa tu correo y contraseña para continuar.');
      return;
    }

    setLoading(true);
    const response = await authApi.login(user.trim(), password);
    setLoading(false);

    if (response.error || !response.data?.token) {
      setError(response.message || 'No fue posible iniciar sesión.');
      return;
    }

    setAuthToken(response.data.token);
    onLogin(response.data.token);
  };

  return (
    <main className="login-page">
      <section className="login-shell">
        <div className="login-intro">
          <div className="login-brand-mark">
            <Stethoscope size={30} />
          </div>
          <span className="badge badge-info"><Activity size={14} /> Plataforma Cloud Native</span>
          <h1>Telemedicina <span className="text-gradient">Rural</span></h1>
          <p>
            Accede al centro de operaciones para coordinar atenciones médicas,
            revisar fichas y supervisar tus microservicios.
          </p>
          <div className="login-feature-list">
            <div><ShieldCheck size={18} /> Sesión protegida con JWT</div>
            <div><LockKeyhole size={18} /> Gateway BFF con validación centralizada</div>
          </div>
        </div>

        <form className="glass-card login-card" onSubmit={handleSubmit}>
          <div className="login-card-heading">
            <div>
              <p className="eyebrow">Acceso de demostración</p>
              <h2>Iniciar sesión</h2>
            </div>
            <ShieldCheck size={28} style={{ color: 'var(--accent-teal)' }} />
          </div>

          <p className="login-helper">
            Ingresa tus credenciales para acceder a tu sesión protegida con JWT.
          </p>

          <div className="form-group">
            <label className="form-label" htmlFor="login-user">Correo del usuario</label>
            <input
              id="login-user"
              type="email"
              className="form-input"
              value={user}
              onChange={(event) => setUser(event.target.value)}
              placeholder="nombre@telemedicina.cl"
              autoComplete="email"
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label" htmlFor="login-password">Contraseña</label>
            <input
              id="login-password"
              type="password"
              className="form-input"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Ingresa tu contraseña"
              autoComplete="current-password"
              required
            />
          </div>

          {error && <p className="login-error" role="alert">{error}</p>}

          <button className="btn btn-primary login-submit" type="submit" disabled={loading}>
            {loading ? 'Validando acceso...' : 'Entrar al centro de operaciones'}
            <ArrowRight size={17} />
          </button>

          <p className="login-footnote">Acceso de desarrollo · sesión válida por 24 horas</p>
        </form>
      </section>
    </main>
  );
}