public final class Complex {
  private static Double TOL = 1e-6;

  public double real, imag;

  public static Complex parse(String z) {
    if (z.endsWith("i")) {
      String x = z.substring(0, z.length() - 1);
      if (x.length() == 0) {
        return new Complex(0d, 1d);
      } else {
        return new Complex(0d, Double.parseDouble(x));
      }
    } else {
      String x = z;
      return new Complex(Double.parseDouble(x), 0d);
    }
  }

  public static Complex fromPolar(double abs, double arg) {
    return new Complex(abs * Math.cos(arg), abs * Math.sin(arg));
  }

  public Complex(double real, double imag) {
    this.real = real;
    this.imag = imag;
  }

  public Complex(double real) {
    this(real, 0);
  }

  public Double abs() {
    return Math.sqrt(this.abs2());
  }

  public Double abs2() {
    return this.real * this.real + this.imag * this.imag;
  }

  // (a + bi)(c + di) = ac - bd + i(ad + bc)
  public Complex multiply(Complex other) {
    return new Complex(this.real * other.real - this.imag * other.imag,
        this.real * other.imag + this.imag * other.real);
  }

  public Complex multiply(Double other) {
    return multiply(new Complex(other));
  }

  // "real-ify" (like rationalise) the denominator of (a + bi) / (c + di)
  // by doing ((a + bi) / (c + di)) * ((c - di) / (c - di))
  // = ((ac + bd) / (c^2 +d^2)) + i((bc - ad) / (c^2 + d^2))
  // c^2 + d^2 is the abs2 of other
  public Complex divide(Complex other) {
    return new Complex((this.real * other.real + this.imag * other.imag) / other.abs2(),
        (this.imag * other.real - this.real * other.imag) / other.abs2());
  }

  public Complex add(Complex other) {
    return new Complex(this.real + other.real, this.imag + other.imag);
  }

  public Complex sub(Complex other) {
    return new Complex(this.real - other.real, this.imag - other.imag);
  }

  public Complex sub(Double other) {
    return sub(new Complex(other));
  }

  public double arg() {
    return Math.atan2(this.imag, this.real);
  }

  public Complex ln() {
    return new Complex(Math.log(abs()), arg());
  }

  public Complex pow(Complex z) {
    if (z.imag == 0) return pow(z.real);
    double abs = Math.pow(abs(), z.real) * Math.exp(-z.imag * arg());
    double arg = z.real * arg() + z.imag * Math.log(abs());
    return Complex.fromPolar(abs, arg);
  }

  public Complex pow(double x) {
    if (x % 1 == 0) return pow((int) x);
    double abs = Math.pow(abs(), x);
    double arg = x * arg();
    return Complex.fromPolar(abs, arg);
  } 

  public Complex pow(int x) {
    Complex out = new Complex(1);

    while (x-- > 0) {
      out = out.multiply(this);
    }

    return out;
  }

  public Complex sin() {
    return new Complex(Math.sin(real) * Math.cosh(imag), Math.cos(real) * Math.sinh(real));
  }

  public Complex cos() {
    return new Complex(Math.cos(real) * Math.cosh(imag), -Math.sin(real) * Math.sinh(real));
  }

  @Override
  public String toString() {
    return String.format("(%.2f + %.2fi)", real, imag);
  }

  @Override
  public int hashCode() {
    int hash = 7;

    hash = hash * 31 + Double.hashCode(real);
    hash = hash * 31 + Double.hashCode(imag);

    return hash;
  }

  public boolean equals(Complex other) {
    return other.sub(this).abs() <= TOL;
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof Complex) && this.equals((Complex) other);
  }

  public static void main(String[] args) {
    System.out.println(new Complex(1, 1).pow(new Complex(1, 1)));
  }
}
