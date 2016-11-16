package deserializer;

import java.lang.reflect.Constructor;
import java.util.IdentityHashMap;
import java.util.List;

import org.jdom2.Attribute;
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
		Object obj = null;
		Element rootElement = doc.getRootElement();
		List<Element> children = rootElement.getChildren();
		
		_RootObject = getObjectFromElement(children.get(0));		
		
		return _RootObject;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object getObjectFromElement(Element el)
	{
		Attribute rootClassAtt = el.getAttribute("class");
		String className = rootClassAtt.getValue();
		Object obj = null;
		try{
			Class rootClass = Class.forName(className);
			Constructor constr = rootClass.getDeclaredConstructor(new Class[]{});
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
