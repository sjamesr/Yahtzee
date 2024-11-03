package org.example;

public class Combination {
    private final String name;
    private final ScoreCalculator scoreCalculator;

    public Combination(String name, ScoreCalculator calculator) {
        this.name = name;
        this.scoreCalculator = calculator;
    }

    public String getName() {
        return name;
    }

    public int score(YahtzeeDice dice) {
        return scoreCalculator.getScore(dice);
    }

    public interface ScoreCalculator {
        int getScore(YahtzeeDice dice);
    }

    @Override
    public String toString() {
        return getName();
    }
}
