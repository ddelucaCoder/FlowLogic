package com.FlowLogic;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import org.w3c.dom.events.Event;

import java.io.File;
import java.io.FileInputStream;


public class UserInterface extends Application {

    // Global constants for screen width, height, and grid dimensions
    private static final int SCREEN_WIDTH = 1280;      // Width of the screen
    private static final int SCREEN_HEIGHT = 720;     // Height of the screen

    private static final int CELL_SIZE = 32;          // Fixed cell size of 32x32
    private static final int GRID_ROWS = 100;         // Number of rows in the grid
    private static final int GRID_COLS = 100;         // Number of columns in the grid

    // Variables to track zoom and pan offsets
    private double offsetX = 0;
    private double offsetY = 0;
    public static Grid grid;

    // Tracks if the User is Panning the screen disables clicking events
    private boolean pan = false;

    @Override
    public void start(Stage primaryStage) throws Error{
        //Stops user from resizing the window
        primaryStage.setResizable(false);

        // Create an AnchorPane to contain everything
        AnchorPane root = new AnchorPane();
        root.setStyle("-fx-background-color: lightgray;");

        Pane gridContainer = new Pane();
        gridContainer.setStyle("-fx-border-color: black; -fx-border-width: 2px;");

        Group gridGroup = new Group();
        gridContainer.getChildren().add(gridGroup);
        root.getChildren().add(gridContainer);

        // Define grid container size such that the grid is a Square
        double gridViewWidth = SCREEN_WIDTH * ((SCREEN_HEIGHT * 1.0) / SCREEN_WIDTH);
        double gridViewHeight = SCREEN_HEIGHT * 1.00;

        gridContainer.setPrefSize(gridViewWidth, gridViewHeight);

        Rectangle clip = new Rectangle(gridViewWidth, gridViewHeight);
        gridContainer.setClip(clip);

        AnchorPane.setLeftAnchor(gridContainer, (SCREEN_WIDTH - gridViewWidth) / 2);
        AnchorPane.setRightAnchor(gridContainer, (SCREEN_WIDTH - gridViewWidth) / 2);


        // Create the zoom and pan functionality
        final Scale scale = new Scale();
        gridGroup.getTransforms().add(scale);

        double maxZoom = SCREEN_HEIGHT/(32 * grid.getNumColumns() * 1.0);
        scale.setY(maxZoom);
        scale.setX(maxZoom);

        // Zoom in/out using mouse wheel
        root.setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                scale.setX(scale.getX() * 1.1);
                scale.setY(scale.getY() * 1.1);
            } else {
                scale.setX(scale.getX() / 1.1);
                scale.setY(scale.getY() / 1.1);
            }
            if (scale.getX() < maxZoom || scale.getY() < maxZoom) {
                scale.setX(maxZoom);
                scale.setY(maxZoom);
            }

            ensureXY(gridContainer, scale);

            gridGroup.setTranslateX(offsetX);
            gridGroup.setTranslateY(offsetY);
        });

        // Panning functionality (dragging the grid)
        final double[] mousePos = new double[2];
        gridGroup.setOnMousePressed(event -> {
            mousePos[0] = event.getSceneX();
            mousePos[1] = event.getSceneY();
        });

        gridGroup.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - mousePos[0];
            double deltaY = event.getSceneY() - mousePos[1];
            offsetX += deltaX;
            offsetY += deltaY;

            ensureXY(gridContainer, scale);

            gridGroup.setTranslateX(offsetX);
            gridGroup.setTranslateY(offsetY);
            mousePos[0] = event.getSceneX();
            mousePos[1] = event.getSceneY();
            pan = true;
        });

        // Create the grid cells
        createGridCells(gridGroup);

        gridGroup.setOnMouseClicked(event -> {
            if (!pan){
                // Get the mouse click coordinates
                double x = event.getX();
                double y = event.getY();

                // Calculate the grid position (row, column)
                int row = (int) (y / CELL_SIZE);
                int col = (int) (x / CELL_SIZE);

                Image img = new Image("file:Images/Penguin.png");
                grid.getFrontGrid()[row][col].setFill(new ImagePattern(img));

            }
            pan = false;
        });

        VBox left = new VBox();
        left.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
        left.setPrefWidth((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2);
        left.setStyle("-fx-background-color: #D3D3D3;");

        AnchorPane.setLeftAnchor(left, 0.0);
        AnchorPane.setTopAnchor(left, 0.0);     // Set top anchor
        AnchorPane.setBottomAnchor(left, 0.0);  // Set bottom anchor
        root.getChildren().add(left);

        VBox right = new VBox();
        right.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
        right.setPrefWidth((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2);
        right.setStyle("-fx-background-color: #D3D3D3;");

        AnchorPane.setRightAnchor(right, 0.0);
        AnchorPane.setTopAnchor(right, 0.0);     // Set top anchor
        AnchorPane.setBottomAnchor(right, 0.0);  // Set bottom anchor
        root.getChildren().add(right);


        // Set up a Scene
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("FlowLogic");
        primaryStage.show();
    }

    private void createGridCells(Group gridGroup) {
        // Create a large grid of cells that always exists
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                // Create each cell as a rectangle
                Rectangle cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                grid.getFrontGrid()[row][col] = cell;
                cell.setFill(Color.LIGHTGRAY);
                cell.setStroke(Color.BLACK);
                cell.setX(col * CELL_SIZE);
                cell.setY(row * CELL_SIZE);

                // Add the cell to the grid group
                gridGroup.getChildren().add(cell);
            }
        }
    }

    private void ensureXY(Pane gridContainer, Scale scale){
        double scaleFactor = scale.getX(); // Get current scale
        double gridWidth = GRID_COLS * CELL_SIZE * scaleFactor; // Scaled grid width
        double gridHeight = GRID_ROWS * CELL_SIZE * scaleFactor; // Scaled grid height
        double gridContainerWidth = gridContainer.getPrefWidth();
        double gridContainerHeight = gridContainer.getPrefHeight();

        // Ensure grid stays within visible bounds
        double minX = gridContainerWidth - gridWidth;
        double minY = gridContainerHeight - gridHeight;

        if (offsetX > 0) {
            offsetX = 0; // Prevent panning right
        }
        if (offsetX < minX) {
            offsetX = minX; // Prevent panning left
        }
        if (offsetY > 0) {
            offsetY = 0; // Prevent panning down
        }
        if (offsetY < minY) {
            offsetY = minY; // Prevent panning up
        }
    }

    public static void main(String[] args) {
        grid = new Grid(GRID_ROWS,GRID_COLS);
        launch(args);
    }
}