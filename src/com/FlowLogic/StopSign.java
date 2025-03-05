package com.FlowLogic;
import javafx.scene.image.Image;

import java.util.Queue;
public class StopSign extends Intersection {
    private Queue<Vehicle> queue;



    private Image imageFile;

    public StopSign(int rowNum, int colNum, Road[] roads) {
        super(rowNum, colNum, roads);
        this.imageFile = new Image("file:Images/4WayStopSign");
    }


    /**
     * This function adds vehicles to the stop sign queue
     *
     * @param vehicle : a vehicle to add to the queue
     */
    public void addToIntersection(Vehicle vehicle) {
        queue.add(vehicle);
    }

    /**
     * This function removes the first vehicle from the queue
     * To be used when the proper waiting time for the vehicle as the first
     * car at the stop sign
     *
     * @return boolean
     */
    public boolean goThroughIntersection(Vehicle vehicle) {
        if (!queue.isEmpty()) {
            queue.remove();
            return true;
        }
        return false;
    }

    // Getters and Setters
    public Queue<Vehicle> getQueue() {
        return queue;
    }

    public GridObject clone() {

        return null;
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
