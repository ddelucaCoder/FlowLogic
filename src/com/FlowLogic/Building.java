package com.FlowLogic;

import javafx.scene.image.Image;

/**
 * Class definition file for com.FlowLogic.Building objects
 */
public class Building implements GridObject {
    String name;
    int rowNum;
    int colNum;
    int xLength, yLength, dailyPopulation;
    private Image imageFile;

    private String color;


    /**
     * Class Definition Function
     * @param xLength Length of the building along the x-axis of the Grid
     * @param yLength Length of the building along the y-axis of the Grid
     * @param dailyPopulation The daily population of people the building will receive
     */
    public Building(int rowNum, int colNum, int xLength, int yLength, int dailyPopulation) {
        this.xLength = xLength;
        this.yLength = yLength;
        this.dailyPopulation = dailyPopulation;
        imageFile = new Image("file:Images/RedBuilding.png");
        this.name = "Building";
    }

    public Building(int xLength, int yLength, int dailyPopulation) {
        this.rowNum = 0;
        this.colNum = 0;
        this.xLength = xLength;
        this.yLength = yLength;
        this.dailyPopulation = dailyPopulation;
        imageFile = new Image("file:Images/RedBuilding.png");
        this.name = "Building";
    }

    public Building() {
        this.xLength = 1;
        this.yLength = 1;
        this.dailyPopulation = 0;
        this.color = "red";
        this.name = "Building";
        updateImage();
    }

    public Building(String color) {
        this.color = color;
        this.xLength = 1;
        this.yLength = 1;
        this.dailyPopulation = 0;
        this.name = "Building";
        updateImage();
    }

    public Building(Building b) {
        this.rowNum = b.getRowNum();
        this.colNum = b.getColNum();
        this.xLength = b.getxLength();
        this.yLength = b.getyLength();
        this.dailyPopulation = b.getDailyPopulation();
        this.color = b.getColor();
        this.name = "Building";
        updateImage();
    }

    private void updateImage() {
        if (this.color.equals("red")) {
            imageFile = new Image("file:Images/RedBuilding.png");
        } else if (this.color.equals("yellow")) {
            imageFile = new Image("file:Images/YellowBuilding.png");
        } else if (this.color.equals("green")) {
            imageFile = new Image("file:Images/GreenBuilding.png");
        }
    }


    public String getColor() {
        return this.color;
    }

    /*
     * Getter and Setter Methods
     */
    public int getRowNum() {
        return rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public void setColNum(int newCol) {
        this.colNum = newCol;
    }

    @Override

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

    public void setColor(String color) {
        this.color = color;
        updateImage();
    }

    public GridObject clone() {
        return new Building(this);
    }
    /*
     * toString Method for Debugging Purposes
     */
    @Override
    public String toString() {
        return "Building{" +
                "rowNum=" + rowNum +
                ", colNum=" + colNum +
                ", xLength=" + xLength +
                ", yLength=" + yLength +
                ", dailyPopulation=" + dailyPopulation +
                '}';
    }
    public Image getImageFile() {
        return imageFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
