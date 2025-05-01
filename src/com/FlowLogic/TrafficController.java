package com.FlowLogic;

import java.lang.reflect.Array;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import static com.FlowLogic.UserInterface.GRID_SIZE;


public class TrafficController {

    public static int numCarsLeft;
    private ArrayList<Vehicle> vehicles;
    private ArrayList<GridObject> intersections;

    private int totalTime = 20000;

    private ArrayList<Road> destinations;
    private ArrayList<Road> entrances;

    private int time_between = 5;

    private Grid grid;

    private final Random random = new Random();

    private int[][] graph;

    // stats parameters
    private long totalTripTime;

    public TrafficController(int avgSize, int numCars, Grid g) {
        graph = g.gridToGraph();
        this.numCarsLeft = numCars;

        System.out.println("Graph: " + Arrays.deepToString(graph));

        grid = g;
        vehicles = new ArrayList<>();
        destinations = new ArrayList<>();
        entrances = new ArrayList<>();

        Random ran = new Random();
        for (int i = 0; i < numCars; i++) {
            int len = ran.nextInt(6) -3 + avgSize;
            vehicles.add(new Vehicle(len));
        }
        System.out.println("Num cars: " + vehicles.size());
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
        Simulation sim = new Simulation(numCarsLeft);

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
            if (totalTime <= 0 || numCarsLeft <= 0) {
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
                calcActiveVehicles();
                //System.out.println("Active vehicles = " + UserInterface.activeVehicles);
                //sim.updateStatisticsLabels(sim.getLeft());
                // add old / new vehicles to the sim
                if (s != null && (s.getNewObject() == null || !s.getNewObject().equals(s.getOldObject()))) {
                    f.addStep(s);
                }
            }
            sim.addFrame(f);
        }
        return sim;
    }
    public void rushHour() {
        this.time_between = 3;
        Vehicle.rushHour(5, 3);
    }


    public ArrayList<Vehicle> getVehicles() {
        return vehicles;
    }
    public long getAvgTripTime() {
        int num = vehicles.size();
        for (Vehicle vehicle : vehicles) {
            totalTripTime += vehicle.getTripTime();
            System.out.println("Vehcile trip time = " + vehicle.getTripTime());
        }
        System.out.println("AVGSTAT: total time = " + totalTripTime + " Num = " + num);
        if (num != 0) {
            return totalTripTime / num;
        }
        return totalTripTime;
    }

    public long getAvgIntersectionWaitTime() {
        int totalStops = 0;
        long totalWaitTime = 0;
        for (Vehicle vehicle : vehicles) {
            totalWaitTime += vehicle.getTotalWaitTime();
            totalStops += vehicle.getTotalStops();
        }
        if (totalStops != 0) {
            return totalWaitTime / totalStops;
        }
        return totalWaitTime;
    }

    public long getMaxWaitTime() {
        long max = 0;
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getMaxWaitTime() > max) {
                max = vehicle.getMaxWaitTime();
            }
        }
        return max;
    }
    public long getMinWaitTime() {
        long min = Integer.MAX_VALUE;
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getMinWaitTime() < min) {
                min = vehicle.getMinWaitTime();
                //System.out.println("Final min wait is now: " + min);
            }
        }
        return min;
    }

    public void calcActiveVehicles() {
        int totalCars = 0;
        for (Vehicle vehicle : vehicles) {
            if (vehicle.isSpawned()) {
                totalCars++;
            }
        }
        UserInterface.activeVehicles = totalCars;
    }


}
