package client;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.Scanner;
import java.util.Vector;

import org.jdom2.Document;

import serializer.ArrayField;
import serializer.ClassField;
import serializer.ObjectField;
import serializer.ObjectHandler;
import serializer.PrimitiveField;
import serializer.Serializer;

public class ObjectSerializerClient {
	private Menu _MainMenu;
	private final String DELIMITER = "   ";
	
	public ObjectSerializerClient()
	{
		
	}
	
	/**
	 * 
	 */
	public void run()
	{
		String[] classes = new String[] { "classes.ClassA", "classes.ClassB", "classes.ClassC", "classes.ClassD" };
		Scanner keyboard = new Scanner(System.in);
		Integer choice;
		
		_MainMenu = new Menu("Welcome to the Object Serializer. Please choose an object to instantiate", System.in, System.out);
		initializeMenu(classes, _MainMenu);
		
		_MainMenu.displayTitle();
		choice = getMenuOption(_MainMenu, "");
		while(choice != _MainMenu.getCancelOption() && choice != -1)
		{
			ObjectHandler objHandler = getObjectHandler(classes[choice]);
			instantiateObjectFields(objHandler, DELIMITER);
			
			Serializer serializer = new Serializer();
			Document doc = serializer.serialize(objHandler.getRootObject());
			
			//String path = getLine("Enter an absolute file path to save serialized object: ", "");
			//serializer.writeXML(doc, path);
			
			sendDocument(doc);
			
			_MainMenu.displayTitle();
			choice = getMenuOption(_MainMenu, "");
		}
		
		System.out.println("Thank you for using the Object Serializer.");
		System.out.println("Press any key to exit");
		keyboard.nextLine();
		keyboard.close();
	}
	
	/**
	 * 
	 * @param doc
	 */
	public void sendDocument(Document doc)
	{
		try{
			Socket socket = new Socket("localhost", 2255);
			int count;
			OutputStream outputStream = socket.getOutputStream();
			
			String xmlOutput = Serializer.documentToString(doc);
			byte[] buffer = new byte[xmlOutput.length()];
			
			BufferedInputStream inStream = new BufferedInputStream(new ByteArrayInputStream(xmlOutput.getBytes()));
			
			try{
				while ((count = inStream.read(buffer)) > 0)
				{
					outputStream.write(buffer, 0, count);
					outputStream.flush();
				}
		
				inStream.close();
				outputStream.close();
				socket.close();
			}catch (Exception ex)
			{
				System.out.println("Explosion... " + ex.getMessage());
			}
		}catch(Exception ex)
		{
			System.out.println("Explosion..." + ex.getMessage());
		}
	}
	
	/**
	 * instantiateObjectFields takes in an ObjectHandler and generates a menu for the user to instantiate and set all fields within
	 * the Object in ObjectHandler
	 * 
	 * @param objHandler The ObjectHandler for the object being instantiated.
	 * @param delimiter  A delimiter used for formatting client output. Will prefix all output.
	 */
	public void instantiateObjectFields(ObjectHandler objHandler, String delimiter)
	{
		Menu fieldMenu = new Menu(delimiter + "Set fields for " + objHandler.getRootClass().getName(), System.in, System.out);
		Vector<ClassField> fields = objHandler.getFields();
		Integer choice;
		
		initializeMenu(objHandler.getFieldNames(), fieldMenu);
		
		fieldMenu.displayTitle();
		choice = getMenuOption(fieldMenu, delimiter);
		
		while(choice != fieldMenu.getCancelOption() && choice != -1)
		{
			setField(fields.elementAt(choice), delimiter);
			fieldMenu.clearOptions();
			initializeMenu(objHandler.getFieldNames(), fieldMenu);
		
			fieldMenu.displayTitle();
			choice = getMenuOption(fieldMenu, delimiter);
		}
	}
	
	/**
	 * setField determines the type of the field and performs the necessary actions required to instantiate, and set the fields of the object.
	 * 
	 * @param classField the ClassField that represents the field
	 * @param delimiter A delimiter that will prefix all output.
	 */
	public void setField(ClassField classField, String delimiter)
	{
		ObjectHandler objHandler;
		Object obj = classField.getValue();
		
		if(classField instanceof ArrayField){
			if (obj == null)
			{
				createArray(classField.getParentObject(), classField.getField(), delimiter);
			}else
			{
				fillArray(obj, delimiter);
			}	
		}else if(classField instanceof ObjectField){			
			//if object has not been initialize yet, create new object
			if (obj == null)
			{
				objHandler = getObjectHandler(classField.getTypeName());
				setObjectValue(classField, objHandler.getRootObject());	
			}else
				objHandler = new ObjectHandler(obj);
			
			instantiateObjectFields(objHandler, delimiter + DELIMITER);
		}else if(classField instanceof PrimitiveField)
		{
			setPrimitiveValue(classField, delimiter);
		}else
		{
			//handle collection
		}
	}

	/**
	 * createArray takes will determine the dimensions of an array field, then ask the user for the sizes of each dimension.
	 * <p>
	 * The user then has the option to fill the array, or leave it to the default instantiated values.
	 * 
	 * @param parentObj The object to which the field belongs
	 * @param field The field that represents the array being instantiated
	 * @param delimiter a delimiter that will prefix all output.
	 */
	@SuppressWarnings("rawtypes")
	public void createArray(Object parentObj, Field field, String delimiter)
	{
		int dimensions = Integer.parseInt(ObjectHandler.getClassNameDetails(field.getType())[0]);
		int[] sizes = new int[dimensions];
		System.out.println(dimensions+"D array detected.");
		Class componentType = field.getType();
		//don't do anything unless actually array.
		if(dimensions > 0)
		{
			for(int i = 0; i < dimensions; i++){
				sizes[i] =  getInt("Please enter array size for dimension " + (i + 1) + ": ", delimiter);
				componentType = componentType.getComponentType();
			}
			
			try{
				field.set(parentObj, Array.newInstance(componentType, sizes));
				
				if(getYesNo("Would you like to fill array now? (y/n): ", delimiter) == 'Y')
				{
					fillArray(field.get(parentObj), delimiter);
				}
			}catch(Exception ex)
			{
				System.out.println(delimiter + ex.getMessage());
			}
		}	
	}
	
	/**
	 * fillArray takes in an array Object and recursively fills the object with user input.
	 * 
	 * @param obj An array Object
	 * @param delimiter a delimiter that will prefix all output.
	 */
	public void fillArray(Object obj, String delimiter)
	{
		int length;
		Object nextArrObj;
		Object value;
		
		if (obj.getClass().isArray())
		{			
			length = Array.getLength(obj);
			for(int i = 0; i < length; i++)
			{
				nextArrObj = Array.get(obj, i);
				if (nextArrObj == null)
				{
					System.out.println("Element " + i);
					ObjectHandler objHandler = getObjectHandler(obj.getClass().getComponentType().getName());
					Array.set(obj, i, objHandler.getRootObject());
					instantiateObjectFields(objHandler, delimiter + DELIMITER);
				}
				else if (nextArrObj.getClass().isArray())
				{
					fillArray(nextArrObj, delimiter);
				}
				else if (nextArrObj.getClass().isPrimitive() || ObjectHandler.isWrapper(nextArrObj.getClass()))
				{
					do
					{
						value = getPrimitiveObject(nextArrObj.getClass().getTypeName(), 
								getLine("Enter value for element [" + i + "]: ", delimiter));
					}while(value == null);
					
					Array.set(obj, i, value);
				}else
				{
					ObjectHandler objHandler = new ObjectHandler(nextArrObj);
					instantiateObjectFields(objHandler, delimiter + DELIMITER);
				}
			}
		}
	}
	
	/**
	 * setPrimitiveValue takes in a ClassField representing a field of an object and sets the value of field to user input.
	 * 
	 * @param classField The ClassField that represents the Field and Object being set
	 * @param delimiter A delimiter that will prefix all output
	 */
	public void setPrimitiveValue(ClassField classField, String delimiter)
	{
		@SuppressWarnings("resource")
		Scanner keyboard = new Scanner(System.in);
		String input;
		Object obj;
		
		System.out.print(delimiter + "Please enter a value for " + classField.getTypeName() + " " + classField.getName() 
			+ " (" + getPrimitiveFormat(classField.getTypeName()) + "): ");
		input = keyboard.next();
		
		obj = getPrimitiveObject(classField.getTypeName(), input);
		while(obj == null)
		{
			System.out.print(delimiter + "Please enter a value for " + classField.getTypeName() + " "+ classField.getName() 
				+ " (" + getPrimitiveFormat(classField.getTypeName()) + "): ");
			input = keyboard.next();
			obj = getPrimitiveObject(classField.getTypeName(), input);
		}
		
		setObjectValue(classField, obj);
	}

	/**
	 * getPrimitiveObject takes in a class type name in String format (assumed to represent a primitive, or primitive wrapper type) and a String value and
	 * returns a primitive Object wrapper that represents the value of the String passed in.
	 * 
	 * If the value is in bad form for the class type name that was received, or the class type name is not of primitive or primitive wrapper type,
	 * null is returned.
	 *  
	 * @param typeName A String representation of the values class type. (ie, "java.lang.Integer","byte","char","java.lang.Character", etc.)
	 * @param value The value of the desired Object
	 * @return A primitive wrapper Object of the requested typeName with value "value", or null if input is in bad form.
	 */
	public Object getPrimitiveObject(String typeName, String value)
	{
		Object retVal = null;
		
		try{
			switch(typeName)
			{
				case "byte":
				case "java.lang.Byte":
					retVal = new Byte(Byte.parseByte(value));
					break;
				case "short":
				case "java.lang.Short":
					retVal = new Short(Short.parseShort(value));
					break;
				case "int":
				case "java.lang.Integer":
					retVal = new Integer(Integer.parseInt(value));
					break;
				case "long":
				case "java.lang.Long":
					retVal = new Long(Long.parseLong(value));
					break;
				case "float":
				case "java.lang.Float":
					retVal = new Float(Float.parseFloat(value));
					break;
				case "double":
				case "java.lang.Double":
					retVal = new Double(Double.parseDouble(value));
					break;
				case "boolean":
				case "java.lang.Boolean":
					retVal = new Boolean(Boolean.parseBoolean(value));
					break;
				case "char":
				case "java.lang.Character":
					retVal = new Character(value.charAt(0));
					break;
			}
		}catch(Exception ex)
		{
			System.out.println("Bad value format. Please retry.");
		}
		
		return retVal;
	}
	
	/**
	 * getPrimitiveFormat returns a string representation of the valid values of the specified "typeName".
	 * 
	 * @param typeName the primitive type for which the format is desired (ie, "int", "byte", "char" ...)
	 * @return a string representation of the valid values of the specified "typeName"
	 */
	public String getPrimitiveFormat(String typeName)
	{
		String retVal = "";
		switch(typeName)
		{
			case "byte":
				retVal = "-128 to 127";
				break;
			case "short":
				retVal = "-32768 to 32767";
				break;
			case "int":
				retVal = "whole number";
				break;
			case "long":
				retVal = "whole number";
				break;
			case "float":
				retVal = "decimal number";
				break;
			case "double":
				retVal = "decimal number";
				break;
			case "boolean":
				retVal = "'true' or 'false'";
				break;
			case "char":
				retVal = "single character, ie 'c'";
				break;
		}
		
		return retVal;
	}
	
	/**
	 * setObjectValue takes in a ClassField representation of a field and an Object value and sets the value of the field to that Object.
	 * 
	 * @param classField The ClassField representation of a field of an Object
	 * @param value the value that we wish to set the field's value to
	 */
	public void setObjectValue(ClassField classField, Object value)
	{
		//we don't set accessible true because the ClassField automatically sets accessible to true
		try{
			classField.getField().set(classField.getParentObject(), value);
		}catch(Exception ex)
		{
			System.out.println("Error setting field. " + ex.getMessage());
		}
	}
		
	/**
	 * getObjectHandler is a simple wrapper to create an ObjectHandler based on a className that handles all possible thrown
	 * exceptions.
	 * 
	 * @param className the name of the class for which we desire an ObjectHandler (ie. "java.lang.String", "packageName.ClassName")
	 * @return an ObjectHandler that represents the instantiation of the Object, or null if there was an error in creating the ObjectHandler
	 */
	public ObjectHandler getObjectHandler(String className)
	{
		ObjectHandler objHandler = null;
		
		try{
			objHandler = new ObjectHandler(className);
			
		}catch(IllegalAccessException ex)
		{
			System.out.println("Whoops. Illegal access exception thrown. Please choose an object with which you have access. " + ex.getMessage());
		}catch(InstantiationException ex)
		{
			System.out.println("Whoops. Instantiation exception thrown. Could not instantiate object. " + ex.getMessage());
		}catch(ClassNotFoundException ex)
		{
			System.out.println("Class not found. Please ensure that the class exists.");
		}
		
		return objHandler;
	}
	
	/**
	 * getMenuOption attempts to retrieve a menu option from a specified menu
	 * 
	 * @param menu The Menu for which an option is desired.
	 * @param delimiter a delimiter that will prefix all output.
	 * @return an Integer representation of the option chosen.
	 */
	public Integer getMenuOption(Menu menu, String delimiter)
	{
		Integer choice = -1;
		try{
			choice = menu.displayMenuGetOption(delimiter);
		}catch(NoMenuOptionsException ex)
		{
			System.out.println(delimiter + ex.getMessage());
		}
		
		return choice;
	}
	
	/**
	 * 
	 * @param options
	 * @param menu
	 */
	public void initializeMenu(String[] options, Menu menu){
		int i;
		
		for (i = 0; i < options.length; i++)
			menu.addOption(i, options[i]);
		
		menu.addOption(i, "Exit");
		menu.setCancelOption(i);
	}

	/**
	 * 
	 * @param message
	 * @return
	 */
	@SuppressWarnings("resource")
	public Integer getInt(String message, String delimiter)
	{
		Scanner keyboard = new Scanner(System.in);
		
		Integer choice = -1;
		
		do{
			System.out.print(delimiter + message);
			String input = keyboard.nextLine();
			
			try{
				choice = Integer.parseInt(input);
			}catch(NumberFormatException ex)
			{
				System.out.println(delimiter + "Invalid entry. Please enter a number.");
			}
		}while(choice == -1);
		
		//keyboard is not closed because it is used in the menu system, closing System.in will crash the program
		
		return choice;
	}
	

	/**
	 * 
	 * @param message
	 * @return
	 */
	@SuppressWarnings("resource")
	public Character getYesNo(String message, String delimiter)
	{
		Scanner keyboard = new Scanner(System.in);
		
		Character choice = 'x';
		
		do{
			System.out.print(delimiter + message);
			String input = keyboard.nextLine().toUpperCase();
			
			if (input.charAt(0) != 'Y' && input.charAt(0) != 'N')
				System.out.println(delimiter + "Invalid entry. Please enter 'y' or 'n'.");
			else
				choice = input.charAt(0);

		}while(choice == 'x');
		
		//keyboard is not closed because it is used in the menu system, closing System.in will crash the program
		
		return choice;
	}
	
	@SuppressWarnings("resource")
	public String getLine(String message, String delimiter)
	{
		Scanner keyboard = new Scanner(System.in);

		System.out.print(delimiter + message);
		String input = keyboard.nextLine();
		
		//keyboard is not closed because it is used in the menu system, closing System.in will crash the program

		return input;
	}
	
}
