package com.FlowLogic;
import javafx.scene.image.Image;

import java.util.ArrayDeque;
import java.util.Queue;

public class StopSign extends Intersection {
    private Queue<Vehicle> queue = new ArrayDeque<Vehicle>();
    private final int WAIT_TIME = 5; // This is the wait time for vehicles at the stop sign
    int timer = WAIT_TIME;

    private Image imageFile;

    public StopSign(int rowNum, int colNum, Road[] roads) {
        super(rowNum, colNum, roads);
        this.imageFile = new Image("file:Images/4WayStopSign.png");
    }

    public StopSign(StopSign s) {
        super(s.getRowNum(), s.getColNum(), s.getRoadList());
        this.imageFile = new Image("file:Images/4WayStopSign.png");
    }

    /**
     * Updates the stop sign state for each simulation tick.
     * Decrements timer and releases vehicles from queue when timer reaches zero.
     *
     * @return A Step object (usually null for stop signs)
     */
    public Step tick() {
        timer--;
        if (timer <= 0) {
            timer = WAIT_TIME;
            if (!queue.isEmpty()) {
                Vehicle go = queue.poll(); // Use poll() to remove and return the first vehicle
                go.stopSignLetGo(); // Call the vehicle's method to release it
                System.out.println("StopSign releasing vehicle");
            }
        }
        return null;
    }

    /**
     * This function adds vehicles to the stop sign queue
     *
     * @param vehicle : a vehicle to add to the queue
     */
    public void addToIntersection(Vehicle vehicle) {
        if (!queue.contains(vehicle)) { // Avoid duplicate entries
            if (queue.isEmpty()) {
                timer = WAIT_TIME;
            }
            queue.add(vehicle);
            System.out.println("Vehicle added to stop sign queue, queue size: " + queue.size());
        }
    }

    // Getters and Setters
    public Queue<Vehicle> getQueue() {
        return queue;
    }

    public GridObject clone() {
        return new StopSign(this);
    }

    @Override
    public Image getImageFile() {
        return imageFile;
    }

    @Override
    public void setImageFile(Image imageFile) {
        this.imageFile = imageFile;
    }

    public void setQueue(Queue<Vehicle> queue) {
        this.queue = queue;
    }
}