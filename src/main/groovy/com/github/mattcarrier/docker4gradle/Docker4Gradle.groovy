package com.github.mattcarrier.docker4gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.github.mattcarrier.docker4gradle.core.Docker4GradleExtensions
import com.github.mattcarrier.docker4gradle.core.ImageConfiguration
import com.github.mattcarrier.docker4gradle.task.DockerStartTask

class Docker4Gradle implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create("docker", Docker4GradleExtensions)
        project.docker.extensions.images = project.container(ImageConfiguration)
        project.tasks.create("d4g-start", DockerStartTask);
    }
}
