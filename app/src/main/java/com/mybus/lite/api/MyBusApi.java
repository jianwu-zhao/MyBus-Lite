package com.mybus.lite.api;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MyBusApi {
    // 掌上公交核心
    public static final String LOSTFOUND = "http://quanguo.mygolbs.com:8081/Lostfound";
    public static final String FUZHOU = "http://0591.mygolbs.com/FuZhou";
    public static final String WX = "https://wx.mygolbs.com";
    public static final String AMAP = "http://tsapi.amap.com";
    public static final String STATIC = "http://static.mygolbs.com";
    
    // 1. 根据城市代码查询线路列表
    public static String lines(String cityCode) throws Exception {
        return post(LOSTFOUND + "/GetLineServlet", "cityCode=" + URLEncoder.encode(cityCode, "UTF-8"));
    }
    
    // 2. 线路评分排行
    public static String ranking(String cityCode, int size) throws Exception {
        return get(FUZHOU + "/ManServlet", "size=" + size);
    }
    
    // 3. 失物招领列表
    public static String lostfound(String cityCode, String keyword, int type) throws Exception {
        return post(LOSTFOUND + "/LostFoundServlet", 
            "citycode=" + URLEncoder.encode(cityCode, "UTF-8") + 
            "&CMD=0&messageType=" + type + 
            "&gjz=" + URLEncoder.encode(keyword, "UTF-8"));
    }
    
    // 4. 失物详情
    public static String lostDetail(String id) throws Exception {
        return post(LOSTFOUND + "/LostFoundServlet", "id=" + URLEncoder.encode(id, "UTF-8") + "&CMD=1");
    }
    
    // 5. 微信签名
    public static String wxSign(String url) throws Exception {
        return get(WX + "/WxBusServer/ApiData.do", 
            "CMD=204&kkk=58581313&signurl=" + URLEncoder.encode(url, "UTF-8"));
    }
    
    // 6. 用户登录
    public static String userLogin(String account, String pass) throws Exception {
        return post(LOSTFOUND + "/userLoginServlet", 
            "account=" + URLEncoder.encode(account, "UTF-8") + 
            "&verificationCode=" + URLEncoder.encode(pass, "UTF-8"));
    }
    
    // 7. 发布/新增
    public static String release(String data) throws Exception {
        return post(LOSTFOUND + "/ReleaseServlet", data);
    }
    
    // 8. 修改
    public static String modify(String data) throws Exception {
        return post(LOSTFOUND + "/ModifyServlet", data);
    }
    
    // 9. 用户信息
    public static String userInfo() throws Exception {
        return post(LOSTFOUND + "/getUserInfo", "");
    }
    
    // 10. 城市信息
    public static String cityInfo(String city) throws Exception {
        return post(LOSTFOUND + "/GetLineServlet", "cityCode=" + URLEncoder.encode(city, "UTF-8"));
    }
    
    private static String post(String url, String data) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(15000);
        c.setReadTimeout(15000);
        c.setRequestProperty("User-Agent", "Mozilla/5.0 MyBus");
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        c.setRequestProperty("Referer", url);
        OutputStream os = c.getOutputStream();
        os.write(data.getBytes("UTF-8"));
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
    
    private static String get(String url, String query) throws Exception {
        String u = url + (url.contains("?") ? "&" : "?") + query;
        HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
        c.setRequestMethod("GET");
        c.setConnectTimeout(15000);
        c.setReadTimeout(15000);
        c.setRequestProperty("User-Agent", "Mozilla/5.0 MyBus");
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
