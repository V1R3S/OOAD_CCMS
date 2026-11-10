package edu.aau.arena.service;

import static edu.aau.ccms.Model.*;

import edu.aau.arena.util.Passwords;
import edu.aau.arena.rules.TournamentRules;
import java.time.*;
import java.util.*;

/** Fictional teaching data, seeded only when no database exists. */
public final class DemoData {
  public static void populate(State s, LocalDateTime now) {
    String[] names = {
      "Maya Hassan",
      "Omar Salem",
      "Noor Ali",
      "Zayed Ahmed",
      "Sara Ibrahim",
      "Rashid Khalid",
      "Lina Saeed",
      "Faisal Noor",
      "Hessa Omar",
      "Ali Hamad"
    };
    for (int i = 1; i <= 10; i++) user(s, "student" + i, names[i - 1], Role.STUDENT);
    user(s, "admin", "Arena Administrator", Role.ADMIN);
    user(s, "organizer1", "Amina Mansour", Role.ORGANIZER);
    user(s, "organizer2", "Yousef Nasser", Role.ORGANIZER);
    user(s, "judge1", "Dr. Leila Faris", Role.JUDGE);
    user(s, "judge2", "Dr. Sami Adel", Role.JUDGE);
    user(s, "judge3", "Dr. Hana Karim", Role.JUDGE);
    team(s, "alpha", "Team Alpha", "student1", "student2");
    team(s, "warriors", "Code Warriors", "student3", "student4");
    team(s, "innovators", "Innovators", "student5", "student6");
    team(s, "circuit", "Circuit Breakers", "student7", "student8");
    team(s, "nova", "Nova Labs", "student9", "student10");
    competition(
        s,
        "hackathon",
        "Hackathon 2026",
        "Build a thoughtful solution for a more connected, sustainable campus.",
        Format.PROJECT,
        Phase.REGISTRATION,
        12,
        4,
        now.plusDays(30),
        Set.of("judge1", "judge2"));
    competition(
        s,
        "programming",
        "Programming Contest",
        "Ten problems. Five hours. Precision, teamwork, and a little ingenuity.",
        Format.PROGRAMMING,
        Phase.PUBLISHED,
        16,
        3,
        now.plusDays(30),
        Set.of("judge1"));
    competition(
        s,
        "robotics",
        "Robotics Challenge",
        "A head-to-head robotics bracket, from the opening round to a campus champion.",
        Format.KNOCKOUT,
        Phase.ACTIVE,
        8,
        4,
        now.plusDays(45),
        Set.of("judge3"));
    for (String tid : List.of("alpha", "warriors", "innovators")) registration(s, "hackathon", tid);
    int i = 0;
    for (String tid : List.of("alpha", "warriors", "innovators")) {
      registration(s, "programming", tid);
      Submission sub =
          new Submission(
              "sub" + i,
              "programming",
              tid,
              "Contest solution set",
              "Demonstration submission; external link is illustrative.",
              "https://example.com/arena/" + tid,
              now.minusDays(2));
      s.submissions().put(sub.id(), sub);
      Evaluation e =
          new Evaluation(
              "eval" + i,
              "programming",
              tid,
              "judge1",
              new int[] {8, 9, 8}[i],
              new int[] {180, 240, 190}[i],
              new int[] {2, 1, 2}[i],
              "Official demo result verified against the sample score sheet.");
      s.evaluations().put(e.id(), e);
      i++;
    }
    for (String tid : List.of("alpha", "warriors", "innovators", "circuit"))
      registration(s, "robotics", tid);
    s.matches().put("semi1", new Match("semi1", "robotics", 1, "alpha", "warriors", null));
    s.matches().put("semi2", new Match("semi2", "robotics", 1, "innovators", "circuit", null));
    s.competitions().put("quiz", new Competition("quiz", "Academic Quiz", 
        "Test your knowledge together. A judge records each team's official points out of 20.",
        CompetitionType.QUIZ, TournamentFormat.SCORE_BASED, Phase.JUDGING, "organizer1",
        12, 4, now.plusDays(30), Set.of("judge2"), 20));
    s.competitions().put("league", new Competition("league", "Campus League",
        "Every team meets once. Win three points, draw one, and climb the league table.",
        CompetitionType.MATCH, TournamentFormat.ROUND_ROBIN, Phase.ACTIVE, "organizer1",
        12, 4, now.plusDays(30), Set.of("judge3"), 0));
    List<String> leagueTeams = List.of("alpha", "warriors", "innovators");
    for (String teamId : leagueTeams) {
      registration(s, "quiz", teamId);
      registration(s, "league", teamId);
    }
    for (Match match : TournamentRules.forFormat(TournamentFormat.ROUND_ROBIN).start("league", leagueTeams))
      s.matches().put(match.id(), match);
    s.audit()
        .add(
            new Audit(
                now,
                "System",
                "Loaded fictional Arena demonstration data: 16 users, 5 teams, 5 competitions."));
  }

  private static void user(State s, String id, String name, Role role) {
    s.users().put(id, new User(id, name, id + "@campus.test", role, Passwords.hash("Campus@2026")));
  }

  private static void team(State s, String id, String name, String captain, String member) {
    s.teams().put(id, new Team(id, name, captain, List.of(captain, member)));
  }

  private static void competition(
      State s,
      String id,
      String title,
      String description,
      Format format,
      Phase phase,
      int capacity,
      int size,
      LocalDateTime deadline,
      Set<String> judges) {
    s.competitions()
        .put(
            id,
            new Competition(
                id,
                title,
                description,
                format,
                phase,
                "organizer1",
                capacity,
                size,
                deadline,
                judges));
  }

  private static void registration(State s, String cid, String tid) {
    String id = cid + "-" + tid;
    s.registrations().put(id, new Registration(id, cid, tid));
  }
}
