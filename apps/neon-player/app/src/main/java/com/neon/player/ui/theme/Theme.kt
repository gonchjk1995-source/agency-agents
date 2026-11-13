package com.neon.player.ui.theme

import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neon.player.R

object Neon {
    val Void = Color(0xFF04050D)
    val Abyss = Color(0xFF0B0D24)
    val Deep = Color(0xFF140A33)
    val Panel = Color(0xCC0D1030)
    val Cyan = Color(0xFF00F0FF)
    val Magenta = Color(0xFFFF2BD6)
    val Violet = Color(0xFF8A2BFF)
    val Amber = Color(0xFFFFB443)
    val TextHi = Color(0xFFE8F7FF)
    val TextLo = Color(0xFF8590BA)

    val Chamfer = CutCornerShape(topStart = 16.dp, bottomEnd = 16.dp)
    val ChamferSmall = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)
}

/** Latin-only display face: logo, digits, HUD labels. */
val Orbitron = FontFamily(
    Font(R.font.orbitron, FontWeight.Normal),
    Font(R.font.orbitron, FontWeight.Medium),
    Font(R.font.orbitron, FontWeight.Bold),
    Font(R.font.orbitron, FontWeight.Black),
)

/** Techy face with full Cyrillic for everything the user reads. */
val Exo2 = FontFamily(
    Font(R.font.exo2, FontWeight.Normal),
    Font(R.font.exo2, FontWeight.Medium),
    Font(R.font.exo2, FontWeight.SemiBold),
    Font(R.font.exo2, FontWeight.Bold),
    Font(R.font.exo2, FontWeight.ExtraBold),
)

fun TextStyle.glow(color: Color, radius: Float = 18f) =
    copy(shadow = Shadow(color = color, offset = Offset.Zero, blurRadius = radius))

val Hud = TextStyle(
    fontFamily = Orbitron,
    fontWeight = FontWeight.Medium,
    fontSize = 10.sp,
    letterSpacing = 3.sp,
    color = Neon.TextLo,
)

private val base = Typography()

private val NeonTypography = Typography(
    headlineMedium = base.headlineMedium.copy(fontFamily = Exo2, fontWeight = FontWeight.ExtraBold),
    headlineSmall = base.headlineSmall.copy(fontFamily = Exo2, fontWeight = FontWeight.Bold),
    titleLarge = base.titleLarge.copy(fontFamily = Exo2, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = Exo2, fontWeight = FontWeight.SemiBold),
    titleSmall = base.titleSmall.copy(fontFamily = Exo2, fontWeight = FontWeight.SemiBold),
    bodyLarge = base.bodyLarge.copy(fontFamily = Exo2),
    bodyMedium = base.bodyMedium.copy(fontFamily = Exo2),
    bodySmall = base.bodySmall.copy(fontFamily = Exo2),
    labelLarge = base.labelLarge.copy(fontFamily = Exo2, fontWeight = FontWeight.Bold, letterSpacing = 2.sp),
    labelMedium = base.labelMedium.copy(fontFamily = Exo2, fontWeight = FontWeight.SemiBold),
    labelSmall = base.labelSmall.copy(fontFamily = Exo2),
)

@Composable
fun NeonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Neon.Cyan,
            onPrimary = Neon.Void,
            secondary = Neon.Magenta,
            onSecondary = Neon.Void,
            tertiary = Neon.Violet,
            background = Neon.Void,
            onBackground = Neon.TextHi,
            surface = Neon.Abyss,
            onSurface = Neon.TextHi,
            onSurfaceVariant = Neon.TextLo,
        ),
        typography = NeonTypography,
        content = content,
    )
}
