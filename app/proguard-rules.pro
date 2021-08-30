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

##game
#-keep class kptech.game.kit.** { *; }
#
##messager
#-keep class kptech.cloud.kit.msg.** { *; }
#
##bd game
#-keepattributes SourceFile,LineNumberTable,Signature
#-keepclasseswithmembernames class * {
#    native <methods>;
#}
#-keep class com.mci.play.** { *; }
#
##-keep class com.alibaba.fastjson.** { *; }
##-keep class com.redfinger.playsdk.fragment.**{*;}
##-keep class com.redfinger.playsdk.widget.**{*;}
##-keep class com.gc.redfinger.** { *; }
#
## 0
#-keep class com.zad.sdk.operation.floatview.DragFloatManager {}
#-keepclassmembers class com.zad.sdk.operation.floatview.DragFloatManager {public void setMoveX(int);}
#-keep class com.zad.sdk.Oapi.** {*;}
#-keep class com.zad.sdk.Ozmtad.common.netbean.response.**{*;}
#-keep class com.zad.sdk.Onet.bean.**{*;}
#-keep class com.zad.sdk.Ocore.base.IZadAdSign
#-keep class com.zad.sdk.Oad_provider.** {<init>(android.app.Activity, java.lang.String, java.lang.String, com.zad.sdk.Oapi.callback.BaseZadAdObserver, com.zad.sdk.Ocore.base.IZadAdSign);}
#-keep class com.zad.sdk.Oad_provider.dgt.GDTSDKInit {public static void init(android.app.Application);}
#-keep class com.zad.sdk.Oad_provider.baidu.BaiDuSDKInit {public static void init(android.app.Application);}
#-keep class com.zad.sdk.Oad_provider.mi.MiSDKInit {public static void init(android.app.Application);}
#-keep class com.zad.sdk.Oad_provider.oppo.OppoSDKInit {public static void init(android.appw.Application);}
#
#-keep class com.baidu.mobads.** { *; }
#-keep class com.baidu.mobad.** { *; }
#-keep class com.bun.miitmdid.core.** {*;}
#-keep class com.bytedance.sdk.openadsdk.** { *; }
#-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
#-keep class com.pgl.sys.ces.* {*;}
#
##-keep class com.squareup.picasso.* {*;}
#-keep class com.bun.miitmdid.core.** {*;}



#game
-keep class kptech.game.kit.** { *; }
#messager
-keep class kptech.cloud.kit.msg.** { *; }
-keepattributes SourceFile,LineNumberTable,Signature
-keepclasseswithmembernames class * { native <methods>; }
-keep class com.mci.play.** { *; }

-keepattributes Signature,*Annotation*
-keep public class org.xutils.** {
    public protected *;
}
-keep public interface org.xutils.** {
    public protected *;
}
-keepclassmembers class * extends org.xutils.** {
    public protected *;
}
-keepclassmembers @org.xutils.db.annotation.* class * {*;}
-keepclassmembers @org.xutils.http.annotation.* class * {*;}
-keepclassmembers class * {
    @org.xutils.view.annotation.Event <methods>;
}


