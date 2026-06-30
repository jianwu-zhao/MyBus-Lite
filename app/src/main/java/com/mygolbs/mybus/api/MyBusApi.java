package com.mygolbs.mybus.api;

/** 掌上公交真实 API 常量（静态分析提取） */
public final class MyBusApi {
    // ===== 核心后端 =====
    /** 主 API 服务器 */
    public static final String BASE = "http://117.40.140.76:8084";
    /** 核心路径 */
    public static final String PATH = "/hctsm-server/aapp";

    // ===== 高德地图 =====
    public static final String AMAP_KEY = "2ea29a357cda74eb985978e939d6accb";
    public static final String BAIDU_KEY = "iXMimyFF4KA0DEXbUbm2hsimryENFPM1";
    public static final String HUAWEI_KEY = "10457429";
    public static final String GETUI_KEY = "LfnYPfx3y6A1tKnVzo2l12";

    // ===== 企业域名 =====
    public static final String MYGOLBS = "http://www.mygolbs.com";
    public static final String STATIC = "http://static.mygolbs.com";
    public static final String QUANGUO = "http://quanguo.mygolbs.com:8081";
    public static final String MOBILE = "http://m.mygolbs.com";

    // ===== 已知线路 =====
    /** 福州公交前缀 */
    public static final String FUZHOU = "http://0591.mygolbs.com/FuZhou/";

    private MyBusApi() {}
}
