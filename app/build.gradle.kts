plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-parcelize")
    id("kotlin-kapt")
    kotlin("kapt")
}

android {
    namespace = "com.xaye.downloader"
    compileSdk = 33

    defaultConfig {
        applicationId = "com.xaye.downloader"
        minSdk = 24
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    viewBinding {
        enable = true
    }

}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.8.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("io.github.cymchad:BaseRecyclerViewAdapterHelper4:4.1.4")

    // Room
    implementation("androidx.room:room-runtime:2.6.0-alpha01")
    kapt("androidx.room:room-compiler:2.6.0-alpha01")
    // Kotlin 使用 kapt 替代 annotationProcessor
    // 可选 - Kotlin扩展和协程支持
    implementation("androidx.room:room-ktx:2.6.0-alpha01")
    // 可选 - RxJava 支持
    //implementation("androidx.room:room-rxjava2:$room_version")
    implementation("com.google.code.gson:gson:2.8.6")
    // 权限请求框架：https://github.com/getActivity/XXPermissions
    implementation("com.github.getActivity:XXPermissions:18.63")


}