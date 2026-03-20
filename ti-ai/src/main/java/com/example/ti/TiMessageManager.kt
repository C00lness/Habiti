package com.example.ti

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TiMessageManager {
    private val _message = MutableStateFlow<TiMessage?>(null)
    val message: StateFlow<TiMessage?> = _message.asStateFlow()

    fun showMessage(text: String, type: TiMessageType = TiMessageType.INFO) {
        _message.value = TiMessage(text, type)
    }

    fun clearMessage() {
        _message.value = null
    }
}