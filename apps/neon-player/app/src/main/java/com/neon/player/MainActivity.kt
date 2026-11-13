package com.neon.player

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.neon.player.ui.screens.LibraryScreen
import com.neon.player.ui.screens.NowPlayingScreen
import com.neon.player.ui.screens.PermissionScreen
import com.neon.player.ui.theme.NeonTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            NeonTheme { NeonApp(viewModel) }
        }
    }
}

private val audioPermission =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

private fun requestedPermissions(): Array<String> =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(audioPermission, Manifest.permission.POST_NOTIFICATIONS)
    } else {
        arrayOf(audioPermission)
    }

private fun Context.hasAudioAccess() =
    ContextCompat.checkSelfPermission(this, audioPermission) == PackageManager.PERMISSION_GRANTED

@Composable
fun NeonApp(viewModel: PlayerViewModel) {
    val context = LocalContext.current
    var granted by rememberSaveable { mutableStateOf(context.hasAudioAccess()) }
    var deniedForGood by rememberSaveable { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        granted = result[audioPermission] == true || context.hasAudioAccess()
        val activity = context as? Activity
        deniedForGood = !granted && activity != null && !activity.shouldShowRequestPermissionRationale(audioPermission)
    }

    // Re-check on every return to the app: the user may have changed it in Settings.
    LifecycleResumeEffect(Unit) {
        granted = context.hasAudioAccess()
        if (granted) viewModel.refreshLibrary()
        onPauseOrDispose { }
    }
    LaunchedEffect(granted) {
        if (granted) viewModel.refreshLibrary()
    }

    if (!granted) {
        PermissionScreen(
            openSettingsInstead = deniedForGood,
            onRequest = { launcher.launch(requestedPermissions()) },
            onOpenSettings = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                )
            },
        )
        return
    }

    val library by viewModel.library.collectAsStateWithLifecycle()
    val tracks by viewModel.visibleTracks.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val playback by viewModel.playback.collectAsStateWithLifecycle()
    var playerOpen by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = playerOpen) { playerOpen = false }

    Box(Modifier.fillMaxSize()) {
        LibraryScreen(
            library = library,
            tracks = tracks,
            query = query,
            onQueryChange = viewModel::setQuery,
            playback = playback,
            onPlay = {
                viewModel.play(it)
                playerOpen = true
            },
            onShuffleAll = {
                viewModel.shuffleAll()
                playerOpen = true
            },
            onTogglePlay = viewModel::togglePlay,
            onNext = viewModel::next,
            onOpenPlayer = { playerOpen = true },
        )
        AnimatedVisibility(
            visible = playerOpen && playback.current != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
        ) {
            NowPlayingScreen(
                state = playback,
                onClose = { playerOpen = false },
                onTogglePlay = viewModel::togglePlay,
                onNext = viewModel::next,
                onPrevious = viewModel::previous,
                onSeek = viewModel::seekTo,
                onToggleShuffle = viewModel::toggleShuffle,
                onCycleRepeat = viewModel::cycleRepeat,
            )
        }
    }
}
