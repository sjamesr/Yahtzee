package org.example;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main {
    private static KeyEventDispatcher dispatcher;

    public static void main(String[] args) {
        NewGameDialog dialog = new NewGameDialog(null);
        dialog.addActionListener(e -> {
            YahtzeeGame game = dialog.getGame();
            if (game == null) {
                System.exit(0);
            } else {
                JFrame f = getNewGameFrame(dialog.getGame());
                showStandingsWhenGameEnds(game, f.getContentPane());
                game.addGameStateListener(() -> {
                    if (game.isGameOver()) {
                        f.setVisible(false);
                        f.dispose();
                        dialog.setVisible(true);
                    }
                });
                dialog.setVisible(false);
                f.setVisible(true);
            }
        });
        dialog.setVisible(true);
    }

    public static JFrame getNewGameFrame(YahtzeeGame game) {
        JFrame f = new JFrame("Yahtzee!");

        Action quitAction = new AbstractAction("Quit") {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        };
        quitAction.putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_Q);
        quitAction.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Q, KeyEvent.CTRL_DOWN_MASK));

        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

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
            Combination comboToPlay = null;
            int row = moveTable.getSelectedRow();
            if (row != -1) {
                Object o = moveTable.getValueAt(row, 0);
                if (o instanceof Combination) {
                    comboToPlay = (Combination) o;
                }
            }
            makeMoveAction.setCombinationToPlay(comboToPlay);
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

        c.gridy++;
        c.weighty = 0;
        c.anchor = GridBagConstraints.CENTER;
        for (int i = 0; i < 5; i++) {
            var checkbox = new JCheckBox(holdActions.get(i));
            checkbox.setHideActionText(true);
            checkbox.setHorizontalAlignment(SwingConstants.CENTER);
            f.getContentPane().add(checkbox, c);
        }

        var rollAction = new RollAction(game);
        JButton rollButton = new JButton(rollAction);
        c.gridy++;
        c.gridwidth = 2;
        f.getContentPane().add(rollButton, c);

        c.gridwidth = GridBagConstraints.REMAINDER;
        f.getContentPane().add(new JButton(makeMoveAction), c);

        if (dispatcher != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
        }

        dispatcher = new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getID() != KeyEvent.KEY_PRESSED) {
                    return false;
                }

                Action action = null;
                boolean toggle = false;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_1:
                        action = holdActions.get(0);
                        toggle = true;
                        break;
                    case KeyEvent.VK_2:
                        action = holdActions.get(1);
                        toggle = true;
                        break;
                    case KeyEvent.VK_3:
                        action = holdActions.get(2);
                        toggle = true;
                        break;
                    case KeyEvent.VK_4:
                        action = holdActions.get(3);
                        toggle = true;
                        break;
                    case KeyEvent.VK_5:
                        action = holdActions.get(4);
                        toggle = true;
                        break;
                    case KeyEvent.VK_SPACE:
                        action = makeMoveAction;
                        break;
                    case KeyEvent.VK_R:
                        action = rollAction;
                        break;
                    default:
                }

                if (action != null && action.isEnabled()) {
                    if (toggle) {
                        action.putValue(AbstractAction.SELECTED_KEY, !(action.getValue(AbstractAction.SELECTED_KEY) == Boolean.TRUE));
                    }

                    action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
                    return true;
                }
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);

        var menuBar = new JMenuBar();
        var gameMenu = new JMenu("Game");
        gameMenu.setMnemonic('G');

        for (var action : holdActions) {
            gameMenu.add(new JCheckBoxMenuItem(action));
        }

        gameMenu.addSeparator();
        gameMenu.add(rollAction);
        gameMenu.add(makeMoveAction);
        gameMenu.addSeparator();
        gameMenu.add(quitAction);

        menuBar.add(gameMenu);
        f.setJMenuBar(menuBar);

        f.revalidate();
        f.pack();
        f.setResizable(false);

        return f;
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
                Component result = super.prepareRenderer(renderer, row, column);

                if (row == -1 || column == -1) {
                    return result;
                }

                result.setFont(regularFont);
                result.setEnabled(true);

                ScoreTableModel.Row modelRow = ((ScoreTableModel) getModel()).getRow(row);
                Object o = modelRow.getValue(0);

                if (modelRow.style() == ScoreTableModel.RowStyle.TOTAL) {
                    result.setFont(boldFont);
                } else if (modelRow.style() == ScoreTableModel.RowStyle.COMBO) {
                    Combination combo = o instanceof Combination ? (Combination) o : null;
                    if (column == 0 && combo != null && game.getPlayerMoves(game.getWhoseTurn()).containsKey(combo)) {
                        // Current player has already played this combo
                        result.setFont(strikethroughFont);
                        result.setEnabled(false);
                    } else if (column > 0 && combo != null && game.getPlayerMoves(column - 1).containsKey(combo)) {
                        // Player for this column has already locked in this combo.
                        result.setFont(regularFont);
                        result.setEnabled(false);
                    }
                }

                return result;
            }
        };

        moveTable.setColumnSelectionAllowed(false);
        moveTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        return moveTable;
    }

    private static class MakeMoveAction extends AbstractAction implements YahtzeeGame.GameStateListener {
        private final YahtzeeGame game;
        private Combination combinationToPlay;

        public MakeMoveAction(YahtzeeGame game) {
            this.game = game;
            putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_S);
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK));
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
                putValue(AbstractAction.NAME, "Play " + combinationToPlay.getName() + " for " + combinationToPlay.score() + " points");
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
            putValue(AbstractAction.SHORT_DESCRIPTION, "Hold die " + (die + 1));
            putValue(AbstractAction.NAME, "Hold die " + (die + 1));
            putValue(AbstractAction.MNEMONIC_KEY, '1' + die);
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke('1' + die, KeyEvent.CTRL_DOWN_MASK));
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
            putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
            putValue(AbstractAction.MNEMONIC_KEY, KeyEvent.VK_R);
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

    private static void showStandingsWhenGameEnds(YahtzeeGame game, Component parent) {
        game.addGameStateListener(() -> {
            // Detect the game over scenario.
            if (game.isGameOver()) {
                // Create the standings:
                StringBuilder builder = new StringBuilder("Game over!\n\n");

                List<Integer> standings = new ArrayList<>();
                for (int i = 0; i < game.getPlayers().size(); i++) {
                    standings.add(i);
                }

                standings.sort(Comparator.comparing(game::getPlayerScore).reversed());

                int place = 0;
                int lastScore = Integer.MAX_VALUE;
                for (int i = 0; i < game.getPlayers().size(); i++) {
                    int currentScore = game.getPlayerScore(i);
                    if (currentScore < lastScore) {
                        place++;
                    }
                    builder.append(place).append(". ").append(game.getPlayers().get(i).getName())
                            .append(" ").append(currentScore).append("\n");

                    lastScore = currentScore;
                }

                JOptionPane.showMessageDialog(parent, builder);
            }
        });
    }
}