package serializer;

import java.lang.reflect.Field;

public class ClassField {
	private Object _ParentObject;

	private boolean _Inherited;
	private Field _Field;
	
	//maintained before setAccessible set to true.
	private int _Modifiers;

	public ClassField(Object parent, Field field, boolean inherited)
	{
		_Field = field;
		_Modifiers = field.getModifiers();
		_Field.setAccessible(true);
		_Inherited = inherited;
		_ParentObject = parent;
	}
	
	//getters/setters	
	public String getName()
	{
		return _Field.getName();
	}
	
	public String getTypeName()
	{
		return _Field.getType().getName();
	}//getters/setters

	public Object getParentObject() {
		return _ParentObject;
	}

	public boolean isInherited() {
		return _Inherited;
	}

	public Field getField() {
		return _Field;
	}
	
	public int get_Modifiers() {
		return _Modifiers;
	}
	
	public Object getValue(){
		Object obj = null;
		try{
			obj = _Field.get(_ParentObject);
		}catch(Exception ex)
		{
			System.out.println("Error accessing variable");
		}
		
		return obj;
	}
}
