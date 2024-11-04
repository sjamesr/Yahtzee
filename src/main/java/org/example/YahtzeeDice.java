package org.example;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class YahtzeeDice {
    private final Random random;
    private final int[] dice;
    private final boolean[] held;

    /**
     * Returns new, rolled Yahtzee dice, none being held.
     */
    public YahtzeeDice() {
        random = new Random();
        dice = new int[5];
        held = new boolean[5];
        roll();
    }

    /**
     * Returns new Yahtzee dice with the given values, for testing.
     */
    public YahtzeeDice(int d1, int d2, int d3, int d4, int d5) {
        random = new Random();
        dice = new int[] { d1, d2, d3, d4, d5 };
        held = new boolean[5];
    }

    public int getDie(int index) {
        return dice[index];
    }

    public void setHeld(int index, boolean isHeld) {
        held[index] = isHeld;
    }

    public boolean isHeld(int index) {
        return held[index];
    }

    public void roll() {
        for (int i = 0; i < dice.length; i++) {
            if (!held[i]) {
                dice[i] = random.nextInt(1, 7);
            }
        }
    }

    public void clearHeld() {
        Arrays.fill(held, false);
    }

    public List<Integer> getDice() {
        return Arrays.stream(dice).boxed().toList();
    }

    public void setDice(List<Integer> newDice) {
        int i = 0;
        for (int d : newDice) {
            if (i >= dice.length) {
                return;
            }

            dice[i++] = d;
        }
    }
}
