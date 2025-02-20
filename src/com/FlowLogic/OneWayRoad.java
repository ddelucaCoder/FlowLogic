package com.FlowLogic;

import java.util.ArrayList;


/**
 * This class is for a one lane road that extends the road class
 * It allows cars to only go one way on it, with all lanes going
 * in one direction.
 */
public class OneWayRoad extends Road{

    private int direction;
    private int numLanes;
    private ArrayList<Vehicle> vehicleList;

    public OneWayRoad(int orientation, int speedLimit, boolean isInRoad, int inCars, int rowNum, int colNum, int direction, int numLanes, ArrayList<Vehicle> vehicleList) {
        super(orientation, speedLimit, isInRoad, inCars, rowNum, colNum);
        this.direction = direction;
        this.numLanes = numLanes;
        this.vehicleList = vehicleList;
    }

    public boolean addCar(ArrayList<Vehicle> addCarList) {
        if (addCarList.isEmpty()) {
            System.out.println("You must select a car to add\n");
            return false;
        }
        this.vehicleList.addAll(addCarList);
        return true;
    }

    public boolean addCar (Vehicle car) {
        if (car == null) {
            System.out.println("Cannot add null car\n");
            return false;
        }
        this.vehicleList.add(car);
        return true;
    }


    /* Getters and setters */

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public int getNumLanes() {
        return numLanes;
    }

    public void setNumLanes(int numLanes) {
        this.numLanes = numLanes;
    }

    public ArrayList<Vehicle> getVehicleList() {
        return vehicleList;
    }

    public void setVehicleList(ArrayList<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }


}
