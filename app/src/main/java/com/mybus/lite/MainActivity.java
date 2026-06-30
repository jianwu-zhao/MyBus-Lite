package com.mybus.lite;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends Activity {
    private EditText cityInput;
    private EditText lineInput;
    private TextView resultView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int dp = (int)(14 * getResources().getDisplayMetrics().density + 0.5f);
        LinearLayout root = new LinearLayout(this);
        root.setPadding(dp, dp, dp, dp);
        root.setOrientation(LinearLayout.VERTICAL);
        
        TextView title = new TextView(this);
        title.setText("掌上公交实时查询");
        title.setTextSize(18);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));
        
        cityInput = new EditText(this);
        cityInput.setHint("城市代码 如:02700 福州=0591 简阳=02805");
        cityInput.setText("02700");
        root.addView(cityInput, new LinearLayout.LayoutParams(-1, -2));
        
        lineInput = new EditText(this);
        lineInput.setHint("线路名/关键词");
        lineInput.setText("5路");
        root.addView(lineInput, new LinearLayout.LayoutParams(-1, -2));
        
        Button btn = new Button(this);
        btn.setText("查询");
        root.addView(btn, new LinearLayout.LayoutParams(-1, -2));
        
        ScrollView sv = new ScrollView(this);
        resultView = new TextView(this);
        resultView.setTextSize(12);
        sv.addView(resultView, new ScrollView.LayoutParams(-1, -2));
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));
        
        btn.setOnClickListener(v -> new Thread(() -> {
            try {
                String city = cityInput.getText().toString().trim();
                String kw = lineInput.getText().toString().trim();
                String out = query(city, kw);
                runOnUiThread(() -> resultView.setText(out));
            } catch (Exception e) {
                runOnUiThread(() -> resultView.setText("错误: " + e));
            }
        }).start());
        
        setContentView(root);
    }
    
    private String query(String city, String keyword) throws Exception {
        StringBuilder out = new StringBuilder();
        // 1. 查线路列表
        out.append("=== 线路列表 ===\n");
        out.append(fetchLines(city));
        // 2. 过滤
        if (keyword != null && !keyword.isEmpty()) {
            out.append("\n\n=== 搜索结果 ===\n");
            String raw = fetchLines(city);
            String[] lines = raw.replace("},{", "\n}\n{").split("\n");
            for (String line : lines) {
                if (line.contains(keyword)) {
                    out.append(line).append("\n");
                }
            }
        }
        return out.toString();
    }
    
    private String fetchLines(String city) throws Exception {
        String url = "http://quanguo.mygolbs.com:8081/Lostfound/GetLineServlet";
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(15000);
        c.setReadTimeout(15000);
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream os = c.getOutputStream();
        os.write(("cityCode=" + URLEncoder.encode(city, "UTF-8")).getBytes());
        os.close();
        int code = c.getResponseCode();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream(), "UTF-8"));
        String line;
        StringBuilder sb = new StringBuilder();
        while ((line = r.readLine()) != null) sb.append(line).append('\n');
        r.close();
        c.disconnect();
        return sb.toString();
    }
}
