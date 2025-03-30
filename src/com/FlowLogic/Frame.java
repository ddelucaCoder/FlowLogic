package com.FlowLogic;

import java.util.ArrayList;

public class Frame {
    ArrayList<Step> steps;

    public Frame() {
        this.steps = new ArrayList<Step>();
    }

    public void addStep(Step s) {
        steps.add(s);
    }

    public ArrayList<Step> getSteps() {
        return steps;
    }
}
