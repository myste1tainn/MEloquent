package myste1tainn.model;

import com.google.common.reflect.TypeToken;
import myste1tainn.db.DB;
import myste1tainn.db.Query.Operator;
import myste1tainn.db.Query.Type;
import myste1tainn.db.Results;
import myste1tainn.db.Row;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by Akeereena on 10/24/15.
 */
public class Models<T extends Model> extends ArrayList<T>
{
	private Eloquent eloquent = null;

	public Models()
	{

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
			eloquent = new Eloquent<T>(get(0).table());
			String deleteQuery = eloquent.where("id", "?").build(Type.DELETE);
			DB db = getDB(useIsolateDB);

			PreparedStatement pst = db.prepare(deleteQuery);

			Model m;
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
				eloquent = new Eloquent<T>(get(0).table());
				DB db = getDB(useIsolateDB);
				PreparedStatement pst;
				eloquent.setColumns(get(0).keySet().toArray());
				eloquent.setValues(get(0).values().toArray());
				eloquent.where("id", get(0).getLong("id"));
				String sql = eloquent.build(Type.UPDATE);
				pst = db.prepare(sql);

				for (T item : this)
				{
					eloquent.setValues(item.values().toArray());
					eloquent.where("id", item.getLong("id"));
					pst = eloquent.setArguments(pst);
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
		if (useIsolateDB)
			return new DB();
		else
			return DB.defaultInstance();
	}
}
