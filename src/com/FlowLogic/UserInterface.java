package com.FlowLogic;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.File;


public class UserInterface extends Application {

    // Global constants for screen width, height, and grid dimensions
    private static final int SCREEN_WIDTH = 1280;      // Width of the screen
    private static final int SCREEN_HEIGHT = 720;     // Height of the screen

    private static final int CELL_SIZE = 32;          // Fixed cell size of 32x32

    private static int GRID_SIZE = 20;         // Number of rows and columns in the grid

    // Variables to track zoom and pan offsets
    private static double offsetX = 0;
    private static double offsetY = 0;
    public static Grid grid = new Grid(0,0);

    // Tracks if the User is Panning the screen disables clicking events
    private static boolean pan = false;


    public static Group gridGroup = new Group();
    public static Rectangle clip;
    public static Pane gridContainer = new Pane();
    public static double gridViewWidth;
    public static double gridViewHeight;
    public static double maxZoom;
    public static Scale scale = new Scale();

    private static Stage stage;

    @Override
    public void start(Stage primaryStage) throws Error{
        stage = primaryStage;
        stage.setTitle("FlowLogic");

        Label title = new Label("FlowLogic");
        title.setStyle("-fx-font-size: 32px; -fx-font-weight: bold;");
        Button newButton = new Button("New");
        Button loadButton = new Button("Load");
        newButton.setPrefSize(100, 20);
        loadButton.setPrefSize(100,20);
        newButton.setOnAction(event -> {
            Stage dialog = new Stage();
            dialog.setTitle("FlowLogic");
            Label prompt = new Label("Enter a size for the Grid");
            TextField sizeField = new TextField();
            // Define a TextFormatter that only allows digits
            TextFormatter<String> numberFormatter = new TextFormatter<>(change -> {
                if (change.getText().matches("[0-9]*")) {
                    return change;  // Accept change
                }
                return null;  // Reject change
            });
            sizeField.setTextFormatter(numberFormatter);
            Button confirmButton = new Button("OK");
            confirmButton.setOnAction(e -> {
                int value = Integer.parseInt(sizeField.getText());
                System.out.println("User entered: " + value);
                grid = new Grid(value,value);
                GRID_SIZE = value;
                dialog.close();
                setupBuildMenu();
            });
            VBox layout = new VBox(10,prompt , sizeField, confirmButton);
            layout.setAlignment(Pos.CENTER);
            Scene s = new Scene(layout, 200, 150);
            dialog.setScene(s);
            dialog.showAndWait();
        });
        loadButton.setOnAction(e -> setupLoadMenu());
        VBox root = new VBox(20, title, newButton, loadButton);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    private static void setupBuildMenu(){
        //Stops user from resizing the window
        stage.setResizable(false);

        // Create an AnchorPane to contain everything
        AnchorPane root = new AnchorPane();

        gridContainer = new Pane();
        gridContainer.setStyle("-fx-border-color: black; -fx-border-width: 2px;");

        // Define grid container size such that the grid is a Square
        gridViewWidth = SCREEN_WIDTH * ((SCREEN_HEIGHT * 1.0) / SCREEN_WIDTH);
        gridViewHeight = SCREEN_HEIGHT * 1.00;

        gridGroup = new Group();
        gridContainer.getChildren().add(gridGroup);
        root.getChildren().add(gridContainer);

        gridContainer.setPrefSize(gridViewWidth, gridViewHeight);

        clip = new Rectangle(gridViewWidth, gridViewHeight);
        gridContainer.setClip(clip);

        AnchorPane.setLeftAnchor(gridContainer, (SCREEN_WIDTH - gridViewWidth) / 2);
        AnchorPane.setRightAnchor(gridContainer, (SCREEN_WIDTH - gridViewWidth) / 2);


        // Create the zoom and pan functionality
        scale = new Scale();
        gridGroup.getTransforms().add(scale);

        maxZoom = SCREEN_HEIGHT / (32 * grid.getNumColumns() * 1.0);
        scale.setY(maxZoom);
        scale.setX(maxZoom);

        // Zoom in/out using mouse wheel
        gridContainer.setOnScroll(event -> {
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

        gridContainer.setOnMouseDragged(event -> {
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



        gridContainer.setOnDragOver(event -> {
            //Cell accepts transfer if the source is an image and is coming from the Dragboard
            if (event.getGestureSource() instanceof ImageView && event.getDragboard().hasImage()) {
                event.acceptTransferModes(TransferMode.COPY);
            }
        });

        gridContainer.setOnDragDropped(event -> {
            //Fills the cell with the image
            Dragboard db = event.getDragboard();
            // Get the mouse click coordinates
            double x = (event.getX() - offsetX) / scale.getX();
            double y = (event.getY() - offsetY) / scale.getY();

            // Calculate the grid position (row, column)
            int row = (int) (y / CELL_SIZE);
            int col = (int) (x / CELL_SIZE);
            Rectangle cell = grid.getFrontGrid()[row][col];
            if (!(cell.getFill() instanceof ImagePattern)) {
                cell.setFill(new ImagePattern(db.getImage()));
                grid.placeObjectByImage(db.getString(), row, col);
            }

        });

        GridPane left = new GridPane();
        left.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
        left.setPrefWidth((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2);
        left.setStyle("-fx-background-color: #D3D3D3;");
        left.setHgap(6);
        left.setVgap(6);

        AnchorPane.setLeftAnchor(left, 0.0);
        AnchorPane.setTopAnchor(left, 0.0);     // Set top anchor
        AnchorPane.setBottomAnchor(left, 0.0);  // Set bottom anchor
        root.getChildren().add(left);
        addDraggableImages(left, 4);

        VBox right = new VBox();
        right.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
        right.setPrefWidth((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2);
        right.setStyle("-fx-background-color: #D3D3D3;");

        AnchorPane.setRightAnchor(right, 0.0);
        AnchorPane.setTopAnchor(right, 0.0);     // Set top anchor
        AnchorPane.setBottomAnchor(right, 0.0);  // Set bottom anchor
        root.getChildren().add(right);


        // Add the save button to the bottom right of the grid
        saveGridButton(right, grid);
        // Add the load button to the grid
        loadGridButton(right, grid);
        // add the resize button to the top right
        hideResizeBox(right, grid);


        gridContainer.setOnMouseClicked(event -> {
            if (!pan){
                // Get the mouse click coordinates
                double x = (event.getX() - offsetX) / scale.getX();
                double y = (event.getY() - offsetY) / scale.getY();

                // Calculate the grid position (row, column)
                int row = (int) (y / CELL_SIZE);
                int col = (int) (x / CELL_SIZE);

                // select the square
                grid.select(row, col, right);
            }
            pan = false;
        });

        // Set up a Scene
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(scene);
        stage.show();
    }

    private static void setupLoadMenu() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        loadGridButton(root, grid);
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(scene);
        stage.show();

    }

    private static void createGridCells(Group gridGroup) {
        // Create a large grid of cells that always exists
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                // Create each cell as a rectangle
                Rectangle cell = grid.getFrontGrid()[row][col];
                if (cell == null) {
                    cell = new Rectangle(CELL_SIZE, CELL_SIZE);
                }
                if (!(cell.getFill() instanceof ImagePattern)) {
                    //Default Cell config
                    cell.setFill(Color.LIGHTGRAY);
                    cell.setStroke(Color.BLACK);
                    cell.setX(col * CELL_SIZE);
                    cell.setY(row * CELL_SIZE);
                    grid.getFrontGrid()[row][col] = cell;
                }
                gridGroup.getChildren().add(cell);
            }
        }
    }

    private static void ensureXY(Pane gridContainer, Scale scale){
        double scaleFactor = scale.getX(); // Get current scale
        double gridWidth = GRID_SIZE * CELL_SIZE * scaleFactor; // Scaled grid width
        double gridHeight = GRID_SIZE * CELL_SIZE * scaleFactor; // Scaled grid height
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
    private static void addDraggableImages(GridPane left, int numColumns) {
        File dir = new File("Images");
        int count = 0;
        if (dir.exists() && dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                Image img = new Image(file.toURI().toString());
                ImageView imageView = new ImageView(img);
                imageView.setUserData(file.getName());
                imageView.setFitWidth(64);
                imageView.setFitHeight(64);
                int row = count / numColumns;
                int col = count % numColumns;
                count++;

                // Enable dragging
                imageView.setOnDragDetected(event -> {
                    //Transfers the data
                    Dragboard db = imageView.startDragAndDrop(TransferMode.COPY);
                    //Stores the data
                    ClipboardContent content = new ClipboardContent();
                    content.putString(file.getName());
                    content.putImage(img);
                    db.setContent(content);
                });

                left.add(imageView, col, row);
            }
        }
    }

    /**
     * This method will add a save grid button to the bottom right of the application
     * It will be connected to Grid.java's saveGridState function
     *
     * @param mainLayout The main VBox layout
     * @param grid The Grid object containing the grid data to save
     */
    public static void saveGridButton(VBox mainLayout, Grid grid) {
        // Create the button
        Button saveButton = new Button("Save Current Layout");
        saveButton.setPrefSize((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2, 30);

        // Add the button to the AnchorPane
        mainLayout.getChildren().add(saveButton);

        saveButton.setOnAction(event -> {
            // Create a file chooser dialog - select where to save it
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Current Layout");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );

            // Show the save dialog
            File file = fileChooser.showSaveDialog(mainLayout.getScene().getWindow());

            if (file != null) {
                // Call the grid's saveGridState method with the selected file path
                boolean saveSuccessful = grid.saveGridState(file.getAbsolutePath());

                if (saveSuccessful) {
                    // Insert any additional success logic here (popup?)
                    System.out.println("Grid saved successfully to " + file.getName());
                } else {
                    // Insert any additional error logic here (popup?)
                    System.out.println("Failed to save grid to " + file.getName());
                }
            }
        });
    }

    /**
     * This method will add a load grid button to the UI of the application
     * It will be connected to Grid.java's loadGridState function
     *
     * @param mainLayout The main AnchorPane layout
     * @param grid The Grid object containing the grid data to save
     */
    public static void loadGridButton(VBox mainLayout, Grid grid) {
        // Create the button
        Button loadButton = new Button("Load Existing Layout");
        loadButton.setPrefSize((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2, 30);

        // Add the button to the AnchorPane
        mainLayout.getChildren().add(loadButton);

        loadButton.setOnAction(event -> {
            // Create a file chooser dialog - select where to save it
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load Existing Layout");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JSON Files", "*.json")
            );

            // Show the load dialog
            File file = fileChooser.showOpenDialog(mainLayout.getScene().getWindow());

            if (file != null) {
                // Call the grid's loadGridState method with the selected file path
                boolean loadSuccessful = grid.loadGridState(file.getAbsolutePath());

                if (loadSuccessful) {
                    // Insert any additional success logic here (popup?)
                    System.out.println("Grid loaded successfully from " + file.getName());
                    setupBuildMenu();
                } else {
                    // Insert any additional error logic here (popup?)
                    System.out.println("Failed to load grid from " + file.getName());
                }
            }
        });
    }

    public static void refreshGrid(int newSize) {
        GRID_SIZE = newSize;
        gridGroup.getChildren().clear();
        createGridCells(gridGroup);
        AnchorPane.setLeftAnchor(gridContainer, (SCREEN_WIDTH - gridViewWidth) / 2);
        AnchorPane.setRightAnchor(gridContainer, (SCREEN_WIDTH - gridViewWidth) / 2);
        maxZoom = SCREEN_HEIGHT / (32 * grid.getNumColumns() * 1.0);
        scale.setY(maxZoom);
        scale.setX(maxZoom);
    }

    public static void hideResizeBox(VBox mainLayout, Grid grid) {
        Button showButton = new Button("Edit Grid Size");
        showButton.setPrefSize((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2, 30);
        mainLayout.getChildren().add(showButton);
        showButton.setOnAction(e -> {
            mainLayout.getChildren().remove(showButton);
            gridResizeBox(mainLayout, grid);
        });
    }

    public static void gridResizeBox(VBox mainLayout, Grid grid) {
        Label instructionLabel = new Label("Enter a size for the grid:");
        TextField sizeField = new TextField();
        // Define a TextFormatter that only allows digits
        TextFormatter<String> numberFormatter = new TextFormatter<>(change -> {
            if (change.getText().matches("[0-9]*")) {
                return change;  // Accept change
            }
            return null;  // Reject change
        });
        sizeField.setPrefSize((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2, 30);

        sizeField.setTextFormatter(numberFormatter);

        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String input = sizeField.getText();
            int size;
            if (input.isEmpty()) {
                return;
            } else {
                size = Integer.parseInt(input);
            }
            grid.resize(size, size);
            refreshGrid(size);

        });

        // Add the button to the AnchorPane
        mainLayout.getChildren().add(instructionLabel);
        mainLayout.getChildren().add(sizeField);
        mainLayout.getChildren().add(submitButton);

        Button hideButton = new Button("Hide this option");
        mainLayout.getChildren().add(hideButton);
        hideButton.setOnAction(e -> {
            mainLayout.getChildren().remove(instructionLabel);
            mainLayout.getChildren().remove(sizeField);
            mainLayout.getChildren().remove(submitButton);
            mainLayout.getChildren().remove(hideButton);
            hideResizeBox(mainLayout, grid);
        });
    }

    public static void showBuildingOptions(VBox mainLayout, Grid grid, int xLen, int yLen, int dailyPop, int row,
                                           int col) {
        Label titleLabel = new Label("Building Options");
        Label xLabel = new Label("xLength:");
        TextField xLengthField = new TextField();
        Label yLabel = new Label("yLength:");
        TextField yLengthField = new TextField();
        Label populationLabel = new Label("dailyPopulation:");
        TextField populationField = new TextField();
        Button submitButton = new Button("Submit Changes");
        Button removeButton = new Button("Remove Building");
        Button closeButton = new Button("Close Building Options");


        // formatters
        TextFormatter<String> numberFormatterX = new TextFormatter<>(change -> {
            if (change.getText().matches("[0-9]*")) {
                return change;  // Accept change
            }
            return null;  // Reject change
        });

        TextFormatter<String> numberFormatterY = new TextFormatter<>(change -> {
            if (change.getText().matches("[0-9]*")) {
                return change;  // Accept change
            }
            return null;  // Reject change
        });

        TextFormatter<String> numberFormatterPop = new TextFormatter<>(change -> {
            if (change.getText().matches("[0-9]*")) {
                return change;  // Accept change
            }
            return null;  // Reject change
        });

        // add formatters to fields
        xLengthField.setTextFormatter(numberFormatterX);
        yLengthField.setTextFormatter(numberFormatterY);
        populationField.setTextFormatter(numberFormatterPop);

        xLengthField.setText(Integer.toString(xLen));
        yLengthField.setText(Integer.toString(yLen));
        populationField.setText(Integer.toString(dailyPop));

        mainLayout.getChildren().add(titleLabel);
        mainLayout.getChildren().add(xLabel);
        mainLayout.getChildren().add(xLengthField);
        mainLayout.getChildren().add(yLabel);
        mainLayout.getChildren().add(yLengthField);
        mainLayout.getChildren().add(populationLabel);
        mainLayout.getChildren().add(populationField);
        mainLayout.getChildren().add(submitButton);
        mainLayout.getChildren().add(removeButton);
        mainLayout.getChildren().add(closeButton);

        removeButton.setOnAction(e -> {
            grid.remove(row, col);
            refreshGrid(GRID_SIZE);
            mainLayout.getChildren().remove(titleLabel);
            mainLayout.getChildren().remove(xLabel);
            mainLayout.getChildren().remove(xLengthField);
            mainLayout.getChildren().remove(yLabel);
            mainLayout.getChildren().remove(yLengthField);
            mainLayout.getChildren().remove(populationLabel);
            mainLayout.getChildren().remove(populationField);
            mainLayout.getChildren().remove(submitButton);
            mainLayout.getChildren().remove(removeButton);
            mainLayout.getChildren().remove(closeButton);
        });

        closeButton.setOnAction(e -> {
            mainLayout.getChildren().remove(titleLabel);
            mainLayout.getChildren().remove(xLabel);
            mainLayout.getChildren().remove(xLengthField);
            mainLayout.getChildren().remove(yLabel);
            mainLayout.getChildren().remove(yLengthField);
            mainLayout.getChildren().remove(populationLabel);
            mainLayout.getChildren().remove(populationField);
            mainLayout.getChildren().remove(submitButton);
            mainLayout.getChildren().remove(removeButton);
            mainLayout.getChildren().remove(closeButton);
        });


    }

    public static void main(String[] args) {
        launch(args);
    }
}
