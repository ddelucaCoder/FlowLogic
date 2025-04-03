package com.FlowLogic;

import java.util.ArrayList;
import java.util.List;

public class MultiLaneConnect {

    private static int totalCount;
    private int count = 0;
    private List<Road> laneList;

    public MultiLaneConnect() {
        totalCount++;
        count = totalCount;
        this.laneList = new ArrayList<>();
    }


    public void addRoadToList(Road road) {
        laneList.add(road);
        road.setLaneContainer(this);
    }

    public void removeRoadFromList(Road road) {
        laneList.remove(road);
    }

    public int getCount() {
        return count;
    }
    public List<Road> getLaneList() {
        return laneList;
    }
}
