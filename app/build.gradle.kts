import java.util.Properties

val dotenv = Properties()
val envFile = file("../.env")
if (envFile.exists()) {
    envFile.inputStream().use { dotenv.load(it) }
    println(".env loaded from ${envFile.absolutePath}")
} else {
    println(".env file not found, skipping")
}
val apiKey = dotenv.getProperty("API_KEY") ?: ""

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.nhlstenden.navigationapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nhlstenden.navigationapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        manifestPlaceholders["API_KEY"] = apiKey
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.room.runtime)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.android.material:material:1.9.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation ("com.journeyapps:zxing-android-embedded:4.3.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation ("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation ("org.mockito:mockito-core:5.12.0")
    testImplementation ("org.mockito:mockito-inline:5.2.0") // for static mocking
    implementation(libs.preference)


    annotationProcessor(libs.room.compiler)

    implementation("androidx.core:core-splashscreen:1.0.1")


    // Glide for image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-camera2:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
}
