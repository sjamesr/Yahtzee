package org.example;

import com.google.common.collect.*;

import java.util.*;
import java.util.stream.Collectors;

public class YahtzeeGame {
    private final List<YahtzeePlayer> players;
    private final YahtzeeDice dice;
    private int whoseTurn;
    private int rollsRemaining = 2;
    private final List<Combination> upperCombinations;
    private final List<Combination> lowerCombinations;
    private final List<Map<Combination, Integer>> movesMade;
    private final List<GameStateListener> listeners = new ArrayList<>();
    private final int[] upperSectionScore;
    private final int[] lowerSectionScore;
    private final int[] bonusYahtzeeCount;
    private final Combination yahtzeeCombo;

    public YahtzeeGame(List<YahtzeePlayer> players) {
        if (players.isEmpty()) {
            throw new IllegalArgumentException("Yahtzee games must have at least one player");
        }
        this.dice = new YahtzeeDice();

        upperCombinations = new ArrayList<>();
        upperCombinations.add(new Combination("Aces", () -> dice.getDice().stream().mapToInt(v -> v == 1 ? 1 : 0).sum()));
        upperCombinations.add(new Combination("Twos", () -> dice.getDice().stream().mapToInt(v -> v == 2 ? 2 : 0).sum()));
        upperCombinations.add(new Combination("Threes", () -> dice.getDice().stream().mapToInt(v -> v == 3 ? 3 : 0).sum()));
        upperCombinations.add(new Combination("Fours", () -> dice.getDice().stream().mapToInt(v -> v == 4 ? 4 : 0).sum()));
        upperCombinations.add(new Combination("Fives", () -> dice.getDice().stream().mapToInt(v -> v == 5 ? 5 : 0).sum()));
        upperCombinations.add(new Combination("Sixes", () -> dice.getDice().stream().mapToInt(v -> v == 6 ? 6 : 0).sum()));

        lowerCombinations = new ArrayList<>();
        lowerCombinations.add(new Combination("Chance", () -> dice.getDice().stream().reduce(0, Integer::sum)));

        lowerCombinations.add(new Combination("Three of a kind", () -> {
            var v = TreeMultiset.create(dice.getDice());
            // If any element is repeated 3 times, the value is the sum of all the elements
            return v.stream().anyMatch(x -> v.count(x) >= 3) ? dice.getDice().stream().reduce(0, Integer::sum) : 0;
        }));

        lowerCombinations.add(new Combination("Four of a kind", () -> {
            var v = TreeMultiset.create(dice.getDice());
            return v.stream().anyMatch(x -> v.count(x) >= 4) ? dice.getDice().stream().reduce(0, Integer::sum) : 0;
        }));

        yahtzeeCombo = new Combination("Yahtzee", () -> isYahtzee() ? 50 : 0);

        lowerCombinations.add(new Combination("Full house", () -> {
            if (isJoker()) {
                return 25;
            }

            var v = TreeMultiset.create(dice.getDice());
            return v.entrySet().stream().mapToInt(Multiset.Entry::getCount)
                    .boxed().collect(Collectors.toSet()).equals(ImmutableSet.of(2, 3)) ? 25 : 0;
        }));

        lowerCombinations.add(new Combination("Small straight",
                () -> {
                    if (isJoker() || longestSequenceLength(dice.getDice().stream().sorted().toList()) >= 4) {
                        return 30;
                    }
                    return 0;
                }));

        lowerCombinations.add(new Combination("Large straight",
                () -> {
                    if (isJoker() || longestSequenceLength(dice.getDice().stream().sorted().toList()) == 5) {
                        return 40;
                    }
                    return 0;
                }));

        lowerCombinations.add(yahtzeeCombo);

        // take a copy
        this.players = new ArrayList<>(players);
        this.whoseTurn = 0;
        this.movesMade = new ArrayList<>(players.size());
        for (int i = 0; i < players.size(); i++) {
            movesMade.add(new HashMap<>());
        }

        this.upperSectionScore = new int[players.size()];
        this.lowerSectionScore = new int[players.size()];
        this.bonusYahtzeeCount = new int[players.size()];
    }

    public void addGameStateListener(GameStateListener l) {
        listeners.add(l);
    }

    public List<YahtzeePlayer> getPlayers() {
        return Collections.unmodifiableList(players);
    }

    public List<Combination> getUpperCombinations() {
        return Collections.unmodifiableList(upperCombinations);
    }

    public List<Combination> getLowerCombinations() {
        return Collections.unmodifiableList(lowerCombinations);
    }

    /**
     * Returns {@code true} if there is a Joker on the table. A Joker exists if:
     *
     * <ul>
     *   <li>the dice make a Yahtzee
     *   <li>the corresponding upper combination (e.g. Twos if the Yahtzee is of the number 2) has
     *       already been played
     *   <li>The Yahtzee combination has already been played and scored 50 points (not zero).
     * </ul>
     */
    public boolean isJoker() {
        if (!isYahtzee()) {
            return false;
        }

        int roll = dice.getDie(0);
        Combination upperCombination = upperCombinations.get(roll - 1);
        if (!movesMade.get(whoseTurn).containsKey(upperCombination)) {
            // The upper combination has not been played
            return false;
        }

      // Check if Yahtzee move has either not been played, or it was played and scored a 0.
      return movesMade.get(whoseTurn).getOrDefault(yahtzeeCombo, 0) != 0;
    }

    /**
     * Tells the game that the current player has selected the given combination.
     */
    public void makeMove(Combination combination) {
        Map<Combination, Integer> playerMoves = movesMade.get(whoseTurn);
        if (playerMoves.containsKey(combination)) {
            throw new IllegalStateException(players.get(whoseTurn) + " has already played " + combination.getName());
        }

        int score = combination.score();

        // Check for bonus Yahtzee.
        if (isYahtzee() && playerMoves.getOrDefault(yahtzeeCombo, 0) != 0) {
            bonusYahtzeeCount[whoseTurn]++;
        }

        playerMoves.put(combination, score);

        if (upperCombinations.contains(combination)) {
            upperSectionScore[whoseTurn] += score;
        } else if (lowerCombinations.contains(combination)) {
            lowerSectionScore[whoseTurn] += score;
        }

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


    public int getUpperSectionScore(int player) {
        return upperSectionScore[player];
    }

    public int getUpperSectionBonusScore(int player) {
        return upperSectionScore[player] >= 63 ? 35 : 0;
    }

    public int getLowerSectionScore(int player) {
        return lowerSectionScore[player];
    }

    public int getBonusYahtzeeCount(int player) {
        return bonusYahtzeeCount[player];
    }

    public int getBonusYahtzeeScore(int player) {
        return 100 * bonusYahtzeeCount[player];
    }

    public int getPlayerScore(int player) {
        int upper = getUpperSectionScore(player);
        int upperBonus = getUpperSectionBonusScore(player);
        int lower = getLowerSectionScore(player);
        int lowerBonus = getBonusYahtzeeScore(player);
        return upper + upperBonus + lower + lowerBonus;
    }

    public boolean isGameOver() {
        int moveCount = lowerCombinations.size() + upperCombinations.size();
        for (Map<Combination, Integer> playerMoves : movesMade) {
            if (playerMoves.size() != moveCount) {
                return false;
            }
        }

        return true;
    }

    public YahtzeeDice getDice() {
        return dice;
    }

    public void setDice(List<Integer> newDice) {
        dice.setDice(newDice);
    }

    public boolean isYahtzee() {
        return dice.getDice().stream().allMatch(x -> x == dice.getDie(0));
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
