package edu.aau.ccms.ui;

import static edu.aau.ccms.Model.*;
import static edu.aau.ccms.ui.CampusTheme.*;
import static edu.aau.ccms.ui.UiComponents.*;

import edu.aau.ccms.service.CampusService;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

final class CompetitionViews {
  private final CampusApp app;
  private final CampusService service;

  CompetitionViews(CampusApp app, CampusService service) {
    this.app = app;
    this.service = service;
  }

  JPanel competitionCards(List<Competition> competitions) {
    JPanel cards = panel(new GridLayout(0, 3, 17, 17), PAPER);
    if (competitions.isEmpty()) {
      cards.setLayout(new BorderLayout());
      cards.add(empty("No competitions match. Try another search or format."));
    } else {
      for (Competition competition : competitions) cards.add(competitionCard(competition));
    }
    int rows = Math.max(1, (competitions.size() + 2) / 3);
    cards.setPreferredSize(new Dimension(900, rows * 318 + (rows - 1) * 17));
    cards.setMaximumSize(new Dimension(Integer.MAX_VALUE, rows * 318 + (rows - 1) * 17));
    return cards;
  }

  private JPanel competitionCard(Competition competition) {
    JPanel tile = rounded(WHITE, 24);
    tile.setLayout(new BorderLayout());
    tile.setBorder(new EmptyBorder(1, 1, 1, 1));
    Color tint =
        switch (competition.format()) {
          case PROJECT -> new Color(239, 232, 223);
          case PROGRAMMING -> new Color(232, 235, 244);
          case KNOCKOUT -> new Color(228, 236, 232);
        };
    JPanel banner = rounded(tint, 24);
    banner.setLayout(new BorderLayout());
    banner.setBorder(new EmptyBorder(18, 20, 18, 20));
    banner.setPreferredSize(new Dimension(260, 83));
    banner.add(
        label(formatName(competition.format()).toUpperCase(Locale.ROOT), 10, MUTED, true),
        BorderLayout.CENTER);
    banner.add(new JLabel(formatIcon(competition.format())), BorderLayout.EAST);
    tile.add(banner, BorderLayout.NORTH);
    JPanel details = stack(WHITE);
    details.setOpaque(false);
    details.setBorder(new EmptyBorder(16, 20, 18, 20));
    JLabel status =
        label(
            title(competition.phase()),
            10,
            competition.phase() == Phase.JUDGING ? GOLD : GREEN,
            true);
    status.setOpaque(true);
    status.setBackground(new Color(237, 243, 236));
    status.setBorder(new EmptyBorder(5, 8, 5, 8));
    UiComponents.add(details, status);
    gap(details, 10);
    JLabel name = wrappedLabel(competition.title(), 16, INK, true);
    name.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
    UiComponents.add(details, name);
    gap(details, 8);
    JTextArea description = paragraph(competition.description(), WHITE);
    description.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    description.setPreferredSize(new Dimension(220, 48));
    description.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
    UiComponents.add(details, description);
    details.add(Box.createVerticalGlue());
    UiComponents.add(
        details,
        label(
            service.registrations(competition.id()).size()
                + " / "
                + competition.capacity()
                + " teams   ·   "
                + competition.deadline().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            10,
            MUTED,
            false));
    gap(details, 12);
    JButton open =
        app.button("Explore competition  →", () -> app.showCompetition(competition.id()), false);
    open.setName("open-competition-" + competition.id());
    open.setForeground(ACCENT);
    UiComponents.add(details, open);
    tile.add(details, BorderLayout.CENTER);
    return tile;
  }

  JPanel bracket(Competition competition) {
    List<Match> matches = service.matches(competition.id());
    if (matches.isEmpty())
      return empty(
          "The bracket will be generated when registration closes. Entries must total 2, 4, 8, or"
              + " 16 teams.");
    int entries = service.registrations(competition.id()).size();
    int rounds = Integer.numberOfTrailingZeros(Integer.highestOneBit(entries));
    JPanel columns = panel(new GridLayout(1, rounds + 1, 16, 0), PAPER);
    columns.setBorder(new EmptyBorder(18, 16, 18, 16));
    for (int round = 1; round <= rounds; round++) {
      JPanel column = stack(PAPER);
      UiComponents.add(
          column,
          label(
              round == rounds ? "FINAL" : round == rounds - 1 ? "SEMIFINALS" : "ROUND " + round,
              11,
              MUTED,
              true));
      gap(column, 14);
      boolean populated = false;
      for (Match match : matches)
        if (match.round() == round) {
          populated = true;
          JPanel tile = card();
          tile.setBorder(new EmptyBorder(17, 16, 17, 16));
          UiComponents.add(tile, label(service.team(match.leftTeamId()).name(), 13, INK, true));
          gap(tile, 8);
          UiComponents.add(tile, label(service.team(match.rightTeamId()).name(), 13, MUTED, false));
          gap(tile, 14);
          if (match.winnerId() != null)
            UiComponents.add(
                tile, label("Winner: " + service.team(match.winnerId()).name(), 11, GREEN, true));
          else if (service.currentUser().role() == Role.JUDGE
              && competition.judgeIds().contains(service.currentUser().id())
              && competition.phase() == Phase.ACTIVE)
            UiComponents.add(
                tile,
                app.button(
                    "Record winner", () -> app.dialogs.winnerDialog(competition, match), true));
          else UiComponents.add(tile, label("Awaiting match result", 11, MUTED, false));
          tile.setMaximumSize(new Dimension(Integer.MAX_VALUE, 145));
          UiComponents.add(column, tile);
          gap(column, 14);
        }
      if (!populated) {
        JPanel waiting = card();
        UiComponents.add(waiting, label("Awaiting round winners", 12, MUTED, false));
        waiting.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        UiComponents.add(column, waiting);
      }
      columns.add(column);
    }
    JPanel podium = stack(PAPER);
    UiComponents.add(podium, label("CHAMPION", 11, MUTED, true));
    gap(podium, 14);
    JPanel winner = rounded(new Color(250, 247, 239), 20);
    winner.setLayout(new BoxLayout(winner, BoxLayout.Y_AXIS));
    winner.setBorder(new EmptyBorder(25, 18, 25, 18));
    String champion =
        matches.stream()
            .filter(m -> m.round() == rounds && m.winnerId() != null)
            .map(m -> service.team(m.winnerId()).name())
            .findFirst()
            .orElse(null);
    UiComponents.add(
        winner, label(champion == null ? "Who will take the title?" : champion, 14, GREEN, true));
    gap(winner, 12);
    UiComponents.add(
        winner,
        label(
            champion == null ? "The final decides it." : "Knockout competition winner",
            11,
            MUTED,
            false));
    winner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
    UiComponents.add(podium, winner);
    columns.add(podium);
    columns.setPreferredSize(
        new Dimension((rounds + 1) * 270, Math.max(280, (entries / 2) * 159 + 55)));
    JScrollPane scroll = new JScrollPane(columns);
    scroll.setBorder(new LineBorder(LINE));
    scroll.getVerticalScrollBar().setUnitIncrement(20);
    JPanel wrapper = panel(new BorderLayout(), PAPER);
    wrapper.add(scroll);
    return wrapper;
  }

  JPanel leaderboard(Competition c) {
    JPanel p = panel(new BorderLayout(0, 8), WHITE);
    List<Standing> rows = service.standings(c.id());
    p.add(
        table(
            new String[] {"Rank", "Team", "Score", "Details"},
            rows.stream()
                .map(
                    s ->
                        new Object[] {
                          s.rank(),
                          s.team(),
                          String.format(Locale.ROOT, "%.2f", s.score()),
                          s.detail()
                        })
                .toList()));
    p.add(
        app.button(
            "Export standings as CSV",
            () -> {
              JFileChooser chooser = new JFileChooser();
              chooser.setSelectedFile(new java.io.File("standings.csv"));
              if (chooser.showSaveDialog(app) == JFileChooser.APPROVE_OPTION) {
                Path path = chooser.getSelectedFile().toPath();
                if (Files.exists(path)
                    && JOptionPane.showConfirmDialog(
                            app, "Replace the existing file?", "Export", JOptionPane.YES_NO_OPTION)
                        != JOptionPane.YES_OPTION) return;
                try {
                  Files.writeString(path, service.standingsCsv(c.id()));
                  JOptionPane.showMessageDialog(app, "Standings exported.");
                } catch (IOException e) {
                  throw new IllegalStateException("Could not write that file.", e);
                }
              }
            },
            false),
        BorderLayout.SOUTH);
    return p;
  }
}
