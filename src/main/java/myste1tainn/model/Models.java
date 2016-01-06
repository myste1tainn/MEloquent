package myste1tainn.model;

import myste1tainn.db.DB;
import myste1tainn.db.Query.Type;
import myste1tainn.db.Results;
import myste1tainn.db.Row;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Akeereena on 10/24/15.
 */
public class Models<T extends Model> extends ArrayList<T>
{
	private DB db = new DB();

	public Models()
	{}

	public Models(List<T> modelList)
	{
		for (T model:modelList)
		{
			this.add(model);
		}
	}

	public int[] destroy()
	{
		return destroy(false);
	}
	public int[] destroy(boolean useIsolateDB)
	{
		if (size() == 0) return null;

		int[] destroyedCount = null;
		try
		{
			T model = get(0);
			String deleteQuery = model.where("id", "?").builder.build(Type.DELETE);
			DB db = getDB(useIsolateDB);

			PreparedStatement pst = db.prepare(deleteQuery);

			T m;
			for (int i = 1; i < size()+1; i++)
			{
				m = get(i-1);
				pst.setObject(1, m.getLong("id"));
				pst.addBatch();
			}

			destroyedCount = db.executeNonQueryBatch(pst);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return destroyedCount;
	}

	public void set(String col, Object val)
	{
		for (T item : this)
		{
			item.set(col, val);
		}
	}

	public int[] save()
	{
		return save(false);
	}
	public int[] save(boolean useIsolateDB)
	{
		if (size() == 0) return null;

		int[] affectedRows = null;
		if (size() > 0)
		{
			try
			{
				T model = get(0);
				DB db = getDB(useIsolateDB);
				PreparedStatement pst;
				model.builder.setColumns(get(0).keySet().toArray());
				model.builder.setValues(get(0).values().toArray());
				model.where("id", get(0).getLong("id"));
				String sql = model.builder.build(Type.UPDATE);
				pst = db.prepare(sql);

				for (Model item : this)
				{
					model.builder.setValues(item.values().toArray());
					model.where("id", item.getLong("id"));
					pst = model.builder.setArguments(pst);
					pst.addBatch();
				}

				affectedRows = db.executeNonQueryBatch(pst);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		return affectedRows;
	}

	private DB getDB(boolean useIsolateDB)
	{
		return db;
//		if (useIsolateDB)
//			return new DB();
//		else
//			return DB.defaultInstance();
	}

	public ArrayList<T> toArrayList()
	{
		ArrayList<T> arrayList = new ArrayList<T>();

		for (T item: this)
		{
			arrayList.add(item);
		}

		return arrayList;
	}

	public List<String> stringValueListForKey(String key)
	{
		ArrayList<String> valueList = new ArrayList<String>();

		for (T item:this)
		{
			valueList.add(item.getString(key));
		}

		return valueList;
	}

	public Models<T> subList(int from, int to)
	{
		if (to > size()) to = size();
		return new Models(super.subList(from,to));
	}
}
