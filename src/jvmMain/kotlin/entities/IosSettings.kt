package io.github.afalabarce.projectcreator.entities

data class IosSettings(
    val developmentLanguage: String = "es",
    val originalExecutableName: String = "",
    val executableName: String = "",
    val originalBundleIdentifier: String = "",
    val bundleIdentifier: String = "",
    val productName: String = "",
    val originalProductName: String = "",
    val originalDevelopmentTeam: String = "",
    val developmentTeam: String = "",
): ISettings
