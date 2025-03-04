package com.FlowLogic;

import javafx.scene.shape.Rectangle;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

    public static int GRID_SIZE;

    //Allows for quick conversion from Image file to backend object
    public static HashMap<String, GridObject> imgToObj;

    public Grid(int numRows, int numColumns) {
        this.numRows = numRows;
        this.numColumns = numColumns;
        grid = new GridObject[numRows][numColumns];
        frontGrid = new Rectangle[numRows][numColumns];
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
            StringBuilder jsonFileContent = new StringBuilder();
            try (Scanner scanner = new Scanner(new FileReader(filename))) {
                while (scanner.hasNextLine()) {
                    jsonFileContent.append(scanner.nextLine());
                }
            }

            // Parse JSON data
            JSONObject gridJson = new JSONObject(jsonFileContent);

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
                    case "Road":
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

            System.out.println("Successfully loaded grid from " + filename);
            return true;
        }
        catch (Exception e){
            // Insert additional error logic here if needed
            System.out.println("Error saving grid to file: " + e.getMessage());
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

    private Rectangle getImgFromObject(GridObject obj) {
        return null;
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
        frontGrid = new Rectangle[newNumRows][newNumCols];
        // iterate
        for (int i = 0; i < leastRows; i++) {
            for (int k = 0; k < leastCols; k++) {
                // copy objects from old grid to new one
                newGrid[i][k] = grid[i][k];
                //frontGrid[i][k] = grid[i][k];
            }
        }

        // update grid var
        this.numRows = newNumRows;
        this.numColumns = newNumCols;
        this.grid = newGrid;

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

}
