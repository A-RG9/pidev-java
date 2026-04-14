package services;

import entities.parcours_de_sante;
import org.junit.jupiter.api.*;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParcoursDeSanteServicesTest {

    private static ParcoursDeSanteServices service;
    private static int testParcoursId = -1;

    @BeforeAll
    public static void setUp() {

        service = new ParcoursDeSanteServices();
        System.out.println("--- Début des tests ---");
    }

    @Test
    @Order(1)
    public void testAjouter() {
        try {

            parcours_de_sante p = new parcours_de_sante();
            p.setNom_parcours("Test unitaire Nom");
            p.setLocalisation_parcours("Test unitaire Loc");
            p.setLatitude_parcours(36.8065);
            p.setLongitude_parcours(10.1815);
            p.setDistance_parcours(12.5);
            p.setDate_creation("2026-04-01");
            p.setImage_parcours("test_image.png");

            service.ajouter(p);

            List<parcours_de_sante> list = service.afficher();
            boolean found = false;

            for (parcours_de_sante item : list) {
                if ("Test unitaire Nom".equals(item.getNom_parcours())) {
                    found = true;
                    testParcoursId = item.getId();
                    break;
                }
            }

            assertTrue(found, "Le parcours de test devrait être dans la base de données après l'ajout.");
            System.out.println("✅ testAjouter réussi (ID généré : " + testParcoursId + ")");

        } catch (SQLException e) {
            fail("Exception SQL lors de l'ajout : " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    public void testAfficher() {
        try {
            List<parcours_de_sante> list = service.afficher();

            assertNotNull(list, "La liste ne doit pas être null");
            assertTrue(list.size() > 0, "La liste doit contenir au moins un élément");

            System.out.println("✅ testAfficher réussi (" + list.size() + " éléments trouvés)");

        } catch (SQLException e) {
            fail("Exception SQL lors de l'affichage : " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    public void testModifier() {

        assertTrue(testParcoursId != -1, "ID du parcours introuvable. testAjouter a peut-être échoué.");

        try {

            parcours_de_sante pModifier = new parcours_de_sante();
            pModifier.setId(testParcoursId);
            pModifier.setNom_parcours("Test Modifié");
            pModifier.setLocalisation_parcours("Test unitaire Loc");
            pModifier.setLatitude_parcours(36.8065);
            pModifier.setLongitude_parcours(10.1815);
            pModifier.setDistance_parcours(15.0);
            pModifier.setDate_creation("2026-04-01");
            pModifier.setImage_parcours("test_image.png");


            service.modifier(pModifier);

            List<parcours_de_sante> list = service.afficher();
            boolean updateVerified = false;
            for (parcours_de_sante item : list) {
                if (item.getId() == testParcoursId) {
                    assertEquals("Test Modifié", item.getNom_parcours(), "Le nom n'a pas été modifié.");
                    assertEquals(15.0f, item.getDistance_parcours(), "La distance n'a pas été modifiée.");
                    updateVerified = true;
                    break;
                }
            }

            assertTrue(updateVerified, "Le parcours modifié n'a pas pu être vérifié.");
            System.out.println("✅ testModifier réussi");

        } catch (SQLException e) {
            fail("Exception SQL lors de la modification : " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    public void testSupprimer() {

        assertTrue(testParcoursId != -1, "ID du parcours introuvable. testAjouter a peut-être échoué.");

        try {

            service.supprimer(testParcoursId);

            List<parcours_de_sante> list = service.afficher();
            boolean stillExists = false;
            for (parcours_de_sante item : list) {
                if (item.getId() == testParcoursId) {
                    stillExists = true;
                    break;
                }
            }

            assertFalse(stillExists, "Le parcours devrait être supprimé de la base de données.");
            System.out.println("✅ testSupprimer réussi. Base de données nettoyée.");

        } catch (SQLException e) {
            fail("Exception SQL lors de la suppression : " + e.getMessage());
        }
    }

    @AfterAll
    public static void tearDown() {
        System.out.println("--- Fin des tests ---");
    }
}