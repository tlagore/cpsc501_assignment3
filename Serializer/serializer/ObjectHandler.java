package serializer;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Vector;

@SuppressWarnings("rawtypes")
public class ObjectHandler {
	private Class _RootClass;
	private Object _RootObject;
	private Vector<ClassField> _Fields;
	
	/**
	 * attempts to instantiates an object for a specified class name and generate the field information for the new object
	 * @param className
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
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
	 * Takes in an already instantiated object and generates field information for the object
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
	 * set as public to allow for testing
	 * 
	 * generates a list of the fields for the object that was handed in to ObjectHandler 
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
	 * 
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
	public String getArrayType(String str)
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
	public String[] getClassNameDetails(Class c)
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
				else
					fieldNames[i] += " = " + obj.hashCode();
			}
			
			
		}
		
		return fieldNames;
	}
	
	public Class getRootClass()
	{
		return _RootClass;
	}
	
}
