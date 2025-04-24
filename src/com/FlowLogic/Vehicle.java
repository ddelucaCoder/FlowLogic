package com.FlowLogic;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;

import java.util.*;

import static com.FlowLogic.CarState.*;
import static com.FlowLogic.Direction.*;
import static com.FlowLogic.UserInterface.GRID_SIZE;
import static com.FlowLogic.UserInterface.grid;

/**
 * Vehicle class that handles vehicle behavior in traffic simulation.
 * Implements movement, collision avoidance, and interaction with traffic control elements.
 */
public class Vehicle {
    // Vehicle physical properties
    private int length;
    private int width = 10;
    private Rectangle car;

    // Vehicle position and movement
    private int x;
    private int y;
    private int speed;
    private int curRotation = 0; // Degrees
    private Direction direction;
    private CarState state;

    // Path properties
    private ArrayList<GridObject> intersectionPath;
    private ArrayList<Direction> directionPath;
    private int startRoadID;
    private int endRoadID;
    private int timeIn;

    // Intersection handling
    private Intersection currentIntersection;
    private Intersection lastStopped;
    private int lastIntersectionX;
    private int lastIntersectionY;
    private boolean turnPositionSet = false;

    // Roundabout properties
    private int roundAboutPos = -1;
    private Roundabout curRoundabout = null;

    // Constants for vehicle behavior
    private static final int TURN_RATE = 45;
    private static final int FOLLOWING_DISTANCE = 15; // Increased for safer distance
    private static final int FAST_DECEL = 6;
    private static final int ACCEL = 3;
    private Direction lastDir = RIGHT;

    private StopLight straight;

    /**
     * Creates a new vehicle with the specified length.
     * @param length The length of the vehicle
     */
    public Vehicle(int length) {
        this.length = length;
        this.intersectionPath = new ArrayList<>();
        this.directionPath = new ArrayList<>();
        this.startRoadID = 0;
        this.endRoadID = 0;
        this.timeIn = 0;
        this.x = 0;
        this.y = 0;
        this.speed = 0;
        this.state = NOT_SPAWNED;
        this.direction = null;
        this.currentIntersection = null;

        // Initialize vehicle visualization off-screen
        Rectangle newCar = new Rectangle(-100, -100, width, length);
        newCar.setRotate(curRotation);
        newCar.setVisible(true);
        //Image image = new Image("file:Images/BlueCar.png");
        //newCar.setFill(new ImagePattern(image));
        this.car = newCar;
    }

    /**
     * Copy constructor for creating a duplicate of an existing vehicle.
     * @param v The vehicle to copy
     */
    public Vehicle(Vehicle v) {
        this.intersectionPath = new ArrayList<>(v.intersectionPath);
        this.directionPath = new ArrayList<>(v.directionPath);
        this.width = v.width;
        this.length = v.length;
        this.curRotation = v.curRotation;
        this.startRoadID = v.startRoadID;
        this.endRoadID = v.endRoadID;
        this.timeIn = v.timeIn;
        this.x = v.x;
        this.y = v.y;
        this.speed = v.speed;
        this.state = v.state;
        this.direction = v.direction;
        this.car = v.car;
        this.currentIntersection = v.currentIntersection;
        this.lastStopped = v.lastStopped;
        this.lastIntersectionX = v.lastIntersectionX;
        this.lastIntersectionY = v.lastIntersectionY;
        this.turnPositionSet = v.turnPositionSet;
        this.roundAboutPos = v.roundAboutPos;
        this.curRoundabout = v.curRoundabout;
    }

    /**
     * Attempts to spawn the vehicle on the grid.
     * Checks for collisions with other vehicles before spawning.
     *
     * @param allVehicles List of all vehicles in the simulation
     * @return true if successfully spawned, false otherwise
     */
    private boolean spawn(List<Vehicle> allVehicles) {
        int[] coords = Grid.getRealCoords(this.intersectionPath.get(0));
        int spawnX = coords[1];
        int spawnY = coords[0];

        // Create a temporary position for collision check
        int tempX = spawnX + 16;
        int tempY = spawnY + 16 - (width / 2);

        // Check if any existing vehicle is occupying the spawn location
        for (Vehicle other : allVehicles) {
            if (this == other) continue; // Skip self

            // Calculate distance between spawn point and other vehicle center
            double dx = tempX - other.getX();
            double dy = tempY - other.getY();
            double distance = Math.sqrt(dx*dx + dy*dy);

            // If another vehicle is too close to our spawn point, don't spawn
            if (distance < length + 5) {  // Added safety margin
                return false; // Cannot spawn, location occupied
            }
        }

        // Location is clear, proceed with spawning
        x = tempX;
        y = tempY;
        speed = 0;
        direction = directionPath.get(0);
        state = FORWARD;

        // Set initial rotation based on direction
        switch (direction) {
            case UP -> curRotation = 0;
            case RIGHT -> curRotation = 90;
            case DOWN -> curRotation = 180;
            case LEFT -> curRotation = 270;
        }

        car = new Rectangle(x, y, width, length);
        return true; // Successfully spawned
    }

    /**
     * Moves the vehicle forward based on its current direction and speed.
     */
    private void moveForward() {
        int xVel = 0;
        int yVel = 0;

        switch (direction) {
            case UP -> yVel = -speed;
            case RIGHT -> xVel = speed;
            case DOWN -> yVel = speed;
            case LEFT -> xVel = -speed;
        }

        x += xVel;
        y += yVel;
    }

    /**
     * Gets the grid object at the vehicle's current position.
     *
     * @param g The grid containing all objects
     * @return The grid object at the vehicle's current position
     */
    private GridObject getCurrentGridObject(Grid g) {
        return g.getSpotRealCoords(x, y);
    }

    /**
     * Gets the grid object at the specified coordinates.
     *
     * @param g The grid containing all objects
     * @param coords The coordinates [x, y] to check
     * @return The grid object at the specified coordinates
     */
    private GridObject getCurrentGridObject(Grid g, int[] coords) {
        return g.getSpotRealCoords(coords[0], coords[1]);
    }

    /**
     * Increases the vehicle's speed.
     */
    private void accelerate() {
        speed += ACCEL;
    }

    /**
     * Determines the appropriate light state based on the vehicle's direction.
     *
     * @param light The traffic light to check
     * @return The color of the light for the vehicle's direction
     */
    private int getLightStateForDirection(StopLight light) {
        if (direction == UP || direction == DOWN) {
            // Vertical traffic - check lightOne
            return light.getLightOneColor();
        } else if (direction == LEFT || direction == RIGHT) {
            // Horizontal traffic - check lightTwo
            return light.getLightTwoColor();
        }
        return light.RED; // Default to RED if direction is undefined
    }

    /**
     * Moves the vehicle forward slightly after completing a turn.
     * Ensures the vehicle clears the intersection.
     */
    private void moveForwardAfterTurn() {
        int moveDistance = 10; // Distance to move after turning

        // Move vehicle forward in the current direction
        switch (direction) {
            case UP:
                y -= moveDistance;
                break;
            case DOWN:
                y += moveDistance;
                break;
            case LEFT:
                x -= moveDistance;
                break;
            case RIGHT:
                x += moveDistance;
                break;
        }
    }

    /**
     * Checks if the vehicle needs to decelerate based on obstacles ahead.
     * Handles traffic lights, stop signs, other vehicles, and destination approaches.
     *
     * @param g The grid containing all objects
     * @param otherVehicles List of all other vehicles to check for collisions
     * @return true if deceleration is needed, false otherwise
     */
    private boolean decelerate(Grid g, List<Vehicle> otherVehicles) {
        boolean needToDecelerate = false;

        // Check for vehicles in front
        for (Vehicle other : otherVehicles) {
            if (this == other) continue; // Skip self
            if (other.state == NOT_SPAWNED || other.state == DESTINATION_REACHED) continue;

            // Calculate distance to other vehicle
            double distance = calculateDistanceToVehicle(other);

            // Check if other vehicle is in front of us
            if (isVehicleInFront(other)) {
                // Calculate safe following distance based on our speed
                int safeDistance = FOLLOWING_DISTANCE + speed;

                // If we're within the safe distance, slow down proportionally
                if (distance < safeDistance) {
                    // The closer we are, the more we slow down
                    if (distance > 10) {
                        // Set speed proportional to distance (distance/3)
                        int targetSpeed = (int)(distance / 3);

                        // Only slow down if our current speed is higher than the target
                        if (speed > targetSpeed) {
                            speed = targetSpeed;
                        }

                        // Maintain minimum speed unless very close
                        if (speed < 5 && distance > length) {
                            speed = 5;
                        }
                    } else  {
                        // Emergency stop if distance is zero or negative (collision)
                        speed = 0;
                        return true;
                    }

                    needToDecelerate = true;
                }
            }
        }

        // Check for traffic lights ahead - scan farther ahead for higher speeds
        int lookAheadDistance = Math.max(64, ((speed / 5) + 1) * 32);

        for (int i = 0; i < lookAheadDistance; i += 16) {  // Smaller step size for more precise detection
            if (getCurrentGridObject(g, front(i)) instanceof StopLight light) {
                int lightState = getLightStateForDirection(light);
                if (lastStopped == light) continue;

                if (lightState == light.RED) {
                    // Original code for stopping at red light when not in intersection
                    if (i < 15) {
                        int[] coords = Grid.getRealCoords(light);
                        lastIntersectionX = coords[1];
                        lastIntersectionY = coords[0];

                        lastStopped = light;
                        speed = 0;
                        currentIntersection = light;

                        // Position the vehicle precisely at the stop line
                        positionAtStopLine(light);

                        // Set appropriate state based on path
                        if (light != straight && directionPath.size() > 1 && directionPath.get(1) != direction) {
                            state = STOPPED_TURNING;
                        } else {
                            state = STOPPED_FORWARD;
                        }

                        // Add the vehicle to the appropriate queue
                        light.addToQueue(this);
                    } else {
                        // Gradual deceleration as we approach
                        speed = i / 3;
                        if (speed < 5 && i > 32) speed = 5; // Maintain minimum speed unless very close
                    }
                    return true;
                } else if (lightState == light.YELLOW) {

                    // If already in intersection or too close to stop safely, proceed
                    if (i < 30 && speed > 10) {
                        // Already committed to intersection - proceed through
                        if (direction != directionPath.get(1) && light != straight) {
                            int targetSpeed = i / 3 + 5;
                            if (targetSpeed < speed) {
                                speed = targetSpeed;
                            }
                            int coords[] = Grid.getRealCoords(light);
                            int intersectionX = coords[1] + 16;
                            int intersectionY = coords[0] + 16;
                            int frontX = front()[0];
                            int frontY = front()[1];
                            int distance =
                                (int) Math.sqrt(Math.pow((intersectionX - frontX), 2) + Math.pow((intersectionY - frontY),
                                    2));
                            if (distance < 36) {
                                intersectionPath.remove(0);
                                lastStopped = (Intersection) intersectionPath.get(0);
                                lastIntersectionX = intersectionPath.get(0).getColNum() * Grid.GRID_SIZE;
                                lastIntersectionY = intersectionPath.get(0).getRowNum() * Grid.GRID_SIZE;
                                lastDir = directionPath.remove(0);
                                direction = directionPath.get(0);
                                speed = 0;
                                state = TURNING;
                            }
                        }
                    } else {
                        // slow down
                        int targetSpeed = i / 3;
                        if (targetSpeed < speed) {
                            speed = targetSpeed;
                            if (speed < 3) speed = 3;
                        }

                        if (i < 15) { // close enough to just stop
                            int[] coords = Grid.getRealCoords(light);
                            lastIntersectionX = coords[1];
                            lastIntersectionY = coords[0];

                            lastStopped = light;
                            speed = 0;
                            currentIntersection = light;

                            // Position vehicle precisely at stop line
                            positionAtStopLine(light);

                            // Set appropriate state based on path
                            if (light != straight && directionPath.size() > 1 && directionPath.get(1) != direction) {
                                state = STOPPED_TURNING;
                            } else {
                                state = STOPPED_FORWARD;
                            }

                            // Add vehicle to appropriate queue
                            light.addToQueue(this);
                        }
                    }
                } else {
                    // light is green
                    // if we need to turn
                    if (light != straight) {
                        if (directionPath.size() > 1 && direction != directionPath.get(1)) {
                            int targetSpeed = i / 3 + 5;
                            if (targetSpeed < speed) {
                                speed = targetSpeed;
                            }
                            int coords[] = Grid.getRealCoords(light);
                            int intersectionX = coords[1] + 16;
                            int intersectionY = coords[0] + 16;
                            int frontX = front()[0];
                            int frontY = front()[1];
                            int distance =
                                (int) Math.sqrt(Math.pow((intersectionX - frontX), 2) + Math.pow((intersectionY - frontY),
                                    2));
                            if (distance < 36) {
                                intersectionPath.remove(0);
                                lastStopped = (Intersection) intersectionPath.get(0);
                                lastDir = directionPath.remove(0);
                                direction = directionPath.get(0);
                                lastIntersectionX = intersectionPath.get(0).getColNum() * Grid.GRID_SIZE;
                                lastIntersectionY = intersectionPath.get(0).getRowNum() * Grid.GRID_SIZE;
                                speed = 0;
                                state = TURNING;
                            }
                        }
                    }
                }
            }
        }

        // IMPROVED STOP SIGN HANDLING - Look ahead further based on speed
        for (int i = 0; i < lookAheadDistance; i += 16) {
            if (getCurrentGridObject(g, front(i)) instanceof StopSign s) {
                if (lastStopped != s) {
                    // Begin deceleration earlier - gradual approach
                    if (i > 64) {
                        // Far from stop sign, start gradual deceleration
                        speed = i / 3;
                        if (speed < 5) speed = 5; // Maintain minimum speed unless very close
                        return true;
                    } else if (i <= 64 && i > 20) {
                        // Getting closer, more significant deceleration
                        speed = i / 4;
                        if (speed < 3) speed = 3;
                        return true;
                    } else if (i <= length + 5) {
                        // Very close to the stop sign

                        // Store the intersection coordinates for future use
                        int[] coords = Grid.getRealCoords(s);
                        lastIntersectionX = coords[1];
                        lastIntersectionY = coords[0];

                        lastStopped = s;
                        speed = 0;
                        currentIntersection = s; // Set current intersection to stop sign

                        // Position the vehicle precisely at the stop line
                        positionAtStopLine(s);

                        // Set appropriate state based on path
                        if (directionPath.size() > 1 && directionPath.get(1) != direction) {
                            state = STOPPED_TURNING;
                        } else {
                            state = STOPPED_FORWARD;
                        }

                        // Use the StopSign's existing method to add to queue
                        s.addToIntersection(this);

                        return true;
                    }
                }
            }
        }

        // Roundabout logic
        if (getCurrentGridObject(g, front(5)) instanceof Roundabout r) {
            if (curRoundabout != r) {
                speed = 0;
                r.getQueue().add(this);
                if (directionPath.size() > 1 && directionPath.get(0) != directionPath.get(1)) {
                    state = STOPPED_TURNING;
                } else {
                    state = STOPPED_FORWARD;
                }
            }
        }

        // Check if nearing destination or stop sign
        for (int i = 0; i < ((speed / 10) + 1) * 32; i += 32) {
            if (getCurrentGridObject(g, front(i)) instanceof Road r && r.getIntersectionID() == endRoadID ||
                getCurrentGridObject(g, front(i)) instanceof StopSign j ||
                getCurrentGridObject(g, front(i)) instanceof Roundabout round) {
                if (speed > i / 3) speed = i / 3;
                if (speed < 5) speed = 5;
                return true;
            }
        }

        return needToDecelerate;
    }

    /**
     * Positions the vehicle precisely at the stop line of a traffic light.
     * Handles JavaFX coordinate system with center-based rotation.
     *
     * @param light The traffic light to stop at
     */
    /**
     * Positions the vehicle precisely at the stop line of a traffic control element.
     * Works for both traffic lights and stop signs.
     *
     * @param intersection The intersection element to stop at
     */
    private void positionAtStopLine(GridObject intersection) {
        int[] intersectionCoords = Grid.getRealCoords(intersection);
        int intersectionX = intersectionCoords[1];  // Column coordinate
        int intersectionY = intersectionCoords[0];  // Row coordinate
        int gridSize = Grid.GRID_SIZE;

        int intersectionCenterX = intersectionX + (gridSize / 2);
        int intersectionCenterY = intersectionY + (gridSize / 2);

        int targetCenterX, targetCenterY;

        switch (direction) {
            case UP:
                targetCenterX = intersectionCenterX;
                targetCenterY = intersectionY + gridSize + length / 2;
                break;

            case DOWN:
                targetCenterX = intersectionCenterX;
                targetCenterY = intersectionY - length / 2;
                break;

            case LEFT:
                targetCenterX = intersectionX + gridSize + length / 2;
                targetCenterY = intersectionCenterY;
                break;

            case RIGHT:
                targetCenterX = intersectionX - length / 2;
                targetCenterY = intersectionCenterY;
                break;
            default:
                return;
        }

        int targetX = targetCenterX - width/2;
        int targetY = targetCenterY - length/2;

        x = targetX;
        y = targetY;
    }

    /**
     * Calculates the distance to another vehicle.
     *
     * @param other The other vehicle to check distance to
     * @return The distance between this vehicle and the other vehicle
     */
    private double calculateDistanceToVehicle(Vehicle other) {
        // Get front coordinates of this vehicle and back coordinates of other vehicle
        int[] myFront = front();
        int[] otherBack = other.back(0);

        // Calculate center of other vehicle
        int otherCenterX = other.x + other.width/2;
        int otherCenterY = other.y + other.length/2;

        // Calculate the back of the other vehicle based on its direction
        // Calculate dist between my front and other's back
        double dx = myFront[0] - otherBack[0];
        double dy = myFront[1] - otherBack[1];

        return Math.sqrt(dx*dx + dy*dy);
    }

    /**
     * Determines if another vehicle is in front of this vehicle.
     *
     * @param other The other vehicle to check
     * @return true if the other vehicle is in front, false otherwise
     */
    /**
     * Determines if another vehicle is in front of this vehicle.
     * Enhanced to detect turning vehicles in the intersection.
     *
     * @param other The other vehicle to check
     * @return true if the other vehicle is in front, false otherwise
     */
    private boolean isVehicleInFront(Vehicle other) {
        // First check: is the other vehicle turning in an intersection?
        if (other.state == TURNING) {
            // Get intersection boundaries
            int otherIntersectionX = other.lastIntersectionX;
            int otherIntersectionY = other.lastIntersectionY;
            int gridSize = Grid.GRID_SIZE;

            // Check if we're approaching the same intersection
            int[] myFront = front(32); // Look ahead a bit
            boolean approachingIntersection =
                myFront[0] >= otherIntersectionX &&
                    myFront[0] <= otherIntersectionX + gridSize &&
                    myFront[1] >= otherIntersectionY &&
                    myFront[1] <= otherIntersectionY + gridSize;

            if (approachingIntersection) {
                return true; // Consider turning vehicle as being in front
            }
        }

        // For vehicles heading in the same direction
        if (direction != other.direction) return false;

        int myCenterX = 0;
        int myCenterY = 0;
        int otherCenterX = 0;
        int otherCenterY = 0;

        // Calculate centers
        switch (direction) {
            case UP:
                myCenterX = x + width/2;
                myCenterY = y + length/2;
                otherCenterX = other.x + other.width/2;
                otherCenterY = other.y + other.length/2;
                break;
            case DOWN:
                myCenterX = x + width/2;
                myCenterY = y + length/2;
                otherCenterX = other.x + other.width/2;
                otherCenterY = other.y + other.length/2;
                break;
            case LEFT:
                myCenterX = x + length/2;
                myCenterY = y + width/2;
                otherCenterX = other.x + other.length/2;
                otherCenterY = other.y + other.width/2;
                break;
            case RIGHT:
                myCenterX = x + length/2;
                myCenterY = y + width/2;
                otherCenterX = other.x + other.length/2;
                otherCenterY = other.y + other.width/2;
                break;
        }


        switch (direction) {
            case UP:
                return otherCenterY < myCenterY && Math.abs(otherCenterX - myCenterX) < width;
            case DOWN:
                return otherCenterY > myCenterY && Math.abs(otherCenterX - myCenterX) < width;
            case LEFT:
                return otherCenterX < myCenterX && Math.abs(otherCenterY - myCenterY) < length;
            case RIGHT:
                return otherCenterX > myCenterX && Math.abs(otherCenterY - myCenterY) < length;
            default:
                return false;
        }
    }

    /**
     * Executes a right turn at an intersection.
     * Fixed to handle coordinate system correctly and prevent teleporting.
     *
     * @return A Step object containing the previous and current state
     */
    private Step turnRight(Grid g) {
        Vehicle past = new Vehicle(this);

        // If turn position hasn't been set yet, set it
        if (!turnPositionSet) {
            // Position the vehicle properly at the center of the intersection
            int centerX = lastIntersectionX + Grid.GRID_SIZE/2;
            int centerY = lastIntersectionY + Grid.GRID_SIZE/2;

            // Position car to turn around the center of the intersection
            // Offset by half the smaller dimension (width) to ensure turning radius is consistent
            // Use center positions instead of top-left for proper pivoting
            this.x = centerX - width/2;
            this.y = centerY - width/2;
            turnPositionSet = true;
        }

        // Rotate the vehicle gradually
        curRotation += TURN_RATE;

        // Keep curRotation in the range [0, 360)
        if (curRotation >= 360) {
            curRotation -= 360;
        }

        // Check if we've completed the turn (reached a cardinal direction)
        if (curRotation % 90 == 0) {

            // Turn completed
            state = FORWARD;
            turnPositionSet = false;
            moveForwardAfterTurn();
        }

        return new Step(past, new Vehicle(this));
    }

    /**
     * Executes a left turn at an intersection.
     * Fixed to handle coordinate system correctly and prevent teleporting.
     *
     * @return A Step object containing the previous and current state
     */
    private Step turnLeft() {
        Vehicle past = new Vehicle(this);

        // If turn position hasn't been set yet, set it
        if (!turnPositionSet) {
            // Position the vehicle properly at the center of the intersection
            int centerX = lastIntersectionX + Grid.GRID_SIZE/2;
            int centerY = lastIntersectionY + Grid.GRID_SIZE/2;

            // Position car to turn around the center of the intersection
            // Offset by half the smaller dimension (width) to ensure turning radius is consistent
            this.x = centerX - width/2;
            this.y = centerY - width/2;
            turnPositionSet = true;

        }

        // Rotate the vehicle gradually
        curRotation -= TURN_RATE;

        // Keep curRotation in the range [0, 360)
        if (curRotation < 0) {
            curRotation += 360;
        }

        // Check if we've completed the turn (reached a cardinal direction)
        if (curRotation % 90 == 0) {
            // Turn completed
            state = FORWARD;
            turnPositionSet = false;
            moveForwardAfterTurn();
        }

        return new Step(past, new Vehicle(this));
    }

    /**
     * Main method that updates the vehicle state for each simulation tick.
     *
     * @param g The grid containing all objects
     * @param allVehicles List of all vehicles in the simulation
     * @return A Step object containing the previous and current state
     */
    public Step tick(Grid g, List<Vehicle> allVehicles) {
        // Check if destination reached
        if (state != DESTINATION_REACHED &&
            getCurrentGridObject(g) == intersectionPath.get(intersectionPath.size() - 1)) {
            System.out.println("Destination Reached");
            state = DESTINATION_REACHED;
            return new Step(this, null);
        } else if (state == DESTINATION_REACHED) {
            return null;
        }

        // STATE MACHINE
        if (state == NOT_SPAWNED) {
            // Handle vehicle spawn timer
            this.timeIn--;
            if (this.timeIn <= 0) {
                if (this.spawn(allVehicles)) {
                    return new Step(null, new Vehicle(this));
                } else {
                    return null;
                }
            }
            return null;
        } else if (state == FORWARD) {
            // Normal forward movement
            Vehicle before = new Vehicle(this);


            // ADD BACK HERE

            // check all cells that we'd pass through
            int oldX = x;
            int oldY = y;

            // If we're still in FORWARD state after decelerate check, move forward
            if (state == FORWARD) {
                moveForward();

                // remove if we went through a stoplight
                if (oldX != x) {
                    if (x > oldX) {
                        for (int i = oldX; i <= x; i += 32) {
                            // Check for StopLight
                            if (intersectionPath.size() > 1 && getCurrentGridObject(g, new int[]{i, y}) instanceof StopLight s && s == intersectionPath.get(1)) {
                                if (directionPath.size() > 1 && directionPath.get(0) == directionPath.get(1)) {
                                    System.out.println("Removed StopLight: " + intersectionPath.get(0) + " by " + this);
                                    straight = s;
                                    intersectionPath.remove(0);
                                    lastDir = directionPath.remove(0);
                                    if (!directionPath.isEmpty()) {
                                        direction = directionPath.get(0);
                                    }
                                }
                            }
                            // Check for ONE_WAY Road
                            if (intersectionPath.size() > 1 && getCurrentGridObject(g, new int[]{i, y}) instanceof OneWayRoad road && road == intersectionPath.get(1)) {
                                System.out.println("Removed ONE_WAY Road: " + intersectionPath.get(0) + " by " + this);
                                intersectionPath.remove(0);
                                lastDir = directionPath.remove(0);
                                if (!directionPath.isEmpty()) {
                                    direction = directionPath.get(0);
                                }
                            }
                        }
                    } else {
                        for (int i = x; i <= oldX; i += 32) {
                            // Check for StopLight
                            if (intersectionPath.size() > 1 && getCurrentGridObject(g, new int[]{i, y}) instanceof StopLight s && s == intersectionPath.get(1)) {
                                if (directionPath.size() > 1 && directionPath.get(0) == directionPath.get(1)) {
                                    System.out.println("Removed StopLight: " + intersectionPath.get(0) + " by " + this);
                                    straight = s;
                                    intersectionPath.remove(0);
                                    lastDir = directionPath.remove(0);
                                    if (!directionPath.isEmpty()) {
                                        direction = directionPath.get(0);
                                    }
                                }
                            }
                            // Check for ONE_WAY Road
                            if (intersectionPath.size() > 1 && getCurrentGridObject(g, new int[]{i, y}) instanceof OneWayRoad road && road == intersectionPath.get(1)) {
                                System.out.println("Removed ONE_WAY Road: " + intersectionPath.get(0) + " by " + this);
                                intersectionPath.remove(0);
                                lastDir = directionPath.remove(0);
                                if (!directionPath.isEmpty()) {
                                    direction = directionPath.get(0);
                                }
                            }
                        }
                    }
                } else if (oldY != y) {
                    if (y > oldY) {
                        for (int i = oldY; i <= y; i += 32) {
                            // Check for StopLight
                            if (intersectionPath.size() > 1 && getCurrentGridObject(g, new int[]{x, i}) instanceof StopLight s && s == intersectionPath.get(1)) {
                                if (directionPath.size() > 1 && directionPath.get(0) == directionPath.get(1)) {
                                    System.out.println("Removed StopLight: " + intersectionPath.get(0) + " by " + this);
                                    straight = s;
                                    intersectionPath.remove(0);
                                    lastDir = directionPath.remove(0);
                                    if (!directionPath.isEmpty()) {
                                        direction = directionPath.get(0);
                                    }
                                }
                            }
                            // Check for ONE_WAY Road
                            if (intersectionPath.size() > 1 && getCurrentGridObject(g, new int[]{x, i}) instanceof OneWayRoad road && road == intersectionPath.get(1)) {
                                System.out.println("Removed ONE_WAY Road: " + intersectionPath.get(0) + " by " + this);
                                intersectionPath.remove(0);
                                lastDir = directionPath.remove(0);
                                if (!directionPath.isEmpty()) {
                                    direction = directionPath.get(0);
                                }
                            }
                        }
                    } else {
                        for (int i = y; i <= oldY; i += 32) {
                            // Check for StopLight
                            if (intersectionPath.size() > 1 && getCurrentGridObject(g, new int[]{x, i}) instanceof StopLight s && s == intersectionPath.get(1)) {
                                if (directionPath.size() > 1 && directionPath.get(0) == directionPath.get(1)) {
                                    System.out.println("Removed StopLight: " + intersectionPath.get(0) + " by " + this);
                                    straight = s;
                                    intersectionPath.remove(0);
                                    lastDir = directionPath.remove(0);
                                    if (!directionPath.isEmpty()) {
                                        direction = directionPath.get(0);
                                    }
                                }
                            }
                            // Check for ONE_WAY Road
                            if (intersectionPath.size() > 1 && getCurrentGridObject(g, new int[]{x, i}) instanceof OneWayRoad road && road == intersectionPath.get(1)) {
                                System.out.println("Removed ONE_WAY Road: " + intersectionPath.get(0) + " by " + this);
                                intersectionPath.remove(0);
                                lastDir = directionPath.remove(0);
                                if (!directionPath.isEmpty()) {
                                    direction = directionPath.get(0);
                                }
                            }
                        }
                    }
                }
            }

            // Check if we need to decelerate, otherwise accelerate if below speed limit
            if (!decelerate(g, allVehicles) &&
                ((getCurrentGridObject(g) instanceof Road r &&
                    r.getSpeedLimit() > speed) || (getCurrentGridObject(g) instanceof Intersection))) {
                accelerate();
            }

            return new Step(before, new Vehicle(this));
        } else if (state == STOPPED_FORWARD || state == STOPPED_TURNING) {
            // Waiting at a traffic light or stop sign
            return new Step(new Vehicle(this), new Vehicle(this));
        } else if (state == TURNING) {
            // Execute turns based on current and next direction
            if (directionPath.size() >= 1) {
                if ((direction == RIGHT && lastDir == UP) // correct
                    || (direction == DOWN && lastDir == RIGHT) // correct
                    || (direction == UP && lastDir == LEFT) // correct
                    || (direction == LEFT && lastDir == DOWN)) { // correct
                    return this.turnRight(g);
                } else if ((lastDir == RIGHT && direction == UP) // correct
                    || (lastDir == DOWN && direction == RIGHT) // correct
                    || (lastDir == UP && direction == LEFT) // correct
                    || (lastDir == LEFT && direction == DOWN)) { // correct
                    return this.turnLeft();
                } else {
                    System.out.println("Turning but directions don't match");
                    System.out.println("Cur Dir: " + this.direction + " Last Dir: " + this.lastDir);
                }
            } else {
                System.out.println("Turning but doesn't work");
            }
        } else if (state == ROUND_ABOUT_GO) {
            // Handle roundabout movement
            Vehicle old = new Vehicle(this);
            curRoundabout.availableSpots[roundAboutPos] = true;
            roundAboutPos = (roundAboutPos + 1) % 4;
            curRoundabout.availableSpots[roundAboutPos] = false;
            System.out.println(Arrays.toString(curRoundabout.getAvailableSpots()));

            // Position vehicle based on roundabout position
            int[] coords = Grid.getRealCoords(this.curRoundabout);
            int roundX = coords[1];
            int roundY = coords[0];

            // Calculate center of the roundabout
            int centerX = roundX + (Grid.GRID_SIZE / 2);
            int centerY = roundY + (Grid.GRID_SIZE / 2);

            // Convert to top-left position for rectangle
            x = centerX - (width / 2);
            y = centerY - (length / 2);

            // Set rotation and direction based on position
            switch (roundAboutPos) {
                case 0: // Right side of roundabout
                    this.curRotation = 90;  // Facing right
                    this.direction = RIGHT;
                    if (directionPath.get(0) == RIGHT) {
                        this.state = FORWARD;
                        curRoundabout.availableSpots[roundAboutPos] = true;
                    }
                    break;
                case 1: // Top of roundabout
                    this.curRotation = 0;   // Facing up
                    this.direction = UP;
                    if (directionPath.get(0) == UP) {
                        this.state = FORWARD;
                        curRoundabout.availableSpots[roundAboutPos] = true;
                    }
                    break;
                case 2: // Left side of roundabout
                    this.curRotation = 270; // Facing left
                    this.direction = LEFT;
                    if (directionPath.get(0) == LEFT) {
                        this.state = FORWARD;
                        curRoundabout.availableSpots[roundAboutPos] = true;
                    }
                    break;
                case 3: // Bottom of roundabout
                    this.curRotation = 180; // Facing down
                    this.direction = DOWN;
                    if (directionPath.get(0) == DOWN) {
                        this.state = FORWARD;
                        curRoundabout.availableSpots[roundAboutPos] = true;
                    }
                    break;
            }

            // offset
            switch (roundAboutPos) {
                case 0: // Right side
                    x += 10;
                    break;
                case 1: // Top
                    y -= 10;
                    break;
                case 2: // Left side
                    x -= 10;
                    break;
                case 3: // Bottom
                    y += 10;
                    break;
            }

            return new Step(old, new Vehicle(this));
        }
        return null;
    }


    /**
     * Releases the vehicle from a stop sign.
     * Enhanced to properly handle turning.
     */
    public void stopSignLetGo() {
        // Check if we need to turn based on the next direction in our path
        boolean shouldTurn = false;
        Direction nextDirection = null;

        if (directionPath.size() > 1) {
            nextDirection = directionPath.get(1);
            shouldTurn = nextDirection != direction;
        }

        if (state == STOPPED_FORWARD || (state == STOPPED_TURNING && !shouldTurn)) {
            // Change state to ensure the vehicle starts moving
            this.state = FORWARD;
            // Move the vehicle slightly forward past the stop line
            moveForwardAfterStop();
        } else if (state == STOPPED_TURNING || shouldTurn) {
            // For turns, set the turning state and let the turn handler take over
            this.state = TURNING;
            turnPositionSet = false; // Will be set in turnLeft/turnRight methods

            // Ensure we're using the correct direction for determining turn direction
            if (shouldTurn && nextDirection != null) {
                // The vehicle needs to know it's turning
                System.out.println("Vehicle turning at stop sign from " + direction + " to " + nextDirection);
            }
        }
        currentIntersection = null;

        // Update path information
        if (!intersectionPath.isEmpty()) {
            System.out.println("Removed: " + intersectionPath.get(0) + " by " + this);
            intersectionPath.remove(0);
        }
        if (!directionPath.isEmpty()) {
            lastDir = directionPath.remove(0);
            direction = directionPath.get(0);
            System.out.println("New Direction = " + direction);
        }

        // Ensure car is visible and active
        car.setVisible(true);

        // Set a moderate starting speed
        speed = 5;
    }

    /**
     * Repositions the vehicle after a stop sign or traffic light.
     * Fixed to handle coordinate system correctly and ensure smooth movement.
     */
    private void moveForwardAfterStop() {
        int moveDistance = 12; // Distance to move after stopping

        // Move vehicle forward in the current direction
        switch (direction) {
            case UP:
                // Move upward (negative Y)
                y -= moveDistance;
                break;
            case DOWN:
                // Move downward (positive Y)
                y += moveDistance;
                break;
            case LEFT:
                // Move left (negative X)
                x -= moveDistance;
                break;
            case RIGHT:
                // Move right (positive X)
                x += moveDistance;
                break;
        }

        // Set a moderate starting speed to ensure smooth acceleration
        speed = 8;
    }

    /**
     * Releases the vehicle from a traffic light.
     * Enhanced to ensure proper state transitions and prevent getting stuck.
     */
    public void stopLightLetGo() {
        System.out.println("stopSignLetGo called on vehicle at [" + x + "," + y +
            "], state: " + state + ", direction: " + direction);

        // Verify we have a valid path
        if (directionPath == null || directionPath.isEmpty()) {
            System.out.println("WARNING: Vehicle has no direction path!");
            // Set to FORWARD state to prevent getting stuck
            this.state = FORWARD;
            speed = 5;
            return;
        }

        // Print current path information for debugging
        System.out.println("Current path info - path size: " + directionPath.size() +
            ", current direction: " + direction);
        if (directionPath.size() > 1) {
            System.out.println("Next direction in path: " + directionPath.get(1));
        }

        // Check if we need to turn based on the next direction in our path
        Direction nextDirection = null;

        if (state == STOPPED_FORWARD) {
            // Change state to ensure the vehicle starts moving
            CarState oldState = this.state;
            this.state = FORWARD;
            System.out.println("State transition: " + oldState + " -> " + this.state);

            // Move the vehicle slightly forward past the stop line
            int oldX = x;
            int oldY = y;
            moveForwardAfterStop();
            speed = 5;
            System.out.println("Position change: [" + oldX + "," + oldY + "] -> [" + x + "," + y + "]");
            return;
        } else if (state == STOPPED_TURNING) {
            // For turns, set the turning state and let the turn handler take over
            CarState oldState = this.state;
            this.state = TURNING;
            System.out.println("State transition: " + oldState + " -> " + this.state);

            turnPositionSet = false; // Will be set in turnLeft/turnRight methods

        } else {
            System.out.println("WARNING: Unexpected vehicle state: " + state);
            // Try to recover by setting to FORWARD state
            this.state = FORWARD;
            speed = 5;
        }


        // Ensure car is visible and active
        car.setVisible(true);

        // Set a moderate starting speed
        speed = 5;
        System.out.println("Vehicle speed set to: " + speed);



        // Update path information
        if (!intersectionPath.isEmpty()) {
                System.out.println("Removed: " + intersectionPath.get(0) + " by " + this);
                intersectionPath.remove(0);

        }
        if (!directionPath.isEmpty()) {
            lastDir = directionPath.remove(0);
            direction = directionPath.get(0);
            System.out.println("New Direction = " + direction);
        }
    }


    /**
     * Handles entry into a roundabout.
     *
     * @param r The roundabout to enter
     */
    public void roundAboutGo(Roundabout r) {
        int entryPoint = 0;
        switch (direction) {
            case UP -> entryPoint = 3;
            case RIGHT -> entryPoint = 2;
            case DOWN -> entryPoint = 1;
            case LEFT -> entryPoint = 0;
        }

        int prev = (entryPoint - 1 == -1) ? 3 : entryPoint - 1;
        if (r.getAvailableSpots()[entryPoint] && r.getAvailableSpots()[prev]) {
            System.out.println("Entering slot:" + entryPoint);
            r.getAvailableSpots()[entryPoint] = false;
            curRoundabout = r;
            roundAboutPos = entryPoint;
            state = ROUND_ABOUT_GO;
            if (!intersectionPath.isEmpty()) {
                System.out.println("Removed: " + intersectionPath.get(0) + " by " + this);
                intersectionPath.remove(0);
            }
            if (!directionPath.isEmpty()) {
                lastDir = directionPath.remove(0);
                direction = directionPath.get(0);
            }
        }
    }

    /**
     * Sets the start and end roads for the vehicle's journey.
     *
     * @param r The starting road
     * @param d The destination road
     */
    public void setInOut(Road r, Road d) {
        startRoadID = r.getIntersectionID();
        endRoadID = d.getIntersectionID();
    }

    /**
     * Sets the time delay before the vehicle spawns.
     *
     * @param timeIn The time delay
     */
    public void setTimeIn(int timeIn) {
        this.timeIn = timeIn;
    }

    /**
     * Builds a path from path arrays generated by Dijkstra's algorithm.
     *
     * @param previous Array of previous vertices
     * @param start Start vertex
     * @param target Target vertex
     * @param intersections List of all intersections
     */
    private void getArrayListsFromDjikstras(int[] previous, int start, int target,
                                            ArrayList<GridObject> intersections) {
        if (intersectionPath == null) intersectionPath = new ArrayList<>();
        else intersectionPath.clear();

        if (directionPath == null) directionPath = new ArrayList<>();
        else directionPath.clear();

        // Use a stack to build the path in reverse order
        Stack<GridObject> tempPath = new Stack<>();
        int current = target;

        // Trace back from target to start
        while (current != start) {
            tempPath.push(intersections.get(current));
            current = previous[current];
        }

        // Add the starting point
        tempPath.push(intersections.get(start));

        // Convert stack to path and calculate directions
        intersectionPath.add(tempPath.pop());
        while (!tempPath.isEmpty()) {
            GridObject prev = intersectionPath.get(intersectionPath.size() - 1);
            GridObject next = tempPath.pop();
            intersectionPath.add(next);

            Direction calculatedDirection;
            if (next.getColNum() > prev.getColNum()) {
                calculatedDirection = Direction.RIGHT;
            } else if (next.getColNum() < prev.getColNum()) {
                calculatedDirection = Direction.LEFT;
            } else if (next.getRowNum() > prev.getRowNum()) {
                calculatedDirection = Direction.DOWN;
            } else {
                calculatedDirection = Direction.UP;
            }

            // NEW: Validate direction if next is a one-way road
            if (next instanceof OneWayRoad oneWayRoad) {
                if (oneWayRoad.getDirection() != calculatedDirection) {
                    System.out.println("WARNING: Path contains wrong direction on one-way road!");
                    // Handle the error - either skip this road or find alternative
                }
            }

            directionPath.add(calculatedDirection);
        }
        System.out.println("Path found!");
    }

    /**
     * Implements Dijkstra's algorithm to find the shortest path.
     *
     * @param adjMatrix Adjacency matrix representing the road network
     * @param startID Start vertex ID
     * @param target Target vertex ID
     * @param intersections List of all intersections
     */
    private void modifiedDjikstras(int[][] adjMatrix, int startID, int target, ArrayList<GridObject> intersections) {
        int n = adjMatrix.length;
        int[] distance = new int[n];
        boolean[] visited = new boolean[n];
        int[] previous = new int[n];

        // Initialize arrays
        for (int i = 0; i < n; i++) {
            distance[i] = Integer.MAX_VALUE;
            visited[i] = false;
            previous[i] = -1;
        }

        // Start vertex has distance 0
        distance[startID] = 0;

        // Process each vertex
        for (int count = 0; count < n - 1; count++) {
            int u = minDistance(distance, visited, n);

            // Early termination if target reached
            if (u == target) {
                break;
            }

            visited[u] = true;

            // Update distances to adjacent vertices
            for (int v = 0; v < n; v++) {
                if (!visited[v] && adjMatrix[u][v] != 0 &&
                    distance[u] != Integer.MAX_VALUE &&
                    distance[u] + adjMatrix[u][v] < distance[v]) {

                    distance[v] = distance[u] + adjMatrix[u][v];
                    previous[v] = u;
                }
            }
        }

        // Build the path from the results
        getArrayListsFromDjikstras(previous, startID, target, intersections);
    }

    /**
     * Finds the vertex with minimum distance.
     *
     * @param distance Array of distances
     * @param visited Array of visited flags
     * @param n Number of vertices
     * @return Index of the vertex with minimum distance
     */
    private int minDistance(int[] distance, boolean[] visited, int n) {
        int min = Integer.MAX_VALUE;
        int minIndex = -1;

        for (int v = 0; v < n; v++) {
            if (!visited[v] && distance[v] <= min) {
                min = distance[v];
                minIndex = v;
            }
        }
        return minIndex;
    }

    /**
     * Finds the shortest path from start to end using Dijkstra's algorithm.
     *
     * @param adjMatrix Adjacency matrix representing the road network
     * @param intersections List of all intersections
     */
    public void findPath(int[][] adjMatrix, ArrayList<GridObject> intersections) {
        System.out.println("Dijkstra's Alg: ");
        System.out.println("Start x: " + intersections.get(startRoadID).getColNum() + " y: " + intersections.get(startRoadID).getRowNum());
        System.out.println("End x: " + intersections.get(endRoadID).getColNum() + " y: " + intersections.get(endRoadID).getRowNum());
        modifiedDjikstras(adjMatrix, startRoadID, endRoadID, intersections);
    }

    /**
     * Gets the coordinates in front of the vehicle.
     *
     * @return Array [x, y] of coordinates in front of the vehicle
     */
    private int[] front() {
        return front(0);
    }

    /**
     * Gets the coordinates to the left of the vehicle.
     *
     * @return Array [x, y] of coordinates to the left of the vehicle
     */
    private int[] left() {
        return left(0);
    }

    /**
     * Gets the coordinates to the right of the vehicle.
     *
     * @return Array [x, y] of coordinates to the right of the vehicle
     */
    private int[] right() {
        return right(0);
    }

    /**
     * Gets the coordinates in front of the vehicle with an offset.
     * Accounts for vehicle's central rotation.
     *
     * @param delta Distance offset
     * @return Array [x, y] of coordinates in front of the vehicle
     */
    private int[] front(int delta) {
        // Calculate the center of the vehicle
        int centerX = x + width/2;
        int centerY = y + length/2;

        // Calculate the position in front based on direction
        switch (direction) {
            case RIGHT:
                return new int[]{centerX + length/2 + delta, centerY};
            case LEFT:
                return new int[]{centerX - length/2 - delta, centerY};
            case UP:
                return new int[]{centerX, centerY - length/2 - delta};
            default: // DOWN
                return new int[]{centerX, centerY + length/2 + delta};
        }
    }

    /**
     * Gets the coordinates in front of the vehicle with an offset.
     * Accounts for vehicle's central rotation.
     *
     * @param delta Distance offset
     * @return Array [x, y] of coordinates in front of the vehicle
     */
    private int[] back(int delta) {
        // Calculate the center of the vehicle
        int centerX = x + width/2;
        int centerY = y + length/2;

        // Calculate the position in front based on direction
        switch (direction) {
            case RIGHT:
                return new int[]{centerX - length/2 - delta, centerY};
            case LEFT:
                return new int[]{centerX + length/2 + delta, centerY};
            case UP:
                return new int[]{centerX, centerY + length/2 + delta};
            case DOWN: // DOWN
                return new int[]{centerX, centerY - length/2 - delta};
            default:
                return null;
        }
    }

    /**
     * Gets the coordinates to the left of the vehicle with an offset.
     * Accounts for vehicle's central rotation.
     *
     * @param delta Distance offset
     * @return Array [x, y] of coordinates to the left of the vehicle
     */
    private int[] left(int delta) {
        // Calculate the center of the vehicle
        int centerX = x + width/2;
        int centerY = y + length/2;

        // Calculate the position to the left based on direction
        switch (direction) {
            case RIGHT:
                return new int[]{centerX, centerY - width/2 - delta};
            case LEFT:
                return new int[]{centerX, centerY + width/2 + delta};
            case UP:
                return new int[]{centerX - width/2 - delta, centerY};
            default: // DOWN
                return new int[]{centerX + width/2 + delta, centerY};
        }
    }

    /**
     * Gets the coordinates to the right of the vehicle with an offset.
     * Accounts for vehicle's central rotation.
     *
     * @param delta Distance offset
     * @return Array [x, y] of coordinates to the right of the vehicle
     */
    private int[] right(int delta) {
        // Calculate the center of the vehicle
        int centerX = x + width/2;
        int centerY = y + length/2;

        // Calculate the position to the right based on direction
        switch (direction) {
            case RIGHT:
                return new int[]{centerX, centerY + width/2 + delta};
            case LEFT:
                return new int[]{centerX, centerY - width/2 - delta};
            case UP:
                return new int[]{centerX + width/2 + delta, centerY};
            default: // DOWN
                return new int[]{centerX - width/2 - delta, centerY};
        }
    }

    // Getters and setters

    /**
     * Gets the vehicle's direction.
     *
     * @return The current direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Gets the vehicle's state.
     *
     * @return The current state
     */
    public CarState getState() {
        return state;
    }

    /**
     * Gets the vehicle's visual representation.
     *
     * @return The Rectangle representing the vehicle
     */
    public Rectangle getCar() {
        return car;
    }

    /**
     * Sets the vehicle's visual representation.
     *
     * @param car The Rectangle to use
     */
    public void setCar(Rectangle car) {
        this.car = car;
    }

    /**
     * Gets the vehicle's length.
     *
     * @return The length
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the vehicle's width.
     *
     * @return The width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the vehicle's x-coordinate.
     *
     * @return The x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the vehicle's y-coordinate.
     *
     * @return The y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the vehicle's current rotation in degrees.
     *
     * @return The rotation in degrees
     */
    public int getCurRotation() {
        return curRotation;
    }
}