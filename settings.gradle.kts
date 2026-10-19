pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "MonMoisClair"
include(":app")

// Le volume T7 (exFAT/AFP) génère des fichiers AppleDouble `._*` qui cassent
 // le AAPT/AGP. Les artefacts de build vont donc sur le disque local.
gradle.beforeProject {
    val localRoot = file("${System.getProperty("user.home")}/.cache/mon-mois-clair/gradle-build")
    project.layout.buildDirectory.set(File(localRoot, project.name))
}
