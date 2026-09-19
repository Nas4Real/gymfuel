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
    primary = Color(0xFF173B2B),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE9EEE8),
    onPrimaryContainer = Color(0xFF17211B),
    secondary = Color(0xFF3B7251),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDCECE1),
    onSecondaryContainer = Color(0xFF173B2B),
    background = Color(0xFFF7F7F2),
    onBackground = Color(0xFF17211B),
    surface = Color.White,
    onSurface = Color(0xFF17211B),
    surfaceVariant = Color(0xFFE9EEE8),
    onSurfaceVariant = Color(0xFF5E6962),
    outline = Color(0xFFCBD3CD),
    error = Color(0xFFB3261E),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF0E6A2A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF102B18),
    onPrimaryContainer = Color(0xFFB7EAC5),
    secondary = Color(0xFF013B95),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0B1D39),
    onSecondaryContainer = Color(0xFFC2D8FF),
    tertiary = Color(0xFF8A6500),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF2C240C),
    onTertiaryContainer = Color(0xFFF4D77C),
    background = Color(0xFF000000),
    onBackground = Color(0xFFF6F6F3),
    surface = Color(0xFF0B0B0B),
    onSurface = Color(0xFFF6F6F3),
    surfaceVariant = Color(0xFF151515),
    onSurfaceVariant = Color(0xFFAAAAA5),
    outline = Color(0xFF2A2A2A),
    error = Color(0xFF950101),
    onError = Color.White,
    errorContainer = Color(0xFF310B0B),
    onErrorContainer = Color(0xFFFFC7C7),
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
    protein = Color(0xFF257A52),
    carbohydrate = Color(0xFFC88A16),
    fat = Color(0xFFD66B55),
    water = Color(0xFF013B95),
    calories = Color(0xFF26352D),
    success = Color(0xFF2F7D4C),
    warning = Color(0xFFA96B00),
)

private val DarkSemanticColors = GymFuelSemanticColors(
    protein = Color(0xFF0E6A2A),
    carbohydrate = Color(0xFF8A6500),
    fat = Color(0xFF950101),
    water = Color(0xFF013B95),
    calories = Color(0xFFF6F6F3),
    success = Color(0xFF0E6A2A),
    warning = Color(0xFF8A6500),
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
