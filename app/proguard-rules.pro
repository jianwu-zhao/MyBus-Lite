# 掌上公交精简版 ProGuard 规则

# 保留模型类
-keep class com.mygolbs.mybus.model.** { *; }

# 保留 API 接口
-keep interface com.mygolbs.mybus.api.** { *; }

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# 高德地图
-dontwarn com.amap.api.**
-keep class com.amap.api.** { *; }
