package com.FlowLogic;

import javafx.scene.image.Image;
import org.w3c.dom.html.HTMLAreaElement;

public class Hazard implements GridObject {
    private int rowNum;
    private int colNum;
    private final Image imageFile;
    private GridObject coveredObject;


    public Hazard(int rowNum, int colNum) {
        this.rowNum = rowNum;
        this.colNum = colNum;
        this.imageFile = new Image("file:Images/Hazard.png");
    }

    public Hazard(Hazard hazard) {
        this.rowNum = hazard.rowNum;
        this.colNum = hazard.colNum;
        this.imageFile = hazard.imageFile;
    }

    @Override
    public int getRowNum() {
        return rowNum;
    }

    @Override
    public int getColNum() {
        return colNum;
    }

    @Override
    public Image getImageFile() {
        return imageFile;
    }

    @Override
    public void setRowNum(int row) {
        rowNum = row;
    }

    @Override
    public void setColNum(int col) {
        colNum = col;
    }

    @Override
    public GridObject clone() {
        return new Hazard(this);
    }
}
