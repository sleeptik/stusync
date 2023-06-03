package com.stusyncteam.stusync.storage.stats

import java.util.Date

data class SyncStats(
    var lastSync: Date = Date(),
    var totalCreatedEvents: Int = 0,
    var totalModifiedEvents: Int = 0,
    var totalDeletedEvents: Int = 0
)
