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

        Combination fullHouse = null;
        for (var c : game.getLowerCombinations()) {
            if (c.getName().equals("Full house")) {
                fullHouse = c;
            }
        }

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

        // It's our lucky day. Because it's a bonus Yahtzee, full house and straights have special values.
        {
            Combination fullHouse = comboByName(game, "Full house");
            assertEquals(25, fullHouse.score());
            assertEquals(30, comboByName(game, "Small straight").score());
            assertEquals(40, comboByName(game, "Large straight").score());

            // Play the full house move
            game.makeMove(fullHouse);
        }

        // Now we should have a bonus Yahtzee, and a total score of 175 (50 for the first Yahtzee, 100 for the bonus
        // second, 25 for the full house).
        assertEquals(175, game.getPlayerScore(0));
    }

    @Test
    public void testYahtzeeJokerNotAllowed() {
        // Pretend that we already burned a Yahtzee move
        YahtzeeGame game = new YahtzeeGame(List.of(new YahtzeePlayer("Patrick")));

        game.setDice(List.of(1, 3, 5, 6, 6));
        game.makeMove(comboByName(game, "Yahtzee"));

        assertEquals(0, game.getBonusYahtzeeCount(0));

        // Now let's say we have a Yahtzee. Full house etc. do not get special treatment.
        assertEquals(0, comboByName(game, "Full house").score());
        assertEquals(0, comboByName(game, "Small straight").score());
        assertEquals(0, comboByName(game, "Large straight").score());

        // No points, sad.
        assertEquals(0, game.getPlayerScore(0));
    }

    private static Combination comboByName(YahtzeeGame g, String name) {
        return Streams.concat(g.getUpperCombinations().stream(), g.getLowerCombinations().stream())
                .filter(c -> c.getName().equals(name)).findFirst().orElseThrow();
    }
}
