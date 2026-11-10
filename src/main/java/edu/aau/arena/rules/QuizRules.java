package edu.aau.arena.rules;

import static edu.aau.ccms.Model.*;
import java.util.List;

final class QuizRules implements CompetitionRules {
  private final int maximum;

  QuizRules(int maximum) { this.maximum = maximum; }

  public String explanation() {
    return "Quiz / academic competition · Award 0–" + maximum
        + " points (one point per correct answer, or an announced points rubric). "
        + "Ranking events use one official result per team; equal scores share a rank. "
        + "Match events use the selected tournament rules. Questions are administered outside Arena.";
  }

  public boolean requiresSubmission() { return false; }

  public void validate(int a, int b, int c) {
    CompetitionRules.range(a, maximum, "Quiz points");
    if (b != 0 || c != 0) throw new IllegalArgumentException("Quiz scoring uses only awarded points.");
  }

  public double score(Evaluation evaluation) { return evaluation.a(); }
  public String detail(List<Evaluation> evaluations) { return "Points / " + maximum; }
}
