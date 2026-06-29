/**
 * Frida 脚本 - 动态抓取掌上公交 API 请求
 * 
 * 用法（手机服务器上线后）：
 *   adb shell
 *   su
 *   frida -U -l frida_capture_api.js com.mygolbs.mybus
 * 
 * 或在 Magisk 环境下：
 *   adb exec-out su -c "frida -U -l frida_capture_api.js com.mygolbs.mybus"
 * 
 * 抓包结果会写入 /sdcard/mybus_api_capture.txt
 */

'use strict';

var LOG_PATH = '/sdcard/mybus_api_capture.txt';
var capturedUrls = {};

function log(msg) {
    console.log('[MyBus] ' + msg);
    var file = new File(LOG_PATH, 'a');
    file.write(new Date().toISOString() + ' | ' + msg + '\n');
    file.close();
}

// 拦截 HttpURLConnection
Java.perform(function() {
    log('=== Frida 脚本已加载 ===');
    log('等待网络请求...');

    // 方式1: 拦截 URL.openConnection
    var URL = Java.use('java.net.URL');
    URL.openConnection.overload().implementation = function() {
        var urlStr = this.toString();
        var conn = this.openConnection();
        
        if (urlStr.indexOf('mygolbs') >= 0 || urlStr.indexOf('117.40') >= 0 || 
            urlStr.indexOf('amap') >= 0 || urlStr.indexOf('bus') >= 0 ||
            urlStr.indexOf('line') >= 0 || urlStr.indexOf('station') >= 0 ||
            urlStr.indexOf('api') >= 0 || urlStr.indexOf('city') >= 0) {
            
            log('[URL] ' + urlStr);
            capturedUrls[urlStr] = (capturedUrls[urlStr] || 0) + 1;
        }
        
        return conn;
    };

    // 方式2: 拦截 OkHttpClient.newCall
    try {
        var OkHttpClient = Java.use('okhttp3.OkHttpClient');
        var Request = Java.use('okhttp3.Request');
        
        OkHttpClient.newCall.overload('okhttp3.Request').implementation = function(request) {
            var url = request.url().toString();
            
            if (url.indexOf('mygolbs') >= 0 || url.indexOf('117.40') >= 0 ||
                url.indexOf('amap') >= 0 || url.indexOf('city') >= 0 ||
                url.indexOf('line') >= 0 || url.indexOf('station') >= 0) {
                
                log('[OkHttp] ' + request.method() + ' ' + url);
                
                // 打印请求头
                var headers = request.headers();
                if (headers) {
                    var names = headers.names();
                    for (var i = 0; i < names.size(); i++) {
                        var name = names.get(i);
                        log('  Header: ' + name + ': ' + headers.get(name));
                    }
                }
                
                // 打印请求体
                try {
                    var body = request.body();
                    if (body) {
                        var buffer = Java.array('byte', [0]);
                        // body.writeTo(buffer);
                    }
                } catch(e) {}
            }
            
            return this.newCall(request);
        };
    } catch(e) {
        log('[OkHttp hook 失败] ' + e);
    }

    // 方式3: 拦截 HttpsURLConnection
    try {
        var HttpsURLConnection = Java.use('javax.net.ssl.HttpsURLConnection');
        
        HttpsURLConnection.connect.implementation = function() {
            var url = this.getURL().toString();
            if (url.indexOf('mygolbs') >= 0 || url.indexOf('117.40') >= 0) {
                log('[HTTPS] connect: ' + url);
            }
            this.connect();
        };
    } catch(e) {}

    log('=== Hook 安装完成，请操作 APP ===');
});
