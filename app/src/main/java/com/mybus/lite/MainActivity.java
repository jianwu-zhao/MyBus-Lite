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
    private EditText cityInput, keywordInput;
    private Button searchBtn;
    private TextView resultView;
    private ProgressBar progress;
    private Spinner modeSelector;

    private static final String[] MODES = {
        "线路列表（按城市）",
        "线路评分（按线路）",
        "实时站点（按城市/线路）",
        "实时到站预估（按线路/方向）",
        "失物招领（按城市）",
        "微信JS-SDK签名"
    };

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

        keywordInput = new EditText(this);
        keywordInput.setHint("线路名（如：91路）");
        keywordInput.setText("91路");
        root.addView(keywordInput);

        modeSelector = new Spinner(this);
        modeSelector.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MODES));
        root.addView(modeSelector);

        searchBtn = new Button(this);
        searchBtn.setText("查询");
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
                String kw = keywordInput.getText().toString().trim();
                int mode = modeSelector.getSelectedItemPosition();
                String result = "";
                StringBuilder sb = new StringBuilder();

                switch (mode) {
                    case 0:
                        // 线路列表：按城市查
                        sb.append("=== 线路列表 ===").append("\n");
                        String body = MyBusApi.getLineList(city);
                        // 解析并格式化显示
                        try {
                            org.json.JSONObject obj = new org.json.JSONObject(body);
                            if (obj.getInt("status") == 1) {
                                org.json.JSONArray lines = obj.getJSONObject("info").getJSONArray("lines");
                                for (int i = 0; i < Math.min(lines.length(), 20); i++) {
                                    org.json.JSONObject line = lines.getJSONObject(i);
                                    sb.append(line.getString("lineName"))
                                      .append(" → ")
                                      .append(line.getString("from"))
                                      .append(" → ")
                                      .append(line.getString("to"))
                                      .append("\n");
                                }
                                if (lines.length() > 20) sb.append("...共").append(lines.length()).append("条线路\n");
                            } else {
                                sb.append("暂不支持该城市\n");
                            }
                        } catch (Exception e) {
                            sb.append("解析失败: ").append(e.getMessage()).append("\n");
                            sb.append("原始返回:\n").append(body.substring(0, Math.min(500, body.length())));
                        }
                        result = sb.toString();
                        break;

                    case 1:
                        // 线路评分
                        sb.append("=== 线路评分 ===").append("\n");
                        String score = MyBusApi.searchBus(kw, 50);
                        try {
                            org.json.JSONObject obj = new org.json.JSONObject(score);
                            if (obj.getString("state").equals("1")) {
                                org.json.JSONArray data = obj.getJSONArray("data");
                                for (int i = 0; i < Math.min(data.length(), 20); i++) {
                                    org.json.JSONObject item = data.getJSONObject(i);
                                    sb.append(item.getString("routeName")).append(" ")
                                      .append("🏆").append(item.getInt("sum")).append("\n");
                                }
                            }
                        } catch (Exception e) {
                            sb.append("原始:\n").append(score.substring(0, Math.min(500, score.length())));
                        }
                        result = sb.toString();
                        break;

                    case 2:
                        // 实时站点列表
                        sb.append("=== 实时到站站点 ===").append("\n");
                        String stations1 = MyBusApi.getArriveStations(city, kw, 1);
                        String stations2 = MyBusApi.getArriveStations(city, kw, 2);
                        sb.append("【方向1】\n").append(formatStationResponse(stations1)).append("\n");
                        sb.append("\n【方向2】\n").append(formatStationResponse(stations2));
                        result = sb.toString();
                        break;

                    case 3:
                        // 实时到站预估
                        sb.append("=== 实时到站预估 ===").append("\n");
                        String[] p = kw.split("\\|");
                        String routeName = p[0];
                        String routeId = p.length > 1 ? p[1] : "161";
                        int dir = p.length > 2 ? Integer.parseInt(p[2]) : 2;
                        int sta = p.length > 3 ? Integer.parseInt(p[3]) : 7;
                        String timeResp = MyBusApi.getArriveTime(city, routeName, routeId, dir, sta);
                        sb.append(formatArriveResponse(timeResp));
                        result = sb.toString();
                        break;

                    case 4:
                        // 失物招领
                        sb.append("=== 失物招领 ===").append("\n");
                        String lost = MyBusApi.getLostList(city, kw, 2);
                        try {
                            org.json.JSONObject obj = new org.json.JSONObject(lost);
                            if (obj.optInt("requeststatus") == 1) {
                                org.json.JSONArray list = obj.optJSONArray("list");
                                if (list != null && list.length() > 0) {
                                    for (int i = 0; i < Math.min(list.length(), 10); i++) {
                                        org.json.JSONObject item = list.getJSONObject(i);
                                        sb.append(item.optString("GoodsType", "未知"))
                                          .append(" - ")
                                          .append(item.optString("companyNameGj", ""))
                                          .append("\n");
                                    }
                                } else {
                                    sb.append("暂无数据\n");
                                }
                            } else {
                                sb.append("暂无数据\n");
                            }
                        } catch (Exception e) {
                            sb.append("返回:\n").append(lost.substring(0, Math.min(300, lost.length())));
                        }
                        result = sb.toString();
                        break;

                    case 5:
                        // 微信JS-SDK签名
                        sb.append("=== 微信JS-SDK签名 ===").append("\n");
                        String wx = MyBusApi.wxSign("https://www.mygolbs.com");
                        try {
                            org.json.JSONObject obj = new org.json.JSONObject(wx);
                            sb.append("appid: ").append(obj.optString("appid")).append("\n");
                            sb.append("status: ").append(obj.optInt("status")).append("\n");
                            sb.append("msg: ").append(obj.optString("msg")).append("\n");
                        } catch (Exception e) {
                            sb.append("返回:\n").append(wx.substring(0, Math.min(300, wx.length())));
                        }
                        result = sb.toString();
                        break;
                }

                final String finalResult = result;
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

    private String formatStationResponse(String json) {
        StringBuilder sb = new StringBuilder();
        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            if (obj.getInt("status") == 1) {
                org.json.JSONArray data = obj.getJSONArray("data");
                for (int i = 0; i < data.length(); i++) {
                    org.json.JSONObject station = data.getJSONObject(i);
                    sb.append(station.getInt("stationOrder")).append(". ")
                      .append(station.getString("stationName")).append("\n");
                }
            } else {
                sb.append("暂无数据\n");
            }
        } catch (Exception e) {
            sb.append("解析失败: ").append(e.getMessage()).append("\n");
            sb.append(json.length() > 0 ? json.substring(0, Math.min(500, json.length())) : "空\n");
        }
        return sb.toString();
    }

    private String formatArriveResponse(String json) {
        StringBuilder sb = new StringBuilder();
        try {
            org.json.JSONObject obj = new org.json.JSONObject(json);
            if (obj.optInt("status") == 1) {
                org.json.JSONObject data = obj.optJSONObject("data");
                if (data != null) {
                    sb.append("预计：" + data.optString("title", "")).append("\n");
                    sb.append("描述：" + data.optString("desc", "")).append("\n");
                    org.json.JSONObject at = data.optJSONObject("at");
                    if (at != null) {
                        sb.append("\n到站信息:\n");
                        sb.append("  估计时间: ").append(at.optString("estimatedTime", "")).append("\n");
                        sb.append("  站数: ").append(at.optInt("staNum")).append("\n");
                        sb.append("  距离: ").append(at.optString("dis", "")).append("\n");
                        sb.append("  平均时间: ").append(at.optString("avgTime", "")).append("\n");
                        sb.append("  拥堵: ").append(at.optString("traffic", "")).append("\n");
                    }
                }
            } else {
                sb.append("暂无数据\n");
            }
        } catch (Exception e) {
            sb.append("解析失败: ").append(e.getMessage()).append("\n");
        }
        return sb.toString();
    }
}
