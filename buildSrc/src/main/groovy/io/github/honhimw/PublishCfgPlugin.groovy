package io.github.honhimw

import com.vanniktech.maven.publish.MavenPublishBaseExtension
import com.vanniktech.maven.publish.MavenPublishPlugin
import io.github.honhimw.task.DeployIfAbsent
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.plugins.signing.SigningPlugin

class PublishCfgPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.plugins.apply SigningPlugin
        project.plugins.apply MavenPublishPlugin

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

        project.tasks.register('deployIfAbsent', DeployIfAbsent)
    }

}
