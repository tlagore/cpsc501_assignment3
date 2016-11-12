package serializer;

import java.lang.reflect.Field;

public class ObjectField extends ClassField {
	
	public ObjectField(Object parentObject, Field field, boolean inherited)
	{
		super(parentObject, field, inherited);
	}
}
