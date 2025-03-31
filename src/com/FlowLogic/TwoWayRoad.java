package com.FlowLogic;

import javafx.scene.image.Image;

public class TwoWayRoad extends Road {

    private OneWayRoad left;
    private OneWayRoad right;
    private int rowNum;
    private int colNum;
    private Image imageFile;


    public TwoWayRoad(Orientation orientation, int speedLimit, boolean isInRoad, int inCars, int rowNum, int colNum, OneWayRoad left, OneWayRoad right) {
        super(orientation, speedLimit, isInRoad, inCars, rowNum, colNum);
        this.left = left;
        this.right = right;
        //left.updateGraphic();
        //right.updateGraphic();
        updateGraphic();
    }

    public TwoWayRoad(Orientation orientation) {
        super(orientation, 25, false, 0, 0, 0);
        this.left = new OneWayRoad(Orientation.HORIZONTAL, Direction.LEFT);
        this.right = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        left.updateGraphic();
        right.updateGraphic();
    }

    public TwoWayRoad(TwoWayRoad t) {
        super(t.getOrientation(), t.getSpeedLimit(), t.isInRoad(), t.getInCars(), t.getRowNum(), t.getColNum());
        this.left = t.getLeft();
        this.right = t.getRight();
        left.updateGraphic();
        right.updateGraphic();
    }

    public void updateGraphic() {
        //left.updateGraphic();
        //right.updateGraphic();
        if (this.getOrientation() == Orientation.VERTICAL) {
                this.imageFile = new Image("file:Images/TwoWayRoad.png");
        } else if (this.getOrientation() == Orientation.HORIZONTAL) {
                this.imageFile = new Image("file:Images/TwoWayRoadRight.png");
        }
    }

    public GridObject clone() {
        return new TwoWayRoad(this);
    }

    public void rotateRoad(Direction direction) {
        if (direction == Direction.UP) {
            left.rotateRoad(Direction.DOWN);
            right.rotateRoad(Direction.UP);
        } else if (direction == Direction.DOWN) {
            left.rotateRoad(Direction.UP);
            right.rotateRoad(Direction.DOWN);
        } else if (direction == Direction.RIGHT) {
            left.rotateRoad(Direction.LEFT);
            right.rotateRoad(Direction.RIGHT);
        } else if (direction == Direction.LEFT) {
            left.rotateRoad(Direction.RIGHT);
            right.rotateRoad(Direction.LEFT);
        }
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
