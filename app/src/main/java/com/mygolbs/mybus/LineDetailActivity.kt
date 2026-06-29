package com.mygolbs.mybus

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.mygolbs.mybus.api.AmapDataMapper
import com.mygolbs.mybus.api.BusApiClient
import com.mygolbs.mybus.databinding.ActivityLineDetailBinding
import com.mygolbs.mybus.model.BusLine
import com.mygolbs.mybus.model.Station
import kotlinx.coroutines.*

/**
 * 线路详情页 - 显示站点列表
 *
 * 数据源：高德地图公交 API
 * 搜索线路名称获取完整信息（含站点列表和方向）。
 */
class LineDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLineDetailBinding
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private lateinit var line: BusLine

    /** 上行站点列表 */
    private var upStations: List<Station> = emptyList()
    /** 下行站点列表 */
    private var downStations: List<Station> = emptyList()

    private val stationAdapter = StationAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLineDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        line = intent.getSerializableExtra("line") as? BusLine ?: run {
            Toast.makeText(this, "数据错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        supportActionBar?.apply {
            title = line.lineName
            setDisplayHomeAsUpEnabled(true)
        }

        setupViews()
        loadDetail()
    }

    private fun setupViews() {
        // 显示线路基本信息
        binding.lineInfo.text = "${line.fromName} → ${line.toName}"
        binding.lineTime.text = "${line.startTime} - ${line.endTime}"

        // 站点列表
        binding.recyclerStations.layoutManager = LinearLayoutManager(this)
        binding.recyclerStations.adapter = stationAdapter

        // 方向 Tab 切换
        binding.tabDirs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                showStationsForTab(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    /**
     * 加载线路详情
     *
     * 通过高德 API 按线路名称搜索，从返回结果中提取站点列表。
     * 高德会返回该线路的所有方向（上行/下行各一条记录）。
     */
    private fun loadDetail() {
        binding.loadingView.visibility = View.VISIBLE

        scope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    BusApiClient.amapInstance.searchLine(
                        keywords = line.lineName,
                        city = extractCityName()
                    )
                }

                binding.loadingView.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val detail = AmapDataMapper.extractLineDetail(response.body()!!)
                    upStations = detail.upStations
                    downStations = detail.downStations

                    // 更新线路信息（高德返回的数据可能更完整）
                    detail.line?.let { updatedLine ->
                        line = updatedLine
                        supportActionBar?.title = line.lineName
                        binding.lineInfo.text = "${line.fromName} → ${line.toName}"
                        binding.lineTime.text = "${line.startTime} - ${line.endTime}"
                    }

                    // 配置 Tab
                    configureTabs()

                    // 显示默认方向
                    showStationsForTab(binding.tabDirs.selectedTabPosition)
                } else {
                    Toast.makeText(
                        this@LineDetailActivity,
                        "加载失败（${response.code()}）",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                binding.loadingView.visibility = View.GONE
                Toast.makeText(
                    this@LineDetailActivity,
                    "网络错误：${e.localizedMessage ?: "未知错误"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * 根据数据情况配置 Tab 显示
     */
    private fun configureTabs() {
        val tabCount = binding.tabDirs.tabCount // 布局中定义了2个tab

        if (upStations.isNotEmpty() && downStations.isNotEmpty()) {
            // 两个方向都有数据，显示两个 Tab
            binding.tabDirs.getTabAt(0)?.text = "上行（${upStations.size}站）"
            binding.tabDirs.getTabAt(1)?.text = "下行（${downStations.size}站）"
            binding.tabDirs.visibility = android.view.View.VISIBLE
        } else if (upStations.isNotEmpty()) {
            // 只有一个方向
            binding.tabDirs.getTabAt(0)?.text = "站点列表（${upStations.size}站）"
            binding.tabDirs.getTabAt(1)?.visibility = android.view.View.GONE
            binding.tabDirs.visibility = android.view.View.VISIBLE
        } else if (downStations.isNotEmpty()) {
            binding.tabDirs.getTabAt(0)?.text = "站点列表（${downStations.size}站）"
            binding.tabDirs.getTabAt(1)?.visibility = android.view.View.GONE
            binding.tabDirs.visibility = android.view.View.VISIBLE
        } else {
            // 无站点数据
            binding.tabDirs.visibility = android.view.View.GONE
            Toast.makeText(this, "未获取到站点信息", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * 根据 Tab 位置显示对应方向的站点
     */
    private fun showStationsForTab(tabPosition: Int) {
        val stations = when (tabPosition) {
            0 -> upStations
            1 -> downStations
            else -> upStations
        }
        stationAdapter.submitList(stations)
    }

    /**
     * 从线路名称或基本信息中提取城市名
     *
     * 由于高德 API 需要城市参数，这里尝试从上下文中推断。
     * 实际使用时用户应先在主界面选择城市。
     */
    private fun extractCityName(): String {
        // 从 Intent 额外数据中读取城市名（由 MainActivity 传入）
        return intent.getStringExtra("city_name") ?: "厦门"
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    companion object {
        /**
         * 启动线路详情页
         *
         * @param context 上下文
         * @param line 线路信息
         * @param cityName 城市名（高德 API 需要），默认 "厦门"
         */
        fun start(context: Context, line: BusLine, cityName: String? = null) {
            val intent = Intent(context, LineDetailActivity::class.java).apply {
                putExtra("line", line)
                if (cityName != null) {
                    putExtra("city_name", cityName)
                }
            }
            context.startActivity(intent)
        }
    }
}
