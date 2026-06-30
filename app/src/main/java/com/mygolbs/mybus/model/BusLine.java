package com.mygolbs.mybus.model;

/** 公交线路 */
public class BusLine {
    public String id;
    public String name;
    public String startName;
    public String endName;
    /** 上行还是下行 */
    public String direction;
    /** 首班车时间 */
    public String firstTime;
    /** 末班车时间 */
    public String lastTime;
    /** 间隔分钟 */
    public int interval;
}
