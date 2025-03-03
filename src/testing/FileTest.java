package testing;

import org.junit.jupiter.api.*;
import org.json.JSONObject;
import org.json.JSONArray;

import com.FlowLogic.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

public class FileTest {

    private Grid grid;
    private final String TEST_FILE = "test_grid.json";

    // Method will set up a grid before each test
    @BeforeEach
    public void setUp() {
        // Initialize a 10x10 grid for testing
        grid = new Grid(10, 10);
        Grid.GRID_SIZE = 50; // Set the grid size for consistent testing

        // Make sure test file doesn't exist at the start of each test
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    // Method will empty the test file after a test finishes
    @AfterEach
    public void tearDown() {
        // Clean up test file after each test
        File file = new File(TEST_FILE);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Test Saving an empty grid
     * Will test saving dimensions and size of the grid
     */
    @Test
    public void testSaveEmptyGrid() {
        // Test saving an empty grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Verify file exists
        File file = new File(TEST_FILE);
        assertTrue(file.exists());

        // Verify dimensions and size (no objects in the grid yet)
        try {
            String content = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
            JSONObject gridJson = new JSONObject(content);

            assertEquals(10, gridJson.getInt("numRows"));
            assertEquals(10, gridJson.getInt("numColumns"));
            assertEquals(50, gridJson.getInt("gridSize"));
            assertTrue(gridJson.has("objects"));

            JSONArray objects = gridJson.getJSONArray("objects");
            assertEquals(0, objects.length()); // Empty grid has no objects
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * Test Saving a Grid with a building in it
     */
    @Test
    public void testSaveGridWithBuilding() {
        // Add a building to the grid
        Building building = new Building(2, 3, 100);
        grid.addObject(building, 5, 5);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Verify building was saved correctly
        try {
            String content = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
            JSONObject gridJson = new JSONObject(content);
            JSONArray objects = gridJson.getJSONArray("objects");

            assertEquals(1, objects.length());
            JSONObject buildingJson = objects.getJSONObject(0);

            assertEquals(5, buildingJson.getInt("row"));
            assertEquals(5, buildingJson.getInt("column"));
            assertEquals("Building", buildingJson.getString("type"));

            JSONObject properties = buildingJson.getJSONObject("properties");
            assertEquals(2, properties.getInt("xLength"));
            assertEquals(3, properties.getInt("yLength"));
            assertEquals(100, properties.getInt("dailyPopulation"));
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * Test Saving a Grid with a Road in it
     */
    @Test
    public void testSaveGridWithRoad() {
        // Add a road to the grid
        Road road = new Road(Orientation.HORIZONTAL, 45, false, 0, 3, 3);
        road.setLength(5);
        grid.addObject(road, 3, 3);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Verify road was saved correctly
        try {
            String content = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
            JSONObject gridJson = new JSONObject(content);
            JSONArray objects = gridJson.getJSONArray("objects");

            assertEquals(1, objects.length());
            JSONObject roadJson = objects.getJSONObject(0);

            assertEquals(3, roadJson.getInt("row"));
            assertEquals(3, roadJson.getInt("column"));
            assertEquals("Road", roadJson.getString("type"));

            JSONObject properties = roadJson.getJSONObject("properties");
            assertEquals("HORIZONTAL", properties.getString("orientation"));
            assertEquals(45, properties.getInt("speedLimit"));
            assertEquals(5, properties.getInt("length"));
            assertFalse(properties.getBoolean("isInRoad"));
            assertEquals(0, properties.getInt("inCars"));
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * Test Saving a Grid with a parking lot in it
     */
    @Test
    public void testSaveGridWithParking() {
        // Add a parking lot to the grid
        Parking parking = new Parking(3, 4, 50, 10);
        grid.addObject(parking, 7, 7);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Verify parking lot was saved correctly
        try {
            String content = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
            JSONObject gridJson = new JSONObject(content);
            JSONArray objects = gridJson.getJSONArray("objects");

            assertEquals(1, objects.length());
            JSONObject parkingJson = objects.getJSONObject(0);

            assertEquals(7, parkingJson.getInt("row"));
            assertEquals(7, parkingJson.getInt("column"));
            assertEquals("Parking", parkingJson.getString("type"));

            JSONObject properties = parkingJson.getJSONObject("properties");
            assertEquals(3, properties.getInt("xLength"));
            assertEquals(4, properties.getInt("yLength"));
            assertEquals(50, properties.getInt("parkingCapacity"));
            assertEquals(10, properties.getInt("numCars"));
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * Test Saving a Grid with an intersection in it
     */
    @Test
    public void testSaveGridWithIntersection() {
        // Create an intersection with connected roads
        Road road1 = new Road(Orientation.HORIZONTAL, 45, false, 0, 4, 3);
        Road road2 = new Road(Orientation.VERTICAL, 35, false, 0, 3, 4);

        grid.addObject(road1, 4, 3);
        grid.addObject(road2, 3, 4);

        // Create and add the intersection manually
        Road[] roadList = new Road[4];
        roadList[0] = road1;
        roadList[1] = road2;
        Intersection intersection = new Intersection(4, 4, roadList);
        grid.addObject(intersection, 4, 4);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Verify intersection was saved correctly
        try {
            String content = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
            JSONObject gridJson = new JSONObject(content);
            JSONArray objects = gridJson.getJSONArray("objects");

            // Find the intersection in the saved objects
            JSONObject intersectionJson = null;
            for (int i = 0; i < objects.length(); i++) {
                JSONObject obj = objects.getJSONObject(i);
                if ("Intersection".equals(obj.getString("type")) &&
                        obj.getInt("row") == 4 && obj.getInt("column") == 4) {
                    intersectionJson = obj;
                    break;
                }
            }

            assertNotNull(intersectionJson, "Intersection not found in saved data");

            JSONObject properties = intersectionJson.getJSONObject("properties");
            assertTrue(properties.has("connectedRoads"));

            JSONArray connectedRoads = properties.getJSONArray("connectedRoads");
            assertEquals(2, connectedRoads.length());

            // Verify one of the connected roads is a horizontal road
            boolean hasHorizontalRoad = false;
            boolean hasVerticalRoad = false;

            for (int i = 0; i < connectedRoads.length(); i++) {
                JSONObject roadRef = connectedRoads.getJSONObject(i);
                if (roadRef.getString("orientation").equals("HORIZONTAL")) {
                    hasHorizontalRoad = true;
                }
                if (roadRef.getString("orientation").equals("VERTICAL")) {
                    hasVerticalRoad = true;
                }
            }

            assertTrue(hasHorizontalRoad, "No horizontal road connected to intersection");
            assertTrue(hasVerticalRoad, "No vertical road connected to intersection");

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * Test Saving an empty grid and loading it again
     */
    @Test
    public void testSaveAndLoadEmptyGrid() {
        // Save the empty grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Create a new grid to load into
        Grid loadedGrid = new Grid(5, 5); // Different dimensions to ensure they're properly loaded

        // Load the grid
        assertTrue(loadedGrid.loadGridState(TEST_FILE));

        // Verify dimensions were loaded correctly
        assertEquals(10, loadedGrid.getNumRows());
        assertEquals(10, loadedGrid.getNumColumns());
        assertEquals(50, Grid.GRID_SIZE);
    }

    /**
     * Test Saving a grid with various objects, then loading it once more back into GridObjects
     */
    @Test
    public void testSaveAndLoadGridWithObjects() {
        // Create a grid with various objects
        Building building = new Building(2, 3, 100);
        grid.addObject(building, 1, 1);

        Road horizontalRoad = new Road(Orientation.HORIZONTAL, 45, false, 0, 3, 2);
        horizontalRoad.setLength(3);
        grid.addObject(horizontalRoad, 3, 2);

        Road verticalRoad = new Road(Orientation.VERTICAL, 35, false, 0, 2, 3);
        verticalRoad.setLength(2);
        grid.addObject(verticalRoad, 2, 3);

        Parking parking = new Parking(3, 2, 25, 5);
        grid.addObject(parking, 6, 6);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Create a new grid to load into
        Grid loadedGrid = new Grid(5, 5);

        // Load the grid
        assertTrue(loadedGrid.loadGridState(TEST_FILE));

        // Verify everything was loaded correctly
        // Check dimensions
        assertEquals(10, loadedGrid.getNumRows());
        assertEquals(10, loadedGrid.getNumColumns());

        // Check Building
        GridObject loadedBuilding = loadedGrid.getAtSpot(1, 1);
        assertTrue(loadedBuilding instanceof Building);
        assertEquals(2, ((Building) loadedBuilding).getxLength());
        assertEquals(3, ((Building) loadedBuilding).getyLength());
        assertEquals(100, ((Building) loadedBuilding).getDailyPopulation());

        // Check Horizontal Road
        GridObject loadedHRoad = loadedGrid.getAtSpot(3, 2);
        assertTrue(loadedHRoad instanceof Road);
        assertEquals(Orientation.HORIZONTAL, ((Road) loadedHRoad).getOrientation());
        assertEquals(45, ((Road) loadedHRoad).getSpeedLimit());
        assertEquals(3, ((Road) loadedHRoad).getLength());

        // Check Vertical Road
        GridObject loadedVRoad = loadedGrid.getAtSpot(2, 3);
        assertTrue(loadedVRoad instanceof Road);
        assertEquals(Orientation.VERTICAL, ((Road) loadedVRoad).getOrientation());
        assertEquals(35, ((Road) loadedVRoad).getSpeedLimit());
        assertEquals(2, ((Road) loadedVRoad).getLength());

        // Check Parking
        GridObject loadedParking = loadedGrid.getAtSpot(6, 6);
        assertTrue(loadedParking instanceof Parking);
        assertEquals(3, ((Parking) loadedParking).getxLength());
        assertEquals(2, ((Parking) loadedParking).getyLength());
        assertEquals(25, ((Parking) loadedParking).getParkingCapacity());
        assertEquals(5, ((Parking) loadedParking).getNumCars());
    }

    /**
     * Test loading a grid with an intersection
     */
    @Test
    public void testLoadGridWithIntersection() {
        // Create a road setup with an intersection
        Road horizontalRoad = new Road(Orientation.HORIZONTAL, 40, false, 0, 5, 4);
        Road verticalRoad = new Road(Orientation.VERTICAL, 35, false, 0, 4, 5);

        grid.addObject(horizontalRoad, 5, 4);
        grid.addObject(verticalRoad, 4, 5);

        // Creating intersection manually to ensure it has the correct connected roads
        Road[] roadList = new Road[4];
        roadList[0] = horizontalRoad;
        roadList[1] = verticalRoad;
        Intersection intersection = new Intersection(5, 5, roadList);
        grid.addObject(intersection, 5, 5);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Create a new grid to load into
        Grid loadedGrid = new Grid(5, 5);

        // Load the grid
        assertTrue(loadedGrid.loadGridState(TEST_FILE));

        // Verify the intersection was loaded with its connected roads
        GridObject loadedObject = loadedGrid.getAtSpot(5, 5);
        assertTrue(loadedObject instanceof Intersection);

        Intersection loadedIntersection = (Intersection) loadedObject;
        Road[] loadedRoads = loadedIntersection.getRoadList();

        // Verify connected roads
        boolean foundHorizontal = false;
        boolean foundVertical = false;

        for (Road road : loadedRoads) {
            if (road != null) {
                if (road.getOrientation() == Orientation.HORIZONTAL) {
                    foundHorizontal = true;
                } else if (road.getOrientation() == Orientation.VERTICAL) {
                    foundVertical = true;
                }
            }
        }

        assertTrue(foundHorizontal, "Horizontal road not connected to intersection after loading");
        assertTrue(foundVertical, "Vertical road not connected to intersection after loading");
    }

    /**
     * Error Checking Test Case: Load from a nonexistent file
     */
    @Test
    public void testLoadGridWithInvalidFile() {
        // Try to load from a non-existent file
        assertFalse(grid.loadGridState("hiimdylanthisfiledoesnotexist.json"));

        // Try to load from an invalid JSON file
        try {
            Files.write(Paths.get(TEST_FILE), "This is not valid JSON".getBytes());
            assertFalse(grid.loadGridState(TEST_FILE));
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * Error Checking Test Case: Try Saving to a location that cannot be written to
     */
    @Test
    public void testSaveGridWithErrorHandling() {
        // Test saving to a non-writable location (if possible)
        // This test might be environment-dependent
        String invalidPath = "/invalid_directory/hiimdylanthislocationdoesnotexist.json";
        assertFalse(grid.saveGridState(invalidPath));
    }

    /**
     * Complex Test Case: Saving and loading a grid with multiple buildings, parking lots, intersections, etc.
     */
    @Test
    public void testComplexGridSaveAndLoad() {
        // Create a more complex grid with multiple objects and intersections
        // Buildings
        grid.addObject(new Building(2, 2, 200), 0, 0);
        grid.addObject(new Building(3, 2, 150), 0, 7);
        grid.addObject(new Building(2, 4, 300), 7, 0);

        // Parking lots
        grid.addObject(new Parking(2, 2, 50, 20), 8, 8);
        grid.addObject(new Parking(3, 1, 30, 15), 1, 5);

        // Roads - creating a small network
        Road road1 = new Road(Orientation.HORIZONTAL, 45, false, 5, 3, 1);
        Road road2 = new Road(Orientation.HORIZONTAL, 45, false, 3, 3, 2);
        Road road3 = new Road(Orientation.HORIZONTAL, 45, false, 2, 3, 3);
        Road road4 = new Road(Orientation.VERTICAL, 35, false, 4, 2, 3);
        Road road5 = new Road(Orientation.VERTICAL, 35, false, 1, 4, 3);

        grid.addObject(road1, 3, 1);
        grid.addObject(road2, 3, 2);
        grid.addObject(road3, 3, 3);
        grid.addObject(road4, 2, 3);
        grid.addObject(road5, 4, 3);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Create a new grid to load into
        Grid loadedGrid = new Grid(5, 5);

        // Load the grid
        assertTrue(loadedGrid.loadGridState(TEST_FILE));

        // Verify dimensions
        assertEquals(10, loadedGrid.getNumRows());
        assertEquals(10, loadedGrid.getNumColumns());

        // Verify a sampling of objects
        // Check a building
        assertTrue(loadedGrid.getAtSpot(0, 0) instanceof Building);
        assertEquals(200, ((Building)loadedGrid.getAtSpot(0, 0)).getDailyPopulation());

        // Check a parking lot
        assertTrue(loadedGrid.getAtSpot(8, 8) instanceof Parking);
        assertEquals(50, ((Parking)loadedGrid.getAtSpot(8, 8)).getParkingCapacity());

        // Check a road
        assertTrue(loadedGrid.getAtSpot(3, 1) instanceof Road);
        assertEquals(Orientation.HORIZONTAL, ((Road)loadedGrid.getAtSpot(3, 1)).getOrientation());
        assertEquals(5, ((Road)loadedGrid.getAtSpot(3, 1)).getInCars());

        // Check if intersection was created and connected properly at (3,3)
        assertTrue(loadedGrid.getAtSpot(3, 3) instanceof Intersection);
        Intersection loadedIntersection = (Intersection) loadedGrid.getAtSpot(3, 3);

        // Count the connected roads to the intersection
        int connectedRoads = 0;
        for (Road road : loadedIntersection.getRoadList()) {
            if (road != null) {
                connectedRoads++;
            }
        }

        // We should have both horizontal and vertical roads connected
        assertTrue(connectedRoads > 0, "No roads connected to intersection after loading");
    }
}