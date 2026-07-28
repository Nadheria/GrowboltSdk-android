plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("com.vanniktech.maven.publish")
    id("signing")
}

signing {
    useGpgCmd()
}

android {
    namespace = "com.growbolt.sdk"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        targetSdk = 36
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    // Lifecycle / ViewModel / Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.11.0")

    implementation("com.jakewharton.timber:timber:5.0.1")

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Secure Storage
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation(libs.play.services.ads.identifier)

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation("androidx.activity:activity-ktx:1.9.0")
    implementation("androidx.fragment:fragment-ktx:1.8.0")
    implementation("com.intuit.sdp:sdp-android:1.1.1")
    implementation("com.squareup.picasso:picasso:2.8")
    implementation("com.airbnb.android:lottie:6.4.0")
    implementation("androidx.activity:activity:1.9.0")
}

mavenPublishing {
    coordinates(
        groupId = "ai.growbolt",
        artifactId = "growbolt-sdk",
        version = "1.2.2"
    )

    pom {
        name.set("Growbolt Android SDK")
        description.set("Native Android Offerwall SDK for the Growbolt platform.")
        url.set("https://github.com/Growbolt/Growbolt-sdk-andorid-")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("growbolt")
                name.set("Growbolt")
                url.set("https://github.com/Growbolt")
            }
        }

        scm {
            url.set("https://github.com/Growbolt/Growbolt-sdk-andorid-")
            connection.set("scm:git:git://github.com/Growbolt/Growbolt-sdk-andorid-.git")
            developerConnection.set("scm:git:ssh://git@github.com/Growbolt/Growbolt-sdk-andorid-.git")
        }
    }

    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)

    if (System.getenv("SIGNING_KEY") != null || project.hasProperty("signing.keyId")) {
        signAllPublications()
    }
}