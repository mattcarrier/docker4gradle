package com.github.mattcarrier.docker4gradle.core

import static org.junit.Assert.*

import java.nio.file.Files
import java.nio.file.Paths

import org.gradle.api.Project
import org.gradle.tooling.BuildLauncher
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.junit.After
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import com.google.common.collect.Lists
import com.spotify.docker.client.DefaultDockerClient
import com.spotify.docker.client.DockerClient
import com.spotify.docker.client.DockerClient.ListContainersParam
import com.spotify.docker.client.DockerClient.ListImagesParam
import com.spotify.docker.client.messages.Container
import com.spotify.docker.client.messages.Image

class ConfigurationTest {
	private static DockerClient client;
	private static Optional<String> lastRunningContainerId;

	@ClassRule
	public static TemporaryFolder projectDir = new TemporaryFolder()

	private Project project

	@BeforeClass
	static void classSetup() {
		client = DefaultDockerClient.fromEnv().build()

		lastRunningContainerId = client.listContainers(ListContainersParam.withStatusRunning()).stream().map( { c ->
			c.id()
		}).max(new Comparator() {
			int compare(c1, c2) {
				if (c1.created() < c2.created) return -1
				if (c1.created() == c2.created()) return 0
				return 1
			}
		})

		Files.write(Paths.get(projectDir.newFile("build.gradle").getPath()),
				ConfigurationTest.class.getClassLoader().getResourceAsStream("gradletestbuild").getBytes())
	}

	@After
	void after() {
		getCreatedContainers().parallelStream().forEach({ c ->
			client.stopContainer(c.id(), 30)
			client.removeContainer(c.id())
		})
	}

	List<Container> getCreatedContainers() {
		final List<Container> containers;
		if (lastRunningContainerId.isPresent()) {
			return containers = client.listContainers(ListContainersParam.containersCreatedSince(lastRunningContainerId.get()))
		} else {
			return containers = client.listContainers(ListContainersParam.withStatusRunning())
		}
	}

	void run(List<String> tasks) throws URISyntaxException, IOException {
		final ProjectConnection connection = GradleConnector.newConnector()
				.forProjectDirectory(projectDir.root)
				.connect()

		try {
			final BuildLauncher build = connection.newBuild()

			//select tasks to run:
			build.forTasks(tasks.toArray(new String[0]))

			//kick the build off:
			build.run()
		} finally {
			connection.close()
		}
	}

	@Test
	void start() {
		run(Lists.newArrayList("d4g-start"))

		final Image image = client.listImages(ListImagesParam.byName("wurstmeister/zookeeper:latest")).iterator().next()

		final List<Container> containers = getCreatedContainers()
		assertEquals(1, containers.size())
		assertEquals(image.id, containers.iterator().next().imageId)
	}

	@Test
	void startAndAutoPull() {
		client.removeImage("wurstmeister/zookeeper:latest");
		assertTrue(client.listImages(ListImagesParam.byName("wurstmeister/zookeeper:latest")).isEmpty())
		start()
	}
}
