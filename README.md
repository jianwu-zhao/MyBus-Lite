# MyBus Lite（掌上公交精简查询）

一个干净的公交查询壳应用，用 GitHub Actions 构建 APK。

## 当前定位

- 不集成广告 SDK。
- 不包含 VIP、付费权益绕过、Xposed、LSPatch 相关代码。
- 先用官方/公开 H5 页面保证可用性，后续再逐步接入已验证的公开公交接口。

## 已知 API 线索

静态分析掌上公交 APK 后，记录到的主要接口/域名线索：

- `http://117.40.140.76:8084/hctsm-server/aapp`
- `http://www.mygolbs.com`
- `http://static.mygolbs.com`
- `http://quanguo.mygolbs.com:8081`
- `http://m.mygolbs.com`
- `http://mpsapi.amap.com/`
- `http://tsapi.amap.com/v1`

这些接口路径和参数仍需合法验证；当前 APK 不依赖未验证私有接口。

## App 功能

当前版本：

- Android 原生 WebView 容器。
- 加载公交查询 H5 页面。
- 支持返回键网页后退。
- 支持 HTTP 明文接口配置，便于后续接入测试接口。

## 项目结构

```text
app/src/main/java/com/mygolbs/mybus/MainActivity.kt
app/src/main/AndroidManifest.xml
.github/workflows/build.yml
```

## GitHub Actions 构建

推送到 `main` 或 `master` 后自动执行：

```bash
./gradlew assembleDebug --stacktrace
```

构建产物会上传到 Actions artifact：

```text
app/build/outputs/apk/debug/*.apk
```

## 本地构建

需要 Android SDK：

```bash
./gradlew assembleDebug
```

Termux 本地缺 SDK 时不用本地编译，直接依赖 GitHub Actions。

## 合规说明

本仓库只用于公交查询客户端开发和接口线索整理，不提供付费功能绕过、不提供第三方 App 修改模块。