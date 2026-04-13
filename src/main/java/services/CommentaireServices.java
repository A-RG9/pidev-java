package services;

import entities.commentaire_publication;
import utils.MyDataBase;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentaireServices implements ICrud<commentaire_publication> {
    Connection con;

    public CommentaireServices() {
        con = MyDataBase.getInstance().getConnection();
    }

    @Override
    public void ajouter(commentaire_publication c) throws SQLException {

        String sql = "INSERT INTO `commentaire_publication`(`commentaire`, `date_commentaire`, `publication_parcours_id`) " +
                "VALUES ('" + c.getCommentaire() + "','" + c.getDate_commentaire() + "'," + c.getPublication_parcours_id() + ")";

        Statement st = con.createStatement();
        st.executeUpdate(sql);
        System.out.println("Commentaire ajouté avec succès!");
    }

    @Override
    public void modifier(commentaire_publication c) throws SQLException {
        String sql = "UPDATE `commentaire_publication` SET `commentaire` = ?, `date_commentaire` = ?, `publication_parcours_id` = ? WHERE `id` = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getCommentaire());
            ps.setString(2, c.getDate_commentaire());
            ps.setInt(3, c.getPublication_parcours_id());
            ps.setInt(4, c.getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Commentaire ID " + c.getId() + " mis à jour avec succès !");
            } else {
                System.out.println("Aucun commentaire trouvé avec l'ID " + c.getId());
            }
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification du commentaire : " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void supprimer(int id) throws SQLException {

        String sql = "DELETE FROM `commentaire_publication` WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Commentaire supprimé via PreparedStatement");
    }

    @Override
    public List<commentaire_publication> afficher() throws SQLException {
        List<commentaire_publication> list = new ArrayList<>();
        String sql = "SELECT * FROM commentaire_publication";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            commentaire_publication c = new commentaire_publication();
            c.setId(rs.getInt("id"));
            c.setCommentaire(rs.getString("commentaire"));
            c.setDate_commentaire(rs.getString("date_commentaire"));
            c.setPublication_parcours_id(rs.getInt("publication_parcours_id"));
            list.add(c);
        }
        return list;
    }
}