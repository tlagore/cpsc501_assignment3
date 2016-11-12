package serializer;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Vector;

@SuppressWarnings("rawtypes")
public class ObjectHandler {
	private Class _RootClass;
	private Object _RootObject;
	private Vector<ClassField> _Fields;
	
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
		for(i = 0; i < _Fields.size(); i++)
		{
			fieldNames[i] = _Fields.elementAt(i).getTypeName() + " " + _Fields.elementAt(i).getName() 
					+ (_Fields.elementAt(i).isInherited() ? " (Inherited)" : "");
		}
		
		return fieldNames;
	}
	
	public Class getRootClass()
	{
		return _RootClass;
	}
	
}
