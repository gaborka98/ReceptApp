package com.company.Forms;

import com.company.MyClass.Filter;
import com.company.MysqlConnector;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class FilterView extends JFrame {
    private JComboBox categoryComboBox;
    private JPanel panel1;
    private JCheckBox tojasCheckBox;
    private JCheckBox husCheckBox;
    private JCheckBox laktozCheckBox;
    private JCheckBox cukorCheckBox;
    private JButton szuresButton;
    private JCheckBox glutenCheckBox;
    private JSlider diffSlider;
    private JLabel sliderValue;

    private MysqlConnector conn = MysqlConnector.getInstance();
    private RecipesList parent;



    FilterView(RecipesList parent) {
        this.parent = parent;

        setContentPane(panel1);
        setTitle("Szűrés");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        categoryComboBox.addItem("");
        LoadComboBoxFromArray(conn.getAllCategory(), categoryComboBox);

        szuresButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillFilter();
            }
        });
        diffSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (diffSlider.getValue() == 0) { sliderValue.setText("Ki"); }
                else { sliderValue.setText(Integer.toString(diffSlider.getValue())); }
            }
        });
    }

    private void fillFilter() {
        Filter filter = new Filter();
        filter.setCategory(this.categoryComboBox.getSelectedIndex() != 0 ? this.categoryComboBox.getSelectedIndex() : null);
        filter.setDifficulty(this.diffSlider.getValue());
        filter.setCukor(this.cukorCheckBox.isSelected());
        filter.setGluten(this.glutenCheckBox.isSelected());
        filter.setHus(this.husCheckBox.isSelected());
        filter.setLaktoz(this.laktozCheckBox.isSelected());
        filter.setTojas(this.tojasCheckBox.isSelected());

        parent.setFilter(filter);

        this.dispose();
    }

    public static void LoadComboBoxFromArray(ArrayList<String> items, JComboBox comboBox) {
        for (String iter : items) {
            comboBox.addItem(iter);
        }
    }
}
