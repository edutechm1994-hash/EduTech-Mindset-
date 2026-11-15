package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.local.MediaAssetEntity
import com.example.data.local.MediaKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale

/**
 * Reusable Media3 ExoPlayer VideoPlayer Composable supporting local URIs
 * (android.resource://, file://, content://) and remote HTTP/HTTPS URLs.
 */
@Composable
fun VideoPlayer(
    videoSource: String,
    modifier: Modifier = Modifier,
    autoPlay: Boolean = false,
    initialPositionMs: Long = 0L,
    onPositionChanged: (positionMs: Long, durationMs: Long) -> Unit = { _, _ -> },
    onPlaybackEnded: () -> Unit = {},
    onToggleFullscreen: (() -> Unit)? = null,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val latestOnPositionChanged by rememberUpdatedState(onPositionChanged)
    val latestOnEnded by rememberUpdatedState(onPlaybackEnded)

    var playbackSpeed by remember { mutableFloatStateOf(1f) }
    var isPlaying by remember { mutableStateOf(autoPlay) }
    var isBuffering by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val exoPlayer = remember(videoSource) {
        ExoPlayer.Builder(context).build().apply {
            if (videoSource.isNotBlank()) {
                setMediaItem(MediaItem.fromUri(Uri.parse(videoSource)))
                if (initialPositionMs > 0L) {
                    seekTo(initialPositionMs)
                }
                prepare()
                playWhenReady = autoPlay
            }
        }
    }

    LaunchedEffect(exoPlayer, isPlaying) {
        while (isPlaying) {
            val dur = exoPlayer.duration.coerceAtLeast(0L)
            if (dur > 0L) {
                latestOnPositionChanged(exoPlayer.currentPosition.coerceAtLeast(0L), dur)
            }
            delay(1000L)
        }
    }

    DisposableEffect(exoPlayer, lifecycleOwner) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                isBuffering = playbackState == Player.STATE_BUFFERING
                if (playbackState == Player.STATE_ENDED) {
                    val dur = exoPlayer.duration.coerceAtLeast(0L)
                    if (dur > 0L) {
                        latestOnPositionChanged(dur, dur)
                    }
                    latestOnEnded()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                errorMessage = "تعذّر تشغيل الفيديو: تحقق من الرابط أو صلاحية الملف"
                isBuffering = false
            }
        }
        exoPlayer.addListener(listener)

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                exoPlayer.pause()
                val dur = exoPlayer.duration.coerceAtLeast(0L)
                if (dur > 0L) {
                    latestOnPositionChanged(exoPlayer.currentPosition.coerceAtLeast(0L), dur)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            val dur = exoPlayer.duration.coerceAtLeast(0L)
            if (dur > 0L) {
                latestOnPositionChanged(exoPlayer.currentPosition.coerceAtLeast(0L), dur)
            }
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.medium)
                .background(Color.Black),
        ) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = true
                        keepScreenOn = true
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT,
                        )
                    }
                },
                update = { it.player = exoPlayer },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("media3_video_player_view"),
            )

            if (isBuffering && errorMessage == null) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center),
                )
            }

            if (errorMessage != null) {
                Surface(
                    color = Color.Black.copy(alpha = 0.85f),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(32.dp),
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = errorMessage ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White,
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                errorMessage = null
                                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(videoSource)))
                                exoPlayer.prepare()
                                exoPlayer.play()
                            },
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("إعادة المحاولة")
                        }
                    }
                }
            }

            if (onToggleFullscreen != null) {
                IconButton(
                    onClick = onToggleFullscreen,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape)
                        .testTag("video_fullscreen_button"),
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "ملء الشاشة",
                        tint = Color.White,
                    )
                }
            }
        }

        // Quick playback & speed controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilledIconButton(
                onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                modifier = Modifier
                    .size(42.dp)
                    .testTag("video_play_pause_button"),
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                )
            }
            IconButton(
                onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)) },
            ) {
                Icon(Icons.Default.Replay10, contentDescription = "ترجيع 10 ثوان")
            }
            IconButton(
                onClick = {
                    val dur = exoPlayer.duration.takeIf { it > 0 } ?: Long.MAX_VALUE
                    exoPlayer.seekTo((exoPlayer.currentPosition + 10_000L).coerceAtMost(dur))
                },
            ) {
                Icon(Icons.Default.Forward10, contentDescription = "تقديم 10 ثوان")
            }

            Icon(
                imageVector = Icons.Default.Speed,
                contentDescription = "سرعة التشغيل",
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(18.dp),
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f),
            ) {
                items(listOf(0.75f, 1.0f, 1.25f, 1.5f, 2.0f)) { spd ->
                    FilterChip(
                        selected = playbackSpeed == spd,
                        onClick = {
                            playbackSpeed = spd
                            exoPlayer.setPlaybackSpeed(spd)
                        },
                        label = { Text("${spd}x", style = MaterialTheme.typography.labelSmall) },
                    )
                }
            }
        }
    }
}

@Composable
fun MediaPlayerModal(
    media: MediaAssetEntity,
    onDismiss: () -> Unit,
) {
    val kind = runCatching { MediaKind.valueOf(media.mediaType) }.getOrDefault(MediaKind.DOCUMENT)
    when (kind) {
        MediaKind.VIDEO -> VideoPlayerDialog(media = media, onDismiss = onDismiss)
        MediaKind.AUDIO -> AudioPlayerDialog(media = media, onDismiss = onDismiss)
        MediaKind.IMAGE -> ImageLightboxDialog(media = media, onDismiss = onDismiss)
        MediaKind.DOCUMENT -> DocumentViewerDialog(media = media, onDismiss = onDismiss)
    }
}

@Composable
fun VideoPlayerDialog(
    media: MediaAssetEntity,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${media.category} • مشغّل الفيديو عالي الدقة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_video_player"),
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق المشغل")
                    }
                }

                VideoPlayer(
                    videoSource = media.uriString,
                    autoPlay = true,
                )

                Text(
                    text = media.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
fun AudioPlayerDialog(
    media: MediaAssetEntity,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isPlaying by remember { mutableStateOf(true) }
    var currentPosMs by remember { mutableLongStateOf(0L) }
    var durationMs by remember { mutableLongStateOf(1L) }
    var speed by remember { mutableFloatStateOf(1f) }

    val exoPlayer = remember(media.uriString) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(media.uriString)))
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }

    LaunchedEffect(exoPlayer, isPlaying) {
        while (true) {
            currentPosMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            durationMs = exoPlayer.duration.takeIf { it > 0 } ?: 12_000L
            delay(200)
        }
    }

    DisposableEffect(exoPlayer, lifecycleOwner) {
        val listener = object : Player.Listener {
            override fun onIsPlayingChanged(playing: Boolean) {
                isPlaying = playing
            }
        }
        exoPlayer.addListener(listener)
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) exoPlayer.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 10.dp,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "استوديو الصوت التعليمي",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Text(
                    text = media.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Surface(
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                ) {
                    AudioWaveformVisualizer(
                        isPlaying = isPlaying,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    )
                }

                Slider(
                    value = (currentPosMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f),
                    onValueChange = { frac ->
                        val target = (frac * durationMs).toLong()
                        exoPlayer.seekTo(target)
                        currentPosMs = target
                    },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(formatMillis(currentPosMs), style = MaterialTheme.typography.labelMedium)
                    Text(formatMillis(durationMs), style = MaterialTheme.typography.labelMedium)
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { exoPlayer.seekTo((exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)) },
                    ) {
                        Icon(Icons.Default.Replay10, contentDescription = "ترجيع 10 ثوان")
                    }

                    FilledIconButton(
                        onClick = { if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play() },
                        modifier = Modifier
                            .size(64.dp)
                            .testTag("audio_play_pause_button"),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                            modifier = Modifier.size(34.dp),
                        )
                    }

                    IconButton(
                        onClick = { exoPlayer.seekTo((exoPlayer.currentPosition + 10_000L).coerceAtMost(durationMs)) },
                    ) {
                        Icon(Icons.Default.Forward10, contentDescription = "تقديم 10 ثوان")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(1.0f, 1.25f, 1.5f).forEach { spd ->
                        FilterChip(
                            selected = speed == spd,
                            onClick = {
                                speed = spd
                                exoPlayer.setPlaybackSpeed(spd)
                            },
                            label = { Text("${spd}x") },
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ImageLightboxDialog(
    media: MediaAssetEntity,
    onDismiss: () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.94f),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = media.uriString,
                    contentDescription = media.title,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val newScale = (scale * zoom).coerceIn(1f, 4f)
                                scale = newScale
                                if (newScale > 1f) {
                                    offsetX += pan.x
                                    offsetY += pan.y
                                } else {
                                    offsetX = 0f
                                    offsetY = 0f
                                }
                            }
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onDoubleTap = {
                                    scale = if (scale > 1.2f) 1f else 2.2f
                                    offsetX = 0f
                                    offsetY = 0f
                                },
                            )
                        }
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY,
                        ),
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = media.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.ZoomIn,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "قرّب بالإصبعين أو انقر مرتين للتكبير",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f),
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun DocumentViewerDialog(
    media: MediaAssetEntity,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val isGuide = media.uriString.startsWith("guide://")
    var fontSizeSp by remember { mutableFloatStateOf(16f) }
    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }
    var renderedPage by remember { mutableStateOf<Bitmap?>(null) }
    var pdfError by remember { mutableStateOf<String?>(null) }

    if (!isGuide) {
        LaunchedEffect(media.uriString, currentPage) {
            withContext(Dispatchers.IO) {
                try {
                    val uri = Uri.parse(media.uriString)
                    val pfd: ParcelFileDescriptor? = if (uri.scheme == "file") {
                        ParcelFileDescriptor.open(File(uri.path!!), ParcelFileDescriptor.MODE_READ_ONLY)
                    } else {
                        context.contentResolver.openFileDescriptor(uri, "r")
                    }
                    pfd?.use { descriptor ->
                        PdfRenderer(descriptor).use { renderer ->
                            totalPages = renderer.pageCount
                            if (renderer.pageCount > 0) {
                                val safePage = currentPage.coerceIn(0, renderer.pageCount - 1)
                                renderer.openPage(safePage).use { page ->
                                    val bmp = Bitmap.createBitmap(
                                        (page.width * 2).coerceAtLeast(800),
                                        (page.height * 2).coerceAtLeast(1100),
                                        Bitmap.Config.ARGB_8888,
                                    )
                                    bmp.eraseColor(android.graphics.Color.WHITE)
                                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                                    renderedPage = bmp
                                }
                            }
                        }
                    }
                    pdfError = null
                } catch (e: Exception) {
                    pdfError = "تعذّر عرض المستند بصيغة PDF (${e.localizedMessage ?: "صيغة غير مدعومة"})"
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(media.title, style = MaterialTheme.typography.titleLarge)
                        Text(
                            text = if (isGuide) "ملخص تفاعلي محفوظ محلياً" else "عارض PDF المحلي",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                if (isGuide) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("حجم الخط:", style = MaterialTheme.typography.labelMedium)
                        Button(onClick = { fontSizeSp = (fontSizeSp - 2f).coerceAtLeast(13f) }) {
                            Text("A-")
                        }
                        Button(onClick = { fontSizeSp = (fontSizeSp + 2f).coerceAtMost(24f) }) {
                            Text("A+")
                        }
                    }
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                    ) {
                        Text(
                            text = media.description,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = fontSizeSp.sp,
                                lineHeight = (fontSizeSp * 1.6f).sp,
                                fontWeight = FontWeight.Normal,
                            ),
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(18.dp),
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        val bmp = renderedPage
                        when {
                            pdfError != null -> Text(
                                text = pdfError ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(16.dp),
                            )
                            bmp != null -> Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "صفحة PDF ${currentPage + 1}",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize(),
                            )
                            else -> Text("جاري تحضير صفحات المستند...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    if (totalPages > 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = { if (currentPage > 0) currentPage-- },
                                enabled = currentPage > 0,
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "السابق")
                            }
                            Text(
                                text = "صفحة ${currentPage + 1} من $totalPages",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            IconButton(
                                onClick = { if (currentPage < totalPages - 1) currentPage++ },
                                enabled = currentPage < totalPages - 1,
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "التالي")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatMillis(ms: Long): String {
    val totalSec = (ms / 1000L).coerceAtLeast(0L)
    val min = totalSec / 60L
    val sec = totalSec % 60L
    return String.format(Locale.US, "%02d:%02d", min, sec)
}
