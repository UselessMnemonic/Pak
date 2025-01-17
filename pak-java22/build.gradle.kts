plugins {
    id("java")
}

group = "com.uselessmnemonic"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(9)
    }
}
