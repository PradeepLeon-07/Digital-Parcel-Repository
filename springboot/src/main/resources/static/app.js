const API = '/api';

// ─── AUTH ────────────────────────────────────────────────────────────────────

function getToken() { return localStorage.getItem('token'); }
function getRole()  { return localStorage.getItem('role'); }
function getUser()  { return localStorage.getItem('username'); }

async function login() {
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();
    const errEl = document.getElementById('loginError');

    if (!username || !password) {
        showEl(errEl, 'Please enter username and password.');
        return;
    }

    try {
        const res = await fetch(`${API}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!res.ok) {
            showEl(errEl, 'Invalid username or password.');
            return;
        }

        const data = await res.json();
        localStorage.setItem('token', data.token);
        localStorage.setItem('username', data.username);
        localStorage.setItem('role', data.role);

        errEl.classList.add('hidden');
        showApp();
    } catch (e) {
        showEl(errEl, 'Cannot connect to server. Is the app running?');
    }
}

function logout() {
    localStorage.clear();
    document.getElementById('appPage').classList.add('hidden');
    document.getElementById('loginPage').classList.remove('hidden');
    document.getElementById('username').value = '';
    document.getElementById('password').value = '';
}

// ─── APP INIT ────────────────────────────────────────────────────────────────

function showApp() {
    document.getElementById('loginPage').classList.add('hidden');
    document.getElementById('appPage').classList.remove('hidden');

    const role = getRole();
    document.getElementById('navUser').textContent = `${getUser()} (${role.replace('ROLE_', '')})`;

    buildTabs(role);
}

function buildTabs(role) {
    const tabBar = document.getElementById('tabBar');
    tabBar.innerHTML = '';

    const isStaff = role === 'ROLE_ADMIN' || role === 'ROLE_SECURITY';
    const isAdmin = role === 'ROLE_ADMIN';

    const tabs = [];

    if (isStaff) {
        tabs.push({ id: 'dashboard', label: '📊 Dashboard', fn: loadDashboard });
        tabs.push({ id: 'parcels',   label: '📦 All Parcels', fn: loadParcels });
        tabs.push({ id: 'addParcel', label: '➕ Add Parcel', fn: () => {} });
        tabs.push({ id: 'students',  label: '🎓 Students', fn: loadStudents });
    }

    if (isAdmin) {
        tabs.push({ id: 'addStudent', label: '➕ Add Student', fn: () => {} });
    }

    if (role === 'ROLE_STUDENT') {
        tabs.push({ id: 'myParcels', label: '📦 My Parcels', fn: loadMyParcels });
    }

    tabs.forEach((tab, i) => {
        const btn = document.createElement('button');
        btn.className = 'tab-btn' + (i === 0 ? ' active' : '');
        btn.textContent = tab.label;
        btn.onclick = () => switchTab(tab.id, btn, tab.fn);
        tabBar.appendChild(btn);
    });

    // Show first tab
    if (tabs.length > 0) {
        showTabContent(tabs[0].id);
        tabs[0].fn();
    }
}

function switchTab(tabId, btn, fn) {
    document.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    showTabContent(tabId);
    fn();
}

function showTabContent(tabId) {
    document.querySelectorAll('.tab-content').forEach(el => el.classList.add('hidden'));
    document.getElementById('tab-' + tabId).classList.remove('hidden');
}

// ─── DASHBOARD ───────────────────────────────────────────────────────────────

async function loadDashboard() {
    const data = await apiFetch('/dashboard');
    if (!data) return;

    const cards = [
        { number: data.totalParcels,      label: 'Total Parcels' },
        { number: data.received,          label: 'Received' },
        { number: data.readyForCollection,label: 'Ready for Collection' },
        { number: data.collected,         label: 'Collected' },
        { number: data.receivedToday,     label: 'Received Today' },
        { number: data.collectedToday,    label: 'Collected Today' },
    ];

    document.getElementById('dashboardCards').innerHTML = cards.map(c => `
        <div class="card">
            <div class="number">${c.number}</div>
            <div class="label">${c.label}</div>
        </div>
    `).join('');
}

// ─── PARCELS ─────────────────────────────────────────────────────────────────

async function loadParcels() {
    document.getElementById('searchParcelId').value = '';
    document.getElementById('filterStatus').value = '';
    const parcels = await apiFetch('/parcels');
    renderParcelsTable(parcels, 'parcelsTable');
}

async function searchParcel() {
    const parcelId = document.getElementById('searchParcelId').value.trim();
    if (!parcelId) { loadParcels(); return; }
    const parcel = await apiFetch(`/parcels/search?parcelId=${encodeURIComponent(parcelId)}`);
    renderParcelsTable(parcel ? [parcel] : [], 'parcelsTable');
}

async function filterByStatus() {
    const status = document.getElementById('filterStatus').value;
    if (!status) { loadParcels(); return; }
    const parcels = await apiFetch(`/parcels/status/${status}`);
    renderParcelsTable(parcels, 'parcelsTable');
}

async function loadMyParcels() {
    const username = getUser();
    const parcels = await apiFetch(`/parcels/student/${username}`);
    renderParcelsTable(parcels, 'myParcelsTable', true);
}

function renderParcelsTable(parcels, containerId, readOnly = false) {
    const el = document.getElementById(containerId);
    if (!parcels || parcels.length === 0) {
        el.innerHTML = '<div class="empty-msg">No parcels found.</div>';
        return;
    }

    const isAdmin = getRole() === 'ROLE_ADMIN';
    const isStaff = getRole() === 'ROLE_ADMIN' || getRole() === 'ROLE_SECURITY';

    const rows = parcels.map(p => `
        <tr>
            <td>${p.parcelId}</td>
            <td>${p.studentName}<br><small style="color:#888">${p.studentRegisterNumber}</small></td>
            <td>${p.courierName}</td>
            <td>${p.senderName || '-'}</td>
            <td>${p.receivedDate}</td>
            <td>${p.storageLocation || '-'}</td>
            <td><span class="badge ${badgeClass(p.status)}">${formatStatus(p.status)}</span></td>
            <td>${p.collectedDate || '-'}</td>
            ${!readOnly && isStaff ? `<td>${actionButtons(p, isAdmin)}</td>` : ''}
        </tr>
    `).join('');

    const actionHeader = !readOnly && isStaff ? '<th>Actions</th>' : '';

    el.innerHTML = `
        <div class="table-wrap">
        <table>
            <thead><tr>
                <th>Parcel ID</th><th>Student</th><th>Courier</th><th>Sender</th>
                <th>Received</th><th>Location</th><th>Status</th><th>Collected</th>
                ${actionHeader}
            </tr></thead>
            <tbody>${rows}</tbody>
        </table>
        </div>
    `;
}

function actionButtons(p, isAdmin) {
    let btns = '';
    if (p.status !== 'COLLECTED') {
        btns += `<button class="btn-action btn-collect" onclick="collectParcel(${p.id})">Collect</button>`;
    }
    if (p.status === 'RECEIVED') {
        btns += `<button class="btn-action btn-ready" onclick="markReady(${p.id})">Mark Ready</button>`;
    }
    if (isAdmin) {
        btns += `<button class="btn-action btn-delete" onclick="deleteParcel(${p.id})">Delete</button>`;
    }
    return btns;
}

async function collectParcel(id) {
    if (!confirm('Mark this parcel as collected?')) return;
    const res = await apiFetch(`/parcels/${id}/collect`, 'POST');
    if (res) { alert('Parcel marked as collected!'); loadParcels(); loadDashboard(); }
}

async function markReady(id) {
    const res = await apiFetch(`/parcels/${id}`, 'PUT', { status: 'READY_FOR_COLLECTION' });
    if (res) { loadParcels(); }
}

async function deleteParcel(id) {
    if (!confirm('Delete this parcel? This cannot be undone.')) return;
    await apiFetchNoBody(`/parcels/${id}`, 'DELETE');
    loadParcels();
    loadDashboard();
}

async function addParcel() {
    const msgEl = document.getElementById('addParcelMsg');
    const body = {
        parcelId:              document.getElementById('newParcelId').value.trim(),
        studentRegisterNumber: document.getElementById('newRegisterNumber').value.trim(),
        courierName:           document.getElementById('newCourierName').value.trim(),
        senderName:            document.getElementById('newSenderName').value.trim(),
        receivedDate:          document.getElementById('newReceivedDate').value,
        storageLocation:       document.getElementById('newStorageLocation').value.trim()
    };

    if (!body.parcelId || !body.studentRegisterNumber || !body.courierName || !body.receivedDate) {
        showMsg(msgEl, 'Please fill in all required fields.', false);
        return;
    }

    const res = await apiFetch('/parcels', 'POST', body);
    if (res) {
        showMsg(msgEl, `Parcel "${res.parcelId}" added successfully!`, true);
        document.querySelectorAll('#tab-addParcel input').forEach(i => i.value = '');
    } else {
        showMsg(msgEl, 'Failed to add parcel. Check the register number and parcel ID.', false);
    }
}

// ─── STUDENTS ────────────────────────────────────────────────────────────────

async function loadStudents() {
    document.getElementById('searchRegNum').value = '';
    const students = await apiFetch('/students');
    renderStudentsTable(students);
}

async function searchStudent() {
    const regNum = document.getElementById('searchRegNum').value.trim();
    if (!regNum) { loadStudents(); return; }
    const student = await apiFetch(`/students/${regNum}`);
    renderStudentsTable(student ? [student] : []);
}

function renderStudentsTable(students) {
    const el = document.getElementById('studentsTable');
    if (!students || students.length === 0) {
        el.innerHTML = '<div class="empty-msg">No students found.</div>';
        return;
    }

    const rows = students.map(s => `
        <tr>
            <td>${s.registerNumber}</td>
            <td>${s.name}</td>
            <td>${s.department}</td>
            <td>${s.year}</td>
            <td>${s.phone}</td>
            <td>${s.email}</td>
        </tr>
    `).join('');

    el.innerHTML = `
        <div class="table-wrap">
        <table>
            <thead><tr>
                <th>Register No.</th><th>Name</th><th>Department</th>
                <th>Year</th><th>Phone</th><th>Email</th>
            </tr></thead>
            <tbody>${rows}</tbody>
        </table>
        </div>
    `;
}

async function addStudent() {
    const msgEl = document.getElementById('addStudentMsg');
    const body = {
        registerNumber: document.getElementById('sRegisterNumber').value.trim(),
        name:           document.getElementById('sName').value.trim(),
        department:     document.getElementById('sDepartment').value.trim(),
        year:           parseInt(document.getElementById('sYear').value),
        phone:          document.getElementById('sPhone').value.trim(),
        email:          document.getElementById('sEmail').value.trim(),
        password:       document.getElementById('sPassword').value.trim()
    };

    if (!body.registerNumber || !body.name || !body.department || !body.year || !body.phone || !body.email || !body.password) {
        showMsg(msgEl, 'Please fill in all fields.', false);
        return;
    }

    const res = await apiFetch('/students', 'POST', body);
    if (res) {
        showMsg(msgEl, `Student "${res.name}" added successfully!`, true);
        document.querySelectorAll('#tab-addStudent input').forEach(i => i.value = '');
    } else {
        showMsg(msgEl, 'Failed to add student. Register number or email may already exist.', false);
    }
}

// ─── HELPERS ─────────────────────────────────────────────────────────────────

async function apiFetch(path, method = 'GET', body = null) {
    try {
        const opts = {
            method,
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getToken()}`
            }
        };
        if (body) opts.body = JSON.stringify(body);
        const res = await fetch(API + path, opts);
        if (res.status === 204) return true;
        if (!res.ok) return null;
        return await res.json();
    } catch (e) {
        return null;
    }
}

async function apiFetchNoBody(path, method) {
    try {
        await fetch(API + path, {
            method,
            headers: { 'Authorization': `Bearer ${getToken()}` }
        });
    } catch (e) {}
}

function badgeClass(status) {
    if (status === 'RECEIVED') return 'badge-received';
    if (status === 'READY_FOR_COLLECTION') return 'badge-ready';
    if (status === 'COLLECTED') return 'badge-collected';
    return '';
}

function formatStatus(status) {
    return status.replace(/_/g, ' ');
}

function showEl(el, msg) {
    el.textContent = msg;
    el.classList.remove('hidden');
}

function showMsg(el, msg, success) {
    el.textContent = msg;
    el.className = success ? 'success-msg' : 'error-msg';
}

// Auto-login if token exists in localStorage
window.onload = () => {
    if (getToken()) showApp();
};
