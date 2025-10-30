package io.github.honhimw

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.jvm.tasks.Jar
import org.gradle.plugins.signing.SigningPlugin

import static java.nio.charset.StandardCharsets.UTF_8

class LibraryPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply JavaLibraryPlugin
        project.plugins.apply SigningPlugin
        project.plugins.apply MavenPublishPlugin

        java project
        dependencies project
        test project
        publish project

        project.configurations.configureEach {
            it.resolutionStrategy {
                cacheChangingModulesFor 0, 'SECONDS'
                cacheDynamicVersionsFor 0, 'SECONDS'
            }
        }
    }

    void java(Project project) {
        project.extensions.configure(JavaPluginExtension) { java ->
            java.sourceCompatibility = JavaVersion.VERSION_1_8
            java.targetCompatibility = JavaVersion.VERSION_1_8
        }
        project.tasks.withType(JavaCompile).configureEach { compile ->
            compile.sourceCompatibility = JavaVersion.VERSION_1_8
            compile.targetCompatibility = JavaVersion.VERSION_1_8
            compile.options.encoding = UTF_8.name()
            compile.options.compilerArgs << "-Xlint:deprecation"
        }
        project.tasks.withType(Jar).configureEach { jar ->
            jar.enabled = true
        }
    }

    void dependencies(Project project) {
        project.repositories {
            project.repositories.mavenCentral()
        }
        project.dependencies {
            testImplementation platform(project.libs.junit.bom)
            testImplementation 'org.junit.jupiter:junit-jupiter'
            testImplementation 'org.junit.platform:junit-platform-engine'
            testImplementation 'org.junit.platform:junit-platform-launcher'
        }
    }

    void test(Project project) {
        project.tasks.withType(Test).configureEach { test ->
            test.useJUnitPlatform()
            test.testLogging {
                showStandardStreams = true
            }
        }
    }

    void publish(Project project) {
        project.afterEvaluate {
            project.extensions.configure(MavenPublishBaseExtension) {
                it.publishToMavenCentral false
                it.signAllPublications()

                it.coordinates project.group as String, project.name, project.version as String

                it.pom { pom ->
                    pom.name.set project.name
                    pom.description.set project.description
                    pom.url.set 'https://github.com/honhimW/jimmer-ddl'
                    pom.licenses { licenses ->
                        licenses.license { license ->
                            license.name.set 'The Apache License, Version 2.0'
                            license.url.set 'https://www.apache.org/licenses/LICENSE-2.0.txt'
                            license.distribution.set 'https://www.apache.org/licenses/LICENSE-2.0.txt'
                        }
                    }
                    pom.developers { developers ->
                        developers.developer { developer ->
                            developer.id.set 'honhimw'
                            developer.name.set 'honhimw'
                            developer.url.set 'https://honhimW.github.io'
                            developer.email.set 'honhimw@outlook.com'
                        }
                    }
                    pom.scm { scm ->
                        scm.url.set 'https://github.com/honhimW/jimmer-ddl'
                        scm.connection.set 'scm:git:git://github.com/honhimW/jimmer-ddl.git'
                        scm.developerConnection.set 'scm:git:ssh://github.com/honhimW/jimmer-ddl.git'
                    }
                }
            }
        }
    }

}
