package com.stusyncteam.stusync.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.stusyncteam.stusync.storage.stats.SyncStats

class SyncStatsViewModel : ViewModel() {
    val syncStats by lazy { MutableLiveData<SyncStats>() }
}