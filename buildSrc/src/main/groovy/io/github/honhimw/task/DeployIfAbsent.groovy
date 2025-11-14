package io.github.honhimw.task

import groovy.xml.XmlParser
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.nio.charset.StandardCharsets

class DeployIfAbsent extends DefaultTask {

    @Override
    String getGroup() {
        return 'deploy'
    }

    @TaskAction
    void run() {
        def latestVersion = getLatestVersion(project.name)
        println "module: ${project.name}, current: ${project.version}, latest: ${latestVersion}"
        if (project.version != latestVersion) {
            dependsOn ":${project.name}:publishAllPublicationsToMavenCentralRepository"
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
