package com.mygolbs.mybus.api;
import java.io.*;
import java.net.*;

/** 高德地图公交 API 查询示例 */
public class AmapQuery {
    static final String KEY = "2ea29a357cda74eb985978e939d6accb";

    /** 搜索线路 */
    public static String searchLine(String city, String lineName) throws Exception {
        URL u = new URL("http://tsapi.amap.com/v1/bus/search?"
            + "key=" + URLEncoder.encode(KEY, "UTF-8")
            + "&city=" + URLEncoder.encode(city, "UTF-8")
            + "&name=" + URLEncoder.encode(lineName, "UTF-8")
            + "&output=json");
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.setConnectTimeout(5000);
        c.setRequestMethod("GET");
        int code = c.getResponseCode();
        if (code != 200) return "HTTP " + code;
        BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String l;
        while ((l = r.readLine()) != null) sb.append(l);
        r.close();
        return sb.toString();
    }
}
