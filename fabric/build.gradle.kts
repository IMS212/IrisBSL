plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

repositories {
    maven {
        url = uri("https://api.modrinth.com/maven")
    }
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentFabric: Configuration by configurations.getting

configurations {
    compileOnly.configure { extendsFrom(common) }
    runtimeOnly.configure { extendsFrom(common) }
    developmentFabric.extendsFrom(common)
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:${rootProject.property("fabric_loader_version")}")
    modImplementationInclude("com.github.zafarkhaja:java-semver:0.10.2")
    modImplementationInclude(fabricApi.module("fabric-key-binding-api-v1", project.property("fabric_api_version").toString()))
    modImplementation(fabricApi.module("fabric-rendering-data-attachment-v1", project.property("fabric_api_version").toString()))
    modImplementation(fabricApi.module("fabric-rendering-fluids-v1", project.property("fabric_api_version").toString()))
    modImplementation(fabricApi.module("fabric-resource-loader-v0", project.property("fabric_api_version").toString()))
    modImplementation(fabricApi.module("fabric-renderer-api-v1", project.property("fabric_api_version").toString()))
    // Fabric API. This is technically optional, but you probably want it anyway.
    //modImplementation "net.fabricmc.fabric-api:fabric-api:$rootproject.property("fabric_api_version").toString()"
    implementation(group = "com.lodborg", name = "interval-tree", version = "1.0.0")

    modImplementationInclude("io.github.douira:glsl-transformer:2.0.0-pre13")
    modImplementationInclude("org.antlr:antlr4-runtime:4.11.1")
    modImplementationInclude("org.anarres:jcpp:1.4.14")
    if (rootProject.property("custom_sodium")!!.equals("true")) {
        modImplementation(files(rootProject.projectDir.resolve("custom_sodium").resolve(rootProject.property("sodium_version").toString())))
    } else {
        modImplementation("maven.modrinth:sodium:" + rootProject.property("sodium_version"))
    }

    common(project(":common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", "transformProductionFabric")) { isTransitive = false }
}

tasks.processResources {
    inputs.property("group", rootProject.property("maven_group"))
    inputs.property("version", project.version)

    filesMatching("fabric.mod.json") {
        expand(mapOf(
                "group" to rootProject.property("maven_group"),
                "version" to project.version,

                "mod_id" to rootProject.property("mod_id"),
                "minecraft_version" to rootProject.property("minecraft_version"),
        ))
    }
}

tasks.shadowJar {
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    dependsOn(tasks.shadowJar)
    archiveClassifier.set(null as String?)
}

tasks.jar {
    archiveClassifier.set("dev")
}

tasks.sourcesJar {
    val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
    dependsOn(commonSources)
    from(commonSources.archiveFile.map { zipTree(it) })
}

components.getByName("java") {
    this as AdhocComponentWithVariants
    this.withVariantsFromConfiguration(project.configurations["shadowRuntimeElements"]) {
        skip()
    }
}

fun modImplementationInclude(s : String) {
    dependencies.modImplementation(s)
    dependencies.include(s)
}


fun modImplementationInclude(s : Dependency) {
    dependencies.modImplementation(s)
    dependencies.include(s)
}
