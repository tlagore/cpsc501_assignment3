package client;

import classes.ClassB;
import serializer.Serializer;

public class Client {
	
	public static void main(String[] args)
	{
		//ObjectSerializerClient client = new ObjectSerializerClient();
		//client.run();
		
		Serializer serializer = new Serializer();
		serializer.serialize((Object)new ClassB());
		
		//Serializer serializer = new Serializer();
		//serializer.serialize(new classes.ClassD());
		//serializer.writeXML(serializer.serialize(new classes.ClassD()));
	}
}
