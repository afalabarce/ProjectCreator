package io.github.afalabarce.projectcreator.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.afalabarce.projectcreator.composables.editors.AndroidTemplateEditor
import io.github.afalabarce.projectcreator.composables.editors.FlutterTemplateEditor
import io.github.afalabarce.projectcreator.composables.editors.IosTemplateEditor
import io.github.afalabarce.projectcreator.composables.editors.NoneTemplateEditor
import io.github.afalabarce.projectcreator.composables.external.FileChooserDialog
import io.github.afalabarce.projectcreator.entities.TemplateProject
import io.github.afalabarce.projectcreator.entities.TemplateProjectUiState
import io.github.afalabarce.projectcreator.enums.ProjectType
import io.github.afalabarce.projectcreator.ui.theme.*
import io.github.afalabarce.projectcreator.utilities.stringResource
import io.github.afalabarce.projectcreator.viewModels.AppViewModel
import kotlinx.coroutines.Dispatchers

@Composable
private fun TemplateItem(
    template: TemplateProject,
    onClick: (TemplateProject) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp).clickable { onClick(template) },
        backgroundColor = lightGrey,
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ){
            ProjectTemplateIcon(
                template = template,
                modifier = Modifier.size(48.dp).padding(horizontal = 6.dp),
            )

            Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                Text(
                    text = template.title,
                    modifier = Modifier.fillMaxWidth(),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start,
                    fontSize = 16.sp
                )
                Text(
                    text = template.path,
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    textAlign = TextAlign.Start,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun ProjectTemplates(
    templates: List<TemplateProject>,
    onClickTemplate: (TemplateProject) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxHeight().width(400.dp).padding(8.dp),
        backgroundColor = backGrey,
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            Text(
                text = Locale.current.stringResource("project_templates"),
                modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            )
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(templates) { template ->
                    TemplateItem(template) { clickedTemplate -> onClickTemplate(clickedTemplate) }
                }
            }
        }
    }
}

@Composable
private fun MainScaffold(
    acceptIsActive: Boolean,
    onAcceptClick: () -> Unit,
    onCancelClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxSize().padding(vertical = 8.dp).padding(end = 8.dp),
        elevation = 4.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            backgroundColor = backGrey,
            floatingActionButton = {
                Row(modifier = Modifier.padding(end = 4.dp)) {
                    FloatingActionButton(
                        onClick = { if (acceptIsActive) onAcceptClick() },
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(CornerSize(10.dp)),
                        backgroundColor = if (acceptIsActive) lightBlueTaRSyS else darkGrey
                    ) {
                        Icon(Icons.Filled.Done, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = onCancelClick,
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(CornerSize(10.dp)),
                        backgroundColor = orangeColor
                    ) {
                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White)
                    }
                }
            },
            isFloatingActionButtonDocked = true,
            floatingActionButtonPosition = FabPosition.End,
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp, 0.dp, 0.dp, 0.dp)),
                    backgroundColor = blueTaRSyS,
                    cutoutShape = RoundedCornerShape(CornerSize(10.dp))
                ) { }
            },
            content = content
        )
    }
}

@Composable
private fun MainTemplateEditor(
    currentTemplate: TemplateProject?,
    readOnly: Boolean,
    onRequestFolderSelector: () -> Unit,
    onFieldModify: (String, String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        if (currentTemplate != null)
            ProjectTemplateIcon(
                template = currentTemplate,
                modifier = Modifier.size(64.dp).padding(horizontal = 6.dp),
            )
        else{
            Spacer(modifier = Modifier.size(64.dp))
        }

        Spacer(modifier = Modifier.size(6.dp))


        when (currentTemplate?.projectType ?: ProjectType.None){
            ProjectType.Android, ProjectType.AndroidKts -> AndroidTemplateEditor(currentTemplate!!, readOnly, onRequestFolderSelector, onFieldModify)
            ProjectType.IOS -> IosTemplateEditor(currentTemplate!!, readOnly, onRequestFolderSelector, onFieldModify)
            ProjectType.Flutter -> FlutterTemplateEditor(currentTemplate!!, readOnly, onRequestFolderSelector, onFieldModify)
            ProjectType.HybridAngular -> { }
            ProjectType.HybridVue -> { }
            ProjectType.HybridReact -> { }
            ProjectType.HybridJsVanilla -> { }
            else -> NoneTemplateEditor()
        }
    }
}

@Composable
fun MainScreen(viewModel: AppViewModel) {
    val uiState by viewModel.appUiState.collectAsState(initial = TemplateProjectUiState(), Dispatchers.IO)

    var showFileSelectorDialog by remember { mutableStateOf(false) }
    var showMakeProjectConfirmation by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        ProjectTemplates(uiState.templates) { clickedTemplate -> viewModel.loadTemplate(clickedTemplate) }

        MainScaffold(
            acceptIsActive = uiState.currentTemplate != null,
            onAcceptClick = { showMakeProjectConfirmation = true },
            onCancelClick = { viewModel.loadTemplate(null) }
        ) {
            MainTemplateEditor(
                currentTemplate = uiState.currentTemplate,
                readOnly = uiState.currentTemplate == null,
                onRequestFolderSelector = { showFileSelectorDialog = true },
            ) { field, value ->
                viewModel.updateTemplate(uiState.currentTemplate, field, value)
            }
        }

        //region UI Dialogs section

        FileChooserDialog(
            visible = showFileSelectorDialog,
            onlyDirectories = true,
            borderColor = blueTaRSyS,
            iconImage = painterResource("mipmap/ic_launcher.png"),
            title = Locale.current.stringResource("folder_project_selector_title"),
            backgroundColor = lightGrey,
            dotDotColor = orangeColor,
            acceptTitle = Locale.current.stringResource("ok"),
            cancelTitle = Locale.current.stringResource("cancel"),
            acceptButtonColor = blueTaRSyS,
            cancelButtonColor = orangeColor
        ) { selectedFile, _ ->
            showFileSelectorDialog = false
            viewModel.updateTemplate(uiState.currentTemplate, "FolderDestination", selectedFile?.absolutePath ?: "")
        }

        CustomAlertDialog(
            visible = showMakeProjectConfirmation,
            title = Locale.current.stringResource("attention"),
            borderColor = blueTaRSyS,
            backgroundColor = lightGrey,
            iconImage = painterResource("mipmap/ic_launcher.png"),
            text = Locale.current.stringResource("confirm_project_creation"),
            buttons = {
                Row(modifier = Modifier.padding(end = 4.dp)) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.makeNewProject(uiState.currentTemplate!!)
                            showMakeProjectConfirmation = false
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(CornerSize(10.dp)),
                        backgroundColor = blueTaRSyS
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Done, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = Locale.current.stringResource("yes"), color = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    FloatingActionButton(
                        onClick = {
                            showMakeProjectConfirmation = false
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(CornerSize(10.dp)),
                        backgroundColor = orangeColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Done, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = Locale.current.stringResource("no"), color = Color.White)
                        }
                    }
                }
            }

        ) {
            showMakeProjectConfirmation = false
        }

        ProgressDialog(
            visible = uiState.isWorking,
            text = uiState.workingMessage,
            iconImage = painterResource("mipmap/ic_launcher.png"),
            title = Locale.current.stringResource("please_wait"),
            borderColor = blueTaRSyS,
            backgroundColor = lightGrey,
        )

        CustomAlertDialog(
            visible = uiState.currentTemplate?.withError ?: false,
            title = Locale.current.stringResource("attention"),
            borderColor = blueTaRSyS,
            backgroundColor = lightGrey,
            size = DpSize(600.dp, 300.dp),
            iconImage = painterResource("mipmap/ic_launcher.png"),
            text = uiState.currentTemplate?.errorMessage ?: "",
            buttons = {
                Row(modifier = Modifier.padding(end = 4.dp)) {
                    FloatingActionButton(
                        onClick = {
                            viewModel.updateTemplate(uiState.currentTemplate, "NoError", "")
                        },
                        modifier = Modifier.height(48.dp),
                        shape = RoundedCornerShape(CornerSize(10.dp)),
                        backgroundColor = orangeColor
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Filled.Done, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = Locale.current.stringResource("ok"), color = Color.White)
                        }
                    }
                }
            }
        ) {
            viewModel.updateTemplate(uiState.currentTemplate, "NoError", "")
        }
        //endregion
    }

    LaunchedEffect("loadTemplates") {
        viewModel.loadTemplates()
    }
}