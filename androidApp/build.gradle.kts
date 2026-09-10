plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    id("com.github.triplet.play") version "3.11.0"
}

// 签名配置读取顺序：Gradle 属性（本地 ~/.gradle/gradle.properties）> 环境变量（CI Secrets 注入）
fun signingProp(name: String): String? =
    providers.gradleProperty(name).orElse(providers.environmentVariable(name)).orNull

// 版本代码：Gradle 属性（-PversionCode）> 环境变量 > Git 提交总数（本地兜底）。
// Play 要求每次上传的 versionCode 严格递增；CI 通过 HERBMIND_VERSION_CODE
// 注入 workflow 运行序号（单调递增），本地构建退回 Git 提交总数。
fun versionCodeOf(): Int {
    val explicit = providers.gradleProperty("versionCode")
        .orElse(providers.environmentVariable("HERBMIND_VERSION_CODE"))
        .orNull
    if (explicit != null) return explicit.toInt()
    return providers.exec {
        commandLine("git", "rev-list", "--count", "HEAD")
        workingDir(rootDir)
    }.standardOutput.asText.get().trim().toInt()
}

android {
    namespace = "hua.lee.herbmind.android"
    compileSdk = 36
    buildToolsVersion = "36.1.0"

    defaultConfig {
        applicationId = "hua.lee.herbmind"
        minSdk = 24
        targetSdk = 36
        versionCode = versionCodeOf()
        versionName = "1.0.0"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    signingConfigs {
        create("release") {
            storeFile = signingProp("HERBMIND_STORE_FILE")?.let { file(it) }
            storePassword = signingProp("HERBMIND_STORE_PASSWORD")
            keyAlias = signingProp("HERBMIND_KEY_ALIAS")
            keyPassword = signingProp("HERBMIND_KEY_PASSWORD")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            // 仅当签名配置完整（本地属性或 CI 环境变量）时启用签名，否则保持未签名
            if (signingProp("HERBMIND_STORE_FILE") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
}

// Google Play 发布（gradle-play-publisher），用法与账号配置见 docs/google-play-cli.md
play {
    // 服务账号 JSON 私钥（已 gitignore，需自行放置到仓库根目录）
    serviceAccountCredentials.set(file("${rootDir}/play-service-account.json"))
    track.set("alpha") // 封闭式测试（Alpha）
}

dependencies {
    // Shared module
    implementation(project(":shared"))

    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")

    // Koin for Android
    implementation("io.insert-koin:koin-android:3.5.3")
    implementation("io.insert-koin:koin-androidx-compose:3.5.3")

    // AndroidX
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")

    // DateTime
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.5.0")

    // Google AdMob（版本统一由 libs.versions.toml 管理）
    implementation(libs.play.services.ads)

    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.androidx.arch.core.testing)

    // UI Testing
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
