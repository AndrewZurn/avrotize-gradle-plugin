package com.andrewzurn.gradleplugins.avrotize;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.io.FileWriter;
import java.nio.file.Files;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.BuildResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

class AvrotizePluginFunctionalTest {
    @TempDir
    File projectDir;

    private File getBuildFile() {
        return new File(projectDir, "build.gradle");
    }

    private File getSettingsFile() {
        return new File(projectDir, "settings.gradle");
    }

    @Test void canRunTask() throws IOException {
        writeString(getSettingsFile(), "");
        
        // Create a dummy avrotize script
        File avrotizeMock = new File(projectDir, "avrotize-mock.sh");
        writeString(avrotizeMock, "#!/bin/sh\n" +
                "if [ \"$1\" = \"--version\" ]; then\n" +
                "  echo \"avrotize 1.0.0\"\n" +
                "  exit 0\n" +
                "fi\n" +
                "echo \"Mock Avrotize called with $@\"\n" +
                "# Simulate output creation\n" +
                "if [ \"$1\" = \"j2a\" ]; then\n" +
                "  # The output file is specified by --out <file>\n" +
                "  # We need to find the argument after --out\n" +
                "  outfile=\"\"\n" +
                "  next_is_out=0\n" +
                "  for arg in \"$@\"; do\n" +
                "    if [ $next_is_out -eq 1 ]; then\n" +
                "      outfile=\"$arg\"\n" +
                "      next_is_out=0\n" +
                "    fi\n" +
                "    if [ \"$arg\" = \"--out\" ]; then\n" +
                "      next_is_out=1\n" +
                "    fi\n" +
                "  done\n" +
                "  if [ -n \"$outfile\" ]; then\n" +
                "    touch \"$outfile\"\n" +
                "  fi\n" +
                "fi\n" +
                "if [ \"$1\" = \"a2java\" ]; then\n" +
                "  # The output dir is specified by --out <dir>\n" +
                "  # We need to find the argument after --out\n" +
                "  outdir=\"\"\n" +
                "  next_is_out=0\n" +
                "  for arg in \"$@\"; do\n" +
                "    if [ $next_is_out -eq 1 ]; then\n" +
                "      outdir=\"$arg\"\n" +
                "      next_is_out=0\n" +
                "    fi\n" +
                "    if [ \"$arg\" = \"--out\" ]; then\n" +
                "      next_is_out=1\n" +
                "    fi\n" +
                "  done\n" +
                "  if [ -n \"$outdir\" ]; then\n" +
                "    mkdir -p \"$outdir\"\n" +
                "    touch \"$outdir/Test.java\"\n" +
                "  fi\n" +
                "fi\n" +
                "if [ \"$1\" = \"a2p\" ]; then\n" +
                "  # The output dir is specified by --out <dir>\n" +
                "  # We need to find the argument after --out\n" +
                "  outdir=\"\"\n" +
                "  next_is_out=0\n" +
                "  for arg in \"$@\"; do\n" +
                "    if [ $next_is_out -eq 1 ]; then\n" +
                "      outdir=\"$arg\"\n" +
                "      next_is_out=0\n" +
                "    fi\n" +
                "    if [ \"$arg\" = \"--out\" ]; then\n" +
                "      next_is_out=1\n" +
                "    fi\n" +
                "  done\n" +
                "  if [ -n \"$outdir\" ]; then\n" +
                "    mkdir -p \"$outdir\"\n" +
                "    touch \"$outdir/test.proto\"\n" +
                "  fi\n" +
                "fi\n");
        avrotizeMock.setExecutable(true);

        // Create a dummy input file
        File schemaDir = new File(projectDir, "src/main/resources/schema");
        schemaDir.mkdirs();
        File schemaFile = new File(schemaDir, "test.json");
        writeString(schemaFile, "{}");

        writeString(getBuildFile(),
            "plugins {\n" +
            "  id('com.andrewzurn.gradleplugins.avrotize')\n" +
            "}\n" +
            "avrotize {\n" +
            "  avrotizePath = '" + avrotizeMock.getAbsolutePath() + "'\n" +
            "  packageName = 'com.example'\n" +
            "}\n");

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("avrotize");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        // Verify the result
        assertTrue(result.getOutput().contains("Converting JSON Schema to Avro"));
        assertTrue(result.getOutput().contains("Generating Java code from Avro"));
        
        File generatedFile = new File(projectDir, "build/generated/sources/avrotize/java/main/Test.java");
        assertTrue(generatedFile.exists(), "Generated file should exist");
    }

    @Test void canRunProtoTask() throws IOException {
        writeString(getSettingsFile(), "");
        
        // Create a dummy avrotize script (reusing the logic from canRunTask, but we need to duplicate or extract it)
        // For simplicity, I'll just copy the mock script creation here or assume it's the same test class instance? 
        // No, JUnit creates new instance per test.
        
        File avrotizeMock = new File(projectDir, "avrotize-mock.sh");
        writeString(avrotizeMock, "#!/bin/sh\n" +
                "if [ \"$1\" = \"--version\" ]; then\n" +
                "  echo \"avrotize 1.0.0\"\n" +
                "  exit 0\n" +
                "fi\n" +
                "echo \"Mock Avrotize called with $@\"\n" +
                "if [ \"$1\" = \"j2a\" ]; then\n" +
                "  outfile=\"\"\n" +
                "  next_is_out=0\n" +
                "  for arg in \"$@\"; do\n" +
                "    if [ $next_is_out -eq 1 ]; then\n" +
                "      outfile=\"$arg\"\n" +
                "      next_is_out=0\n" +
                "    fi\n" +
                "    if [ \"$arg\" = \"--out\" ]; then\n" +
                "      next_is_out=1\n" +
                "    fi\n" +
                "  done\n" +
                "  if [ -n \"$outfile\" ]; then\n" +
                "    touch \"$outfile\"\n" +
                "  fi\n" +
                "fi\n" +
                "if [ \"$1\" = \"a2p\" ]; then\n" +
                "  outdir=\"\"\n" +
                "  next_is_out=0\n" +
                "  for arg in \"$@\"; do\n" +
                "    if [ $next_is_out -eq 1 ]; then\n" +
                "      outdir=\"$arg\"\n" +
                "      next_is_out=0\n" +
                "    fi\n" +
                "    if [ \"$arg\" = \"--out\" ]; then\n" +
                "      next_is_out=1\n" +
                "    fi\n" +
                "  done\n" +
                "  if [ -n \"$outdir\" ]; then\n" +
                "    mkdir -p \"$outdir\"\n" +
                "    touch \"$outdir/test.proto\"\n" +
                "  fi\n" +
                "fi\n");
        avrotizeMock.setExecutable(true);

        File schemaDir = new File(projectDir, "src/main/resources/schema");
        schemaDir.mkdirs();
        File schemaFile = new File(schemaDir, "test.json");
        writeString(schemaFile, "{}");

        writeString(getBuildFile(),
            "plugins {\n" +
            "  id('com.andrewzurn.gradleplugins.avrotize')\n" +
            "}\n" +
            "avrotize {\n" +
            "  avrotizePath = '" + avrotizeMock.getAbsolutePath() + "'\n" +
            "  outputFormat = 'proto'\n" +
            "}\n");

        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("avrotize");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

        assertTrue(result.getOutput().contains("Converting JSON Schema to Avro"));
        assertTrue(result.getOutput().contains("Generating Proto definitions from Avro"));
        
        File generatedFile = new File(projectDir, "build/generated/sources/avrotize/java/main/test.proto");
        assertTrue(generatedFile.exists(), "Generated proto file should exist");
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}
