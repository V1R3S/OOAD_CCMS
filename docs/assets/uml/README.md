# UML artwork

These nine SVG files were supplied by the project team on 6 November 2026. They are the editable originals used by the reports. Each matching PDF is a vector illustration required by the LaTeX source, not a duplicate report-page screenshot.

| Diagram | Report |
|---|---|
| `01_use_case` | System Analysis, Use Case Model |
| `02_registration_sequence` | System Analysis, Dynamic Models |
| `03_competition_state` | System Analysis, Competition Lifecycle |
| `04_competition_activity` | System Analysis, Format-Specific Workflow |
| `05_conceptual_domain` | System Analysis, Object and Class Model |
| `06_software_architecture` | System Design, Subsystem Decomposition |
| `07_strategy_class` | System Design, Final Object Design |
| `08_final_domain` | System Design, Final Object Relationships |
| `09_package_dependencies` | System Design, Packages and Dependencies |

## Rebuilding the artwork

An ordinary LaTeX build uses the included PDFs and needs no additional artwork tools. After editing an SVG, the optional conversion script requires Node.js, the Playwright package, a Chromium browser or Microsoft Edge, and the DejaVu Sans regular TrueType font. Run from the project root, supplying the font installed on your computer.

```text
node scripts/render-uml.cjs --font "path/to/DejaVuSans.ttf"
```

Playwright uses its installed Chromium by default. To use another local Chromium browser, append `--browser "path/to/browser-executable"`.

The converter embeds the font and retains vector shapes and selectable text. It crops the original 1800 by 1120 canvas to the diagram region at x=60, y=175, width=1680, height=855. This omits the poster heading and footer while preserving diagram labels and notes. Report headings, captions, and page numbering are supplied by LaTeX. The original SVG files remain unchanged. If the artwork moves outside that region, adjust the crop in the conversion script before rebuilding.
