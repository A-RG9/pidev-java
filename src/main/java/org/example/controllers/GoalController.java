package org.example.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import org.example.dao.GoalDaoImpl;
import org.example.dao.IGoalDao;
import org.example.models.Goal;
import org.example.utils.Database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

public class GoalController {

    private IGoalDao goalDao = new GoalDaoImpl();

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private VBox formOverlay;
    @FXML
    private Label lblError;

    // --- Champs du formulaire ---
    @FXML
    private TextField txtTitle;
    @FXML
    private TextArea txtDescription;
    @FXML
    private ComboBox<String> comboCategory;
    @FXML
    private ComboBox<String> comboStatus;
    @FXML
    private ComboBox<String> comboDifficulty;
    @FXML
    private ComboBox<UserItem> comboPatient;
    @FXML
    private ComboBox<UserItem> comboCoach;
    @FXML
    private DatePicker dateStart;
    @FXML
    private DatePicker dateEnd;
    @FXML
    private Slider sliderProgress;

    // Variables pour gérer la modification
    private boolean isEditMode = false;
    private Goal selectedGoal = null;

    @FXML
    public void initialize() {
        // Remplir les ComboBox statiques
        comboCategory.setItems(FXCollections.observableArrayList("Nutrition", "Sport", "Mental", "Général"));
        comboStatus.setItems(FXCollections.observableArrayList("En cours", "Terminé", "En attente"));
        comboDifficulty.setItems(FXCollections.observableArrayList("Facile", "Moyen", "Difficile"));

        // Valeurs par défaut
        comboCategory.setValue("Général");
        comboStatus.setValue("En cours");
        comboDifficulty.setValue("Moyen");

        loadUsersIntoCombos();
        loadGoals();
    }

    private void loadUsersIntoCombos() {
        ObservableList<UserItem> users = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid, email FROM users")) {
            while (rs.next()) {
                users.add(new UserItem(rs.getString("uuid"), rs.getString("email")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        comboPatient.setItems(users);
        comboCoach.setItems(users);
    }

    private void loadGoals() {
        cardsContainer.getChildren().clear(); // On vide l'affichage
        List<Goal> goals = goalDao.getAllGoals();

        for (Goal goal : goals) {
            try {
                // Création d'une CARTE pour chaque objectif
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/GoalCard.fxml"));
                Node card = loader.load();

                GoalCardController controller = loader.getController();
                // CORRECTION ICI : On envoie "this" pour que la carte puisse nous parler !
                controller.setGoalData(goal, this);

                cardsContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void openAddForm() {
        isEditMode = false;
        selectedGoal = null;
        formOverlay.setVisible(true);

        // Réinitialiser les champs
        txtTitle.clear();
        txtDescription.clear();
        dateStart.setValue(null);
        dateEnd.setValue(null);
        sliderProgress.setValue(0);
        comboPatient.getSelectionModel().clearSelection();
        comboCoach.getSelectionModel().clearSelection();
        lblError.setVisible(false);
    }

    // --- NOUVELLE MÉTHODE : MODIFICATION ---
    public void openEditForm(Goal goal) {
        this.selectedGoal = goal;
        this.isEditMode = true;
        formOverlay.setVisible(true);
        lblError.setVisible(false);

        // On pré-remplit les champs avec les infos de l'objectif cliqué
        txtTitle.setText(goal.getTitle());
        txtDescription.setText(goal.getDescription());
        comboCategory.setValue(goal.getCategory());
        comboStatus.setValue(goal.getStatus());
        comboDifficulty.setValue(goal.getDifficultyLevel());
        sliderProgress.setValue(goal.getProgress());
        dateStart.setValue(goal.getStartDate());
        dateEnd.setValue(goal.getEndDate());

        // Retrouver le bon patient et coach dans les menus déroulants
        comboPatient.getItems().stream().filter(u -> u.getUuid().equals(goal.getPatientId())).findFirst().ifPresent(u -> comboPatient.setValue(u));
        comboCoach.getItems().stream().filter(u -> u.getUuid().equals(goal.getCoachId())).findFirst().ifPresent(u -> comboCoach.setValue(u));
    }

    // --- NOUVELLE MÉTHODE : SUPPRESSION ---
    public void handleDelete(Goal goal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Voulez-vous vraiment supprimer cet objectif ?");
        alert.setContentText(goal.getTitle());

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            goalDao.deleteGoal(goal.getId());
            loadGoals(); // Rafraîchit l'affichage
        }
    }

    @FXML
    private void closeForm() {
        formOverlay.setVisible(false);
    }

    @FXML
    private void handleSave() {
        if (txtTitle.getText().isEmpty() || comboPatient.getValue() == null || comboCoach.getValue() == null) {
            lblError.setText("Titre, Patient et Coach obligatoires !");
            lblError.setVisible(true);
            lblError.setManaged(true);
            return;
        }

        if (isEditMode && selectedGoal != null) {
            // CORRECTION : MODE MODIFICATION
            selectedGoal.setTitle(txtTitle.getText());
            selectedGoal.setDescription(txtDescription.getText());
            selectedGoal.setPatientId(comboPatient.getValue().getUuid());
            selectedGoal.setCoachId(comboCoach.getValue().getUuid());
            selectedGoal.setCategory(comboCategory.getValue());
            selectedGoal.setStatus(comboStatus.getValue());
            selectedGoal.setDifficultyLevel(comboDifficulty.getValue());
            selectedGoal.setProgress((int) sliderProgress.getValue());
            selectedGoal.setStartDate(dateStart.getValue());
            selectedGoal.setEndDate(dateEnd.getValue());

            goalDao.updateGoal(selectedGoal);
        } else {
            // CORRECTION : MODE AJOUT
            Goal newGoal = new Goal(
                    0,
                    txtTitle.getText(),
                    txtDescription.getText(),
                    comboPatient.getValue().getUuid(),
                    comboCoach.getValue().getUuid(),
                    comboCategory.getValue(),
                    comboStatus.getValue(),
                    comboDifficulty.getValue(),
                    (int) sliderProgress.getValue(),
                    dateStart.getValue(),
                    dateEnd.getValue()
            );
            goalDao.addGoal(newGoal);
        }

        closeForm();
        loadGoals(); // Rafraîchit les cartes
    }

    // Classe interne pour gérer les ComboBox d'utilisateurs
    public static class UserItem {
        private String uuid;
        private String email;

        public UserItem(String uuid, String email) {
            this.uuid = uuid;
            this.email = email;
        }

        public String getUuid() {
            return uuid;
        }

        @Override
        public String toString() {
            return email;
        }
    }

    private void updateGlobalStats() {
        List<Goal> goals = goalDao.getAllGoals();
        long total = goals.size();
        long completed = goals.stream().filter(g -> "Terminé".equals(g.getStatus())).count();
    }
}