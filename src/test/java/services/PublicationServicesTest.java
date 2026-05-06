package services;

import com.wellora.model.publication_parcours;
import com.wellora.dao.PublicationDAO;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PublicationServicesTest {

    private static PublicationDAO service;
    private static int testPubId = -1;
    private static final int PARCOURS_ID = 13;

    @BeforeAll
    public static void setUp() {
        service = new PublicationDAO();
        System.out.println("--- Début des tests Publication ---");
    }

    @Test
    @Order(1)
    public void testAjouter() {
        try {
            publication_parcours pub = new publication_parcours();
            pub.setParcours_de_sante_id(PARCOURS_ID);
            pub.setText_publication("Contenu de test unitaire");
            pub.setAmbiance(5);
            pub.setSecurite(4);
            pub.setExperience("Excellent");
            pub.setType_publication("Review");
            pub.setDate_publication("2026-04-15");
            pub.setImage_publication("test_pub.png");

            service.ajouter(pub);


            List<publication_parcours> list = service.afficherParParcours(PARCOURS_ID);
            boolean found = false;

            for (publication_parcours item : list) {
                if ("Contenu de test unitaire".equals(item.getText_publication())) {
                    found = true;
                    testPubId = item.getId();
                    break;
                }
            }

            assertTrue(found, "La publication de test devrait être présente après l'ajout.");
            System.out.println("✅ testAjouter réussi (ID généré : " + testPubId + ")");

        } catch (SQLException e) {
            fail("Exception SQL lors de l'ajout : " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    public void testAfficherParParcours() {
        try {
            List<publication_parcours> list = service.afficherParParcours(PARCOURS_ID);

            assertNotNull(list, "La liste ne doit pas être null");
            assertTrue(list.size() > 0, "La liste doit contenir au moins la publication ajoutée");

            System.out.println("✅ testAfficher réussi (" + list.size() + " publications trouvées)");

        } catch (SQLException e) {
            fail("Exception SQL lors de l'affichage : " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    public void testModifier() {
        assertTrue(testPubId != -1, "ID de publication introuvable.");

        try {
            publication_parcours pModif = new publication_parcours();
            pModif.setId(testPubId);
            pModif.setParcours_de_sante_id(PARCOURS_ID);
            pModif.setText_publication("Contenu modifié");
            pModif.setAmbiance(1);
            pModif.setSecurite(1);
            pModif.setExperience("Poor");
            pModif.setType_publication("Warning");
            pModif.setDate_publication("2026-04-15");
            pModif.setImage_publication("test_pub_modifie.png");

            service.modifier(pModif);

            List<publication_parcours> list = service.afficherParParcours(PARCOURS_ID);
            boolean updateVerified = false;
            for (publication_parcours item : list) {
                if (item.getId() == testPubId) {
                    assertEquals("Contenu modifié", item.getText_publication());
                    assertEquals("Poor", item.getExperience());
                    assertEquals(1, item.getAmbiance());
                    updateVerified = true;
                    break;
                }
            }

            assertTrue(updateVerified, "La modification n'a pas pu être vérifiée en base.");
            System.out.println("✅ testModifier réussi");

        } catch (SQLException e) {
            fail("Exception SQL lors de la modification : " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    public void testSupprimer() {
        assertTrue(testPubId != -1, "ID de publication introuvable.");

        try {
            service.supprimer(testPubId);

            List<publication_parcours> list = service.afficherParParcours(PARCOURS_ID);
            boolean stillExists = false;
            for (publication_parcours item : list) {
                if (item.getId() == testPubId) {
                    stillExists = true;
                    break;
                }
            }

            assertFalse(stillExists, "La publication devrait être supprimée.");
            System.out.println("✅ testSupprimer réussi. Base de données nettoyée.");

        } catch (SQLException e) {
            fail("Exception SQL lors de la suppression : " + e.getMessage());
        }
    }

    @AfterAll
    public static void tearDown() {
        System.out.println("--- Fin des tests Publication ---");
    }
}