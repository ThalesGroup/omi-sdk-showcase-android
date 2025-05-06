package com.onewelcome.core.theme

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color

fun Color.darken(@FloatRange(from = 0.0, to = 1.0) factor: Float) =
  Color(
    (red * factor).coerceIn(0.0f, 1.0f),
    (green * factor).coerceIn(0.0f, 1.0f),
    (blue * factor).coerceIn(0.0f, 1.0f)
  )

fun List<String>.separateItemsWithComa() = this.joinToString(", ")