plugins {
    id("kotlin")
    alias(libs.plugins.stream.java.library)
}

dependencies {
    api(libs.moshi)
    api(libs.moshi.kotlin)
    api(libs.retrofit)
    api(libs.threetenbp)

    detektPlugins(libs.detekt.formatting)
}
