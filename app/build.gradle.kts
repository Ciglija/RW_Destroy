plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.rw_destroy"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.rw_destroy"
        minSdk = 24
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("org.apache.commons:commons-csv:1.10.0")
    implementation("org.apache.poi:poi-ooxml:5.2.3")
    implementation("mysql:mysql-connector-java:8.0.33")
    // JUnit 5 for unit tests
    testImplementation ("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    // Mockito for mocking
    testImplementation ("org.mockito:mockito-core:5.5.0")
}