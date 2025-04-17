package com.FlowLogic;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import static com.FlowLogic.UserInterface.GRID_SIZE;


public class TrafficController {

    private int numCars;
    private ArrayList<Vehicle> vehicles;
    private ArrayList<GridObject> intersections;

    private int totalTime = 1000;

    private ArrayList<Road> destinations;
    private ArrayList<Road> entrances;

    private int time_between = 5;

    private Grid grid;

    private final Random random = new Random();

    private int[][] graph;

    public TrafficController(int avgSize, int numCars, Grid g) {
        graph = g.gridToGraph();
        System.out.println("Graph: " + Arrays.deepToString(graph));
        grid = g;
        vehicles = new ArrayList<>();
        destinations = new ArrayList<>();
        entrances = new ArrayList<>();
        double cell_size = (720 * 1.0)/GRID_SIZE;
        for (int i = 0; i < numCars; i++) {
            Random ran = new Random();
            int len = ran.nextInt(6) -3 + avgSize;
            len = ((int) (((len * 1.0) /32) * cell_size)) / 2;
            vehicles.add(new Vehicle(len));
        }
        intersections = g.intersections;
        System.out.println("Num intersections: " + intersections.size());
        for (GridObject obj : grid.intersections) {
            if (obj instanceof OneWayRoad r) {
                if (grid.checkAroundDest(r)) {
                    System.out.println("Dest: x: " + r.getColNum() + " y: " + r.getRowNum());
                    destinations.add(r);
                }
                if (r.isInRoad()) {
                    System.out.println("Ent: x: " + r.getColNum() + " y: " + r.getRowNum());
                    entrances.add(r);
                }
            }
        }
        System.out.println("Num Entrances: " + entrances.size());
        System.out.println("Num Destinations: " + destinations.size());
    }

    private Road getRandomInRoad() {
        // get random in roads
        int randInd = random.nextInt(entrances.size());
        return entrances.get(randInd);
    }
    private Road getRandomDestination() {
        // get random in roads
        int randInd = random.nextInt(destinations.size());
        return destinations.get(randInd);
    }

    public Simulation runSimulation() {
        Simulation sim = new Simulation(numCars);


        // generate cars and their in-roads and out-roads and time of entrance and destination
        int currentTime = 0;
        // get each car's route
        for (Vehicle v : vehicles) {
            v.setInOut(getRandomInRoad(), getRandomDestination());
            v.setTimeIn(currentTime += time_between);
            v.findPath(graph, grid.intersections);
        }

        // run simulation
        boolean running = true;
        while (running) {
            Frame f = new Frame();
            totalTime--;
            if (totalTime <= 0) {
                running = false;
            }
            for (GridObject g : intersections) {
                // update each intersection
                if (g instanceof Intersection i) {
                    Step s = i.tick();
                    // add old / new intersection to the sim
                    if (s != null && !s.getNewObject().equals(s.getOldObject())) {
                        f.addStep(s);
                    }
                }

            }

            for (Vehicle v : vehicles) {
                // update each vehicle
                Step s = v.tick(grid, vehicles);
                // add old / new vehicles to the sim
                if (s != null && (s.getNewObject() == null || !s.getNewObject().equals(s.getOldObject()))) {
                    f.addStep(s);
                }
            }
            sim.addFrame(f);
        }
        return sim;
    }

}
