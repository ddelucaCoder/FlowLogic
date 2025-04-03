package com.FlowLogic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class TrafficController {

    private int numCars;
    private ArrayList<Vehicle> vehicles;
    private ArrayList<GridObject> intersections;

    private int totalTime = 1000;

    private ArrayList<Road> destinations;
    private ArrayList<Road> entrances;

    private Grid grid;

    private final Random random = new Random();

    public TrafficController(int avgSize, int numCars, Grid g) {
        g.gridToGraph();
        grid = g;
        vehicles = new ArrayList<>();
        destinations = new ArrayList<>();
        entrances = new ArrayList<>();
        for (int i = 0; i < numCars; i++) {
            Random ran = new Random();
            vehicles.add(new Vehicle(ran.nextInt(6) -3 + avgSize));
        }
        intersections = g.intersections;
        for (GridObject obj : grid.intersections) {
            if (obj instanceof Road r) {
                if (grid.checkAroundDest(r)) {
                    destinations.add(r);
                }
                if (r.isInRoad()) {
                    entrances.add(r);
                }
            }
        }

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

        // get each car's route
        for (Vehicle v : vehicles) {
            v.setInOut(getRandomInRoad(), getRandomDestination());
            v.setTimeIn(0);//TODO: (int) (Math.random() *  totalTime));
            v.findPath(grid.gridToGraph(), grid.intersections);
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
                Step s = v.tick(grid);
                // add old / new vehicles to the sim
                if (s != null && (s.getNewObject() == null || !s.getNewObject().equals(s.getOldObject()))) {
                    f.addStep(s);
                }
            }
            sim.addFrame(f);
        }
        return null;
    }

}
