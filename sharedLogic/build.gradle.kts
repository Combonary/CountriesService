@file:OptIn(ApolloExperimental::class)

import com.apollographql.apollo.annotations.ApolloExperimental
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import org.gradle.api.publish.maven.MavenPublication

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.apollo)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    id("maven-publish")
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
        }
    }
    
    android {
       namespace = "com.pamtech.countriesservice.sharedLogic"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        commonMain.dependencies {
            api(libs.apollo.runtime)
            implementation(libs.ktor.client.core)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
        }
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.apollo.testing.support)
            implementation(libs.kotlinx.coroutines.test)
        }
        val androidHostTest by getting {
            dependencies {
                implementation(libs.runner)
            }
        }
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspIosArm64", libs.room.compiler)
    add("kspIosSimulatorArm64", libs.room.compiler)
}

apollo {
    service("service") {
        packageName.set("com.pamtech.countriesservice.graphql")
        generateDataBuilders.set(true)
        introspection {
            endpointUrl.set("https://countries.trevorblades.com/")
            schemaFile.set(file("src/commonMain/graphql/com/pamtech/countriesservice/graphql/schema.graphqls"))
        }
    }
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { inputStream ->
            load(inputStream)
        }
    }
}

group = "io.github.Combonary"
version = "1.0.2"

publishing {
    publications.withType<MavenPublication> {
        artifactId = "countries-service"
        pom {
            name.set("CountriesService")
            description.set("A Kotlin Multiplatform library for fetching country data.")
            url.set("https://github.com/Combonary/CountriesService")
            licenses {
                license {
                    name.set("MIT")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }
            developers {
                developer {
                    id.set("Combonary")
                    name.set("Combonary")
                }
            }
            scm {
                connection.set("scm:git:git://github.com/Combonary/CountriesService.git")
                developerConnection.set("scm:git:ssh://github.com/Combonary/CountriesService.git")
                url.set("https://github.com/Combonary/CountriesService")
            }
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Combonary/CountriesService")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                    ?: localProperties.getProperty("gpr.user")
                password = System.getenv("GITHUB_TOKEN")
                    ?: localProperties.getProperty("gpr.key")
            }
        }
    }
}

