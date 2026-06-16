pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    // Auto-provisions a JDK 21 toolchain (this machine has only JDK 17). One-time
    // download on first build; the Gradle daemon itself still runs on JDK 17.
    // 1.0.0 is required for Gradle 9: earlier releases reference the removed
    // JvmVendorSpec.IBM_SEMERU and fail with NoSuchFieldError on Gradle 9.4.1.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "Memolio"
include(":app")
