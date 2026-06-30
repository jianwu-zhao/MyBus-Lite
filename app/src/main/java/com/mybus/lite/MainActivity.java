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
        "线路列表（全国）",
        "线路评分（福州）",
        "失物招领",
        "微信JS-SDK签名",
        "实时站点列表（厦门示例91路）",
        "实时到站预测（厦门示例91路|161|2|7）",
        "用户登录",
        "发送验证码"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int dp = (int)(12 * getResources().getDisplayMetrics().density + 0.5f);
        LinearLayout root = new LinearLayout(this);
        root.setPadding(dp, dp, dp, dp);
        root.setOrientation(LinearLayout.VERTICAL);
        
        TextView title = new TextView(this);
        title.setText("掌上公交真实接口测试\n");
        title.setTextSize(18);
        root.addView(title);
        
        cityInput = new EditText(this);
        cityInput.setHint("城市代码（027=武汉 0592=厦门 0591=福州）");
        cityInput.setText("0592");
        root.addView(cityInput);
        
        keywordInput = new EditText(this);
        keywordInput.setHint("线路名/参数（如：91路|161|2|7）");
        keywordInput.setText("91路");
        root.addView(keywordInput);
        
        modeSelector = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, MODES);
        modeSelector.setAdapter(adapter);
        root.addView(modeSelector);
        
        searchBtn = new Button(this);
        searchBtn.setText("查询掌上公交");
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
                    case 0:
                        result = MyBusApi.getLineList(city);
                        break;
                    case 1:
                        result = MyBusApi.searchBus(kw, 200);
                        break;
                    case 2:
                        result = MyBusApi.getLostList(city, kw, 2);
                        break;
                    case 3:
                        result = MyBusApi.wxSign("https://www.mygolbs.com");
                        break;
                    case 4:
                        result = MyBusApi.getArriveStations(city, kw, 1)
                            + "\n\n=== 返向 ===\n"
                            + MyBusApi.getArriveStations(city, kw, 2);
                        break;
                    case 5: {
                        String[] p = kw.split("\\|");
                        String routeName = p[0], routeId = p.length>1?p[1]:"161";
                        int dir = p.length>2?Integer.parseInt(p[2]):2;
                        int sta = p.length>3?Integer.parseInt(p[3]):7;
                        result = MyBusApi.getArriveTime(city, routeName, routeId, dir, sta);
                        break;
                    }
                    case 6:
                        result = MyBusApi.login("13800138000", "123456");
                        break;
                    case 7:
                        result = MyBusApi.sendCode("13800138000");
                        break;
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
