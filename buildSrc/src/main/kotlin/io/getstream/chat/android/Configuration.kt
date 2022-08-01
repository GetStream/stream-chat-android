package io.getstream.chat.android

object Configuration {
    const val compileSdk = 32
    const val targetSdk = 32
    const val minSdk = 21
    const val majorVersion = 5
    const val minorVersion = 6
    const val patchVersion = 1
    const val versionName = "$majorVersion.$minorVersion.$patchVersion"
    const val snapshotVersionName = "$majorVersion.$minorVersion.${patchVersion + 1}-SNAPSHOT"
    const val artifactGroup = "io.getstream"
}
