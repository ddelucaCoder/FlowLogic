package com.FlowLogic;

import java.util.ArrayList;

public class TrafficController {

    private int numCars;
    private ArrayList<Vehicle> vehicles;
    private ArrayList<Intersection> intersections;

    // TODO: set up frame and step classes

    public TrafficController() {

    }

    public Simulation runSimulation() {
        // TODO: do the actual code lol

        // generate cars and their in-roads and out-roads and time of entrance and destination


        // get each car's route

        // run simulation
        boolean running = true;
        while (running) {
            for (Intersection i : intersections) {
                // TODO: update each intersection
                // TODO: add old / new intersection to the sim
            }

            for (Vehicle v : vehicles) {
                // TODO: update each vehicle
                // TODO: add old / new vehicles to the sim
            }
            running = false;
        }
        return null;
    }

}
