package serializer;

import java.lang.reflect.Field;

public class PrimitiveField extends ClassField {
	
	public PrimitiveField(Object parentObject, Field field, boolean inherited)
	{
		super(parentObject, field, inherited);
	}
}
