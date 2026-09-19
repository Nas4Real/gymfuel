package com.gymfuel.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gymfuel.app.R

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E5FD1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE9FF),
    onPrimaryContainer = Color(0xFF0A2759),
    secondary = Color(0xFF365F91),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD9E8FF),
    onSecondaryContainer = Color(0xFF112A47),
    background = Color(0xFFF4F7FB),
    onBackground = Color(0xFF111827),
    surface = Color.White,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFE9EEF6),
    onSurfaceVariant = Color(0xFF4C5668),
    outline = Color(0xFFC5CEDC),
    error = Color(0xFFBA1A1A),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4D8DFF),
    onPrimary = Color(0xFF071A38),
    primaryContainer = Color(0xFF142D58),
    onPrimaryContainer = Color(0xFFD5E3FF),
    secondary = Color(0xFF8EB7FF),
    onSecondary = Color(0xFF071A38),
    secondaryContainer = Color(0xFF1C2D49),
    onSecondaryContainer = Color(0xFFDCE8FF),
    tertiary = Color(0xFFE0B329),
    onTertiary = Color(0xFF211A00),
    tertiaryContainer = Color(0xFF3B300B),
    onTertiaryContainer = Color(0xFFFFE69A),
    background = Color(0xFF05070A),
    onBackground = Color(0xFFF4F7FB),
    surface = Color(0xFF10141B),
    onSurface = Color(0xFFF4F7FB),
    surfaceVariant = Color(0xFF171C26),
    onSurfaceVariant = Color(0xFFB8C0CE),
    outline = Color(0xFF30394A),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF50101B),
    onErrorContainer = Color(0xFFFFD9DF),
)

@Immutable
data class GymFuelSemanticColors(
    val protein: Color,
    val carbohydrate: Color,
    val fat: Color,
    val water: Color,
    val calories: Color,
    val success: Color,
    val warning: Color,
)

private val LightSemanticColors = GymFuelSemanticColors(
    protein = Color(0xFF147A43),
    carbohydrate = Color(0xFF8A6500),
    fat = Color(0xFFB4232D),
    water = Color(0xFF1E5FD1),
    calories = Color(0xFF1E293B),
    success = Color(0xFF147A43),
    warning = Color(0xFF7A5700),
)

private val DarkSemanticColors = GymFuelSemanticColors(
    protein = Color(0xFF43C679),
    carbohydrate = Color(0xFFF0B429),
    fat = Color(0xFFFF625F),
    water = Color(0xFF4D8DFF),
    calories = Color(0xFFE8EDF5),
    success = Color(0xFF43C679),
    warning = Color(0xFFF0B429),
)

private val LocalGymFuelSemanticColors = staticCompositionLocalOf {
    DarkSemanticColors
}

object GymFuelTokens {
    val colors: GymFuelSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGymFuelSemanticColors.current
}

private val Poppins = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_semibold, FontWeight.SemiBold),
    Font(R.font.poppins_bold, FontWeight.Bold),
)

private fun poppinsStyle(
    fontSize: Int,
    lineHeight: Int,
    fontWeight: FontWeight = FontWeight.Normal,
) = TextStyle(
    fontFamily = Poppins,
    fontWeight = fontWeight,
    fontSize = fontSize.sp,
    lineHeight = lineHeight.sp,
)

private val GymFuelTypography = Typography(
    displayLarge = poppinsStyle(36, 42, FontWeight.Bold),
    displayMedium = poppinsStyle(32, 38, FontWeight.Bold),
    displaySmall = poppinsStyle(28, 34, FontWeight.SemiBold),
    headlineLarge = poppinsStyle(26, 32, FontWeight.SemiBold),
    headlineMedium = poppinsStyle(22, 28, FontWeight.SemiBold),
    headlineSmall = poppinsStyle(20, 26, FontWeight.SemiBold),
    titleLarge = poppinsStyle(18, 24, FontWeight.SemiBold),
    titleMedium = poppinsStyle(16, 22, FontWeight.SemiBold),
    titleSmall = poppinsStyle(14, 20, FontWeight.SemiBold),
    bodyLarge = poppinsStyle(15, 22),
    bodyMedium = poppinsStyle(13, 19),
    bodySmall = poppinsStyle(12, 17),
    labelLarge = poppinsStyle(14, 20, FontWeight.SemiBold),
    labelMedium = poppinsStyle(12, 17, FontWeight.SemiBold),
    labelSmall = poppinsStyle(11, 16, FontWeight.SemiBold),
)

private val GymFuelShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(28.dp),
)

object GymFuelSpacing {
    val xSmall = 4.dp
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val page = 20.dp
    val xLarge = 24.dp
    val xxLarge = 32.dp
    val minimumTouchTarget = 48.dp
}

@Composable
fun GymFuelTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalGymFuelSemanticColors provides
            if (darkTheme) DarkSemanticColors else LightSemanticColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = GymFuelTypography,
            shapes = GymFuelShapes,
            content = content,
        )
    }
}
