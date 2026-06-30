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
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends Activity {
    private static final String LINE_API = "http://quanguo.mygolbs.com:8081/Lostfound/GetLineServlet";
    private EditText cityCodeInput;
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
        title.setText("掌上公交真实 API 调试版");
        title.setTextSize(20);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        cityCodeInput = new EditText(this);
        cityCodeInput.setHint("城市代码：武汉=02700");
        cityCodeInput.setSingleLine(true);
        cityCodeInput.setText("02700");
        root.addView(cityCodeInput, new LinearLayout.LayoutParams(-1, -2));

        lineInput = new EditText(this);
        lineInput.setHint("线路，如 5路");
        lineInput.setSingleLine(true);
        lineInput.setText("5路");
        root.addView(lineInput, new LinearLayout.LayoutParams(-1, -2));

        searchButton = new Button(this);
        searchButton.setText("查询武汉线路 JSON");
        root.addView(searchButton, new LinearLayout.LayoutParams(-1, -2));

        progressBar = new ProgressBar(this);
        progressBar.setVisibility(View.GONE);
        root.addView(progressBar, new LinearLayout.LayoutParams(-1, -2));

        ScrollView scroll = new ScrollView(this);
        resultView = new TextView(this);
        resultView.setTextSize(12);
        resultView.setText("已验证真实 JSON 接口：\nPOST " + LINE_API + "\n参数：cityCode=02700\n\n点击查询会过滤 5路。\n注意：MyBusWeChatMessage 返回 HTML，不是公交 JSON。");
        scroll.addView(resultView, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        searchButton.setOnClickListener(v -> search());
        setContentView(root);
    }

    private void search() {
        final String cityCode = cityCodeInput.getText().toString().trim();
        final String keyword = lineInput.getText().toString().trim();
        progressBar.setVisibility(View.VISIBLE);
        searchButton.setEnabled(false);
        resultView.setText("请求中...");
        new Thread(() -> {
            String out;
            try {
                String body = requestLines(cityCode);
                out = filterLines(body, keyword);
            } catch (Exception e) {
                out = "请求失败: " + e.getMessage();
            }
            final String finalOut = out;
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                searchButton.setEnabled(true);
                resultView.setText(finalOut);
            });
        }).start();
    }

    private String requestLines(String cityCode) throws Exception {
        String post = "cityCode=" + URLEncoder.encode(cityCode, "UTF-8");
        HttpURLConnection c = (HttpURLConnection) new URL(LINE_API).openConnection();
        c.setConnectTimeout(10000);
        c.setReadTimeout(15000);
        c.setRequestMethod("POST");
        c.setDoOutput(true);
        c.setRequestProperty("User-Agent", "Mozilla/5.0 MyBus-Lite");
        c.setRequestProperty("Referer", "http://quanguo.mygolbs.com:8081/Lostfound/");
        c.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        OutputStream os = c.getOutputStream();
        os.write(post.getBytes("UTF-8"));
        os.close();
        int code = c.getResponseCode();
        BufferedReader r = new BufferedReader(new InputStreamReader(
                code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream(), "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) sb.append(line).append('\n');
        r.close();
        c.disconnect();
        return "URL: " + LINE_API + "\nPOST: " + post + "\nHTTP: " + code + "\n\n" + sb;
    }

    private String filterLines(String raw, String keyword) {
        if (keyword == null || keyword.length() == 0) return pretty(raw);
        StringBuilder out = new StringBuilder();
        out.append("关键词：").append(keyword).append("\n\n");
        String[] parts = raw.replace("},{", "}\n{").split("\n");
        for (String part : parts) {
            if (part.contains(keyword)) out.append(part).append("\n\n");
        }
        if (out.toString().trim().equals("关键词：" + keyword)) {
            out.append("未过滤到，下面显示原始返回：\n").append(pretty(raw));
        }
        return out.toString();
    }

    private String pretty(String s) {
        return s.replace("},{", "},\n{").replace(",\"", ",\n\"");
    }

    private int dp(int v) { return (int) (v * getResources().getDisplayMetrics().density + 0.5f); }
}
