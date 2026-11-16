# Arena project files

Keep the maintained files below. Generated folders are useful locally but do not belong in the submission ZIP. File extensions may be hidden by Windows Explorer; the two start-app files are different launcher layers.

## Every top-level folder

| Folder | Purpose |
|---|---|
| `src/` | Production Java, automated tests and two historical data fixtures. |
| `reports/` | Combined Word report and required illustrations. |
| `presentation/` | Arena PowerPoint and four-person speaking/demo script. |
| `prototype/` | Independent HTML/CSS/JavaScript visual walkthrough. |
| `requirements/` | University assignment outlines; the sample-data product name is updated only. |
| `scripts/` | Optional document, diagram, capture, evidence and ZIP rebuild helpers. |
| `project-notes/` | Demo dataset reference and current technical verification notes. |
| `study-guide/` | Detailed personal learning companion; excluded from the submission ZIP. |
| `.tools/` | Local Java/Maven/TeX tools and caches; ignored and excluded. |
| `.local-archive/` | Preserved originals and earlier deliverables; ignored and excluded. |
| `build/` | Reproducible compiled app, tests, logs and private visual checks; ignored. |
| `target/` | Maven classes, test reports and arena.jar; ignored. |
| `dist/` | Submission ZIP and checksum; generated and ignored. |
| `data/` | Local saved snapshots, created when the app starts; private and excluded. |
| `.git/` | Git history and index; preserved, never included in the submission. |

## Root files

| File | Purpose |
|---|---|
| `.gitignore` | Excludes generated output, local tools, secrets and live data; permits both intentional legacy fixtures. |
| `.gitattributes` | Sets portable text line endings and binary handling. |
| `aau.png` | Original Al Ain University logo used in the app, prototype and documents. |
| `pom.xml` | Java 17 Maven build and JUnit suite integration. |
| `README.md` | Start here: installation, run/test commands, features and limitations. |
| `PROJECT-FILES.md` | This complete maintained-file inventory. |
| `build-app.ps1` | Windows native compile/package and optional service/UI tests. |
| `build-app.sh` | POSIX native build/test route, stored with LF endings. |
| `start-app.ps1` | Windows launcher with JDK detection and preserved-data path selection. |
| `start-app.cmd` | Double-click launcher that invokes the PowerShell startup script. |

## src

| File | Purpose |
|---|---|
| `src/main/java/edu/aau/arena/model/UserSummary.java` | Account directory/session view. It deliberately has no credential field. |
| `src/main/java/edu/aau/arena/repository/Repository.java` | Single-process local repository with filtered reads and atomic replacement. |
| `src/main/java/edu/aau/arena/rules/CompetitionRules.java` | Scoring strategy for the activity. TournamentRules owns scheduling and match progression. |
| `src/main/java/edu/aau/arena/rules/KnockoutRules.java` | Legacy-named match-only scoring policy. Progression is in KnockoutTournamentRules. |
| `src/main/java/edu/aau/arena/rules/KnockoutTournamentRules.java` | Java implementation of KnockoutTournamentRules. |
| `src/main/java/edu/aau/arena/rules/ProgrammingRules.java` | Java implementation of ProgrammingRules. |
| `src/main/java/edu/aau/arena/rules/ProjectRules.java` | Java implementation of ProjectRules. |
| `src/main/java/edu/aau/arena/rules/QuizRules.java` | Java implementation of QuizRules. |
| `src/main/java/edu/aau/arena/rules/RoundRobinRules.java` | Java implementation of RoundRobinRules. |
| `src/main/java/edu/aau/arena/rules/ScoreRankingRules.java` | Java implementation of ScoreRankingRules. |
| `src/main/java/edu/aau/arena/rules/TournamentRules.java` | Tournament strategy: fixture generation, advancement, completion, and match standings. |
| `src/main/java/edu/aau/arena/service/ArenaService.java` | Application facade: authorization, invariants, workflow, and durable transactions. |
| `src/main/java/edu/aau/arena/service/DemoData.java` | Fictional teaching data, seeded only when no database exists. |
| `src/main/java/edu/aau/arena/ui/AppDialogs.java` | Java implementation of AppDialogs. |
| `src/main/java/edu/aau/arena/ui/ArenaApp.java` | Swing boundary layer. Business rules remain in ArenaService. |
| `src/main/java/edu/aau/arena/ui/ArenaTheme.java` | Shared visual components for the desktop implementation of the Arena prototype. |
| `src/main/java/edu/aau/arena/ui/CompetitionViews.java` | Java implementation of CompetitionViews. |
| `src/main/java/edu/aau/arena/ui/UiComponents.java` | Java implementation of UiComponents. |
| `src/main/java/edu/aau/arena/ui/WorkspacePanel.java` | Pages follow the available width; long content scrolls vertically instead of pushing the window wider. |
| `src/main/java/edu/aau/arena/util/Passwords.java` | Java implementation of Passwords. |
| `src/main/java/edu/aau/ccms/Model.java` | Persisted names remain stable for legacy data. Domain writes pass through ArenaService. |
| `src/test/java/edu/aau/arena/AcceptanceMavenTest.java` | JUnit entry points preserve the ordered lifecycle scenarios and fail Maven on any failed check. |
| `src/test/java/edu/aau/arena/ArenaServiceTest.java` | Dependency-free executable acceptance suite. Failures return a non-zero exit code. |
| `src/test/java/edu/aau/arena/ArenaTournamentTest.java` | Real service/repository regressions plus exhaustive supported-size tournament checks. |
| `src/test/java/edu/aau/arena/ArenaUiSmokeTest.java` | Exercises real Swing controls on the event thread, using an isolated repository. |
| `src/test/resources/legacy-active-v1.bin` | Fictional historical version-one serialized fixture for backward-compatibility testing; not user data. |
| `src/test/resources/legacy-campus-v1.bin` | Fictional historical version-one serialized fixture for backward-compatibility testing; not user data. |
| `src/test/resources/README.md` | Supporting explanation and rebuild guidance. |

## reports

| File | Purpose |
|---|---|
| `reports/Arena-Combined-Report.docx` | Combined Analysis, Design and Final Report in Word; the maintained report document. |
| `reports/assets/prototype/01-overview.png` | Labelled browser-prototype capture; not a Java screenshot. |
| `reports/assets/prototype/02-registration.png` | Labelled browser-prototype capture; not a Java screenshot. |
| `reports/assets/prototype/03-scoring.png` | Labelled browser-prototype capture; not a Java screenshot. |
| `reports/assets/prototype/04-bracket.png` | Labelled browser-prototype capture; not a Java screenshot. |
| `reports/assets/prototype/05-quiz.png` | Labelled browser-prototype capture; not a Java screenshot. |
| `reports/assets/prototype/06-league.png` | Labelled browser-prototype capture; not a Java screenshot. |
| `reports/assets/prototype/07-creation.png` | Labelled browser-prototype capture; not a Java screenshot. |
| `reports/assets/screenshots/01-sign-in.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/02-overview.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/03-competitions.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/04-competition.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/05-bracket.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/06-results.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/07-teams.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/08-judging-studio.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/09-league-fixtures.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/10-match-score-dialog.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/11-quiz-score-dialog.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/12-league-table.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/screenshots/13-create-competition.png` | Actual Arena Swing screen/dialog capture; report illustration. |
| `reports/assets/uml/01_use_case.pdf` | Matching vector UML figure used by reports and slides. |
| `reports/assets/uml/01_use_case.svg` | Editable UML source. |
| `reports/assets/uml/02_registration_sequence.pdf` | Matching vector UML figure used by reports and slides. |
| `reports/assets/uml/02_registration_sequence.svg` | Editable UML source. |
| `reports/assets/uml/03_competition_state.pdf` | Matching vector UML figure used by reports and slides. |
| `reports/assets/uml/03_competition_state.svg` | Editable UML source. |
| `reports/assets/uml/04_competition_activity.pdf` | Matching vector UML figure used by reports and slides. |
| `reports/assets/uml/04_competition_activity.svg` | Editable UML source. |
| `reports/assets/uml/05_conceptual_domain.pdf` | Matching vector UML figure used by reports and slides. |
| `reports/assets/uml/05_conceptual_domain.svg` | Editable UML source. |
| `reports/assets/uml/06_software_architecture.pdf` | Matching vector UML figure used by reports and slides. |
| `reports/assets/uml/06_software_architecture.svg` | Editable UML source. |
| `reports/assets/uml/07_strategy_class.pdf` | Matching vector UML figure used by reports and slides. |
| `reports/assets/uml/07_strategy_class.svg` | Editable UML source. |
| `reports/assets/uml/08_design_class.pdf` | Matching vector UML figure used by reports and slides. |
| `reports/assets/uml/08_design_class.svg` | Editable UML source. |
| `reports/assets/uml/08a_application_classes.pdf` | Matching vector UML figure used by reports and slides. |
| `reports/assets/uml/08a_application_classes.svg` | Editable UML source. |
| `reports/assets/uml/08b_record_classes.pdf` | Matching vector UML figure used by reports and slides. |
| `reports/assets/uml/08b_record_classes.svg` | Editable UML source. |
| `reports/assets/uml/09_package_dependencies.pdf` | Matching vector UML figure used by reports and slides. |
| `reports/assets/uml/09_package_dependencies.svg` | Editable UML source. |
| `reports/assets/uml/README.md` | Diagram inventory and rebuild instructions. |
| `reports/assets/wireframes/01-overview.png` | Matching wireframe illustration. |
| `reports/assets/wireframes/01-overview.svg` | Editable low-fidelity layout. |
| `reports/assets/wireframes/02-competition.png` | Matching wireframe illustration. |
| `reports/assets/wireframes/02-competition.svg` | Editable low-fidelity layout. |
| `reports/assets/wireframes/03-judging.png` | Matching wireframe illustration. |
| `reports/assets/wireframes/03-judging.svg` | Editable low-fidelity layout. |
| `reports/assets/wireframes/04-bracket.png` | Matching wireframe illustration. |
| `reports/assets/wireframes/04-bracket.svg` | Editable low-fidelity layout. |

## presentation

| File | Purpose |
|---|---|
| `presentation/Arena-presentation.pptx` | Current 21-slide deck; slides 9 and 11 use the supplied Analysis diagrams. |
| `presentation/speaking-script-and-demo.md` | Slide-by-slide scripts, four speakers, live-demo clicks and professor-question preparation. |

## prototype

| File | Purpose |
|---|---|
| `prototype/app.js` | Temporary prototype interactions and sample workflows. |
| `prototype/index.html` | Prototype page structure and AAU branding. |
| `prototype/styles.css` | Prototype colours, typography, spacing and responsive layout. |

## requirements

| File | Purpose |
|---|---|
| `requirements/implementation-report-requirements.txt` | Academic assignment brief. Preserve its assessment requirements. |
| `requirements/sample-data-requirements.txt` | Academic assignment brief. Preserve its assessment requirements. |
| `requirements/system-analysis-requirements.txt` | Academic assignment brief. Preserve its assessment requirements. |
| `requirements/system-design-requirements.txt` | Academic assignment brief. Preserve its assessment requirements. |
| `requirements/testing-requirements.txt` | Academic assignment brief. Preserve its assessment requirements. |

## scripts

| File | Purpose |
|---|---|
| `scripts/generate-design-class-diagram.py` | Checks all 37 production type names and rebuilds the design/strategy SVGs. |
| `scripts/generate-wireframes.py` | Rebuilds four editable wireframes and their PNG versions. |
| `scripts/package-submission.py` | Creates a clean Arena ZIP and SHA-256 manifest, excluding private data/tools. |
| `scripts/render-uml.cjs` | Converts editable report SVG diagrams to font-embedded vector PDFs. |
| `scripts/test-prototype.cjs` | Runs 17 browser checks and optionally captures seven report illustrations. |

## project-notes

| File | Purpose |
|---|---|
| `project-notes/demo-accounts-and-data.md` | All fictional accounts, teams, five seeded events and initial records. |
| `project-notes/technical-review.md` | Implemented changes, actually executed checks and honest limitations. |

## study-guide

| File | Purpose |
|---|---|
| `study-guide/Arena-study-guide.pdf` | Finished readable PDF generated from its LaTeX source. |
| `study-guide/Arena-study-guide.tex` | Editable LaTeX report, shared style or learning appendix; rebuild the corresponding PDF after edits. |
| `study-guide/README.md` | Supporting explanation and rebuild guidance. |
| `study-guide/source-index.tex` | Editable LaTeX report, shared style or learning appendix; rebuild the corresponding PDF after edits. |
| `study-guide/test-catalogue.tex` | Editable LaTeX report, shared style or learning appendix; rebuild the corresponding PDF after edits. |

## What to submit

Run `python scripts/package-submission.py`. The result is `dist/Arena-submission.zip`. It contains maintained sources, tests, the combined Word report, assets, slides, prototype, requirements, demo-account reference, maintenance scripts and build instructions. The speaking script, internal technical review, this file inventory, and personal study guides remain in the working folder and are excluded from the submission. A JDK must be installed separately.

The old Model namespace, legacy data filenames and fictional campus.test credentials are compatibility or university concepts, not product branding. Earlier files in the private archive are deliberately preserved. No commit or push was made.
