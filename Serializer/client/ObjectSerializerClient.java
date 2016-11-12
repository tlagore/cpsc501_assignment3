package client;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Scanner;
import java.util.Vector;

import serializer.ArrayField;
import serializer.ClassField;
import serializer.ObjectField;
import serializer.ObjectHandler;
import serializer.PrimitiveField;

public class ObjectSerializerClient {
	private Menu _MainMenu;
	private final String DELIMITER = "  ";
	
	public ObjectSerializerClient()
	{
		
	}
	
	/**
	 * 
	 */
	public void run()
	{
		String[] classes = new String[] { "classes.ClassA", "classes.ClassB", "classes.ClassC" };
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
	 * @param objHandler
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
	 * 
	 * @param classField
	 */
	public void setField(ClassField classField, String delimiter)
	{
		ObjectHandler objHandler;
		Object obj = classField.getValue();
		
		if(classField instanceof ArrayField){
			if (obj == null)
			{
				createArray(classField, delimiter);
			}else
			{
				//set array values	
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
	
	public void createArray(ClassField classField, String delimiter)
	{
		//ensure handed in class is an array
		Field field = classField.getField();
		if (field.getType().isArray())
		{
			Integer size = getInt("Please enter array size: ", delimiter);
			try{
				field.set(classField.getParentObject(), Array.newInstance(field.getType().getComponentType(), size));
				
				if(getYesNo("Would you like to fill array now? (y/n): ", delimiter) == 'Y')
				{
					//fill array
				}
			}catch(Exception ex)
			{
				System.out.println(ex.getMessage());
			}
			
		}
	}
	
	/**
	 * 
	 * @param classField
	 * @param delimiter
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
	 * 
	 * @param typeName
	 * @param value
	 * @return
	 */
	public Object getPrimitiveObject(String typeName, String value)
	{
		Object retVal = null;
		
		try{
			switch(typeName)
			{
				case "byte":
					retVal = new Byte(Byte.parseByte(value));
					break;
				case "short":
					retVal = new Short(Short.parseShort(value));
					break;
				case "int":
					retVal = new Integer(Integer.parseInt(value));
					break;
				case "long":
					retVal = new Long(Long.parseLong(value));
					break;
				case "float":
					retVal = new Float(Float.parseFloat(value));
					break;
				case "double":
					retVal = new Double(Double.parseDouble(value));
					break;
				case "boolean":
					retVal = new Boolean(Boolean.parseBoolean(value));
					break;
				case "char":
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
	 * 
	 * @param typeName
	 * @return
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
				retVal = "number";
				break;
			case "long":
				retVal = "number";
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
		
	public ObjectHandler getObjectHandler(String str)
	{
		ObjectHandler objHandler = null;
		
		try{
			objHandler = new ObjectHandler(str);
			
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
	 * 
	 * @param menu
	 * @return
	 */
	public Integer getMenuOption(Menu menu, String delimiter)
	{
		Integer choice = -1;
		try{
			choice = menu.displayMenuGetOption(delimiter);
		}catch(NoMenuOptionsException ex)
		{
			System.out.println(ex.getMessage());
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
		
		//keyboard.close();
		
		return choice;
	}
	

	/**
	 * 
	 * @param message
	 * @return
	 */
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
		
		//keyboard.close();
		
		return choice;
	}
	
}
