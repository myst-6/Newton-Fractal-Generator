public final class Point extends Pair<Double, Double> {
    public Point(Double first, Double second) {
        super(first, second);
    }

    public Point sub(Point other) {
        return new Point(this.first - other.first, this.second - other.second);
    }

    public Point add(Point other) {
        return new Point(this.first + other.first, this.second + other.second);
    }

    public Point multiply(double other) {
        return new Point(this.first * other, this.second * other);
    }

    public Point interpolate(Point other, double t) {
        return other.sub(this).multiply(t).add(this);
    }
}