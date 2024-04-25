package com.ugent.pidgeon.util;


/**
 * Class that represents a pair of objects
 * @param <S> First object type
 * @param <T> Second object type
 */
public class Pair<S, T> {
    private final S first;
    private final T second;

    public Pair(S first, T second) {
        this.first = first;
        this.second = second;
    }

    public S getFirst() {
        return first;
    }

    public T getSecond() {
        return second;
    }
}
