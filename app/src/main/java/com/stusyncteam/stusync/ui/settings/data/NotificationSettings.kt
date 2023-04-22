package com.stusyncteam.stusync.ui.settings.data

data class NotificationSettings(
    var shouldNotifyOnScheduleChanges: Boolean = false,
    var shouldNotifyBeforeNextLessonStarts: Boolean = false
)
