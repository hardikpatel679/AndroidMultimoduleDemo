package com.hdapp.core.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Dimens(
    val default: Dp = 0.dp,
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val buttonHeight: Dp = 56.dp,
    val iconSize: Dp = 24.dp,
    val spacerHeight: Dp = 16.dp,
    val paddingLarge: Dp = 24.dp,
    val cornerRadius: Dp = 12.dp
)

val LocalDimens = compositionLocalOf { Dimens() }
