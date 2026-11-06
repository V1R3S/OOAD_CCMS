package edu.aau.ccms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * JUnit entry points preserve the ordered lifecycle scenarios and fail Maven on any failed check.
 */
class AcceptanceMavenTest {
  @Test
  void serviceWorkflows() throws Exception {
    CampusServiceTest.main(new String[0]);
  }

  @Test
  @EnabledIfSystemProperty(named = "ccms.uiTests", matches = "true")
  void swingInteractions() throws Exception {
    CampusUiSmokeTest.main(new String[0]);
  }
}
