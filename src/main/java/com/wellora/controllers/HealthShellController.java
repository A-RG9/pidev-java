package com.wellora.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

/**
 * HealthShellController — wraps integ health views inside PIDEV-JAVA's dark sidebar.
 * Creates a HealthNavigationProxy so integ controllers can navigate within the shell.
 */
public class HealthShellController extends BaseController {

    @FXML private BorderPane rootPane;
    @FXML private VBox contentArea;

    private HealthNavigationProxy proxy;

    @FXML
    private void initialize() {
        proxy = new HealthNavigationProxy(contentArea, this);
    }

    /** Load a health view into the content area (called from BaseController.switchHealthScene) */
    public void setContent(Parent content) {
        contentArea.getChildren().setAll(content);
    }

    /** Load a health view AND inject proxy for navigation */
    public void setContentWithProxy(String fxmlPath) {
        setContentWithProxy(fxmlPath, null);
    }

    /** Load a health view AND inject proxy for navigation, with data for the controller */
    public void setContentWithProxy(String fxmlPath, Object data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            Object controller = loader.getController();
            proxy.injectProxy(controller);
            
            // Pass data to controller if it has an initData method
            if (data != null && controller != null) {
                try {
                    java.lang.reflect.Method method = controller.getClass().getMethod("initData", data.getClass());
                    method.invoke(controller, data);
                } catch (NoSuchMethodException e) {
                    // Try with Object parameter
                    try {
                        java.lang.reflect.Method method = controller.getClass().getMethod("initData", Object.class);
                        method.invoke(controller, data);
                    } catch (NoSuchMethodException ex) {
                        // No initData method, that's ok
                    }
                }
            }
            
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("❌ Shell load failed: " + fxmlPath + " — " + e.getMessage());
            e.printStackTrace();
        }
    }

    public HealthNavigationProxy getProxy() { return proxy; }

    @Override
    protected Parent getRoot() { return rootPane; }
}
