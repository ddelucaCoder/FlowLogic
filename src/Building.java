/*
 * Class definition file for Building objects
 *
 * Purdue University
 *
 * @author Dylan Mitchell
 * @version February 18, 2025
 */
public class Building {
    int xLength, yLength, dailyPopulation;
    public Building(int xLength, int yLength, int dailyPopulation) {
        this.xLength = xLength;
        this.yLength = yLength;
        this.dailyPopulation = dailyPopulation;
    }

    public int getxLength() {
        return xLength;
    }

    public void setxLength(int xLength) {
        this.xLength = xLength;
    }

    public int getyLength() {
        return yLength;
    }

    public void setyLength(int yLength) {
        this.yLength = yLength;
    }

    public int getDailyPopulation() {
        return dailyPopulation;
    }

    public void setDailyPopulation(int dailyPopulation) {
        this.dailyPopulation = dailyPopulation;
    }
}
