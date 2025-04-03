package testing;

import com.FlowLogic.*;
import javafx.application.Platform;

import java.util.ArrayList;

import java.util.Arrays;

public class BasicSimTest {

    static void initToolkit() throws InterruptedException {
        Thread initThread = new Thread(() -> Platform.startup(() -> {}));
        initThread.setDaemon(true);
        initThread.start();
        initThread.join();  // Wait for JavaFX platform to initialize
    }

    public static void main(String[] args) {
        try {
            // Initialize JavaFX toolkit before creating any JavaFX objects
            initToolkit();

            // Now it's safe to create objects that might use JavaFX
            Grid g = new Grid(5, 5);
            g.addObject(new OneWayRoad(Orientation.HORIZONTAL, 25, true, 0, 0, 0,
                Direction.RIGHT, 1, new ArrayList<>()), 0, 0);
            g.addObject(new OneWayRoad(Orientation.HORIZONTAL, 25, false, 0, 0, 1,
                Direction.RIGHT, 1, new ArrayList<>()), 0, 1);
            g.addObject(new OneWayRoad(Orientation.HORIZONTAL, 25, false, 0, 0, 2,
                Direction.RIGHT, 1, new ArrayList<>()), 0, 2);
            g.addObject(new OneWayRoad(Orientation.HORIZONTAL, 25, false, 0, 0, 3,
                Direction.RIGHT, 1, new ArrayList<>()), 0, 3);
            g.addObject(new OneWayRoad(Orientation.HORIZONTAL, 25, false, 0, 0, 4,
                Direction.RIGHT, 1, new ArrayList<>()), 0, 4);

            g.addObject(new Parking(), 1, 4);

            System.out.println("Grid Graph");
            System.out.println(Arrays.deepToString(g.gridToGraph()));
            // set up a sim

            TrafficController tc = new TrafficController(1, g);

            // run the sim
            tc.runSimulation();
            // print the output

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}