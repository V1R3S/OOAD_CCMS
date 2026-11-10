package edu.aau.arena.ui;

import java.awt.*;
import javax.swing.*;

/** Pages follow the available width; long content scrolls vertically instead of pushing the window wider. */
final class WorkspacePanel extends JPanel implements Scrollable {
  WorkspacePanel() {
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(ArenaTheme.PAPER);
  }
  public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
  public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 20; }
  public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) { return Math.max(20, visible.height - 40); }
  public boolean getScrollableTracksViewportWidth() { return true; }
  public boolean getScrollableTracksViewportHeight() { return false; }
}
