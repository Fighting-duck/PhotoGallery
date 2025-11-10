plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("org.jetbrains.kotlin.plugin.parcelize")
}

android {
    namespace = "com.lsy.kotlin_demo2"
    compileSdk = 35 // 编译版本

    defaultConfig {
        applicationId = "com.lsy.kotlin_demo2"
        minSdk = 31
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"  //JVM版本
    }
    buildFeatures{
        viewBinding = true
        dataBinding = true
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // 协程核心库
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // 生命周期感知协程（Activity/Fragment 专用）
    implementation ("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    // AndroidX 核心组件
    implementation("androidx.activity:activity-ktx:1.8.0")
    //implementation("androidx.fragment:fragment-ktx:1.6.2")

    // 网络与刷新
    implementation("com.android.volley:volley:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.0.0")

    // 图片加载
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // 生命周期与导航
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")

    // 工具库
    implementation("com.google.code.gson:gson:2.8.6")
    implementation("io.supercharge:shimmerlayout:2.1.0")

    // 图片预览（AndroidX 版本）
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    //viewpager2
    implementation(libs.androidx.viewpager2)
    implementation(libs.google.material)
}