# Arena

Java desktop project for **Object Oriented Analysis and Design / System Analysis & Design**, Al Ain University.

Dr. Yazeed Ghadi · Section 61D · Courses 0103430 / 0102331

## Start here

- **Run Java:** double-click [start-app.cmd](start-app.cmd).
- **Read the reports:** [Arena-Combined-Report.docx](reports/Arena-Combined-Report.docx) contains the Analysis, Design, and Final Report as Parts I–III.
  Analysis includes interface mockups/screenshots in Appendix A. Final Report includes the user guide in Appendix A.
- **Present:** open [the PowerPoint](presentation/Arena-presentation.pptx). The current edited deck contains 21 slides. The separate speaking script is retained in the working project for rehearsal and excluded from the submission ZIP.
- **Explore the prototype:** open [prototype/index.html](prototype/index.html).
- **Working-folder companions:** `PROJECT-FILES.md` explains every maintained file, and `study-guide` contains personal learning material. These are excluded from the submission ZIP.

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

The `ui-tests` profile needs a graphical desktop and is skipped by default. The first Maven build downloads build dependencies; the app has no external runtime libraries.

Use `sh build-app.sh test` or `sh build-app.sh ui-test` on a POSIX shell. Java targets release 17.

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
| `reports/` | Combined Word report (Analysis, Design, Final Report) |
| `reports/assets/` | Necessary UML, Java/prototype screenshots, and wireframes |
| `presentation/` | Slide deck; the working folder also retains a private speaking script |
| `project-notes/` | Demo-data explanation; the internal technical review stays in the working folder |
| `requirements/` | Original assignment outlines; only the product name in the sample-data brief was updated |
| `src/` | Application, tests, and two historical compatibility fixtures |
| `prototype/` | Browser prototype with temporary sample data |
| `scripts/` | Report, asset, evidence, and packaging helpers |

The personal learning companion is kept in `study-guide/` in the working project and excluded from the submission ZIP. The file guide lists its contents.

The prototype's role switch does not authenticate users. Refresh resets its independent sample data. Java is the persistent implementation used for the live demonstration.

## Edit reports

The reports are maintained in [reports/Arena-Combined-Report.docx](reports/Arena-Combined-Report.docx). Edit it directly in Word. Keep `aau.png` at the root; the app and prototype use it.

Report images are required illustrations. SVGs are editable artwork; matching PDF/PNG versions are the report figures. See [UML notes](reports/assets/uml/README.md). These are not duplicate screenshots of report pages.

Optional prototype verification requires Node.js and Playwright: `node scripts/test-prototype.cjs --browser "path/to/browser"`. Add `--capture` to rebuild its seven illustrations. Java report images come from `java -jar build/arena.jar --capture reports/assets/screenshots`.

Version-one serialized PROJECT and PROGRAMMING records map to score ranking, and KNOCKOUT maps to general match plus knockout. The persisted `edu.aau.ccms.Model` names and legacy filename are intentional compatibility identifiers, not visible product branding. Never open upgraded data in the older application.

## Submission and Git

```sh
python scripts/package-submission.py
```

Python 3.10+ creates `dist/Arena-submission.zip` with the app source, tests, build configuration, logo, original outlines, the combined Word report and its assets, prototype, slide deck, demo-account reference, maintenance scripts, and SHA-256 manifest. Install a JDK separately to build it. The speaking script, internal technical review, file inventory, and personal study guides remain in the working folder and are excluded from the ZIP.

`.tools`, `.local-archive`, `build`, `target`, `data`, and `dist` are ignored. The archive preserves the pre-cleanup files; it is not submitted. The group reviews changes and manages commits/pushes.

| Member | Student ID |
|---|---|
| Khaled Alkhawaldeh | 202410091 |
| Abdulrahman Alnaqbi | 202340042 |
| Yousif Alhammadi | 202310707 |
| Ahmed Alsamma | 202320192 |

Developed with AI assistance. Actual individual contributions require group confirmation; speaking assignments do not establish authorship.
