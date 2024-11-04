package org.example;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        int[] remaining = new int[]{20};
        JFrame f = new JFrame("Hello, world!");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        YahtzeeGame game = new YahtzeeGame(List.of(new YahtzeePlayer("Player 1"), new YahtzeePlayer("Player 2")));

        // hold dice actions
        var holdActions = new ArrayList<Action>();
        for (int i = 0; i < game.getDice().getDice().size(); i++) {
            holdActions.add(new HoldDieAction(game, i));
        }

        var l = new GridBagLayout();
        f.getContentPane().setLayout(l);

        var c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridwidth = 5;
        MakeMoveAction makeMoveAction = new MakeMoveAction(game);
        JTable moveTable = getMoveTable(game);
        moveTable.getSelectionModel().addListSelectionListener(e -> {
            if (moveTable.getSelectedRow() == -1) {
                makeMoveAction.setCombinationToPlay(null);
            } else {
                makeMoveAction.setCombinationToPlay((Combination) moveTable.getValueAt(moveTable.getSelectedRow(), 0));
            }
        });
        var scrollPane = new JScrollPane(moveTable);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setPreferredSize(new Dimension((int) moveTable.getPreferredSize().getWidth(),
                5 + (int) (moveTable.getPreferredSize().getHeight()
                        + moveTable.getTableHeader().getPreferredSize().getHeight())));
        f.getContentPane().add(scrollPane, c);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;
        c.gridy++;
        c.gridwidth = 1;
        for (int i = 0; i < 5; i++) {
            var button = new JDiceToggleButton(game, i);
            button.setAction(holdActions.get(i));
            f.getContentPane().add(button, c);
        }

        JButton rollButton = new JButton(new RollAction(game));
        c.gridy++;
        c.gridwidth = 2;
        f.getContentPane().add(rollButton, c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        f.getContentPane().add(new JButton(makeMoveAction), c);

        f.pack();
        f.setVisible(true);
        f.setResizable(false);
    }

    private static JTable getMoveTable(YahtzeeGame game) {
        JTable moveTable = new JTable(new ScoreTableModel(game)) {
            private final Font regularFont;
            private final Font boldFont;
            private final Font strikethroughFont;

            {
                regularFont = getFont();
                boldFont = regularFont.deriveFont(Collections.singletonMap(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD));
                strikethroughFont = regularFont.deriveFont(Collections.singletonMap(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON));
            }

            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                int whoseTurn = game.getWhoseTurn();
                setFont(regularFont);

                // If the current player has already made the given move, render the score in bold and the name in
                // strikethrough.
                //
                // TODO: we need to handle Yahtzee correctly, it can be played more than once for a bonus
                if (game.getPlayerMoves(whoseTurn).containsKey((Combination) getValueAt(row, 0))) {
                    if (column == 0) {
                        setFont(strikethroughFont);
                    } else if (column - 1 == whoseTurn) {
                        setFont(boldFont);
                    }
                }

                return super.prepareRenderer(renderer, row, column);
            }
        };

        moveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return moveTable;
    }

    private static class MakeMoveAction extends AbstractAction implements YahtzeeGame.GameStateListener {
        private final YahtzeeGame game;
        private Combination combinationToPlay;

        public MakeMoveAction(YahtzeeGame game) {
            this.game = game;
            setEnabled(false);
            updateName();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            game.makeMove(combinationToPlay);
        }

        public void setCombinationToPlay(Combination combo) {
            this.combinationToPlay = combo;
            setEnabled(combo != null && !game.getPlayerMoves(game.getWhoseTurn()).containsKey(combo));
            updateName();
        }

        @Override
        public void gameStateChanged() {
            updateName();
        }

        private void updateName() {
            if (combinationToPlay == null) {
                putValue(AbstractAction.NAME, "Select combination to play...");
            } else {
                putValue(AbstractAction.NAME, "Play " + combinationToPlay.getName() + " for " + combinationToPlay.score(game.getDice()) + " points");
            }
        }
    }

    private static class HoldDieAction extends AbstractAction implements YahtzeeGame.GameStateListener {
        private final YahtzeeGame game;
        private final int die;

        public HoldDieAction(YahtzeeGame game, int die) {
            this.game = game;
            this.die = die;
            putValue(AbstractAction.SELECTED_KEY, false);
            game.addGameStateListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            game.setDieHeld(die, (boolean) getValue(AbstractAction.SELECTED_KEY));
        }

        @Override
        public void gameStateChanged() {
            putValue(AbstractAction.SELECTED_KEY, game.getDice().isHeld(die));
        }
    }

    private static class RollAction extends AbstractAction implements YahtzeeGame.GameStateListener {
        private final YahtzeeGame game;

        public RollAction(YahtzeeGame game) {
            this.game = game;
            setEnabled(game.getRollsRemaining() > 0);
            putValue(AbstractAction.NAME, "Roll, " + game.getRollsRemaining() + " remaining");
            game.addGameStateListener(this);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            game.rollDice();
        }

        @Override
        public void gameStateChanged() {
            putValue(AbstractAction.NAME, "Roll, " + game.getRollsRemaining() + " remaining");
            setEnabled(game.getRollsRemaining() > 0);
        }
    }

    private static class ScoreTableModel extends AbstractTableModel implements YahtzeeGame.GameStateListener {
        private final YahtzeeGame game;

        public ScoreTableModel(YahtzeeGame game) {
            this.game = game;
            game.addGameStateListener(this);
        }

        @Override
        public int getRowCount() {
            return game.getCombinations().size();
        }

        @Override
        public int getColumnCount() {
            // One column for the combination name, plus one for each player
            return 1 + game.getPlayers().size();
        }

        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return "Combination";
            }

            return (game.getWhoseTurn() == columnIndex - 1 ? "* " : "") + game.getPlayers().get(columnIndex - 1).getName();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return game.getCombinations().get(rowIndex);
            }

            YahtzeePlayer p = game.getPlayers().get(columnIndex - 1);
            Combination c = game.getCombinations().get(rowIndex);
            Map<Combination, Integer> playerMoves = game.getPlayerMoves(columnIndex - 1);

            // In this case, the player has already made the move.
            if (playerMoves.containsKey(c)) {
                return playerMoves.get(c);
            }

            // If it's this player's turn, show the score they'd get if they made this move.
            if (columnIndex - 1 == game.getWhoseTurn()) {
                return c.score(game.getDice());
            }

            return "";
        }

        @Override
        public void gameStateChanged() {
            fireTableStructureChanged();
        }
    }
}