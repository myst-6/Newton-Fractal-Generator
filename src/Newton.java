import java.util.function.Function;

public class Newton {
  public Function<Complex, Complex> f, df;
  int width, maxiter;
  double minX, maxX, minY, maxY;

  public Newton(Function<Complex, Complex> f, Function<Complex, Complex> df, int width, int maxiter, double minX,
      double maxX, double minY, double maxY) {
    this.f = f;
    this.df = df;
    this.width = width;
    this.maxiter = maxiter;
    this.minX = minX;
    this.maxX = maxX;
    this.minY = minY;
    this.maxY = maxY;
  }

  public Pair<Complex, Integer> newton(Complex x) {
        int i = 0;
    
        for (; i < maxiter; i++) {
          Complex xTemp = x;
          x = x.sub(f.apply(x).divide(df.apply(x)));
          if (Double.isNaN(x.real) || Double.isNaN(x.imag)) {
            return new Pair<>(x, maxiter);
          }
          System.out.println(x);
          if (xTemp.equals(x))
            break;
        }
    
        return new Pair<>(x, i);
      }

}
