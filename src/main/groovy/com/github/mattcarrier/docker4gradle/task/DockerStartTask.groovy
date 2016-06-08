package com.github.mattcarrier.docker4gradle.task

import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException

import com.spotify.docker.client.messages.ContainerCreation
import com.spotify.docker.client.messages.Image

public class DockerStartTask extends Docker4GradleDefaultTask {
    @TaskAction
    public void start() {
        project.extensions.docker.images.stream().forEach { imageConfiguration ->
            Image image = getImage(imageConfiguration);
            try {
                final ContainerCreation container = getDockerClient()
                        .createContainer(buildContainerConfig(image, imageConfiguration));
                getDockerClient().startContainer(container.id());
            } catch (Exception e) {
                throw new TaskExecutionException(this, e);
            }
        }
    }
}
