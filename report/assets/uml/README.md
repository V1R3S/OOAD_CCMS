# Arena UML diagrams

Analysis now uses the team-supplied replacements added on 12 November 2026:

- `supplied/01_authentication_use_case.png` and its PDF wrapper: the supplied authentication-focused use-case image, unchanged.
- `supplied/05_core_domain.svg`, `.png`, and `.pdf`: the first sheet from the supplied class-diagram bundle. The PDF preserves its vector artwork.

The presentation uses these same two images on slides 9 and 11 of the current 21-slide deck. The other three supplied class sheets remain in the original root ZIP; this update replaces the Analysis diagrams, not the separate Design report's implementation diagrams. The older `01_use_case` and `05_conceptual_domain` assets below are retained reference artwork and are no longer included in Analysis. `render-uml.cjs` renders the original top-level SVGs only, so it does not overwrite the supplied artwork.

These editable SVGs preserve the team-supplied diagram style and have been updated for Arena. Matching vector PDFs are the report figures. Slides use the same report artwork, not separate simplified drawings.

| Source | Purpose |
|---|---|
| 01_use_case | Four roles and shared account capabilities |
| 02_registration_sequence | Captain registration, validation, saving and rollback |
| 03_competition_state | Guarded competition lifecycle |
| 04_competition_activity | Score ranking, knockout and round-robin workflows |
| 05_conceptual_domain | Analysis class model of competition concepts |
| 06_software_architecture | UI, application service, policies and persistence |
| 07_strategy_class | Independent scoring and tournament interfaces |
| 08_design_class | All 37 named production Java types, including records and enums |
| 08a_application_classes | Readable application, UI and persistence details |
| 08b_record_classes | Readable persisted records and relationships |
| 09_package_dependencies | Actual Java package dependencies |

The Design Class Diagram includes every named production type, with selected real fields and methods. It is not a list of every method, anonymous Swing adapter, or JDK class. Record components are private final fields with public generated accessors. Relationships between persisted records resolve IDs rather than recursive Java object references. The compatibility namespace remains `edu.aau.ccms.Model` so old snapshots can load.

`python scripts/generate-design-class-diagram.py` checks the 37-name inventory against Java sources and regenerates the strategy and design sheets. The other SVGs are directly editable. After changing SVGs, run:

```text
node scripts/render-uml.cjs --font "path/to/DejaVuSans.ttf" --browser "path/to/browser"
```

This optional authoring step needs Node.js, Playwright and Chromium/Edge. It removes only poster headers and footers, keeps the full horizontal drawing, embeds the font and creates vector PDFs. The Word report uses images of the included PDFs, so these tools are not needed to read it. Replace the figure in the Word report after editing a diagram.
