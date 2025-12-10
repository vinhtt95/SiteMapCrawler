package com.vinhtt.sitemapcrawler;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * Standard JavaFX Application class.
 *
 * @author vinhtt
 * @version 1.0
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/main-view.fxml")));
        Scene scene = new Scene(root);

        // Load CSS
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/dark-theme.css")).toExternalForm());

        stage.setTitle("SiteMap Crawler - MVVM Architecture");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Entry point called by Launcher.
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch();
    }
}