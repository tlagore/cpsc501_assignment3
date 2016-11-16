package deserializer;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

/**
 * Deserializer handles receiving JDOM Documents representing serialized objects serialized by the Serializer class and 
 * returns the Object represented by the Document.
 * 
 * @author Tyrone Lagore
 *
 */
public class Deserializer {
	private HashMap<Integer, Object> _DeserializedObjects;
	private Object _RootObject;
	
	/**
	 * default and only constructor for Deserializer
	 */
	public Deserializer()
	{
		_DeserializedObjects = new HashMap<Integer,Object>();
		_RootObject = null;
	}
	
	/**
	 * deserialize takes in jdom Document and returns the deserialized object that it represents.
	 * 
	 * deserialize assumes that the first element of the children represents the root object.
	 * 
	 * deserialize cannot handle Collection 
	 * 
	 * @param doc The Document representing the serialized object
	 * @return the deserialized object
	 */
	public Object deserialize(Document doc)
	{
		_RootObject = null;
		
		try{
			Element rootElement = doc.getRootElement();
			List<Element> children = rootElement.getChildren();
			
			_RootObject = getObjectFromElement(children.get(0));	
			if (_RootObject != null)
			{
				_DeserializedObjects.put(Integer.valueOf(children.get(0).getAttributeValue("id")), _RootObject);
				populateObjects(children, 1);
				resolveReferences(children);
			}
		}catch(Exception ex)
		{
			System.out.println("General exception caught: " + ex.getMessage() + " " + ex.getCause());
		}
		
		//clear Deserialized objects in case function is called again with a new Document
		_DeserializedObjects.clear();
		
		return _RootObject;
	}
	
	/**
	 * resolveReferences takes in a list of child elements and resolves their references by iterating through
	 * the elements inner children.
	 * 
	 * resolveReferences assumes that the inner objects of the serialized document have already been individually
	 * deserialized by running "populateObjects", however will not run unless at least one object has been deserialized.
	 * 
	 * @param elements The children elements of a Document representing a serialized object.
	 */
	public void resolveReferences(List<Element> elements)
	{
		Element curElement;
		Element curField;
		List<Element> curElementChildren;
		
		Integer objId;
		if(_DeserializedObjects.size() != 0){
			for(int i = 0; i < elements.size(); i++)
			{
				curElement = elements.get(i);
				curElementChildren = curElement.getChildren();
				objId = Integer.valueOf(curElement.getAttributeValue("id"));
				
				for(int j = 0; j < curElementChildren.size(); j++)
				{
					curField = curElementChildren.get(j);
					
					if (curField.getName().compareTo("field") == 0)
					{
						resolveField(curField, objId);
					}else
					{
						if(curField.getName().compareTo("value") == 0)
						{
							resolvePrimitiveArray(curElementChildren, objId);
						}else
						{
							resolveObjectArray(curElementChildren, objId);
						}	
					}
				}
			}
		}else
			System.out.println("No deserialized objects to resolve references. Run populateObjects(List<Children>, startingIndex) first.");
	}
	
	/**
	 * resolvePrimitiveArray takes in an object ID representing an array object and the children of the Element 
	 * that represented the Array Object and sets each element of the array to the value of the child value.
	 * 
	 * this function assumes that the objId belongs to the Element to which the children belong and that the array
	 * object itself has already been deserialized. Additionally, it assumes that the represented array is a primitive array.
	 * 
	 * @param children The children of an Element array representing a deserialized Primitive Array object
	 * @param objId The ID attribute of the Element that represented array object
	 */
	public void resolvePrimitiveArray(List<Element> children, Integer objId)
	{
		
		Object array = _DeserializedObjects.get(objId);
		Element indexElement;
		String value;
		
		if (array.getClass().isArray())
		{
			for (int i = 0; i < children.size(); i++)
			{
				indexElement = children.get(i);
				value = indexElement.getText();
				
				Array.set(array, i, getPrimitiveObject(array.getClass().getComponentType().getName(), value));
			}
		}else
			System.out.println("Object is not an array. Use resolveField or check document format.");
	}
	
	/**
	 * resolveObjectArray takes in an object ID representing an array object and the children of the Element 
	 * that represented the Array Object and sets each element of the array to the REFERENCE value of the child value.
	 * 
	 * this function assumes that the objId belongs to the Element to which the children belong and that the array
	 * object itself has already been deserialized. Additionally, it assumes that the REFERENCE specified by the child
	 * element has also been deserialized.
	 * 
	 * @param children The children of an Element array representing a deserialized Object Array object
	 * @param objId The ID attribute of the Element that represented array object
	 */
	public void resolveObjectArray(List<Element> children, Integer objId)
	{
		Object array = _DeserializedObjects.get(objId);
		Element indexElement;
		String reference;
		
		if(array.getClass().isArray())
		{
			for (int i = 0; i < children.size(); i++)
			{
				indexElement = children.get(i);
				reference = indexElement.getText();
				
				Array.set(array, i, _DeserializedObjects.get(Integer.valueOf(reference)));
			}
		}else
			System.out.println("Object is not an array. Use resolveField or check document format.");
	}
	
	/**
	 * resolveField resolves a single field elements reference. resolveField can handle Object type references as well
	 * as literal primitives.
	 * 
	 * @param curField an Element representing the field being inspected
	 * @param objId The object ID of the deserialized object that the field belongs to
	 */
	public void resolveField(Element curField, Integer objId)
	{
		Element referenceOrValue;
		String value, reference;
		String className = curField.getAttributeValue("declaringclass");
		String fieldName = curField.getAttributeValue("name");
		
		Field field = getFieldFromClass(getClassForName(className), fieldName);
		if(field != null)
		{
			referenceOrValue = curField.getChildren().get(0);
			if(referenceOrValue.getName().compareTo("value") == 0)
			{
				value = referenceOrValue.getText();
				
				try{
					field.setAccessible(true);
					field.set(_DeserializedObjects.get(objId), getPrimitiveObject(field.getType().getName(), value));
					field.setAccessible(false);
				}catch(Exception ex)
				{
					System.out.println(ex.getMessage());
				}
			}else
			{
				reference = referenceOrValue.getText();
				try{
					if(reference.compareTo("") != 0){
						field.setAccessible(true);
						field.set(_DeserializedObjects.get(objId), _DeserializedObjects.get(Integer.valueOf(reference)));
						field.setAccessible(false);
					}
				}catch(Exception ex)
				{
					System.out.println(ex.getMessage());
				}
			}
		}
	}
	
	/**
	 * getFieldFromClass is a simple wrapper that handles the try/catch of attempting to get a Field from a Class by string name.
	 * 
	 * @param cl the Class to which the supposed field belongs
	 * @param name the string name of the field being retrieved
	 * @return a Field object representing the field requested, or null if the field is not found.
	 */
	@SuppressWarnings("rawtypes")
	public Field getFieldFromClass(Class cl, String name)
	{
		Field field = null;
		try{
			field = cl.getDeclaredField(name);
		}catch(NoSuchFieldException ex)
		{
			System.out.println(ex.getMessage());
		}
		return field;
	}
	
	/**
	 * getClassForName is a simple wrapper that handles the try/catch of attempting to get a Class by string name
	 * 
	 * @param className A string representation of the class name
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Class getClassForName(String className)
	{
		Class cl = null;
		try{
			cl = Class.forName(className);
		}catch(ClassNotFoundException ex)
		{
			System.out.println(ex.getMessage());
		}
		return cl;
	}
	
	
	/**
	 * populateObjects takes in a list of elements and a starting index and attempts to generate each object represented
	 * by the elements by calling the default no-arg constructor of the represented objects.
	 * 
	 * populateObjects should be called before any reference resolving is attempted.
	 * 
	 * @param elements The children elements of the documents root element.
	 * @param startingIndex the index for which to start on the elements. 0 if all elements are to be deserialized.
	 */
	public void populateObjects(List<Element> elements, int startingIndex)
	{
		Object curObject;
		String size;
		Element curElement;
		
		for (int i = startingIndex; i < elements.size(); i ++)
		{
			curElement = elements.get(i);
			size = curElement.getAttributeValue("size");
			if (size != null)
			{
				curObject = getArrayObjectFromElement(curElement, Integer.valueOf(size));
			}else
			{
				curObject = getObjectFromElement(elements.get(i));
			}
			
			if (curObject != null)
				_DeserializedObjects.put(Integer.valueOf(curElement.getAttributeValue("id")), curObject);
		}
	}
	
	/**
	 * getArrayObjectFromElement instantiates an array object represented by a passed in Element to the size specified.
	 * 
	 * @param el The element representing the serialized array object
	 * @param size The size of the array
	 * @return an Object representation of the specified Element. Note that the Object is a new Array with no resolved references.
	 */
	@SuppressWarnings("rawtypes")
	public Object getArrayObjectFromElement(Element el, int size)
	{
		Object obj = null;
		String className = el.getAttributeValue("class");
		Class componentType;
		
		try{
			Class rootClass = Class.forName(className);
			componentType = rootClass.getComponentType();
			
			obj = Array.newInstance(componentType, size);
		}catch(ClassNotFoundException ex)
		{
			System.out.println("Deserializer: Could not find class: " + className + ". Error Message: " + ex.getMessage());
		}
		
		return obj;
	}
	
	/**
	 * getObjectFromElement retrieves the Object representation of an Element by calling the no-arg constructor for the Elements "class" attribute.
	 * 
	 * @param el The elemnt representing the serialized object
	 * @return an Object representation of the Element. Note that the Object is a new Object generated from the no-arg constructor and has no references or primitives resolved.
 	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getObjectFromElement(Element el)
	{
		String className = el.getAttributeValue("class");
		Object obj = null;
		try{
			Class rootClass = Class.forName(className);
			Constructor constr = rootClass.getDeclaredConstructor(new Class[]{});
			constr.setAccessible(true);
			obj = constr.newInstance(new Object[]{});
		}catch(ClassNotFoundException ex)
		{
			System.out.println("Deserializer: Could not find class: " + className + ". Error Message: " + ex.getMessage());
		}catch(NoSuchMethodException ex)
		{
			System.out.println("Deserializer: Could not retrieve no arg constructor for class: " + className + ". Error Message: " + ex.getMessage());
		}catch(Exception ex)
		{
			System.out.println("Deserializer: Could not instantiate object for class: " + className + ". Erorr Message: " + ex.getMessage());
		}
		
		return obj;
	}
	
	/**
	 * getPrimitiveObject takes in a class type name in String format (assumed to represent a primitive, or primitive wrapper type) and a String value and
	 * returns a primitive Object wrapper that represents the value of the String passed in.
	 * 
	 * If the value is in bad form for the class type name that was received, or the class type name is not of primitive or primitive wrapper type,
	 * null is returned.
	 *  
	 * @param typeName A String representation of the values class type. (ie, "java.lang.Integer","byte","char","java.lang.Character", etc.)
	 * @param value The value of the desired Object
	 * @return A primitive wrapper Object of the requested typeName with value "value", or null if input is in bad form.
	 */
	public Object getPrimitiveObject(String typeName, String value)
	{
		Object retVal = null;
		
		try{
			switch(typeName)
			{
				case "byte":
				case "java.lang.Byte":
					retVal = new Byte(Byte.parseByte(value));
					break;
				case "short":
				case "java.lang.Short":
					retVal = new Short(Short.parseShort(value));
					break;
				case "int":
				case "java.lang.Integer":
					retVal = new Integer(Integer.parseInt(value));
					break;
				case "long":
				case "java.lang.Long":
					retVal = new Long(Long.parseLong(value));
					break;
				case "float":
				case "java.lang.Float":
					retVal = new Float(Float.parseFloat(value));
					break;
				case "double":
				case "java.lang.Double":
					retVal = new Double(Double.parseDouble(value));
					break;
				case "boolean":
				case "java.lang.Boolean":
					retVal = new Boolean(Boolean.parseBoolean(value));
					break;
				case "char":
				case "java.lang.Character":
					retVal = new Character(value.charAt(0));
					break;
			}
		}catch(Exception ex)
		{
			System.out.println("Bad value format. Please retry.");
		}
		
		return retVal;
	}
}
