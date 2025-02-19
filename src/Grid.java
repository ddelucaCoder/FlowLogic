import GridObject;

/**
 * This class represents the grid that holds all of the "GridObjects," 
 * which includes roads and buildings.
 */

public class Grid {

    private int numRows;
    private int numColumns;

    private GridObject[][] grid;

    public Grid(int numRows, int numColumns) {
        this.numRows = numRows;
        this.numColumns = numColumns;
        grid = new GridObjects[numRows][numColumns];
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

    public void resize(int newNumRows, int newNumCols) {
        // create new 
        GridObject[][] newGrid = new GridObject[newNumRows][newNumCols];
        
        // iterate
        for (int i = 0; i < numRows; i++) {
            for (int k = 0; k < numColumns; k++) {
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
     */
    public void addObject(GridObject newObject, int rowNum, int colNum) {
        // if the spot is already full then do nothing
        if (grid[rowNum][colNum] != null) {
            return;
        }
        // add object to grid
        grid[rowNum][colNum] = newObject;
    }

    public GridObject getAtSpot(int rowNum, int colNum) {
        return grid[rowNum][colNum]
    }

}
