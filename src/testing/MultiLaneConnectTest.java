package testing;

import javafx.embed.swing.JFXPanel;
import org.junit.jupiter.api.*;

import com.FlowLogic.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class MultiLaneConnectTest {

    private Grid grid;
    private final String TEST_FILE = "test_multilane.json";

    @BeforeEach
    public void setUp() {
        // Initialize the JavaFX platform
        new JFXPanel(); // This will initialize the JavaFX toolkit, preventing a Runtime Exception

        // Initialize a 15x15 grid for testing
        grid = new Grid(15, 15);
        Grid.GRID_SIZE = 50; // Set the grid size for consistent testing
        grid.setTestingMode(true);

        // Make sure test file doesn't exist at the start of each test
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    @AfterEach
    public void tearDown() {
        // Clean up test file after each test
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Test that two roads placed side by side in the same direction are added to the same multi-lane container
     */
    @Test
    public void testAdjacentRoadsAddedToMultiLane() {
        // Create two horizontal one-way roads
        OneWayRoad road1 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        OneWayRoad road2 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);

        road1.setSpeedLimit(45);
        road2.setSpeedLimit(45);

        // Add roads to grid in adjacent positions
        grid.addObject(road1, 5, 5);
        grid.addObject(road2, 6, 5);

        // Check that both roads have a lane container
        assertNotNull(road1.getLaneContainer(), "Road 1 should have a lane container");
        assertNotNull(road2.getLaneContainer(), "Road 2 should have a lane container");

        // Check that both roads have the same lane container
        assertEquals(road1.getLaneContainer(), road2.getLaneContainer(),
                "Both roads should be in the same multi-lane container");

        // Check that the lane container contains both roads
        MultiLaneConnect container = road1.getLaneContainer();
        assertEquals(2, container.getLaneList().size(), "Lane container should have 2 roads");
        assertTrue(container.getLaneList().contains(road1), "Lane container should contain road 1");
        assertTrue(container.getLaneList().contains(road2), "Lane container should contain road 2");
    }

    /**
     * Test that roads with different orientations or directions are not added to the same multi-lane container
     */
    @Test
    public void testDifferentRoadsNotAddedToSameMultiLane() {
        // Create roads with different orientations/directions
        OneWayRoad horizontalRightRoad = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        OneWayRoad horizontalLeftRoad = new OneWayRoad(Orientation.HORIZONTAL, Direction.LEFT);
        OneWayRoad verticalRoad = new OneWayRoad(Orientation.VERTICAL, Direction.DOWN);

        // Add roads to grid in adjacent positions
        grid.addObject(horizontalRightRoad, 5, 5);
        grid.addObject(horizontalLeftRoad, 6, 5);
        grid.addObject(verticalRoad, 5, 6);

        // Verify horizontal right road has its own container
        assertNotNull(horizontalRightRoad.getLaneContainer(),
                "Horizontal right road should have a lane container");

        // Verify horizontal left road has its own separate container
        assertNotNull(horizontalLeftRoad.getLaneContainer(),
                "Horizontal left road should have a lane container");

        // Verify vertical road has its own separate container
        assertNotNull(verticalRoad.getLaneContainer(),
                "Vertical road should have a lane container");

        // Verify all roads have different containers
        assertNotEquals(horizontalRightRoad.getLaneContainer(), horizontalLeftRoad.getLaneContainer(),
                "Roads with different directions should not share a container");
        assertNotEquals(horizontalRightRoad.getLaneContainer(), verticalRoad.getLaneContainer(),
                "Roads with different orientations should not share a container");
    }

    /**
     * Test that a new lane added to a road is added to the existing multi-lane container
     */
    @Test
    public void testNewLaneAddedToExistingMultiLane() {
        // Create initial roads
        OneWayRoad road1 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        OneWayRoad road2 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);

        // Add initial roads to grid
        grid.addObject(road1, 5, 5);
        grid.addObject(road2, 6, 5);

        // Get the initial container
        MultiLaneConnect initialContainer = road1.getLaneContainer();
        int initialContainerCount = initialContainer.getCount();

        // Add a third lane
        OneWayRoad road3 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        grid.addObject(road3, 7, 5);

        // Verify the third road is added to the same container
        assertEquals(initialContainer, road3.getLaneContainer(),
                "New road should be added to existing container");
        assertEquals(initialContainerCount, road3.getLaneContainer().getCount(),
                "Container count should remain the same");

        // Verify container now has three roads
        assertEquals(3, initialContainer.getLaneList().size(),
                "Lane container should now have 3 roads");
        assertTrue(initialContainer.getLaneList().contains(road3),
                "Lane container should contain the new road");
    }

    /**
     * Test saving and loading a grid with multiple lanes and verifying the multi-lane container is maintained
     */
    @Test
    public void testSaveAndLoadMultiLane() {
        // Create three horizontal one-way roads to form a multi-lane road
        OneWayRoad road1 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        OneWayRoad road2 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        OneWayRoad road3 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);

        // Set properties
        road1.setSpeedLimit(45);
        road2.setSpeedLimit(50);
        road3.setSpeedLimit(55);

        // Add roads to grid to form a multi-lane road
        grid.addObject(road1, 5, 5);
        grid.addObject(road2, 6, 5);
        grid.addObject(road3, 7, 5);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Create a new grid to load into
        Grid loadedGrid = new Grid(10, 10);
        loadedGrid.setTestingMode(true);

        // Load the grid
        assertTrue(loadedGrid.loadGridState(TEST_FILE));

        // Get the loaded roads
        GridObject loadedRoad1 = loadedGrid.getAtSpot(5, 5);
        GridObject loadedRoad2 = loadedGrid.getAtSpot(6, 5);
        GridObject loadedRoad3 = loadedGrid.getAtSpot(7, 5);

        // Verify all loaded objects are roads
        assertTrue(loadedRoad1 instanceof OneWayRoad);
        assertTrue(loadedRoad2 instanceof OneWayRoad);
        assertTrue(loadedRoad3 instanceof OneWayRoad);

        OneWayRoad loadedOneWayRoad1 = (OneWayRoad) loadedRoad1;
        OneWayRoad loadedOneWayRoad2 = (OneWayRoad) loadedRoad2;
        OneWayRoad loadedOneWayRoad3 = (OneWayRoad) loadedRoad3;

        // Verify each road has a lane container
        assertNotNull(loadedOneWayRoad1.getLaneContainer());
        assertNotNull(loadedOneWayRoad2.getLaneContainer());
        assertNotNull(loadedOneWayRoad3.getLaneContainer());

        // Verify all roads have the same lane container
        assertSame(loadedOneWayRoad1.getLaneContainer(), loadedOneWayRoad2.getLaneContainer());
        assertSame(loadedOneWayRoad1.getLaneContainer(), loadedOneWayRoad3.getLaneContainer());

        // Verify the lane container has all three roads
        MultiLaneConnect loadedContainer = loadedOneWayRoad1.getLaneContainer();
        assertEquals(3, loadedContainer.getLaneList().size());
        assertTrue(loadedContainer.getLaneList().contains(loadedOneWayRoad1));
        assertTrue(loadedContainer.getLaneList().contains(loadedOneWayRoad2));
        assertTrue(loadedContainer.getLaneList().contains(loadedOneWayRoad3));
    }
}