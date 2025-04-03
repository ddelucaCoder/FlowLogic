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
            Simulation sim = tc.runSimulation();

            Grid h = new Grid(5, 5);
            h.addObject(new OneWayRoad(Orientation.HORIZONTAL, 25, true, 0, 0, 0,
                Direction.RIGHT, 1, new ArrayList<>()), 0, 0);
            h.addObject(new OneWayRoad(Orientation.HORIZONTAL, 25, false, 0, 0, 1,
                Direction.RIGHT, 1, new ArrayList<>()), 0, 1);
            h.addObject(new StopSign(0, 2, new Road[4]), 0, 2);
            h.addObject(new OneWayRoad(Orientation.HORIZONTAL, 25, false, 0, 0, 3,
                Direction.RIGHT, 1, new ArrayList<>()), 0, 3);
            h.addObject(new OneWayRoad(Orientation.HORIZONTAL, 25, false, 0, 0, 4,
                Direction.RIGHT, 1, new ArrayList<>()), 0, 4);

            h.addObject(new Parking(), 1, 4);

            System.out.println("Grid Graph");
            System.out.println(Arrays.deepToString(g.gridToGraph()));
            // set up a sim

            TrafficController tc1 = new TrafficController(1, h);

            // run the sim
            Simulation sim1 = tc1.runSimulation();




        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}