package com.github.mattcarrier.docker4gradle.task

import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel

import com.github.mattcarrier.docker4gradle.core.Docker4GradleExtensions
import com.github.mattcarrier.docker4gradle.core.ImageConfiguration
import com.google.common.collect.ImmutableMap
import com.spotify.docker.client.AnsiProgressHandler;
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.messages.ContainerConfig
import com.spotify.docker.client.messages.Image

class Docker4GradleDefaultTask extends DefaultTask {
	protected Docker4GradleDefaultTask() {
		logging.captureStandardOutput(LogLevel.QUIET)
	}

	protected Docker4GradleExtensions getDocker4GradleExtensions() {
		project.extensions.docker
	}

	protected DockerClient getDockerClient() {
		getDocker4GradleExtensions().client
	}

	protected Image getImage(ImageConfiguration imageConfiguration) {
		final String repoTag = imageConfiguration.image.contains(":") ?: imageConfiguration.image + ":latest"
		Image image = project.extensions.docker.imageByNameMap.get(repoTag)
		if (!image) {
			getDockerClient().pull(imageConfiguration.image, new AnsiProgressHandler())
			getDocker4GradleExtensions().refreshImageMaps()
		}

		project.extensions.docker.imageByNameMap.get(repoTag)
	}

	protected ContainerConfig buildContainerConfig(Image image, ImageConfiguration imageConfiguration) {
		ContainerConfig.builder().env(imageConfiguration.env).image(image.id()).build()
	}
}
