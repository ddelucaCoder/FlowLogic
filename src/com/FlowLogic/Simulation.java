package com.FlowLogic;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.stage.Stage;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static com.FlowLogic.UserInterface.GRID_SIZE;

public class Simulation {
    int numVehicles;
    ArrayList<Vehicle> vehicles;
    ArrayList<Frame> frames;

    public Simulation(int numVehicles) {
        this.numVehicles = numVehicles;//This and next line may need to be changed based
        vehicles = new ArrayList<Vehicle>();//on how users decide on vehicles in simulation
        frames = new ArrayList<Frame>();
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
            confirmAlert.setContentText("You will have to remake it.");

            if (confirmAlert.showAndWait().get() == ButtonType.OK) {
                // User confirmed the close, so return to build menu
                UserInterface.setupBuildMenu();
                exit.set(true);
            }
        });

        new Thread(() -> {
            System.out.println(frames.size());
            for (Frame f : frames) {
                if (exit.get() == true) {
                    break;
                }
                Platform.runLater(() -> { // Ensures UI updates happen on JavaFX thread
                    System.out.println("Rendering frame");
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
                            update.setFill(Color.BLUE);
                            update.setStroke(Color.BLACK);
                            update.setStrokeWidth(2);
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
                    Thread.sleep(1000); // Simulate delay, but UI won't freeze
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);

    }
}
