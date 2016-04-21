package myste1tainn.model;

import myste1tainn.db.DB;
import myste1tainn.db.Query.Type;
import myste1tainn.db.Results;
import myste1tainn.db.Row;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Akeereena on 9/5/15.
 */
public abstract class Model<T extends Model> extends Eloquent<T>
{
	public abstract String getTable();


	/**
	 * Create new and empty instance of model for sole purpose
	 * of getting table name
	 */
	public Model()
	{}

	public Model(Class<T> clazz)
	{
		super(clazz);
	}

	public Model(Class<T> clazz, long id)
	{
		this(clazz);
		this.doFindSelf(id);
	}

	private void doFindSelf(long id)
	{
		String sql;
		PreparedStatement pst;
		Results results;
		try
		{
			DB db = new DB();

			where("id", id);
			sql = build(Type.SELECT);
			pst = db.prepare(sql);
			builder.setArguments(pst);
			results = db.executeQuery(pst);

			if (results.size() > 0)
			{
				this.applyAttribute(results.get(0));
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

	public Long count()
	{
		Long count = null;

		try
		{
			Results result = db.executeQuery("SELECT count(*) rowCount FROM "+this.getTable());
			count = (Long)result.get(0).get("rowCount");
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}

		return count;
	}

	public Models<T> all()
	{
		Models<T> models = new Models<T>();

		String sql;
		PreparedStatement pst;
		Results results;
		try
		{
			sql = build(Type.SELECT);
			pst = db.prepare(sql);
			results = db.executeQuery(pst);

			T m;
			for (Row row : results)
			{
				m = clazz.newInstance();
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
	public long destroy(long id)
	{
		long affectedRows = 0;

		String sql;
		PreparedStatement pst;
		try
		{
			where("id", id);
			sql = build(Type.DELETE);
			pst = db.prepare(sql);
			pst = builder.setArguments(pst);
			affectedRows = db.executeNonQuery(pst);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		finally
		{
			return affectedRows;
		}
	}
	public int[] destroy(long[] ids)
	{
		int[] affectedRows = null;

		String sql;
		PreparedStatement pst;
		try
		{
			where("id", ids[0]);
			sql = build(Type.DELETE);
			pst = db.prepare(sql);

			for (long id : ids)
			{
				pst.setObject(1, id);
				pst.addBatch();
			}

			affectedRows = db.executeNonQueryBatch(pst);
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

	public void add(String col) {
		add(col, 1);
	}

	public void add(String col, int value) {
		try {
			DB db = new DB();
			String sql = "UPDATE " + this.getTable();
			sql += " SET " + col + " = " + col + " + " + value;
			sql += " WHERE id = " + this.getLong("id");
			db.executeNonQuery(sql);
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

	public long save()
	{
		long affectedRows = 0;
		Set<String> keys = tracks.keySet();

		try
		{
			String sql;
			if (this.getLong("id") != null && this.getLong("id") > -1)
			{
				builder.setColumns(keys.toArray());
				builder.setValues(tracks.values().toArray());
				where("id", getLong("id"));
				sql = build(Type.UPDATE);
			}
			else
			{
				// TODO: #25 Make this specify-able by editing Model to track change,
				// not taking all the columns into consideration as the current version.
				keys.remove("id");
				builder.setColumns(keys.toArray());
				builder.setValues(tracks.values().toArray());
				sql = build(Type.INSERT);
			}

			PreparedStatement pst = db.prepare(sql);
			pst = builder.setArguments(pst);
			affectedRows = db.executeNonQuery(pst);

			if (affectedRows > 0)
			{
				// TODO: Can this be better?
				// ID was removed earlier for INSERT so check null
				if (!containsKey("id"))
				{
					// TODO: Can this be better
					// Get the inserted ID of this model and set it
					Results results = db.executeQuery("SELECT MAX(ID) as id FROM " + clazz.newInstance().getTable());
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

	public void truncate()
	{
		try
		{
			db.executeQuery("TRUNCATE "+this.getTable());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
