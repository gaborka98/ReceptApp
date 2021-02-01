package com.company.Forms;

import javax.swing.*;
import java.awt.*;

public class DetailView extends JFrame {


    private int selectedRowId;
    private JTextPane textPane1;

    public DetailView(Object valueAt) {
        this.selectedRowId = (Integer) valueAt;

        Dimension dim = new Dimension(500,750);

        setLocationRelativeTo(null);
        setContentPane(textPane1);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setTitle("Receptek");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        textPane1.setEditable(false);
        textPane1.setText("fasdinlaisdcnalisdnca \ndasdasdasdasdasd");
    }
}
