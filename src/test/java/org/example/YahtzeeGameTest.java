package org.example;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

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
        Combination chanceCombo = game.getCombinations().getFirst();
        assertEquals("Chance", chanceCombo.getName());

        int expectedScore = game.getDice().getDice().stream().reduce(0, Integer::sum);
        assertEquals(expectedScore, chanceCombo.score(game.getDice()));

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
}
