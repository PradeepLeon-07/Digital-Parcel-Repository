import { useState } from 'react';
import { api } from '../api';

export default function Login({ onLogin }) {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');

  async function submit(event) {
    event.preventDefault();
    setError('');
    try {
      const result = await api('/auth/login', { method: 'POST', body: JSON.stringify(form) });
      localStorage.setItem('token', result.token);
      localStorage.setItem('username', result.username);
      localStorage.setItem('role', result.role);
      onLogin(result);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <main className="login-page">
      <section className="login-panel">
        <p className="eyebrow">COLLEGE OPERATIONS</p>
        <h1>Digital Parcel Repository</h1>
        <p className="muted">A clear chain of custody for every delivery.</p>
        {error && <div className="alert error">{error}</div>}
        <form onSubmit={submit}>
          <label>
            Username
            <input required value={form.username} onChange={e => setForm({ ...form, username: e.target.value })} />
          </label>
          <label>
            Password
            <input required type="password" value={form.password} onChange={e => setForm({ ...form, password: e.target.value })} />
          </label>
          <button className="primary wide">Sign in</button>
        </form>
        <p className="hint">Local demo: admin / admin123</p>
      </section>
    </main>
  );
}
