# CCMS presentation script

Open the [13-slide PowerPoint](output/presentation/CCMS-OOAD-presentation.pptx) in Presenter View. Each slide’s notes contain its speaker and spoken lines. Use this sheet during the live demo, when the Java window is on screen.

Allow six to eight minutes for the slides and two to three minutes for the demo. These are speaking assignments for the presentation.

| Speaker | Slides | Topic | Live part |
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

**Slide 3 · Actors and permissions**

“A captain enters a team. An assigned judge can decide a match, but publishing the result takes the owning organizer or an administrator. Once it’s published, all four roles can see it. Abdulrahman, how do those rules appear in the model?”

## Abdulrahman Alnaqbi · slides 4–6

**Slide 4 · A registration links a team to an event**

“Follow the top row from Team to Competition. Each registration links exactly one team to one event. A team can enter other events through other registrations. For a project, the entry can hold one current submission, and several judges can review it.”

**Slide 5 · Competition rules as strategies**

“CampusService asks for the policy that matches the event’s format. ProjectRules computes a weighted score. ProgrammingRules checks solved count and penalties. A knockout uses match winners instead of a rubric, and the service creates the next round.”

**Slide 6 · Competition lifecycle**

“Look at the move from Active to Judging. For a project, every registered team must have submitted. For a knockout, the final needs a winner. The organizer requests the change, and the service checks that condition. Yousif will show what those differences mean for the teams.”

## Yousif Alhammadi · slides 7–9

**Slide 7 · Project judging**

“Suppose one judge gives Team Alpha 90 for innovation, 80 for execution and 70 for presentation. With weights of 40, 35 and 25 percent, that review is worth 81.50. The team’s final score is the average across all assigned judges.”

**Slide 8 · Programming standings**

“Code Warriors wins with nine problems solved. Alpha and Innovators both solved eight, so we compare penalties. Alpha took 180 minutes and made two wrong attempts. Twenty minutes per attempt brings its penalty to 220, ten minutes better than Innovators.”

**Slide 9 · The knockout final opens after both semifinals**

“In Robotics Challenge, the first semifinal fills one place in the final. The other place stays open until the second winner is recorded. Then the service creates exactly one final. We’ll show that moment live. Ahmed, what does the application check before it saves an action?”

## Ahmed Alsamma · slides 10–13

**Slide 10 · What happens when a captain registers?**

“When a captain clicks Register my team, the form sends the team and event IDs to CampusService. The service checks the captain, registration phase and roster before asking the repository to save. If the write fails, the previous state is restored.”

**Slide 11 · Prototype and Java application**

“On the left is the browser prototype we used to try the navigation. On the right is the Java app. Both use the same sidebar and competition layout. In Java, the signed-in account determines which actions are available, and completed actions are saved locally.”

**Slide 12 · Verification on Windows**

“On Windows, 70 service checks and 16 Swing checks passed. We tested whether missing reviews block publication, whether two semifinal wins create exactly one final, and whether a failed write rolls back the change. The Swing checks also exercised the real scoring dialog.”

**Slide 13 · Live demo**

“Let’s run Robotics Challenge. We’ll decide the matches as a judge, publish as the organizer, then see what a student sees. I’ll drive the app. Yousif, talk us through the bracket.”

## Live demo cue sheet

Ahmed performs every click. The named speaker delivers the line. Reopen **Bracket** after saving each match because the refreshed workspace opens on **Teams**. Use **Campus@2026** for all three demo accounts.

### 1. Open the bracket

**Ahmed clicks** — Switch from slide 13 to the prepared Java window. Show Robotics Challenge on its **Bracket** tab.

**Khaled says** — “Watch who can decide a match and who can make the result public.”

### 2. First semifinal

**Ahmed clicks** — In the Team Alpha / Code Warriors semifinal, click **Record winner**. Choose **Team Alpha** and click **OK**. Click **Bracket** again. Point to **Awaiting round winners** in the final column.

**Yousif says** — “Team Alpha beats Code Warriors. The final still has to wait for another winner.”

### 3. Second semifinal

**Ahmed clicks** — In the Innovators / Circuit Breakers semifinal, click **Record winner**. Choose **Innovators** and click **OK**. Reopen **Bracket**. Point to the new **FINAL** match.

**Yousif says** — “Innovators wins the other semifinal. Now both opponents are known, and the final appears.”

### 4. Final

**Ahmed clicks** — In the **FINAL** column, click **Record winner**. Choose **Team Alpha**, click **OK**, and reopen **Bracket** to show the **CHAMPION** panel.

**Yousif says** — “Team Alpha wins the final. The champion is decided, but the organizer still has to publish the results.”

### 5. Certify

**Ahmed clicks** — Click **Sign out**. Sign in as **organizer1@campus.test**. Click **Competitions**, then **Explore competition** on **Robotics Challenge**. Click **Certify bracket** and confirm **OK**. Point to **Judging**.

**Abdulrahman says** — “The final now has a winner, so the organizer can certify the bracket and move it into Judging.”

### 6. Publish

**Ahmed clicks** — Click **Publish results** and confirm **OK**. Show the **Published** phase.

**Abdulrahman says** — “Certification and publication are separate steps. This one makes the standings official.”

### 7. View and close

**Ahmed clicks** — Click **Sign out**. Sign in as **student1@campus.test**. Click **Results** in the sidebar. Scroll to **Robotics Challenge** and point to Team Alpha and Innovators.

**Khaled says** — “As a student, I can see Team Alpha first with two wins and Innovators second with one. All three formats share this publication step, even though they produce a winner differently. Thank you. We’re ready for questions.”

## Extra examples for questions

The following walkthroughs are available if the instructor wants more detail. The planned live demo ends with the published Robotics Challenge standings.

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
