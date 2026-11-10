package edu.aau.arena.rules;

import static edu.aau.ccms.Model.*;
import java.util.*;
import java.util.function.Function;

public final class RoundRobinRules implements TournamentRules {
  public String explanation() {
    return "Round robin · Every pair meets once. Win 3 points, draw 1, loss 0. "
        + "Ties: points, wins, score difference, scores for, team name, then stable team ID. "
        + "All fixtures must finish before certification and publication. Match results are final.";
  }

  public List<Match> start(String cid, List<String> teams) {
    TournamentRules.validateEntrants(teams);
    List<String> rotation = new ArrayList<>(teams);
    if (rotation.size() % 2 != 0) rotation.add(null);
    List<Match> fixtures = new ArrayList<>();
    int count = rotation.size();
    for (int round = 1; round < count; round++) {
      for (int i = 0; i < count / 2; i++) {
        String left = rotation.get(i), right = rotation.get(count - 1 - i);
        if (left != null && right != null) fixtures.add(TournamentRules.match(cid, round, left, right));
      }
      rotation.add(1, rotation.remove(count - 1));
    }
    return List.copyOf(fixtures);
  }

  public List<Match> nextRound(String cid, List<Match> matches) { return List.of(); }
  public boolean matchesComplete(List<Match> matches) {
    return !matches.isEmpty() && matches.stream().allMatch(Match::decided);
  }

  public List<LeagueStanding> table(List<String> teams, List<Match> matches, Function<String, String> name) {
    // played, wins, draws, losses, scored, conceded, points
    Map<String, int[]> stats = new LinkedHashMap<>();
    teams.forEach(t -> stats.put(t, new int[7]));
    for (Match match : matches) {
      if (!match.decided() || match.bye()) continue;
      int[] left = stats.get(match.leftTeamId()), right = stats.get(match.rightTeamId());
      int a = match.leftScore(), b = match.rightScore();
      left[0]++; right[0]++;
      left[4] += a; left[5] += b; right[4] += b; right[5] += a;
      if (a == b) { left[2]++; right[2]++; left[6]++; right[6]++; }
      else {
        int[] winner = a > b ? left : right, loser = a > b ? right : left;
        winner[1]++; winner[6] += 3; loser[3]++;
      }
    }
    Comparator<String> order = Comparator.<String>comparingInt(t -> stats.get(t)[6]).reversed()
        .thenComparing(Comparator.<String>comparingInt(t -> stats.get(t)[1]).reversed())
        .thenComparing(Comparator.<String>comparingInt(t -> stats.get(t)[4] - stats.get(t)[5]).reversed())
        .thenComparing(Comparator.<String>comparingInt(t -> stats.get(t)[4]).reversed())
        .thenComparing(name, String.CASE_INSENSITIVE_ORDER).thenComparing(Function.identity());
    List<String> ordered = teams.stream().sorted(order).toList();
    List<LeagueStanding> table = new ArrayList<>();
    for (int i = 0; i < ordered.size(); i++) {
      String team = ordered.get(i); int[] s = stats.get(team);
      table.add(new LeagueStanding(i + 1, team, name.apply(team), s[0], s[1], s[2], s[3], s[4], s[5], s[6]));
    }
    return List.copyOf(table);
  }

  public List<Standing> standings(List<String> teams, List<Match> matches, Function<String, String> name) {
    boolean complete = matchesComplete(matches);
    return table(teams, matches, name).stream().map(s -> new Standing(s.rank(), s.teamId(), s.team(), s.points(),
        (complete && s.rank() == 1 ? "Champion · " : "") + "P " + s.played() + " W " + s.won()
        + " D " + s.drawn() + " L " + s.lost() + " Diff " + s.difference())).toList();
  }
}
