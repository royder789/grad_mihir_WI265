import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Main {
    public static void main(String[] args) {
        try {
            String url = "jdbc:postgresql://db:5432/docker";
            String user = "postgres";
            String password = "Svnit@02082002";

            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();

            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS students (id SERIAL PRIMARY KEY, name VARCHAR(50))");
            stmt.executeUpdate("INSERT INTO students (name) VALUES ('Mihir')");

            System.out.println("Record Inserted Successfully!");

            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}