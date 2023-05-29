package com.stusyncteam.stusync.storage.settings

data class NotificationSettings(
    var shouldNotifyOnScheduleChanges: Boolean = false,
    var shouldNotifyBeforeNextLessonStarts: Boolean = false
)
