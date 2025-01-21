plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

dependencies {
    implementation(project(":pak-java:api"))
}
