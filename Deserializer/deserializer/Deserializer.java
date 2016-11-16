package deserializer;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;

public class Deserializer {
	private IdentityHashMap<Integer, Object> _DeserializedObjects;
	private Object _RootObject;
	
	public Deserializer()
	{
		_DeserializedObjects = new IdentityHashMap<Integer,Object>();
	}
	
	public Object deserialize(Document doc)
	{
		Element rootElement = doc.getRootElement();
		List<Element> children = rootElement.getChildren();
		
		_RootObject = getObjectFromElement(children.get(0));	
		if (_RootObject != null)
		{
			_DeserializedObjects.put(Integer.valueOf(children.get(0).getAttributeValue("id")), _RootObject);
			populateObjects(children, 1);
			//resolveReferences(children);
		}
		
		ArrayList<Object> objects = new ArrayList<Object>(_DeserializedObjects.values());
		for(int i = 0; i < objects.size(); i++)
		{
			System.out.println(objects.get(i).getClass().getName());
		}
		
		return _RootObject;
	}
	
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
				System.out.println("Encountered array: " + curElement.getAttributeValue("class") + " " + curElement.getAttributeValue("id"));
			}else
			{
				curObject = getObjectFromElement(elements.get(i));
			}
			
			if (curObject != null)
				_DeserializedObjects.put(Integer.valueOf(curElement.getAttributeValue("id")), curObject);
		}
	}
	
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
}
