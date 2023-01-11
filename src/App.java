import java.util.function.Function;
import javax.imageio.ImageIO;
import java.util.ArrayList;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public final class App {
  private static final class Pixel extends Pair<Integer, Double> {
    public Pixel(Integer first, Double second) {
      super(first, second);
    }
  }

  // changeable
  private static int SIZE = 2600;
  private static double MAX_ITER = 1024;
  private static Point diff = new Point(2d, 2d);
  private static Point centre = new Point(0d, 0d);

  // calculated (unchangeable)
  private static Point min = centre.sub(diff);
  private static Point max = centre.add(diff);

  public static Function<Complex, Complex> poly(Double... coeffs) {
    return (n) -> {
      Complex out = new Complex(0, 0);

      for (int i = 0, j = coeffs.length - 1; i < coeffs.length; i++, j--) {
        out = out.add(n.pow(j).multiply(coeffs[i]));
      }

      return out;
    };
  }

  public static Double[] diff(Double... coeffs) {
    Double[] diffCoeffs = new Double[coeffs.length - 1];

    for (int i = 0, j = coeffs.length - 1; j > 0; i++, j--) {
      diffCoeffs[i] = coeffs[i] * j;
    }

    return diffCoeffs;
  }

  public static Pair<Complex, Double> newton(Function<Complex, Complex> f, Function<Complex, Complex> df, Complex x) {
    double i = 0;

    for (; i < MAX_ITER; i++) {
      Complex xTemp = x;
      x = x.sub(f.apply(x).divide(df.apply(x)));
      if (xTemp.equals(x))
        break;
    }

    return new Pair<>(x, i);
  }

  public static Complex sin(Complex z) {
    return new Complex(Math.sin(z.real) * Math.cosh(z.imag), Math.cos(z.real) * Math.sinh(z.imag));
  }

  public static Complex cos(Complex z) {
    return new Complex(Math.cos(z.real) * Math.cosh(z.imag), Math.sin(z.real) * Math.sinh(z.imag));
  }

  public static Complex sinh(Complex z) {
    return new Complex(Math.sinh(z.real) * Math.cos(z.imag), Math.cosh(z.real) * Math.sin(z.imag));
  }

  public static Complex cosh(Complex z) {
    return new Complex(Math.cosh(z.real) * Math.cos(z.imag), Math.sinh(z.real) * Math.sin(z.imag));
  }

  public static void main(String[] args) {
    // Double[] coeffs = new Double[] { 1d, 0d, -2d, 2d };
    Double[] coeffs = new Double[] { 0d, 7d, 7d, 5d, 3d, 8d, 3d, 7d, 8d, 7d, 9d };
    Function<Complex, Complex> f = poly(coeffs);
    Function<Complex, Complex> df = poly(diff(coeffs));

    ArrayList<Complex> roots = new ArrayList<Complex>();
    Pixel[][] pixels = new Pixel[SIZE][SIZE];

    for (int i = 0; i < SIZE; i++) {
      if (i % 16 == 0) {
        System.out.println(i);
      }

      double a = (max.second - min.second) * ((double) i / SIZE) + min.second;

      for (int j = 0; j < SIZE; j++) {
        double b = (max.first - min.first) * ((double) j / SIZE) + min.first;

        Pair<Complex, Double> res = newton(f, df, new Complex(a, b));
        if (res.second < MAX_ITER) {
          boolean found = false;
          int root = 0;

          for (; root < roots.size(); root++) {
            if (roots.get(root).equals(res.first)) {
              found = true;
              break;
            }
          }

          if (!found) {
            roots.add(res.first);
          }

          pixels[i][j] = new Pixel(root, (res.second / MAX_ITER));
        } else {
          pixels[i][j] = new Pixel(-1, 1d);
        }
      }
    }

    BufferedImage bi = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);

    for (int i = 0; i < SIZE; i++) {
      for (int j = 0; j < SIZE; j++) {
        Pixel pixel = pixels[i][j];
        int color = Color.HSBtoRGB(pixel.first.floatValue() / (float) roots.size(), 1f,
            (float) Math.pow(1 - pixel.second, 50));

        bi.setRGB(i, j, color);
      }
    }

    File file = new File("fractal_banner.png");

    try {
      ImageIO.write(bi, "png", file);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
