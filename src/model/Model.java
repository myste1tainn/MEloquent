package model;

import database.DB;
import database.QueryType;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by Akeereena on 9/5/15.
 */
public abstract class Model<T extends Model>
{
	// Eloquent Builder
	private Eloquent<T> mEB = null;
	private HashMap<String, Object> mFields;
	private Class<T> c;

	public Model(Class<T> c) {
		this(c, false);
	}

	public Model(Class<T> c, boolean isolateDB) {
		super();

		mFields = new HashMap<String, Object>();
		mEB = new Eloquent<T>(isolateDB);
		mEB.setClass(c);
	}

	public ArrayList<T> all() throws Exception
	{
		ArrayList<T> items = mEB.fetch();
		mEB.setQueryType(QueryType.UPDATE);
		return items;
	}

	public T find(long id) throws Exception
	{
		where("id", id);
		ArrayList<T> items = mEB.fetch();
		T item = (items.size() > 0) ? items.get(0) : null;
		item.where("id", id);
		return item;
	}

	public boolean hasRows()
	{
		long count = count();
		return count > 0;
	}

	public boolean isEmpty()
	{
		long count = count();
		return count > -1 && count == 0;
	}

	public long count()
	{
		long count = -1;
		String sql = "SELECT COUNT(*) AS count FROM " + tableName();

		HashMap<String, Object> r = null;
		try
		{
			r = (HashMap<String, Object>) DB.defaultInstance().executeQuery(sql).get(0);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return (Long) r.get("id");
	}

	/**
	 * Destroy self
	 * @return
	 * @throws Exception
	 */
	public long destroy() throws Exception
	{
		return destroy(getLong("id"));
	}

	/**
	 * Destroy the specified id
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public long destroy(long id) throws Exception
	{
		mEB.setQueryType(QueryType.DELETE);
		mEB._where("id", "=", id);
		return mEB.execute();
	}

	public int[] bulkDestroy(ArrayList<T> models) throws IllegalAccessException, SQLException, InstantiationException
	{
		mEB.setQueryType(QueryType.DELETE);
		mEB._where("id", "=", "?");
		return mEB.executeBatch(models);
	}

	public long save() throws Exception
	{
		if (this.get("id") == null)
		{
			mEB.setQueryType(QueryType.INSERT);
		}
		else
		{
			mEB.setQueryType(QueryType.UPDATE);
		}

		mEB.createColumnAndValue(this);

		this._setWithOutChange("id", mEB.execute());

		return (Long)this.get("id");
	}

	public Eloquent<T> where (String col, Object val)
	{
		return mEB._where(col, "=", val);
	}

	public void set(String column, Object value)
	{
		mEB.setQueryType(QueryType.UPDATE);
		mFields.put(column, value);
	}

	private void _setWithOutChange(String column, Object value)
	{
		mFields.put(column, value);
	}

	public Object get(String column)
	{
		return mFields.get(column);
	}
	public int getInt(String column)
	{
		return (Integer) mFields.get(column);
	}
	public long getLong(String column)
	{
		return (Long) mFields.get(column);
	}
	public String getString(String column)
	{
		return (String) mFields.get(column);
	}
	public Date getDate(String column) { return (Date) mFields.get(column); }
	public Timestamp getTimestamp(String column) {
		return new Timestamp(getDate(column).getTime());
	}

	public Set<String> allColumns()
	{
		return mFields.keySet();
	}

	public Collection<Object> allValues()
	{
		return mFields.values();
	}

	public abstract String tableName();
	public void setProperty(HashMap<String, Object> prop)
	{
		mFields = prop;
	}
}
