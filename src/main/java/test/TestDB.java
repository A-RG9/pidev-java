package test;

import java.sql.*;

public class TestDB {
    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/wellcare", "root", "");
            ResultSet rs = conn.getMetaData().getColumns(null, null, "consultation", null);
            while (rs.next()) {
                System.out.println(rs.getString("COLUMN_NAME"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
