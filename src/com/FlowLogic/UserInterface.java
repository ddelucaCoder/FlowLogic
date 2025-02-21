package com.FlowLogic;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class UserInterface extends Application {

    private static final double WIDTH = 640;
    private static final double HEIGHT = 480;

    public void start(Stage stage) {
        // Set up your JavaFX UI components
        BorderPane layoutManager = new BorderPane();
        Scene scene = new Scene(layoutManager, WIDTH, HEIGHT);

        stage.setTitle("Intro to JavaFX");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
    public static void main(String[] args) {
        launch(args);
    }
}
