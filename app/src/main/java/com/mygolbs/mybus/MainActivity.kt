package com.mygolbs.mybus

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.mygolbs.mybus.api.MyBusApi
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * 掌上公交查询 - 掌上公交后端 API 优先
 *
 * 默认用 117.40.140.76:8084 的掌上公交后端
 * 高德 API 作为备用
 */
class MainActivity : AppCompatActivity() {

    private lateinit var etLine: EditText
    private lateinit var etCity: EditText
    private lateinit var btnSearch: Button
    private lateinit var tvResult: TextView
    private lateinit var pbLoading: ProgressBar

    companion object {
        // 掌上公交后端
        private const val MYBUS_BASE = "http://117.40.140.76:8084"
        private const val MYBUS_PATH = "/hctsm-server/aapp"

        // 高德备用
        private const val AMAP_BASE = "http://tsapi.amap.com"
        private const val AMAP_PATH = "/v1/bus/search"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etLine = findViewById(R.id.et_line)
        etCity = findViewById(R.id.et_city)
        btnSearch = findViewById(R.id.btn_search)
        tvResult = findViewById(R.id.tv_result)
        pbLoading = findViewById(R.id.pb_loading)

        // 默认城市：福州
        etCity.setText("福州")

        btnSearch.setOnClickListener {
            val line = etLine.text.toString().trim()
            val city = etCity.text.toString().trim()
            if (line.isEmpty()) {
                tvResult.text = "请输入公交线路名"
                return@setOnClickListener
            }
            searchBus(line, city)
        }
    }

    private fun searchBus(line: String, city: String) {
        pbLoading.visibility = ProgressBar.VISIBLE
        tvResult.text = "正在查询…"
        btnSearch.isEnabled = false

        Thread {
            try {
                // 先试掌上公交后端
                val result = queryMyBus(line, city)
                runOnUiThread {
                    tvResult.text = result
                    pbLoading.visibility = ProgressBar.GONE
                    btnSearch.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    tvResult.text = "掌上后端错误: ${e.message}\n\n尝试高德备用…"
                    // 备用
                    try {
                        val fallback = queryAmap(line, city)
                        tvResult.text = "高德备用:\n$fallback"
                    } catch (e2: Exception) {
                        tvResult.text = "全部失败: ${e2.message}"
                    }
                    pbLoading.visibility = ProgressBar.GONE
                    btnSearch.isEnabled = true
                }
            }
        }.start()
    }

    /** 掌上公交后端 */
    private fun queryMyBus(line: String, city: String): String {
        val url = "$MYBUS_BASE$MYBUS_PATH/bus/search?" +
                "key=${URLEncoder.encode(MyBusApi.AMAP_KEY, "UTF-8")}" +
                "&city=${URLEncoder.encode(city, "UTF-8")}" +
                "&name=${URLEncoder.encode(line, "UTF-8")}" +
                "&output=json"

        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.requestMethod = "GET"

        val code = conn.responseCode
        if (code != 200) {
            return "掌上后端返回 HTTP $code\n\n尝试高德备用…"
        }

        val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
        val sb = StringBuilder()
        var l: String?
        while (reader.readLine().also { l = it } != null) sb.append(l)
        reader.close()
        conn.disconnect()

        return "掌上后端结果:\n${formatJson(sb.toString())}"
    }

    /** 高德 API 备用 */
    private fun queryAmap(line: String, city: String): String {
        val url = "$AMAP_BASE$AMAP_PATH?" +
                "key=${URLEncoder.encode(MyBusApi.AMAP_KEY, "UTF-8")}" +
                "&city=${URLEncoder.encode(city, "UTF-8")}" +
                "&name=${URLEncoder.encode(line, "UTF-8")}" +
                "&output=json"

        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.requestMethod = "GET"

        val code = conn.responseCode
        val reader = if (code in 200..299) {
            BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
        } else {
            BufferedReader(InputStreamReader(conn.errorStream, "UTF-8"))
        }

        val sb = StringBuilder()
        var l: String?
        while (reader.readLine().also { l = it } != null) sb.append(l)
        reader.close()
        conn.disconnect()

        return "高德返回 ($code):\n${formatJson(sb.toString())}"
    }

    private fun formatJson(str: String): String {
        return try {
            val sb = StringBuilder()
            var indent = 0
            var inString = false
            for (c in str) {
                when {
                    c == '"' -> { sb.append(c); inString = !inString }
                    c == '{' || c == '[' -> {
                        sb.append(c).append('\n')
                        indent++
                        repeat(indent) { sb.append("  ") }
                    }
                    c == '}' || c == ']' -> {
                        indent--
                        sb.append('\n')
                        repeat(indent) { sb.append("  ") }
                        sb.append(c)
                    }
                    c == ',' -> { sb.append(c).append('\n'); repeat(indent) { sb.append("  ") } }
                    c == ':' -> { sb.append(": ") }
                    !inString && c == ' ' -> {}
                    else -> sb.append(c)
                }
            }
            sb.toString()
        } catch (e: Exception) {
            str
        }
    }
}
