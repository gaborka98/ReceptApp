package com.company.MyClass;

public class Ingredient {
    private int id;
    private String name;
    private double measure;
    private String unit;
    private int group;
    private int multipli;

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }

    public int getMultipli() {
        return multipli;
    }

    public void setMultipli(int multipli) {
        this.multipli = multipli;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMeasure() {
        return measure;
    }

    public void setMeasure(double measure) {
        this.measure = measure;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Ingredient(int id, String name, double measure, int group, String unit) {
        this.id = id;
        this.name = name;
        this.measure = measure;
        this.unit = generateMeasure(group, unit);
    }

    public Ingredient(String name, double measure, String unit) {
        this.name = name;
        this.measure = measure;
        this.unit = unit;
    }

    private String generateMeasure(int group, String unit) {
        if (group == 1) {
            if (measure >= 1000) { measure /= 1000; return "kg"; }
            else if (measure >= 10) { measure /= 10; return "dkg"; }
            else return "g";
        }
        else if (group == 2) {
            if (measure >= 1000) { measure /= 1000; return "l"; }
            else if (measure >= 10) { measure /= 10; return "dl"; }
            else return "ml";
        }
        else return unit;
    }
}
