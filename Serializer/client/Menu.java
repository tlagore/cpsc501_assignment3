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
	private Integer _CancelOption;
	
	public Menu(String title, InputStream is, PrintStream os)
	{
		_Output = os;
		_Title = title;
		_Options = new HashMap<Integer, String>();
		_Input = new Scanner(is);
		_Output = os;
		_CancelOption = -1;
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
	
	/**
	 * displays the menu and prompts for a choice.
	 * 
	 * @return the menu choice, or -1 if there are no options associated with the menu
	 */
	public Integer displayMenuGetOption(String delimiter) throws NoMenuOptionsException
	{
		if (_Options.size() > 0)
		{
			displayMenu(delimiter);
			return getOption(delimiter + "Enter a choice: ", delimiter);
		}else
			throw new NoMenuOptionsException("Menu has no options. Add options before calling this function.");
		
	}
	
	public void displayMenu(String delimiter)
	{
		for (Integer key: _Options.keySet())
		{
			_Output.println(delimiter + key.toString() + ") " + _Options.get(key));
		}
	}
	
	public void displayTitle()
	{
		_Output.println(_Title);
	}
	
	public Integer getOption(String message, String delimiter)
	{
		Integer choice = -1;
		String input = "";
			
		do
		{
			_Output.print(delimiter + message);
			
			input = _Input.nextLine();
			
			try{
				choice = Integer.parseInt(input);
				
				if(!_Options.containsKey(choice))
					System.out.println(delimiter + "That is not a valid option.");
				
			}catch(NumberFormatException ex)
			{
				System.out.println(delimiter + "Invalid entry");
			}
			
		}while (!_Options.containsKey(choice));
		
		return choice;
	}
	
	public void clearOptions()
	{
		_Options.clear();
		_CancelOption = -1;
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
	
	/**
	 * 
	 * @return
	 */
	public Integer getCancelOption()
	{
		return _CancelOption;
	}
	
	/**
	 * 
	 * @param option
	 */
	public void setCancelOption(Integer option)
	{
		_CancelOption = option;
	}
}
