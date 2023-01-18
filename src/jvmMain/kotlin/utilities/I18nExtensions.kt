package io.github.afalabarce.projectcreator.utilities

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.res.ResourceLoader
import androidx.compose.ui.text.intl.Locale
import java.util.*


private class I18nExtensions {
    companion object {
        var localeProperties: Properties? = null
        var commonLocaleProperties: Properties? = null
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun String.existsAsResourceFile(): Boolean {
    try {
        val resource = ResourceLoader.Default.load(this)

        return resource.available() != 0
    } catch (_: Exception) {

    }

    return false
}

@OptIn(ExperimentalComposeUiApi::class)
private fun String.getProperties(): Properties? {
    try {
        if (this.existsAsResourceFile()) {
            val rLoader = ResourceLoader.Default
            return Properties().also { p ->
                p.load(rLoader.load(this).bufferedReader(Charsets.UTF_8))
            }
        }
    } catch (_: Exception) {

    }

    return null
}

fun Locale.stringResource(resourceKey: String, vararg parameters: Any?): String {
    val localeName = "values/strings.${this.language}.locale"
    val commonLocaleName = "values/strings.locale"

    if (I18nExtensions.localeProperties == null)
        I18nExtensions.localeProperties = localeName.getProperties()

    if (I18nExtensions.commonLocaleProperties == null)
        I18nExtensions.commonLocaleProperties = commonLocaleName.getProperties()

    if (I18nExtensions.localeProperties != null || I18nExtensions.commonLocaleProperties != null)
        return (I18nExtensions.localeProperties?.getProperty(
            resourceKey,
            I18nExtensions.commonLocaleProperties?.getProperty(resourceKey, resourceKey)
        ) ?: I18nExtensions.commonLocaleProperties?.getProperty(resourceKey, resourceKey)
        ?: resourceKey).format(*parameters)

    return resourceKey
}