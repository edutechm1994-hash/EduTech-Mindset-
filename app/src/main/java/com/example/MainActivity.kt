package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PermMedia
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.PermMedia
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.local.AppPreferences
import com.example.data.local.UserPreferencesRepository
import com.example.firebase.authStateFlow
import com.example.firebase.configureAppCheck
import com.example.firebase.signOutFromGoogleAndFirebase
import com.example.ui.components.MediaPlayerModal
import com.example.ui.components.QuantumMotionBackground
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.AuthSignInScreen
import com.example.ui.screens.CourseDetailDialog
import com.example.ui.screens.CourseModuleScreen
import com.example.ui.screens.CoursesScreen
import com.example.ui.screens.EDUTECH_WEBSITE_URL
import com.example.ui.screens.EduTechToolkitScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MediaHubScreen
import com.example.ui.screens.MentorAcademyScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.WebsitePortalScreen
import com.example.ui.screens.WelcomeMotionScreen
import com.example.ui.theme.EduTechMindsetTheme
import com.example.ui.viewmodel.AssistantChatMessage
import com.example.ui.viewmodel.CloudSyncStatus
import com.example.ui.viewmodel.EduTechViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.serialization.Serializable

@Serializable object HomeRoute
@Serializable object CoursesRoute
@Serializable object AssistantRoute
@Serializable object MediaRoute
@Serializable object NotesRoute
@Serializable object MentorRoute
@Serializable object ToolkitRoute
@Serializable data class WebsitePortalRoute(val url: String = EDUTECH_WEBSITE_URL)

private data class NavTabItem<T : Any>(
    val route: T,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

class MainActivity : ComponentActivity() {
    private var deepLinkWebsiteUrl by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Must execute first before setContent, ViewModels, or any Firebase call
        configureAppCheck(this, intent)
        deepLinkWebsiteUrl = extractEduTechLink(intent)
        enableEdgeToEdge()
        setContent {
            val prefsRepo = remember(applicationContext) { UserPreferencesRepository(applicationContext) }
            val rootPrefs by prefsRepo.preferencesFlow.collectAsStateWithLifecycle(initialValue = AppPreferences())
            val authFlow = remember { Firebase.auth.authStateFlow() }
            val initialUser = remember { Firebase.auth.currentUser }
            val currentUser by authFlow.collectAsStateWithLifecycle(initialValue = initialUser)

            val isSystemDark = isSystemInDarkTheme()
            val effectiveDarkTheme = rootPrefs.resolveDarkTheme(isSystemDark)

            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                EduTechMindsetTheme(
                    darkTheme = effectiveDarkTheme,
                    dynamicColor = rootPrefs.dynamicColorEnabled,
                ) {
                    val user = currentUser
                    if (user == null) {
                        AuthSignInScreen(
                            onAuthSuccess = { /* AuthStateListener drives transition reactively */ }
                        )
                    } else {
                        val uid = user.uid
                        val context = LocalContext.current
                        val coroutineScope = rememberCoroutineScope()
                        val credentialManager = remember(context) { CredentialManager.create(context) }

                        key(uid) {
                            val viewModel: EduTechViewModel = viewModel(
                                key = uid,
                                factory = EduTechViewModel.factory(application, uid),
                            )
                            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                            val cloudSyncStatus by viewModel.cloudSyncStatus.collectAsStateWithLifecycle()
                            val showWelcome by viewModel.showWelcome.collectAsStateWithLifecycle()
                            val activeMedia by viewModel.activeMediaPlayer.collectAsStateWithLifecycle()
                            val selectedCourseWithModules by viewModel.selectedCourseWithModules.collectAsStateWithLifecycle()
                            val selectedModuleWithProgress by viewModel.selectedModuleWithProgress.collectAsStateWithLifecycle()
                            val filteredCoursesWithModules by viewModel.filteredCoursesWithModules.collectAsStateWithLifecycle()
                            val matchedModules by viewModel.matchedModules.collectAsStateWithLifecycle()
                            val courseSearchQuery by viewModel.courseSearchQuery.collectAsStateWithLifecycle()
                            val selectedCourseCategory by viewModel.selectedCourseCategory.collectAsStateWithLifecycle()
                            val assistantMessages by viewModel.assistantMessages.collectAsStateWithLifecycle()
                            val isAssistantThinking by viewModel.isAssistantThinking.collectAsStateWithLifecycle()
                            val latestSpokenReply by viewModel.latestSpokenReply.collectAsStateWithLifecycle()
                            val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

                            EduTechAppShell(
                                uiState = uiState,
                                firebaseUserName = user.displayName,
                                firebaseUserEmail = user.email,
                                cloudSyncStatus = cloudSyncStatus,
                                onSignOut = {
                                    signOutFromGoogleAndFirebase(
                                        credentialManager = credentialManager,
                                        scope = coroutineScope,
                                    )
                                },
                                showWelcome = showWelcome,
                                activeMedia = activeMedia,
                                selectedCourseWithModules = selectedCourseWithModules,
                                selectedModuleWithProgress = selectedModuleWithProgress,
                                filteredCoursesWithModules = filteredCoursesWithModules,
                                matchedModules = matchedModules,
                                courseSearchQuery = courseSearchQuery,
                                selectedCourseCategory = selectedCourseCategory,
                                assistantMessages = assistantMessages,
                                isAssistantThinking = isAssistantThinking,
                                latestSpokenReply = latestSpokenReply,
                                snackbarMessage = snackbarMessage,
                                onClearSnackbar = viewModel::clearSnackbar,
                                onShowNotice = viewModel::showNotice,
                                onDismissWelcome = viewModel::dismissWelcome,
                                onReplayWelcome = viewModel::replayWelcome,
                                onSetThemeMode = viewModel::setThemeMode,
                                onToggleDarkTheme = viewModel::toggleDarkTheme,
                                onToggleDynamicColor = viewModel::setDynamicColor,
                                onToggleHighMotion = viewModel::toggleHighMotion,
                                onUpdateLearnerName = viewModel::updateLearnerName,
                                onUpdateFounderAvatar = viewModel::updateFounderAvatar,
                                onSearchQueryChange = viewModel::updateCourseSearchQuery,
                                onCategoryChange = viewModel::updateCourseCategory,
                                onOpenCourse = viewModel::openCourseDetail,
                                onOpenMatchedModule = viewModel::openCourseAndModule,
                                onCloseCourse = viewModel::closeCourseDetail,
                                onUpdateCourseProgress = viewModel::updateCourseProgress,
                                onToggleCourseComplete = viewModel::toggleCourseComplete,
                                onToggleModuleComplete = viewModel::toggleModuleCompleted,
                                onOpenModule = viewModel::openModuleScreen,
                                onCloseModule = viewModel::closeModuleScreen,
                                onModulePlaybackPositionChanged = viewModel::updateModulePlaybackPosition,
                                onAttachLocalVideoToModule = viewModel::attachLocalVideoToModule,
                                onAttachVideoUrlToModule = viewModel::attachVideoUrlToModule,
                                onToggleCourseBookmark = viewModel::toggleCourseBookmark,
                                onAddCustomCourse = viewModel::addCustomCourse,
                                onDeleteCourse = viewModel::deleteCourse,
                                onOpenMedia = viewModel::openMediaPlayer,
                                onCloseMedia = viewModel::closeMediaPlayer,
                                onImportLocalMedia = viewModel::importLocalMedia,
                                onAddExternalLink = viewModel::addExternalMediaLink,
                                onToggleMediaBookmark = viewModel::toggleMediaBookmark,
                                onDeleteMedia = viewModel::deleteMedia,
                                onSaveNote = viewModel::saveNote,
                                onToggleNotePin = viewModel::toggleNotePin,
                                onDeleteNote = viewModel::deleteNote,
                                onSendAssistantPrompt = viewModel::sendAssistantPrompt,
                                onOpenSuggestedCourse = viewModel::openSuggestedCourseById,
                                onCompleteFocusSession = viewModel::completeFocusSession,
                                pendingDeepLinkUrl = deepLinkWebsiteUrl,
                                onConsumeDeepLinkUrl = { deepLinkWebsiteUrl = null },
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val link = extractEduTechLink(intent)
        if (link != null) {
            deepLinkWebsiteUrl = link
        }
    }

    private fun extractEduTechLink(intent: Intent?): String? {
        val data = intent?.data ?: return null
        return if (data.host == "edutech-mindset.pages.dev") data.toString() else null
    }
}

@Composable
private fun EduTechAppShell(
    uiState: com.example.ui.viewmodel.EduTechUiState,
    firebaseUserName: String?,
    firebaseUserEmail: String?,
    cloudSyncStatus: CloudSyncStatus,
    onSignOut: () -> Unit,
    showWelcome: Boolean,
    activeMedia: com.example.data.local.MediaAssetEntity?,
    selectedCourseWithModules: com.example.data.local.CourseWithModules?,
    selectedModuleWithProgress: com.example.data.local.ModuleWithProgress?,
    filteredCoursesWithModules: List<com.example.data.local.CourseWithModules>,
    matchedModules: List<com.example.data.local.ModuleWithProgress>,
    courseSearchQuery: String,
    selectedCourseCategory: String,
    assistantMessages: List<AssistantChatMessage>,
    isAssistantThinking: Boolean,
    latestSpokenReply: AssistantChatMessage?,
    snackbarMessage: String?,
    onClearSnackbar: () -> Unit,
    onShowNotice: (String) -> Unit,
    onDismissWelcome: () -> Unit,
    onReplayWelcome: () -> Unit,
    onSetThemeMode: (com.example.data.local.ThemeMode) -> Unit,
    onToggleDarkTheme: (Boolean) -> Unit,
    onToggleDynamicColor: (Boolean) -> Unit,
    onToggleHighMotion: (Boolean) -> Unit,
    onUpdateLearnerName: (String) -> Unit,
    onUpdateFounderAvatar: (android.net.Uri) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onOpenCourse: (com.example.data.local.CourseEntity) -> Unit,
    onOpenMatchedModule: (com.example.data.local.ModuleEntity) -> Unit,
    onCloseCourse: () -> Unit,
    onUpdateCourseProgress: (com.example.data.local.CourseEntity, Int) -> Unit,
    onToggleCourseComplete: (com.example.data.local.CourseEntity) -> Unit,
    onToggleModuleComplete: (com.example.data.local.CourseEntity, com.example.data.local.ModuleEntity, Boolean) -> Unit,
    onOpenModule: (com.example.data.local.ModuleEntity) -> Unit,
    onCloseModule: () -> Unit,
    onModulePlaybackPositionChanged: (com.example.data.local.CourseEntity, com.example.data.local.ModuleEntity, Long, Long) -> Unit,
    onAttachLocalVideoToModule: (com.example.data.local.ModuleEntity, android.net.Uri) -> Unit,
    onAttachVideoUrlToModule: (com.example.data.local.ModuleEntity, String) -> Unit,
    onToggleCourseBookmark: (com.example.data.local.CourseEntity) -> Unit,
    onAddCustomCourse: (String, String, String, String, String, Int) -> Unit,
    onDeleteCourse: (com.example.data.local.CourseEntity) -> Unit,
    onOpenMedia: (com.example.data.local.MediaAssetEntity) -> Unit,
    onCloseMedia: () -> Unit,
    onImportLocalMedia: (android.net.Uri, String, String, String) -> Unit,
    onAddExternalLink: (String, String, com.example.data.local.MediaKind, String, String) -> Unit,
    onToggleMediaBookmark: (com.example.data.local.MediaAssetEntity) -> Unit,
    onDeleteMedia: (com.example.data.local.MediaAssetEntity) -> Unit,
    onSaveNote: (Long?, String, String, String, String, android.net.Uri?, String, String) -> Unit,
    onToggleNotePin: (com.example.data.local.StudyNoteEntity) -> Unit,
    onDeleteNote: (com.example.data.local.StudyNoteEntity) -> Unit,
    onSendAssistantPrompt: (String) -> Unit,
    onOpenSuggestedCourse: (Long) -> Unit,
    onCompleteFocusSession: () -> Unit,
    pendingDeepLinkUrl: String?,
    onConsumeDeepLinkUrl: () -> Unit,
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(pendingDeepLinkUrl) {
        if (!pendingDeepLinkUrl.isNullOrBlank()) {
            onDismissWelcome()
            navController.navigate(WebsitePortalRoute(pendingDeepLinkUrl)) {
                launchSingleTop = true
            }
            onConsumeDeepLinkUrl()
        }
    }

    LaunchedEffect(snackbarMessage) {
        if (!snackbarMessage.isNullOrBlank()) {
            snackbarHostState.showSnackbar(snackbarMessage)
            onClearSnackbar()
        }
    }

    val navItems = remember {
        listOf(
            NavTabItem(HomeRoute, "الرئيسية", Icons.Filled.Home, Icons.Outlined.Home, "nav_home"),
            NavTabItem(CoursesRoute, "المسارات", Icons.Filled.MenuBook, Icons.Outlined.MenuBook, "nav_courses"),
            NavTabItem(AssistantRoute, "المساعد", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "nav_assistant"),
            NavTabItem(MediaRoute, "الوسائط", Icons.Filled.PermMedia, Icons.Outlined.PermMedia, "nav_media"),
            NavTabItem(NotesRoute, "ملاحظاتي", Icons.Filled.EditNote, Icons.Outlined.EditNote, "nav_notes"),
        )
    }
    val railItems = remember {
        navItems + NavTabItem(MentorRoute, "الأكاديمية", Icons.Filled.Person, Icons.Outlined.Person, "nav_mentor")
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    fun navigateTab(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Subtle Motion Background across the app
        QuantumMotionBackground(animate = uiState.preferences.isHighMotionEnabled && !showWelcome)

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (!useRail && !showWelcome) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            windowInsets = WindowInsets.navigationBars,
                        ) {
                            navItems.forEach { item ->
                                val selected = currentDestination?.hasRoute(item.route::class) == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTab(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label,
                                        )
                                    },
                                    label = { Text(item.label, maxLines = 1) },
                                    modifier = Modifier.testTag(item.testTag),
                                )
                            }
                        }
                    }
                },
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    if (useRail && !showWelcome) {
                        NavigationRail(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            modifier = Modifier
                                .fillMaxHeight()
                                .windowInsetsPadding(WindowInsets.navigationBars),
                        ) {
                            railItems.forEach { item ->
                                val selected = currentDestination?.hasRoute(item.route::class) == true
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTab(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label,
                                        )
                                    },
                                    label = { Text(item.label) },
                                    modifier = Modifier.testTag(item.testTag),
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = HomeRoute,
                        modifier = Modifier.weight(1f),
                    ) {
                        composable<HomeRoute> {
                            HomeScreen(
                                state = uiState,
                                onReplayWelcome = onReplayWelcome,
                                onToggleTheme = onToggleDarkTheme,
                                onOpenCourse = onOpenCourse,
                                onOpenMedia = onOpenMedia,
                                onNavigateCourses = { navigateTab(CoursesRoute) },
                                onNavigateAssistant = { navigateTab(AssistantRoute) },
                                onNavigateMedia = { navigateTab(MediaRoute) },
                                onNavigateNotes = { navigateTab(NotesRoute) },
                                onNavigateMentor = { navController.navigate(MentorRoute) { launchSingleTop = true } },
                                onNavigateToolkit = { navController.navigate(ToolkitRoute) { launchSingleTop = true } },
                                onNavigateWebsite = { navController.navigate(WebsitePortalRoute()) { launchSingleTop = true } },
                            )
                        }
                        composable<CoursesRoute> {
                            CoursesScreen(
                                coursesWithModules = filteredCoursesWithModules,
                                matchedModules = matchedModules,
                                searchQuery = courseSearchQuery,
                                selectedCategory = selectedCourseCategory,
                                onSearchQueryChange = onSearchQueryChange,
                                onCategoryChange = onCategoryChange,
                                onOpenCourse = onOpenCourse,
                                onOpenMatchedModule = onOpenMatchedModule,
                                onToggleBookmark = onToggleCourseBookmark,
                                onAddCourse = onAddCustomCourse,
                            )
                        }
                        composable<AssistantRoute> {
                            AssistantScreen(
                                messages = assistantMessages,
                                isThinking = isAssistantThinking,
                                latestSpokenReply = latestSpokenReply,
                                onSendPrompt = onSendAssistantPrompt,
                                onOpenSuggestedCourse = onOpenSuggestedCourse,
                                onShowNotice = onShowNotice,
                            )
                        }
                        composable<MediaRoute> {
                            MediaHubScreen(
                                mediaAssets = uiState.mediaAssets,
                                internalStorageBytes = uiState.internalStorageBytes,
                                onOpenMedia = onOpenMedia,
                                onToggleBookmark = onToggleMediaBookmark,
                                onDeleteMedia = onDeleteMedia,
                                onImportLocalUri = onImportLocalMedia,
                                onAddExternalLink = onAddExternalLink,
                            )
                        }
                        composable<NotesRoute> {
                            NotesScreen(
                                notes = uiState.notes,
                                onSaveNote = onSaveNote,
                                onTogglePin = onToggleNotePin,
                                onDeleteNote = onDeleteNote,
                                onOpenAttachedMedia = onOpenMedia,
                            )
                        }
                        composable<MentorRoute> {
                            MentorAcademyScreen(
                                state = uiState,
                                firebaseUserName = firebaseUserName,
                                firebaseUserEmail = firebaseUserEmail,
                                cloudSyncStatus = cloudSyncStatus,
                                onSignOut = onSignOut,
                                onUpdateLearnerName = onUpdateLearnerName,
                                onUpdateFounderAvatar = onUpdateFounderAvatar,
                                onSetThemeMode = onSetThemeMode,
                                onToggleDynamicColor = onToggleDynamicColor,
                                onToggleHighMotion = onToggleHighMotion,
                                onReplayWelcome = onReplayWelcome,
                                onOpenWebsite = { navController.navigate(WebsitePortalRoute()) { launchSingleTop = true } },
                                onOpenToolkit = { navController.navigate(ToolkitRoute) { launchSingleTop = true } },
                            )
                        }
                        composable<ToolkitRoute> {
                            EduTechToolkitScreen(
                                onBack = { navController.popBackStack() },
                                onSaveBlueprintToNotes = { title, content, tag ->
                                    onSaveNote(null, title, content, tag, "cyan", null, "", "")
                                },
                                onCompleteFocusSession = onCompleteFocusSession,
                                onOpenWebsitePortal = { navController.navigate(WebsitePortalRoute()) { launchSingleTop = true } },
                                onShowNotice = onShowNotice,
                            )
                        }
                        composable<WebsitePortalRoute> { backStackEntry ->
                            val route = backStackEntry.toRoute<WebsitePortalRoute>()
                            WebsitePortalScreen(
                                initialUrl = route.url,
                                onBack = { navController.popBackStack() },
                            )
                        }
                    }
                }
            }
        }

        // Course Detail Modal
        if (selectedCourseWithModules != null && selectedModuleWithProgress == null) {
            CourseDetailDialog(
                courseWithModules = selectedCourseWithModules,
                onDismiss = onCloseCourse,
                onUpdateProgress = { prog -> onUpdateCourseProgress(selectedCourseWithModules.course, prog) },
                onToggleComplete = { onToggleCourseComplete(selectedCourseWithModules.course) },
                onToggleModuleComplete = { mod, done ->
                    onToggleModuleComplete(selectedCourseWithModules.course, mod, done)
                },
                onOpenModule = onOpenModule,
                onDeleteCourse = { onDeleteCourse(selectedCourseWithModules.course) },
                onPlayCourseMedia = onOpenMedia,
            )
        }

        // Dedicated Course Module Screen with embedded Media3 VideoPlayer
        if (selectedCourseWithModules != null && selectedModuleWithProgress != null) {
            CourseModuleScreen(
                courseWithModules = selectedCourseWithModules,
                activeModuleWithProgress = selectedModuleWithProgress,
                onBack = onCloseModule,
                onSelectModule = onOpenModule,
                onToggleModuleComplete = { mod, done ->
                    onToggleModuleComplete(selectedCourseWithModules.course, mod, done)
                },
                onPlaybackPositionChanged = { mod, pos, dur ->
                    onModulePlaybackPositionChanged(selectedCourseWithModules.course, mod, pos, dur)
                },
                onAttachLocalVideo = onAttachLocalVideoToModule,
                onAttachVideoUrl = onAttachVideoUrlToModule,
                onOpenFullscreenMedia = onOpenMedia,
            )
        }

        // Full Multimedia Modal (Video / Audio Waveform / Image Lightbox / PDF Viewer)
        if (activeMedia != null) {
            MediaPlayerModal(
                media = activeMedia,
                onDismiss = onCloseMedia,
            )
        }

        // Welcome & Motion Graphics Showcase Overlay
        AnimatedVisibility(
            visible = showWelcome,
            enter = fadeIn(tween(350)),
            exit = fadeOut(tween(420)) + slideOutVertically(
                targetOffsetY = { -it / 4 },
                animationSpec = tween(420),
            ),
        ) {
            BackHandler(enabled = showWelcome) {
                onDismissWelcome()
            }
            WelcomeMotionScreen(
                highMotionEnabled = uiState.preferences.isHighMotionEnabled,
                founderAvatarUri = uiState.preferences.founderAvatarUri,
                onEnterPlatform = onDismissWelcome,
            )
        }
    }
}
