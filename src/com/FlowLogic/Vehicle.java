package com.FlowLogic;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Random;

public class Vehicle {
    public Vehicle() {
    }
    public static void disperse(ArrayList<Vehicle> vehicles, Grid grid) {
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
