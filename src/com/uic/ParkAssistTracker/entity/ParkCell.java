package com.uic.ParkAssistTracker.entity;

/**
 * Created by rrajath on 3/6/14.
 */
public class ParkCell extends Cell {

    private int navCellId;
    private int parkCellId;
    private int xCord;
    private int yCord;

    public void setNavCellId(int id) {
        this.navCellId = id;
    }

    public int getNavCellId() {
        return navCellId;
    }

    public void setParkCellId(int id) {
        this.parkCellId = id;
    }

    public int getParkCellId() {
        return parkCellId;
    }

    public void setXCord(int x) {
        this.xCord = x;
    }

    public int getXCord() {
        return xCord;
    }

    public void setYCord(int y) {
        this.yCord = y;
    }

    public int getYCord() {
        return yCord;
    }

    public String toString() {
        return String.valueOf(getParkCellId() + "   " + ";" + getNavCellId() + ";" + getXCord() + ";" + getYCord() + "    ");
    }

}
