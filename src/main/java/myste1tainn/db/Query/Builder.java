package myste1tainn.db.Query;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Akeereena on 9/5/15.
 * SQL Query String Builder
 */
public class Builder
{
	private Grammar grammar = Grammar.MySQL;

	// SQL String Components in Where clause
	private ArrayList<String> wheres;   // Wheres
	private ArrayList<Object> whereVals;// Where Values

	// SQL String components in Select/Update clause
	private Object[] cols;              // Columns
	private Object[] vals;              // Values

	private String table;

	public Builder()
	{
		this(Grammar.MySQL);
	}
	public Builder(Grammar grammar)
	{
		this.grammar = grammar;
	}

	public void setTable(String table)
	{
		this.table = table;
	}

	public void setColumns(Object[] cols)
	{
		this.cols = cols;
	}
	public void setValues(Object[] vals) { this.vals = vals; }

	public void setWhere(String col, Operator op, Object val)
	{
		wheres = new ArrayList<String>();
		whereVals = new ArrayList<Object>();
		createWhereSentence(col, op, val, null);
	}

	public void addWhere(String col, Operator op, Object val, Conjunction conj)
	{
		if (wheres == null)
			setWhere(col, op, val);
		else
			createWhereSentence(col, op, val, conj);
	}

	private void createWhereSentence(String col, Operator op, Object val, Conjunction conj)
	{
		String conjunctionString = "";
		if (conj != null) conjunctionString = conj.toString() + " ";

		if (op == Operator.In)
		{
			for (long l : (long[])val)
			{
				whereVals.add(l);
			}
			wheres.add(conjunctionString + col + " " + op.toString() + " " + whereInValString());
		}
		else
		{
			whereVals.add(val);
			wheres.add(conjunctionString + col + " " + op.toString() + " ? ");
		}
	}

	// TODO: remove this once done with #25
	// Do a search all file for #25
	private Type type;
	public String build(Type type) throws SQLException
	{
		this.type = type;
		switch(type)
		{
			case SELECT:
				return buildSelect();
			case INSERT:
				return buildInsert();
			case UPDATE:
				return buildUpdate();
			case DELETE:
				return buildDelete();
			default:
				throw new SQLException("Cannot build SQL, no SQL type was specified");
		}
	}

	public PreparedStatement setArguments(PreparedStatement pst) throws SQLException
	{
		if (type == Type.UPDATE)
		{
			int i = 1;
			for (; i < vals.length+1; i++)
			{
				pst.setObject(i, vals[i-1]);
			}
			for (int j = 0; j < whereVals.size(); j++)
			{
				pst.setObject(i+j, whereVals.get(j));
			}
		}
		else if (type == Type.INSERT)
		{
			int i = 1;
			for (; i < vals.length+1; i++)
			{
				pst.setObject(i, vals[i-1]);
			}
		}
		else
		{
			for (int i = 1; i < whereVals.size()+1; i++)
			{
				pst.setObject(i, whereVals.get(i-1));
			}
		}

		return pst;
	}

	private String buildSelect()
	{
		String colString = colStringWithoutParenthesesOrStar();

		String sql;
		sql = "SELECT " + colString + " ";
		sql += "FROM " + table + " ";
		sql += whereString();

		return sql;
	}

	private String buildInsert()
	{
		String sql;
		sql = "INSERT INTO " + table + " ";
		sql += colStringWithParentheses();
		sql += " VALUES ";
		sql += valString();

		return sql;
	}

	private String buildUpdate()
	{
		String sql;
		sql = "UPDATE " + table + " ";
		sql += "SET " + colStringWithVals() + " ";
		sql += whereString();

		return sql;
	}

	private String buildDelete()
	{
		String sql;
		sql = "DELETE FROM " + table + " ";
		sql += whereString();

		return sql;
	}

	private String whereString()
	{
		if (wheres == null) return "";

		String s = "WHERE ";
		for (String where : wheres)
		{
			s += where;
		}

		return s;
	}

	private String colStringWithParentheses()
	{
		String s = "(";
		for (Object col : cols)
		{
			if (col == cols[cols.length-1])
			{
				s += col + ")";
			}
			else
			{
				s += col + ", ";
			}
		}

		return s;
	}

	private String colStringWithoutParentheses()
	{
		String s = "";
		for (Object col : cols)
		{
			if (col == cols[cols.length-1])
			{
				s += col + " ";
			}
			else
			{
				s += col + ", ";
			}
		}

		return s;
	}

	private String colStringWithoutParenthesesOrStar()
	{
		String s;
		if (cols == null || cols.length > 0)
		{
			s = "*";
		}
		else
		{
			s = colStringWithoutParentheses();
		}

		return s;
	}

	private String colStringWithVals()
	{
		String s = "";
		if (cols.length > 0)
		{
			for (Object col : cols)
			{
				if (col == cols[cols.length-1])
				{
					s += col + " = ?";
				}
				else
				{
					s += col + " = ?, ";
				}
			}
		}

		return s;
	}

	private String valString()
	{
		String s = "(?";
		for (int i = 1; i < cols.length ; i++)
		{
			s += ", ?";
		}
		s += ")";

		return s;
	}

	private String whereInValString()
	{
		if (whereVals == null || whereVals.size() == 0) return "";

		String s = "(?";
		for (int i = 0; i < whereVals.size()-1; i++)
		{
			s += ", ?";
		}
		s += ")";

		return s;
	}
}
