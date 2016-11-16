package classes;

public class ClassA {
	public ClassA()
	{
		
	}
	
	private boolean boolVar = false;
	private int thing = 42;
	private char type = 'c';
	private int[][][] arr;
	private ClassC[] objArr = new ClassC[] { new ClassC(), new ClassC() };
	
	public void setBoolVar(boolean var)
	{
		boolVar = var;
	}
	
	public boolean getBoolVar()
	{
		return boolVar;
	}
	
	public void setThing(int var)
	{
		thing = var;
	}
	
	public int getThing()
	{
		return thing;
	}
}
