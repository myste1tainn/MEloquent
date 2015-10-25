package myste1tainn.model;

import myste1tainn.db.DB;
import myste1tainn.db.Query.Operator;
import myste1tainn.db.Query.Type;
import myste1tainn.db.Results;
import myste1tainn.db.Row;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Akeereena on 9/5/15.
 */
public abstract class Model<T extends Model> extends HashMap<String, Object>
{
	public Eloquent eloquent = null;
	private Class<T> modelClass = null;
	private T model = null;

	protected abstract String table();
	public static <T extends Model> String tableName()
	{
		return null;
	}

	public static <T extends Model> Models<T> all(Class<T> modelClass)
	{
		Eloquent<T> eloquent = new Eloquent<T>(T.tableName());
		Models<T> models = new Models<T>();

		String sql;
		PreparedStatement pst;
		Results results;
		try
		{
			sql = eloquent.build(Type.SELECT);
			pst = DB.defaultInstance().prepare(sql);
			results = DB.defaultInstance().executeQuery(pst);

			for (Row row : results)
			{
				T m = modelClass.newInstance();
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
	public static <T extends Model> T find(Class<T> modelClass, long id)
	{
		Eloquent<T> eloquent = null;
		try
		{
			T m = modelClass.newInstance();
			eloquent = new Eloquent<T>(m.table());
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		T m = null;

		String sql;
		PreparedStatement pst;
		Results results;
		try
		{
			sql = eloquent.where("id", id).build(Type.SELECT);
			pst = DB.defaultInstance().prepare(sql);
			eloquent.setArguments(pst);
			results = DB.defaultInstance().executeQuery(pst);

			if (results.size() > 0)
			{
				m = modelClass.newInstance();
				m.applyAttribute(results.get(0));
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			return m;
		}
	}
	public static <T extends Model> long destroy(Class<T> modelClass, long id)
	{
		Eloquent<T> eloquent = new Eloquent<T>(T.tableName());
		long affectedRows = 0;

		String sql;
		PreparedStatement pst;
		try
		{
			sql = eloquent.where("id", id).build(Type.DELETE);
			pst = DB.defaultInstance().prepare(sql);
			eloquent.setArguments(pst);
			affectedRows = DB.defaultInstance().executeNonQuery(pst);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			return affectedRows;
		}
	}
	public static <T extends Model> int[] destroy(Class<T> modelClass, long[] ids)
	{
		Eloquent<T> eloquent = new Eloquent<T>(T.tableName());
		int[] affectedRows = null;

		String sql;
		PreparedStatement pst;
		try
		{
			sql = eloquent.where("id", ids[0]).build(Type.DELETE);
			pst = DB.defaultInstance().prepare(sql);

			for (long id : ids)
			{
				pst.setObject(1, id);
				pst.addBatch();
			}

			affectedRows = DB.defaultInstance().executeNonQueryBatch(pst);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			return affectedRows;
		}
	}

	public void applyAttribute(Row row)
	{
		Set<String> keys = row.keySet();

		for (String key : keys)
		{
			set(key, row.get(key));
		}
	}

	public long destroy()
	{
		long destroyedCount = 0;
		try
		{
			eloquent.where("id", getLong("id"));
			String sql = eloquent.build(Type.DELETE);
			PreparedStatement pst = DB.defaultInstance().prepare(sql);
			pst = eloquent.setArguments(pst);
			destroyedCount = DB.defaultInstance().executeNonQuery(pst);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return destroyedCount;
	}

	public long save()
	{
		long affectedRows = 0;
		Set<String> keys = super.keySet();

		try
		{
			String sql;
			if (this.getLong("id") > -1)
			{
				eloquent.setColumns(keys.toArray());
				eloquent.setValues(values().toArray());
				eloquent.where("id", getLong("id"));
				sql = eloquent.build(Type.UPDATE);
			}
			else
			{
				// TODO: #25 Make this specify-able by editing Model to track change,
				// not taking all the columns into consideration as the current version.
				keys.remove("id");
				eloquent.setColumns(keys.toArray());
				eloquent.setValues(values().toArray());
				sql = eloquent.build(Type.INSERT);
			}

			PreparedStatement pst = DB.defaultInstance().prepare(sql);
			pst = eloquent.setArguments(pst);
			affectedRows = DB.defaultInstance().executeNonQuery(pst);

			if (affectedRows > 0)
			{
				// TODO: Can this be better?
				// ID was removed earlier for INSERT so check null
				if (!containsKey("id"))
				{
					// TODO: Can this be better
					// Get the inserted ID of this model and set it
					Results results = DB.defaultInstance().executeQuery("SELECT MAX(ID) as id FROM " + tableName());
					this.set("id", results.get(0).get("id"));
				}
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			return affectedRows;
		}
	}

	public static Eloquent where(String col, Object val)
	{
		return Model.where(col, Operator.Equal, val);
	}
	public static <T extends Model> Eloquent where(String col, Operator op, Object val)
	{
		Eloquent<T> eloquent = new Eloquent<T>(T.tableName());
		return eloquent.where(col, op, val);
	}

	public void set(String col, Object val)
	{
		put(col, val);
	}

	public Integer getInt(Object column)
	{
		if (containsKey(column))
			return Integer.parseInt(get(column).toString());
		else
			return null;
	}
	public Long getLong(Object column)
	{
		if (containsKey(column))
			return Long.parseLong(get(column).toString());
		else
			return null;
	}
	public String getString(Object column)
	{
		if (containsKey(column))
			return get(column).toString();
		else
			return "";
	}
	public Timestamp getTimestamp(Object column)
	{
		if (containsKey(column))
			return new Timestamp(getLong(column));
		else
			return null;
	}
}
