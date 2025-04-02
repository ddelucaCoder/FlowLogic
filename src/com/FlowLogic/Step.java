package com.FlowLogic;

public class Step {
    Object oldObject;
    Object newObject;

    public Step(Object oldObject, Object newObject) {
        this.oldObject = oldObject;
        this.newObject = newObject;
    }

    public Object getOldObject() {
        return oldObject;
    }

    public void setOldObject(Object oldObject) {
        this.oldObject = oldObject;
    }

    public Object getNewObject() {
        return newObject;
    }

    public void setNewObject(Object newObject) {
        this.newObject = newObject;
    }
}
