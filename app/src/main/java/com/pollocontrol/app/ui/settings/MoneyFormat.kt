package com.pollocontrol.app.ui.settings

import com.pollocontrol.app.data.settings.AppCurrency
import java.util.Locale

fun formatMoney(amount: Double, currency: AppCurrency, decimals: Int = 2): String =
    "${currency.code} ${String.format(Locale.US, "%,.${decimals}f", amount)}"
