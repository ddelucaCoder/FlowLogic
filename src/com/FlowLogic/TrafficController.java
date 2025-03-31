package com.FlowLogic;

import java.util.ArrayList;

public class TrafficController {

    private int numCars;
    private ArrayList<Vehicle> vehicles;
    private ArrayList<Intersection> intersections;

    private int totalTime;

    private ArrayList<GridObject> destinations;
    private ArrayList<GridObject> entrances;


    // TODO: set up frame and step classes

    public TrafficController() {

    }

    public Simulation runSimulation() {
        Simulation sim = new Simulation(numCars);

        // generate cars and their in-roads and out-roads and time of entrance and destination

        // get each car's route
        for (Vehicle v : vehicles) {
            v.setInOut(entrances, destinations);
            v.setTimeIn((int) (Math.random() *  totalTime));
            v.generatePath();
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
