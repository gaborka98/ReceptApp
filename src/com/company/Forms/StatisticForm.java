package com.company.Forms;

import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StatisticForm extends JFrame{
    private JPanel panel1;
    private JTextArea topFiveRecipe;
    private JScrollPane scroll;
    private JTextArea topFiveIngredient;
    private JLabel topRecipe;
    private JLabel topMonth;
    private JLabel topIngMonth;
    private JLabel topIngThisMonth;
    private JLabel topBuyThisMonth;
    private JLabel topUseThisMonth;

    private MainMenu parent;

    private MysqlConnector conn = MysqlConnector.getInstance();

    public User getLoggedIn() {
        return parent.getLoggedIn();
    }

    public StatisticForm(MainMenu parent) {
        this.parent = parent;

        setContentPane(panel1);
        setTitle("Raktár szerkesztése");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parent.setVisible(true);
            }
        });

        topFiveRecipe.setText(conn.topFiveRecipeStat(getLoggedIn().getId()));
        topFiveIngredient.setText(conn.topFiveIngredientStat(getLoggedIn().getId()));
    }
}
