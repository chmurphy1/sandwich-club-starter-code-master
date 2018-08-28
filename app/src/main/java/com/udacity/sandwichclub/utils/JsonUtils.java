package com.udacity.sandwichclub.utils;

import com.udacity.sandwichclub.exceptions.MalformedJSONException;
import com.udacity.sandwichclub.model.Sandwich;

import java.util.HashMap;
import java.util.List;

public class JsonUtils {

    public static Sandwich parseSandwichJson(String json) {

        SimpleJsonParser myJsonParser = new SimpleJsonParser(json);

        try {
            myJsonParser.parse();
        } catch (MalformedJSONException e) {
            e.printStackTrace();
        }
        HashMap sandwichData = myJsonParser.getParsedJson();

        Sandwich sandwichObj = new Sandwich();

        sandwichObj.setAlsoKnownAs((List<String>)sandwichData.get(SandwichConstants.aka));
        sandwichObj.setDescription((String)sandwichData.get(SandwichConstants.description));
        sandwichObj.setImage((String)sandwichData.get(SandwichConstants.image));
        sandwichObj.setIngredients((List<String>)sandwichData.get(SandwichConstants.ingredients));
        sandwichObj.setMainName((String)sandwichData.get(SandwichConstants.mainName));
        sandwichObj.setPlaceOfOrigin((String)sandwichData.get(SandwichConstants.placeOfOrigin));

        myJsonParser = null;

        return sandwichObj;
    }
}