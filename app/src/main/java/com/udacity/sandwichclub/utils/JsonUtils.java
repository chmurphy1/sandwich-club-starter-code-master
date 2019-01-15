package com.udacity.sandwichclub.utils;
import com.udacity.sandwichclub.exceptions.MalformedJSONException;
import com.udacity.sandwichclub.model.Sandwich;
import com.udacity.sandwichclub.parser.SimpleJsonParser;

public class JsonUtils {

    public static Sandwich parseSandwichJson(String json) {

        SimpleJsonParser<Sandwich> myJsonParser = new SimpleJsonParser(json, Sandwich.class);
        try {
            myJsonParser.startParser();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        Sandwich sandwichObj = myJsonParser.getResult();

        return sandwichObj;
    }
}