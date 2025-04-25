package com.FlowLogic;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.FlowLogic.UserInterface.GRID_SIZE;

public class Simulation {
    int numVehicles;
    ArrayList<Vehicle> vehicles;
    ArrayList<Frame> frames;
    long avgTimeAtIntersections = 0;
    long avgTripTime = 360;
    long maxTimeAtIntersections = 0;
    long minTimeAtIntersections = 0;
    int numActiveVehicles = 0;
    int totalTime = 0;
    VBox left;

    public Simulation(int numVehicles) {
        this.numVehicles = numVehicles;//This and next line may need to be changed based
        vehicles = new ArrayList<Vehicle>();//on how users decide on vehicles in simulation
        frames = new ArrayList<Frame>();
        left = new VBox();
    }

    public void addFrame(Frame f) {
        frames.add(f);
    }

    public void disperse(Grid grid) {
        ArrayList<OneWayRoad> roads = new ArrayList<>();
        ArrayList<Parking> parking = new ArrayList<>();
        for (GridObject[] row : grid.getGrid()) {
            for (GridObject o : row) {
                if (o instanceof OneWayRoad) {
                    roads.add((OneWayRoad)o);
                }
                if (o instanceof  Parking) {
                    parking.add((Parking)o);
                }
            }
        }

        for (Vehicle v : vehicles) {
            Random random = new Random();
            //v.setStart(roads.get(random.nextInt(roads.size())));
            //v.setDestination(parking.get(random.nextInt(parking.size())));
        }
    }

    public void simulate() {
        //Code goes here to compute frames/steps
    }
    int SCREEN_WIDTH = 1280;
    int SCREEN_HEIGHT = 720;
    public void display(Stage stage, AnchorPane root, Pane gridGroup, Grid grid){
        VBox right = new VBox();
        right.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
        right.setPrefWidth((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2);
        right.setStyle("-fx-background-color: #D3D3D3;");

        AnchorPane.setRightAnchor(right, 0.0);
        AnchorPane.setTopAnchor(right, 0.0);     // Set top anchor
        AnchorPane.setBottomAnchor(right, 0.0);  // Set bottom anchor
        root.getChildren().add(right);

        Button back = new Button("Back");
        back.setPrefSize((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2, 30);
        right.getChildren().add(back);
        AtomicReference<Boolean> exit = new AtomicReference<>(false);

        back.setOnAction(e -> {
            // Confirm close with the user
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Confirm Simulation Exit");
            confirmAlert.setHeaderText("Are you sure you want to close the simulation?");

            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                // User confirmed the close, so return to build menu
                UserInterface.setupBuildMenu();
                exit.set(true);
            }
        });

        //VBox left = new VBox();
        left.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
        left.setPrefWidth((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2);
        left.setStyle("-fx-background-color: #D3D3D3;");

        AnchorPane.setLeftAnchor(left, 0.0);
        AnchorPane.setTopAnchor(left, 0.0);     // Set top anchor
        AnchorPane.setBottomAnchor(left, 0.0);  // Set bottom anchor
        root.getChildren().add(left);

        Button suggestion = new Button("Create Suggestions");
        suggestion.setPrefSize((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2, 30);
        left.getChildren().add(suggestion);
        suggestion.setOnAction(e -> {
            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
            infoAlert.setResizable(true);
            infoAlert.getDialogPane().setPrefSize(700, 500); // Make wider and taller
            infoAlert.setTitle("Suggestions Menu");
            String suggestionText = "";
            int numSuggestions = 0;
            if (avgTimeAtIntersections > 60) {
                numSuggestions++;
                suggestionText = suggestionText + numSuggestions + ". Cars appear to spend a long time at intersections. Consider expanding the road to allow more cars through!\n";
            }

            if (avgTripTime > 120) {
                numSuggestions++;
                suggestionText = suggestionText + numSuggestions + ". Cars appear to be taking quite some time to reach their destination from certain entry points. Consider creating a shorter path.\n";
            }

            if (suggestionText.isEmpty()) {
                suggestionText = "No suggestions to be made!";
            }
            infoAlert.setHeaderText("Number of Suggestions: " + numSuggestions);

            infoAlert.setContentText(suggestionText);
            infoAlert.showAndWait();
        });

        Button viewStatistics = new Button("View Statistics");
        viewStatistics.setPrefSize((SCREEN_WIDTH - SCREEN_HEIGHT * 1.0) / 2, 30);
        left.getChildren().add(viewStatistics);
        viewStatistics.setOnAction(e -> {
            left.getChildren().clear();

            Label statisticsTitle = new Label("Simulation Statistics:");
            statisticsTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

            Label avgTripTimeLabel = new Label("Average Trip Time: " + avgTripTime);
            avgTripTimeLabel.setStyle("-fx-font-size: 16px;");

            Label avgIntersectionWaitLabel = new Label("Average Intersection Wait Time: " + avgTimeAtIntersections);
            avgIntersectionWaitLabel.setStyle("-fx-font-size: 16px;");

            Label maxIntersectionWaitLabel = new Label("Max Intersection Wait Time: " + maxTimeAtIntersections);
            maxIntersectionWaitLabel.setStyle("-fx-font-size: 16px;");

            Label minIntersectionWaitLabel = new Label("Min Intersection Wait Time: " + minTimeAtIntersections);
            minIntersectionWaitLabel.setStyle("-fx-font-size: 16px;");

            //Label numActiveVehiclesLabel = new Label("Number of Active Vehicles: " + UserInterface.activeVehicles);
            //numActiveVehiclesLabel.setStyle("-fx-font-size: 16px;");

            left.getChildren().addAll(suggestion, viewStatistics, statisticsTitle, avgTripTimeLabel, avgIntersectionWaitLabel, maxIntersectionWaitLabel,
                    minIntersectionWaitLabel/*, numActiveVehiclesLabel*/);

        });


        AtomicReference<Double> delay = new AtomicReference<>(1.0);

        // Create the slider and label
        Slider delaySlider = new Slider(0.5, 2.0, delay.get());
        delaySlider.setShowTickLabels(true);
        delaySlider.setShowTickMarks(true);
        delaySlider.setMajorTickUnit(0.5);
        delaySlider.setBlockIncrement(0.1);

        Label delayLabel = new Label("Speed: " + delay.get());

        // Add listener to update the delay variable and label
        delaySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double snappedValue = Math.round(newVal.doubleValue() * 10) / 10.0;
            delaySlider.setValue(snappedValue); // Force slider to snap to nearest 0.1
            delay.set(snappedValue);
            delayLabel.setText(String.format("Speed: %.1f", snappedValue));
        });

        right.getChildren().addAll(delayLabel, delaySlider);

        new Thread(() -> {
            System.out.println(frames.size());
            for (Frame f : frames) {
                if (exit.get() == true) {
                    break;
                }
                Platform.runLater(() -> { // Ensures UI updates happen on JavaFX thread
                    System.out.println("Rendering frame");
                    //updateStatisticsLabels(left);
                    for (Step s : f.getSteps()) {
                        Object oldObj = s.oldObject;
                        Object newObj = s.newObject;

                        if (oldObj instanceof  Vehicle) {
                            Vehicle car = (Vehicle) oldObj;
                            if (gridGroup.getChildren().contains(car.getCar())) {
                                gridGroup.getChildren().remove(car.getCar());
                            }
                        }

                        if (newObj instanceof Vehicle) {
                            Vehicle car = (Vehicle) newObj;
                            int x = car.getX();
                            int y = car.getY();
                            double cell_size = (720 * 1.0)/GRID_SIZE;
                            x = (int) (((x * 1.0) /32) * cell_size);
                            y = (int) (((y * 1.0) /32) * cell_size);
                            Rectangle update = car.getCar();
                            update.setX(x);
                            update.setY(y);
                            //update.setFill(car.getCar().getFill());
                            Image image;
                            if (car.getLength() <= 15) {
                                image = new Image("file:Images/Prius.png");
                            } else if (car.getLength() <= 20) {
                                image = new Image("file:Images/BlueCar.png");
                            } else if (car.getLength() <= 25) {
                                image = new Image("file:Images/BusTaxi.png");
                            } else {
                                image = new Image("file:Images/Semi.png");
                            }
                            update.setFill(new ImagePattern(image));
                            //update.setStroke(Color.BLACK);
                            //update.setStrokeWidth(2);
                            update.setWidth(((car.getWidth() * 1.0) /32) * cell_size / 2);
                            update.setHeight(((car.getLength() * 1.0) /32) * cell_size / 2);
                            //update.setFill(Color.BLUE);
                            //update.setStroke(Color.BLACK);
                            //update.setStrokeWidth(2);
                            update.setRotate(car.getCurRotation());
                            if (!gridGroup.getChildren().contains(car.getCar())) {
                                gridGroup.getChildren().add(car.getCar());
                            }
                        }
                        if (newObj instanceof StopLight) {
                            StopLight light = (StopLight) newObj;
                            Rectangle rect = grid.getFrontGrid()[light.getRowNum()][light.getColNum()];
                            //double cell_size = (720.0)/GRID_SIZE;
                            int x = light.getColNum() * Grid.GRID_SIZE;
                            int y = light.getRowNum() * Grid.GRID_SIZE;

                            x = (int) (((x * 1.0))); ///32)) * cell_size);
                            y = (int) (((y * 1.0))); ///32)) * cell_size);

                            rect.setX(x);
                            rect.setY(y);


                            if (light.getLightOneColor() != 0) rect.setRotate(90);
                            else rect.setRotate(0);
                            rect.setFill(new ImagePattern(light.getImageFile()));
                        }
                    }
                });

                try {
                    Thread.sleep((long)(500 / delay.get())); // Simulate delay, but UI won't freeze
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);

    }

    /**
     * Updates the statistics labels in the left VBox panel with current simulation data allowing
     * statistics to be updated every frame
     * @param left The VBox containing the statistics labels
     */
    public void updateStatisticsLabels(VBox left) {
        // Get current children
        ArrayList<Node> nodesToKeep = new ArrayList<>();

        // Keep first two elements (suggestions button and title)
        for (int i = 0; i < 2; i++) {
            if (i < left.getChildren().size()) {
                nodesToKeep.add(left.getChildren().get(i));
            }
        }

        // Clear all existing children
        left.getChildren().clear();

        // Add back the title/label we saved
        left.getChildren().addAll(nodesToKeep);

        Label avgTripTimeLabel = new Label("Average Trip Time: " + avgTripTime);
        avgTripTimeLabel.setStyle("-fx-font-size: 16px;");

        Label avgIntersectionWaitLabel = new Label("Average Intersection Wait Time: " + avgTimeAtIntersections);
        avgIntersectionWaitLabel.setStyle("-fx-font-size: 16px;");

        Label maxIntersectionWaitLabel = new Label("Max Intersection Wait Time: " + maxTimeAtIntersections);
        maxIntersectionWaitLabel.setStyle("-fx-font-size: 16px;");

        Label minIntersectionWaitLabel = new Label("Min Intersection Wait Time: " + minTimeAtIntersections);
        minIntersectionWaitLabel.setStyle("-fx-font-size: 16px;");

        //Label numActiveVehiclesLabel = new Label("Active Vehicles: " + UserInterface.activeVehicles);
       // numActiveVehiclesLabel.setStyle("-fx-font-size: 16px;");

        // Add all the statistics labels to the VBox
        left.getChildren().addAll(avgTripTimeLabel, avgIntersectionWaitLabel,
                maxIntersectionWaitLabel, minIntersectionWaitLabel/*, numActiveVehiclesLabel*/);
    }



    public void setAvgTimeAtIntersections(long avgTimeAtIntersections) {
        this.avgTimeAtIntersections = avgTimeAtIntersections;
    }

    public void setAvgTripTime(long avgTripTime) {
        this.avgTripTime = avgTripTime;
    }

    public void setMaxTimeAtIntersections(long maxTimeAtIntersections) {
        this.maxTimeAtIntersections = maxTimeAtIntersections;
    }

    public void setMinTimeAtIntersections(long minTimeAtIntersections) {
        this.minTimeAtIntersections = minTimeAtIntersections;
    }

    public void setNumActiveVehicles(int numActiveVehicles) {
        this.numActiveVehicles = numActiveVehicles;
    }

    public VBox getLeft() {
        return left;
    }
}
