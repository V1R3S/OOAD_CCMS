// Optional asset rebuild. Report compilation uses the included vector PDFs.
// Requires Node.js, Playwright, Chromium (or Edge), and DejaVuSans.ttf.
const fs = require('node:fs');
const path = require('node:path');
const { chromium } = require('playwright');

async function main() {
  const args = process.argv.slice(2);
  const option = name => {
    const index = args.indexOf(name);
    return index < 0 ? undefined : args[index + 1];
  };
  const fontPath = option('--font');
  if (!fontPath || !fs.existsSync(fontPath)) {
    throw new Error('Provide --font followed by the path to DejaVuSans.ttf.');
  }
  const directory = path.resolve(__dirname, '../docs/assets/uml');
  const sources = fs.readdirSync(directory).filter(name => name.endsWith('.svg')).sort();
  if (sources.length !== 9) throw new Error('Expected the nine supplied UML SVGs.');
  const fontData = fs.readFileSync(fontPath).toString('base64');
  const browser = await chromium.launch({ headless: true, executablePath: option('--browser') });
  try {
    const page = await browser.newPage();
    for (const name of sources) {
      const svg = fs.readFileSync(path.join(directory, name), 'utf8');
      // Retain all diagram content. Only the surrounding poster title/footer is omitted.
      // The original SVG remains byte-for-byte unchanged alongside the PDF.
      const reportSvg = svg.replace(/<\?xml[^>]*\?>\s*/, '')
        .replace('viewBox="0 0 1800 1120"', 'viewBox="60 175 1680 855"')
        .replace('width="1800px"', 'width="1680px"')
        .replace('height="1120px"', 'height="855px"');
      if (reportSvg === svg || !reportSvg.includes('viewBox="60 175 1680 855"')) {
        throw new Error(`Unexpected SVG dimensions: ${name}`);
      }
      await page.setContent(`<!doctype html><html><head><meta charset="utf-8"><style>
        @font-face { font-family: 'DejaVu Sans'; src: url(data:font/ttf;base64,${fontData}); }
        @page { size: 1680px 855px; margin: 0; }
        html, body { margin: 0; width: 1680px; height: 855px; overflow: hidden; }
        svg { display: block; }
      </style></head><body>${reportSvg}</body></html>`);
      await page.evaluate(() => document.fonts.ready);
      await page.pdf({ path: path.join(directory, name.replace('.svg', '.pdf')),
        preferCSSPageSize: true, printBackground: true });
      console.log(`Rendered ${name}`);
    }
  } finally {
    await browser.close();
  }
}

main().catch(error => { console.error(error.message); process.exitCode = 1; });
