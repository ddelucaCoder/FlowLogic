package com.FlowLogic;


/*
 * Class definition file for com.FlowLogic.Building objects
 *
 * Purdue University
 *
 * @author Dylan Mitchell
 * @version February 18, 2025
 */
public class Building implements GridObject {
    int rowNum;
    int colNum;
    int xLength, yLength, dailyPopulation;
    public Building(int xLength, int yLength, int dailyPopulation) {
        this.xLength = xLength;
        this.yLength = yLength;
        this.dailyPopulation = dailyPopulation;
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

    public int getDailyPopulation() {
        return dailyPopulation;
    }

    public void setDailyPopulation(int dailyPopulation) {
        this.dailyPopulation = dailyPopulation;
    }
}
