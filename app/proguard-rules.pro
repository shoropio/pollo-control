# Add project specific ProGuard rules here.

# Keep annotation metadata
-keepattributes *Annotation*

# Keep Hilt-related classes
-keep class com.pollocontrol.app.** { *; }
-keep @dagger.hilt.android.Entrypoint class * { *; }

# Keep Room entities and DAOs
-keep class com.pollocontrol.app.data.local.entity.** { *; }
-keepclassmembers class com.pollocontrol.app.data.local.entity.** {
    * get;
    * set;
}

# Keep Room database
-keep class com.pollocontrol.app.data.local.PolloControlDatabase { *; }

# Keep Gson
-keep class com.google.gson.internal.* { *; }

# Keep coroutines
-keep interface org.jetbrains.kotlinx.coroutines.** { *; }
-keep class org.jetbrains.kotlinx.coroutines.** { *; }

# Keep Firebase
-keep class com.google.firebase.** { *; }

# Keep AndroidX
-keep class androidx.lifecycle.** { *; }
-keep class androidx.room.** { *; }
-keep class androidx.hilt.** { *; }
-keep class androidx.compose.** { *; }

# Keep application class
-keep class com.pollocontrol.app.MainApplication { *; }