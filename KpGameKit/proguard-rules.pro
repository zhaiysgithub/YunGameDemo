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

#修改混淆时的包名，解决a.a.a与其它包冲突的问题
-obfuscationdictionary fname.txt
-classobfuscationdictionary fname.txt
-packageobfuscationdictionary fname.txt

-keepattributes SourceFile,LineNumberTable,Signature
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep class com.alibaba.fastjson.** { *; }
#-keep class com.redfinger.playsdk.fragment.**{*;}
#-keep class com.redfinger.playsdk.widget.**{*;}
#-keep class com.gc.redfinger.** { *; }

-keep class kptech.cloud.kit.msg.** { *; }
-keep class kptech.game.kit.** { *; }
-keep class com.mci.play.** { *; }

#-keep class com.mci.play.** { *; }
#-keep class com.yd.yunapp.** { *; }
#-keep class com.bun.miitmdid.core.** {*;}
