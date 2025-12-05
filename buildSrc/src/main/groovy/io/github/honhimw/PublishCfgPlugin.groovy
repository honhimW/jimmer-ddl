package io.github.honhimw

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import groovy.xml.XmlParser
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.signing.SigningPlugin

import java.nio.charset.StandardCharsets

class PublishCfgPlugin implements Plugin<Project> {

    private Project project

    @Override
    void apply(Project project) {
        this.project = project
        project.plugins.apply SigningPlugin
        project.plugins.apply MavenPublishPlugin

        project.afterEvaluate {
            project.extensions.configure(MavenPublishBaseExtension) {
                it.publishToMavenCentral false
                it.signAllPublications()

                it.coordinates project.group as String, project.name, project.version as String

                it.pom { pom ->
                    pom.name.set project.name
                    pom.description.set project.description
                    pom.url.set 'https://github.com/honhimW/jimmer-playground'
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
                        scm.url.set 'https://github.com/honhimW/jimmer-playground'
                        scm.connection.set 'scm:git:git://github.com/honhimW/jimmer-playground.git'
                        scm.developerConnection.set 'scm:git:ssh://github.com/honhimW/jimmer-playground.git'
                    }
                }
            }
        }

        project.tasks.register('deployIfAbsent') {
            it.group = 'deploy'
            def latestVersion = getLatestVersion(project.name)
            println "module: ${project.name}, current: ${project.version}, latest: ${latestVersion}"
            if (project.version != latestVersion) {
                it.dependsOn ":${project.name}:publishAllPublicationsToMavenCentralRepository"
            }
        }
    }

    String getLatestVersion(String artifactId) {
        def url = "https://repo1.maven.org/maven2/io/github/honhimw/${artifactId}/maven-metadata.xml"
        def response = project.uri(url).toURL().openStream().withCloseable { stream -> return stream.bytes }
        def xmlContent = new XmlParser().parseText(new String(response, StandardCharsets.UTF_8))
        def latestVersion = xmlContent?.versioning?.latest?.text()
        return latestVersion
    }

}
