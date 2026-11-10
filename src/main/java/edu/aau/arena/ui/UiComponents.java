package edu.aau.arena.ui;

import static edu.aau.ccms.Model.*;
import static edu.aau.arena.ui.ArenaTheme.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

final class UiComponents {
  static JPanel panel(LayoutManager layout, Color color) {
    JPanel p = new JPanel(layout);
    p.setBackground(color);
    return p;
  }

  static JLabel label(String text, int size, Color color, boolean bold) {
    JLabel l = new JLabel();
    l.putClientProperty("html.disable", Boolean.TRUE);
    l.setText(text);
    l.setFont(new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size));
    l.setForeground(color);
    return l;
  }

  static JLabel wrappedLabel(String text, int size, Color color, boolean bold) {
    JLabel label = label("", size, color, bold);
    label.putClientProperty("html.disable", Boolean.FALSE);
    label.setText("<html>" + escape(text) + "</html>");
    return label;
  }

  static DefaultListCellRenderer plainRenderer() {
    DefaultListCellRenderer renderer = new DefaultListCellRenderer();
    renderer.putClientProperty("html.disable", Boolean.TRUE);
    return renderer;
  }

  static Team chooseTeam(Component parent, String prompt, String heading, List<Team> teams) {
    JComboBox<Team> choice = new JComboBox<>(teams.toArray(Team[]::new));
    choice.setRenderer(plainRenderer());
    int answer =
        JOptionPane.showConfirmDialog(
            parent,
            new Object[] {prompt, choice},
            heading,
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE);
    return answer == JOptionPane.OK_OPTION ? (Team) choice.getSelectedItem() : null;
  }

  static JTextArea paragraph(String text, Color background) {
    JTextArea a = new JTextArea(text);
    a.setEditable(false);
    a.setLineWrap(true);
    a.setWrapStyleWord(true);
    a.setOpaque(true);
    a.setBackground(background);
    a.setForeground(MUTED);
    a.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    a.setBorder(null);
    return a;
  }

  static JPanel stack(Color color) {
    JPanel p = panel(null, color);
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    return p;
  }

  static void add(JPanel parent, JComponent child) {
    child.setAlignmentX(Component.LEFT_ALIGNMENT);
    parent.add(child);
  }

  static void gap(JPanel parent, int n) {
    parent.add(Box.createVerticalStrut(n));
  }

  static JPanel card() {
    JPanel p = rounded(WHITE, 24);
    p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
    p.setBorder(new EmptyBorder(22, 24, 22, 24));
    return p;
  }

  static JTextField field(String value) {
    JTextField f = new JTextField(value);
    styleField(f);
    return f;
  }

  static void styleField(JTextField f) {
    f.setForeground(INK);
    f.setBackground(WHITE);
    f.setBorder(new CompoundBorder(new LineBorder(LINE), new EmptyBorder(10, 12, 10, 12)));
    f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 43));
  }

  static String title(Enum<?> value) {
    String s = value.name().toLowerCase(Locale.ROOT).replace('_', ' ');
    return Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }

  static String escape(String s) {
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }

  static JTable grid(String[] columns, List<Object[]> rows) {
    DefaultTableModel model =
        new DefaultTableModel(rows.toArray(Object[][]::new), columns) {
          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        };
    JTable t = new JTable(model);
    t.setRowHeight(43);
    t.setShowVerticalLines(false);
    t.setGridColor(LINE);
    t.setSelectionBackground(new Color(218, 237, 229));
    t.setSelectionForeground(INK);
    t.setForeground(INK);
    t.setBackground(WHITE);
    t.setAutoCreateRowSorter(true);
    for (int i = 0; i < columns.length; i++) {
      TableColumn column = t.getColumnModel().getColumn(i);
      String name = columns[i];
      boolean identity = Set.of("Team", "Name", "Home", "Away", "Captain", "Judge", "Who").contains(name);
      boolean description = Set.of("Details", "Feedback", "Action", "Project", "Email").contains(name);
      column.setMinWidth(identity ? 160 : description ? 180 : 55);
      column.setPreferredWidth(identity ? 200 : description ? 300 : 85);
    }
    t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
    t.getTableHeader().setBackground(PAPER);
    t.getTableHeader().setForeground(MUTED);
    t.getTableHeader().setPreferredSize(new Dimension(800, 40));
    t.getTableHeader()
        .setDefaultRenderer(
            (table, value, selected, focus, row, column) -> {
              JLabel heading = label(String.valueOf(value), 11, MUTED, true);
              heading.setOpaque(true);
              heading.setBackground(PAPER);
              heading.setBorder(
                  new CompoundBorder(
                      new MatteBorder(0, 0, 1, 0, LINE), new EmptyBorder(10, 12, 10, 12)));
              return heading;
            });
    DefaultTableCellRenderer render =
        new DefaultTableCellRenderer() {
          {
            putClientProperty("html.disable", Boolean.TRUE);
          }

          @Override
          public Component getTableCellRendererComponent(
              JTable table, Object value, boolean selected, boolean focus, int row, int col) {
            Component comp =
                super.getTableCellRendererComponent(table, value, selected, focus, row, col);
            setBorder(new EmptyBorder(0, 12, 0, 12));
            if (!selected) comp.setBackground(row % 2 == 0 ? WHITE : new Color(249, 250, 247));
            setToolTipText("<html>" + escape(String.valueOf(value)) + "</html>");
            return comp;
          }
        };
    t.setDefaultRenderer(Object.class, render);
    return t;
  }

  static JScrollPane table(String[] columns, List<Object[]> rows) {
    JScrollPane s = new JScrollPane(grid(columns, rows));
    s.getViewport().setBackground(WHITE);
    s.setBorder(new LineBorder(LINE));
    s.setPreferredSize(new Dimension(800, 300));
    return s;
  }

  static JPanel empty(String message) {
    JPanel p = panel(new BorderLayout(), WHITE);
    p.setBorder(new EmptyBorder(30, 25, 30, 25));
    p.add(paragraph(message, WHITE));
    return p;
  }
}
