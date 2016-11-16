package client;

import classes.ClassB;
import serializer.Serializer;

public class Client {
	
	public static void main(String[] args)
	{
		ObjectSerializerClient client = new ObjectSerializerClient();
		client.run();
	}
}
