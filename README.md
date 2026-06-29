# 掌上公交精简版 (MyBus Lite)

## 项目说明
掌上公交的第三方精简客户端，去广告、更轻量。

## 当前状态

| 模块 | 状态 | 说明 |
|------|------|------|
| ✅ APK 框架 | 完成 | Kotlin + Material Design 3 + ViewBinding |
| ✅ 高德地图公交 API | **已启用** | 当前数据源 |
| ✅ 城市选择 | 完成 | 内置 30 个主要城市 |
| ✅ 线路搜索 | 完成 | 按关键词搜索公交线路 |
| ✅ 线路站点列表 | 完成 | 支持上行/下行方向切换 |
| ✅ Xposed VIP 模块 | 完成 | 已编译签名 APK |
| ❌ 掌上公交原始 API | 待抓包 | 梆梆加固，需 Frida 动态分析 |
| ❌ 附近站点 | 待实现 | 需要掌上公交 API 或 GPS 定位 |
| ❌ 实时公交位置 | 待实现 | 需要掌上公交 API |
| ❌ 路线规划 | 待实现 | 高德 API 已集成，待绑 UI |

## 数据源说明

当前默认使用 **高德地图公交 API** 作为数据源，覆盖：
- 城市列表（内置）
- 公交线路搜索（按名称）
- 线路站点列表（含上下行）
- 公交路径规划（代码已集成，UI 待实现）

高德 Key 已在 `build.gradle` 中配置了一个演示 Key：
```
AMAP_KEY = "45bf007bbf84b9982e721aa5bd259b1f"
```
如遇限流，请 **自行注册高德开放平台** 获取 Key：
1. 打开 https://lbs.amap.com/
2. 注册 → 应用管理 → 创建新应用
3. 添加「Web 服务 API」→ 获取 Key
4. 替换 `app/build.gradle` 中的 `AMAP_KEY`

## 目录结构
```
app/src/main/java/com/mygolbs/mybus/
├── api/
│   ├── AmapDataMapper.kt      # 高德数据 → 内部模型转换器（新增）
│   ├── BusApiService.kt       # 掌上公交后端 API（待修正）
│   ├── ApiClient.kt           # API 客户端（支持多后端切换）
│   └── AmapBusApiService.kt   # 高德地图公交 API（当前数据源）
├── model/
│   └── BusModels.kt          # 数据模型
├── MainActivity.kt            # 主界面（已适配高德 API）
├── LineDetailActivity.kt      # 线路详情（已适配高德 API）
├── LineAdapter.kt             # 线路列表适配器
└── StationAdapter.kt          # 站点列表适配器
```

## 构建方式

### 方式1: Android Studio（推荐）
```bash
# 用 Android Studio 打开此目录
# 等待 Gradle 同步完成
# 点击 Run ▶ 按钮
```

### 方式2: 命令行 Gradle
```bash
# 需要安装 Android SDK
export ANDROID_HOME=/path/to/android-sdk
./gradlew assembleDebug
```

## Xposed VIP 模块
见 `xposed-adblock/` 目录，已编译签名 APK：
```
xposed-adblock/build/mybus-vip-signed.apk
```
安装后在 LSPosed 中启用，勾选掌上公交包名即可。

## 未来：抓取掌上公交真实 API
如要获取掌上公交原始 API（梆梆加固），需要：
1. 手机 root + 安装 Frida
2. 运行 `run_frida.sh`
3. 操作 APP 搜索线路
4. 修正 `BusApiService.kt` 中的接口路径

## 备用方案
如果高德 API Key 不可用，还可以使用：
- 高德地图 JS API（网页版）
- 百度地图公交 API
- 腾讯地图公交 API
