package model;

import database.DB;
import database.QueryBuilder;
import database.QueryType;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 * Created by Akeereena on 9/5/15.
 */
public class Eloquent<T extends Model>
{
	private QueryBuilder mQueryBuilder = new QueryBuilder();
	private Class<T> mModelClass;
	private boolean useIsolateDB = false;

	public Eloquent(boolean isolateDB)
	{
		useIsolateDB = isolateDB;
	}

	private DB getDB()
	{
		if (useIsolateDB)
			return new DB();
		else
			return DB.defaultInstance();
	}

	public void setClass(Class<T> c)
	{
		mModelClass = c;
	}

	public ArrayList<T> fetch() throws Exception
	{
		this.setQueryType(QueryType.SELECT);
		mQueryBuilder.setTargetTable(mModelClass.newInstance().tableName());
		String queryString = mQueryBuilder.build();

		DB db = getDB();
		ArrayList<HashMap<String, Object>> dbItems = db.executeQuery(queryString);

		ArrayList<T> items = new ArrayList<T>();

		for (HashMap<String, Object> dbItem : dbItems)
		{
			T item = mModelClass.newInstance();
			item.setProperty(dbItem);
			items.add(item);
		}

		return items;
	}

	public long execute() throws IllegalAccessException, InstantiationException, SQLException
	{
		String table = mModelClass.newInstance().tableName();
		mQueryBuilder.setTargetTable(table);
		String queryString = mQueryBuilder.build();

		DB db = getDB();

		if (mQueryBuilder.type == QueryType.INSERT)
		{
			long affectedRows = db.executeNonQuery(queryString);

			if (affectedRows > 0)
			{
				ArrayList<HashMap<String, Object>> result = db.executeQuery("SELECT MAX(`id`) AS 'lastID' FROM " + table);
				return (Long)result.get(0).get("lastID");
			}
			else
			{
				return -1;
			}
		}
		else
		{
			return db.executeNonQuery(queryString);
		}
	}

	public int[] executeBatch(ArrayList<T> models) throws IllegalAccessException, InstantiationException, SQLException
	{
		String table = mModelClass.newInstance().tableName();
		mQueryBuilder.setTargetTable(table);
		String queryString = mQueryBuilder.build();

		DB db = getDB();

		if (mQueryBuilder.type == QueryType.INSERT)
		{
			// TODO: Figure out a way to execute batch insert
//			long affectedRows = db.executeNonQuery(queryString);
//
//			if (affectedRows > 0)
//			{
//				ArrayList<HashMap<String, Object>> result = db.executeQuery("SELECT MAX(`id`) AS 'lastID' FROM " + table);
//				return (Long)result.get(0).get("lastID");
//			}
//			else
			{
				return null;
			}
		}
		else
		{
			PreparedStatement stmt = db.prepare(queryString);

			for (Model m : models)
			{
				stmt.setLong(1, m.getLong("id"));
				stmt.addBatch();
			}

			return db.executeNonQueryBatch(stmt);
		}
	}

	public void createColumnAndValue(Model model)
	{
		if (mQueryBuilder.type == QueryType.INSERT)
		{
			mQueryBuilder.setColumnClause(model.allColumns());
			mQueryBuilder.setValueClause(model.allValues());
		}
		else if (mQueryBuilder.type == QueryType.UPDATE)
		{
			mQueryBuilder.setUpdateClause(model.allColumns(), model.allValues());
		}
	}

	public void setQueryType(QueryType type)
	{
		mQueryBuilder.type = type;
	}

	protected Eloquent _where(String col, String op, Object val)
	{
		mQueryBuilder.createWhereClause(col, op, val);
		return this;
	}

	public static Eloquent where(String col, Object val)
	{
		return Eloquent.where(col, "=", val);
	}
	public static Eloquent where(String col, String op, Object val)
	{
		return new Eloquent(false)._where(col, op, val);
	}
	public Eloquent andWhere(String col, Object val)
	{
		return this.andWhere(col, "=", val);
	}
	public Eloquent andWhere(String col, String op, Object val)
	{
		mQueryBuilder.appendWhereClause("AND", col, op, val);
		return this;
	}
	public Eloquent orWhere(String col, Object val)
	{
		return this.orWhere(col, "=", val);
	}
	public Eloquent orWhere(String col, String op, Object val)
	{
		mQueryBuilder.appendWhereClause("OR", col, op, val);
		return this;
	}
}
