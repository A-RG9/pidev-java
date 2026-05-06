import com.wellcare.javafx.util.MyDataBase;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

public class InspectSchema {
    public static void main(String[] args) {
        try {
            Connection connection = MyDataBase.getInstance().getConnection();
            Statement stmt = connection.createStatement();
            
            System.out.println("--- Table: users ---");
            ResultSet rs = stmt.executeQuery("DESCRIBE users");
            while (rs.next()) {
                System.out.println(String.format("%s | %s | %s | %s | %s",
                    rs.getString("Field"),
                    rs.getString("Type"),
                    rs.getString("Null"),
                    rs.getString("Key"),
                    rs.getString("Default")));
            }
            
            System.out.println("\n--- Constraints ---");
            rs = stmt.executeQuery("SELECT CONSTRAINT_NAME, CONSTRAINT_TYPE FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_NAME='users' AND TABLE_SCHEMA='wellora'");
            while (rs.next()) {
                System.out.println(rs.getString("CONSTRAINT_NAME") + " | " + rs.getString("CONSTRAINT_TYPE"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
