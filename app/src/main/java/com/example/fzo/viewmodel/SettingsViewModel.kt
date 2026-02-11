package com.example.fzo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.fzo.data.AppSettings
import com.example.fzo.data.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() {
    private val _settings = MutableStateFlow(repo.load())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    fun setAutoPlayAll(value: Boolean) {
        update { it.copy(autoPlayAll = value) }
    }

    fun setShuffleEnabled(value: Boolean) {
        update { it.copy(shuffleEnabled = value) }
    }

    fun setVolume(value: Float) {
        update { it.copy(volume = value) }
    }

    private fun update(block: (AppSettings) -> AppSettings) {
        val newSettings = block(_settings.value)
        _settings.value = newSettings
        repo.save(newSettings)
    }
}

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SettingsViewModel(SettingsRepository(context.applicationContext)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
