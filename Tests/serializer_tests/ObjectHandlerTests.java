package serializer_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import serializer.ObjectHandler;

public class ObjectHandlerTests {

	@Test
	public void testConstructors(){
		ObjectHandler handler;
		try{
			handler = new ObjectHandler("classes.ClassA");
			handler = new ObjectHandler("classes.ClassB");
			handler = new ObjectHandler("classes.ClassC");
			handler = new ObjectHandler("classes.ClassD");
			String obj = new String("hello");
			handler = new ObjectHandler((Object)obj);
		}catch(Exception ex)
		{
			fail();
		}
		
		try{
			handler = new ObjectHandler("saksljdal");
			
			//shouldn't hit this line because Exception is caught
			fail();
		}catch(Exception ex)
		{
			
		}
	}
	
	@Test
	public void testGetFieldNames() {
		ObjectHandler handler;
		try{
			handler = new ObjectHandler("classes.ClassA");
			String [] fields = handler.getFieldNames();
			
			assertEquals(fields[0].contains("boolean boolVar"), true);
			assertEquals(fields[1].contains("int thing"), true);
			assertEquals(fields[2].contains("char type"), true);
			assertEquals(fields[3].contains("int [][][] arr"), true);
			assertEquals(fields[4].contains("classes.ClassC [] objArr"), true);
			
		}catch(Exception Ex)
		{
			fail();
		}
	}
	
	@Test
	public void testGetArrayType()
	{
		assertEquals(ObjectHandler.getArrayType("B").compareTo("byte") == 0, true);
		assertEquals(ObjectHandler.getArrayType("C").compareTo("char") == 0, true);
		assertEquals(ObjectHandler.getArrayType("D").compareTo("double") == 0, true);
		assertEquals(ObjectHandler.getArrayType("F").compareTo("float") == 0, true);
		assertEquals(ObjectHandler.getArrayType("I").compareTo("int") == 0, true);
		assertEquals(ObjectHandler.getArrayType("J").compareTo("long") == 0, true);
		assertEquals(ObjectHandler.getArrayType("LFloat").compareTo("Float") == 0, true);
		assertEquals(ObjectHandler.getArrayType("S").compareTo("short") == 0, true);
		assertEquals(ObjectHandler.getArrayType("Z").compareTo("boolean") == 0, true);
		assertEquals(ObjectHandler.getArrayType("Q").compareTo("unknown") == 0, true);
	}

}
