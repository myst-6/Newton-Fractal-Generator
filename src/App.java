import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

public final class App {
  public static final int DEFAULT_WIDTH = 512;

  public static Pair<JPanel, JTextField> makeField(String text, String initial) {
    JPanel panel = new JPanel();
    panel.setSize(new Dimension(100, 50));
    panel.setPreferredSize(new Dimension(100, 50));
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
    combined.setSize(new Dimension(200, 50));
    combined.setPreferredSize(new Dimension(200, 50));
    return combined;
  }

  public static void main(String[] args) {
    Dimension size = new Dimension(1024, 1024);
    JFrame frame = new JFrame();
    frame.setSize(size);
    JPanel content = new JPanel();
    content.setSize(size);
    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
    JPanel settings = new JPanel();
    settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
    settings.setSize(new Dimension(200, 400));
    settings.setPreferredSize(new Dimension(200, 400));
    Pair<JPanel, JTextField> eq = makeField("Equation:", "z^2 + 1"),
        minX = makeField("Min X:", "-2.0"),
        maxX = makeField("Max X:", "2.0"),
        minY = makeField("Min Y:", "-2.0"),
        maxY = makeField("Max Y:", "2.0"),
        width = makeField("Width of Image:", String.valueOf(DEFAULT_WIDTH));
    JButton button = new JButton("Draw");
    settings.add(eq.first);
    settings.add(horizontal(minX.first, maxX.first));
    settings.add(horizontal(minY.first, maxY.first));
    settings.add(width.first);
    settings.add(button);
    button.setAlignmentX(Box.CENTER_ALIGNMENT);
    content.add(settings);
    ImageComponent image = new ImageComponent(DEFAULT_WIDTH, DEFAULT_WIDTH);
    image.setAlignmentX(Box.CENTER_ALIGNMENT);
    image.setBorder(new LineBorder(Color.BLACK, 2, true));
    content.add(image);
    frame.setContentPane(content);
    frame.setVisible(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    button.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        prepareImage(image, eq.second.getText(), minX.second.getText(), maxX.second.getText(), minY.second.getText(),
            maxY.second.getText(), width.second.getText());
      }
    });
  }

  public static void setTimeout(Runnable runnable, int delay) {
    new Thread(() -> {
      try {
        Thread.sleep(delay);
        runnable.run();
      } catch (Exception e) {
        System.err.println(e);
      }
    }).start();
  }

  public static void prepareImage(ImageComponent image, String eq_s, String minX_s, String maxX_s, String minY_s,
      String maxY_s, String width_s) {
    Equation eq = Equation.parse(eq_s);
    Function<Complex, Complex> f = eq.f();
    Function<Complex, Complex> df = eq.derivative().f();
    double minX = Double.valueOf(minX_s);
    double maxX = Double.valueOf(maxX_s);
    double minY = Double.valueOf(minY_s);
    double maxY = Double.valueOf(maxY_s);
    double width = Double.valueOf(width_s);
    double aspect = (maxY - minY) / (maxX - minX);
    double height = Math.floor(width * aspect);
    System.out.println(minX + "," + maxX + "," + minY + "," + maxY + "," + width + "," + aspect + "," + height);
    image.changeSize((int) width, (int) height);
    double incrementX = (maxX - minX) / width;
    double incrementY = (maxY - minY) / height;
    Newton newton = new Newton(f, df, (int) width, 1024, minX, maxX, minY, maxY);
    // allow time for the component to resize first (~100ms)
    setTimeout(() -> drawImage(image, newton, (int) width, (int) height, minX, minY, incrementX, incrementY), 100);
  }

  public static void drawImage(ImageComponent image, Newton newton, int width, int height, double minX,
      double minY, double incrementX, double incrementY) {

    Graphics2D g2d = (Graphics2D) image.getGraphics();

    ArrayList<Complex> list = new ArrayList<>();
    ArrayList<Pair<Integer, Integer>> pairs = new ArrayList<>();

    for (int x = 0; x < width; x++) {
      for (int y = 0; y < height; y++) {
        Complex pt = new Complex(minX + incrementX * x, minY + incrementY * y);
        Pair<Complex, Integer> data = newton.newton(pt);
        int idx = list.indexOf(data.first);
        if (idx == -1) {
          if (data.second != 1024) {
            idx = list.size();
            list.add(data.first);
          }
        }
        pairs.add(new Pair<>(idx, data.second));
      }
    }

    int n_hues = list.size();
    float[] hues = new float[n_hues];
    for (int i = 0; i < n_hues; i++) {
      float hue = (float) i / (float) n_hues;
      hues[i] = hue;
    }

    System.out.println(Arrays.toString(hues));

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
        g2d.fillRect(x, y, 1, 1);
      }
    }
  }
}
