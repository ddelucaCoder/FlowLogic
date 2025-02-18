import GridObjects;

public class Grid {

    private int numRows;
    private int numColumns;

    private GridObject[][] grid;

    public Grid(int numRows, int numColumns) {
        this.numRows = numRows;
        this.numColumns = numColumns;
        grid = new GridObjects[numRows][numColumns];
    }

    public int getNumRows() {
        return this.numRows;
    }

    public int getNumColumns() {
        return this.numColumns;
    }

    public void resize(int newXSize, int newYSize) {

    }

    public void addObject(GridObject newObject, int rowNum, int colNum) {
        if (grid[rowNum][colNum] != null) {
            return;
        }
        grid[rowNum][colNum] = newObject;
    }

}
