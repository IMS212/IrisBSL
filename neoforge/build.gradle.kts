plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    forge()
}

val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating
val developmentForge: Configuration by configurations.getting

configurations {
    compileOnly.configure { extendsFrom(common) }
    runtimeOnly.configure { extendsFrom(common) }
    developmentForge.extendsFrom(common)
}

loom {
    // use this if you are using the official mojang mappings
    // and want loom to stop warning you about their license
    silentMojangMappingsLicense()

    accessWidenerPath = file("../common/src/main/resources/iris.accesswidener")

    forge {
        convertAccessWideners = true

        mixinConfigs(
                "mixins.iris.json",
                "mixins.iris.neoforge.json",
                "mixins.iris.fantastic.json",
                "mixins.iris.fixes.maxfpscrash.json",
                "mixins.iris.vertexformat.json",
                "mixins.iris.compat.sodium.json",
                "iris-batched-entity-rendering.mixins.json"
        )
    }
}

repositories {
    // Add repositories to retrieve artifacts from in here.
    // You should only use this when depending on other mods because
    // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
    // See https://docs.gradle.org/current/userguide/declaring_repositories.html
    // for more information about repositories.
    maven {
        url = uri("https://maven.neoforged.net/releases")
    }

    exclusiveContent {
        forRepository {
            maven {
                name = "Modrinth"
                url = uri("https://api.modrinth.com/maven")
            }
        }
        filter {
            includeGroup("maven.modrinth")
        }
    }
    mavenLocal()
}

fun modImplementationInclude(s : String) {
    dependencies.modImplementation(s)
    dependencies.forgeRuntimeLibrary(s)
    dependencies.include(s)
}

fun modImplementationInclude(s : ConfigurableFileCollection) {
    dependencies.modImplementation(s)
    dependencies.forgeRuntimeLibrary(s)
    dependencies.include(s)
}

dependencies {
    forge("net.neoforged:forge:${rootProject.property("neoforge_version")}")

    if (rootProject.property("custom_sodium")!!.equals("true")) {
        modImplementation(files(rootProject.projectDir.resolve("custom_sodium").resolve(rootProject.property("sodium_version").toString())))
    } else {
        modImplementation("maven.modrinth:sodium:" + rootProject.property("sodium_version"))
    }
    implementation(group = "com.lodborg", name = "interval-tree", version = "1.0.0")
    forgeRuntimeLibrary(group = "com.lodborg", name = "interval-tree", version = "1.0.0")

    modCompileOnly("maven.modrinth:distanthorizons:2.0.0-a-1.18.2")
    compileOnly("io.github.llamalad7:mixinextras-common:0.3.5")
    annotationProcessor("io.github.llamalad7:mixinextras-common:0.3.5")
    implementation("io.github.llamalad7:mixinextras-forge:0.3.5")
    modImplementationInclude(files("glsl-transformer-2.0.0.jar"))
    modImplementationInclude("org.anarres:jcpp:1.4.14")
    modImplementationInclude("com.github.zafarkhaja:java-semver:0.10.2")

    common(project(":common", "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", "transformProductionForge")) { isTransitive = false }

}

tasks.shadowJar {
    exclude("fabric.mod.json")
    exclude("architectury.common.json")
    configurations = listOf(shadowCommon)
    archiveClassifier.set("dev-shadow")
}

tasks.remapJar {
    injectAccessWidener.set(true)
    inputFile.set(tasks.shadowJar.get().archiveFile)
    dependsOn(tasks.shadowJar)
    archiveClassifier.set(null as String?)
    atAccessWideners.add("iris.accesswidener")
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

// this will replace the property "${version}" in your mods.toml
// with the version you've defined in your gradle.properties
tasks.processResources {
    filesMatching("META-INF/mods.toml") {
        expand(mapOf("version" to project.version))
    }
}
