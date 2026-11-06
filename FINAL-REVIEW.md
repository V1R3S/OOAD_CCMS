# Final project review

Review date: 3 November 2026 (Asia/Dubai). Project: Campus Competition Management System, version 1.1.

## A. Changes implemented

### Application structure and code

Production code now separates `ui`, `service`, `repository`, `rules`, `util`, and `model` responsibilities. `CampusApp` retains startup, navigation, and page composition. Forms moved to `AppDialogs`; cards, brackets, and standings moved to `CompetitionViews`; repeated controls moved to `UiComponents`. `CampusTheme` retains the established visual identity. Each competition policy has its own source file.

The serialized records deliberately remain in `edu.aau.ccms.Model`. Moving their binary class names would break existing snapshots. The service owns its state privately and exposes immutable records or copied collections. A preserved fictional pre-refactor database provides compatibility evidence. This is a documented compatibility exception, not an incomplete accidental move.

Source names follow Java conventions. Assignment outlines have descriptive filenames; their original contents were compared against Git and are unchanged. The sample-data guide points to the relocated seed class.

### Security and reliability

Account methods (`login`, `currentUser`, `createUser`, and `users`) now return `UserSummary`, containing only ID, name, email, and role. The complete directory is administrator-only; organizers use a separate restricted judge picker. Stored credentials remain internal to authentication and persistence.

Input validation handles null credentials, missing account roles, relative submission URLs, and null match winners without corrupting state. Password verification rejects malformed or excessive derivation parameters. CSV formula protection handles multiline fields. Swing labels, tables, and selection controls display user text literally; intentionally formatted text and tooltips escape markup.

Saves validate size and readability before replacing the current snapshot, create temporary files exclusively, flush content, and preserve pre-existing temporary obstructions. Cleanup does not mask the original write failure. Closed repositories reject writes; close is idempotent. Service rollback preserves memory when persistence fails. Existing locking, filtered deserialization, and atomic replacement with a normal-replacement fallback remain in place.

### Build, tests, documents, and repository

Maven now runs the existing acceptance suites through JUnit Jupiter and Surefire. The native scripts remain usable with a JDK alone. The shell script has LF endings, respects `JAVA_HOME`, and handles POSIX versus Windows-shell classpaths. Git attributes preserve line endings. PowerShell builds clean only their verified compiled-output directories and fall back correctly when `JAVA_HOME` is invalid.

The 3 November audit reviewed the eight original UML sources. Seven were corrected; the accurate Strategy diagram was retained. Changes included inherited common use cases for every actor (including published results), organizer/admin audit and management associations, domain cardinalities, activity start/end and branch merge, valid publication guards, realistic save wording, and the new physical packages. Relationship lines represent ID associations, not invented composition. On 6 November, these illustrations were replaced at the team's request with nine supplied SVG diagrams, including separate conceptual and final domain models. Their vector PDF versions are embedded on landscape pages in the analysis and design reports.

All five LaTeX reports were revised and rebuilt with the supplied AAU logo. The analysis and interface reports now embed actual browser-prototype captures. The interface report includes four prototype screens, eight Java screens, and four wireframes. The prototype's role switch, temporary data, and illustrative events are distinguished from Java authentication and persistence. Proposed individual responsibilities remain explicitly unverified.

A submission script uses an allowlist and SHA-256 manifest. Tools, caches, databases, Git internals, compiled output, and temporary PDF renders are excluded from the ZIP. Necessary report illustrations remain included because LaTeX depends on them.

## B. Bugs fixed

| Defect | Resolution and regression evidence |
|---|---|
| Password derivations exposed in general account responses | Credential-free summaries and role-restricted directories; TC-60 and TC-61 |
| Null credentials could fail inconsistently | Generic rejection and cleared session; TC-62 |
| Missing role could create an invalid account | Validation before mutation; TC-63 |
| Relative URL could cause a null-pointer error | Explicit scheme/host validation, including uppercase HTTPS; TC-64 |
| Unbounded/malformed password derivation parameters | Validate rounds, salt, hash, and password length; TC-65 |
| Formula escaping missed multiline CSV cells | Check the leading character independently of line breaks; TC-66 |
| Oversized writes could produce an unreadable database | Reject before file replacement; TC-67 |
| Repository could write after close | Require an open channel and valid lock; TC-68 |
| Refactor risked invalidating existing snapshots | Preserve serialized names and load old fixture; TC-69 |
| Null knockout winner caused an unhelpful failure | Validate before changing matches; TC-70 |
| User names could activate Swing HTML rendering | Literal renderers and escaped formatted text; UI-16 |
| Maven silently omitted custom acceptance tests | Two JUnit wrapper methods propagate suite failures |
| Shell line endings and Windows-shell classpath | LF Git rule and platform-aware separator |
| Package refactor left outdated documentation | Updated sources, diagrams, commands, screenshots, and PDF exports |

The original 59 service and 15 UI checks were retained. Eleven service regressions and one UI regression were added. Tests were not removed or weakened.

## C. Final file structure

```text
OOAD_CCMS/
  README.md, FINAL-REVIEW.md, presentation-guide.md
  pom.xml, .gitignore, .gitattributes, aau.png
  build-app.ps1, build-app.sh, start-app.cmd, start-app.ps1
  src/main/java/edu/aau/ccms/
    Model.java                  # Existing serialized records
    model/UserSummary.java
    service/                    # CampusService and DemoData
    repository/Repository.java
    rules/                      # Interface and three format strategies
    ui/                         # Frame, dialogs, views, widgets, theme
    util/Passwords.java
  src/test/java/edu/aau/ccms/     # Service/UI suites and JUnit adapter
  src/test/resources/            # Fictional legacy snapshot and explanation
  requirements/                  # Five unchanged assignment outlines
  sample-data/README.md
  prototype/                     # HTML/CSS/JavaScript clickable prototype
  docs/
    system-analysis/, system-design/, implementation-report/
    testing-report/, interface-mockups/
    shared/                      # AAU preamble and references
    assets/                      # Java/prototype screenshots and wireframes
    evidence/                    # Executed CSV results and summaries
  output/pdf/                    # Five submission-ready report exports
  output/presentation/           # OOAD PowerPoint with presenter notes
  scripts/                       # Report, wireframe, evidence, ZIP helpers
  dist/CCMS-submission.zip        # Generated clean submission; ignored by Git
```

`.tools`, `build`, and `target` remain local ignored development/output directories. No original project data was deleted. The only included `.bin` is the documented fictional compatibility fixture.

## D. Executed verification

Windows, JDK 21 compiling for Java 17; Maven 3.9.9. Execution summaries record UTC timestamps, corresponding to the stated local review date. Tests use isolated generated data and a fixed clock for deadline scenarios.

| Command / check | Result |
|---|---|
| `./build-app.ps1 -Test -UiTest` | 70 service + 16 Swing checks passed, zero failed |
| `mvn clean compile test -Pui-tests package` | Clean compilation and runnable JAR succeeded; 70 + 16 checks passed |
| `mvn test` | 70 service checks passed; Swing intentionally skipped without profile |
| `sh build-app.sh ui-test` under Windows Git shell | 70 service + 16 Swing checks passed, zero failed |
| `java -jar target/campus-ccms.jar --data <isolated path> --capture docs/assets/screenshots` | App launched and eight real Swing screens rendered |
| Browser prototype capture through Edge/Playwright | Four screens captured; no JavaScript page errors during the walkthrough |
| `./scripts/build-reports.ps1 -CachedOnly` | Five PDFs compiled from LaTeX; rendered pages visually reviewed |
| Assignment preservation | All five outlines matched their original Git content |
| Archive integrity / independent build | All manifest hashes passed; extracted native build passed 70 + 16 checks; extracted Maven clean compile/test passed 70 service checks |

Maven reports **two wrapper methods**, not 86 independent JUnit methods. With the UI profile both wrappers pass; without it one passes and one is skipped. Within those wrappers the runners record 70 service scenarios and 16 UI scenarios. Their CSV tables and timestamped summaries are preserved in `docs/evidence` and embedded in the Testing Report.

Coverage includes authentication, authorization, all three competition formats, scoring boundaries, registration duplicates and overlap, roster locking, deadlines, submission replacement, complete judging, publication, bracket progression, CSV, password change, file locking, corrupt data, failed-save rollback, restart persistence, old-data compatibility, real controls/dialogs, minimum-window layout, and literal user-text rendering. Swing checks use real controls on the event-dispatch thread, not operating-system mouse automation.

The build initially encountered sandbox network restrictions fetching Maven plugins; it succeeded after the declared dependencies were downloaded. Shell validation found and fixed a Windows-shell classpath issue. These intermediate failures were resolved, not counted as passing runs. Tectonic emits a local Fontconfig configuration warning; PDFs are generated with embedded fonts and visually checked.

Unverified: native Linux/macOS execution, running on a JDK 17 runtime itself, screen-reader/high-DPI behavior, full manual mouse/keyboard coverage, formal usability studies, measured load targets, and power-loss recovery. No branch-coverage percentage or security certification is claimed.

## E. Requirements coverage

| University deliverable / requirement | Final coverage |
|---|---|
| Analysis outline | Background, scope, actors, functional/nonfunctional requirements, use cases, sequence/activity/state/class models, interface paths, glossary and references |
| Design outline | Architecture, deployment assumptions, persistence, security, boundaries, subsystem contracts, detailed classes/strategies, packages and trade-offs |
| Implementation outline | Environment, features, challenges, design decisions, test summary, limitations, collaboration disclosure, Git workflow, conclusion, references and user-guide appendix |
| Testing outline | Unit/integration/system strategy, detailed cases with expected/actual results, executed results, defects and limitations |
| Sample-data outline | 10 students, 3 judges, 2 organizers, 1 admin; 5 teams, 3 competitions, registrations, submissions, evaluations, rankings and bracket matches |
| Instructor's format variation request | Project 40/35/25 multi-judge rubric; programming solved-count/penalty policy and exclusive score owner; knockout progression and certification |
| Account/team/entry management | Implemented with role, owner/captain, phase, capacity, size, duplicate and overlap checks |
| Results and persistence | Guarded publication, all-role published results, CSV, restart persistence and failure handling |
| Mock-ups and presentation | Editable wireframes, clickable browser prototype, documented Java screens, OOAD PowerPoint and demo guide |
| Collaboration attribution | Names/IDs supplied by group; actual individual contributions still require confirmation |

The supplied outlines are the completeness reference. Venue scheduling, automatic code execution, cloud services, email, byes, forfeits, and account deletion were not added. The original requirements were not edited to make the project appear complete.

## F. Remaining issues and boundaries

1. The group must confirm actual individual contributions. The report's proposed review/presentation allocation is not verified authorship.
2. The group should review and commit the staged snapshot, then push through its own workflow if submitting a GitHub link. No commit, push, history rewrite, or invented historical attribution was performed.
3. Native Linux/macOS, accessibility, high-DPI, load/performance, and power-loss validation remain outside the executed evidence. Windows is the verified environment.
4. This remains a small, trusted-computer application: one process, local unencrypted snapshots, whole-state writes, and some operations on the Swing thread. The saved file is not an adversarial import format. Missing submissions can block progression; byes, forfeits, and result correction are documented scope limitations.

No unresolved failure remains in the executed application suites. These limits do not imply that every possible defect has been excluded.

## G. Ratings

These are engineering judgments against the supplied coursework scope, not a prediction of the professor's grade.

| Area | Score / 10 | Basis |
|---|---:|---|
| Completeness | 9.0 | All supplied deliverables and three format workflows present |
| Functionality | 9.0 | Positive and negative workflows verified |
| Code quality | 8.5 | Clearer responsibilities; frame/service remain substantial |
| Object-oriented design | 9.0 | Immutable records, strategy policies, encapsulated use cases |
| Architecture | 8.5 | Proportionate layered local app; snapshot and EDT limitations |
| File structure | 9.0 | Descriptive packages and artifacts; explicit compatibility exception |
| Security | 8.5 | Service authorization, hash-free responses, bounded credentials, safe rendering/export |
| UI/UX | 8.5 | Consistent prototype/app design and exercised controls; formal usability unverified |
| Testing | 9.0 | 86 retained/added scenarios, Maven integration, storage and UI regressions |
| UML diagrams | 8.5 | Corrected relationships, transitions and package model; kept readable coursework scope |
| Documentation | 9.0 | Sources/exports synchronized, real screenshots, honest limitations |
| Git repository | 8.0 | Complete snapshot prepared; historical development evidence remains limited |
| Submission readiness | 8.5 | Technical package ready; group attribution and own commit/push remain |

**Overall: 8.7/10.** The core workflows and academic deliverables are strong and reproducible. Remaining deductions reflect local-application trade-offs, limited platform/usability evidence, and unresolved human attribution rather than missing competition functionality.

## H. Final verdict

**Technically ready for university evaluation on Windows, subject to the group's final academic review.** Confirm actual contributions and review the reports before handing them in. Commit and push the prepared source if a repository URL is part of the submission. No additional in-scope software feature is currently blocked.

The clean extraction used for the Java audit passed 70 service checks and 16 Swing checks with the native build. Its default Maven run passed the 70 service checks and skipped the optional UI suite. The package was subsequently rebuilt to include the OOAD PowerPoint and the supplied UML artwork. It contains 108 project files plus a SHA-256 manifest, including five reports and the presentation. The Java source, tests, application build scripts, POM, logo, and compatibility fixture are unchanged by the 6 November diagram replacement.

Git preparation: the reviewed project snapshot is staged for the group's own commit. The five assignment-file relocations are detected as unchanged-content renames. Build outputs and local tools remain ignored. No files necessary to build or read the reports are left untracked, and no commits or pushes were made.
