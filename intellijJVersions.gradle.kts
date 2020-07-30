package software.aws.toolkits.gradle

enum class Product(val prefix: String) {
    IntellijCommunity("IC"),
    IntellijUltimate("IU"),
    Rider("RD")
}

data class IdeProfile(
    val sdkVersion: String,
    val plugins: List<String>,
    val rdGenVersion: String? = null,
    val nugetVersion: String? = null
)

data class Profile(
    val sinceVersion: String,
    val untilVersion: String,
    val products: Map<Product, IdeProfile>
)

val ideProfiles = mapOf(
    "2019.3" to Profile(
        sinceVersion = "193",
        untilVersion = "193.*",
        products = mapOf(
            Product.IntellijCommunity to IdeProfile(
                sdkVersion = "IC-2019.3",
                plugins = listOf(
                    "org.jetbrains.plugins.terminal",
                    "org.jetbrains.plugins.yaml",
                    "PythonCore:193.5233.139",
                    "java",
                    "com.intellij.gradle",
                    "org.jetbrains.idea.maven",
                    "Docker:193.5233.140"
                )
            ),
            Product.IntellijUltimate to IdeProfile(
                sdkVersion = "IU-2019.3",
                plugins = listOf(
                    "org.jetbrains.plugins.terminal",
                    "Pythonid:193.5233.109",
                    "org.jetbrains.plugins.yaml",
                    "JavaScript",
                    "JavaScriptDebugger"
                )
            ),
            Product.Rider to IdeProfile(
                sdkVersion = "RD-2019.3.4",
                rdGenVersion = "0.193.146",
                nugetVersion = "2019.3.4",
                plugins = listOf(
                    "org.jetbrains.plugins.yaml"
                )
            )
        )/*
        "2020.1": [
            "sinceVersion": "201",
            "untilVersion": "201.*",
            "products"    : [
                "IC": [
                    sdkVersion: "IC-2020.1",
                    plugins   : [
                        "org.jetbrains.plugins.terminal",
                        "org.jetbrains.plugins.yaml",
                        "PythonCore:201.6668.31",
                        "java",
                        "com.intellij.gradle",
                        "org.jetbrains.idea.maven",
                        "Docker:201.6668.30"
                    ]
                ],
                "IU": [
                    sdkVersion: "IU-2020.1",
                    plugins   : [
                        "org.jetbrains.plugins.terminal",
                        "Pythonid:201.6668.31",
                        "org.jetbrains.plugins.yaml",
                        "JavaScript",
                        "JavaScriptDebugger",
                        "com.intellij.database",
                    ]
                ],
                "RD": [
                    sdkVersion  : "RD-2020.1.0",
                    rdGenVersion: "0.201.69",
                    nugetVersion: "2020.1.0",
                    plugins     : [
                        "org.jetbrains.plugins.yaml"
                    ]
                ]
            ]
        ],
        "2020.2": [
            "sinceVersion": "202",
            "untilVersion": "202.*",
            "products"    : [
                "IC": [
                    sdkVersion: "IC-202.6250.13-EAP-SNAPSHOT",
                    plugins   : [
                        "org.jetbrains.plugins.terminal",
                        "org.jetbrains.plugins.yaml",
                        "PythonCore:202.6250.13",
                        "java",
                        "com.intellij.gradle",
                        "org.jetbrains.idea.maven",
                        "Docker:202.6250.6"
                    ]
                ],
                "IU": [
                    sdkVersion: "IU-202.6250.13-EAP-SNAPSHOT",
                    plugins   : [
                        "org.jetbrains.plugins.terminal",
                        "Pythonid:202.6250.13",
                        "org.jetbrains.plugins.yaml",
                        "JavaScript",
                        "JavaScriptDebugger",
                        "com.intellij.database",
                    ]
                ],
                "RD": [
                    sdkVersion  : "RD-2020.2-SNAPSHOT",
                    rdGenVersion: "0.202.113",
                    nugetVersion: "2020.2.0-eap07",
                    plugins     : [
                        "org.jetbrains.plugins.yaml"
                    ]
                ]
            ]
        ]
    ]
    */
    )
)
/*

fun idePlugins(productCode: String) {
    return ideProduct(productCode).plugins
}

fun ideSdkVersion(productCode: String) {
    return ideProduct(productCode).sdkVersion
}

private fun ideProduct( productCode: String) {
    def product = ideProfile ()["products"][productCode]
    if (product == null) {
        throw new IllegalArgumentException ("Unknown IDE product `$productCode` for ${resolveIdeProfileName()}")
    }
    return product
}

private fun ideSinceVersion() {
    def guiVersion = ideProfile ()["sinceVersion"]
    if (guiVersion == null) {
        throw new IllegalArgumentException ("Missing 'sinceVersion' key for ${resolveIdeProfileName()}")
    }
    return guiVersion
}

fun ideUntilVersion() {
    def guiVersion = ideProfile ()["untilVersion"]
    if (guiVersion == null) {
        throw new IllegalArgumentException ("Missing 'untilVersion' key for ${resolveIdeProfileName()}")
    }
    return guiVersion
}

// https://www.myget.org/feed/rd-snapshots/package/maven/com.jetbrains.rd/rd-gen
fun rdGenVersion() {
    def rdGen = ideProduct ("RD").rdGenVersion
    if (rdGen == null) {
        throw new IllegalArgumentException ("Missing 'rdGenVersion' in 'RD' product for ${resolveIdeProfileName()}")
    }
    return rdGen
}

// https://www.nuget.org/packages/JetBrains.Rider.SDK/
fun riderNugetSdkVersion() {
    def rdGen = ideProduct ("RD").nugetVersion
    if (rdGen == null) {
        throw new IllegalArgumentException ("Missing 'nugetVersion' in 'RD' product for ${resolveIdeProfileName()}")
    }
    return rdGen
}

fun ideProfile() {
    def profileName = resolveIdeProfileName ()
    def profile = ideProfiles ()[profileName]
    if (profile == null) {
        throw new IllegalArgumentException ("Unknown ideProfile `$profileName`")
    }

    return profile
}
*/

fun resolveIdeProfileName(): String {
    val ideProfileName: String by project
    return System.getenv().get("ALTERNATIVE_IDE_PROFILE_NAME") ?: ideProfileName
}

fun shortenVersion(ver: String) = ver.trim().substring(2)

extra["ideProfiles"] = ideProfiles
/*extra
    idePlugins = this.&idePlugins
    ideSdkVersion = this.&ideSdkVersion
    ideSinceVersion = this.&ideSinceVersion
    ideUntilVersion = this.&ideUntilVersion
    ideProfile = this.&ideProfile
    rdGenVersion = this.&rdGenVersion
    riderNugetSdkVersion = this.&riderNugetSdkVersion
    resolveIdeProfileName = this.&resolveIdeProfileName
    shortenVersion = this.&shortenVersion

 */
