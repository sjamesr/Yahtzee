package org.example;

import com.google.common.collect.*;

import java.util.*;
import java.util.stream.Collectors;

public class YahtzeeGame {
    private final List<YahtzeePlayer> players;
    private final YahtzeeDice dice;
    private int whoseTurn;
    private int rollsRemaining = 2;
    private final List<Combination> combinations;
    private final List<Map<Combination, Integer>> movesMade;
    private final List<GameStateListener> listeners = new ArrayList<>();

    public YahtzeeGame(List<YahtzeePlayer> players) {
        if (players.isEmpty()) {
            throw new IllegalArgumentException("Yahtzee games must have at least one player");
        }

        this.combinations = new ArrayList<>();
        combinations.add(new Combination("Chance", d -> d.getDice().stream().reduce(0, Integer::sum)));
        combinations.add(new Combination("Aces", d -> d.getDice().stream().mapToInt(v -> v == 1 ? 1 : 0).sum()));
        combinations.add(new Combination("Twos", d -> d.getDice().stream().mapToInt(v -> v == 2 ? 2 : 0).sum()));
        combinations.add(new Combination("Threes", d -> d.getDice().stream().mapToInt(v -> v == 3 ? 3 : 0).sum()));
        combinations.add(new Combination("Fours", d -> d.getDice().stream().mapToInt(v -> v == 4 ? 4 : 0).sum()));
        combinations.add(new Combination("Fives", d -> d.getDice().stream().mapToInt(v -> v == 5 ? 5 : 0).sum()));
        combinations.add(new Combination("Sixes", d -> d.getDice().stream().mapToInt(v -> v == 6 ? 6 : 0).sum()));

        combinations.add(new Combination("Full house", d -> {
            var v = TreeMultiset.create(d.getDice());
            return v.entrySet().stream().mapToInt(Multiset.Entry::getCount).boxed().collect(Collectors.toSet()).equals(ImmutableSet.of(2, 3)) ? 25 : 0;
        }));

        combinations.add(new Combination("Three of a kind", d -> {
            var v = TreeMultiset.create(d.getDice());
            // If any element is repeated 3 times, the value is the sum of all the elements
            return v.stream().anyMatch(x -> v.count(x) >= 3) ? d.getDice().stream().reduce(0, Integer::sum) : 0;
        }));

        combinations.add(new Combination("Four of a kind", d -> {
            var v = TreeMultiset.create(d.getDice());
            return v.stream().anyMatch(x -> v.count(x) >= 4) ? d.getDice().stream().reduce(0, Integer::sum) : 0;
        }));

        combinations.add(new Combination("Small straight",
                d -> longestSequenceLength(d.getDice().stream().sorted().toList()) >= 4 ? 30 : 0));

        combinations.add(new Combination("Large straight",
                d -> longestSequenceLength(d.getDice().stream().sorted().toList()) == 5 ? 40 : 0));

        combinations.add(new Combination("Yahtzee", d -> d.getDice().stream().allMatch(x -> x == d.getDie(0)) ? 50 : 0));

        // take a copy
        this.players = new ArrayList<>(players);
        this.dice = new YahtzeeDice();
        this.whoseTurn = 0;
        this.movesMade = new ArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            movesMade.add(new HashMap<>());
        }
    }

    public void addGameStateListener(GameStateListener l) {
        listeners.add(l);
    }

    public List<YahtzeePlayer> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    /**
     * Tells the game that the current player has selected the given combination.
     */
    public void makeMove(Combination combination) {
        Map<Combination, Integer> playerMoves = movesMade.get(whoseTurn);
        if (playerMoves.containsKey(combination)) {
            throw new IllegalStateException(players.get(whoseTurn) + " has already played " + combination.getName());
        }

        playerMoves.put(combination, combination.score(dice));
        whoseTurn = (whoseTurn + 1) % players.size();
        dice.clearHeld();
        dice.roll();
        rollsRemaining = 2;

        fireGameStateChanged();
    }

    /**
     * Tells the game that the current player has held the selected die.
     */
    public void setDieHeld(int die, boolean held) {
        dice.setHeld(die, held);
        fireGameStateChanged();
    }

    /**
     * Tells the game that the current player is rolling the dice.
     */
    public void rollDice() {
        if (rollsRemaining == 0) {
            throw new IllegalStateException("Player is out of turns");
        }

        dice.roll();
        rollsRemaining--;
        fireGameStateChanged();
    }

    public Map<Combination, Integer> getPlayerMoves(int player) {
        return Collections.unmodifiableMap(movesMade.get(player));
    }

    public int getWhoseTurn() {
        return whoseTurn;
    }

    public int getRollsRemaining() {
        return rollsRemaining;
    }

    public List<Combination> getCombinations() {
        return Collections.unmodifiableList(combinations);
    }

    public int getPlayerScore(int player) {
        return movesMade.get(player).values().stream().reduce(0, Integer::sum);
    }

    public YahtzeeDice getDice() {
        return dice;
    }

    private void fireGameStateChanged() {
        for (GameStateListener l : listeners) {
            l.gameStateChanged();
        }
    }

    public interface GameStateListener {
        /**
         * Informs the listener that the game state has changed. A fancier version might tell the listener exactly what
         * has changed (e.g. dice rolled, move made, etc.).
         */
        void gameStateChanged();
    }

    private static int longestSequenceLength(List<Integer> sequence) {
        int length = 1;
        int max = 1;
        for (int i = 1; i < sequence.size(); i++) {
            int cur = sequence.get(i);
            int last = sequence.get(i - 1);
            if (cur == last + 1) {
                length++;
                if (length > max) {
                    max = length;
                }
            } else if (cur != last) {
                length = 1;
            }
        }

        return max;
    }
}
