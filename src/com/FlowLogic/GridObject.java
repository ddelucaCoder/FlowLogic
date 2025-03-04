package com.FlowLogic;

import javafx.scene.image.Image;

public interface GridObject {
    // Getter methods that classes must implement
    int getRowNum();
    int getColNum();

    Image getImageFile();

    void setRowNum(int row);
    void setColNum(int col);

    GridObject clone();
}