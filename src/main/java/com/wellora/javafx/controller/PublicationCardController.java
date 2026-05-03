package com.wellora.javafx.controller;

import com.wellora.controllers.HealthShellController;
import com.wellora.model.commentaire_publication;
import com.wellora.model.parcours_de_sante;
import com.wellora.model.publication_parcours;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import com.wellora.dao.CommentaireDAO;
import com.wellora.dao.PublicationDAO;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PublicationCardController {

    @FXML private Label dateLabel;
    @FXML private Label ambianceLabel;
    @FXML private Label safetyLabel;
    @FXML private Label experienceLabel;
    @FXML private Label typeLabel;
    @FXML private Label popularityLabel;

    @FXML private TextFlow textFlow;
    @FXML private ImageView imageView;
    @FXML private Button btnEditPub;
    @FXML private Button btnDeletePub;

    @FXML private Label commentsToggleLabel;
    @FXML private VBox commentSection;
    @FXML private VBox commentsContainer;
    @FXML private TextField commentInputField;
    @FXML private Button btnSubmitComment;

    private publication_parcours currentPublication;
    private parcours_de_sante parentParcours;
    private Runnable refreshFeedCallback;

    private final PublicationDAO pubService = new PublicationDAO();
    private final CommentaireDAO commentService = new CommentaireDAO();

    public void initialize() {
        commentsToggleLabel.setOnMouseClicked(e -> {
            boolean isVisible = commentSection.isVisible();
            commentSection.setVisible(!isVisible);
            commentSection.setManaged(!isVisible);
            if (!isVisible) {
                loadComments();
            }
        });

        btnSubmitComment.setOnAction(e -> handleAddComment());
        commentInputField.setOnAction(e -> handleAddComment());
    }

    public void setData(publication_parcours pub, parcours_de_sante parcours, Runnable refreshCallback, Consumer<String> onHashtagClicked) {
        this.currentPublication = pub;
        this.parentParcours = parcours;
        this.refreshFeedCallback = refreshCallback;

        dateLabel.setText(pub.getDate_publication());
        ambianceLabel.setText("⭐ " + pub.getAmbiance() + "/5 ambiance");
        safetyLabel.setText("🛡 " + pub.getSecurite() + "/5 safety");
        experienceLabel.setText("◉ " + pub.getExperience());
        typeLabel.setText(pub.getType_publication());

        textFlow.getChildren().clear();
        String content = pub.getText_publication();

        if (content != null && !content.isEmpty()) {
            Matcher matcher = Pattern.compile("(#\\w+)").matcher(content);
            int lastEnd = 0;

            while (matcher.find()) {
                String normalText = content.substring(lastEnd, matcher.start());
                if (!normalText.isEmpty()) {
                    Text t = new Text(normalText);
                    t.setStyle("-fx-fill: #2d3436; -fx-font-size: 14;");
                    textFlow.getChildren().add(t);
                }

                String hashtag = matcher.group(1);
                Hyperlink link = new Hyperlink(hashtag);
                link.setStyle("-fx-text-fill: #00a693; -fx-font-weight: bold; -fx-padding: 0; -fx-border-width: 0;");
                link.setOnAction(e -> {
                    if (onHashtagClicked != null) {
                        onHashtagClicked.accept(hashtag);
                    }
                });
                textFlow.getChildren().add(link);

                lastEnd = matcher.end();
            }

            String trailingText = content.substring(lastEnd);
            if (!trailingText.isEmpty()) {
                Text t = new Text(trailingText);
                t.setStyle("-fx-fill: #2d3436; -fx-font-size: 14;");
                textFlow.getChildren().add(t);
            }
        }

        if (pub.getExperience().equalsIgnoreCase("Bad")) {
            experienceLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-font-size: 12;");
        } else if (pub.getExperience().equalsIgnoreCase("Excellent")) {
            experienceLabel.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 12;");
        } else {
            experienceLabel.setStyle("-fx-text-fill: #f1c40f; -fx-font-weight: bold; -fx-font-size: 12;");
        }

        if (pub.getImage_publication() != null && !pub.getImage_publication().isEmpty()) {
            File file = new File(pub.getImage_publication());
            if (file.exists()) {
                imageView.setImage(new Image(file.toURI().toString()));
                imageView.setManaged(true);
            } else {
                imageView.setManaged(false);
            }
        } else {
            imageView.setManaged(false);
        }

        btnDeletePub.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous vraiment supprimer cette publication ?");
            if (confirm.showAndWait().get() == ButtonType.OK) {
                try {
                    pubService.supprimer(pub.getId());
                    refreshFeedCallback.run();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnEditPub.setOnAction(e -> {
            try {
                // Load the modifier publication view directly
                FXMLLoader modifierLoader = new FXMLLoader(getClass().getResource("/fxml/ModifierPublication.fxml"));
                Parent modifierView = modifierLoader.load();
                ModifierPublicationController modifierCtrl = modifierLoader.getController();
                modifierCtrl.initData(parentParcours, currentPublication);
                
                // Load the shell and set the modifier view as content
                FXMLLoader shellLoader = new FXMLLoader(getClass().getResource("/com/wellora/views/HealthShell.fxml"));
                Parent shellRoot = shellLoader.load();
                HealthShellController shellCtrl = shellLoader.getController();
                shellCtrl.setContent(modifierView);
                Stage stage = (Stage) ((Node)btnEditPub).getScene().getWindow();
                stage.getScene().setRoot(shellRoot);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        updateCommentCount();
    }

    private void updateCommentCount() {
        try {
            List<commentaire_publication> comments = commentService.afficherParPublication(currentPublication.getId());
            commentsToggleLabel.setText("💬 " + comments.size() + " comments");

            int commentsCount = comments.size();
            LocalDate pubDate;
            try {
                pubDate = LocalDate.parse(currentPublication.getDate_publication().split(" ")[0]);
            } catch (Exception ex) {
                pubDate = LocalDate.now();
            }

            long daysOld = ChronoUnit.DAYS.between(pubDate, LocalDate.now());
            if (daysOld < 0) daysOld = 0;

            long score = (commentsCount * 10) - daysOld;
            popularityLabel.setText("🔥 " + score);

        } catch (Exception e) {
            System.err.println("Error fetching comment count");
        }
    }

    private void loadComments() {
        commentsContainer.getChildren().clear();
        try {
            List<commentaire_publication> comments = commentService.afficherParPublication(currentPublication.getId());
            commentsToggleLabel.setText("💬 " + comments.size() + " comments");

            for (commentaire_publication comment : comments) {
                commentsContainer.getChildren().add(createCommentUI(comment));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String filterBadWords(String input) {
        if (input == null || input.trim().isEmpty()) {
            return input;
        }
        try {
            String encodedText = URLEncoder.encode(input, "UTF-8");
            URL url = new URL("https://www.purgomalum.com/service/plain?text=" + encodedText);
            Scanner scanner = new Scanner(url.openStream(), "UTF-8");
            StringBuilder response = new StringBuilder();
            while (scanner.hasNextLine()) {
                response.append(scanner.nextLine());
            }
            scanner.close();
            return response.toString();
        } catch (Exception e) {
            return input;
        }
    }

    private void handleAddComment() {
        String text = commentInputField.getText().trim();
        if (text.isEmpty()) return;

        text = filterBadWords(text);

        try {
            commentaire_publication newComment = new commentaire_publication();
            newComment.setCommentaire(text);
            newComment.setPublication_parcours_id(currentPublication.getId());
            newComment.setDate_commentaire(java.time.LocalDate.now().toString());

            commentService.ajouter(newComment);
            commentInputField.clear();
            loadComments();
            updateCommentCount();
        } catch (SQLException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur lors de l'ajout du commentaire.");
            alert.show();
        }
    }

    private VBox createCommentUI(commentaire_publication comment) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 8; -fx-border-color: #dfe6e9; -fx-border-radius: 8;");

        HBox header = new HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label userLabel = new Label("User • ");
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2d3436; -fx-font-size: 12;");

        Label dateLabel = new Label(comment.getDate_commentaire());
        dateLabel.setStyle("-fx-text-fill: #b2bec3; -fx-font-size: 11;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnEdit = new Button("✏");
        btnEdit.setStyle("-fx-background-color: transparent; -fx-text-fill: #747d8c; -fx-cursor: hand; -fx-padding: 0 5 0 5;");

        Button btnDelete = new Button("✖");
        btnDelete.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff7675; -fx-cursor: hand; -fx-padding: 0 5 0 5;");

        header.getChildren().addAll(userLabel, dateLabel, spacer, btnEdit, btnDelete);

        Label contentLabel = new Label(comment.getCommentaire());
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-text-fill: #2d3436; -fx-font-size: 13;");

        box.getChildren().addAll(header, contentLabel);

        btnEdit.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog(comment.getCommentaire());
            dialog.setTitle("Modifier Commentaire");
            dialog.setHeaderText("Modifiez votre commentaire :");
            dialog.setContentText("Commentaire :");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newText -> {
                if (!newText.trim().isEmpty()) {
                    try {
                        String filteredText = filterBadWords(newText);
                        comment.setCommentaire(filteredText);
                        commentService.modifier(comment);
                        loadComments();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        });

        btnDelete.setOnAction(e -> {
            try {
                commentService.supprimer(comment.getId());
                loadComments();
                updateCommentCount();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        return box;
    }
}