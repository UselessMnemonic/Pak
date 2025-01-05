var profile = "debug"

val cargoHome = providers.systemProperty("CARGO_HOME").map {
    layout.projectDirectory.dir(it)
}.getOrElse(
    layout.projectDirectory.dir(System.getProperty("user.home")).dir(".cargo")
)
val cargo = cargoHome.dir("bin").file("cargo")
val rustSrc = layout.projectDirectory.dir("src").asFileTree

data class TargetSpec(
    val name: String,
    val target: String
)

val targets = mutableListOf<TargetSpec>(
    TargetSpec("Win64", "x86_64-pc-windows-msvc"),
    //TargetSpec("MacOS64", "x86_64-apple-darwin"),
    //TargetSpec("Linux64", "x86_64-unknown-linux-gnu")
)

val updateTask = tasks.create<Exec>("update") {
    group = "cargo"
    var cargoToml = layout.projectDirectory.file("Cargo.toml")
    var cargoLock = layout.projectDirectory.file("Cargo.lock")
    inputs.file(cargoToml)
    outputs.file(cargoLock)
    commandLine(cargo.asFile, "update")
}


val buildAllTask = tasks.create("buildAll") {
    group = "cargo"
    for (target in targets) {
        var tree = layout.buildDirectory.get().dir(target.target).dir(profile).asFileTree.matching {
            include("${project.name}.dll")
            include("lib${project.name}.dylib")
            include("lib${project.name}.so")
        }
        outputs.files(tree)
    }
}

for (target in targets) {
    val buildTask = tasks.create<Exec>("build${target.name}") {
        group = "cargo"
        var targetDir = layout.buildDirectory.dir(target.target)
        inputs.files(rustSrc)
        outputs.dir(targetDir)

        val args = mutableListOf(cargo.asFile, "build", "--target=${target.target}", "--target-dir=${layout.buildDirectory.get()}")
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
