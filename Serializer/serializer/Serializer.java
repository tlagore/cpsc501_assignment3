package serializer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.IdentityHashMap;
import java.util.Vector;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 * Serializer handles the Serialization of Objects and returns a JDOM Document object to be Deserialized
 * by the class Deserializer.
 * 
 * @author Tyrone Lagore
 *
 */
public class Serializer {
	private IdentityHashMap<Integer, Object> _SerializedObjects;
	
	public Serializer()
	{
		_SerializedObjects = new IdentityHashMap<Integer, Object>();
	}
	
	/** 
	 * serialize takes in an Object and returns a JDOM Document representation of the object.
	 * 
	 * Serialize assumes that the base level object being handed in is <b>not</b> an array object,
	 * but could handle such objects with minor tweaking.
	 * 
	 * @param obj The object to serializer
	 * @return
	 */
	public Document serialize(Object obj)
	{
		Element serialized = new Element("serialized");
		Document doc = new Document(serialized);
		
		serializeObject(obj, doc.getRootElement(), doc);
		
		return doc;
	}
	
	/**
	 * serializeObject serializes a single Object, then calls iterateObjectFields to serialize the Objects fields
	 * 
	 * @param obj The object being serialized
	 * @param root The root Element to which the object will belong
	 * @param doc The base Document of the Serializer
	 */
	private void serializeObject(Object obj, Element root, Document doc) 
	{
		int objId = -1;	
		Element objectElement;
		Attribute classAtt, idAtt;
		
		if(obj != null)
		{
			objId = System.identityHashCode(obj);
			if(!_SerializedObjects.containsKey(objId))
			{
				objectElement = new Element("object");
				classAtt = new Attribute("class", obj.getClass().getName());
				idAtt = new Attribute("id", String.valueOf(objId));
				
				_SerializedObjects.put(objId, obj);
				
				objectElement.setAttribute(classAtt);
				objectElement.setAttribute(idAtt);
				
				root.addContent(objectElement);
				iterateObjFields(obj, objectElement, doc);
			}
		}	
	}
	
	/**
	 * serializeArrayObject recursively serializes an ArrayObject
	 * 
	 * @param obj The object representing the Array
	 * @param root The root Element to which the array belongs
	 * @param doc The base Document of the Serializer
	 */
	@SuppressWarnings("unused")
	private void serializeArrayObject(Object obj, Element root, Document doc) 
	{
		int objId = -1;	
		Element objectElement, valueElement;
		Attribute classAtt, idAtt, sizeAtt;
		Object nextArrObj;
		
		if(obj != null && obj.getClass().isArray())
		{
			objId = System.identityHashCode(obj);
			if(!_SerializedObjects.containsKey(objId))
			{
				objectElement = new Element("object");
				classAtt = new Attribute("class", obj.getClass().getName());
				idAtt = new Attribute("id", String.valueOf(objId));
				sizeAtt = new Attribute("size", String.valueOf(Array.getLength(obj)));
				
				_SerializedObjects.put(objId, obj);
				
				objectElement.setAttribute(classAtt);
				objectElement.setAttribute(idAtt);
				objectElement.setAttribute(sizeAtt);
				
				root.addContent(objectElement);
				
				for(int i = 0; i < Array.getLength(obj); i++)
				{
					nextArrObj = Array.get(obj, i);
					valueElement = new Element("reference");
					
					if(nextArrObj != null)
					{
						if(nextArrObj.getClass().isPrimitive() || ObjectHandler.isWrapper(nextArrObj.getClass()))
						{
							valueElement = new Element("value");
							valueElement.setText(nextArrObj.toString());
						}else if(nextArrObj.getClass().isArray())
						{
							valueElement.setText(String.valueOf(nextArrObj.hashCode()));
							_SerializedObjects.put(nextArrObj.hashCode(), nextArrObj);
							
							serializeArrayObject(nextArrObj, root, doc);
						}else
						{
							valueElement.setText(String.valueOf(nextArrObj.hashCode()));
							_SerializedObjects.put(nextArrObj.hashCode(), nextArrObj);
							
							serializeObject(nextArrObj, doc.getRootElement(), doc);
						}
					}else
						valueElement.setText("");
					
					objectElement.addContent(valueElement);
				}
			}
		}	
	}
	
	/**
	 * serializeObjectOrArray Reflectively calls a passed in Method with specified parameters to serialize an Object or an Array
	 * 
	 * @param method The method representing the method that should be called (either "serializeArrayObject" or "serializeObject"
	 * @param params The parameters for the methods, <p>{ "Object" representing the object being serialized, <p>"Element" representing the root element being serialized, <p>"Document" representing the base Document } 
	 * @param fieldElement the Element that the Object or Array is represented by
	 * @param curField The field that represents the Object or Array
	 * @param parentObj The parent Object to which the array or object belong
	 */
	public void serializeObjectOrArray(Method method, Object[] params, Element fieldElement, Field curField, Object parentObj)
	{
		String value;

		Element reference = new Element("reference");
		try{
			curField.setAccessible(true);
			params[0] = curField.get(parentObj);
		}catch(IllegalAccessException ex)
		{
			System.out.println("IllegalAccessExcetpion thrown. " + ex.getMessage());
		}
		
		value = "";
		if (params[0] != null)
		{
			value = String.valueOf(params[0].hashCode());
			//serializeObject(objVal, doc.getRootElement(), doc);
			try{
				method.invoke(this, params);
			}catch (Exception ex)
			{
				System.out.println("Error calling method. " + ex.getMessage());
			}
		}			
		
		reference.setText(value);
		fieldElement.addContent(reference);
	}
	
	/**
	 * iterateObjFields takes in an Object, a root Element to which the Object is represented by, and the base Document
	 * for which the final serialized object will represent.
	 * <p>

	 * @param obj The Object for which the fields need to be serialized
	 * @param root The root Element to which the fields will be added
	 * @param doc The base Document for which the final serialized object will represent.
	 */
	public void iterateObjFields(Object obj, Element root, Document doc)
	{
		Field curField;
		ObjectHandler objHandler = new ObjectHandler(obj);
		Vector<ClassField> fields = objHandler.getFields();
		
		for(int i  = 0; i < fields.size(); i++)
		{
			curField = fields.elementAt(i).getField();
			
			Element fieldElement = getFieldElement(obj, curField);			
			
			if(fields.elementAt(i) instanceof PrimitiveField){
				serializePrimitive(obj, curField, fieldElement);
				
			}else if (fields.elementAt(i) instanceof ObjectField){
				try{
					Method method = this.getClass().getDeclaredMethod("serializeObject", new Class[]{Object.class, Element.class, Document.class});
					serializeObjectOrArray(method, new Object[]{null, doc.getRootElement(), doc}, fieldElement, curField, obj);
				}catch(NoSuchMethodException ex)
				{
					System.out.println("Cannot find method. " + ex.getMessage());
				}
			}else if (fields.elementAt(i) instanceof ArrayField){
				try{
					Method method = this.getClass().getDeclaredMethod("serializeArrayObject", new Class[]{Object.class, Element.class, Document.class});
					serializeObjectOrArray(method, new Object[]{null, doc.getRootElement(), doc}, fieldElement, curField, obj);
				}catch(NoSuchMethodException ex)
				{
					System.out.println("Cannot find method. " + ex.getMessage());
				}
			}else if (fields.elementAt(i) instanceof CollectionField){
				//serializeCollection(parentObj, curField, doc);
			}
			
			root.addContent(fieldElement);
		}
	}
	
	/**
	 * Gets a field element with properly set name and declaringclass attributes based on the object and field passed in.
	 * 
	 * @param obj The object to which the field belongs
	 * @param field The field being represented by the returned Element object
	 * @return an Element with properly set attributes
	 */
	public Element getFieldElement(Object obj, Field field)
	{
		Element fieldElement = new Element("field");
		Attribute fieldName = new Attribute("name", field.getName());
		Attribute declaringClass = new Attribute("declaringclass", obj.getClass().getName());
		
		fieldElement.setAttribute(fieldName);
		fieldElement.setAttribute(declaringClass);
		
		return fieldElement;
	}
	
	/**
	 * serializePrimitive takes in a parentObject, a field representing a primitive and a fieldElement representing a generic
	 * field and adds an Element representing the value of the primitive to the fieldElement
	 * 
	 * @param parentObj The object to which the field belongs
	 * @param field The field object representing the primitive field
	 * @param fieldElement A generic Element representing a field
	 */
	public void serializePrimitive(Object parentObj, Field field, Element fieldElement)
	{		
		try{
			field.setAccessible(true);
			fieldElement.addContent(new Element("value").setText(field.get(parentObj).toString()));	
		}catch(IllegalAccessException ex)
		{
			ex.printStackTrace();
		}
	}
	
	/**
	 * serializeCollection in the future will take in an Object that represents a collection and serialize
	 * the elements of the collection.
	 * 
	 * Currently not implemented
	 * 
	 * @param parentObj the Object to which the field representing the collection belongs
	 * @param field the Field Object that represents the Collection
	 * @param root The root Element of the to which content of the serialized Collection will belong to
	 */
	@SuppressWarnings("unused")
	private void serializeCollection(Object parentObj, Field field, Element root) 
	{
		// TODO Auto-generated method stub
	}
	
	/**
	 * writeXML takes in a Document and a path and attempts to write the XML that represents the Document to file.
	 * 
	 * @param doc The Document object for which the XML is desired
	 * @param strPath A fully qualified string representation of the desired file location
	 */
	public void writeXML(Document doc, String strPath)
	{
		Path path = Paths.get(strPath);
		File file = new File(strPath);
		
		if(!Files.exists(path))
		{
			file.getParentFile().mkdirs();

			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			
			try{
				xmlOutput.output(doc, new FileWriter(strPath));
			}catch(Exception ex)
			{
				System.out.println(ex.getMessage());
			}
		}
	}
	
	/**
	 * documentToString takes in a JDOM Document and returns the XML String representation of the Document
	 * @param doc The Document for which the XML is desired
	 * @return a string representation of the XML of the Document object
	 */
	public static String documentToString(Document doc)
	{
		XMLOutputter outputter = new XMLOutputter();
		return outputter.outputString(doc);
	}


}
