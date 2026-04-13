package services;

import entities.publication_parcours;
import utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicationServices implements ICrud<publication_parcours> {
    Connection con;

    public PublicationServices() {
        con = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(publication_parcours p) throws SQLException {
        String sql = "INSERT INTO `publication_parcours`(`image_publication`, `ambiance`, `securite`, `date_publication`, `text_publication`, `experience`, `type_publication`, `parcours_de_sante_id`) " +
                "VALUES ('" + p.getImage_publication() + "'," +
                p.getAmbiance() + "," +
                p.getSecurite() + ",'" +
                p.getDate_publication() + "','" +
                p.getText_publication() + "','" +
                p.getExperience() + "','" +
                p.getType_publication() + "'," +
                p.getParcours_de_sante_id() + ")";

        Statement statement = con.createStatement();
        statement.executeUpdate(sql);
        System.out.println("Publication ajoutée avec succes!");
    }

    @Override
    public void supprimer(int id) throws SQLException {

        String sql = "DELETE FROM `publication_parcours` WHERE `id`=?";
        PreparedStatement preparedStatement = con.prepareStatement(sql);
        preparedStatement.setInt(1, id);
        preparedStatement.executeUpdate();
        System.out.println("Publication supprimée");
    }

    @Override
    public void modifier(publication_parcours p) throws SQLException {
        String sql = "UPDATE `publication_parcours` SET `image_publication` = ?, `ambiance` = ?, " +
                "`securite` = ?, `date_publication` = ?, `text_publication` = ?, " +
                "`experience` = ?, `type_publication` = ?, `parcours_de_sante_id` = ? " +
                "WHERE `id` = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getImage_publication());
            ps.setInt(2, p.getAmbiance());
            ps.setInt(3, p.getSecurite());
            ps.setString(4, p.getDate_publication());
            ps.setString(5, p.getText_publication());
            ps.setString(6, p.getExperience());
            ps.setString(7, p.getType_publication());
            ps.setInt(8, p.getParcours_de_sante_id());
            ps.setInt(9, p.getId());

            ps.executeUpdate();
            System.out.println("Publication " + p.getId() + " modifiée.");
        }
    }

    @Override
    public List<publication_parcours> afficher() throws SQLException {
        List<publication_parcours> publicationList = new ArrayList<>();
        String sql = "SELECT * FROM publication_parcours";
        Statement statement = con.createStatement();
        ResultSet rs = statement.executeQuery(sql);

        while (rs.next()) {
            publication_parcours p = new publication_parcours();
            p.setId(rs.getInt("id"));
            p.setImage_publication(rs.getString("image_publication"));
            p.setAmbiance(rs.getInt("ambiance"));
            p.setSecurite(rs.getInt("securite"));
            p.setDate_publication(rs.getString("date_publication"));
            p.setText_publication(rs.getString("text_publication"));
            p.setExperience(rs.getString("experience"));
            p.setType_publication(rs.getString("type_publication"));
            p.setParcours_de_sante_id(rs.getInt("parcours_de_sante_id"));
            publicationList.add(p);
        }
        return publicationList;
    }
}