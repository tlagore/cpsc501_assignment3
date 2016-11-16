package deserializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * WebWorker is a threaded class that receives a socket and awaits a command.
 * 
 * It parses the command then returns the appropriate header response and file information if necessary.
 * 
 * WebWorker can only handle simple GET requests and cannot handle any conditional information
 * 
 * @author Tyrone
 */
public class WebWorker extends Thread{
	private Socket _Socket;
	private final int MAX_IN_SIZE = 10*1024;
	
	public WebWorker(Socket socket)
	{
		_Socket = socket;
	}
	
	/**
	 * Attempts to retrieve a command from the sockets input stream. It then parses the retrieved command
	 * and returns a header specifying the HTTP code as well as requested file if the command is in good form. 
	 */
	public void run()
	{
		PrintWriter outputStream;
		byte[] input = new byte[MAX_IN_SIZE];
		String command = "";
		String response = "";
		int amountRead;
		
		System.out.println("Thread " + this.getId() + " handling a request.");
		try{
			outputStream = new PrintWriter(new DataOutputStream(_Socket.getOutputStream()));
			
			amountRead = _Socket.getInputStream().read(input);
			
			
			outputStream.close();
			_Socket.close();
			System.out.println();
		}catch(IOException ex)
		{
			System.out.println("Error in thread " + this.getId() + ": " + ex.getMessage());
		}		
		System.out.println("Thread " + this.getId() + " exiting.");
	}
	
	/**
	 * extractStringFromByte extracts the contents of a byte array into a string. The command is assumed to be delimited by 
	 * new line characters.
	 * 
	 * The characters of the array are *not* checked to ensure that they are String compatible characters.
	 * 
	 * @param data the byte array holding the command
	 * @return a String representation of the byte array
	 */
	private String extractStringFromByte(byte[] data)
	{
		String command = "";
		String line;
		InputStream inputStream = new ByteArrayInputStream(data);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		try
		{
			line = reader.readLine();
			while (line != null && !line.isEmpty())
			{
				command += line + "\r\n";
				line  = reader.readLine();
			}
		}catch(IOException ex)
		{
			System.out.println("Error: " + ex.getMessage());
		}
		
		return command;
	}
}
