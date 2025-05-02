package com.FlowLogic;

import javafx.scene.image.Image;
import java.util.LinkedList;
import java.util.Queue;

public class StopLight extends Intersection implements GridObject {
    private int timingOne; // vertical light (top-bottom)
    private int timingTwo; // horizontal light (left-right)
    private int lightOneColor; // vertical light (top-bottom)
    private int lightTwoColor; // horizontal light (left-right)
    final int RED = 0;
    final int YELLOW = 1;
    final int GREEN = 2;
    private int rowNum;
    private int colNum;

    private int YELLOW_TIMING = 5;
    private Image imageFile;
    private Image redGreen4WayImage;
    private Image redYellow4WayImage;
    private Image greenRed4WayImage;
    private Image yellowRed4WayImage;
    private Image allRed4WayImage;

    private int timer = 10;

    // Queues for vehicles waiting at the light
    private Queue<Vehicle> verticalQueue; // For UP/DOWN traffic
    private Queue<Vehicle> horizontalQueue; // For LEFT/RIGHT traffic

    public Vehicle isHolding = null;

    public StopLight(Road roadOne, Road roadTwo, int timingOne, int timingTwo, int lightOneColor, int lightTwoColor, Road[] roadList, int rowNum, int colNum) {
        super(rowNum, colNum, roadList);
        this.lightOneColor = lightOneColor; // Vertical Light
        this.lightTwoColor = lightTwoColor; // Horizontal Light
        this.timingOne = timingOne;
        this.timingTwo = timingTwo;
        this.redGreen4WayImage = new Image("file:Images/RedGreen4WayStopLight.png");
        this.redYellow4WayImage = new Image("file:Images/RedYellow4WayStopLight.png");
        this.greenRed4WayImage = new Image("file:Images/GreenRed4WayStopLight.png");
        this.yellowRed4WayImage = new Image("file:Images/YellowRed4WayStopLight.png");
        this.allRed4WayImage = new Image("file:Images/AllRed4WayStopLight.png");
        this.imageFile = new Image("file:Images/RedGreen4WayStopLight.png");

        // Initialize the vehicle queues
        this.verticalQueue = new LinkedList<>();
        this.horizontalQueue = new LinkedList<>();
    }

    public StopLight(StopLight s) {
        super(s.getRowNum(), s.getColNum(), s.getRoadList());
        this.rowNum = s.getRowNum();
        this.colNum = s.getColNum();
        this.lightOneColor = s.getLightOneColor(); // Vertical Light
        this.lightTwoColor = s.getLightTwoColor(); // Horizontal Light
        this.timingOne = s.getTimingOne();
        this.timingTwo = s.getTimingTwo();
        this.redGreen4WayImage = new Image("file:Images/RedGreen4WayStopLight.png");
        this.redYellow4WayImage = new Image("file:Images/RedYellow4WayStopLight.png");
        this.greenRed4WayImage = new Image("file:Images/GreenRed4WayStopLight.png");
        this.yellowRed4WayImage = new Image("file:Images/YellowRed4WayStopLight.png");
        this.allRed4WayImage = new Image("file:Images/AllRed4WayStopLight.png");
        this.imageFile = s.getImageFile();

        // Copy the queues
        this.verticalQueue = new LinkedList<>(s.getVerticalQueue());
        this.horizontalQueue = new LinkedList<>(s.getHorizontalQueue());
    }

    public void initializeStopLightGraphics() {
        if (lightOneColor == RED) {
            imageFile = redGreen4WayImage;
        } else if (lightOneColor == GREEN) {
            imageFile = greenRed4WayImage;
        }
    }

    public GridObject clone() {
        return new StopLight(this);
    }

    /**
     * This functions switches the stop lights by one instance
     * If its on green it will change to yellow for 5 counts
     * If on red it will switch to green once the green light has turned red
     *
     * @return int : timing for the next light state
     */
    public int switchLights() {
        if (lightOneColor == RED && lightTwoColor == RED) {
            lightOneColor = GREEN;
            releaseVehicles(true, false); // Release vertical traffic
        }
        if (lightOneColor == YELLOW) {
            lightOneColor = RED;
            lightTwoColor = GREEN;
            this.imageFile = getRedGreen4WayImage();
            releaseVehicles(false, true); // Release horizontal traffic
            return timingTwo;
        } else if (lightOneColor == GREEN) {
            lightOneColor = YELLOW;
            lightTwoColor = RED;
            this.imageFile = getRedYellow4WayImage();
            return YELLOW_TIMING;
        } else if (lightTwoColor == YELLOW) {
            lightTwoColor = RED;
            lightOneColor = GREEN;
            this.imageFile = getRedGreen4WayImage();
            releaseVehicles(true, false); // Release vertical traffic
            return timingOne;
        } else if (lightTwoColor == GREEN) {
            lightTwoColor = YELLOW;
            lightOneColor = RED;
            this.imageFile = getRedYellow4WayImage();
            return YELLOW_TIMING;
        }
        return -1;
    }

    /**
     * Releases vehicles from the appropriate queue when the light changes
     * @param releaseVertical true to release vertical traffic (UP/DOWN)
     * @param releaseHorizontal true to release horizontal traffic (LEFT/RIGHT)
     */
    private void releaseVehicles(boolean releaseVertical, boolean releaseHorizontal) {
        if (releaseVertical) {
            while (!verticalQueue.isEmpty()) {
                Vehicle v = verticalQueue.poll();
                v.stopLightLetGo();
            }
        }

        if (releaseHorizontal) {
            while (!horizontalQueue.isEmpty()) {
                Vehicle v = horizontalQueue.poll();
                v.stopLightLetGo();
            }
        }
    }

    /**
     * Add a vehicle to the appropriate queue based on its direction
     * @param vehicle the vehicle to add to the queue
     */
    public void addToQueue(Vehicle vehicle) {
        Direction dir = vehicle.getDirection();
        if (dir == Direction.UP || dir == Direction.DOWN) {
            verticalQueue.add(vehicle);
        } else {
            horizontalQueue.add(vehicle);
        }
    }

    /**
     * Called by the simulation to get and change the state of the light at hand
     * @return Step containing before and after states if there was a change
     */
    public Step tick() {
        timer--;
        if (timer <= 0) {
            StopLight clone = (StopLight) this.clone();
            timer = switchLights();
            return new Step(clone, new StopLight(this));
        }
        return null;
    }

    // Add getters for the queues
    public Queue<Vehicle> getVerticalQueue() {
        return verticalQueue;
    }

    public Queue<Vehicle> getHorizontalQueue() {
        return horizontalQueue;
    }

    // Existing getters and setters...
    @Override
    public Image getImageFile() {
        return imageFile;
    }

    @Override
    public void setImageFile(Image imageFile) {
        this.imageFile = imageFile;
    }

    public int getTimingOne() {
        return timingOne;
    }

    public void setTimingOne(int timingOne) {
        this.timingOne = timingOne;
    }

    public int getTimingTwo() {
        return timingTwo;
    }

    public void setTimingTwo(int timingTwo) {
        this.timingTwo = timingTwo;
    }

    public int getLightOneColor() {
        return lightOneColor;
    }

    public void setLightOneColor(int lightOneColor) {
        this.lightOneColor = lightOneColor;
    }

    public int getLightTwoColor() {
        return lightTwoColor;
    }

    public void setLightTwoColor(int lightTwoColor) {
        this.lightTwoColor = lightTwoColor;
    }

    public int getRowNum() {
        return rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public void setColNum(int newCol) {
        this.colNum = newCol;
    }

    public void setRowNum(int newRow) {
        this.rowNum = newRow;
    }

    public Image getRedGreen4WayImage() {
        return redGreen4WayImage;
    }

    public void setRedGreen4WayImage(Image redGreen4WayImage) {
        this.redGreen4WayImage = redGreen4WayImage;
    }

    public Image getRedYellow4WayImage() {
        return redYellow4WayImage;
    }

    public void setRedYellow4WayImage(Image redYellow4WayImage) {
        this.redYellow4WayImage = redYellow4WayImage;
    }
}