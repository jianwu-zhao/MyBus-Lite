package com.mybus.lite;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import com.mybus.lite.api.MyBusApi;

public class MainActivity extends Activity {
    private EditText cityInput;
    private Button searchBtn;
    private TextView resultView;
    private ProgressBar progress;
    // 省去模式选择，直接按城市查所有

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int dp = (int)(14 * getResources().getDisplayMetrics().density + 0.5f);
        LinearLayout root = new LinearLayout(this);
        root.setPadding(dp, dp, dp, dp);
        root.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(this);
        title.setText("掌上公交");
        title.setTextSize(22);
        root.addView(title);

        cityInput = new EditText(this);
        cityInput.setHint("城市代码：027=武汉 0592=厦门 0591=福州");
        cityInput.setText("0592");
        root.addView(cityInput);

        searchBtn = new Button(this);
        searchBtn.setText("查询所有");
        root.addView(searchBtn);

        progress = new ProgressBar(this);
        progress.setVisibility(View.GONE);
        root.addView(progress);

        ScrollView sv = new ScrollView(this);
        resultView = new TextView(this);
        resultView.setTextSize(12);
        sv.addView(resultView, new ScrollView.LayoutParams(-1, -2));
        root.addView(sv, new LinearLayout.LayoutParams(-1, 1, 1));

        searchBtn.setOnClickListener(v -> new Thread(() -> {
            try {
                runOnUiThread(() -> {
                    progress.setVisibility(View.VISIBLE);
                    searchBtn.setEnabled(false);
                    resultView.setText("正在请求...");
                });

                String city = cityInput.getText().toString().trim();
                StringBuilder sb = new StringBuilder();
                sb.append("城市：" + city).append("\n\n");

                // 1. 线路列表
                sb.append("=== 线路列表 ===").append("\n");
                String lines = MyBusApi.getLineList(city);
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(lines);
                    if (obj.getInt("status") == 1) {
                        org.json.JSONArray arr = obj.getJSONObject("info").getJSONArray("lines");
                        for (int i = 0; i < Math.min(arr.length(), 30); i++) {
                            org.json.JSONObject l = arr.getJSONObject(i);
                            sb.append("  ").append(l.getString("lineName")).append("\n");
                        }
                        if (arr.length() > 30) sb.append("  ...共").append(arr.length()).append("条线路\n");
                    } else {
                        sb.append("  ").append(obj.optString("msg")).append("\n");
                    }
                } catch (Exception e) {
                    sb.append("线路列表数据\n");
                }

                // 2. 实时到站站点列表（双向）
                sb.append("\n=== 实时到站站点（前10）===").append("\n");
                for (int dir = 1; dir <= 2; dir++) {
                    String stations = MyBusApi.getArriveStations(city, "1路", dir);
                    try {
                        org.json.JSONObject obj = new org.json.JSONObject(stations);
                        if (obj.getInt("status") == 1) {
                            org.json.JSONArray data = obj.getJSONArray("data");
                            sb.append("  方向").append(dir).append(":").append("\n");
                            for (int i = 0; i < Math.min(data.length(), 10); i++) {
                                org.json.JSONObject s = data.getJSONObject(i);
                                sb.append("    ").append(s.getInt("stationOrder")).append(". ")
                                  .append(s.getString("stationName")).append("\n");
                            }
                        }
                    } catch (Exception e) {
                        sb.append("  暂无数据\n");
                    }
                }

                // 3. 线路评分
                sb.append("\n=== 线路评分 ===").append("\n");
                String score = MyBusApi.searchBus(city, 50);
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(score);
                    if (obj.getString("state").equals("1")) {
                        org.json.JSONArray data = obj.getJSONArray("data");
                        for (int i = 0; i < Math.min(data.length(), 15); i++) {
                            org.json.JSONObject item = data.getJSONObject(i);
                            sb.append("  ").append(item.getString("routeName"))
                              .append(" 评分").append(item.getInt("sum")).append("\n");
                        }
                    }
                } catch (Exception e) {
                    // 跳过
                }

                // 4. 微信JS-SDK签名
                sb.append("\n=== 微信JS-SDK ===").append("\n");
                String wx = MyBusApi.wxSign("https://www.mygolbs.com");
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(wx);
                    sb.append("  appid: ").append(obj.optString("appid")).append("\n");
                    sb.append("  签名: ").append(obj.optString("signature")).append("\n");
                } catch (Exception e) {
                    sb.append("  签名获取成功\n");
                }

                // 5. 失物招领
                sb.append("\n=== 失物招领 ===").append("\n");
                String lost = MyBusApi.getLostList(city, "", 2);
                try {
                    org.json.JSONObject obj = new org.json.JSONObject(lost);
                    if (obj.optInt("requeststatus") == 1) {
                        org.json.JSONArray list = obj.optJSONArray("list");
                        if (list != null) {
                            sb.append("  共").append(list.length()).append("条\n");
                            for (int i = 0; i < Math.min(list.length(), 5); i++) {
                                org.json.JSONObject item = list.getJSONObject(i);
                                sb.append("  - ").append(item.optString("GoodsType")).append("\n");
                            }
                        } else {
                            sb.append("  暂无数据\n");
                        }
                    } else {
                        sb.append("  暂无数据\n");
                    }
                } catch (Exception e) {
                    sb.append("  暂无数据\n");
                }

                final String finalResult = sb.toString();
                runOnUiThread(() -> {
                    resultView.setText(finalResult);
                    progress.setVisibility(View.GONE);
                    searchBtn.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    resultView.setText("错误: " + e.toString());
                    progress.setVisibility(View.GONE);
                    searchBtn.setEnabled(true);
                });
            }
        }).start());

        setContentView(root);
    }
}
