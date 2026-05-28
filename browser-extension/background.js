const DEFAULT_BASE_URL = 'http://localhost:8081';
const STORAGE_KEYS = {
  BASE_URL: 'shrtly_base_url',
  API_KEY: 'shrtly_api_key',
  RECENT_LINKS: 'shrtly_recent_links'
};

chrome.runtime.onInstalled.addListener(() => {
  chrome.contextMenus.create({
    id: 'shorten-link',
    title: 'Shorten with Shrtly',
    contexts: ['link']
  });
  chrome.contextMenus.create({
    id: 'shorten-page',
    title: 'Shorten this page with Shrtly',
    contexts: ['page']
  });
});

chrome.contextMenus.onClicked.addListener(async (info, tab) => {
  const url = info.linkUrl || info.pageUrl;
  if (!url) return;
  const result = await shortenUrl(url);
  if (result.success) {
    try {
      await navigator.clipboard.writeText(result.shortUrl);
    } catch {
      chrome.scripting?.executeScript?.({
        target: { tabId: tab.id },
        func: (text) => navigator.clipboard.writeText(text),
        args: [result.shortUrl]
      });
    }
    chrome.notifications.create({
      type: 'basic',
      iconUrl: 'icons/icon48.png',
      title: 'Shrtly',
      message: `Shortened! ${result.shortUrl} (copied to clipboard)`
    });
  } else {
    chrome.notifications.create({
      type: 'basic',
      iconUrl: 'icons/icon48.png',
      title: 'Shrtly Error',
      message: result.error || 'Failed to shorten URL'
    });
  }
});

chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  switch (request.action) {
    case 'shorten':
      shortenUrl(request.url).then(sendResponse);
      return true;
    case 'getRecentLinks':
      getRecentLinks().then(sendResponse);
      return true;
    case 'clearRecentLinks':
      clearRecentLinks().then(sendResponse);
      return true;
  }
});

async function getSettings() {
  const result = await chrome.storage.sync.get([STORAGE_KEYS.BASE_URL, STORAGE_KEYS.API_KEY]);
  return {
    baseUrl: result[STORAGE_KEYS.BASE_URL] || DEFAULT_BASE_URL,
    apiKey: result[STORAGE_KEYS.API_KEY] || ''
  };
}

async function shortenUrl(url) {
  try {
    const settings = await getSettings();
    const headers = { 'Content-Type': 'application/json' };
    if (settings.apiKey) headers['X-API-Key'] = settings.apiKey;

    const response = await fetch(`${settings.baseUrl}/api/shorten`, {
      method: 'POST',
      headers,
      body: JSON.stringify({ url })
    });

    if (!response.ok) {
      const err = await response.json().catch(() => ({}));
      return { success: false, error: err.error || `HTTP ${response.status}` };
    }

    const data = await response.json();
    const shortUrl = data.shortUrl || `${settings.baseUrl}/${data.shortCode}`;

    await addRecentLink({ shortCode: data.shortCode, shortUrl, longUrl: url, createdAt: new Date().toISOString() });

    return { success: true, shortUrl, shortCode: data.shortCode };
  } catch (e) {
    return { success: false, error: e.message };
  }
}

async function getRecentLinks() {
  const result = await chrome.storage.local.get(STORAGE_KEYS.RECENT_LINKS);
  return result[STORAGE_KEYS.RECENT_LINKS] || [];
}

async function addRecentLink(link) {
  const links = await getRecentLinks();
  links.unshift(link);
  if (links.length > 50) links.length = 50;
  await chrome.storage.local.set({ [STORAGE_KEYS.RECENT_LINKS]: links });
}

async function clearRecentLinks() {
  await chrome.storage.local.remove(STORAGE_KEYS.RECENT_LINKS);
}
