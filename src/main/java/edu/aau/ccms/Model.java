package edu.aau.ccms;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/** Domain values are immutable; all writes pass through CampusService. */
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
      Set<String> judgeIds)
      implements Serializable {
    public Competition {
      judgeIds = Set.copyOf(judgeIds);
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
          judgeIds);
    }

    @Override
    public String toString() {
      return title;
    }
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

  /** Project: a,b,c are rubric scores. Programming: solved, elapsed minutes, wrong attempts. */
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
      String winnerId)
      implements Serializable {
    public Match winner(String team) {
      return new Match(id, competitionId, round, leftTeamId, rightTeamId, team);
    }
  }

  public record Standing(int rank, String teamId, String team, double score, String detail) {}

  public record Audit(LocalDateTime time, String actor, String action) implements Serializable {}

  public static final class State implements Serializable {
    private static final long serialVersionUID = 1L;
    private int schema = 1;

    public int schema() {
      return schema;
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
