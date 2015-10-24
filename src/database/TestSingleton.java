package database;

/**
 * Created by Akeereena on 9/5/15.
 */
public class TestSingleton
{
	private static TestSingleton ourInstance = new TestSingleton();

	public static TestSingleton getInstance()
	{
		return ourInstance;
	}

	private TestSingleton()
	{
	}
}
