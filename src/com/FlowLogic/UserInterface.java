package com.FlowLogic;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
import java.util.*;

import static com.FlowLogic.Direction.UP;


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
    private static Scene lastScene;
    private static CheckBox selectEntireRoadCheckbox;

    // Save Directory Path
    private static final String SAVE_DIRECTORY = "saves";
    private static VBox options = new VBox();

    @Override
    public void start(Stage primaryStage) throws Error {
        mainMenu(primaryStage);
    }
    public static void mainMenu(Stage primaryStage) {
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
        lastScene = scene;
    }

    public static void setupBuildMenu(){
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
            // check to see if it is a hazard
            if (grid.isHazard(db.getString())) {
                Image image = new Image("file:Images/Hazard.png");
                GridObject obj = grid.getAtSpot(row, col);
                System.out.println("Should be Road: "+ obj.toString());
                grid.remove(row, col);
                grid.placeObjectByImage("Hazard.png", row, col);
                cell.setFill(new ImagePattern(image));
                Hazard hazard = (Hazard) grid.getAtSpot(row, col);

                hazard.setCoveredObject(obj);
            }
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
        Button menu = new Button("Menu");
        menu.setPrefSize((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2, 30);
        menu.setOnAction(e -> {
            mainMenu(stage);
        });
        right.getChildren().add(menu);

        Button simulate = new Button("Simulate");
        simulate.setPrefSize((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2, 30);
        simulate.setOnAction(e -> {
            //Add prompt for vehicle selection here
            //TODO: ISAAC - add average car size prompt here (do manual and auto)
            //TODO: ISAAC / COLIN - add num vehicles prompt
            TrafficController tc = new TrafficController(5,5, grid); // TODO: ISAAC / COLIN update params based on prompts
            Simulation sim = tc.runSimulation();
            root.getChildren().remove(right);
            root.getChildren().remove(left);
            sim.display(stage, root); // display the simulation
        });
        right.getChildren().add(simulate);

        // Add a checkbox under the menu button
        selectEntireRoadCheckbox = new CheckBox("Toggle Select Entire Road");
        selectEntireRoadCheckbox.setPrefWidth((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2);
        selectEntireRoadCheckbox.setPadding(new Insets(10, 0, 0, 10)); // Add some padding
        selectEntireRoadCheckbox.setOnAction(event -> {
            boolean isSelected = selectEntireRoadCheckbox.isSelected();
            System.out.println("Select Entire Road is now: " + (isSelected ? "ON" : "OFF"));
            // Additional functionality can be added here later
        });
        right.getChildren().add(selectEntireRoadCheckbox);


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


        right.getChildren().add(options);


        // Set up a Scene
        Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);

        stage.setScene(scene);
        stage.show();
        lastScene = scene;
    }

    /**
     * Helper function that returns the status of the "Select Entire Road" checkbox
     * @return boolean indicating true if the checkbox is checked, false otherwise
     */
    public static boolean isEntireRoadSelectionEnabled() {
        // Return the current state of the checkbox
        return selectEntireRoadCheckbox != null && selectEntireRoadCheckbox.isSelected();
    }

    public static void setupLoadMenu() {
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
            backButton.setOnAction(e -> {
                if (lastScene != null) {
                    stage.setScene(lastScene);
                }
                else {
                    mainMenu(stage);
                }
            });

            root.getChildren().addAll(titleLabel, noFilesLabel, browseButton, backButton);
        } else {
            // Save files found

            // Create a map to store filename to File object mapping for easy access to renaming/deleting files
            Map<String, File> fileMap = new HashMap<>();

            // Convert file array to observable list for ListView
            ObservableList<String> fileNames = FXCollections.observableArrayList();
            for (File file : saveFiles) {
                fileNames.add(file.getName());
                fileMap.put(file.getName(), file);
            }

            // Create ListView to show save files
            ListView<String> saveFileListView = new ListView<>(fileNames);
            saveFileListView.setPrefHeight(400);
            saveFileListView.setPrefWidth(400);

            HBox buttonBox = new HBox(20);
            buttonBox.setAlignment(Pos.CENTER);

            Button loadButton = new Button("Load Selected");
            Button renameButton = new Button("Rename Selected");
            Button deleteButton = new Button("Delete Selected");
            Button cancelButton = new Button("Cancel");

            // Initially disable the buttons until a selection is made
            loadButton.setDisable(true);
            renameButton.setDisable(true);
            deleteButton.setDisable(true);

            // Add a listener to the ListView's selection model
            // This will make sure the buttons only show up when a file is selected
            saveFileListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                // Enable/disable buttons based on whether an item is selected
                boolean hasSelection = (newValue != null);
                loadButton.setDisable(!hasSelection);
                renameButton.setDisable(!hasSelection);
                deleteButton.setDisable(!hasSelection);
            });

            // Set the action of the Load Button
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

            // Set the action of the rename button
            renameButton.setOnAction((e -> {
                String selectedFileName = saveFileListView.getSelectionModel().getSelectedItem();
                if (selectedFileName != null) {
                    File selectedFile = fileMap.get(selectedFileName);
                    renameSaveFile(selectedFile);
                } else {
                    showErrorAlert("Please select a save file to rename");
                }
            }));

            // Set the action of the delete button
            deleteButton.setOnAction((e -> {
                String selectedFileName = saveFileListView.getSelectionModel().getSelectedItem();
                if (selectedFileName != null) {
                    File selectedFile = fileMap.get(selectedFileName);
                    deleteSaveFile(selectedFile);
                } else {
                    showErrorAlert("Please select a save file to delete");
                }
            }));


            cancelButton.setOnAction(e -> {
                if (lastScene != null) {
                    stage.setScene(lastScene);
                }
                else {
                    mainMenu(stage);
                }
            });

            buttonBox.getChildren().addAll(loadButton, renameButton, deleteButton, browseButton, cancelButton);
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
    public static void loadGridButton(VBox mainLayout, Grid grid) {
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
    private static void browseForExternalSaveFile() {
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
    private static void copyFile(File sourceFile, File destinationFile) throws IOException {
        Files.copy(sourceFile.toPath(), destinationFile.toPath(),
                java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        showInfoAlert("File Copied", "File '" + sourceFile.getName() +
                "' has been copied to the save directory");
    }

    /**
     * Asks the user for a new filename and copies the file with that name
     */
    private static void renameAndCopyFile(File sourceFile) {
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
     * This function will allow a user to rename a save file
     *
     * @param sourceFile The file that will be renamed
     */
    private static void renameSaveFile(File sourceFile) {
        // First check to ensure the file exists
        if (!sourceFile.exists()) {
            showErrorAlert("File does not exist: " + sourceFile.getAbsolutePath());
        }

        // Show the dialog to get the new name
        TextInputDialog dialog = new TextInputDialog(sourceFile.getName().replace(".json", ""));
        dialog.setTitle("Rename File");
        dialog.setHeaderText("Please enter a new name for the file");
        dialog.setContentText("New filename (without extension):");

        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isEmpty()) {
                try {
                    // Create the new file path
                    String newFilePath = SAVE_DIRECTORY + File.separator + newName + ".json";
                    File newFile = new File(newFilePath);

                    // Check if a file with the new name already exists
                    if (newFile.exists()) {
                        // Check with user to ensure they are ok with overwriting it
                        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                        confirmAlert.setTitle("File Already Exists");
                        confirmAlert.setHeaderText("A file with the name '" + newName + ".json' already exists.");
                        confirmAlert.setContentText("Do you want to overwrite it?");

                        if (confirmAlert.showAndWait().get() != ButtonType.OK) {
                            // User cancelled overwrite
                            return;
                        }
                    }

                    // Rename the file
                    boolean success = sourceFile.renameTo(newFile);

                    if (success) {
                        showInfoAlert("File Renamed", "File successfully renamed to " + newName + ".json");
                        // Refresh the load menu to show the updated file name
                        setupLoadMenu();
                    }
                    else {
                        showErrorAlert("Failed to rename file. Please try again.");
                    }
                } catch (Exception e) {
                    showErrorAlert("Error renaming file: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * This function will allow a user to delete a save file
     *
     * @param file The file that will be deleted
     */
    private static void deleteSaveFile(File file) {
        if (!file.exists()) {
            showErrorAlert("File does not exist: " + file.getAbsolutePath());
            return;
        }

        // Confirm deletion with the user
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Are you sure you want to delete '" + file.getName() + "'?");
        confirmAlert.setContentText("This action cannot be undone.");

        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            // User confirmed the deletion, delete the file
            boolean success = file.delete();

            if (success) {
                showInfoAlert("File Deleted", "File '" + file.getName() + "' was successfully deleted.");
                // Refresh the load menu to show the updated file list
                setupLoadMenu();
            } else {
                showErrorAlert("Failed to delete file. Please try again.");
            }
        }
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
        options.getChildren().clear();
        // Get the building object
        GridObject obj = grid.getAtSpot(row, col);
        String name = ((Building)obj).getName();

        Label titleLabel = new Label(name + " Options");
        Label xLabel = new Label("xLength:");
        TextField xLengthField = new TextField();
        Label yLabel = new Label("yLength:");
        TextField yLengthField = new TextField();
        Label populationLabel = new Label("dailyPopulation:");
        TextField populationField = new TextField();
        Button renameButton = new Button("Rename Building");
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

        options.getChildren().add(titleLabel);
        options.getChildren().add(xLabel);
        options.getChildren().add(xLengthField);
        options.getChildren().add(yLabel);
        options.getChildren().add(yLengthField);
        options.getChildren().add(populationLabel);
        options.getChildren().add(populationField);
        options.getChildren().add(submitButton);
        options.getChildren().add(renameButton);
        options.getChildren().add(removeButton);
        options.getChildren().add(closeButton);

        renameButton.setOnAction(e-> {
            openBuildingRenameDialog((Building)obj);
            options.getChildren().clear();
            showBuildingOptions(mainLayout, grid, xLen, yLen, dailyPop, row, col);
            refreshGrid(GRID_SIZE);
        });

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
                options.getChildren().clear();
                showBuildingOptions(mainLayout, grid, xLenNew, yLenNew, popNew, row, col);
                refreshGrid(GRID_SIZE);
            }
        });

        removeButton.setOnAction(e -> {
            grid.remove(row, col);
            refreshGrid(GRID_SIZE);
            options.getChildren().clear();
        });

        closeButton.setOnAction(e -> {
            options.getChildren().clear();
        });


    }

    public static void showParkingOptions(VBox mainLayout, Grid grid, int xLen, int yLen, int dailyPop, int row,
                                           int col) {
        options.getChildren().clear();
        System.out.println("Old Parking Capacity: " + dailyPop);
        Label titleLabel = new Label("Parking Options");
        Label xLabel = new Label("xLength:");
        TextField xLengthField = new TextField();
        Label yLabel = new Label("yLength:");
        TextField yLengthField = new TextField();
        Label parkingLabel = new Label("Parking Capacity:");
        TextField parkingField = new TextField();
        Button submitButton = new Button("Submit Changes");
        Button removeButton = new Button("Remove Parking");
        Button closeButton = new Button("Close Parking Options");


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

        options.getChildren().add(titleLabel);
        options.getChildren().add(xLabel);
        options.getChildren().add(xLengthField);
        options.getChildren().add(yLabel);
        options.getChildren().add(yLengthField);
        options.getChildren().add(parkingLabel);
        options.getChildren().add(parkingField);
        options.getChildren().add(submitButton);
        options.getChildren().add(removeButton);
        options.getChildren().add(closeButton);

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
                options.getChildren().clear();


                showParkingOptions(mainLayout, grid, xLenNew, yLenNew, popNew, row, col);
                refreshGrid(GRID_SIZE);
            }
        });

        removeButton.setOnAction(e -> {
            grid.remove(row, col);
            refreshGrid(GRID_SIZE);
            options.getChildren().clear();
        });

        closeButton.setOnAction(e -> {
            options.getChildren().clear();
        });
    }

    /**
     * Opens a dialog allowing the user to rename a building
     *
     * @param building The building object to rename
     * @return true if the rename was successful, false otherwise
     */
    private static boolean openBuildingRenameDialog(Building building) {
        if (building == null) {
            showErrorAlert("No building selected to rename");
            return false;
        }

        // Create a dialog to get the new road name
        TextInputDialog dialog = new TextInputDialog(building.getName());
        dialog.setTitle("Rename Building");
        dialog.setHeaderText("Enter a new name for this building");
        dialog.setContentText("Building name:");

        // Show the dialog and wait for user input
        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isEmpty()) {
                building.setName(newName);
                System.out.println("Building renamed to: " + newName);
            }
        });

        return true;
    }

    /**
     * Opens a dialog allowing the user to rename a road
     *
     * @param road The Road object to rename
     * @return true if the rename was successful, false otherwise
     */
    private static boolean openRoadRenameDialog(Road road) {
        if (road == null) {
            showErrorAlert("No road selected to rename");
            return false;
        }

        // Create a dialog to get the new road name
        TextInputDialog dialog = new TextInputDialog(road.getName());
        dialog.setTitle("Rename Road");
        dialog.setHeaderText("Enter a new name for this road");
        dialog.setContentText("Road name:");

        // Show the dialog and wait for user input
        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isEmpty()) {
                road.setName(newName);
                System.out.println("Road renamed to: " + newName);
            }
        });

        return true;
    }

    /**
     * Shows options for road renaming for a group of connected road tiles
     *
     * @param mainLayout The VBox layout to add UI elements to
     * @param grid The Grid containing the road objects
     * @param connectedRoads Set of road coordinates to be renamed
     * @return true if successful, false otherwise
     */
    public static boolean renameConnectedRoads(VBox mainLayout, Grid grid, Set<int[]> connectedRoads) {
        if (connectedRoads == null || connectedRoads.isEmpty()) {
            showErrorAlert("No roads selected to rename");
            return false;
        }

        // Get the first road to determine the current name
        int[] firstCoord = connectedRoads.iterator().next();
        Road firstRoad = (Road) grid.getAtSpot(firstCoord[0], firstCoord[1]);

        if (firstRoad == null) {
            showErrorAlert("Selected tile is not a road");
            return false;
        }

        String currentName = firstRoad.getName();

        // Create a dialog to get the new road name
        TextInputDialog dialog = new TextInputDialog(currentName);
        dialog.setTitle("Rename Road");
        dialog.setHeaderText("Enter a new name for " + (connectedRoads.size() > 1 ?
                "these " + connectedRoads.size() + " road tiles" :
                "this road tile"));
        dialog.setContentText("Road name:");

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.isEmpty()) {
                // Apply the new name to all connected road tiles
                for (int[] coord : connectedRoads) {
                    GridObject obj = grid.getAtSpot(coord[0], coord[1]);
                    if (obj instanceof Road) {
                        ((Road) obj).setName(newName);
                    }
                }

                System.out.println("Renamed " + connectedRoads.size() +
                        " road tiles to: " + newName);
            }
        });

        return true;
    }


    /**
     * Function for showing road objects when a single road is selected
     *
     * @param mainLayout - Layout pane you are working with
     * @param grid - current grid object
     * @param row - row of the selected object
     * @param col - column of the selected object
     */
    public static void showRoadOptions(VBox mainLayout, Grid grid, int row, int col) {
        options.getChildren().clear();
        // Get the road object
        GridObject obj = grid.getAtSpot(row, col);
        String name = ((Road)obj).getName();
        OneWayRoad oneRoad = (OneWayRoad)obj;
        Image image = oneRoad.getImageFile();
        String multiLane = ("MultiLaneConnector: " + ((Road) obj).getLaneContainer().getCount());

        Label titleLabel = new Label(name + " Options");
        Button renameButt = new Button("Rename Road");
        Button upButt = new Button("Change Direction Up");
        Button downButt = new Button("Change Direction Down");
        Button leftButt = new Button("Change Direction Left");
        Button rightButt = new Button("Change Direction Right");
        Button removeButton = new Button("Remove Road");
        Button closeButton = new Button("Close Road Options");
        CheckBox inRoad = new CheckBox("Make Input Road");
        Label multiLabel = new Label(multiLane);
        Button addLaneRight = new Button("Add Lane to the Right");
        Button addLaneLeft = new Button("Add Lane to the Left");

        options.getChildren().add(titleLabel);
        options.getChildren().add(renameButt);
        options.getChildren().add(upButt);
        options.getChildren().add(downButt);
        options.getChildren().add(leftButt);
        options.getChildren().add(rightButt);
        options.getChildren().add(removeButton);
        options.getChildren().add(closeButton);
        options.getChildren().add(multiLabel);
        options.getChildren().add(addLaneLeft);
        options.getChildren().add(addLaneRight);

        OneWayRoad road = (OneWayRoad) grid.getGrid()[row][col];
        if ((row == 0 && road.getDirection() == Direction.DOWN) ||
                (row == grid.getNumRows() - 1 && road.getDirection() == Direction.UP) ||
                (col == 0 && road.getDirection() == Direction.RIGHT) ||
                (col == grid.getNumColumns() - 1 && road.getDirection() == Direction.LEFT))
        {
            if (((OneWayRoad) grid.getGrid()[row][col]).getInRoad()) {
                inRoad.setSelected(true);
            }
            options.getChildren().add(inRoad);
        }

        addLaneRight.setOnAction(e -> {
            Direction oneDir = oneRoad.getDirection();
            if (oneDir == UP) {
                Rectangle cell = grid.getFrontGrid()[row][col + 1];
                if (!(cell.getFill() instanceof ImagePattern)) {
                    cell.setFill(new ImagePattern(image));
                    grid.placeObjectByImage("RoadImage.png", row, col + 1);
                    System.out.println(image.getUrl());
                }
            } else if (oneDir == Direction.DOWN) {
                Rectangle cell = grid.getFrontGrid()[row][col - 1];
                if (!(cell.getFill() instanceof ImagePattern)) {
                    cell.setFill(new ImagePattern(image));
                    grid.placeObjectByImage("RoadImageDown.png", row, col - 1);
                    System.out.println(image);
                }
            } else if (oneDir == Direction.RIGHT) {
                Rectangle cell = grid.getFrontGrid()[row + 1][col];
                if (!(cell.getFill() instanceof ImagePattern)) {
                    cell.setFill(new ImagePattern(image));
                    grid.placeObjectByImage("RoadImageRight.png", row + 1, col);
                    System.out.println(image);
                }
            } else if (oneDir == Direction.LEFT) {
                Rectangle cell = grid.getFrontGrid()[row - 1][col];
                if (!(cell.getFill() instanceof ImagePattern)) {
                    cell.setFill(new ImagePattern(image));
                    grid.placeObjectByImage("RoadImageLeft.png", row - 1, col);
                    System.out.println(image);
                }
            }
        });

        addLaneLeft.setOnAction(e -> {
            Direction oneDir = oneRoad.getDirection();
            if (oneDir == UP) {
                Rectangle cell = grid.getFrontGrid()[row][col - 1];
                if (!(cell.getFill() instanceof ImagePattern)) {
                    cell.setFill(new ImagePattern(image));
                    grid.placeObjectByImage("RoadImage.png", row, col - 1);
                    System.out.println(image);
                }
            } else if (oneDir == Direction.DOWN) {
                Rectangle cell = grid.getFrontGrid()[row][col + 1];
                if (!(cell.getFill() instanceof ImagePattern)) {
                    cell.setFill(new ImagePattern(image));
                    grid.placeObjectByImage("RoadImageDown.png", row, col + 1);
                    System.out.println(image);
                }
            } else if (oneDir == Direction.RIGHT) {
                Rectangle cell = grid.getFrontGrid()[row - 1][col];
                if (!(cell.getFill() instanceof ImagePattern)) {
                    cell.setFill(new ImagePattern(image));
                    grid.placeObjectByImage("RoadImageRight.png", row - 1, col);
                    System.out.println(image);
                }
            } else if (oneDir == Direction.LEFT) {
                Rectangle cell = grid.getFrontGrid()[row + 1][col];
                if (!(cell.getFill() instanceof ImagePattern)) {
                    cell.setFill(new ImagePattern(image));
                    grid.placeObjectByImage("RoadImageLeft.png", row + 1, col);
                    System.out.println(image);
                }
            }
        });

        upButt.setOnAction(e -> {

            Rectangle cell = grid.getFrontGrid()[row][col];
            cell.setFill(new ImagePattern(new Image("file:Images/RoadImage.png")));
            grid.placeObjectByImage("RoadImage.png", row, col);
            grid.changeRoadDirection(row, col, Direction.UP);
            options.getChildren().clear();


            showRoadOptions(mainLayout, grid, row, col);
            refreshGrid(GRID_SIZE);
        });

        downButt.setOnAction(e -> {

            Rectangle cell = grid.getFrontGrid()[row][col];
            cell.setFill(new ImagePattern(new Image("file:Images/RoadImageDown.png")));
            grid.placeObjectByImage("RoadImageDown.png", row, col);
            grid.changeRoadDirection(row, col, Direction.DOWN);
            options.getChildren().clear();

            showRoadOptions(mainLayout, grid, row, col);
            refreshGrid(GRID_SIZE);
        });

        leftButt.setOnAction(e -> {

            Rectangle cell = grid.getFrontGrid()[row][col];
            cell.setFill(new ImagePattern(new Image("file:Images/RoadImageLeft.png")));
            grid.placeObjectByImage("RoadImageLeft.png", row, col);
            grid.changeRoadDirection(row, col, Direction.LEFT);
            options.getChildren().clear();


            showRoadOptions(mainLayout, grid, row, col);
            refreshGrid(GRID_SIZE);
        });

        rightButt.setOnAction(e -> {

            Rectangle cell = grid.getFrontGrid()[row][col];
            cell.setFill(new ImagePattern(new Image("file:Images/RoadImageRight.png")));
            grid.placeObjectByImage("RoadImageRight.png", row, col);
            grid.changeRoadDirection(row, col, Direction.RIGHT);
            options.getChildren().clear();

            showRoadOptions(mainLayout, grid, row, col);
            refreshGrid(GRID_SIZE);
        });

        removeButton.setOnAction(e -> {
            grid.remove(row, col);
            refreshGrid(GRID_SIZE);
            options.getChildren().clear();

        });

        renameButt.setOnAction(e -> {
            openRoadRenameDialog((Road)obj);
            options.getChildren().clear();


            showRoadOptions(mainLayout, grid, row, col);
            refreshGrid(GRID_SIZE);
        });

        closeButton.setOnAction(e -> {
            options.getChildren().clear();
        });

        inRoad.setOnAction(event -> {
            if (inRoad.isSelected()) {
                road.setInRoad(true);
            } else {
                road.setInRoad(false);
            }
        });
    }



    public static void showHazardOptions(VBox mainLayout, Grid grid, int row, int col) {
        options.getChildren().clear();
        // Get the road object
        GridObject obj = grid.getAtSpot(row, col);
        Hazard hazard= (Hazard)obj;
        Image image = hazard.getCoveredObject().getImageFile();
        System.out.println("Covered image: " + image.getUrl());

        Label titleLabel = new Label("Hazard Options");
        Button fixRoad = new Button("Fix Road");
        Button closeButton = new Button("Close Hazard Options");


        options.getChildren().add(titleLabel);
        options.getChildren().add(fixRoad);
        options.getChildren().add(closeButton);

        fixRoad.setOnAction(e -> {
            grid.synchronizeGrid();
            Rectangle cell = grid.getFrontGrid()[row][col];
            System.out.println(grid.getFrontGrid()[row][col].toString());
            cell.setFill(new ImagePattern(image));
            grid.remove(row, col);
            grid.addObject(hazard.getCoveredObject(), row, col);
            grid.mergeRoads(row, col);
        });

        closeButton.setOnAction(e -> {
           options.getChildren().clear();
        });

    }

    /**
     * Function for showing road objects when multiple road tiles are selected
     *
     * @param mainLayout - Layout pane you are working with
     * @param grid - current grid object
     * @param row - row of the selected object
     * @param col - column of the selected object
     * @param connectedRoads - set of the selected road objects
     */
    public static void showRoadOptions(VBox mainLayout, Grid grid, int row, int col, Set<int[]> connectedRoads) {
        // Get the first road to determine the name
        String roadName = "Road";
        if (!connectedRoads.isEmpty()) {
            int[] firstCoord = connectedRoads.iterator().next();
            GridObject obj = grid.getAtSpot(firstCoord[0], firstCoord[1]);
            if (obj instanceof Road) {
                roadName = ((Road) obj).getName();
            }
        }

        final Label[] titleLabel = {new Label(roadName + " Options")};
        Button flipDirectionButton = new Button("Flip Road Direction");
        Button renameRoadButton = new Button("Rename Road");
        Button removeButton = new Button("Remove Road");
        Button closeButton = new Button("Close Road Options");

        // Label showing number of selected road tiles
        Label selectedTilesLabel;
        if (connectedRoads.size() > 1) {
            selectedTilesLabel = new Label("Selected Road Tiles: " + connectedRoads.size());
            mainLayout.getChildren().add(selectedTilesLabel);
        } else {
            selectedTilesLabel = null;
        }

        mainLayout.getChildren().add(titleLabel[0]);
        mainLayout.getChildren().add(flipDirectionButton);
        mainLayout.getChildren().add(renameRoadButton);
        mainLayout.getChildren().add(removeButton);
        mainLayout.getChildren().add(closeButton);

        // Helper function to clear options menu
        Runnable clearMenu = () -> {
            mainLayout.getChildren().remove(titleLabel[0]);
            if (selectedTilesLabel != null) {
                mainLayout.getChildren().remove(selectedTilesLabel);
            }
            mainLayout.getChildren().remove(flipDirectionButton);
            mainLayout.getChildren().remove(renameRoadButton);
            mainLayout.getChildren().remove(removeButton);
            mainLayout.getChildren().remove(closeButton);
        };

        flipDirectionButton.setOnAction(e -> {
            // For each connected road tile, flip its direction
            for (int[] coord : connectedRoads) {
                int r = coord[0];
                int c = coord[1];
                GridObject obj = grid.getAtSpot(r, c);

                if (obj instanceof Road) {
                    Direction currentDirection = null;
                    Direction newDirection = null;

                    // Get current direction
                    if (obj instanceof OneWayRoad) {
                        currentDirection = ((OneWayRoad) obj).getDirection();

                        // Determine the opposite direction
                        switch (currentDirection) {
                            case UP:
                                newDirection = Direction.DOWN;
                                grid.getFrontGrid()[r][c].setFill(new ImagePattern(new Image("file:Images/RoadImageDown.png")));
                                break;
                            case DOWN:
                                newDirection = Direction.UP;
                                grid.getFrontGrid()[r][c].setFill(new ImagePattern(new Image("file:Images/RoadImage.png")));
                                break;
                            case LEFT:
                                newDirection = Direction.RIGHT;
                                grid.getFrontGrid()[r][c].setFill(new ImagePattern(new Image("file:Images/RoadImageRight.png")));
                                break;
                            case RIGHT:
                                newDirection = Direction.LEFT;
                                grid.getFrontGrid()[r][c].setFill(new ImagePattern(new Image("file:Images/RoadImageLeft.png")));
                                break;
                        }

                        // Apply the new direction
                        if (newDirection != null) {
                            grid.changeRoadDirection(r, c, newDirection);
                        }
                    }
                    // TODO: Implement Road Flip for Two Way Roads
                }
            }

            clearMenu.run();
            showRoadOptions(mainLayout, grid, row, col, grid.getConnectedRoadTiles(row, col));
            refreshGrid(GRID_SIZE);
        });

        renameRoadButton.setOnAction(e -> {
            // Use the new renameConnectedRoads function
            boolean renameSuccessful = renameConnectedRoads(mainLayout, grid, connectedRoads);

            if (renameSuccessful) {
                // Update the title label with the new name (get from first road)
                if (!connectedRoads.isEmpty()) {
                    int[] firstCoord = connectedRoads.iterator().next();
                    GridObject obj = grid.getAtSpot(firstCoord[0], firstCoord[1]);
                    if (obj instanceof Road) {
                        String newName = ((Road) obj).getName();

                        // Replace the label with updated title
                        mainLayout.getChildren().remove(titleLabel[0]);
                        titleLabel[0] = new Label(newName + " Options");
                        mainLayout.getChildren().add(5, titleLabel[0]);
                    }
                }
            }

            clearMenu.run();
            showRoadOptions(mainLayout, grid, row, col, grid.getConnectedRoadTiles(row, col));
            refreshGrid(GRID_SIZE);
        });

        removeButton.setOnAction(e -> {
            // Remove all connected road tiles
            for (int[] coord : connectedRoads) {
                grid.remove(coord[0], coord[1]);
            }
            refreshGrid(GRID_SIZE);
            clearMenu.run();
        });

     closeButton.setOnAction(e -> {
            clearMenu.run();
        });
    }

    public static void showTrafficLightOptions(VBox mainLayout, Grid grid, int row, int col) {
        options.getChildren().clear();
        Label titleLabel= new Label("Traffic Light Options");
        Label verticalLabel = new Label ("Vertical Timing:");
        TextField verticalField = new TextField();
        Label horizontalLabel = new Label ("Horizontal Timing:");
        TextField horizontalField = new TextField();
        Button submitButton = new Button("Submit Changes");
        Button removeButton = new Button("Remove Intersection");
        Button closeButton = new Button("Close Traffic Light Options");

        TextFormatter<String> numberFormatterVert = new TextFormatter<>(change -> {
            if (change.getText().matches("[0-9]*")) {
                return change;  // Accept change
            }
            return null;  // Reject change
        });

        TextFormatter<String> numberFormatterHor = new TextFormatter<>(change -> {
            if (change.getText().matches("[0-9]*")) {
                return change;  // Accept change
            }
            return null;  // Reject change
        });

        verticalField.setTextFormatter(numberFormatterVert);
        horizontalField.setTextFormatter(numberFormatterHor);

        StopLight light = (StopLight) grid.getAtSpot(row, col);

        verticalField.setText(Integer.toString(light.getTimingOne()));
        horizontalField.setText(Integer.toString(light.getTimingTwo()));


        options.getChildren().add(titleLabel);
        options.getChildren().add(verticalLabel);
        options.getChildren().add(verticalField);
        options.getChildren().add(horizontalLabel);
        options.getChildren().add(horizontalField);
        options.getChildren().add(submitButton);
        options.getChildren().add(removeButton);
        options.getChildren().add(closeButton);

        submitButton.setOnAction(e -> {
            int vert = Integer.parseInt(verticalField.getText());
            int hor = Integer.parseInt(verticalField.getText());
            grid.updateTiming(light, vert, hor);
            options.getChildren().clear();
            showTrafficLightOptions(mainLayout, grid, row, col);
        });

        removeButton.setOnAction(e -> {
            grid.remove(row, col);
            refreshGrid(GRID_SIZE);
            options.getChildren().clear();
        });

        closeButton.setOnAction(e -> {
            options.getChildren().clear();
        });


    }


    public static void main(String[] args) {
        launch(args);
    }
}
