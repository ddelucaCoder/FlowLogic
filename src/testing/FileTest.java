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
     * Test Saving a Grid with a OneWayRoad in it
     */
    @Test
    public void testSaveGridWithOneWayRoad() {
        // Add a one-way road to the grid
        OneWayRoad road = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        road.setSpeedLimit(45);
        road.setLength(5);
        grid.addObject(road, 3, 3);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Verify one-way road was saved correctly
        try {
            String content = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
            JSONObject gridJson = new JSONObject(content);
            JSONArray objects = gridJson.getJSONArray("objects");

            assertEquals(1, objects.length());
            JSONObject roadJson = objects.getJSONObject(0);

            assertEquals(3, roadJson.getInt("row"));
            assertEquals(3, roadJson.getInt("column"));
            assertEquals("OneWayRoad", roadJson.getString("type"));

            JSONObject properties = roadJson.getJSONObject("properties");
            assertEquals("HORIZONTAL", properties.getString("orientation"));
            assertEquals(45, properties.getInt("speedLimit"));
            assertEquals(5, properties.getInt("length"));
            assertFalse(properties.getBoolean("isInRoad"));
            assertEquals(0, properties.getInt("inCars"));
            assertEquals("RIGHT", properties.getString("direction"));
        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * Test Saving a Grid with a TwoWayRoad in it
     */
    @Test
    public void testSaveGridWithTwoWayRoad() {
        // Add a two-way road to the grid
        TwoWayRoad road = new TwoWayRoad(Orientation.HORIZONTAL);
        road.setSpeedLimit(40);
        road.setLength(4);
        grid.addObject(road, 4, 4);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Verify two-way road was saved correctly
        try {
            String content = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
            JSONObject gridJson = new JSONObject(content);
            JSONArray objects = gridJson.getJSONArray("objects");

            assertEquals(1, objects.length());
            JSONObject roadJson = objects.getJSONObject(0);

            assertEquals(4, roadJson.getInt("row"));
            assertEquals(4, roadJson.getInt("column"));
            assertEquals("TwoWayRoad", roadJson.getString("type"));

            JSONObject properties = roadJson.getJSONObject("properties");
            assertEquals("HORIZONTAL", properties.getString("orientation"));
            assertEquals(40, properties.getInt("speedLimit"));
            assertEquals(4, properties.getInt("length"));
            assertFalse(properties.getBoolean("isInRoad"));
            assertEquals(0, properties.getInt("inCars"));

            // Check if left and right roads exist
            assertTrue(properties.has("leftRoad"));
            assertTrue(properties.has("rightRoad"));
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
     * Test Saving a Grid with an intersection connected with OneWayRoads
     */
    @Test
    public void testSaveGridWithIntersection() {
        // Create an intersection with connected one-way roads
        OneWayRoad road1 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        OneWayRoad road2 = new OneWayRoad(Orientation.VERTICAL, Direction.DOWN);

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

        OneWayRoad horizontalRoad = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        horizontalRoad.setSpeedLimit(45);
        horizontalRoad.setLength(3);
        grid.addObject(horizontalRoad, 3, 2);

        OneWayRoad verticalRoad = new OneWayRoad(Orientation.VERTICAL, Direction.DOWN);
        verticalRoad.setSpeedLimit(35);
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
        assertTrue(loadedHRoad instanceof OneWayRoad);
        assertEquals(Orientation.HORIZONTAL, ((OneWayRoad) loadedHRoad).getOrientation());
        assertEquals(45, ((OneWayRoad) loadedHRoad).getSpeedLimit());
        assertEquals(3, ((OneWayRoad) loadedHRoad).getLength());
        assertEquals(Direction.RIGHT, ((OneWayRoad) loadedHRoad).getDirection());

        // Check Vertical Road
        GridObject loadedVRoad = loadedGrid.getAtSpot(2, 3);
        assertTrue(loadedVRoad instanceof OneWayRoad);
        assertEquals(Orientation.VERTICAL, ((OneWayRoad) loadedVRoad).getOrientation());
        assertEquals(35, ((OneWayRoad) loadedVRoad).getSpeedLimit());
        assertEquals(2, ((OneWayRoad) loadedVRoad).getLength());
        assertEquals(Direction.DOWN, ((OneWayRoad) loadedVRoad).getDirection());

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
        OneWayRoad horizontalRoad = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        horizontalRoad.setSpeedLimit(40);
        grid.addObject(horizontalRoad, 5, 4);

        OneWayRoad verticalRoad = new OneWayRoad(Orientation.VERTICAL, Direction.DOWN);
        verticalRoad.setSpeedLimit(35);
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
        OneWayRoad road1 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        road1.setSpeedLimit(45);
        road1.setInCars(5);

        OneWayRoad road2 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        road2.setSpeedLimit(45);
        road2.setInCars(3);

        OneWayRoad road3 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        road3.setSpeedLimit(45);
        road3.setInCars(2);

        OneWayRoad road4 = new OneWayRoad(Orientation.VERTICAL, Direction.DOWN);
        road4.setSpeedLimit(35);
        road4.setInCars(4);

        OneWayRoad road5 = new OneWayRoad(Orientation.VERTICAL, Direction.DOWN);
        road5.setSpeedLimit(35);
        road5.setInCars(1);

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
        assertTrue(loadedGrid.getAtSpot(3, 1) instanceof OneWayRoad);
        assertEquals(Orientation.HORIZONTAL, ((OneWayRoad)loadedGrid.getAtSpot(3, 1)).getOrientation());
        assertEquals(5, ((OneWayRoad)loadedGrid.getAtSpot(3, 1)).getInCars());
        assertEquals(Direction.RIGHT, ((OneWayRoad)loadedGrid.getAtSpot(3, 1)).getDirection());

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

    /**
     * Test Saving a Grid with a StopSign in it
     */
    @Test
    public void testSaveGridWithStopSign() {
        // Create roads for the stop sign
        OneWayRoad road1 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        OneWayRoad road2 = new OneWayRoad(Orientation.VERTICAL, Direction.DOWN);
        Road[] roads = {road1, road2, null, null};

        // Add a stop sign to the grid
        StopSign stopSign = new StopSign(3, 3, roads);
        grid.addObject(stopSign, 3, 3);

        // Add the roads to the grid too
        grid.addObject(road1, 3, 2);
        grid.addObject(road2, 2, 3);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Verify stop sign was saved correctly
        try {
            String content = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
            JSONObject gridJson = new JSONObject(content);
            JSONArray objects = gridJson.getJSONArray("objects");

            // Find the StopSign in the saved objects
            JSONObject stopSignJson = null;
            for (int i = 0; i < objects.length(); i++) {
                JSONObject obj = objects.getJSONObject(i);
                if ("StopSign".equals(obj.getString("type")) &&
                        obj.getInt("row") == 3 && obj.getInt("column") == 3) {
                    stopSignJson = obj;
                    break;
                }
            }

            assertNotNull(stopSignJson, "StopSign not found in saved data");

            // Check properties
            JSONObject properties = stopSignJson.getJSONObject("properties");
            assertTrue(properties.has("connectedRoads"));

            // Verify connected roads
            JSONArray connectedRoads = properties.getJSONArray("connectedRoads");
            assertTrue(connectedRoads.length() > 0);

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * Test Saving a Grid with a StopLight in it
     */
    @Test
    public void testSaveGridWithStopLight() {
        // Create roads for the stop light
        Road roadOne = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        Road roadTwo = new OneWayRoad(Orientation.VERTICAL, Direction.DOWN);
        Road[] roads = {roadOne, roadTwo, null, null};

        // Add a stop light to the grid - using integer values 0 for RED, 2 for GREEN
        StopLight stopLight = new StopLight(roadOne, roadTwo, 20, 15, 0, 2, roads, 4, 4);
        stopLight.initializeStopLightGraphics();
        grid.addObject(stopLight, 4, 4);

        // Add the roads to the grid too
        grid.addObject(roadOne, 4, 3);
        grid.addObject(roadTwo, 3, 4);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Verify stop light was saved correctly
        try {
            String content = new String(Files.readAllBytes(Paths.get(TEST_FILE)));
            JSONObject gridJson = new JSONObject(content);
            JSONArray objects = gridJson.getJSONArray("objects");

            // Find the StopLight in the saved objects
            JSONObject stopLightJson = null;
            for (int i = 0; i < objects.length(); i++) {
                JSONObject obj = objects.getJSONObject(i);
                if ("StopLight".equals(obj.getString("type")) &&
                        obj.getInt("row") == 4 && obj.getInt("column") == 4) {
                    stopLightJson = obj;
                    break;
                }
            }

            assertNotNull(stopLightJson, "StopLight not found in saved data");

            // Check properties
            JSONObject properties = stopLightJson.getJSONObject("properties");
            assertTrue(properties.has("connectedRoads"));
            assertEquals(20, properties.getInt("timingOne"));
            assertEquals(15, properties.getInt("timingTwo"));
            assertEquals(0, properties.getInt("lightOneColor")); // 0 = RED
            assertEquals(2, properties.getInt("lightTwoColor")); // 2 = GREEN

            // Verify connected roads
            JSONArray connectedRoads = properties.getJSONArray("connectedRoads");
            assertTrue(connectedRoads.length() > 0);

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        }
    }

    /**
     * Test Loading a Grid with a StopSign in it
     */
    @Test
    public void testLoadGridWithStopSign() {
        // Create roads for the stop sign
        OneWayRoad road1 = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        OneWayRoad road2 = new OneWayRoad(Orientation.VERTICAL, Direction.DOWN);
        Road[] roads = {road1, road2, null, null};

        // Add a stop sign to the grid
        StopSign stopSign = new StopSign(3, 3, roads);
        grid.addObject(stopSign, 3, 3);

        // Add the roads to the grid too
        grid.addObject(road1, 3, 2);
        grid.addObject(road2, 2, 3);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Create a new grid to load into
        Grid loadedGrid = new Grid(5, 5);

        // Load the grid
        assertTrue(loadedGrid.loadGridState(TEST_FILE));

        // Verify the stop sign was loaded correctly
        GridObject loadedObject = loadedGrid.getAtSpot(3, 3);
        assertTrue(loadedObject instanceof StopSign);

        // Check connected roads
        StopSign loadedStopSign = (StopSign) loadedObject;
        Road[] loadedRoads = loadedStopSign.getRoadList();

        // Verify at least one road is connected
        boolean hasConnectedRoad = false;
        for (Road road : loadedRoads) {
            if (road != null) {
                hasConnectedRoad = true;
                break;
            }
        }

        assertTrue(hasConnectedRoad, "No roads connected to StopSign after loading");
    }

    /**
     * Test Loading a Grid with a StopLight in it
     */
    @Test
    public void testLoadGridWithStopLight() {
        // Create roads for the stop light
        Road roadOne = new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT);
        Road roadTwo = new OneWayRoad(Orientation.VERTICAL, Direction.DOWN);
        Road[] roads = {roadOne, roadTwo, null, null};

        // Add a stop light to the grid - using integer values 0 for RED, 2 for GREEN
        StopLight stopLight = new StopLight(roadOne, roadTwo, 20, 15, 0, 2, roads, 4, 4);
        stopLight.initializeStopLightGraphics();
        grid.addObject(stopLight, 4, 4);

        // Add the roads to the grid too
        grid.addObject(roadOne, 4, 3);
        grid.addObject(roadTwo, 3, 4);

        // Save the grid
        assertTrue(grid.saveGridState(TEST_FILE));

        // Create a new grid to load into
        Grid loadedGrid = new Grid(5, 5);

        // Load the grid
        assertTrue(loadedGrid.loadGridState(TEST_FILE));

        // Verify the stop light was loaded correctly
        GridObject loadedObject = loadedGrid.getAtSpot(4, 4);
        assertTrue(loadedObject instanceof StopLight);

        // Check properties
        StopLight loadedStopLight = (StopLight) loadedObject;
        assertEquals(20, loadedStopLight.getTimingOne());
        assertEquals(15, loadedStopLight.getTimingTwo());
        assertEquals(0, loadedStopLight.getLightOneColor()); // 0 = RED
        assertEquals(2, loadedStopLight.getLightTwoColor()); // 2 = GREEN

        // Check connected roads
        Road[] loadedRoads = loadedStopLight.getRoadList();

        // Verify at least one road is connected
        boolean hasConnectedRoad = false;
        for (Road road : loadedRoads) {
            if (road != null) {
                hasConnectedRoad = true;
                break;
            }
        }

        assertTrue(hasConnectedRoad, "No roads connected to StopLight after loading");
    }

    /**
     * Test renaming a saved grid file
     */
    @Test
    public void testRenameSaveFile() {
        // First create and save a grid to our test file
        Building building = new Building(2, 3, 100);
        grid.addObject(building, 1, 1);
        assertTrue(grid.saveGridState(TEST_FILE));

        // Verify original file exists
        File originalFile = new File(TEST_FILE);
        assertTrue(originalFile.exists());

        // Create the new filename
        String newFileName = "renamed_test_grid.json";
        File newFile = new File(newFileName);

        // Delete the new file if it already exists from a previous test
        if (newFile.exists()) {
            newFile.delete();
        }

        // Rename the file
        assertTrue(originalFile.renameTo(newFile));

        // Verify the original file no longer exists
        assertFalse(originalFile.exists());

        // Verify the new file exists
        assertTrue(newFile.exists());

        // Load from the renamed file to verify it contains the correct data
        Grid loadedGrid = new Grid(5, 5);
        assertTrue(loadedGrid.loadGridState(newFileName));

        // Verify the loaded grid has our building
        GridObject loadedBuilding = loadedGrid.getAtSpot(1, 1);
        assertTrue(loadedBuilding instanceof Building);
        assertEquals(2, ((Building) loadedBuilding).getxLength());
        assertEquals(3, ((Building) loadedBuilding).getyLength());
        assertEquals(100, ((Building) loadedBuilding).getDailyPopulation());

        // Clean up the renamed file
        newFile.delete();
    }

    /**
     * Test deleting a saved grid file
     */
    @Test
    public void testDeleteSaveFile() {
        // First create and save a grid to our test file
        Building building = new Building(2, 3, 100);
        grid.addObject(building, 1, 1);
        assertTrue(grid.saveGridState(TEST_FILE));

        // Verify file exists
        File file = new File(TEST_FILE);
        assertTrue(file.exists());

        // Delete the file
        assertTrue(file.delete());

        // Verify file no longer exists
        assertFalse(file.exists());

        // Try to load from the deleted file and verify it fails
        Grid loadedGrid = new Grid(5, 5);
        assertFalse(loadedGrid.loadGridState(TEST_FILE));

        // Verify the dimensions of the loaded grid remain unchanged
        // (since the load operation failed)
        assertEquals(5, loadedGrid.getNumRows());
        assertEquals(5, loadedGrid.getNumColumns());
    }
}