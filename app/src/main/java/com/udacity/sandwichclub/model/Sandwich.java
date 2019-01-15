package com.udacity.sandwichclub.model;

import java.util.ArrayList;
import java.util.List;
import com.udacity.sandwichclub.parser.annotations.Getter;
import com.udacity.sandwichclub.parser.annotations.Setter;

public class Sandwich {

    private String mainName;
    private List<String> alsoKnownAs = null;
    private String placeOfOrigin;
    private String description;
    private String image;
    private List<String> ingredients = null;

    /**
     * No args constructor for use in serialization
     */
    public Sandwich() {
        this.mainName = "";
        this.alsoKnownAs = new ArrayList<>();
        this.placeOfOrigin = "";
        this.description = "";
        this.image = "";
        this.ingredients = new ArrayList<>();
    }

    public Sandwich(String mainName, List<String> alsoKnownAs, String placeOfOrigin, String description, String image, List<String> ingredients) {
        this.mainName = mainName;
        this.alsoKnownAs = alsoKnownAs;
        this.placeOfOrigin = placeOfOrigin;
        this.description = description;
        this.image = image;
        this.ingredients = ingredients;
    }
    @Getter(getterTarget = "mainName")
    public String getMainName() {
        return mainName;
    }

    @Setter(setterTarget = "mainName")
    public void setMainName(String mainName) {
        this.mainName = mainName;
    }

    @Getter(getterTarget = "alsoKnownAs")
    public List<String> getAlsoKnownAs() {
        return alsoKnownAs;
    }

    @Setter(setterTarget = "alsoKnownAs")
    public void setAlsoKnownAs(List<String> alsoKnownAs) {
        this.alsoKnownAs = alsoKnownAs;
    }

    @Getter(getterTarget = "placeOfOrigin")
    public String getPlaceOfOrigin() {
        return placeOfOrigin;
    }

    @Setter(setterTarget = "placeOfOrigin")
    public void setPlaceOfOrigin(String placeOfOrigin) {
        this.placeOfOrigin = placeOfOrigin;
    }

    @Getter(getterTarget = "description")
    public String getDescription() {
        return description;
    }

    @Setter(setterTarget = "description")
    public void setDescription(String description) {
        this.description = description;
    }

    @Getter(getterTarget = "image")
    public String getImage() {
        return image;
    }

    @Setter(setterTarget = "image")
    public void setImage(String image) {
        this.image = image;
    }

    @Getter(getterTarget = "ingredients")
    public List<String> getIngredients() {
        return ingredients;
    }

    @Setter(setterTarget = "ingredients")
    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
}
