package com.andrewzurn.gradleplugins.avrotize;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;

public abstract class AvrotizeExtension {
    public abstract DirectoryProperty getInputDirectory();
    public abstract DirectoryProperty getOutputDirectory();
    public abstract Property<String> getInputFormat();
    public abstract Property<String> getOutputFormat();
    public abstract Property<String> getAvrotizePath();
    public abstract Property<String> getPackageName();
}
