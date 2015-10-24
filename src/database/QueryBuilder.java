package database;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

/**
 * Created by Akeereena on 9/5/15.
 * SQL Query String Builder
 */
public class QueryBuilder<T>
{
	private String mTargetTable     = "";
	private String mWhereClause     = "";   // e.g. "WHERE A = B AND C = 0 OR D = 21"
	private String mGroupClause     = "";   // e.g. "GROUP BY A, B, C"
	private String mHavingClause    = "";   // e.g. "HAVING COUNT(A) > 1"
	private String mOrderClause     = "";   // e.g. "ORDER BY A, B, C"
	private String mColumnClause    = "*";  // e.g. "A, B, C, D" or "*"
	private String mValueClause     = "";   // e.g. "1,2,3,"A String""
	private String mUpdateClause    = "";   // e.g. "SET A = 1, B = 2, C = 3"
	public QueryType type = QueryType.UNSPECIFIED;

	public String build() throws SQLException
	{
		switch (type) {
			case INSERT:
				return "INSERT INTO " + "`" + mTargetTable + "` " +
						"(" + mColumnClause + ") " +
						"VALUES (" + mValueClause + ")";
			case UPDATE:
				return "UPDATE `" + mTargetTable + "` " +
						mUpdateClause + " " + mWhereClause;
			case DELETE:
				return "DELETE FROM `" + mTargetTable + "` " + mWhereClause;
			case SELECT:
				return "SELECT " + mColumnClause + " FROM `" + mTargetTable + "` " +
						mWhereClause + " " + mGroupClause + " " +
						mHavingClause + " " + mOrderClause;
			case UNSPECIFIED:
				throw new SQLException("Building query string failed, unknown or unspecified query type.");
			default:
				throw new SQLException("Building query string failed, unknown or unspecified query type.");
		}
	}

	public void setTargetTable(String table)
	{
		mTargetTable = table;
	}

	private String resolveValueString(Object val) {
		if (val instanceof String)
		{
			if (val.equals("?"))
			{
				// The ? is for prepared statement and should not be encapsed in quotation
				return (String)val;
			}
			else
			{
				return "'" + val + "'";
			}
		}
		else if (val instanceof Integer ||
				val instanceof Float ||
				val instanceof Double ||
				val instanceof Long)
		{
			return val + "";
		}
		else if (val instanceof Date)
		{
			DateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			val = df.format(val);
			return "STR_TO_DATE('" + val + "', '%m-%d-%Y %H:%i:%S')";
		}
		else if (val == null)
		{
			return "NULL";
		}

		return "'" + val + "'";
	}

	public void createWhereClause(String col, String op, Object val)
	{
		mWhereClause = "WHERE `" + col + "` " + op + " " + resolveValueString(val);
	}

	public void appendWhereClause(String conjunction, String col, String op, Object val)
	{
		mWhereClause += " " + conjunction + " `" + col + "` " + op + " " + resolveValueString(val);
	}

	public void createUpdateClause(String col, Object val)
	{
		mUpdateClause = "SET `" + col + "` = " + resolveValueString(val);
	}

	public void appendUpdateClause(String col, Object val)
	{
		mUpdateClause += ", `" + col + "` = " + resolveValueString(val);
	}

	public void setGroupClause(String col)
	{
		mGroupClause = "GROUP BY `" + col + "`";
	}

	public void setGroupClause(ArrayList<String> cols)
	{
		mGroupClause = "GROUP BY `" + cols.get(0) + "`";

		for (String col : cols)
		{
			if (col == cols.get(0)) continue;
			mGroupClause = ", `" + col + "`";
		}
	}

	public void setColumnClause(String col)
	{
		mColumnClause = "`" + col + "`";
	}

	public void setColumnClause(Set<String> cols)
	{
		boolean first = true;
		for (String col : cols)
		{
			if (first)
			{
				mColumnClause = "`" + col + "`";
				first = !first;
			}
			else
			{
				mColumnClause += ", `" + col + "`";
			}
		}
	}

	public void setUpdateClause(Set<String> cols, Collection<Object> vals)
	{
		int i = 0;
		Object val;
		Object[] valArray = vals.toArray();
		for (String col : cols)
		{
			val = valArray[i];
			if (i > 0)
			{
				appendUpdateClause(col, val);
			}
			else
			{
				createUpdateClause(col, val);
			}
			i++;
		}
	}

	public void setValueClause(Collection<Object> values)
	{
		boolean first = true;
		for (Object value : values)
		{
			if (first)
			{
				mValueClause = "'" + value + "'";
				first = !first;
			}
			else
			{
				mValueClause += ", " + resolveValueString(value);
			}
		}
	}

	private String parseValue(Object value)
	{
		if (value instanceof Date)
		{
			DateFormat df = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
			value = df.format(value);
			return "STR_TO_DATE('" + value + "', '%m-%d-%Y %H:%i:%S')";
		}

		return "'" + value + "'";
	}
}
