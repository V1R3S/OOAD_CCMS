package edu.aau.arena.rules;

import static edu.aau.ccms.Model.*;

import java.util.*;

/** Scoring strategy for the activity. TournamentRules owns scheduling and match progression. */
public interface CompetitionRules {
  String explanation();

  void validate(int a, int b, int c);

  double score(Evaluation evaluation);

  String detail(List<Evaluation> evaluations);

  default boolean requiresSubmission() {
    return true;
  }

  default boolean requiresAllJudges() { return false; }

  static CompetitionRules forCompetition(Competition competition) {
    return switch (competition.type()) {
      case PROJECT -> new ProjectRules();
      case PROGRAMMING -> new ProgrammingRules();
      case QUIZ -> new QuizRules(competition.quizMaxPoints());
      case MATCH -> new KnockoutRules();
    };
  }

  /** Legacy adapter: all new application code selects by Competition.type(). */
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
