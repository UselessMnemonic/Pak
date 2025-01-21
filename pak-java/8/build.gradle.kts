plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}

dependencies {
    implementation(project(":pak-java:api"))
}
