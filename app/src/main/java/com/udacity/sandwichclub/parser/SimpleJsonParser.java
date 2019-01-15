package com.udacity.sandwichclub.parser;

import com.udacity.sandwichclub.parser.annotations.Getter;
import com.udacity.sandwichclub.parser.annotations.Setter;
import com.udacity.sandwichclub.parser.utils.Constants;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

public class SimpleJsonParser <T>{
	
	private StringBuilder sb;
	private Class mapping; 
	private Class itemType;
	private T result;
	private boolean isArray = false;
	private HashMap<String, Method> getters = new HashMap();
	private HashMap<String, Method> setters = new HashMap();
	
	public SimpleJsonParser(String json, Class mapping){
		this.sb = new StringBuilder(json.trim());	
		this.mapping = mapping;	
		this.itemType = null;
	}
	
	public T getResult() {
		return result;
	}
	
	public void startParser() throws InstantiationException, IllegalAccessException {
		
		//Create object from class file to store
		//results in
		this.result = (T)this.mapping.newInstance();
		
		//This will setup the setter and getter hashmaps
		//with the setters and getters for the data structure.
		setupGettersAndSetters(null);
		
		//Lets check the opening and closing curly braces before we process
		//the string.  The very first character should be { an the last character
		//should be }.
		
		if((sb.indexOf(Constants.OPENING_CURLY_BRACE) == 0) &&
		   (sb.lastIndexOf(Constants.CLOSING_CURLY_BRACE) == sb.length()-1))
		{
			sb.deleteCharAt(0);
			sb.deleteCharAt(sb.length()-1);
			parse(null);
		}
		else {
			//throw error 
		}
	}
	private <E> void parse(E e) {
		if(sb.length() > 0) {
			int colonIndex = -1;
			
			//Trim white space from the front of
			//the StringBuilder
			if(sb.charAt(0) == Constants.BLANK_CHAR) {
				sb.deleteCharAt(0);
			}
			
			//Get the key from the left hand side 
			String key = Constants.BLANK;
			if(isArray != true) {
				colonIndex = sb.indexOf(Constants.COLON);
				key = findJsonKey(colonIndex); 
			}	
			//Now find the ending character on the
			//right hand side
			int endingCharPos = findEndOfValueKeyPair(colonIndex);
			
			//Use the ending character position to store
			//a string representing the value.
			String value = sb.substring(colonIndex + 1, endingCharPos).trim();
			
			//remove the key and the value from the 
			//StringBuilder
			sb.delete(0, endingCharPos + 1);

			//if ((e != null) && (i != null)) {
			//	processKeyValuePair(key, value, e, i);
			//	parse(e, i);
			//}
			if(e != null) {
				processKeyValuePair(key, value, e);
				parse(e);
			}
			else {
				processKeyValuePair(key, value, null);
				parse(null);
			}
		}
	}
	
	private <E> void setupGettersAndSetters(E e) {
		//Get all getters and setters for the object
		Method[] methods;
		if(e != null) {
			methods = e.getClass().getMethods();
		}
		else {
			methods = result.getClass().getMethods();
		}
		
		for(Method m : methods) {
			Annotation sAnnotation = m.getAnnotation(Setter.class);
			Annotation gAnnotation = m.getAnnotation(Getter.class);
			
			if((sAnnotation != null) && (sAnnotation instanceof Setter)) {
				Setter setterAnnotation = (Setter) sAnnotation;
				setters.put(setterAnnotation.setterTarget(), m);
			}
			else if((gAnnotation != null) && gAnnotation instanceof Getter) {
				Getter getterAnnotation = (Getter) gAnnotation;
				getters.put(getterAnnotation.getterTarget(), m);
			}
			
		}
	}
	
	private <E, I> void processKeyValuePair(String key, String value, E e) {
		//Find the first character of the string, to determine
		//whether the string is an object, list, string, or
		//something else.
		
		//save original StringBuilder
		StringBuilder sbPrime;
		
		switch(value.charAt(0)) {
			case Constants.OPENING_BRACKET_CHAR:
				 //Save original StringBuilder value
				 sbPrime = sb;
				 
				 //create new StringBuilder with value
				 sb = new StringBuilder(value);
				 isArray = true;
				 
				 //Delete Brackets
				 sb.deleteCharAt(0);
				 sb.deleteCharAt(sb.length()-1);
				 
				 //Get the list and the type of objects
				 //that it should contain
				 e = getObj(key, e);
				 itemType = getGetterType(key, e);

				 parse(e);	
				 
				 //re-assign sb to original StringBuilder
				 sb = sbPrime;
				 isArray = false;
				 itemType = null;
			break;
			case Constants.OPENING_CURLY_BRACE_CHAR:
				 //Save original values
				 sbPrime = sb;
				 HashMap<String, Method> pGetters = getters;
				 HashMap<String, Method> pSetters = setters;
				 
				 //create new StringBuilder with value
				 sb = new StringBuilder(value);
				 
				 //Delete curly braces
				 sb.deleteCharAt(0);
				 sb.deleteCharAt(sb.length()-1);
				 
				 //Get the object and setup the getters
				 //and setters hashmaps
				 
				 if(isArray) {
					 try {
						isArray = false;
						I i = (I) itemType.newInstance();
						if(!Constants.BLANK.equals(i)) {
							getters = new HashMap<String, Method>();
							setters = new HashMap<String, Method>();
							setupGettersAndSetters(i);
							parse(i);
							saveListItem(null, e, i);
							isArray = true;
						}
					} catch (InstantiationException | IllegalAccessException e1) {
						e1.printStackTrace();
					}
				 }
				 else {
					 e = getObj(key, e);
					 getters = new HashMap<String, Method>();
					 setters = new HashMap<String, Method>();
					 setupGettersAndSetters(e);
					 parse(e);	
				 }
				 
				 //restore original values
				 sb = sbPrime;
				 getters = pGetters;
				 setters = pSetters;
			break;
			default:
				//Objects, strings, booleans, null, and numbers will
				//be processed here.
				if((e != null) && !(e instanceof List)) {
					saveValues(key, value, e);
				}
				else if((e != null) && (e instanceof List)) {
					//For lists
					saveListItem(value, e, null);
				}
				else {
					saveValues(key, value, result);
				}
		}	
	}
	
	private <E, I> void saveListItem(String value, E e, I i) {
		Method[] methods = e.getClass().getMethods();
		Method addMethod = null;
		
		//Find the add method with a single parameter
		for(Method m : methods) {
			if(m.getName().equals("add")) {
				if(m.getParameterCount() == 1) {
					addMethod = m;
				}
			}
		}
		
		//check to see if the add method is not null
		//before use
		if(addMethod != null) {
			try {
				if(i == null) {
					addValueToList(value, e, addMethod);
				}
				else if(i != null){
					addMethod.invoke(e, i);
					//Throw an error data doesn't have a proper type
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private <E> void addValueToList(String value, E e, Method addMethod) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {	
		if(itemType == String.class) {
			if(value.indexOf('"') == 0 && value.lastIndexOf('"') == value.length()-1 ){
				value = value.substring(1, value.length()-1);
			}
			addMethod.invoke(e, value);
			return;
		}
		
		//Search for the first double quote and remove it
		//from the string
		if(value.charAt(0) == Constants.DOUBLE_QUOTE_CHAR) {
			value = value.replaceFirst(Constants.DOUBLE_QUOTE, Constants.BLANK);
		}
		
		//Search for the last double quote and remove it
		if(value.charAt(value.length()-1) == Constants.DOUBLE_QUOTE_CHAR) {
			value = value.substring(0, value.length()-1);
		}
		
		if((itemType == Long.class) || (itemType == long.class)) {
			addMethod.invoke(e, Long.parseLong(value));
		}
		else if((itemType == Integer.class) || (itemType == int.class)) {
			addMethod.invoke(e, Integer.parseInt(value));
		}
		else if((itemType == Short.class) || (itemType == short.class)) {
			addMethod.invoke(e, Short.parseShort(value));
		}
		else if((itemType == Byte.class) || (itemType == byte.class)) {
			addMethod.invoke(e, Byte.parseByte(value));
		}
		else if((itemType == Double.class) || (itemType == double.class)) {
			addMethod.invoke(e, Double.parseDouble(value));
		}
		else if((itemType == Float.class) || (itemType == float.class)) {
			addMethod.invoke(e, Float.parseFloat(value));
		}
		else if((itemType == Boolean.class) || (itemType == boolean.class)) {
			addMethod.invoke(e, Boolean.parseBoolean(value));
		}
		else if((itemType == Character.class) || (itemType == char.class)) {
			addMethod.invoke(e, value.charAt(0));
		}	
	}
		
	private <E> E getObj(String key, E e) {
		
		Method getter = getters.get(key);
		if(getter != null) {
			try {
				if(e != null) {
					e = (E) getter.invoke(e);
				}
				else {
					e = (E) getter.invoke(result);
				}
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
				e1.printStackTrace();
			}
		}		
		return e;
	}
	
	private <E> Class getGetterType(String key, E e) {		
		Class listType = null;
		
		Method getter = getters.get(key);
		
		if(java.util.List.class.isAssignableFrom(e.getClass())) {
			Annotation gAnnotation = getter.getAnnotation(Getter.class);

			if((gAnnotation != null) && gAnnotation instanceof Getter) {
				Getter getterAnnotation = (Getter) gAnnotation;
				listType = getterAnnotation.ClassType();
			}
		}	
		 return listType;
	}
	
	private <E> void saveValues(String key, String value, E e) {
		Method setter = setters.get(key);
		
		//This is null object and will be used
		//only when the value equals null
		Object arg = null;
		
		try {
			if(setter != null) {
			
				if(setter.getParameterCount() != 1) {
					//Throw an error stating that only 1
					//parameter is expected for the setter.
				}
				else {
					Class[] parameterTypes = setter.getParameterTypes();
					
					if(parameterTypes[0].isPrimitive()){
						if(parameterTypes[0].isAssignableFrom(int.class)){
							setter.invoke(e, Integer.parseInt(value));
						}
						else if(parameterTypes[0].isAssignableFrom(double.class)) {
							setter.invoke(e, Double.parseDouble(value));
						}
						else if(parameterTypes[0].isAssignableFrom(boolean.class)) {
							setter.invoke(e, Boolean.parseBoolean(value));
						}
						else if(parameterTypes[0].isAssignableFrom(char.class)) {
							setter.invoke(e, value.charAt(0));
						}
						else if(parameterTypes[0].isAssignableFrom(float.class)) {
							setter.invoke(e, Float.parseFloat(value));
						}
						else if(parameterTypes[0].isAssignableFrom(long.class)) {
							setter.invoke(e, Long.parseLong(value));
						}
						else if(parameterTypes[0].isAssignableFrom(short.class)) {
							setter.invoke(e, Short.parseShort(value));
						}
						else if(parameterTypes[0].isAssignableFrom(byte.class)) {
							setter.invoke(e, Byte.parseByte(value));
						}
					}
					else if(parameterTypes[0].isAssignableFrom(String.class)) {
						//It's a string remove the double 
						//quotes surrounding the string.
						
						if((value.charAt(0) == Constants.DOUBLE_QUOTE_CHAR) && 
						   (value.charAt(value.length()-1) == Constants.DOUBLE_QUOTE_CHAR)){
							value = value.substring(1, value.length()-1);
						}
						
						if(!value.equals("null")) {
							setter.invoke(e, value);
						}else {
							setter.invoke(e, arg);
						}
					}
				}
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e1) {
			e1.printStackTrace();
		}
	}
	
	private int findEndOfValueKeyPair(int colonIndex) {
		int endingChar = colonIndex + 1;
		
		int doubleQuoteCounter = 0;
		int openingBracketCounter = 0;
		int closingBraketCounter = 0;
		int openingCurlyBraceCounter = 0;
		int closingCurlyBraceCounter = 0;	
		
		while(endingChar < sb.length()) {
			
			if(sb.charAt(endingChar) == Constants.DOUBLE_QUOTE_CHAR) {
				doubleQuoteCounter++;
			}
			else if(sb.charAt(endingChar) == Constants.OPENING_BRACKET_CHAR) {
				openingBracketCounter++;
			}
			else if(sb.charAt(endingChar) == Constants.CLOSING_BRACKET_CHAR) {
				closingBraketCounter++;
			}
			else if(sb.charAt(endingChar) == Constants.OPENING_CURLY_BRACE_CHAR) {
				openingCurlyBraceCounter++;
			}
			else if(sb.charAt(endingChar) == Constants.CLOSING_CURLY_BRACE_CHAR) {
				closingCurlyBraceCounter++;
			}
			else if(sb.charAt(endingChar) == Constants.COMMA_CHAR) {
				
				//Check to make sure strings have a matching
				//double quotes, arrays have a matching
				//ending bracket, and objects have matching
				//curly braces.
				if((doubleQuoteCounter % 2 == 0) && 
				   ((openingBracketCounter == closingBraketCounter) && (closingBraketCounter == 0)) &&
				   ((openingCurlyBraceCounter == closingCurlyBraceCounter) && (closingCurlyBraceCounter == 0))){
					break;
				}
				else if ( (openingBracketCounter > 0) && (closingBraketCounter > 0) && 
						(openingBracketCounter % closingBraketCounter == 0)) {
					break;
				}
				else if ( (openingCurlyBraceCounter > 0) && (closingCurlyBraceCounter > 0) && 
						(openingCurlyBraceCounter % closingCurlyBraceCounter == 0)) {
					break;
				}
			}
			endingChar++;
		}
		
		if(doubleQuoteCounter % 2 == 1) {
			//throw an error because there is a missing
			//double quote
			System.out.println("Double Quote Mismatch Error");
		}
		else if(openingBracketCounter != closingBraketCounter) {
			//throw an error because there is a missing
			//bracket
			System.out.println("Bracket Mismatch Error");
		}
		else if(openingCurlyBraceCounter != closingCurlyBraceCounter) {
			//throw an error because there is a missing
			//bracket
			System.out.println("Curly Brace Mismatch Error");
		}
		
		return endingChar;
	}
	
	private String findJsonKey(int colonIndex) {
		
		int leftDoubleQuotePos = -1; 
		int rightDoubleQuotePos = -1; 
		String key = "";
		
		//lets search for the first double quote on
		//the left hand side ignoring blanks before
		//the double quote.
		for(int i = 0; i < colonIndex; i++) {
			
			if(sb.charAt(i) == Constants.DOUBLE_QUOTE_CHAR){ 
				leftDoubleQuotePos = i; 
				break; 
			} 
			else if((sb.charAt(i) != Constants.DOUBLE_QUOTE_CHAR) && 
					((sb.charAt(i) != Constants.BLANK_CHAR))) {
				//throw an error saying the first character on the left
				//hand side must be a blank or a double quote
			}
		}
		
        //Find the double quote of the right hand side 
         for(int i = colonIndex -1; i != 0 ; i--){ 
              if(sb.charAt(i) == Constants.DOUBLE_QUOTE_CHAR){ 
                  rightDoubleQuotePos = i; 
                  break; 
              } 
              else if((sb.charAt(i) != Constants.DOUBLE_QUOTE_CHAR) && 
					((sb.charAt(i) != Constants.BLANK_CHAR))) {
				//throw an error saying the first character on the left
				//hand side must be a blank or a double quote
			}
          } 
         
         if(leftDoubleQuotePos == rightDoubleQuotePos){ 
        	 //if the index of the left and right double quote
        	 //are the same throw an error
         } 
         else{ 
        	 key = sb.substring(leftDoubleQuotePos+1, rightDoubleQuotePos); 
         } 
         return key; 
	}
}