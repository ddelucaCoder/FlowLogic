package com.FlowLogic;
import javafx.scene.image.Image;

import java.util.List;

/**
 * Class definition file for com.FlowLogic.Roundabout objects
 * Purdue University
 *
 * @author Dylan Mitchell
 * @version February 23, 2025
 */

public class Roundabout extends Intersection{
    Boolean availableSpots[];



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
        this.availableSpots = new Boolean[4];
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

}
