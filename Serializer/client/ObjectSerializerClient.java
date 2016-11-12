package client;

import java.util.Scanner;
import java.util.Vector;

import serializer.ClassField;
import serializer.ObjectHandler;

public class ObjectSerializerClient {
	private Menu _MainMenu;
	
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
		choice = getMenuOption(_MainMenu);
		while(choice != _MainMenu.getCancelOption() && choice != -1)
		{
			try{
				ObjectHandler objHandler = new ObjectHandler(classes[choice]);
				instantiateObjectFields(objHandler);
				
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
			_MainMenu.displayTitle();
			choice = getMenuOption(_MainMenu);
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
	public void instantiateObjectFields(ObjectHandler objHandler)
	{
		Menu fieldMenu = new Menu("Set fields for " + objHandler.getRootClass().getName(), System.in, System.out);
		Vector<ClassField> fields = objHandler.getFields();
		Integer choice;
		
		initializeMenu(objHandler.getFieldNames(), fieldMenu);
		
		fieldMenu.displayTitle();
		choice = getMenuOption(fieldMenu);
		
		while(choice != fieldMenu.getCancelOption() && choice != -1)
		{
			
		
			fieldMenu.displayTitle();
			choice = getMenuOption(fieldMenu);
		}
	}
	
	/**
	 * 
	 * @param menu
	 * @return
	 */
	public Integer getMenuOption(Menu menu)
	{
		Integer choice = -1;
		try{
			choice = menu.displayMenuGetOption();
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
