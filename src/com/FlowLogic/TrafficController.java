package com.FlowLogic;

import java.util.ArrayList;
import java.util.Random;

public class TrafficController {

    private int numCars;
    private ArrayList<Vehicle> vehicles;
    private ArrayList<Intersection> intersections;

    private int totalTime;

    private ArrayList<Road> destinations;
    private ArrayList<Road> entrances;

    private Grid grid;

    private final Random random = new Random();

    public TrafficController() {

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
            v.setTimeIn((int) (Math.random() *  totalTime));
            v.findPath(grid.gridToGraph(), grid.intersections);
        }

        // run simulation
        boolean running = true;
        while (running) {
            Frame f = new Frame();
            for (Intersection i : intersections) {
                // update each intersection
                Step s = i.tick();
                // add old / new intersection to the sim
                if (!s.getNewObject().equals(s.getOldObject())) {
                    f.addStep(s);
                }

            }

            for (Vehicle v : vehicles) {
                // update each vehicle
                Step s = v.tick();
                // add old / new vehicles to the sim
                if (!s.getNewObject().equals(s.getOldObject())) {
                    f.addStep(s);
                }
            }
            sim.addFrame(f);
            running = false;
        }
        return null;
    }

}
