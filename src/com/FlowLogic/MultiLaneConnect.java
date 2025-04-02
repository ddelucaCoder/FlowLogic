package com.FlowLogic;

import java.util.ArrayList;
import java.util.List;

public class MultiLaneConnect {


    private List<Road> laneList;

    public MultiLaneConnect() {
        this.laneList = new ArrayList<>();
    }


    public void addRoadToList(Road road) {
        laneList.add(road);
        road.setLaneContainer(this);
    }

    public void removeRoadFromList(Road road) {
        laneList.remove(road);
    }
    public List<Road> getLaneList() {
        return laneList;
    }
}
