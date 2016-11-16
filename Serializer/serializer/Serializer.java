package serializer;

import java.io.FileWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
		
		serializeObject(obj, doc.getRootElement(), doc);
		
		writeXML(doc);
		
		return doc;
	}
	
	/**
	 * 
	 * @param obj
	 * @param root
	 * @param doc
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
	 * 
	 * @param obj
	 * @param root
	 * @param doc
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
	 * Reflectively calls a passed in Method with specified parameters 
	 * 
	 * @param method
	 * @param params
	 * @param fieldElement
	 * @param curField
	 * @param parentObj
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
	 * 
	 * @param obj
	 * @param root
	 * @param doc
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
	 * 
	 * @param parentObj
	 * @param field
	 * @param fieldElement
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
	 * 
	 * @param parentObj
	 * @param field
	 * @param root
	 */
	@SuppressWarnings("unused")
	private void serializeCollection(Object parentObj, Field field, Element root) 
	{
		// TODO Auto-generated method stub
	}
	
	/**
	 * 
	 * @param doc
	 */
	public void writeXML(Document doc)
	{
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		try{
			xmlOutput.output(doc, new FileWriter("C:\\Users\\Tyrone\\Desktop\\test.xml"));
		}catch(Exception ex)
		{
			System.out.println(ex.getMessage());
		}
	}


}
