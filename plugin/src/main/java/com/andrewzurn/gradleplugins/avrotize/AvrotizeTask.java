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

        getLogger().info("Input directory: " + inputDir.getAbsolutePath());
        if (!inputDir.exists()) {
            getLogger().warn("Input directory does not exist: " + inputDir.getAbsolutePath());
            return;
        }

        // Ensure output directory exists
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Use fileTree to recursively traverse the input directory
        for (File inputFile : getProject().fileTree(inputDir)) {
            getLogger().info("Processing for input file: " + inputFile.getAbsolutePath());
            processFile(avrotizeExe, inputFile, outputDir, inFormat, outFormat, pkg);
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
        // This is a simplified implementation. A robust one would have a full graph or matrix.
        
        if ("jsonschema".equalsIgnoreCase(inFormat) || "json".equalsIgnoreCase(inFormat)) {
            if ("java".equalsIgnoreCase(outFormat)) {
                convertJsonSchemaToJava(avrotizeExe, inputFile, outputDir, pkg);
            } else if ("avro".equalsIgnoreCase(outFormat)) {
                convertJsonSchemaToAvro(avrotizeExe, inputFile, outputDir);
            } else if ("proto".equalsIgnoreCase(outFormat) || "protobuf".equalsIgnoreCase(outFormat)) {
                convertJsonSchemaToProto(avrotizeExe, inputFile, outputDir);
            } else {
                 throw new GradleException("Unsupported conversion: " + inFormat + " to " + outFormat);
            }
        } else if ("avro".equalsIgnoreCase(inFormat)) {
             if ("java".equalsIgnoreCase(outFormat)) {
                convertAvroToJava(avrotizeExe, inputFile, outputDir, pkg);
            } else if ("proto".equalsIgnoreCase(outFormat) || "protobuf".equalsIgnoreCase(outFormat)) {
                convertAvroToProto(avrotizeExe, inputFile, outputDir);
            } else {
                 throw new GradleException("Unsupported conversion: " + inFormat + " to " + outFormat);
            }
        } else {
            // Fallback or generic handling could go here, or throw exception
            throw new GradleException("Unsupported input format: " + inFormat);
        }
    }

    private void convertJsonSchemaToJava(String avrotizeExe, File inputFile, File outputDir, String pkg) {
        List<String> args = new ArrayList<>();
        args.add(avrotizeExe);
        args.add("s2java");
        args.add(inputFile.getAbsolutePath());
        args.add("--out");
        args.add(outputDir.getAbsolutePath());
        args.add("--jackson-annotation");

        if (pkg != null && !pkg.isEmpty()) {
            args.add("--package");
            args.add(pkg);
        }
        
        getLogger().lifecycle("Generating Java code from JSON Schema: " + inputFile.getName());
        execCommand(args);
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

    private void convertJsonSchemaToProto(String avrotizeExe, File inputFile, File outputDir) {
        List<String> args = new ArrayList<>();
        args.add(avrotizeExe);
        args.add("s2p");
        args.add(inputFile.getAbsolutePath());
        args.add("--out");
        args.add(outputDir.getAbsolutePath());
        args.add("--naming-mode");
        args.add("snake");
        
        getLogger().lifecycle("Converting JSON Schema to Proto: " + inputFile.getName());
        execCommand(args);
    }

    private void convertAvroToProto(String avrotizeExe, File inputFile, File outputDir) {
        List<String> args = new ArrayList<>();
        args.add(avrotizeExe);
        args.add("a2p");
        args.add(inputFile.getAbsolutePath());
        args.add("--out");
        args.add(outputDir.getAbsolutePath());
        args.add("--naming-mode");
        args.add("snake");
        
        getLogger().lifecycle("Converting Avro to Proto: " + inputFile.getName());
        execCommand(args);
    }

    private void convertAvroToJava(String avrotizeExe, File inputFile, File outputDir, String pkg) {
        List<String> args = new ArrayList<>();
        args.add(avrotizeExe);
        args.add("a2java");
        args.add(inputFile.getAbsolutePath());
        args.add("--out");
        args.add(outputDir.getAbsolutePath());
        args.add("--jackson-annotation");
        
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
