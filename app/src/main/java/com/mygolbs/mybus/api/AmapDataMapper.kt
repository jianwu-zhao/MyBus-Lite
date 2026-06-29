package com.mygolbs.mybus.api

import com.mygolbs.mybus.model.*

/**
 * 高德地图 API 数据 → 内部数据模型的转换器
 *
 * 高德公交 API 返回的 [AmapBusLine] / [AmapBusStop] 等模型
 * 与 APP 内部使用的 [BusLine] / [Station] 结构不同，
 * 通过此映射器完成转换，UI 层无需感知数据源差异。
 */
object AmapDataMapper {

    // ==================== 城市 ====================

    /**
     * 高德 API 没有城市列表接口，这里维护一份常用城市硬编码列表。
     * 如果需要更完整的城市列表，可以考虑引入 [city/search] 接口的第三方实现。
     */
    val CHINA_CITIES: List<City> = listOf(
        City("bj", "北京", "北京", 39.9042, 116.4074, "beijing", true),
        City("sh", "上海", "上海", 31.2304, 121.4737, "shanghai", true),
        City("gz", "广州", "广东", 23.1291, 113.2644, "guangzhou", true),
        City("sz", "深圳", "广东", 22.5431, 114.0579, "shenzhen", true),
        City("xm", "厦门", "福建", 24.4798, 118.0894, "xiamen", true),
        City("hz", "杭州", "浙江", 30.2741, 120.1551, "hangzhou", true),
        City("cd", "成都", "四川", 30.5728, 104.0668, "chengdu", true),
        City("wh", "武汉", "湖北", 30.5928, 114.3055, "wuhan", true),
        City("nj", "南京", "江苏", 32.0603, 118.7969, "nanjing", true),
        City("cq", "重庆", "重庆", 29.4316, 106.9123, "chongqing", true),
        City("su", "苏州", "江苏", 31.2990, 120.5853, "suzhou", true),
        City("tj", "天津", "天津", 39.3434, 117.3616, "tianjin", true),
        City("fz", "福州", "福建", 26.0745, 119.2965, "fuzhou", false),
        City("zz", "郑州", "河南", 34.7466, 113.6254, "zhengzhou", false),
        City("cs", "长沙", "湖南", 28.2282, 112.9388, "changsha", false),
        City("jn", "济南", "山东", 36.6512, 116.9972, "jinan", false),
        City("sy", "沈阳", "辽宁", 41.8057, 123.4315, "shenyang", false),
        City("he", "合肥", "安徽", 31.8206, 117.2272, "hefei", false),
        City("km", "昆明", "云南", 25.0389, 102.7183, "kunming", false),
        City("xa", "西安", "陕西", 34.3416, 108.9398, "xian", false),
        City("wx", "无锡", "江苏", 31.4910, 120.3054, "wuxi", false),
        City("nb", "宁波", "浙江", 29.8683, 121.5440, "ningbo", false),
        City("qz", "泉州", "福建", 24.8739, 118.6004, "quanzhou", false),
        City("qn", "青岛", "山东", 36.0671, 120.3826, "qingdao", false),
        City("dl", "大连", "辽宁", 38.9140, 121.6147, "dalian", false),
        City("zz", "珠海", "广东", 22.2710, 113.5767, "zhuhai", false),
        City("dc", "东莞", "广东", 23.0208, 113.7518, "dongguan", false),
        City("fs", "佛山", "广东", 23.0219, 113.1219, "foshan", false),
        City("hz", "惠州", "广东", 23.1110, 114.4168, "huizhou", false),
        City("zh", "中山", "广东", 22.5176, 113.3928, "zhongshan", false)
    )

    // ==================== 公交线路 ====================

    /**
     * 将高德的 [AmapBusLine] 转换为 APP 内部 [BusLine]
     */
    fun toBusLine(amapLine: AmapBusLine): BusLine {
        return BusLine(
            lineId = amapLine.id,
            lineName = amapLine.name,
            fromName = amapLine.start_stop,
            toName = amapLine.end_stop,
            startTime = amapLine.start_time,
            endTime = amapLine.end_time,
            price = amapLine.basic_price.ifEmpty { amapLine.total_price },
            distance = amapLine.distance,
            company = amapLine.company,
            direction = 0
        )
    }

    /**
     * 将高德的 [AmapBusStop] 转换为 APP 内部 [Station]
     */
    fun toStation(amapStop: AmapBusStop): Station {
        val parts = amapStop.location.split(",")
        return Station(
            stationId = amapStop.id,
            stationName = amapStop.name,
            lat = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
            lng = parts.getOrNull(0)?.toDoubleOrNull() ?: 0.0,
            orderNo = amapStop.sequence
        )
    }

    /**
     * 将高德的线路列表（含站点）转换为 APP 的 [LineDetail]
     *
     * 高德 API 可能返回多条方向记录（上行/下行各一条），
     * 这里自动拆分到 upStations / downStations。
     *
     * @return Pair(LineDetail, 原始 AmapBusLine 列表)
     */
    fun toLineDetail(amapLines: List<AmapBusLine>): Pair<LineDetail, List<AmapBusLine>> {
        val upLine = amapLines.firstOrNull()
        val downLine = amapLines.getOrNull(1)

        val line = upLine?.let { toBusLine(it) }
        val upStations = upLine?.busstops?.map { toStation(it) } ?: emptyList()
        val downStations = downLine?.busstops?.map { toStation(it) } ?: emptyList()

        return LineDetail(
            line = line?.copy(direction = 0),
            upStations = upStations,
            downStations = downStations
        ) to amapLines
    }

    /**
     * 从高德响应的 [AmapBusResponse] 中提取线路列表（仅列表展示用，不含站点）
     */
    fun extractBusLines(response: AmapBusResponse): List<BusLine> {
        return response.buslines.map { toBusLine(it) }
    }

    /**
     * 从高德响应的 [AmapBusResponse] 中提取线路详情（含站点）
     */
    fun extractLineDetail(response: AmapBusResponse): LineDetail {
        return toLineDetail(response.buslines).first
    }
}
