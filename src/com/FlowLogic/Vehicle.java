package com.FlowLogic;

import javafx.scene.shape.Rectangle;

import java.util.*;

import static com.FlowLogic.CarState.*;
import static com.FlowLogic.Direction.*;

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
    private static final int SLOW_DECEL = 2;
    private static final int FAST_DECEL = 6;
    private static final int ACCEL = 3;
    private static final int STOP_LINE_DISTANCE = 20; // Increased to fix positioning at stop line

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
                    if (distance > 0) {
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
                    } else {
                        // Emergency stop if distance is zero or negative (collision)
                        speed = 0;
                    }

                    return true;
                }
            }
        }

        // Check for traffic lights ahead - scan farther ahead for higher speeds
        int lookAheadDistance = Math.max(64, ((speed / 5) + 1) * 32);

        for (int i = 0; i < lookAheadDistance; i += 16) {  // Smaller step size for more precise detection
            if (getCurrentGridObject(g, front(i)) instanceof StopLight light) {
                int lightState = getLightStateForDirection(light);

                if (lightState == light.RED) {
                    // Original code for stopping at red light when not in intersection
                    if (i < 20) { // Very close to the light
                        // Only stop if not already in intersection and not already stopped at this light
                        if (lastStopped != light) {
                            // Store the intersection coordinates for future use
                            int[] coords = Grid.getRealCoords(light);
                            lastIntersectionX = coords[1];
                            lastIntersectionY = coords[0];

                            lastStopped = light;
                            speed = 0;
                            currentIntersection = light;

                            // Position the vehicle precisely at the stop line
                            positionAtStopLine(light);

                            // Set appropriate state based on path
                            if (directionPath.size() > 1 && directionPath.get(1) != direction) {
                                state = STOPPED_TURNING;
                            } else {
                                state = STOPPED_FORWARD;
                            }

                            // Add the vehicle to the appropriate queue
                            light.addToQueue(this);
                        }
                    } else {
                        // Gradual deceleration as we approach
                        speed = i / 3;
                        if (speed < 5 && i > 32) speed = 5; // Maintain minimum speed unless very close
                    }
                    return true;
                } else if (lightState == light.YELLOW) {

                    // Calculate if we can stop in time based on physics
                    int stoppingDistance = (speed * speed) / (2 * FAST_DECEL);
                    boolean canStop = i > stoppingDistance;

                    // If already in intersection or too close to stop safely, proceed
                    if (!canStop) {
                        // Already committed to intersection - proceed through
                        // Maintain speed or slightly reduce if necessary
                        if (speed > 15) {
                            speed -= SLOW_DECEL / 2;  // Minor speed reduction for safety
                        }
                        // Ensure minimum speed to clear intersection
                        if (speed < 10) {
                            speed = 10;
                        }
                    } else {
                        // Can stop safely - gradually decelerate
                        if (i < 96) { // Within three grid cells
                            // More significant deceleration for yellow when closer
                            // Gradual deceleration as we approach
                            speed = i / 3;
                            if (speed < 5 && i > 32) speed = 5; // Maintain minimum speed unless very close

                            if (i < 20 && lastStopped != light) { // Very close, just stop
                                // Store intersection coordinates for future use
                                int[] coords = Grid.getRealCoords(light);
                                lastIntersectionX = coords[1];
                                lastIntersectionY = coords[0];

                                lastStopped = light;
                                speed = 0;
                                currentIntersection = light;

                                // Position vehicle precisely at stop line
                                positionAtStopLine(light);

                                // Set appropriate state based on path
                                if (directionPath.size() > 1 && directionPath.get(1) != direction) {
                                    state = STOPPED_TURNING;
                                } else {
                                    state = STOPPED_FORWARD;
                                }

                                // Add vehicle to appropriate queue
                                light.addToQueue(this);
                            }
                        } else {
                            // More distant, moderate deceleration
                            // Gradual deceleration as we approach
                            speed = i / 3;
                            if (speed < 5 && i > 32) speed = 5; // Maintain minimum speed unless very close
                        }
                    }
                }

                // If light is green, no need to decelerate for it
            }
        }

        // Stop sign handling
        if (getCurrentGridObject(g, front(5)) instanceof StopSign s) {
            if (lastStopped != s) {
                // Store the intersection coordinates for future use
                int[] coords = Grid.getRealCoords(s);
                lastIntersectionX = coords[1];
                lastIntersectionY = coords[0];

                lastStopped = s;
                speed = 0;
                s.getQueue().add(this);
                if (directionPath.size() > 1 && directionPath.get(1) != direction) {
                    state = STOPPED_TURNING;
                } else {
                    state = STOPPED_FORWARD;
                }
                return true;
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
                directionPath.remove(0);
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
     * Fixed to correctly handle JavaFX coordinate system and Rectangle positioning.
     *
     * @param light The traffic light to stop at
     */
    private void positionAtStopLine(StopLight light) {
        int[] lightCoords = Grid.getRealCoords(light);
        int lightX = lightCoords[1];  // Column coordinate
        int lightY = lightCoords[0];  // Row coordinate
        int gridSize = Grid.GRID_SIZE;

        // GridSize/2 is the center of the intersection cell
        int centerX = lightX + gridSize/2;
        int centerY = lightY + gridSize/2;

        // Store current position before adjustment to prevent teleporting
        int oldX = x;
        int oldY = y;


        // Calculate the desired stop position based on approach direction
        int targetX = oldX;
        int targetY = oldY;

        switch (direction) {
            case UP:
                // Coming from bottom to top (negative Y direction)
                // Stop at bottom of intersection
                targetX = centerX - width/2;  // Center horizontally
                targetY = lightY + gridSize + STOP_LINE_DISTANCE - length;  // Position at stop line
                break;

            case DOWN:
                // Coming from top to bottom (positive Y direction)
                // Stop at top of intersection
                targetX = centerX - width/2;  // Center horizontally
                targetY = lightY - STOP_LINE_DISTANCE;  // Position at stop line
                break;

            case LEFT:
                // Coming from right to left (negative X direction)
                // Stop at right of intersection
                targetX = lightX + gridSize + STOP_LINE_DISTANCE - length;  // Position at stop line
                targetY = centerY - width/2;  // Center vertically
                break;

            case RIGHT:
                // Coming from left to right (positive X direction)
                // Stop at left of intersection
                targetX = lightX - length;  // Position at stop line
                targetY = centerY - width/2;  // Center vertically
                break;
        }

        // Calculate how far we need to move
        int deltaX = targetX - oldX;
        int deltaY = targetY - oldY;


        // Apply the limited adjustment
        x = oldX + deltaX;
        y = oldY + deltaY;
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
        int[] otherBack;

        // Calculate the back of the other vehicle based on its direction
        switch (other.direction) {
            case UP -> otherBack = new int[]{other.x, other.y + other.length};
            case DOWN -> otherBack = new int[]{other.x, other.y - other.length};
            case LEFT -> otherBack = new int[]{other.x + other.length, other.y};
            case RIGHT -> otherBack = new int[]{other.x - other.length, other.y};
            default -> otherBack = new int[]{other.x, other.y};
        }

        // Calculate Euclidean distance between my front and other's back
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

        // Original check for vehicles heading in the same direction
        if (direction != other.direction) return false;

        switch (direction) {
            case UP:
                return other.y < y && Math.abs(other.x - x) < width;
            case DOWN:
                return other.y > y && Math.abs(other.x - x) < width;
            case LEFT:
                return other.x < x && Math.abs(other.y - y) < length;
            case RIGHT:
                return other.x > x && Math.abs(other.y - y) < length;
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
    private Step turnRight() {
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

            // Update path information
            if (!intersectionPath.isEmpty()) {
                intersectionPath.remove(0);
            }
            if (!directionPath.isEmpty()) {
                directionPath.remove(0);
            }
            if (!directionPath.isEmpty()) {
                direction = directionPath.get(0);
            }

            // Reposition the vehicle according to its new orientation and exit the intersection
            repositionAfterTurn();
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

            // Update path information
            if (!intersectionPath.isEmpty()) {
                intersectionPath.remove(0);
            }
            if (!directionPath.isEmpty()) {
                directionPath.remove(0);
            }
            if (!directionPath.isEmpty()) {
                direction = directionPath.get(0);
            }

            // Reposition the vehicle according to its new orientation and exit the intersection
            repositionAfterTurn();
        }

        return new Step(past, new Vehicle(this));
    }

    /**
     * Repositions the vehicle after completing a turn to ensure it's in the correct lane
     * and fully exits the intersection.
     */
    private void repositionAfterTurn() {
        int gridSize = Grid.GRID_SIZE;
        int exitDistance = 25; // Distance to place vehicle outside intersection

        // Calculate center of intersection
        int centerX = lastIntersectionX + gridSize/2;
        int centerY = lastIntersectionY + gridSize/2;

        // Calculate the position outside the intersection in the new direction
        switch (direction) {
            case UP:
                // Place the vehicle above the intersection (negative Y)
                x = centerX - width/2; // Center horizontally
                y = lastIntersectionY - exitDistance - length; // Position fully above intersection
                break;

            case DOWN:
                // Place the vehicle below the intersection (positive Y)
                x = centerX - width/2; // Center horizontally
                y = lastIntersectionY + gridSize + exitDistance; // Position fully below intersection
                break;

            case LEFT:
                // Place the vehicle to the left of the intersection (negative X)
                x = lastIntersectionX - exitDistance - length; // Position fully left of intersection
                y = centerY - width/2; // Center vertically
                break;

            case RIGHT:
                // Place the vehicle to the right of the intersection (positive X)
                x = lastIntersectionX + gridSize + exitDistance; // Position fully right of intersection
                y = centerY - width/2; // Center vertically
                break;
        }

        // Start with a non-zero speed to ensure movement continues
        speed = 8;
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

            if (directionPath.size() > 1 && directionPath.get(0) != directionPath.get(1)) {
                // We have a turn coming up in our path
                GridObject currentObj = getCurrentGridObject(g);
                if (currentObj instanceof StopLight || currentObj instanceof Intersection) {
                    // We're at an intersection
                    int[] coords = Grid.getRealCoords(currentObj);
                    int intersectionX = coords[1];
                    int intersectionY = coords[0];
                    int gridSize = Grid.GRID_SIZE;

                    // Check if we're inside the intersection
                    boolean inIntersection = false;
                    switch (direction) {
                        case UP:
                            inIntersection = y <= intersectionY + gridSize && y + length >= intersectionY;
                            break;
                        case DOWN:
                            inIntersection = y + length >= intersectionY && y <= intersectionY + gridSize;
                            break;
                        case LEFT:
                            inIntersection = x <= intersectionX + gridSize && x + length >= intersectionX;
                            break;
                        case RIGHT:
                            inIntersection = x + length >= intersectionX && x <= intersectionX + gridSize;
                            break;
                    }

                    if (inIntersection) {
                        // We're in the intersection and need to turn - update state
                        lastIntersectionX = intersectionX;
                        lastIntersectionY = intersectionY;
                        state = TURNING;
                        turnPositionSet = false;
                        System.out.println("Vehicle entering intersection to turn from " + direction + " to " + directionPath.get(1));
                        return new Step(before, new Vehicle(this));
                    }
                }
            }

            // Check if we need to decelerate, otherwise accelerate if below speed limit
            if (!decelerate(g, allVehicles) &&
                getCurrentGridObject(g) instanceof Road &&
                ((Road) getCurrentGridObject(g)).getSpeedLimit() > speed) {
                accelerate();
            }

            // If we're still in FORWARD state after decelerate check, move forward
            if (state == FORWARD) {
                moveForward();
            }

            return new Step(before, new Vehicle(this));
        } else if (state == STOPPED_FORWARD) {
            // Waiting at a traffic light
            if (currentIntersection instanceof StopLight light) {
                // Check if the light has turned green for our direction
                if (getLightStateForDirection(light) == light.GREEN) {
                    // Release the vehicle when light turns green
                    stopLightLetGo();
                    return new Step(new Vehicle(this), new Vehicle(this));
                }
            }
            return new Step(new Vehicle(this), new Vehicle(this));
        } else if (state == STOPPED_TURNING) {
            // Waiting to turn at a traffic light
            if (currentIntersection instanceof StopLight light) {
                // Check if the light has turned green for our direction
                if (getLightStateForDirection(light) == light.GREEN) {
                    // Release the vehicle when light turns green
                    stopLightLetGo();
                    return new Step(new Vehicle(this), new Vehicle(this));
                }
            }
            return null;
        } else if (state == TURNING) {
            // Execute turns based on current and next direction
            if (directionPath.size() > 1) {
                Direction next = directionPath.get(1);
                if ((next == RIGHT && direction == UP)
                    || (next == DOWN && direction == RIGHT)
                    || (next == UP && direction == LEFT)
                    || (next == LEFT && direction == DOWN)) {
                    return this.turnLeft();
                } else if ((direction == RIGHT && next == UP)
                    || (direction == DOWN && next == RIGHT)
                    || (direction == UP && next == LEFT)
                    || (direction == LEFT && next == DOWN)) {
                    return this.turnRight();
                }
            }
        } else if (state == ROUND_ABOUT_GO) {
            // Handle roundabout movement
            Vehicle old = new Vehicle(this);
            curRoundabout.availableSpots[roundAboutPos] = true;
            roundAboutPos += 1;
            roundAboutPos %= 4;
            curRoundabout.availableSpots[roundAboutPos] = false;
            System.out.println(Arrays.toString(curRoundabout.getAvailableSpots()));

            // Position vehicle based on roundabout position
            int coords[] = Grid.getRealCoords(this.curRoundabout);
            int roundX = coords[1];
            int roundY = coords[0];

            switch (roundAboutPos) {
                case 0:
                    x = roundX + 26;
                    y = roundY + 16;
                    this.curRotation = 90;
                    this.direction = RIGHT;
                    if (directionPath.get(0) == RIGHT) {
                        this.state = FORWARD;
                        curRoundabout.availableSpots[roundAboutPos] = true;
                    }
                    break;
                case 1:
                    y = roundY + 6;
                    x = roundX + 16;
                    this.curRotation = 0;
                    this.direction = UP;
                    if (directionPath.get(0) == UP) {
                        this.state = FORWARD;
                        curRoundabout.availableSpots[roundAboutPos] = true;
                    }
                    break;
                case 2:
                    x = roundX + 6;
                    y = roundY + 16;
                    this.curRotation = 270;
                    this.direction = LEFT;
                    if (directionPath.get(0) == LEFT) {
                        this.state = FORWARD;
                        curRoundabout.availableSpots[roundAboutPos] = true;
                    }
                    break;
                case 3:
                    y = roundY + 26;
                    x = roundX + 16;
                    this.curRotation = 180;
                    this.direction = DOWN;
                    if (directionPath.get(0) == DOWN) {
                        this.state = FORWARD;
                        curRoundabout.availableSpots[roundAboutPos] = true;
                    }
                    break;
            }

            return new Step(old, new Vehicle(this));
        }
        return null;
    }

    /**
     * Releases the vehicle from a stop sign.
     */
    public void stopSignLetGo() {
        if (state == STOPPED_FORWARD) {
            this.state = FORWARD;
        } else if (state == STOPPED_TURNING) {
            this.state = TURNING;
        }
        this.speed = 0;
    }

    /**
     * Releases the vehicle from a traffic light.
     * Enhanced to ensure proper state transitions and prevent getting stuck.
     */
    public void stopLightLetGo() {
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
            moveForwardAfterGreenLight();
        } else if (state == STOPPED_TURNING || shouldTurn) {
            // For turns, set the turning state and let the turn handler take over
            this.state = TURNING;
            turnPositionSet = false; // Will be set in turnLeft/turnRight methods

            // Ensure we're using the correct direction for determining turn direction
            if (shouldTurn && nextDirection != null) {
                // The vehicle needs to know it's turning, even if its state wasn't STOPPED_TURNING
                System.out.println("Vehicle turning from " + direction + " to " + nextDirection);
            }
        }

        // Reset intersection references to avoid getting stuck at the same light
        lastStopped = null;
        currentIntersection = null;

        // Ensure car is visible and active
        car.setVisible(true);
    }

    /**
     * Repositions the vehicle after a traffic light turns green.
     * Fixed to handle coordinate system correctly and ensure smooth movement.
     */
    private void moveForwardAfterGreenLight() {
        int moveDistance = 12; // Distance to move after light changes

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

            // Determine direction based on relative positions
            if (next.getColNum() > prev.getColNum()) {
                directionPath.add(Direction.RIGHT);
            } else if (next.getColNum() < prev.getColNum()) {
                directionPath.add(Direction.LEFT);
            } else if (next.getRowNum() > prev.getRowNum()) {
                directionPath.add(Direction.DOWN);
            } else {
                directionPath.add(Direction.UP);
            }
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
        System.out.println(endRoadID);
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
     *
     * @param delta Distance offset
     * @return Array [x, y] of coordinates in front of the vehicle
     */
    private int[] front(int delta) {
        // Get position in front of car based on its orientation
        switch (direction) {
            case RIGHT:
                return new int[]{x + length + delta, y};
            case LEFT:
                return new int[]{x - delta, y};
            case UP:
                return new int[]{x, y - delta};
            default: // DOWN
                return new int[]{x, y + length + delta};
        }
    }

    /**
     * Gets the coordinates to the left of the vehicle with an offset.
     *
     * @param delta Distance offset
     * @return Array [x, y] of coordinates to the left of the vehicle
     */
    private int[] left(int delta) {
        switch (direction) {
            case RIGHT:
                return new int[]{x + (int) (0.5 * length), y - delta};
            case LEFT:
                return new int[]{x - (int) (0.5 * length), y + delta};
            case UP:
                return new int[]{x - delta, y - (int) (0.5 * length)};
            default: // DOWN
                return new int[]{x + delta, y + (int) (0.5 * length)};
        }
    }

    /**
     * Gets the coordinates to the right of the vehicle with an offset.
     *
     * @param delta Distance offset
     * @return Array [x, y] of coordinates to the right of the vehicle
     */
    private int[] right(int delta) {
        switch (direction) {
            case RIGHT:
                return new int[]{x + (int) (0.5 * length), y + width + delta};
            case LEFT:
                return new int[]{x - (int) (0.5 * length), y - width - delta};
            case UP:
                return new int[]{x + width + delta, y - (int) (0.5 * length)};
            default: // DOWN
                return new int[]{x - width - delta, y + (int) (0.5 * length)};
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