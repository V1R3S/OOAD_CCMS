package edu.aau.arena;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * JUnit entry points preserve the ordered lifecycle scenarios and fail Maven on any failed check.
 */
class AcceptanceMavenTest {
  @Test
  void serviceWorkflows() throws Exception {
    ArenaServiceTest.main(new String[0]);
  }

  @Test
  void arenaTournamentWorkflows() throws Exception {
    ArenaTournamentTest.main(new String[0]);
  }

  @Test
  @EnabledIfSystemProperty(named = "arena.uiTests", matches = "true")
  void swingInteractions() throws Exception {
    ArenaUiSmokeTest.main(new String[0]);
  }
}
