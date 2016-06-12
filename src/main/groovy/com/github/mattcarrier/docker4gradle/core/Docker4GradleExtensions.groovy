package com.github.mattcarrier.docker4gradle.core

import java.util.stream.Collectors

import com.google.common.collect.ImmutableMap
import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.exceptions.DockerException
import com.spotify.docker.client.messages.Container;
import com.spotify.docker.client.messages.Image

class Docker4GradleExtensions {
	final DockerClient client
	private volatile ImmutableMap<String, Image> imageByNameMap
	private volatile ImmutableMap<String, Image> imageByIdMap
	private volatile ImmutableMap<String, Container> containerMap

	Docker4GradleExtensions() {
		client = DefaultDockerClient.fromEnv().build()
	}

	ImmutableMap<String, Image> getImageByNameMap() {
		if (!imageByNameMap) {
			refreshImageMaps()
		}

		imageByNameMap
	}

	ImmutableMap<String, Image> getImageByIdMap() {
		if (!imageByIdMap) {
			refreshImageMaps()
		}

		imageByIdMap
	}

	void refreshImageMaps() {
		imageByNameMap = ImmutableMap.copyOf(client.listImages().parallelStream().collect(Collectors.toMap ({ image ->
			image.repoTags().get(0)
		}, { image -> image } )))
		imageByIdMap = ImmutableMap.copyOf(imageByNameMap.values().parallelStream().collect(Collectors.toMap ({ image ->
			image.id()
		}, { image -> image } )))
	}

	ImmutableMap<String, Container> getContainerMap() {
		if (null == containerMap) {
			refreshContainerMap()
		}

		containerMap
	}

	void refreshContainerMap() {
		containerMap = ImmutableMap.copyOf(client.listContainers().parallelStream().collect(Collectors.toMap ({ container -> container.get }, { container -> container } )))
	}
}
