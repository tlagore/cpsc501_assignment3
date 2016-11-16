package deserializer;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

public class Deserializer {
	private HashMap<Integer, Object> _DeserializedObjects;
	private Object _RootObject;
	
	public Deserializer()
	{
		_DeserializedObjects = new HashMap<Integer,Object>();
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
		Element rootElement = doc.getRootElement();
		List<Element> children = rootElement.getChildren();
		
		_RootObject = getObjectFromElement(children.get(0));	
		if (_RootObject != null)
		{
			_DeserializedObjects.put(Integer.valueOf(children.get(0).getAttributeValue("id")), _RootObject);
			populateObjects(children, 1);
			resolveReferences(children);
		}
		
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
	 * resolvePrimitive array takes in an object ID representing an array object 
	 * 
	 * @param children
	 * @param objId
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
	 * 
	 * @param children
	 * @param objId
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
	 * 
	 * @param curField
	 * @param objId
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
					field.setAccessible(true);
					field.set(_DeserializedObjects.get(objId), _DeserializedObjects.get(Integer.valueOf(reference)));
					field.setAccessible(false);
				}catch(Exception ex)
				{
					System.out.println(ex.getMessage());
				}
			}
		}
	}
	
	/**
	 * 
	 * @param cl
	 * @param name
	 * @return
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
	 * 
	 * @param className
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
	 * 
	 * @param elements
	 * @param startingIndex
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
	 * 
	 * @param el
	 * @param size
	 * @return
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
