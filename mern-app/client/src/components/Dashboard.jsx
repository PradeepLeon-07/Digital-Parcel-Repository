import { useEffect, useState } from 'react';
import { api } from '../api';
import Overview from './Overview';
import ParcelList from './ParcelList';
import StudentList from './StudentList';
import ParcelForm from './ParcelForm';
import StudentForm from './StudentForm';

const emptyParcel = { parcelId: '', studentRegisterNumber: '', courierName: '', senderName: '', receivedDate: new Date().toISOString().slice(0, 10), storageLocation: '' };
const emptyStudent = { registerNumber: '', name: '', department: '', year: '', phone: '', email: '', password: '' };

export default function Dashboard({ session, onLogout }) {
  const isStaff = ['ADMIN', 'SECURITY'].includes(session.role);
  const isAdmin = session.role === 'ADMIN';

  const [tab, setTab] = useState(isStaff ? 'dashboard' : 'mine');
  const [message, setMessage] = useState(null);
  const [dashboard, setDashboard] = useState(null);
  const [parcels, setParcels] = useState([]);
  const [students, setStudents] = useState([]);
  const [parcelForm, setParcelForm] = useState(emptyParcel);
  const [studentForm, setStudentForm] = useState(emptyStudent);
  const [filter, setFilter] = useState('');
  const [search, setSearch] = useState('');

  useEffect(() => {
    if (tab === 'dashboard') loadDashboard();
    if (tab === 'parcels') loadParcels();
    if (tab === 'students') loadStudents();
    if (tab === 'mine') loadMine();
  }, [tab]);

  async function run(action) {
    try { setMessage(null); await action(); } catch (err) { setMessage({ type: 'error', text: err.message }); }
  }

  async function loadDashboard() { setDashboard(await api('/dashboard')); }
  async function loadParcels() { setParcels(await api(filter ? `/parcels/status/${filter}` : '/parcels')); }
  async function loadMine() { setParcels(await api(`/parcels/student/${session.username}`)); }
  async function loadStudents() { setStudents(await api('/students')); }

  async function addParcel(event) {
    event.preventDefault();
    await run(async () => {
      await api('/parcels', { method: 'POST', body: JSON.stringify(parcelForm) });
      setParcelForm(emptyParcel);
      setMessage({ type: 'success', text: 'Parcel added.' });
    });
  }

  async function addStudent(event) {
    event.preventDefault();
    await run(async () => {
      await api('/students', { method: 'POST', body: JSON.stringify({ ...studentForm, year: Number(studentForm.year) }) });
      setStudentForm(emptyStudent);
      setMessage({ type: 'success', text: 'Student account created.' });
    });
  }

  async function updateParcel(id, body) {
    await run(async () => {
      await api(`/parcels/${id}`, { method: 'PUT', body: JSON.stringify(body) });
      loadParcels();
      loadDashboard();
    });
  }

  async function collect(id) {
    if (window.confirm('Mark this parcel as collected?')) await updateParcel(id, { status: 'COLLECTED' });
  }

  async function deleteParcel(id) {
    if (window.confirm('Delete this parcel?')) await run(async () => {
      await api(`/parcels/${id}`, { method: 'DELETE' });
      loadParcels();
      loadDashboard();
    });
  }

  const displayed = search ? parcels.filter(p => p.parcelId.toLowerCase().includes(search.toLowerCase())) : parcels;

  const tabs = isStaff
    ? [['dashboard', 'Overview'], ['parcels', 'All parcels'], ['addParcel', 'Add parcel'], ['students', 'Students'], ...(isAdmin ? [['addStudent', 'Add student']] : [])]
    : [['mine', 'My parcels']];

  return (
    <div className="app-shell">
      <header>
        <div>
          <p className="eyebrow">PARCEL DESK</p>
          <h1>Digital Parcel Repository</h1>
        </div>
        <div className="account">
          <span>{session.username} <b>{session.role}</b></span>
          <button className="ghost" onClick={onLogout}>Sign out</button>
        </div>
      </header>
      <nav>
        {tabs.map(([id, label]) => (
          <button key={id} className={tab === id ? 'active' : ''} onClick={() => { setMessage(null); setTab(id); }}>
            {label}
          </button>
        ))}
      </nav>
      <main className="content">
        {message && <div className={`alert ${message.type}`}>{message.text}</div>}
        {tab === 'dashboard' && <Overview data={dashboard} />}
        {tab === 'parcels' && <ParcelList parcels={displayed} filter={filter} setFilter={setFilter} search={search} setSearch={setSearch} onRefresh={loadParcels} onReady={id => updateParcel(id, { status: 'READY_FOR_COLLECTION' })} onCollect={collect} onDelete={deleteParcel} isAdmin={isAdmin} />}
        {tab === 'mine' && <ParcelList parcels={parcels} readOnly />}
        {tab === 'students' && <StudentList students={students} />}
        {tab === 'addParcel' && <ParcelForm form={parcelForm} setForm={setParcelForm} onSubmit={addParcel} />}
        {tab === 'addStudent' && <StudentForm form={studentForm} setForm={setStudentForm} onSubmit={addStudent} />}
      </main>
    </div>
  );
}
