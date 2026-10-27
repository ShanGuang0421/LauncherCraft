plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.sgty.lcraft"
    compileSdk = 36 

    defaultConfig {
        applicationId = "com.sgty.lcraft"
        minSdk = 21 
        targetSdk = 36  
        versionCode = 2
        versionName = "1.1_subver1"
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17 
        targetCompatibility = JavaVersion.VERSION_17 
    }

    buildTypes {
        release {
            isMinifyEnabled = false // 关掉混淆，防崩溃
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = false // 加这一行，强制关掉
    }
}

configurations.all {
    resolutionStrategy {
        force("androidx.core:core-ktx:1.12.0") // 指定一个稳定版
    }
}
dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.android.material)
    implementation("androidx.core:core-ktx:1.12.0") // 确保有这个
    implementation("androidx.appcompat:appcompat:1.6.1")

}