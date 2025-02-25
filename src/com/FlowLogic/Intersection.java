package com.FlowLogic;

public class Intersection implements GridObject {

    Road[] roadList;
    int rowNum;
    int colNum;

    public Intersection(int rowNum, int colNum, Road[] roadList) {
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.roadList = roadList;
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
}
