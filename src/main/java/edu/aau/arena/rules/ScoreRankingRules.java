package edu.aau.arena.rules;

import static edu.aau.ccms.Model.*;
import java.util.List;

final class ScoreRankingRules implements TournamentRules {
  public String explanation() {
    return "Score-based ranking. Complete the required submissions and official reviews before publication. Equal results share a rank.";
  }
  public List<Match> start(String cid, List<String> teams) { return List.of(); }
  public List<Match> nextRound(String cid, List<Match> matches) { return List.of(); }
  public boolean matchesComplete(List<Match> matches) { return true; }
}
