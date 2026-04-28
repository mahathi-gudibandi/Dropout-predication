const API_BASE = 'http://localhost:8081/api';

// ===== Token helpers =====
function getToken() { return localStorage.getItem('token'); }
function getUsername() { return localStorage.getItem('username'); }
function isLoggedIn() { return !!getToken(); }

function saveAuth(token, username) {
    localStorage.setItem('token', token);
    localStorage.setItem('username', username);
}

function clearAuth() {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
}

// ===== Redirect if not authenticated =====
function requireAuth() {
    if (!isLoggedIn()) {
        window.location.href = 'login.html';
    }
}

// ===== Logout =====
function logout() {
    clearAuth();
    window.location.href = 'login.html';
}

// ===== Update nav visibility =====
function updateNav() {
    const loggedIn = isLoggedIn();
    const navLogin = document.getElementById('navLogin');
    const navLogout = document.getElementById('navLogout');
    const navAddStudent = document.getElementById('navAddStudent');
    const navDashboard = document.getElementById('navDashboard');
    const heroDashboard = document.getElementById('heroDashboard');

    if (navLogin) navLogin.style.display = loggedIn ? 'none' : 'block';
    if (navLogout) navLogout.style.display = loggedIn ? 'block' : 'none';
    if (navAddStudent) navAddStudent.style.display = loggedIn ? 'block' : 'none';
    if (navDashboard) navDashboard.style.display = loggedIn ? 'block' : 'none';
    if (heroDashboard) heroDashboard.style.display = loggedIn ? 'inline-block' : 'none';
}

// ===== Login handler =====
async function handleLogin(event) {
    event.preventDefault();
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value;
    const errorEl = document.getElementById('loginError');
    errorEl.textContent = '';

    try {
        const res = await fetch(`${API_BASE}/auth/login`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!res.ok) {
            const msg = await res.text();
            errorEl.textContent = msg || 'Login failed. Check your credentials.';
            return;
        }

        const data = await res.json();
        saveAuth(data.token, data.username);
        window.location.href = 'dashboard.html';
    } catch (err) {
        errorEl.textContent = 'Cannot connect to server. Make sure the backend is running.';
    }
}

// ===== Signup handler =====
async function handleSignup(event) {
    event.preventDefault();
    const username = document.getElementById('signupUsername').value.trim();
    const password = document.getElementById('signupPassword').value;
    const errorEl = document.getElementById('signupError');
    const successEl = document.getElementById('signupSuccess');
    errorEl.textContent = '';
    successEl.textContent = '';

    try {
        const res = await fetch(`${API_BASE}/auth/signup`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        if (!res.ok) {
            const msg = await res.text();
            errorEl.textContent = msg || 'Signup failed.';
            return;
        }

        const data = await res.json();
        saveAuth(data.token, data.username);
        window.location.href = 'dashboard.html';
    } catch (err) {
        errorEl.textContent = 'Cannot connect to server. Make sure the backend is running.';
    }
}
