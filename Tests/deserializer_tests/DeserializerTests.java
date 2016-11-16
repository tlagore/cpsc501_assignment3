package deserializer_tests;

import static org.junit.Assert.assertEquals;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.junit.Test;

import deserializer.Deserializer;
import serializer.Serializer;

public class DeserializerTests {

	@Test
	public void testGetObjectFromElement() {
		Deserializer deserializer = new Deserializer();
		
		Element el = new Element("object");
		el.setAttribute(new Attribute("class", "classes.ClassA"));
		Object obj = deserializer.getObjectFromElement(el);
		
		assertEquals(obj instanceof classes.ClassA, true);
		
		Element el1 = new Element("object");
		el1.setAttribute(new Attribute("class", "classes.ClassB"));
		Object obj1 = deserializer.getObjectFromElement(el1);
		
		assertEquals(obj1 instanceof classes.ClassB, true);
		
		Element el2 = new Element("object");
		el2.setAttribute(new Attribute("class", "classes.ClassC"));
		Object obj2 = deserializer.getObjectFromElement(el2);
		
		assertEquals(obj2 instanceof classes.ClassC, true);
		
		Element el3 = new Element("object");
		el3.setAttribute(new Attribute("class", "classes.ClassD"));
		Object obj3 = deserializer.getObjectFromElement(el3);
		
		assertEquals(obj3 instanceof classes.ClassD, true);
	}
	
	@Test
	public void testDeserialize()
	{
		String str = new String("hello");
		Serializer serializer = new Serializer();
		Deserializer deserializer = new Deserializer();
		
		//serialize then immediately deserialize object into new object and ensure fields match
		Object newStr = deserializer.deserialize(serializer.serialize((Object)str));
		assertEquals(((String)newStr).length(), str.length());
		assertEquals(((String)newStr).compareTo(str) == 0, true);
		
		classes.ClassA classA = new classes.ClassA();
		classA.setBoolVar(true);
		classA.setThing(999);
		
		classes.ClassA newObj = (classes.ClassA)deserializer.deserialize(serializer.serialize((Object)classA));	
		assertEquals(classA.getThing(), newObj.getThing());
		assertEquals(classA.getBoolVar(), newObj.getBoolVar());
	}

}
