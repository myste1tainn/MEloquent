package myste1tainn.db.Query;

/**
 * Created by Akeereena on 10/24/15.
 */
public enum Operator
{
	Equal("="), InEqual("<>"),
	Greater(">"), GreaterOrEqual(">="),
	Less("<"), LessOrEqual("<="),
	In("IN");

	private final String stringValue;

	private Operator(final String op)
	{
		stringValue = op;
	}


	@Override
	public String toString()
	{
		return stringValue;
	}
}
