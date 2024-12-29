HEAD
import java.util.Properties
import java.io.FileInputStream
function_openai

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.final_project"
    compileSdk = 34

HEAD

    buildFeatures {
        buildConfig = true
    }

    val localProperties = Properties().apply {
        load(FileInputStream(rootProject.file("local.properties")))
    }

    val openApiKey: String = localProperties.getProperty("OPENAI_API_KEY") ?: ""

function_openai
    defaultConfig {
        applicationId = "com.example.final_project"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
HEAD


        buildConfigField("String", "OPENAI_API_KEY", "\"${openApiKey}\"")
function_openai
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
}

dependencies {
HEAD

    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation ("androidx.appcompat:appcompat:X.X.X")

    // Gson for JSON parsing
    implementation ("com.google.code.gson:gson:2.10.1")
    // WebSocket for real-time chat
    implementation ("org.java-websocket:Java-WebSocket:1.5.2")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("androidx.drawerlayout:drawerlayout:1.1.1")
    // AppCompat and Material Design
    implementation ("androidx.appcompat:appcompat:1.6.1")
    implementation ("com.google.android.material:material:1.11.0")

    implementation ("androidx.room:room-runtime:2.6.1")
    annotationProcessor ("androidx.room:room-compiler:2.6.1")

function_openai
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
