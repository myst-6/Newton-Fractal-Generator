public final class Complex {
    private static Double TOL = 1e-6;

    public double real, imag;

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

    public Complex pow(int x) {
        Complex out = new Complex(1);

        while (x-- > 0) {
            out = out.multiply(this);
        }

        return out;
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
}
