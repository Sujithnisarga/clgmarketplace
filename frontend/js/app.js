const API = 'http://localhost:8080/api';
const WS_URL = 'http://localhost:8080/ws';

// Auth helpers
const getToken = () => localStorage.getItem('token');
const getUser = () => JSON.parse(localStorage.getItem('user') || 'null');
const setAuth = (token, user) => { localStorage.setItem('token', token); localStorage.setItem('user', JSON.stringify(user)); };
const clearAuth = () => { localStorage.removeItem('token'); localStorage.removeItem('user'); };
const isLoggedIn = () => !!getToken();

// API fetch wrapper
async function apiFetch(path, options = {}) {
  const token = getToken();
  const headers = { ...(options.headers || {}) };
  if (token) headers['Authorization'] = `Bearer ${token}`;
  if (!(options.body instanceof FormData)) headers['Content-Type'] = 'application/json';

  const res = await fetch(API + path, { ...options, headers });
  if (res.status === 401) { clearAuth(); window.location.href = '/pages/login.html'; return; }
  const data = res.headers.get('content-type')?.includes('json') ? await res.json() : {};
  if (!res.ok) throw new Error(data.message || 'Request failed');
  return data;
}

// Toast notifications
function showToast(title, message, type = 'info') {
  let container = document.getElementById('toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'toast-container';
    document.body.appendChild(container);
  }
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.innerHTML = `<div><div class="toast-title">${title}</div><div class="toast-msg">${message}</div></div>`;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}

// Countdown timer
function startCountdown(endTime, el, onEnd) {
  const end = new Date(endTime).getTime();
  function tick() {
    const now = Date.now();
    const diff = end - now;
    if (diff <= 0) {
      el.textContent = 'ENDED';
      el.className = el.className.replace('urgent', '') + ' ended';
      if (onEnd) onEnd();
      return;
    }
    const h = Math.floor(diff / 3600000);
    const m = Math.floor((diff % 3600000) / 60000);
    const s = Math.floor((diff % 60000) / 1000);
    el.textContent = `${String(h).padStart(2,'0')}:${String(m).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
    if (diff < 60000) el.classList.add('urgent');
    setTimeout(tick, 1000);
  }
  tick();
}

// Format currency
const fmt = (n) => n != null ? `₹${parseFloat(n).toFixed(2)}` : '—';

// Format date
const fmtDate = (d) => d ? new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : '';

// Render navbar
function renderNavbar(activePage = '') {
  const user = getUser();
  const nav = document.getElementById('navbar');
  if (!nav) return;
  nav.innerHTML = `
    <a href="/index.html" class="logo">College<span>Market</span></a>
    <div class="nav-links">
      <a href="/index.html" class="${activePage === 'home' ? 'active' : ''}">Browse</a>
      <a href="/pages/auctions.html" class="${activePage === 'auctions' ? 'active' : ''}">Auctions</a>
      ${user ? `<a href="/pages/sell.html" class="${activePage === 'sell' ? 'active' : ''}">+ Sell</a>` : ''}
    </div>
    <div class="nav-user">
      ${user ? `
        <a href="/pages/dashboard.html" class="${activePage === 'dashboard' ? 'active' : ''}" style="text-decoration:none">
          ${user.avatar
            ? `<img src="http://localhost:8080${user.avatar}" class="nav-avatar" alt="${user.name}">`
            : `<div class="nav-avatar-placeholder">${user.name[0].toUpperCase()}</div>`}
        </a>
        <button class="btn btn-secondary btn-sm" onclick="logout()">Logout</button>
      ` : `
        <a href="/pages/login.html" class="btn btn-secondary btn-sm">Login</a>
        <a href="/pages/register.html" class="btn btn-primary btn-sm">Sign Up</a>
      `}
    </div>
  `;
}

function logout() {
  clearAuth();
  window.location.href = '/index.html';
}

// Require auth guard
function requireAuth() {
  if (!isLoggedIn()) { window.location.href = '/pages/login.html'; return false; }
  return true;
}

// Render item card
function renderItemCard(item) {
  const isAuction = item.listingType === 'AUCTION';
  const img = item.images?.[0] ? `<img src="http://localhost:8080${item.images[0]}" alt="${item.title}" loading="lazy">` : `<div class="no-img">📦</div>`;
  const badge = isAuction
    ? `<span class="badge badge-auction">Auction</span>`
    : `<span class="badge badge-fixed">Fixed</span>`;
  const statusBadge = item.status !== 'ACTIVE'
    ? `<span class="badge badge-${item.status.toLowerCase()}">${item.status}</span>` : '';
  const price = isAuction
    ? `<span class="price">${fmt(item.currentBid)}</span>`
    : `<span class="price">${fmt(item.price)}</span>`;
  const timer = isAuction && item.status === 'ACTIVE'
    ? `<div class="countdown" id="timer-${item.id}">--:--:--</div>` : '';

  return `
    <div class="item-card" onclick="window.location.href='/pages/item.html?id=${item.id}'">
      ${img}
      <div class="item-card-body">
        <div class="item-card-title">${item.title}</div>
        <div style="color:var(--text-muted);font-size:0.8rem;margin-bottom:0.5rem">${item.category}</div>
        <div class="item-card-meta">
          ${price}
          <div style="display:flex;gap:0.4rem;align-items:center">${badge}${statusBadge}</div>
        </div>
        ${timer}
      </div>
    </div>
  `;
}

// Init timers after rendering cards
function initCardTimers(items) {
  items.forEach(item => {
    if (item.listingType === 'AUCTION' && item.status === 'ACTIVE' && item.endTime) {
      const el = document.getElementById(`timer-${item.id}`);
      if (el) startCountdown(item.endTime, el, () => {
        el.closest('.item-card').querySelector('.badge-auction')?.replaceWith(
          Object.assign(document.createElement('span'), { className: 'badge badge-ended', textContent: 'Ended' })
        );
      });
    }
  });
}

// Stars renderer
function renderStars(rating) {
  const full = Math.round(rating);
  return '★'.repeat(full) + '☆'.repeat(5 - full);
}
