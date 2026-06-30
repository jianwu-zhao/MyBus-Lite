package com.mybus.lite.api;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MyBusApi {
    // 掌上公交核心后端
    public static final String BASE = "http://quanguo.mygolbs.com:8081";
    public static final String LOSTFOUND = BASE + "/Lostfound";
    public static final String FUZHOU = "http://0591.mygolbs.com/FuZhou";
    
    // 1. 获取城市代码（原 APP 内部）
    public static String getCityCode() {
        return "02700";
    }
    
    // 2. 查询线路列表
    public static String getLineList(String cityCode) throws Exception {
        return post(LOSTFOUND + "/GetLineServlet", 
            "cityCode=" + URLEncoder.encode(cityCode, "UTF-8"));
    }
    
    // 3. 搜索公交线路（评分排行）
    public static String searchBus(String routeName, int type) throws Exception {
        return post(FUZHOU + "/ManServlet", 
            "size=200&routeName=" + URLEncoder.encode(routeName, "UTF-8"));
    }
    
    // 4. 获取线路详情
    public static String getLineDetail(String id) throws Exception {
        return post(LOSTFOUND + "/LostFoundServlet", 
            "id=" + URLEncoder.encode(id, " "));
    }
    
    // 5. 失物招领列表
    public static String getLostList(String cityCode, String keyword, int type) throws Exception {
        return post(LOSTFOUND + "/LostFoundServlet", 
            "citycode=" + URLEncoder.encode(cityCode, "UTF-8") + 
            "&CMD=0&messageType=" + type + 
            "&gjz=" + URLEncoder.encode(keyword, "UTF-8"));
    }
    
    // 6. 用户登录
    public static String login(String account, String code) throws Exception {
        return post(LOSTFOUND + "/userLoginServlet", 
            "account=" + URLEncoder.encode(account, "UTF-8") + 
            "&verificationCode=" + URLEncoder.encode(code, "UTF-8"));
    }
    
    // 7. 用户信息
    public static String getUserInfo() throws Exception {
        return post(LOSTFOUND + "/getUserInfo", "");
    }
    
    // 8. 发布/释放
    public static String release(String data) throws Exception {
        return post(LOSTFOUND + "/ReleaseServlet", data);
    }
    
    // 9. 修改
    public static String modify(String data) throws Exception {
        return post(LOSTFOUND + "/ModifyServlet", data);
    }
    
    // 10. 发送验证码
    public static String sendCode(String phone) throws Exception {
        return post(LOSTFOUND + "/sendVerificationCodeServlet",
            "account=" + URLEncoder.encode(phone, "UTF-8"));
    }
    
    private static String post(String url, String data) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(15000);
        c.setReadTimeout(15000);
        c.setRequestProperty("User-Agent", "Mozilla/5.0 MyBus-Lite");
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
        c.setRequestProperty("User-Agent", "Mozilla/5.0 MyBus-Lite");
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
