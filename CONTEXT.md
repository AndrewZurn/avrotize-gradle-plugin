# Agent Context: Avrotize Gradle Plugin

> **Note to Agent:** This file contains critical context, architectural decisions, and troubleshooting history established during previous development sessions. Use this to orient yourself quickly and avoid regressing on solved issues.

## 1. Project Identity
*   **Goal:** A Gradle plugin wrapping the [Avrotize](https://github.com/clemensv/avrotize) Python CLI.
*   **Core Function:** Automates schema conversion (JSON Schema, Avro, Proto) and code generation (Java) within the Gradle build lifecycle.
*   **Target Audience:** JVM developers (Java, Kotlin) needing schema-first development.

## 2. Architectural Decisions & Reasoning
*   **CLI Wrapper Strategy:**
    *   *Decision:* The plugin does **not** reimplement Avrotize logic. It wraps the CLI executable.
    *   *Reasoning:* Avrotize is complex; wrapping ensures feature parity and ease of maintenance.
    *   *Implication:* The user must have `avrotize` installed (via pip). The plugin checks for its presence.
*   **Generic JVM Support:**
    *   *Initial State:* Plugin applied `java` plugin directly.
    *   *Problem:* Failed in Kotlin-only projects or other JVM languages.
    *   *Solution:* Switched to `project.getPlugins().withType(JavaBasePlugin.class)`.
    *   *Benefit:* Automatically detects `main` source set in any JVM project and adds generated sources.
*   **Testing Strategy (Critical):**
    *   *Pattern:* Functional tests use **Gradle TestKit**.
    *   *Constraint:* We cannot assume `avrotize` is installed on the test runner.
    *   *Solution:* Tests generate a **mock shell script** (`avrotize-mock.sh`) that simulates CLI arguments and file outputs.
    *   *Action:* When writing new tests, ensure the mock script handles the specific arguments your new feature uses.

## 3. Implementation Details & "Gotchas"
*   **Task Logic (`AvrotizeTask.java`):**
    *   **Chaining:** Some conversions require multiple steps.
        *   *JSON -> Java:* `j2a` (JSON to Avro) -> `a2java` (Avro to Java).
        *   *JSON -> Proto:* `s2p` (Schema to Proto) -> `a2p` (Avro to Proto). *Note: Verify `s2p` vs `j2a` usage in future sessions.*
    *   **Inputs/Outputs:** Uses Gradle's `DirectoryProperty` for incremental build support (though full incremental support requires more fine-grained input tracking).
*   **Build Script (`build.gradle.kts`):**
    *   *Resolved Issue:* A previous edit introduced an invalid `artifact = ...` property in the `gradlePlugin` block, causing build failures. This was removed.
    *   *Status:* Build is currently stable.

## 4. Current File Map
*   **Logic:** `plugin/src/main/java/com/andrewzurn/gradleplugins/avrotize/AvrotizeTask.java` (Main execution logic)
*   **Entry:** `plugin/src/main/java/com/andrewzurn/gradleplugins/avrotize/AvrotizePlugin.java` (Registration & SourceSet config)
*   **Config:** `plugin/src/main/java/com/andrewzurn/gradleplugins/avrotize/AvrotizeExtension.java` (DSL definition)
*   **Tests:** `plugin/src/functionalTest/java/com/andrewzurn/gradleplugins/avrotize/AvrotizePluginFunctionalTest.java` (Integration tests with mocks)

## 5. Future Roadmap / Pending Tasks
*   **Publishing:** The `maven-publish` or `com.gradle.plugin-publish` setup is missing.
*   **Validation:** The `convertJsonSchemaToProto` method uses `s2p`. Verify if this is the correct Avrotize command or if it should be `j2a`.
*   **Error Handling:** Current error handling is basic (checks exit code). Could be improved to parse Avrotize stderr.

## 6. User Instructions & Preferences
*   **Philosophy:** "Wrap, don't reimplement." The plugin is a bridge to the CLI tool.
*   **Compatibility:** Ensure the plugin works for *any* JVM project (Java, Kotlin, Groovy), not just those applying the `java` plugin.
*   **Testing:** "Ensure that it's also well tested." Functional tests must verify the plugin's behavior in a real Gradle environment (using TestKit).
*   **Documentation:** Maintain clear documentation and always give credit to the original Avrotize authors (Clemens Vasters).
*   **Context Maintenance:** Keep this `CONTEXT.md` file updated with new architectural decisions and "gotchas" at the end of every session.
