package com.company.MyClass;

public class Ingredient {
    private int id;
    private String name;
    private double measure;
    private String unit;
    private int group;

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
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

    public Ingredient(int id, String name, double measure, String unit, int group) {
        this.id = id;
        this.name = name;
        this.measure = measure;
        this.unit = unit;
        this.group = group;
    }

    public Ingredient(String name, double measure, String unit, int group) {
        this.name = name;
        this.measure = measure;
        this.unit = unit;
        this.group = group;
    }

    public double getFancyMeasure() {
        if (getGroup() == 1) {
            if (getMeasure() >= 1000) { return (getMeasure() / 1000.0);}
            else if (getMeasure() >= 10) { return (getMeasure() / 10.0);}
            else return getMeasure();
        }
        else if (getGroup() == 2) {
            if (getMeasure() >= 1000) { return (getMeasure() / 1000.0); }
            else if (getMeasure() >= 10) { return (getMeasure() / 10.0); }
            else return getMeasure();
        }
        else {
            return getMeasure();
        }
    }
}
