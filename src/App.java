import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;

public final class App {
  public static Pair<JPanel, JTextField> makeField(String text, String initial) {
    JPanel panel = new JPanel();
    panel.setSize(new Dimension(150, 20));
    panel.setPreferredSize(new Dimension(150, 20));
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    JLabel label = new JLabel(text);
    JTextField field = new JTextField(initial);
    panel.add(label);
    panel.add(field);
    panel.setAlignmentX(Box.CENTER_ALIGNMENT);
    return new Pair<>(panel, field);
  }

  public static JPanel horizontal(JPanel first, JPanel second) {
    JPanel combined = new JPanel();
    combined.setLayout(new BoxLayout(combined, BoxLayout.X_AXIS));
    combined.add(first);
    combined.add(second);
    combined.setAlignmentX(Box.CENTER_ALIGNMENT);
    combined.setSize(new Dimension(300, 20));
    combined.setPreferredSize(new Dimension(300, 20));
    return combined;
  }

  public App() {
    JFrame frame = new JFrame("Newton Fractal: Root");
    JPanel content = new JPanel();
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    JPanel settings = new JPanel();
    settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
    settings.setSize(new Dimension(300, 400));
    settings.setPreferredSize(new Dimension(300, 400));
    Pair<JPanel, JTextField> eq = makeField("Equation:", "z^2 + 1"),
        minX = makeField("Min X:", "-2.0"),
        maxX = makeField("Max X:", "2.0"),
        minY = makeField("Min Y:", "-2.0"),
        maxY = makeField("Max Y:", "2.0"),
        width = makeField("Width of Image:", "512"),
        height = makeField("Height of Image:", "512"),
        grayscale = makeField("Grayscale Depth:", "100"),
        maxiter = makeField("Max Iterations:", "1024");
    JButton button = new JButton("Draw");
    settings.add(eq.first);
    settings.add(horizontal(minX.first, maxX.first));
    settings.add(horizontal(minY.first, maxY.first));
    settings.add(horizontal(width.first, height.first));
    settings.add(horizontal(grayscale.first, maxiter.first));
    settings.add(button);
    button.setAlignmentX(Box.CENTER_ALIGNMENT);
    content.add(settings);
    Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
    content.setBorder(padding);
    frame.setContentPane(content);
    frame.pack();
    frame.setVisible(true);
    frame.setAlwaysOnTop(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        prepareImage(eq.second.getText(), minX.second.getText(), maxX.second.getText(), minY.second.getText(),
            maxY.second.getText(), width.second.getText(), height.second.getText(), grayscale.second.getText(),
            maxiter.second.getText());
      }
    });
  }

  public static Thread setTimeout(Runnable runnable, int delay) {
    Thread t = new Thread(() -> {
      try {
        Thread.sleep(delay);
        runnable.run();
      } catch (Exception e) {
        System.err.println(e);
      }
    });
    t.start();
    return t;
  }

  public static void prepareImage(String eq_s, String minX_s, String maxX_s, String minY_s,
      String maxY_s, String width_s, String height_s, String grayscale_s, String maxiter_s) {
    Equation eq = Equation.parse(eq_s).simplify();
    Function<Complex, Complex> f = eq.f();
    Function<Complex, Complex> df = eq.derivative().simplify().f();
    double minX = Double.valueOf(minX_s);
    double maxX = Double.valueOf(maxX_s);
    double minY = Double.valueOf(minY_s);
    double maxY = Double.valueOf(maxY_s);
    double width = Double.valueOf(width_s);
    double height = Double.valueOf(height_s);
    double grayscale = Double.valueOf(grayscale_s);
    double maxiter = Double.valueOf(maxiter_s);
    double incrementX = (maxX - minX) / width;
    double incrementY = (maxY - minY) / height;
    Newton newton = new Newton(f, df, (int) width, 1024, minX, maxX, minY, maxY);
    JFrame imageFrame = new JFrame("Newton Fractal: " + eq_s);
    ImageComponent image = new ImageComponent((int) width, (int) height);
    imageFrame.setContentPane(image);
    imageFrame.setAlwaysOnTop(true);
    imageFrame.setAutoRequestFocus(true);
    imageFrame.setResizable(false);
    imageFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    imageFrame.setVisible(true);
    imageFrame.pack(); // resize
    // allow time for resize
    Thread t = setTimeout(
        () -> drawImage(eq_s, image, newton, (int) width, (int) height, minX, minY, incrementX, incrementY, grayscale,
            maxiter),
        100);
    imageFrame.addWindowListener(new WindowListener() {

      @Override
      public void windowOpened(WindowEvent e) {
      }

      @Override
      public void windowClosing(WindowEvent e) {
        t.interrupt();
      }

      @Override
      public void windowClosed(WindowEvent e) {
      }

      @Override
      public void windowIconified(WindowEvent e) {
      }

      @Override
      public void windowDeiconified(WindowEvent e) {
      }

      @Override
      public void windowActivated(WindowEvent e) {
      }

      @Override
      public void windowDeactivated(WindowEvent e) {
      }

    });
  }

  public static void drawImage(String eq, ImageComponent image, Newton newton, int width, int height, double minX,
      double minY, double incrementX, double incrementY, double grayscale, double maxiter) {

    Graphics2D g2d = (Graphics2D) image.getGraphics();
    image.setIgnoreRepaint(true);

    ArrayList<Complex> list = new ArrayList<>();
    ArrayList<Pair<Integer, Integer>> pairs = new ArrayList<>();

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        Complex pt = new Complex(minX + incrementX * x, minY + incrementY * y);
        Pair<Complex, Integer> data = newton.newton(pt);
        int idx = list.indexOf(data.first);
        if (idx == -1) {
          if (data.second != (int) maxiter) {
            idx = list.size();
            list.add(data.first);
          }
        }
        pairs.add(new Pair<>(idx, data.second));
        float brightness = (float) Math.pow(1d - ((double) data.second / maxiter), grayscale);
        Color color = Color.getHSBColor(0, 0, brightness);
        g2d.setColor(color);
        g2d.fillRect(x, y, 1, 1);
      }
    }

    int n_hues = list.size();
    float[] hues = new float[n_hues];
    for (int i = 0; i < n_hues; i++) {
      float hue = (float) i / (float) n_hues;
      hues[i] = hue;
    }

    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d2 = (Graphics2D) bi.getGraphics();
    int i = 0;
    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++, i++) {
        Pair<Integer, Integer> pair = pairs.get(i);
        int hue_i = pair.first, iter = pair.second;
        float hue = hue_i == -1 ? 0 : hues[hue_i];
        float saturation = 1;
        float brightness = (float) Math.pow(1d - ((double) iter / 1024d), 100d);
        Color color = Color.getHSBColor(hue, saturation, brightness);
        g2d.setColor(color);
        g2d2.setColor(color);
        g2d.fillRect(x, y, 1, 1);
        g2d2.fillRect(x, y, 1, 1);
      }
    }
    image.addMouseListener(new MouseListener() {

      @Override
      public void mouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON3) {
          return;
        }
        int result = JOptionPane.showConfirmDialog(image, "Save " + eq + "?");
        if (result == JOptionPane.OK_OPTION) {
          JFileChooser fileChooser = new JFileChooser();
          fileChooser.setFileFilter(new FileNameExtensionFilter("Image", "png", "jpg"));
          fileChooser.setSelectedFile(new File("./" + eq + ".png"));
          fileChooser.setDialogTitle("Choose where to save the file");
          int userSelection = fileChooser.showSaveDialog(image);
          if (userSelection == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String extension;
            if (file.getName().endsWith("png")) {
              extension = "png";
            } else if (file.getName().endsWith("jpg")) {
              extension = "jpg";
            } else {
              extension = null;
              JOptionPane.showMessageDialog(image, "Incorrect file extension; must be png or jpg", "Bad file",
                  JOptionPane.ERROR_MESSAGE);
            }
            if (extension != null) {
              try {
                ImageIO.write(bi, extension, file);
                JOptionPane.showMessageDialog(image, "Image successfully saved");
              } catch (IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(image, "Sorry, there was an error saving the image", "Unknown error",
                    JOptionPane.ERROR_MESSAGE);
              }
            }
          }
        }
      }

      @Override
      public void mousePressed(MouseEvent e) {
      }

      @Override
      public void mouseReleased(MouseEvent e) {
      }

      @Override
      public void mouseEntered(MouseEvent e) {
      }

      @Override
      public void mouseExited(MouseEvent e) {
      }

    });
  }

  public static void main(String[] args) {
    new App();
  }
}
