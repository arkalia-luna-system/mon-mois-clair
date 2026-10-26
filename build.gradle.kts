plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
}

// Dependabot : jars transitifs AGP / lint / UTP (classpath build — pas APK runtime).
// Ne pas monter protobuf en 4.x (casse outil AGP).
val patchedForces = arrayOf(
    "com.google.protobuf:protobuf-java:3.25.9",
    "commons-io:commons-io:2.22.0",
    "org.apache.commons:commons-compress:1.26.0",
    "org.apache.commons:commons-lang3:3.18.0",
    "org.bouncycastle:bcprov-jdk18on:1.86",
    "org.bouncycastle:bcpkix-jdk18on:1.86",
    "org.jdom:jdom2:2.0.6.1",
    "org.bitbucket.b_c:jose4j:0.9.7",
    "io.netty:netty-handler:4.1.139.Final",
    "io.netty:netty-codec:4.1.139.Final",
    "io.netty:netty-codec-http:4.1.139.Final",
    "io.netty:netty-codec-http2:4.1.139.Final",
    "io.netty:netty-common:4.1.139.Final",
    "io.netty:netty-buffer:4.1.139.Final",
    "io.netty:netty-transport:4.1.139.Final",
    "io.netty:netty-transport-native-unix-common:4.1.139.Final",
    "io.netty:netty-handler-proxy:4.1.139.Final",
    "io.netty:netty-codec-socks:4.1.139.Final",
)

fun org.gradle.api.artifacts.ResolutionStrategy.applyPatchedForces() {
    force(*patchedForces)
    eachDependency {
        if (requested.group == "io.netty") {
            useVersion("4.1.139.Final")
            because("Dependabot CVE — aligner toute la famille netty")
        }
    }
}

allprojects {
    configurations.configureEach {
        resolutionStrategy.applyPatchedForces()
    }
}
