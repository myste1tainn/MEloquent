package database;

import model.Model;
import utility.Config;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class DB<T extends Model> implements IDB
{
	private static DB mDefaultInstance = null;
	private Connection connection = null;
	private Statement st = null;
	private ResultSet rs = null;
	private ResultSetMetaData rsMeta = null;
	private Config config = new Config("database");

	public DB()
	{
	}
	public static DB defaultInstance() {
		if (mDefaultInstance == null) {
			mDefaultInstance = new DB();
		}

		return mDefaultInstance;
	}

	private synchronized void connect() throws SQLException
	{
		if (connection == null || connection.isClosed()) {
			connection = DriverManager.getConnection(config.get("connectionString"),
													config.get("username"),
													config.get("password"));

			st = this.connection.createStatement();
		}
	}

	private synchronized void disconnect() throws SQLException {
		if (st != null) { st.close(); st = null; }
		if (rs != null) { rs.close(); rs = null; }
		if (connection != null) { connection.close(); connection = null; }
	}

	@Override
	public synchronized ArrayList<HashMap<String, Object>> executeQuery(String queryString) throws SQLException
	{
		connect();

		rs = st.executeQuery(queryString);
		rsMeta = rs.getMetaData();
		ArrayList<HashMap<String, Object>> results = new ArrayList<HashMap<String, Object>>();

		try
		{
			while(rs.next()) {
				HashMap<String, Object> row = new HashMap<String, Object>();

				for (int i = 1; i < rsMeta.getColumnCount() + 1; i++) {
					Object a = rs.getObject(i);

					if (a != null)
					{
						row.put(rsMeta.getColumnName(i), a);
					}
				}

				results.add(row);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			disconnect();
			return results;
		}
	}

	@Override
	public long executeNonQuery(String queryString) throws SQLException
	{
		connect();
		long affectedRows = st.executeUpdate(queryString);
		disconnect();
		return affectedRows;
	}

	public PreparedStatement prepare(String queryString) throws SQLException
	{
		connect();
		return connection.prepareStatement(queryString);
	}

	public int[] executeNonQueryBatch(PreparedStatement preparedStatement) throws SQLException
	{
		int[] affectedRows = preparedStatement.executeBatch();
		disconnect();
		return affectedRows;
	}
}
