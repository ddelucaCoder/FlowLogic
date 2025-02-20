package com.FlowLogic;

public class Intersection {
    // the first road that intersects in the intersection
    Road roadOne;
    // the second road that intersects in the intersection
    Road roadTwo;

    public Intersection(Road roadOne, Road roadTwo) {
        this.roadOne = roadOne;
        this.roadTwo = roadTwo;
    }

    public Road getRoadOne() {
        return roadOne;
    }

    public void setRoadOne(Road roadOne) {
        this.roadOne = roadOne;
    }

    public Road getRoadTwo() {
        return roadTwo;
    }

    public void setRoadTwo(Road roadTwo) {
        this.roadTwo = roadTwo;
    }
}
