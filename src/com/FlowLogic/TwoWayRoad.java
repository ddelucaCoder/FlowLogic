package com.FlowLogic;

public class TwoWayRoad extends Road {

    private OneWayRoad left;
    private OneWayRoad right;
    private int rowNum;
    private int colNum;


    public TwoWayRoad(Orientation orientation, int speedLimit, boolean isInRoad, int inCars, int rowNum, int colNum, OneWayRoad left, OneWayRoad right) {
        super(orientation, speedLimit, isInRoad, inCars, rowNum, colNum);
        this.left = left;
        this.right = right;
    }

    public OneWayRoad getLeft() {
        return left;
    }

    public void setLeft(OneWayRoad left) {
        this.left = left;
    }

    public OneWayRoad getRight() {
        return right;
    }

    public void setRight(OneWayRoad right) {
        this.right = right;
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
}
