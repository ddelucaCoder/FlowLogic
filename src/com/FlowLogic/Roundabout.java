package com.FlowLogic;
import javafx.scene.image.Image;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Class definition file for com.FlowLogic.Roundabout objects
 * Purdue University
 *
 * @author Dylan Mitchell
 * @version February 23, 2025
 */

public class Roundabout extends Intersection{
    Boolean availableSpots[] = {true, true, true, true};
    private Queue<Vehicle> queue = new ArrayDeque<Vehicle>();

    private final int WAIT_TIME = 2; // TODO: adjust if necessary
    int timer = WAIT_TIME;

    private Image imageFile;

    /**
     * Class Definition Function
     * @param availableSpots True/False list representing the current spots in the roundabout that are open for cars
     *                       true means the spot is open. false means the spot is taken.
     *                       Index:0 = Right
     *                       Index:1 = North
     *                       Index:2 = 180 degrees
     *                       Index:3 = 3/2 pi radians
     */
    public Roundabout(Boolean[] availableSpots, int row, int col, Road[] roadList) {
        super(row, col, roadList);
        this.imageFile = new Image("file:Images/roundabout.png");
    }

    public Roundabout(Roundabout r) {
        super(r.getRowNum(), r.getColNum(), r.getRoadList());
        this.availableSpots = r.getAvailableSpots();
        this.imageFile = r.getImageFile();
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
    public GridObject clone() {
        return new Roundabout(this);
    }

    /*
     * Getter and Setter Methods
     */



    public Boolean[] getAvailableSpots() {
        return availableSpots;
    }

    public void setAvailableSpots(Boolean[] availableSpots) {
        this.availableSpots = availableSpots;
    }

    public Image getImageFile() {
        return imageFile;
    }
    public void setImageFile(Image imageFile) {
        this.imageFile = imageFile;
    }

    public Queue<Vehicle> getQueue() {
        return queue;
    }


    public Step tick() {
        timer--;
        if (timer <= 0) {
            if (!queue.isEmpty()) {
                timer = WAIT_TIME;
                Vehicle go = queue.remove();
                go.roundAboutGo(this);
            }
        }
        return null;
    }

}
