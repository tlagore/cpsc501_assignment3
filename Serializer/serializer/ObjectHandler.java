package serializer;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

@SuppressWarnings("rawtypes")
public class ObjectHandler {
	private Class _RootClass;
	private Object _RootObject;
	private Vector<ClassField> _Fields;
	
	/**
	 * attempts to instantiates an object for a specified class name and generate the field information for the new object
	 * @param className The name of the class being instantiated.
	 * @throws ClassNotFoundException thrown if the class name handed in cannot be found
	 * @throws IllegalAccessException thrown if the class attempting to be instantiated cannot be accessed
	 * @throws InstantiationException thrown if there is an error instantiating the object for the class name
	 */
	public ObjectHandler(String className) throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		_Fields = new Vector<ClassField>();
		try{
			_RootClass = Class.forName(className);
			_RootObject = _RootClass.newInstance();
			generateFields(_RootClass, false);
			generateInheritedFields();
			
		}catch(ClassNotFoundException ex)
		{
			throw ex;
		}catch(IllegalAccessException ex)
		{
			throw ex;
		}catch(InstantiationException ex)
		{
			throw ex;
		}
	}
	
	/**
	 * Secondary constructor, takes in an already instantiated object and generates field information for the object
	 * @param obj
	 */
	public ObjectHandler(Object obj)
	{
		_Fields = new Vector<ClassField>();
		_RootObject = obj;
		_RootClass = obj.getClass();
		generateFields(_RootClass, false);
		generateInheritedFields();
	}
	
	/**
	 * generateFields generates a list of ClassField representations of the fields of the passed in class
	 * 
	 * @param cl the class for which the fields need to be generated
	 * @param inherited true if the field of the class is inherited, false if it belongs to the class itself
	 */
	public void generateFields(Class cl, boolean inherited)
	{
		Field[] fields = cl.getDeclaredFields();
		for (int i = 0; i < fields.length; i ++)
		{
			//Array field
			if(fields[i].getType().isArray()){
				ArrayField field = new ArrayField(_RootObject, fields[i], inherited);
				_Fields.addElement(field);
			//Primitive field	
			}else if (fields[i].getType().isPrimitive()){
				PrimitiveField field = new PrimitiveField(_RootObject, fields[i], inherited);
				_Fields.addElement(field);
			//Collection field
			}else if (Collection.class.isAssignableFrom(fields[i].getType())){
				CollectionField field = new CollectionField(_RootObject, fields[i], inherited);
				_Fields.addElement(field);
			//Object field
			}else{
				ObjectField field = new ObjectField(_RootObject, fields[i], inherited);
				_Fields.addElement(field);
			}
		}
	}
	
	/**
	 * generateInheritedFields obtains a list of all superclasses from which the RootClass inherits and generatesFields for them.
	 */
	public void generateInheritedFields()
	{
		Vector<Class> superclasses = new Vector<Class>();
		getAllSuperclasses(_RootClass, superclasses);
		
		for (int i = 0; i < superclasses.size(); i ++)
			generateFields(superclasses.elementAt(i), true);
	}
	
	/**
	 * getAllSuperclasses takes in a Class and a Class Vector and recursively fills the Vector with all superclasses
	 * of the given Class object.
	 *  
	 * @param c the Class being inspected
	 * @param superclasses the Superclasses of the given Class
	 */
	public void getAllSuperclasses(Class c, Vector<Class> superclasses)
	{
		Class superclass = c.getSuperclass();
		if (superclass != null)
		{
			superclasses.addElement(superclass);
			getAllSuperclasses(superclass, superclasses);
		}
	}
	
	/**
	 * getArrayType returns a readable string version of the array based on the passed in string.<p>
	 * 
	 * The passed in string is assumed to be in the form LObjectType, B, C, D, F, I , J, S, or Z and can be derived by 
	 * performing .getClass().getName() on any array object, removing leading '['
	 * 
	 * @param str The object.getClass().getName() representation of an array object without leading '['
	 * @return a readable type of the array
	 */
	public static String getArrayType(String str)
	{
		String type = "";
		
		switch(str.charAt(0))
		{
			case 'B':
				type = "byte";
				break;
			case 'C':
				type = "char";
				break;
			case 'D':
				type = "double";
				break;
			case 'F':
				type = "float";
				break;
			case 'I':
				type = "int";
				break;
			case 'J':
				type = "long";
				break;
			case 'L':
				type = str.substring(1);
				break;
			case 'S':
				type = "short";
				break;
			case 'Z':
				type = "boolean";
				break;
			default:
				type = "unknown";
				break;
		}
		
		return type;
	}
	
	/**
	 * getClassNameDetails
	 * 
	 * takes in a class and returns the details of the classes name in the form of a String[2].
	 * 
	 * @param c the class for which the name is to be derived
	 * @return String[], String[0] = Dimensions of array (0 if not an array), String[1] contains the name of the class (or char code if a primitive non-array).
	 */
	public static String[] getClassNameDetails(Class c)
	{
		String[] fullClassName = new String[]{"",""};
		String className = c.getName();
		
		int arrayDimensions = 0;
		
		while(className.charAt(arrayDimensions) == '[')
		{
			arrayDimensions++;
		}
		
		fullClassName[0] = String.valueOf(arrayDimensions);
		
		if (arrayDimensions != 0)
		{
			//has dimensions >= 1, remove ['s from class name and drop ';' in case it is an array of objects
			fullClassName[1] = className.substring(arrayDimensions, className.length()).replace(";", "");
		}else
			fullClassName[1] = className;
		
		return fullClassName;
	}
	
	
	/* Getters and setters */
	public Object getRootObject()
	{
		return _RootObject;
	}
	
	public Vector<ClassField> getFields()
	{
		return _Fields;
	}
	
	/**
	 * Get field names returns an array containing a string representation of each field within the Object being handled by the ObjectHandler<p>
	 * field names are in the form "type name = value" for objects and primitives (where value is the hashCode in the instance of an Object)
	 * and the form "type [][] name = { { val1, val2 }, { val3, val4 } } for arrays. If the array represents an array of objects, the hashCode
	 * of the object is displayed.
	 * 
	 * @return a String[] containing the details of each field of the class.
	 */
	public String[] getFieldNames()
	{
		int i;
		String [] fieldNames = new String[_Fields.size()];
		Object obj = null;
		Field curField;

		for(i = 0; i < _Fields.size(); i++)
		{
			curField = _Fields.elementAt(i).getField();
			
			String [] fieldDetails = getClassNameDetails(curField.getType());
			String type = fieldDetails[1];
			String dimensions = "";
			
			if(fieldDetails[0].compareTo("0") != 0)
			{
				type = getArrayType(type);
				for(int j = 0; j < Integer.parseInt(fieldDetails[0]); j ++)
					dimensions += "[]";
				
				dimensions += " ";
			}
			
			try{
				curField.setAccessible(true);
				obj = curField.get(_Fields.elementAt(i).getParentObject());
			}catch(Exception ex)
			{
				System.out.println(ex.getMessage());
			}
			
			fieldNames[i] = type + " " + dimensions + _Fields.elementAt(i).getName() 
					+ (_Fields.elementAt(i).isInherited() ? " (Inherited)" : "");
			
			if(obj == null)
			{
				fieldNames[i] += " = null";
			}else
			{
				if (curField.getType().isPrimitive())
				{
					if(type.compareTo("char") == 0)
						fieldNames[i] += " = '" + obj + "'";
					else
						fieldNames[i] += " = " + obj;
				}
				else if (curField.getType().isArray())
				{
					fieldNames[i] += " = " + arrayToString(_Fields.elementAt(i).getValue());
					
				}else
					fieldNames[i] += " = " + obj.hashCode();
			}	
		}
		
		return fieldNames;
	}
	
	/**
	 * arrayToString takes in an array object and returns a string representation of the contents of the array in the form<p>
	 * { { 1, 2 }, { 3, 4 } } where the Object represented an int[2][2] array. If the array object passed in is an array of Objects,
	 * the hashCode of the object will be displayed instead.
	 * 
	 * @param obj The array Object being examined
	 * @return a string representation of the contents of the array
	 */
	public String arrayToString(Object obj)
	{
		String contents = "";
		
		if (obj != null && obj.getClass().isArray())
		{
			int length = Array.getLength(obj);
			Object nextArrObj;
			
			contents = "{";
			for (int i = 0; i < length; i ++)
			{
				nextArrObj = Array.get(obj, i);
				if(nextArrObj != null)
				{
					if(nextArrObj.getClass().isArray())
						contents += arrayToString(nextArrObj);
					else if (nextArrObj.getClass().isPrimitive() || isWrapper(nextArrObj.getClass()))
						contents += nextArrObj;
					else
						contents += "(hashCode)" + nextArrObj.hashCode();
				}
				else
					contents += "null";
				
				if (i < length - 1)
					contents += ", ";
			}
			contents += "}";
		}
		
		return contents;
	}

	
	/**
	 * isWrapper takes in a class and returns whether or not the class is a wrapper for a primitive object
	 * 
	 * @param c Class being inspected 
	 * @return true if c is a wrapper for a primitive object, false if it is not
	 */
	public static boolean isWrapper(Class c)
	{
		Set<Class<?>> wrappers = new HashSet<Class<?>>();
		
		wrappers.add(Byte.class);
		wrappers.add(Short.class);
		wrappers.add(Boolean.class);
		wrappers.add(Integer.class);
		wrappers.add(Double.class);
		wrappers.add(Character.class);		
		wrappers.add(Long.class);
		wrappers.add(Float.class);
		wrappers.add(Void.class);
        
        return wrappers.contains(c);
	}
	
	/* getters / setters */
	public Class getRootClass()
	{
		return _RootClass;
	}
	
}
