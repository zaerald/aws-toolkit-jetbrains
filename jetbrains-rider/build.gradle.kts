// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import groovy.lang.Closure
import org.jetbrains.intellij.IntelliJPluginExtension
import org.jetbrains.intellij.tasks.PrepareSandboxTask
import com.jetbrains.rd.generator.gradle.RdgenTask
import com.jetbrains.rd.generator.gradle.RdgenParams

apply(from = "../intellijJVersions.gradle")

buildscript {
    apply(from = "../intellijJVersions.gradle")
    val rdGenVersion: groovy.lang.Closure<String> by project.extra
    val rdVersion = rdGenVersion()
    project.extra["rd_version"] = rdGenVersion()

    logger.info("Using rd-gen: $rdVersion")

    repositories {
        maven("https://www.myget.org/F/rd-snapshots/maven/")
        mavenCentral()
    }

    dependencies {
        classpath("com.jetbrains.rd:rd-gen:$rdVersion")
    }
}

fun Project.intellij(): IntelliJPluginExtension = extensions["intellij"] as IntelliJPluginExtension

project.extra["resharperPluginPath"] = File(projectDir, "ReSharper.AWS")
val resharperBuildPath = File(project.buildDir, "dotnetBuild")
project.extra["resharperBuildPath"] =resharperBuildPath

val buildConfiguration = project.extra.properties["BuildConfiguration"] ?: "Debug" // TODO: Do we ever want to make a release build?

apply(plugin = "org.jetbrains.intellij")
apply(plugin = "com.jetbrains.rdgen")

apply(from= "protocol.gradle.kts")
apply(from= "backend.gradle.kts")

val ideSdkVersion: Closure<String> by ext
val idePlugins: Closure<ArrayList<String>> by ext

dependencies {
    compile(project(path=":jetbrains-core"))
    testImplementation(project(path=":jetbrains-core", configuration="testArtifacts"))
}

val riderGeneratedSources = "$buildDir/generated-src"
project.extra["riderGeneratedSources"] = riderGeneratedSources

sourceSets {
    main.get().java.srcDir(riderGeneratedSources)
}

extensions.configure<IntelliJPluginExtension>("intellij") {
    val parentIntellijTask = project(":jetbrains-core").extensions["intellij"] as IntelliJPluginExtension
    version = ideSdkVersion("RD")
    pluginName = parentIntellijTask.pluginName
    updateSinceUntilBuild = parentIntellijTask.updateSinceUntilBuild

    // Workaround for https://youtrack.jetbrains.com/issue/IDEA-179607
    val extraPlugins = listOf("rider-plugins-appender")
    setPlugins(*(idePlugins("RD") + extraPlugins).toTypedArray())

    // Disable downloading source to avoid issues related to Rider SDK naming that is missed in Idea
    // snapshots repository. The task is failed because if is unable to find related IC sources.
    downloadSources = false
    instrumentCode = false
}

val resharperParts = listOf(
    "AWS.Daemon",
    "AWS.Localization",
    "AWS.Project",
    "AWS.Psi",
    "AWS.Settings"
)

// Tasks:
//
// `buildPlugin` depends on `prepareSandbox` task and then zips up the sandbox dir and puts the file in rider/build/distributions
// `runIde` depends on `prepareSandbox` task and then executes IJ inside the sandbox dir
// `prepareSandbox` depends on the standard Java `jar` and then copies everything into the sandbox dir

tasks.withType(PrepareSandboxTask::class.java).configureEach {
    val intellij = project.extensions["intellij"] as IntelliJPluginExtension
    dependsOn(tasks["buildReSharperPlugin"])

    val files = resharperParts.map {"$resharperBuildPath/bin/$it/$buildConfiguration/${it}.dll"} +
        resharperParts.map {"$resharperBuildPath/bin/$it/$buildConfiguration/${it}.pdb"}
    from(files) {
        into("${intellij.pluginName}/dotnet")
    }
}

tasks.compileKotlin {
    dependsOn(tasks["generateModels"])
}

tasks.test {
    systemProperty("log.dir", "${project.intellij().sandboxDirectory}-test/logs")
    useTestNG()
    environment("LOCAL_ENV_RUN", true)
    maxHeapSize = "1024m"
}

tasks.integrationTest {
    useTestNG()
    environment("LOCAL_ENV_RUN", true)
}

tasks.jar {
    archiveBaseName.set("aws-intellij-toolkit-rider")
}
