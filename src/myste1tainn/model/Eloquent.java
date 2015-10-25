package myste1tainn.model;

import myste1tainn.db.DB;
import myste1tainn.db.Query.*;
import myste1tainn.db.Results;
import myste1tainn.db.Row;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Akeereena on 9/5/15.
 */
public class Eloquent<T extends Model> extends Builder
{
	public Eloquent(String tableName)
	{
		setTable(tableName);
	}


	public Eloquent<T> where(String col, Object val)
	{
		return where(col, Operator.Equal, val);
	}
	public Eloquent<T> where(String col, Operator op, Object val)
	{
		setWhere(col, op, val);
		return this;
	}

	public Eloquent<T> andWhere(String col, Object val)
	{
		andWhere(col, Operator.Equal, val);
		return this;
	}
	public Eloquent<T> andWhere(String col, Operator op, Object val)
	{
		addWhere(col, op, val, Conjunction.And);
		return this;
	}
	public Eloquent<T> orWhere(String col, Object val)
	{
		orWhere(col, Operator.Equal, val);
		return this;
	}

	public Eloquent<T> orWhere(String col, Operator op, Object val)
	{
		addWhere(col, op, val, Conjunction.Or);
		return this;
	}

	public Models<T> fetch(Class<T> modelClass)
	{
		Models<T> models = new Models<T>();

		try
		{
			setTable(modelClass.newInstance().table());
			String sql = build(Type.SELECT);
			PreparedStatement pst = DB.defaultInstance().prepare(sql);
			pst = setArguments(pst);
			Results results = DB.defaultInstance().executeQuery(pst);

			T m;
			for (Row row : results)
			{
				m = modelClass.newInstance();
				m.applyAttribute(row);
				models.add(m);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			return models;
		}
	}

	public long destroy()
	{
		long destroyedCount = 0;
		try
		{
			String sql = build(Type.DELETE);
			PreparedStatement pst = DB.defaultInstance().prepare(sql);
			pst = setArguments(pst);
			destroyedCount = DB.defaultInstance().executeNonQuery(pst);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			return destroyedCount;
		}
	}
}
