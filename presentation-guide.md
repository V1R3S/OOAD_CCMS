# CCMS presentation script

Open the [PowerPoint](output/presentation/CCMS-OOAD-presentation.pptx) in Presenter View. The deck contains **13 main slides and four hidden UML backup slides**. Slides 1–13 keep the original speaking assignments. Each slide has its speaker, spoken lines and pointing cues in its notes. Use the live cue sheet below while the Java window is on screen.

Allow six to eight minutes for the main slides and two to three minutes for the demo. These are rehearsal estimates, not a time limit supplied by the instructor. Backup slides are for questions. These assignments describe who presents each topic, not who originally implemented it.

| Speaker | Main slides | Topic | Live part |
|---|---|---|---|
| **Khaled Alkhawaldeh** | 1–3 | Problem, competition formats and actors | Introduce the live scenario and close on the student result. |
| **Abdulrahman Alnaqbi** | 4–6 | Domain model, Strategy and lifecycle | Explain certification and publication as Ahmed operates the organizer account. |
| **Yousif Alhammadi** | 7–9 | Project scoring, programming ranking and knockout rounds | Call the two semifinals and final as Ahmed records the winners. |
| **Ahmed Alsamma** | 10–13 | Registration, interface comparison, tests and demo introduction | Operate the Java app for the full demo, including all three accounts. |

## Before class

1. Ahmed launches the Java app from the project folder with an unused data filename. Use a different filename for the actual presentation so both semifinals start undecided.

   ```powershell
   .\start-app.ps1 -DataFile data/rehearsal-01.bin
   ```

2. Sign in as **judge3@campus.test** with the seeded password **Campus@2026**. Click **Judging studio**, then **Explore competition** on the **Robotics Challenge** card, then **Bracket**. Check that both semifinal matches offer **Record winner**.
3. Leave the Java window there and put the PowerPoint on slide 1. Ahmed keeps the mouse for the live demo. Each speaker advances the deck with the right-arrow key after their line.

## Khaled Alkhawaldeh · slides 1–3

**Slide 1 · Campus Competition Management System**

“Good morning, Dr. Ghadi. Imagine you’re organizing a hackathon, a programming contest and a robotics tournament. All three need team registration. What changes is how you decide who won. That’s the problem we designed our Java application to solve.”

**Slide 2 · What determines the winner?**

“These are the three events in the app. In the hackathon, judges review a project. In programming, solved problems and time penalties determine the ranking. In robotics, a team has to win its matches to advance.”

**Slide 3 · Actors and use cases**

“At the top left, Campus user represents capabilities shared by all four roles. Below it, students manage teams and captains register or submit work. Judges evaluate entries or record match winners. The owning organizer or an administrator advances the event and publishes results. Abdulrahman will connect these actions to the domain model.”

**Point or click**
Point to Campus user, then Student, Judge and Configure / advance event.

## Abdulrahman Alnaqbi · slides 4–6

**Slide 4 · Conceptual domain model**

“Start with Team in the upper middle, then follow the line down to Registration. Each registration links one team to one competition. For project and programming entries, it can have one current submission. Judges evaluate that submission. The Match class supports the separate knockout workflow.”

**Point or click**
Point to Team, Registration, Competition, then Submission, Evaluation and Match.

**Slide 5 · Competition rules as strategies**

“On the right, CampusService depends on the CompetitionRules interface. The three implementations below supply different validation and scoring behavior. That is our use of polymorphism. ProjectRules calculates a weighted rubric. ProgrammingRules interprets solved problems and penalties. KnockoutRules requires no submission, while CampusService manages the rounds.”

**Point or click**
Point to CampusService, the interface, and each of the three implementations.

**Slide 6 · Competition lifecycle**

“Follow the top row from Draft to Active, then down to Judging. Projects and programming contests need every registered team's submission before judging. A knockout needs a completed bracket. The owner or administrator requests each transition, and the service enforces its guard. Yousif will explain the different scoring rules.”

**Point or click**
Follow Draft, Registration and Active, then the downward arrow to Judging and left to Published.

## Yousif Alhammadi · slides 7–9

**Slide 7 · Project judging**

“Suppose one judge gives Team Alpha 90 for innovation, 80 for execution and 70 for presentation. With weights of 40, 35 and 25 percent, that review is worth 81.50. The team’s final score is the average across all assigned judges.”

**Slide 8 · Programming standings**

“Code Warriors wins with nine problems solved. Alpha and Innovators both solved eight, so we compare penalties. Alpha took 180 minutes and made two wrong attempts. Twenty minutes per attempt brings its penalty to 220, ten minutes better than Innovators.”

**Slide 9 · The knockout final opens after both semifinals**

“After the first semifinal, we know one winner but the final does not exist yet. Once both winners are recorded, the service creates exactly one final. We will show that moment live. Ahmed, what does the application check before it saves an action?”

**Point or click**
Point to the two semifinal matches and the waiting final column.

## Ahmed Alsamma · slides 10–13

**Slide 10 · Team registration sequence**

“Read this from top to bottom. The captain selects a team in CampusApp, which calls register on CampusService. The service checks authority, phase, deadline, capacity and roster conflicts. It then updates the state and calls Repository.save. Success refreshes the view. If saving fails, the service restores the previous state.”

**Point or click**
Trace the call from Captain to CampusApp, then CampusService and Repository. Finish on the dashed return messages.

**Slide 11 · Prototype and Java application**

“On the left is the browser prototype we used to try the navigation. On the right is the Java app. Both use the same sidebar and competition layout. In Java, the signed-in account determines which actions are available, and completed actions are saved locally.”

**Slide 12 · Verification on Windows**

“On Windows, 70 service checks and 16 Swing checks passed. We tested whether missing reviews block publication, whether two semifinal wins create exactly one final, and whether a failed write rolls back the change. The Swing checks also exercised the real scoring dialog.”

**Slide 13 · Live demo**

“Let's run Robotics Challenge. We will decide the matches as a judge, publish as the organizer, then see what a student sees. I will drive the app while the team explains each step.”

**Point or click**
Switch to the prepared Java window. Khaled introduces the scenario, then Yousif narrates the matches.

## Live demo cue sheet

Ahmed performs every click. The named speaker delivers the line. Reopen **Bracket** after saving each match because the refreshed workspace opens on **Teams**. Use **Campus@2026** for all three demo accounts.

### 1. Open the bracket

**Ahmed clicks**
Switch from slide 13 to the prepared Java window. Show Robotics Challenge on its **Bracket** tab.

**Khaled says**
“Watch who can decide a match and who can make the result public.”

### 2. First semifinal

**Ahmed clicks**
In the Team Alpha / Code Warriors semifinal, click **Record winner**. Choose **Team Alpha** and click **OK**. Click **Bracket** again. Point to **Awaiting round winners** in the final column.

**Yousif says**
“Team Alpha beats Code Warriors. The final still has to wait for another winner.”

### 3. Second semifinal

**Ahmed clicks**
In the Innovators / Circuit Breakers semifinal, click **Record winner**. Choose **Innovators** and click **OK**. Reopen **Bracket**. Point to the new **FINAL** match.

**Yousif says**
“Innovators wins the other semifinal. Now both opponents are known, and the final appears.”

### 4. Final

**Ahmed clicks**
In the **FINAL** column, click **Record winner**. Choose **Team Alpha**, click **OK**, and reopen **Bracket** to show the **CHAMPION** panel.

**Yousif says**
“Team Alpha wins the final. The champion is decided, but the organizer still has to publish the results.”

### 5. Certify

**Ahmed clicks**
Click **Sign out**. Sign in as **organizer1@campus.test**. Click **Competitions**, then **Explore competition** on **Robotics Challenge**. Click **Certify bracket** and confirm **OK**. Point to **Judging**.

**Abdulrahman says**
“The final now has a winner, so the organizer can certify the bracket and move it into Judging.”

### 6. Publish

**Ahmed clicks**
Click **Publish results** and confirm **OK**. Show the **Published** phase.

**Abdulrahman says**
“Certification and publication are separate steps. This one makes the standings official.”

### 7. View and close

**Ahmed clicks**
Click **Sign out**. Sign in as **student1@campus.test**. Click **Results** in the sidebar. Scroll to **Robotics Challenge** and point to Team Alpha and Innovators.

**Khaled says**
“As a student, I can see Team Alpha first with two wins and Innovators second with one. All three formats share this publication step, even though they produce a winner differently. Thank you. We’re ready for questions.”

## UML backup slides for questions

The normal slide show skips slides 14–17. Do not advance through them during the main talk. For a question, return to PowerPoint and open the relevant slide from the thumbnail pane. The named speaker gives the short answer below.

| Slide | Diagram | Speaker | Use when asked about |
|---|---|---|---|
| 14 | Format-specific workflow | Yousif Alhammadi | Differences in format workflows |
| 15 | Software architecture | Ahmed Alsamma | Separation of responsibilities |
| 16 | Final domain relationships | Abdulrahman Alnaqbi | Record attributes and relationships |
| 17 | Package dependencies | Ahmed Alsamma | Code organization and dependencies |

**Slide 14 · Format-specific workflow**

“This activity diagram shows where the workflows separate. The left branch receives submissions and collects scores. The right branch generates a bracket and records winners until there is a champion. Both branches check completeness before publishing results.”

**Point**
Point to the Format decision, trace each branch, then the merge before publication.

**Slide 15 · Software architecture**

“The Swing interface calls CampusService. The service controls use cases and permissions, uses the domain rules, and asks Repository to save state. Keeping these responsibilities separate lets us test business rules without opening the UI. This is one local Java process, with a snapshot file for storage.”

**Point**
Trace Presentation to CampusService, then Domain and Repository, and finally Local Storage.

**Slide 16 · Final domain relationships**

“This view adds types to the conceptual model. Records connect through identifiers. A registration refers to one team and competition, and a match stores its participants and optional winner. These are selected attributes. The implementation uses immutable Java records and replacement values for updates.”

**Point**
Point to the typed IDs in Registration and the winner field in Match.

**Slide 17 · Package dependencies**

“The packages separate interface code, application services, rule strategies and persistence. Passwords belongs in util. The persisted domain records keep their original Model names so existing data remains readable. This diagram shows the main dependencies. The UI also reads rule explanations, and CampusApp constructs Repository at startup.”

**Point**
Follow ui to service and its dependencies, then point to Model / model.

## What the professor is likely expecting

His approval message explicitly asks for clear object-oriented analysis and design, different competition formats, and differences in their rules and workflows. The supplied assignment outlines specify analysis models, software design, implementation and testing reports. They do not specify a separate presentation rubric or a speaking time limit. The priorities below are preparation advice based on that evidence.

- Explain the problem and the three formats briefly. Use the formats to show why registration alone is insufficient.
- Connect a requirement to UML and then to code. For example, captain registration becomes the sequence diagram and CampusService.register.
- Explain the object-oriented decisions. Cover the CompetitionRules interface and polymorphism, immutable records, service authorization, and separation of UI from persistence.
- Demonstrate one complete workflow. The planned judge, organizer and student demo shows progression and publication. Keep the programming tie-break and a rejected invalid action ready if asked.
- Explain the test evidence and limits honestly. The recorded 3 November Windows runs passed 70 service checks and 16 Swing checks. Linux/macOS execution and a formal usability study remain unverified.
- Be ready to explain your own work accurately. Speaking assignments do not establish historical contributions, and everyone should understand the design decisions they discuss.

During rehearsal, each person should answer “Why did we design it this way?” without reading. Explain one meaningful relationship on a diagram rather than reading every box. Keep the demonstration focused and avoid clicking through every screen.

Likely follow-up questions include why Strategy fits this problem, why roles use an enum, where authorization is enforced, what changes for a fourth format, and how the service recovers after a failed save.

Sources for preparation are the instructor approval message supplied in the conversation and the original outlines in `requirements/system-analysis-requirements.txt`, `requirements/system-design-requirements.txt`, `requirements/implementation-report-requirements.txt` and `requirements/testing-requirements.txt`.

## Extra examples for questions

Use these only if the instructor requests more detail. The planned demo ends with the published Robotics Challenge standings.

## 1. Explain the design (2 minutes)

Show the three competition cards. Explain that registration is shared, but the formats differ in what gets submitted, who records the decision, how readiness is checked, and how results are calculated. Open `CompetitionRules.java`, then show the explicit lifecycle guards in `CampusService.advance`.

Do not claim that the project uses the GoF State pattern or a database server. It uses an enum-based state machine and local snapshot persistence. Explain why immutable records and service-level authorization prevent the UI from being the only enforcement layer.

## 2. Project judging (5 minutes)

1. Sign in as `organizer1@campus.test`. Open **Hackathon 2026** and start it. Three teams are already registered.
2. Try **Begin judging** immediately. The missing-submission guard rejects it.
3. Sign in as each captain: student1 (Alpha), student3 (Code Warriors), student5 (Innovators). Submit a title, description, and `https://example.com/project` link.
4. As organizer1, begin judging.
5. As judge1 and then judge2, evaluate all three submissions. Use Alpha 90/80/70, Warriors 80/80/80, Innovators 70/70/70 for both judges. Expected averages: 81.50, 80.00, 70.00.
6. As organizer1, publish. As student1, open Results and show the official leaderboard.

## 3. Programming tie-break (1 minute)

Open the seeded published **Programming Contest**. Code Warriors has 9 solved, penalty 260. Alpha and Innovators each have 8 solved; Alpha's penalty 220 beats Innovators' 230. Scores are judge-entered; the application does not execute code.

## 4. Bracket progression (2 minutes)

1. As judge3, open **Robotics Challenge → Bracket**.
2. Record Alpha and Innovators as semifinal winners. A final appears only after both results are saved.
3. Record Alpha as champion. Finalized matches cannot be edited.
4. As organizer1, certify the bracket, then publish. Alpha has 2 wins; Innovators 1; both semifinal losers share rank 3.

## 5. Persistence and evidence (1 minute)

Close and reopen the app. Show that results remain. Show `docs/evidence/service-test-results.csv` and explain the deadline, role, duplicate-entry, file-lock, corrupted-data, and failed-write rollback tests. The suite is run with `build-app.ps1 -Test -UiTest`.

## Questions to prepare for

- Why composition for roles? All roles share identity; permissions differ by use case.
- Why Strategy? Project and programming interpret evaluation fields differently; knockout uses a different progression workflow.
- What still changes for a new format? Enum/factory, policy, relevant service aggregation/guards, UI labels, and tests.
- Why lock team rosters? Otherwise a student could join a registered team after passing duplicate-member checks.
- Why cannot missing work be ignored? The current policy requires complete participation; explicit forfeit handling is future work.
- How would you scale it? Move to a transactional database and server-side sessions before supporting multiple clients.
