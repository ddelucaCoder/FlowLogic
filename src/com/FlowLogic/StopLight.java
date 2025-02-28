package com.FlowLogic;

import javafx.scene.image.Image;

public class StopLight extends Intersection implements GridObject {
    private int timingOne;
    private int timingTwo;
    private int lightOneColor;
    private int lightTwoColor;
    final int RED = 0;
    final int YELLOW = 1;
    final int GREEN = 2;
    private int rowNum;
    private int colNum;



    private Image redGreen4WayImage;
    private Image redYellow4WayImage;

    public StopLight(Road roadOne, Road roadTwo, int timingOne, int timingTwo, int lightOneColor, int lightTwoColor, Road[] roadList, int rowNum, int colNum) {
        super(rowNum, colNum, roadList);
        this.lightOneColor = lightOneColor;
        this.lightTwoColor = lightTwoColor;
        this.timingOne = timingOne;
        this.timingTwo = timingTwo;
        this.redGreen4WayImage = new Image("file:Images/RedGreen4WayStopLight.png");
        this.redYellow4WayImage = new Image("file:Images/RedYellow4WayStopLight.png");
    }


    /**
     * This functions switches the stop lights by one instance
     * If its on green it will change to yellow for 5 counts
     * If on red it will switch to green once the green light has turned red
     *
     * @return boolean : was successful
     */
    public boolean switchLights() {
        if (lightOneColor == RED) {
            lightTwoColor = YELLOW;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lightTwoColor = RED;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lightOneColor = GREEN;
        } else {
            if (lightOneColor == GREEN) {
                lightOneColor = YELLOW;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lightOneColor = RED;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lightTwoColor = GREEN;
            }
        }
        return true;
    }

    // getters and setters
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
