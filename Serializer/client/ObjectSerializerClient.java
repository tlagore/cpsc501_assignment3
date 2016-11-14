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
				createArray(classField.getParentObject(), classField.getField(), delimiter);
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
	 * 
	 * @param obj
	 * @param delimiter
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
					//object
					try{
						ObjectHandler objHandler = new ObjectHandler(nextArrObj);
						instantiateObjectFields(objHandler, delimiter + DELIMITER);
					}catch(Exception ex)
					{
						System.out.println("Couldn't instantiate object.");
					}
				}
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
	
	public String getLine(String message, String delimiter)
	{
		Scanner keyboard = new Scanner(System.in);

		System.out.print(delimiter + message);
		String input = keyboard.nextLine();

		return input;
	}
	
}
