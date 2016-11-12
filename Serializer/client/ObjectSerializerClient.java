package client;

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
		if(classField instanceof ArrayField){
			//handle array
		}else if(classField instanceof ObjectField){
			ObjectHandler objHandler = getObjectHandler(classField.getTypeName());
			instantiateObjectFields(objHandler, delimiter + DELIMITER);				
		}else if(classField instanceof PrimitiveField)
		{
			System.out.println("primitive detected");
			try{
				classField.getField().setAccessible(true);
				classField.getField().set(classField.getParentObject(), (Object)(new Integer(5)));
			}catch(Exception ex)
			{
				System.out.println("Error accessing variable");
			}
		}else
		{
			//handle collection
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
	public Integer getInt(String message)
	{
		Scanner keyboard = new Scanner(System.in);
		
		Integer choice = -1;
		
		do{
			System.out.println(message);
			String input = keyboard.nextLine();
			
			try{
				choice = Integer.parseInt(input);
			}catch(NumberFormatException ex)
			{
				System.out.println("Invalid entry. Please enter a number.");
			}
		}while(choice == -1);
		
		keyboard.close();
		
		return choice;
	}
	
	
}
