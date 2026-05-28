const createShortUrl = async (z, bundle) => {
  const response = await z.request({
    url: `${bundle.authData.baseUrl}/api/shorten`,
    method: 'POST',
    headers: { 'X-API-Key': bundle.authData.apiKey, 'Content-Type': 'application/json' },
    body: { url: bundle.inputData.url, customCode: bundle.inputData.customCode, tags: bundle.inputData.tags }
  });
  return response.data;
};

const bulkShorten = async (z, bundle) => {
  const response = await z.request({
    url: `${bundle.authData.baseUrl}/api/shorten/bulk`,
    method: 'POST',
    headers: { 'X-API-Key': bundle.authData.apiKey, 'Content-Type': 'application/json' },
    body: { urls: bundle.inputData.urls }
  });
  return response.data;
};

const listUrls = async (z, bundle) => {
  const response = await z.request({
    url: `${bundle.authData.baseUrl}/api/urls`,
    method: 'GET',
    headers: { 'X-API-Key': bundle.authData.apiKey }
  });
  return response.data;
};

const getAnalytics = async (z, bundle) => {
  const response = await z.request({
    url: `${bundle.authData.baseUrl}/api/analytics/${bundle.inputData.shortCode}`,
    method: 'GET',
    headers: { 'X-API-Key': bundle.authData.apiKey }
  });
  return response.data;
};

const getStats = async (z, bundle) => {
  const response = await z.request({
    url: `${bundle.authData.baseUrl}/api/stats/${bundle.inputData.shortCode}`,
    method: 'GET'
  });
  return response.data;
};

const testAuth = async (z, bundle) => {
  const response = await z.request({
    url: `${bundle.authData.baseUrl}/api/keys`,
    method: 'GET',
    headers: { 'X-API-Key': bundle.authData.apiKey }
  });
  if (response.status === 200) return { authenticated: true };
  throw new Error('Invalid API key');
};

module.exports = {
  version: require('./package.json').version,
  platformVersion: require('zapier-platform-core').version,

  authentication: {
    type: 'custom',
    test: testAuth,
    fields: [
      { key: 'baseUrl', label: 'Shrtly Base URL', helpText: 'Your Shrtly instance URL (e.g. https://shrtly.app or http://localhost:8081)', required: true },
      { key: 'apiKey', label: 'API Key', helpText: 'Create an API key in Shrtly Dashboard > API Keys.', required: true, type: 'password' }
    ]
  },

  triggers: {
    new_link_click: {
      key: 'new_link_click',
      noun: 'Link Click',
      display: { label: 'New Link Click', description: 'Triggers when a short link is clicked.' },
      operation: {
        type: 'hook',
        performSubscribe: async (z, bundle) => {
          const response = await z.request({
            url: `${bundle.authData.baseUrl}/api/webhooks`,
            method: 'POST',
            headers: { 'X-API-Key': bundle.authData.apiKey, 'Content-Type': 'application/json' },
            body: { url: bundle.targetUrl, events: 'link.clicked' }
          });
          return { id: response.data.id };
        },
        performUnsubscribe: async (z, bundle) => {
          await z.request({
            url: `${bundle.authData.baseUrl}/api/webhooks/${bundle.subscribeData.id}`,
            method: 'DELETE',
            headers: { 'X-API-Key': bundle.authData.apiKey }
          });
          return {};
        },
        perform: (z, bundle) => [bundle.cleanedRequest]
      }
    }
  },

  creates: {
    create_short_url: {
      key: 'create_short_url',
      noun: 'Short URL',
      display: { label: 'Create Short URL', description: 'Shortens a long URL.' },
      operation: {
        inputFields: [
          { key: 'url', label: 'Long URL', type: 'string', required: true, helpText: 'The URL to shorten.' },
          { key: 'customCode', label: 'Custom Code', type: 'string', required: false, helpText: 'Optional custom short code.' },
          { key: 'tags', label: 'Tags', type: 'string', required: false, helpText: 'Comma-separated tags.' }
        ],
        perform: createShortUrl
      }
    },
    bulk_shorten: {
      key: 'bulk_shorten',
      noun: 'Bulk Shorten',
      display: { label: 'Bulk Shorten URLs', description: 'Shortens multiple URLs at once.' },
      operation: {
        inputFields: [
          { key: 'urls', label: 'URLs', type: 'string', required: true, helpText: 'JSON array of {url, tags?} objects.' }
        ],
        perform: bulkShorten
      }
    }
  },

  searches: {
    list_urls: {
      key: 'list_urls',
      noun: 'URL',
      display: { label: 'List URLs', description: 'Lists all your short URLs.' },
      operation: { perform: listUrls }
    },
    get_analytics: {
      key: 'get_analytics',
      noun: 'Analytics',
      display: { label: 'Get Analytics', description: 'Gets click analytics for a short URL.' },
      operation: {
        inputFields: [
          { key: 'shortCode', label: 'Short Code', type: 'string', required: true, helpText: 'The short code to get analytics for.' }
        ],
        perform: getAnalytics
      }
    },
    get_stats: {
      key: 'get_stats',
      noun: 'Stats',
      display: { label: 'Get Public Stats', description: 'Gets public click count for a short URL (no auth needed).' },
      operation: {
        inputFields: [
          { key: 'shortCode', label: 'Short Code', type: 'string', required: true }
        ],
        perform: getStats
      }
    }
  }
};
