package com.mygolbs.mybus

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * 掌上公交查询 - 使用真实掌上公交 API
 *
 * 从 www.mygolbs.com 的 app.js 中提取的 API：
 * - WxBusServer: https://wx.mygolbs.com/WxBusServer/
 * - MyBusWeChatMessage: https://wx.mygolbs.com/MyBusWeChatMessage
 */
class MainActivity : AppCompatActivity() {

    private lateinit var etLine: EditText
    private lateinit var etCity: EditText
    private lateinit var btnSearch: Button
    private lateinit var tvResult: TextView
    private lateinit var pbLoading: ProgressBar

    companion object {
        // 核心 API
        private const val BASE_URL = "https://wx.mygolbs.com"
        private const val API_PATH = "/WxBusServer/"
        private const val DATA_PATH = "/MyBusWeChatMessage"
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
                val result = queryApi(line, city)
                runOnUiThread {
                    tvResult.text = result
                    pbLoading.visibility = ProgressBar.GONE
                    btnSearch.isEnabled = true
                }
            } catch (e: Exception) {
                runOnUiThread {
                    tvResult.text = "错误: ${e.message}\n\n${queryFallback(line, city)}"
                    pbLoading.visibility = ProgressBar.GONE
                    btnSearch.isEnabled = true
                }
            }
        }.start()
    }

    /** 调用掌上公交 API（WxBusServer） */
    private fun queryApi(line: String, city: String): String {
        // 先从 WeChatMessage 接口查询
        val url = "$BASE_URL$DATA_PATH?cmd=204&" +
                "city=${URLEncoder.encode(city, "UTF-8")}&" +
                "line=${URLEncoder.encode(line, "UTF-8")}&" +
                "output=json"

        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.setRequestProperty("Content-Type", "application/json;charset=utf-8")
        conn.requestMethod = "GET"

        val code = conn.responseCode
        if (code != 200) {
            return "API 返回 $code\n\n尝试备用接口…\n${queryWs(line, city)}"
        }

        val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
        val sb = StringBuilder()
        var l: String?
        while (reader.readLine().also { l = it } != null) sb.append(l)
        reader.close()
        conn.disconnect()

        if (sb.isEmpty()) return "无数据"
        return formatJson(sb.toString())
    }

    /** 备用接口（WxBusServer） */
    private fun queryWs(line: String, city: String): String {
        val url = "$BASE_URL$API_PATH?cmd=205&" +
                "city=${URLEncoder.encode(city, "UTF-8")}&" +
                "line=${URLEncoder.encode(line, "UTF-8")}&" +
                "output=json"

        try {
            val conn = URL(url).openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"

            val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
            val sb = StringBuilder()
            var l: String?
            while (reader.readLine().also { l = it } != null) sb.append(l)
            reader.close()
            conn.disconnect()

            return "备用接口:\n${formatJson(sb.toString())}"
        } catch (e: Exception) {
            return "备用接口失败: ${e.message}"
        }
    }

    private fun formatJson(str: String): String {
        // 简单格式化
        return try {
            str.replace("},{", "},\n{")
                .replace("[", "[\n").replace("]", "\n]")
                .replace(",", ",\n")
        } catch (e: Exception) { str }
    }
}