package com.herbmind.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = HerbColors.BambooGreen,
    onPrimary = HerbColors.PureWhite,
    primaryContainer = HerbColors.BambooGreenPale,
    onPrimaryContainer = HerbColors.BambooGreenDark,
    secondary = HerbColors.Ochre,
    onSecondary = HerbColors.PureWhite,
    secondaryContainer = HerbColors.RicePaperDark,
    onSecondaryContainer = HerbColors.OchreDark,
    background = HerbColors.RicePaper,
    onBackground = HerbColors.InkBlack,
    surface = HerbColors.PureWhite,
    onSurface = HerbColors.InkBlack,
    surfaceVariant = HerbColors.RicePaperDark,
    onSurfaceVariant = HerbColors.InkGray,
    outline = HerbColors.BorderLight,
    error = HerbColors.Cinnabar,
    onError = HerbColors.PureWhite
)

@Composable
fun HerbMindTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
