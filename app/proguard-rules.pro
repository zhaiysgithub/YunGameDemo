# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,Signature

-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.alibaba.fastjson.** { *; }
-keep class com.redfinger.playsdk.fragment.**{*;}
-keep class com.redfinger.playsdk.widget.**{*;}
-keep class com.gc.redfinger.** { *; }

-keep class kptech.kit.gamebox.** { *; }

# 0
-keep class com.zad.sdk.operation.floatview.DragFloatManager {}
-keepclassmembers class com.zad.sdk.operation.floatview.DragFloatManager {public void setMoveX(int);}
-keep class com.zad.sdk.Oapi.** {*;}
-keep class com.zad.sdk.Ozmtad.common.netbean.response.**{*;}
-keep class com.zad.sdk.Onet.bean.**{*;}
-keep class com.zad.sdk.Ocore.base.IZadAdSign
-keep class com.zad.sdk.Oad_provider.** {<init>(android.app.Activity, java.lang.String, java.lang.String, com.zad.sdk.Oapi.callback.BaseZadAdObserver, com.zad.sdk.Ocore.base.IZadAdSign);}
-keep class com.zad.sdk.Oad_provider.baidu.BaiDuSDKInit {public static void init(android.app.Application);}
-keep class com.zad.sdk.Oad_provider.mi.MiSDKInit {public static void init(android.app.Application);}
-keep class com.zad.sdk.Oad_provider.oppo.OppoSDKInit {public static void init(android.app.Application);}

# 1
-dontwarn com.androidquery.**
-keep class com.androidquery.** { *;}
-dontwarn tv.danmaku.**
-keep class tv.danmaku.** { *;}
-dontwarn androidx.**

# 3
-keep class com.baidu.mobads.** { *; }
-keep class com.baidu.mobad.** { *; }

# 4
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.pgl.sys.ces.* {*;}

# 5
-keep class com.xiaomi.ad.**{*;}
-keep class com.miui.zeus.**{*;}

-keep class com.android.id.impl.** {*;}
-keep class com.qq.e.comm.**{*;}
-keep class com.qq.e.ads.**{*;}
-keep class com.tencent.**{*;}
-keep class com.tencent.smtt.sdk.**{*;}