package deserializer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * @author Tyrone Lagore
 */
public class DeserializerServer extends Thread{
	private final int MAX_IN_SIZE = 10*1024;
	
	private ServerSocket _ServerSocket;
	private boolean _Shutdown;

	public DeserializerServer(int port){
		try {
			_Shutdown = false;
			_ServerSocket = new ServerSocket(port);
		}catch(IOException ex)
		{
			System.out.println("Error instantiating server socket for port " + port + ". " + ex.getMessage());
		}
	}
	
	/**
	 * run runs a loop waiting for socket connections. When it acquires a connection, it creates a thread
	 * to handle any requests for that socket and then continues to wait for connections.
	 */
	public void run()
	{
		try{
			while(!_Shutdown)
			{
				Socket socket = _ServerSocket.accept();
				System.out.println("Acquired connection.");
				readSocket(socket);
			}
		}catch(IOException ex)
		{
			System.out.println("Server: " + ex.getMessage());
		}
		System.out.println("Server: Server has shut down.");
	}
	
	public void readSocket(Socket socket)
	{
		PrintWriter outputStream;
		byte[] input = new byte[MAX_IN_SIZE];
		String docString = "";
		int amountRead;
		
		try{
			outputStream = new PrintWriter(new DataOutputStream(socket.getOutputStream()));
			
			amountRead = socket.getInputStream().read(input);
			
			docString = extractStringFromByte(input);
			
			System.out.println(docString);
			
			try{
				SAXBuilder saxBuilder = new SAXBuilder();
				Document doc = saxBuilder.build(new StringReader(docString));
				
				Deserializer deserializer = new Deserializer();
				deserializer.deserialize(doc);
			}catch(JDOMException ex)
			{
				System.out.println("JDOMException: " + ex.getMessage());
			}catch(IOException ex)
			{
				System.out.println("IOException: " + ex.getMessage());
			}
			
			outputStream.close();
			socket.close();
			System.out.println();
		}catch(IOException ex)
		{
			System.out.println("Error: " + ex.getMessage());
		}		
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
				command += line;
				line  = reader.readLine();
			}
		}catch(IOException ex)
		{
			System.out.println("Error: " + ex.getMessage());
		}
		
		return command.trim();
	}
	
	/**
	 * shutdown tells the ExecutorService to close all current threads, closes the current socket, and terminates the 
	 * loop that waits for connections.
	 */
	public void shutdown(){
		System.out.println("Server: Received shutdown request, shutting down...");
		try{
			_Shutdown = true;
			_ServerSocket.close();
		}catch(IOException ex)
		{
			System.out.println("Server: Error closing socket: " + ex.getMessage());
		}
	}

}
