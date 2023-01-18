@file:OptIn(ExperimentalFoundationApi::class)

package io.github.afalabarce.projectcreator.composables.external

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.AbsoluteCutCornerShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.WindowPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.io.File

private data class FileChooserUiState(
    val currentPath: File,
    val firstLoad: Boolean = true,
    val fileSelection: Boolean,
    val selectedFileOrDirectory: File?,
    val windowPosition: WindowPosition = WindowPosition(Alignment.Center),
    val windowMoving: Boolean = false,
    val firstPosition: Offset? = null,
)

private class FileChooserViewModel : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val _uiState by lazy {
        MutableStateFlow(
            FileChooserUiState(
                currentPath = File(System.getProperty("user.home")),
                firstLoad = true,
                fileSelection = true,
                selectedFileOrDirectory = null
            )
        )
    }
    val uiState: StateFlow<FileChooserUiState>
        get() = this._uiState

    fun close() {
        this._uiState.update { old ->
            old.copy(
                firstPosition = null,
                currentPath = File(System.getProperty("user.home")),
                firstLoad = true
            )
        }
    }

    fun setFirstPosition(offsetPosition: Offset?) {
        this._uiState.update { old -> old.copy(firstPosition = offsetPosition) }
    }

    fun setSelectionType(onlyDirectories: Boolean) {
        this._uiState.update { old -> old.copy(fileSelection = !onlyDirectories) }
    }

    fun windowIsMoving(isMoving: Boolean) {
        this._uiState.update { old ->
            old.copy(
                windowMoving = isMoving,
                firstPosition = if (!isMoving) null else old.firstPosition
            )
        }
    }

    fun setNewPosition(offsetPosition: Offset) {
        this._uiState.update { old ->
            old.copy(
                windowPosition = WindowPosition(
                    Dp(offsetPosition.x),
                    Dp(offsetPosition.y)
                ), firstPosition = offsetPosition
            )
        }
    }

    fun setNewFolder(folder: File) {
        if (folder.name == "File.listRoots") {
            this._uiState.update { old -> old.copy(currentPath = folder, firstLoad = false) }
        } else if (folder.isDirectory) {
            this._uiState.update { old -> old.copy(currentPath = folder, firstLoad = false) }
        }
    }

    fun selectFileOrDirectory(selected: File?) {
        this._uiState.update { old -> old.copy(selectedFileOrDirectory = selected) }
    }

    fun setFirstLoad(value: Boolean) {
        this._uiState.update { old -> old.copy(firstLoad = value) }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun FileChooserDialog(
    visible: Boolean,
    onlyDirectories: Boolean,
    baseDirectory: File? = null,
    backgroundColor: Color,
    borderColor: Color,
    iconImage: Painter,
    title: String,
    acceptTitle: String = "Aceptar",
    acceptButtonColor: Color,
    cancelTitle: String = "Cancelar",
    cancelButtonColor: Color,
    dotDotColor: Color,
    onFileChoosen: (File?, Boolean) -> Unit,
) {
    val viewModel = remember { FileChooserViewModel() }
    viewModel.setSelectionType(onlyDirectories)
    val uiState by viewModel.uiState.collectAsState()
    var dialogPosition: WindowPosition by remember { mutableStateOf(WindowPosition(Alignment.Center)) }
    var dialogOffset: Offset? by remember { mutableStateOf(null) }

    if (baseDirectory != null && uiState.firstLoad)
        viewModel.setNewFolder(baseDirectory)

    if (uiState.firstLoad) {
        dialogPosition = WindowPosition(Alignment.Center)
        viewModel.setFirstLoad(false)
    }

    Dialog(
        onCloseRequest = { },
        transparent = true,
        state = DialogState(position = dialogPosition, size = DpSize(800.dp, 600.dp)),
        visible = visible,
        resizable = true,
        undecorated = true,
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(CornerSize(10.dp)),
            elevation = 6.dp,
            border = BorderStroke(width = 2.dp, borderColor),
            backgroundColor = backgroundColor,
        ) {

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    WindowDraggableArea {
                        TopAppBar(
                            backgroundColor = borderColor,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(6.dp),
                                horizontalArrangement = Arrangement.Start,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Image(iconImage, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    modifier = Modifier.fillMaxWidth(0.9f),
                                    color = Color.White,
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                    IconButton(
                                        modifier = Modifier,
                                        onClick = {
                                            viewModel.close()
                                            onFileChoosen(null, false)
                                        },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Clear,
                                            contentDescription = null,
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                floatingActionButtonPosition = FabPosition.End,
                isFloatingActionButtonDocked = true,
                floatingActionButton = {
                    Row(modifier = Modifier.padding(end = 4.dp)) {
                        FloatingActionButton(
                            onClick = {
                                if (onlyDirectories) {
                                    viewModel.close()
                                    onFileChoosen(uiState.currentPath, true)
                                } else {
                                    if (uiState.selectedFileOrDirectory != null) {
                                        viewModel.close()
                                        onFileChoosen(uiState.selectedFileOrDirectory, true)
                                    }
                                }
                            },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(CornerSize(10.dp)),
                            backgroundColor = acceptButtonColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.Done, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = acceptTitle, color = Color.White)
                            }

                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FloatingActionButton(
                            onClick = {
                                viewModel.close()
                                onFileChoosen(null, false)
                            },
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(CornerSize(10.dp)),
                            backgroundColor = cancelButtonColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = cancelTitle, color = Color.White)
                            }

                        }
                    }
                },
                bottomBar = {
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp, 0.dp, 0.dp, 0.dp)),
                        backgroundColor = borderColor,
                        cutoutShape = RoundedCornerShape(CornerSize(10.dp))
                    ) { }
                }

            ) {
                Column(modifier = Modifier.fillMaxSize().padding(start = 6.dp, end = 6.dp, bottom = 64.dp)) {
                    PathContainer(
                        path = uiState.currentPath,
                        color = borderColor,
                        dotDotColor = dotDotColor
                    ) { newFolder ->
                        viewModel.setNewFolder(newFolder)
                    }
                    val currentPath = uiState.currentPath
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            if (currentPath.name.contains("File.listRoots")) File.listRoots() else currentPath.listFiles()
                                ?.filter { x -> (onlyDirectories && x.isDirectory) || !onlyDirectories }?.toTypedArray()
                                ?: arrayOf()
                        ) { file ->
                            FileItem(file, file == uiState.selectedFileOrDirectory) {
                                if (file.isDirectory) {
                                    viewModel.setNewFolder(file)
                                } else {
                                    viewModel.selectFileOrDirectory(file)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FileItem(file: File, isSelected: Boolean = false, onClick: (File) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable { onClick(file) }.clip(MaterialTheme.shapes.small)
            .background(if (isSelected) Color(0xFF989898) else Color.Transparent),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = if (file.isDirectory) Icons.Filled.Folder else Icons.Filled.FilePresent,
            contentDescription = null,
            tint = if (file.isDirectory) Color(0xFFFFBF00) else Color.Gray
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = if (file.name.isNullOrEmpty()) file.absolutePath else file.name,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PathContainer(path: File, dotDotColor: Color, color: Color, clickedPath: (File) -> Unit) {
    val dotDot = ".."
    val parentFolder: String = try {
        if (File(path.parent).name == "") File(path.parent).absolutePath else File(path.parent).name
    } catch (ex: Exception) {
        path.absolutePath
    }
    val currentPath: String = if (path.name.contains("File.listRoots")) "" else path.name

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = {
                clickedPath(File(System.getProperty("user.home")))
            }
        ) {
            Icon(Icons.Filled.Home, contentDescription = null, tint = color)
        }
        Text(
            text = "Ruta actual:",
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.Bold
        )

        ContentTag(
            isLeft = false,
            color = dotDotColor,
            text = dotDot
        ) {
            if (currentPath != "")
                clickedPath(File(path.parent))
            else if (System.getProperty("os.name").contains("windows", true)) {
                clickedPath(File("File.listRoots"))
            }
        }
        ContentTag(
            isLeft = false,
            color = color,
            text = if (path.name.contains("File.listRoots")) "Unidades" else parentFolder
        ) {
            if (currentPath != "")
                clickedPath(File(path.parent))
        }
        if (currentPath != "")
            ContentTag(
                isLeft = false,
                color = color,
                text = currentPath
            ) {

            }
    }
}

@Composable
private fun ContentTag(isLeft: Boolean, color: Color, text: String, fontSize: TextUnit = 14.sp, onClick: () -> Unit) {
    Surface(
        shape = if (isLeft)
            AbsoluteCutCornerShape(topLeftPercent = 50, bottomLeftPercent = 50)
        else
            AbsoluteCutCornerShape(topRightPercent = 50, bottomRightPercent = 50),
        modifier = Modifier.padding(8.dp).clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .background(color)
                .padding(
                    start = if (isLeft) fontSize.value.dp * 1.1f else fontSize.value.dp / 2,
                    end = if (isLeft) fontSize.value.dp / 2 else fontSize.value.dp * 1.1f,
                    top = 4.dp,
                    bottom = 4.dp,
                )
        ) {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.h6,
                fontSize = fontSize,
                fontWeight = FontWeight.W300,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }
    }
}