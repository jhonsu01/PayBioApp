# ============================================================
# PayBio — R8 / ProGuard rules (release, minify enabled)
# ============================================================

# Keep ALL app code. The codebase is tiny, so this has negligible size impact
# but eliminates any risk of R8 breaking reflection-driven pieces:
#  - ViewModels instantiated by androidx (PaymentViewModel)
#  - Room @Entity / @Dao referenced by generated code
#  - Components referenced by name in AndroidManifest (Application, Activity,
#    WidgetProvider). Library code is still shrunk/obfuscated.
-keep class com.local.paybio.** { *; }

# --- SQLCipher (loaded via JNI; native methods must survive) ---
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# --- Google ML Kit + Play Services Tasks (reflection / @Keep) ---
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.android.odml.** { *; }
-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.**
-dontwarn com.google.android.odml.**

# --- Room ---
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# --- AndroidX lifecycle (ViewModel reflection) ---
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# --- Generic safety nets ---
# Native methods
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}
# Enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# Annotations / generics / inner classes metadata
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations

# Common transitive deps that may emit missing-class warnings under R8 full mode
-dontwarn org.bouncycastle.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn javax.annotation.**
-dontwarn javax.lang.model.**
