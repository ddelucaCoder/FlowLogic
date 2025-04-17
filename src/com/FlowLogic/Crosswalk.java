package com.FlowLogic;

import javafx.scene.image.Image;

import java.util.ArrayList;

public class Crosswalk extends OneWayRoad implements GridObject {
    private final Image imageFile;

    public Crosswalk(Orientation orientation, int speedLimit, boolean isInRoad, int inCars, int rowNum, int colNum, Direction direction, int numLanes, ArrayList<Vehicle> vehicleList) {
        super(orientation, speedLimit, isInRoad, inCars, rowNum, colNum, direction, numLanes, vehicleList);
        if (orientation == Orientation.VERTICAL) {
            this.imageFile = new Image("file:Images/CrosswalkVertical.png");
        } else {
            this.imageFile = new Image("file:Images/CrosswalkHorizontal.png");
        }
    }

    public Crosswalk(Crosswalk cross) {
        super(cross.getOrientation(), cross.getSpeedLimit(), cross.isInRoad(), cross.getInCars(), cross.getRowNum(), cross.getColNum(), cross.getDirection(), cross.getNumLanes(), cross.getVehicleList());
        this.imageFile = cross.imageFile;
    }


    @Override
    public int getRowNum() {
        return 0;
    }

    @Override
    public int getColNum() {
        return 0;
    }

    @Override
    public Image getImageFile() {
        return null;
    }

    @Override
    public void setRowNum(int row) {

    }

    @Override
    public void setColNum(int col) {

    }

    @Override
    public GridObject clone() {
        return new Crosswalk(this);
    }
}
