# Keep Xtream/DTO models for kotlinx.serialization reflection
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.internal.ClassSerializerCacheKt { *; }
-keep,includedescriptorclasses class com.clmf.player.data.remote.dto.**$$serializer { *; }
-keepclassmembers class com.clmf.player.data.remote.dto.** {
    *** Companion;
}
-keepclasseswithmembers class com.clmf.player.data.remote.dto.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Media3
-dontwarn com.google.android.exoplayer2.**
