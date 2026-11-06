# Fictional demonstration dataset

The executable seed is `src/main/java/edu/aau/ccms/service/DemoData.java`. It runs only when the chosen data file does not exist. No account belongs to a real student or professor; group details in the reports and browser prototype are presentation content; the Java demo accounts are fictional.

All seeded accounts have the initial password `Campus@2026`. Each password is independently salted and hashed when seeded. Account emails are `<ID>@campus.test`.

| IDs | Names / roles |
|---|---|
| student1–student10 | Maya Hassan, Omar Salem, Noor Ali, Zayed Ahmed, Sara Ibrahim, Rashid Khalid, Lina Saeed, Faisal Noor, Hessa Omar, Ali Hamad |
| judge1–judge3 | Dr. Leila Faris, Dr. Sami Adel, Dr. Hana Karim |
| organizer1–organizer2 | Amina Mansour, Yousef Nasser |
| admin | Campus Administrator |

| Team | Captain | Other member |
|---|---|---|
| Team Alpha | student1 | student2 |
| Code Warriors | student3 | student4 |
| Innovators | student5 | student6 |
| Circuit Breakers | student7 | student8 |
| Nova Labs | student9 | student10 |

| Event | Initial state | Entries and judges |
|---|---|---|
| Hackathon 2026 | Registration; project format; capacity 12; max roster 4 | Alpha, Warriors, Innovators; judges 1 and 2 |
| Programming Contest | Published; capacity 16; max roster 3 | Alpha, Warriors, Innovators; judge1 |
| Robotics Challenge | Active; knockout; capacity 8; max roster 4 | Alpha vs Warriors; Innovators vs Circuit Breakers; judge3 |

Programming scores are `(solved, elapsed, wrong attempts)`: Alpha `(8,180,2)`, Warriors `(9,240,1)`, Innovators `(8,190,2)`. Project links use `example.com` and are illustrative. The showcase and programming deadlines are first-run time plus 30 days, and robotics plus 45 days. The published programming fixture is intentionally pre-completed for immediate demonstration.

The seed creates 10 registrations, 3 submissions, 3 evaluations, and 2 undecided matches. New user-created records use UUIDs; sample IDs are readable. Tests create a separate fixture and never modify the user's default database.
