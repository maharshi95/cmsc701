package hw1.utils;

import java.util.Objects;

public class Interval {

    public int start; // inclusive
    public int end; // exclusive

    /**
     * Create an interval of integers from start (inclusive) to end (exclusive)
     * @param start
     * @param end
     */
    public Interval(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public String toString() {
        return "[" + start + ", " + end + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interval interval = (Interval) o;
        return start == interval.start && end == interval.end;
    }

    public Interval copy() {
        return new Interval(start, end);
    }

    public int size() {
        return end - start;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
