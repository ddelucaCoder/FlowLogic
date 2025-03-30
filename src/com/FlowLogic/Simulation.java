package com.FlowLogic;

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
}
