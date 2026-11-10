package edu.aau.arena.rules;

import static edu.aau.ccms.Model.*;

import java.util.*;

final class ProgrammingRules implements CompetitionRules {
  public String explanation() {
    return "Programming contest · Most solved problems wins; ties use lowest penalty (elapsed"
        + " minutes + 20 per wrong attempt). One assigned judge records each official"
        + " result. No code is executed.";
  }

  public void validate(int a, int b, int c) {
    CompetitionRules.range(a, 10, "Problems solved");
    CompetitionRules.range(b, 300, "Elapsed minutes");
    CompetitionRules.range(c, 100, "Wrong attempts");
  }

  public double score(Evaluation e) {
    return e.a();
  }

  public String detail(List<Evaluation> values) {
    Evaluation e = values.get(0);
    return (e.b() + 20 * e.c()) + " penalty min";
  }
}
