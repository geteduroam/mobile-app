-keepattributes SourceFile,LineNumberTable
-keep class org.xmlpull.v1.** { *; }
-dontwarn org.xmlpull.v1.**

-keep public class org.simpleframework.**{ *; }
-keep class org.simpleframework.xml.**{ *; }
-keep class org.simpleframework.xml.core.**{ *; }
-keep class org.simpleframework.xml.util.**{ *; }
-keepattributes *Annotation*
-keepattributes Signature