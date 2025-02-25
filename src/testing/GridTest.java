package testing;

import com.FlowLogic.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class GridTest {
    private Grid grid;
    private int numCols = 10;
    private int numRows = 10;
 
    @BeforeEach
    void setUp() {
        grid = new Grid(numRows, numCols);
        Grid.GRID_SIZE = 32;
    }

    @Test
    void testBasicConstructor() {
        assertEquals(numCols, grid.getNumRows());
        assertEquals(numRows, grid.getNumColumns());
    }


    @Test
    void testResize() {
        // expanding the grid
        int newRows = 7;
        int newCols = 8;
        grid.resize(newRows, newCols);
        
        assertEquals(newRows, grid.getNumRows());
        assertEquals(newCols, grid.getNumColumns());

        // check if 

        // shrinking the grid
        newRows = 3;
        newCols = 4;
        grid.resize(newRows, newCols);
        
        assertEquals(newRows, grid.getNumRows());
        assertEquals(newCols, grid.getNumColumns());
    }


    @Test
    void testAddObject() {
        GridObject object = new Building(1, 1, 15);
        int row = 2;
        int col = 3;
        
        grid.addObject(object, row, col);
        
        assertEquals(grid.getAtSpot(row, col), object);
    }

    @Test
    void testAddObjectToOccupiedSpace() {
        GridObject obj1 = new Building(1, 1, 5);
        GridObject obj2 = new Building(1, 1, 6);
        int row = 2;
        int col = 3;
        
        grid.addObject(obj1, row, col);
        grid.addObject(obj2, row, col);

        assertEquals(grid.getAtSpot(row, col), obj1);
        
    }

    @Test
    void testGridToCoordinate() {
        // FOUR CORNERS FIRST
        // grid spot (0, 0) = (0, 288) TOP LEFT
        int[] response = {0, 288};
        assertArrayEquals(grid.gridToCoordinate(0, 0), response);

        // grid spot (9, 0) = (0, 0) BOTTOM LEFT
        response[0] = 0;
        response[1] = 0;
        assertArrayEquals(grid.gridToCoordinate(9, 0), response);

        // grid spot (9, 9) = (288, 288) BOTTOM RIGHT
        response[0] = 288;
        response[1] = 0;
        assertArrayEquals(grid.gridToCoordinate(9, 9), response);

        // grid spot (0, 9) = (288, 288) TOP RIGHT
        response[0] = 288;
        response[1] = 288;
        assertArrayEquals(grid.gridToCoordinate(0, 9), response);

        // NOW SOME RANDOM ONES

        // grid spot (8, 1) = (32, 32) TOP RIGHT
        response[0] = 32;
        response[1] = 32;
        assertArrayEquals(grid.gridToCoordinate(8, 1), response);

        // grid spot (6, 3) = (96, 96) TOP RIGHT
        response[0] = 96;
        response[1] = 96;
        assertArrayEquals(grid.gridToCoordinate(6, 3), response);

        // grid spot (6, 4) = (128, 96) TOP RIGHT
        response[0] = 128;
        response[1] = 96;
        assertArrayEquals(grid.gridToCoordinate(6, 4), response);
    }

    @Test
    void testCoordinateToGrid() {
        int[] response = {0, 0};
        assertArrayEquals(grid.coordinateToGrid(4, 296), response);


        response[0] = 6;
        response[1] = 3;
        assertArrayEquals(grid.coordinateToGrid(100, 100), response);

        response[0] = 0;
        response[1] = 9;
        assertArrayEquals(grid.coordinateToGrid(300, 300), response);

        response[0] = 9;
        response[1] = 9;
        assertArrayEquals(grid.coordinateToGrid(300, 10), response);

    }
}