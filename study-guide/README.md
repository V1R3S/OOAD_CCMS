# Understanding Arena

Open **Arena-study-guide.pdf** for the complete learning companion. It explains the requirements, five reports, all production Java files and service operations, eleven UML sheets, prototype, tests, builds, and presentation, with worked examples and exercises.

The guide assumes basic Java knowledge. Read it alongside the application and source. Its appendices contain a source starting-line index, all 86 recorded service/Swing checks, all eleven UML sheets, and a reading map.

The guide describes Arena as reviewed and tested on 10 November 2026. It explains the seven supported type/format combinations, quiz scoring, knockout byes, league tables, legacy-data migration and the current presentation.

This folder is a personal study companion, separate from the submission package's allowlist. It does not alter the five academic reports, slides, app, or stored data.

## Editable source

- `Arena-study-guide.tex`: explanatory text and layout, using the shared AAU theme and root logo.
- `source-index.tex`: source navigation appendix, with line numbers for the reviewed snapshot.
- `test-catalogue.tex`: preserved test descriptions and results.

To rebuild with the workspace's existing Tectonic installation, run from the project root:

```powershell
$env:TECTONIC_CACHE_DIR = "$PWD/.tools/tex-cache"
.\.tools\tectonic\tectonic.exe --only-cached --outdir study-guide study-guide/Arena-study-guide.tex
```

On another computer, use an installed TeX distribution and compile from this folder with pdfLaTeX twice. Keep the source index and evidence date accurate if the application is changed later.
