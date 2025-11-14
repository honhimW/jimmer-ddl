package io.github.honhimw

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.testing.Test

class TestingCfgPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.dependencies { DependencyHandler dep ->
            def libs = project.rootProject.extensions.getByType(VersionCatalogsExtension).named('libs')
            libs.findLibrary('junit-bom').ifPresent {
                dep.add JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, dep.platform(it)
                dep.add JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, 'org.junit.jupiter:junit-jupiter'
                dep.add JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, 'org.junit.platform:junit-platform-engine'
                dep.add JavaPlugin.TEST_IMPLEMENTATION_CONFIGURATION_NAME, 'org.junit.platform:junit-platform-launcher'
            }
        }
        project.tasks.withType(Test).configureEach { test ->
            test.useJUnitPlatform()
            test.testLogging {
                showStandardStreams = true
            }
        }
    }
}
