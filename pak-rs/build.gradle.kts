import org.gradle.internal.extensions.stdlib.capitalized

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.tomlj:tomlj:1.1.1")
    }
}

val cargoHome = providers.systemProperty("CARGO_HOME").map {
    layout.projectDirectory.dir(it)
}.getOrElse(
    layout.projectDirectory.dir(System.getProperty("user.home")).dir(".cargo")
)
val cargoBin = cargoHome.dir("bin").file("cargo").asFile
val cargoConfig = with(layout.projectDirectory.dir(".cargo").file("config.toml").asFile.reader()) {
    org.tomlj.Toml.parse(this)
}

val rustSrc = layout.projectDirectory.dir("src").asFileTree
val targets = cargoConfig.getTable("target")!!.keySet()
var profile = "debug"

val updateTask = tasks.create<Exec>("update") {
    group = "cargo"
    val cargoToml = layout.projectDirectory.file("Cargo.toml")
    val cargoLock = layout.projectDirectory.file("Cargo.lock")
    inputs.file(cargoToml)
    outputs.file(cargoLock)
    commandLine(cargoBin, "update")
}

val buildAllTask = tasks.create("buildAll") {
    group = "cargo"
    for (target in targets) {
        val tree = layout.buildDirectory.get().dir(target).dir(profile).asFileTree.matching {
            include("pak.dll")
            include("libpak.dylib")
            include("libpak.so")
        }
        outputs.files(tree)
    }
}

for (target in targets) {
    val buildName = target.split('-').joinToString(separator = "") { it.capitalized() }
    val buildTask = tasks.create<Exec>("build$buildName") {
        group = "cargo"
        val targetDir = layout.buildDirectory.dir(target)
        inputs.files(rustSrc)
        outputs.dir(targetDir)

        val args = mutableListOf(cargoBin, "build", "--target=${target}", "--target-dir=${layout.buildDirectory.get()}")
        if (profile == "release") {
            args.add("--release")
        } else if (profile != "debug") {
            args.add("--profile=$profile")
        }
        commandLine(args)
    }
    buildTask.dependsOn(updateTask)
    buildAllTask.dependsOn(buildTask)
}

tasks.create("clean") {
    group = "cargo"
    doLast {
        layout.buildDirectory.get().asFile.deleteRecursively()
    }
}
