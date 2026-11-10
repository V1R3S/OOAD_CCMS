package edu.aau.arena.service;

import static edu.aau.ccms.Model.*;

import edu.aau.arena.model.UserSummary;
import edu.aau.arena.repository.Repository;
import edu.aau.arena.rules.CompetitionRules;
import edu.aau.arena.rules.TournamentRules;
import edu.aau.arena.rules.RoundRobinRules;
import edu.aau.arena.util.Passwords;
import java.io.*;
import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.*;

/** Application facade: authorization, invariants, workflow, and durable transactions. */
public final class ArenaService {
  private State state;
  private final Repository repository;
  private final Clock clock;
  private String session;

  public ArenaService(Repository repository, boolean demo) throws IOException {
    this(repository, demo, Clock.systemDefaultZone());
  }

  public ArenaService(Repository repository, boolean demo, Clock clock) throws IOException {
    this.repository = repository;
    this.clock = clock;
    state = repository.load();
    if (state == null) {
      state = new State();
      if (demo) DemoData.populate(state, now());
      repository.save(state);
    }
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock);
  }

  private static String id() {
    return UUID.randomUUID().toString();
  }

  private static String text(String value, String label, int max) {
    if (value == null || value.isBlank() || value.strip().length() > max)
      throw new IllegalArgumentException(label + " is required (maximum " + max + " characters).");
    return value.strip();
  }

  private static void require(boolean condition, String message) {
    if (!condition) throw new IllegalArgumentException(message);
  }

  public UserSummary login(String email, String password) {
    session = null;
    require(email != null && password != null, "Email or password is incorrect.");
    User user =
        state.users().values().stream()
            .filter(u -> u.email().equalsIgnoreCase(email.strip()))
            .findFirst()
            .orElse(null);
    if (user == null || !Passwords.verify(password, user.password()))
      throw new IllegalArgumentException("Email or password is incorrect.");
    session = user.id();
    return summary(user);
  }

  public void logout() {
    session = null;
  }

  private User currentAccount() {
    User user = state.users().get(session);
    if (user == null) throw new IllegalArgumentException("Sign in to continue.");
    return user;
  }

  private User role(Role... roles) {
    User user = currentAccount();
    require(Arrays.asList(roles).contains(user.role()), "Your role cannot perform this action.");
    return user;
  }

  private static UserSummary summary(User u) {
    return new UserSummary(u.id(), u.name(), u.email(), u.role());
  }

  public UserSummary currentUser() {
    return summary(currentAccount());
  }

  public List<UserSummary> users() {
    role(Role.ADMIN);
    return state.users().values().stream().map(ArenaService::summary).toList();
  }

  public List<UserSummary> judges() {
    role(Role.ADMIN, Role.ORGANIZER);
    return state.users().values().stream()
        .filter(u -> u.role() == Role.JUDGE)
        .map(ArenaService::summary)
        .toList();
  }

  public List<Team> teams() {
    currentUser();
    return List.copyOf(state.teams().values());
  }

  public List<Competition> competitions() {
    currentUser();
    return List.copyOf(state.competitions().values());
  }

  public List<Audit> audit() {
    role(Role.ADMIN, Role.ORGANIZER);
    return List.copyOf(state.audit());
  }

  public List<Registration> registrations(String cid) {
    competition(cid);
    return state.registrations().values().stream()
        .filter(r -> r.competitionId().equals(cid))
        .toList();
  }

  public List<Submission> submissions(String cid) {
    Competition c = competition(cid);
    User user = currentAccount();
    require(
        manages(c) || c.judgeIds().contains(user.id()) || user.role() == Role.STUDENT,
        "Only assigned judges and competition staff may view submissions.");
    return state.submissions().values().stream()
        .filter(s -> s.competitionId().equals(cid))
        .filter(s -> user.role() != Role.STUDENT || team(s.teamId()).members().contains(user.id()))
        .toList();
  }

  public List<Evaluation> evaluations(String cid) {
    Competition c = competition(cid);
    User user = currentAccount();
    return state.evaluations().values().stream()
        .filter(e -> e.competitionId().equals(cid))
        .filter(
            e ->
                manages(c)
                    || e.judgeId().equals(user.id())
                    || (c.phase().ordinal() >= Phase.PUBLISHED.ordinal()
                        && team(e.teamId()).members().contains(user.id())))
        .toList();
  }

  public List<Match> matches(String cid) {
    competition(cid);
    return state.matches().values().stream().filter(m -> m.competitionId().equals(cid)).toList();
  }

  public Competition competition(String cid) {
    currentUser();
    Competition c = state.competitions().get(cid);
    require(c != null, "Competition does not exist.");
    return c;
  }

  public Team team(String tid) {
    currentUser();
    Team t = state.teams().get(tid);
    require(t != null, "Team does not exist.");
    return t;
  }

  public String userName(String uid) {
    currentUser();
    User u = state.users().get(uid);
    return u == null ? "Unknown" : u.name();
  }

  public boolean manages(Competition c) {
    User u = currentAccount();
    return u.role() == Role.ADMIN || (u.role() == Role.ORGANIZER && c.organizerId().equals(u.id()));
  }

  public static boolean requiresSubmission(Competition c) {
    return c.tournamentFormat() == TournamentFormat.SCORE_BASED
        && CompetitionRules.forCompetition(c).requiresSubmission();
  }

  public static String rulesExplanation(Competition c) {
    String scoring = c.tournamentFormat() == TournamentFormat.SCORE_BASED
        ? CompetitionRules.forCompetition(c).explanation()
        : c.type() == CompetitionType.QUIZ
            ? "Quiz matches: an assigned judge records both teams' points (0–" + c.quizMaxPoints()
                + "). No submissions or automatic question engine. Knockout ties require an external tie-break before entry."
            : "General matches: judges confirm a knockout winner or enter both teams' scores for league fixtures.";
    return scoring + "\n"
        + TournamentRules.forFormat(c.tournamentFormat()).explanation();
  }

  private void manager(Competition c) {
    require(
        manages(c), "Only the owning organizer or an administrator can manage this competition.");
  }

  private void captain(Team t) {
    require(
        t.captainId().equals(currentUser().id()), "Only the team captain can perform this action.");
  }

  private <T> T change(String action, Supplier<T> operation) {
    byte[] backup;
    try {
      backup = Repository.encode(state);
    } catch (IOException e) {
      throw new IllegalStateException("Cannot prepare a safe update.", e);
    }
    try {
      T result = operation.get();
      state.audit().add(new Audit(now(), currentUser().name(), action));
      if (state.audit().size() > 2000) state.audit().remove(0);
      repository.save(state);
      return result;
    } catch (Exception failure) {
      try {
        state = Repository.decode(backup);
      } catch (Exception restore) {
        throw new IllegalStateException("Cannot restore the in-memory transaction.", restore);
      }
      if (failure instanceof RuntimeException runtime) throw runtime;
      throw new IllegalStateException(
          "The update could not be saved; no changes were applied.", failure);
    }
  }

  public UserSummary createUser(String name, String email, Role userRole, String password) {
    role(Role.ADMIN);
    String n = text(name, "Name", 80), e = text(email, "Email", 120).toLowerCase(Locale.ROOT);
    require(e.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"), "Enter a valid email address.");
    require(
        state.users().values().stream().noneMatch(u -> u.email().equalsIgnoreCase(e)),
        "Email is already registered.");
    require(userRole != null, "Choose an account role.");
    String hash = Passwords.hash(password);
    return change(
        "Created " + userRole + " account: " + n,
        () -> {
          User u = new User(id(), n, e, userRole, hash);
          state.users().put(u.id(), u);
          return summary(u);
        });
  }

  public void changePassword(String oldPassword, String newPassword) {
    User u = currentAccount();
    require(Passwords.verify(oldPassword, u.password()), "Current password is incorrect.");
    String hash = Passwords.hash(newPassword);
    change(
        "Changed own password",
        () -> {
          state.users().put(u.id(), new User(u.id(), u.name(), u.email(), u.role(), hash));
          return null;
        });
  }

  public Team createTeam(String name) {
    User u = role(Role.STUDENT);
    String n = text(name, "Team name", 60);
    require(
        state.teams().values().stream().noneMatch(t -> t.name().equalsIgnoreCase(n)),
        "That team name is already in use.");
    return change(
        "Created team: " + n,
        () -> {
          Team t = new Team(id(), n, u.id(), List.of(u.id()));
          state.teams().put(t.id(), t);
          return t;
        });
  }

  public void joinTeam(String tid) {
    User u = role(Role.STUDENT);
    Team t = team(tid);
    require(!t.members().contains(u.id()), "You already belong to this team.");
    require(
        state.registrations().values().stream().noneMatch(r -> r.teamId().equals(tid)),
        "This roster is locked because the team is registered. Join an unregistered team.");
    require(t.members().size() < 8, "A team can have at most eight members.");
    change(
        "Joined team: " + t.name(),
        () -> {
          List<String> members = new ArrayList<>(t.members());
          members.add(u.id());
          state.teams().put(tid, new Team(tid, t.name(), t.captainId(), members));
          return null;
        });
  }

  public void leaveTeam(String tid) {
    Team t = team(tid);
    String uid = role(Role.STUDENT).id();
    require(t.members().contains(uid), "You are not a member of this team.");
    require(
        !t.captainId().equals(uid),
        "The captain must keep the team. Create or join another team if needed.");
    require(
        state.registrations().values().stream().noneMatch(r -> r.teamId().equals(tid)),
        "Registered team rosters are locked.");
    change(
        "Left team: " + t.name(),
        () -> {
          List<String> members = new ArrayList<>(t.members());
          members.remove(uid);
          state.teams().put(tid, new Team(tid, t.name(), t.captainId(), members));
          return null;
        });
  }

  public Competition createCompetition(
      String title,
      String description,
      Format format,
      int capacity,
      int teamSize,
      LocalDateTime deadline,
      Set<String> judges) {
    return createCompetition(title, description, legacyType(format), legacyTournament(format),
        capacity, teamSize, deadline, judges, 0);
  }

  public Competition createCompetition(String title, String description, CompetitionType type,
      TournamentFormat tournamentFormat, int capacity, int teamSize, LocalDateTime deadline,
      Set<String> judges, int quizMaxPoints) {
    User owner = role(Role.ADMIN, Role.ORGANIZER);
    Competition c =
        validatedCompetition(
            id(), title, description, type, tournamentFormat, owner.id(), capacity, teamSize,
            deadline, judges, quizMaxPoints);
    return change(
        "Created competition: " + c.title(),
        () -> {
          state.competitions().put(c.id(), c);
          return c;
        });
  }

  public void editCompetition(
      String cid,
      String title,
      String description,
      Format format,
      int capacity,
      int teamSize,
      LocalDateTime deadline,
      Set<String> judges) {
    editCompetition(cid, title, description, legacyType(format), legacyTournament(format),
        capacity, teamSize, deadline, judges, 0);
  }

  public void editCompetition(String cid, String title, String description, CompetitionType type,
      TournamentFormat tournamentFormat, int capacity, int teamSize, LocalDateTime deadline,
      Set<String> judges, int quizMaxPoints) {
    Competition old = competition(cid);
    manager(old);
    require(
        old.phase() == Phase.DRAFT,
        "Rules can only be edited in draft, before registration opens.");
    Competition c =
        validatedCompetition(
            cid,
            title,
            description,
            type,
            tournamentFormat,
            old.organizerId(),
            capacity,
            teamSize,
            deadline,
            judges,
            quizMaxPoints);
    change(
        "Updated draft: " + c.title(),
        () -> {
          state.competitions().put(cid, c);
          return null;
        });
  }

  private Competition validatedCompetition(
      String cid,
      String title,
      String description,
      CompetitionType type,
      TournamentFormat tournamentFormat,
      String owner,
      int capacity,
      int teamSize,
      LocalDateTime deadline,
      Set<String> judges,
      int quizMaxPoints) {
    require(type != null, "Choose a competition type.");
    require(tournamentFormat != null && type.supports(tournamentFormat),
        "This competition type does not support that tournament format.");
    require(capacity >= 2 && capacity <= 64, "Capacity must be 2–64 teams.");
    require(teamSize >= 1 && teamSize <= 8, "Team size must be 1–8 students.");
    require(
        deadline != null && deadline.isAfter(now()), "Submission deadline must be in the future.");
    require(judges != null && !judges.isEmpty(), "Assign at least one judge.");
    for (String uid : judges)
      require(
          state.users().containsKey(uid) && state.users().get(uid).role() == Role.JUDGE,
          "Assign only judge accounts.");
    if (tournamentFormat != TournamentFormat.SCORE_BASED)
      require(capacity <= 16, "Match tournaments support 2–16 teams; knockout byes are automatic.");
    if (type == CompetitionType.QUIZ)
      require(quizMaxPoints >= 1 && quizMaxPoints <= 10000, "Quiz maximum points must be 1–10,000.");
    return new Competition(
        cid,
        text(title, "Title", 100),
        text(description, "Description", 2000),
        type,
        tournamentFormat,
        Phase.DRAFT,
        owner,
        capacity,
        teamSize,
        deadline,
        judges,
        quizMaxPoints);
  }

  public void register(String cid, String tid) {
    Competition c = competition(cid);
    Team t = team(tid);
    captain(t);
    require(c.phase() == Phase.REGISTRATION, "Registration is not open.");
    require(now().isBefore(c.deadline()), "The registration/submission deadline has passed.");
    require(t.members().size() <= c.teamSize(), "This team exceeds the allowed team size.");
    List<Registration> existing = registrations(cid);
    require(existing.size() < c.capacity(), "Competition is full.");
    require(
        existing.stream().noneMatch(r -> r.teamId().equals(tid)), "Team is already registered.");
    require(
        existing.stream()
            .noneMatch(r -> team(r.teamId()).members().stream().anyMatch(t.members()::contains)),
        "A member is already registered in another team for this competition.");
    change(
        "Registered " + t.name() + " in " + c.title(),
        () -> {
          Registration r = new Registration(id(), cid, tid);
          state.registrations().put(r.id(), r);
          return null;
        });
  }

  public void withdraw(String cid, String tid) {
    Competition c = competition(cid);
    captain(team(tid));
    require(
        c.phase() == Phase.REGISTRATION, "Withdrawal is allowed only while registration is open.");
    require(registered(cid, tid), "Team is not registered.");
    change(
        "Withdrew " + team(tid).name() + " from " + c.title(),
        () -> {
          state
              .registrations()
              .values()
              .removeIf(r -> r.competitionId().equals(cid) && r.teamId().equals(tid));
          return null;
        });
  }

  private boolean registered(String cid, String tid) {
    return registrations(cid).stream().anyMatch(r -> r.teamId().equals(tid));
  }

  public void submit(String cid, String tid, String title, String description, String url) {
    Competition c = competition(cid);
    Team t = team(tid);
    captain(t);
    require(requiresSubmission(c), "This competition uses scores or matches, not submissions.");
    require(c.phase() == Phase.ACTIVE, "Submissions are accepted only during the active phase.");
    require(now().isBefore(c.deadline()), "The submission deadline has passed.");
    require(registered(cid, tid), "Register the team first.");
    String heading = text(title, "Submission title", 100),
        body = text(description, "Description", 4000),
        link = text(url, "Project URL", 500);
    try {
      URI uri = URI.create(link);
      require(
          Set.of("https", "http").contains(String.valueOf(uri.getScheme()).toLowerCase(Locale.ROOT))
              && uri.getHost() != null
              && uri.getUserInfo() == null,
          "Use a valid HTTP or HTTPS project URL.");
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Use a valid HTTP or HTTPS project URL.");
    }
    change(
        "Submitted work for " + t.name() + " in " + c.title(),
        () -> {
          state
              .submissions()
              .values()
              .removeIf(s -> s.competitionId().equals(cid) && s.teamId().equals(tid));
          Submission s = new Submission(id(), cid, tid, heading, body, link, now());
          state.submissions().put(s.id(), s);
          return null;
        });
  }

  public void evaluate(String cid, String tid, int a, int b, int pointsC, String feedback) {
    Competition c = competition(cid);
    User judge = role(Role.JUDGE);
    require(c.judgeIds().contains(judge.id()), "You are not assigned to this competition.");
    require(c.phase() == Phase.JUDGING, "Scoring is available only during judging.");
    require(c.tournamentFormat() == TournamentFormat.SCORE_BASED,
        "Use match results for this tournament format.");
    require(registered(cid, tid), "Team is not registered.");
    require(
        !requiresSubmission(c) || state.submissions().values().stream()
            .anyMatch(s -> s.competitionId().equals(cid) && s.teamId().equals(tid)),
        "A submission is required before scoring.");
    CompetitionRules rules = CompetitionRules.forCompetition(c);
    rules.validate(a, b, pointsC);
    String note = text(feedback, "Feedback", 1000);
    if (!rules.requiresAllJudges())
      require(
          state.evaluations().values().stream()
              .noneMatch(
                  e ->
                      e.competitionId().equals(cid)
                          && e.teamId().equals(tid)
                          && !e.judgeId().equals(judge.id())),
          "Another judge already owns this official result.");
    change(
        "Scored " + team(tid).name() + " in " + c.title(),
        () -> {
          state
              .evaluations()
              .values()
              .removeIf(
                  e ->
                      e.competitionId().equals(cid)
                          && e.teamId().equals(tid)
                          && e.judgeId().equals(judge.id()));
          Evaluation e = new Evaluation(id(), cid, tid, judge.id(), a, b, pointsC, note);
          state.evaluations().put(e.id(), e);
          return null;
        });
  }

  public void advance(String cid) {
    Competition c = competition(cid);
    manager(c);
    require(c.phase() != Phase.ARCHIVED, "This competition is already archived.");
    List<Registration> entries = registrations(cid);
    TournamentRules tournament = TournamentRules.forFormat(c.tournamentFormat());
    if (c.phase() == Phase.DRAFT)
      require(
          now().isBefore(c.deadline()),
          "Deadline has passed. Edit the draft before opening registration.");
    if (c.phase() == Phase.REGISTRATION) {
      require(entries.size() >= 2, "At least two teams must register before starting.");
      if (c.tournamentFormat() != TournamentFormat.SCORE_BASED)
        require(entries.size() <= 16, "Match tournaments support 2–16 teams.");
      else if (requiresSubmission(c))
        require(now().isBefore(c.deadline()), "The submission deadline has passed.");
    }
    if (c.phase() == Phase.ACTIVE) {
      if (c.tournamentFormat() != TournamentFormat.SCORE_BASED)
        require(tournament.matchesComplete(matches(cid)), c.tournamentFormat() == TournamentFormat.KNOCKOUT
            ? "Complete every bracket round before certifying results."
            : "Complete every league fixture before certifying results.");
      else if (requiresSubmission(c))
        for (Registration r : entries)
          require(
              state.submissions().values().stream()
                  .anyMatch(s -> s.competitionId().equals(cid) && s.teamId().equals(r.teamId())),
              "Every registered team must submit before judging begins.");
    }
    if (c.phase() == Phase.JUDGING && c.tournamentFormat() == TournamentFormat.SCORE_BASED) {
      for (Registration r : entries) {
        List<Evaluation> scores = rawEvaluations(cid, r.teamId());
        require(
            !CompetitionRules.forCompetition(c).requiresAllJudges()
                ? scores.size() == 1
                : scores.stream()
                    .map(Evaluation::judgeId)
                    .collect(Collectors.toSet())
                    .containsAll(c.judgeIds()),
            "Complete all required evaluations before publishing results.");
      }
    }
    Phase next = Phase.values()[c.phase().ordinal() + 1];
    change(
        c.title() + ": " + c.phase() + " → " + next,
        () -> {
          if (c.phase() == Phase.REGISTRATION) {
            require(matches(cid).isEmpty(), "Fixtures already exist for this competition.");
            tournament.start(cid, entries.stream().map(Registration::teamId).toList())
                .forEach(match -> state.matches().put(match.id(), match));
          }
          state.competitions().put(cid, c.withPhase(next));
          return null;
        });
  }

  public void recordWinner(String matchId, String winnerId) {
    Match match = pendingMatch(matchId);
    Competition c = competition(match.competitionId());
    require(c.type() == CompetitionType.MATCH && c.tournamentFormat() == TournamentFormat.KNOCKOUT,
        "Record both teams' scores for quiz or league matches.");
    require(
        winnerId != null
            && (winnerId.equals(match.leftTeamId()) || winnerId.equals(match.rightTeamId())),
        "Winner must be one of the two teams in this match.");
    saveMatchResult(c, match.winner(winnerId), "Recorded bracket winner: " + team(winnerId).name());
  }

  public void recordMatchScore(String matchId, int leftScore, int rightScore) {
    Match match = pendingMatch(matchId);
    Competition c = competition(match.competitionId());
    require(c.tournamentFormat() == TournamentFormat.ROUND_ROBIN || c.type() == CompetitionType.QUIZ,
        "Choose the confirmed winner for this general knockout match.");
    int maximum = c.type() == CompetitionType.QUIZ ? c.quizMaxPoints() : 10000;
    CompetitionRules.range(leftScore, maximum, "Left team score");
    CompetitionRules.range(rightScore, maximum, "Right team score");
    require(c.tournamentFormat() != TournamentFormat.KNOCKOUT || leftScore != rightScore,
        "A knockout match cannot end in a draw. Complete a tie-break outside Arena, then enter the final scores.");
    saveMatchResult(c, match.result(leftScore, rightScore),
        "Recorded match: " + team(match.leftTeamId()).name() + " " + leftScore + "–" + rightScore
            + " " + team(match.rightTeamId()).name());
  }

  private Match pendingMatch(String matchId) {
    Match match = state.matches().get(matchId);
    require(match != null, "Match does not exist.");
    Competition c = competition(match.competitionId());
    User judge = role(Role.JUDGE);
    require(c.judgeIds().contains(judge.id()), "You are not assigned to this competition.");
    require(c.tournamentFormat() != TournamentFormat.SCORE_BASED, "This competition has no fixtures.");
    require(c.phase() == Phase.ACTIVE, "Matches can only be decided while the competition is active.");
    require(!match.decided() && !match.bye(), "This match is already final.");
    return match;
  }

  private void saveMatchResult(Competition c, Match result, String action) {
    change(action, () -> {
      state.matches().put(result.id(), result);
      TournamentRules.forFormat(c.tournamentFormat()).nextRound(c.id(), matches(c.id()))
          .forEach(next -> state.matches().put(next.id(), next));
      return null;
    });
  }

  private List<Evaluation> rawEvaluations(String cid, String tid) {
    return state.evaluations().values().stream()
        .filter(e -> e.competitionId().equals(cid) && e.teamId().equals(tid))
        .toList();
  }

  public List<Standing> standings(String cid) {
    Competition c = competition(cid);
    require(
        c.phase().ordinal() >= Phase.PUBLISHED.ordinal() || manages(c),
        "Results are released when the organizer publishes them.");
    if (c.tournamentFormat() != TournamentFormat.SCORE_BASED)
      return TournamentRules.forFormat(c.tournamentFormat()).standings(
          registrations(cid).stream().map(Registration::teamId).toList(), matches(cid), tid -> team(tid).name());
    CompetitionRules rules = CompetitionRules.forCompetition(c);
    List<Standing> values = new ArrayList<>();
    Map<String, Integer> penalties = new HashMap<>();
    for (Registration r : registrations(cid)) {
      List<Evaluation> scores = rawEvaluations(cid, r.teamId());
      double score;
      String detail;
      if (scores.isEmpty()) continue;
      score = scores.stream().mapToDouble(rules::score).average().orElse(0);
      detail = rules.detail(scores);
      if (c.type() == CompetitionType.PROGRAMMING)
        penalties.put(r.teamId(), scores.get(0).b() + 20 * scores.get(0).c());
      values.add(new Standing(0, r.teamId(), team(r.teamId()).name(), score, detail));
    }
    values.sort(
        Comparator.comparingDouble(Standing::score)
            .reversed()
            .thenComparingInt(s -> penalties.getOrDefault(s.teamId(), 0))
            .thenComparing(Standing::team));
    List<Standing> ranked = new ArrayList<>();
    int rank = 0;
    Standing previous = null;
    for (int i = 0; i < values.size(); i++) {
      Standing s = values.get(i);
      if (previous == null
          || Math.abs(previous.score() - s.score()) > 0.000001
          || !Objects.equals(penalties.get(previous.teamId()), penalties.get(s.teamId())))
        rank = i + 1;
      ranked.add(new Standing(rank, s.teamId(), s.team(), s.score(), s.detail()));
      previous = s;
    }
    return ranked;
  }

  public String standingsCsv(String cid) {
    if (competition(cid).tournamentFormat() == TournamentFormat.ROUND_ROBIN) {
      StringBuilder csv = new StringBuilder("Rank,Team,Played,Wins,Draws,Losses,Scored,Conceded,Difference,Points\r\n");
      for (LeagueStanding s : leagueStandings(cid))
        csv.append(s.rank()).append(',').append(csv(s.team())).append(',').append(s.played())
            .append(',').append(s.won()).append(',').append(s.drawn()).append(',').append(s.lost())
            .append(',').append(s.scored()).append(',').append(s.conceded()).append(',')
            .append(s.difference()).append(',').append(s.points()).append("\r\n");
      return csv.toString();
    }
    StringBuilder out = new StringBuilder("Rank,Team,Score,Details\r\n");
    for (Standing s : standings(cid))
      out.append(s.rank())
          .append(',')
          .append(csv(s.team()))
          .append(',')
          .append(String.format(Locale.ROOT, "%.2f", s.score()))
          .append(',')
          .append(csv(s.detail()))
          .append("\r\n");
    return out.toString();
  }

  public List<LeagueStanding> leagueStandings(String cid) {
    Competition c = competition(cid);
    require(c.tournamentFormat() == TournamentFormat.ROUND_ROBIN, "This is not a league competition.");
    require(c.phase().ordinal() >= Phase.PUBLISHED.ordinal() || manages(c),
        "Results are released when the organizer publishes them.");
    return new RoundRobinRules().table(registrations(cid).stream().map(Registration::teamId).toList(),
        matches(cid), tid -> team(tid).name());
  }

  private static String csv(String value) {
    String safe =
        !value.isEmpty() && "=+@-\t\r".indexOf(value.charAt(0)) >= 0 ? "'" + value : value;
    return '"' + safe.replace("\"", "\"\"") + '"';
  }
}
