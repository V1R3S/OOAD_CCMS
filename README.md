# Arena

Java desktop project for **Object Oriented Analysis and Design / System Analysis & Design**, Al Ain University.

Dr. Yazeed Ghadi · Section 61D · Courses 0103430 / 0102331

## Start here

- **Run Java:** double-click [start-app.cmd](start-app.cmd).
- **Read the five reports:** open [reports](reports).
- **Present:** open [the PowerPoint](presentation/Arena-presentation.pptx) and [speaking script / live-demo instructions](presentation/speaking-script-and-demo.md). The presentation follows the supplied redesigned deck: 20 main slides and four technical backup slides.
- **Explore the prototype:** open [prototype/index.html](prototype/index.html).
- **Understand every file:** read [PROJECT-FILES.md](PROJECT-FILES.md), including what to edit, keep, and rebuild.

## Build, run, and test

Requires **JDK 17 or later**, configured through `JAVA_HOME` or `PATH`. Windows scripts also detect the optional portable JDK in `.tools/java`; local tools are excluded from the submission.

```powershell
.\start-app.ps1
.\build-app.ps1 -Test -UiTest
# Fresh demo: choose a filename that does not already exist.
.\start-app.ps1 -DataFile data/rehearsal-01.bin
```

Native builds produce `build/arena.jar`. Maven is also supported:

```sh
mvn clean compile
mvn test
mvn test -Pui-tests
mvn package
java -jar target/arena.jar
```

`mvn test` runs 98 service checks (70 original checks and 28 Arena regressions). The UI profile adds 22 real Swing checks and requires a graphical desktop. Maven reports three wrapper methods around these suites, with the Swing wrapper skipped by default. The 120 checks are not 120 independent JUnit methods. The first Maven build downloads build/test dependencies; the app has no external runtime libraries.

Use `sh build-app.sh test` or `sh build-app.sh ui-test` on a POSIX shell. This route was verified through Git shell on Windows; native Linux/macOS runtime behavior remains unverified. Java targets release 17; recorded builds used JDK 21.

## Demo accounts and behavior

All seeded passwords are **Campus@2026**.

| Role | Account |
|---|---|
| Organizer | organizer1@campus.test |
| Administrator | admin@campus.test |
| Project judges | judge1@campus.test, judge2@campus.test |
| Knockout judge | judge3@campus.test |
| Team Alpha captain | student1@campus.test |

See [demo accounts and sample data](project-notes/demo-accounts-and-data.md) for all 16 fictional accounts, five teams, and five events. Missing data triggers the seed; existing files retain changes and original deadlines. New installations use `data/arena.bin`. When `data/campus.bin` already exists, the launcher and application reuse it without deleting or reseeding it.

- **Projects:** 40/35/25 weighted rubric; all assigned judges review every entry.
- **Programming:** rank solved count first, then lower time penalty; judges enter results manually.
- **Quiz:** one official points result per team, from zero to a configured maximum; equal scores share ranks.
- **Knockout:** 2–16 teams; byes fill a bracket where needed, winners progress automatically, then the owner certifies and publishes.
- **Round robin:** every pair meets once; wins earn 3 points, draws 1, losses 0. Ties compare wins, score difference, scores for, team name, then stable ID.

Competition type describes the activity; tournament format controls progression. Supported combinations:

| Type | Score ranking | Knockout | Round robin |
|---|---|---|---|
| Project | Yes | No | No |
| Programming | Yes | No | No |
| Quiz / academic | Yes | Yes | Yes |
| General match | No | Yes | Yes |

Quiz knockout requires decisive scores; tied matches need an external tie-break before entry. General knockout preserves winner selection. League matches permit draws. Score competitions accept 2–64 teams; match tournaments accept 2–16. No automatic quiz question engine or code execution is included.

Service checks enforce roles, ownership, roster locks, deadlines, score bounds, and completeness. This local single-process app excludes cloud synchronization, email, automatic code grading, venue scheduling, and forfeits.

## Where everything belongs

| Folder | Contents |
|---|---|
| `reports/` | Five finished PDFs |
| `reports/source/` | One named LaTeX source per report |
| `reports/shared/` | AAU style and references |
| `reports/assets/` | Necessary UML, Java/prototype screenshots, and wireframes |
| `reports/test-evidence/` | Preserved CSV results, timestamps, and LaTeX result tables |
| `presentation/` | One slide deck and its speaking/demo script |
| `project-notes/` | Current verification notes and demo-data explanation |
| `requirements/` | Original assignment outlines; only the product name in the sample-data brief was updated |
| `src/` | Application, tests, and two historical compatibility fixtures |
| `prototype/` | Browser prototype with temporary sample data |
| `scripts/` | Report, asset, evidence, and packaging helpers |

The personal learning companion is kept in `study-guide/` in the working project and excluded from the submission ZIP. The file guide lists its contents.

The prototype's role switch does not authenticate users. Refresh resets its independent sample data. Java is the persistent implementation used for the live demonstration.

## Edit and rebuild reports

Edit a named file in `reports/source`, such as `system-analysis.tex`. Each contains both the compilation entry point and report text. The shared style is `reports/shared/aau-report-style.tex`. Keep `aau.png` at the root; the app, prototype, and covers use it.

```powershell
.\scripts\build-reports.ps1 -CachedOnly
.\scripts\build-reports.ps1 -CachedOnly -Report system-analysis,system-design
```

The script uses local or installed Tectonic. On a fresh computer, omit `-CachedOnly` to obtain required TeX dependencies. Alternatively, compile a named source from `reports/source` twice using pdfLaTeX. Script-generated intermediates go in `build/latex`.

Report images are required illustrations. SVGs are editable artwork; matching PDF/PNG versions are embedded by LaTeX. See [UML notes](reports/assets/uml/README.md). These are not duplicate screenshots of report pages.

Preserved evidence records 98 service checks, 22 Swing checks, and 17 additional browser-prototype checks passing on 10 November 2026. Fresh runs write results under `build`. `scripts/update-test-evidence.py` copies executed results into the reports. See [technical review](project-notes/technical-review.md) for commands and limits.

Optional prototype verification requires Node.js and Playwright: `node scripts/test-prototype.cjs --browser "path/to/browser"`. Add `--capture` to rebuild its seven illustrations. Java report images come from `java -jar build/arena.jar --capture reports/assets/screenshots`.

Version-one serialized PROJECT and PROGRAMMING records map to score ranking, and KNOCKOUT maps to general match plus knockout. The persisted `edu.aau.ccms.Model` names and legacy filename are intentional compatibility identifiers, not visible product branding. Two genuine version-one fixtures test loading, continued operation, saving, and restarting. Never open upgraded data in the older application.

## Submission and Git

```sh
python scripts/package-submission.py
```

Python 3.10+ creates `dist/Arena-submission.zip` with the app source, tests, build configuration, logo, original outlines, reports and required sources/assets, prototype, presentation, notes, and SHA-256 manifest. Install a JDK separately to build it.

`.tools`, `.local-archive`, `build`, `target`, `data`, and `dist` are ignored. The archive preserves the pre-cleanup files; it is not submitted. The group reviews changes and manages commits/pushes.

| Member | Student ID |
|---|---|
| Khaled Alkhawaldeh | 202410091 |
| Abdulrahman Alnaqbi | 202340042 |
| Yousif Alhammadi | 202310707 |
| Ahmed Alsamma | 202320192 |

Developed with AI assistance. Actual individual contributions require group confirmation; speaking assignments do not establish authorship.
