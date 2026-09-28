import { useState } from 'react';
import Login from './components/Login';
import Dashboard from './components/Dashboard';

export default function App() {
  const [session, setSession] = useState(() =>
    localStorage.getItem('token')
      ? { username: localStorage.getItem('username'), role: localStorage.getItem('role') }
      : null
  );

  if (!session) return <Login onLogin={setSession} />;

  function logout() { localStorage.clear(); setSession(null); }

  return <Dashboard session={{ ...session, role: session.role.replace('ROLE_', '') }} onLogout={logout} />;
}
