plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
}

android {
    namespace = "io.getstream.chat.android.network"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testOptions.targetSdk = libs.versions.targetSdk.get().toInt()
        lint.targetSdk = libs.versions.targetSdk.get().toInt()
    }
}

dependencies {
    api(libs.moshi)
    api(libs.moshi.kotlin)
    api(libs.retrofit)
    api(libs.itu.date.version)
    ksp(libs.moshi.codegen)

    detektPlugins(libs.detekt.formatting)
}
