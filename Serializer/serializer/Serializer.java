package serializer;

import java.io.FileWriter;
import java.util.IdentityHashMap;
import java.util.Vector;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Serializer {
	private IdentityHashMap<Integer, Object> _SerializedObjects;
	
	public Serializer()
	{
		_SerializedObjects = new IdentityHashMap<Integer, Object>();
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
		
		for(int i  = 0; i < fields.size(); i++)
		{
			if(fields.elementAt(i) instanceof PrimitiveField){
				serializePrimitive(fields.elementAt(i), doc);
				
			}else if (fields.elementAt(i) instanceof ObjectField){
				serializeObject(fields.elementAt(i), doc);
			
			}else if (fields.elementAt(i) instanceof ArrayField){
				serializeArray(fields.elementAt(i), doc);
				
			}else if (fields.elementAt(i) instanceof CollectionField){
				serializeCollection(fields.elementAt(i), doc);
			}
		}
		
		return doc;
	}
	
	
	public void serializePrimitive(ClassField classField, Document doc)
	{		
		Element fieldElement = new Element("field");
		Attribute fieldName = new Attribute("name", classField.getField().getName());
		Attribute declaringClass = new Attribute("declaringclass", classField.getParentObject().getClass().getName());
		
		
		fieldElement = fieldElement.setAttribute(fieldName);
		fieldElement = fieldElement.setAttribute(declaringClass);
		try{
			fieldElement.addContent(new Element("value").setText(classField.getField().get(classField.getParentObject()).toString()));	
		}catch(IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
		
		doc.getRootElement().addContent(fieldElement);
	}
	
	private void serializeCollection(ClassField elementAt, Document doc) 
	{
		// TODO Auto-generated method stub
	}

	private void serializeArray(ClassField elementAt, Document doc) 
	{
		// TODO Auto-generated method stub
	}

	private void serializeObject(ClassField classField, Document doc) 
	{
		// TODO Auto-generated method stub
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
