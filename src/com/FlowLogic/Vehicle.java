package com.FlowLogic;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Random;

import java.util.ArrayList;

public class Vehicle {

    ArrayList<Intersection> intersectionPath;
    ArrayList<Direction> directionPath;

    int startRoadID;
    int endRoadID;

    public Vehicle() {
    }


    private void getArrayListsFromDjikstras(int[] previous, int start, int target,
                                            ArrayList<Intersection> intersections) {
        while (target != start) {
            // add target intersection
            intersectionPath.add(0, intersections.get(target));
            // TODO: figure out directions

            target = previous[target];
        }
    }

    private void modifiedDjikstras(int[][] adjMatrix, int startID, int target, ArrayList<Intersection> intersections) {
        //set up djikstra's algorithm
        int n = adjMatrix.length;
        int[] distance = new int[n];
        boolean[] visited = new boolean[n];
        int[] previous = new int[n];
        for (int i = 0; i < n; i++) {
            distance[i] = Integer.MAX_VALUE;
            visited[i] = false;
            previous[i] = -1;
        }

        distance[startID] = 0;

        for (int count = 0; count < n - 1; count++) {
            int u = minDistance(distance, visited, n);

            if (u == target) {
                break;
            }

            visited[u] = true;

            // Update distance value of the adjacent vertices
            for (int v = 0; v < n; v++) {
                if (!visited[v] && adjMatrix[u][v] != 0 &&
                    distance[u] != Integer.MAX_VALUE &&
                    distance[u] + adjMatrix[u][v] < distance[v]) {

                    distance[v] = distance[u] + adjMatrix[u][v];
                    previous[v] = u;
                }
            }
        }
        getArrayListsFromDjikstras(previous, startID, target, intersections);
    }

    private int minDistance(int[] distance, boolean[] visited, int n) {
        int min = Integer.MAX_VALUE;
        int minIndex = -1;

        for (int v = 0; v < n; v++) {
            if (!visited[v] && distance[v] <= min) {
                min = distance[v];
                minIndex = v;
            }
        }
        return minIndex;
    }

    public void findPath(int[][] adjMatrix, ArrayList<Intersection> intersections) {
        modifiedDjikstras(adjMatrix, startRoadID, endRoadID, intersections);

    }
}
