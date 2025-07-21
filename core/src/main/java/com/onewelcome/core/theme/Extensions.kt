package com.onewelcome.core.theme

import androidx.annotation.FloatRange
import androidx.compose.ui.graphics.Color
import com.onewelcome.core.util.Constants

fun Color.darken(@FloatRange(from = 0.0, to = 1.0) factor: Float) =
  Color(
    (red * factor).coerceIn(0.0f, 1.0f),
    (green * factor).coerceIn(0.0f, 1.0f),
    (blue * factor).coerceIn(0.0f, 1.0f)
  )

fun List<String>.separateItemsWithComa() = this.joinToString(", ")

fun String?.isNotFullScreenRoute(): Boolean = this?.contains(Constants.FULLSCREEN_PAGE) == false
