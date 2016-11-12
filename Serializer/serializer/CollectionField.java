package serializer;

import java.lang.reflect.Field;

public class CollectionField extends ClassField {
	public CollectionField(Object parentObject, Field field, boolean inherited)
	{
		super(parentObject, field, inherited);
	}
}
