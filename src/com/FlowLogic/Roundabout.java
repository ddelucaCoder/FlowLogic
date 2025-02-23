package com.FlowLogic;
import java.util.List;

/**
 * Class definition file for com.FlowLogic.Roundabout objects
 * Purdue University
 *
 * @author Dylan Mitchell
 * @version February 23, 2025
 */

public class Roundabout implements GridObject {
    int rowNum;
    int colNum;
    List<Boolean> availableSpots;

    /**
     * Class Definition Function
     * @param availableSpots True/False list representing the current spots in the roundabout that are open for cars
     *                       true means the spot is open. false means the spot is taken.
     */
    public Roundabout(List<Boolean> availableSpots) {
        this.availableSpots = availableSpots;
    }

    /**
     * Gets the total number of available spots in the roundabout
     *
     * @return integer representing how many spots are available in the roundabout
     */
    public int getNumAvailableSpots() {
        int numAvailableSpots = 0;
        for (Boolean availableSpot : availableSpots) {
            if (availableSpot) {
                numAvailableSpots++;
            }
        }

        return numAvailableSpots;
    }

    /**
     * Closes a spot in the roundabout
     */
    public void fillSpot() {
        for (int i = 0; i < availableSpots.size(); i++) {
            if (availableSpots.get(i)) {
                availableSpots.set(i, false);
                break;
            }
        }
    }

    /**
     * Opens a spot in the roundabout
     */
    public void openSpot() {
        for (int i = 0; i < availableSpots.size(); i++) {
            if (!availableSpots.get(i)) {
                availableSpots.set(i, true);
                break;
            }
        }
    }

    /*
     * Getter and Setter Methods
     */

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public void setColNum(int colNum) {
        this.colNum = colNum;
    }

    public List<Boolean> getAvailableSpots() {
        return availableSpots;
    }

    public void setAvailableSpots(List<Boolean> availableSpots) {
        this.availableSpots = availableSpots;
    }

    /*
     * toString Method for Debugging Purposes
     */
    public String toString() {
        return "Roundabout{" +
                "rowNum=" + rowNum +
                ", colNum=" + colNum +
                ", availableSpots=" + availableSpots +
                '}';
    }
}
