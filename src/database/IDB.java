package database;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by Akeereena on 9/5/15.
 */
public interface IDB<T>
{
	List<T> executeQuery(String queryString) throws SQLException;
	long executeNonQuery(String queryString) throws SQLException;
}
