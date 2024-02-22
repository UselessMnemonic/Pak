plugins {
    kotlin("multiplatform") version "1.9.21"
}

group = "com.uselessmnemonic"
version = "0.1"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
    }
    js("nodejs") {
        nodejs()
    }
    js("browser") {
        browser()
    }
    linuxX64()
    sourceSets {
        val commonMain by getting
        val jvmMain by getting
        val browserMain by getting {
            dependsOn(commonMain)
            dependencies {
                implementation(npm("com.uselessmnemonic.pak.pako", "2.1.0"))
            }
        }
        val nodejsMain by getting {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting
    }
}
