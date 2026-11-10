# Arena implementation and verification notes

Review date: 10 November 2026 (UAE). This describes changes implemented in the working repository. Earlier reports are preserved in `.local-archive`; they are not submitted as current evidence.

## Implemented

- Rebranded the application, prototype, reports, diagrams, assets and presentation as Arena. Kept AAU and UAE context.
- Separated activity (`CompetitionType`) from progression (`TournamentFormat`) with seven supported combinations. Scoring and tournament strategies have separate interfaces.
- Added bounded quiz scoring, knockout byes for all entrant counts 2–16, and complete round-robin fixtures/standings with deterministic tie-breaking.
- Preserved authentication, credential-free account summaries, service authorization, team locks, submissions, audit, CSV safeguards and failed-save rollback.
- Retained the serialized `edu.aau.ccms.Model` identity. Schema-one records map to explicit type/format fields, with two genuine legacy fixtures covering historical completed data and active knockout continuation.
- Updated form relevance, responsive cards, minimum viewport support, table column widths, match score controls and tab retention after result entry.
- Added a Design Class Diagram covering all 37 named production types and readable focused sheets.

## Executed verification

| Command | Observed result |
|---|---|
| `build-app.ps1 -Test -UiTest` | 70 original service + 28 Arena regression + 22 Swing checks passed |
| `mvn clean compile` | Success with Java release 17 |
| `mvn test` | 98 service checks passed; Swing wrapper skipped as designed |
| `mvn package` | Success; produced `target/arena.jar` |
| `mvn test -Pui-tests` | Three suite wrappers passed; 120 named checks passed |
| `sh build-app.sh test` through Git shell on Windows | 98 service checks passed; LF script worked |
| `node scripts/test-prototype.cjs --capture --browser ...` | 17 checks passed, seven browser captures regenerated |
| Fresh submission ZIP extraction: `build-app.ps1 -Test -UiTest` | Built successfully; all 98 service and 22 Swing checks passed without bundled development tools |

The new service suite loops over every supported knockout and league entrant count from 2 to 16. Each loop remains one named test; counts are not inflated. Tests cover invalid type/format combinations, score bounds, unauthorized changes, publication guards, draws, byes, fixture uniqueness, restart, legacy migration and failed-write rollback. Swing checks operate real controls on the event-dispatch thread and test forms, scoring, league tables and a 1100 × 720 window. They are automated interactions, not a formal participant usability study.

Machine-readable executed evidence is in `reports/test-evidence`. Maven displays suite wrappers rather than one JUnit method per named check. Java targets release 17; these Windows runs used JDK 21. Native Linux/macOS behavior and institutional deployment are not claimed.

## Scope and limitations

The app uses a single local process and trusted operating-system accounts. Snapshots are not encrypted. There is no cloud service, automatic code execution, question-delivery engine, venue calendar, result correction after match confirmation or forfeit workflow. Quiz scores are entered manually. Tied quiz knockout scores require an external tie-break. League ties finally use name and stable ID, a documented deterministic policy rather than an extra playoff.

The browser prototype has temporary independent data and a role selector. Its creation dialog previews valid settings; Java performs persistent competition creation. It is clearly described as a prototype. Current screenshots are actual Java renders, separate from prototype captures and wireframes.

The five report PDFs and the personal learning guide were rebuilt from LaTeX. The presentation uses the actual report UML and a complete 37-type Design Class Diagram. Its 20 main slides and four hidden backups follow the supplied redesigned presentation; the speaking guide and embedded notes use the same order. The earlier concept film was replaced with a current Java screen. The complete class overview is dense; focused application and record sheets in the Design Report provide readable detail.

Maintained text, exported PDF text and PowerPoint text were checked for Australian references; none were found. Academic identity and examples use Al Ain University / UAE context. The only old product abbreviation in the reports is the explicitly explained serialized compatibility namespace.

## Git and academic attribution

No history was rewritten, no commits were fabricated and no push was performed. Prior reorganization appears as old tracked paths removed and new paths added until the group stages its work. Review the complete diff and commit necessary sources, tests, documents and assets together. Ignore caches, portable tools, builds, live data and archives. Individual contributions require the group's confirmation; speaking roles are not evidence of authorship.
