package edu.aau.arena.rules;

import static edu.aau.ccms.Model.*;
import java.util.*;
import java.util.function.Function;

/** Tournament strategy: fixture generation, advancement, completion, and match standings. */
public interface TournamentRules {
  String explanation();
  List<Match> start(String competitionId, List<String> teamIds);
  List<Match> nextRound(String competitionId, List<Match> matches);
  boolean matchesComplete(List<Match> matches);

  default List<Standing> standings(List<String> teams, List<Match> matches,
      Function<String, String> teamName) {
    throw new IllegalArgumentException("Score-based standings use competition scoring rules.");
  }

  static TournamentRules forFormat(TournamentFormat format) {
    return switch (format) {
      case SCORE_BASED -> new ScoreRankingRules();
      case KNOCKOUT -> new KnockoutTournamentRules();
      case ROUND_ROBIN -> new RoundRobinRules();
    };
  }

  static void validateEntrants(List<String> teams) {
    if (teams.size() < 2 || teams.size() > 16 || teams.stream().anyMatch(Objects::isNull)
        || new HashSet<>(teams).size() != teams.size())
      throw new IllegalArgumentException("Match tournaments need 2–16 distinct teams.");
  }

  static Match match(String cid, int round, String left, String right) {
    return new Match(UUID.randomUUID().toString(), cid, round, left, right, right == null ? left : null);
  }
}
