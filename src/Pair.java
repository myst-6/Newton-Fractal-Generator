public class Pair<T, U> {
    T first;
    U second;

    public Pair(T first, U second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public String toString() {
        return String.valueOf(this.first) + "," + String.valueOf(this.second);
    }
}
