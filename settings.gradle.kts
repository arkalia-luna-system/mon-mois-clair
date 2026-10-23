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
// Dependabot : forcer aussi le buildscript de chaque projet (UTP/lint).
gradle.beforeProject {
    val localRoot = file("${System.getProperty("user.home")}/.cache/mon-mois-clair/gradle-build")
    project.layout.buildDirectory.set(File(localRoot, project.name))

    val forceNetty = "4.1.139.Final"
    val patched = listOf(
        "com.google.protobuf:protobuf-java:3.25.9",
        "commons-io:commons-io:2.22.0",
        "org.bouncycastle:bcprov-jdk18on:1.86",
        "org.jdom:jdom2:2.0.6.1",
        "org.bitbucket.b_c:jose4j:0.9.7",
        "io.netty:netty-handler:$forceNetty",
        "io.netty:netty-codec:$forceNetty",
        "io.netty:netty-codec-http:$forceNetty",
        "io.netty:netty-codec-http2:$forceNetty",
        "io.netty:netty-common:$forceNetty",
        "io.netty:netty-buffer:$forceNetty",
        "io.netty:netty-transport:$forceNetty",
    )
    fun org.gradle.api.artifacts.ResolutionStrategy.applyPatches() {
        force(*patched.toTypedArray())
        eachDependency {
            if (requested.group == "io.netty") {
                useVersion(forceNetty)
                because("Dependabot — famille netty patchée")
            }
        }
    }
    buildscript.configurations.configureEach { resolutionStrategy.applyPatches() }
    configurations.configureEach { resolutionStrategy.applyPatches() }
}
