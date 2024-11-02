package org.example;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        JFrame f = new JFrame("Hello, world!");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        var l = new GridBagLayout();
        f.getContentPane().setLayout(l);

        var c = new GridBagConstraints();
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        Random r = new Random();

        var buttons = new ArrayList<JDiceToggleButton>(5);

        for (int i = 1; i <= 5; i++) {
            var button = new JDiceToggleButton(r.nextInt(1, 7));
            buttons.add(button);
            f.getContentPane().add(button, c);
        }

        JButton rollButton = new JButton("Roll");
        rollButton.addActionListener(e -> {
            for (JDiceToggleButton b : buttons) {
                if (!b.isSelected()) {
                    b.setValue(r.nextInt(1, 7));
                }
            }

            if (buttons.stream().allMatch(b -> b.getValue() == 6)) {
                JOptionPane.showMessageDialog(f.getContentPane(), "SKIBIDI!!!! YOU WIN!");
            }
        });
        c.gridy++;

        f.getContentPane().add(rollButton, c);

//        f.pack();
        f.setVisible(true);
        f.setSize(100, 100);
        f.setLocation(200, 200);
    }
}