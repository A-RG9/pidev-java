import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class CheckTables {
    public static void main(String[] args) {
        try {
            Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/wellora", "root", "");
            ResultSet rs = c.createStatement().executeQuery("SHOW TABLES");
            while(rs.next()) {
                System.out.println(rs.getString(1));
            }
        } catch(Exception e) { e.printStackTrace(); }
    }
}
