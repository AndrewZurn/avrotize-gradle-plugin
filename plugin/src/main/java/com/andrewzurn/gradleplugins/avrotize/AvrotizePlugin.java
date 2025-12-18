package com.andrewzurn.gradleplugins.avrotize;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;

import java.io.File;

public class AvrotizePlugin implements Plugin<Project> {
    public void apply(Project project) {
        // Create the extension
        AvrotizeExtension extension = project.getExtensions().create("avrotize", AvrotizeExtension.class);

        // Set defaults
        extension.getInputDirectory().convention(project.getLayout().getProjectDirectory().dir("src/main/resources/schema"));
        extension.getOutputDirectory().convention(project.getLayout().getBuildDirectory().dir("generated/sources/avrotize/java/main"));
        extension.getInputFormat().convention("jsonschema");
        extension.getOutputFormat().convention("java");
        extension.getAvrotizePath().convention("avrotize");

        // Register the task
        project.getTasks().register("avrotize", AvrotizeTask.class, task -> {
            task.getInputDirectory().set(extension.getInputDirectory());
            task.getOutputDirectory().set(extension.getOutputDirectory());
            task.getInputFormat().set(extension.getInputFormat());
            task.getOutputFormat().set(extension.getOutputFormat());
            task.getAvrotizePath().set(extension.getAvrotizePath());
            task.getPackageName().set(extension.getPackageName());
        });

        // Add the output directory to the main source set
        project.getPlugins().withType(JavaBasePlugin.class, plugin -> {
            SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
            sourceSets.configureEach(sourceSet -> {
                if (sourceSet.getName().equals(SourceSet.MAIN_SOURCE_SET_NAME)) {
                    sourceSet.getJava().srcDir(extension.getOutputDirectory());
                }
            });
        });
    }
}
