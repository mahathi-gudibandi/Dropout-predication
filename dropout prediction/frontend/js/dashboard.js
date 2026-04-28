let allStudents = [];
let pieChart = null;
let scatterChart = null;

async function loadDashboard() {
    try {
        const res = await fetch(`${API_BASE}/students`, {
            headers: { Authorization: `Bearer ${getToken()}` }
        });
        if (!res.ok) { showTableError('Failed to load students.'); return; }
        allStudents = await res.json();
        renderSummary(allStudents);
        renderCharts(allStudents);
        renderTable(allStudents);
    } catch {
        showTableError('Cannot connect to server.');
    }
}

function renderSummary(students) {
    document.getElementById('totalCount').textContent = students.length;
    document.getElementById('lowCount').textContent = students.filter(s => s.dropoutRisk === 'LOW').length;
    document.getElementById('mediumCount').textContent = students.filter(s => s.dropoutRisk === 'MEDIUM').length;
    document.getElementById('highCount').textContent = students.filter(s => s.dropoutRisk === 'HIGH').length;
}

function renderCharts(students) {
    const low = students.filter(s => s.dropoutRisk === 'LOW').length;
    const medium = students.filter(s => s.dropoutRisk === 'MEDIUM').length;
    const high = students.filter(s => s.dropoutRisk === 'HIGH').length;

    // Pie Chart
    const pieCtx = document.getElementById('riskPieChart').getContext('2d');
    if (pieChart) pieChart.destroy();
    pieChart = new Chart(pieCtx, {
        type: 'doughnut',
        data: {
            labels: ['Low Risk', 'Medium Risk', 'High Risk'],
            datasets: [{
                data: [low, medium, high],
                backgroundColor: ['rgba(67,233,123,0.8)', 'rgba(249,202,36,0.8)', 'rgba(255,101,132,0.8)'],
                borderColor: ['#43e97b', '#f9ca24', '#ff6584'],
                borderWidth: 2
            }]
        },
        options: {
            responsive: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { color: 'rgba(255,255,255,0.7)', padding: 16, font: { size: 12 } }
                }
            },
            cutout: '65%'
        }
    });

    // Scatter Chart
    const colorMap = { LOW: 'rgba(67,233,123,0.8)', MEDIUM: 'rgba(249,202,36,0.8)', HIGH: 'rgba(255,101,132,0.8)' };
    const scatterCtx = document.getElementById('scatterChart').getContext('2d');
    if (scatterChart) scatterChart.destroy();

    scatterChart = new Chart(scatterCtx, {
        type: 'scatter',
        data: {
            datasets: ['LOW', 'MEDIUM', 'HIGH'].map(risk => ({
                label: `${risk} Risk`,
                data: students.filter(s => s.dropoutRisk === risk).map(s => ({ x: s.attendancePercentage, y: s.gpa })),
                backgroundColor: colorMap[risk],
                pointRadius: 7,
                pointHoverRadius: 9
            }))
        },
        options: {
            responsive: false,
            scales: {
                x: {
                    title: { display: true, text: 'Attendance (%)', color: 'rgba(255,255,255,0.5)' },
                    ticks: { color: 'rgba(255,255,255,0.5)' },
                    grid: { color: 'rgba(255,255,255,0.05)' }
                },
                y: {
                    title: { display: true, text: 'GPA', color: 'rgba(255,255,255,0.5)' },
                    ticks: { color: 'rgba(255,255,255,0.5)' },
                    grid: { color: 'rgba(255,255,255,0.05)' }
                }
            },
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: { color: 'rgba(255,255,255,0.7)', padding: 16, font: { size: 12 } }
                }
            }
        }
    });
}

function renderTable(students) {
    const tbody = document.getElementById('studentTableBody');
    if (students.length === 0) {
        tbody.innerHTML = '<tr><td colspan="10" class="loading">No students found.</td></tr>';
        return;
    }
    tbody.innerHTML = students.map((s, i) => `
        <tr>
            <td>${i + 1}</td>
            <td><a href="student-profile.html?id=${s.id}" class="student-link">${s.name}</a></td>
            <td>${s.attendancePercentage}%</td>
            <td>${s.gpa}</td>
            <td>${s.behaviorScore}/10</td>
            <td>${s.engagementLevel}/10</td>
            <td>${s.stressLevel}/10</td>
            <td>${s.feeStatus}</td>
            <td><span class="badge badge-${s.dropoutRisk?.toLowerCase()}">${riskEmoji(s.dropoutRisk)} ${s.dropoutRisk}</span></td>
            <td class="action-cell">
                <a href="student-profile.html?id=${s.id}" class="action-btn">👁️</a>
                <button class="action-btn" onclick="confirmDelete(${s.id})">🗑️</button>
            </td>
        </tr>
    `).join('');
}

function filterStudents() {
    const risk = document.getElementById('riskFilter').value;
    const search = document.getElementById('searchInput').value.toLowerCase();
    const filtered = allStudents.filter(s =>
        (risk === 'ALL' || s.dropoutRisk === risk) &&
        s.name.toLowerCase().includes(search)
    );
    renderTable(filtered);
}

async function confirmDelete(id) {
    if (!confirm('Delete this student?')) return;
    await fetch(`${API_BASE}/students/${id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    loadDashboard();
}

function exportCSV() {
    window.location.href = `${API_BASE}/students/export/csv?token=${getToken()}`;
}

// Trigger CSV export with auth header via fetch + blob
function downloadCSV() {
    fetch(`${API_BASE}/students/export/csv`, {
        headers: { Authorization: `Bearer ${getToken()}` }
    })
    .then(r => r.blob())
    .then(blob => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'students.csv';
        a.click();
        URL.revokeObjectURL(url);
    });
}

function openImport() {
    document.getElementById('importModal').style.display = 'flex';
}
function closeImport() {
    document.getElementById('importModal').style.display = 'none';
}

async function handleImport(event) {
    event.preventDefault();
    const file = document.getElementById('csvFile').files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append('file', file);

    const res = await fetch(`${API_BASE}/students/import`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${getToken()}` },
        body: formData
    });
    const result = await res.json();
    alert(`Import complete: ${result.imported} imported, ${result.failed} failed.`);
    closeImport();
    loadDashboard();
}

function showTableError(msg) {
    document.getElementById('studentTableBody').innerHTML =
        `<tr><td colspan="10" class="loading">${msg}</td></tr>`;
}

function riskEmoji(risk) { return { LOW: '🟢', MEDIUM: '🟡', HIGH: '🔴' }[risk] || ''; }
