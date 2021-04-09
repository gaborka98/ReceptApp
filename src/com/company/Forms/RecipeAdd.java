package com.company.Forms;

import com.company.MyClass.Ingredient;
import com.company.MyClass.Recipe;
import com.company.MysqlConnector;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class RecipeAdd extends JFrame {
    private JPanel panel;
    private JTextPane descField;
    private JButton backButton;
    private JButton saveButton;
    private JTable ingTable;
    private JButton newIngButton;
    private JButton ingDeleteButton;
    private JTextField titleField;
    private JCheckBox laktozCB;
    private JCheckBox glutenCheckBox;
    private JCheckBox husCheckBox;
    private JCheckBox tojasCheckBox;
    private JCheckBox cukorCheckBox;
    private JButton dbAddIng;
    private JSlider diffSlider;
    private JLabel diffText;
    private JComboBox<String> categoryCombo;
    private JButton newCategoryButton;
    private JButton deleteButton;
    private JButton imgUploadBtn;
    private JButton imgDeleteBtn;
    private final JComboBox<String> ingCombo = new JComboBox<>();
    private final JComboBox<String> unitCombo = new JComboBox<>();

    private RecipesList parent;
    private MysqlConnector conn = MysqlConnector.getInstance();
    private String[] columns = {"Név", "Mennyiség", "Mértékegység"};

    private Recipe selectedRecipe;

    public RecipeAdd(RecipesList parent, String state, Integer recipeId) {
        this.parent = parent;

        deleteButton.setVisible("edit".equals(state));

        ingCombo.setModel(new DefaultComboBoxModel<String>(conn.getAllIngredients().toArray(new String[0])));
        categoryCombo.setModel(new DefaultComboBoxModel<String>(conn.getAllCategory().toArray(new String[0])));

        ingTable.setModel(new DefaultTableModel(columns,0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        });

        DefaultTableModel model = (DefaultTableModel) ingTable.getModel();
        model.addRow(new Object[]{"","",""});
        TableColumn ingCol = ingTable.getColumnModel().getColumn(0);
        ingCol.setCellEditor(new DefaultCellEditor(ingCombo));

        if ("edit".equals(state)) {
            this.selectedRecipe = conn.getRecipeById(recipeId);
            loadForm(recipeId);
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                parent.setVisible(true);
            }
        });

        setContentPane(panel);
        setTitle("Recept részletei");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);

        imgDeleteBtn.setVisible((selectedRecipe.getImg() != null));
        imgUploadBtn.setVisible((selectedRecipe.getImg() == null));

        ingTable.getModel().addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent e) {
                if (e.getColumn() == 0 && e.getFirstRow() != -1) {
                    unitCombo.setModel(new DefaultComboBoxModel<String>(conn.getAllUnitByIngredientName(ingTable.getValueAt(e.getFirstRow(), e.getColumn()).toString()).toArray(new String[0])));
                    TableColumn unitCol = ingTable.getColumnModel().getColumn(2);
                    unitCol.setCellEditor(new DefaultCellEditor(unitCombo));
                }
            }
        });

        newIngButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) ingTable.getModel();
                model.addRow(new Object[]{"","","","","","",""});
                TableColumn col = ingTable.getColumnModel().getColumn(0);
                col.setCellEditor(new DefaultCellEditor(ingCombo));
            }
        });
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RecipeAdd.this.dispose();
                parent.setVisible(true);
            }
        });
        ingDeleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedRow();
            }
        });
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if ("edit".equals(state)) {
                    if (updateRecipe()) {
                        JOptionPane.showMessageDialog(RecipeAdd.this, "Sikeresen frissítetted a receptet az adatbázisban");
                        RecipeAdd.this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(RecipeAdd.this, "Hiba történt a recept frissítése közben!");
                    }
                } else {
                    if (addRecipe()) {
                        JOptionPane.showMessageDialog(RecipeAdd.this, "Sikeresen hozzáadtad a receptet az adatbázishoz");
                        RecipeAdd.this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(RecipeAdd.this, "Hiba történt a recept hozzáadása közben!");
                    }
                }
            }
        });
        dbAddIng.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewIngredient newIngredient = new NewIngredient();
                newIngredient.setVisible(true);
                newIngredient.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        ingCombo.setModel(new DefaultComboBoxModel<String>(conn.getAllIngredients().toArray(new String[0])));
                        TableColumn col = ingTable.getColumnModel().getColumn(0);
                        col.setCellEditor(new DefaultCellEditor(ingCombo));
                    }
                });
            }
        });
        diffSlider.addComponentListener(new ComponentAdapter() {
        });
        diffSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                diffText.setText(String.valueOf(diffSlider.getValue()));
            }
        });
        newCategoryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String newCategory = JOptionPane.showInputDialog(RecipeAdd.this, "Add meg az új kategória nevét!");
                if (!conn.getAllCategory().contains(newCategory)){
                    if (conn.addCategory(newCategory)) {
                        categoryCombo.setModel(new DefaultComboBoxModel<String>(conn.getAllCategory().toArray(new String[0])));
                    }
                }
            }
        });
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (conn.deleteRecipeById(recipeId, selectedRecipe.getAllergies_id())) {
                    JOptionPane.showMessageDialog(RecipeAdd.this, "Sikeresen törölted a(z) " + selectedRecipe.getName() + " nevű receptet!");
                    RecipeAdd.this.dispose();
                }
            }
        });
        imgUploadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fc = new JFileChooser();
                fc.showOpenDialog(RecipeAdd.this);
                File file = fc.getSelectedFile();
                if (conn.addImage(file, selectedRecipe.getId())) {
                    JOptionPane.showMessageDialog(RecipeAdd.this, "Kép feltöltése sikeresen megtörtént");
                    imgDeleteBtn.setVisible(true);
                    imgUploadBtn.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(RecipeAdd.this, "A kép feltöltése sikertelen");
                }
            }
        });
        imgDeleteBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (conn.deleteImage(selectedRecipe.getId())) {
                    JOptionPane.showMessageDialog(RecipeAdd.this, "A kép törlése sikeresen megtörtént");
                    imgDeleteBtn.setVisible(false);
                    imgUploadBtn.setVisible(true);
                }
            }
        });
    }

    private boolean updateRecipe() {
        boolean success = false;
        if (validateData()) {
            HashMap<String, Boolean> allergiesTemp = generateAllergies();
            ArrayList<Ingredient> ingredientsTemp = generateIngredients();
            String name = titleField.getText();
            String description = descField.getText();
            int diff = diffSlider.getValue();
            String category = categoryCombo.getSelectedItem().toString();
            Recipe toAdd = new Recipe(name, description, category, diff, selectedRecipe.getAllergies_id(), allergiesTemp, ingredientsTemp);

            success = conn.updateRecipeById(selectedRecipe.getId(), toAdd);
        }
        return success;
    }

    private void loadForm(int recipeId) {
        titleField.setText(selectedRecipe.getName());
        descField.setText(selectedRecipe.getDescription());
        categoryCombo.setSelectedIndex(conn.getAllCategory().indexOf(selectedRecipe.getCategory()));
        diffSlider.setValue(selectedRecipe.getDifficulty());
        diffText.setText(String.valueOf(selectedRecipe.getDifficulty()));
        laktozCB.setSelected(selectedRecipe.getAllergies().get("laktoz"));
        glutenCheckBox.setSelected(selectedRecipe.getAllergies().get("gluten"));
        husCheckBox.setSelected(selectedRecipe.getAllergies().get("hus"));
        tojasCheckBox.setSelected(selectedRecipe.getAllergies().get("tojas"));
        cukorCheckBox.setSelected(selectedRecipe.getAllergies().get("cukor"));

        DefaultTableModel model = (DefaultTableModel) ingTable.getModel();
        model.setRowCount(0);

        if (selectedRecipe.getIngredients() != null) {
            for (Ingredient iter : selectedRecipe.getIngredients()) {
                ingCombo.setModel(new DefaultComboBoxModel<String>(conn.getAllIngredients().toArray(new String[0])));
                ingCombo.setSelectedItem(conn.getAllIngredients().indexOf(iter.getName()));
                unitCombo.setSelectedItem(conn.getAllUnitByIngredientName(iter.getName()).indexOf(iter.getUnit()));
                model.addRow(new Object[]{iter.getName(), iter.getFancyMeasure(), iter.getUnit()});
                TableColumn col = ingTable.getColumnModel().getColumn(0);
                col.setCellEditor(new DefaultCellEditor(ingCombo));
                TableColumn unitCol = ingTable.getColumnModel().getColumn(2);
                unitCol.setCellEditor(new DefaultCellEditor(unitCombo));
            }
        }
    }

    private void deleteSelectedRow() {
        int rowCount = ingTable.getRowCount();
        if (rowCount > 1 ) {
            DefaultTableModel model = (DefaultTableModel) ingTable.getModel();
            model.removeRow(ingTable.getSelectedRow());
        }
    }

    private boolean addRecipe() {
        boolean success = false;
        if (validateData()) {
            HashMap<String, Boolean> allergiesTemp = generateAllergies();
            ArrayList<Ingredient> ingredientsTemp = generateIngredients();
            String name = titleField.getText();
            String description = descField.getText();
            int diff = diffSlider.getValue();
            String category = categoryCombo.getSelectedItem().toString();
            Recipe toAdd = new Recipe(name, description, category, diff, allergiesTemp, ingredientsTemp);

            success = conn.insertRecipe(toAdd);
        }
        return success;
    }

    private ArrayList<Ingredient> generateIngredients() {
        ArrayList<Ingredient> toReturn = new ArrayList<>();
        int rowCount = ingTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            toReturn.add(new Ingredient(
                    ingTable.getValueAt(i,0).toString(),
                    Double.parseDouble(ingTable.getValueAt(i,1).toString()) * conn.getMultipliByUnit(ingTable.getValueAt(i,2).toString()),
                    ingTable.getValueAt(i,2).toString(),
                    conn.getIngredientGroupByName(ingTable.getValueAt(i, 0).toString())
            ));
        }
        return toReturn;
    }

    private boolean validateData() {
        if (titleField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nem adtál meg recept nevet!");
            return false;
        }
        if (descField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nem adtál meg recept leírást!");
            return false;
        }
        int rowCount = ingTable.getRowCount();
        for (int i = 0; i < rowCount; i++) {
            if (ingTable.getValueAt(i, 0).toString().isEmpty() || ingTable.getValueAt(i, 1).toString().isEmpty() || ingTable.getValueAt(i, 2).toString().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nem töltöttél ki minden hozzávalót");
                return false;
            }
        }
        return true;
    }

    private HashMap<String, Boolean> generateAllergies() {
        HashMap<String, Boolean> toReturn = new HashMap<>();
        toReturn.put("laktoz", laktozCB.isSelected());
        toReturn.put("gluten", glutenCheckBox.isSelected());
        toReturn.put("hus", husCheckBox.isSelected());
        toReturn.put("tojas", tojasCheckBox.isSelected());
        toReturn.put("cukor", cukorCheckBox.isSelected());
        return toReturn;
    }
}
