package com.FlowLogic;

public class Step {
    GridObject oldObject;
    GridObject newObject;

    public Step(GridObject oldObject, GridObject newObject) {
        this.oldObject = oldObject;
        this.newObject = newObject;
    }

    public GridObject getOldObject() {
        return oldObject;
    }

    public void setOldObject(GridObject oldObject) {
        this.oldObject = oldObject;
    }

    public GridObject getNewObject() {
        return newObject;
    }

    public void setNewObject(GridObject newObject) {
        this.newObject = newObject;
    }
}
