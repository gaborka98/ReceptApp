package com.company.MyClass;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class Recipe {
    private int id;
    private String name;
    private String description;
    private String category;
    private int difficulty;
    private int allergies_id;
    private ArrayList<Ingredient> ingredients;
    private HashMap<String, Boolean> allergies;
    private BufferedImage img;

    public BufferedImage getImg() {
        return img;
    }

    public void setImg(BufferedImage img) {
        this.img = img;
    }

    public ArrayList<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(ArrayList<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public Recipe(int id, String name, String description, String category, int difficulty,int allergies_id, HashMap<String, Boolean> allergies, ArrayList<Ingredient> ingredients) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.allergies_id = allergies_id;
        this.allergies = allergies;
        this.ingredients = ingredients.isEmpty() ? null : ingredients;
    }

    public int getAllergies_id() {
        return allergies_id;
    }

    public void setAllergies_id(int allergies_id) {
        this.allergies_id = allergies_id;
    }

    public Recipe(String name, String description, String category, int difficulty, int allergies_id, HashMap<String, Boolean> allergies, ArrayList<Ingredient> ingredients) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.allergies_id = allergies_id;
        this.allergies = allergies;
        this.ingredients = ingredients.isEmpty() ? null : ingredients;
    }
    public Recipe(String name, String description, String category, int difficulty, HashMap<String, Boolean> allergies, ArrayList<Ingredient> ingredients) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.allergies = allergies;
        this.ingredients = ingredients.isEmpty() ? null : ingredients;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public HashMap<String, Boolean> getAllergies() {
        return allergies;
    }

    public void setAllergies(HashMap<String, Boolean> allergies) {
        this.allergies = allergies;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
