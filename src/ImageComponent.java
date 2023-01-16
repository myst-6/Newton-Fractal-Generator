import java.awt.Dimension;

import javax.swing.JPanel;

public class ImageComponent extends JPanel {
  int width, height;

  public ImageComponent(int width, int height) {
    this.changeSize(width, height);
  }

  // i didn't realise 'resize' was already a function on JPanel
  // so it's not 'changeSize'
  public void changeSize(int width, int height) {
    this.width = width;
    this.height = height;
    setSize(new Dimension(width, height));
    setPreferredSize(new Dimension(width, height));
  }
}
