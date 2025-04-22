package com.FlowLogic;

import java.util.Stack;

public class CappedStack<E> extends Stack<E> {
    private final int maxSize;

    public CappedStack(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public E push(E item) {
        if (size() >= maxSize) {
            // Remove the bottom element (index 0)
            removeElementAt(0);
        }
        return super.push(item);
    }
}
