package edu.aau.ccms.rules;

import static edu.aau.ccms.Model.*;

import java.util.*;

/** Strategy: each format owns validation, scoring, and its ranking policy. */
public interface CompetitionRules {
  String explanation();

  void validate(int a, int b, int c);

  double score(Evaluation evaluation);

  String detail(List<Evaluation> evaluations);

  default boolean requiresSubmission() {
    return true;
  }

  static CompetitionRules forFormat(Format format) {
    return switch (format) {
      case PROJECT -> new ProjectRules();
      case PROGRAMMING -> new ProgrammingRules();
      case KNOCKOUT -> new KnockoutRules();
    };
  }

  static void range(int value, int max, String name) {
    if (value < 0 || value > max)
      throw new IllegalArgumentException(name + " must be between 0 and " + max + ".");
  }
}
