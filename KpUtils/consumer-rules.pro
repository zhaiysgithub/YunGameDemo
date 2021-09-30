


-keepattributes Signature,*Annotation*
-keep public class com.kptech.kputils.** {
    public protected *;
}
-keep public interface com.kptech.kputils.** {
    public protected *;
}
-keepclassmembers class * extends com.kptech.kputils.** {
    public protected *;
}
-keepclassmembers @com.kptech.kputils.db.annotation.* class * {*;}
-keepclassmembers @com.kptech.kputils.http.annotation.* class * {*;}
-keepclassmembers class * {
    @com.kptech.kputils.view.annotation.Event <methods>;
}