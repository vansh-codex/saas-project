# Keep kotlinx.serialization generated serializers.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.friday.assistant.**$$serializer { *; }
-keepclassmembers class com.friday.assistant.** {
    *** Companion;
}
-keepclasseswithmembers class com.friday.assistant.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
