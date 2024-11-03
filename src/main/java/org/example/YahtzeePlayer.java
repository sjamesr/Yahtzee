package org.example;

public class YahtzeePlayer {
    private final String name;

    public YahtzeePlayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
