package com.example.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class MediaKind(val labelAr: String) {
    VIDEO("فيديو"),
    AUDIO("صوت"),
    IMAGE("صورة"),
    DOCUMENT("مستند"),
}

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val category: String,
    val description: String,
    val lessonContent: String,
    val durationMinutes: Int,
    val progressPercent: Int = 0,
    val isCompleted: Boolean = false,
    val isBookmarked: Boolean = false,
    val mediaType: String = MediaKind.VIDEO.name,
    val mediaUri: String = "",
    val coverKey: String = "future_tech",
    val instructorName: String = "الأستاذ باسل الفقعاوي",
    val isUserCreated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "course_modules",
    foreignKeys = [
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index(value = ["courseId"])],
)
data class ModuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseId: Long,
    val orderIndex: Int,
    val title: String,
    val summary: String,
    val contentBody: String,
    val durationMinutes: Int,
    val mediaType: String = MediaKind.DOCUMENT.name,
    val mediaUri: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "module_progress",
    primaryKeys = ["moduleId"],
    foreignKeys = [
        ForeignKey(
            entity = ModuleEntity::class,
            parentColumns = ["id"],
            childColumns = ["moduleId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = CourseEntity::class,
            parentColumns = ["id"],
            childColumns = ["courseId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["courseId"]),
        Index(value = ["moduleId"]),
    ],
)
data class ModuleProgressEntity(
    val moduleId: Long,
    val courseId: Long,
    val progressPercent: Int = 0,
    val isCompleted: Boolean = false,
    val lastPositionMs: Long = 0L,
    val completedAt: Long? = null,
    val updatedAt: Long = System.currentTimeMillis(),
)

data class ModuleWithProgress(
    @Embedded val module: ModuleEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "moduleId",
    )
    val progress: ModuleProgressEntity?,
)

data class CourseWithModules(
    @Embedded val course: CourseEntity,
    @Relation(
        entity = ModuleEntity::class,
        parentColumn = "id",
        entityColumn = "courseId",
    )
    val modules: List<ModuleWithProgress>,
) {
    val orderedModules: List<ModuleWithProgress>
        get() = modules.sortedBy { it.module.orderIndex }

    val computedProgressPercent: Int
        get() {
            if (modules.isEmpty()) return course.progressPercent
            val total = modules.sumOf { it.progress?.progressPercent ?: 0 }
            return (total / modules.size).coerceIn(0, 100)
        }

    val isAllModulesCompleted: Boolean
        get() = if (modules.isEmpty()) course.isCompleted else modules.all { it.progress?.isCompleted == true }
}

@Entity(tableName = "media_assets")
data class MediaAssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val mediaType: String,
    val uriString: String,
    val description: String,
    val fileSizeBytes: Long = 0L,
    val durationLabel: String = "",
    val isBookmarked: Boolean = false,
    val isBundled: Boolean = false,
    val isInternalFile: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val tag: String = "ملخص درس",
    val accentKey: String = "cyan",
    val attachedMediaUri: String = "",
    val attachedMediaType: String = "",
    val isPinned: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "learner_stats")
data class LearnerStatsEntity(
    @PrimaryKey val id: Int = 1,
    val learnerName: String = "متعلّم مبدع",
    val xpPoints: Int = 120,
    val streakDays: Int = 1,
    val completedLessons: Int = 0,
    val lastActiveDay: Long = System.currentTimeMillis() / 86_400_000L,
)
