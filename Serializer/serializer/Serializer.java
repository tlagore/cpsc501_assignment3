package serializer;

import org.jdom2.Document;
import org.jdom2.Element;

public class Serializer {
	public Serializer()
	{
		
	}
	
	public Document serialize(Object obj)
	{
		Element serialized = new Element("serialized");
		Document doc = new Document(serialized);
		
		ObjectHandler objHandler = new ObjectHandler(obj);
		
		return doc;
	}
}
