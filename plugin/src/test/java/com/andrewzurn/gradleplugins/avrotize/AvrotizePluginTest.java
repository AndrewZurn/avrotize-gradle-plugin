package com.andrewzurn.gradleplugins.avrotize;

import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AvrotizePluginTest {
    @Test void pluginRegistersATask() {
        // Create a test project and apply the plugin
        Project project = ProjectBuilder.builder().build();
        project.getPlugins().apply("com.andrewzurn.gradleplugins.avrotize");

        // Verify the result
        assertNotNull(project.getTasks().findByName("avrotize"));
    }
}
