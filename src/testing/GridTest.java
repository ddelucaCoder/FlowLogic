package testing;

import com.FlowLogic.*;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static com.FlowLogic.Orientation.HORIZONTAL;
import static com.FlowLogic.Orientation.VERTICAL;
import static javafx.application.Application.launch;
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
    @BeforeAll
    static void initToolkit() throws InterruptedException {
        Thread initThread = new Thread(() -> Platform.startup(() -> {}));
        initThread.setDaemon(true);
        initThread.start();
        initThread.join();  // Wait for JavaFX platform to initialize
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

    @Test
    void testIntersectionSnapping() {
        grid.resize(3, 3);
        grid.testGridInit();

        // place roads around
        Road road1 = new Road(VERTICAL, 25, false, 0, 0, 1);
        Road road2 = new Road(HORIZONTAL, 25, false, 0, 1, 2);
        Road road3 = new Road(VERTICAL, 25, false, 0, 2, 1);
        Road road4 = new Road(HORIZONTAL, 25, false, 0, 1, 0);
        Road[] allRoads = {road1, road2, road3, road4};

        for (Road r : allRoads) {
            grid.addObject(r, r.getRowNum(), r.getColNum());
        }

        // place new road
        grid.addObject(new Road(VERTICAL, 25, false, 0, 1, 1), 1, 1);

        assertTrue(grid.getAtSpot(1, 1) instanceof Intersection);

        // parallel roads should not form an intersection:
        grid = new Grid(3, 3);
        grid.testGridInit();

        allRoads[0] = new Road(VERTICAL, 25, false, 0, 1, 0);
        allRoads[1] = new Road(VERTICAL, 25, false, 0, 1, 1);
        allRoads[2] = new Road(VERTICAL, 25, false, 0, 1, 2);
        allRoads[3] = null;
        for (Road r : allRoads) {
            if (r == null) {
                continue;
            }
            grid.addObject(r, r.getRowNum(), r.getColNum());
        }
        assertFalse(grid.getAtSpot(1, 0) instanceof Intersection);
        assertFalse(grid.getAtSpot(1, 1) instanceof Intersection);
        assertFalse(grid.getAtSpot(1, 2) instanceof Intersection);

        // but just one road different should:
        grid = new Grid(3, 3);
        grid.testGridInit();

        allRoads[0] = new Road(HORIZONTAL, 25, false, 0, 1, 0);
        for (Road r : allRoads) {
            if (r == null) {
                continue;
            }
            grid.addObject(r, r.getRowNum(), r.getColNum());
        }
        assertFalse(grid.getAtSpot(1, 0) instanceof Intersection);
        assertTrue(grid.getAtSpot(1, 1) instanceof Intersection);
        assertFalse(grid.getAtSpot(1, 2) instanceof Intersection);

        grid = new Grid(3, 3);
        grid.testGridInit();

        allRoads[0] = new Road(VERTICAL, 25, false, 0, 1, 0);
        allRoads[1] = new Road(HORIZONTAL, 25, false, 0, 1, 1);
        for (Road r : allRoads) {
            if (r == null) {
                continue;
            }
            grid.addObject(r, r.getRowNum(), r.getColNum());
        }
        assertTrue(grid.getAtSpot(1, 0) instanceof Intersection);
        assertFalse(grid.getAtSpot(1, 1) instanceof Intersection);
        assertTrue(grid.getAtSpot(1, 2) instanceof Intersection);
    }

    @Test
    void testBasicGTG() {
        grid = new Grid(3, 3);
        grid.testGridInit();
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 0, 0, Direction.RIGHT, 1, new ArrayList<>()), 0, 0);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 0, 1, Direction.RIGHT, 1, new ArrayList<>()), 0, 1);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 0, 2, Direction.RIGHT, 1, new ArrayList<>()), 0, 2);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 2, 0, Direction.RIGHT, 1, new ArrayList<>()), 2, 0);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 2, 1, Direction.RIGHT, 1, new ArrayList<>()), 2, 1);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 2, 2, Direction.RIGHT, 1, new ArrayList<>()), 2, 2);
        grid.addObject(new OneWayRoad(VERTICAL, 25, false,
            0, 1, 0, Direction.UP, 1, new ArrayList<>()), 1, 0);
        grid.addObject(new OneWayRoad(VERTICAL, 25, false,
            0, 1, 2, Direction.DOWN, 1, new ArrayList<>()), 1, 2);



        int[][] gtg = grid.gridToGraph();

        int[][] expected = {{0, 1, 0, 0},
                            {0, 0, 0, 1},
                            {1, 0, 0, 1},
                            {0, 0, 0, 0}};


        for (int i = 0; i < gtg.length; i++) {
            assertArrayEquals(expected[i], gtg[i]);
        }

    }

    @Test
    void testAdvancedGTG() {
        grid = new Grid(4, 7);
        grid.testGridInit();
        grid.addObject(new TwoWayRoad(HORIZONTAL, 25, false,
            0, 0, 0, new OneWayRoad(HORIZONTAL, Direction.LEFT), new OneWayRoad(HORIZONTAL, Direction.RIGHT)),
            0, 0);
        grid.addObject(new TwoWayRoad(HORIZONTAL, 25, false,
                0, 0, 1, new OneWayRoad(HORIZONTAL, Direction.LEFT), new OneWayRoad(HORIZONTAL, Direction.RIGHT)),
            0, 1);
        grid.addObject(new TwoWayRoad(HORIZONTAL, 25, false,
                0, 0, 2, new OneWayRoad(HORIZONTAL, Direction.LEFT), new OneWayRoad(HORIZONTAL, Direction.RIGHT)),
            0, 2);
        grid.addObject(new TwoWayRoad(HORIZONTAL, 25, false,
                0, 0, 3, new OneWayRoad(HORIZONTAL, Direction.LEFT), new OneWayRoad(HORIZONTAL, Direction.RIGHT)),
            0, 3);
        grid.addObject(new TwoWayRoad(HORIZONTAL, 25, false,
                0, 0, 4, new OneWayRoad(HORIZONTAL, Direction.LEFT), new OneWayRoad(HORIZONTAL, Direction.RIGHT)),
            0, 4);

        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 0, 5, Direction.LEFT, 1, new ArrayList<>()), 0, 5);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 0, 6, Direction.LEFT, 1, new ArrayList<>()), 0, 6);

        grid.addObject(new OneWayRoad(VERTICAL, 25, false,
            0, 1, 0, Direction.UP, 1, new ArrayList<>()), 1, 0);
        grid.addObject(new OneWayRoad(VERTICAL, 25, false,
            0, 2, 0, Direction.UP, 1, new ArrayList<>()), 2, 0);

        grid.addObject(new OneWayRoad(VERTICAL, 25, false,
            0, 1, 4, Direction.DOWN, 1, new ArrayList<>()), 1, 4);
        grid.addObject(new OneWayRoad(VERTICAL, 25, false,
            0, 2, 4, Direction.DOWN, 1, new ArrayList<>()), 2, 4);

        grid.addObject(new TwoWayRoad(VERTICAL, 25, false,
                0, 1, 6, new OneWayRoad(VERTICAL, Direction.UP), new OneWayRoad(VERTICAL, Direction.DOWN)),
            1, 6);
        grid.addObject(new TwoWayRoad(VERTICAL, 25, false,
                0, 2, 6, new OneWayRoad(VERTICAL, Direction.UP), new OneWayRoad(VERTICAL, Direction.DOWN)),
            2, 6);


        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 3, 0, Direction.RIGHT, 1, new ArrayList<>()), 3, 0);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 3, 1, Direction.RIGHT, 1, new ArrayList<>()), 3, 1);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 3, 2, Direction.RIGHT, 1, new ArrayList<>()), 3, 2);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 3, 3, Direction.RIGHT, 1, new ArrayList<>()), 3, 3);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 3, 4, Direction.RIGHT, 1, new ArrayList<>()), 3, 4);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 3, 5, Direction.RIGHT, 1, new ArrayList<>()), 3, 5);
        grid.addObject(new OneWayRoad(HORIZONTAL, 25, false,
            0, 3, 6, Direction.RIGHT, 1, new ArrayList<>()), 3, 6);



        int[][] gtg = grid.gridToGraph();

        int[][] expected = {{0,1,0,0,0,0},
                            {1,0,0,0,1,0},
                            {0,1,0,0,0,1},
                            {1,0,0,0,1,0},
                            {0,0,0,0,0,1},
                            {0,0,1,0,0,0}};

        /*
        for (int i = 0; i < gtg.length; i++) {
            System.out.print("[ ");
            for (int k = 0; k < gtg.length; k++) {
                System.out.print(gtg[i][k] + ", ");
            }
            System.out.println("]");
        }
         */


        for (int i = 0; i < gtg.length; i++) {
            assertArrayEquals(expected[i], gtg[i]);
        }

    }
}