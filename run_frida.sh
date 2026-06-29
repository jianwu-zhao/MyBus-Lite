#!/system/bin/sh
# 掌上公交 Frida 抓包脚本 - 在手机服务器上运行
# 需要 root + frida 已安装

PACKAGE="com.mygolbs.mybus"
SCRIPT="/data/local/tmp/frida_capture_api.js"
OUTPUT="/sdcard/mybus_api_capture.txt"

echo "=== 掌上公交 API 抓包 ==="
echo "包名: $PACKAGE"
echo "输出: $OUTPUT"
echo ""

# 复制脚本到手机
cp /sdcard/frida_capture_api.js $SCRIPT 2>/dev/null || \
cp /data/local/tmp/frida_capture_api.js $SCRIPT

# 清空旧日志
echo "=== MyBus API Capture Started ===" > $OUTPUT

# 检查 APP 是否运行
if pidof $PACKAGE > /dev/null; then
    echo "✅ APP 已运行，PID: $(pidof $PACKAGE)"
else
    echo "⚠️ APP 未运行，请启动掌上公交"
    am start -n $PACKAGE/.MainActivity
    sleep 3
fi

# 启动 Frida
echo "🚀 启动 Frida..."
echo "请操作 A搜索线路、查看实时公交等"
echo ""
frida -U -l $SCRIPT $PACKAGE 2>&1 | tee -a $OUTPUT

echo ""
echo "✅ 抓包完成"
echo "结果保存在: $OUTPUT"
echo "用以下命令查看:"
echo "  cat $OUTPUT"
