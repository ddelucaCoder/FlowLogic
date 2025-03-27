package com.FlowLogic;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;


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

    // Save Directory Path
    private static final String SAVE_DIRECTORY = "saves";

    @Override
    public void start(Stage primaryStage) throws Error {
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

    private void setupBuildMenu(){
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
            Image dbImage = db.getImage();
            // Get the mouse click coordinates
            double x = (event.getX() - offsetX) / scale.getX();
            double y = (event.getY() - offsetY) / scale.getY();

            // Calculate the grid position (row, column)
            int row = (int) (y / CELL_SIZE);
            int col = (int) (x / CELL_SIZE);
            Rectangle cell = grid.getFrontGrid()[row][col];
            // Check to see if it is a two-way road
            if (grid.isTwoWayRoad(db.getString())) {
                System.out.println("This is a two way road\n");
                // add the two roads to the graph
                int row2;
                int col2;
                Rectangle cell2;
                if(db.getString().equals("TwoWayRoad.png")) {
                    // add up and down one-ways
                    col2 = col + 1;
                    cell2 = grid.getFrontGrid()[row][col2];
                    Image image = new Image("file:Images/RoadImage.png");
                    if (!(cell2.getFill() instanceof ImagePattern)) {
                        cell2.setFill(new ImagePattern(image));
                        grid.placeObjectByImage("RoadImage.png", row, col2);
                        System.out.println(image.getUrl());
                    }
                    dbImage = new Image("file:Images/RoadImageDown.png");
                    if (!(cell.getFill() instanceof ImagePattern)) {
                        cell.setFill(new ImagePattern(dbImage));
                        grid.placeObjectByImage("RoadImageDown.png", row, col);
                        System.out.println(dbImage.getUrl());
                    }
                } else {
                    // add left and right one-ways
                    row2 = row + 1;
                    cell2 = grid.getFrontGrid()[row2][col];
                    Image image = new Image("file:Images/RoadImageRight.png");
                    if (!(cell2.getFill() instanceof ImagePattern)) {
                        cell2.setFill(new ImagePattern(image));
                        grid.placeObjectByImage("RoadImageRight.png", row2, col);
                        System.out.println(image.getUrl());
                    }
                    dbImage = new Image("file:Images/RoadImageLeft.png");
                    if (!(cell.getFill() instanceof ImagePattern)) {
                        cell.setFill(new ImagePattern(dbImage));
                        grid.placeObjectByImage("RoadImageLeft.png", row, col);
                        System.out.println(dbImage.getUrl());
                    }
                }
            } else if (!(cell.getFill() instanceof ImagePattern)) {
                cell.setFill(new ImagePattern(dbImage));
                grid.placeObjectByImage(db.getString(), row, col);
                System.out.println(db.getString());
            }

            // Update container classes for roads
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

    private void setupLoadMenu() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Load Saved Layout");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // Get all save files from the save directory
        File[] saveFiles = getSaveFiles();

        // Create "I don't see my file" button
        Button browseButton = new Button("I don't see my file");
        browseButton.setOnAction(e -> browseForExternalSaveFile());

        if (saveFiles == null || saveFiles.length == 0) {
            // No available save files
            Label noFilesLabel = new Label("No save files found");
            Button backButton = new Button("Back to Main Menu");
            backButton.setOnAction(e -> start(stage));

            root.getChildren().addAll(titleLabel, noFilesLabel, browseButton, backButton);
        } else {
            // Save files found
            // Convert file array to observable list for ListView
            ObservableList<String> fileNames = FXCollections.observableArrayList();
            for (File file : saveFiles) {
                fileNames.add(file.getName());
            }

            // Create ListView to show save files
            ListView<String> saveFileListView = new ListView<>(fileNames);
            saveFileListView.setPrefHeight(400);
            saveFileListView.setPrefWidth(400);

            HBox buttonBox = new HBox(20);
            buttonBox.setAlignment(Pos.CENTER);

            Button loadButton = new Button("Load Selected");
            Button cancelButton = new Button("Cancel");

            loadButton.setOnAction(e -> {
                loadGridButton(root, grid);
                String selectedFileName = saveFileListView.getSelectionModel().getSelectedItem();
                if (selectedFileName != null) {
                    File selectedFile = new File(SAVE_DIRECTORY + File.separator + selectedFileName);
                    boolean loadSuccessful = grid.loadGridState(selectedFile.getAbsolutePath());

                    if (loadSuccessful) {
                        System.out.println("Grid loaded successfully from " + selectedFileName);
                        setupBuildMenu();
                    } else {
                        // Show error message
                        showErrorAlert("Failed to load grid from " + selectedFileName);
                    }
                } else {
                    showErrorAlert("Please select a save file");
                }
            });

            cancelButton.setOnAction(e -> start(stage));

            buttonBox.getChildren().addAll(loadButton, browseButton, cancelButton);
            root.getChildren().addAll(titleLabel, saveFileListView, buttonBox);
        }

        //loadGridButton(root, grid);
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

        // Add the button to the VBox
        mainLayout.getChildren().add(saveButton);

        saveButton.setOnAction(event -> {
            // Show save dialog to get a name
            TextInputDialog dialog = new TextInputDialog("layout");
            dialog.setTitle("Save Layout");
            dialog.setHeaderText("Enter a name for your layout");
            dialog.setContentText("Name:");

            createSaveDirectory();

            dialog.showAndWait().ifPresent(name -> {

                // Create filename with name and timestamp
                String filename = name + ".json";
                String filePath = SAVE_DIRECTORY + File.separator + filename;

                boolean saveSuccessful = grid.saveGridState(filePath);

                if (saveSuccessful) {
                    showInfoAlert("Layout saved", "Layout saved successfully as " + filename);
                    System.out.println("Grid saved successfully to " + filename);
                } else {
                    showErrorAlert("Failed to save layout to " + filename);
                    System.out.println("Failed to save grid to " + filename);
                }
            });
        });
    }

    /**
     * This method will add a load grid button to the UI of the application
     * It will be connected to Grid.java's loadGridState function
     *
     * @param mainLayout The main AnchorPane layout
     * @param grid The Grid object containing the grid data to save
     */
    public void loadGridButton(VBox mainLayout, Grid grid) {
        // Create the button
        Button loadButton = new Button("Load Existing Layout");
        loadButton.setPrefSize((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2, 30);

        // Add the button to the VBox
        mainLayout.getChildren().add(loadButton);

        loadButton.setOnAction(event -> setupLoadMenu());
    }

    /**
     * Creates the save directory if it doesn't exist
     */
    private static void createSaveDirectory() {
        Path path = Paths.get(SAVE_DIRECTORY);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
                System.out.println("Created save directory: " + path.toAbsolutePath());
            } catch (Exception e) {
                System.err.println("Failed to create save directory: " + e.getMessage());
            }
        }
    }

    /**
     * Gets all JSON files from the save directory
     *
     * @return Array of save files, sorted by last modified date (newest first)
     */
    private static File[] getSaveFiles() {
        File saveDir = new File(SAVE_DIRECTORY);
        File[] files = saveDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));

        if (files != null && files.length > 0) {
            // Sort files by last modified date (newest first)
            Arrays.sort(files, Comparator.comparing(File::lastModified).reversed());
        }

        return files;
    }

    /**
     * Shows an error alert dialog if something fails
     */
    private static void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Shows an information alert dialog when something succeeds
     */
    private static void showInfoAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Opens file explorer to select a save file from anywhere on the system,
     * then copies it to the save directory
     *
     * Used if the user's layout file, for whatever reason, is not in the correct directory
     */
    private void browseForExternalSaveFile() {
        // Create save directory if it doesn't exist
        createSaveDirectory();

        // Create a file chooser dialog
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Browse for Save File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("JSON Files", "*.json")
        );

        // Show the file dialog
        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            try {
                // Create destination file path in save directory
                String fileName = selectedFile.getName();
                File destinationFile = new File(SAVE_DIRECTORY + File.separator + fileName);

                // Check if file with same name already exists - ask to overwrite if it does
                if (destinationFile.exists()) {
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("File Already Exists");
                    confirmAlert.setHeaderText("A file with the name '" + fileName + "' already exists in the save directory.");
                    confirmAlert.setContentText("Do you want to replace it?");

                    if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                        // User confirmed overwrite
                        copyFile(selectedFile, destinationFile);
                    } else {
                        // User canceled overwrite - ask for a new name
                        renameAndCopyFile(selectedFile);
                        return;
                    }
                } else {
                    // No conflict, copy the file
                    copyFile(selectedFile, destinationFile);
                }

                // Refresh the load menu to show the new file
                setupLoadMenu();

            } catch (Exception e) {
                showErrorAlert("Error copying file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Copies a file from source to destination
     */
    private void copyFile(File sourceFile, File destinationFile) throws IOException {
        Files.copy(sourceFile.toPath(), destinationFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        showInfoAlert("File Copied", "File '" + sourceFile.getName() +
                "' has been copied to the save directory");
    }

    /**
     * Asks the user for a new filename and copies the file with that name
     */
    private void renameAndCopyFile(File sourceFile) {
        TextInputDialog dialog = new TextInputDialog(sourceFile.getName().replace(".json", "") + "_copy");
        dialog.setTitle("Rename File");
        dialog.setHeaderText("Please enter a new name for the file");
        dialog.setContentText("New filename (without extension):");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isEmpty()) {
                try {
                    // Create destination file with new name
                    File destinationFile = new File(SAVE_DIRECTORY + File.separator + newName + ".json");

                    // Copy the file
                    copyFile(sourceFile, destinationFile);

                    // Refresh the load menu
                    setupLoadMenu();

                } catch (Exception e) {
                    showErrorAlert("Error copying file: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * This function goes through and recreates the grid cell by cell (this is maybe not ideal). It is also used when
     * we need to resize the grid.
     * @param newSize - the size of the grid
     */
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

    /**
     * shows option to show the grid resizing option
     * @param mainLayout
     * @param grid
     */

    public static void hideResizeBox(VBox mainLayout, Grid grid) {
        Button showButton = new Button("Edit Grid Size");
        showButton.setPrefSize((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2, 30);
        mainLayout.getChildren().add(showButton);
        showButton.setOnAction(e -> {
            mainLayout.getChildren().remove(showButton);
            gridResizeBox(mainLayout, grid);
        });
    }

    /**
     * option to edit grid size
     * @param mainLayout
     * @param grid
     */

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

        submitButton.setOnAction(e -> {
            int xLenNew = Integer.parseInt(xLengthField.getText());
            int yLenNew = Integer.parseInt(yLengthField.getText());
            int popNew = Integer.parseInt(populationField.getText());
            if (xLenNew > 0 && yLenNew > 0 && popNew >= 0) {
                if (xLenNew != xLen || yLenNew != yLen) {
                    grid.changeBuildingSize(row, col, xLenNew, yLenNew);
                }
                if (popNew != dailyPop) {
                    grid.changeDailyPopulationBuilding(row, col, popNew);
                }
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

                showBuildingOptions(mainLayout, grid, xLenNew, yLenNew, popNew, row, col);
                refreshGrid(GRID_SIZE);
            }
        });

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

    public static void showParkingOptions(VBox mainLayout, Grid grid, int xLen, int yLen, int dailyPop, int row,
                                           int col) {
        Label titleLabel = new Label("Parking Options");
        Label xLabel = new Label("xLength:");
        TextField xLengthField = new TextField();
        Label yLabel = new Label("yLength:");
        TextField yLengthField = new TextField();
        Label parkingLabel = new Label("parkingCapacity:");
        TextField parkingField = new TextField();
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
        parkingField.setTextFormatter(numberFormatterPop);

        xLengthField.setText(Integer.toString(xLen));
        yLengthField.setText(Integer.toString(yLen));
        parkingField.setText(Integer.toString(dailyPop));

        mainLayout.getChildren().add(titleLabel);
        mainLayout.getChildren().add(xLabel);
        mainLayout.getChildren().add(xLengthField);
        mainLayout.getChildren().add(yLabel);
        mainLayout.getChildren().add(yLengthField);
        mainLayout.getChildren().add(parkingLabel);
        mainLayout.getChildren().add(parkingField);
        mainLayout.getChildren().add(submitButton);
        mainLayout.getChildren().add(removeButton);
        mainLayout.getChildren().add(closeButton);

        submitButton.setOnAction(e -> {
            int xLenNew = Integer.parseInt(xLengthField.getText());
            int yLenNew = Integer.parseInt(yLengthField.getText());
            int popNew = Integer.parseInt(parkingField.getText());
            if (xLenNew > 0 && yLenNew > 0 && popNew >= 0) {
                if (xLenNew != xLen || yLenNew != yLen) {
                    grid.changeBuildingSize(row, col, xLenNew, yLenNew);
                }
                if (popNew != dailyPop) {
                    grid.changeParkingCapacity(row, col, popNew);
                }
                mainLayout.getChildren().remove(titleLabel);
                mainLayout.getChildren().remove(xLabel);
                mainLayout.getChildren().remove(xLengthField);
                mainLayout.getChildren().remove(yLabel);
                mainLayout.getChildren().remove(yLengthField);
                mainLayout.getChildren().remove(parkingLabel);
                mainLayout.getChildren().remove(parkingField);
                mainLayout.getChildren().remove(submitButton);
                mainLayout.getChildren().remove(removeButton);
                mainLayout.getChildren().remove(closeButton);

                showParkingOptions(mainLayout, grid, xLenNew, yLenNew, popNew, row, col);
                refreshGrid(GRID_SIZE);
            }
        });

        removeButton.setOnAction(e -> {
            grid.remove(row, col);
            refreshGrid(GRID_SIZE);
            mainLayout.getChildren().remove(titleLabel);
            mainLayout.getChildren().remove(xLabel);
            mainLayout.getChildren().remove(xLengthField);
            mainLayout.getChildren().remove(yLabel);
            mainLayout.getChildren().remove(yLengthField);
            mainLayout.getChildren().remove(parkingLabel);
            mainLayout.getChildren().remove(parkingField);
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
            mainLayout.getChildren().remove(parkingLabel);
            mainLayout.getChildren().remove(parkingField);
            mainLayout.getChildren().remove(submitButton);
            mainLayout.getChildren().remove(removeButton);
            mainLayout.getChildren().remove(closeButton);
        });


    }

   public static void showRoadOptions(VBox mainLayout, Grid grid, int row, int col) {
        Label titleLabel= new Label("Road Options");
        Label directionLabel = new Label ("Direction:");
        Button upButt = new Button("Up");
        Button downButt = new Button("Down");
        Button leftButt = new Button("Left");
        Button rightButt = new Button("Right");
        Button removeButton = new Button("Remove Road");
        Button closeButton = new Button("Close Road Options");


        mainLayout.getChildren().add(titleLabel);
        mainLayout.getChildren().add(directionLabel);
        mainLayout.getChildren().add(upButt);
        mainLayout.getChildren().add(downButt);
        mainLayout.getChildren().add(leftButt);
        mainLayout.getChildren().add(rightButt);
        mainLayout.getChildren().add(removeButton);
        mainLayout.getChildren().add(closeButton);

        upButt.setOnAction(e -> {

            Rectangle cell = grid.getFrontGrid()[row][col];
            cell.setFill(new ImagePattern(new Image("file:Images/RoadImage.png")));
            grid.placeObjectByImage("RoadImage.png", row, col);
            grid.changeRoadDirection(row, col, Direction.UP);
            mainLayout.getChildren().remove(titleLabel);
            mainLayout.getChildren().remove(directionLabel);
            mainLayout.getChildren().remove(upButt);
            mainLayout.getChildren().remove(downButt);
            mainLayout.getChildren().remove(leftButt);
            mainLayout.getChildren().remove(rightButt);
            mainLayout.getChildren().remove(removeButton);
            mainLayout.getChildren().remove(closeButton);

            showRoadOptions(mainLayout, grid, row, col);
            refreshGrid(GRID_SIZE);
        });

       downButt.setOnAction(e -> {

           Rectangle cell = grid.getFrontGrid()[row][col];
           cell.setFill(new ImagePattern(new Image("file:Images/RoadImageDown.png")));
           grid.placeObjectByImage("RoadImageDown.png", row, col);
           grid.changeRoadDirection(row, col, Direction.DOWN);
           mainLayout.getChildren().remove(titleLabel);
           mainLayout.getChildren().remove(directionLabel);
           mainLayout.getChildren().remove(upButt);
           mainLayout.getChildren().remove(downButt);
           mainLayout.getChildren().remove(leftButt);
           mainLayout.getChildren().remove(rightButt);
           mainLayout.getChildren().remove(removeButton);
           mainLayout.getChildren().remove(closeButton);

           showRoadOptions(mainLayout, grid, row, col);
           refreshGrid(GRID_SIZE);
       });

       leftButt.setOnAction(e -> {

           Rectangle cell = grid.getFrontGrid()[row][col];
           cell.setFill(new ImagePattern(new Image("file:Images/RoadImageLeft.png")));
           grid.placeObjectByImage("RoadImageLeft.png", row, col);
           grid.changeRoadDirection(row, col, Direction.LEFT);
           mainLayout.getChildren().remove(titleLabel);
           mainLayout.getChildren().remove(directionLabel);
           mainLayout.getChildren().remove(upButt);
           mainLayout.getChildren().remove(downButt);
           mainLayout.getChildren().remove(leftButt);
           mainLayout.getChildren().remove(rightButt);
           mainLayout.getChildren().remove(removeButton);
           mainLayout.getChildren().remove(closeButton);

           showRoadOptions(mainLayout, grid, row, col);
           refreshGrid(GRID_SIZE);
       });

       rightButt.setOnAction(e -> {

           Rectangle cell = grid.getFrontGrid()[row][col];
           cell.setFill(new ImagePattern(new Image("file:Images/RoadImageRight.png")));
           grid.placeObjectByImage("RoadImageUP.png", row, col);
           grid.changeRoadDirection(row, col, Direction.RIGHT);
           mainLayout.getChildren().remove(titleLabel);
           mainLayout.getChildren().remove(directionLabel);
           mainLayout.getChildren().remove(upButt);
           mainLayout.getChildren().remove(downButt);
           mainLayout.getChildren().remove(leftButt);
           mainLayout.getChildren().remove(rightButt);
           mainLayout.getChildren().remove(removeButton);
           mainLayout.getChildren().remove(closeButton);

           showRoadOptions(mainLayout, grid, row, col);
           refreshGrid(GRID_SIZE);
       });

       removeButton.setOnAction(e -> {
           grid.remove(row, col);
           refreshGrid(GRID_SIZE);
           mainLayout.getChildren().remove(titleLabel);
           mainLayout.getChildren().remove(directionLabel);
           mainLayout.getChildren().remove(upButt);
           mainLayout.getChildren().remove(downButt);
           mainLayout.getChildren().remove(leftButt);
           mainLayout.getChildren().remove(rightButt);
           mainLayout.getChildren().remove(removeButton);
           mainLayout.getChildren().remove(closeButton);
       });

       closeButton.setOnAction(e -> {
           mainLayout.getChildren().remove(titleLabel);
           mainLayout.getChildren().remove(directionLabel);
           mainLayout.getChildren().remove(upButt);
           mainLayout.getChildren().remove(downButt);
           mainLayout.getChildren().remove(leftButt);
           mainLayout.getChildren().remove(rightButt);
           mainLayout.getChildren().remove(removeButton);
           mainLayout.getChildren().remove(closeButton);
       });


   }


    public static void main(String[] args) {
        launch(args);
    }
}
