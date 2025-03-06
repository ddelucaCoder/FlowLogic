package com.FlowLogic;

import javafx.scene.image.Image;

public class Intersection implements GridObject {

    private Road[] roadList;
    private int rowNum;
    private int colNum;
    private Image imageFile;

    private int intersectionID;

    public Intersection(int rowNum, int colNum, Road[] roadList) {
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.roadList = roadList;
        this.imageFile = new Image("file:Images/BasicIntersection.png");
    }

    public Intersection(Intersection i) {
        this.rowNum = i.getRowNum();
        this.colNum = i.getColNum();
        this.roadList = i.getRoadList();
        this.imageFile = new Image("file:Images/BasicIntersection.png");
    }

    public int getIntersectionID() {
        return intersectionID;
    }

    public void setIntersectionID(int intersectionID) {
        this.intersectionID = intersectionID;
    }

    public GridObject clone() {
        return new Intersection(this);
    }

    @Override
    public int getColNum() {
        return colNum;
    }

    @Override
    public void setColNum(int colNum) {
        this.colNum = colNum;
    }

    @Override
    public int getRowNum() {
        return rowNum;
    }


    @Override
    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public void addRoad(Road road) {
        for (int i = 0; i < 4; i++) {
            if (roadList[i] != null) {
                roadList[i] = road;
                return;
            }
        }
    }

    public Road[] getRoadList() {
        return roadList;
    }

    public void setRoadList(Road[] roadList) {
        this.roadList = roadList;
    }


    public Image getImageFile() {
        return imageFile;
    }

    public void setImageFile(Image imageFile) {
        this.imageFile = imageFile;
    }
}
