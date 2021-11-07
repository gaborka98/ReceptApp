package com.company.Forms;

import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

public class StatisticForm extends JFrame{
    private JPanel panel1;
    private JTextArea topFiveRecipe;
    private JScrollPane scroll;
    private JTextArea topFiveIngredient;
    private JLabel topRecipe;
    private JLabel topMonth;
    private JLabel topIngMonth;
    private JLabel topIngThisMonth;
    private JLabel topUseThisMonth;
    private JButton visszaButtonBottom;
    private JButton visszaButtonTop;

    private MainMenu parent;

    private MysqlConnector conn = MysqlConnector.getInstance();

    public User getLoggedIn() {
        return parent.getLoggedIn();
    }

    private static final HashMap<String, String> monthsNames = new HashMap<String,String>() {{
        put("January", "Január");
        put("February", "Február");
        put("March", "Március");
        put("April", "Április");
        put("May", "Május");
        put("June", "Június");
        put("July", "Július");
        put("August", "Augusztus");
        put("September", "Szeptember");
        put("October", "Október");
        put("November", "November");
        put("December", "December");
    }};

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

        ActionListener listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StatisticForm.this.dispose();
                parent.setVisible(true);
            }
        };

        try {
            topFiveRecipe.setText(conn.topFiveRecipeStat(getLoggedIn().getId()));
            topFiveIngredient.setText(conn.topFiveIngredientStat(getLoggedIn().getId()));
            topRecipe.setText(conn.topRecipeActualMonth(getLoggedIn().getId()));
            topMonth.setText(monthsNames.get(conn.topMonthRecipe(getLoggedIn().getId())));
            topIngMonth.setText(monthsNames.get(conn.topMonthIng(getLoggedIn().getId())));
            topIngThisMonth.setText(conn.topIngActMonth(getLoggedIn().getId()));
            topUseThisMonth.setText(conn.topIngActMonthUse(getLoggedIn().getId()));
        } catch (NullPointerException e) {
            JOptionPane.showMessageDialog(StatisticForm.this, "A statisztikák lekérése során valamelyik adat nem letölthető!");
        }

        visszaButtonBottom.addActionListener(listener);
        visszaButtonTop.addActionListener(listener);
    }
}
