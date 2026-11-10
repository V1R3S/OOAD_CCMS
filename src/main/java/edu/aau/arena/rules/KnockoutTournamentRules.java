package edu.aau.arena.rules;

import static edu.aau.ccms.Model.*;
import java.util.*;
import java.util.function.Function;

final class KnockoutTournamentRules implements TournamentRules {
  public String explanation() {
    return "Single elimination · 2–16 teams. Registration order sets the seeds; early seeds receive first-round byes when needed. "
        + "Each played match needs one winner. Completed rounds generate the next round automatically. No draws or result revisions.";
  }

  public List<Match> start(String cid, List<String> teams) {
    TournamentRules.validateEntrants(teams);
    int bracketSize = Integer.highestOneBit(teams.size() - 1) << 1;
    int byes = bracketSize - teams.size();
    List<Match> result = new ArrayList<>();
    for (int i = 0; i < byes; i++) result.add(TournamentRules.match(cid, 1, teams.get(i), null));
    for (int i = byes; i < teams.size(); i += 2)
      result.add(TournamentRules.match(cid, 1, teams.get(i), teams.get(i + 1)));
    return List.copyOf(result);
  }

  public List<Match> nextRound(String cid, List<Match> matches) {
    int round = matches.stream().mapToInt(Match::round).max().orElse(0);
    List<Match> current = matches.stream().filter(m -> m.round() == round).toList();
    if (current.size() <= 1 || current.stream().anyMatch(m -> !m.decided())) return List.of();
    List<Match> result = new ArrayList<>();
    for (int i = 0; i < current.size(); i += 2)
      result.add(TournamentRules.match(cid, round + 1, current.get(i).winnerId(), current.get(i + 1).winnerId()));
    return List.copyOf(result);
  }

  public boolean matchesComplete(List<Match> matches) {
    if (matches.isEmpty() || matches.stream().anyMatch(m -> !m.decided())) return false;
    int lastRound = matches.stream().mapToInt(Match::round).max().orElse(0);
    return matches.stream().filter(m -> m.round() == lastRound).count() == 1;
  }

  public List<Standing> standings(List<String> teams, List<Match> matches, Function<String, String> name) {
    int finalRound = matches.stream().mapToInt(Match::round).max().orElse(0);
    String champion = matchesComplete(matches)
        ? matches.stream().filter(m -> m.round() == finalRound).findFirst().orElseThrow().winnerId() : null;
    Map<String, Integer> progress = new HashMap<>();
    Map<String, Long> wins = new HashMap<>();
    for (String team : teams) {
      int reached = matches.stream().filter(m -> team.equals(m.leftTeamId()) || team.equals(m.rightTeamId()))
          .mapToInt(Match::round).max().orElse(0);
      progress.put(team, team.equals(champion) ? finalRound + 1 : reached);
      wins.put(team, matches.stream().filter(m -> !m.bye() && team.equals(m.winnerId())).count());
    }
    List<String> ordered = teams.stream().sorted(Comparator.<String>comparingInt(progress::get).reversed()
        .thenComparing(name).thenComparing(Function.identity())).toList();
    List<Standing> result = new ArrayList<>();
    int rank = 0, previous = -1;
    for (int i = 0; i < ordered.size(); i++) {
      String team = ordered.get(i);
      if (progress.get(team) != previous) rank = i + 1;
      result.add(new Standing(rank, team, name.apply(team), wins.get(team),
          team.equals(champion) ? "Champion" : "Round " + progress.get(team) + " · played-match wins"));
      previous = progress.get(team);
    }
    return List.copyOf(result);
  }
}
