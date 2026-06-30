package com.mygolbs.mybus.model;

/** 公交站点 */
public class BusStation {
    public String id;
    public String name;
    public double lat;
    public double lng;
    /** 经过线路列表 */
    public String[] lines;
}
