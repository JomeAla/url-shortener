# Shrtly Browser Extension

Chrome and Firefox extension for the Shrtly URL shortener.

## Features

- **Right-click context menu**: Right-click any link or page and select "Shorten with Shrtly"
- **Popup UI**: Quick URL shortening with recent links history
- **Clipboard integration**: Shortened URLs are automatically copied to clipboard
- **Configurable**: Point to any Shrtly instance (self-hosted or cloud)

## Installation

### Development (unpacked)

1. Open Chrome/Edge: `chrome://extensions`
2. Enable "Developer mode"
3. Click "Load unpacked" and select the `browser-extension/` folder

For Firefox:
1. Open `about:debugging#/runtime/this-firefox`
2. Click "Load Temporary Add-on" and select `browser-extension/manifest.json`

### Icons

PNG icons are required for production. Generate them:

```bash
cd browser-extension
npm install sharp
node icons/generate.js
```

Or use an online SVG-to-PNG converter (https://convertio.co/svg-png/) with sizes 16, 48, and 128px.

## Usage

1. Click the Shrtly icon in your toolbar to open the popup
2. Paste a URL and click "Shorten"
3. Or right-click any link on any page and select "Shorten with Shrtly"
4. Your recent links appear in the popup for quick access

## Settings

1. Right-click the Shrtly icon → "Options" (or click "Settings" in the popup)
2. Enter your Shrtly Base URL (e.g. `https://shrtly.app` or `http://localhost:8081`)
3. Enter your API Key (create one in Shrtly Dashboard → API Keys)
4. Click "Save" then "Test Connection" to verify

## Build for Distribution

```bash
# Chrome: package as .zip
cd browser-extension
zip -r shrtly-chrome.zip . -x "*.git*" -x "node_modules/*"

# Firefox: package as .zip
cd browser-extension
zip -r shrtly-firefox.zip . -x "*.git*" -x "node_modules/*" -x "icons/generate.js"
```
