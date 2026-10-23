plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
    id("com.google.dagger.hilt.android") version "2.52" apply false
}

// Dependabot : jars transitifs AGP / lint / UTP (pas dans l’APK runtime).
// Force versions patchées — CVE-2024-7254 (protobuf), CVE-2024-47554 (commons-io),
// alertes netty 4.1.93. Ne pas monter protobuf en 4.x (casse outil AGP).
subprojects {
    configurations.configureEach {
        resolutionStrategy {
            force(
                "com.google.protobuf:protobuf-java:3.25.5",
                "commons-io:commons-io:2.18.0",
            )
            eachDependency {
                if (requested.group == "io.netty") {
                    useVersion("4.1.118.Final")
                    because("Dependabot CVE — netty 4.1.93 transitif AGP/grpc")
                }
            }
        }
    }
}
