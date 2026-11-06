package edu.aau.ccms.rules;

import static edu.aau.ccms.Model.*;

import java.util.*;

final class KnockoutRules implements CompetitionRules {
  public String explanation() {
    return "Knockout bracket · 2, 4, 8, or 16 teams. Registration order determines seeding; each"
        + " match needs a winner. Winners advance until one champion remains.";
  }

  public boolean requiresSubmission() {
    return false;
  }

  public void validate(int a, int b, int c) {
    throw new IllegalArgumentException("Record bracket winners instead of rubric scores.");
  }

  public double score(Evaluation e) {
    throw new UnsupportedOperationException();
  }

  public String detail(List<Evaluation> values) {
    return "Match wins";
  }
}
