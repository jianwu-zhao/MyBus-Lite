package com.mybus.adblock;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

/**
 * 掌上公交 VIP 解锁模块
 * 
 * 原理：
 * 1. Hook SharedPreferences，强制 VIP 相关字段返回 true
 * 2. Hook 所有 isVip/isPremium/isPro/isSVip 等方法返回 true
 * 3. Hook 用户信息 JSON 解析，注入 VIP 字段
 * 4. Hook 广告展示（VIP 用户不应有广告）
 * 
 * 目标包名：com.mygolbs.mybus
 */
public class MyBusAdBlock implements IXposedHookLoadPackage {

    private static final String TAG = "MyBusVIP";
    private static final String TARGET_PACKAGE = "com.mygolbs.mybus";

    // VIP 相关的 SharedPreferences key（常见命名）
    private static final Set<String> VIP_KEYS = new HashSet<>();
    static {
        VIP_KEYS.add("vip");
        VIP_KEYS.add("is_vip");
        VIP_KEYS.add("isVip");
        VIP_KEYS.add("vip_status");
        VIP_KEYS.add("vipStatus");
        VIP_KEYS.add("vip_level");
        VIP_KEYS.add("vipLevel");
        VIP_KEYS.add("vip_expire");
        VIP_KEYS.add("vipExpire");
        VIP_KEYS.add("vip_expire_time");
        VIP_KEYS.add("is_premium");
        VIP_KEYS.add("isPremium");
        VIP_KEYS.add("is_pro");
        VIP_KEYS.add("isPro");
        VIP_KEYS.add("is_svip");
        VIP_KEYS.add("isSVip");
        VIP_KEYS.add("svip");
        VIP_KEYS.add("member_type");
        VIP_KEYS.add("memberType");
        VIP_KEYS.add("user_type");
        VIP_KEYS.add("userType");
        VIP_KEYS.add("is_member");
        VIP_KEYS.add("isMember");
        VIP_KEYS.add("subscribe");
        VIP_KEYS.add("is_subscribe");
        VIP_KEYS.add("ad_free");
        VIP_KEYS.add("adFree");
        VIP_KEYS.add("no_ad");
        VIP_KEYS.add("noAd");
        VIP_KEYS.add("remove_ad");
        VIP_KEYS.add("removeAd");
        VIP_KEYS.add("paid");
        VIP_KEYS.add("is_paid");
        VIP_KEYS.add("isPaid");
        VIP_KEYS.add("purchase_status");
        VIP_KEYS.add("purchaseStatus");
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(TARGET_PACKAGE)) return;

        XposedBridge.log(TAG + ": VIP 解锁模块已加载");

        // === 策略 1: Hook SharedPreferences ===
        hookSharedPreferences(lpparam);

        // === 策略 2: Hook isVip/isPremium/isPro 等方法 ===
        hookVipMethods(lpparam);

        // === 策略 3: Hook Application.onCreate 扫描运行时类 ===
        hookOnCreate(lpparam);

        // === 策略 4: 拦截广告（VIP 免广告）===
        hookAds(lpparam);
    }

    /**
     * 策略 1: Hook SharedPreferences
     * 所有 VIP 相关的 key 返回 true/1/有效值
     */
    private void hookSharedPreferences(XC_LoadPackage.LoadPackageParam lpparam) {
        // Hook getString
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl", lpparam.classLoader,
            "getString", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    String key = (String) param.args[0];
                    if (isVipKey(key)) {
                        String val = (String) param.getResult();
                        // 如果当前值不是 VIP 值，强制返回
                        if (!"1".equals(val) && !"true".equals(val) && !"vip".equals(val)
                            && !"svip".equals(val) && !"premium".equals(val)) {
                            param.setResult(getVipStringValue(key));
                        }
                    }
                }
            });

        // Hook getBoolean
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl", lpparam.classLoader,
            "getBoolean", String.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    String key = (String) param.args[0];
                    if (isVipKey(key)) {
                        param.setResult(true);
                    }
                }
            });

        // Hook getInt
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl", lpparam.classLoader,
            "getInt", String.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    String key = (String) param.args[0];
                    if (isVipKey(key)) {
                        int result = (Integer) param.getResult();
                        if (result == 0) {
                            param.setResult(1);
                        }
                    }
                }
            });

        // Hook getLong (用于 vip_expire_time)
        XposedHelpers.findAndHookMethod("android.app.SharedPreferencesImpl", lpparam.classLoader,
            "getLong", String.class, long.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    String key = (String) param.args[0];
                    if (isVipKey(key) || key.contains("expire") || key.contains("Expire")) {
                        // 返回 2099 年的时间戳
                        param.setResult(4102444800L); // 2099-12-31 23:59:59
                    }
                }
            });

        XposedBridge.log(TAG + ": SharedPreferences hook 已安装");
    }

    /**
     * 策略 2: Hook isVip/isPremium/isPro 等方法
     * 扫描所有已加载的类，找到返回 boolean 的 VIP 检查方法
     */
    private void hookVipMethods(XC_LoadPackage.LoadPackageParam lpparam) {
        // 常见 VIP 方法名
        String[] vipMethodNames = {
            "isVip", "isVIP", "is_vip",
            "isPremium", "is_Premium", "is_premium",
            "isPro", "isProUser",
            "isSVip", "isSvip", "is_svip",
            "isMember", "is_member",
            "isPaid", "is_paid",
            "isSubscribe", "isSubscribed", "is_subscribe",
            "isAdFree", "is_ad_free", "isNoAd",
            "hasVip", "hasVipPrivilege", "hasPrivilege",
            "checkVip", "checkVipStatus", "checkVipValid",
            "getVipStatus", "getVipLevel", "getVipType",
            "getMemberStatus", "getMemberLevel", "getMemberType",
            "isExpired", "isVipExpired",  // 注意：这些要返回 false
            "isVipValid",
            "canRemoveAd", "shouldShowAd",  // 注意：shouldShowAd 要返回 false
            "isLogin",  // 确保登录状态
        };

        // 需要返回 false 的方法
        Set<String> falseMethods = new HashSet<>();
        falseMethods.add("isExpired");
        falseMethods.add("isVipExpired");
        falseMethods.add("shouldShowAd");
        falseMethods.add("needShowAd");
        falseMethods.add("isAdEnabled");

        // 在 Application.onCreate 后扫描所有类
        hookOnCreateAndScan(lpparam, vipMethodNames, falseMethods);
    }

    private void hookOnCreateAndScan(XC_LoadPackage.LoadPackageParam lpparam,
                                      String[] vipMethods, Set<String> falseMethods) {
        XposedHelpers.findAndHookMethod("android.app.Application", lpparam.classLoader,
            "onCreate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    Context ctx = (Context) param.thisObject;
                    ClassLoader cl = ctx.getClassLoader();

                    // 扫描 com.mygolbs 包下所有类
                    scanAndHookClasses(cl, "com.mygolbs", vipMethods, falseMethods);
                    // 也扫描 com.leyou (乐游 SDK)
                    scanAndHookClasses(cl, "com.leyou", vipMethods, falseMethods);
                }
            });
    }

    private void scanAndHookClasses(ClassLoader cl, String prefix,
                                     String[] vipMethods, Set<String> falseMethods) {
        // 通过反射尝试加载可能的类
        // 掌上公交可能的用户/会员相关类
        String[] possibleClasses = {
            prefix + ".mybus.UserManager",
            prefix + ".mybus.UserInfo",
            prefix + ".mybus.UserBean",
            prefix + ".mybus.AccountManager",
            prefix + ".mybus.MemberManager",
            prefix + ".mybus.VipManager",
            prefix + ".mybus.AdManager",
            prefix + ".mybus.Config",
            prefix + ".mybus.Constant",
            prefix + ".mybus.utils.SharedPrefs",
            prefix + ".mybus.utils.UserUtils",
            prefix + ".mybus.utils.VipUtils",
            prefix + ".mybus.model.User",
            prefix + ".mybus.model.UserInfo",
            prefix + ".mybus.model.VipInfo",
            prefix + ".mybus.api.ApiResponse",
            prefix + ".mybus.bean.UserBean",
            prefix + ".mybus.bean.VipBean",
            prefix + ".UserManager",
            prefix + ".UserInfo",
            prefix + ".VipManager",
            prefix + ".MemberManager",
            prefix + ".AdManager",
            prefix + ".Config",
            prefix + ".utils.SharedPrefs",
            prefix + ".utils.UserUtils",
            // 混淆后的单字母/双字母类名
            prefix + ".a", prefix + ".b", prefix + ".c", prefix + ".d",
            prefix + ".e", prefix + ".f", prefix + ".g", prefix + ".h",
        };

        for (String className : possibleClasses) {
            try {
                Class<?> clazz = XposedHelpers.findClass(className, cl);
                hookVipMethodsInClass(clazz, vipMethods, falseMethods);
            } catch (Throwable ignored) {
                // 大部分类不存在，正常
            }
        }
    }

    private void hookVipMethodsInClass(Class<?> clazz, String[] vipMethods, Set<String> falseMethods) {
        try {
            for (Method method : clazz.getDeclaredMethods()) {
                String name = method.getName();
                Class<?> returnType = method.getReturnType();

                // 精确匹配 VIP 方法名
                for (String vipName : vipMethods) {
                    if (name.equals(vipName) || name.toLowerCase().contains(vipName.toLowerCase())) {
                        boolean shouldReturnFalse = falseMethods.contains(name);

                        if (returnType == boolean.class) {
                            XposedBridge.hookMethod(method,
                                XC_MethodReplacement.returnConstant(!shouldReturnFalse));
                            XposedBridge.log(TAG + ": [HOOK] " + clazz.getName() + "." + name + "() → " + !shouldReturnFalse);
                        } else if (returnType == int.class) {
                            XposedBridge.hookMethod(method,
                                XC_MethodReplacement.returnConstant(shouldReturnFalse ? 0 : 1));
                            XposedBridge.log(TAG + ": [HOOK] " + clazz.getName() + "." + name + "() → " + (shouldReturnFalse ? 0 : 1));
                        } else if (returnType == String.class) {
                            XposedBridge.hookMethod(method,
                                XC_MethodReplacement.returnConstant(shouldReturnFalse ? "0" : "1"));
                            XposedBridge.log(TAG + ": [HOOK] " + clazz.getName() + "." + name + "() → \"" + (shouldReturnFalse ? "0" : "1") + "\"");
                        } else if (returnType == long.class) {
                            XposedBridge.hookMethod(method,
                                XC_MethodReplacement.returnConstant(4102444800L));
                            XposedBridge.log(TAG + ": [HOOK] " + clazz.getName() + "." + name + "() → 2099年");
                        }
                        break;
                    }
                }

                // 模糊匹配：方法名包含 vip/premium/member/subscribe 且返回 boolean
                String lowerName = name.toLowerCase();
                if (returnType == boolean.class &&
                    (lowerName.contains("vip") || lowerName.contains("premium") ||
                     lowerName.contains("member") || lowerName.contains("subscri") ||
                     lowerName.contains("paid") || lowerName.contains("privilege") ||
                     lowerName.contains("adfree") || lowerName.contains("noad"))) {

                    boolean shouldReturnFalse = lowerName.contains("expire") || lowerName.contains("showad");
                    XposedBridge.hookMethod(method,
                        XC_MethodReplacement.returnConstant(!shouldReturnFalse));
                    XposedBridge.log(TAG + ": [FUZZY HOOK] " + clazz.getName() + "." + name + "() → " + !shouldReturnFalse);
                }

                // 方法名包含 expire/invalid + vip → 返回 false
                if (returnType == boolean.class &&
                    (lowerName.contains("expire") || lowerName.contains("invalid"))) {
                    XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(false));
                    XposedBridge.log(TAG + ": [EXPIRE HOOK] " + clazz.getName() + "." + name + "() → false");
                }
            }
        } catch (Throwable ignored) {}
    }

    /**
     * 策略 3: Hook Application.onCreate 做最终扫描
     */
    private void hookOnCreate(XC_LoadPackage.LoadPackageParam lpparam) {
        // 已在 hookVipMethods 中处理
    }

    /**
     * 策略 4: 拦截广告（VIP 专属）
     */
    private void hookAds(XC_LoadPackage.LoadPackageParam lpparam) {
        // 拦截广告 SDK 的 show 方法
        String[] adClasses = {
            "com.leyou.plugin.pb",  // FusionController
            "com.kwad.sdk.api.KsFullScreenVideoAd",
            "com.kwad.sdk.api.KsSplashScreenAd",
            "com.bytedance.sdk.openadsdk.TTSplashAd",
            "com.bytedance.sdk.openadsdk.TTFullScreenVideoAd",
            "com.qq.e.ads.splash.SplashAD",
        };

        for (String className : adClasses) {
            try {
                Class<?> clazz = XposedHelpers.findClass(className, lpparam.classLoader);
                for (Method method : clazz.getDeclaredMethods()) {
                    String name = method.getName().toLowerCase();
                    if (name.contains("show") || name.contains("display")) {
                        if (method.getReturnType() == void.class) {
                            XposedBridge.hookMethod(method, XC_MethodReplacement.DO_NOTHING);
                        } else if (method.getReturnType() == boolean.class) {
                            XposedBridge.hookMethod(method, XC_MethodReplacement.returnConstant(false));
                        }
                    }
                }
            } catch (Throwable ignored) {}
        }

        XposedBridge.log(TAG + ": 广告拦截 hook 已安装");
    }

    // ======================== 工具方法 ========================

    private boolean isVipKey(String key) {
        if (key == null) return false;
        String lower = key.toLowerCase();
        if (VIP_KEYS.contains(key) || VIP_KEYS.contains(lower)) return true;
        return lower.contains("vip") || lower.contains("premium") || lower.contains("member")
            || lower.contains("subscri") || lower.contains("paid") || lower.contains("ad_free")
            || lower.contains("noad") || lower.contains("removead") || lower.contains("svip");
    }

    private String getVipStringValue(String key) {
        String lower = key.toLowerCase();
        if (lower.contains("expire") || lower.contains("time")) {
            return "4102444800"; // 2099年时间戳
        }
        if (lower.contains("level") || lower.contains("type")) {
            return "9"; // 最高等级
        }
        if (lower.contains("status")) {
            return "1";
        }
        return "true";
    }
}
