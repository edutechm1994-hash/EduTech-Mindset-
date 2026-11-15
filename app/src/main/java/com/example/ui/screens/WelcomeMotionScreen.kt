package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.AnimatedProgressRing
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.QuantumMotionBackground
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun WelcomeMotionScreen(
    highMotionEnabled: Boolean,
    founderAvatarUri: String = "",
    onEnterPlatform: () -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val coroutineScope = rememberCoroutineScope()
    var initialEntered by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(80)
        initialEntered = true
    }

    val infiniteTransition = rememberInfiniteTransition(label = "welcome_pulse")
    val ctaPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.035f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cta_scale",
    )

    val haloRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "halo_rot",
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Live Canvas Quantum Motion Background
        QuantumMotionBackground(animate = highMotionEnabled)

        // 2. Parallax Atmospheric Hero Backdrop
        Image(
            painter = painterResource(id = R.drawable.edutech_hero_banner_1794771056331),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val shift = (pagerState.currentPage + pagerState.currentPageOffsetFraction) * -28f
                    translationX = shift
                    scaleX = 1.12f
                    scaleY = 1.12f
                }
                .alpha(0.22f),
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.50f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.84f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.97f),
                        )
                    )
                )
        )

        // 3. Main Immersive Onboarding Layout
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Top Bar: Brand Chip + Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 580.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "${pagerState.currentPage + 1} / 3 • EduTech Mindset",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }

                TextButton(
                    onClick = onEnterPlatform,
                    modifier = Modifier.testTag("welcome_skip_button"),
                ) {
                    Text(
                        text = "تخطي",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Pager Content with Parallax & Scale Transitions
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            ) { page ->
                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction)
                    .absoluteValue
                    .coerceIn(0f, 1f)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            val scale = 1f - (pageOffset * 0.10f)
                            scaleX = scale
                            scaleY = scale
                            alpha = 1f - (pageOffset * 0.45f)
                            translationY = pageOffset * 28f
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    when (page) {
                        0 -> WelcomeBrandSlide(
                            visible = initialEntered,
                            highMotionEnabled = highMotionEnabled,
                        )
                        1 -> WelcomeMentorSlide(
                            highMotionEnabled = highMotionEnabled,
                            haloRotation = haloRotation,
                            founderAvatarUri = founderAvatarUri,
                        )
                        else -> WelcomeMediaPowerSlide(
                            highMotionEnabled = highMotionEnabled,
                        )
                    }
                }
            }

            // Bottom Navigation: Expanding Indicators + Morphing CTA
            Column(
                modifier = Modifier
                    .widthIn(max = 540.dp)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                // Expanding Pill Indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    repeat(3) { index ->
                        val isSelected = pagerState.currentPage == index
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 32.dp else 10.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "dot_width_$index",
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            },
                            label = "dot_color_$index",
                        )
                        Box(
                            modifier = Modifier
                                .height(10.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(dotColor)
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                        )
                    }
                }

                // Morphing Action Button
                val isLastPage = pagerState.currentPage == 2
                Button(
                    onClick = {
                        if (isLastPage) {
                            onEnterPlatform()
                        } else {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .scale(if (isLastPage && highMotionEnabled) ctaPulse else 1f)
                        .testTag("welcome_enter_button"),
                    shape = MaterialTheme.shapes.large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isLastPage) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        contentColor = if (isLastPage) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                ) {
                    AnimatedContent(
                        targetState = isLastPage,
                        transitionSpec = { fadeIn(tween(220)) togetherWith fadeOut(tween(180)) },
                        label = "cta_label",
                    ) { last ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = if (last) "ابدأ الرحلة التعليمية" else "التالي",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                            )
                            Icon(
                                imageVector = if (last) Icons.Default.RocketLaunch else Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WelcomeBrandSlide(
    visible: Boolean,
    highMotionEnabled: Boolean,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(500)) + scaleIn(
                initialScale = 0.6f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            ),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFFFDFEFE),
                    border = BorderStroke(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                            )
                        ),
                    ),
                    shadowElevation = if (highMotionEnabled) 14.dp else 6.dp,
                    modifier = Modifier.size(144.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_edutech_official_logo),
                        contentDescription = "شعار EduTech Mindset الرسمي",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                    )
                }
                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = Color(0xFF071D35),
                    border = BorderStroke(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.primary,
                            )
                        ),
                    ),
                    shadowElevation = if (highMotionEnabled) 14.dp else 6.dp,
                    modifier = Modifier.size(116.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.edutech_brand_emblem_1794771055994),
                        contentDescription = "رمز EduTech Mindset التعليمي",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(520, delayMillis = 120)) + slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
            ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = "EduTech Mindset",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "عقلية التعليم والتكنولوجيا",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "رؤية تعليمية متكاملة تبني المهارات الرقمية وتدمج التكنولوجيا بالإبداع التربوي — بإدارة الأستاذ باسل أشرف الفقعاوي.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(520, delayMillis = 240)),
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                WelcomeFeaturePill(icon = Icons.Default.MenuBook, label = "مسارات تفاعلية")
                WelcomeFeaturePill(icon = Icons.Default.Storage, label = "تخزين محلي آمن")
                WelcomeFeaturePill(icon = Icons.Default.PlayCircle, label = "وسائط شاملة")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WelcomeMentorSlide(
    highMotionEnabled: Boolean,
    haloRotation: Float,
    founderAvatarUri: String,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(152.dp)) {
            Box(
                modifier = Modifier
                    .size(146.dp)
                    .graphicsLayer {
                        if (highMotionEnabled) rotationZ = haloRotation
                    }
                    .border(
                        width = 2.5.dp,
                        brush = Brush.sweepGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary,
                                Color.Transparent,
                                MaterialTheme.colorScheme.primary,
                            )
                        ),
                        shape = CircleShape,
                    )
            )
            if (founderAvatarUri.isNotBlank()) {
                AsyncImage(
                    model = founderAvatarUri,
                    contentDescription = "الأستاذ باسل أشرف الفقعاوي",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(126.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.img_instructor_avatar_1794764865940),
                    contentDescription = "الأستاذ باسل أشرف الفقعاوي",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(126.dp)
                        .clip(CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.secondary, CircleShape),
                )
            }
        }

        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    )
                ),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "الأستاذ باسل أشرف الفقعاوي",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "موثّق",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Text(
                    text = "مؤسس ومدير منصة EduTech Mindset",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary,
                )
                Text(
                    text = "«نؤمن أن التكنولوجيا الحقيقية تبدأ من العقلية المبدعة. هنا تجتمع الأدوات الحديثة مع المنهجية التربوية لبناء تجربة تعلّم لا تُنسى.»",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WelcomeFeaturePill(icon = Icons.Default.AutoAwesome, label = "قيادة رقمية")
                    WelcomeFeaturePill(icon = Icons.Default.MenuBook, label = "تصميم التعليم")
                }
            }
        }
    }
}

@Composable
private fun WelcomeMediaPowerSlide(
    highMotionEnabled: Boolean,
) {
    Column(
        modifier = Modifier
            .widthIn(max = 520.dp)
            .verticalScroll(rememberScrollState())
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_track_media_lab_1794764865682),
                        contentDescription = "استوديو الوسائط",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .height(92.dp)
                            .clip(MaterialTheme.shapes.medium),
                    )
                    Image(
                        painter = painterResource(id = R.drawable.img_track_future_tech_1794764866174),
                        contentDescription = "المسارات الذكية",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .height(92.dp)
                            .clip(MaterialTheme.shapes.medium),
                    )
                }

                // Live kinetic waveform + progress ring preview
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp),
                    ) {
                        AudioWaveformVisualizer(
                            isPlaying = highMotionEnabled,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(64.dp)) {
                        AnimatedProgressRing(
                            progressFraction = 0.85f,
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 6.dp,
                        )
                        Text("100%", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Text(
                    text = "قوة الوسائط والتخزين المحلي",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "شغّل الفيديو عالي الدقة، البودكاست الصوتي، الصور التفاعلية، وملفات PDF، واحفظ مساراتك وملاحظاتك مباشرة في قاعدة بيانات هاتفك لتعمل بكفاءة في كل وقت.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    WelcomeFeaturePill(icon = Icons.Default.PlayCircle, label = "ExoPlayer")
                    WelcomeFeaturePill(icon = Icons.Default.GraphicEq, label = "استوديو صوت")
                    WelcomeFeaturePill(icon = Icons.Default.Storage, label = "Room محلي")
                }
            }
        }
    }
}

@Composable
private fun WelcomeFeaturePill(
    icon: ImageVector,
    label: String,
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
