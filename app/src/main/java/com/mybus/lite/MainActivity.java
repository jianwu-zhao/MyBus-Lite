package com.mybus.lite;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private EditText input1, input2;
    private Button searchBtn;
    private TextView resultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(14 * getResources().getDisplayMetrics().density + 0.5f);
        root.setPadding(pad, pad, pad, pad);

        input1 = new EditText(this);
        input1.setHint("城市代码（027=武汉/0592=厦门）");
        input1.setText("0592");
        root.addView(input1);

        input2 = new EditText(this);
        input2.setHint("线路名（91路等）");
        input2.setText("91路");
        root.addView(input2);

        searchBtn = new Button(this);
        searchBtn.setText("查询");
        root.addView(searchBtn);

        ScrollView sv = new ScrollView(this);
        resultView = new TextView(this);
        resultView.setTextSize(12);
        resultView.setText("掌上公交接口测试");
        sv.addView(resultView, new ScrollView.LayoutParams(-1, -2));
        root.addView(sv, new LinearLayout.LayoutParams(-1, 1, 1));

        searchBtn.setOnClickListener(v -> new Thread(() -> {
            try {
                String city = input1.getText().toString().trim();
                String line = input2.getText().toString().trim();
                StringBuilder sb = new StringBuilder();

                // 实时到站站点列表
                String url = "https://at.mygolbs.com:38886/ArriveTimes/getStation?cityCode="
                    + java.net.URLEncoder.encode(city, "UTF-8")
                    + "&routeName=" + java.net.URLEncoder.encode(java.net.URLEncoder.encode(line, "UTF-8"), "UTF-8")
                    + "&direction=1";
                java.net.HttpURLConnection c = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                c.setRequestProperty("User-Agent", "Mozilla/5.0");
                int code = c.getResponseCode();
                java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(
                    code >= 200 && code < 400 ? c.getInputStream() : c.getErrorStream(), "UTF-8"));
                String line2;
                while ((line2 = r.readLine()) != null) sb.append(line2).append('\n');
                r.close();
                c.disconnect();

                runOnUiThread(() -> resultView.setText(sb.toString()));
            } catch (Exception e) {
                runOnUiThread(() -> resultView.setText("错误: " + e.toString()));
            }
        }).start());

        setContentView(root);
    }
}
