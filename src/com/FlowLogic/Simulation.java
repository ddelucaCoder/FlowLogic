package com.FlowLogic;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

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
    public void display(Stage stage, AnchorPane root){
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
        back.setOnAction(e -> {
            UserInterface.setupBuildMenu();
        });
        for (Frame f : frames) {
            for (Step s : f.getSteps()) {

            }
        }

        //Scene scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);

    }
}
