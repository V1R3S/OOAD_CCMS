package edu.aau.arena.rules;

import static edu.aau.ccms.Model.*;

import java.util.*;

/** Legacy-named match-only scoring policy. Progression is in KnockoutTournamentRules. */
final class KnockoutRules implements CompetitionRules {
  public String explanation() {
    return "General match competition. Assigned judges confirm match outcomes; no project submission or rubric is required.";
  }

  public boolean requiresSubmission() {
    return false;
  }

  public void validate(int a, int b, int c) {
    throw new IllegalArgumentException("Record match results instead of rubric scores.");
  }

  public double score(Evaluation e) {
    throw new UnsupportedOperationException();
  }

  public String detail(List<Evaluation> values) {
    return "Match wins";
  }
}
