import javax.swing.JComponent;

public class ImageComponent extends JComponent {
    int width, height;

    public ImageComponent(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}
