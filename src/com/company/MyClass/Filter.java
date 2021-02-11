package com.company.MyClass;

public class Filter {
    private Integer category;
    private Integer difficulty;
    private Boolean tojas;
    private Boolean gluten;
    private Boolean laktoz;
    private Boolean hus;
    private Boolean cukor;
    private Boolean favorites;

    public Filter() {
        this.category = null;
        this.difficulty = null;
        this.tojas = null;
        this.gluten = null;
        this.laktoz = null;
        this.hus = null;
        this.cukor = null;
        this.favorites = null;
    }

    public Boolean getFavorites() {
        return favorites;
    }

    public void setFavorites(Boolean favorites) {
        this.favorites = favorites;
    }

    public Integer getCategory() {
        return category;
    }

    public void setCategory(Integer category) {
        this.category = category;
    }

    public Integer getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Integer difficulty) {
        this.difficulty = difficulty;
    }

    public Boolean getTojas() {
        return tojas;
    }

    public void setTojas(Boolean tojas) {
        this.tojas = tojas;
    }

    public Boolean getGluten() {
        return gluten;
    }

    public void setGluten(Boolean gluten) {
        this.gluten = gluten;
    }

    public Boolean getLaktoz() {
        return laktoz;
    }

    public void setLaktoz(Boolean laktoz) {
        this.laktoz = laktoz;
    }

    public Boolean getHus() {
        return hus;
    }

    public void setHus(Boolean hus) {
        this.hus = hus;
    }

    public Boolean getCukor() {
        return cukor;
    }

    public void setCukor(Boolean cukor) {
        this.cukor = cukor;
    }
}
