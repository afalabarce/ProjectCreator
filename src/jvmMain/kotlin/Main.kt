package io.github.afalabarce.projectcreator

import androidx.compose.material.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import io.github.afalabarce.projectcreator.composables.MainScreen
import io.github.afalabarce.projectcreator.utilities.stringResource
import io.github.afalabarce.projectcreator.viewModels.AppViewModel

fun main() = application {
    val viewModel = AppViewModel()
    Window(
        state = WindowState(WindowPlacement.Maximized),
        icon = painterResource("mipmap/ic_launcher.png"),
        title = Locale.current.stringResource("app_name"),
        onCloseRequest = ::exitApplication
    ) {
        MaterialTheme {
            MainScreen(viewModel)
        }
    }
}
