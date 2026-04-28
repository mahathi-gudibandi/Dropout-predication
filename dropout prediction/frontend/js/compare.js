let allStudents = [];
let radarInstance = null;

async function initCompare() {
    const res = await fetch(`${API_BASE}/students`, {
        headers: { Authorization: `Bearer ${getToken()}` }
    });
    allStudents = await res.json();

    const selA = document.getElementById('selectA');
    const selB = document.getElementById('selectB');

    allStudents.forEach(s => {
        selA.innerHTML += `<option value="${s.id}">${s.name}</option>`;
        selB.innerHTML += `<option value="${s.id}">${s.name}</option>`;
    });
}

async function runCompare() {
    const idA = document.getElementById('selectA').value;
    const idB = document.getElementById('selectB').value;
    if (!idA || !idB || idA === idB) return;

    const sA = allStudents.find(s => s.id == idA);
    const sB = allStudents.find(s => s.id == idB);

    document.getElementById('comparePlaceholder').style.display = 'none';
    document.getElementById('compareResult').style.display = 'block';

    renderCompareCard('cardA', sA);
    renderCompareCard('cardB', sB);
    renderRadar(sA, sB);
}

function renderCompareCard(elId, s) {
    const el = document.getElementById(elId);
    const initials = s.name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2);
    el.innerHTML = `
        <div style="text-align:center; margin-bottom:1.25rem">
            <div class="profile-avatar" style="margin:0 auto 0.75rem">${initials}</div>
            <h3 style="font-size:1.1rem">${s.name}</h3>
            <span class="risk-badge ${s.dropoutRisk}" style="margin-top:0.5rem; display:inline-flex">
                ${riskEmoji(s.dropoutRisk)} ${s.dropoutRisk} RISK
            </span>
        </div>
        <div class="compare-stats">
            ${compareRow('Attendance', s.attendancePercentage + '%')}
            ${compareRow('GPA', s.gpa + ' / 10')}
            ${compareRow('Behavior', s.behaviorScore + ' / 10')}
            ${compareRow('Engagement', s.engagementLevel + ' / 10')}
            ${compareRow('Stress', s.stressLevel + ' / 10')}
            ${compareRow('Fee Status', s.feeStatus)}
            ${compareRow('Scholarship', s.scholarship ? '✅ Yes' : '❌ No')}
        </div>
        <a href="student-profile.html?id=${s.id}" class="btn btn-outline full-width" style="margin-top:1rem; font-size:0.85rem">View Full Profile</a>
    `;
}

function compareRow(label, value) {
    return `<div class="compare-row"><span class="compare-label">${label}</span><span class="compare-value">${value}</span></div>`;
}

function renderRadar(sA, sB) {
    const ctx = document.getElementById('radarChart').getContext('2d');
    if (radarInstance) radarInstance.destroy();

    radarInstance = new Chart(ctx, {
        type: 'radar',
        data: {
            labels: ['Attendance', 'GPA', 'Behavior', 'Engagement', 'Low Stress', 'Income Score'],
            datasets: [
                {
                    label: sA.name,
                    data: [
                        sA.attendancePercentage / 10,
                        sA.gpa,
                        sA.behaviorScore,
                        sA.engagementLevel,
                        10 - sA.stressLevel,
                        Math.min(sA.familyIncome / 10000, 10)
                    ],
                    borderColor: '#6c63ff',
                    backgroundColor: 'rgba(108,99,255,0.2)',
                    pointBackgroundColor: '#6c63ff'
                },
                {
                    label: sB.name,
                    data: [
                        sB.attendancePercentage / 10,
                        sB.gpa,
                        sB.behaviorScore,
                        sB.engagementLevel,
                        10 - sB.stressLevel,
                        Math.min(sB.familyIncome / 10000, 10)
                    ],
                    borderColor: '#ff6584',
                    backgroundColor: 'rgba(255,101,132,0.2)',
                    pointBackgroundColor: '#ff6584'
                }
            ]
        },
        options: {
            responsive: true,
            scales: {
                r: {
                    min: 0, max: 10,
                    ticks: { color: 'rgba(255,255,255,0.5)', backdropColor: 'transparent' },
                    grid: { color: 'rgba(255,255,255,0.1)' },
                    pointLabels: { color: 'rgba(255,255,255,0.7)', font: { size: 13 } },
                    angleLines: { color: 'rgba(255,255,255,0.1)' }
                }
            },
            plugins: { legend: { labels: { color: 'rgba(255,255,255,0.8)', font: { size: 13 } } } }
        }
    });
}

function riskEmoji(risk) { return { LOW: '🟢', MEDIUM: '🟡', HIGH: '🔴' }[risk] || ''; }
