package edu.aau.ccms;

import static edu.aau.ccms.Model.*;

import edu.aau.ccms.model.UserSummary;
import edu.aau.ccms.repository.Repository;
import edu.aau.ccms.rules.CompetitionRules;
import edu.aau.ccms.service.CampusService;
import edu.aau.ccms.util.Passwords;
import java.nio.file.*;
import java.time.*;
import java.util.*;

/** Dependency-free executable acceptance suite. Failures return a non-zero exit code. */
public final class CampusServiceTest {
  private static int passed, failed;
  private static final List<String> results = new ArrayList<>();
  private static final Clock CLOCK =
      Clock.fixed(Instant.parse("2026-10-29T08:00:00Z"), ZoneOffset.UTC);
  private static final LocalDateTime DEADLINE = LocalDateTime.of(2026, 12, 1, 17, 0);
  private static final String PASSWORD = "Campus@2026";

  private interface Checked {
    void run() throws Exception;
  }

  private static void test(String id, String description, Checked action) {
    try {
      action.run();
      passed++;
      results.add(id + ",PASS,\"" + description + "\"");
      System.out.println("PASS " + id + " " + description);
    } catch (Throwable e) {
      failed++;
      results.add(id + ",FAIL,\"" + description + ": " + e.toString().replace('"', '\'') + "\"");
      System.err.println("FAIL " + id + ": " + description);
      e.printStackTrace();
    }
  }

  private static void check(boolean value) {
    if (!value) throw new AssertionError("Condition was false");
  }

  private static void rejects(String fragment, Checked action) throws Exception {
    try {
      action.run();
    } catch (IllegalArgumentException | IllegalStateException e) {
      check(e.getMessage().toLowerCase(Locale.ROOT).contains(fragment.toLowerCase(Locale.ROOT)));
      return;
    }
    throw new AssertionError("Expected rejection: " + fragment);
  }

  private static void as(CampusService s, String account) {
    s.login(account + "@campus.test", PASSWORD);
  }

  private static Competition create(CampusService s, Format format, Set<String> judges) {
    as(s, "organizer1");
    return s.createCompetition(
        "Test " + format,
        "A complete format workflow for acceptance testing.",
        format,
        8,
        4,
        DEADLINE,
        judges);
  }

  private static void enter(CampusService s, String cid, String account, String team) {
    as(s, account);
    s.register(cid, team);
  }

  private static void submit(CampusService s, String cid, String account, String team) {
    as(s, account);
    s.submit(
        cid,
        team,
        "Project title",
        "A complete description of the team project.",
        "https://example.com/project");
  }

  private static CampusService seed(Repository r) throws Exception {
    return new CampusService(r, true, CLOCK);
  }

  public static void main(String[] args) throws Exception {
    passed = 0;
    failed = 0;
    results.clear();
    Files.createDirectories(Path.of("build"));
    Path root = Files.createTempDirectory(Path.of("build"), "acceptance-");
    Path data = root.resolve("campus.bin");
    try (Repository repository = new Repository(data)) {
      CampusService s = seed(repository);
      test(
          "TC-01",
          "FR-01: valid authentication",
          () -> {
            as(s, "organizer1");
            check(s.currentUser().role() == Role.ORGANIZER);
          });
      test(
          "TC-02",
          "FR-01: wrong password clears session",
          () -> {
            rejects("incorrect", () -> s.login("organizer1@campus.test", "wrong"));
            rejects("Sign in", s::currentUser);
          });
      test(
          "TC-03",
          "FR-01: unknown account rejected",
          () -> rejects("incorrect", () -> s.login("nobody@campus.test", PASSWORD)));
      test(
          "TC-04",
          "FR-02: student cannot create accounts",
          () -> {
            as(s, "student1");
            rejects("role", () -> s.createUser("Test", "test@campus.test", Role.ADMIN, PASSWORD));
          });
      test(
          "TC-05",
          "FR-02: administrator creates an account",
          () -> {
            as(s, "admin");
            s.createUser("Test Student", "new@campus.test", Role.STUDENT, PASSWORD);
            check(s.users().size() == 17);
          });
      test(
          "TC-06",
          "FR-02: duplicate email is case insensitive",
          () ->
              rejects(
                  "already",
                  () -> s.createUser("Duplicate", "NEW@campus.test", Role.STUDENT, PASSWORD)));
      test(
          "TC-07",
          "FR-02: short passwords rejected",
          () ->
              rejects(
                  "8 characters",
                  () -> s.createUser("Weak", "weak@campus.test", Role.STUDENT, "short")));
      test(
          "TC-08",
          "FR-03: student cannot create competitions",
          () -> {
            as(s, "student1");
            rejects(
                "role",
                () ->
                    s.createCompetition(
                        "Title", "Description", Format.PROJECT, 8, 4, DEADLINE, Set.of("judge1")));
          });
      Competition project = create(s, Format.PROJECT, Set.of("judge1", "judge2"));
      String cid = project.id();
      test(
          "TC-09",
          "FR-03: new competition is a draft",
          () -> check(s.competition(cid).phase() == Phase.DRAFT));
      test(
          "TC-10",
          "FR-03: only owning organizer manages competition",
          () -> {
            as(s, "organizer2");
            rejects("owning organizer", () -> s.advance(cid));
          });
      test(
          "TC-11",
          "FR-04: draft registration rejected",
          () -> {
            as(s, "student1");
            rejects("not open", () -> s.register(cid, "alpha"));
          });
      as(s, "organizer1");
      s.advance(cid);
      test(
          "TC-12",
          "FR-03: rule edits blocked after opening",
          () ->
              rejects(
                  "draft",
                  () ->
                      s.editCompetition(
                          cid,
                          "Edit",
                          "Description",
                          Format.PROJECT,
                          8,
                          4,
                          DEADLINE,
                          Set.of("judge1"))));
      test(
          "TC-13",
          "FR-04: at least two entries required",
          () -> rejects("two teams", () -> s.advance(cid)));
      test(
          "TC-14",
          "FR-04: captain registers team",
          () -> {
            enter(s, cid, "student1", "alpha");
            check(s.registrations(cid).size() == 1);
          });
      test(
          "TC-15",
          "FR-04: duplicate registration rejected",
          () -> rejects("already registered", () -> s.register(cid, "alpha")));
      test(
          "TC-16",
          "FR-04: noncaptain registration rejected",
          () -> {
            as(s, "student2");
            rejects("captain", () -> s.register(cid, "alpha"));
          });
      test(
          "TC-17",
          "FR-05: registered roster is locked",
          () -> {
            as(s, "student9");
            rejects("roster is locked", () -> s.joinTeam("alpha"));
          });
      test(
          "TC-18",
          "FR-04: same student cannot enter twice",
          () -> {
            as(s, "student1");
            Team t = s.createTeam("Overlap");
            rejects("already registered", () -> s.register(cid, t.id()));
          });
      test(
          "TC-19",
          "FR-04: withdrawal during registration",
          () -> {
            enter(s, cid, "student3", "warriors");
            s.withdraw(cid, "warriors");
            check(s.registrations(cid).size() == 1);
            s.register(cid, "warriors");
          });
      as(s, "organizer1");
      s.advance(cid);
      test(
          "TC-20",
          "FR-06: missing submissions block judging",
          () -> rejects("Every registered team", () -> s.advance(cid)));
      test(
          "TC-21",
          "FR-06: non-HTTP submission URL rejected",
          () -> {
            as(s, "student1");
            rejects(
                "HTTP",
                () -> s.submit(cid, "alpha", "Title", "Description", "javascript:alert(1)"));
          });
      test(
          "TC-22",
          "FR-06: submission may be replaced before judging",
          () -> {
            submit(s, cid, "student1", "alpha");
            s.submit(
                cid,
                "alpha",
                "Revised title",
                "Revised description",
                "https://example.com/revised");
            check(
                s.submissions(cid).size() == 1
                    && s.submissions(cid).get(0).title().equals("Revised title"));
          });
      test(
          "TC-23",
          "FR-06: student sees only own team's submissions",
          () -> {
            submit(s, cid, "student3", "warriors");
            as(s, "student1");
            check(s.submissions(cid).size() == 1);
          });
      test(
          "TC-24",
          "FR-07: scores rejected outside judging",
          () -> {
            as(s, "judge1");
            rejects("only during judging", () -> s.evaluate(cid, "alpha", 90, 80, 70, "Feedback"));
          });
      as(s, "organizer1");
      s.advance(cid);
      test(
          "TC-25",
          "FR-07: unassigned judge rejected",
          () -> {
            as(s, "judge3");
            rejects("not assigned", () -> s.evaluate(cid, "alpha", 90, 80, 70, "Feedback"));
          });
      test(
          "TC-26",
          "FR-07: scores outside rubric rejected",
          () -> {
            as(s, "judge1");
            rejects("between 0 and 100", () -> s.evaluate(cid, "alpha", 101, 80, 70, "Feedback"));
          });
      test(
          "TC-27",
          "FR-07: replacing judge score does not duplicate it",
          () -> {
            s.evaluate(cid, "alpha", 90, 80, 70, "Initial review");
            s.evaluate(cid, "alpha", 80, 80, 80, "Updated review");
            check(s.evaluations(cid).size() == 1);
          });
      test(
          "TC-28",
          "FR-08: unfinished reviews block publication",
          () -> {
            as(s, "organizer1");
            rejects("all required evaluations", () -> s.advance(cid));
          });
      test(
          "TC-29",
          "FR-08: student cannot read unpublished leaderboard",
          () -> {
            as(s, "student1");
            rejects("released", () -> s.standings(cid));
          });
      as(s, "judge1");
      s.evaluate(cid, "warriors", 80, 80, 80, "Consistent result");
      as(s, "judge2");
      s.evaluate(cid, "alpha", 80, 80, 80, "Second review");
      s.evaluate(cid, "warriors", 80, 80, 80, "Second review");
      as(s, "organizer1");
      s.advance(cid);
      test(
          "TC-30",
          "FR-08: equal project averages share rank",
          () -> {
            as(s, "student1");
            List<Standing> list = s.standings(cid);
            check(
                list.size() == 2
                    && list.get(0).rank() == 1
                    && list.get(1).rank() == 1
                    && list.get(0).score() == 80);
          });
      test(
          "TC-31",
          "FR-07: published scores cannot be modified",
          () -> {
            as(s, "judge1");
            rejects(
                "only during judging", () -> s.evaluate(cid, "alpha", 90, 90, 90, "Late review"));
          });
      test(
          "TC-32",
          "FR-08: standings export has header and rows",
          () -> {
            String csv = s.standingsCsv(cid);
            check(csv.startsWith("Rank,Team,Score,Details\r\n") && csv.contains("Team Alpha"));
          });
      test(
          "TC-33",
          "FR-03: archive is terminal",
          () -> {
            as(s, "organizer1");
            s.advance(cid);
            rejects("already archived", () -> s.advance(cid));
          });
      test(
          "TC-34",
          "FR-09: programming ranks solved then penalty",
          () -> {
            List<Standing> list = s.standings("programming");
            check(
                list.get(0).teamId().equals("warriors")
                    && list.get(1).teamId().equals("alpha")
                    && list.get(2).teamId().equals("innovators"));
          });
      test(
          "TC-35",
          "FR-07: weighted project formula",
          () -> {
            Evaluation e = new Evaluation("x", "x", "x", "x", 90, 80, 70, "");
            check(Math.abs(CompetitionRules.forFormat(Format.PROJECT).score(e) - 81.5) < 0.00001);
          });
      test(
          "TC-36",
          "FR-10: only assigned judge records bracket results",
          () -> {
            as(s, "judge1");
            rejects("not assigned", () -> s.recordWinner("semi1", "alpha"));
          });
      test(
          "TC-37",
          "FR-10: winner must play in the selected match",
          () -> {
            as(s, "judge3");
            rejects("one of the two teams", () -> s.recordWinner("semi1", "nova"));
          });
      test(
          "TC-38",
          "FR-10: semifinals create exactly one final",
          () -> {
            s.recordWinner("semi1", "alpha");
            s.recordWinner("semi2", "innovators");
            check(s.matches("robotics").size() == 3);
          });
      test(
          "TC-39",
          "FR-10: finalized match cannot be overwritten",
          () -> rejects("already final", () -> s.recordWinner("semi1", "warriors")));
      test(
          "TC-40",
          "FR-10: unfinished bracket blocks certification",
          () -> {
            as(s, "organizer1");
            rejects("Complete every bracket round", () -> s.advance("robotics"));
          });
      test(
          "TC-41",
          "FR-10: champion is published after final",
          () -> {
            as(s, "judge3");
            Match last =
                s.matches("robotics").stream()
                    .filter(m -> m.round() == 2)
                    .findFirst()
                    .orElseThrow();
            s.recordWinner(last.id(), "alpha");
            as(s, "organizer1");
            s.advance("robotics");
            s.advance("robotics");
            check(
                s.standings("robotics").get(0).teamId().equals("alpha")
                    && s.standings("robotics").get(0).score() == 2);
          });
      test(
          "TC-42",
          "NFR-02: password changes invalidate old password",
          () -> {
            as(s, "student10");
            s.changePassword(PASSWORD, "NewPass@2026");
            s.logout();
            rejects("incorrect", () -> s.login("student10@campus.test", PASSWORD));
            s.login("student10@campus.test", "NewPass@2026");
          });
      test(
          "TC-43",
          "NFR-02: salted hashes differ for equal passwords",
          () -> {
            String a = Passwords.hash(PASSWORD), b = Passwords.hash(PASSWORD);
            check(!a.equals(b) && Passwords.verify(PASSWORD, a));
          });
      test(
          "TC-44",
          "NFR-04: concurrent data access is locked",
          () -> {
            try (Repository ignored = new Repository(data)) {
              throw new AssertionError("Second repository opened");
            } catch (java.io.IOException expected) {
              check(expected.getMessage().contains("locked"));
            }
          });
      test(
          "TC-45",
          "FR-11: successful changes are audited",
          () -> {
            as(s, "admin");
            check(s.audit().size() > 20);
          });
      test(
          "TC-46",
          "FR-11: students cannot read administrative audit",
          () -> {
            as(s, "student1");
            rejects("role", s::audit);
          });
      Competition timed = create(s, Format.PROJECT, Set.of("judge1"));
      s.advance(timed.id());
      enter(s, timed.id(), "student1", "alpha");
      enter(s, timed.id(), "student3", "warriors");
      as(s, "organizer1");
      s.advance(timed.id());
      test(
          "TC-47",
          "FR-06: submission at deadline is rejected",
          () -> {
            CampusService later =
                new CampusService(
                    repository,
                    true,
                    Clock.fixed(DEADLINE.toInstant(ZoneOffset.UTC), ZoneOffset.UTC));
            as(later, "student1");
            rejects(
                "deadline has passed",
                () ->
                    later.submit(
                        timed.id(), "alpha", "Late", "Description", "https://example.com"));
          });
      test(
          "TC-48",
          "NFR-04: failed disk write rolls back domain changes",
          () -> {
            as(s, "student9");
            int before = s.teams().size();
            Path blocked = Path.of(data + ".tmp");
            Files.createDirectory(blocked);
            Files.writeString(blocked.resolve("block"), "Prevent file replacement");
            rejects("could not be saved", () -> s.createTeam("Must roll back"));
            check(s.teams().size() == before);
            Files.delete(blocked.resolve("block"));
            Files.delete(blocked);
          });
      test(
          "TC-49",
          "FR-03: knockout capacity must be a power of two",
          () -> {
            as(s, "organizer1");
            rejects(
                "2, 4, 8, or 16",
                () ->
                    s.createCompetition(
                        "Invalid bracket",
                        "Description",
                        Format.KNOCKOUT,
                        6,
                        4,
                        DEADLINE,
                        Set.of("judge1")));
          });
      test(
          "TC-50",
          "FR-03: past deadlines rejected",
          () ->
              rejects(
                  "future",
                  () ->
                      s.createCompetition(
                          "Past",
                          "Description",
                          Format.PROJECT,
                          8,
                          4,
                          LocalDateTime.of(2020, 1, 1, 12, 0),
                          Set.of("judge1"))));
      test(
          "TC-51",
          "FR-03: nonjudge cannot be assigned as judge",
          () ->
              rejects(
                  "only judge",
                  () ->
                      s.createCompetition(
                          "Bad judge",
                          "Description",
                          Format.PROJECT,
                          8,
                          4,
                          DEADLINE,
                          Set.of("student1"))));
      Competition prog = create(s, Format.PROGRAMMING, Set.of("judge1", "judge2"));
      s.advance(prog.id());
      enter(s, prog.id(), "student1", "alpha");
      enter(s, prog.id(), "student3", "warriors");
      as(s, "organizer1");
      s.advance(prog.id());
      submit(s, prog.id(), "student1", "alpha");
      submit(s, prog.id(), "student3", "warriors");
      as(s, "organizer1");
      s.advance(prog.id());
      test(
          "TC-52",
          "FR-09: one official programming result per team",
          () -> {
            as(s, "judge1");
            s.evaluate(prog.id(), "alpha", 8, 180, 2, "Official result");
            as(s, "judge2");
            rejects(
                "already owns",
                () -> s.evaluate(prog.id(), "alpha", 9, 150, 1, "Conflicting result"));
          });
      test(
          "TC-53",
          "FR-09: programming results complete publication workflow",
          () -> {
            as(s, "judge2");
            s.evaluate(prog.id(), "warriors", 8, 180, 2, "Equal official result");
            as(s, "organizer1");
            s.advance(prog.id());
            check(s.standings(prog.id()).stream().allMatch(r -> r.rank() == 1));
          });
      Competition small =
          s.createCompetition(
              "Small teams",
              "Only solo entrants",
              Format.PROJECT,
              2,
              1,
              DEADLINE,
              Set.of("judge1"));
      s.advance(small.id());
      test(
          "TC-54",
          "FR-04: oversized teams rejected",
          () -> {
            as(s, "student1");
            rejects("team size", () -> s.register(small.id(), "alpha"));
          });
      test(
          "TC-55",
          "FR-05: student can create an unlocked team",
          () -> {
            as(s, "student9");
            Team t = s.createTeam("Free agents");
            check(t.captainId().equals("student9"));
          });
      // The password changed in TC-42: explicitly authenticate using the new value.
      s.login("student10@campus.test", "NewPass@2026");
      Team free =
          s.teams().stream().filter(t -> t.name().equals("Free agents")).findFirst().orElseThrow();
      test(
          "TC-56",
          "FR-05: unlocked membership changes persist",
          () -> {
            s.joinTeam(free.id());
            check(s.team(free.id()).members().size() == 2);
            s.leaveTeam(free.id());
            check(s.team(free.id()).members().size() == 1);
          });
      test(
          "TC-57",
          "FR-08: CSV formula injection escaped",
          () -> {
            as(s, "student9");
            Team formula = s.createTeam("=SUM(1+1)");
            as(s, "organizer1");
            Competition special =
                s.createCompetition(
                    "CSV test", "Safe export", Format.PROJECT, 2, 4, DEADLINE, Set.of("judge1"));
            s.advance(special.id());
            enter(s, special.id(), "student9", formula.id());
            enter(s, special.id(), "student3", "warriors");
            as(s, "organizer1");
            s.advance(special.id());
            submit(s, special.id(), "student9", formula.id());
            submit(s, special.id(), "student3", "warriors");
            as(s, "organizer1");
            s.advance(special.id());
            as(s, "judge1");
            s.evaluate(special.id(), formula.id(), 1, 1, 1, "Review");
            s.evaluate(special.id(), "warriors", 2, 2, 2, "Review");
            as(s, "organizer1");
            s.advance(special.id());
            check(s.standingsCsv(special.id()).contains("\"'=SUM(1+1)\""));
          });
    }
    test(
        "TC-58",
        "NFR-04: restart preserves accounts and published champion",
        () -> {
          try (Repository repo = new Repository(data)) {
            CampusService s = seed(repo);
            s.login("new@campus.test", PASSWORD);
            check(s.standings("robotics").get(0).teamId().equals("alpha"));
          }
        });
    test(
        "TC-59",
        "NFR-04: corrupt data is not silently reset",
        () -> {
          Path corrupt = root.resolve("corrupt.bin");
          Files.writeString(corrupt, "not a valid database");
          try (Repository repo = new Repository(corrupt)) {
            try {
              seed(repo);
              throw new AssertionError("Corrupt file accepted");
            } catch (java.io.IOException expected) {
              check(Files.readString(corrupt).equals("not a valid database"));
            }
          }
        });
    test(
        "TC-60",
        "SEC: account APIs return credential-free summaries",
        () -> {
          try (Repository r = new Repository(root.resolve("summaries.bin"))) {
            CampusService s = seed(r);
            UserSummary session = s.login("admin@campus.test", PASSWORD);
            check(session.getClass().getRecordComponents().length == 4);
            check(s.currentUser() instanceof UserSummary);
            check(s.users().stream().allMatch(u -> u.getClass().getRecordComponents().length == 4));
            check(
                s.createUser("Safe account", "safe@campus.test", Role.STUDENT, PASSWORD)
                    instanceof UserSummary);
          }
        });
    test(
        "TC-61",
        "SEC: account directory is administrator-only",
        () -> {
          try (Repository r = new Repository(root.resolve("directory.bin"))) {
            CampusService s = seed(r);
            rejects("Sign in", s::users);
            for (String account : List.of("student1", "organizer1", "judge1")) {
              as(s, account);
              rejects("role", s::users);
            }
            as(s, "organizer1");
            check(
                s.judges().size() == 3
                    && s.judges().stream().allMatch(u -> u.role() == Role.JUDGE));
            as(s, "student1");
            rejects("role", s::judges);
          }
        });
    test(
        "TC-62",
        "SEC: null credentials reject and clear the session",
        () -> {
          try (Repository r = new Repository(root.resolve("null-login.bin"))) {
            CampusService s = seed(r);
            as(s, "admin");
            rejects("incorrect", () -> s.login(null, PASSWORD));
            rejects("Sign in", s::currentUser);
            rejects("incorrect", () -> s.login("admin@campus.test", null));
          }
        });
    test(
        "TC-63",
        "FR-02: missing account role leaves accounts unchanged",
        () -> {
          try (Repository r = new Repository(root.resolve("null-role.bin"))) {
            CampusService s = seed(r);
            as(s, "admin");
            int before = s.users().size();
            rejects("role", () -> s.createUser("Bad role", "role@campus.test", null, PASSWORD));
            check(s.users().size() == before);
          }
        });
    test(
        "TC-64",
        "FR-06: relative URL rejects without a null-pointer error",
        () -> {
          try (Repository r = new Repository(root.resolve("relative-url.bin"))) {
            CampusService s = seed(r);
            as(s, "organizer1");
            s.advance("hackathon");
            as(s, "student1");
            rejects(
                "HTTP",
                () -> s.submit("hackathon", "alpha", "Title", "Description", "/relative/path"));
            check(s.submissions("hackathon").isEmpty());
            s.submit("hackathon", "alpha", "Title", "Description", "HTTPS://example.com/project");
            check(s.submissions("hackathon").size() == 1);
          }
        });
    test(
        "TC-65",
        "SEC: malformed and excessive password derivation parameters reject",
        () -> {
          check(!Passwords.verify(PASSWORD, "2147483647:AAAA:AAAA"));
          check(!Passwords.verify(null, Passwords.hash(PASSWORD)));
          rejects("maximum", () -> Passwords.hash("a".repeat(1025)));
        });
    test(
        "TC-66",
        "FR-08: multiline formula cells are escaped in exported CSV",
        () -> {
          Path file = root.resolve("multiline-csv.bin");
          try (Repository r = new Repository(file)) {
            seed(r);
            State fixture = r.load();
            Team t = fixture.teams().get("alpha");
            fixture
                .teams()
                .put(
                    "alpha",
                    new Team(t.id(), "=SUM(1+1)\nquoted \"text\"", t.captainId(), t.members()));
            r.save(fixture);
            CampusService s = seed(r);
            as(s, "student1");
            String csv = s.standingsCsv("programming");
            check(csv.contains("\"'=SUM(1+1)\nquoted \"\"text\"\"\""));
          }
        });
    test(
        "TC-67",
        "NFR-04: oversized snapshot preserves the last readable file",
        () -> {
          Path file = root.resolve("oversize.bin");
          try (Repository r = new Repository(file)) {
            seed(r);
            byte[] before = Files.readAllBytes(file);
            State fixture = r.load();
            fixture.audit().add(new Audit(LocalDateTime.now(), "Test", "x".repeat(20_000_001)));
            try {
              r.save(fixture);
              throw new AssertionError("Oversized data accepted");
            } catch (java.io.IOException expected) {
              check(expected.getMessage().contains("limit"));
            }
            check(Arrays.equals(before, Files.readAllBytes(file)));
            check(r.load() != null);
          }
        });
    test(
        "TC-68",
        "NFR-04: closed repository rejects writes and permits repeated close",
        () -> {
          Path file = root.resolve("closed.bin");
          Repository r = new Repository(file);
          seed(r);
          State fixture = r.load();
          r.close();
          r.close();
          try {
            r.save(fixture);
            throw new AssertionError("Closed save accepted");
          } catch (java.io.IOException expected) {
            check(expected.getMessage().contains("closed"));
          }
        });
    test(
        "TC-69",
        "NFR-04: pre-refactor version-one database remains readable",
        () -> {
          Path file = root.resolve("legacy.bin");
          Files.copy(Path.of("src/test/resources/legacy-campus-v1.bin"), file);
          try (Repository r = new Repository(file)) {
            CampusService s = seed(r);
            as(s, "admin");
            check(s.users().size() == 17);
            check(s.standings("robotics").get(0).teamId().equals("alpha"));
          }
        });
    test(
        "TC-70",
        "FR-10: a null winner rejects without changing the bracket",
        () -> {
          try (Repository r = new Repository(root.resolve("null-winner.bin"))) {
            CampusService s = seed(r);
            as(s, "judge3");
            rejects("one of the two", () -> s.recordWinner("semi1", null));
            check(s.matches("robotics").stream().allMatch(m -> m.winnerId() == null));
          }
        });
    String summary =
        "Executed " + (passed + failed) + " tests: " + passed + " passed, " + failed + " failed.";
    Files.writeString(
        Path.of("build/test-results.csv"),
        "Test ID,Status,Description\n" + String.join("\n", results) + "\n");
    Files.writeString(
        Path.of("build/test-summary.txt"), summary + "\nExecuted at: " + Instant.now() + "\n");
    System.out.println(summary);
    if (failed > 0) throw new AssertionError(failed + " service checks failed");
  }
}
