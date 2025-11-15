import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlinx.serialization)
    id("com.google.devtools.ksp") version "2.0.21-1.0.28"
}

// Load API key from local.properties, secrets.properties, or environment
val localProps = Properties()
val f = rootProject.file("local.properties")
if (f.exists()) {
    f.inputStream().use { localProps.load(it) }
}
val secretsFile = rootProject.file("secrets.properties")
val secretsMap = if (secretsFile.exists()) {
    secretsFile.readLines()
        .filter { it.contains("=") }
        .map { it.split("=") }
        .associate { it[0].trim() to it[1].trim() }
} else {
    emptyMap()
}
val apiKeyRaw = localProps.getProperty("CLOUD_VISION_API_KEY")
    ?: secretsMap["CLOUD_VISION_API_KEY"]
    ?: System.getenv("CLOUD_VISION_API_KEY")
    ?: ""
val quotedApiKey = if (apiKeyRaw.isBlank()) "\"\"" else "\"$apiKeyRaw\""

android {
    namespace = "cs4530.u1433303.cs4530drawingapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "cs4530.u1433303.cs4530drawingapplication"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "CLOUD_VISION_API_KEY", quotedApiKey)
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
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "META-INF/DEPENDENCIES"
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/LICENSE"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE"
            excludes += "META-INF/NOTICE.txt"
            excludes += "google/protobuf/*.proto"
            excludes += "com/google/protobuf/*.proto"
        }
    }
    
}

dependencies {
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation(libs.androidx.compose.ui.graphics)
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("com.github.skydoves:colorpickerview:2.3.0")
    implementation("com.google.android.material:material:1.13.0")
    implementation(libs.androidx.ui.viewbinding)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(kotlin("test"))
    
    // Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Silence SLF4J warnings on Android (optional but reduces log noise)
    implementation("org.slf4j:slf4j-android:1.7.36")

}






