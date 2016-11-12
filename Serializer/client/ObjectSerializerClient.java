package client;

import java.util.Scanner;
import java.util.Vector;

import serializer.ClassField;
import serializer.ObjectHandler;

public class ObjectSerializerClient {
	private Menu _MainMenu;
	private Integer _ExitOption;
	
	public ObjectSerializerClient()
	{
		
	}
	
	public void run()
	{
		String[] classes = new String[] { "classes.ClassA", "classes.ClassB", "classes.ClassC" };
		Scanner keyboard = new Scanner(System.in);
		Integer choice;
		
		initializeMainMenu(classes);
		
		_MainMenu.displayTitle();
		choice = _MainMenu.displayMenuGetOption();
		while(choice != _ExitOption)
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
			choice = _MainMenu.displayMenuGetOption();
		}
		
		System.out.println("Thank you for using the Object Serializer.");
		System.out.println("Press any key to exit");
		keyboard.nextLine();
		keyboard.close();
	}
	
	public void instantiateObjectFields(ObjectHandler objHandler)
	{
		Menu fieldMenu = new Menu("Set fields for " + objHandler.getRootClass().getName(), System.in, System.out);
		Vector<ClassField> fields = objHandler.getFields();
		Integer choice,
			exitOption;
		int i;
		
		for(i = 0; i < fields.size(); i++){
			fieldMenu.addOption(i, fields.elementAt(i).getTypeName() + " " + fields.elementAt(i).getName());
		}
		
		fieldMenu.addOption(i, "Exit");
		exitOption = i;
		
		fieldMenu.displayTitle();
		choice = fieldMenu.displayMenuGetOption();
		
		while(choice != exitOption)
		{
			
		
			fieldMenu.displayTitle();
			choice = fieldMenu.displayMenuGetOption();
		}
		
	}
	
	public void initializeMainMenu(String[] options)
	{
		_MainMenu = new Menu("Welcome to the Object Serializer. Please choose an object to instantiate.", System.in, System.out);
		int i;
		
		for (i = 0; i < options.length; i++)
			_MainMenu.addOption(i, options[i]);
		
		_MainMenu.addOption(i, "Exit");
		_ExitOption = i;
	}
	
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
