package com.pollocontrol.app.data.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsManager(context: Context) {
    private val preferences = context.getSharedPreferences("pollo_control_settings", Context.MODE_PRIVATE)

    private val _currency = MutableStateFlow(
        AppCurrency.fromCode(preferences.getString(KEY_CURRENCY, null))
    )
    val currency: StateFlow<AppCurrency> = _currency.asStateFlow()

    fun setCurrency(currency: AppCurrency) {
        preferences.edit().putString(KEY_CURRENCY, currency.code).apply()
        _currency.value = currency
    }

    companion object {
        private const val KEY_CURRENCY = "currency"
    }
}

enum class AppCurrency(
    val code: String,
    val displayName: String
) {
    MXN("MXN", "Peso Mexicano"),
    CRC("CRC", "Colon Costarricense"),
    USD("USD", "Dolar Estadounidense");

    val label: String = "$displayName ($code)"

    companion object {
        fun fromCode(code: String?): AppCurrency =
            entries.firstOrNull { it.code == code } ?: CRC
    }
}
