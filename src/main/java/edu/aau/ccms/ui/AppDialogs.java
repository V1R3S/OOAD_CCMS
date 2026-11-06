package edu.aau.ccms.ui;

import static edu.aau.ccms.Model.*;
import static edu.aau.ccms.ui.CampusTheme.*;
import static edu.aau.ccms.ui.UiComponents.*;

import edu.aau.ccms.model.UserSummary;
import edu.aau.ccms.service.CampusService;
import java.awt.*;
import java.awt.event.*;
import java.time.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

final class AppDialogs {
  private final CampusApp app;
  private final CampusService service;

  AppDialogs(CampusApp app, CampusService service) {
    this.app = app;
    this.service = service;
  }

  private JPanel form() {
    JPanel p = panel(new GridBagLayout(), PAPER);
    p.setBorder(new EmptyBorder(10, 10, 10, 10));
    return p;
  }

  private void row(JPanel p, int index, String name, JComponent control) {
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
    d.add(form);
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
    JComboBox<Format> format = new JComboBox<>(Format.values());
    format.setSelectedItem(c == null ? Format.PROJECT : c.format());
    JSpinner cap = new JSpinner(new SpinnerNumberModel(c == null ? 8 : c.capacity(), 2, 64, 1)),
        size = new JSpinner(new SpinnerNumberModel(c == null ? 4 : c.teamSize(), 1, 8, 1));
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
    row(p, 2, "Competition format", format);
    row(p, 3, "Maximum teams", cap);
    row(p, 4, "Maximum members / team", size);
    row(p, 5, "Deadline (YYYY-MM-DDTHH:MM)", deadline);
    row(p, 6, "Judges (Ctrl-click to select)", new JScrollPane(judgeList));
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
            throw new IllegalArgumentException("Use a deadline such as 2026-12-01T17:00.");
          }
          if (c == null)
            service.createCompetition(
                name.getText(),
                description.getText(),
                (Format) format.getSelectedItem(),
                (int) cap.getValue(),
                (int) size.getValue(),
                date,
                selected);
          else
            service.editCompetition(
                c.id(),
                name.getText(),
                description.getText(),
                (Format) format.getSelectedItem(),
                (int) cap.getValue(),
                (int) size.getValue(),
                date,
                selected);
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
        service.submissions(c.id()).stream().map(s -> service.team(s.teamId())).toList();
    if (teams.isEmpty())
      throw new IllegalArgumentException("There are no submissions to evaluate.");
    JComboBox<Team> team = new JComboBox<>(teams.toArray(Team[]::new));
    team.setRenderer(plainRenderer());
    boolean project = c.format() == Format.PROJECT;
    JSpinner a = new JSpinner(new SpinnerNumberModel(0, 0, project ? 100 : 10, 1)),
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
    row(p, 1, project ? "Innovation /100 (40%)" : "Problems solved /10", a);
    row(p, 2, project ? "Execution /100 (35%)" : "Elapsed minutes /300", b);
    row(p, 3, project ? "Presentation /100 (25%)" : "Wrong attempts (+20 min each)", third);
    JLabel total = label("", 20, GREEN, true);
    total.setName("evaluation-total");
    Runnable updateTotal =
        () ->
            total.setText(
                project
                    ? String.format(
                        Locale.ROOT,
                        "%.2f / 100",
                        (int) a.getValue() * .4
                            + (int) b.getValue() * .35
                            + (int) third.getValue() * .25)
                    : ((int) b.getValue() + 20 * (int) third.getValue()) + " penalty minutes");
    a.addChangeListener(event -> updateTotal.run());
    b.addChangeListener(event -> updateTotal.run());
    third.addChangeListener(event -> updateTotal.run());
    updateTotal.run();
    row(p, 4, project ? "Weighted total" : "Time penalty", total);
    row(p, 5, "Feedback", new JScrollPane(feedback));
    dialog(
        "Evaluate submission",
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
