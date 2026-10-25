plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

// Sleutels en wachtwoorden uitsluitend via de lokale omgeving/CI, nooit in Git.
val uploadSigningNames = listOf(
    "CRYPTORA_KEYSTORE_PATH", "CRYPTORA_STORE_PASSWORD",
    "CRYPTORA_KEY_ALIAS", "CRYPTORA_KEY_PASSWORD"
)
val uploadSigning = uploadSigningNames.associateWith { providers.environmentVariable(it).orNull }
val hasUploadSigning = uploadSigning.values.all { !it.isNullOrBlank() }

android {
    namespace = "com.cryptora.app"
    // API 36 is de gekozen releasebasis. Controleer de actuele Play-eis bij indiening.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cryptora.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"

        vectorDrawables { useSupportLibrary = true }
    }

    signingConfigs {
        if (hasUploadSigning) {
            create("playUpload") {
                storeFile = file(uploadSigning.getValue("CRYPTORA_KEYSTORE_PATH")!!)
                storePassword = uploadSigning.getValue("CRYPTORA_STORE_PASSWORD")
                keyAlias = uploadSigning.getValue("CRYPTORA_KEY_ALIAS")
                keyPassword = uploadSigning.getValue("CRYPTORA_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            if (hasUploadSigning) signingConfig = signingConfigs.getByName("playUpload")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    // Play Store "vraag om een beoordeling"-flow (officiële, niet-opdringerige
    // In-App Review API — geen eigen popup, Google bepaalt zelf of/wanneer de
    // systeemdialoog daadwerkelijk verschijnt). Gebruik de publieke Task-API
    // rechtstreeks; de KTX-wrapper bevat ontbrekende annotaties bij R8.
    implementation("com.google.android.play:review:2.0.2")

    testImplementation("junit:junit:4.13.2")

    androidTestImplementation(platform("androidx.compose:compose-bom:2024.10.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

// bundleRelease blijft bruikbaar voor een ongetekende technische controle.
// Gebruik bundlePlayRelease voor een echte upload: ontbrekende signing geeft een fout.
val verifyPlayReleaseSigning = tasks.register("verifyPlayReleaseSigning") {
    group = "verification"
    description = "Controleert of de uploadkeystore veilig via de omgeving is geconfigureerd."
    doLast {
        val missing = uploadSigning.filterValues { it.isNullOrBlank() }.keys
        if (missing.isNotEmpty()) {
            throw GradleException("Release niet uploadklaar. Ontbrekende omgevingsvariabelen: " + missing.joinToString())
        }
        if (!file(uploadSigning.getValue("CRYPTORA_KEYSTORE_PATH")!!).isFile) {
            throw GradleException("Release niet uploadklaar: de opgegeven uploadkeystore bestaat niet.")
        }
    }
}
tasks.matching { it.name == "bundleRelease" }.configureEach {
    mustRunAfter(verifyPlayReleaseSigning)
}
tasks.register("bundlePlayRelease") {
    group = "build"
    description = "Bouwt een ondertekende App Bundle voor Google Play met de eigen uploadkey."
    dependsOn(verifyPlayReleaseSigning, "bundleRelease")
}
