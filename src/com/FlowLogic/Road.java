package com.FlowLogic;


import javafx.scene.image.Image;

/**
 * This class is a Grid Object for a road. The road will connect
 * buildings and cars will drive on them. They are placed on the grid
 * by the user.
 */
public class Road implements GridObject {

    private Orientation orientation;
    private int speedLimit;
    private int length;
    private boolean isInRoad;
    private int inCars;
    private Image imageFile;
    private int rowNum;
    private int colNum;

    public Road (Orientation orientation, int speedLimit, boolean isInRoad, int inCars, int rowNum, int colNum) {
        this.orientation = orientation;
        this.speedLimit = speedLimit;
        this.isInRoad = isInRoad;
        this.inCars = inCars;
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.imageFile = new Image("file:Images/RoadImage.png");


    }

    @Override
    public int getRowNum() {
        return rowNum;
    }
    public GridObject clone() {
        return null;
    }

    @Override
    public int getColNum() {
        return colNum;
    }

    @Override
    public void setRowNum(int row) {
        this.rowNum = row;
    }

    @Override
    public void setColNum(int col) {
        this.colNum = col;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation = orientation;
    }

    public int getSpeedLimit() {
        return speedLimit;
    }

    public void setSpeedLimit(int speedLimit) {
        this.speedLimit = speedLimit;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isInRoad() {
        return isInRoad;
    }

    public void setInRoad(boolean inRoad) {
        isInRoad = inRoad;
    }

    public int getInCars() {
        return inCars;
    }

    public void setInCars(int inCars) {
        this.inCars = inCars;
    }

    public Image getImageFile() {
        return imageFile;
    }

    public void setImageFile(Image imageFile) {
        this.imageFile = imageFile;
    }
}
