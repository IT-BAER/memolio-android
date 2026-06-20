# Memolio release R8/ProGuard rules.
# Most libs (Room, Hilt, RevenueCat, Coil, AndroidX) ship their own consumer rules,
# so only the reflection-driven bits below need explicit keeps.

# ---- kotlinx.serialization ----
# @Serializable classes resolve their generated serializer via reflection on the
# synthetic Companion / $serializer. R8 would otherwise strip/rename them.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$Companion Companion;
}
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# Belt-and-braces for this app's own serializable model/DTO types.
-keep,includedescriptorclasses class com.baer.memolio.**$$serializer { *; }
-keepclassmembers class com.baer.memolio.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Ktor (embedded CIO server) ----
# Engine + content-negotiation use service loading / reflection that R8 can't trace.
-keep class io.ktor.** { *; }
-keepclassmembers class io.ktor.** { *; }
-dontwarn io.ktor.**
-dontwarn kotlinx.coroutines.**
-dontwarn org.slf4j.**

# Ktor pulls SLF4J; no binding shipped (logs are no-op). Keep it from erroring.
-dontwarn org.slf4j.impl.**
