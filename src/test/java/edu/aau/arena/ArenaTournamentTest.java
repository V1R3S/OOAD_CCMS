package edu.aau.arena;

import static edu.aau.ccms.Model.*;
import edu.aau.arena.repository.Repository;
import edu.aau.arena.rules.*;
import edu.aau.arena.service.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.stream.*;

/** Real service/repository regressions plus exhaustive supported-size tournament checks. */
public final class ArenaTournamentTest {
  private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-10T12:00:00Z"), ZoneOffset.UTC);
  private static final LocalDateTime DEADLINE = LocalDateTime.now(CLOCK).plusDays(30);
  private static byte[] seed;
  private static int passed, failed;
  private static final List<String> results = new ArrayList<>();
  private static final String[] TEAMS = {"alpha", "warriors", "innovators", "circuit", "nova"};
  private static final String[] CAPTAINS = {"student1", "student3", "student5", "student7", "student9"};

  @FunctionalInterface interface Check { void run() throws Exception; }
  private static void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }
  private static void rejects(String fragment, Runnable action) {
    try { action.run(); } catch (IllegalArgumentException | IllegalStateException error) {
      check(error.getMessage().toLowerCase(Locale.ROOT).contains(fragment.toLowerCase(Locale.ROOT)),
          "Expected '" + fragment + "', got: " + error);
      return;
    }
    throw new AssertionError("Expected rejection: " + fragment);
  }
  private static void test(String id, String description, Check action) {
    try {
      action.run(); passed++; results.add(id + ",PASS,\"" + description + "\"");
      System.out.println("PASS " + id + " " + description);
    } catch (Throwable error) {
      failed++; results.add(id + ",FAIL,\"" + description + "\"");
      System.err.println("FAIL " + id + " " + description); error.printStackTrace();
    }
  }
  private static final class Fixture implements AutoCloseable {
    final Path file;
    final Repository repository;
    final ArenaService service;
    Fixture() throws Exception {
      file = Files.createTempDirectory(Path.of("build"), "arena-regression-").resolve("arena.bin");
      Files.write(file, seed);
      repository = new Repository(file);
      service = new ArenaService(repository, false, CLOCK);
      login("organizer1");
    }
    void login(String user) { service.login(user + "@campus.test", "Campus@2026"); }
    Competition create(CompetitionType type, TournamentFormat format) {
      return service.createCompetition("Regression " + type, "An isolated test competition.",
          type, format, 16, 4, DEADLINE, Set.of("judge1", "judge2"), 20);
    }
    String start(CompetitionType type, TournamentFormat format, int teams) {
      login("organizer1");
      Competition c = create(type, format);
      service.advance(c.id());
      for (int i = 0; i < teams; i++) {
        login(CAPTAINS[i]); service.register(c.id(), TEAMS[i]);
      }
      login("organizer1"); service.advance(c.id());
      return c.id();
    }
    public void close() throws Exception { repository.close(); }
  }

  public static void main(String[] args) throws Exception {
    passed = failed = 0; results.clear();
    Files.createDirectories(Path.of("build"));
    State demo = new State(); DemoData.populate(demo, LocalDateTime.now(CLOCK)); seed = Repository.encode(demo);
    test("AR-01", "All seven supported type and tournament combinations save and reload", () -> {
      try (Fixture f = new Fixture()) {
        int count = 0;
        for (CompetitionType type : CompetitionType.values()) for (TournamentFormat format : TournamentFormat.values()) {
          if (type.supports(format)) {
            Competition c = f.create(type, format);
            Competition loaded = f.repository.load().competitions().get(c.id());
            check(loaded.type() == type && loaded.tournamentFormat() == format, "Type/format changed"); count++;
          }
        }
        check(count == 7, "Compatibility matrix changed unexpectedly");
      }
    });
    test("AR-02", "Unsupported combinations reject without adding records or audit entries", () -> {
      try (Fixture f = new Fixture()) {
        int count = f.service.competitions().size(), audit = f.service.audit().size();
        for (CompetitionType type : CompetitionType.values()) for (TournamentFormat format : TournamentFormat.values())
          if (!type.supports(format)) rejects("does not support", () -> f.create(type, format));
        check(count == f.service.competitions().size() && audit == f.service.audit().size(), "Rejected creation mutated state");
      }
    });
    test("AR-03", "Null type or tournament and invalid quiz maxima reject", () -> {
      try (Fixture f = new Fixture()) {
        rejects("type", () -> f.create(null, TournamentFormat.SCORE_BASED));
        rejects("format", () -> f.create(CompetitionType.QUIZ, null));
        for (int max : new int[] {0, -1, 10001}) rejects("maximum", () -> f.service.createCompetition(
            "Quiz", "Invalid maximum", CompetitionType.QUIZ, TournamentFormat.SCORE_BASED, 8, 4, DEADLINE, Set.of("judge1"), max));
      }
    });
    test("AR-04", "Students cannot create tournaments and other organizers cannot edit them", () -> {
      try (Fixture f = new Fixture()) {
        Competition c = f.create(CompetitionType.QUIZ, TournamentFormat.KNOCKOUT);
        f.login("student1"); rejects("role", () -> f.create(CompetitionType.QUIZ, TournamentFormat.KNOCKOUT));
        f.login("organizer2"); rejects("owning organizer", () -> f.service.editCompetition(c.id(), "Edited", "Description",
            CompetitionType.QUIZ, TournamentFormat.ROUND_ROBIN, 6, 4, DEADLINE, Set.of("judge1"), 25));
      }
    });
    test("AR-05", "Draft type changes persist and registration freezes the configuration", () -> {
      try (Fixture f = new Fixture()) {
        Competition c = f.create(CompetitionType.PROJECT, TournamentFormat.SCORE_BASED);
        f.service.editCompetition(c.id(), "Quiz league", "Round-robin academic quiz", CompetitionType.QUIZ,
            TournamentFormat.ROUND_ROBIN, 5, 3, DEADLINE, Set.of("judge2"), 30);
        check(f.service.competition(c.id()).quizMaxPoints() == 30, "Settings not edited");
        f.service.advance(c.id());
        rejects("draft", () -> f.service.editCompetition(c.id(), "Quiz", "Updated", CompetitionType.QUIZ,
            TournamentFormat.SCORE_BASED, 8, 4, DEADLINE, Set.of("judge1"), 20));
      }
    });
    test("AR-06", "Quiz ranking enters judging without submissions and rejects submitted work", () -> {
      try (Fixture f = new Fixture()) {
        String cid = f.start(CompetitionType.QUIZ, TournamentFormat.SCORE_BASED, 3);
        f.login("student1"); rejects("not submissions", () -> f.service.submit(cid, "alpha", "Quiz", "Answers", "https://example.com"));
        f.login("organizer1"); f.service.advance(cid);
        check(f.service.competition(cid).phase() == Phase.JUDGING && f.service.submissions(cid).isEmpty(), "Incorrect quiz workflow");
      }
    });
    test("AR-07", "Quiz points enforce lower upper and irrelevant-input boundaries", () -> {
      try (Fixture f = new Fixture()) {
        f.login("judge2");
        for (int score : new int[] {-1, 21}) rejects("0", () -> f.service.evaluate("quiz", "alpha", score, 0, 0, "Official points"));
        rejects("only", () -> f.service.evaluate("quiz", "alpha", 10, 1, 0, "Official points"));
        rejects("only", () -> f.service.evaluate("quiz", "alpha", 10, 0, 1, "Official points"));
        f.service.evaluate("quiz", "alpha", 0, 0, 0, "Zero correct answers");
        f.service.evaluate("quiz", "alpha", 20, 0, 0, "All answers correct");
        check(f.service.evaluations("quiz").size() == 1 && f.service.evaluations("quiz").get(0).a() == 20, "Review replacement incorrect");
      }
    });
    test("AR-08", "Quiz publication needs all results and equal top scores share first place", () -> {
      try (Fixture f = new Fixture()) {
        rejects("evaluations", () -> f.service.advance("quiz"));
        f.login("judge2");
        f.service.evaluate("quiz", "alpha", 20, 0, 0, "Official score");
        f.service.evaluate("quiz", "warriors", 20, 0, 0, "Official score");
        f.service.evaluate("quiz", "innovators", 0, 0, 0, "Official score");
        f.login("organizer1"); f.service.advance("quiz");
        List<Standing> rows = f.service.standings("quiz");
        check(rows.stream().map(Standing::rank).toList().equals(List.of(1, 1, 3)), "Quiz tied ranks incorrect");
        f.login("student1"); check(f.service.standings("quiz").size() == 3, "Published quiz hidden");
      }
    });
    test("AR-09", "Quiz official result belongs to one assigned judge", () -> {
      try (Fixture f = new Fixture()) {
        String cid = f.start(CompetitionType.QUIZ, TournamentFormat.SCORE_BASED, 2);
        f.service.advance(cid); f.login("judge1"); f.service.evaluate(cid, "alpha", 15, 0, 0, "Official score");
        f.login("judge2"); rejects("already owns", () -> f.service.evaluate(cid, "alpha", 16, 0, 0, "Overwrite attempt"));
        f.login("judge3"); rejects("not assigned", () -> f.service.evaluate(cid, "warriors", 16, 0, 0, "Unassigned attempt"));
      }
    });
    test("AR-10", "Students cannot see provisional league tables or enter match scores", () -> {
      try (Fixture f = new Fixture()) {
        f.login("student1");
        rejects("released", () -> f.service.leagueStandings("league"));
        rejects("released", () -> f.service.standingsCsv("league"));
        Match m = f.service.matches("league").get(0);
        rejects("role", () -> f.service.recordMatchScore(m.id(), 1, 0));
        f.login("judge1"); rejects("not assigned", () -> f.service.recordMatchScore(m.id(), 1, 0));
      }
    });
    test("AR-11", "All 2 through 16 team knockout brackets finish with exactly n minus one played matches", () -> {
      TournamentRules rules = TournamentRules.forFormat(TournamentFormat.KNOCKOUT);
      for (int count = 2; count <= 16; count++) {
        List<String> teams = IntStream.range(0, count).mapToObj(i -> "T" + i).toList();
        List<Match> matches = new ArrayList<>(rules.start("knockout", teams));
        check(matches.stream().filter(Match::bye).allMatch(Match::decided), "Bye did not advance");
        int safety = 0;
        while (!rules.matchesComplete(matches)) {
          check(++safety <= 5, "Bracket failed to finish");
          for (int i = 0; i < matches.size(); i++) if (!matches.get(i).decided()) matches.set(i, matches.get(i).winner(matches.get(i).leftTeamId()));
          matches.addAll(rules.nextRound("knockout", matches));
        }
        check(matches.stream().filter(m -> !m.bye()).count() == count - 1, "Wrong number of played matches for " + count);
        check(matches.stream().map(Match::id).distinct().count() == matches.size(), "Duplicate ID");
        check(rules.nextRound("knockout", matches).isEmpty(), "Final generated another round");
        List<Standing> standings = rules.standings(teams, matches, t -> t);
        check(standings.get(0).rank() == 1 && standings.get(0).detail().equals("Champion")
            && standings.get(1).rank() == 2, "Bye distorted champion ranking");
      }
    });
    test("AR-12", "Tournament entrants reject duplicate null too-small and oversized lists", () -> {
      for (TournamentFormat format : List.of(TournamentFormat.KNOCKOUT, TournamentFormat.ROUND_ROBIN)) {
        TournamentRules rules = TournamentRules.forFormat(format);
        for (List<String> ids : List.of(List.<String>of(), List.of("a"), List.of("a", "a"),
            Arrays.asList("a", null), IntStream.range(0, 17).mapToObj(String::valueOf).toList()))
          rejects("distinct", () -> rules.start("invalid", ids));
      }
    });
    test("AR-13", "Three-team quiz knockout records points advances byes and publishes a champion", () -> {
      try (Fixture f = new Fixture()) {
        String cid = f.start(CompetitionType.QUIZ, TournamentFormat.KNOCKOUT, 3);
        check(f.service.matches(cid).stream().filter(Match::bye).count() == 1, "Missing bye");
        f.login("judge1"); Match first = f.service.matches(cid).stream().filter(m -> !m.bye()).findFirst().orElseThrow();
        rejects("draw", () -> f.service.recordMatchScore(first.id(), 10, 10));
        rejects("scores", () -> f.service.recordWinner(first.id(), first.leftTeamId()));
        rejects("0", () -> f.service.recordMatchScore(first.id(), 21, 0));
        f.service.recordMatchScore(first.id(), 15, 12);
        Match last = f.service.matches(cid).stream().filter(m -> !m.decided()).findFirst().orElseThrow();
        check(last.round() == 2, "Next round not generated");
        f.service.recordMatchScore(last.id(), 20, 19);
        f.login("organizer1"); f.service.advance(cid); f.service.advance(cid);
        check(f.service.standings(cid).get(0).teamId().equals("alpha"), "Wrong champion after bye");
      }
    });
    test("AR-14", "Five-team general knockout preserves winner selection and rejects repeated results", () -> {
      try (Fixture f = new Fixture()) {
        String cid = f.start(CompetitionType.MATCH, TournamentFormat.KNOCKOUT, 5);
        rejects("every bracket", () -> f.service.advance(cid));
        f.login("judge1");
        while (f.service.matches(cid).stream().anyMatch(m -> !m.decided())) {
          Match match = f.service.matches(cid).stream().filter(m -> !m.decided()).findFirst().orElseThrow();
          rejects("confirmed winner", () -> f.service.recordMatchScore(match.id(), 1, 0));
          f.service.recordWinner(match.id(), match.leftTeamId());
          int count = f.service.matches(cid).size();
          rejects("already final", () -> f.service.recordWinner(match.id(), match.leftTeamId()));
          check(count == f.service.matches(cid).size(), "Duplicate round generated");
        }
        f.login("organizer1"); f.service.advance(cid); f.service.advance(cid);
        check(f.service.standings(cid).get(0).detail().equals("Champion"), "Champion missing");
      }
    });
    test("AR-15", "All 2 through 16 team league schedules contain every pair once with no round collisions", () -> {
      TournamentRules rules = TournamentRules.forFormat(TournamentFormat.ROUND_ROBIN);
      for (int count = 2; count <= 16; count++) {
        List<String> teams = IntStream.range(0, count).mapToObj(i -> "T" + i).toList();
        List<Match> matches = rules.start("league", teams);
        check(matches.size() == count * (count - 1) / 2, "Wrong fixture count");
        Set<Set<String>> pairs = new HashSet<>(); Map<Integer, Set<String>> rounds = new HashMap<>();
        for (Match m : matches) {
          check(!m.bye() && !m.decided(), "Unexpected fixture bye or result");
          check(pairs.add(Set.of(m.leftTeamId(), m.rightTeamId())), "Repeated pair");
          Set<String> playing = rounds.computeIfAbsent(m.round(), r -> new HashSet<>());
          check(playing.add(m.leftTeamId()) && playing.add(m.rightTeamId()), "Team scheduled twice in same round");
        }
        for (String team : teams) check(matches.stream().filter(m -> team.equals(m.leftTeamId()) || team.equals(m.rightTeamId())).count() == count - 1, "Missing opponent");
      }
    });
    test("AR-16", "League win draw loss counters and 3-1-0 points are accurate", () -> {
      List<Match> matches = List.of(new Match("1", "L", 1, "a", "b", null).result(2, 0),
          new Match("2", "L", 2, "a", "c", null).result(1, 1),
          new Match("3", "L", 3, "b", "c", null).result(3, 2));
      List<LeagueStanding> table = new RoundRobinRules().table(List.of("a", "b", "c"), matches, t -> t);
      LeagueStanding top = table.get(0);
      check(top.teamId().equals("a") && top.points() == 4 && top.won() == 1 && top.drawn() == 1 && top.lost() == 0 && top.played() == 2, "Top counters wrong");
      check(top.scored() == 3 && top.conceded() == 1 && top.difference() == 2, "Scores wrong");
      check(table.get(1).points() == 3 && table.get(2).points() == 1, "3-1-0 wrong");
    });
    test("AR-17", "League tied points use wins then score difference and scores for", () -> {
      RoundRobinRules rules = new RoundRobinRules();
      List<Match> cycle = List.of(new Match("1", "L", 1, "a", "b", null).result(3, 0),
          new Match("2", "L", 2, "b", "c", null).result(2, 0), new Match("3", "L", 3, "c", "a", null).result(1, 0));
      check(rules.table(List.of("a", "b", "c"), cycle, t -> t).get(0).teamId().equals("a"), "Difference tie-break failed");
      List<Match> even = List.of(new Match("1", "L", 1, "a", "b", null).result(2, 1),
          new Match("2", "L", 2, "b", "c", null).result(4, 3), new Match("3", "L", 3, "c", "a", null).result(6, 5));
      check(rules.table(List.of("a", "b", "c"), even, t -> t).get(0).teamId().equals("c"), "Scores-for tie-break failed");
      List<Match> wins = List.of(new Match("1", "L", 1, "a", "x", null).result(1, 0),
          new Match("2", "L", 2, "b", "x", null).result(9, 9), new Match("3", "L", 3, "b", "y", null).result(8, 8),
          new Match("4", "L", 4, "b", "z", null).result(7, 7));
      check(rules.table(List.of("a", "b", "x", "y", "z"), wins, t -> t).get(0).teamId().equals("a"), "Wins tie-break failed");
    });
    test("AR-18", "Completely tied league results use case-insensitive names then stable IDs", () -> {
      List<Match> draw = List.of(new Match("1", "L", 1, "a", "b", null).result(0, 0));
      RoundRobinRules rules = new RoundRobinRules();
      check(rules.table(List.of("b", "a"), draw, t -> t.equals("a") ? "Zebra" : "alpha").get(0).teamId().equals("b"), "Name tie-break failed");
      check(rules.table(List.of("b", "a"), draw, t -> "Same").get(0).teamId().equals("a"), "ID tie-break failed");
    });
    test("AR-19", "League cannot certify early and completed fixtures publish and export full statistics", () -> {
      try (Fixture f = new Fixture()) {
        rejects("every league", () -> f.service.advance("league"));
        f.login("judge3");
        for (Match m : f.service.matches("league")) f.service.recordMatchScore(m.id(), 2, 1);
        Match decided = f.service.matches("league").get(0);
        rejects("already final", () -> f.service.recordMatchScore(decided.id(), 0, 0));
        check(f.service.matches("league").size() == 3, "League created duplicate fixtures");
        f.login("organizer1"); f.service.advance("league"); f.service.advance("league");
        f.login("student1"); check(f.service.leagueStandings("league").size() == 3, "Published table missing");
        String csv = f.service.standingsCsv("league");
        check(csv.startsWith("Rank,Team,Played,Wins,Draws,Losses,Scored,Conceded,Difference,Points") && csv.lines().count() == 4, "Incomplete league CSV");
      }
    });
    test("AR-20", "Quiz leagues accept draws within configured points and reject out-of-range scores", () -> {
      try (Fixture f = new Fixture()) {
        String cid = f.start(CompetitionType.QUIZ, TournamentFormat.ROUND_ROBIN, 2);
        f.login("judge1"); Match m = f.service.matches(cid).get(0);
        rejects("0", () -> f.service.recordMatchScore(m.id(), -1, 0));
        rejects("0", () -> f.service.recordMatchScore(m.id(), 0, 21));
        f.service.recordMatchScore(m.id(), 20, 20);
        check(f.service.matches(cid).get(0).drawn(), "Draw not stored");
        f.login("organizer1"); f.service.advance(cid); f.service.advance(cid);
        check(f.service.leagueStandings(cid).stream().allMatch(s -> s.points() == 1), "Draw points incorrect");
      }
    });
    test("AR-21", "General league scoring rejects values above the supported limit", () -> {
      try (Fixture f = new Fixture()) {
        f.login("judge3"); Match m = f.service.matches("league").get(0);
        rejects("0", () -> f.service.recordMatchScore(m.id(), 10001, 0));
        rejects("scores", () -> f.service.recordWinner(m.id(), m.leftTeamId()));
        f.service.recordMatchScore(m.id(), 10000, 0);
        check(f.service.matches("league").get(0).leftScore() == 10000, "Boundary rejected");
      }
    });
    test("AR-22", "Failed match write rolls back the result new round and audit then succeeds on retry", () -> {
      try (Fixture f = new Fixture()) {
        String cid = f.start(CompetitionType.QUIZ, TournamentFormat.KNOCKOUT, 3);
        int audit = f.service.audit().size(); f.login("judge1");
        Match m = f.service.matches(cid).stream().filter(x -> !x.decided()).findFirst().orElseThrow();
        byte[] original = Files.readAllBytes(f.file); Path blocker = Path.of(f.file + ".tmp");
        Files.writeString(blocker, "Existing recovery file must be preserved");
        rejects("no changes", () -> f.service.recordMatchScore(m.id(), 20, 19));
        check(Arrays.equals(original, Files.readAllBytes(f.file)), "Failed save changed disk");
        check(f.service.matches(cid).size() == 2 && f.service.matches(cid).stream().filter(x -> !x.decided()).count() == 1, "Failed result leaked");
        f.login("organizer1"); check(f.service.audit().size() == audit, "Failed audit leaked");
        check(Files.readString(blocker).startsWith("Existing"), "Recovery file overwritten");
        Files.delete(blocker); f.login("judge1"); f.service.recordMatchScore(m.id(), 20, 19);
        check(f.service.matches(cid).size() == 3, "Retry did not generate final");
      }
    });
    test("AR-23", "Failed league write leaves standings and fixture unchanged", () -> {
      try (Fixture f = new Fixture()) {
        List<LeagueStanding> before = f.service.leagueStandings("league");
        f.login("judge3"); Match m = f.service.matches("league").get(0); f.repository.close();
        rejects("no changes", () -> f.service.recordMatchScore(m.id(), 1, 0));
        f.login("organizer1"); check(before.equals(f.service.leagueStandings("league")), "Failed score changed standings");
        try (Repository reopened = new Repository(f.file)) {
          check(!reopened.load().matches().get(m.id()).decided(), "Failed score persisted");
        }
      }
    });
    test("AR-24", "Quiz and league results survive a complete repository restart", () -> {
      Path file;
      try (Fixture f = new Fixture()) {
        file = f.file; f.login("judge2"); f.service.evaluate("quiz", "alpha", 17, 0, 0, "Official score");
        f.login("judge3"); Match m = f.service.matches("league").get(0); f.service.recordMatchScore(m.id(), 2, 2);
      }
      try (Repository repository = new Repository(file)) {
        ArenaService restarted = new ArenaService(repository, false, CLOCK);
        restarted.login("organizer1@campus.test", "Campus@2026");
        check(restarted.evaluations("quiz").get(0).a() == 17, "Quiz points lost");
        check(restarted.matches("league").get(0).drawn(), "Draw lost");
        check(restarted.leagueStandings("league").stream().mapToInt(LeagueStanding::points).sum() == 2, "Draw standings lost");
      }
    });
    test("AR-25", "Legacy bytes migrate without rewriting on read and save as schema two", () -> {
      Path source = Path.of("src/test/resources/legacy-campus-v1.bin"); byte[] old = Files.readAllBytes(source);
      Path copy = Files.createTempDirectory(Path.of("build"), "legacy-arena-").resolve("campus.bin"); Files.write(copy, old);
      try (Repository repository = new Repository(copy)) {
        State migrated = repository.load();
        check(Arrays.equals(old, Files.readAllBytes(copy)), "Read rewrote legacy data");
        check(migrated.schema() == 2, "Schema not upgraded");
        for (Competition c : migrated.competitions().values()) {
          check(c.type() == legacyType(c.format()) && c.tournamentFormat() == legacyTournament(c.format()), "Legacy format misinterpreted");
        }
        repository.save(migrated);
      }
      try (Repository repository = new Repository(copy)) {
        State loaded = repository.load(); check(loaded.schema() == 2 && !loaded.users().isEmpty(), "Migration lost records");
      }
      check(Arrays.equals(old, Files.readAllBytes(source)), "Original fixture changed");
    });
    test("AR-26", "Legacy active knockout continues to champion in Arena", () -> {
      Path copy = Files.createTempDirectory(Path.of("build"), "legacy-workflow-").resolve("campus.bin");
      Files.copy(Path.of("src/test/resources/legacy-active-v1.bin"), copy);
      try (Repository repository = new Repository(copy)) {
        ArenaService service = new ArenaService(repository, false, CLOCK);
        service.login("judge3@campus.test", "Campus@2026");
        while (service.matches("robotics").stream().anyMatch(m -> !m.decided())) {
          Match m = service.matches("robotics").stream().filter(x -> !x.decided()).findFirst().orElseThrow();
          service.recordWinner(m.id(), m.leftTeamId());
        }
        service.login("organizer1@campus.test", "Campus@2026");
        service.advance("robotics"); service.advance("robotics");
        check(service.standings("robotics").get(0).detail().equals("Champion"), "Legacy champion missing");
      }
    });
    test("AR-27", "Failed quiz write restores the prior evaluation", () -> {
      try (Fixture f = new Fixture()) {
        f.login("judge2"); f.service.evaluate("quiz", "alpha", 10, 0, 0, "Original result"); f.repository.close();
        rejects("no changes", () -> f.service.evaluate("quiz", "alpha", 20, 0, 0, "Unsaved change"));
        check(f.service.evaluations("quiz").get(0).a() == 10, "Failed quiz edit leaked");
      }
    });
    test("AR-28", "Match scores cannot change after publication and archived events cannot advance", () -> {
      try (Fixture f = new Fixture()) {
        String cid = f.start(CompetitionType.QUIZ, TournamentFormat.ROUND_ROBIN, 2);
        f.login("judge1"); Match m = f.service.matches(cid).get(0); f.service.recordMatchScore(m.id(), 2, 1);
        f.login("organizer1"); f.service.advance(cid); f.service.advance(cid); f.service.advance(cid);
        rejects("archived", () -> f.service.advance(cid));
        f.login("judge1"); rejects("active", () -> f.service.recordMatchScore(m.id(), 0, 0));
      }
    });
    Files.writeString(Path.of("build/arena-test-results.csv"), "Test ID,Status,Description\n" + String.join("\n", results) + "\n");
    String summary = "Executed " + (passed + failed) + " Arena regression checks: " + passed + " passed, " + failed + " failed.\nExecuted at: " + Instant.now() + "\n";
    Files.writeString(Path.of("build/arena-test-summary.txt"), summary); System.out.print(summary);
    if (failed > 0) throw new AssertionError(failed + " Arena checks failed");
  }
}
