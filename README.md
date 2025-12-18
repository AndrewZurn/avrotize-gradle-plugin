# Avrotize Gradle Plugin

A Gradle plugin that wraps the [Avrotize](https://github.com/clemensv/avrotize) command-line tool to facilitate schema conversion and code generation within your Gradle build process.

## Overview

Avrotize is a powerful tool for converting between various data structure definitions (JSON Schema, Avro, Protobuf, etc.) and generating code for multiple programming languages. This plugin integrates Avrotize into the Gradle build lifecycle, allowing you to automatically generate Java classes or other schema formats from your source schemas.

## Features

*   **Seamless Integration**: Automatically hooks into the build process.
*   **Configurable**: Customize input/output directories, formats, and the Avrotize executable path.
*   **JVM Support**: Works with Java, Kotlin, and other JVM-based Gradle projects.
*   **Auto-Compilation**: Generated Java code is automatically added to the project's main source set for compilation.
*   **Chained Conversions**: Supports multi-step conversions (e.g., JSON Schema -> Avro -> Java) automatically.

## Prerequisites

This plugin requires the `avrotize` command-line tool to be installed on your system.

To install Avrotize, please follow the instructions in the [official repository](https://github.com/clemensv/avrotize):

## Usage

### Applying the Plugin

Add the following to your `build.gradle.kts` (Kotlin DSL) or `build.gradle` (Groovy DSL):

**Kotlin DSL:**

```kotlin
plugins {
    id("com.andrewzurn.gradleplugins.avrotize") version "1.0.0"
}
```

**Groovy DSL:**

```groovy
plugins {
    id 'com.andrewzurn.gradleplugins.avrotize' version '1.0.0'
}
```

### Configuration

Configure the plugin using the `avrotize` block. The following example shows the available options with their default values:

```kotlin
avrotize {
    // Directory containing your source schemas
    // Default: src/main/resources/schema
    inputDirectory.set(file("src/main/resources/schemas"))

    // Directory where generated files will be placed
    // Default: build/generated/sources/avrotize/java/main
    outputDirectory.set(layout.buildDirectory.dir("generated/my-sources"))

    // Format of the input files (e.g., "jsonschema", "avro")
    // Default: "jsonschema"
    inputFormat.set("jsonschema")

    // Desired output format (e.g., "java", "avro", "proto")
    // Default: "java"
    outputFormat.set("java")

    // Path to the avrotize executable
    // Default: "avrotize" (assumes it's in your PATH)
    avrotizePath.set("/usr/local/bin/avrotize")

    // Optional: Package name for generated Java classes
    packageName.set("com.example.generated")
}
```

### Supported Conversions

The plugin currently supports the following conversion flows:

*   **JSON Schema** (`jsonschema` / `json`) -> **Java** (`java`)
*   **JSON Schema** (`jsonschema` / `json`) -> **Avro** (`avro`)
*   **JSON Schema** (`jsonschema` / `json`) -> **Protobuf** (`proto` / `protobuf`)
*   **Avro** (`avro`) -> **Java Code** (`java`)
*   **Avro** (`avro`) -> **Protobuf Definitions** (`proto` / `protobuf`)

## Credits

This plugin is a wrapper around the excellent **Avrotize** tool created by **Clemens Vasters** and others.

*   **Avrotize Project**: [https://github.com/clemensv/avrotize](https://github.com/clemensv/avrotize)

Please refer to the original project for detailed documentation on Avrotize's capabilities and schema support.

## License

MIT
