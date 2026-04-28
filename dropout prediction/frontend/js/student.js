// ===== Add Student & Predict =====
async function handleAddStudent(event) {
    event.preventDefault();
    const errorEl = document.getElementById('formError');
    errorEl.textContent = '';

    const student = {
        name: document.getElementById('name').value.trim(),
        attendancePercentage: parseFloat(document.getElementById('attendance').value),
        gpa: parseFloat(document.getElementById('gpa').value),
        behaviorScore: parseInt(document.getElementById('behaviorScore').value),
        engagementLevel: parseInt(document.getElementById('engagementLevel').value),
        stressLevel: parseInt(document.getElementById('stressLevel').value),
        counseling: document.getElementById('counseling').value === 'true',
        familyIncome: parseFloat(document.getElementById('familyIncome').value),
        scholarship: document.getElementById('scholarship').value === 'true',
        feeStatus: document.getElementById('feeStatus').value,
        parentEmail: document.getElementById('parentEmail').value.trim() || null,
        parentPhone: document.getElementById('parentPhone').value.trim() || null
    };

    try {
        const res = await fetch(`${API_BASE}/addStudent`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getToken()}`
            },
            body: JSON.stringify(student)
        });

        if (!res.ok) {
            errorEl.textContent = 'Failed to add student. Please check your inputs.';
            return;
        }

        const saved = await res.json();
        showResult(saved);
    } catch (err) {
        errorEl.textContent = 'Cannot connect to server.';
    }
}

function showResult(student) {
    const panel = document.getElementById('resultPanel');
    const badge = document.getElementById('riskBadge');
    const score = document.getElementById('riskScore');
    const list = document.getElementById('interventionList');

    badge.textContent = `${riskEmoji(student.dropoutRisk)} ${student.dropoutRisk} RISK`;
    badge.className = `risk-badge ${student.dropoutRisk}`;

    // Fetch full prediction for interventions
    fetch(`${API_BASE}/predict`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${getToken()}`
        },
        body: JSON.stringify(student)
    })
    .then(r => r.json())
    .then(pred => {
        score.textContent = `Risk Score: ${(pred.riskScore * 100).toFixed(1)}%`;
        list.innerHTML = pred.interventions
            .map(i => `<div class="intervention-item">💡 ${i}</div>`)
            .join('');
        panel.style.display = 'block';
        panel.scrollIntoView({ behavior: 'smooth' });
    });
}

function riskEmoji(risk) {
    return { LOW: '🟢', MEDIUM: '🟡', HIGH: '🔴' }[risk] || '';
}
