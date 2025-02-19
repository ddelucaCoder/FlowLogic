import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


@Suite
@SuiteDisplayName("Grid Tests")
class GridTest {
    private Grid grid;
    private int numCols = 10;
    private int numRows = 10;
 
    @BeforeEach
    void setUp() {
        grid = new Grid(numRows, numCols);
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
        GridObject object = new GridObject(); 
        int row = 2;
        int col = 3;
        
        grid.addObject(object, row, col);
        
        assertEquals(getAtSpot(row, col), object);

    }

    @Test
    void testAddObjectToOccupiedSpace() {
        GridObject obj1 = new Building(1, 1, 5);
        GridObject obj2 = new Building(1, 1, 6);
        int row = 2;
        int col = 3;
        
        grid.addObject(obj1, row, col);
        grid.addObject(obj2, row, col);

        assertEquals(grid.getAtSpot(row, col), obj1)
        
    }
}