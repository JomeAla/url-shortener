document.addEventListener('DOMContentLoaded', () => {
  const urlInput = document.getElementById('urlInput');
  const shortenBtn = document.getElementById('shortenBtn');
  const status = document.getElementById('status');
  const recentList = document.getElementById('recentList');
  const clearBtn = document.getElementById('clearBtn');
  const openSettings = document.getElementById('openSettings');

  loadRecentLinks();

  urlInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') handleShorten();
  });
  shortenBtn.addEventListener('click', handleShorten);
  clearBtn.addEventListener('click', handleClear);
  openSettings.addEventListener('click', () => chrome.runtime.openOptionsPage());

  async function handleShorten() {
    const url = urlInput.value.trim();
    if (!url) { showStatus('Please enter a URL', 'error'); return; }
    shortenBtn.disabled = true;
    shortenBtn.textContent = '...';
    showStatus('Shortening...', 'info');
    try {
      const response = await chrome.runtime.sendMessage({ action: 'shorten', url });
      if (response.success) {
        showStatus(`Copied: ${response.shortUrl}`, 'success');
        try { await navigator.clipboard.writeText(response.shortUrl); } catch {}
        urlInput.value = '';
        loadRecentLinks();
      } else {
        showStatus(response.error || 'Failed to shorten', 'error');
      }
    } catch (e) {
      showStatus(e.message, 'error');
    }
    shortenBtn.disabled = false;
    shortenBtn.textContent = 'Shorten';
  }

  async function loadRecentLinks() {
    try {
      const links = await chrome.runtime.sendMessage({ action: 'getRecentLinks' });
      renderRecentLinks(links || []);
    } catch { recentList.innerHTML = '<div class="empty-state">Could not load links</div>'; }
  }

  function renderRecentLinks(links) {
    if (!links || links.length === 0) {
      recentList.innerHTML = '<div class="empty-state">No links yet. Shorten your first URL!</div>';
      return;
    }
    recentList.innerHTML = links.map(l => `
      <div class="recent-item">
        <span class="short" data-url="${l.shortUrl}">${l.shortUrl}</span>
        <span class="long" title="${l.longUrl}">${l.longUrl}</span>
        <span class="time">${formatTime(l.createdAt)}</span>
      </div>
    `).join('');
    recentList.querySelectorAll('.short').forEach(el => {
      el.addEventListener('click', () => {
        navigator.clipboard.writeText(el.dataset.url);
        showStatus('Copied to clipboard!', 'success');
      });
    });
  }

  async function handleClear() {
    await chrome.runtime.sendMessage({ action: 'clearRecentLinks' });
    loadRecentLinks();
    showStatus('Recent links cleared', 'info');
  }

  function showStatus(msg, type) {
    status.textContent = msg;
    status.className = 'status ' + (type || '');
    clearTimeout(showStatus._timer);
    if (type !== 'info') showStatus._timer = setTimeout(() => { status.textContent = ''; status.className = 'status'; }, 4000);
  }

  function formatTime(iso) {
    if (!iso) return '';
    const d = new Date(iso);
    const now = new Date();
    const diff = now - d;
    if (diff < 60000) return 'just now';
    if (diff < 3600000) return `${Math.floor(diff / 60000)}m ago`;
    if (diff < 86400000) return `${Math.floor(diff / 3600000)}h ago`;
    return d.toLocaleDateString();
  }
});
