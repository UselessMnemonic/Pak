plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "Pak"

include("pak-rs")
include("pak-java")
include("pak-java:api")
include("pak-java:22")
include("pak-java:8")
