const STORAGE_KEYS = {
  BASE_URL: 'shrtly_base_url',
  API_KEY: 'shrtly_api_key'
};
const DEFAULT_BASE_URL = 'http://localhost:8081';

document.addEventListener('DOMContentLoaded', async () => {
  const baseUrlInput = document.getElementById('baseUrl');
  const apiKeyInput = document.getElementById('apiKey');
  const saveBtn = document.getElementById('saveBtn');
  const testBtn = document.getElementById('testBtn');
  const saveStatus = document.getElementById('saveStatus');
  const testStatus = document.getElementById('testStatus');

  const result = await chrome.storage.sync.get([STORAGE_KEYS.BASE_URL, STORAGE_KEYS.API_KEY]);
  baseUrlInput.value = result[STORAGE_KEYS.BASE_URL] || DEFAULT_BASE_URL;
  apiKeyInput.value = result[STORAGE_KEYS.API_KEY] || '';

  saveBtn.addEventListener('click', async () => {
    const baseUrl = baseUrlInput.value.trim().replace(/\/+$/, '');
    const apiKey = apiKeyInput.value.trim();
    await chrome.storage.sync.set({
      [STORAGE_KEYS.BASE_URL]: baseUrl || DEFAULT_BASE_URL,
      [STORAGE_KEYS.API_KEY]: apiKey
    });
    saveStatus.textContent = 'Settings saved!';
    saveStatus.className = 'success';
    setTimeout(() => { saveStatus.textContent = ''; }, 3000);
  });

  testBtn.addEventListener('click', async () => {
    const baseUrl = (baseUrlInput.value.trim() || DEFAULT_BASE_URL).replace(/\/+$/, '');
    const apiKey = apiKeyInput.value.trim();
    testStatus.textContent = 'Testing...';
    testStatus.className = '';
    try {
      const headers = {};
      if (apiKey) headers['X-API-Key'] = apiKey;
      const response = await fetch(`${baseUrl}/api/health`, { headers });
      if (response.ok) {
        const data = await response.json();
        testStatus.textContent = `Connected! Server: ${data.status || 'UP'}`;
        testStatus.className = 'success';
      } else {
        testStatus.textContent = `Server responded with HTTP ${response.status}`;
        testStatus.className = 'error';
      }
    } catch (e) {
      testStatus.textContent = `Connection failed: ${e.message}`;
      testStatus.className = 'error';
    }
  });
});
