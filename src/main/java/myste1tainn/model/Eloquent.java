package myste1tainn.model;

import myste1tainn.db.DB;
import myste1tainn.db.Query.*;
import myste1tainn.db.Results;
import myste1tainn.db.Row;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by Akeereena on 9/5/15.
 */
public class Eloquent<T extends Model> extends Attributes
{
	// Template model
	protected Class<T> clazz;
	protected Builder builder;
	protected Object dynamicTableName = null;

	// Dedicate DB connection for each model
	protected DB db = DB.defaultInstance();

	/**
	 * Create eloquent query builder without scope (table)
	 */
	public Eloquent()
	{}

	/**
	 * Create eloquent query builder with the specify scope (model's class/table)
	 * @param clazz
	 */
	public Eloquent(Class<T> clazz)
	{
		this.clazz = clazz;
		this.builder = new Builder();
	}


	public Eloquent where(String col, Object val)
	{
		return where(col, Operator.Equal, val);
	}
	public Eloquent where(String col, Operator op, Object val)
	{
		String tableName = getModelInstance().getTable();
		this.builder.setTable(tableName);

		builder.setWhere(col, op, val);
		return this;
	}

	private T getModelInstance()
	{
		T instance = null;
		Constructor<T> cf = null;
		if (dynamicTableName != null)
		{
			Constructor<T>[] constructors = (Constructor<T>[])clazz.getDeclaredConstructors();

			for (Constructor<T> c:constructors)
			{
				// TODO: Parameter Types should be match more accurately
				if (c.getParameterTypes().length > 1) continue;

				String dname = dynamicTableName.getClass().getSimpleName().toLowerCase();
				String cname = c.getParameterTypes()[0].getSimpleName().toLowerCase();
				if (dname.equals(cname))
				{
					cf = c;
					break;
				}
			}
		}

		try
		{
			if (cf != null) instance = cf.newInstance(dynamicTableName);
			else instance = clazz.newInstance();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}

		return instance;
	}

	public Eloquent andWhere(String col, Object val)
	{
		andWhere(col, Operator.Equal, val);
		return this;
	}
	public Eloquent andWhere(String col, Operator op, Object val)
	{
		builder.addWhere(col, op, val, Conjunction.And);
		return this;
	}
	public Eloquent orWhere(String col, Object val)
	{
		orWhere(col, Operator.Equal, val);
		return this;
	}

	public Eloquent<T> orWhere(String col, Operator op, Object val)
	{
		builder.addWhere(col, op, val, Conjunction.Or);
		return this;
	}

	public long destroy()
	{
		long destroyedCount = 0;
		try
		{
			// If id column existed, use id column instead for speed
			// else use the 'where' that user have specified through eloquent
			if (get("id") != null)
			{
				where("id", "id");
			}

			String sql = build(Type.DELETE);
			PreparedStatement pst = db.prepare(sql);
			pst = builder.setArguments(pst);
			destroyedCount = db.executeNonQuery(pst);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return destroyedCount;
	}

	public Models<T> fetch()
	{
		Models<T> models = new Models<T>();

		try
		{
			DB db = new DB();
			String sql = builder.build(Type.SELECT);
			PreparedStatement pst = db.prepare(sql);
			pst = builder.setArguments(pst);
			Results results = db.executeQuery(pst);

			T m;
			for (Row row : results)
			{
				m = getModelInstance();
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

	public String build(Type type) throws SQLException
	{
		builder.setTable(getModelInstance().getTable());
		return builder.build(type);
	}
}
