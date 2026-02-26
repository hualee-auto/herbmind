package ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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

private val DarkColorScheme = darkColorScheme(
    primary = HerbColors.BambooGreenLight,
    onPrimary = HerbColors.InkBlack,
    primaryContainer = HerbColors.BambooGreenDark,
    onPrimaryContainer = HerbColors.BambooGreenPale,
    secondary = HerbColors.OchreLight,
    onSecondary = HerbColors.InkBlack,
    secondaryContainer = HerbColors.OchreDark,
    onSecondaryContainer = HerbColors.OchreLight,
    background = HerbColors.InkBlack,
    onBackground = HerbColors.RicePaper,
    surface = HerbColors.InkDark,
    onSurface = HerbColors.RicePaper,
    surfaceVariant = HerbColors.InkGray,
    onSurfaceVariant = HerbColors.InkLight,
    outline = HerbColors.InkGray,
    error = HerbColors.CinnabarLight,
    onError = HerbColors.InkBlack
)

@Composable
fun HerbMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
