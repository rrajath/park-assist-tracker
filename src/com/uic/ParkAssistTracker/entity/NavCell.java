package com.uic.ParkAssistTracker.entity;

/**
 * Created by rrajath on 3/6/14.
 */
public class NavCell extends Cell {

    private int navCellId;
    private int fpId;
    private String direction;
    private int xCord;
    private int yCord;

    public void setNavCellId(int id) {
        this.navCellId = id;
    }

    public int getNavCellId() {
        return navCellId;
    }

    public int getFpId() { return fpId; }

    public void setFpId(int fpId) { this.fpId = fpId; }

    public void setDirection(String dir) {
        this.direction = dir;
    }

    public String getDirection() {
        return direction;
    }

    public void setXCord(int x) {
       xCord = x;
    }

    public int getXCord() {
        return xCord;
    }

    public void setYCord(int y) {
        yCord = y;
    }

    public int getYCord() {
        return yCord;
    }

    public String toString() {
        return String.valueOf(getNavCellId() + "   " + ";" + getFpId() + ";" + getDirection() + ";" + getXCord() + ";" + getYCord() + "    ");
    }
}
