package serializer_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

import serializer.ObjectHandler;
import serializer.Serializer;

public class SerializerTests {

	@Test
	public void testSerializePrimitive() {
		Serializer serializer = new Serializer();
		Document doc = new Document(new Element("serialized"));
		String[] values = new String[]{"hello", "ch", "bool"};
		
		try{
			ObjectHandler objHandler = new ObjectHandler("classes.ClassD");
			for(int i = 0; i < objHandler.getFields().size(); i++)
			{
				serializer.serializePrimitive(objHandler.getFields().elementAt(i), doc);
			}
			
			List<Element> els = doc.getRootElement().getChildren();
			
			for(int i = 0; i < els.size(); i++)
			{
				Attribute at = els.get(i).getAttribute("declaringclass");
				assertEquals(at.getValue(), "classes.ClassD");
				
				Attribute atName = els.get(i).getAttribute("name");
				boolean inValues = false;
				for(int j = 0; j < values.length; j++)
					if(atName.getValue().compareTo(values[i]) == 0)
						inValues = true;
				
				assertEquals(inValues, true);
			}
			
		}catch(Exception ex)
		{
			fail("Should instantiate");
		}
		

		
	}

}
