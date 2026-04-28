let currentStudent = null;
let trendChartInstance = null;

function getStudentId() {
    return new URLSearchParams(window.location.search).get('id');
}

async function loadProfile() {
    const id = getStudentId();
    if (!id) { window.location.href = 'dashboard.html'; return; }

    const [student, history, notes, pred] = await Promise.all([
        apiFetch(`/students/${id}`),
        apiFetch(`/students/${id}/history`),
        apiFetch(`/students/${id}/notes`),
        apiPost('/predict', await apiFetch(`/students/${id}`))
    ]);

    currentStudent = student;

    renderHero(student);
    renderStats(student);
    renderTrend(history);
    renderNotes(notes);
    renderInterventions(pred.interventions);

    document.getElementById('profileHero').style.display = 'block';
    document.getElementById('profileMain').style.display = 'block';
}

function renderHero(s) {
    const initials = s.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
    document.getElementById('profileAvatar').textContent = initials;
    document.getElementById('profileName').textContent = s.name;

    const badge = document.getElementById('profileRiskBadge');
    badge.textContent = `${riskEmoji(s.dropoutRisk)} ${s.dropoutRisk} RISK`;
    badge.className = `risk-badge ${s.dropoutRisk}`;

    document.getElementById('profileMeta').innerHTML = `
        <div class="meta-chip">📅 Attendance: ${s.attendancePercentage}%</div>
        <div class="meta-chip">📖 GPA: ${s.gpa}/10</div>
        <div class="meta-chip">💰 ${s.feeStatus}</div>
        <div class="meta-chip">${s.scholarship ? '🏅 Scholarship' : '❌ No Scholarship'}</div>
        ${s.counseling ? '<div class="meta-chip">🧘 Counseling</div>' : ''}
    `;
}

function renderStats(s) {
    setStatCard('statAttendance', 'barAttendance', s.attendancePercentage + '%', s.attendancePercentage);
    setStatCard('statGpa', 'barGpa', s.gpa + ' / 10', s.gpa * 10);
    setStatCard('statBehavior', 'barBehavior', s.behaviorScore + ' / 10', s.behaviorScore * 10);
    setStatCard('statEngagement', 'barEngagement', s.engagementLevel + ' / 10', s.engagementLevel * 10);
    setStatCard('statStress', 'barStress', s.stressLevel + ' / 10', s.stressLevel * 10);

    document.getElementById('statIncome').textContent = '$' + s.familyIncome.toLocaleString();
    document.getElementById('statFee').textContent = s.feeStatus === 'PAID' ? '✅ Paid' : '⚠️ Pending';
    document.getElementById('statScholarship').textContent = s.scholarship ? '✅ Yes' : '❌ No';
}

function setStatCard(valId, barId, text, pct) {
    document.getElementById(valId).textContent = text;
    setTimeout(() => {
        const bar = document.getElementById(barId);
        if (bar) bar.style.width = Math.min(pct, 100) + '%';
    }, 300);
}

function renderTrend(history) {
    if (!history || history.length === 0) return;
    const labels = history.map((h, i) => `Record ${i + 1}`);
    const riskMap = { LOW: 1, MEDIUM: 2, HIGH: 3 };
    const riskColors = { LOW: '#43e97b', MEDIUM: '#f9ca24', HIGH: '#ff6584' };

    const ctx = document.getElementById('trendChart').getContext('2d');
    if (trendChartInstance) trendChartInstance.destroy();

    trendChartInstance = new Chart(ctx, {
        type: 'line',
        data: {
            labels,
            datasets: [
                {
                    label: 'GPA',
                    data: history.map(h => h.gpa),
                    borderColor: '#6c63ff',
                    backgroundColor: 'rgba(108,99,255,0.08)',
                    tension: 0.4, fill: true, yAxisID: 'y',
                    pointBackgroundColor: '#6c63ff',
                    pointRadius: 5, pointHoverRadius: 8
                },
                {
                    label: 'Attendance %',
                    data: history.map(h => h.attendancePercentage),
                    borderColor: '#43e97b',
                    backgroundColor: 'rgba(67,233,123,0.06)',
                    tension: 0.4, fill: true, yAxisID: 'y1',
                    pointBackgroundColor: '#43e97b',
                    pointRadius: 5, pointHoverRadius: 8
                },
                {
                    label: 'Risk Level',
                    data: history.map(h => riskMap[h.dropoutRisk] || 0),
                    borderColor: '#ff6584',
                    backgroundColor: 'transparent',
                    tension: 0.4, fill: false, yAxisID: 'y2',
                    pointBackgroundColor: history.map(h => riskColors[h.dropoutRisk] || '#fff'),
                    pointRadius: 7, pointHoverRadius: 10,
                    borderDash: [6, 3]
                }
            ]
        },
        options: {
            responsive: true,
            interaction: { mode: 'index', intersect: false },
            plugins: {
                legend: {
                    labels: { color: 'rgba(255,255,255,0.6)', font: { size: 12 }, padding: 20 }
                }
            },
            scales: {
                x: {
                    ticks: { color: 'rgba(255,255,255,0.4)', font: { size: 11 } },
                    grid: { color: 'rgba(255,255,255,0.04)' }
                },
                y: {
                    position: 'left',
                    title: { display: true, text: 'GPA', color: 'rgba(255,255,255,0.4)', font: { size: 11 } },
                    ticks: { color: 'rgba(255,255,255,0.4)' },
                    grid: { color: 'rgba(255,255,255,0.04)' }
                },
                y1: {
                    position: 'right',
                    title: { display: true, text: 'Attendance %', color: 'rgba(255,255,255,0.4)', font: { size: 11 } },
                    ticks: { color: 'rgba(255,255,255,0.4)' },
                    grid: { drawOnChartArea: false }
                },
                y2: { display: false, min: 0, max: 4 }
            }
        }
    });
}

function renderNotes(notes) {
    const list = document.getElementById('notesList');
    if (!notes || notes.length === 0) {
        list.innerHTML = '<div class="no-notes">📭 No notes yet. Add the first one above.</div>';
        return;
    }
    list.innerHTML = notes.map(n => `
        <div class="note-item">
            <div class="note-text">${escapeHtml(n.note)}</div>
            <div class="note-meta">
                <span class="note-author">👤 ${n.addedBy}</span>
                <span>🕐 ${new Date(n.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}</span>
                <button class="note-delete" onclick="deleteNote(${n.id})" title="Delete note">🗑️</button>
            </div>
        </div>
    `).join('');
}

function renderInterventions(interventions) {
    document.getElementById('profileInterventions').innerHTML =
        interventions.map(i => `<div class="intervention-item">💡 ${i}</div>`).join('');
}

async function addNote() {
    const input = document.getElementById('noteInput');
    const text = input.value.trim();
    if (!text) return;
    await apiPost(`/students/${getStudentId()}/notes`, { note: text });
    input.value = '';
    renderNotes(await apiFetch(`/students/${getStudentId()}/notes`));
}

async function deleteNote(noteId) {
    await apiDelete(`/notes/${noteId}`);
    renderNotes(await apiFetch(`/students/${getStudentId()}/notes`));
}

function openEditModal() {
    const s = currentStudent;
    document.getElementById('editName').value = s.name;
    document.getElementById('editAttendance').value = s.attendancePercentage;
    document.getElementById('editGpa').value = s.gpa;
    document.getElementById('editBehavior').value = s.behaviorScore;
    document.getElementById('editEngagement').value = s.engagementLevel;
    document.getElementById('editStress').value = s.stressLevel;
    document.getElementById('editIncome').value = s.familyIncome;
    document.getElementById('editFeeStatus').value = s.feeStatus;
    document.getElementById('editScholarship').value = String(s.scholarship);
    document.getElementById('editCounseling').value = String(s.counseling);
    document.getElementById('editModal').style.display = 'flex';
}

function closeEditModal() {
    document.getElementById('editModal').style.display = 'none';
}

async function saveEdit(event) {
    event.preventDefault();
    const updated = {
        name: document.getElementById('editName').value,
        attendancePercentage: parseFloat(document.getElementById('editAttendance').value),
        gpa: parseFloat(document.getElementById('editGpa').value),
        behaviorScore: parseInt(document.getElementById('editBehavior').value),
        engagementLevel: parseInt(document.getElementById('editEngagement').value),
        stressLevel: parseInt(document.getElementById('editStress').value),
        familyIncome: parseFloat(document.getElementById('editIncome').value),
        feeStatus: document.getElementById('editFeeStatus').value,
        scholarship: document.getElementById('editScholarship').value === 'true',
        counseling: document.getElementById('editCounseling').value === 'true'
    };
    await apiPut(`/students/${getStudentId()}`, updated);
    closeEditModal();
    loadProfile();
}

async function deleteStudent() {
    if (!confirm('Delete this student? This cannot be undone.')) return;
    await apiDelete(`/students/${getStudentId()}`);
    window.location.href = 'dashboard.html';
}

// ===== API helpers =====
async function apiFetch(path) {
    const res = await fetch(`${API_BASE}${path}`, { headers: { Authorization: `Bearer ${getToken()}` } });
    return res.json();
}
async function apiPost(path, body) {
    const res = await fetch(`${API_BASE}${path}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${getToken()}` },
        body: JSON.stringify(body)
    });
    return res.json();
}
async function apiPut(path, body) {
    const res = await fetch(`${API_BASE}${path}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${getToken()}` },
        body: JSON.stringify(body)
    });
    return res.json();
}
async function apiDelete(path) {
    await fetch(`${API_BASE}${path}`, { method: 'DELETE', headers: { Authorization: `Bearer ${getToken()}` } });
}

function riskEmoji(risk) { return { LOW: '🟢', MEDIUM: '🟡', HIGH: '🔴' }[risk] || ''; }
function escapeHtml(str) { return str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;'); }
