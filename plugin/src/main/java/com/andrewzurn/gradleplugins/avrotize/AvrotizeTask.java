package com.andrewzurn.gradleplugins.avrotize;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.FileSystemOperations;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AvrotizeTask extends DefaultTask {

    @InputDirectory
    public abstract DirectoryProperty getInputDirectory();

    @OutputDirectory
    public abstract DirectoryProperty getOutputDirectory();

    @Input
    public abstract Property<String> getInputFormat();

    @Input
    public abstract Property<String> getOutputFormat();

    @Input
    public abstract Property<String> getAvrotizePath();

    @Input
    @Optional
    public abstract Property<String> getPackageName();

    @Inject
    protected abstract ExecOperations getExecOperations();

    @Inject
    protected abstract FileSystemOperations getFileSystemOperations();

    @TaskAction
    public void run() {
        String avrotizeExe = getAvrotizePath().get();
        checkAvrotizePresence(avrotizeExe);

        File inputDir = getInputDirectory().get().getAsFile();
        File outputDir = getOutputDirectory().get().getAsFile();
        String inFormat = getInputFormat().get();
        String outFormat = getOutputFormat().get();
        String pkg = getPackageName().getOrNull();

        if (!inputDir.exists()) {
            getLogger().warn("Input directory does not exist: " + inputDir.getAbsolutePath());
            return;
        }
        
        // Ensure output directory exists
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        File[] inputFiles = inputDir.listFiles();
        if (inputFiles == null) return;

        for (File inputFile : inputFiles) {
            if (inputFile.isFile()) {
                processFile(avrotizeExe, inputFile, outputDir, inFormat, outFormat, pkg);
            }
        }
    }

    private void checkAvrotizePresence(String avrotizeExe) {
        try {
            getExecOperations().exec(spec -> {
                spec.commandLine(avrotizeExe, "--version");
                spec.setIgnoreExitValue(true);
                spec.setStandardOutput(new ByteArrayOutputStream());
                spec.setErrorOutput(new ByteArrayOutputStream());
            });
        } catch (Exception e) {
            throw new GradleException("Avrotize executable not found at '" + avrotizeExe + "'. " +
                    "Please install it following the instructions at https://github.com/clemensv/avrotize", e);
        }
    }

    private void processFile(String avrotizeExe, File inputFile, File outputDir, String inFormat, String outFormat, String pkg) {
        // Simple mapping logic
        // If input is JSON Schema (json) and output is Java (java)
        // We need j2a -> a2java
        
        // This is a simplified implementation. A robust one would have a full graph or matrix.
        
        if ("jsonschema".equalsIgnoreCase(inFormat) || "json".equalsIgnoreCase(inFormat)) {
            if ("java".equalsIgnoreCase(outFormat)) {
                convertJsonSchemaToJava(avrotizeExe, inputFile, outputDir, pkg);
            } else if ("avro".equalsIgnoreCase(outFormat)) {
                convertJsonSchemaToAvro(avrotizeExe, inputFile, outputDir);
            } else {
                 throw new GradleException("Unsupported conversion: " + inFormat + " to " + outFormat);
            }
        } else if ("avro".equalsIgnoreCase(inFormat)) {
             if ("java".equalsIgnoreCase(outFormat)) {
                convertAvroToJava(avrotizeExe, inputFile, outputDir, pkg);
            } else {
                 throw new GradleException("Unsupported conversion: " + inFormat + " to " + outFormat);
            }
        } else {
            // Fallback or generic handling could go here, or throw exception
            throw new GradleException("Unsupported input format: " + inFormat);
        }
    }

    private void convertJsonSchemaToJava(String avrotizeExe, File inputFile, File outputDir, String pkg) {
        // Step 1: j2a (JSON Schema to Avro)
        // We'll create a temporary file for the Avro schema
        File tempAvroFile = new File(getTemporaryDir(), inputFile.getName() + ".avsc");
        
        List<String> j2aArgs = new ArrayList<>();
        j2aArgs.add(avrotizeExe);
        j2aArgs.add("j2a");
        j2aArgs.add(inputFile.getAbsolutePath());
        j2aArgs.add("--out");
        j2aArgs.add(tempAvroFile.getAbsolutePath());
        
        getLogger().lifecycle("Converting JSON Schema to Avro: " + inputFile.getName());
        execCommand(j2aArgs);

        // Step 2: a2java (Avro to Java)
        convertAvroToJava(avrotizeExe, tempAvroFile, outputDir, pkg);
    }

    private void convertJsonSchemaToAvro(String avrotizeExe, File inputFile, File outputDir) {
        File outputFile = new File(outputDir, inputFile.getName().replace(".json", ".avsc"));
        List<String> args = new ArrayList<>();
        args.add(avrotizeExe);
        args.add("j2a");
        args.add(inputFile.getAbsolutePath());
        args.add("--out");
        args.add(outputFile.getAbsolutePath());
        
        getLogger().lifecycle("Converting JSON Schema to Avro: " + inputFile.getName());
        execCommand(args);
    }

    private void convertAvroToJava(String avrotizeExe, File inputFile, File outputDir, String pkg) {
        List<String> args = new ArrayList<>();
        args.add(avrotizeExe);
        args.add("a2java");
        args.add(inputFile.getAbsolutePath());
        args.add("--out");
        args.add(outputDir.getAbsolutePath());
        
        if (pkg != null && !pkg.isEmpty()) {
            args.add("--package");
            args.add(pkg);
        }

        getLogger().lifecycle("Generating Java code from Avro: " + inputFile.getName());
        execCommand(args);
    }

    private void execCommand(List<String> args) {
        getExecOperations().exec(spec -> {
            spec.commandLine(args);
        });
    }
}
