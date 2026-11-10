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
  const directory = path.resolve(__dirname, '../reports/assets/uml');
  const sources = fs.readdirSync(directory).filter(name => name.endsWith('.svg')).sort();
  if (!sources.includes('08_design_class.svg')) throw new Error('The source-checked Design Class Diagram is missing.');
  const fontData = fs.readFileSync(fontPath).toString('base64');
  const browser = await chromium.launch({ headless: true, executablePath: option('--browser') });
  try {
    const page = await browser.newPage();
    for (const name of sources) {
      const svg = fs.readFileSync(path.join(directory, name), 'utf8');
      // Retain diagram content; omit only the poster title/footer in report figures.
      const dimensions = svg.match(/viewBox="0 0 (\d+) (\d+)"/);
      if (!dimensions) throw new Error(`Unexpected SVG dimensions: ${name}`);
      const width = Number(dimensions[1]), height = Number(dimensions[2]) - 265;
      const reportSvg = svg.replace(/<\?xml[^>]*\?>\s*/, '')
        .replace(dimensions[0], `viewBox="0 175 ${width} ${height}"`)
        .replace(`width="${dimensions[1]}px"`, `width="${width}px"`)
        .replace(`height="${dimensions[2]}px"`, `height="${height}px"`);
      await page.setContent(`<!doctype html><html><head><meta charset="utf-8"><style>
        @font-face { font-family: 'DejaVu Sans'; src: url(data:font/ttf;base64,${fontData}); }
        @page { size: ${width}px ${height}px; margin: 0; }
        html, body { margin: 0; width: ${width}px; height: ${height}px; overflow: hidden; }
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
