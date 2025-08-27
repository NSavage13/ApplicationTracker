package db;
import java.sql.*;


public class Database {

private static final String URL = "jdbc:mysql://localhost:3306/job_tracker";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "****"; // Don't forget to hide this

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
    
}
