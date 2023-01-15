import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.Function;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

  enum Type {
    STR,
    CONSTANT, VAR,
    POW,
    MUL, DIV,
    ADD, SUB,
    COS, SIN, LN
  }

  public String token = null;
  private Type type = Type.STR;
  private Complex data = new Complex(0d);
  private Equation[] args = new Equation[0];

  // TODO trig^2(x) => (trig(x))^2
  // TODO trig^-1(x) => arctrig(x)
  private static ArrayList<Equation> tokenise(String eq) {
    Pattern stringPattern = Pattern.compile("[z*/+\\-^]|\\d+|[a-z]+", Pattern.CASE_INSENSITIVE);
    Matcher m = stringPattern.matcher(eq);
    ArrayList<MatchResult> matches = (ArrayList<MatchResult>) m.results().collect(Collectors.toList());
    ArrayList<Equation> tokens = new ArrayList<>();
    for (int i = 0; i < matches.size(); i++) {
      String token = matches.get(i).group();
      tokens.add(new Equation(token));
    }
    return tokens;
  }

  private Equation(String token) {
    this.type = Type.STR;
    this.token = token;
  }

  private Equation(Complex data) {
    this.type = Type.CONSTANT;
    this.data = data;
  }

  private Equation(Type type, Equation... args) {
    this.type = type;
    this.args = args;
  }

  private static Equation reduce(ArrayList<Equation> tokens) {
    // constant/var
    for (int i = 0; i < tokens.size(); i++) {
      Equation eq = tokens.get(i);
      if (eq.token == null)
        continue;
      if (eq.token.equals("z")) {
        tokens.remove(i);
        tokens.add(i, new Equation(Type.VAR));
      } else if (eq.token.matches("\\d+(\\.\\d+)?")) {
        tokens.remove(i);
        tokens.add(i, new Equation(Complex.parse(eq.token)));
      }
    }
    // brackets
    Stack<Integer> stack = new Stack<>();
    for (int i = 0; i < tokens.size(); i++) {
      Equation eq = tokens.get(i);
      if (eq.token == null)
        continue;
      if (eq.token == "(") {
        stack.add(i);
      } else if (eq.token == ")") {
        int start = stack.pop();
        int end = i;
        ArrayList<Equation> inBrackets = new ArrayList<>();
        tokens.remove(start);
        for (int j = start + 1; j < end; j++) {
          Equation next = tokens.remove(start);
          inBrackets.add(next);
        }
        Equation simple = reduce(inBrackets);
        tokens.add(start, simple);
      }
    }
    // powers
    for (int i = 0; i < tokens.size(); i++) {
      Equation eq = tokens.get(i);
      if (eq.token == null)
        continue;
      if (eq.token.equals("^")) {
        Equation base = tokens.remove(i - 1);
        tokens.remove(i - 1);
        Equation power = tokens.remove(i - 1);
        Equation simple = new Equation(Type.POW, base, power);
        tokens.add(i - 1, simple);
        i--;
      }
    }
    // functions
    for (int i = 0; i < tokens.size(); i++) {
      Equation eq = tokens.get(i);
      if (eq.token == null)
        continue;
      if (!"*/+-".contains(eq.token) && eq.token.matches("\\D+")) {
        Equation f = tokens.remove(i);
        Equation arg = tokens.remove(i);
        Type type;
        if (f.token.equals("sin"))
          type = Type.SIN;
        else if (f.token.equals("cos"))
          type = Type.COS;
        else if (f.token.equals("ln"))
          type = Type.LN;
        else
          throw new RuntimeException("Unknown function: " + f.token);
        Equation simple = new Equation(type, arg);
        tokens.add(i, simple);
      }
    }
    // TODO unary add/sub
    // multiply/divide
    for (int i = 0; i < tokens.size(); i++) {
      Equation eq = tokens.get(i);
      if (eq.token == null)
        continue;
      if (eq.token.equals("*") || eq.token.equals("/")) {
        Equation base = tokens.remove(i - 1);
        tokens.remove(i - 1);
        Equation power = tokens.remove(i - 1);
        Equation simple = new Equation(eq.token.equals("*") ? Type.MUL : Type.DIV, base, power);
        tokens.add(i - 1, simple);
        i--;
      }
    }
    // add/sub
    for (int i = 0; i < tokens.size(); i++) {
      Equation eq = tokens.get(i);
      if (eq.token == null)
        continue;
      if (eq.token.equals("+") || eq.token.equals("-")) {
        Equation base = tokens.remove(i - 1);
        tokens.remove(i - 1);
        Equation power = tokens.remove(i - 1);
        Equation simple = new Equation(eq.token.equals("+") ? Type.ADD : Type.SUB, base, power);
        tokens.add(i - 1, simple);
        i--;
      }
    }
    return tokens.get(0);
  }

  public static Equation parse(String eq) {
    ArrayList<Equation> tokens = tokenise(eq);
    return reduce(tokens);
  }

  public String toString() {
    if (type == Type.STR) {
      return "Equation(" + token + ")";
    } else if (type == Type.CONSTANT) {
      return "Equation(" + data + ")";
    } else if (type == Type.VAR) {
      return "Equation(VAR)";
    } else {
      String arString = Arrays.toString(args);
      return "Equation(" + type + "," + arString + ")";
    }
  }

  public Complex apply(Complex z) {
    List<Complex> temp = Arrays.stream(args)
        .collect(Collectors.mapping((Equation eq) -> eq.apply(z), Collectors.toList()));
    ArrayList<Complex> zs = (ArrayList<Complex>) temp;
    switch (type) {
      case VAR:
        return z;
      case CONSTANT:
        return data;
      case POW:
        return zs.get(0).pow(zs.get(1));
      case MUL:
        return zs.get(0).multiply(zs.get(1));
      case DIV:
        return zs.get(0).divide(zs.get(1));
      case ADD:
        return zs.get(0).add(zs.get(1));
      case SUB:
        return zs.get(0).sub(zs.get(1));
      case COS:
        // TODO cosine function
        return null;
      case SIN:
        // TODO sine function
        return null;
      case LN:
        return z.ln();
      default:
        throw new RuntimeException("Not yet implemented " + type);
    }
  }

  public Function<Complex, Complex> f() {
    return this::apply;
  }

  public Equation derivative() {
    List<Equation> temp = Arrays.stream(args)
        .collect(Collectors.mapping((Equation eq) -> eq.derivative(), Collectors.toList()));
    ArrayList<Equation> ds = (ArrayList<Equation>) temp;
    switch (type) {
      case VAR:
        // d/dz (z) = 1
        return new Equation(new Complex(1d));
      case CONSTANT:
        // d/dz (a) = 0
        return new Equation(new Complex(0d));
      case POW: {
        // d/dz (f(z)^g(z)) = (f(z)^g(z)) * ((g'(x)*ln(f(x))) + g(x)*f'(x)/f(x))
        Equation first = this;
        Equation second_first = new Equation(Type.MUL, ds.get(1), new Equation(Type.LN, args[0]));
        Equation second_second_second = new Equation(Type.DIV, ds.get(0), args[0]);
        Equation second_second = new Equation(Type.MUL, args[1], second_second_second);
        Equation second = new Equation(Type.ADD, second_first, second_second);
        Equation overall = new Equation(Type.MUL, first, second);
        return overall;
      }
      case MUL: {
        // d/dz (f(z)*g(z)) = f(z)*g'(z) + f'(z)*g(z)
        Equation first = new Equation(Type.MUL, args[0], ds.get(1));
        Equation second = new Equation(Type.MUL, ds.get(0), args[1]);
        Equation overall = new Equation(Type.ADD, first, second);
        return overall;
      }
      case DIV: {
        // d/dz (f(z)/g(z)) = (f'(z)*g(z) - f(z)*g'(z)) / (g*g)
        Equation first_first = new Equation(Type.MUL, ds.get(0), args[1]);
        Equation first_second = new Equation(Type.MUL, args[0], ds.get(1));
        Equation first = new Equation(Type.SUB, first_first, first_second);
        Equation second = new Equation(Type.MUL, args[1], args[1]);
        Equation overall = new Equation(Type.DIV, first, second);
        return overall;
      }
      case ADD:
        // d/dz (f(z)+g(z)) = f'(z) + g'(z)
        return new Equation(Type.ADD, ds.get(0), ds.get(1));
      case SUB:
        return new Equation(Type.SUB, ds.get(0), ds.get(1));
      case COS:
        // TODO cosine function
        return null;
      case SIN:
        // TODO sine function
        return null;
      case LN:
        // TODO ln function
        return null;
      default:
        throw new RuntimeException("Not yet implemented " + type);
    }
  }

  public static void main(String[] args) {
    Equation poly = Equation.parse("3*z^2+2*z");
    System.out.println(poly.toString());
    System.out.println(poly.f().apply(new Complex(3d, 1d)));
    System.out.println(poly.derivative());
    System.out.println(poly.derivative().f().apply(new Complex(3d)));
  }
}
