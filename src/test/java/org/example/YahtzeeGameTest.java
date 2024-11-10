package org.example;

import com.google.common.collect.Streams;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class YahtzeeGameTest {

    @Test
    public void testRollDice() {
        YahtzeeGame game = new YahtzeeGame(List.of(new YahtzeePlayer("Patrick"), new YahtzeePlayer("James")));
        assertEquals(game.getPlayers().size(), 2);
        assertEquals(0, game.getPlayerScore(0));
        assertEquals(0, game.getPlayerScore(1));

        // Do a couple of rolls
        game.rollDice();
        assertEquals(1, game.getRollsRemaining());
        game.rollDice();
        assertEquals(0, game.getRollsRemaining());

        // Trying to roll again should throw, the player is out of turns
        assertThrows(IllegalStateException.class, game::rollDice);

        // Play the chance move
        Combination chanceCombo = comboByName(game, "Chance");
        assertNotNull(chanceCombo);

        int expectedScore = game.getDice().getDice().stream().reduce(0, Integer::sum);
        assertEquals(expectedScore, chanceCombo.score());

        // Make the move
        game.makeMove(chanceCombo);

        // Should have updated the player's score
        assertEquals(expectedScore, game.getPlayerScore(0));

        // Should have advanced the turn count
        assertEquals(1, game.getWhoseTurn());

        // Should have unheld all the die
        int numDice = game.getDice().getDice().size();
        for (int i = 0; i < numDice; i++) {
            assertFalse(game.getDice().isHeld(i));
        }

        // Making the move should have rolled the dice for the new player
        assertEquals(2, game.getRollsRemaining());
    }

    @Test
    public void testFullHouse() {
        YahtzeeGame game = new YahtzeeGame(List.of(new YahtzeePlayer("Patrick"), new YahtzeePlayer("James")));

        Combination fullHouse = comboByName(game, "Full house");
        assertNotNull(fullHouse);

        game.setDice(List.of(2, 1, 1, 2, 2));
        assertEquals(25, fullHouse.score());

        game.setDice(List.of(1, 1, 2, 1, 2));
        assertEquals(25, fullHouse.score());

        game.setDice(List.of(3, 1, 1, 2, 2));
        assertEquals(0, fullHouse.score());

        game.setDice(List.of(1, 1, 1, 1, 1));
        assertEquals(0, fullHouse.score());
    }

    @Test
    public void testYahtzeeJokerRules() {
        YahtzeeGame game = new YahtzeeGame(List.of(new YahtzeePlayer("Patrick")));

        game.setDice(List.of(6, 6, 6, 6, 6));

        {
            Combination yahtzee = comboByName(game, "Yahtzee");
            assertEquals(50, yahtzee.score());

            // Play the Yahtzee
            game.makeMove(yahtzee);
        }

        game.setDice(List.of(5, 5, 5, 5, 5));

        // We've already got a Yahtzee, but it can't be a Joker yet (5s haven't been played)
        {
            assertEquals(0, comboByName(game, "Full house").score());
            assertEquals(0, comboByName(game, "Small straight").score());
            assertEquals(0, comboByName(game, "Large straight").score());

            // Play the fives move
            game.makeMove(comboByName(game, "Fives"));
        }

        game.setDice(List.of(5, 5, 5, 5, 5));
        {
            // It's our lucky day, another Yahtzee of 5s, this time it can act as Joker, because
            // the 5s move has already been played.
            assertEquals(25, comboByName(game, "Full house").score());
            assertEquals(30, comboByName(game, "Small straight").score());
            assertEquals(40, comboByName(game, "Large straight").score());

            // Play the full house move
            game.makeMove(comboByName(game, "Full house"));
        }

        // Our score should be 50 (original Yahtzee) + 25 (fives move) + 100 (bonus Yahtzee) +
        // 25 (full house) + 100 (second bonus Yahtzee) = 300
        assertEquals(300, game.getPlayerScore(0));
    }

    @Test
    public void testYahtzeeJokerNotAllowed() {
        // Pretend that we already burned a Yahtzee move
        YahtzeeGame game = new YahtzeeGame(List.of(new YahtzeePlayer("Patrick")));

        game.setDice(List.of(1, 3, 5, 6, 6));
        game.makeMove(comboByName(game, "Yahtzee"));

        assertEquals(0, game.getBonusYahtzeeCount(0));
        assertEquals(0, game.getPlayerScore(0));

        // Pretend we got a Yahtzee, play the twos move.
        game.setDice(List.of(2, 2, 2, 2, 2));
        game.makeMove(comboByName(game, "Twos"));

        assertEquals(10, game.getPlayerScore(0));

        // Now get another Yahtzee
        game.setDice(List.of(2, 2, 2, 2, 2));

        // Now let's say we have a Yahtzee. Full house etc. do not get special treatment, again
        // because Yahtzee is not an option.
        assertEquals(0, comboByName(game, "Full house").score());
        assertEquals(0, comboByName(game, "Small straight").score());
        assertEquals(0, comboByName(game, "Large straight").score());

        // Play the four of a kind move:
        game.makeMove(comboByName(game, "Four of a kind"));

        // No bonus Yahtzee
        assertEquals(0, game.getBonusYahtzeeCount(0));

        // Our score should be 10 (twos move) + 10 (four of a kind)
        assertEquals(20, game.getPlayerScore(0));
    }

    private static Combination comboByName(YahtzeeGame g, String name) {
        return Streams.concat(g.getUpperCombinations().stream(), g.getLowerCombinations().stream())
                .filter(c -> c.getName().equals(name)).findFirst().orElseThrow();
    }
}
