package serializer;

import java.lang.reflect.Field;

public class ArrayField extends ClassField {
	private Class _InnerType;
	
	public ArrayField(Object parentObject, Field field, boolean inherited)
	{
		super(parentObject, field, inherited);
	}
	
	public void set(Object value, int index)
	{
		//TODO implement
	}
}
