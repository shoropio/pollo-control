package com.pollocontrol.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary — vibrant teal-blue
val BluePrimary = Color(0xFF0088CC)
val BlueDark = Color(0xFF006699)
val BlueLight = Color(0xFF33A1DE)
val BlueSurface = Color(0xFFE6F4FA)

// Accent — emerald & rose for fintech feel
val AccentGreen = Color(0xFF00C853)
val AccentGreenDark = Color(0xFF00A844)
val AccentRose = Color(0xFFFF4081)
val AccentAmber = Color(0xFFFFAB00)

// Status
val StatusPositive = AccentGreen
val StatusNegative = Color(0xFFFF5252)
val StatusWarning = AccentAmber
val StatusInfo = BlueLight

// --- DARK THEME (premium fintech default) ---
val DarkBg = Color(0xFF0D1117)
val DarkSurface = Color(0xFF161B22)
val DarkSurface2 = Color(0xFF1C2128)
val DarkBorder = Color(0xFF30363D)
val DarkOnBg = Color(0xFFF0F6FC)
val DarkOnSurface = Color(0xFFF0F6FC)
val DarkOnSurfaceVariant = Color(0xFF8B949E)
val DarkCardBg = Color(0xFF161B22)
val DarkNavBg = Color(0xFF0D1117)

// --- LIGHT THEME ---
val LightBg = Color(0xFFF6F8FA)
val LightSurface = Color(0xFFFFFFFF)
val LightSurface2 = Color(0xFFF0F2F5)
val LightBorder = Color(0xFFD0D7DE)
val LightOnBg = Color(0xFF1C2128)
val LightOnSurface = Color(0xFF1C2128)
val LightOnSurfaceVariant = Color(0xFF656D76)
val LightCardBg = Color(0xFFFFFFFF)

// Legacy aliases (backward compat)
val SuccessGreen = StatusPositive
val ErrorRed = StatusNegative
val WarningOrange = StatusWarning
val InfoBlue = StatusInfo
val CoralAccent = AccentRose
val EmeraldAccent = AccentGreen
val SlatePrimary = Color(0xFF6C757D)
val SlateLight = Color(0xFFADB5BD)
// Material 3 light color scheme
val md_theme_light_primary = BluePrimary
val md_theme_light_onPrimary = Color(0xFFFFFFFF)
val md_theme_light_primaryContainer = Color(0xFFCEECFF)
val md_theme_light_onPrimaryContainer = Color(0xFF001F33)
val md_theme_light_secondary = Color(0xFF4D6475)
val md_theme_light_onSecondary = Color(0xFFFFFFFF)
val md_theme_light_secondaryContainer = Color(0xFFD0E9FF)
val md_theme_light_onSecondaryContainer = Color(0xFF071E2B)
val md_theme_light_tertiary = Color(0xFFF54A7A)
val md_theme_light_onTertiary = Color(0xFFFFFFFF)
val md_theme_light_background = LightBg
val md_theme_light_onBackground = LightOnBg
val md_theme_light_surface = LightSurface
val md_theme_light_onSurface = LightOnSurface
val md_theme_light_surfaceVariant = LightSurface2
val md_theme_light_onSurfaceVariant = LightOnSurfaceVariant
val md_theme_light_outline = LightBorder
val md_theme_light_error = Color(0xFFD92D20)
val md_theme_light_onError = Color(0xFFFFFFFF)
val md_theme_light_errorContainer = Color(0xFFFFE5E5)
val md_theme_light_onErrorContainer = Color(0xFF410002)

// Material 3 dark color scheme
val md_theme_dark_primary = BlueLight
val md_theme_dark_onPrimary = Color(0xFF003549)
val md_theme_dark_primaryContainer = Color(0xFF004C6D)
val md_theme_dark_onPrimaryContainer = BlueSurface
val md_theme_dark_secondary = Color(0xFFA6C8DD)
val md_theme_dark_onSecondary = Color(0xFF0E3347)
val md_theme_dark_secondaryContainer = Color(0xFF2B4A5E)
val md_theme_dark_onSecondaryContainer = Color(0xFFD0E9FF)
val md_theme_dark_tertiary = Color(0xFFFF99B3)
val md_theme_dark_onTertiary = Color(0xFF5F0026)
val md_theme_dark_background = DarkBg
val md_theme_dark_onBackground = DarkOnBg
val md_theme_dark_surface = DarkSurface
val md_theme_dark_onSurface = DarkOnSurface
val md_theme_dark_surfaceVariant = DarkSurface2
val md_theme_dark_onSurfaceVariant = DarkOnSurfaceVariant
val md_theme_dark_outline = DarkBorder
val md_theme_dark_error = Color(0xFFFFB4AB)
val md_theme_dark_onError = Color(0xFF690005)
val md_theme_dark_errorContainer = Color(0xFF93000A)
val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
