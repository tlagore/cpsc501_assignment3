package client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Scanner;

public class Menu implements Closeable {
	private String _Title;
	private HashMap<Integer, String> _Options;
	private Scanner _Input;
	private PrintStream _Output;
	
	public Menu(String title, InputStream is, PrintStream os)
	{
		_Output = os;
		_Title = title;
		_Options = new HashMap<Integer, String>();
		_Input = new Scanner(is);
		_Output = os;
	}
	
	/**
	 * adds a new option to the menu if the key does not already exist in the menu
	 * 
	 * @param option the value of the option
	 */
	public void addOption(Integer key, String option)
	{
		if (!_Options.containsKey(key)){
			_Options.put(key, option);
		}
	}
	
	public Integer displayMenuGetOption()
	{
		displayMenu();
		return getOption("Enter a choice: ");
	}
	
	public void displayMenu()
	{
		for (Integer key: _Options.keySet())
		{
			_Output.println(key.toString() + ") " + _Options.get(key));
		}
	}
	
	public void displayTitle()
	{
		_Output.println(_Title);
	}
	
	public Integer getOption(String message)
	{
		Integer choice = -1;
		String input;
		
		_Output.print(message);
		do
		{
			input = _Input.nextLine();
			try{
				choice = Integer.parseInt(input);
			}catch(NumberFormatException ex)
			{
				System.out.println("Invalid entry");
			}
			
			if(!_Options.containsKey(choice))
				System.out.println("That is not a valid option.");
			
		}while (!_Options.containsKey(choice));
		
		return choice;
	}
	
	@Override
	public void close() throws IOException
	{
		try{
			_Input.close();
		}catch(Exception ex){
			throw new IOException(ex.getMessage());
		}
	}
}
