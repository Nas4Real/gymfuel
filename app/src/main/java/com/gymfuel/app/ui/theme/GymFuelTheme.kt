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
    primary = Color(0xFFB7F397),
    onPrimary = Color(0xFF0C2413),
    primaryContainer = Color(0xFF203C2A),
    onPrimaryContainer = Color(0xFFD9FFD7),
    secondary = Color(0xFF84D6A3),
    onSecondary = Color(0xFF092416),
    secondaryContainer = Color(0xFF284634),
    onSecondaryContainer = Color(0xFFD9FFE5),
    tertiary = Color(0xFFF0C66E),
    onTertiary = Color(0xFF2A2000),
    tertiaryContainer = Color(0xFF4C3A0D),
    onTertiaryContainer = Color(0xFFFFE7A3),
    background = Color(0xFF090D0A),
    onBackground = Color(0xFFF2F7F2),
    surface = Color(0xFF101612),
    onSurface = Color(0xFFF2F7F2),
    surfaceVariant = Color(0xFF18231B),
    onSurfaceVariant = Color(0xFFA9B7AC),
    outline = Color(0xFF314238),
    error = Color(0xFFFFB4AB),
)

@Immutable
data class GymFuelSemanticColors(
    val protein: Color,
    val carbohydrate: Color,
    val fat: Color,
    val calories: Color,
    val success: Color,
    val warning: Color,
)

private val LightSemanticColors = GymFuelSemanticColors(
    protein = Color(0xFF257A52),
    carbohydrate = Color(0xFFC88A16),
    fat = Color(0xFFD66B55),
    calories = Color(0xFF26352D),
    success = Color(0xFF2F7D4C),
    warning = Color(0xFFA96B00),
)

private val DarkSemanticColors = GymFuelSemanticColors(
    protein = Color(0xFF8EE3B0),
    carbohydrate = Color(0xFFF0C66E),
    fat = Color(0xFFFFA58F),
    calories = Color(0xFFF2F7F2),
    success = Color(0xFF84D6A3),
    warning = Color(0xFFF0C66E),
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
