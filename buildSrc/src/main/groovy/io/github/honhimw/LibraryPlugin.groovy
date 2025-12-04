package io.github.honhimw


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaLibraryPlugin

@SuppressWarnings('unused')
class LibraryPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply JavaLibraryPlugin

        project.plugins.apply JavaCfgPlugin
        project.plugins.apply TestingCfgPlugin
    }

}
