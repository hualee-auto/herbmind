plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
}

@file:OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)

kotlin {
    // Android target is handled by androidApp module
    // androidTarget()
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            
            implementation(projects.shared)
            
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.koin)
            
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            
            implementation(libs.coil.compose)
            implementation(libs.coil.network)
            
            implementation(libs.kotlinx.coroutines.core)
        }
    }
}
