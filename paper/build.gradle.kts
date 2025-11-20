import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    id("com.gradleup.shadow") version ("9.1.0")
    id("de.eldoria.plugin-yml.paper") version ("0.8.0")
    id("xyz.jpenilla.run-paper") version ("3.0.2")
}

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }

    maven("https://repo.extendedclip.com/releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    api(project(":api"))

    implementation("org.bstats:bstats-bukkit:3.0.2")

    compileOnly("org.spongepowered:configurate-yaml:4.2.0")
    compileOnly("me.clip:placeholderapi:2.11.6")
}

paper {
    name = getProjectName(rootProject.name)
    main = "${rootProject.group}.${rootProject.name.replace("-", "")}.PaperPlugin"
    description = rootProject.description
    version = rootProject.version.toString()

    author = "Bytephoria"
    website = "https://bytephoria.team"
    apiVersion = "1.20"
    generateLibrariesJson = true
    foliaSupported = true

    serverDependencies {
        register("PlaceholderAPI") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
    }

}

tasks {

    runServer {
        minecraftVersion("1.21.10")
    }

    shadowJar {

        relocate("org.bstats", "team.bytephoria.bstats")

        archiveBaseName.set(rootProject.name)
        archiveVersion.set(rootProject.version.toString())
        archiveClassifier.set("")
    }
}

/**
 * Converts a hyphen-separated project name into PascalCase.
 */
fun getProjectName(baseName: String): String {
    return baseName.split("-")
        .joinToString("") {
                part -> part.replaceFirstChar {
            it.uppercase()
        }
        }
}