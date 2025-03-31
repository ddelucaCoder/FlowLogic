package com.FlowLogic;

public class Step {
    double xPos;
    double yPos;
    double xInc;
    double yInc;

    public Step(double xPos, double yPos, double xInc, double yInc) {
        this.xPos = xPos;
        this.yPos = yPos;
        this.xInc = xInc;
        this.yInc = yInc;
    }

    public double getxPos() {
        return xPos;
    }

    public double getyPos() {
        return yPos;
    }

    public double getxInc() {
        return xInc;
    }

    public double getyInc() {
        return yInc;
    }
}
