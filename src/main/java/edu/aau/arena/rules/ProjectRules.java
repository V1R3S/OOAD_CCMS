package edu.aau.arena.rules;

import static edu.aau.ccms.Model.*;

import java.util.*;

final class ProjectRules implements CompetitionRules {
  public boolean requiresAllJudges() { return true; }
  public String explanation() {
    return "Project showcase · Innovation 40% + execution 35% + presentation 25%. All assigned"
        + " judges score every team. Equal averages share a rank.";
  }

  public void validate(int a, int b, int c) {
    CompetitionRules.range(a, 100, "Innovation");
    CompetitionRules.range(b, 100, "Execution");
    CompetitionRules.range(c, 100, "Presentation");
  }

  public double score(Evaluation e) {
    return e.a() * .40 + e.b() * .35 + e.c() * .25;
  }

  public String detail(List<Evaluation> values) {
    return values.size() + " judge review(s) · /100";
  }
}
