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
    JDialog dialog;
    YahtzeeGame game;
    List<ActionListener> listeners;
    int desiredPlayerCount = 2;
        List<YahtzeePlayer> players = new ArrayList<>();

    public NewGameDialog(Frame owner) {
        dialog = new JDialog(owner);
        game = null;
        listeners = new ArrayList<>();
        dialog.getContentPane().setLayout(new GridBagLayout());
        var c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 1;

        Component mainPanel = getMainPanel();
        dialog.getContentPane().add(mainPanel, c);

        dialog.pack();
        dialog.setModal(false);
    }

    public void addActionListener(ActionListener l) {
        listeners.add(l);
    }

    public void setVisible(boolean visible) {
        dialog.setVisible(visible);
    }

    private Component getMainPanel() {
        var cont = new Container();
        cont.setLayout(new GridBagLayout());
        var c = new GridBagConstraints();
        c.gridy = 0;
        cont.add(new JLabel("Players"), c);
        var spinnerModel = new SpinnerNumberModel(desiredPlayerCount, 1, 10, 1);
        cont.add(new JSpinner(spinnerModel), c);

        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        JComponent playerNamesPanel = getPlayerNamesPanel(spinnerModel);
        playerNamesPanel.setBorder(new TitledBorder("Player names"));
        cont.add(playerNamesPanel, c);

        c.gridy++;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.LINE_END;
        c.gridwidth = 1;
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(e -> {
            game = new YahtzeeGame(players.subList(0, desiredPlayerCount));
            fireActionPerformedEvent(e);
        });
        cont.add(okButton, c);

        c.anchor = GridBagConstraints.LINE_START;
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> {
            game = null;
            fireActionPerformedEvent(e);
        });
        cont.add(cancelButton, c);


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

            c.weightx = 0;
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
