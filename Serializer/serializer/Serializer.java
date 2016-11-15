package serializer;

import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Vector;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Serializer {
	private IdentityHashMap<Integer, Object> _SerializedObjects;
	private int _CurrentKey;
	
	public Serializer()
	{
		_SerializedObjects = new IdentityHashMap<Integer, Object>();
		_CurrentKey = 0;
	}
	
	/** 
	 * serialize takes in an Object and returns a JDOM Document representation of the object.
	 * @param obj
	 * @return
	 */
	public Document serialize(Object obj)
	{
		Element serialized = new Element("serialized");
		Document doc = new Document(serialized);
		
		ObjectHandler objHandler = new ObjectHandler(obj);
		Vector<ClassField> fields = objHandler.getFields();
		Object parentObj;
		Field curField;
		
		for(int i  = 0; i < fields.size(); i++)
		{
			parentObj = fields.elementAt(i).getParentObject();
			curField = fields.elementAt(i).getField();
			
			if(fields.elementAt(i) instanceof PrimitiveField){
				serializePrimitive(parentObj, curField, doc);
				
			}else if (fields.elementAt(i) instanceof ObjectField){
				serializeObject(parentObj, curField, doc);
			
			}else if (fields.elementAt(i) instanceof ArrayField){
				serializeArray(parentObj, curField, doc);
				
			}else if (fields.elementAt(i) instanceof CollectionField){
				serializeCollection(parentObj, curField, doc);
			}
		}
		
		writeXML(doc);
		
		return doc;
	}
	
	
	public void serializePrimitive(Object parentObj, Field field, Document doc)
	{		
		Element fieldElement = new Element("field");
		Attribute fieldName = new Attribute("name", field.getName());
		Attribute declaringClass = new Attribute("declaringclass", parentObj.getClass().getName());
		
		
		fieldElement = fieldElement.setAttribute(fieldName);
		fieldElement = fieldElement.setAttribute(declaringClass);
		try{
			field.setAccessible(true);
			fieldElement.addContent(new Element("value").setText(field.get(parentObj).toString()));	
		}catch(IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
		
		doc.getRootElement().addContent(fieldElement);
	}
	
	private void serializeCollection(Object parentObj, Field field, Document doc) 
	{
		// TODO Auto-generated method stub
	}

	private void serializeArray(Object parentObj, Field field, Document doc) 
	{
		// TODO Auto-generated method stub
	}

	private void serializeObject(Object parentObj, Field field, Document doc) 
	{
		int objId = -1;	
		Element objectElement;
		Attribute name, idAtt;
		Object obj = null;
		try{
			field.setAccessible(true);
			obj = field.get(parentObj);
		}catch(IllegalAccessException ex)
		{
			//IllegalAccessException
		}
		
		if(obj == null)
		{
			
		}else
		{
			objId = System.identityHashCode(obj);
			if(!_SerializedObjects.containsKey(objId))
			{
				objectElement = new Element("object");
				name = new Attribute("name", field.getName());
				idAtt = new Attribute("id", String.valueOf(objId));
				
				Field[] fields = obj.getClass().getDeclaredFields();
				for (int i = 0; i < fields.length; i++)
				{
					
				}
			}
		}	
	}
	
	public void writeXML(Document doc)
	{
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		try{
			xmlOutput.output(doc, new FileWriter("C:\\Users\\tyron\\Desktop\\test.xml"));
		}catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}


}
