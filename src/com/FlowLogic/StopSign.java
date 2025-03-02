package com.FlowLogic;
import java.util.Queue;
public class StopSign extends Intersection {
    private Queue<Vehicle> queue;
    private int rowNum;
    private int colNum;

    public StopSign(int rowNum, int colNum, OneWayRoad one, OneWayRoad two) {
        super(rowNum, colNum,new Road[]{one, two});// Idk if this is right but whatever was already here was wrong
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

    public void setQueue(Queue<Vehicle> queue) {
        this.queue = queue;
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
}
