import java.io.FileInputStream
import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.0"
    id("kotlin-parcelize")
}

// Carico proprietà locali (OPENAI_API_KEY, ecc.)
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) load(FileInputStream(f))
}

android {
    namespace = "it.mediterraneanrecords.tarotdraw"
    compileSdk = 34

    // --- Signing (release) ---------------------------------------------------
    signingConfigs {
        create("release") {
            // Legge i valori da gradle.properties
            val storePath = (project.findProperty("RELEASE_STORE_FILE") as String?) ?: ""
            val storePass = (project.findProperty("RELEASE_STORE_PASSWORD") as String?) ?: ""
            val alias = (project.findProperty("RELEASE_KEY_ALIAS") as String?) ?: ""
            val keyPass = (project.findProperty("RELEASE_KEY_PASSWORD") as String?) ?: ""

            if (storePath.isNotBlank()) {
                storeFile = rootProject.file(storePath)
                storePassword = storePass
                keyAlias = alias
                keyPassword = keyPass
            } else {
                logger.lifecycle("⚠️  Nessun keystore configurato (RELEASE_STORE_FILE vuoto). La build release non sarà firmata.")
            }
        }
    }

    // --- Flavors FREE/PRO ----------------------------------------------------
    flavorDimensions += "tier"
    productFlavors {
        create("free") {
            dimension = "tier"
            applicationIdSuffix = ".free"
            versionNameSuffix = "-free"
            buildConfigField("Boolean", "IS_PRO", "false")
        }
        create("pro") {
            dimension = "tier"
            applicationIdSuffix = ".pro"
            versionNameSuffix = "-pro"
            buildConfigField("Boolean", "IS_PRO", "true")
        }
    }

    // --- Config di base ------------------------------------------------------
    defaultConfig {
        applicationId = "it.mediterraneanrecords.tarotdraw"
        minSdk = 24
        targetSdk = 34
        versionCode = 4
        versionName = "1.4-FULL"

        // Espongo la chiave come BuildConfig.OPENAI_API_KEY
        val openAiKey = localProps.getProperty("OPENAI_API_KEY") ?: ""
        buildConfigField("String", "OPENAI_API_KEY", "\"$openAiKey\"")
    }

    // --- Build types ---------------------------------------------------------
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    // --- Lint / Java / Kotlin / Compose / Packaging --------------------------
    lint { checkReleaseBuilds = false }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    //banner pubblicitario
    //implementation("com.google.android.gms:play-services-ads:23.4.0")
    dependencies {
        // ... altre dipendenze

        // Aggiungi queste due righe per Markwon
        val markwonVersion = "4.6.2" // Controlla l'ultima versione disponibile
        implementation("io.noties.markwon:core:$markwonVersion")
    }

    // Animazioni Compose (per animateFloatAsState/tween)
    implementation("androidx.compose.animation:animation")

    // Coroutines (per runBlocking/flow.first)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // BOM Compose (ottobre 2024)
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Kotlinx Serialization (runtime)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Base AndroidX / lingua
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Material & DataStore
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Rete + JSON per integrazione AI
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}