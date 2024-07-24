package com.github.istin.dmtools.presale.model;

public class Estimation {
    public int optimistic;
    public int pessimistic;
    public int mostLikely;

    public Estimation(int optimistic, int pessimistic, int mostLikely) {
        this.optimistic = optimistic;
        this.pessimistic = pessimistic;
        this.mostLikely = mostLikely;
    }
}
