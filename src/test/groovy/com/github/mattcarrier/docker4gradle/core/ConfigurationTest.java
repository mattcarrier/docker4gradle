package com.github.mattcarrier.docker4gradle.core;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Test;

public class ConfigurationTest {
    private Project project;

    @Before
    public void setup() {
        project = ProjectBuilder.builder().build();
    }

    @Test
    public void dockerObject() {
        
    }
}
