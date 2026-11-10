package edu.aau.arena.ui;

import static edu.aau.ccms.Model.*;
import static edu.aau.arena.ui.ArenaTheme.*;
import static edu.aau.arena.ui.UiComponents.*;

import edu.aau.arena.model.UserSummary;
import edu.aau.arena.service.ArenaService;
import edu.aau.arena.rules.CompetitionRules;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

final class AppDialogs {
  private final ArenaApp app;
  private final ArenaService service;

  AppDialogs(ArenaApp app, ArenaService service) {
    this.app = app;
    this.service = service;
  }

  private JPanel form() {
    JPanel p = panel(new GridBagLayout(), PAPER);
    p.setBorder(new EmptyBorder(10, 10, 10, 10));
    return p;
  }

  private JLabel row(JPanel p, int index, String name, JComponent control) {
    GridBagConstraints g = new GridBagConstraints();
    g.gridx = 0;
    g.gridy = index;
    g.anchor = GridBagConstraints.NORTHWEST;
    g.insets = new Insets(7, 0, 7, 18);
    JLabel fieldLabel = label(name, 13, INK, true);
    fieldLabel.setLabelFor(control);
    control.getAccessibleContext().setAccessibleName(name);
    p.add(fieldLabel, g);
    g.gridx = 1;
    g.weightx = 1;
    g.fill = GridBagConstraints.HORIZONTAL;
    g.insets = new Insets(4, 0, 4, 0);
    control.setPreferredSize(new Dimension(390, control instanceof JScrollPane ? 105 : 40));
    p.add(control, g);
    return fieldLabel;
  }

  private void dialog(String heading, JPanel form, Runnable save) {
    JDialog d = new JDialog(app, heading, true);
    d.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
    d.getRootPane()
        .registerKeyboardAction(
            e -> d.dispose(),
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    d.setLayout(new BorderLayout());
    JScrollPane scroll = new JScrollPane(form);
    scroll.setBorder(null);
    scroll.getVerticalScrollBar().setUnitIncrement(20);
    d.add(scroll);
    JPanel footer = panel(new FlowLayout(FlowLayout.RIGHT, 12, 12), PAPER);
    footer.add(app.button("Cancel", d::dispose, false));
    footer.add(
        app.button(
            "Save",
            () -> {
              save.run();
              d.dispose();
            },
            true));
    d.add(footer, BorderLayout.SOUTH);
    d.pack();
    Rectangle screen = app.getGraphicsConfiguration().getBounds();
    d.setSize(Math.min(d.getWidth() + 28, screen.width - 80), Math.min(d.getHeight(), screen.height - 100));
    d.setLocationRelativeTo(app);
    d.setVisible(true);
  }

  private JTextArea inputArea(String text) {
    JTextArea a = new JTextArea(text, 3, 30);
    a.setLineWrap(true);
    a.setWrapStyleWord(true);
    a.setBorder(new EmptyBorder(7, 9, 7, 9));
    return a;
  }

  void competitionDialog(Competition c) {
    JPanel p = form();
    JTextField name = field(c == null ? "" : c.title());
    JTextArea description = inputArea(c == null ? "" : c.description());
    name.setName("competition-title");
    JComboBox<CompetitionType> type = new JComboBox<>(CompetitionType.values());
    type.setName("competition-type");
    type.setSelectedItem(c == null ? CompetitionType.PROJECT : c.type());
    JComboBox<TournamentFormat> format = new JComboBox<>();
    format.setName("tournament-format");
    JSpinner cap = new JSpinner(new SpinnerNumberModel(c == null ? 8 : c.capacity(), 2, 64, 1)),
        size = new JSpinner(new SpinnerNumberModel(c == null ? 4 : c.teamSize(), 1, 8, 1));
    cap.setName("competition-capacity");
    JSpinner quizMaximum = new JSpinner(new SpinnerNumberModel(c == null || c.quizMaxPoints() == 0 ? 100 : c.quizMaxPoints(), 1, 10000, 1));
    quizMaximum.setName("quiz-maximum");
    JTextField deadline =
        field(
            (c == null ? LocalDateTime.now().plusDays(30).withSecond(0).withNano(0) : c.deadline())
                .toString());
    List<UserSummary> judges = service.judges();
    JList<UserSummary> judgeList = new JList<>(judges.toArray(UserSummary[]::new));
    judgeList.setCellRenderer(plainRenderer());
    judgeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    if (c != null)
      judgeList.setSelectedIndices(
          java.util.stream.IntStream.range(0, judges.size())
              .filter(i -> c.judgeIds().contains(judges.get(i).id()))
              .toArray());
    row(p, 0, "Competition title", name);
    row(p, 1, "Description", new JScrollPane(description));
    row(p, 2, "Competition type", type);
    row(p, 3, "Tournament format", format);
    row(p, 4, "Maximum teams", cap);
    row(p, 5, "Maximum members / team", size);
    JLabel quizLabel = row(p, 6, "Maximum quiz points", quizMaximum);
    JLabel deadlineLabel = row(p, 7, "Deadline (YYYY-MM-DDTHH:MM)", deadline);
    row(p, 8, "Judges (Ctrl-click to select)", new JScrollPane(judgeList));
    JTextArea explanation = paragraph("", PAPER);
    explanation.setName("competition-rules-explanation");
    row(p, 9, "Rules and progression", explanation);
    explanation.setPreferredSize(new Dimension(390, 165));
    Runnable describe = () -> {
      CompetitionType chosenType = (CompetitionType) type.getSelectedItem();
      TournamentFormat chosenFormat = (TournamentFormat) format.getSelectedItem();
      if (chosenFormat == null) return;
      boolean quiz = chosenType == CompetitionType.QUIZ;
      quizLabel.setVisible(quiz); quizMaximum.setVisible(quiz);
      SpinnerNumberModel model = (SpinnerNumberModel) cap.getModel();
      int maximum = chosenFormat == TournamentFormat.SCORE_BASED ? 64 : 16;
      model.setMaximum(maximum);
      if ((int) cap.getValue() > maximum) cap.setValue(maximum);
      Competition sample = new Competition("preview", "Preview", "", chosenType, chosenFormat,
          Phase.DRAFT, "", 8, 4, LocalDateTime.now(), Set.of(), (int) quizMaximum.getValue());
      explanation.setText(ArenaService.rulesExplanation(sample));
      deadlineLabel.setText(ArenaService.requiresSubmission(sample)
          ? "Registration / submission closes" : "Registration closes");
      deadline.setToolTipText("Local date and time: YYYY-MM-DDTHH:MM. Registration opens when you advance the draft.");
      p.revalidate(); p.repaint();
    };
    Runnable updateFormats = () -> {
      TournamentFormat previous = (TournamentFormat) format.getSelectedItem();
      format.removeAllItems();
      CompetitionType selectedType = (CompetitionType) type.getSelectedItem();
      for (TournamentFormat candidate : TournamentFormat.values())
        if (selectedType.supports(candidate)) format.addItem(candidate);
      if (previous != null && selectedType.supports(previous)) format.setSelectedItem(previous);
      describe.run();
    };
    type.addActionListener(event -> updateFormats.run());
    format.addActionListener(event -> describe.run());
    quizMaximum.addChangeListener(event -> describe.run());
    updateFormats.run();
    if (c != null) format.setSelectedItem(c.tournamentFormat());
    dialog(
        c == null ? "Create competition" : "Edit draft",
        p,
        () -> {
          Set<String> selected = new HashSet<>();
          judgeList.getSelectedValuesList().forEach(u -> selected.add(u.id()));
          LocalDateTime date;
          try {
            date = LocalDateTime.parse(deadline.getText().strip());
          } catch (Exception e) {
            throw new IllegalArgumentException("Enter a future local deadline using YYYY-MM-DDTHH:MM.");
          }
          if (c == null)
            service.createCompetition(
                name.getText(),
                description.getText(),
                (CompetitionType) type.getSelectedItem(),
                (TournamentFormat) format.getSelectedItem(),
                (int) cap.getValue(),
                (int) size.getValue(),
                date,
                selected,
                (int) quizMaximum.getValue());
          else
            service.editCompetition(
                c.id(),
                name.getText(),
                description.getText(),
                (CompetitionType) type.getSelectedItem(),
                (TournamentFormat) format.getSelectedItem(),
                (int) cap.getValue(),
                (int) size.getValue(),
                date,
                selected,
                (int) quizMaximum.getValue());
          app.showPage("Competitions");
        });
  }

  private List<Team> captainTeams() {
    return service.teams().stream()
        .filter(t -> t.captainId().equals(service.currentUser().id()))
        .toList();
  }

  void registrationDialog(Competition c, boolean withdraw) {
    List<Team> teams = captainTeams();
    if (teams.isEmpty())
      throw new IllegalArgumentException(
          "Create a team in My teams first. Only captains can register.");
    Team selected =
        chooseTeam(
            app, "Select a team you captain.", withdraw ? "Withdraw team" : "Register team", teams);
    if (selected != null) {
      if (withdraw) service.withdraw(c.id(), selected.id());
      else service.register(c.id(), selected.id());
      app.showCompetition(c.id());
    }
  }

  void submissionDialog(Competition c) {
    List<Team> teams =
        captainTeams().stream()
            .filter(
                t ->
                    service.registrations(c.id()).stream().anyMatch(r -> r.teamId().equals(t.id())))
            .toList();
    if (teams.isEmpty())
      throw new IllegalArgumentException("You must captain a registered team to submit work.");
    JComboBox<Team> team = new JComboBox<>(teams.toArray(Team[]::new));
    team.setRenderer(plainRenderer());
    JTextField name = field(""), url = field("https://");
    JTextArea description = inputArea("");
    Runnable populate =
        () -> {
          Team t = (Team) team.getSelectedItem();
          Optional<Submission> old =
              service.submissions(c.id()).stream()
                  .filter(s -> s.teamId().equals(t.id()))
                  .findFirst();
          name.setText(old.map(Submission::title).orElse(""));
          url.setText(old.map(Submission::url).orElse("https://"));
          description.setText(old.map(Submission::description).orElse(""));
        };
    team.addActionListener(e -> populate.run());
    populate.run();
    JPanel p = form();
    row(p, 0, "Registered team", team);
    row(p, 1, "Submission title", name);
    row(p, 2, "Project description", new JScrollPane(description));
    row(p, 3, "Repository / project URL", url);
    dialog(
        "Submit your work",
        p,
        () -> {
          service.submit(
              c.id(),
              ((Team) team.getSelectedItem()).id(),
              name.getText(),
              description.getText(),
              url.getText());
          app.showCompetition(c.id());
        });
  }

  void evaluationDialog(Competition c) {
    List<Team> teams =
        service.registrations(c.id()).stream().map(r -> service.team(r.teamId())).toList();
    if (teams.isEmpty())
      throw new IllegalArgumentException("There are no registered teams to evaluate.");
    JComboBox<Team> team = new JComboBox<>(teams.toArray(Team[]::new));
    team.setRenderer(plainRenderer());
    boolean project = c.type() == CompetitionType.PROJECT;
    boolean quiz = c.type() == CompetitionType.QUIZ;
    JSpinner a = new JSpinner(new SpinnerNumberModel(0, 0, quiz ? c.quizMaxPoints() : project ? 100 : 10, 1)),
        b = new JSpinner(new SpinnerNumberModel(0, 0, project ? 100 : 300, 1)),
        third = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
    JTextArea feedback = inputArea("");
    Runnable populate =
        () -> {
          String tid = ((Team) team.getSelectedItem()).id();
          Optional<Evaluation> old =
              service.evaluations(c.id()).stream()
                  .filter(
                      e -> e.teamId().equals(tid) && e.judgeId().equals(service.currentUser().id()))
                  .findFirst();
          a.setValue(old.map(Evaluation::a).orElse(0));
          b.setValue(old.map(Evaluation::b).orElse(0));
          third.setValue(old.map(Evaluation::c).orElse(0));
          feedback.setText(old.map(Evaluation::feedback).orElse(""));
        };
    team.addActionListener(e -> populate.run());
    populate.run();
    JPanel p = form();
    row(p, 0, "Team", team);
    row(p, 1, quiz ? "Awarded points /" + c.quizMaxPoints() : project ? "Innovation /100 (40%)" : "Problems solved /10", a);
    if (!quiz) {
      row(p, 2, project ? "Execution /100 (35%)" : "Elapsed minutes /300", b);
      row(p, 3, project ? "Presentation /100 (25%)" : "Wrong attempts (+20 min each)", third);
    }
    JLabel total = label("", 20, GREEN, true);
    total.setName("evaluation-total");
    Runnable updateTotal =
        () ->
            total.setText(
                quiz ? a.getValue() + " / " + c.quizMaxPoints() + " points" : project
                    ? String.format(
                        Locale.ROOT,
                        "%.2f / 100",
                        CompetitionRules.forCompetition(c).score(new Evaluation("", c.id(), "", "",
                            (int) a.getValue(), (int) b.getValue(), (int) third.getValue(), "")))
                    : ((int) b.getValue() + 20 * (int) third.getValue()) + " penalty minutes");
    a.addChangeListener(event -> updateTotal.run());
    b.addChangeListener(event -> updateTotal.run());
    third.addChangeListener(event -> updateTotal.run());
    updateTotal.run();
    row(p, 4, quiz ? "Official points" : project ? "Weighted total" : "Time penalty", total);
    row(p, 5, "Feedback", new JScrollPane(feedback));
    dialog(
        quiz ? "Record quiz points" : "Evaluate submission",
        p,
        () -> {
          service.evaluate(
              c.id(),
              ((Team) team.getSelectedItem()).id(),
              (int) a.getValue(),
              (int) b.getValue(),
              (int) third.getValue(),
              feedback.getText());
          app.showCompetition(c.id());
        });
  }

  void winnerDialog(Competition c, Match m) {
    if (c.type() == CompetitionType.QUIZ || c.tournamentFormat() == TournamentFormat.ROUND_ROBIN) {
      matchScoreDialog(c, m);
      return;
    }
    Team[] choices = {service.team(m.leftTeamId()), service.team(m.rightTeamId())};
    Team selected =
        chooseTeam(
            app,
            "Choose the confirmed match winner. This result is final.",
            "Record winner",
            List.of(choices));
    if (selected != null) {
      service.recordWinner(m.id(), selected.id());
      app.showCompetition(c.id());
    }
  }

  void matchScoreDialog(Competition c, Match match) {
    int maximum = c.type() == CompetitionType.QUIZ ? c.quizMaxPoints() : 10000;
    JSpinner left = new JSpinner(new SpinnerNumberModel(0, 0, maximum, 1));
    JSpinner right = new JSpinner(new SpinnerNumberModel(0, 0, maximum, 1));
    left.setName("match-left-score"); right.setName("match-right-score");
    JPanel p = form();
    row(p, 0, service.team(match.leftTeamId()).name(), left);
    row(p, 1, service.team(match.rightTeamId()).name(), right);
    JTextArea note = paragraph(c.tournamentFormat() == TournamentFormat.KNOCKOUT
        ? "A tied score cannot advance a team. Complete an external tie-break and enter the final scores."
        : "A win awards 3 league points; a draw awards 1 to each team. Results are final.", PAPER);
    row(p, 2, "Result policy", note);
    note.setPreferredSize(new Dimension(390, 70));
    dialog("Record match score", p, () -> {
      service.recordMatchScore(match.id(), (int) left.getValue(), (int) right.getValue());
      app.showCompetition(c.id());
    });
  }

  void userDialog() {
    JPanel p = form();
    JTextField name = field(""), email = field("");
    JPasswordField pass = new JPasswordField();
    styleField(pass);
    JComboBox<Role> roles = new JComboBox<>(Role.values());
    roles.setSelectedItem(Role.STUDENT);
    row(p, 0, "Full name", name);
    row(p, 1, "Email address", email);
    row(p, 2, "Role", roles);
    row(p, 3, "Password (8+ characters)", pass);
    dialog(
        "Add account",
        p,
        () -> {
          service.createUser(
              name.getText(),
              email.getText(),
              (Role) roles.getSelectedItem(),
              new String(pass.getPassword()));
          pass.setText("");
          app.showPage("People");
        });
  }

  void passwordDialog() {
    JPanel p = form();
    JPasswordField old = new JPasswordField(), next = new JPasswordField();
    styleField(old);
    styleField(next);
    row(p, 0, "Current password", old);
    row(p, 1, "New password (8+ characters)", next);
    dialog(
        "Change your password",
        p,
        () -> {
          service.changePassword(new String(old.getPassword()), new String(next.getPassword()));
          old.setText("");
          next.setText("");
        });
  }
}
