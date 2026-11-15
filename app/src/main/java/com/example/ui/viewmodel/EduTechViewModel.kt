package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppPreferences
import com.example.data.local.CourseEntity
import com.example.data.local.CourseWithModules
import com.example.data.local.EduTechDatabase
import com.example.data.local.LearnerStatsEntity
import com.example.data.local.LocalMediaStorageManager
import com.example.data.local.MediaAssetEntity
import com.example.data.local.MediaKind
import com.example.data.local.ModuleEntity
import com.example.data.local.ModuleWithProgress
import com.example.data.local.StudyNoteEntity
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.AssistantRepository
import com.example.data.repository.EduTechRepository
import com.example.data.repository.LocalLearningSnapshot
import com.example.data.repository.LocalPlaceholderAssistantRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EduTechUiState(
    val isLoading: Boolean = true,
    val courses: List<CourseEntity> = emptyList(),
    val coursesWithModules: List<CourseWithModules> = emptyList(),
    val mediaAssets: List<MediaAssetEntity> = emptyList(),
    val notes: List<StudyNoteEntity> = emptyList(),
    val stats: LearnerStatsEntity = LearnerStatsEntity(),
    val preferences: AppPreferences = AppPreferences(),
    val internalStorageBytes: Long = 0L,
)

data class AssistantChatMessage(
    val id: Long = System.nanoTime(),
    val text: String,
    val isFromUser: Boolean,
    val isSampleNotice: Boolean = false,
    val suggestedCourseId: Long? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class EduTechViewModel(application: Application) : AndroidViewModel(application) {

    private val database = EduTechDatabase.getInstance(application)
    private val repository: EduTechRepository = EduTechRepository(
        context = application,
        courseDao = database.courseDao(),
        moduleDao = database.moduleDao(),
        progressDao = database.progressDao(),
        dao = database.eduTechDao(),
        prefsRepo = UserPreferencesRepository(application),
        mediaStorage = LocalMediaStorageManager(application),
    )

    private val assistantRepository: AssistantRepository = LocalPlaceholderAssistantRepository()

    private val _isLoading = MutableStateFlow(true)
    private val _internalStorageBytes = MutableStateFlow(0L)

    private val _showWelcome = MutableStateFlow(true)
    val showWelcome: StateFlow<Boolean> = _showWelcome.asStateFlow()

    private val _activeMediaPlayer = MutableStateFlow<MediaAssetEntity?>(null)
    val activeMediaPlayer: StateFlow<MediaAssetEntity?> = _activeMediaPlayer.asStateFlow()

    // Room + ViewModel Search State for Courses & Modules
    private val _courseSearchQuery = MutableStateFlow("")
    val courseSearchQuery: StateFlow<String> = _courseSearchQuery.asStateFlow()

    private val _selectedCourseCategory = MutableStateFlow("الكل")
    val selectedCourseCategory: StateFlow<String> = _selectedCourseCategory.asStateFlow()

    val filteredCoursesWithModules: StateFlow<List<CourseWithModules>> = combine(
        _courseSearchQuery,
        _selectedCourseCategory,
    ) { query, category -> query to category }
        .flatMapLatest { (query, category) ->
            repository.searchCoursesWithModules(query).combine(flowOf(category)) { list, cat ->
                if (cat == "الكل") list else list.filter { it.course.category == cat }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    val matchedModules: StateFlow<List<ModuleWithProgress>> = _courseSearchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchModulesWithProgress(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList(),
        )

    private val _selectedCourseId = MutableStateFlow<Long?>(null)
    val selectedCourseWithModules: StateFlow<CourseWithModules?> = combine(
        _selectedCourseId,
        repository.coursesWithModules,
    ) { selectedId, list ->
        if (selectedId == null) null else list.firstOrNull { it.course.id == selectedId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    private val _selectedModuleId = MutableStateFlow<Long?>(null)
    val selectedModuleWithProgress: StateFlow<ModuleWithProgress?> = combine(
        _selectedModuleId,
        selectedCourseWithModules,
    ) { moduleId, courseWithModules ->
        if (moduleId == null || courseWithModules == null) null
        else courseWithModules.orderedModules.firstOrNull { it.module.id == moduleId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null,
    )

    private val _assistantMessages = MutableStateFlow(
        listOf(
            AssistantChatMessage(
                text = "مرحباً بك في المساعد الشخصي والصوتي لمنصة EduTech Mindset! يمكنك التحدث بالميكروفون أو الكتابة للبحث في مساراتك، متابعة تقدمك، أو طلب خطة تعلّم.",
                isFromUser = false,
                isSampleNotice = true,
            )
        )
    )
    val assistantMessages: StateFlow<List<AssistantChatMessage>> = _assistantMessages.asStateFlow()

    private val _isAssistantThinking = MutableStateFlow(false)
    val isAssistantThinking: StateFlow<Boolean> = _isAssistantThinking.asStateFlow()

    private val _latestSpokenReply = MutableStateFlow<AssistantChatMessage?>(null)
    val latestSpokenReply: StateFlow<AssistantChatMessage?> = _latestSpokenReply.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    val uiState: StateFlow<EduTechUiState> = combine(
        repository.coursesWithModules,
        repository.mediaAssets,
        repository.studyNotes,
        repository.learnerStats,
        repository.preferences,
    ) { coursesWithModules, media, notes, stats, prefs ->
        EduTechUiState(
            isLoading = _isLoading.value,
            courses = coursesWithModules.map { it.course },
            coursesWithModules = coursesWithModules,
            mediaAssets = media,
            notes = notes,
            stats = stats ?: LearnerStatsEntity(),
            preferences = prefs,
            internalStorageBytes = _internalStorageBytes.value,
        )
    }.combine(_internalStorageBytes) { state, bytes ->
        state.copy(internalStorageBytes = bytes, isLoading = _isLoading.value)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = EduTechUiState(),
    )

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            refreshStorageSize()
            _isLoading.value = false
        }
    }

    private fun refreshStorageSize() {
        viewModelScope.launch {
            _internalStorageBytes.value = repository.getInternalStorageUsedBytes()
        }
    }

    fun updateCourseSearchQuery(query: String) {
        _courseSearchQuery.value = query
    }

    fun updateCourseCategory(category: String) {
        _selectedCourseCategory.value = category
    }

    fun openCourseAndModule(module: ModuleEntity) {
        _selectedCourseId.value = module.courseId
        _selectedModuleId.value = module.id
    }

    fun sendAssistantPrompt(prompt: String) {
        val clean = prompt.trim()
        if (clean.isBlank() || _isAssistantThinking.value) return
        val userMsg = AssistantChatMessage(text = clean, isFromUser = true)
        _assistantMessages.value = _assistantMessages.value + userMsg
        _isAssistantThinking.value = true

        viewModelScope.launch {
            try {
                val snapshot = LocalLearningSnapshot(
                    stats = uiState.value.stats,
                    courses = uiState.value.coursesWithModules,
                    notes = uiState.value.notes,
                )
                val reply = assistantRepository.ask(clean, snapshot)
                val botMsg = AssistantChatMessage(
                    text = reply.text,
                    isFromUser = false,
                    isSampleNotice = reply.isSampleNotice,
                    suggestedCourseId = reply.suggestedCourseId,
                )
                _assistantMessages.value = _assistantMessages.value + botMsg
                _latestSpokenReply.value = botMsg
            } finally {
                _isAssistantThinking.value = false
            }
        }
    }

    fun openSuggestedCourseById(courseId: Long) {
        _selectedCourseId.value = courseId
        _selectedModuleId.value = null
    }

    fun dismissWelcome() {
        _showWelcome.value = false
        viewModelScope.launch {
            repository.setCompletedWelcome(true)
        }
    }

    fun replayWelcome() {
        _showWelcome.value = true
    }

    fun openMediaPlayer(media: MediaAssetEntity) {
        _activeMediaPlayer.value = media
    }

    fun closeMediaPlayer() {
        _activeMediaPlayer.value = null
    }

    fun openCourseDetail(course: CourseEntity) {
        _selectedCourseId.value = course.id
    }

    fun closeCourseDetail() {
        _selectedModuleId.value = null
        _selectedCourseId.value = null
    }

    fun openModuleScreen(module: ModuleEntity) {
        _selectedModuleId.value = module.id
    }

    fun closeModuleScreen() {
        _selectedModuleId.value = null
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }

    fun showNotice(message: String) {
        _snackbarMessage.value = message
    }

    fun setThemeMode(mode: com.example.data.local.ThemeMode) {
        viewModelScope.launch {
            repository.setThemeMode(mode)
        }
    }

    fun toggleDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleDarkTheme(enabled)
        }
    }

    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            repository.setDynamicColor(enabled)
        }
    }

    fun toggleHighMotion(enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleHighMotion(enabled)
        }
    }

    fun updateLearnerName(name: String) {
        viewModelScope.launch {
            repository.updateLearnerName(name)
            _snackbarMessage.value = "تم تحديث اسم المتعلّم محلياً"
        }
    }

    fun updateFounderAvatar(uri: Uri) {
        viewModelScope.launch {
            try {
                repository.updateFounderAvatarFromUri(uri)
                refreshStorageSize()
                _snackbarMessage.value = "تم حفظ صورة الأستاذ باسل أشرف الفقعاوي محلياً"
            } catch (e: Exception) {
                _snackbarMessage.value = "تعذّر تحديث الصورة: ${e.localizedMessage ?: "خطأ"}"
            }
        }
    }

    fun toggleModuleCompleted(course: CourseEntity, module: ModuleEntity, currentlyCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleModuleCompleted(course, module, currentlyCompleted)
            if (!currentlyCompleted) {
                _snackbarMessage.value = "تم إتمام «${module.title}» (+25 XP)"
            }
        }
    }

    fun updateModulePlaybackPosition(
        course: CourseEntity,
        module: ModuleEntity,
        positionMs: Long,
        durationMs: Long,
    ) {
        viewModelScope.launch {
            repository.updateModulePlaybackPosition(course, module, positionMs, durationMs)
        }
    }

    fun attachLocalVideoToModule(module: ModuleEntity, uri: Uri) {
        viewModelScope.launch {
            try {
                repository.attachVideoToModule(module, localUri = uri, externalUrl = null)
                refreshStorageSize()
                _snackbarMessage.value = "تم حفظ الفيديو محلياً وربطه بالوحدة (+10 XP)"
            } catch (e: Exception) {
                _snackbarMessage.value = "تعذّر حفظ الفيديو: ${e.localizedMessage ?: "خطأ"}"
            }
        }
    }

    fun attachVideoUrlToModule(module: ModuleEntity, url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            repository.attachVideoToModule(module, localUri = null, externalUrl = url)
            _snackbarMessage.value = "تم ربط رابط الفيديو بالوحدة (+10 XP)"
        }
    }

    fun updateCourseProgress(course: CourseEntity, progress: Int) {
        viewModelScope.launch {
            repository.updateCourseProgress(course, progress)
            if (progress >= 100 && !course.isCompleted) {
                _snackbarMessage.value = "أحسنت! تم إتمام الدرس واكتساب +50 XP"
            }
        }
    }

    fun toggleCourseComplete(course: CourseEntity) {
        viewModelScope.launch {
            val willComplete = !course.isCompleted
            repository.toggleCourseCompleted(course)
            if (willComplete) {
                _snackbarMessage.value = "رائع! تم إكمال المسار وإضافة +50 XP"
            }
        }
    }

    fun toggleCourseBookmark(course: CourseEntity) {
        viewModelScope.launch {
            repository.toggleCourseBookmark(course)
        }
    }

    fun addCustomCourse(
        title: String,
        subtitle: String,
        category: String,
        description: String,
        lessonContent: String,
        durationMinutes: Int,
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addCustomCourse(
                title = title,
                subtitle = subtitle,
                category = category,
                description = description,
                lessonContent = lessonContent,
                durationMinutes = durationMinutes,
            )
            _snackbarMessage.value = "تم حفظ الدرس الجديد في قاعدة البيانات المحلية (+20 XP)"
        }
    }

    fun deleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            if (_selectedCourseId.value == course.id) {
                _selectedModuleId.value = null
                _selectedCourseId.value = null
            }
            _snackbarMessage.value = "تم حذف الدرس من التخزين المحلي"
        }
    }

    fun importLocalMedia(
        uri: Uri,
        title: String,
        category: String,
        description: String,
    ) {
        viewModelScope.launch {
            try {
                val saved = repository.importLocalMediaFromUri(uri, title, category, description)
                refreshStorageSize()
                _snackbarMessage.value = "تم حفظ «${saved.title}» محلياً على جهازك (+15 XP)"
            } catch (e: Exception) {
                _snackbarMessage.value = "تعذّر حفظ الملف: ${e.localizedMessage ?: "خطأ غير متوقع"}"
            }
        }
    }

    fun addExternalMediaLink(
        title: String,
        url: String,
        kind: MediaKind,
        category: String,
        description: String,
    ) {
        if (title.isBlank() || url.isBlank()) return
        viewModelScope.launch {
            repository.addExternalMediaLink(title, url, kind, category, description)
            _snackbarMessage.value = "تمت إضافة رابط الوسائط إلى مكتبتك المحلية (+10 XP)"
        }
    }

    fun toggleMediaBookmark(item: MediaAssetEntity) {
        viewModelScope.launch {
            repository.toggleMediaBookmark(item)
        }
    }

    fun deleteMedia(item: MediaAssetEntity) {
        viewModelScope.launch {
            repository.deleteMedia(item)
            refreshStorageSize()
            _snackbarMessage.value = "تم حذف الوسائط وتحرير المساحة المحلية"
        }
    }

    fun saveNote(
        existingId: Long? = null,
        title: String,
        content: String,
        tag: String,
        accentKey: String,
        attachedUri: Uri? = null,
        existingMediaUri: String = "",
        existingMediaType: String = "",
    ) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            try {
                repository.saveNote(
                    existingId = existingId,
                    title = title.ifBlank { "ملاحظة سريعة" },
                    content = content,
                    tag = tag,
                    accentKey = accentKey,
                    attachedUri = attachedUri,
                    existingMediaUri = existingMediaUri,
                    existingMediaType = existingMediaType,
                )
                refreshStorageSize()
                _snackbarMessage.value = if (existingId == null || existingId == 0L) {
                    "تم حفظ الملاحظة محلياً (+15 XP)"
                } else {
                    "تم تحديث الملاحظة"
                }
            } catch (e: Exception) {
                _snackbarMessage.value = "تعذّر حفظ المرفق: ${e.localizedMessage ?: "خطأ"}"
            }
        }
    }

    fun toggleNotePin(note: StudyNoteEntity) {
        viewModelScope.launch {
            repository.toggleNotePin(note)
        }
    }

    fun deleteNote(note: StudyNoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
            refreshStorageSize()
            _snackbarMessage.value = "تم حذف الملاحظة"
        }
    }

    fun completeFocusSession() {
        viewModelScope.launch {
            repository.rewardFocusSession()
            _snackbarMessage.value = "أحسنت! اكتملت جلسة التركيز وأُضيف +15 XP"
        }
    }
}
