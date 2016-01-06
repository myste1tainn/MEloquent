package myste1tainn.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Akeereena on 9/5/15.
 */
public interface IDB<T>
{
	List<T> executeQuery(String queryString) throws SQLException;
	long executeNonQuery(String queryString) throws SQLException;
	List<T> executeQuery(PreparedStatement pst) throws SQLException;
	long executeNonQuery(PreparedStatement pst) throws SQLException;
}
