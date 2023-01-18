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
fun AndroidTemplateEditor(
    currentTemplate: TemplateProject,
    readOnly: Boolean,
    onRequestFolderSelector: () -> Unit,
    onFieldModify: (String, String) -> Unit,
){
    val (projectNameFocus, packageNameFocus, destPathFocus) = remember { FocusRequester.createRefs() }

    Column {
        OutlinedTextField(
            value = currentTemplate.androidSettings?.projectName ?: "",
            label = { Text(Locale.current.stringResource("project_name")) },
            modifier = Modifier.width(400.dp).focusRequester(projectNameFocus),
            singleLine = true,
            readOnly = readOnly,
            onValueChange = { pName -> onFieldModify("ProjectName", pName) }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            OutlinedTextField(
                value = currentTemplate.androidSettings?.originalPackageName ?: "",
                label = { Text(Locale.current.stringResource("template_package_name")) },
                modifier = Modifier.width(400.dp),
                singleLine = true,
                readOnly = true,
                onValueChange = { }
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = currentTemplate.androidSettings?.packageName ?: "",
                label = { Text(Locale.current.stringResource("project_base_package_name")) },
                modifier = Modifier.width(400.dp).focusRequester(packageNameFocus),
                singleLine = true,
                readOnly = readOnly,
                onValueChange = { packName -> onFieldModify("PackageName", packName) }
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = currentTemplate.folderDestination,
            label = { Text(Locale.current.stringResource("project_destination_folder")) },
            modifier = Modifier.width(808.dp).focusRequester(destPathFocus),
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