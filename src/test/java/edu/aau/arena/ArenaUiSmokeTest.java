package edu.aau.arena;

import edu.aau.ccms.Model;

import edu.aau.arena.repository.Repository;
import edu.aau.arena.service.ArenaService;
import edu.aau.arena.ui.ArenaApp;
import edu.aau.arena.ui.ArenaTheme;
import java.awt.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.swing.*;

/** Exercises real Swing controls on the event thread, using an isolated repository. */
public final class ArenaUiSmokeTest {
  private static ArenaApp app;
  private static ArenaService service;
  private static int passed, failed;
  private static final List<String> results = new ArrayList<>();

  private static void check(boolean condition, String message) {
    if (!condition) throw new AssertionError(message);
  }

  private static List<Component> children(Container parent) {
    List<Component> result = new ArrayList<>();
    for (Component child : parent.getComponents()) {
      result.add(child);
      if (child instanceof Container container) result.addAll(children(container));
    }
    return result;
  }

  private static <T extends Component> T find(
      Container parent, Class<T> type, Predicate<T> predicate) {
    return children(parent).stream()
        .filter(type::isInstance)
        .map(type::cast)
        .filter(predicate)
        .findFirst()
        .orElseThrow(() -> new AssertionError("Control not found: " + type.getSimpleName()));
  }

  private static JButton button(String text) {
    return find(app, JButton.class, b -> b.getText().equals(text));
  }

  private static boolean hasButton(String text) {
    return children(app).stream().anyMatch(c -> c instanceof JButton b && b.getText().equals(text));
  }

  private static void login(String account) {
    find(app, JTextField.class, f -> "login-email".equals(f.getName()))
        .setText(account + "@campus.test");
    find(app, JPasswordField.class, f -> "login-password".equals(f.getName()))
        .setText("Campus@2026");
    button("Sign in  →").doClick();
    app.validate();
  }

  private static long cards() {
    return children(app).stream()
        .filter(
            c ->
                c instanceof JButton
                    && c.getName() != null
                    && c.getName().startsWith("open-competition-"))
        .count();
  }

  private static void openCompetition(String id) {
    find(app, JButton.class, b -> ("open-competition-" + id).equals(b.getName())).doClick();
  }

  private static void modal(Runnable open, Consumer<JDialog> interaction) {
    AtomicReference<Throwable> error = new AtomicReference<>();
    Timer timer = new Timer(80, null);
    Timer timeout =
        new Timer(
            8000,
            event -> {
              error.compareAndSet(
                  null, new AssertionError("Dialog did not complete within eight seconds"));
              for (Window window : Window.getWindows())
                if (window instanceof JDialog) window.dispose();
            });
    timeout.setRepeats(false);
    timer.addActionListener(
        event -> {
          for (Window window : Window.getWindows()) {
            if (window instanceof JDialog dialog && dialog.isVisible()) {
              timer.stop();
              try {
                interaction.accept(dialog);
              } catch (Throwable failure) {
                error.set(failure);
                dialog.dispose();
              }
              break;
            }
          }
        });
    timer.start();
    timeout.start();
    try {
      open.run();
    } finally {
      timer.stop();
      timeout.stop();
    }
    if (error.get() != null) throw new AssertionError("Dialog interaction failed", error.get());
  }

  private static void test(String id, String description, Runnable action) throws Exception {
    AtomicReference<Throwable> failure = new AtomicReference<>();
    SwingUtilities.invokeAndWait(
        () -> {
          try {
            action.run();
          } catch (Throwable error) {
            failure.set(error);
          }
        });
    if (failure.get() == null) {
      passed++;
      results.add(id + ",PASS,\"" + description + "\"");
      System.out.println("PASS " + id + " " + description);
    } else {
      failed++;
      results.add(id + ",FAIL,\"" + description + "\"");
      System.err.println("FAIL " + id + " " + description);
      failure.get().printStackTrace();
    }
  }

  public static void main(String[] args) throws Exception {
    passed = 0;
    failed = 0;
    results.clear();
    Files.createDirectories(Path.of("build"));
    if (GraphicsEnvironment.isHeadless())
      throw new IllegalStateException("UI tests need a graphical Windows/macOS/Linux session.");
    Path root = Files.createTempDirectory(Path.of("build"), "ui-smoke-");
    try (Repository repository = new Repository(root.resolve("campus.bin"))) {
      service = new ArenaService(repository, true);
      SwingUtilities.invokeAndWait(
          () -> {
            app = new ArenaApp(service);
            app.setVisible(true);
          });
      try {
        test(
            "UI-01",
            "Sign-in controls and packaged university artwork are available",
            () -> {
              check(
                  ArenaTheme.class.getResource("/aau.png") != null,
                  "Logo missing from application classpath");
              check(
                  find(app, JPasswordField.class, f -> "login-password".equals(f.getName()))
                          .getPassword()
                          .length
                      == 0,
                  "Password not empty");
            });
        test(
            "UI-02",
            "Organizer signs in through the real form and sees permitted navigation",
            () -> {
              login("organizer1");
              check(
                  hasButton("Activity") && !hasButton("People"), "Incorrect organizer navigation");
              check(cards() == 3, "Dashboard should contain three competition cards");
            });
        test(
            "UI-03",
            "Search handles no results and restores cards when cleared",
            () -> {
              button("Competitions").doClick();
              JTextField search =
                  find(app, JTextField.class, f -> "competition-search".equals(f.getName()));
              search.setText("does-not-exist");
              check(cards() == 0, "Search did not filter cards");
              search.setText("");
              check(cards() == 5, "Clearing search did not restore all five seeded competitions");
            });
        test(
            "UI-04",
            "Format filter and competition card navigate to the correct event",
            () -> {
              find(app, JComboBox.class, c -> "competition-format-filter".equals(c.getName()))
                  .setSelectedIndex(2);
              check(cards() == 1, "Knockout filter should show one card");
              openCompetition("robotics");
              check(
                  find(app, JLabel.class, l -> "page-heading".equals(l.getName()))
                      .getText()
                      .equals("Robotics Challenge"),
                  "Wrong competition opened");
            });
        test(
            "UI-05",
            "Bracket tab displays the seeded round",
            () -> {
              JTabbedPane tabs = find(app, JTabbedPane.class, t -> true);
              tabs.setSelectedIndex(tabs.indexOfTab("Bracket"));
              check(
                  children(app).stream()
                      .anyMatch(c -> c instanceof JLabel l && l.getText().equals("SEMIFINALS")),
                  "Round missing");
            });
        test(
            "UI-06",
            "Results page shows published standings",
            () -> {
              button("Results").doClick();
              check(
                  find(app, JTable.class, t -> true).getRowCount() == 3,
                  "Published standings missing");
            });
        test(
            "UI-07",
            "Sign-out returns to an empty sign-in form",
            () -> {
              button("Sign out").doClick();
              check(
                  find(app, JTextField.class, f -> "login-email".equals(f.getName()))
                      .getText()
                      .isEmpty(),
                  "Email retained after sign-out");
              check(
                  find(app, JPasswordField.class, f -> true).getPassword().length == 0,
                  "Password retained after sign-out");
            });
        test(
            "UI-08",
            "Student navigation hides organizer and judge functions",
            () -> {
              login("student1");
              check(
                  !hasButton("Activity") && !hasButton("People") && !hasButton("Judging studio"),
                  "Unauthorized navigation visible");
              button("Competitions").doClick();
              check(!hasButton("+  Create competition"), "Student sees competition creation");
            });
        test(
            "UI-09",
            "Create-team dialog saves through the service and refreshes the roster",
            () -> {
              button("My teams").doClick();
              int before = service.teams().size();
              modal(
                  () -> button("+  Create a team").doClick(),
                  dialog -> {
                    find(dialog, JTextField.class, f -> true).setText("UI Smoke Team");
                    find(
                            dialog,
                            JButton.class,
                            b -> b.getText().equals(UIManager.getString("OptionPane.okButtonText")))
                        .doClick();
                  });
              check(service.teams().size() == before + 1, "Team was not created");
              check(
                  children(app).stream()
                      .anyMatch(c -> c instanceof JLabel l && l.getText().equals("UI Smoke Team")),
                  "New team not rendered");
            });
        test(
            "UI-10",
            "Judging studio lists only the current judge's assignments",
            () -> {
              button("Sign out").doClick();
              login("judge3");
              button("Judging studio").doClick();
              check(cards() == 2, "Judge 3 should see robotics and the league only");
              openCompetition("robotics");
              JTabbedPane tabs = find(app, JTabbedPane.class, t -> true);
              tabs.setSelectedIndex(tabs.indexOfTab("Bracket"));
              check(hasButton("Record winner"), "Active bracket judging action missing");
            });
        test(
            "UI-11",
            "Match-winner dialog persists the selected team",
            () -> {
              modal(
                  () -> button("Record winner").doClick(),
                  dialog ->
                      find(
                              dialog,
                              JButton.class,
                              b ->
                                  b.getText()
                                      .equals(UIManager.getString("OptionPane.okButtonText")))
                          .doClick());
              check(
                  service.matches("robotics").stream().anyMatch(m -> m.winnerId() != null),
                  "Winner was not recorded");
            });
        test(
            "UI-12",
            "Administrator opens the account directory",
            () -> {
              button("Sign out").doClick();
              login("admin");
              button("People").doClick();
              check(
                  find(app, JTable.class, t -> true).getRowCount() == 16,
                  "Account directory incomplete");
            });
        test(
            "UI-13",
            "Canceling account creation leaves stored users unchanged",
            () -> {
              int before = service.users().size();
              modal(
                  () -> button("+  Add account").doClick(),
                  dialog ->
                      find(dialog, JButton.class, b -> b.getText().equals("Cancel")).doClick());
              check(service.users().size() == before, "Cancel changed users");
            });
        test(
            "UI-14",
            "Competition cards remain within the viewport at minimum window size",
            () -> {
              app.setSize(1280, 800);
              button("Competitions").doClick();
              app.validate();
              JScrollPane scroll =
                  find(app, JScrollPane.class, s -> s.getViewport().getView() instanceof JPanel);
              check(
                  scroll.getViewport().getView().getWidth() <= scroll.getViewport().getWidth(),
                  "Horizontal layout overflow");
              check(cards() == 5, "Cards missing at minimum size");
            });
        test(
            "UI-15",
            "Project scoring dialog calculates a live total and saves the review",
            () -> {
              // Prepare a ready-to-judge fixture through the service, then exercise the UI.
              service.login("organizer1@campus.test", "Campus@2026");
              service.advance("hackathon");
              String[] accounts = {"student1", "student3", "student5"};
              String[] teams = {"alpha", "warriors", "innovators"};
              for (int i = 0; i < accounts.length; i++) {
                service.login(accounts[i] + "@campus.test", "Campus@2026");
                service.submit(
                    "hackathon",
                    teams[i],
                    "UI review project",
                    "Project prepared for the scoring dialog test.",
                    "https://example.com/project");
              }
              service.login("organizer1@campus.test", "Campus@2026");
              service.advance("hackathon");
              button("Sign out").doClick();
              login("judge1");
              button("Judging studio").doClick();
              openCompetition("hackathon");
              modal(
                  () -> button("Evaluate a submission").doClick(),
                  dialog -> {
                    find(
                            dialog,
                            JSpinner.class,
                            s ->
                                s.getAccessibleContext()
                                    .getAccessibleName()
                                    .startsWith("Innovation"))
                        .setValue(90);
                    find(
                            dialog,
                            JSpinner.class,
                            s ->
                                s.getAccessibleContext()
                                    .getAccessibleName()
                                    .startsWith("Execution"))
                        .setValue(80);
                    find(
                            dialog,
                            JSpinner.class,
                            s ->
                                s.getAccessibleContext()
                                    .getAccessibleName()
                                    .startsWith("Presentation"))
                        .setValue(70);
                    check(
                        find(dialog, JLabel.class, l -> "evaluation-total".equals(l.getName()))
                            .getText()
                            .equals("81.50 / 100"),
                        "Incorrect live weighted total");
                    find(dialog, JTextArea.class, a -> a.isEditable())
                        .setText(
                            "Clear concept, strong execution, and a well-structured presentation.");
                    find(dialog, JButton.class, b -> b.getText().equals("Save")).doClick();
                  });
              check(service.evaluations("hackathon").size() == 1, "Review was not saved");
            });
        test(
            "UI-16",
            "User-supplied markup remains literal in names and table cells",
            () -> {
              String literal = "<html><b>Literal name</b></html>";
              button("Sign out").doClick();
              login("admin");
              service.createUser(literal, "literal@campus.test", Model.Role.STUDENT, "Campus@2026");
              button("People").doClick();
              JTable table = find(app, JTable.class, t -> true);
              int row = 0;
              while (row < table.getRowCount() && !literal.equals(table.getValueAt(row, 0))) row++;
              check(row < table.getRowCount(), "Literal account missing from directory");
              JLabel cell = (JLabel) table.prepareRenderer(table.getCellRenderer(row, 0), row, 0);
              check(literal.equals(cell.getText()), "Account name was changed");
              check(cell.getClientProperty("html") == null, "Table interpreted user HTML");
              check(
                  cell.getToolTipText().contains("&lt;html&gt;"),
                  "Tooltip failed to escape markup");
              button("Sign out").doClick();
              login("student1");
              service.createTeam(literal);
              button("My teams").doClick();
              JLabel name = find(app, JLabel.class, l -> literal.equals(l.getText()));
              check(name.getClientProperty("html") == null, "Team label interpreted user HTML");
            });
        test("UI-17", "Arena branding appears in window login and sidebar", () -> {
          check(app.getTitle().startsWith("Arena"), "Old window title");
          button("Sign out").doClick();
          check(children(app).stream().anyMatch(c -> c instanceof JLabel l && l.getText().equals("WELCOME TO ARENA")), "Login branding missing");
          login("organizer1");
          check(children(app).stream().anyMatch(c -> c instanceof JLabel l && l.getText().equals("Arena")), "Sidebar branding missing");
        });
        test("UI-18", "Creation form filters compatible formats and reveals quiz settings", () -> {
          button("Competitions").doClick();
          int before = service.competitions().size();
          modal(() -> button("+  Create competition").doClick(), dialog -> {
            JComboBox types = find(dialog, JComboBox.class, c -> "competition-type".equals(c.getName()));
            JComboBox formats = find(dialog, JComboBox.class, c -> "tournament-format".equals(c.getName()));
            JSpinner quiz = find(dialog, JSpinner.class, s -> "quiz-maximum".equals(s.getName()));
            check(formats.getItemCount() == 1 && !quiz.isVisible(), "Project exposed irrelevant controls");
            types.setSelectedItem(Model.CompetitionType.QUIZ);
            check(formats.getItemCount() == 3 && quiz.isVisible(), "Quiz controls missing");
            formats.setSelectedItem(Model.TournamentFormat.ROUND_ROBIN);
            String explanation = find(dialog, JTextArea.class, a -> "competition-rules-explanation".equals(a.getName())).getText();
            check(explanation.contains("Win 3") && explanation.contains("points"), "League explanation missing");
            types.setSelectedItem(Model.CompetitionType.MATCH);
            check(formats.getItemCount() == 2 && !quiz.isVisible(), "General-match controls incorrect");
            find(dialog, JButton.class, b -> b.getText().equals("Cancel")).doClick();
          });
          check(service.competitions().size() == before, "Cancelled draft saved");
        });
        test("UI-19", "Creation form saves a quiz league with selected settings", () -> {
          modal(() -> button("+  Create competition").doClick(), dialog -> {
            find(dialog, JTextField.class, f -> "competition-title".equals(f.getName())).setText("UI Quiz League");
            find(dialog, JTextArea.class, a -> a.isEditable()).setText("A league created through the real Swing form.");
            find(dialog, JComboBox.class, c -> "competition-type".equals(c.getName())).setSelectedItem(Model.CompetitionType.QUIZ);
            find(dialog, JComboBox.class, c -> "tournament-format".equals(c.getName())).setSelectedItem(Model.TournamentFormat.ROUND_ROBIN);
            find(dialog, JSpinner.class, s -> "quiz-maximum".equals(s.getName())).setValue(30);
            find(dialog, JList.class, l -> true).setSelectedIndex(0);
            find(dialog, JButton.class, b -> b.getText().equals("Save")).doClick();
          });
          Model.Competition c = service.competitions().stream().filter(x -> x.title().equals("UI Quiz League")).findFirst().orElseThrow();
          check(c.type() == Model.CompetitionType.QUIZ && c.tournamentFormat() == Model.TournamentFormat.ROUND_ROBIN && c.quizMaxPoints() == 30, "Incorrect saved settings");
        });
        test("UI-20", "Quiz scoring presents one points control and saves without submissions", () -> {
          button("Sign out").doClick(); login("judge2"); button("Judging studio").doClick(); openCompetition("quiz");
          JTabbedPane tabs = find(app, JTabbedPane.class, t -> true);
          check(tabs.indexOfTab("Submissions") == -1, "Quiz exposed submissions");
          modal(() -> button("Record quiz points").doClick(), dialog -> {
            check(children(dialog).stream().filter(JSpinner.class::isInstance).count() == 1, "Irrelevant scoring fields visible");
            find(dialog, JSpinner.class, s -> true).setValue(18);
            find(dialog, JTextArea.class, a -> a.isEditable()).setText("18 correct answers verified by the judge.");
            check(find(dialog, JLabel.class, l -> "evaluation-total".equals(l.getName())).getText().equals("18 / 20 points"), "Points preview incorrect");
            find(dialog, JButton.class, b -> b.getText().equals("Save")).doClick();
          });
          check(service.evaluations("quiz").get(0).a() == 18, "Quiz result missing");
        });
        test("UI-21", "League fixture dialog saves a draw and refreshes the results", () -> {
          button("Sign out").doClick(); login("judge3"); button("Judging studio").doClick(); openCompetition("league");
          JTabbedPane tabs = find(app, JTabbedPane.class, t -> true); tabs.setSelectedIndex(tabs.indexOfTab("Fixtures"));
          JTable fixtures = find((Container) tabs.getSelectedComponent(), JTable.class, t -> true);
          check(fixtures.getRowCount() == 3, "Missing odd-team fixtures"); fixtures.setRowSelectionInterval(0, 0);
          modal(() -> button("Record selected match score").doClick(), dialog -> {
            find(dialog, JSpinner.class, s -> "match-left-score".equals(s.getName())).setValue(2);
            find(dialog, JSpinner.class, s -> "match-right-score".equals(s.getName())).setValue(2);
            find(dialog, JButton.class, b -> b.getText().equals("Save")).doClick();
          });
          check(service.matches("league").stream().anyMatch(Model.Match::drawn), "Draw not saved");
          JTabbedPane refreshed = find(app, JTabbedPane.class, t -> true);
          check(refreshed.getTitleAt(refreshed.getSelectedIndex()).equals("Fixtures"), "Saving a result lost the fixture tab");
          button("Sign out").doClick(); login("organizer1"); button("Competitions").doClick(); openCompetition("league");
          tabs = find(app, JTabbedPane.class, t -> true); tabs.setSelectedIndex(tabs.indexOfTab("Results preview"));
          JTable table = find((Container) tabs.getSelectedComponent(), JTable.class, t -> true);
          check(table.getColumnCount() == 10 && table.getRowCount() == 3, "Full league table missing");
        });
        test("UI-22", "Workspace remains within the viewport at 1100 by 720", () -> {
          app.setSize(1100, 720); button("Competitions").doClick(); app.validate();
          JScrollPane scroll = find(app, JScrollPane.class, s -> s.getViewport().getView() instanceof JPanel);
          check(scroll.getViewport().getView().getWidth() <= scroll.getViewport().getWidth(), "Workspace wider than viewport");
          check(cards() == 6, "Seeded competitions or new quiz league missing");
        });
      } finally {
        SwingUtilities.invokeAndWait(
            () -> {
              for (Window window : Window.getWindows()) window.dispose();
            });
      }
    }
    Files.writeString(
        Path.of("build/ui-test-results.csv"),
        "Test ID,Status,Description\n" + String.join("\n", results) + "\n");
    String summary =
        "Executed "
            + (passed + failed)
            + " Swing UI checks: "
            + passed
            + " passed, "
            + failed
            + " failed.\nExecuted at: "
            + Instant.now()
            + "\n";
    Files.writeString(Path.of("build/ui-test-summary.txt"), summary);
    System.out.print(summary);
    if (failed > 0) throw new AssertionError(failed + " UI checks failed");
  }
}
