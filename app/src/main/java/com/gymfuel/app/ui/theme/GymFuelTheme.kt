package com.gymfuel.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Typography
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

private val Paper = Color(0xFFF7F7F2)
private val CleanPaper = Color(0xFFFFFFFF)
private val MossWash = Color(0xFFE9EEE8)
private val Forest = Color(0xFF173B2B)
private val Ink = Color(0xFF17211B)
private val FieldGray = Color(0xFF5E6962)
private val Grid = Color(0xFFCBD3CD)
private val Brick = Color(0xFFB3261E)

private val DeepInk = Color(0xFF101713)
private val ForestBlack = Color(0xFF18211C)
private val RaisedMoss = Color(0xFF233129)
private val Mint = Color(0xFF9ED7B3)
private val PaperText = Color(0xFFF1F5F1)
private val SageGray = Color(0xFFAAB7AE)
private val DarkGrid = Color(0xFF3B4A40)

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
    protein = Color(0xFF71D39B),
    carbohydrate = Color(0xFFF2C66D),
    fat = Color(0xFFFFAB95),
    calories = Color(0xFFDDE7E0),
    success = Color(0xFF6FD394),
    warning = Color(0xFFF3C36A),
)

private val LocalGymFuelSemanticColors = staticCompositionLocalOf {
    LightSemanticColors
}

object GymFuelTokens {
    val colors: GymFuelSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalGymFuelSemanticColors.current
}

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = CleanPaper,
    primaryContainer = MossWash,
    onPrimaryContainer = Ink,
    background = Paper,
    onBackground = Ink,
    surface = CleanPaper,
    onSurface = Ink,
    surfaceVariant = MossWash,
    onSurfaceVariant = FieldGray,
    outline = Grid,
    error = Brick,
)

private val DarkColors = darkColorScheme(
    primary = Mint,
    onPrimary = Color(0xFF092416),
    primaryContainer = RaisedMoss,
    onPrimaryContainer = PaperText,
    background = DeepInk,
    onBackground = PaperText,
    surface = ForestBlack,
    onSurface = PaperText,
    surfaceVariant = RaisedMoss,
    onSurfaceVariant = SageGray,
    outline = DarkGrid,
    error = Color(0xFFFFB4AB),
)

private val SpaceGrotesk = FontFamily(
    Font(R.font.space_grotesk_variable, FontWeight.Normal),
    Font(R.font.space_grotesk_variable, FontWeight.SemiBold),
    Font(R.font.space_grotesk_variable, FontWeight.Bold),
)

val JetBrainsMono = FontFamily(
    Font(R.font.jetbrains_mono_variable, FontWeight.Normal),
    Font(R.font.jetbrains_mono_variable, FontWeight.SemiBold),
)

private val GymFuelTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 21.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
)

private val GymFuelShapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(8.dp),
    large = RoundedCornerShape(12.dp),
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
    darkTheme: Boolean = isSystemInDarkTheme(),
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
