package com.FlowLogic;

import javafx.scene.shape.Rectangle;

/**
 * This class represents the grid that holds all of the "GridObjects"
 * which includes roads and buildings.
 */

public class Grid {

    // The number of rows in the grid
    private int numRows;

    // The number of columns in the grid
    private int numColumns;

    // The grid where objects are stored
    private GridObject[][] grid;
    private Rectangle[][] frontGrid;

    public static int GRID_SIZE;

    public Grid(int numRows, int numColumns) {
        this.numRows = numRows;
        this.numColumns = numColumns;
        grid = new GridObject[numRows][numColumns];
        frontGrid = new Rectangle[numRows][numColumns];
    }

    public Grid(String filename) {
        // TODO: implement grid loading from file 
    }

    public int getNumRows() {
        return this.numRows;
    }

    public int getNumColumns() {
        return this.numColumns;
    }

    public GridObject[][] getGrid() {
        return this.grid;
    }

    public Rectangle[][] getFrontGrid() {
        return frontGrid;
    }

    /**
     * Resizes the grid from its current size to newNumRows X newNumCols. If
     * the grid shrinks, and there are grid objects in the part of the grid
     * that is being removed, those objects will be removed with it.
     * @param newNumRows - the number of rows for the new grid size
     * @param newNumCols - the number of columns for the new grid size
     */
    public void resize(int newNumRows, int newNumCols) {
        // create new 
        GridObject[][] newGrid = new GridObject[newNumRows][newNumCols];
        int leastRows = Math.min(numRows, newNumRows);
        int leastCols = Math.min(numColumns, newNumCols);
        // iterate
        for (int i = 0; i < leastRows; i++) {
            for (int k = 0; k < leastCols; k++) {
                // copy objects from old grid to new one
                newGrid[i][k] = grid[i][k];
            }
        }

        // update grid var
        this.numRows = newNumRows;
        this.numColumns = newNumCols;
        this.grid = newGrid;
    }

    /**
     * This method adds a grid object, newObject, to the grid at (rowNum, colNum).
     * If there is already something in the spot, then it will do nothing.
     * @param newObject - the object to place in the grid
     * @param rowNum - the row number to place the object at
     * @param colNum - the column number to place the object at
     */
    public void addObject(GridObject newObject, int rowNum, int colNum) {
        // if the spot is already full then do nothing
        if (grid[rowNum][colNum] != null) {
            return;
        }
        // add object to grid
        grid[rowNum][colNum] = newObject;
    }

    /**
     * This gets the object at (rowNum, colNum) on the grid.
     * @param rowNum - The row number to get at
     * @param colNum - The column number to get at
     * @return the GridObject at (rowNum, colNum)
     */
    public GridObject getAtSpot(int rowNum, int colNum) {
        return grid[rowNum][colNum];
    }

    /**
     * This function takes a row and column number and turns it
     * into a coordinate on the UI screen
     * @param row - row number to translate
     * @param col - col number to translate
     * @return array of ints formatted (x, y), the bottom left of (row, col) grid spot
     */
    public int[] gridToCoordinate(int row, int col) {
        int[] answer = new int[2];

        // top left = 0, 0
        answer[1] = (numRows - row - 1) * GRID_SIZE;
        // cols already line up
        answer[0] = col * GRID_SIZE;

        return answer;
    }

    /**
     * This function takes coordinates from the UI and translates
     * them into grid coordinates
     * @param x - the x coordinate from the ui.
     * @param y - the y coordinate from the ui.
     * @return an array of integers formatted [row, col]
     */
    public int[] coordinateToGrid(int x, int y) {
        int[] answer = new int[2];

        // round the numbers down to nearest GRID SIZE
        x -= (x % GRID_SIZE);
        y -= (y % GRID_SIZE);

        //translate to grid coords, reversing the y for rows
        answer[0] = numRows - 1 - (y / GRID_SIZE);
        answer[1] = x / GRID_SIZE;

        return answer;
    }

}
