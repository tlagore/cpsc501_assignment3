package deserializer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * WebServer takes in a port number and creates a ServerSocket on that port. It then waits in a loop
 * for HTTP requests. When it receives a socket connection, it creates a WebWorker thread to handle
 * the command and goes back to listening.
 * <p>
 * shutdown() can be called to terminate the WebServer loop.
 * 
 * @author Tyrone Lagore
 */
public class DeserializerServer extends Thread {
	
	//Thread throttle. Allow 15 threads at one time to be executing, rest are placed in a queue by ExecutorService
	private final int MAX_THREADS = 15;
	private boolean _Shutdown;
	private ServerSocket _ServerSocket;
	
	//Handles thread execution
	private ExecutorService _ExecutorService;
	
	public DeserializerServer(int port){
		_Shutdown = false;
		_ExecutorService = Executors.newFixedThreadPool(MAX_THREADS);
		try {
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
				_ExecutorService.execute(new WebWorker(socket));
			}
		}catch(IOException ex)
		{
			System.out.println("Server: " + ex.getMessage());
		}
		System.out.println("Server: Server has shut down. Remaining threads will shutdown shortly.");
	}
	
	/**
	 * shutdown tells the ExecutorService to close all current threads, closes the current socket, and terminates the 
	 * loop that waits for connections.
	 */
	public void shutdown(){
		System.out.println("Server: Received shutdown request, shutting down...");
		try{
			_ExecutorService.shutdownNow();
			System.out.println("Server: Sent shutdown quests to all threads.");
			_ServerSocket.close();
		}catch(IOException ex)
		{
			System.out.println("Server: Error closing socket: " + ex.getMessage());
		}
		
		_Shutdown = true;
	}

}
