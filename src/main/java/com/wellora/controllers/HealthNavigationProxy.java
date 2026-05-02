package com.wellora.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

/**
 * HealthNavigationProxy — bridges HealthShellController (PIDEV-JAVA shell)
 * with integ controllers that expect a MainController for in-shell navigation.
 *
 * Integ controllers call proxy.showHealthJournals() etc. instead of mainController.*
 */
public class HealthNavigationProxy {

    private final VBox contentArea;
    private final HealthShellController shellController;

    public HealthNavigationProxy(VBox contentArea, HealthShellController shellController) {
        this.contentArea = contentArea;
        this.shellController = shellController;
    }

    public void showHomepage()        { loadHealth("/fxml/homepage.fxml"); }
    public void showDashboard()       { loadHealth("/fxml/dashboard.fxml"); }
    public void showHealthJournals()  { loadHealth("/fxml/healthjournal-list.fxml"); }
    public void showHealthEntries()   { loadHealth("/fxml/healthentry-list.fxml"); }
    public void showSymptoms()        { loadHealth("/fxml/symptom-list.fxml"); }
    public void showCalendar()        { loadHealth("/fxml/calendar.fxml"); }
    public void showPrediction()      { loadHealth("/fxml/prediction.fxml"); }

    /** Load any health view directly into the shell content area */
    public void loadContentView(Parent view) {
        contentArea.getChildren().setAll(view);
    }

    private void loadHealth(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            injectProxy(loader.getController());
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("❌ HealthNav failed: " + fxmlPath + " — " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Inject this proxy into any integ controller that needs navigation */
    public void injectProxy(Object controller) {
        if (controller == null) return;
        try {
            if (controller instanceof com.wellora.javafx.controller.HomepageController hc) {
                hc.setMainControllerProxy(this);
            } else if (controller instanceof com.wellora.javafx.controller.HealthjournalListController c) {
                c.setMainControllerProxy(this);
            } else if (controller instanceof com.wellora.javafx.controller.HealthentryListController c) {
                c.setMainControllerProxy(this);
            } else if (controller instanceof com.wellora.javafx.controller.HealthjournalFormController c) {
                c.setMainControllerProxy(this);
            } else if (controller instanceof com.wellora.javafx.controller.HealthentryFormController c) {
                c.setMainControllerProxy(this);
            } else if (controller instanceof com.wellora.javafx.controller.SymptomListController c) {
                c.setMainControllerProxy(this);
            } else if (controller instanceof com.wellora.javafx.controller.CalendarController c) {
                c.setMainControllerProxy(this);
            } else if (controller instanceof com.wellora.javafx.controller.PredictionController c) {
                c.setMainControllerProxy(this);
            }
        } catch (Exception e) {
            System.err.println("Proxy injection failed: " + e.getMessage());
        }
    }
}
