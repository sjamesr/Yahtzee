package org.example;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class NewGameDialog {
    private final JDialog dialog;
    private YahtzeeGame game = null;
    private final List<ActionListener> listeners = new ArrayList<>();
    int desiredPlayerCount = 2;
    private final List<YahtzeePlayer> players = new ArrayList<>();

    public NewGameDialog(Frame owner) {
        dialog = new JDialog(owner);
        dialog.setContentPane(getMainPanel());
        dialog.pack();
        dialog.setModal(false);
    }

    public void addActionListener(ActionListener l) {
        listeners.add(l);
    }

    public void setVisible(boolean visible) {
        dialog.setVisible(visible);
    }

    private JPanel getMainPanel() {
        var cont = new JPanel();
        cont.setLayout(new GridBagLayout());
        var outerConstraints = new GridBagConstraints();
        outerConstraints.fill = GridBagConstraints.HORIZONTAL;
        outerConstraints.weightx = 1;
        outerConstraints.weighty = 0;
        outerConstraints.gridy = 0;
        outerConstraints.gridwidth = 2;

        var spinnerModel = new SpinnerNumberModel(desiredPlayerCount, 1, 10, 1);

        {
            JPanel spinnerPanel = new JPanel(new GridBagLayout());
            var c = new GridBagConstraints();
            c.gridy = 0;
            c.anchor = GridBagConstraints.LINE_START;
            spinnerPanel.add(new JLabel("Players"), c);

            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            spinnerPanel.add(new JSpinner(spinnerModel), c);

            cont.add(spinnerPanel, outerConstraints);
        }

        {
            outerConstraints.gridy++;
            outerConstraints.fill = GridBagConstraints.BOTH;
            outerConstraints.weighty = 1;
            JComponent playerNamesPanel = getPlayerNamesPanel(spinnerModel);
            playerNamesPanel.setBorder(new TitledBorder("Player names"));
            cont.add(playerNamesPanel, outerConstraints);
        }

        {
            outerConstraints.gridy++;
            outerConstraints.fill = GridBagConstraints.NONE;
            outerConstraints.weightx = 0;
            outerConstraints.weighty = 0;
            outerConstraints.gridwidth = 1;
            JButton startButton = new JButton("Start game!");
            startButton.setMnemonic('S');
            startButton.addActionListener(e -> {
                game = new YahtzeeGame(players.subList(0, desiredPlayerCount));
                fireActionPerformedEvent(e);
            });
            cont.add(startButton, outerConstraints);

            JButton quitButton = new JButton("Quit");
            quitButton.setMnemonic('Q');
            quitButton.addActionListener(e -> {
                game = null;
                fireActionPerformedEvent(e);
            });
            cont.add(quitButton, outerConstraints);
        }

        return cont;
    }

    private void fireActionPerformedEvent(ActionEvent e) {
        for (ActionListener l : listeners) {
            l.actionPerformed(e);
        }
    }

    private JComponent getPlayerNamesPanel(SpinnerNumberModel model) {
        var panel = new JPanel(new GridBagLayout());
        populatePlayerNames(panel);

        model.addChangeListener(e -> {
            desiredPlayerCount = model.getNumber().intValue();
            populatePlayerNames(panel);
            panel.invalidate();
            dialog.pack();
        });

        return panel;
    }

    private void populatePlayerNames(Container cont) {
        cont.removeAll();

        var c = new GridBagConstraints();
        c.gridy = 0;
        for (int i = 0; i < desiredPlayerCount; i++) {
            YahtzeePlayer player;
            if (i >= players.size()) {
                player = new YahtzeePlayer(String.format("Player %d", i + 1));
                players.add(player);
            } else {
                player = players.get(i);
            }

            c.fill = GridBagConstraints.NONE;
            c.anchor = GridBagConstraints.LINE_START;
            cont.add(new JLabel(String.format("Player %d", i + 1)), c);
            c.weightx = 1;
            c.fill = GridBagConstraints.HORIZONTAL;
            JTextField nameField = new JTextField(player.getName());
            cont.add(nameField, c);
            c.gridy++;

            nameField.getDocument().addDocumentListener(new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    player.setName(nameField.getText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    player.setName(nameField.getText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    player.setName(nameField.getText());
                }
            });
        }
    }

    public YahtzeeGame getGame() {
        return game;
    }
}
