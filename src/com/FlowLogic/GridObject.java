package com.FlowLogic;

public interface GridObject {
    // Getter methods that classes must implement
    int getRowNum();
    int getColNum();

    void setRowNum(int row);
    void setColNum(int col);

    GridObject clone();
}