// Top-level build file for fzo-kotlin native Android app

plugins {
    id("com.android.application") version "8.7.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.25" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}
