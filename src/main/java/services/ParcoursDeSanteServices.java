package services;

import entities.parcours_de_sante;
import entities.publication_parcours; // Make sure you have this import
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParcoursDeSanteServices implements ICrud<parcours_de_sante> {
    Connection con;

    public ParcoursDeSanteServices() {
        con = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(parcours_de_sante p) throws SQLException {
        String sql = "INSERT INTO `parcours_de_sante`(`nom_parcours`, `localisation_parcours`, `latitude_parcours`, `longitude_parcours`, `distance_parcours`, `date_creation`, `image_parcours`) " +
                "VALUES ('" + p.getNom_parcours() + "','" + p.getLocalisation_parcours() + "'," + p.getLatitude_parcours() + "," + p.getLongitude_parcours() + "," + p.getDistance_parcours() + ",'" + p.getDate_creation() + "','" + p.getImage_parcours() + "')";

        Statement statement = con.createStatement();
        statement.executeUpdate(sql);
        System.out.println("Parcours ajouté avec succes!");
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM `parcours_de_sante` WHERE `id`=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
        System.out.println("Parcours supprimé");
    }

    @Override
    public void modifier(parcours_de_sante p) throws SQLException {
        String sql = "UPDATE `parcours_de_sante` SET `nom_parcours` = ?, `localisation_parcours` = ?, " +
                "`latitude_parcours` = ?, `longitude_parcours` = ?, `distance_parcours` = ?, " +
                "`date_creation` = ?, `image_parcours` = ? WHERE `id` = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getNom_parcours());
            ps.setString(2, p.getLocalisation_parcours());
            ps.setDouble(3, p.getLatitude_parcours());
            ps.setDouble(4, p.getLongitude_parcours());
            ps.setDouble(5, p.getDistance_parcours());
            ps.setString(6, p.getDate_creation());
            ps.setString(7, p.getImage_parcours());
            ps.setInt(8, p.getId());

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Parcours ID " + p.getId() + " mis à jour avec succès !");
            }
        }
    }

    @Override
    public List<parcours_de_sante> afficher() throws SQLException {
        List<parcours_de_sante> parcoursList = new ArrayList<>();
        String sql = "SELECT * FROM parcours_de_sante";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            parcours_de_sante p = new parcours_de_sante();
            p.setId(rs.getInt("id"));
            p.setNom_parcours(rs.getString("nom_parcours"));
            p.setLocalisation_parcours(rs.getString("localisation_parcours"));
            p.setLatitude_parcours(rs.getDouble("latitude_parcours"));
            p.setLongitude_parcours(rs.getDouble("longitude_parcours"));
            p.setDistance_parcours(rs.getDouble("distance_parcours"));
            p.setDate_creation(rs.getString("date_creation"));
            p.setImage_parcours(rs.getString("image_parcours"));

            String sqlPub = "SELECT id FROM publication_parcours WHERE parcours_de_sante_id=?";

            try (PreparedStatement psPub = con.prepareStatement(sqlPub)) {
                psPub.setInt(1, p.getId());
                try (ResultSet rsPub = psPub.executeQuery()) {
                    while (rsPub.next()) {

                        publication_parcours pub = new publication_parcours();
                        p.getPublications().add(pub);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Erreur chargement publications pour le parcours " + p.getId() + " : " + e.getMessage());
            }

            parcoursList.add(p);
        }
        return parcoursList;
    }
}