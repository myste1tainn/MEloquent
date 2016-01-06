package myste1tainn.model;

import myste1tainn.db.Row;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Akeereena on 11/4/15.
 */
public class Attributes extends HashMap<String, Object>
{
	protected HashMap<String, Object> tracks = new HashMap<String, Object>();

	public void applyAttribute(Row row)
	{
		Set<String> keys = row.keySet();

		for (String key : keys)
		{
			setWithoutTrack(key, row.get(key));
		}
	}
	protected void setWithoutTrack(String col, Object val)
	{
		put(col, val);
	}
	public void set(String col, Object val)
	{
		put(col, val);
		track(col, val);
	}
	protected void track(String col, Object val)
	{
		tracks.put(col, val);
	}
	protected void clearTrack()
	{
		tracks = new HashMap<String, Object>();
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
	public Boolean getBoolean(Object column)
	{
		if (containsKey(column))
			return Boolean.parseBoolean(getString(column));
		else
			return null;
	}
	public Timestamp getTimestamp(Object column)
	{
		if (containsKey(column)){
			String ds = getString(column);
			ds = ds.substring(0, "yyyy-MM-dd HH:mm:ss".length());
			SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date d = null;
			try
			{
				d = f.parse(ds);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
			long milliseconds = d.getTime();
			return new Timestamp(milliseconds);
		}
		else
			return null;
	}
}
