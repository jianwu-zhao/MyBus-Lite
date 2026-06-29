package com.mygolbs.mybus

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.mygolbs.mybus.api.AmapDataMapper
import com.mygolbs.mybus.api.BusApiClient
import com.mygolbs.mybus.databinding.ActivityMainBinding
import com.mygolbs.mybus.model.BusLine
import com.mygolbs.mybus.model.City
import kotlinx.coroutines.*

/**
 * 掌上公交精简版 - 主界面
 *
 * 数据源：高德地图公交 API（备用方案）
 * 原掌上公交 API 因梆梆加固 + 国内服务器限制暂未接入。
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val locationPermissionCode = 1001
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var searchJob: Job? = null

    private val lineAdapter = LineAdapter { line ->
        LineDetailActivity.start(this, line, currentCity.cityName)
    }

    /** 当前选中城市 */
    private var currentCity: City = AmapDataMapper.CHINA_CITIES.first { it.cityName == "厦门" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "掌上公交精简版"

        checkLocationPermission()
        setupViews()
        updateCityDisplay()
    }

    private fun setupViews() {
        // 搜索结果列表
        binding.recyclerLines.layoutManager = LinearLayoutManager(this)
        binding.recyclerLines.adapter = lineAdapter

        // 搜索
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchLines(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchJob?.cancel()
                if (newText.isNullOrBlank()) {
                    binding.recyclerLines.visibility = View.GONE
                    binding.emptyView.visibility = View.VISIBLE
                    binding.emptyText.text = "搜索公交线路或站点"
                    return true
                }
                searchJob = scope.launch {
                    delay(500) // 防抖
                    searchLines(newText)
                }
                return true
            }
        })

        // 底部导航
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_search -> true
                R.id.nav_nearby -> {
                    Toast.makeText(this, "附近站点功能需要掌上公交原始 API（待抓包）", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_fav -> {
                    Toast.makeText(this, "收藏功能待实现", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // 城市选择
        binding.citySelector.setOnClickListener {
            showCityPicker()
        }

        // 重试按钮
        binding.retryBtn.setOnClickListener {
            // 重试最后一次搜索
            val lastQuery = binding.searchView.query?.toString()
            if (!lastQuery.isNullOrBlank()) {
                searchLines(lastQuery)
            } else {
                binding.errorView.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
            }
        }
    }

    // ==================== 城市选择 ====================

    private fun updateCityDisplay() {
        binding.citySelector.text = "${currentCity.cityName} ▼"
    }

    private fun showCityPicker() {
        val cities = AmapDataMapper.CHINA_CITIES
        val names = cities.map { it.cityName }.toTypedArray()
        val currentIndex = cities.indexOfFirst { it.cityId == currentCity.cityId }
            .coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("选择城市")
            .setSingleChoiceItems(names, currentIndex) { dialog, which ->
                currentCity = cities[which]
                updateCityDisplay()
                dialog.dismiss()
                // 清空搜索结果，重新搜索
                lineAdapter.submitList(emptyList())
                binding.recyclerLines.visibility = View.GONE
                binding.emptyView.visibility = View.VISIBLE
                binding.emptyText.text = "已切换到 ${currentCity.cityName}"
                // 如果搜索框有内容，自动搜索
                val query = binding.searchView.query?.toString()
                if (!query.isNullOrBlank()) {
                    searchLines(query)
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }

    // ==================== 搜索线路 ====================

    private fun searchLines(keyword: String) {
        if (keyword.isBlank()) return

        binding.loadingView.visibility = View.VISIBLE
        binding.errorView.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
        binding.recyclerLines.visibility = View.GONE

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    BusApiClient.amapInstance.searchLine(
                        keywords = keyword,
                        city = currentCity.cityName
                    )
                }

                binding.loadingView.visibility = View.GONE

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.buslines.isNotEmpty()) {
                        // 缓存完整数据供详情页使用
                        val lines = AmapDataMapper.extractBusLines(body)
                        lineAdapter.currentCityName = currentCity.cityName
                        binding.recyclerLines.visibility = View.VISIBLE
                        lineAdapter.submitList(lines)
                    } else {
                        binding.emptyView.visibility = View.VISIBLE
                        binding.emptyText.text = "未找到匹配的线路"
                    }
                } else {
                    showError("搜索失败（${response.code()}）")
                }
            } catch (e: Exception) {
                binding.loadingView.visibility = View.GONE
                showError("网络错误：${e.localizedMessage ?: "未知错误"}")
            }
        }
    }

    // ==================== UI 辅助 ====================

    private fun showError(msg: String) {
        binding.errorView.visibility = View.VISIBLE
        binding.errorText.text = msg
        binding.recyclerLines.visibility = View.GONE
        binding.emptyView.visibility = View.GONE
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                locationPermissionCode
            )
        }
    }

    // ==================== 菜单 ====================

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                showDataSourceInfo()
                true
            }
            R.id.action_about -> {
                Toast.makeText(
                    this,
                    "掌上公交精简版 v${BuildConfig.VERSION_NAME}\n数据源：高德地图",
                    Toast.LENGTH_LONG
                ).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showDataSourceInfo() {
        AlertDialog.Builder(this)
            .setTitle("数据源")
            .setMessage("当前使用高德地图公交 API 作为数据源。\n\n" +
                    "掌上公交原始 API 因梆梆加固保护，\n" +
                    "待 Frida 动态抓包获取真实接口后切换。")
            .setPositiveButton("知道了", null)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
