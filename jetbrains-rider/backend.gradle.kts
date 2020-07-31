// Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: Apache-2.0

val backendGroup = "backend"

task prepareBuildProps {
    def riderSdkVersionPropsPath = new File(resharperPluginPath, "RiderSdkPackageVersion.props")
    group = backendGroup

    inputs.property("riderNugetSdkVersion", riderNugetSdkVersion())
    outputs.file(riderSdkVersionPropsPath)

    doLast {
        def riderSdkVersion = riderNugetSdkVersion()
        def configText = """<Project>
  <PropertyGroup>
    <RiderSDKVersion>[$riderSdkVersion]</RiderSDKVersion>
  </PropertyGroup>
</Project>
"""
        riderSdkVersionPropsPath.write(configText)
    }
}

task prepareNuGetConfig {
    group = backendGroup

    def nugetConfigPath = new File(projectDir, "NuGet.Config")

    inputs.property("rdVersion", ideSdkVersion("RD"))
    outputs.file(nugetConfigPath)

    doLast {
        def nugetPath = getNugetPackagesPath()
        def configText = """<?xml version="1.0" encoding="utf-8"?>
<configuration>
  <packageSources>
    <add key="resharper-sdk" value="${nugetPath}" />
  </packageSources>
</configuration>
"""
        nugetConfigPath.write(configText)
    }
}

task buildReSharperPlugin {
    group = backendGroup
    description = 'Builds the full ReSharper backend plugin solution'
    dependsOn generateModels, prepareBuildProps, prepareNuGetConfig

    inputs.dir(resharperPluginPath)
    outputs.dir(resharperBuildPath)

    outputs.files({
        fileTree(file("${resharperPluginPath.absolutePath}/src")).matching {
            include "**/bin/Debug/**/AWS*.dll"
            include "**/bin/Debug/**/AWS*.pdb"
        }.collect()
    })

    doLast {
        def arguments = ["build"]
        arguments << "${resharperPluginPath.canonicalPath}/ReSharper.AWS.sln"
        arguments << "/p:DefineConstants=\"PROFILE_${resolveIdeProfileName().replace(".", "_")}\""
        exec {
            executable = "dotnet"
            args = arguments
        }
    }
}

project.tasks.clean.dependsOn(cleanPrepareBuildProps, cleanPrepareNuGetConfig, cleanBuildReSharperPlugin)

private File getNugetPackagesPath() {
    def sdkPath = intellij.ideaDependency.classes
    println("SDK path: $sdkPath")

    // 2019
    def riderSdk = new File(sdkPath, "lib/ReSharperHostSdk")
    // 2020.1
    if (!riderSdk.exists()) {
        riderSdk = new File(sdkPath, "lib/DotNetSdkForRdPlugins")
    }

    println("NuGet packages: $riderSdk")
    if (!riderSdk.isDirectory()) throw new IllegalStateException("${riderSdk} does not exist or not a directory")

    return riderSdk
}