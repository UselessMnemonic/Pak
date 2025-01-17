plugins {
    kotlin("multiplatform") version "2.0.20"
    id("io.kotest.multiplatform") version "6.0.0.M1"
}

group = "com.uselessmnemonic"
version = "1.0"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

kotlin {
    jvm {
        withJava()
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_22
            moduleName = "com.uselessmnemonic.pak"
        }
    }
    js {
        useCommonJs()
        nodejs()
        browser()
    }
    mingwX64()
//    macosX64()
//    linuxX64()

    sourceSets {
        commonTest {
            dependencies {
                implementation("io.kotest:kotest-assertions-core:6.0.0.M1")
                implementation("io.kotest:kotest-framework-engine:6.0.0.M1")
            }
        }
        jvmTest {
            dependencies {
                implementation("io.kotest:kotest-runner-junit5:6.0.0.M1")
            }
        }
        jsMain {
            dependencies {
                implementation(npm("pako", file("src/jsMain/resources/pako")))
            }
        }
    }
}

tasks.named<Test>("jvmTest") {
    useJUnitPlatform()
    filter {
        isFailOnNoMatchingTests = false
    }
    testLogging {
        showExceptions = true
        showStandardStreams = true
        events = setOf(
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
        )
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    environment["RUST_BACKTRACE"] = "1"
}

tasks.named<ProcessResources>("jvmProcessResources") {
    val zExtBuildAll = tasks.getByPath(":pak-rs:buildAll")
    from(zExtBuildAll.outputs.files) {
        eachFile {
            val target = file.parentFile.parentFile.name.split('-')[0]
            into(target)
        }
    }
}
