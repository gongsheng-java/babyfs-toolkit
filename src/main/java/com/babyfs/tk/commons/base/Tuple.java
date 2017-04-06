package com.babyfs.tk.commons.base;

/**
 * 三元组
 */
public class Tuple<FIRST, SECOND, THIRD> {
    private final FIRST first;
    private final SECOND second;
    private final THIRD third;

    public Tuple(FIRST first, SECOND second, THIRD third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public FIRST getFirst() {
        return first;
    }

    public SECOND getSecond() {
        return second;
    }

    public THIRD getThird() {
        return third;
    }

    public static <FIRST, SECOND, THIRD> Tuple<FIRST, SECOND, THIRD> of(FIRST first, SECOND second, THIRD third) {
        return new Tuple<FIRST, SECOND, THIRD>(first, second, third);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple tuple = (Tuple) o;

        if (first != null ? !first.equals(tuple.first) : tuple.first != null) return false;
        if (second != null ? !second.equals(tuple.second) : tuple.second != null) return false;
        if (third != null ? !third.equals(tuple.third) : tuple.third != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        result = 31 * result + (third != null ? third.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Tuple{" +
                "first=" + first +
                ", second=" + second +
                ", third=" + third +
                '}';
    }
}
