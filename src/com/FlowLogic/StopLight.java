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
    private Image imageFile;
    private Image redGreen4WayImage;
    private Image redYellow4WayImage;
    private Image greenRed4WayImage;
    private Image yellowRed4WayImage;
    private Image allRed4WayImage;

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
    }

    public StopLight(StopLight s) {
        super(s.getRowNum(), s.getColNum(), s.getRoadList());
        this.lightOneColor = s.getLightOneColor(); // Vertical Light
        this.lightTwoColor = s.getLightTwoColor(); // Horizontal Light
        this.timingOne = s.getTimingOne();
        this.timingTwo = s.getTimingTwo();
        this.redGreen4WayImage = new Image("file:Images/RedGreen4WayStopLight.png");
        this.redYellow4WayImage = new Image("file:Images/RedYellow4WayStopLight.png");
        this.greenRed4WayImage = new Image("file:Images/GreenRed4WayStopLight.png");
        this.yellowRed4WayImage = new Image("file:Images/YellowRed4WayStopLight.png");
        this.allRed4WayImage = new Image("file:Images/AllRed4WayStopLight.png");
        this.imageFile = new Image("file:Images/RedGreen4WayStopLight.png");
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
     * @return boolean : was successful
     */
    public boolean switchLights() {
        if (lightOneColor == RED) {
            lightTwoColor = YELLOW;
            imageFile = redYellow4WayImage;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lightTwoColor = RED;
            imageFile = allRed4WayImage;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            lightOneColor = GREEN;
            imageFile = greenRed4WayImage;
        } else {
            if (lightOneColor == GREEN) {
                lightOneColor = YELLOW;
                imageFile = yellowRed4WayImage;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lightOneColor = RED;
                imageFile = allRed4WayImage;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                lightTwoColor = GREEN;
                imageFile = redGreen4WayImage;
            }
        }
        return true;
    }

    // getters and setters
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
