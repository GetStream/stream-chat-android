plugins {
    id("kotlin")
    alias(libs.plugins.stream.java.library)
    alias(libs.plugins.ksp)
}

dependencies {
    api(libs.moshi)
    api(libs.retrofit)
    ksp(libs.moshi.codegen)

    detektPlugins(libs.detekt.formatting)
}
