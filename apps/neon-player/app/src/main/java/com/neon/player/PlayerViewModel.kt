package com.neon.player

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.neon.player.data.MusicRepository
import com.neon.player.data.Track
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class RepeatMode { OFF, ALL, ONE }

data class PlaybackState(
    val current: Track? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val shuffle: Boolean = false,
    val repeat: RepeatMode = RepeatMode.OFF,
    val queueIndex: Int = 0,
    val queueSize: Int = 0,
)

data class LibraryState(
    val loading: Boolean = true,
    val tracks: List<Track> = emptyList(),
)

class PlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val repository = MusicRepository(app)
    private var controller: MediaController? = null
    private var ticker: Job? = null

    private val _library = MutableStateFlow(LibraryState())
    val library: StateFlow<LibraryState> = _library.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val visibleTracks: StateFlow<List<Track>> = combine(_library, _query) { lib, q ->
        val trimmed = q.trim()
        lib.tracks.filter { it.matches(trimmed) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _playback = MutableStateFlow(PlaybackState())
    val playback: StateFlow<PlaybackState> = _playback.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = syncFromPlayer()
    }

    init {
        viewModelScope.launch {
            val token = SessionToken(app, ComponentName(app, PlaybackService::class.java))
            val c = MediaController.Builder(app, token).buildAsync().await()
            controller = c
            c.addListener(listener)
            syncFromPlayer()
        }
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            val tracks = runCatching { repository.loadTracks() }.getOrDefault(emptyList())
            _library.value = LibraryState(loading = false, tracks = tracks)
            syncFromPlayer()
        }
    }

    fun setQuery(value: String) {
        _query.value = value
    }

    /** Plays [track] with the currently visible list as the queue. */
    fun play(track: Track) {
        val queue = visibleTracks.value.ifEmpty { listOf(track) }
        startQueue(queue, queue.indexOf(track).coerceAtLeast(0), shuffle = false)
    }

    fun shuffleAll() {
        val queue = visibleTracks.value
        if (queue.isEmpty()) return
        startQueue(queue, queue.indices.random(), shuffle = true)
    }

    private fun startQueue(queue: List<Track>, index: Int, shuffle: Boolean) {
        val c = controller ?: return
        c.setMediaItems(queue.map { it.toMediaItem() }, index, 0L)
        c.shuffleModeEnabled = shuffle
        c.prepare()
        c.play()
    }

    fun togglePlay() {
        val c = controller ?: return
        when {
            c.isPlaying -> c.pause()
            c.mediaItemCount == 0 -> shuffleAll()
            else -> {
                if (c.playbackState == Player.STATE_ENDED) c.seekToDefaultPosition(0)
                c.play()
            }
        }
    }

    fun next() = controller?.seekToNext() ?: Unit

    fun previous() = controller?.seekToPrevious() ?: Unit

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _playback.update { it.copy(positionMs = positionMs) }
    }

    fun toggleShuffle() {
        controller?.let { it.shuffleModeEnabled = !it.shuffleModeEnabled }
    }

    fun cycleRepeat() {
        val c = controller ?: return
        c.repeatMode = when (c.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    private fun syncFromPlayer() {
        val c = controller ?: return
        val id = c.currentMediaItem?.mediaId?.toLongOrNull()
        val track = id?.let { wanted -> _library.value.tracks.firstOrNull { it.id == wanted } }
            ?: c.currentMediaItem?.toFallbackTrack()
        _playback.value = PlaybackState(
            current = track,
            isPlaying = c.isPlaying,
            positionMs = c.currentPosition.coerceAtLeast(0),
            durationMs = c.duration.takeIf { it > 0 } ?: track?.durationMs ?: 0,
            shuffle = c.shuffleModeEnabled,
            repeat = when (c.repeatMode) {
                Player.REPEAT_MODE_ALL -> RepeatMode.ALL
                Player.REPEAT_MODE_ONE -> RepeatMode.ONE
                else -> RepeatMode.OFF
            },
            queueIndex = c.currentMediaItemIndex,
            queueSize = c.mediaItemCount,
        )
        if (c.isPlaying) startTicker() else stopTicker()
    }

    private fun startTicker() {
        if (ticker?.isActive == true) return
        ticker = viewModelScope.launch {
            while (isActive) {
                controller?.let { c ->
                    _playback.update { it.copy(positionMs = c.currentPosition.coerceAtLeast(0)) }
                }
                delay(200)
            }
        }
    }

    private fun stopTicker() {
        ticker?.cancel()
        ticker = null
    }

    override fun onCleared() {
        stopTicker()
        controller?.removeListener(listener)
        controller?.release()
        controller = null
    }

    private fun Track.toMediaItem(): MediaItem = MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(artist)
                .setAlbumTitle(album)
                .setArtworkUri(artUri)
                .build(),
        )
        .build()

    /** Used when the service is still playing a queue but the library is not loaded yet. */
    private fun MediaItem.toFallbackTrack(): Track? {
        if (mediaId.isEmpty()) return null
        return Track(
            id = mediaId.toLongOrNull() ?: -1,
            title = mediaMetadata.title?.toString().orEmpty(),
            artist = mediaMetadata.artist?.toString().orEmpty(),
            album = mediaMetadata.albumTitle?.toString().orEmpty(),
            albumId = -1,
            durationMs = 0,
            uri = localConfiguration?.uri ?: android.net.Uri.EMPTY,
            artUri = mediaMetadata.artworkUri ?: android.net.Uri.EMPTY,
        )
    }
}
