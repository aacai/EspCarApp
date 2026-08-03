# =============================================
# Kotlin Metadata (反射/序列化依赖)
# =============================================
-keep class kotlin.Metadata { *; }

# =============================================
# Kotlin Coroutines (官方规则)
# https://github.com/Kotlin/kotlinx.coroutines
# =============================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# =============================================
# Kotlinx Serialization (官方规则)
# https://github.com/Kotlin/kotlinx.serialization
# =============================================
-keepattributes *Annotation*, InnerClasses, EnclosingMethod

# Keep serializer for companion objects of @Serializable classes
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <1>$<2> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep INSTANCE.serializer() of @Serializable objects
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep $$serializer classes
-keepnames class **$$serializer {
    static **$$serializer INSTANCE;
}

# Keep our own serializable classes
-keep,includedescriptorclasses class zhiqiu.car.app.**$$serializer { *; }
-keepclassmembers class zhiqiu.car.app.** {
    *** Companion;
}
-keepclasseswithmembers class zhiqiu.car.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# =============================================
# 项目自身代码 (仅保留必要的)
# =============================================
-keep @androidx.compose.runtime.Composable public class * { *; }
-keepclassmembers class * extends androidx.activity.ComponentActivity {
    public *** onCreate(...);
}
-keepclassmembers class * {
    @composable *** *(...);
}