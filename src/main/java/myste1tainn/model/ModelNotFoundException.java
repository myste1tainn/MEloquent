package myste1tainn.model;

/**
 * Created by Akeereena on 10/24/15.
 */
public class ModelNotFoundException extends Exception
{
	public ModelNotFoundException(Class modelClass, long id)
	{
		super("Model of name " + modelClass.toString() + " with ID " + id + " does not existed");
	}
}
