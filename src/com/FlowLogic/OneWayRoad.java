package com.FlowLogic;

import javafx.scene.image.Image;

import java.util.ArrayList;


/**
 * This class is for a one lane road that extends the road class
 * It allows cars to only go one way on it, with all lanes going
 * in one direction.
 */
public class OneWayRoad extends Road implements GridObject {

    private Direction direction;
    private int numLanes;
    private ArrayList<Vehicle> vehicleList;
    private int rowNum;
    private int colNum;
    private Image imageFile;


    public OneWayRoad(Orientation orientation, int speedLimit, boolean isInRoad, int inCars, int rowNum, int colNum, Direction direction, int numLanes, ArrayList<Vehicle> vehicleList) {
        super(orientation, speedLimit, isInRoad, inCars, rowNum, colNum);
        this.direction = direction;
        this.numLanes = numLanes;
        this.vehicleList = vehicleList;
        updateGraphic();
    }

    public boolean addCar(ArrayList<Vehicle> addCarList) {
        if (addCarList.isEmpty()) {
            System.out.println("You must select a car to add\n");
            return false;
        }
        this.vehicleList.addAll(addCarList);
        return true;
    }

    public boolean addCar(Vehicle car) {
        if (car == null) {
            System.out.println("Cannot add null car\n");
            return false;
        }
        this.vehicleList.add(car);
        return true;
    }

    public void updateGraphic() {
        if (this.getDirection() == Direction.UP) {
            this.imageFile = new Image("file:Images/RoadImage.png");
        } else if (this.getDirection() == Direction.LEFT) {
            this.imageFile = new Image("file:Images/RoadImageLeft");
        } else if (this.getDirection() == Direction.DOWN) {
            this.imageFile = new Image("file:Images/RoadImageDown");
        } else if (this.getDirection() == Direction.RIGHT) {
            this.imageFile = new Image("file:Images/RoadImageRight");
        }
    }

   public void rotateRoad(Direction direction) {
        this.direction = direction;
        updateGraphic();
   }


    /* Getters and setters */

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
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

    public int getRowNum() {
        return rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public void setColNum(int newCol) {
        this.colNum = newCol;
    }

    public void setRowNum(int newRow) {
        this.rowNum = newRow;

    }

    @Override
    public Image getImageFile() {
        return imageFile;
    }

    @Override
    public void setImageFile(Image imageFile) {
        this.imageFile = imageFile;
    }

}
