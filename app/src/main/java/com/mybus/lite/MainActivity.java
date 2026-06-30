package com.mybus.lite;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends Activity {
    private EditText cityCodeInput;
    private EditText lineNameInput;
    private TextView resultView;
    private Button searchButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int p = (int)(14 * getResources().getDisplayMetrics().density + 0.5f);
        root.setPadding(p, p, p, p);
        
        cityCodeInput = new EditText(this);
        cityCodeInput.setHint("城市代码，如武汉=02700");
        cityCodeInput.setText("02700");
        root.addView(cityCodeInput, new LinearLayout.LayoutParams(-1, -2));
        
        lineNameInput = new EditText(this);
        lineNameInput.setHint("线路名，如5路");
        lineNameInput.setText("5路");
        root.addView(lineNameInput, new LinearLayout.LayoutParams(-1, -2));
        
        searchButton = new Button(this);
        searchButton.setText("查线路");
        root.addView(searchButton, new LinearLayout.LayoutParams(-1, -2));
        
        resultView = new TextView(this);
        resultView.setTextSize(12);
        root.addView(resultView, new LinearLayout.LayoutParams(-1, 0, 1));
        
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(() -> {
                    try {
                        String result = query();
                        runOnUiThread(() -> resultView.setText(result));
                    } catch (Exception e) {
                        runOnUiThread(() -> resultView.setText("错误: " + e));
                    }
                }).start();
            }
        });
        
        setContentView(root);
    }
    
    private String query() throws Exception {
        String cityCode = cityCodeInput.getText().toString().trim();
        String lineName = lineNameInput.getText().toString().trim();
        String url = "http://quanguo.mygolbs.com:8081/Lostfound/GetLineServlet";
        String post = "cityCode=" + URLEncoder.encode(cityCode, "UTF-8");
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setConnectTimeout(15000);
        c.setReadTimeout(15000);
        c.setRequestProperty("User-Agent", "Mozilla/5.0 MyBus-Lite/1.0");
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        c.getOutputStream().write(post.getBytes("UTF-8"));
        c.getOutputStream().close();
        int code = c.getResponseCode();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line);
        r.close();
        c.disconnect();
        String raw = sb.toString();
        return format(raw);
    }
    
    private String format(String s) {
        if (s == null || s.isEmpty()) return "空";
        s = s.replace(",", ",\n").replace(":", " : ");
        return s;
    }
}
