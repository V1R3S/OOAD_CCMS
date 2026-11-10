# Arena presentation and speaking guide

Al Ain University, United Arab Emirates. Dr. Yazeed Ghadi. Section 61D. Courses 0103430 / 0102331.

20 main slides and four hidden technical backups, following the supplied redesigned deck. Allow about 12–15 minutes including a 3–4 minute core demo; adjust to the instructor’s actual time limit. Speaking assignments are proposed presentation duties, not verified development contributions.

| Presenter | Slides |
|---|---|
| Khaled Alkhawaldeh | 1–5 and closing 20 |
| Abdulrahman Alnaqbi | 6–11 |
| Yousif Alhammadi | 12–14 |
| Ahmed Alsamma | 15–19 and demo operation |

## 1. Arena — Khaled
**Say:** “Arena is our competition management application for Al Ain University. We will explain the requirements, object-oriented design, working application and executed tests.”

**Click / point:** Introduce the four group members. Advance to slide 2.

## 2. The problem — Khaled
**Say:** “Team entries, judging decisions and published results often become separate tasks. The important requirement is to apply the correct rules before declaring a winner. Our professor specifically requested different competition workflows beyond registration.”

**Click / point:** Point to the three coordination areas.

## 3. Shared workflow — Khaled
**Say:** “The shared journey is registration, participation, evaluation and publication. Accounts, permissions and persistence are common, while scoring and tournament progression vary.”

**Click / point:** Follow the four stages from left to right.

## 4. Actual application — Khaled
**Say:** “This image comes from the actual Java Swing application. It shows our five seeded examples, including the new academic quiz and campus league. The browser prototype is separate and does not store Java data.”

**Click / point:** Point to the type and tournament labels. This slide is a current screenshot, not the old concept film.

## 5. Type and tournament — Khaled
**Say:** “Type describes the activity. Format describes how winners progress or rank. Project and programming use ranking. Quiz supports ranking, knockout and league. General matches support knockout and league. The service blocks unsupported combinations.”

**Click / point:** Read one example across the two columns. Handoff: Abdulrahman will explain the roles and design.

## 6. Roles — Abdulrahman
**Say:** “Students manage their teams and captains register or submit. Assigned judges enter results. Organizers manage their own events. Administrators create accounts and manage all events. Captain is a responsibility of a student, not a fifth role.”

**Click / point:** Point to each role. Explain that hiding buttons is not the permission boundary.

## 7. Lifecycle — Abdulrahman
**Say:** “A competition moves forward from Draft to Archived. Guards reject premature transitions. Projects and programming need submissions and reviews. Quiz ranking needs official points. Match tournaments need all required matches completed before certification and publication.”

**Click / point:** Trace the actual report state diagram. Distinguish Active from Judging.

## 8. Creation — Abdulrahman
**Say:** “The organizer chooses an activity first, then a compatible tournament. Relevant settings appear, including a quiz maximum for quiz events. The explanation below the form states the scoring and progression policy. Rules lock when registration opens.”

**Click / point:** Point to the separate selectors and the rules explanation.

## 9. Architecture — Abdulrahman
**Say:** “ArenaApp and its UI helpers call ArenaService. The service enforces use cases, permissions and lifecycle. Strategies implement scoring and progression. Repository saves local snapshots. This is one Java desktop process.”

**Click / point:** Trace the report architecture diagram from presentation to service and persistence.

## 10. Design Class Diagram — Abdulrahman
**Say:** “This Design Class Diagram includes every named production type: 37 classes, interfaces, records and enums. The names match the Java source. It shows selected fields and operations, with readable focused sheets in the Design Report. Records keep stable identifiers for relationships.”

**Click / point:** Point to UI, service, the two strategy families and model records. Do not try to read every member aloud. Open the report detail sheets if asked.

## 11. Strategies — Abdulrahman
**Say:** “CompetitionRules selects project, programming, quiz or general-match scoring policy. TournamentRules separately selects ranking, knockout or round-robin progression. The service calls these interfaces without duplicating fixture algorithms. That is Strategy and polymorphism.”

**Click / point:** Point to the two interfaces and their realizations. Handoff to Yousif for concrete scoring examples.

## 12. Project scoring — Yousif
**Say:** “Innovation, execution and presentation carry weights of 40, 35 and 25 percent. Marks 90, 80 and 70 give 81.50. The final team score averages all assigned judges, and publication requires every expected review.”

**Click / point:** Point to each term, then the total. Explain that 81.50 is one review, not necessarily the final average.

## 13. Programming ranking — Yousif
**Say:** “Solved problems decide first. Equal solved counts compare elapsed minutes plus twenty minutes for each wrong attempt. Alpha and Innovators both solved eight; penalties 220 and 230 put Alpha ahead. Code Warriors solved nine and wins despite its higher penalty.”

**Click / point:** Follow the table. State that judges enter official results manually; Arena does not execute code.

## 14. Tournament progression — Yousif
**Say:** “Knockout supports every count from two to sixteen and gives byes where needed. A full current round must finish before the next appears. Round robin schedules every pair once and uses three points for a win, one for a draw. Its documented tie-break order gives deterministic standings.”

**Click / point:** Compare the two columns. Handoff: Ahmed will explain validation, testing and the live demo.

## 15. Registration sequence — Ahmed
**Say:** “The captain selects a competition and team. ArenaService checks captain ownership, phase, deadline, capacity and roster conflicts. It changes state and audit, then Repository saves. Failed validation changes nothing; a failed save restores the prior snapshot.”

**Click / point:** Follow the actual report sequence diagram downward.

## 16. Safeguards — Ahmed
**Say:** “Authorization lives in the service. Passwords use salted PBKDF2. Account listings return credential-free summaries. Repository uses a process lock and guarded saves; failed writes roll back. CSV export neutralizes formula prefixes. The trusted local snapshot is not encrypted.”

**Click / point:** Point to the safeguards without claiming cloud-level security.

## 17. Executed tests — Ahmed
**Say:** “On November 10 we executed 98 service checks and 22 Swing checks, all passing. The service total includes 70 original checks and 28 Arena regressions. Seventeen browser-prototype checks also passed. Maven runs three wrapper methods around the application suites.”

**Click / point:** Point to 120 and the split. Explain that tests include legacy files, failed saves and every match entrant count from two to sixteen.

## 18. Scope — Ahmed
**Say:** “Quiz scoring and round-robin tournaments are implemented in this release. Cloud synchronization, automated code execution, quiz question delivery, venue scheduling, forfeits and editing confirmed match results remain outside scope.”

**Click / point:** Keep implemented and excluded features distinct.

## 19. Live demo — Ahmed
**Say:** “We will complete the Robotics Challenge in Java, showing judge, organizer and student permissions. If time permits, we will also enter quiz points and a league draw.”

**Click / point:** Switch to Arena and follow the detailed cue sheet below.

## 20. Closing — Khaled
**Say:** “Arena brings competition workflows into one application. Our OOAD decisions separate shared use cases, activity scoring and tournament progression. We verified the important rules and preserved existing saved data. Thank you; we are ready for questions.”

**Click / point:** Return from the app to slide 20. The remaining slides are hidden technical backups.

## 21. Use cases — Abdulrahman
**Say:** “All four roles share account, browsing and published-results capabilities. The other associations identify the actions specific to each role.”

**Click / point:** Open only for a question about actors or permissions.

## 22. Analysis class model — Abdulrahman
**Say:** “This analysis view shows domain concepts. The Design Class Diagram on slide 10 is the complete implementation type inventory. Registration links a team and event, and evaluations attach to entries so quiz does not need a submission.”

**Click / point:** Explain multiplicity and the difference between conceptual and implementation views.

## 23. Activity diagram — Yousif
**Say:** “The workflow branches into ranking, knockout and league. Project and programming require work submissions, quiz ranking does not. All branches must satisfy completion guards before publication.”

**Click / point:** Trace the relevant branch when answering a workflow question.

## 24. Packages — Ahmed
**Say:** “The dependency diagram separates UI, service, rules, persistence and utilities. Persisted Model names remain in their original namespace for serialization compatibility; this is an internal technical identifier.”

**Click / point:** Point out the inward dependencies and the deliberate compatibility exception.

## Live demo: exact rehearsal steps

Install JDK 17+ and open a fresh dataset before class. Never delete your working data to reset the demo. Use a different filename each rehearsal.

```powershell
.\start-app.ps1 -DataFile data/presentation-01.bin
```

All seeded passwords are `Campus@2026`. Keep credentials in this private cue sheet rather than on public-facing slides. Ahmed operates throughout; the other speakers explain.

| Step | Click / enter | Speaker and explanation |
|---|---|---|
| 1 | Sign in `judge3@campus.test`. Open Judging studio, Robotics Challenge, Bracket. | Khaled: the assigned judge can record results. |
| 2 | Record Team Alpha over Code Warriors. Confirm. | Yousif: one semifinal remains, so there is no final yet. Bracket stays selected. |
| 3 | Record Innovators over Circuit Breakers. Confirm. | Yousif: the final now appears automatically. |
| 4 | Record Team Alpha over Innovators. | Yousif: Alpha is champion; publication is still separate. |
| 5 | Sign out, sign in `organizer1@campus.test`, open Robotics Challenge. Click Certify bracket, then Publish results. | Abdulrahman: only the owner or administrator can advance the event. |
| 6 | Sign out, sign in `student1@campus.test`, open Results. | Khaled: the published champion is now visible to the student. |
| 7 | Close and reopen using the same demo filename. Sign in again and open Results. | Ahmed: the result persists; the session does not. |

### Optional quiz and league extension

1. Sign in as `judge2@campus.test`. Open Academic Quiz in Judging studio. Record Alpha 20, Code Warriors 20 and Innovators 10, with feedback for each. Sign in as organizer1 and publish. Show the shared 1, 1, 3 ranks. Quiz ranking has no submission tab.
2. Sign in as judge3 and open Campus League, Fixtures. Enter Alpha–Warriors 2–1, Alpha–Innovators 1–1, Warriors–Innovators 0–2 (reverse input order if the displayed fixture reverses the teams). After all three results, organizer1 can certify completed matches and publish. Innovators and Alpha each have four points and one win; Innovators wins on score difference (+2 versus +1).
3. Show competition creation as organizer1. Select Quiz and inspect all three formats, then Project and observe ranking only. Cancel if you do not want another draft.

## What the professor is likely assessing

The supplied approval explicitly asks for scope beyond registration and scheduling, with variations in rules and workflows. Prepare to explain actors and requirements, why these classes have these responsibilities, how Strategy supports the variations, and how a real action passes from UI through service to persistence. Demonstrate a successful workflow and explain a rejected action. This is an inference from the supplied instructions, not a confirmed marking rubric or time limit.

## Short answers for questions

- **Why two interfaces?** Scoring describes the activity; progression describes the tournament. Quiz can reuse knockout and league scheduling without duplicating them.
- **Are these every Java class?** Slide 10 includes all 37 named production types, including records and enums, with selected members. The report has larger detail sheets. Tests, JDK types and anonymous adapters are not part of that production inventory.
- **Why does Model retain an old namespace?** Serialized snapshots contain class names. Keeping the persisted names enables tested migration without deleting user data.
- **How are ties handled?** Project/programming/quiz ranking shares equal ranks. League uses points, wins, score difference, scores for, case-insensitive name, then ID. Quiz knockout requires a decisive external tie-break.
- **Is it a website or database server?** No. Java Swing is the persistent local app. The browser prototype is a separate temporary walkthrough.
- **What proves it works?** Executed test logs, a live workflow and a restart using the same data file. Automated tests do not replace a formal usability study.
- **Who developed each part?** Give only contributions the group can confirm. Speaking assignments do not prove authorship.

The old concept video has been replaced with a current Java screenshot so no outdated product branding or unimplemented feature claims remain in the presentation.
