package io.github.honhimw

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar

import static java.nio.charset.StandardCharsets.UTF_8

class JavaCfgPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
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

        project.repositories {
            project.repositories.mavenCentral()
        }

        project.configurations.configureEach {
            it.resolutionStrategy {
                cacheChangingModulesFor 0, 'SECONDS'
                cacheDynamicVersionsFor 0, 'SECONDS'
            }
        }

        def using = project.configurations.create('using')
        using.setCanBeResolved false
        using.setCanBeConsumed false
        using.setCanBeDeclared true
        project.plugins.withType(JavaPlugin).configureEach {
            project.extensions.getByType(JavaPluginExtension).sourceSets.configureEach { ss ->
                project.configurations.named(ss.compileClasspathConfigurationName).configure { it.extendsFrom using }
                project.configurations.named(ss.runtimeClasspathConfigurationName).configure { it.extendsFrom using }
                project.configurations.named(ss.annotationProcessorConfigurationName).configure { it.extendsFrom using }
                project.configurations.named(ss.compileOnlyConfigurationName).configure {
                    it.extendsFrom project.configurations.named(ss.annotationProcessorConfigurationName).get()
                }
            }
        }
    }
}
