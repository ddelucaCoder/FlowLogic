package com.FlowLogic;

import javafx.scene.image.Image;

/**
 * Class Definition File for com.FlowLogic.Parking objects
 * Purdue University
 *
 * @author Dylan Mitchell
 * @version February 20, 2025
 */
public class Parking implements GridObject {
    int rowNum;
    int colNum;
    int xLength, yLength, parkingCapacity, numCars;
    private Image imageFile;


    /**
     * Class Definition Function
     * @param xLength Length of the building along the x-axis of the Grid
     * @param yLength Length of the building along the y-axis of the Grid
     * @param parkingCapacity The max capacity of the parking location
     * @param numCars The number of cars parked at this location
     */
    public Parking(int xLength, int yLength, int parkingCapacity, int numCars) {
        this.xLength = xLength;
        this.yLength = yLength;
        this.parkingCapacity = parkingCapacity;
        this.numCars = numCars;
    }

    /*
     * Getter and Setter Methods
     */
    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public void setColNum(int colNum) {
        this.colNum = colNum;
    }

    public int getxLength() {
        return xLength;
    }

    public void setxLength(int xLength) {
        this.xLength = xLength;
    }

    public int getyLength() {
        return yLength;
    }

    public void setyLength(int yLength) {
        this.yLength = yLength;
    }

    public int getParkingCapacity() {
        return parkingCapacity;
    }

    public void setParkingCapacity(int parkingCapacity) {
        this.parkingCapacity = parkingCapacity;
    }

    public int getNumCars() {
        return numCars;
    }

    public void setNumCars(int numCars) {
        this.numCars = numCars;
    }

    /*
     * toString Method for Debugging Purposes
     */

    public GridObject clone() {
        return null;
    }

    @Override
    public String toString() {
        return "Parking{" +
                "rowNum=" + rowNum +
                ", colNum=" + colNum +
                ", xLength=" + xLength +
                ", yLength=" + yLength +
                ", parkingCapacity=" + parkingCapacity +
                ", numCars=" + numCars +
                '}';
    }
    public Image getImageFile() {
        return imageFile;
    }

}
