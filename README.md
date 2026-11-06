# Campus Competition Management System

Java desktop application for **Object Oriented Analysis and Design / System Analysis & Design** at Al Ain University.

**Dr. Yazeed Ghadi · Section 61D · 0103430 / 0102331**

## Start here

- **Run the app:** double-click [start-app.cmd](start-app.cmd).
- **Explore the clickable prototype:** open [prototype/index.html](prototype/index.html) in a browser. No installation needed.
- **Read the reports:** open the PDFs in [output/pdf](output/pdf).
- **Present the Java app.** Open the [OOAD PowerPoint](output/presentation/CCMS-OOAD-presentation.pptx) and use the [demo guide](presentation-guide.md) for the live walkthrough.

## Interactive prototype

The prototype includes an overview, searchable competition cards, team registration, project submissions, a judging rubric, programming scores, a knockout bracket, and published results. Use **Explore as** to switch between Student, Organizer, and Judge.

Try these walkthroughs:

1. **Project:** register as Student → switch to Organizer and move to Active → submit as Student → open Judging as Organizer → score as Judge → publish as Organizer.
2. **Programming:** open Code Sprint as Judge → enter an official score → inspect the updated ranking → publish as Organizer.
3. **Knockout:** open Robotics Cup as Judge → choose both semifinal winners → choose the champion → move to Judging as Organizer to certify the bracket → publish.

This is a browser design prototype with its own sample data. Refreshing the page or choosing **Reset demo** clears its changes. The Java application stores its data separately.

## Java application

Requires **JDK 17 or later**. Set `JAVA_HOME` or put Java on `PATH`. Scripts also detect an optional portable JDK under `.tools/java`; tools are excluded from the submission ZIP.

```powershell
.\start-app.ps1
.\build-app.ps1 -Test -UiTest
```

All demo passwords are **Campus@2026**.

| Role | Account |
|---|---|
| Organizer | organizer1@campus.test |
| Administrator | admin@campus.test |
| Project judges | judge1@campus.test, judge2@campus.test |
| Knockout judge | judge3@campus.test |
| Student captain | student1@campus.test |

Additional students are `student2@campus.test` through `student10@campus.test`. The app seeds five teams and three competitions on first launch. Local data is stored in `data/campus.bin`.

The three formats use different rules: weighted project rubrics (40/35/25), programming ranks by solved problems then time penalties, and single-elimination brackets. Service checks enforce roles, roster locking, submissions, evaluation completeness, and publication.

Verified on **3 November 2026**: **70 service checks and 16 Swing UI checks passed**. The UI suite opens temporary windows and uses isolated test data. CSV results and timestamped summaries are in [docs/evidence](docs/evidence).

Maven 3.9+ is the standard build route (internet access is needed on first use):

```sh
mvn clean compile
mvn test                 # 70 service checks; Swing wrapper skipped
mvn test -Pui-tests       # 70 service + 16 Swing checks; graphical desktop required
mvn package
java -jar target/campus-ccms.jar
```

Surefire reports two JUnit wrapper methods around the existing ordered lifecycle suites. The 86 checks are assertions/scenarios logged by those suites, not 86 independently runnable JUnit methods. Failures propagate to Maven.

The PowerShell scripts require only a JDK and build `build/campus.jar`. For macOS/Linux, use `sh build-app.sh test` or `sh build-app.sh ui-test` with a graphical session. The shell file has LF endings enforced by `.gitattributes`; Both shell suites passed using the bundled Git shell on Windows. Linux/macOS execution remains unverified.

## Reports and source files

| Folder | Contents |
|---|---|
| `src/main/java` | Java application |
| `src/test/java` | Service and Swing UI tests |
| `prototype` | Editable HTML, CSS, and JavaScript prototype |
| `docs/system-analysis`, `docs/system-design`, `docs/implementation-report` | Main LaTeX reports |
| `docs/testing-report`, `docs/interface-mockups` | Testing and interface documents |
| `docs/shared` | AAU theme and references |
| `docs/assets` | Java screenshots, browser-prototype captures, wireframes, and UML illustrations embedded in the reports |
| `docs/assets/uml` | Nine supplied SVG diagrams and their vector PDF versions for LaTeX |
| `docs/evidence` | Test results used by the testing report |
| `output/pdf` | Five final PDFs |
| `requirements` | Original assignment outlines, with descriptive file names |
| `sample-data` | Sample-data description |
| `scripts` | Document build and asset helpers |

The original `.txt` assignment contents are preserved in `requirements`. Each report folder contains `main.tex` (the compilation entry point) and `content.tex` (the report text). Keep `aau.png` at the project root; the LaTeX covers and prototype use it, and the Java build packages it inside the app. The images in `docs/assets` are required report illustrations, not duplicate PDF-page renders. Browser-prototype screenshots appear in the analysis and interface mock-ups PDFs.

Build the documents with:

```powershell
.\scripts\build-reports.ps1 -CachedOnly
```

Alternatively, compile each `main.tex` from its own folder twice using pdfLaTeX. `.tools`, `build`, and `data` contain local tools or generated files and are excluded from Git.

To rebuild just the reports containing UML, use `.\scripts\build-reports.ps1 -CachedOnly -Report system-analysis,system-design`. The nine supplied diagrams appear on dedicated landscape pages. Their SVG originals are preserved unchanged. The included vector PDFs omit only the poster titles and footers so the reports can use their own headings, captions, and page numbers. No SVG conversion is needed for an ordinary report build. See `docs/assets/uml/README.md` to regenerate artwork after editing an SVG.

This is a single-computer coursework application. It does not include cloud synchronization, email, automatic code execution, or bracket forfeits. Detailed scope and design decisions are in the reports.

## Review status

The supplied outlines are covered by the five reports: analysis (requirements and UML models), system design (architecture and class interfaces), implementation report (including the user guide), testing report, and interface mock-ups. The sample dataset and three distinct competition formats are included.

Testing covers service rules and storage, plus selected real Swing controls and dialogs. Eight app screenshots and four browser-prototype captures were regenerated from the final interfaces. Formal accessibility/high-DPI testing, a user study, load testing, and macOS/Linux execution remain unverified. Cloud services, email, automatic code grading, venue scheduling, and bracket forfeits remain outside the documented scope.

Java packages separate responsibilities:

- `ui`: `CampusApp` navigation, `AppDialogs` forms, `CompetitionViews`, shared controls and theme.
- `service`: `CampusService` authorization/workflows and `DemoData` first-run fixtures.
- `rules`: the rule interface and three format strategies.
- `repository`: file locking, filtered snapshots, validated saves.
- `util`: password derivation and verification.
- `model`: credential-free `UserSummary` responses.

The existing serialized records deliberately remain under `edu.aau.ccms.Model` to preserve saved-data compatibility. Their maps are held privately by the service/repository aggregate and are never returned by service APIs. TC-69 checks a pre-refactor fictional-data fixture. Account listings are administrator-only and never contain password hashes; organizers get a separate judge picker.

## Submission package

Run `python scripts/package-submission.py` with Python 3.10+ to create `dist/CCMS-submission.zip`. The archive includes the source, tests, build scripts, logo, assignment outlines, prototype, LaTeX sources, report assets, test evidence, five PDFs, and the PowerPoint presentation. It includes a SHA-256 manifest. Install a JDK separately to build the app from the ZIP.

See [FINAL-REVIEW.md](FINAL-REVIEW.md) for the audit, executed validation, requirements coverage, limitations, and submission verdict. Actual individual contributions still require group confirmation; the reports label the proposed presentation allocation accordingly. The local source and reports should be committed by the group before submitting a GitHub URL.

## Group

| Name | Student ID |
|---|---|
| Khaled Alkhawaldeh | 202410091 |
| Abdulrahman Alnaqbi | 202340042 |
| Yousif Alhammadi | 202310707 |
| Ahmed Alsamma | 202320192 |

Developed with AI assistance. Commits and pushes are managed by the group.
