import java.util.function.Function;

public class Equation {
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
}
