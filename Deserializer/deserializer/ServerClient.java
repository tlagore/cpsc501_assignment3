package deserializer;

import java.util.Scanner;

public class ServerClient {
	public static void main(String[] args)
	{
		int port = 2255;
		DeserializerServer ds = new DeserializerServer(port);
		System.out.println("Server started on port " + port);
		ds.start();
		
		Scanner keyboard = new Scanner(System.in);
		System.out.print("Press any key to terminate...");
		keyboard.nextLine();
		ds.shutdown();
		
		keyboard.close();
	}
}
