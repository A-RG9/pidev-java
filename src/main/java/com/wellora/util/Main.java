package com.wellora.util;

import com.wellora.model.parcours_de_sante;
import com.wellora.model.publication_parcours;
import com.wellora.model.commentaire_publication;
import com.wellora.dao.ParcoursDeSanteDAO;
import com.wellora.dao.PublicationDAO;
import com.wellora.dao.CommentaireDAO;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        ParcoursDeSanteDAO pds = new ParcoursDeSanteDAO();
        PublicationDAO ps = new PublicationDAO();
        CommentaireDAO cs = new CommentaireDAO();
        try {
            System.out.println("--- Starting Easy Verification ---");

            //parcours_de_sante p = new parcours_de_sante("Test Trail", "Tunis", 36.0, 10.0, 5.0, "2025-10-03", "img.jpg");
            //pds.ajouter(p);

            parcours_de_sante pUpdate = new parcours_de_sante(5, "Updated Name", "New Loc", 30.0, 10.0, 9.9, "2026-01-10", "new.jpg");
            pds.modifier(pUpdate);

            System.out.println("parcours de sante liste:");
            pds.afficher().forEach(System.out::println);
            //pds.supprimer(4);
            publication_parcours newPub = new publication_parcours(
                    "sunny_day.jpg", 5, 4, "2026-04-10","allaaaa", "Good", "Review", 15
            );
            newPub.setText_publication("This trail is amazing!");
            ps.ajouter(newPub);

            publication_parcours updatedPub = new publication_parcours(
                    "ahmed;jpg", 3, 4, "2026-05-01", "alalaaaaa", "Excellent", "Feedback", 15
            );
            ps.modifier(updatedPub);
            ps.supprimer(7);
            System.out.println("\nAll Publications");
            ps.afficher().forEach(System.out::println);
            commentaire_publication c = new commentaire_publication();
            c.setCommentaire("This is a test comment using Statement!");
            c.setDate_commentaire("2026-04-11");
            c.setPublication_parcours_id(3);
            cs.ajouter(c);
            cs.afficher().forEach(System.out::println);
        } catch (SQLException e) {
            System.out.println("Check your connection or SQL syntax: " + e.getMessage());
        }
    }
}