package io.github.afalabarce.projectcreator.composables

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.ResourceLoader
import androidx.compose.ui.res.loadXmlImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogState
import androidx.compose.ui.window.WindowPosition
import io.github.afalabarce.projectcreator.entities.TemplateProject
import io.github.afalabarce.projectcreator.enums.ProjectType
import org.xml.sax.InputSource

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ProjectTemplateIcon(template: TemplateProject, modifier: Modifier){
    Icon(
        modifier = modifier,
        imageVector = when (template.projectType){
            ProjectType.None -> Icons.Filled.Cancel
            ProjectType.Android -> loadXmlImageVector(InputSource(ResourceLoader.Default.load("mipmap/android_logo.xml")) , Density(240f, 1f))
            ProjectType.AndroidKts -> loadXmlImageVector(InputSource(ResourceLoader.Default.load("mipmap/android_logo_kts.xml")) , Density(240f, 1f))
            ProjectType.IOS -> loadXmlImageVector(InputSource(ResourceLoader.Default.load("mipmap/apple_logo.xml")) , Density(240f, 1f))
            ProjectType.Flutter -> loadXmlImageVector(InputSource(ResourceLoader.Default.load("mipmap/flutter_logo.xml")) , Density(240f, 1f))
            else -> loadXmlImageVector(InputSource(ResourceLoader.Default.load("mipmap/ic_hybrid.xml")) , Density(240f, 1f))
        },
        contentDescription = null,
        tint = when (template.projectType){
            ProjectType.None -> Color.Red
            ProjectType.IOS -> Color.DarkGray
            else -> Color.Unspecified
        },
    )
}

@Composable
fun CustomAlertDialog(
    visible: Boolean,
    iconImage: Painter,
    backgroundColor: Color,
    borderColor: Color,
    title: String,
    text: String,
    textStyle: TextStyle = MaterialTheme.typography.body1,
    size: DpSize = DpSize(500.dp, 200.dp),
    buttons: @Composable () -> Unit,
    onClose: () -> Unit,
) {
    Dialog(
        onCloseRequest = { },
        state = DialogState(position = WindowPosition(Alignment.Center), size = size),
        visible = visible,
        resizable = false,
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
                    TopAppBar(backgroundColor = borderColor) {
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
                                    onClick = onClose,
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
                },
                floatingActionButtonPosition = FabPosition.End,
                isFloatingActionButtonDocked = true,
                floatingActionButton = buttons,
                bottomBar = {
                    BottomAppBar(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp, 0.dp, 0.dp, 0.dp)),
                        backgroundColor = borderColor,
                        cutoutShape = RoundedCornerShape(CornerSize(10.dp))
                    ) { }
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 6.dp, end = 6.dp, bottom = 64.dp)
                ) {
                    Text(
                        text = text,
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        style = textStyle
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressDialogTopAppBar(borderColor: Color, iconImage: Painter, title: String, cancellable: Boolean = false, onCancel: () -> Unit = {}){
    TopAppBar(backgroundColor = borderColor) {
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
            if (cancellable) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(
                        modifier = Modifier,
                        onClick = onCancel,
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
}

@Composable
fun ProgressDialog(
    visible: Boolean,
    iconImage: Painter,
    backgroundColor: Color,
    borderColor: Color,
    title: String,
    text: String,
    cancelText: String = "Cancelar",
    cancelColor: Color = Color(0xFFFF6633),
    cancellable: Boolean = false,
    onCancel: () -> Unit = {},
) {
    arrayOf(1).toList()
    val infiniteTransition = rememberInfiniteTransition()
    val animateRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing))
    )

    Dialog(
        onCloseRequest = { },
        state = DialogState(position = WindowPosition(Alignment.Center), size = DpSize(500.dp, 200.dp)),
        visible = visible,
        resizable = false,
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
                    ProgressDialogTopAppBar(
                        borderColor = borderColor,
                        iconImage = iconImage,
                        title = title,
                        cancellable = cancellable,
                        onCancel = onCancel
                    )
                },
                floatingActionButtonPosition = FabPosition.End,
                isFloatingActionButtonDocked = true,
                floatingActionButton = {
                    if (cancellable) {
                        FloatingActionButton(
                            onClick = onCancel,
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(CornerSize(10.dp)),
                            backgroundColor = cancelColor
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Filled.Done, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = cancelText, color = Color.White)
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
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 16.dp, end = 16.dp, bottom = 64.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,

                    ) {
                    Icon(
                        Icons.Filled.Autorenew,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp).rotate(animateRotation),
                        tint = borderColor
                    )
                    Text(
                        text = text,
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        style = MaterialTheme.typography.h6
                    )
                }
            }
        }
    }
}