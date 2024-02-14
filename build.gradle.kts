import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.eclipse.jgit.lib.Constants
import java.io.IOException

plugins {
    java
    id("architectury-plugin") version "3.4-SNAPSHOT"
    id("dev.architectury.loom") version "1.5-SNAPSHOT" apply false
}

architectury {
    minecraft = rootProject.property("minecraft_version").toString()
}

subprojects {
    apply(plugin = "dev.architectury.loom")

    val loom = project.extensions.getByName<LoomGradleExtensionAPI>("loom")

    dependencies {
        "minecraft"("com.mojang:minecraft:${project.property("minecraft_version")}")
        // The following line declares the mojmap mappings, you may use other mappings as well
        "mappings"(
                loom.officialMojangMappings()
        )
        // The following line declares the yarn mappings you may select this one as well.
        // "mappings"("net.fabricmc:yarn:1.18.2+build.3:v2")
    }
}

allprojects {
    apply(plugin = "java")
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")

    fun getVersionMetadata() : String {
        val baseVersion: String = rootProject.property("mod_version").toString().replace("-development-environment", "") + "-" + project.name

        val buildId: String? = System.getenv("GITHUB_RUN_NUMBER")

        if (System.getProperty("iris.release", "false") == "true") {
            // We don't want any suffix if we're doing a publish.
            return baseVersion
        }

        var commitHash = ""
        var isDirty = false
        try {
            val git = Git.open(rootProject.projectDir)
            isDirty = !git.status().call().uncommittedChanges.isEmpty()
            commitHash = git.repository.parseCommit(git.repository.resolve(Constants.HEAD).toObjectId()).name.substring(0, 8)
            git.close()
        } catch (e: RepositoryNotFoundException) {
            // User might have downloaded the repository as a zip.
            return baseVersion + "-nogit"
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: GitAPIException) {
            e.printStackTrace()
        }

        return if (buildId != null) {
            "$baseVersion-build.$buildId-$commitHash"
        } else {
            baseVersion + "-" + commitHash + (if (isDirty) "-dirty" else "")
        }
    }

    base.archivesName.set(rootProject.property("mod_id").toString())
    //base.archivesBaseName = rootProject.property("archives_base_name").toString()
    version = getVersionMetadata()
    group = rootProject.property("maven_group").toString()

    repositories {
        // Add repositories to retrieve artifacts from in here.
        // You should only use this when depending on other mods because
        // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
        // See https://docs.gradle.org/current/userguide/declaring_repositories.html
        // for more information about repositories.
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    java {
        withSourcesJar()
    }
}
