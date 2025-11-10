plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("ReleasePlugin") {
            id = "io.getstream.chat.ReleasePlugin"
            implementationClass = "io.getstream.chat.android.command.release.plugin.ReleasePlugin"
            version = "1.0.0"
        }
        create("ChangelogReleaseSectionPlugin") {
            id = "io.getstream.chat.ChangelogReleaseSectionPlugin"
            implementationClass = "io.getstream.chat.android.command.changelog.plugin.ChangelogReleaseSectionPlugin"
            version = "1.0.0"
        }
    }
}

dependencies {
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
}
