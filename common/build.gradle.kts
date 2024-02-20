architectury {
    common(rootProject.property("enabled_platforms").toString().split(","))
}

repositories {
    maven {
        url = uri("https://api.modrinth.com/maven")
    }
}

dependencies {
    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.
    modImplementation("net.fabricmc:fabric-loader:${project.property("fabric_loader_version")}")

    if (rootProject.property("custom_sodium")!!.equals("true")) {
        modImplementation(files(rootProject.projectDir.resolve("custom_sodium").resolve(rootProject.property("sodium_fabric_version").toString())))
    } else {
        modImplementation("maven.modrinth:sodium:" + rootProject.property("sodium_version"))
    }
    modCompileOnly("maven.modrinth:distanthorizons:2.0.0-a-1.18.2")
    modImplementation("io.github.douira:glsl-transformer:2.0.0-pre13")
    modImplementation("org.antlr:antlr4-runtime:4.11.1")
    modImplementation("org.anarres:jcpp:1.4.14")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.7.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.2")
}

sourceSets {
    val vendored by creating {
        java {
            compileClasspath += main.get().compileClasspath
        }
    }

    val sodiumCompatibility by creating {
        java {
            compileClasspath += main.get().compileClasspath

            compileClasspath += vendored.output
            runtimeClasspath += vendored.output
            compileClasspath += main.get().output
        }
    }

    main {
        java {
            compileClasspath += vendored.output
            runtimeClasspath += vendored.output
            runtimeClasspath += sodiumCompatibility.output
        }
    }
}


loom {
    accessWidenerPath = file("src/main/resources/iris.accesswidener")
    // since loom 0.10, you are **required** to use the
    // "forge" block to configure forge-specific features,
    // such as the mixinConfigs array or datagen


    mods {
        val main by creating { // to match the default mod generated for Forge
            sourceSet("sodiumCompatibility")
            sourceSet("vendored")
            sourceSet("main")
        }
    }
}

tasks.jar {
    archiveClassifier.set("slim")

    manifest {
        attributes(
                mapOf(
                        "Specification-Title" to rootProject.property("mod_id"),
                        "Specification-Vendor" to rootProject.property("mod_author"),
                        "Specification-Version" to "1",
                        "Implementation-Title" to project.property("mod_id"),
                        "Implementation-Version" to version,
                        "Implementation-Vendor" to rootProject.property("mod_author"),
                )
        )
    }

    from(project.sourceSets.get("vendored").output)
    from(project.sourceSets.get("sodiumCompatibility").output)
}
