package io.github.afalabarce.projectcreator.composables.editors

import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import io.github.afalabarce.projectcreator.entities.TemplateProject
import io.github.afalabarce.projectcreator.utilities.stringResource
import java.awt.Cursor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun IosTemplateEditor(
    currentTemplate: TemplateProject,
    readOnly: Boolean,
    onRequestFolderSelector: () -> Unit,
    onFieldModify: (String, String) -> Unit,
){
    val (projectNameFocus, packageNameFocus, destPathFocus) = remember { FocusRequester.createRefs() }
    Column {
        OutlinedTextField(
            value = currentTemplate.iosSettings?.productName ?: "",
            label = { Text(Locale.current.stringResource("ios_product_name")) },
            modifier = Modifier.width(400.dp).focusRequester(projectNameFocus),
            singleLine = true,
            readOnly = readOnly,
            onValueChange = { pName -> onFieldModify("IosProductName", pName) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            OutlinedTextField(
                value = currentTemplate.iosSettings?.developmentLanguage ?: "",
                label = { Text(Locale.current.stringResource("ios_development_language_region")) },
                modifier = Modifier.width(170.dp),
                singleLine = true,
                readOnly = readOnly,
                onValueChange = { devLang -> onFieldModify("IosDevelopmentLanguage", devLang) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = currentTemplate.iosSettings?.developmentTeam ?: "",
                label = { Text(Locale.current.stringResource("ios_development_team")) },
                modifier = Modifier.width(200.dp),
                singleLine = true,
                readOnly = readOnly,
                onValueChange = { devLang -> onFieldModify("IosDevelopmentTeam", devLang) }
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = currentTemplate.iosSettings?.executableName ?: "",
                label = { Text(Locale.current.stringResource("ios_executable_name")) },
                modifier = Modifier.width(550.dp).focusRequester(packageNameFocus),
                singleLine = true,
                readOnly = readOnly,
                onValueChange = { exeName -> onFieldModify("IosExecutableName", exeName) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            OutlinedTextField(
                value = currentTemplate.iosSettings?.originalBundleIdentifier ?: "",
                label = { Text(Locale.current.stringResource("ios_original_bundle_identifier")) },
                modifier = Modifier.width(400.dp),
                singleLine = true,
                readOnly = true,
                onValueChange = { }
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = currentTemplate.iosSettings?.bundleIdentifier ?: "",
                label = { Text(Locale.current.stringResource("ios_bundle_identifier")) },
                modifier = Modifier.width(528.dp).focusRequester(packageNameFocus),
                singleLine = true,
                readOnly = readOnly,
                onValueChange = { bundleId -> onFieldModify("IosBundleIdentifier", bundleId) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = currentTemplate.folderDestination,
            label = { Text(Locale.current.stringResource("project_destination_folder")) },
            modifier = Modifier.width(936.dp).focusRequester(destPathFocus),
            singleLine = true,
            readOnly = readOnly,
            trailingIcon = {
                IconButton(onClick = {
                    if (!readOnly)
                        onRequestFolderSelector()
                }) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.pointerHoverIcon(PointerIcon(Cursor(Cursor.DEFAULT_CURSOR)), false)
                    )
                }
            },
            onValueChange = { folderPath -> onFieldModify("FolderDestination", folderPath) }
        )
    }
}