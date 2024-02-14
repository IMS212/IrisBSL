pluginManagement {
    repositories {
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.architectury.dev/") }
        maven { url = uri("https://files.minecraftforge.net/maven/") }
        gradlePluginPortal()
    }
}

buildscript {
    repositories {
        mavenCentral()
    }

    dependencies {
        classpath("org.eclipse.jgit:org.eclipse.jgit:6.8.0.202311291450-r")
    }
}

rootProject.name = "iris"

include("common")
include("fabric")
include("neoforge")
