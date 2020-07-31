// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

import groovy.lang.Closure
import com.jetbrains.rd.generator.gradle.RdgenTask
import com.jetbrains.rd.generator.gradle.RdgenParams

plugins {
    id("java")
    kotlin("jvm")
    id("com.jetbrains.rdgen")
}

val protocolGroup = "protocol"

val resharperPluginPath: File by project.extra
val rdGenVersion: Closure<String> by project.extra

val csDaemonGeneratedOutput = File(resharperPluginPath, "src/AWS.Daemon/Protocol")
val csPsiGeneratedOutput = File(resharperPluginPath, "src/AWS.Psi/Protocol")
val csAwsSettingGeneratedOutput = File(resharperPluginPath, "src/AWS.Settings/Protocol")
val csAwsProjectGeneratedOutput = File(resharperPluginPath, "src/AWS.Project/Protocol")
val riderGeneratedSources = "$buildDir/generated-src/software/aws/toolkits/jetbrains/protocol"

val modelDir = File(projectDir, "protocol/model")
val rdgenDir = File("${project.buildDir}/rdgen/")

rdgenDir.mkdirs()

tasks.register("generateDaemonModel", tasks.getByName("rdgen").javaClass) {
    val daemonModelSource = File(modelDir, "daemon").canonicalPath
    val ktOutput = File(riderGeneratedSources, "DaemonProtocol")

    inputs.property("rdgen", rdGenVersion())
    inputs.dir(daemonModelSource)
    outputs.dirs(ktOutput, csDaemonGeneratedOutput)

    // NOTE: classpath is evaluated lazily, at execution time, because it comes from the unzipped
    // intellij SDK, which is extracted in afterEvaluate
    params {
        verbose = true
        hashFolder = rdgenDir

        logger.info("Configuring rdgen params")
        classpath {
            logger.info("Calculating classpath for rdgen, intellij.ideaDependency is: ${intellij.ideaDependency}")
            val sdkPath = intellij.ideaDependency.classes
            val rdLibDirectory = File(sdkPath, "lib/rd").canonicalFile

            "$rdLibDirectory/rider-model.jar"
        }
        sources daemonModelSource
        packages = "protocol.model.daemon"

        generator {
            language = "kotlin"
            transform = "asis"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            namespace = "com.jetbrains.rider.model"
            directory = "$ktOutput"
        }

        generator {
            language = "csharp"
            transform = "reversed"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            namespace = "JetBrains.Rider.Model"
            directory = "$csDaemonGeneratedOutput"
        }
    }
}

tasks {
val generatePsiModel by creating(RdgenTask::class) {
//tasks.register("generatePsiModel", tasks.getByName("rdgen").javaClass) {
    configure<RdgenParams> {
    val psiModelSource = File(modelDir, "psi").canonicalPath
    val ktOutput = File(riderGeneratedSources, "PsiProtocol")

    inputs.property("rdgen", rdGenVersion())
    inputs.dir(psiModelSource)
    outputs.dirs(ktOutput, csPsiGeneratedOutput)

    // NOTE: classpath is evaluated lazily, at execution time, because it comes from the unzipped
    // intellij SDK, which is extracted in afterEvaluate
    params {
        verbose = true
        hashFolder = rdgenDir

        logger.info("Configuring rdgen params")
        classpath {
            logger.info("Calculating classpath for rdgen, intellij.ideaDependency is: ${intellij.ideaDependency}")
            val sdkPath = intellij.ideaDependency.classes
            val rdLibDirectory = File(sdkPath, "lib/rd").canonicalFile

            "$rdLibDirectory/rider-model.jar"
        }
        sources psiModelSource
        packages = "protocol.model.psi"

        generator {
            language = "kotlin"
            transform = "asis"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            namespace = "com.jetbrains.rider.model"
            directory = "$ktOutput"
        }

        generator {
            language = "csharp"
            transform = "reversed"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            namespace = "JetBrains.Rider.Model"
            directory = "$csPsiGeneratedOutput"
        }
    }
}}}

tasks.register("generateAwsSettingModel", tasks.getByName("rdgen").javaClass) {
    val settingModelSource = File(modelDir, "setting").canonicalPath
    val ktOutput = File(riderGeneratedSources, "AwsSettingsProtocol")

    inputs.property("rdgen", rdGenVersion())
    inputs.dir(settingModelSource)
    outputs.dirs(ktOutput, csAwsSettingGeneratedOutput)

    // NOTE: classpath is evaluated lazily, at execution time, because it comes from the unzipped
    // intellij SDK, which is extracted in afterEvaluate
    params {
        verbose = true
        hashFolder = rdgenDir

        logger.info("Configuring rdgen params")
        classpath {
            logger.info("Calculating classpath for rdgen, intellij.ideaDependency is: ${intellij.ideaDependency}")
            val sdkPath = intellij.ideaDependency.classes
            val rdLibDirectory = File(sdkPath, "lib/rd").canonicalFile

            "$rdLibDirectory/rider-model.jar"
        }
        sources settingModelSource
        packages = "protocol.model.setting"

        generator {
            language = "kotlin"
            transform = "asis"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            namespace = "com.jetbrains.rider.model"
            directory = "$ktOutput"
        }

        generator {
            language = "csharp"
            transform = "reversed"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            namespace = "JetBrains.Rider.Model"
            directory = "$csAwsSettingGeneratedOutput"
        }
    }
}

tasks.register("generateAwsProjectModel", tasks.getByName("rdgen").javaClass) {
    val projectModelSource = File(modelDir, "project").canonicalPath
    val ktOutput = File(riderGeneratedSources, "AwsProjectProtocol")

    inputs.property("rdgen", rdGenVersion())
    inputs.dir(projectModelSource)
    outputs.dirs(ktOutput, csAwsProjectGeneratedOutput)

    // NOTE: classpath is evaluated lazily, at execution time, because it comes from the unzipped
    // intellij SDK, which is extracted in afterEvaluate
    params {
        verbose = true
        hashFolder = rdgenDir

        logger.info("Configuring rdgen params")
        classpath {
            logger.info("Calculating classpath for rdgen, intellij.ideaDependency is: ${intellij.ideaDependency}")
            val sdkPath = intellij.ideaDependency.classes
            val rdLibDirectory = File(sdkPath, "lib/rd").canonicalFile

            "$rdLibDirectory/rider-model.jar"
        }
        sources projectModelSource
        packages = "protocol.model.project"

        generator {
            language = "kotlin"
            transform = "asis"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            namespace = "com.jetbrains.rider.model"
            directory = "$ktOutput"
        }

        generator {
            language = "csharp"
            transform = "reversed"
            root = "com.jetbrains.rider.model.nova.ide.IdeRoot"
            namespace = "JetBrains.Rider.Model"
            directory = "$csAwsProjectGeneratedOutput"
        }
    }
}

task generateModels {
    group = protocolGroup
    description = "Generates protocol models"

    dependsOn(generateDaemonModel, generatePsiModel, generateAwsSettingModel, generateAwsProjectModel)
}

task cleanGenerateModels {
    group = protocolGroup
    description = "Clean up generated protocol models"

    dependsOn(cleanGenerateDaemonModel, cleanGeneratePsiModel, cleanGenerateAwsSettingModel, cleanGenerateAwsProjectModel)
}
project.tasks.clean.dependsOn(cleanGenerateModels)
