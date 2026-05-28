/**
 * Generates PNG icons from icon.svg for Chrome/Firefox extension.
 * Requires Node.js with `sharp` installed.
 *
 * Usage: node icons/generate.js
 */
const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const sizes = [16, 48, 128];
const svgPath = path.join(__dirname, 'icon.svg');

// Check if sharp is available
try {
  require.resolve('sharp');
} catch {
  console.log('Sharp not found. Install it: npm install sharp');
  console.log('For now, copy icon.svg to icon16.png, icon48.png, icon128.png manually.');
  console.log('Or use an online SVG-to-PNG converter (e.g. https://convertio.co/svg-png/)');
  process.exit(0);
}

const sharp = require('sharp');
const svgBuffer = fs.readFileSync(svgPath);

async function generate() {
  for (const size of sizes) {
    await sharp(svgBuffer)
      .resize(size, size)
      .png()
      .toFile(path.join(__dirname, `icon${size}.png`));
    console.log(`Generated icon${size}.png`);
  }
  console.log('Done!');
}
generate().catch(console.error);
