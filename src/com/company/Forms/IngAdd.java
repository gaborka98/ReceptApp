package com.company.Forms;

import com.company.MyClass.Ingredient;
import com.company.MysqlConnector;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class IngAdd extends JFrame{
    private JComboBox comboBox1;
    private JPanel panel1;
    private JTextField textField1;
    private JComboBox comboBox2;
    private JButton addButton;

    private final MysqlConnector conn = MysqlConnector.getInstance();

    private final StorageEdit parent;

    public IngAdd(StorageEdit parent) {
        this.parent = parent;

        ArrayList<String> list = conn.getAllIngredients();

        comboBox1.setModel(new DefaultComboBoxModel<String>(list.toArray(new String[0])));
        updateUnitCombo();

        setContentPane(panel1);
        setTitle("Alapanyag hozzáadása");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Ingredient toAdd = generateIngredient();
                if (toAdd != null) {
                    parent.addIngredient(toAdd);
                    IngAdd.this.dispose();
                } else {
                    JOptionPane.showMessageDialog(IngAdd.this, "Nem adtál meg minden adatot!");
                }
            }
        });

        comboBox1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateUnitCombo();
            }
        });
    }

    private void updateUnitCombo() {
        ArrayList<String> list = conn.getAllUnitByIngredientName(comboBox1.getSelectedItem().toString());

        comboBox2.setModel(new DefaultComboBoxModel<String>(list.toArray(new String[0])));
    }

    private Ingredient generateIngredient() {
        if (comboBox1.getSelectedItem().toString().isEmpty() && comboBox2.getSelectedItem().toString().isEmpty() && textField1.getText().isEmpty()) {
            return null;
        } else {
            double measure = 0;
            try {
                measure = Double.parseDouble(textField1.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "A szam formátumát rosszul adtad meg! példa: 1, 1.2, 1.25, 2.234");
            }
            return new Ingredient(comboBox1.getSelectedItem().toString(), measure * conn.getMultipliByUnit(comboBox2.getSelectedItem().toString()), comboBox2.getSelectedItem().toString());
        }
    }
}
