package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.AnimatedProgressRing

private const val ALL_CATEGORIES_ID = "all"

@Composable
fun LearningModulesDashboardScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var snapshot by remember { mutableStateOf(EduTechDummyDashboardData.createInitialSnapshot()) }
    var selectedCategoryId by remember { mutableStateOf(ALL_CATEGORIES_ID) }
    var searchQuery by remember { mutableStateOf("") }
    var bookmarkedOnly by remember { mutableStateOf(false) }
    var activeModulePreview by remember { mutableStateOf<LearningModuleItem?>(null) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    val categoryCounts by remember(snapshot.modules) {
        derivedStateOf {
            snapshot.modules.groupingBy { it.categoryId }.eachCount()
        }
    }

    val filteredModules by remember(snapshot.modules, selectedCategoryId, searchQuery, bookmarkedOnly) {
        derivedStateOf {
            val cleanQuery = searchQuery.trim()
            snapshot.modules.filter { module ->
                val matchesCat = selectedCategoryId == ALL_CATEGORIES_ID || module.categoryId == selectedCategoryId
                val matchesBookmark = !bookmarkedOnly || module.isBookmarked
                val matchesQuery = cleanQuery.isEmpty() ||
                    module.title.contains(cleanQuery, ignoreCase = true) ||
                    module.summary.contains(cleanQuery, ignoreCase = true) ||
                    module.instructor.contains(cleanQuery, ignoreCase = true)
                matchesCat && matchesBookmark && matchesQuery
            }
        }
    }

    val featuredModule by remember(snapshot.modules) {
        derivedStateOf {
            snapshot.modules.firstOrNull { it.isFeatured }
                ?: snapshot.modules.firstOrNull { it.progressPercent in 1..99 }
                ?: snapshot.modules.firstOrNull()
        }
    }

    val overallProgressFraction by remember(snapshot.modules) {
        derivedStateOf {
            if (snapshot.modules.isEmpty()) 0f
            else snapshot.modules.map { it.progressPercent }.average().toFloat() / 100f
        }
    }

    fun toggleBookmark(moduleId: String) {
        snapshot = snapshot.copy(
            modules = snapshot.modules.map { item ->
                if (item.id == moduleId) item.copy(isBookmarked = !item.isBookmarked) else item
            }
        )
        activeModulePreview = activeModulePreview?.let { current ->
            if (current.id == moduleId) current.copy(isBookmarked = !current.isBookmarked) else current
        }
    }

    fun advanceProgress(moduleId: String) {
        var addedMinutes = 0
        var addedXp = 0
        snapshot = snapshot.copy(
            modules = snapshot.modules.map { item ->
                if (item.id == moduleId && item.progressPercent < 100) {
                    val nextProgress = (item.progressPercent + 25).coerceAtMost(100)
                    addedMinutes = 6
                    addedXp = if (nextProgress == 100) item.xpReward else 20
                    item.copy(progressPercent = nextProgress)
                } else {
                    item
                }
            },
            metrics = snapshot.metrics.copy(
                completedMinutes = (snapshot.metrics.completedMinutes + addedMinutes)
                    .coerceAtMost(snapshot.metrics.weeklyGoalMinutes),
                totalXp = snapshot.metrics.totalXp + addedXp,
            ),
        )
        activeModulePreview = snapshot.modules.firstOrNull { it.id == moduleId }
    }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        val isWideScreen = maxWidth >= 600.dp

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .testTag("dashboard_modules_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // 1. Top Header with Back Action & Learner Metrics
            item {
                DashboardTopHeader(
                    metrics = snapshot.metrics,
                    onBack = onBack,
                )
            }

            // 2. Asymmetric Glassmorphic Spotlight Card (Continue Learning + Weekly Goal)
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { 24 },
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    ),
                ) {
                    DashboardSpotlightCard(
                        metrics = snapshot.metrics,
                        overallProgress = overallProgressFraction,
                        featuredModule = featuredModule,
                        onOpenFeatured = { mod -> activeModulePreview = mod },
                    )
                }
            }

            // 3. Search Field & Bookmark Filter Strip
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        placeholder = { Text("ابحث في الوحدات المصنّفة...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح البحث")
                                }
                            }
                        },
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dashboard_search_input"),
                    )

                    FilterChip(
                        selected = bookmarkedOnly,
                        onClick = { bookmarkedOnly = !bookmarkedOnly },
                        label = { Text("المفضلة") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (bookmarkedOnly) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("dashboard_bookmark_filter"),
                    )
                }
            }

            // 4. Horizontal Category Selector Strip
            item {
                LazyRow(
                    modifier = Modifier.testTag("dashboard_categories_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 2.dp),
                ) {
                    item(key = ALL_CATEGORIES_ID) {
                        val isSelected = selectedCategoryId == ALL_CATEGORIES_ID
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryId = ALL_CATEGORIES_ID },
                            label = { Text("الكل (${snapshot.modules.size})") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dashboard_cat_all"),
                        )
                    }
                    items(snapshot.categories, key = { it.id }) { category ->
                        val isSelected = selectedCategoryId == category.id
                        val count = categoryCounts[category.id] ?: 0
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategoryId = category.id },
                            label = { Text("${category.title} ($count)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("dashboard_cat_${category.id}"),
                        )
                    }
                }
            }

            // 5. Categorized Learning Modules Sections or Empty State
            if (filteredModules.isEmpty()) {
                item {
                    DashboardEmptyState(
                        onResetFilters = {
                            selectedCategoryId = ALL_CATEGORIES_ID
                            searchQuery = ""
                            bookmarkedOnly = false
                        }
                    )
                }
            } else {
                val visibleCategories = snapshot.categories.filter { cat ->
                    filteredModules.any { it.categoryId == cat.id }
                }

                visibleCategories.forEach { category ->
                    val categoryModules = filteredModules.filter { it.categoryId == category.id }
                    item(key = "header_${category.id}") {
                        CategorySectionHeader(
                            category = category,
                            count = categoryModules.size,
                        )
                    }

                    if (isWideScreen) {
                        val rows = categoryModules.chunked(2)
                        items(rows, key = { row -> "row_${category.id}_${row.first().id}" }) { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                rowItems.forEach { module ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        LearningModuleCard(
                                            module = module,
                                            accentColor = resolveCategoryColor(category.tone),
                                            onOpen = { activeModulePreview = module },
                                            onToggleBookmark = { toggleBookmark(module.id) },
                                        )
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        items(categoryModules, key = { it.id }) { module ->
                            LearningModuleCard(
                                module = module,
                                accentColor = resolveCategoryColor(category.tone),
                                onOpen = { activeModulePreview = module },
                                onToggleBookmark = { toggleBookmark(module.id) },
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    // Quick Interactive Preview Dialog for Selected Dummy Module
    activeModulePreview?.let { module ->
        ModulePreviewDialog(
            module = module,
            onDismiss = { activeModulePreview = null },
            onAdvanceProgress = { advanceProgress(module.id) },
            onToggleBookmark = { toggleBookmark(module.id) },
        )
    }
}

@Composable
private fun DashboardTopHeader(
    metrics: DashboardSummaryMetrics,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("dashboard_back_button"),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "العودة",
                )
            }

            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.secondary),
                modifier = Modifier.size(44.dp),
            ) {
                AsyncImage(
                    model = R.drawable.img_edutech_official_logo,
                    contentDescription = "شعار EduTech Mindset",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                )
            }

            Column {
                Text(
                    text = "لوحة الوحدات المصنّفة",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "EduTech Mindset • مسارات تعليمية ذكية",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            MetricPill(
                icon = Icons.Default.LocalFireDepartment,
                text = "${metrics.activeStreakDays}d",
                tint = MaterialTheme.colorScheme.secondary,
            )
            MetricPill(
                icon = Icons.Default.Bolt,
                text = "${metrics.totalXp}",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun MetricPill(
    icon: ImageVector,
    text: String,
    tint: Color,
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = tint.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, tint.copy(alpha = 0.35f)),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DashboardSpotlightCard(
    metrics: DashboardSummaryMetrics,
    overallProgress: Float,
    featuredModule: LearningModuleItem?,
    onOpenFeatured: (LearningModuleItem) -> Unit,
) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val weeklyFraction = (metrics.completedMinutes.toFloat() / metrics.weeklyGoalMinutes.coerceAtLeast(1))
        .coerceIn(0f, 1f)

    Box(modifier = Modifier.fillMaxWidth()) {
        // Overlapping accent glow layer for depth
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = primary.copy(alpha = 0.08f),
            modifier = Modifier
                .matchParentSize()
                .offset(x = 0.dp, y = 6.dp),
        ) {}

        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            border = BorderStroke(
                width = 1.2.dp,
                brush = Brush.linearGradient(
                    listOf(primary.copy(alpha = 0.55f), secondary.copy(alpha = 0.45f))
                ),
            ),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(primary.copy(alpha = 0.16f), Color.Transparent),
                                center = Offset(size.width * 0.15f, size.height * 0.15f),
                                radius = size.minDimension * 0.95f,
                            ),
                            center = Offset(size.width * 0.15f, size.height * 0.15f),
                            radius = size.minDimension * 0.95f,
                        )
                    }
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = secondary.copy(alpha = 0.16f),
                        ) {
                            Text(
                                text = "متابعة التعلّم • ${metrics.learnerName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = secondary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                        Text(
                            text = featuredModule?.title ?: "خطة EduTech Mindset الأسبوعية",
                            style = MaterialTheme.typography.titleLarge,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "هدف الأسبوع: ${metrics.completedMinutes} / ${metrics.weeklyGoalMinutes} دقيقة",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(72.dp),
                    ) {
                        AnimatedProgressRing(
                            progressFraction = overallProgress,
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 7.dp,
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${(overallProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "إجمالي",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                LinearProgressIndicator(
                    progress = { weeklyFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50)),
                    color = secondary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                if (featuredModule != null) {
                    Button(
                        onClick = { onOpenFeatured(featuredModule) },
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("dashboard_resume_featured_btn"),
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("استكمال الوحدة النشطة (${featuredModule.progressPercent}%)")
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySectionHeader(
    category: DashboardCategory,
    count: Int,
) {
    val accent = resolveCategoryColor(category.tone)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f),
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = accent.copy(alpha = 0.16f),
                modifier = Modifier.size(38.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            Column {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Surface(
            shape = RoundedCornerShape(50),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = "$count وحدات",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
    }
}

@Composable
private fun LearningModuleCard(
    module: LearningModuleItem,
    accentColor: Color,
    onOpen: () -> Unit,
    onToggleBookmark: () -> Unit,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = (module.progressPercent / 100f).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "module_progress_${module.id}",
    )

    Card(
        onClick = onOpen,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (module.progressPercent == 100) {
                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.outlineVariant
            },
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_module_card_${module.id}"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = accentColor.copy(alpha = 0.14f),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = formatIcon(module.format),
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(14.dp),
                            )
                            Text(
                                text = module.format.labelAr,
                                style = MaterialTheme.typography.labelSmall,
                                color = accentColor,
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Text(
                            text = module.difficulty.labelAr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        )
                    }
                }

                IconButton(
                    onClick = onToggleBookmark,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("dashboard_bookmark_${module.id}"),
                ) {
                    Icon(
                        imageVector = if (module.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (module.isBookmarked) "إزالة من المفضلة" else "إضافة إلى المفضلة",
                        tint = if (module.isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Text(
                text = module.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = module.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(15.dp),
                        )
                        Text(
                            text = "${module.durationMinutes} د • ${module.lessonsCount} دروس",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (module.progressPercent == 100) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "مكتمل",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "مكتمل",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                        )
                    } else {
                        Text(
                            text = "${module.progressPercent}% • +${module.xpReward} XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = if (module.progressPercent == 100) MaterialTheme.colorScheme.tertiary else accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

@Composable
private fun DashboardEmptyState(
    onResetFilters: () -> Unit,
) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp),
            )
            Text(
                text = "لا توجد وحدات مطابقة لبحثك",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "جرّب مسح نص البحث أو عرض جميع التصنيفات لاستكشاف كل الوحدات التعليمية.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedButton(
                onClick = onResetFilters,
                modifier = Modifier.minimumInteractiveComponentSize(),
            ) {
                Text("إعادة ضبط الفلاتر")
            }
        }
    }
}

@Composable
private fun ModulePreviewDialog(
    module: LearningModuleItem,
    onDismiss: () -> Unit,
    onAdvanceProgress: () -> Unit,
    onToggleBookmark: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = formatIcon(module.format),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = {
            Text(
                text = module.title,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = module.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "المقدّم: ${module.instructor} • ${module.lessonsCount} دروس (${module.durationMinutes} دقيقة)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                LinearProgressIndicator(
                    progress = { (module.progressPercent / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(50)),
                )
                Text(
                    text = "نسبة الإنجاز الحالية: ${module.progressPercent}% • المكافأة: +${module.xpReward} XP",
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAdvanceProgress,
                enabled = module.progressPercent < 100,
                modifier = Modifier.testTag("dashboard_dialog_advance_btn"),
            ) {
                Text(if (module.progressPercent >= 100) "مكتملة بالكامل" else "تقدّم +25%")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                TextButton(onClick = onToggleBookmark) {
                    Text(if (module.isBookmarked) "إزالة الحفظ" else "حفظ")
                }
                TextButton(onClick = onDismiss) {
                    Text("إغلاق")
                }
            }
        },
    )
}

@Composable
private fun resolveCategoryColor(tone: CategoryTone): Color = when (tone) {
    CategoryTone.PRIMARY -> MaterialTheme.colorScheme.primary
    CategoryTone.SECONDARY -> MaterialTheme.colorScheme.secondary
    CategoryTone.TERTIARY -> MaterialTheme.colorScheme.tertiary
}

private fun formatIcon(format: ModuleFormat): ImageVector = when (format) {
    ModuleFormat.VIDEO -> Icons.Default.PlayCircle
    ModuleFormat.AUDIO -> Icons.Default.GraphicEq
    ModuleFormat.INTERACTIVE -> Icons.Default.AutoAwesome
    ModuleFormat.GUIDE -> Icons.AutoMirrored.Filled.MenuBook
}
