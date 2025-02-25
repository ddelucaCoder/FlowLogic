package com.FlowLogic;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;


public class UserInterface extends Application {

    // Global constants for screen width, height, and grid dimensions
    private static final int SCREEN_WIDTH = 1600;      // Width of the screen
    private static final int SCREEN_HEIGHT = 900;     // Height of the screen

    private static final int CELL_SIZE = 32;          // Fixed cell size of 32x32
    private static final int GRID_ROWS = 10;         // Number of rows in the grid
    private static final int GRID_COLS = 10;         // Number of columns in the grid

    // Variables to track zoom and pan offsets
    private double offsetX = 0;
    private double offsetY = 0;
    private double scaleFactor = 1.0; // Track zoom level

    // Tracks if the User is Panning the screen disables clicking events
    private boolean pan = false;

    @Override
    public void start(Stage primaryStage) {
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
        double gridViewWidth = SCREEN_WIDTH * 0.5625;
        double gridViewHeight = SCREEN_HEIGHT * 1.00;

        gridContainer.setPrefSize(gridViewWidth, gridViewHeight);

        Rectangle clip = new Rectangle(gridViewWidth, gridViewHeight);
        gridContainer.setClip(clip);

        AnchorPane.setLeftAnchor(gridContainer, (SCREEN_WIDTH - gridViewWidth) / 2);
        AnchorPane.setRightAnchor(gridContainer, (SCREEN_WIDTH - gridViewWidth) / 2);


        // Create the zoom and pan functionality
        final Scale scale = new Scale();
        gridGroup.getTransforms().add(scale);

        // Zoom in/out using mouse wheel
        root.setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                scale.setX(scale.getX() * 1.1);
                scale.setY(scale.getY() * 1.1);
            } else {
                scale.setX(scale.getX() / 1.1);
                scale.setY(scale.getY() / 1.1);
            }
            event.consume();
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

            double gridContainerWidth = gridContainer.getPrefWidth();
            double gridContainerHeight = gridContainer.getPrefHeight();
            double gridGroupWidth = gridGroup.getBoundsInLocal().getWidth();
            double gridGroupHeight = gridGroup.getBoundsInLocal().getHeight();

            if (offsetX > 0) {
                offsetX = 0; // Prevent panning to the right
            }
            if (offsetX < gridContainerWidth - gridGroupWidth) {
                offsetX = gridContainerWidth - gridGroupWidth; // Prevent panning to the left
            }
            if (offsetY > 0) {
                offsetY = 0; // Prevent panning down
            }
            if (offsetY < gridContainerHeight - gridGroupHeight) {
                offsetY = gridContainerHeight - gridGroupHeight; // Prevent panning up
            }

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

                // Print the grid position
                System.out.println("Grid position: (" + row + ", " + col + ")");

                // Get the clicked cell (rectangle) and change its color
                for (Object node : gridGroup.getChildren()) {
                    if (node instanceof Rectangle) {
                        Rectangle cell = (Rectangle) node;
                        // Check if the clicked position is within the bounds of this cell
                        if (cell.getX() <= x && cell.getX() + CELL_SIZE > x && cell.getY() <= y && cell.getY() + CELL_SIZE > y) {
                            // Change the color of the clicked cell
                            cell.setFill(Color.BLUE);
                            break;
                        }
                    }
                }
            }
            pan = false;
        });

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
                cell.setFill(Color.LIGHTGRAY);
                cell.setStroke(Color.BLACK);
                cell.setX(col * CELL_SIZE);
                cell.setY(row * CELL_SIZE);

                // Add the cell to the grid group
                gridGroup.getChildren().add(cell);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}