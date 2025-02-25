package com.FlowLogic;

public class StopLight extends Intersection{
    private int timingOne;
    private int timingTwo;
    private int lightOneColor;
    private int lightTwoColor;
    final int RED = 0;
    final int YELLOW = 1;
    final int GREEN = 2;
    public StopLight(Road roadOne, Road roadTwo, int timingOne, int timingTwo, int lightOneColor, int lightTwoColor) {
        super(roadOne, roadTwo);
        this.lightOneColor = lightOneColor;
        this.lightTwoColor = lightTwoColor;
        this.timingOne = timingOne;
        this.timingTwo = timingTwo;
    }


    /**
     * This functions switches the stop lights by one instance
     * If its on green it will change to yellow for 5 counts
     * If on red it will switch to green once the green light has turned red
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
}
