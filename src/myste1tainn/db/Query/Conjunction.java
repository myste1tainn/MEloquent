package myste1tainn.db.Query;

/**
 * Created by Akeereena on 10/24/15.
 */
public enum Conjunction
{
	And("AND"), Or("OR");

	private final String stringValue;

	private Conjunction(final String conj)
	{
		stringValue = conj;
	}


	@Override
	public String toString()
	{
		return stringValue;
	}
}
