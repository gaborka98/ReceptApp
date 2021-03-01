package com.company.Forms;

import com.company.MysqlConnector;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class NewIngredient extends JFrame {
    private JTextField nameField;
    private JPanel panel1;
    private JSpinner group;
    private JButton saveButton;

    private MysqlConnector conn = MysqlConnector.getInstance();

    public NewIngredient() {
        setContentPane(panel1);
        setTitle("Hozzávaló felvétele");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!nameField.getText().isEmpty() && (int) group.getValue() > 0) {
                    conn.addIngredientToDatabase(nameField.getText(), (int) group.getValue());
                    NewIngredient.this.dispose();
                }
            }
        });
    }
}
