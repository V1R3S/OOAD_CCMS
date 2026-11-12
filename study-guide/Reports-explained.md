# Understanding the three Arena reports

Personal study companion — excluded from the submission ZIP.

This follows the report headings and explains the prose, tables, formulas, diagrams and evidence. It is a reading companion, not replacement submission wording. Read the corresponding passage, close it, explain its meaning aloud, then write your own explanation. Reopen the source to check that you have not changed the facts.

The reports were consolidated on 11 November 2026. Analysis received the team's replacement use-case and core-domain diagrams on 12 November, and its four UC description boxes were removed at the team's request. The assignment outline still requests a diagram plus descriptions; the former version is preserved in the private archive. Recorded application tests were executed on 10 November; these document edits do not rerun those tests.

## Before you paraphrase

- Keep requirement IDs, class/method names, formulas, numerical limits, recorded test outcomes, and actual limitations accurate.
- You can change sentence structure, examples and explanatory wording. An example must be identified as an example, rather than an experiment you performed.
- Do not turn a design goal into a measured result. “Intended to be responsive” and “measured below one second” are different claims.
- Do not turn AI-assisted work or a proposed speaking assignment into a claim that a particular student implemented it independently. Confirm actual contributions with the group and follow the course's disclosure requirements.
- Keep references supporting technical claims. Rewrite an explanation without removing its source attribution.
- Nothing about this process guarantees an AI-detector outcome. Its purpose is understanding and accurate authorship.

## Shared cover and references

**Cover details.** The university, course codes, section, instructor, names and student IDs identify the submission. They are factual labels, not prose to paraphrase. Check them for accuracy.

**Project Summary.** Arena runs as a Java desktop program. It coordinates the whole competition workflow, not merely sign-up. The key OOAD idea is that the activity being scored and the tournament structure are separate choices. “Developed for Al Ain University” describes this coursework context; it does not claim the university has officially deployed or endorsed the program.

**Version and date.** Version 2.0 identifies the report/application stage. The report date is not the test execution date. The contents pages are generated automatically by LaTeX.

**References, wherever repeated.** The OMG entry identifies the UML notation reference. The Oracle Swing entry concerns desktop components and Swing's UI thread. ObjectInputFilter concerns restrictions when reading serialized objects. The assignment outlines and instructor feedback establish the required scope. None is an independent certification that Arena works. Preserve dates only when they accurately describe the reference history; do not invent reading dates.

---

# 1. Analysis — what the system must do

Read alongside `reports/01-analysis.pdf`; edit `reports/source/analysis.tex` when you are ready.

## 1.1 Introduction

### Project Background and Problem Statement

**Paragraph beginning “Campus competitions bring together…”** Registration forms can collect names, but do not by themselves decide how to judge projects, rank programming results, or advance tournament winners. The three examples establish why competitions cannot all be handled with identical rules. “Robotics tournament” is an example of a match-based event; Arena does not simulate robots.

**Paragraph beginning “Arena provides…”** The professor specifically asked for more than registration and scheduling. Therefore, the project's main problem is handling different rules while preventing invalid actions and calculating results consistently. “Guarded workflows” means the system checks conditions before allowing an action or moving to the next phase.

**Check your understanding:** Why would a single “score” box be insufficient for all three examples?

### Project Objectives

**Objective box.** The practical objective is a runnable Java application covering teams through published results. The academic objective is to show OOAD in working code. Encapsulation keeps state changes behind controlled operations. Immutable records represent facts that are replaced rather than modified through setters. Polymorphism lets different rule objects implement the same interface. Lifecycle states make allowed actions depend on the competition's phase. Separating UI, logic and storage gives each layer a distinct responsibility.

**Paragraph after the box.** Hiding a button is not enough protection. Calling the service directly must still reject an unauthorized action. Documentation must distinguish tested behaviour from things that might be tested later.

### Project Scope

**Included paragraph.** This is the boundary of delivered functionality: account roles, team participation, entries, submission links, judging, four activity types, three progression formats, results, CSV export, local storage, audit events and supporting models. “Configurable draft rules” means the settings actually exposed by Arena; it does not mean arbitrary user-programmable scoring formulas.

**Excluded paragraph.** Arena does not connect to university login, send email, synchronize computers, run submitted code, collect payments, book rooms or upload files. It handles deadlines and rounds, but has no room/time-slot conflict calendar. These are limits, not claims that those features are broken.

### Proposed Technologies

Java 17 compatibility means the compiled application targets Java 17 even though a JDK 21 compiler was used. Swing/AWT builds the desktop interface. NIO provides file operations and locks. Java cryptography derives password hashes. LaTeX builds reports. Vector UML scales without becoming pixelated. The direct application build uses JDK facilities; Maven and optional report/prototype tools have separate build-time dependencies.

### Team Members

The table identifies four members and their IDs. The following line identifies both courses, section and instructor. This is membership information, not evidence of who wrote each component.

## 1.2 Overview

### System Description

This paragraph describes a complete journey: organizer configures an event; students form teams; captains register; judges score or decide matches; the owner publishes; signed-in users see official results. All people use one local application sequentially. It is not a server supporting simultaneous remote users.

### Target Users and Stakeholders

Read the table row by row:

- **Student:** participates and views results.
- **Captain:** a student with responsibility for a particular team, not a fifth account role.
- **Organizer:** owns events and controls their lifecycle.
- **Judge:** evaluates only assigned events.
- **Administrator:** creates accounts and can manage competitions across organizers.
- **Instructor:** evaluates the coursework; this is not an implemented login role.

### System Features

The table summarizes five workflows. Project ranking combines weighted reviews. Programming ranking uses solved count then penalty. Quiz ranking uses official points without submissions. Knockout eliminates teams across rounds. Round robin gives each pair one match and calculates a league table. This table mixes concise workflow descriptions for readability; the architecture still separates activity type from tournament format.

### Assumptions and Constraints

One running process and a trusted local computer simplify security and storage. Operating-system permissions help protect the unencrypted data file. Deadlines use local computer time. People join teams themselves. Registration locks rosters so eligibility cannot change after entry. A team-size setting is a maximum: an event allowing four people does not require exactly four. Starting needs at least two teams.

## 1.3 Functional Requirements

“Functional” means an action or behaviour the application must provide. “Shall” expresses a requirement, not a prediction. FR IDs connect requirements to tests; keep those IDs unchanged when rewriting the explanation.

### User Management

**FR-01 Authentication.** Login checks email and password. Email case does not create a different identity. Failed login clears any previous session so a failed attempt cannot leave someone acting as the last logged-in user. Signing out removes the session.

**FR-02 Accounts and credentials.** Only the administrator creates accounts and chooses their roles. Duplicate email addresses are rejected. Passwords need at least eight characters. Changing your own password requires proving you know the old one; there is no forgotten-password recovery flow.

### Competition Management

**FR-03 Competition lifecycle.** A competition starts as Draft. Its owner or an administrator controls it. The form stores its name, description, activity type, compatible progression format, relevant quiz settings, capacity, team limit, deadline and assigned judges. Rules become fixed when registration opens. The six phases move forward in order: Draft → Registration → Active → Judging → Published → Archived.

**Acceptance guards.** Each transition checks readiness. Opening registration needs a future deadline. Starting needs two entries; project/programming events also need time left for submissions. Entering Judging requires all necessary work or completed match fixtures. Quiz ranking skips submissions. Publishing projects needs every assigned review; programming/quiz ranking needs one official result per team. Match workflows must complete their certification step. Do not collapse “judging” and “publishing” into one action.

### Registration and Team Management

**FR-04 Entry validation.** Only the captain acts for a team. The service checks phase, deadline, capacity, size, duplicate registration and overlapping members. For example, a student in registered Team A cannot also enter this competition through Team B. Withdrawal is allowed during Registration.

**FR-05 Team membership.** Students create uniquely named teams, join unlocked teams and leave teams they do not captain. The general team maximum is eight, and an event may impose a smaller maximum. Registration locks membership. Captain transfer and a captain leaving their own team are not provided.

### Submission Management

**FR-06.** A submission is a title, description and web link, not an uploaded file. A registered captain can submit or replace it while Active and strictly before the deadline. Replacement leaves one current submission. Quiz and match formats reject submissions. Students see their teams' work; assigned judges and managers have competition-level access.

### Judging and Evaluation

**FR-07 Assigned review.** Being a judge is necessary but insufficient: that judge must be assigned to the event. Scoring occurs during Judging, checks relevant ranges and requires feedback. Judges can revise their own score before publication. Publication locks scores.

**FR-09 Programming results.** Inputs are solved problems (0–10), elapsed minutes (0–300) and wrong attempts (0–100). One assigned judge owns a team's official result, preventing two judges from overwriting one another. “Official” means accepted in Arena; it is still manually entered rather than calculated by executing code.

**FR-10 Knockout workflow.** Registration order determines initial seeds. Between 2 and 16 teams are supported. A non-power-of-two count needs byes: automatic first-round advancement for some teams. Judges decide actual matches during Active. A winner must belong to that match. General matches use winner selection; quiz matches use bounded scores and require a decisive result. Once a round finishes, its winners form the next round exactly once. Saved match results cannot be edited.

### Results and Leaderboard

**FR-08 Results and export.** Managers may preview results before publication. Other roles wait for official publication. Equal score-ranking results use competition ranking: two tied first-place teams are ranked 1, 1, and the next is 3. Knockout uses round reached; leagues use an ordered list of tie-breaks. CSV protection prevents user text beginning with spreadsheet formula markers from being treated as an executable formula in common spreadsheet workflows.

**FR-11 Audit.** Successful changes record who acted, when and what changed. Organizers and administrators can inspect the latest 2,000 events through the service. A failed operation must not remain recorded as a successful change. This is an application history, not a tamper-proof security log.

### Scoring Policies

**Project formula.** I, E and P mean Innovation, Execution and Presentation. For one judge, multiply them by 0.40, 0.35 and 0.25 and add them. Example: 90×0.40 + 80×0.35 + 70×0.25 = 81.5. If another judge gives 88.5, the team's average is (81.5+88.5)/2 = 85. All assigned judges must finish. Displaying two decimals does not mean the system rounds intermediate calculations. The 10^-6 tolerance treats extremely small floating-point differences as equal.

**Programming formula and order.** Penalty = elapsed minutes + 20×wrong attempts. Example: 100 minutes and three wrong attempts gives 160 penalty. Solved count is the primary comparison; a team solving nine problems beats one solving eight regardless of that eight-problem team's lower penalty. With equal solved counts, lower penalty wins. Exact ties share a rank. This is the project's simplified rule, not full ICPC compliance.

**Knockout ranking paragraph.** The champion ranks first because it won the tournament. Other teams are grouped by how far they progressed. Displayed wins count played matches, not byes. Otherwise a champion receiving a bye might misleadingly appear below someone with more played-match wins.

### Arena Extensions: Competition Type and Tournament Format

**Opening paragraph.** These were added in response to the requested Arena improvements. They supplement the academic brief rather than alter the professor's requirements.

**FR-12 Quiz scoring.** A ranking quiz uses integer points from zero to a maximum configured between 1 and 10,000. Other score fields must be zero because they have no meaning here. One judge owns the official result, and equal top scores share first place. Arena records results; someone outside the application asks questions and administers any knockout tie-break.

**FR-13 Round-robin league.** Every team plays every other team once: n(n−1)/2 matches. Four teams give 4×3/2 = 6 matches. One team cannot play two matches in a round; an odd number creates rest rounds. Results award 3 points per win, 1 per draw and 0 per loss. Sort by points, wins, score difference and scores for, all highest first; then name and stable ID ascending. The final two criteria provide a deterministic order even when sporting statistics tie. Every fixture must finish; saved results are final.

**FR-14 Compatible configurations and table.** Type answers “what do participants do?” Format answers “how is the contest organized?” Project and Programming support score ranking only. Quiz supports ranking, knockout and league. General Match supports knockout and league. That gives seven supported combinations. The service rejects others even if a caller bypasses the UI. The closing paragraph preserves existing project/programming submission workflows and the original general knockout use case.

## 1.4 Nonfunctional Requirements

These describe qualities and constraints rather than additional user actions.

### Performance

NFR-01 targets small datasets: at most 64 entries for score ranking and 16 for match tournaments. The one-second navigation target is an intention, not a measured guarantee. Password processing and saving run synchronously enough to briefly block Swing's UI thread. Larger workloads need further performance work.

### Security

NFR-02 stores password derivations with random salts instead of plaintext. It restricts service actions and limits which serialized data can be read. This does not encrypt the database or protect it from someone with unrestricted filesystem access. Hashing a password and encrypting an entire data file solve different problems.

### Usability

NFR-03 asks for understandable labels, navigation, phases, empty states and errors at a minimum 1100×720 window. Some visual and automated checks exist. There has been no formal participant study or accessibility certification.

### Reliability

NFR-04 asks for consistency across failures. A successful change is saved. A failed save restores the previous in-memory state. A lock prevents two processes writing the same data simultaneously. Corruption produces an error rather than silently creating an empty database. Atomic file replacement is used if supported, with a weaker normal replacement fallback.

### Maintainability

NFR-05 makes business rules callable and testable without opening the GUI. Strategies isolate varying policies. Linking requirements to checks helps show which rules were exercised; it does not mean every possible branch has been tested.

## 1.5 System Models

### Use Case Model

**Diagram.** The supplied image focuses on authentication and selected role actions. Actors are external roles; ellipses are actions/goals; the Arena rectangle is the system boundary. Students manage teams and register/withdraw; organizers create/configure events; judges record match results; administrators create accounts and review audit history. The image is a selected view, so omitted actions such as submissions and published results still exist in the functional requirements.

The dashed include/extend relationships are part of the supplied artwork. In the actual Java application, signing in establishes a session and each protected service operation checks it; the app does not prompt for login again on every action. Changing a password is a separate authenticated operation requiring the old password, rather than an optional step inside login. Keep that distinction when discussing the diagram's abstraction. The four former UC boxes are no longer part of this report.

### Dynamic Models

**Registration sequence diagram.** Read messages downward in time: UI request → service checks → mutation/audit → repository save → UI confirmation. Lifelines identify participating objects. Validation rejection happens before mutation; save failure invokes rollback. A successful path diagram is not a claim that failures cannot occur.

**Competition Lifecycle.** The state diagram shows the six phases and conditions on arrows. An arrow indicates a legal transition; it is not just a menu link. Archived is terminal. For match events, Judging represents completion/certification rather than project-rubric review.

**Format-Specific Workflow.** The activity diagram shows branching work: submissions/reviews for applicable ranking activities, successive rounds for knockout, and all fixtures for league. Those branches converge on readiness checks and publication. This explains why shared lifecycle names can contain different activities.

### Object and Class Model

The conceptual diagram shows business concepts and their relationships, rather than every Java helper class. A registration links one team to one competition. A team can enter multiple competitions through separate registrations. A captain is also a member. Relationships use IDs in storage. Multiplicity labels describe allowed counts, not the number currently in the demo.

### User Interface

The prose explains navigation from sign-in to overview, discovery and a competition workspace. Relevant tabs depend on the event. People is for administrators; Activity is for managers. Hiding unavailable actions improves usability, but the service repeats permission checks. The two illustrations compare a browser concept with an implemented Swing workspace; the browser's data resets on refresh.

## 1.6 Glossary & References

- **Entry:** a team's participation in a specific event, not its global team record.
- **Captain:** the student allowed to register/submit for that team.
- **Strategy:** an interchangeable implementation behind an interface.
- **Phase guard:** a condition checked before moving forward.
- **Official result:** the one judge-owned programming/quiz ranking evaluation for a team.
- **Provisional standings:** an unpublished manager preview.
- **Atomic replacement:** a filesystem replacement operation, not a promise of full database-grade crash recovery.

Read the shared reference explanation near the start of this companion.

## 1.A Interface Mockups and Implemented Screens

### Design Direction

**First paragraph:** the white sidebar, burgundy actions, pale green features and serif headings are intentional shared styling. Visual hierarchy makes title, context and details distinguishable. Cards help users enter workflows; tables compare data.

**Second paragraph:** wireframes describe layout with minimal decoration. The actual judging interface uses a reader plus scoring dialog rather than exactly copying the initial split-pane sketch. That is a documented design evolution.

**Third paragraph:** open the HTML prototype to explore design interactions. Its role switch is pretend identity, while Java authenticates real local accounts. They share rules and visual language, but have separate sample state. The browser creation preview is not persistent Java competition creation.

### Navigation and Role Paths

Each table row is a typical journey, not an exhaustive permission list. A captain creates/registers/submits; a member joins/views; an organizer configures and advances; a judge works on assignments; an administrator creates accounts and manages events. The submission path applies only to activity types that require submissions.

### Low-Fidelity Skeletons

The paragraph identifies editable SVG sketches and their generated PNGs. The overview skeleton allocates space for metrics/cards; the competition skeleton places rules, phases, actions and tabs. They demonstrate planned information structure, not working controls.

**Evaluation and bracket views:** the judging skeleton puts work and scoring together conceptually; actual Java uses dialogs. The bracket sketch explains how opponents and winners connect across rounds.

### Clickable Browser Prototype

The introductory paragraph identifies the capture source and 1440-pixel viewport. These are browser images with temporary sample data, not screenshots of Java or proof of authentication. Illustrative dates/venues are not bookings.

- **Overview and registration images:** show discovery and the registration workspace.
- **Scoring and knockout interactions:** show the live weighted-score preview and bracket interaction.
- **Quiz and League Interactions:** show official points and a league including a draw.
- **Creation Preview:** shows type/format compatibility in the browser, with saving delegated to the real Java application.

### Implemented Screens

The opening paragraph says these images came from actual Swing components rendered at 1440×1150 for documentation. That verifies captured appearance; it does not prove all interactions were manually clicked by a human.

- **Sign-in and overview:** show authentication controls and organizer summary metrics.
- **Competition discovery and workspace:** show searching/filtering and a registration-stage project event.
- **Bracket progression and published results:** contrast an active bracket with an official programming ranking.
- **Team directory and assigned judging:** show membership information and judge-scoped assignments.
- **League Fixtures and Standings:** show match entry and the manager-only provisional statistics table.
- **Quiz and Match Scoring:** show a single quiz points input versus two opponents' scores; league draws are accepted, knockout draws are rejected.
- **Competition Creation:** shows separate type and format selectors, relevant settings, judges and explanations.

### Interaction States and Accessibility

**First paragraph:** no results, missing selections and invalid entries must produce useful messages. The UI must not imply a rejected action succeeded. Draft editing and judge actions follow phase/assignment rules.

**Second paragraph:** labels, keyboard focus, text phase indicators, tooltips and a separate description reader improve usability. Twenty-two Swing checks cover selected controls, including the score preview. Screen-reader, high-DPI and formal participant studies remain unverified. Do not paraphrase this as “fully accessible.”

---

# 2. Design — how the software fulfils the requirements

Read alongside `reports/02-design.pdf`; edit `reports/source/design.tex`.

## 2.1 Introduction

### Purpose of the System

The report turns real competition concepts into software objects and operations. Swing presents the actions; ArenaService decides whether they are valid. Shared account/team/storage/publication features coexist with different scoring and progression policies. An “executable object model” means those concepts exist in working Java, not only in diagrams.

### Design Goals

The four bullets mean: reject illegal changes before saving; represent variation with separate scoring and progression interfaces; prevent accidental edits to domain records and save consistent changes; keep setup simple enough for a university demonstration. An invariant is a condition that must stay true after every successful operation, such as a winner belonging to their match.

## 2.2 Software Architecture

### Subsystem Decomposition

**Architecture diagram:** boxes group responsibilities, and arrows show interaction/dependency. It is a logical organization within one desktop program, not separate deployed servers.

**Boundary paragraph:** ArenaApp controls the window/navigation; AppDialogs handles forms; CompetitionViews builds competition-specific visuals; UiComponents supplies reusable controls; ArenaTheme supplies styling; WorkspacePanel handles available width and scrolling. The UI calls the service rather than making official score/storage decisions. The event-dispatch thread (EDT) is Swing's thread for UI work.

**Control paragraph:** ArenaService knows the logged-in account, coordinates use cases, validates permissions and phases, delegates calculations, and asks the repository to save. It is the application's central use-case entry point.

**Domain paragraph:** records represent facts. CompetitionType chooses a scoring strategy; TournamentFormat chooses a progression strategy. ProjectRules, ProgrammingRules and QuizRules implement activity scoring; the historically named KnockoutRules supplies the general match's no-rubric policy. ScoreRankingRules, KnockoutTournamentRules and RoundRobinRules implement progression. A Phase enum plus checks makes a state machine; the code does not create one State-pattern class per phase.

**Infrastructure paragraph:** Repository handles stored snapshots and locking; Passwords derives/verifies credentials; DemoData seeds fictional data only for a missing database. These support the business use cases.

### Hardware/Software Mapping

The application runs inside one Java Virtual Machine on a desktop with a display and writable storage. Running an already-built compatible JAR needs a runtime; the supplied startup script also builds it and therefore needs a JDK. Windows is the tested environment. No web/database server or network is needed for normal Java runtime operation; optional build tools may download dependencies.

### Persistent Data Management

**State paragraph:** Model.State holds collections for each kind of record plus audit/schema information. Stable IDs link records: a registration stores a team ID and competition ID rather than embedding duplicate teams. Insertion order preserves registration-based seeding.

**Repository paragraph:** a companion-file lock prevents simultaneous writers. Schema versions identify stored layouts. Version-one data is mapped to new type/format fields in memory, then saved as version two on a successful write. A 20 MB limit and deserialization filters constrain loaded objects. These are safeguards, not permission to open arbitrary untrusted databases.

**Four transaction steps:** first validate and capture the previous state; next apply the change and its audit entry; then write a validated temporary snapshot and replace the main file; on failure restore memory. Existing temporary paths are not silently overwritten. “Atomic” applies where the filesystem supports the requested replacement.

**Limitations paragraph:** this is snapshot storage, not SQL. It has no general migration framework, automated backup rotation, encrypted database, write-ahead log or power-loss guarantee. Keep a backup while the app is closed. Migration works forward for supported old data; old applications should not open newly upgraded data. Corruption stops loading instead of resetting records.

### Access Control and Security

**Permission table:** everyone signed in can browse and view published results. Only students form teams; only captains register/submit. Organizers/admins create events; owners/admins edit and advance. Assigned judges evaluate/decide matches. Only admins create accounts. Organizers/admins read audit history. Admin authority over events does not give the admin a judge's scoring identity.

**Credential paragraph:** the service sets its own session after checking a password; callers cannot choose an arbitrary actor ID for mutations. Each password has a random 16-byte salt and a 256-bit PBKDF2-HMAC-SHA256 derivation using 120,000 iterations. Repeated derivation makes guessing more expensive. MessageDigest.isEqual compares derived bytes. A salt is not the user's password and need not be secret.

**Limitations/display paragraph:** this is a local coursework boundary, with no rate limiting, password-reset email, university identity integration or encrypted storage. Demo passwords are public for demonstration. Links must be HTTP(S) but are not fetched automatically. User names are displayed as literal text rather than interpreted as Swing HTML. Escaping markup prevents a name such as an HTML tag from changing the display structure.

### Boundary Conditions

Each bullet explains a special situation: missing file → seed demo; existing file → retain records/deadlines; second writer → lock error; invalid request → error without mutation; closing → release lock; incomplete work → block transition. Successful mutations save immediately, so there is no separate Save-All prompt. Automatic forfeits/cancellation are absent, meaning some incomplete events cannot progress.

## 2.3 Subsystem Services

### User Management Subsystem

**Login/account paragraph:** login first clears a previous identity, then checks credentials. Account creation requires admin rights and a unique email.

**Password/directory paragraph:** password change verifies the old password before saving the new derivation. UserSummary returns ID, name, email and role without password data. The complete directory is admin-only; managers get a restricted judge list for assignment. Returning a safe summary is different from returning a full account and merely hiding its password column.

### Competition Management Subsystem

Creation validates bounds, deadline and judge identities. Editing is restricted to Draft. Advancing applies guards before changing phase. Starting fixture generation happens in the same transaction as starting the event: both changes succeed together or are rolled back. Unsupported type/format pairs fail before mutation.

### Registration Subsystem

register checks the captain and every entry constraint. withdraw only removes an entry during Registration. Membership changes are rejected while a team remains registered. A student may belong to several teams overall, but can participate only once in the same event.

### Submission and Evaluation Subsystem

**First paragraph:** submit replaces one entry's current work. evaluate replaces the calling judge's review. Project events need all assigned judges; programming/quiz ranking have one official score owner per entry. Quiz has points and no submission.

**Second paragraph:** recordWinner is for general knockout; it checks assignment and selected participant. recordMatchScore records quiz knockout or league scores. The result and any next round are saved together. Byes support all 2–16 knockout sizes. League fixtures are created once rather than regenerated when each score arrives.

### Results Management Subsystem

standings checks visibility and produces calculated immutable results. standingsCsv exports the same ordering rather than independently calculating rankings. CSV quoting handles commas/quotes; formula protection handles text that a spreadsheet might otherwise execute. Export correctness includes both accurate rankings and safe text formatting.

## 2.4 Low-Level Design

### Object Design Trade-offs

**Composition over a user subclass hierarchy:** one User contains a Role. Student and judge share the same identity fields; a set of nearly identical subclasses would not add useful behaviour here. Roles still receive different permissions through the service.

**Immutable records over mutable entity setters:** records and defensive collection copies prevent callers from changing a team or event behind the service's back. An update constructs a replacement record. Java records alone do not deeply freeze a mutable list; defensive copying matters.

**Strategies plus explicit workflow branching:** interfaces isolate variation, but the service still coordinates guards and aggregation. Adding a format requires changes to enums, factories, compatibility, UI and tests. This is a useful Strategy design, not a claim that extension requires zero edits anywhere.

**Snapshot persistence over SQL:** serializing all state keeps deployment simple and dependencies small. It costs a full snapshot per write and is unsuitable for large concurrent usage without redesign. A future server/database is a proposed direction, not an implemented feature.

### Final Object Design

**Strategy diagram:** look for interfaces, concrete implementations, realization arrows and the service's use of policies. Distinguish scoring from tournament progression.

**Design Class Diagram:** this overview covers 37 named production types, including nested records/enums. It is not 37 Java files and does not include test classes or every platform class.

**Application and UI Classes:** the focused sheet enlarges the implementation classes and selected fields/methods.

**Records and Relationships:** the other focus sheet shows domain data and ID links. An evaluation belongs to an entry even when quiz scoring does not require a submission. Do not draw “every evaluation requires a submission” as a universal rule.

### Packages and Dependencies

The package diagram shows which code groups know about others. UI also uses rule explanations, and the application entry point constructs Repository, so the actual design is not an absolutely isolated UI layer. The prose identifies production/test paths, JDK-only builds and optional Maven tests. The old Model package remains intentionally stable so serialized historical records can load; renaming a file alone would not safely migrate those identities.

### Class Interfaces: Attributes, Methods, Responsibilities

Read the table notation first: `name: Type` means a field's type; `method(...): ReturnType` means an operation and its result; `void` means no returned value. These are selected members, not complete class listings.

| Type | Plain-language responsibility |
|---|---|
| ArenaApp | Starts the program and navigates between application screens. |
| AppDialogs | Collects user input through forms and dialogs. |
| CompetitionViews | Displays competition cards, brackets, fixtures and results. |
| ArenaTheme | Holds shared colours and styled control creation. |
| UiComponents | Builds reusable tables, text areas and other controls. |
| WorkspacePanel | Fits content to viewport width while allowing scrolling. |
| ArenaService | Coordinates authorized use cases, current session and state changes. |
| Repository | Owns file access, loading, saving and lock lifetime. |
| DemoData | Populates a new state with fictional demonstration records. |
| Passwords | Derives password hashes and verifies supplied passwords. |
| UserSummary | Exposes identity/role information without credentials. |
| CompetitionRules | Defines the common contract for scoring/validation policies. |
| ProjectRules | Calculates the 40/35/25 rubric and requires all assigned reviews. |
| ProgrammingRules | Validates programming inputs and contributes the ranking policy. |
| QuizRules | Enforces the configured points maximum and quiz input rules. |
| KnockoutRules | Legacy-named general-match policy: no project submission/rubric. |
| TournamentRules | Defines fixture generation, progression and completion operations. |
| ScoreRankingRules | Represents progression without head-to-head fixtures. |
| KnockoutTournamentRules | Produces knockout rounds/byes and advances winners. |
| RoundRobinRules | Produces each pairing once and calculates league statistics/order. |
| Model | Contains persisted types and mappings from legacy formats. |
| State | Holds schema version, record collections and audit history. |
| User | Stores account identity, role and encoded password derivation. |
| Team | Stores team identity, captain and member IDs. |
| Competition | Stores rules/settings, owner, judges, deadline, phase and compatibility fields. |
| Registration | Connects a team to a competition. |
| Submission | Stores an entry's work description/link and submission time. |
| Evaluation | Stores one judge's inputs and feedback for an entry. |
| Match | Stores opponents, round, optional scores, winner/draw and decision state. |
| Standing | A calculated ranking row with rank, team, score and detail. |
| LeagueStanding | A calculated league row with played/won/drawn/lost, scores and points. |
| Audit | Records the actor, time and description of a successful change. |
| Role | The four account roles: administrator, organizer, judge and student. |
| Format | Historical PROJECT/PROGRAMMING/KNOCKOUT values retained for old files. |
| CompetitionType | Current activity choices and their compatibility rules. |
| TournamentFormat | Score ranking, knockout or round robin. |
| Phase | The six forward lifecycle states. |

### Invariants and Extension Example

**First paragraph:** references must point to real teams/events; overlapping entries are prohibited; project/programming reviews need submitted work; quiz does not; review uniqueness follows the type; a winner must be an opponent; publication locks scores. These are consistency conditions maintained across operations.

**Second paragraph:** a new activity needs a type, scoring policy, compatibility/factory updates and tests. A new tournament needs a progression implementation and format integration. Round robin already demonstrates independent progression: it can work with quiz or general-match activity rules.

## 2.5 Glossary & References

Facade = a central use-case entry point; Strategy = interchangeable policy; record = a compact data carrier whose fields cannot be reassigned; guard = readiness condition; rollback snapshot = a saved copy used to restore memory after failure; EDT = Swing's UI thread. These terms describe specific responsibilities, not decorations to add to every paragraph.

---

# 3. Final Report — what was delivered and how it was checked

Read alongside `reports/03-final-report.pdf`; edit `reports/source/final-report.tex`.

## 3.1 Introduction

### Project Overview

This opening explains how the implementation exceeds basic registration: weighted project judging, programming penalties, quiz points, knockout rounds and league fixtures. It connects the completed behaviour to the instructor's request. Do not describe it as an automated programming judge or online quiz engine.

### Project Objectives

The paragraph reports the deliverables: desktop UI, local persistence, four roles, validated workflows, format-aware scoring, publication controls, executable tests and three coherent reports. Testing and interface evidence are included within those three, rather than requiring five separate PDFs.

## 3.2 Implementation

### Development Environment and Technologies

**First paragraph:** describes the actual toolchain used: Windows/JDK 21 compiling for Java 17. The JDK checksum comparison concerns integrity of the downloaded compiler archive; it is not an Arena test. Swing/AWT, collections, time, cryptography and NIO are standard Java APIs. The app has no external runtime server/library requirement.

**Second paragraph:** lists alternative build/start/test routes. The PowerShell command with Test and UiTest switches runs the native suites. Maven reaches the same suites through wrappers. UI checks need a graphical environment. These are recorded commands, not something the reader can assume ran merely because they are printed.

### Implemented Features

The table is an implementation checklist. Accounts cover login/creation/password change; competitions cover setup and phases; teams cover membership; entries cover event registration; submissions cover links/work access; project/programming/quiz rows describe distinct scoring; knockout/league rows describe progression; results cover previews/publication/export; persistence covers saving/locks/rollback/audit. “Immutable match results” means they cannot be revised after confirmation, not only after publication. Do not expand these summaries into unimplemented features.

### Implementation Challenges and Solutions

- **Fairness across formats:** one generic scoring model was insufficient. Separate activity scoring from tournament progression and enforce compatibility.
- **Incomplete publication:** a partly reviewed team should not appear to have a valid final score. Require complete evidence before official results while allowing manager previews.
- **Registration consistency:** lock rosters and reject overlapping members so the same student cannot bypass eligibility through a second team.
- **Persistence failures:** restore the old state when saving fails so the screen and disk do not disagree. The intentionally blocked temporary path is a test setup, not an actual user-data incident.
- **Local build portability:** explains environment troubleshooting, portable JDK use and the writable JAR temporary path. “Sandbox” refers to the development environment's filesystem restrictions, not an Arena code-execution feature.
- **Screen quality:** a clipped paragraph was fixed by correcting layout sizing, then images were regenerated. This is specific visual evidence rather than a broad claim that all usability issues are solved.

## 3.3 Design Changes

### Deviations from the Original Design

The introductory paragraph avoids inventing an earlier approved architecture: the starting repository had outlines. The bullets describe actual implementation decisions: choose local Swing; use Role composition; separate packages while retaining persisted names; follow the prototype style; use judging dialogs; support deadlines/rounds without a full scheduling calendar. These are design choices and later refinements, not proof of formal professor approval for each one.

### Reasons for Changes

The trade-off is easy installation and a demonstrable OOAD project in exchange for limited deployment scale. Single process means sequential logins. Whole-state persistence means serializing the entire dataset per save. Fixed policies mean the rubric is not a user-defined formula editor.

## 3.4 Testing Summary

### Testing Approach

Tests call services with a controlled clock and separate data. They cover formulas, rejected actions, complete workflows, reopening storage, lock conflicts, corrupt files and a deliberately failed save. Regression tests protect behaviour added during Arena changes. Twenty-two Swing checks exercise actual controls on the EDT; rendered screens provide additional layout evidence. “Exhaustive valid tournament sizes” means counts 2–16, not every possible sequence of results or every input combination.

### Results and Quality Assessment

**Result box:** on 10 November, 98 service/regression checks and 22 Swing checks passed, with zero recorded failures. That is 120 application checks. It does not prove complete branch coverage, certified security or user satisfaction.

**Following paragraph:** Appendix B contains detailed evidence; 13 Java images were rendered and 17 separate browser checks passed. The UI/prototype share visual design. Windows is verified; high-DPI, screen-reader, other operating systems and large-data checks remain gaps. Do not report 137 Java tests: the extra 17 are prototype browser checks.

## 3.5 Project Status

### Completed Features

The paragraph inventories delivered source, build route, seeded demo, tests, models, mockups, screenshots, LaTeX and instructions. “Runnable JAR build” means the included source/build process can produce the JAR; the clean submission excludes generated build output. Original outlines remain available for requirement comparison. The private rehearsal script is now intentionally outside the ZIP; the Final Report's user guide still contains demo workflows.

### Incomplete Features and Known Limitations

Read all seven bullets carefully:

1. Single computer: no simultaneous clients, institutional login, email or remote database.
2. Programming results are entered manually; no code execution/grading sandbox.
3. Knockout/league support 2–16 teams; no double elimination, fixture times, cancellation or result correction.
4. Missing project/programming submissions can block progress; there is no automatic forfeit. Quiz/match events do not need submissions.
5. Links are stored and syntactically validated, not checked by fetching their contents; no uploads.
6. No invitations, captain transfer, deletion or forgotten-password flow. Own-password change still exists.
7. Supported old data migrates forward; manual closed-file backups remain the user's responsibility and storage is unencrypted.

These limits should stay accurate in your rewritten report. They help explain scope and trade-offs.

## 3.6 Team Collaboration

### Team Responsibilities

**Introductory paragraph:** the table is a proposed presentation/review allocation. It does not verify that each person independently implemented the assigned area.

**Table:** Khaled covers integration/traceability/demo; Abdulrahman covers registration and user instructions; Yousif covers strategies/scoring/brackets; Ahmed covers testing/persistence/review. Confirm the actual performed work before turning this into a contribution record.

**Disclosure paragraph:** code and documents were prepared with AI assistance; individual work remains unverified. Your own review, testing, explanation and revisions can be genuine contributions, but describe what each person actually did and follow the course's rules. Do not remove this passage simply to imply a different development history.

### GitHub Development Workflow

The paragraph distinguishes local changes from commits/pushes. A ZIP can contain complete working files even when the Git history does not. The stated four-commit/minimal-tracked-file history is a historical audit observation, not a description that automatically stays current after you commit. Review changes, test, check PDFs, then commit/push yourself. Generated data/tools are ignored. Speaking assignments and a current snapshot cannot prove past authorship.

## 3.7 Conclusion

The conclusion connects requirements, object design and executable behaviour: registration feeds scoring/progression, which feeds guarded publication. A future server/database and richer scheduling/recovery are possible next steps, not current deliverables. A conclusion should synthesize evidence rather than introduce new unsupported features.

## 3.8 References

Use the shared reference explanation at the beginning. Preserve source attribution and only claim source consultation that actually occurred.

## 3.A User Guide

### System Requirements

The normal supplied route needs Windows/PowerShell, a graphical desktop and JDK 17+. The local portable JDK is not bundled in the ZIP. JAVA_HOME or PATH tells scripts where Java is installed. 1100×720 is the minimum window, 1440×1000 the default. Documentation captures use another stated size.

### Installation Instructions

Copy/extract or clone the complete folder. Open it as Maven in an IDE if desired. The direct Java build has no third-party runtime dependencies to fetch. The script searches configured JAVA_HOME, optional local portable JDK, then PATH. First-use Maven/report tools can still need downloads.

### Running the Application

The first command block shows alternatives, not a sequence you must execute every time: start-app launches; build-app -Test builds/tests; java -jar starts a built JAR; Maven package builds through Maven. The UI profile includes desktop checks.

The following paragraphs explain storage: new installations use data/arena.bin; an existing historical campus.bin is reused. Choosing a new DataFile creates a separate rehearsal dataset and avoids resetting existing work. POSIX shell builds are available, but native Linux/macOS runtime behaviour was not verified. Fresh demo data and existing saved data have different deadline behaviour.

### User Roles and Permissions

The first paragraph lists fictional demo accounts and their initial shared password. These are not real email identities. Passwords are independently salted even when the initial text is the same. The second paragraph maps odd-numbered student captains to five teams, each with the following even-numbered student as member. Account identity determines permissions after sign-in.

### Step-by-Step Usage Instructions

- **Project showcase demonstration:** owner starts the seeded event; each captain submits; owner enters Judging; both assigned judges review all three entries; owner publishes; students view/export. This deliberately exercises multiple roles. Missing work/reviews blocks advancement.
- **Programming demonstration:** the existing published sample illustrates solved count first, penalty second. It does not run any submitted program.
- **Knockout demonstration:** judge3 decides two semifinals then the generated final; owner certifies and publishes. A judge decides results; the owner makes them official.
- **Quiz ranking demonstration:** judge2 enters 0–20 points for each entry; owner publishes when complete. Ties are shared and no submission step occurs.
- **League demonstration:** judge3 records the three pairings; draws award a point to both teams; owner checks the table, certifies and publishes. Statistics also appear in CSV.
- **Creating an event:** organizer selects a compatible type/format, relevant maximum, judges and deadline, then opens registration. Ctrl-click supports multiple judge selection. The ISO timestamp is a format example; choose a genuinely future date during your own demo. Captains register through their own accounts; rules then remain fixed.

### Screenshots and Examples

The paired images illustrate the actual overview and published programming results. Analysis Appendix A contains the larger collection: 13 Java captures, 7 browser captures and 4 wireframes. Their different origins must remain clearly identified.

### Troubleshooting

Each table row links a symptom to a cause/action: missing compiler → install/configure JDK; script restriction → use provided launcher without weakening global policy; data lock → close other instance/use another file; advance rejection → satisfy readiness; registration rejection → check eligibility; roster lock → withdraw while allowed/use an unlocked team; corruption → preserve and restore backup; expired demo → new event or separately named dataset. Do not delete an existing database just to make a demonstration start fresh.

## 3.B Testing Evidence

### Introduction — Purpose of Testing

The goal is observable correct behaviour even for invalid input or storage failure. Tests should assert outcomes and invariants, rather than merely repeat each implementation line in another form.

### Introduction — Testing Scope

The paragraph lists covered areas from login through persistence/rollback. Its exclusions prevent overclaiming: no network integrations, automatic grading, multi-client checks, accessibility certification or performance benchmark. Passing tests are evidence within this scope only.

### Test Strategy — Unit Testing

Focused checks isolate small policies: weighted formula, valid ranges, password verification/salts and ordering. They run within a custom dependency-free suite; a test's scope is not determined only by whether it uses JUnit annotations.

### Test Strategy — Integration Testing

**First paragraph:** service calls use the real Repository with isolated files. A fixed clock makes deadlines repeatable instead of depending on today's date. Tests log in through real actor accounts rather than trusting a passed-in “admin=true” flag. The deadline test deliberately sets time exactly on the boundary.

**Second paragraph:** reopening checks persistence; a second repository tests locking; invalid serialized input tests corruption handling; blocking the temporary path forces a save failure. The service must restore the previous team count. These actions target test data, not the user's database.

### Test Strategy — System Testing

Whole workflows cover project, programming and knockout outcomes. Swing checks manipulate real components on the EDT; this is programmatic interaction, not operating-system mouse automation. Rendered screens add visual evidence but no formal human study. A nonzero exit code makes a failed test fail the build rather than merely printing a warning.

### Test Cases — reading the detailed boxes

Requirement = the rule being checked; precondition = setup; steps = performed action; expected = intended outcome; actual = recorded observation; status = whether the assertion passed. A negative test passes when the invalid action is correctly rejected.

| Box | What the particular case demonstrates |
|---|---|
| TC-02, Authentication Test Cases | A wrong login removes the previous authorized session. |
| TC-42, Authentication Test Cases | Password change makes the old credential fail and the new one succeed. |
| TC-10, Competition Management Test Cases | Another organizer cannot advance an event they do not own. |
| TC-28, Competition Management Test Cases | Missing project reviews prevent publication. |
| TC-15, Registration Test Cases | A team cannot register twice in one event. |
| TC-18, Registration Test Cases | A student cannot gain two entries through different teams. |
| TC-22, Submission Test Cases | Resubmission replaces rather than duplicates the entry's current work. |
| TC-47, Submission Test Cases | “Before deadline” excludes a submission at exactly the deadline. |
| TC-35, Evaluation and Leaderboard Test Cases | The concrete 90/80/70 rubric calculates 81.5. |
| TC-38 and TC-41, Evaluation and Leaderboard Test Cases | Two semifinals create one final and publication puts its winner first. |
| TC-48, Evaluation and Leaderboard Test Cases | A deliberately failed save leaves the previous team count intact. |

### Test Results — Test Execution Summary

**First paragraph and native command:** 70 original service checks + 28 Arena checks + 22 Swing checks = 120 passing application checks on 10 November 2026, Windows/JDK 21 targeting Java 17. This is recorded evidence, not a fresh run today.

**Maven paragraph:** three JUnit wrapper methods invoke groups of checks. Default Maven runs the service groups and skips Swing; the UI profile adds it. Therefore a Surefire summary of three wrapper methods does not mean the project only checks three behaviours, and you must not label the 120 internal checks as 120 independent JUnit methods. CSVs/timestamps preserve the detailed outcomes.

**Browser paragraph:** 17 additional checks exercise the separate prototype, including branding and four viewport widths. They use temporary browser state and cannot prove Java authorization or data persistence.

**Commands/tool paragraph:** compile checks compilation; the UI Maven command runs the relevant suites; the browser command needs Node.js, Playwright and a browser. Optional testing tools are not dependencies required by the running Java application.

### Test Results — Passed and Failed Tests

The introductory paragraph explains that each row is a named executed check. Rows may share an intentionally ordered lifecycle setup; they are not all independent tests that can be run in arbitrary order. The source code contains the detailed assertions. The per-row companion table below explains all 70 TC entries.

### Test Results — Arena Type and Tournament Regression Results

These 28 checks protect the extended architecture, valid/invalid combinations, quiz scoring, every supported tournament size, tie-breaks, authorization, rollback/restarts and historical data migration. AR-11 and AR-15 each loop over sizes 2–16 but remain one named check each. Do not multiply the reported test count because a loop has multiple iterations.

### Test Results — Swing Interaction Results

Twenty-two checks exercise actual controls/dialogs. Their CSV and timestamp summaries describe the recorded run. UI checks are valuable, but are not a substitute for service checks or a comprehensive human usability study. Every UI row is explained below.

### Test Results — Identified Defects and Resolutions

| Item | Meaning of the finding and fix |
|---|---|
| BUILD-01 | Development restrictions blocked JAR temporary replacement; a writable build path fixed packaging. |
| BUILD-02 | PowerShell split an unquoted Java option; quoting preserved it as one argument. |
| UI-01 | A text area's requested height clipped its introduction; sizing and screenshots were corrected. |
| SEC-01 | Account APIs exposed credential-bearing records; credential-free UserSummary responses reduced exposure. |
| INPUT-01 | Missing values and relative links produced poor errors; validation now rejects before mutation. |
| SEC-02 | Bad derivation parameters and formula-like CSV text needed bounded parsing/escaping. |
| DATA-01 | A saved snapshot must remain within readable limits; closed repositories must not keep writing. |
| BUILD-03 | Custom checks were not automatically part of Maven; wrappers integrated them and line endings were corrected. |
| UI-02 | Swing can interpret some strings as HTML; user names now stay literal. |
| DATA-02 | Model renaming risked old files; preserved serialized identities and tested migration protect them. |
| UI-03 | League names needed more width, and saving needed to retain the Fixtures context. |
| RULE-01 | Activity and progression were mixed; separate strategies and compatibility checks untangled them. |
| DATA-03 | A result can generate another round; rollback must undo result, round and audit together. |

**Paragraph after the table:** no executed service check remained failing. This is narrower than “no bugs exist.” Mouse/keyboard end-to-end breadth, responsiveness, power-loss recovery and cross-platform behaviour still need more validation.

### Conclusion

The evidence supports central OOAD behaviours: varied policies, permissions, phase guards and consistent persistence. Native suites can rerun with the JDK without an external testing framework. More GUI coverage, randomized result sequences and large-data tests are sensible future checks before any real campus deployment. The references support techniques, not the reported PASS statuses.

## Every recorded test-result row, explained

All rows below explain the existing evidence, not newly executed tests. PASS for a rejection test means the forbidden action was successfully blocked. The original CSV descriptions and exact assertions remain the authority for what each test checked.

### Original service checks: TC-01 through TC-70

| ID | What its recorded check means |
|---|---|
| TC-01 | Correct credentials establish a usable session. |
| TC-02 | A wrong password does not leave the prior user's session active. |
| TC-03 | An email with no account cannot sign in. |
| TC-04 | A student cannot create accounts, even by calling the service. |
| TC-05 | An administrator can successfully create an account. |
| TC-06 | Changing letter case cannot bypass email uniqueness. |
| TC-07 | A password below the minimum length is rejected. |
| TC-08 | A student cannot create an event. |
| TC-09 | Creating an event starts it in Draft, not an active/public phase. |
| TC-10 | Organizer authority is limited by event ownership. |
| TC-11 | Teams cannot register before registration has opened. |
| TC-12 | Opening registration freezes the event's editable rules. |
| TC-13 | An event cannot start with fewer than two entries. |
| TC-14 | A captain can register their team when conditions are valid. |
| TC-15 | Repeating the same team's entry is rejected. |
| TC-16 | Being a team member does not grant the captain's registration permission. |
| TC-17 | Registered membership cannot be changed behind the eligibility checks. |
| TC-18 | A student cannot participate twice through overlapping teams. |
| TC-19 | A valid withdrawal is allowed while Registration remains open. |
| TC-20 | Required but missing submissions prevent entering Judging. |
| TC-21 | A link using an unsupported URL scheme is rejected. |
| TC-22 | Revising work replaces the current submission rather than adding another. |
| TC-23 | Student access to submissions is restricted to their own teams. |
| TC-24 | A rubric evaluation cannot be entered in an inappropriate phase. |
| TC-25 | A judge without assignment cannot evaluate that event. |
| TC-26 | Rubric inputs outside their allowed range are rejected. |
| TC-27 | Updating a judge's review leaves one review for that judge/entry. |
| TC-28 | Partial review coverage cannot become official published results. |
| TC-29 | A student cannot obtain a manager's provisional leaderboard. |
| TC-30 | Equal project averages produce shared ranks. |
| TC-31 | Publication prevents later rubric changes. |
| TC-32 | Export produces the expected CSV headings and result rows. |
| TC-33 | Archived is terminal: there is no next phase. |
| TC-34 | Programming comparison prioritizes solved count, then lower penalty. |
| TC-35 | The weighted rubric gives the expected numerical answer. |
| TC-36 | Knockout decisions require an assigned judge. |
| TC-37 | A team outside a match cannot be selected as its winner. |
| TC-38 | Completing semifinals creates one final, without duplication. |
| TC-39 | A confirmed match result cannot be overwritten. |
| TC-40 | An unfinished bracket cannot be certified as complete. |
| TC-41 | The completed final's winner appears as the published champion. |
| TC-42 | Changing a password makes the old one invalid. |
| TC-43 | Independent salts make stored derivations differ even for identical passwords. |
| TC-44 | A second repository cannot concurrently take the same data lock. |
| TC-45 | Successful changes produce audit entries. |
| TC-46 | Students cannot read the administrative audit view. |
| TC-47 | The precise deadline instant is already too late to submit. |
| TC-48 | A failed write restores the state from before the attempted change. |
| TC-49 | Knockout capacity cannot exceed the supported team limit. |
| TC-50 | Event setup rejects an already-past deadline. |
| TC-51 | A nonjudge account cannot be assigned to judge an event. |
| TC-52 | Programming keeps one official judge-owned result per team. |
| TC-53 | Complete valid programming results allow the publication workflow to finish. |
| TC-54 | A team above the event's size limit cannot register. |
| TC-55 | A student can create a new team that is initially unlocked. |
| TC-56 | Allowed membership changes survive saving. |
| TC-57 | Export escapes text that would otherwise look like a spreadsheet formula. |
| TC-58 | Reopening the application retains accounts and a published champion. |
| TC-59 | Corrupt saved data produces an error rather than silent replacement. |
| TC-60 | Account responses contain safe summaries rather than password hashes. |
| TC-61 | Only an administrator can retrieve the full account directory. |
| TC-62 | Missing credentials are rejected and clear any existing session. |
| TC-63 | Creating an account without a role fails without adding an account. |
| TC-64 | A relative link fails validation cleanly instead of causing a null-pointer error. |
| TC-65 | Malformed or excessive password-derivation parameters are rejected. |
| TC-66 | Multiline exported text also receives formula protection. |
| TC-67 | An oversized new snapshot cannot replace the last readable saved file. |
| TC-68 | A closed repository rejects writes; closing it again is harmless. |
| TC-69 | A historical database from before the refactor can still be read. |
| TC-70 | A missing winner is rejected without altering the bracket. |

### Arena regression checks: AR-01 through AR-28

| ID | What its recorded check means |
|---|---|
| AR-01 | All seven supported activity/progression combinations survive save/reload. |
| AR-02 | Unsupported pairs add neither a competition nor a misleading success audit. |
| AR-03 | Missing choices and invalid quiz maxima are rejected. |
| AR-04 | New tournament paths preserve student restrictions and organizer ownership. |
| AR-05 | Draft configuration edits persist, then become locked at registration. |
| AR-06 | Ranking quizzes enter Judging without work submissions and reject such submissions. |
| AR-07 | Quiz points obey both bounds and reject values in irrelevant score fields. |
| AR-08 | Quiz publication needs every result; tied highest scores share first place. |
| AR-09 | An assigned judge owns the official quiz result; another cannot overwrite it. |
| AR-10 | Students cannot preview private league tables or record match scores. |
| AR-11 | For every team count 2–16, knockout completes with n−1 played matches; byes are not played wins. |
| AR-12 | Tournament generation rejects duplicate/missing teams and unsupported entry counts. |
| AR-13 | A three-team quiz bracket exercises scores, a bye, progression and publication. |
| AR-14 | A five-team general bracket preserves winner selection and immutable results. |
| AR-15 | For every count 2–16, leagues include every pair once and no team twice per round. |
| AR-16 | League counters and 3/1/0 point totals match the recorded outcomes. |
| AR-17 | Tied league points are resolved by wins, difference and scores for in order. |
| AR-18 | Remaining exact ties use case-insensitive names, then stable IDs. |
| AR-19 | Incomplete leagues cannot certify; completed ones publish/export full statistics. |
| AR-20 | Quiz leagues permit draws but enforce the configured score maximum. |
| AR-21 | General leagues reject scores above their supported upper bound. |
| AR-22 | A failed knockout save undoes result, new round and audit; retry can then succeed. |
| AR-23 | A failed league save changes neither its fixture nor its calculated table. |
| AR-24 | Stored quiz and league outcomes remain after closing and reopening the repository. |
| AR-25 | Reading legacy data migrates in memory without rewriting the file; later save writes schema two. |
| AR-26 | An old active knockout can continue through to champion in Arena. |
| AR-27 | A failed quiz save restores the previous evaluation. |
| AR-28 | Publication locks match scores and Archived remains terminal. |

### Swing checks: UI-01 through UI-22

| ID | What its recorded check means |
|---|---|
| UI-01 | Login controls and the packaged university logo are available. |
| UI-02 | Logging in through the actual form displays organizer navigation. |
| UI-03 | An unsuccessful search shows an empty state; clearing it restores cards. |
| UI-04 | Filtering and selecting a card opens the intended competition. |
| UI-05 | The bracket tab renders the initial seeded round. |
| UI-06 | The Results page displays published standings. |
| UI-07 | Signing out returns to a cleared login form. |
| UI-08 | Student navigation hides organizer/judge-only functions. |
| UI-09 | Team creation calls the service and refreshes the displayed roster. |
| UI-10 | Judging studio displays only that judge's assigned events. |
| UI-11 | The winner dialog saves the selected participating team. |
| UI-12 | The administrator can open the account directory. |
| UI-13 | Cancelling account creation leaves the stored account collection unchanged. |
| UI-14 | Competition cards fit the tested minimum-size viewport. |
| UI-15 | Project inputs update the preview total and the review can be saved. |
| UI-16 | User-entered HTML-looking names remain literal text. |
| UI-17 | Arena appears in the window, login and sidebar branding. |
| UI-18 | Type selection filters format choices and shows relevant quiz controls. |
| UI-19 | The creation form actually saves a quiz league with its selected settings. |
| UI-20 | Quiz evaluation shows one meaningful points field and needs no submission. |
| UI-21 | Saving a league draw refreshes results while preserving the fixture workflow. |
| UI-22 | Workspace content fits the tested 1100×720 viewport. |

## A practical way to write your own version

For one passage at a time: (1) identify its claim; (2) explain it without looking; (3) show a matching app action, class or test; (4) write your explanation; (5) compare numbers and limitations with the original. If you cannot demonstrate or explain a claim, learn it before rewriting it. Keep analysis about requirements, design about decisions/responsibilities, and the final report about delivered behaviour and evidence.

Start with Analysis, Project Background and Problem Statement: explain why a registration spreadsheet cannot by itself handle weighted project judging and knockout rounds. Then explain why Arena separates competition activity from tournament structure. Those two ideas connect most of the project.
