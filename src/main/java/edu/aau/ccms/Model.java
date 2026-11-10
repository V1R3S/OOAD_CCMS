package edu.aau.ccms;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/** Persisted names remain stable for legacy data. Domain writes pass through ArenaService. */
public final class Model {
  private Model() {}

  public enum Role {
    ADMIN,
    ORGANIZER,
    JUDGE,
    STUDENT
  }

  public enum Format {
    PROJECT,
    PROGRAMMING,
    KNOCKOUT
  }

  public enum CompetitionType {
    PROJECT("Project competition"),
    PROGRAMMING("Programming competition"),
    QUIZ("Quiz / academic competition"),
    MATCH("General match competition");

    private final String label;
    CompetitionType(String label) { this.label = label; }
    @Override public String toString() { return label; }

    public boolean supports(TournamentFormat format) {
      if (format == null) return false;
      return switch (this) {
        case PROJECT, PROGRAMMING -> format == TournamentFormat.SCORE_BASED;
        case QUIZ -> true;
        case MATCH -> format != TournamentFormat.SCORE_BASED;
      };
    }
  }

  public enum TournamentFormat {
    SCORE_BASED("Score-based ranking"),
    KNOCKOUT("Single-elimination / knockout"),
    ROUND_ROBIN("Round-robin / league");

    private final String label;
    TournamentFormat(String label) { this.label = label; }
    @Override public String toString() { return label; }
  }

  public enum Phase {
    DRAFT,
    REGISTRATION,
    ACTIVE,
    JUDGING,
    PUBLISHED,
    ARCHIVED
  }

  public record User(String id, String name, String email, Role role, String password)
      implements Serializable {
    @Override
    public String toString() {
      return name + " · " + role;
    }
  }

  public record Team(String id, String name, String captainId, List<String> members)
      implements Serializable {
    public Team {
      members = List.copyOf(members);
    }

    @Override
    public String toString() {
      return name;
    }
  }

  public record Competition(
      String id,
      String title,
      String description,
      Format format,
      Phase phase,
      String organizerId,
      int capacity,
      int teamSize,
      LocalDateTime deadline,
      Set<String> judgeIds,
      CompetitionType type,
      TournamentFormat tournamentFormat,
      int quizMaxPoints)
      implements Serializable {
    public Competition {
      judgeIds = Set.copyOf(judgeIds);
      // Java record deserialization supplies null/zero for fields absent from version-one data.
      if (type == null && tournamentFormat == null) {
        if (format == null) throw new IllegalArgumentException("Missing competition type.");
        type = legacyType(format);
        tournamentFormat = legacyTournament(format);
      }
      if (type == null || !type.supports(tournamentFormat))
        throw new IllegalArgumentException("Unsupported competition type and tournament format.");
      if (type == CompetitionType.QUIZ && (quizMaxPoints < 1 || quizMaxPoints > 10000))
        throw new IllegalArgumentException("Quiz maximum points must be 1–10,000.");
      if (type != CompetitionType.QUIZ) quizMaxPoints = 0;
      format = legacyFormat(type, tournamentFormat);
    }

    /** Legacy constructor retained for old fixtures and callers. */
    public Competition(String id, String title, String description, Format format, Phase phase,
        String organizerId, int capacity, int teamSize, LocalDateTime deadline, Set<String> judgeIds) {
      this(id, title, description, format, phase, organizerId, capacity, teamSize, deadline,
          judgeIds, null, null, 0);
    }

    public Competition(String id, String title, String description, CompetitionType type,
        TournamentFormat tournamentFormat, Phase phase, String organizerId, int capacity,
        int teamSize, LocalDateTime deadline, Set<String> judgeIds, int quizMaxPoints) {
      this(id, title, description, legacyFormat(type, tournamentFormat), phase, organizerId,
          capacity, teamSize, deadline, judgeIds, type, tournamentFormat, quizMaxPoints);
    }

    public Competition withPhase(Phase next) {
      return new Competition(
          id,
          title,
          description,
          format,
          next,
          organizerId,
          capacity,
          teamSize,
          deadline,
          judgeIds,
          type,
          tournamentFormat,
          quizMaxPoints);
    }

    @Override
    public String toString() {
      return title;
    }
  }

  public static CompetitionType legacyType(Format format) {
    if (format == null) throw new IllegalArgumentException("Choose a competition type.");
    return switch (format) {
      case PROJECT -> CompetitionType.PROJECT;
      case PROGRAMMING -> CompetitionType.PROGRAMMING;
      case KNOCKOUT -> CompetitionType.MATCH;
    };
  }

  public static TournamentFormat legacyTournament(Format format) {
    return format == Format.KNOCKOUT ? TournamentFormat.KNOCKOUT : TournamentFormat.SCORE_BASED;
  }

  private static Format legacyFormat(CompetitionType type, TournamentFormat tournament) {
    if (type == CompetitionType.PROJECT && tournament == TournamentFormat.SCORE_BASED) return Format.PROJECT;
    if (type == CompetitionType.PROGRAMMING && tournament == TournamentFormat.SCORE_BASED) return Format.PROGRAMMING;
    if (type == CompetitionType.MATCH && tournament == TournamentFormat.KNOCKOUT) return Format.KNOCKOUT;
    return null; // New combinations have no legacy equivalent. Never mislabel them as PROJECT.
  }

  public record Registration(String id, String competitionId, String teamId)
      implements Serializable {}

  public record Submission(
      String id,
      String competitionId,
      String teamId,
      String title,
      String description,
      String url,
      LocalDateTime submittedAt)
      implements Serializable {}

  /** Project: a,b,c are rubric scores. Programming: solved, elapsed, wrong attempts. Quiz: a is points; b,c are zero. */
  public record Evaluation(
      String id,
      String competitionId,
      String teamId,
      String judgeId,
      int a,
      int b,
      int c,
      String feedback)
      implements Serializable {}

  public record Match(
      String id,
      String competitionId,
      int round,
      String leftTeamId,
      String rightTeamId,
      String winnerId,
      Integer leftScore,
      Integer rightScore,
      boolean drawn)
      implements Serializable {
    public Match(String id, String competitionId, int round, String leftTeamId,
        String rightTeamId, String winnerId) {
      this(id, competitionId, round, leftTeamId, rightTeamId, winnerId, null, null, false);
    }

    public Match winner(String team) {
      return new Match(id, competitionId, round, leftTeamId, rightTeamId, team, null, null, false);
    }

    public boolean bye() { return rightTeamId == null; }
    public boolean decided() { return winnerId != null || drawn; }
    public Match result(int left, int right) {
      return new Match(id, competitionId, round, leftTeamId, rightTeamId,
          left == right ? null : left > right ? leftTeamId : rightTeamId, left, right, left == right);
    }
  }

  public record Standing(int rank, String teamId, String team, double score, String detail) {}

  public record LeagueStanding(int rank, String teamId, String team, int played, int won,
      int drawn, int lost, int scored, int conceded, int points) {
    public int difference() { return scored - conceded; }
  }

  public record Audit(LocalDateTime time, String actor, String action) implements Serializable {}

  public static final class State implements Serializable {
    private static final long serialVersionUID = 1L;
    private int schema = 2;

    public int schema() {
      return schema;
    }

    public void upgradeSchema() {
      if (schema != 1 && schema != 2) throw new IllegalArgumentException("Unsupported data version.");
      schema = 2;
    }

    private Map<String, User> users = new LinkedHashMap<>();

    public Map<String, User> users() {
      return users;
    }

    private Map<String, Team> teams = new LinkedHashMap<>();

    public Map<String, Team> teams() {
      return teams;
    }

    private Map<String, Competition> competitions = new LinkedHashMap<>();

    public Map<String, Competition> competitions() {
      return competitions;
    }

    private Map<String, Registration> registrations = new LinkedHashMap<>();

    public Map<String, Registration> registrations() {
      return registrations;
    }

    private Map<String, Submission> submissions = new LinkedHashMap<>();

    public Map<String, Submission> submissions() {
      return submissions;
    }

    private Map<String, Evaluation> evaluations = new LinkedHashMap<>();

    public Map<String, Evaluation> evaluations() {
      return evaluations;
    }

    private Map<String, Match> matches = new LinkedHashMap<>();

    public Map<String, Match> matches() {
      return matches;
    }

    private List<Audit> audit = new ArrayList<>();

    public List<Audit> audit() {
      return audit;
    }
  }
}
