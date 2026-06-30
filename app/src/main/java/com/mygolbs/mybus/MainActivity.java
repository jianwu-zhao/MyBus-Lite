package com.mygolbs.mybus;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends Activity {
    private static final String BASE = "https://wx.mygolbs.com";
    private static final String BUS_API = "/WxBusServer/ApiData.do";

    private EditText cityInput;
    private EditText lineInput;
    private TextView resultView;
    private ProgressBar progressBar;
    private Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(14);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("掌上公交 API 调试版");
        title.setTextSize(20);
        title.setPadding(0, 0, 0, dp(12));
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        cityInput = new EditText(this);
        cityInput.setHint("城市，如 福州");
        cityInput.setSingleLine(true);
        cityInput.setText("福州");
        root.addView(cityInput, new LinearLayout.LayoutParams(-1, -2));

        lineInput = new EditText(this);
        lineInput.setHint("线路，如 1路");
        lineInput.setSingleLine(true);
        root.addView(lineInput, new LinearLayout.LayoutParams(-1, -2));

        searchButton = new Button(this);
        searchButton.setText("验证 CMD=204 签名接口");
        root.addView(searchButton, new LinearLayout.LayoutParams(-1, -2));

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        root.addView(progressBar, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        resultView = new TextView(this);
        resultView.setTextSize(12);
        resultView.setText("已验证：CMD=204 是微信 JS-SDK 签名接口，不是公交查询接口。\n\n正确参数：CMD=204&kkk=58581313&signurl=https://www.mygolbs.com/\n\n下一步继续提取真正公交线路 CMD。");
        scroll.addView(resultView, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        searchButton.setOnClickListener(v -> search());
        setContentView(root);
    }

    private void search() {
        progressBar.setVisibility(View.VISIBLE);
        searchButton.setEnabled(false);
        resultView.setText("请求中...");
        new Thread(() -> {
            String out;
            try {
                out = "[已验证接口 WxBusServer/ApiData.do：CMD=204 微信 JS-SDK 签名]\n" + request(buildWxSignUrl());
            } catch (Exception e) {
                out = "接口失败: " + e.getMessage();
            }
            final String finalOut = out;
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                searchButton.setEnabled(true);
                resultView.setText(finalOut);
            });
        }).start();
    }

    private String buildWxSignUrl() throws Exception {
        return BASE + BUS_API + "?CMD=204&kkk=58581313&signurl="
                + URLEncoder.encode("https://www.mygolbs.com/", "UTF-8");
    }

    private String request(String url) throws Exception {
        HttpURLConnection c = (HttpURLConnection) new URL(url).openConnection();
        c.setConnectTimeout(8000);
        c.setReadTimeout(8000);
        c.setRequestMethod("GET");
        c.setRequestProperty("User-Agent", "Mozilla/5.0 MyBus-Lite");
        int code = c.getResponseCode();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line).append('\n');
        r.close();
        c.disconnect();
        return "URL: " + url + "\nHTTP: " + code + "\n" + pretty(sb.toString());
    }

    private String pretty(String s) {
        if (s == null || s.length() == 0) return "<empty>";
        return s.replace("},{", "},\n{").replace(",\"", ",\n\"");
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
}
