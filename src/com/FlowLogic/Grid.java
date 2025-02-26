package com.FlowLogic;

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

    public static int GRID_SIZE;

    public Grid(int numRows, int numColumns) {
        this.numRows = numRows;
        this.numColumns = numColumns;
        grid = new GridObject[numRows][numColumns];
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

    private void setIntersectionAt(int rowNum, int colNum, Road newRoad) {
        if (grid[rowNum][colNum] instanceof Intersection) {
            // add the new road to the intersection
            Intersection current = ((Intersection) grid[rowNum][colNum]);
            current.addRoad(newRoad);
        } else {
            Intersection newIntersection = new Intersection(rowNum, colNum, new Road[4]);
            Orientation orientation = ((Road) grid[rowNum][colNum]).getOrientation();
            // add all roads around to this intersection
            if ((rowNum - 1 >= 0) && (grid[rowNum - 1][colNum] instanceof Road) &&
                (((Road) grid[rowNum - 1][colNum]).getOrientation() != orientation)) {
                newIntersection.addRoad((Road) grid[rowNum - 1][colNum]);
            }
            if ((colNum - 1 >= 0) && (grid[rowNum][colNum - 1] instanceof Road) &&
                (((Road) grid[rowNum][colNum - 1]).getOrientation() != orientation)) {
                newIntersection.addRoad((Road) grid[rowNum][colNum - 1]);
            }
            if ((rowNum + 1 < numRows) && (grid[rowNum + 1][colNum] instanceof Road) &&
                (((Road) grid[rowNum + 1][colNum]).getOrientation() != orientation)) {
                newIntersection.addRoad((Road) grid[rowNum + 1][colNum]);
            }
            if ((colNum + 1 < numColumns) && (grid[rowNum][colNum + 1] instanceof Road) &&
                (((Road) grid[rowNum][colNum + 1]).getOrientation() != orientation)) {
                newIntersection.addRoad((Road) grid[rowNum][colNum + 1]);
            }
        }
    }

    private void checkIntersectionNeeded(int rowNum, int colNum, Road newRoad) {
        // make sure road that were checking is in bounds
        if ((rowNum < 0) || (colNum < 0) ||
            (rowNum >= numRows) || (colNum >= numColumns)) {
            return;
        }
        if (grid[rowNum][colNum] instanceof Intersection) {
            setIntersectionAt(rowNum, colNum, newRoad);
        } else if (!(grid[rowNum][colNum] instanceof Road)) {
            return;
        }
        // get this roads orientation
        Orientation orientation = ((Road) grid[rowNum][colNum]).getOrientation();
        // check area around for any different roads
        if ((rowNum - 1 >= 0) && (grid[rowNum - 1][colNum] instanceof Road) &&
            (((Road) grid[rowNum - 1][colNum]).getOrientation() != orientation)) {
            setIntersectionAt(rowNum, colNum, newRoad);
        } else if ((colNum - 1 >= 0) && (grid[rowNum][colNum - 1] instanceof Road) &&
            (((Road) grid[rowNum][colNum - 1]).getOrientation() != orientation)) {
            setIntersectionAt(rowNum, colNum, newRoad);
        } else if ((rowNum + 1 < numRows) && (grid[rowNum + 1][colNum] instanceof Road) &&
            (((Road) grid[rowNum + 1][colNum]).getOrientation() != orientation)) {
            setIntersectionAt(rowNum, colNum, newRoad);
        } else if ((colNum + 1 < numColumns) && (grid[rowNum][colNum + 1] instanceof Road) &&
            (((Road) grid[rowNum][colNum + 1]).getOrientation() != orientation)) {
            setIntersectionAt(rowNum, colNum, newRoad);
        }
    }

    /**
     *  This function works together with checkIntersectionNeeded and setIntersection to
     *  automatically snap together
     * @param rowNum - the new road location row
     * @param colNum - the new road location column
     * @param newRoad - the new road object
     */

    private void updateIntersections(int rowNum, int colNum, Road newRoad) {
        // check our current road
        checkIntersectionNeeded(rowNum, colNum, newRoad);
        // check all the roads around us
        // ABOVE
        checkIntersectionNeeded(rowNum - 1, colNum, newRoad);
        // LEFT
        checkIntersectionNeeded(rowNum, colNum - 1, newRoad);
        // BELOW
        checkIntersectionNeeded(rowNum + 1, colNum, newRoad);
        // RIGHT
        checkIntersectionNeeded(rowNum, colNum + 1, newRoad);
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

        // automatically snap new roads into intersections
        if (newObject instanceof Road) {
            updateIntersections(rowNum, colNum, (Road) newObject);
        }

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
