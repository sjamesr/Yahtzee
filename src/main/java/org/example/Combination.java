package org.example;

import java.util.function.Supplier;

public class Combination {
    private final String name;
    private final Supplier<Integer> scoreCalculator;

    public Combination(String name, Supplier<Integer> scoreCalculator) {
        this.name = name;
        this.scoreCalculator = scoreCalculator;
    }

    public String getName() {
        return name;
    }

    public int score() {
        return scoreCalculator.get();
    }

    @Override
    public String toString() {
        return getName();
    }
}
