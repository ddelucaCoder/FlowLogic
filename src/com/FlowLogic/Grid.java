package com.FlowLogic;

import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.FlowLogic.Direction.*;

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

    public static int GRID_SIZE = 32;

    private int numObjs = 0;


    ArrayList<GridObject> intersections;

    private final int MAX_SPEED_LIMIT = 100;

    boolean testingMode = false;


    //Allows for quick conversion from Image file to backend object
    public static HashMap<String, GridObject> imgToObj = new HashMap<>();

    public Grid(int numRows, int numColumns) {
        this.numRows = numRows;
        this.numColumns = numColumns;
        grid = new GridObject[numRows][numColumns];
        frontGrid = new Rectangle[numRows][numColumns];
        populateMap();
    }

    private void populateMap() {
        imgToObj.put("RoadImageDown.png", new OneWayRoad(Orientation.VERTICAL, Direction.DOWN));
        imgToObj.put("RoadImage.png", new OneWayRoad(Orientation.VERTICAL, UP));
        imgToObj.put("RoadImageLeft.png", new OneWayRoad(Orientation.HORIZONTAL, Direction.LEFT));
        imgToObj.put("RoadImageRight.png", new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT));
        imgToObj.put("BasicIntersection.png", new Intersection(0, 0, new Road[4]));
        imgToObj.put("ParkingLot.png", new Parking());
        imgToObj.put("YellowBuilding.png", new Building("yellow"));
        imgToObj.put("GreenBuilding.png", new Building("green"));
        imgToObj.put("RedBuilding.png", new Building("red"));
        imgToObj.put("TwoWayRoadRight.png", new TwoWayRoad(Orientation.HORIZONTAL));
        imgToObj.put("TwoWayRoad.png", new TwoWayRoad(Orientation.VERTICAL));
        imgToObj.put("4WayStopSign.png", new StopSign(0, 0, new Road[4]));
        imgToObj.put("AllRed4WayStopLight.png", new StopLight(null, null, 0, 0, 0, 0, new Road[4], 0, 0));
        imgToObj.put("roundabout.png", new Roundabout(new Boolean[4], 0 ,0, null));
        imgToObj.put("YellowRed4WayStopLight.png", new StopLight(null, null, 0, 0, 0, 0, new Road[4], 0, 0));
        imgToObj.put("RedYellow4WayStopLight.png", new StopLight(null, null, 0, 0, 0, 0, new Road[4], 0, 0));
        imgToObj.put("GreenRed4WayStopLight.png", new StopLight(null, null, 0, 0, 0, 0, new Road[4], 0, 0));
        imgToObj.put("RedGreen4WayStopLight.png", new StopLight(null, null, 0, 0, 0, 0, new Road[4], 0, 0));
        imgToObj.put("Hazard.png", new Hazard(0 ,0));
    }

    /**
     * This method loads a grid state from a JSON file.
     * The method will parse the JSON structure to recreate the grid dimensions and objects.
     *
     * @param filename The name of the file to load the grid state from
     * @return boolean indicating if the load was successful
     */
    public boolean loadGridState(String filename) {
        try {
            // Read in the JSON file content as a large string
            String content = new String(Files.readAllBytes(Paths.get(filename)));
            JSONObject gridJson = new JSONObject(content);

            // Load grid dimensions
            this.numRows = gridJson.getInt("numRows");
            this.numColumns = gridJson.getInt("numColumns");
            GRID_SIZE = gridJson.getInt("gridSize");

            // Initialize a new grid with the loaded dimensions
            this.grid = new GridObject[numRows][numColumns];
            this.frontGrid = new Rectangle[numRows][numColumns];

            // Load objects from JSON
            JSONArray gridObjectsArray = gridJson.getJSONArray("objects");

            // First Loop: Create all of the objects.
            //             Don't connect anything just yet
            for (int i = 0; i < gridObjectsArray.length(); i++) {
                JSONObject cellJson = gridObjectsArray.getJSONObject(i);
                int row = cellJson.getInt("row");
                int col = cellJson.getInt("column");
                String type = cellJson.getString("type");
                JSONObject properties = cellJson.getJSONObject("properties");
                GridObject gridObject = null;

                // Reconstruct the object based on its type in the file
                // Determine the type, then fill out the necessary object fields
                switch (type) {
                    case "OneWayRoad":
                        Orientation orientation = Orientation.valueOf(properties.getString("orientation"));
                        int speedLimit = properties.getInt("speedLimit");
                        int length = properties.getInt("length");
                        boolean isInRoad = properties.getBoolean("isInRoad");
                        int inCars = properties.getInt("inCars");
                        Direction direction = Direction.valueOf(properties.getString("direction"));
                        int numLanes = properties.getInt("numLanes");
                        String name = properties.getString("name");
                        ArrayList<Vehicle> vehicleList = new ArrayList<>();

                        /* Future Change: Check for saved vehicles. For now, not necessary
                        // Check if there are saved vehicles
                        if (properties.has("vehicleList")) {
                            JSONArray vehiclesArray = properties.getJSONArray("vehicleList");
                            for (int j = 0; j < vehiclesArray.length(); j++) {
                                // Insert vehicle parsing here
                            }
                        }
                        */

                        OneWayRoad oneWayRoad = new OneWayRoad(orientation, speedLimit, isInRoad, inCars, row, col, direction,
                                numLanes, vehicleList);
                        oneWayRoad.setLength(length);
                        oneWayRoad.setName(name);

                        gridObject = oneWayRoad;
                        break;
                    case "TwoWayRoad":
                        Orientation twoWayOrientation = Orientation.valueOf(properties.getString("orientation"));
                        int twoWaySpeedLimit = properties.getInt("speedLimit");
                        boolean twoWayIsInRoad = properties.getBoolean("isInRoad");
                        int twoWayInCars = properties.getInt("inCars");
                        String twoWayName = properties.getString("name");

                        // Create left and right one-way roads
                        OneWayRoad leftRoad;
                        OneWayRoad rightRoad;

                        if (properties.has("left") && properties.has("right")) {
                            JSONObject leftObject = properties.getJSONObject("left");
                            JSONObject rightObject = properties.getJSONObject("right");

                            Direction leftDirection = Direction.valueOf(leftObject.getString("direction"));
                            Direction rightDirection = Direction.valueOf(rightObject.getString("direction"));

                            leftRoad = new OneWayRoad(twoWayOrientation, leftDirection);
                            rightRoad = new OneWayRoad(twoWayOrientation, rightDirection);
                        }
                        else {
                            // Set Directions based on their orientation
                            if (twoWayOrientation == Orientation.VERTICAL) {
                                leftRoad = new OneWayRoad(twoWayOrientation, Direction.DOWN);
                                rightRoad = new OneWayRoad(twoWayOrientation, UP);
                            } else {
                                leftRoad = new OneWayRoad(twoWayOrientation, Direction.LEFT);
                                rightRoad = new OneWayRoad(twoWayOrientation, Direction.RIGHT);
                            }
                        }

                        TwoWayRoad twoWayRoad = new TwoWayRoad(twoWayOrientation, twoWaySpeedLimit, twoWayIsInRoad,
                                twoWayInCars, row, col, leftRoad, rightRoad);
                        twoWayRoad.setName(twoWayName);

                        gridObject = twoWayRoad;
                        break;
                    case "Building":
                        int xLength = properties.getInt("xLength");
                        int yLength = properties.getInt("yLength");
                        int dailyPopulation = properties.getInt("dailyPopulation");
                        String color = properties.getString("color");
                        String buildingName = properties.getString("name");

                        Building building = new Building(xLength, yLength, dailyPopulation);
                        building.setRowNum(row);
                        building.setColNum(col);
                        building.setColor(color);
                        building.setName(buildingName);

                        gridObject = building;
                        break;
                    case "Parking":
                        int parkingXLength = properties.getInt("xLength");
                        int parkingYLength = properties.getInt("yLength");
                        int parkingCapacity = properties.getInt("parkingCapacity");
                        int numCars = properties.getInt("numCars");

                        Parking parking = new Parking(parkingXLength, parkingYLength, parkingCapacity, numCars);
                        parking.setRowNum(row);
                        parking.setColNum(col);

                        gridObject = parking;
                        break;
                    case "Intersection":
                        // Roads will be connected during the second pass
                        Intersection intersection = new Intersection(row, col, new Road[4]);
                        gridObject = intersection;
                        break;
                    case "StopSign":
                        // Create a stop sign with empty road list
                        // Roads will be connected during the second pass
                        StopSign stopSign = new StopSign(row, col, new Road[4]);
                        gridObject = stopSign;
                        break;
                    case "StopLight":
                        // Get stoplight properties
                        int timingOne = properties.getInt("timingOne");
                        int timingTwo = properties.getInt("timingTwo");
                        int lightOneColor = properties.getInt("lightOneColor");
                        int lightTwoColor = properties.getInt("lightTwoColor");

                        // Create stoplight with empty road list
                        // Roads will be connected during the second pass
                        StopLight stopLight = new StopLight(null, null, timingOne, timingTwo,
                                lightOneColor, lightTwoColor, new Road[4], row, col);
                        gridObject = stopLight;
                        break;
                    case "Roundabout":
                        Roundabout roundabout = new Roundabout(new Boolean[4], row, col, new Road[4]);
                        gridObject = roundabout;
                        break;
                        //idk
                    case "Hazard":
                        Hazard hazard = new Hazard(row, col);
                        orientation = Orientation.valueOf(properties.getString("orientation"));
                        speedLimit = properties.getInt("speedLimit");
                        length = properties.getInt("length");
                        isInRoad = properties.getBoolean("isInRoad");
                        inCars = properties.getInt("inCars");
                        direction = Direction.valueOf(properties.getString("direction"));
                        numLanes = properties.getInt("numLanes");
                        name = properties.getString("name");
                        vehicleList = new ArrayList<>();
                        OneWayRoad hazardRoad = new OneWayRoad(orientation, speedLimit, isInRoad, inCars, row, col, direction,
                                numLanes, vehicleList);
                        hazardRoad.setLength(length);
                        hazardRoad.setName(name);
                        hazard.setCoveredObject(hazardRoad);
                        gridObject = hazard;
                        break;
                }
                // Add everything to the grid
                if (gridObject != null) {
                    grid[row][col] = gridObject;
                    if (gridObject instanceof OneWayRoad) {
                        mergeRoads(row,col);
                    }
                }
            }
            // Second loop: Connect the roads to all forms of intersections
            for (int i = 0; i < gridObjectsArray.length(); i++) {
                JSONObject objJson = gridObjectsArray.getJSONObject(i);
                String objType = objJson.getString("type");

                // Handle both Intersection, StopSign, Stoplight objects
                if ("Intersection".equals(objType) || "StopSign".equals(objType) || "StopLight".equals(objType)) {
                    int row = objJson.getInt("row");
                    int col = objJson.getInt("column");
                    JSONObject properties = objJson.getJSONObject("properties");
                    JSONArray connectedRoads = properties.getJSONArray("connectedRoads");

                    // Get the appropriate object (either Intersection, StopSign, Stoplight)
                    GridObject trafficController = grid[row][col];

                    // Connect each road to the intersection or stop sign or stop light
                    for (int j = 0; j < connectedRoads.length(); j++) {
                        JSONObject roadRef = connectedRoads.getJSONObject(j);
                        int roadRow = roadRef.getInt("row");
                        int roadCol = roadRef.getInt("column");

                        // Find road from the grid
                        if (roadRow >= 0 && roadRow < numRows && roadCol >= 0 && roadCol < numColumns) {
                            GridObject obj = grid[roadRow][roadCol];
                            if (obj instanceof Road) {
                                if (trafficController instanceof StopSign) {
                                    ((StopSign) trafficController).addRoad((Road) obj);
                                } else if (trafficController instanceof StopLight) {
                                    ((StopLight) trafficController).addRoad((Road) obj);
                                } else if (trafficController instanceof  Intersection) {
                                    ((Intersection) trafficController).addRoad((Road) obj);
                                }
                            }
                        }
                    }
                }
            }
            if (!testingMode) {
                UserInterface.refreshGrid(numRows);
                synchronizeGrid();
            }
            System.out.println("Successfully loaded grid from " + filename);
            return true;
        }
        catch (Exception e){
            // Insert additional error logic here if needed
            System.out.println("Error loading grid: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * This method saves the current grid state to a JSON file.
     * The JSON structure contains the grid dimensions and all grid objects.
     *
     * @param filename The name of the file to save the grid state to
     * @return boolean indicating if the save was successful
     */
    public boolean saveGridState(String filename) {
        JSONObject gridJson = new JSONObject();

        // Save the grid dimensions
        gridJson.put("numRows", this.numRows);
        gridJson.put("numColumns", this.numColumns);
        gridJson.put("gridSize", GRID_SIZE);

        // Create an array for the objects
        JSONArray gridObjectsArray = new JSONArray();

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                GridObject obj = grid[row][col];
                if (obj != null) {
                    JSONObject cellJson = new JSONObject();
                    cellJson.put("row", row);
                    cellJson.put("column", col);
                    cellJson.put("type", obj.getClass().getSimpleName());

                    // Store type-specific properties
                    JSONObject properties = new JSONObject();

                    // Add different properties based on object type
                    if (obj instanceof OneWayRoad road) {
                        properties.put("orientation", road.getOrientation());
                        properties.put("speedLimit", road.getSpeedLimit());
                        properties.put("length", road.getLength());
                        properties.put("isInRoad", road.isInRoad());
                        properties.put("inCars", road.getInCars());
                        properties.put("direction", road.getDirection());
                        properties.put("numLanes", road.getNumLanes());
                        properties.put("vehicleList", road.getVehicleList());
                        properties.put("name", road.getName());
                    }
                    else if (obj instanceof TwoWayRoad road) {
                        properties.put("orientation", road.getOrientation());
                        properties.put("speedLimit", road.getSpeedLimit());
                        properties.put("isInRoad", road.isInRoad());
                        properties.put("inCars", road.getInCars());

                        // Save left and right one-way roads
                        JSONObject leftRoadJson = new JSONObject();
                        if (road.getLeft() != null) {
                            leftRoadJson.put("direction", road.getLeft().getDirection().toString());
                            leftRoadJson.put("numLanes", road.getLeft().getNumLanes());
                        }
                        properties.put("left", leftRoadJson);

                        JSONObject rightRoadJson = new JSONObject();
                        if (road.getRight() != null) {
                            rightRoadJson.put("direction", road.getRight().getDirection().toString());
                            rightRoadJson.put("numLanes", road.getRight().getNumLanes());
                        }
                        properties.put("right", rightRoadJson);

                        properties.put("name", road.getName());
                    }
                    else if (obj instanceof StopSign stopSign) {
                        // Save the connected roads as an array of references
                        JSONArray connectedRoads = new JSONArray();
                        Road[] roadList = stopSign.getRoadList();

                        if (roadList != null) {
                            for (Road road : roadList) {
                                if (road != null) {
                                    JSONObject roadRef = new JSONObject();
                                    roadRef.put("row", road.getRowNum());
                                    roadRef.put("column", road.getColNum());
                                    roadRef.put("orientation", road.getOrientation().toString());
                                    connectedRoads.put(roadRef);
                                }
                            }
                        }
                        properties.put("connectedRoads", connectedRoads);
                    }
                    else if (obj instanceof StopLight stopLight) {
                        // Save the connected roads as an array of references
                        JSONArray connectedRoads = new JSONArray();
                        Road[] roadList = stopLight.getRoadList();

                        if (roadList != null) {
                            for (Road road : roadList) {
                                if (road != null) {
                                    JSONObject roadRef = new JSONObject();
                                    roadRef.put("row", road.getRowNum());
                                    roadRef.put("column", road.getColNum());
                                    roadRef.put("orientation", road.getOrientation().toString());
                                    connectedRoads.put(roadRef);
                                }
                            }
                        }
                        properties.put("connectedRoads", connectedRoads);

                        // Save stoplight-specific properties
                        properties.put("timingOne", stopLight.getTimingOne());
                        properties.put("timingTwo", stopLight.getTimingTwo());
                        properties.put("lightOneColor", stopLight.getLightOneColor());
                        properties.put("lightTwoColor", stopLight.getLightTwoColor());
                    }
                    else if (obj instanceof Intersection intersection) {
                        // Save the connected roads as an array of references
                        JSONArray connectedRoads = new JSONArray();
                        Road[] roadList = intersection.getRoadList();

                        if (roadList != null) {
                            for (Road road : roadList) {
                                if (road != null) {
                                    JSONObject roadRef = new JSONObject();
                                    roadRef.put("row", road.getRowNum());
                                    roadRef.put("column", road.getColNum());
                                    roadRef.put("orientation", road.getOrientation());
                                    connectedRoads.put(roadRef);
                                }
                            }
                        }
                        properties.put("connectedRoads", connectedRoads);
                    }
                    else if (obj instanceof Building building) {
                        properties.put("xLength", building.getxLength());
                        properties.put("yLength", building.getyLength());
                        properties.put("dailyPopulation", building.getDailyPopulation());
                        properties.put("color", building.getColor());
                        properties.put("name", building.getName());
                    }
                    else if (obj instanceof Parking parking) {
                        properties.put("xLength", parking.getxLength());
                        properties.put("yLength", parking.getyLength());
                        properties.put("parkingCapacity", parking.getParkingCapacity());
                        properties.put("numCars", parking.getNumCars());
                    }
                    else if (obj instanceof Hazard hazard) {
                        OneWayRoad road = (OneWayRoad) hazard.getCoveredObject();
                        properties.put("orientation", road.getOrientation());
                        properties.put("speedLimit", road.getSpeedLimit());
                        properties.put("length", road.getLength());
                        properties.put("isInRoad", road.isInRoad());
                        properties.put("inCars", road.getInCars());
                        properties.put("direction", road.getDirection());
                        properties.put("numLanes", road.getNumLanes());
                        properties.put("vehicleList", road.getVehicleList());
                        properties.put("name", road.getName());
                    }

                    cellJson.put("properties", properties);
                    gridObjectsArray.put(cellJson);
                }
            }
        }

        gridJson.put("objects", gridObjectsArray);

        // Write to file
        try (FileWriter file = new FileWriter(filename)) {
            file.write(gridJson.toString());
            file.flush();
            System.out.println("Successfully saved grid to " + filename);
            return true;
        } catch (IOException e) {
            // Insert additional error logic here if needed
            System.out.println("Error saving grid to file: " + e.getMessage());
            return false;
        }

    }


    /**
     * This function is called from the frontend when an image object is dropped onto the grid. It's purpose is to
     * mimic that same action on the backend with the correct object.
     * @param imageFile - the name of the image file dropped
     * @param rowNum - row number where it was dropped
     * @param colNum - the col number where it was dropped
     */
    public void placeObjectByImage(String imageFile, int rowNum, int colNum) {
        GridObject newObject = imgToObj.get(imageFile);
        System.out.println(imageFile);
        addObject(newObject.clone(), rowNum, colNum);
    }

    /**
     * This function checks an image file to see if it is a two-way road
     */
    public boolean isTwoWayRoad(String imageFile) {
        return switch (imageFile) {
            case "TwoWayRoad.png", "TwoWayRoadRight.png" -> true;
            default -> false;
        };
    }

    /**
     * This function detects a hazard
     * @param imageFile
     * @return
     */
    public boolean isHazard(String imageFile) {
        return Objects.equals(imageFile, "Hazard.png");
    }

    /**
     * This function is used to find and merge lanes that are next to the road in the location defined
     * by the coordinates. This returns the multilaneConnect object that all of the roads next to each
     * other will be contained by
     *
     * @param rowNum
     * @param colNum
     */
    public void mergeRoads(int rowNum, int colNum) {
        // get road from spot and create multilane for it
        GridObject obj = getAtSpot(rowNum, colNum);
        Road mainRoad;
        MultiLaneConnect container;
        if (obj instanceof Road) {
            mainRoad = (Road) obj;
        } else {
           // System.out.println("mergeRoads error: No road here to merge\n");
            return;
        }
        if (mainRoad.getLaneContainer() != null) {
            container = mainRoad.getLaneContainer();
        } else {
            container = new MultiLaneConnect();
            mainRoad.setLaneContainer(container);
            container.addRoadToList(mainRoad);
        }
        // get roads around it in same direction and add to multilane
        // check all 4 directions around
        if (!(mainRoad instanceof OneWayRoad oneRoad)) {
            System.out.println("mergeRoads error: Not a one way road\n");
            return;
        }
        Direction oneDir = oneRoad.getDirection();
        if (oneDir == UP || oneDir == Direction.DOWN) {
            // check up and down obj to see if it is a road
            obj = getAtSpot(rowNum, colNum + 1);
            mergeHelper(obj, container, oneDir);
            obj = getAtSpot(rowNum, colNum - 1);
            mergeHelper(obj, container, oneDir);
        } else if (oneDir == Direction.RIGHT || oneDir == Direction.LEFT) {
            obj = getAtSpot(rowNum + 1, colNum);
            mergeHelper(obj, container, oneDir);
            obj = getAtSpot(rowNum - 1, colNum);
            mergeHelper(obj, container, oneDir);
        }
    }

    /**
     * This is a helper function that checks if the obj direction
     * @param obj
     * @param container
     * @param oneDir
     */
    private void mergeHelper(GridObject obj, MultiLaneConnect container, Direction oneDir) {
        if (obj instanceof OneWayRoad checkRoad) {
            if (checkRoad.getDirection() == oneDir) {
                // add to multi lane
                // if multilane already exists, move all roads into new multilane
                if (checkRoad.getLaneContainer() != null) {
                    List<Road> lanes = checkRoad.getLaneContainer().getLaneList();
                    for (Road road : lanes) {
                        road.setLaneContainer(container);
                        container.addRoadToList(road);
                    }
                } else {
                    // not in a multilane yet
                    checkRoad.setLaneContainer(container);
                    container.addRoadToList(checkRoad);
                }
            }
        }
    }

    /**
     * This function is called from the frontend when the user clicks on a square on the grid. It goes through the
     * options of each type of thing that could be clicked and does the appropriate action.
     * for each type of
     * @param row - row to select at
     * @param col - col to select at
     * @param optionLayout - the layout window to be further passed
     */

    public void select(int row, int col, VBox optionLayout) {
        GridObject obj = getAtSpot(row, col);
        if (obj instanceof Building) {
            Building b = (Building) obj;
            UserInterface.showBuildingOptions(optionLayout, this, b.getxLength(), b.getyLength(),
                b.getDailyPopulation(), row, col);
        } else if (obj instanceof Parking) {
            Parking p = (Parking) obj;
            UserInterface.showParkingOptions(optionLayout, this, p.getxLength(), p.getyLength(),
                p.getParkingCapacity(), row, col);
        } else if (obj instanceof Road) {
            if (UserInterface.isEntireRoadSelectionEnabled()) {
                // Get all connected road tiles
                Set<int[]> connectedRoads = getConnectedRoadTiles(row, col);
                UserInterface.showRoadOptions(optionLayout, this, row, col, connectedRoads);
            }
            else {
                UserInterface.showRoadOptions(optionLayout, this, row, col);
            }
        }  else if (obj instanceof StopLight) {
            UserInterface.showTrafficLightOptions(optionLayout, this, row, col);
        } else if (obj instanceof Hazard) {
            UserInterface.showHazardOptions(optionLayout, this, row, col);
        } else if (obj instanceof  Intersection) {
            UserInterface.showIntersectionOptions(optionLayout, this, row, col);
        }
    }

    /**
     * Function to find all connected road tiles that have the same direction and orientation
     * Used to select the entirety of a road.
     * @param row Starting row coordinate
     * @param col Starting column coordinate
     * @return set of coordinates [row, col] of all connected matching road tiles
     */
    public Set<int[]> getConnectedRoadTiles(int row, int col) {
        GridObject startObject = getAtSpot(row, col);

        if (!(startObject instanceof Road)) {
            return new HashSet<>(); // Not a road, return
        }

        Road startRoad = (Road) startObject;
        Orientation roadOrientation = startRoad.getOrientation();
        Direction roadDirection = null;

        // Get direction for one-way roads
        if (startRoad instanceof OneWayRoad) {
            roadDirection = ((OneWayRoad) startRoad).getDirection();
        }

        // Set to store visited coordinates
        Set<int[]> visited = new HashSet<>();
        // Queue for BFS traversal
        Queue<int[]> queue = new LinkedList<>();

        // Start from the provided position
        queue.add(new int[]{row, col});

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int currentRow = current[0];
            int currentCol = current[1];

            // Skip the tile if it is already visited or goes out of range of the current grid
            if (currentRow < 0 ||
                    currentRow >= numRows ||
                    currentCol < 0 ||
                    currentCol >= numColumns ||
                    contains(visited, current)) {
                continue;
            }

            GridObject obj = getAtSpot(currentRow, currentCol);

            // Check if this is a matching road
            if (obj instanceof Road road) {

                // Check if orientation matches
                if (road.getOrientation() != roadOrientation) {
                    continue;
                }

                // For one-way roads, check if direction matches
                if (roadDirection != null && road instanceof OneWayRoad &&
                        ((OneWayRoad) road).getDirection() != roadDirection) {
                    continue;
                }

                // Add to visited
                visited.add(current);

                // Add adjacent tiles to queue
                queue.add(new int[]{currentRow - 1, currentCol}); // Up
                queue.add(new int[]{currentRow + 1, currentCol}); // Down
                queue.add(new int[]{currentRow, currentCol - 1}); // Left
                queue.add(new int[]{currentRow, currentCol + 1}); // Right
            }
        }
        return visited;
    }

    /**
     * Helper method to check if a set of coordinates contains a specific coordinate
     */
    private boolean contains(Set<int[]> set, int[] coord) {
        for (int[] item : set) {
            if (item[0] == coord[0] && item[1] == coord[1]) {
                return true;
            }
        }
        return false;
    }

    /**
     * This function recursively removes all parts of the object at (row, col), even if it extends into other squares
     * @param row - the row we want to delete at
     * @param col - the col we want to delete at
     */

    public void remove(int row, int col) {
        GridObject obj = getAtSpot(row, col);
        if (obj == null) {
            return;
        }
        grid[row][col] = null;
        frontGrid[row][col] = null;
        if (obj == getAtSpot(row - 1, col)) {
            remove(row - 1, col);
        }
        if (obj == getAtSpot(row + 1, col)) {
            remove(row + 1, col);
        }
        if (obj == getAtSpot(row, col - 1)) {
            remove(row, col - 1);
        }
        if (obj == getAtSpot(row, col + 1)) {
            remove(row, col + 1);
        }
    }

    /**
     * This changes the population of the building at (row, col)
     * @param row - the row of the building to change
     * @param col - the col of the building to change
     * @param newPop - the new population to set
     */
    public void changeDailyPopulationBuilding(int row, int col, int newPop) {
        GridObject obj = getAtSpot(row, col);
        if (!(obj instanceof Building)) {
            return;
        }
        ((Building) obj).setDailyPopulation(newPop);
    }

    /**
     * this changes the parking capacity at the parking lot at (row, col)
     * @param row - row of the parking building to change
     * @param col - col of the parking building to change
     * @param newCap - the new capacity for the parking lot
     */
    public void changeParkingCapacity(int row, int col, int newCap) {
        GridObject obj = getAtSpot(row, col);
        if (!(obj instanceof Parking)) {
            return;
        }
        ((Parking) obj).setParkingCapacity(newCap);
    }

    public static int[] getRealCoords(GridObject g) {
        int row = g.getRowNum();
        int col = g.getColNum();

        return new int[]{row * GRID_SIZE, col * GRID_SIZE};
    }

    public GridObject getSpotRealCoords(int x, int y) {
        x /= GRID_SIZE;
        y /= GRID_SIZE;
        return getAtSpot(y, x);
    }

    /**
     * This function changes the size of the building at (row, col)
     * it grows down and to the right, and vise versa for shrinking
     * @param row - row of the building to change
     * @param col - col of the building to change
     * @param newSizeX - new width of the building
     * @param newSizeY - new height of the building
     */

    public void changeBuildingSize(int row, int col, int newSizeX, int newSizeY) {
        GridObject obj = getAtSpot(row, col);
        if (!(obj instanceof Building) && !(obj instanceof Parking)) {
            // if not building or parking lot return
            return;
        }
        // move up to top left for reference
        while (row > 0 && obj == getAtSpot(row - 1, col)) {
            obj = getAtSpot(--row, col);
        }
        while (col > 0 && obj == getAtSpot(row, col - 1)) {
            obj = getAtSpot(row, --col);
        }

        // make sure its in bounds

        if (row + newSizeY - 1 >= numRows || col + newSizeX - 1 >= numColumns) {
            return;
        }


        //  validate area is available
        for (int i = 1; i < newSizeX; i++) {
            for (int k = 1; k < newSizeY; k++) {
                if (getAtSpot(row + i, col + k) != null && !(getAtSpot(row + i, row + k) == obj)) {
                    // not available
                    return;
                }
            }
        }

        if (obj instanceof Building) {
            ((Building) obj).setxLength(newSizeX);
            ((Building) obj).setyLength(newSizeY);
        } else {
            ((Parking) obj).setxLength(newSizeX);
            ((Parking) obj).setyLength(newSizeY);
        }

        // recursively delete the existing building and make a new one
        Image imageFile = grid[row][col].getImageFile();


        remove(row, col);
        // build new one
        for (int i = 0; i < newSizeX; i++) {
            for (int k = 0; k < newSizeY; k++) {
                Rectangle current = new Rectangle(GRID_SIZE, GRID_SIZE);
                current.setFill(new ImagePattern(imageFile));
                current.setY((row + k) * GRID_SIZE);
                current.setX((col + i) * GRID_SIZE);
                grid[row + k][col + i] = obj;
                frontGrid[row + k][col + i] = current;
            }
        }
    }

    public void changeRoadDirection(int row, int col, Direction newDirection) {
        GridObject obj = getAtSpot(row, col);
        if(!(obj instanceof Road)) {
            // not a road
            return;
        }

        if (obj instanceof OneWayRoad oneWayRoad) {
            oneWayRoad.rotateRoad(newDirection);
            updateIntersections(row, col, oneWayRoad);
            System.out.println("Changed Road Direction!\n");
        } else if (obj instanceof TwoWayRoad twoWayRoad) {
            twoWayRoad.rotateRoad(newDirection);
            updateIntersections(row, col, twoWayRoad);
        }

    }

    /**
     * This is a recursive function that sets all touching intersections to have the same ID
     * @param value - the value of the ID
     * @param row - row location of the intersection to check around
     * @param col - col location of the intersection to check around
     */
    private void groupIntersections(int value, int row, int col) {
        if (row < 0 || row >= numRows) return;
        if (col < 0 || col >= numColumns) return;
        if (!(getAtSpot(row, col) instanceof Intersection)) return;
        Intersection i = (Intersection) getAtSpot(row, col);
        if (i.getIntersectionID() != -1) return;

        i.setIntersectionID(value);
        groupIntersections(value, row + 1, col);
        groupIntersections(value, row - 1, col);
        groupIntersections(value, row, col + 1);
        groupIntersections(value, row, col - 1);

    }

    public boolean checkAroundDest(Road r) {
        int row = r.getRowNum();
        int col = r.getColNum();
        if (r.getOrientation() == Orientation.HORIZONTAL) { // CHECK ABOVE AND BELOW
            if (row - 1 >= 0 && getAtSpot(row - 1, col) instanceof Parking) {
                return true;
            } else if (row + 1 < numRows && getAtSpot(row + 1, col) instanceof Parking) {
                return true;
            }
        }
        if (r.getOrientation() == Orientation.VERTICAL) { // CHECK ABOVE AND BELOW
            if (col - 1 >= 0 && getAtSpot(row, col - 1) instanceof Parking) {
                return true;
            } else if (col + 1 < numColumns && getAtSpot(row, col + 1) instanceof Parking) {
                return true;
            }
        }
        return false;
    }

    private void getOutRoadsAroundHelper(Intersection i, ArrayList<OneWayRoad> response) {
        int row = i.getRowNum();
        int col = i.getColNum();
        // MAKE RECURSIVE CALLS TO THE INTERSECTIONS AROUND IT
        if (row + 1 < numRows
            && getAtSpot(row + 1, col) instanceof Intersection j
            && j.getIntersectionID() == i.getIntersectionID() ) {
            getOutRoadsAroundHelper(j, response);
        }
        if (row - 1 >= 0 &&
            getAtSpot(row - 1, col) instanceof Intersection j
            && j.getIntersectionID() == i.getIntersectionID() ) {
            getOutRoadsAroundHelper(j, response);
        }
        if (col + 1 < numColumns
            && getAtSpot(row, col + 1) instanceof Intersection j
            && j.getIntersectionID() == i.getIntersectionID() ) {
            getOutRoadsAroundHelper(j, response);
        }
        if (col - 1 >= 0 &&
            getAtSpot(row, col - 1) instanceof Intersection j
            && j.getIntersectionID() == i.getIntersectionID() ) {
            getOutRoadsAroundHelper(j, response);
        }

        // ADD THE RIGHT ROADS TO THE LIST
        if (row + 1 < numRows
            && getAtSpot(row + 1, col) instanceof OneWayRoad r
            && r.getDirection() == DOWN) {
            response.add(r);
        }
        if (row - 1 >= 0 &&
            getAtSpot(row - 1, col) instanceof OneWayRoad r
            && r.getDirection() == UP) {
            response.add(r);
        }
        if (col + 1 < numColumns
            && getAtSpot(row, col + 1) instanceof OneWayRoad r
            && r.getDirection() == RIGHT) {
            response.add(r);
        }
        if (col - 1 >= 0 &&
            getAtSpot(row, col - 1) instanceof OneWayRoad r
            && r.getDirection() == LEFT) {
            response.add(r);
        }

    }

    private ArrayList<OneWayRoad> getOutRoadsAround(Intersection i) {
        ArrayList<OneWayRoad> response = new ArrayList<>();
        getOutRoadsAroundHelper(i, response);
        return response;
    }


    public int[][] gridToGraph() {
        //TODO: ADD DESTINATIONS AND IN ROADS
        if (intersections != null) {
            for (GridObject i : intersections) {
                if (i instanceof Intersection in) {
                    in.setIntersectionID(-1);
                } else if (i instanceof Road) {
                    ((Road) i).setIntersectionID(-1);
                }
            }
        }
        intersections = new ArrayList<>();
        // count intersections
        int numIntersections = 0;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (getAtSpot(i, j) instanceof Intersection in) {
                    if (in.getIntersectionID() == -1) {
                        groupIntersections(numIntersections++, i, j);
                        intersections.add(in);
                    }
                } else if (getAtSpot(i, j) instanceof OneWayRoad r && r.isInRoad()) { // ADDED IN ROADS TO GRAPH
                    intersections.add(r);
                    r.setIntersectionID(numIntersections++);
                } else if (getAtSpot(i, j) instanceof OneWayRoad r) { // ADDED DESTINATION ROADS TO GRAPH
                    if (checkAroundDest(r)) {
                        intersections.add(r);
                        r.setIntersectionID(numIntersections++);
                    }
                }
            }
        }
        int[][] graph = new int[numIntersections][numIntersections];

        // route between intersections
        for (GridObject obj : intersections) {
            if (obj instanceof Intersection i) {
                ArrayList<OneWayRoad> roads = getOutRoadsAround(i);
                int originalID = i.getIntersectionID();
                for (OneWayRoad r : roads) {
                    GridObject cur = r;
                    int count = 0;
                    int lastID = -1;
                    while (!(cur instanceof Intersection)) {
                        if (cur instanceof Hazard) {
                            break;
                        }
                        if (cur instanceof OneWayRoad d) {
                            count += (MAX_SPEED_LIMIT - d.getSpeedLimit() + 1); // weighted graph
                            if (checkAroundDest(d)) {
                                lastID = d.getIntersectionID();
                                break;
                            }
                            cur = switch (d.getDirection()) {
                                case UP -> d.getRowNum() - 1 >= 0 ? getAtSpot(d.getRowNum() - 1, d.getColNum()) : null;
                                case DOWN ->
                                    d.getRowNum() + 1 < numRows ? getAtSpot(d.getRowNum() + 1, d.getColNum()) : null;
                                case RIGHT -> d.getColNum() + 1 < numColumns ? getAtSpot(d.getRowNum(), d.getColNum() + 1) :
                                    null;
                                case LEFT -> d.getColNum() - 1 >= 0 ? getAtSpot(d.getRowNum(), d.getColNum() - 1) : null;
                            };
                            if (cur == null) {
                                break;
                            }
                        }
                    }
                    if (cur instanceof Intersection j) {
                        lastID = j.getIntersectionID();
                    }
                    if (lastID != -1) {
                        graph[originalID][lastID] = count;
                    }
                }
            } else if (obj instanceof Road r) {
                GridObject cur = r;
                int count = 0;
                int lastID = -1;
                while (!(cur instanceof Intersection)) {
                    if (cur instanceof Hazard) {
                        break;
                    }
                    if (cur instanceof OneWayRoad d) {
                        count += (MAX_SPEED_LIMIT - d.getSpeedLimit() + 1); // weighted graph
                        if (checkAroundDest(d)) {
                            lastID = d.getIntersectionID();
                            break;
                        }
                        cur = switch (d.getDirection()) {
                            case UP -> d.getRowNum() - 1 >= 0 ? getAtSpot(d.getRowNum() - 1, d.getColNum()) : null;
                            case DOWN ->
                                d.getRowNum() + 1 < numRows ? getAtSpot(d.getRowNum() + 1, d.getColNum()) : null;
                            case RIGHT -> d.getColNum() + 1 < numColumns ? getAtSpot(d.getRowNum(), d.getColNum() + 1) :
                                null;
                            case LEFT -> d.getColNum() - 1 >= 0 ? getAtSpot(d.getRowNum(), d.getColNum() - 1) : null;
                        };
                        if (cur == null) {
                            break;
                        }
                    }
                }
                if (cur instanceof Intersection i) {
                    lastID = i.getIntersectionID();
                }
                if (lastID != -1) {
                    graph[r.getIntersectionID()][lastID] = count;
                }
            }
        }
        return graph;
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

    public GridObject getGridObject() {
        return null;
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
        Rectangle[][] newFront = new Rectangle[newNumRows][newNumCols];
        // iterate
        for (int i = 0; i < leastRows; i++) {
            for (int k = 0; k < leastCols; k++) {
                // copy objects from old grid to new one
                newGrid[i][k] = grid[i][k];
                newFront[i][k] = frontGrid[i][k];
            }
        }

        // update grid var
        this.numRows = newNumRows;
        this.numColumns = newNumCols;
        this.grid = newGrid;
        this.frontGrid = newFront;

    }

    private void setIntersectionAt(int rowNum, int colNum, Road newRoad) {
        // if is intersection, just add the newRoad
        if (grid[rowNum][colNum] instanceof Intersection i) {
            i.addRoad(newRoad);
            return;
        }

        Intersection newIntersection = new Intersection(rowNum, colNum, new Road[4]);

        // check the sides
        if (colNum > 0 && grid[rowNum][colNum - 1] instanceof Road &&
            ((Road) grid[rowNum][colNum - 1]).getOrientation() == Orientation.HORIZONTAL) {
            newIntersection.addRoad((Road) grid[rowNum][colNum - 1]);
        }
        if (colNum < numColumns - 1 && grid[rowNum][colNum + 1] instanceof Road &&
            ((Road) grid[rowNum][colNum + 1]).getOrientation() == Orientation.HORIZONTAL) {
            newIntersection.addRoad((Road) grid[rowNum][colNum + 1]);
        }
        if (rowNum > 0 && grid[rowNum - 1][colNum] instanceof Road &&
            ((Road) grid[rowNum - 1][colNum]).getOrientation() == Orientation.VERTICAL) {
            newIntersection.addRoad((Road) grid[rowNum - 1][colNum]);
        }
        if (rowNum < numRows - 1 && grid[rowNum + 1][colNum] instanceof Road &&
            ((Road) grid[rowNum + 1][colNum]).getOrientation() == Orientation.VERTICAL) {
            newIntersection.addRoad((Road) grid[rowNum + 1][colNum]);
        }

        grid[rowNum][colNum] = newIntersection;

        File imageFile = new File("Images/BasicIntersection.png");
        Image intersection = new Image(imageFile.toURI().toString());
        ImagePattern intersectionPattern = new ImagePattern(intersection);
        if (!testingMode) {
            frontGrid[rowNum][colNum].setFill(intersectionPattern);
        }
    }

    public void testGridInit() {
        for (int i = 0; i < numRows; i++) {
            for (int k = 0; k < numColumns; k++) {
                frontGrid[i][k] = new Rectangle();
            }
        }
    }


    /**
     * Checks to see if an intersection is needed at the given spot
     * @param rowNum
     * @param colNum
     * @param newRoad
     */
    private void checkIntersectionNeeded(int rowNum, int colNum, Road newRoad) {
        if (rowNum < 0 || colNum < 0 || rowNum >= numRows || colNum >= numColumns) {
            return;
        }
        if (grid[rowNum][colNum] instanceof Intersection) {
            setIntersectionAt(rowNum, colNum, newRoad);
        }
        if (!(grid[rowNum][colNum] instanceof Road)) {
            return;
        }

        Road checkRoad = (Road) grid[rowNum][colNum];
        if (checkRoad.getOrientation() == Orientation.VERTICAL) {
            // check the sides
            if (colNum > 0 && grid[rowNum][colNum - 1] instanceof Road &&
                ((Road) grid[rowNum][colNum - 1]).getOrientation() == Orientation.HORIZONTAL) {
                setIntersectionAt(rowNum, colNum, newRoad);
                return;
            }
            if (colNum < numColumns - 1 && grid[rowNum][colNum + 1] instanceof Road &&
                ((Road) grid[rowNum][colNum + 1]).getOrientation() == Orientation.HORIZONTAL) {
                setIntersectionAt(rowNum, colNum, newRoad);
                return;
            }
        } else {
            // check the top and bottom
            if (rowNum > 0 && grid[rowNum - 1][colNum] instanceof Road &&
                ((Road) grid[rowNum - 1][colNum]).getOrientation() == Orientation.VERTICAL) {
                setIntersectionAt(rowNum, colNum, newRoad);
                return;
            }
            if (rowNum < numRows - 1 && grid[rowNum + 1][colNum] instanceof Road &&
                ((Road) grid[rowNum + 1][colNum]).getOrientation() == Orientation.VERTICAL) {
                setIntersectionAt(rowNum, colNum, newRoad);
                return;
            }
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
        if (newObject == null) {
            System.out.println("Adding null object to the grid\n");
        }
        // if the spot is already full then do nothing
        if (grid[rowNum][colNum] != null) {
            return;
        }
        // add object to grid
        System.out.println(newObject.toString());
        grid[rowNum][colNum] = newObject;
        grid[rowNum][colNum].setColNum(colNum);
        grid[rowNum][colNum].setRowNum(rowNum);
        mergeRoads(rowNum, colNum);

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
        if (rowNum < 0 || rowNum >= numRows || colNum < 0 || colNum >= numColumns) {
            return null;
        }
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
    /**
     * This function updates the frontend to represent the backend
     */
    public void synchronizeGrid(){
        UserInterface.refreshGrid(numRows);
        for (int i = 0; i < numRows; i++) {
            for (int k = 0; k < numColumns; k++) {
                if (grid[i][k] != null) {
                    frontGrid[i][k].setFill(new ImagePattern(grid[i][k].getImageFile()));
                }
            }
        }
        UserInterface.refreshGrid(numRows);
    }

    public void updateTiming(StopLight s, int newTimingVertical, int newTimingHorizontal) {
        s.setTimingOne(newTimingVertical);
        s.setTimingTwo(newTimingHorizontal);
    }

    public void setTestingMode(boolean testingMode) {
        this.testingMode = testingMode;
    }
}
