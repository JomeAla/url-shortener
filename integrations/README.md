# Shrtly Integrations

This directory contains integration definitions for third-party automation platforms.

## Structure

```
integrations/
  zapier/
    package.json     — Zapier app metadata
    index.js         — Zapier app definition (triggers, actions, searches)
```

## Zapier

To publish the Zapier app:

1. Install the Zapier CLI: `npm install -g zapier-platform-cli`
2. Register: `zapier register "Shrtly"`
3. Push: `zapier push`

The app defines:
- **Trigger**: New Link Click (via webhook subscribe/unsubscribe)
- **Actions**: Create Short URL, Bulk Shorten
- **Searches**: List URLs, Get Analytics, Get Public Stats

## n8n

Use n8n's built-in **Webhook** node to receive link.clicked events from Shrtly, and the **HTTP Request** node to call Shrtly's REST API.

See `/docs` in the app for full connection guides.

## Make (Integromat)

Use Make's **Webhook** module to receive link.clicked events, and the **HTTP** module (Make a Request) to call Shrtly's API.

See `/docs` in the app for full connection guides.
