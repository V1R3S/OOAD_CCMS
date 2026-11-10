package edu.aau.arena.ui;

import edu.aau.ccms.Model;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/** Shared visual components for the desktop implementation of the Arena prototype. */
public final class ArenaTheme {
  static final Color INK = new Color(37, 43, 42);
  static final Color MUTED = new Color(106, 114, 112);
  static final Color PAPER = new Color(247, 248, 245);
  static final Color ACCENT = new Color(112, 39, 50);
  static final Color GREEN = new Color(33, 91, 73);
  static final Color LINE = new Color(224, 229, 221);
  static final Color WHITE = Color.WHITE;
  static final Color GOLD = new Color(173, 134, 66);
  static final Color HERO = new Color(231, 237, 230);
  private static ImageIcon cachedLogo;

  private ArenaTheme() {}

  static JLabel heading(String text, int size) {
    JLabel label = UiComponents.label(text, size, INK, false);
    label.setFont(new Font("Georgia", Font.PLAIN, size));
    label.setForeground(INK);
    return label;
  }

  static JPanel rounded(Color background, int radius) {
    JPanel panel =
        new JPanel() {
          @Override
          protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(getBackground());
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.setColor(LINE);
            g.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g.dispose();
          }
        };
    panel.setOpaque(false);
    panel.setBackground(background);
    return panel;
  }

  static JButton actionButton(String text, boolean primary) {
    JButton button =
        new JButton(text) {
          @Override
          protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color fill = getBackground();
            if (getModel().isRollover() && isEnabled()) fill = primary ? fill.darker() : PAPER;
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            g.setColor(hasFocus() ? GOLD : primary ? getBackground() : LINE);
            g.setStroke(new BasicStroke(hasFocus() ? 2f : 1f));
            g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
            g.dispose();
            super.paintComponent(graphics);
          }
        };
    button.setBackground(primary ? ACCENT : WHITE);
    button.setForeground(primary ? WHITE : INK);
    button.setOpaque(false);
    button.setContentAreaFilled(false);
    button.setBorderPainted(false);
    button.setFocusPainted(false);
    button.setRolloverEnabled(true);
    button.setBorder(new EmptyBorder(11, 16, 11, 16));
    button.setFont(new Font("Segoe UI", Font.BOLD, 12));
    button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return button;
  }

  static JLabel universityLogo() {
    JLabel label = new JLabel("Al Ain University");
    label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    label.setForeground(MUTED);
    if (cachedLogo != null) {
      label.setIcon(cachedLogo);
      label.setText("");
      label.getAccessibleContext().setAccessibleName("Al Ain University logo");
      return label;
    }
    try (InputStream resource = ArenaTheme.class.getResourceAsStream("/aau.png")) {
      if (resource != null) {
        BufferedImage original;
        try (ImageInputStream stream = ImageIO.createImageInputStream(resource)) {
          ImageReader reader = ImageIO.getImageReaders(stream).next();
          try {
            reader.setInput(stream);
            ImageReadParam parameters = reader.getDefaultReadParam();
            parameters.setSourceSubsampling(8, 8, 0, 0);
            original = reader.read(0, parameters);
          } finally {
            reader.dispose();
          }
        }
        // The supplied artwork has large transparent margins; crop only for display.
        int left = original.getWidth(), top = original.getHeight(), right = 0, bottom = 0;
        for (int y = 0; y < original.getHeight(); y++) {
          for (int x = 0; x < original.getWidth(); x++) {
            if ((original.getRGB(x, y) >>> 24) != 0) {
              left = Math.min(left, x);
              top = Math.min(top, y);
              right = Math.max(right, x);
              bottom = Math.max(bottom, y);
            }
          }
        }
        if (right >= left && bottom >= top) {
          BufferedImage crop = original.getSubimage(left, top, right - left + 1, bottom - top + 1);
          int width = Math.max(1, (int) Math.round(68.0 * crop.getWidth() / crop.getHeight()));
          BufferedImage icon = new BufferedImage(width, 68, BufferedImage.TYPE_INT_ARGB);
          Graphics2D g = icon.createGraphics();
          g.setRenderingHint(
              RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
          g.drawImage(crop, 0, 0, width, 68, null);
          g.dispose();
          cachedLogo = new ImageIcon(icon);
          label.setIcon(cachedLogo);
          label.setText("");
          label.getAccessibleContext().setAccessibleName("Al Ain University logo");
        }
      }
    } catch (Exception ignored) {
      // The readable university name remains available if artwork cannot be loaded.
    }
    return label;
  }

  static JComponent heroArtwork() {
    return new JComponent() {
      {
        setPreferredSize(new Dimension(210, 200));
      }

      @Override
      protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int x = getWidth() / 2, y = getHeight() / 2;
        g.setColor(new Color(189, 205, 187));
        for (int diameter : new int[] {240, 180, 120})
          g.drawOval(x - diameter / 2, y - diameter / 2, diameter, diameter);
        g.setColor(new Color(116, 141, 112));
        g.setStroke(new BasicStroke(3));
        for (int i = 0; i < 4; i++) {
          double angle = Math.PI * i / 4 - .15;
          int dx = (int) (38 * Math.cos(angle)), dy = (int) (38 * Math.sin(angle));
          g.drawLine(x - dx, y - dy, x + dx, y + dy);
        }
        g.setColor(GREEN);
        g.fillOval(x + 83, y - 83, 7, 7);
        g.dispose();
      }
    };
  }

  static Icon typeIcon(Model.CompetitionType type) {
    return new Icon() {
      public int getIconWidth() {
        return 40;
      }

      public int getIconHeight() {
        return 40;
      }

      public void paintIcon(Component component, Graphics graphics, int x, int y) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x, y);
        g.setColor(GREEN);
        g.setStroke(new BasicStroke(1.8f));
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (type == Model.CompetitionType.PROJECT) {
          for (int i = 0; i < 4; i++) {
            double angle = Math.PI * i / 4;
            int dx = (int) (17 * Math.cos(angle)), dy = (int) (17 * Math.sin(angle));
            g.drawLine(20 - dx, 20 - dy, 20 + dx, 20 + dy);
          }
        } else if (type == Model.CompetitionType.PROGRAMMING) {
          g.drawPolyline(new int[] {13, 5, 13}, new int[] {9, 20, 31}, 3);
          g.drawPolyline(new int[] {27, 35, 27}, new int[] {9, 20, 31}, 3);
          g.drawLine(23, 7, 17, 33);
        } else if (type == Model.CompetitionType.QUIZ) {
          g.drawOval(4, 4, 32, 32);
          g.setFont(new Font("Georgia", Font.BOLD, 26));
          g.drawString("?", 13, 29);
        } else {
          g.drawPolyline(new int[] {5, 5, 35, 35, 5}, new int[] {6, 34, 6, 34, 6}, 5);
        }
        g.dispose();
      }
    };
  }

  static void styleTabs(JTabbedPane tabs) {
    tabs.setForeground(MUTED);
    tabs.setBackground(PAPER);
    tabs.setUI(
        new javax.swing.plaf.basic.BasicTabbedPaneUI() {
          @Override
          protected void installDefaults() {
            super.installDefaults();
            tabInsets = new Insets(12, 18, 12, 18);
            contentBorderInsets = new Insets(1, 0, 0, 0);
            tabAreaInsets = new Insets(0, 0, 0, 0);
          }

          @Override
          protected void paintTabBackground(
              Graphics g, int placement, int index, int x, int y, int w, int h, boolean selected) {
            g.setColor(selected ? WHITE : PAPER);
            g.fillRect(x, y, w, h);
          }

          @Override
          protected void paintTabBorder(
              Graphics g, int placement, int index, int x, int y, int w, int h, boolean selected) {
            g.setColor(selected ? ACCENT : LINE);
            g.fillRect(x, y + h - 3, w, selected ? 3 : 1);
          }

          @Override
          protected void paintContentBorder(Graphics g, int placement, int index) {}

          @Override
          protected void paintFocusIndicator(
              Graphics g,
              int placement,
              Rectangle[] rects,
              int index,
              Rectangle icon,
              Rectangle text,
              boolean selected) {
            if (tabPane.hasFocus() && selected) {
              g.setColor(GOLD);
              g.drawRect(text.x - 3, text.y - 3, text.width + 6, text.height + 6);
            }
          }
        });
  }
}
