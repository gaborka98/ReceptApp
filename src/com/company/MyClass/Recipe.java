package com.company.MyClass;

import java.util.HashMap;

public class Recipe {
    int id;
    String name;
    String description;
    String category;
    int difficulty;
    HashMap<String, Boolean> allergies;

    public Recipe(int id, String name, String description, String category, int difficulty, HashMap<String, Boolean> allergies) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.allergies = allergies;
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
