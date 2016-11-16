package serializer_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Test;

import serializer.Serializer;

public class SerializerTests {

	@Test
	public void testSerializePrimitive() {
		Serializer serializer = new Serializer();
		Document serializedDoc = serializer.serialize((Object)new classes.ClassA());
		
		Element rootElement = serializedDoc.getRootElement();
		List<Element> children = rootElement.getChildren();
		
		Element thisEl = children.get(0);
		assertEquals(thisEl.getName().compareTo("object") == 0, true);
		assertEquals(thisEl.getAttributeValue("class").compareTo("classes.ClassA") == 0, true);
		
		List<Element> thisElChildren = thisEl.getChildren();
		thisEl = thisElChildren.get(0);
		assertEquals(thisEl.getName().compareTo("field") == 0, true);
		assertEquals(thisEl.getAttributeValue("name").compareTo("boolVar") == 0, true);
		assertEquals(thisEl.getAttributeValue("declaringclass").compareTo("classes.ClassA") == 0, true);
		
		thisEl = thisElChildren.get(1);
		assertEquals(thisEl.getName().compareTo("field") == 0, true);
		assertEquals(thisEl.getAttributeValue("name").compareTo("thing") == 0, true);
		assertEquals(thisEl.getAttributeValue("declaringclass").compareTo("classes.ClassA") == 0, true);
		
		thisEl = thisElChildren.get(2);
		assertEquals(thisEl.getName().compareTo("field") == 0, true);
		assertEquals(thisEl.getAttributeValue("name").compareTo("type") == 0, true);
		assertEquals(thisEl.getAttributeValue("declaringclass").compareTo("classes.ClassA") == 0, true);
		
		thisEl = thisElChildren.get(3);
		assertEquals(thisEl.getName().compareTo("field") == 0, true);
		assertEquals(thisEl.getAttributeValue("name").compareTo("arr") == 0, true);
		assertEquals(thisEl.getAttributeValue("declaringclass").compareTo("classes.ClassA") == 0, true);
		
		thisEl = thisElChildren.get(4);
		assertEquals(thisEl.getName().compareTo("field") == 0, true);
		assertEquals(thisEl.getAttributeValue("name").compareTo("objArr") == 0, true);
		assertEquals(thisEl.getAttributeValue("declaringclass").compareTo("classes.ClassA") == 0, true);
	
		thisEl = children.get(1);
		assertEquals(thisEl.getName().compareTo("object") == 0, true);
		assertEquals(thisEl.getAttributeValue("class").compareTo("[Lclasses.ClassC;") == 0, true);
		assertEquals(thisEl.getAttributeValue("size").compareTo("2") == 0, true);
	}

}
