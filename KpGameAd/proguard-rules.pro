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

-keep class kptech.game.kit.** { *; }
-keep class com.kptach.game.ad.** { *; }

# 0
-keep class com.zad.sdk.operation.floatview.DragFloatManager {}
-keepclassmembers class com.zad.sdk.operation.floatview.DragFloatManager {public void setMoveX(int);}
-keep class com.zad.sdk.Oapi.** {*;}
-keep class com.zad.sdk.Ozmtad.common.netbean.response.**{*;}
-keep class com.zad.sdk.Onet.bean.**{*;}
-keep class com.zad.sdk.Ocore.base.IZadAdSign
-keep class com.zad.sdk.Oad_provider.** {<init>(android.app.Activity, java.lang.String, java.lang.String, com.zad.sdk.Oapi.callback.BaseZadAdObserver, com.zad.sdk.Ocore.base.IZadAdSign);}
-keep class com.zad.sdk.Oad_provider.dgt.GDTSDKInit {public static void init(android.app.Application);}
-keep class com.zad.sdk.Oad_provider.baidu.BaiDuSDKInit {public static void init(android.app.Application);}
-keep class com.zad.sdk.Oad_provider.mi.MiSDKInit {public static void init(android.app.Application);}
-keep class com.zad.sdk.Oad_provider.oppo.OppoSDKInit {public static void init(android.appw.Application);}

-keep class com.baidu.mobads.** { *; }
-keep class com.baidu.mobad.** { *; }
-keep class com.bun.miitmdid.core.** {*;}
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.pgl.sys.ces.* {*;}

-keep class com.squareup.picasso.* {*;}
-keep class com.bun.miitmdid.core.** {*;}
