package com.company.Forms;

import com.company.MyClass.Recipe;
import com.company.MyClass.User;
import com.company.MysqlConnector;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DetailView extends JFrame {

    private JTextPane descriptionText;
    private JButton visszaButton;
    private JButton craftButton;
    private JLabel recipeTitle;
    private JPanel panel;
    private JList ingredientsList;

    private Recipe recipe;
    private RecipesList parent;

    private MysqlConnector conn = MysqlConnector.getInstance();

    public DetailView(Recipe recipe, RecipesList parent) {
        this.recipe = recipe;
        this.parent = parent;

        Dimension dim = new Dimension(600,1000);

        setContentPane(panel);
        setPreferredSize(dim);
        setMinimumSize(dim);
        setLocationRelativeTo(null);
        setTitle("Recep részletei");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                DetailView.this.dispose();
                parent.setVisible(true);
            }
        });

        if(parent.getLoggedIn().getStorageId() == -1) {
            craftButton.setEnabled(false);
            craftButton.setToolTipText("A recept elkészítéséhez létre kell hozz egy raktárat!");
        }

        recipeTitle.setText(recipe.getName());
        descriptionText.setText(recipe.getDescription());

        visszaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DetailView.this.dispose();
                parent.setVisible(true);
            }
        });
    }
}
