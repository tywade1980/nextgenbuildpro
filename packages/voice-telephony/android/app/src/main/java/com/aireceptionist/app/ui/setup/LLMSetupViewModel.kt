package com.aireceptionist.app.ui.setup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aireceptionist.app.ai.llm.ModelDownloader
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ViewModel for LLM setup process
 */
@HiltViewModel
class LLMSetupViewModel @Inject constructor() : ViewModel() {
    
    private val _setupState = MutableLiveData<LLMSetupState>()
    val setupState: LiveData<LLMSetupState> = _setupState
    
    private val _downloadProgress = MutableLiveData<ModelDownloader.DownloadProgress>()
    val downloadProgress: LiveData<ModelDownloader.DownloadProgress> = _downloadProgress
    
    fun setSetupState(state: LLMSetupState) {
        _setupState.value = state
    }
    
    fun setDownloadProgress(progress: ModelDownloader.DownloadProgress) {
        _downloadProgress.value = progress
    }
}