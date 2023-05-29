package com.stusyncteam.stusync.storage.stats

import java.util.Date

data class SyncStats(
    var lastSync: Date,
    var totalCreatedEvents: Int,
    var totalModifiedEvents: Int,
    var totalDeletedEvents: Int
)
