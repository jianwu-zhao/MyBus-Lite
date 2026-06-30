package com.mybus.lite;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.mybus.lite.api.MyBusApi;

public class MainActivity extends Activity {
    private EditText cityInput, keywordInput;
    private Spinner modeSelector;
    private Button searchBtn;
    private TextView resultView;
    private ProgressBar progress;

    private static final String[] MODES = {
        "1-线路列表", "2-线路评分", "3-失物招领", "4-用户登录",
        "5-验证码", "6-发布", "7-用户信息", "8-微信签名",
        "9-实时到站站点列表", "10-实时到站预测"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int dp = (int)(12 * getResources().getDisplayMetrics().density + 0.5f);
        LinearLayout root = new LinearLayout(this);
        root.setPadding(dp, dp, dp, dp);
        root.setOrientation(LinearLayout.VERTICAL);
        
        TextView title = new TextView(this);
        title.setText("掌上公交逆向工具\n");
        title.setTextSize(18);
        root.addView(title);
        
        cityInput = new EditText(this);
        cityInput.setHint("城市代码：02700=武汉，0592=厦门");
        cityInput.setText("0592");
        root.addView(cityInput);
        
        keywordInput = new EditText(this);
        keywordInput.setHint("线路名；实时预测格式：线路|routeId|方向|站序");
        keywordInput.setText("91路|161|2|7");
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
        root.addView(sv, new LinearLayout.LayoutParams(-1, 0, 1));
        
        searchBtn.setOnClickListener(v -> new Thread(() -> {
            try {
                runOnUiThread(() -> {
                    progress.setVisibility(View.VISIBLE);
                    searchBtn.setEnabled(false);
                    resultView.setText("");
                });
                String city = cityInput.getText().toString().trim();
                String kw = keywordInput.getText().toString().trim();
                int mode = modeSelector.getSelectedItemPosition();
                String result = "";
                
                switch (mode) {
                    case 0: result = MyBusApi.getLineList(city); break;
                    case 1: result = MyBusApi.searchBus(kw, 1); break;
                    case 2: result = MyBusApi.getLostList(city, kw, 2); break;
                    case 3: result = MyBusApi.login("test@test.com", "123456"); break;
                    case 4: result = MyBusApi.sendCode("13800138000"); break;
                    case 5: result = MyBusApi.release(""); break;
                    case 6: result = MyBusApi.getUserInfo(); break;
                    case 7: result = MyBusApi.wxSign("https://www.mygolbs.com"); break;
                    case 8: {
                        String routeName = kw.contains("|") ? kw.split("\\|")[0] : kw;
                        result = MyBusApi.getArriveStations(city, routeName, 1)
                            + "\n\n--- 反方向 ---\n"
                            + MyBusApi.getArriveStations(city, routeName, 2);
                        break;
                    }
                    case 9: {
                        String[] p = kw.split("\\|");
                        String routeName = p.length > 0 ? p[0] : "91路";
                        String routeId = p.length > 1 ? p[1] : "161";
                        int direction = p.length > 2 ? Integer.parseInt(p[2]) : 2;
                        int stationOrder = p.length > 3 ? Integer.parseInt(p[3]) : 7;
                        result = MyBusApi.getArriveTime(city, routeName, routeId, direction, stationOrder);
                        break;
                    }
                }
                
                runOnUiThread(() -> {
                    resultView.setText(result);
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
