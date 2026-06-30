package com.mybus.lite;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import java.io.*;
import java.net.*;
import com.mybus.lite.api.MyBusApi;

public class MainActivity extends Activity {
    private EditText cityInput, keywordInput;
    private Spinner modeSelector;
    private Button searchBtn;
    private TextView resultView;
    private ProgressBar progress;
    private ScrollView scrollView;
    
    // 功能模式
    private static final String[] MODES = {
        "查线路列表",
        "查线路评分",
        "失物招领",
        "微信签名",
        "用户登录"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        int dp = (int)(14 * getResources().getDisplayMetrics().density + 0.5f);
        root.setPadding(dp, dp, dp, dp);
        
        // 城市码
        cityInput = new EditText(this);
        cityInput.setHint("城市代码：02700=武汉 0591=福州 02805=简阳");
        cityInput.setText("02700");
        root.addView(cityInput, new LinearLayout.LayoutParams(-1, -2));
        
        // 关键词
        keywordInput = new EditText(this);
        keywordInput.setHint("线路/关键词，如:5路/1路/武汉");
        keywordInput.setText("5路");
        root.addView(keywordInput, new LinearLayout.LayoutParams(-1, -2));
        
        // 模式选择
        modeSelector = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MODES);
        modeSelector.setAdapter(adapter);
        root.addView(modeSelector, new LinearLayout.LayoutParams(-1, -2));
        
        // 查询按钮
        searchBtn = new Button(this);
        searchBtn.setText("查询掌上公交");
        searchBtn.setOnClickListener(this::search);
        root.addView(searchBtn, new LinearLayout.LayoutParams(-1, -2));
        
        // 进度条
        progress = new ProgressBar(this);
        progress.setVisibility(View.GONE);
        root.addView(progress, new LinearLayout.LayoutParams(-1, -2));
        
        // 结果区域
        scrollView = new ScrollView(this);
        resultView = new TextView(this);
        resultView.setTextSize(12);
        resultView.setText("首次使用前请确认城市代码\n武汉=02700, 福州=0591, 简阳=02805");
        scrollView.addView(resultView, new ScrollView.LayoutParams(-1, -2));
        root.addView(scrollView, new LinearLayout.LayoutParams(-1, 0, 1));
        
        setContentView(root);
    }
    
    private void search(View v) {
        progress.setVisibility(View.VISIBLE);
        searchBtn.setEnabled(false);
        resultView.setText("查询中...\n");
        
        new Thread(() -> {
            try {
                String city = cityInput.getText().toString().trim();
                String keyword = keywordInput.getText().toString().trim();
                int mode = modeSelector.getSelectedItemPosition();
                String result = "";
                
                switch (mode) {
                    case 0: // 查线路列表
                        result = MyBusApi.lines(city);
                        result = formatResult(result, keyword);
                        break;
                    case 1: // 查线路评分
                        result = MyBusApi.ranking(city, 50);
                        break;
                    case 2: // 失物招领
                        result = MyBusApi.lostfound(city, keyword, 2);
                        break;
                    case 3: // 微信签名
                        result = MyBusApi.wxSign("https://www.mygolbs.com");
                        break;
                    case 4: // 用户登录
                        result = MyBusApi.userLogin("", "");
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
        }).start();
    }
    
    private String formatResult(String json, String keyword) {
        if (json == null || json.isEmpty()) return "空结果";
        return json.replace(",", ",\n").replace("{", "{\n").replace("}", "\n}").replace("[", "[\n").replace("]", "\n]");
    }
}
