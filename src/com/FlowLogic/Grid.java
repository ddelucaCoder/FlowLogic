package com.FlowLogic;

import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.print.attribute.standard.OrientationRequested;

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
        imgToObj.put("RoadImage.png", new OneWayRoad(Orientation.VERTICAL, Direction.UP));
        imgToObj.put("RoadImageLeft.png", new OneWayRoad(Orientation.HORIZONTAL, Direction.LEFT));
        imgToObj.put("RoadImageRight.png", new OneWayRoad(Orientation.HORIZONTAL, Direction.RIGHT));
        imgToObj.put("BasicIntersection.png", new Intersection(0, 0, new Road[4]));
        imgToObj.put("ParkingLot.png", new Parking());
        imgToObj.put("YellowBuilding.png", new Building("yellow"));
        imgToObj.put("GreenBuilding.png", new Building("green"));
        imgToObj.put("RedBuilding.png", new Building("red"));

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

                        Road road = new Road(orientation, speedLimit, isInRoad, inCars, row, col);
                        road.setLength(length);

                        gridObject = road;
                        break;
                    case "Building":
                        int xLength = properties.getInt("xLength");
                        int yLength = properties.getInt("yLength");
                        int dailyPopulation = properties.getInt("dailyPopulation");

                        Building building = new Building(xLength, yLength, dailyPopulation);
                        building.setRowNum(row);
                        building.setColNum(col);

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
                }
                // Add everything to the grid
                if (gridObject != null) {
                    grid[row][col] = gridObject;
                }
            }
            // Second loop: Connect the roads to intersections
            for (int i = 0; i < gridObjectsArray.length(); i++) {
                JSONObject objJson = gridObjectsArray.getJSONObject(i);
                if ("Intersection".equals(objJson.getString("type"))) {
                    int row = objJson.getInt("row");
                    int col = objJson.getInt("column");
                    JSONObject properties = objJson.getJSONObject("properties");
                    JSONArray connectedRoads = properties.getJSONArray("connectedRoads");

                    Intersection intersection = (Intersection) grid[row][col];

                    // Connect each road to the intersection
                    for (int j = 0; j < connectedRoads.length(); j++) {
                        JSONObject roadRef = connectedRoads.getJSONObject(j);
                        int roadRow = roadRef.getInt("row");
                        int roadCol = roadRef.getInt("column");

                        // Find road from the grid
                        if (roadRow >= 0 && roadRow < numRows && roadCol >= 0 && roadCol < numColumns) {
                            GridObject obj = grid[roadRow][roadCol];
                            if (obj instanceof Road) {
                                intersection.addRoad((Road) obj);
                            }
                        }
                    }
                }
            }
            UserInterface.refreshGrid(numRows);
            synchronizeGrid();
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
                    if (obj instanceof Road) {
                        Road road = (Road) obj;
                        properties.put("orientation", road.getOrientation());
                        properties.put("speedLimit", road.getSpeedLimit());
                        properties.put("length", road.getLength());
                        properties.put("isInRoad", road.isInRoad());
                        properties.put("inCars", road.getInCars());
                    } else if (obj instanceof Intersection intersection) {

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
                    }
                    else if (obj instanceof Parking parking) {
                        properties.put("xLength", parking.getxLength());
                        properties.put("yLength", parking.getyLength());
                        properties.put("parkingCapacity", parking.getParkingCapacity());
                        properties.put("numCars", parking.getNumCars());
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
        addObject(newObject.clone(), rowNum, colNum);
    }

    /**
     * This function is called from the frontend when the user clicks on a square on the grid. It goes through the
     * options of each type of thing that could be clicked and does the appropriate action.
     * for each type of
     * @param row
     * @param col
     * @param optionLayout
     */

    public void select(int row, int col, VBox optionLayout) {
        GridObject obj = getAtSpot(row, col);
        if (obj instanceof Building) {
            Building b = (Building) obj;
            UserInterface.showBuildingOptions(optionLayout, this, b.getxLength(), b.getyLength(),
                b.getDailyPopulation(), row, col);
        } else if (obj instanceof Parking) {
            Parking p = (Parking) obj;
            UserInterface.showBuildingOptions(optionLayout, this, p.getxLength(), p.getyLength(),
                p.getParkingCapacity(), row, col);
        }
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
     * @param row
     * @param col
     * @param newPop
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
     * @param row
     * @param col
     * @param newCap
     */
    public void changeParkingCapacity(int row, int col, int newCap) {
        GridObject obj = getAtSpot(row, col);
        if (!(obj instanceof Parking)) {
            return;
        }
        ((Parking) obj).setParkingCapacity(newCap);
    }

    /**
     * This function changes the size of the building at (row, col)
     * @param row
     * @param col
     * @param newSizeX
     * @param newSizeY
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

        if (row + newSizeY >= numRows || col + newSizeX >= numRows) {
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

    public int[][] gridToGraph() {
        int[][] graph = new int[numRows][numColumns];
        ArrayList<Intersection> intersections = new ArrayList<>();
        // count intersections
        int numIntersections = 0;
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numColumns; j++) {
                if (getAtSpot(i, j) instanceof Intersection in) {
                    in.setIntersectionID(numIntersections++);
                    intersections.add(in);
                }
            }
        }

        // route between intersections
        for (Intersection i : intersections) {
            for (int k = 0; k < 4; k++) {
                Road currentRoad = i.getRoadList()[k];
                if (currentRoad == null) {
                    continue;
                }
                int row = i.getRowNum();
                int col = i.getColNum();
                if ((row > 0) && (grid[row - 1][col] == currentRoad)) {
                    if (currentRoad instanceof OneWayRoad) {
                        if (((OneWayRoad) currentRoad).getDirection() == Direction.DOWN) {
                            continue;
                        }
                    }
                    // otherwise just follow it to its spot
                    GridObject current = currentRoad;
                    while (!(current instanceof Intersection)) {
                        if (row == 0) {
                            continue;
                        }
                        current = grid[row - 1][col];
                        row--;
                    }
                    graph[i.getIntersectionID()][((Intersection) current).getIntersectionID()] = 1;
                } else if ((row <= numRows) && (grid[row + 1][col] == currentRoad)) {
                    if (currentRoad instanceof OneWayRoad) {
                        if (((OneWayRoad) currentRoad).getDirection() == Direction.UP) {
                            continue;
                        }
                    }
                    // otherwise just follow it to its spot
                    GridObject current = currentRoad;
                    while (!(current instanceof Intersection)) {
                        if (row == 0) {
                            continue;
                        }
                        current = grid[row + 1][col];
                        row++;
                    }
                    graph[i.getIntersectionID()][((Intersection) current).getIntersectionID()] = 1;
                } else if ((col > 0) && (grid[row][col - 1] == currentRoad)) {
                    if (currentRoad instanceof OneWayRoad) {
                        if (((OneWayRoad) currentRoad).getDirection() == Direction.RIGHT) {
                            continue;
                        }
                    }
                    // otherwise just follow it to its spot
                    GridObject current = currentRoad;
                    while (!(current instanceof Intersection)) {
                        if (row == 0) {
                            continue;
                        }
                        current = grid[row][col - 1];
                        col--;
                    }
                    graph[i.getIntersectionID()][((Intersection) current).getIntersectionID()] = 1;
                } else if ((col >= 0) && (grid[row][col + 1] == currentRoad)) {
                    if (currentRoad instanceof OneWayRoad) {
                        if (((OneWayRoad) currentRoad).getDirection() == Direction.LEFT) {
                            continue;
                        }
                    }
                    // otherwise just follow it to its spot
                    GridObject current = currentRoad;
                    while (!(current instanceof Intersection)) {
                        if (row == 0) {
                            continue;
                        }
                        current = grid[row][col + 1];
                        col++;
                    }
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
        if (grid[rowNum][colNum] instanceof Intersection) {
            ((Intersection) grid[rowNum][colNum]).addRoad(newRoad);
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
        frontGrid[rowNum][colNum].setFill(intersectionPattern);
    }

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
        for (int i = 0; i < numRows; i++) {
            for (int k = 0; k < numColumns; k++) {
                if (grid[i][k] != null) {
                    frontGrid[i][k].setFill(new ImagePattern(grid[i][k].getImageFile()));
                }
            }
        }
        UserInterface.refreshGrid(numRows);
    }

}
