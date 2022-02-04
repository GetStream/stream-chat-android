import io.getstream.chat.android.Dependencies

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("shot")
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "io.getstream.chat.ui.uitests"
        minSdk = 21
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packagingOptions {
        resources.excludes.add("META-INF/*")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

shot {
    applicationId = "io.getstream.chat.ui.uitests"
}

dependencies {
    implementation(project(":stream-chat-android-ui-components"))
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.5.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    debugImplementation(Dependencies.fragmentTest)

    // Instrumentation tests
    androidTestImplementation(Dependencies.junit4)
    androidTestImplementation(Dependencies.espressoCore)
    androidTestImplementation(Dependencies.androidxTestJunit)
    androidTestImplementation(Dependencies.navigationTest)
}
