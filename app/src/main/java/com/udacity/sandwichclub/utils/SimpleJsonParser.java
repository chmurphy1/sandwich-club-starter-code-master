package com.udacity.sandwichclub.utils;

import com.udacity.sandwichclub.exceptions.MalformedJSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SimpleJsonParser {
    private StringBuilder jsonStringBuilder;
    private HashMap parsedJson;

    public SimpleJsonParser(String json){
        jsonStringBuilder = new StringBuilder(json.trim());
        parsedJson = new HashMap();
    }

    public void parse() throws MalformedJSONException {
        if(jsonStringBuilder.charAt(0) == '{'  && jsonStringBuilder.charAt(jsonStringBuilder.length()-1) == '}'){
            while(jsonStringBuilder.length() != 0){
                //Find the index of the colon
                int colonIndex = jsonStringBuilder.indexOf(":");
                int commaIndex = 0;
                int posOfFirstCharacter = 0;

                //Get the key from the left handside
                String key = findJsonKey(colonIndex);

                //The value of the key from the right hand side
                String value = "";

                //find the first character after the colon
                //that isn't whitespace
                for(int i = colonIndex+1; i < jsonStringBuilder.length(); i++){

                    //This loop will break on the first no white character
                    if(jsonStringBuilder.charAt(i) != ' '){
                        posOfFirstCharacter = i;
                        break;
                    }
                }

                switch((jsonStringBuilder.charAt(posOfFirstCharacter))){
                    case '[':
                        //find matching ]
                        commaIndex = jsonStringBuilder.indexOf("]")+1;

                        if(jsonStringBuilder.charAt(commaIndex ) == ','){
                            //value = jsonStringBuilder.substring(colonIndex+1, commaIndex);
                            parsedJson.put(key, parseJsonArray(jsonStringBuilder.substring(colonIndex+1, commaIndex)));
                        }
                        else if(jsonStringBuilder.indexOf("}") == jsonStringBuilder.length()-1){
                            //Ending up here means we could be at the end of the json string.
                            //So, I'm going to check for the ending }
                            parsedJson.put(key, parseJsonArray(jsonStringBuilder.substring(colonIndex+1, jsonStringBuilder.indexOf("}"))));
                        }
                        else{
                            throw new MalformedJSONException("The json string is missing one of the following characters ] or ,");
                        }
                        jsonStringBuilder.delete(0, commaIndex+1);
                        break;
                    case '{':
                        SimpleJsonParser newParser = new SimpleJsonParser(findJsonObject(colonIndex));
                        newParser.parse();
                        parsedJson.putAll(newParser.getParsedJson());
                        break;
                    case '"':
                        //Find the index of the comma
                        for(int j = colonIndex+2; j < jsonStringBuilder.length();j++){
                            if(jsonStringBuilder.charAt(j) ==  '"'){
                                if((jsonStringBuilder.charAt(j+1) ==  ',') &&
                                        ((jsonStringBuilder.charAt(j+2) ==  '"'))){
                                    commaIndex = j+1;
                                    break;
                                }
                            }
                        }
                        //save the key and value in a HashMap
                        parsedJson.put(key, findJsonValue( posOfFirstCharacter+1, commaIndex));

                        //delete both left and right side of the colon up to the comma
                        //from the stringbuilder
                        jsonStringBuilder.delete(0, commaIndex+1);
                        break;
                }
            }
        }
    }

    public HashMap getParsedJson() {
        return parsedJson;
    }

    private String findJsonObject(int colonIndex) throws MalformedJSONException {
        //find matching }
        String objectString = "";
        int commaIndex = jsonStringBuilder.indexOf("}")+1;
        HashMap segment = null;

        if(jsonStringBuilder.charAt(commaIndex ) == ','){
            objectString = jsonStringBuilder.substring(colonIndex+1, commaIndex);
        }
        else if(jsonStringBuilder.indexOf("}") == jsonStringBuilder.length()-1){
            //Ending up here means we could be at the end of the json string.
            //So, I'm going to check for the ending }
            objectString = jsonStringBuilder.substring(colonIndex+1, jsonStringBuilder.indexOf("}"));
        }
        else{
            throw new MalformedJSONException("The json string is missing one of the following characters } or ,");
        }
        jsonStringBuilder.delete(0,commaIndex+1);
        return objectString;
    }

    private String findJsonValue(int posOfFirstCharacter, int commaIndex) throws MalformedJSONException {
        String rightSide = "";

        if(jsonStringBuilder.charAt(commaIndex ) == ',') {
            rightSide = jsonStringBuilder.substring(posOfFirstCharacter, commaIndex-1);
        }
        else if(jsonStringBuilder.indexOf("}") == jsonStringBuilder.length()-1){
            //Ending up here means we could be at the end of the json string.
            //So, I'm going to check for the ending }
            rightSide = jsonStringBuilder.substring(posOfFirstCharacter, jsonStringBuilder.indexOf("}"));
        }
        else{
            throw new MalformedJSONException("The json string is missing a comma");
        }

        return rightSide;
    }

    private String findJsonKey(int colonIndex) throws MalformedJSONException {
        int leftDoubleQuotePos = -1;
        int rightDoubleQuotePos = -1;
        String key = "";

        //Find the double quote on the hand side
        for(int i = 0; i < colonIndex ; i++){
            if(jsonStringBuilder.charAt(i) == '"'){
                leftDoubleQuotePos = i;
                break;
            }
        }

        //Find the double quote of the right hand side
        for(int i = colonIndex; i != 0 ; i--){
            if(jsonStringBuilder.charAt(i) == '"'){
                rightDoubleQuotePos = i;
                break;
            }
        }

        if(leftDoubleQuotePos == rightDoubleQuotePos){
            throw new MalformedJSONException("The json string is missing the corresponding double quote.");
        }
        else{
            key = jsonStringBuilder.substring(leftDoubleQuotePos+1, rightDoubleQuotePos);
        }
        return key;
    }

    private static List parseJsonArray(String json) throws MalformedJSONException {
        ArrayList list = new ArrayList<>();
        StringBuilder jsonArray = new StringBuilder(json);

        //Delete [ which indicates the beginning of the array
        jsonArray.deleteCharAt(0);

        while(jsonArray.length() > 0){
            //find first character in list
            char firstCharacter = jsonArray.charAt(0);
            int lastCharPos = 0;

            switch(firstCharacter){
                case '{':
                        int endingCurlyBrace = jsonArray.indexOf("}");
                        if(endingCurlyBrace == -1){
                            throw new MalformedJSONException("The json string is missing the character, }.");
                        }
                        else{
                            SimpleJsonParser newParser = new SimpleJsonParser(jsonArray.substring(0, endingCurlyBrace));
                            newParser.parse();
                            list.add(newParser.getParsedJson());
                        }
                    break;
                case '[':
                        int endingBracket = jsonArray.indexOf("]");
                        if(endingBracket == -1){
                            throw new MalformedJSONException("The json string is missing the ] character");
                        }
                        else{
                            list.add(parseJsonArray(jsonArray.substring(0, endingBracket)));
                            jsonArray.delete(0, endingBracket+1);
                        }
                    break;
                default:
                    int comma = jsonArray.indexOf(",");

                    //if comma equals -1 than it did not find
                    //a comma, so search for the ] character
                    if(comma == -1){
                        comma = jsonArray.indexOf("]");
                    }

                    //search for the double quotes after the comma
                    int precedingDoubleQuotes = comma - 1;
                    if((firstCharacter == '"') && (jsonArray.charAt(precedingDoubleQuotes) != '"')) {
                        for (int i = comma; i < jsonArray.length(); i++) {
                            if ((jsonArray.charAt(i) == ',') && (jsonArray.charAt(i - 1) == '"')) {
                                comma = i;
                                break;
                            }
                        }
                    }

                    if((firstCharacter == '"') && (jsonArray.length() == 1)){
                        //That means we add nothing to the ArrayList
                        //because that element is null
                        jsonArray.deleteCharAt(0);
                    }
                    else if( firstCharacter == ']'){
                        //That means we add nothing to the ArrayList
                        //because that element is null and we are at the
                        //end of the list
                        jsonArray.deleteCharAt(0);
                    }
                    else if(comma > 0) {
                        if(firstCharacter == '"') {
                            int secondDoubleQuotePos = 0;
                            for (int i = comma - 1; i > 0; i--) {
                                    if (jsonArray.charAt(i) == '"') {
                                        secondDoubleQuotePos = i;
                                        break;
                                    }
                            }
                            list.add(jsonArray.substring(1, secondDoubleQuotePos));
                        }
                        else {
                            list.add(jsonArray.substring(0, comma));
                        }
                        jsonArray.delete(0, comma+1);
                    }
            }
        }

        return list;
    }
}