import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;

public class CheckUsersColumns {
    public static void main(String[] args) {
        try {
            Connection c = DriverManager.getConnection("jdbc:mysql://localhost:3306/wellora", "root", "");
            ResultSet rs = c.createStatement().executeQuery("DESCRIBE users");
            while(rs.next()) {
                System.out.println(rs.getString(1) + " | " + rs.getString(2));
            }
        } catch(Exception e) { e.printStackTrace(); }
    }
}
