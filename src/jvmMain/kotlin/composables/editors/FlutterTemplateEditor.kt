package io.github.afalabarce.projectcreator.composables.editors

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import io.github.afalabarce.projectcreator.entities.TemplateProject

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FlutterTemplateEditor(
    currentTemplate: TemplateProject,
    readOnly: Boolean,
    onRequestFolderSelector: () -> Unit,
    onFieldModify: (String, String) -> Unit,
){

}