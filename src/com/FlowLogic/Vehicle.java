package com.FlowLogic;
import javafx.scene.paint.Color;
import javafx.scene.paint.Stop;
import javafx.util.Pair;

import javafx.scene.shape.Rectangle;

import java.util.*;

import java.util.ArrayList;

import static com.FlowLogic.CarState.*;
import static com.FlowLogic.Direction.*;

public class Vehicle {

    int length;
    int width = 10;

    ArrayList<GridObject> intersectionPath;
    ArrayList<Direction> directionPath;

    int startRoadID;
    int endRoadID;

    int timeIn;

    int x;
    int y;

    int speed;

    CarState state;

    Direction direction;

    Intersection currentIntersection;

    int angle = 0;

    int curRotation = 0; // ASSUME THIS IS DEGREES
    Rectangle car;

    private final int TURN_RATE = 10;
    private final int FOLLOWING_DISTANCE = 10;
    private final int SLOW_DECEL = 2;
    private final int FAST_DECEL = 6;

    private final int ACCEL = 3;
    private StopSign lastStopped;



    public Vehicle(int length) {
        this.length = length;

        // Initialize collections as empty ArrayLists
        this.intersectionPath = new ArrayList<GridObject>();
        this.directionPath = new ArrayList<Direction>();

        // Initialize primitive types with default values
        this.startRoadID = 0;
        this.endRoadID = 0;
        this.timeIn = 0;
        this.x = 0;
        this.y = 0;
        this.speed = 0;

        // Initialize object references to null
        this.state = NOT_SPAWNED;
        this.direction = null;
        this.currentIntersection = null;
        Rectangle newCar = new Rectangle(-100, -100, width, length);//Hide of screen

        newCar.setRotate(curRotation);
        newCar.setVisible(true);

        car = newCar;
    }

    public Vehicle(Vehicle v) {
        // Clone the ArrayList collections using new instances
        this.intersectionPath = new ArrayList<>(v.intersectionPath);
        this.directionPath = new ArrayList<>(v.directionPath);

        this.car = v.car;

        this.angle = v.angle;
        this.width = v.width;
        this.length = v.length;
        this.curRotation = v.curRotation;

        // Copy primitive values
        this.startRoadID = v.startRoadID;
        this.endRoadID = v.endRoadID;
        this.timeIn = v.timeIn;
        this.x = v.x;
        this.y = v.y;
        this.speed = v.speed;

        // Copy enum value
        this.state = v.state;
        this.direction = v.direction;

        this.car = v.car;

        // Reference to the same intersection object
        // If deep copying is needed, you'd need a copy constructor for Intersection
        this.currentIntersection = v.currentIntersection;
    }

    private void spawn() {
        // TODO: spawn car in
        // set x and y
        int coords[] = Grid.getRealCoords(this.intersectionPath.get(0));
        int spawnX = coords[1]; // TODO: COORDINATES
        int spawnY = coords[0]; // TODO: COORDINATES
        x = spawnX;
        y = spawnY;
        // set speed
        speed = 0;
        // set direction
        direction = directionPath.get(0);
        // set state
        state = FORWARD;

        // rotate the car

        //ASSUME 0 IS UP
        switch (direction) {
            case UP -> curRotation = 0;
            case RIGHT -> curRotation = 90;
            case DOWN -> curRotation = 180;
            case LEFT -> curRotation = 270;
        }
        car = new Rectangle(x, y, width, length);
    }

    /**
     * This function moves the vehicle forward by the correct amount
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
     * This function gets the object that the car is on top of
     * @param g - the grid object
     * @return the road object that the car is on
     */

    private GridObject getCurrentGridObject(Grid g) {
        return g.getSpotRealCoords(x, y);
    }

    private GridObject getCurrentGridObject(Grid g, int[] coords) {
        return g.getSpotRealCoords(coords[0], coords[1]);
    }


    /**
     * Speeds up the car by the constant amount ACCEL
     */
    private void accelerate() {
        speed += ACCEL;
    }

    private boolean decelerate(Grid g) {
        // check if car in front
        // TODO: Check in front for car
        System.out.println(getCurrentGridObject(g, front(5)));
        if (getCurrentGridObject(g, front(5)) instanceof StopSign s) {
            if (lastStopped != s) {
                lastStopped = s;
                speed = 0;
                s.getQueue().add(this);
                if (directionPath.get(1) != direction) {
                    state = STOPPED_TURNING;
                } else {
                    state = STOPPED_FORWARD;
                }
                return true;
            }
        }
        if (getCurrentGridObject(g, front(5)) instanceof Roundabout r) {
            speed = 0;
            r.getQueue().add(this);
        }
        // check if nearing destination or stop sign
        for (int i = 0; i < ((speed / 10) + 1) * 32; i += 32) {
            if (getCurrentGridObject(g, front(i)) instanceof Road r && r.getIntersectionID() == endRoadID ||
                getCurrentGridObject(g, front(i)) instanceof StopSign j ||
                    getCurrentGridObject(g, front(i)) instanceof Roundabout round) { //
                // TODO: make this smoother (always takes 3 frames until its under 5?)
                // assume dist is i
                if (speed > i / 3) speed = i / 3;
                if (speed < 5) speed = 5;
                return true;
            }
        }


        // TODO: stoplight logic
        return false;
    }

    private Step turnRight() {
        Vehicle past = new Vehicle(this);
        // first move the vehicle to the center of the block

        // our intersection coords
        GridObject i = intersectionPath.get(1);
        int[] intersectionCoords = Grid.getRealCoords(i);

        // move to center
        this.x = intersectionCoords[1] + (int) (0.5 * Grid.GRID_SIZE) - (int) (0.5 * this.length);
        this.y = intersectionCoords[0] + (int) (0.5 * Grid.GRID_SIZE) - (int) (0.5 * this.width);

        curRotation -= TURN_RATE;

        if (curRotation % 90 == 0) {
            // done turning
            state = FORWARD;
            intersectionPath.remove(0);
            directionPath.remove(0);
            direction = directionPath.get(0);
        }

        return new Step(past, new Vehicle(this));
    }

    private Step turnLeft() {
        Vehicle past = new Vehicle(this);
        // first move the vehicle to the center of the block

        // our intersection coords
        GridObject i = intersectionPath.get(1);
        int[] intersectionCoords = Grid.getRealCoords(i);

        // move to center
        this.x = intersectionCoords[1] + (int) (0.5 * Grid.GRID_SIZE) - (int) (0.5 * this.width);
        this.y = intersectionCoords[0] + (int) (0.5 * Grid.GRID_SIZE) - (int) (0.5 * this.length);

        curRotation += TURN_RATE;

        if (curRotation % 90 == 0) {
            // done turning
            state = FORWARD;
            intersectionPath.remove(0);
            directionPath.remove(0);
            direction = directionPath.get(0);
        }

        return new Step(past, new Vehicle(this));
    }


    public Step tick(Grid g) {
        // if at destination
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
            this.timeIn--;
            if (this.timeIn <= 0) {
                this.spawn();
                return new Step(null, new Vehicle(this));
            }
            return null;
        } else if (state == FORWARD) {
            // state before
            Vehicle before = new Vehicle(this);
            if (!decelerate(g) &&
                getCurrentGridObject(g) instanceof Road
                && ((Road) getCurrentGridObject(g)).getSpeedLimit() > speed) {
                accelerate();
            }
            if (state == FORWARD) {
                moveForward();
            }
            return new Step(before, new Vehicle(this));
        } else if (state == STOPPED_FORWARD) {
            if (currentIntersection instanceof StopLight) {
                // TODO: Stoplight logic
                // check if were moving
            }
            return new Step(new Vehicle(this), new Vehicle(this));
        } else if (state == STOPPED_TURNING) {
            if (currentIntersection instanceof StopLight) {
                // TODO: Stoplight logic
            }
            return null;
        } else if (state == TURNING) {
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
        return null;
    }

    public void stopSignLetGo() {
        if (state == STOPPED_FORWARD) {
            this.state = FORWARD;
        } else if (state == STOPPED_TURNING) {
            this.state = TURNING;
        }
        this.speed = 0;
    }
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
            //If it is able to advance into the roundabout (spot clear)
            System.out.println("Entering slot:" + entryPoint);
            r.getAvailableSpots()[entryPoint] = false;
        }
    }

    public void setInOut(Road r, Road d) {
        startRoadID = r.getIntersectionID();
        endRoadID = d.getIntersectionID();
    }

    public void setTimeIn(int timeIn) {
        this.timeIn = timeIn;
    }

    private void getArrayListsFromDjikstras(int[] previous, int start, int target,
                                            ArrayList<GridObject> intersections) {
        // init array lists
        if (intersectionPath == null) intersectionPath = new ArrayList<>();
        else intersectionPath.clear();

        if (directionPath == null) directionPath = new ArrayList<>();
        else directionPath.clear();

        // backwards building of a stack
        Stack<GridObject> tempPath = new Stack<>();
        int current = target;
        while (current != start) {
            tempPath.push(intersections.get(current));
            current = previous[current];
        }

        // Add the start intersection
        tempPath.push(intersections.get(start));

        // Pop from stack to get intersections in correct order
        intersectionPath.add(tempPath.pop());
        while (!tempPath.isEmpty()) {
            GridObject prev = intersectionPath.get(intersectionPath.size() - 1);
            GridObject next = tempPath.pop();
            intersectionPath.add(next);

            // calc direction
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
    }

    private void modifiedDjikstras(int[][] adjMatrix, int startID, int target, ArrayList<GridObject> intersections) {
        //set up djikstra's algorithm
        int n = adjMatrix.length;
        int[] distance = new int[n];
        boolean[] visited = new boolean[n];
        int[] previous = new int[n];
        for (int i = 0; i < n; i++) {
            distance[i] = Integer.MAX_VALUE;
            visited[i] = false;
            previous[i] = -1;
        }

        distance[startID] = 0;

        for (int count = 0; count < n - 1; count++) {
            int u = minDistance(distance, visited, n);

            if (u == target) {
                break;
            }

            visited[u] = true;

            // Update distance value of the adjacent vertices
            for (int v = 0; v < n; v++) {
                if (!visited[v] && adjMatrix[u][v] != 0 &&
                    distance[u] != Integer.MAX_VALUE &&
                    distance[u] + adjMatrix[u][v] < distance[v]) {

                    distance[v] = distance[u] + adjMatrix[u][v];
                    previous[v] = u;
                }
            }
        }
        getArrayListsFromDjikstras(previous, startID, target, intersections);
    }

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

    public void findPath(int[][] adjMatrix, ArrayList<GridObject> intersections) {
        System.out.println(endRoadID);
        modifiedDjikstras(adjMatrix, startRoadID, endRoadID, intersections);
    }

    private int[] front() {
        return front(0);
    }

    private int[] left() {
        return left(0);
    }

    private int[] right() {
        return right(0);
    }


    private int[] front(int delta) {
        // COORDINATES ARE TOP LEFT OF CAR
        // LENGTH = x LENGTH
        // WIDTH = y LENGTH
        if (direction == RIGHT) {
            return new int[]{x + length + delta, y};
        } else if (direction == LEFT) {
            return new int[]{x - length - delta, y};
        } else if (direction == UP) {
            return new int[]{x, y - length - delta};
        } else {
            return new int[]{x, y + (length) + delta};
        }
    }


    private int[] left(int delta) {
        // COORDINATES ARE TOP LEFT OF CAR
        // LENGTH = x LENGTH in right facing orientation
        // WIDTH = y LENGTH
        if (direction == RIGHT) {
            return new int[]{x + (int) (0.5 * length), y - delta};
        } else if (direction == LEFT) {
            return new int[]{x - (int) (0.5 * length), y + delta};
        } else if (direction == UP) {
            return new int[]{x - delta, y - (int) (0.5 * width)};
        } else { // DOWN
            return new int[]{x +  delta, y + (int) (0.5 * width)};
        }
    }

    private int[] right(int delta) {
        // COORDINATES ARE TOP LEFT OF CAR
        // LENGTH = x LENGTH in right facing orientation
        // WIDTH = y LENGTH
        if (direction == RIGHT) {
            return new int[]{x + (int) (0.5 * length), y + width + delta};
        } else if (direction == LEFT) {
            return new int[]{x - (int) (0.5 * length), y - width - delta};
        } else if (direction == UP) {
            return new int[]{x + width + delta, y - (int) (0.5 * length)};
        } else {
            return new int[]{x - width - delta, y - (int) (0.5 * length)};
        }
    }

    public Rectangle getCar() {
        return car;
    }
    public void setCar(Rectangle car) {
        this.car = car;
    }
    public int getLength() {
        return length;
    }
    public int getWidth() {
        return width;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getCurRotation() {
        return curRotation;
    }
}
