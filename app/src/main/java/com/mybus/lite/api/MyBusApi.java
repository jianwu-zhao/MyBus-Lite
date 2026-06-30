package com.mybus.lite.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MyBusApi {
    // 掌上公交真实 API 地址
    public static final String BASE_URL = "http://quanguo.mygolbs.com:8081";
    public static final String LOSTFOUND_PATH = "/Lostfound";
    public static final String FUZHOU_PATH = "http://0591.mygolbs.com/FuZhou/";
    
    // 查询线路列表
    public static String getLineList(String cityCode) throws Exception {
        String url = BASE_URL + LOSTFOUND_PATH + "/GetLineServlet";
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(10000);
        c.setReadTimeout(15000);
        c.setRequestProperty("User-Agent", "MyBus-Lite/1.0");
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        c.setRequestProperty("Referer", "http://quanguo.mygolbs.com:8081/Lostfound/");
        OutputStream os = c.getOutputStream();
        os.write(("cityCode=" + URLEncoder.encode(cityCode, "UTF-8")).getBytes("UTF-8"));
        os.close();
        int code = c.getResponseCode();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line);
        r.close();
        c.disconnect();
        return sb.toString();
    }
    
    // 查询线路详情
    public static String getLineDetail(String id) throws Exception {
        String url = BASE_URL + LOSTFOUND_PATH + "/LostFoundServlet";
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(10000);
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream os = c.getOutputStream();
        os.write(("id=" + URLEncoder.encode(id, "UTF-8") + "&CMD=1").getBytes("UTF-8"));
        os.close();
        int code = c.getResponseCode();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line);
        r.close();
        c.disconnect();
        return sb.toString();
    }
    
    // 通过 WxBusServer 查询微信签名
    public static String getWxSign() throws Exception {
        String url = "https://wx.mygolbs.com/WxBusServer/ApiData.do";
        url += "?CMD=204&kkk=58581313&signurl=" + URLEncoder.encode("https://www.mygolbs.com/", "UTF-8");
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        c.setRequestMethod("GET");
        c.setRequestProperty("User-Agent", "Mozilla/5.0");
        int code = c.getResponseCode();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line);
        r.close();
        c.disconnect();
        return sb.toString();
    }
}
