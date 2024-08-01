-keepattributes SourceFile,LineNumberTable
-keep class org.xmlpull.v1.** { *; }
-dontwarn org.xmlpull.v1.**

-keep public class org.simpleframework.**{ *; }
-keep class org.simpleframework.xml.**{ *; }
-keep class org.simpleframework.xml.core.**{ *; }
-keep class org.simpleframework.xml.util.**{ *; }
-keepclassmembers class * {
    @org.simpleframework.xml.* *;
}
-keepattributes Signature
-keepattributes ElementList, Root, *Annotation*
# Required by safe navigation types (otherwise it will throw IllegalArgumentException's when building the graph)
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable