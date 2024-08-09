plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.cravify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.cravify"
        minSdk = 27
        targetSdk = 34
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
}

dependencies {
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.play.services.maps)
    implementation(libs.places)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.ccp)
    implementation(libs.firebase.storage)
    implementation(libs.glide)
    implementation(libs.autoimageslider)
    implementation(libs.firebase.messaging)
    annotationProcessor(libs.glideCompiler)
    implementation(libs.androidexample)
    testImplementation(libs.junit)
    implementation("com.razorpay:checkout:1.6.25")
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}