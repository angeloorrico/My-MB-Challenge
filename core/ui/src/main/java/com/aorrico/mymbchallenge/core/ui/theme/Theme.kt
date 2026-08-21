package com.aorrico.mymbchallenge.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColors = lightColorScheme(
    primary = Blue40,
    onPrimary = Neutral99,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,
    secondary = Amber40,
    onSecondary = Neutral99,
    secondaryContainer = Amber90,
    onSecondaryContainer = Blue10,
    error = Red40,
    onError = Neutral99,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = Neutral99,
    onBackground = Neutral10,
    surface = Neutral99,
    onSurface = Neutral10,
    surfaceVariant = Neutral95,
    onSurfaceVariant = NeutralVariant30,
    outline = NeutralVariant50,
)

private val DarkColors = darkColorScheme(
    primary = Blue80,
    onPrimary = Blue20,
    primaryContainer = Blue20,
    onPrimaryContainer = Blue90,
    secondary = Amber80,
    onSecondary = Amber40,
    secondaryContainer = Amber40,
    onSecondaryContainer = Amber90,
    error = Red80,
    onError = Red10,
    errorContainer = Red40,
    onErrorContainer = Red90,
    background = Neutral10,
    onBackground = Neutral90,
    surface = Neutral10,
    onSurface = Neutral90,
    surfaceVariant = Neutral20,
    onSurfaceVariant = NeutralVariant80,
    outline = NeutralVariant50,
)

@Composable
fun MyMbChallengeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MbTypography,
        content = content,
    )
}
