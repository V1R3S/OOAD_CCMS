package edu.aau.ccms.ui;

import static edu.aau.ccms.Model.*;
import static edu.aau.ccms.ui.CampusTheme.*;
import static edu.aau.ccms.ui.UiComponents.*;

import edu.aau.ccms.repository.Repository;
import edu.aau.ccms.rules.CompetitionRules;
import edu.aau.ccms.service.CampusService;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.function.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/** Swing boundary layer. Business rules remain in CampusService. */
public final class CampusApp extends JFrame {
  private final CampusService service;
  final AppDialogs dialogs;
  private final CompetitionViews views;
  private JPanel body, shell;
  private JTabbedPane competitionTabs;
  private String activePage = "Overview";
  private final Map<String, JButton> navigation = new LinkedHashMap<>();
  private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm");

  public CampusApp(CampusService service) {
    super("Campus · Competition Management");
    this.service = service;
    this.dialogs = new AppDialogs(this, service);
    this.views = new CompetitionViews(this, service);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setMinimumSize(new Dimension(1280, 800));
    setSize(1440, 1000);
    setLocationRelativeTo(null);
    theme();
    showLogin();
  }

  private static void theme() {
    Font font = new Font("Segoe UI", Font.PLAIN, 14);
    for (Object key : Collections.list(UIManager.getDefaults().keys()))
      if (UIManager.get(key) instanceof javax.swing.plaf.FontUIResource) UIManager.put(key, font);
    UIManager.put("Panel.background", PAPER);
    UIManager.put("OptionPane.background", PAPER);
    UIManager.put("TabbedPane.selected", WHITE);
    UIManager.put("TextField.caretForeground", INK);
    UIManager.put("ComboBox.background", WHITE);
  }

  JButton button(String text, Runnable action, boolean primary) {
    JButton button = actionButton(text, primary);
    button.addActionListener(event -> attempt(action));
    return button;
  }

  private void attempt(Runnable action) {
    try {
      action.run();
    } catch (Exception e) {
      JOptionPane.showMessageDialog(
          this, e.getMessage(), "Please check", JOptionPane.WARNING_MESSAGE);
    }
  }

  void showLogin() {
    JPanel page = panel(new GridLayout(1, 2), PAPER);
    JPanel story = stack(ACCENT);
    story.setBorder(new EmptyBorder(75, 60, 65, 60));
    UiComponents.add(story, label("Campus", 30, WHITE, true));
    gap(story, 100);
    JLabel big = heading("Bring your ideas.", 44);
    big.setForeground(WHITE);
    UiComponents.add(story, big);
    JLabel friendly = heading("Find your moment.", 44);
    friendly.setForeground(WHITE);
    UiComponents.add(story, friendly);
    gap(story, 25);
    JTextArea intro =
        paragraph(
            "One place for campus talent to come together. Organize challenges, build your team,"
                + " and turn great work into your next achievement.",
            ACCENT);
    intro.setForeground(new Color(236, 218, 219));
    intro.setPreferredSize(new Dimension(470, 110));
    intro.setMinimumSize(new Dimension(200, 90));
    intro.setMaximumSize(new Dimension(480, 130));
    UiComponents.add(story, intro);
    story.add(Box.createVerticalGlue());
    UiComponents.add(story, label("PROJECTS   /   CODE   /   ROBOTICS", 12, GOLD, true));
    gap(story, 18);
    UiComponents.add(
        story,
        label("Al Ain University · Competition workspace", 13, new Color(236, 218, 219), false));
    JPanel center = panel(new GridBagLayout(), PAPER);
    JPanel form = stack(PAPER);
    form.setPreferredSize(new Dimension(395, 480));
    UiComponents.add(form, label("WELCOME TO CAMPUS", 12, ACCENT, true));
    gap(form, 12);
    UiComponents.add(form, heading("Make your next move.", 30));
    gap(form, 10);
    UiComponents.add(form, label("Sign in to your competition workspace.", 14, MUTED, false));
    gap(form, 30);
    JTextField email = field("");
    email.setName("login-email");
    email.getAccessibleContext().setAccessibleName("Email address");
    JPasswordField password = new JPasswordField();
    password.setName("login-password");
    password.getAccessibleContext().setAccessibleName("Password");
    styleField(password);
    UiComponents.add(form, label("Email address", 13, INK, true));
    gap(form, 7);
    UiComponents.add(form, email);
    gap(form, 18);
    UiComponents.add(form, label("Password", 13, INK, true));
    gap(form, 7);
    UiComponents.add(form, password);
    gap(form, 24);
    JButton sign =
        button(
            "Sign in  →",
            () -> {
              service.login(email.getText(), new String(password.getPassword()));
              password.setText("");
              showShell();
            },
            true);
    UiComponents.add(form, sign);
    getRootPane().setDefaultButton(sign);
    gap(form, 24);
    JTextArea demo =
        paragraph(
            "Demo account: organizer1@campus.test\n"
                + "Password: Campus@2026\n"
                + "Other roles and student accounts are listed in README.md.",
            PAPER);
    demo.setMaximumSize(new Dimension(400, 90));
    UiComponents.add(form, demo);
    center.add(form);
    page.add(story);
    page.add(center);
    setContentPane(page);
    revalidate();
    repaint();
  }

  void showShell() {
    getRootPane().setDefaultButton(null);
    navigation.clear();
    shell = panel(new BorderLayout(), PAPER);
    JPanel sidebar = stack(WHITE);
    sidebar.setPreferredSize(new Dimension(244, 800));
    sidebar.setBorder(
        new CompoundBorder(new MatteBorder(0, 0, 0, 1, LINE), new EmptyBorder(30, 22, 24, 22)));
    JPanel brand = panel(new FlowLayout(FlowLayout.LEFT, 10, 0), WHITE);
    brand.setMaximumSize(new Dimension(210, 55));
    JLabel mark = label(" C· ", 30, WHITE, true);
    mark.setOpaque(true);
    mark.setBackground(ACCENT);
    brand.add(mark);
    JPanel brandWords = stack(WHITE);
    UiComponents.add(brandWords, label("Campus", 25, INK, true));
    UiComponents.add(brandWords, label("Competition management", 10, MUTED, false));
    brand.add(brandWords);
    UiComponents.add(sidebar, brand);
    gap(sidebar, 38);
    UiComponents.add(sidebar, label("WORKSPACE", 10, MUTED, true));
    gap(sidebar, 16);
    Role role = service.currentUser().role();
    for (String page :
        List.of(
            "Overview",
            "Competitions",
            "My teams",
            "Judging studio",
            "Results",
            "People",
            "Activity")) {
      if (page.equals("Judging studio") && role != Role.JUDGE) continue;
      if (page.equals("People") && role != Role.ADMIN) continue;
      if (page.equals("Activity") && role != Role.ADMIN && role != Role.ORGANIZER) continue;
      JButton link = button(page, () -> showPage(page), false);
      link.setHorizontalAlignment(SwingConstants.LEFT);
      link.setMaximumSize(new Dimension(200, 44));
      navigation.put(page, link);
      UiComponents.add(sidebar, link);
      gap(sidebar, 7);
    }
    sidebar.add(Box.createVerticalGlue());
    UiComponents.add(sidebar, universityLogo());
    gap(sidebar, 15);
    UiComponents.add(sidebar, label("Built for campus.", 12, MUTED, false));
    UiComponents.add(sidebar, label("Made for possibility.", 12, MUTED, false));
    gap(sidebar, 18);
    UiComponents.add(sidebar, button("Change password", dialogs::passwordDialog, false));
    gap(sidebar, 8);
    UiComponents.add(
        sidebar,
        button(
            "Sign out",
            () -> {
              service.logout();
              showLogin();
            },
            false));

    JPanel workspace = panel(new BorderLayout(), PAPER);
    JPanel topbar = panel(new BorderLayout(), WHITE);
    topbar.setBorder(
        new CompoundBorder(new MatteBorder(0, 0, 1, 0, LINE), new EmptyBorder(22, 36, 22, 36)));
    topbar.add(
        label("●  Campus workspace  /  Al Ain University", 12, MUTED, false), BorderLayout.WEST);
    topbar.add(
        label(service.currentUser().name() + "   ·   " + title(role), 12, INK, true),
        BorderLayout.EAST);
    body = panel(new BorderLayout(), PAPER);
    workspace.add(topbar, BorderLayout.NORTH);
    workspace.add(body, BorderLayout.CENTER);
    shell.add(sidebar, BorderLayout.WEST);
    shell.add(workspace, BorderLayout.CENTER);
    setContentPane(shell);
    showPage("Overview");
  }

  void showPage(String page) {
    activePage = page;
    navigation.forEach(
        (name, button) -> {
          button.setBackground(name.equals(page) ? new Color(245, 233, 235) : WHITE);
          button.setForeground(name.equals(page) ? ACCENT : MUTED);
        });
    switch (page) {
      case "Overview" -> overview();
      case "Competitions" -> competitionList();
      case "My teams" -> teamsPage();
      case "Judging studio" -> judgingPage();
      case "Results" -> resultsPage();
      case "People" -> peoplePage();
      case "Activity" -> activityPage();
      default -> throw new IllegalArgumentException("Unknown page");
    }
  }

  private JPanel page(String eyebrow, String heading, String subtitle) {
    body.removeAll();
    JPanel content = stack(PAPER);
    content.setBorder(new EmptyBorder(32, 36, 32, 36));
    JPanel topline = panel(new BorderLayout(), PAPER);
    topline.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
    topline.add(label(eyebrow.toUpperCase(Locale.ROOT), 11, ACCENT, true), BorderLayout.WEST);
    topline.add(
        label(LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), 11, MUTED, false),
        BorderLayout.EAST);
    UiComponents.add(content, topline);
    gap(content, 13);
    JLabel title = heading(heading, 36);
    title.setName("page-heading");
    UiComponents.add(content, title);
    gap(content, 10);
    UiComponents.add(content, label(subtitle, 13, MUTED, false));
    gap(content, 26);
    JScrollPane scroll = new JScrollPane(content);
    scroll.setBorder(null);
    scroll.getVerticalScrollBar().setUnitIncrement(20);
    body.add(scroll);
    body.revalidate();
    body.repaint();
    return content;
  }

  private void refresh() {
    body.revalidate();
    body.repaint();
  }

  private void overview() {
    JPanel page =
        page(
            "Your campus, in motion",
            "Good morning, " + service.currentUser().name().split(" ")[0] + ".",
            "A place to challenge yourself. A community to do it with.");
    JPanel hero = rounded(HERO, 26);
    hero.setLayout(new BorderLayout(20, 0));
    hero.setBorder(new EmptyBorder(25, 30, 25, 30));
    JPanel words = stack(HERO);
    UiComponents.add(words, label("THE NEXT GREAT IDEA STARTS HERE", 11, GREEN, true));
    gap(words, 13);
    UiComponents.add(words, heading("Bring your ideas.", 32));
    UiComponents.add(words, heading("Find your moment.", 32));
    gap(words, 13);
    UiComponents.add(
        words, label("From your first prototype to the final round,", 12, MUTED, false));
    UiComponents.add(words, label("there’s a challenge with your name on it.", 12, MUTED, false));
    gap(words, 17);
    JButton explore = button("Explore competitions  →", () -> showPage("Competitions"), true);
    explore.setBackground(GREEN);
    UiComponents.add(words, explore);
    hero.add(words, BorderLayout.CENTER);
    hero.add(heroArtwork(), BorderLayout.EAST);
    hero.setMaximumSize(new Dimension(Integer.MAX_VALUE, 292));
    UiComponents.add(page, hero);
    gap(page, 22);

    JPanel metrics = panel(new GridLayout(1, 3, 17, 0), PAPER);
    metrics.add(
        metric(
            "Competitions this term",
            String.format("%02d", service.competitions().size()),
            "Three ways to compete"));
    metrics.add(
        metric(
            "Open for registration",
            String.format(
                "%02d",
                service.competitions().stream()
                    .filter(c -> c.phase() == Phase.REGISTRATION)
                    .count()),
            "Your next challenge awaits"));
    metrics.add(
        metric(
            "Campus teams", String.format("%02d", service.teams().size()), "Ready to collaborate"));
    metrics.setMaximumSize(new Dimension(Integer.MAX_VALUE, 122));
    UiComponents.add(page, metrics);
    gap(page, 26);
    UiComponents.add(page, label("Find your next challenge", 20, INK, true));
    gap(page, 16);
    UiComponents.add(
        page, views.competitionCards(service.competitions().stream().limit(3).toList()));
    gap(page, 22);
    UiComponents.add(
        page, label("Changes are saved automatically on this computer.", 11, MUTED, false));
    refresh();
  }

  private JPanel metric(String heading, String number, String footer) {
    JPanel panel = card();
    panel.setBorder(new EmptyBorder(17, 22, 17, 22));
    UiComponents.add(panel, label(heading, 11, MUTED, false));
    gap(panel, 7);
    UiComponents.add(panel, label(number, 29, INK, false));
    gap(panel, 5);
    UiComponents.add(panel, label(footer, 11, MUTED, false));
    return panel;
  }

  private void competitionList() {
    JPanel page =
        page(
            "Find your challenge",
            "Something for every ambition.",
            "Explore the formats. Bring a team. Make your mark.");
    JPanel toolbar = panel(new BorderLayout(12, 0), PAPER);
    JTextField query = field("");
    query.setName("competition-search");
    query.getAccessibleContext().setAccessibleName("Search competitions");
    query.setToolTipText("Search titles and descriptions");
    JComboBox<String> filter =
        new JComboBox<>(
            new String[] {
              "All formats", "Project showcase", "Programming contest", "Knockout bracket"
            });
    filter.setName("competition-format-filter");
    filter.getAccessibleContext().setAccessibleName("Filter competition format");
    toolbar.add(query, BorderLayout.CENTER);
    toolbar.add(filter, BorderLayout.EAST);
    toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
    UiComponents.add(page, toolbar);
    gap(page, 18);
    if (service.currentUser().role() == Role.ORGANIZER
        || service.currentUser().role() == Role.ADMIN) {
      UiComponents.add(
          page, button("+  Create competition", () -> dialogs.competitionDialog(null), true));
      gap(page, 20);
    }
    JPanel list = stack(PAPER);
    UiComponents.add(page, list);
    Runnable render =
        () -> {
          list.removeAll();
          String search = query.getText().strip().toLowerCase(Locale.ROOT);
          List<Competition> found =
              service.competitions().stream()
                  .filter(
                      c ->
                          (c.title() + " " + c.description())
                              .toLowerCase(Locale.ROOT)
                              .contains(search))
                  .filter(
                      c ->
                          filter.getSelectedIndex() == 0
                              || c.format().ordinal() == filter.getSelectedIndex() - 1)
                  .toList();
          UiComponents.add(list, views.competitionCards(found));
          refresh();
        };
    query
        .getDocument()
        .addDocumentListener(
            new javax.swing.event.DocumentListener() {
              public void insertUpdate(javax.swing.event.DocumentEvent event) {
                render.run();
              }

              public void removeUpdate(javax.swing.event.DocumentEvent event) {
                render.run();
              }

              public void changedUpdate(javax.swing.event.DocumentEvent event) {
                render.run();
              }
            });
    filter.addActionListener(event -> render.run());
    render.run();
  }

  private void judgingPage() {
    JPanel page =
        page(
            "Fair play, clear decisions",
            "Judging studio",
            "Review your assigned competitions. Apply the right rules to each decision.");
    List<Competition> assigned =
        service.competitions().stream()
            .filter(c -> c.judgeIds().contains(service.currentUser().id()))
            .toList();
    if (assigned.isEmpty())
      UiComponents.add(page, empty("No competitions are assigned to you yet."));
    else UiComponents.add(page, views.competitionCards(assigned));
    gap(page, 22);
    UiComponents.add(
        page,
        label(
            "Projects: weighted rubric   /   Programming: solved + penalties   /   Knockout: match"
                + " winners",
            12,
            MUTED,
            false));
    refresh();
  }

  void showCompetition(String cid) {
    navigation.forEach(
        (name, b) -> {
          b.setBackground(name.equals("Competitions") ? new Color(245, 233, 235) : WHITE);
          b.setForeground(name.equals("Competitions") ? ACCENT : MUTED);
        });
    Competition c = service.competition(cid);
    JPanel p =
        page(
            formatName(c.format()),
            c.title(),
            title(c.phase()) + "   ·   Organized by " + service.userName(c.organizerId()));
    JPanel back = panel(new FlowLayout(FlowLayout.LEFT, 0, 0), PAPER);
    back.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
    back.add(button("←  All competitions", () -> showPage("Competitions"), false));
    UiComponents.add(p, back);
    gap(p, 18);
    JPanel summary = card();
    JTextArea desc = paragraph(c.description(), WHITE);
    desc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));
    UiComponents.add(summary, desc);
    gap(summary, 14);
    UiComponents.add(
        summary,
        label(
            service.registrations(cid).size()
                + " / "
                + c.capacity()
                + " teams     ·     Up to "
                + c.teamSize()
                + " members     ·     Deadline: "
                + c.deadline().format(DATE),
            13,
            INK,
            true));
    gap(summary, 12);
    JTextArea rules = paragraph(CompetitionRules.forFormat(c.format()).explanation(), WHITE);
    rules.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
    UiComponents.add(summary, rules);
    gap(summary, 16);
    JPanel stages = panel(new GridLayout(1, 6, 5, 0), WHITE);
    for (Phase phase : Phase.values()) {
      JLabel stage =
          label(title(phase), 12, phase == c.phase() ? WHITE : MUTED, phase == c.phase());
      stage.setOpaque(true);
      stage.setForeground(phase == c.phase() ? ACCENT : MUTED);
      stage.setBackground(WHITE);
      stage.setHorizontalAlignment(SwingConstants.CENTER);
      stage.setBorder(
          new CompoundBorder(
              new MatteBorder(3, 0, 0, 0, phase == c.phase() ? ACCENT : LINE),
              new EmptyBorder(9, 3, 9, 3)));
      stages.add(stage);
    }
    stages.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
    UiComponents.add(summary, stages);
    summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));
    UiComponents.add(p, summary);
    gap(p, 18);
    JPanel actions = panel(new FlowLayout(FlowLayout.LEFT, 8, 0), PAPER);
    actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
    if (service.manages(c)) {
      if (c.phase() == Phase.DRAFT)
        actions.add(button("Edit rules & judges", () -> dialogs.competitionDialog(c), false));
      if (c.phase() != Phase.ARCHIVED)
        actions.add(
            button(
                nextAction(c),
                () -> {
                  if (JOptionPane.showConfirmDialog(
                          this,
                          "Move to "
                              + title(Phase.values()[c.phase().ordinal() + 1])
                              + "? This transition is final.",
                          "Advance competition",
                          JOptionPane.OK_CANCEL_OPTION)
                      == JOptionPane.OK_OPTION) {
                    service.advance(cid);
                    showCompetition(cid);
                  }
                },
                true));
    }
    if (service.currentUser().role() == Role.STUDENT) {
      if (c.phase() == Phase.REGISTRATION) {
        actions.add(button("Register my team", () -> dialogs.registrationDialog(c, false), true));
        actions.add(button("Withdraw team", () -> dialogs.registrationDialog(c, true), false));
      }
      if (c.phase() == Phase.ACTIVE && c.format() != Format.KNOCKOUT)
        actions.add(button("Submit / update work", () -> dialogs.submissionDialog(c), true));
    }
    if (service.currentUser().role() == Role.JUDGE
        && c.judgeIds().contains(service.currentUser().id())
        && c.phase() == Phase.JUDGING
        && c.format() != Format.KNOCKOUT)
      actions.add(button("Evaluate a submission", () -> dialogs.evaluationDialog(c), true));
    UiComponents.add(p, actions);
    gap(p, 20);
    JTabbedPane tabs = new JTabbedPane();
    styleTabs(tabs);
    competitionTabs = tabs;
    tabs.setPreferredSize(new Dimension(800, 310));
    tabs.addTab(
        "Teams",
        table(
            new String[] {"Team", "Captain", "Members"},
            service.registrations(cid).stream()
                .map(r -> service.team(r.teamId()))
                .map(
                    t ->
                        new Object[] {
                          t.name(), service.userName(t.captainId()), t.members().size()
                        })
                .toList()));
    if (c.format() == Format.KNOCKOUT) tabs.addTab("Bracket", views.bracket(c));
    else if (service.manages(c)
        || c.judgeIds().contains(service.currentUser().id())
        || service.currentUser().role() == Role.STUDENT) {
      List<Submission> submissions = service.submissions(cid);
      JPanel work = panel(new BorderLayout(0, 8), WHITE);
      JTable grid =
          grid(
              new String[] {"Team", "Project", "Submitted"},
              submissions.stream()
                  .map(
                      s ->
                          new Object[] {
                            service.team(s.teamId()).name(), s.title(), s.submittedAt().format(DATE)
                          })
                  .toList());
      work.add(new JScrollPane(grid));
      work.add(
          button(
              "Read selected submission",
              () -> {
                int row = grid.getSelectedRow();
                if (row < 0) throw new IllegalArgumentException("Select a submission first.");
                Submission s = submissions.get(grid.convertRowIndexToModel(row));
                JTextArea details =
                    paragraph(
                        s.title()
                            + "\n\n"
                            + s.description()
                            + "\n\nProject link (copy to your browser):\n"
                            + s.url(),
                        PAPER);
                details.setPreferredSize(new Dimension(550, 250));
                JOptionPane.showMessageDialog(
                    this,
                    new JScrollPane(details),
                    "Submission · " + service.team(s.teamId()).name(),
                    JOptionPane.PLAIN_MESSAGE);
              },
              false),
          BorderLayout.SOUTH);
      tabs.addTab("Submissions", work);
      tabs.addTab(
          "Reviews",
          table(
              new String[] {"Team", "Judge", "A / B / C", "Feedback"},
              service.evaluations(cid).stream()
                  .map(
                      e ->
                          new Object[] {
                            service.team(e.teamId()).name(),
                            service.userName(e.judgeId()),
                            e.a() + " / " + e.b() + " / " + e.c(),
                            e.feedback()
                          })
                  .toList()));
    }
    if (c.phase().ordinal() >= Phase.PUBLISHED.ordinal() || service.manages(c))
      tabs.addTab(
          c.phase().ordinal() < Phase.PUBLISHED.ordinal() ? "Results preview" : "Leaderboard",
          views.leaderboard(c));
    else
      tabs.addTab(
          "Leaderboard", empty("Results will appear here after the organizer publishes them."));
    UiComponents.add(p, tabs);
    refresh();
  }

  private String nextAction(Competition c) {
    return switch (c.phase()) {
      case DRAFT -> "Open registration  →";
      case REGISTRATION -> "Start competition  →";
      case ACTIVE -> c.format() == Format.KNOCKOUT ? "Certify bracket  →" : "Begin judging  →";
      case JUDGING -> "Publish results  →";
      case PUBLISHED -> "Archive competition";
      case ARCHIVED -> "Archived";
    };
  }

  private void teamsPage() {
    JPanel p =
        page(
            "Team workspace",
            "Your people. Your potential.",
            "Create a team, join your classmates, and enter a competition together.");
    if (service.currentUser().role() == Role.STUDENT) {
      UiComponents.add(
          p,
          button(
              "+  Create a team",
              () -> {
                String name = JOptionPane.showInputDialog(this, "Team name");
                if (name != null) {
                  service.createTeam(name);
                  teamsPage();
                }
              },
              true));
      gap(p, 18);
    }
    for (Team t : service.teams()) {
      JPanel row = rounded(WHITE, 24);
      row.setLayout(new BorderLayout(15, 0));
      row.setBorder(new EmptyBorder(18, 22, 18, 22));
      JPanel text = stack(WHITE);
      UiComponents.add(text, label(t.name(), 18, INK, true));
      gap(text, 6);
      UiComponents.add(
          text,
          label(
              "Captain: "
                  + service.userName(t.captainId())
                  + "  ·  "
                  + t.members().size()
                  + " members",
              12,
              MUTED,
              false));
      gap(text, 6);
      UiComponents.add(
          text,
          label(
              String.join(", ", t.members().stream().map(service::userName).toList()),
              12,
              MUTED,
              false));
      row.add(text);
      if (service.currentUser().role() == Role.STUDENT) {
        boolean member = t.members().contains(service.currentUser().id());
        if (!member)
          row.add(
              button(
                  "Join team",
                  () -> {
                    service.joinTeam(t.id());
                    teamsPage();
                  },
                  false),
              BorderLayout.EAST);
        else if (!t.captainId().equals(service.currentUser().id()))
          row.add(
              button(
                  "Leave team",
                  () -> {
                    service.leaveTeam(t.id());
                    teamsPage();
                  },
                  false),
              BorderLayout.EAST);
        else row.add(label("YOU’RE THE CAPTAIN", 11, ACCENT, true), BorderLayout.EAST);
      }
      row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
      UiComponents.add(p, row);
      gap(p, 13);
    }
    refresh();
  }

  private void resultsPage() {
    JPanel p =
        page(
            "The finish line",
            "Celebrate the results.",
            "Official results from completed campus competitions. Equal results share a rank.");
    List<Competition> list =
        service.competitions().stream()
            .filter(c -> c.phase().ordinal() >= Phase.PUBLISHED.ordinal())
            .toList();
    if (list.isEmpty())
      UiComponents.add(p, empty("No published results yet. Check back after judging."));
    for (Competition c : list) {
      UiComponents.add(p, label(c.title(), 21, INK, true));
      gap(p, 14);
      JPanel board = views.leaderboard(c);
      board.setPreferredSize(new Dimension(800, 240));
      board.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
      UiComponents.add(p, board);
      gap(p, 25);
    }
    refresh();
  }

  private void peoplePage() {
    JPanel p =
        page(
            "Administration",
            "The people behind the possibilities.",
            "Create student, judge, organizer, and administrator accounts for this local"
                + " workspace.");
    UiComponents.add(p, button("+  Add account", dialogs::userDialog, true));
    gap(p, 20);
    UiComponents.add(
        p,
        table(
            new String[] {"Name", "Email", "Role"},
            service.users().stream()
                .map(u -> new Object[] {u.name(), u.email(), title(u.role())})
                .toList()));
    refresh();
  }

  private void activityPage() {
    JPanel p =
        page(
            "Workspace history",
            "Every step, accounted for.",
            "Recent successful changes, saved with the actor and local time.");
    List<Audit> events = new ArrayList<>(service.audit());
    Collections.reverse(events);
    UiComponents.add(
        p,
        table(
            new String[] {"When", "Who", "Action"},
            events.stream()
                .map(a -> new Object[] {a.time().format(DATE), a.actor(), a.action()})
                .toList()));
    refresh();
  }

  /** Render actual Swing screens for reproducible documentation; no desktop capture is required. */
  void capture(Path directory) throws IOException {
    Files.createDirectories(directory);
    setSize(1440, 1150);
    setVisible(true);
    saveScreen(directory.resolve("01-sign-in.png"));
    service.login("organizer1@campus.test", "Campus@2026");
    showShell();
    saveScreen(directory.resolve("02-overview.png"));
    showPage("Competitions");
    saveScreen(directory.resolve("03-competitions.png"));
    showCompetition("hackathon");
    saveScreen(directory.resolve("04-competition.png"));
    showCompetition("robotics");
    competitionTabs.setSelectedIndex(1);
    saveScreen(directory.resolve("05-bracket.png"));
    showPage("Results");
    saveScreen(directory.resolve("06-results.png"));
    showPage("My teams");
    saveScreen(directory.resolve("07-teams.png"));
    service.logout();
    service.login("judge3@campus.test", "Campus@2026");
    showShell();
    showPage("Judging studio");
    saveScreen(directory.resolve("08-judging-studio.png"));
    dispose();
  }

  private void saveScreen(Path path) throws IOException {
    validate();
    getContentPane().doLayout();
    BufferedImage image =
        new BufferedImage(
            getContentPane().getWidth(), getContentPane().getHeight(), BufferedImage.TYPE_INT_RGB);
    Graphics2D graphics = image.createGraphics();
    getContentPane().printAll(graphics);
    graphics.dispose();
    ImageIO.write(image, "png", path.toFile());
  }

  public static void main(String[] args) {
    Path data = Path.of(System.getProperty("ccms.data", "data/campus.bin")), capture = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("--data") && i + 1 < args.length) data = Path.of(args[++i]);
      else if (args[i].equals("--capture") && i + 1 < args.length) capture = Path.of(args[++i]);
      else {
        System.err.println("Usage: CampusApp [--data path] [--capture directory]");
        System.exit(2);
      }
    }
    try {
      Repository repository = new Repository(data);
      CampusService service;
      try {
        service = new CampusService(repository, true);
      } catch (Exception e) {
        repository.close();
        throw e;
      }
      Path destination = capture;
      SwingUtilities.invokeLater(
          () -> {
            CampusApp app = new CampusApp(service);
            app.addWindowListener(
                new WindowAdapter() {
                  @Override
                  public void windowClosed(WindowEvent e) {
                    try {
                      repository.close();
                    } catch (IOException ignored) {
                    }
                  }
                });
            if (destination == null) app.setVisible(true);
            else
              try {
                app.capture(destination);
              } catch (IOException e) {
                e.printStackTrace();
                app.dispose();
                System.exit(1);
              }
          });
    } catch (Exception e) {
      System.err.println("Campus could not start: " + e.getMessage());
      if (!GraphicsEnvironment.isHeadless())
        JOptionPane.showMessageDialog(
            null, e.getMessage(), "Campus could not start", JOptionPane.ERROR_MESSAGE);
      System.exit(1);
    }
  }
}
