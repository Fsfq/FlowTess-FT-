package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val IndigoPrimary = Color(0xFFD0BCFF)
val RedPrimary = Color(0xFFFF5555)
val EmeraldPrimary = Color(0xFF4ADE80)
val MintPrimary = Color(0xFF81E6D9)
val GoldPrimary = Color(0xFFF6AD55)

@Composable
fun MyApplicationTheme(
  themeName: String = "indigo",
  fontKey: String = "default",
  content: @Composable () -> Unit,
) {
  val fontFamily = when (fontKey) {
    "monospace" -> FontFamily.Monospace
    "serif" -> FontFamily.Serif
    "sans-serif" -> FontFamily.SansSerif
    "cursive" -> FontFamily.Cursive
    "condensed" -> FontFamily(android.graphics.Typeface.create("sans-serif-condensed", android.graphics.Typeface.NORMAL))
    "black" -> FontFamily(android.graphics.Typeface.create("sans-serif-black", android.graphics.Typeface.NORMAL))
    "thin" -> FontFamily(android.graphics.Typeface.create("sans-serif-thin", android.graphics.Typeface.NORMAL))
    else -> FontFamily.Default
  }

  val selectedColorScheme = when (themeName) {
    "red" -> darkColorScheme(
      primary = Color(0xFFFFB4AB),
      onPrimary = Color(0xFF690005),
      primaryContainer = Color(0xFF93000A),
      onPrimaryContainer = Color(0xFFFFDAD6),
      secondary = Color(0xFFE7BDB7),
      onSecondary = Color(0xFF442A26),
      secondaryContainer = Color(0xFF5D403C),
      onSecondaryContainer = Color(0xFFFFDAD6),
      tertiary = Color(0xFFDEC48C),
      onTertiary = Color(0xFF3E2E04),
      tertiaryContainer = Color(0xFF564419),
      onTertiaryContainer = Color(0xFFFBDFA6),
      background = Color(0xFF0F0E0E),
      onBackground = Color(0xFFEDE0DE),
      surface = Color(0xFF1A1110),
      onSurface = Color(0xFFEDE0DE),
      surfaceVariant = Color(0xFF2B1F1E),
      onSurfaceVariant = Color(0xFFD8C2BF),
      outline = Color(0xFFA08C8A),
      outlineVariant = Color(0xFF534341)
    )
    "emerald" -> darkColorScheme(
      primary = Color(0xFF6CDBAC),
      onPrimary = Color(0xFF003823),
      primaryContainer = Color(0xFF005235),
      onPrimaryContainer = Color(0xFF89F8C7),
      secondary = Color(0xFFB3CCBF),
      onSecondary = Color(0xFF1E352C),
      secondaryContainer = Color(0xFF354C41),
      onSecondaryContainer = Color(0xFFCFE8DB),
      tertiary = Color(0xFFA5CDDC),
      onTertiary = Color(0xFF073541),
      tertiaryContainer = Color(0xFF244C59),
      onTertiaryContainer = Color(0xFFC0E9F9),
      background = Color(0xFF0C0F0D),
      onBackground = Color(0xFFE1E3DF),
      surface = Color(0xFF131A16),
      onSurface = Color(0xFFE1E3DF),
      surfaceVariant = Color(0xFF222B26),
      onSurfaceVariant = Color(0xFFC0C9C2),
      outline = Color(0xFF8A938C),
      outlineVariant = Color(0xFF404943)
    )
    "mint" -> darkColorScheme(
      primary = Color(0xFF80E8DD),
      onPrimary = Color(0xFF003732),
      primaryContainer = Color(0xFF005049),
      onPrimaryContainer = Color(0xFF9DF5EA),
      secondary = Color(0xFFB0CCC8),
      onSecondary = Color(0xFF1E3532),
      secondaryContainer = Color(0xFF324B48),
      onSecondaryContainer = Color(0xFFCCE8E4),
      tertiary = Color(0xFFAFD0E8),
      onTertiary = Color(0xFF17334B),
      tertiaryContainer = Color(0xFF2F4A63),
      onTertiaryContainer = Color(0xFFCBE6FF),
      background = Color(0xFF0C0F0E),
      onBackground = Color(0xFFE0E3E1),
      surface = Color(0xFF121B1A),
      onSurface = Color(0xFFE0E3E1),
      surfaceVariant = Color(0xFF212C2A),
      onSurfaceVariant = Color(0xFFC0C9C7),
      outline = Color(0xFF8A9391),
      outlineVariant = Color(0xFF404947)
    )
    "gold" -> darkColorScheme(
      primary = Color(0xFFFFB866),
      onPrimary = Color(0xFF4B2800),
      primaryContainer = Color(0xFF6B3B00),
      onPrimaryContainer = Color(0xFFFFDDB8),
      secondary = Color(0xFFDFBFAF),
      onSecondary = Color(0xFF3F2B20),
      secondaryContainer = Color(0xFF573E30),
      onSecondaryContainer = Color(0xFFFBDCD0),
      tertiary = Color(0xFFCCD2A3),
      onTertiary = Color(0xFF333517),
      tertiaryContainer = Color(0xFF494B2C),
      onTertiaryContainer = Color(0xFFE8EEBD),
      background = Color(0xFF0F0E0C),
      onBackground = Color(0xFFECE1DB),
      surface = Color(0xFF1A1410),
      onSurface = Color(0xFFECE1DB),
      surfaceVariant = Color(0xFF2E241E),
      onSurfaceVariant = Color(0xFFD6C3B9),
      outline = Color(0xFF9F8D84),
      outlineVariant = Color(0xFF52443D)
    )
    "neon" -> darkColorScheme(
      primary = Color(0xFF00FFCC),
      onPrimary = Color(0xFF00372A),
      primaryContainer = Color(0xFF00503F),
      onPrimaryContainer = Color(0xFF9DF5E2),
      secondary = Color(0xFFB0CCC5),
      onSecondary = Color(0xFF1E352F),
      secondaryContainer = Color(0xFF324B44),
      onSecondaryContainer = Color(0xFFCCE8E0),
      tertiary = Color(0xFFAFD0E8),
      onTertiary = Color(0xFF17334B),
      background = Color(0xFF0B1412),
      onBackground = Color(0xFFE0E3E2),
      surface = Color(0xFF101C1A),
      onSurface = Color(0xFFE0E3E2),
      surfaceVariant = Color(0xFF212C2A),
      onSurfaceVariant = Color(0xFFC0C9C7),
      outline = Color(0xFF8A9391),
      outlineVariant = Color(0xFF404947)
    )
    "amber" -> darkColorScheme(
      primary = Color(0xFFFFB300),
      onPrimary = Color(0xFF402D00),
      primaryContainer = Color(0xFF5B4000),
      onPrimaryContainer = Color(0xFFFFE094),
      secondary = Color(0xFFD9C3A0),
      onSecondary = Color(0xFF3C2E15),
      secondaryContainer = Color(0xFF54442A),
      onSecondaryContainer = Color(0xFFF6DFBB),
      tertiary = Color(0xFFB4CC9A),
      onTertiary = Color(0xFF233511),
      background = Color(0xFF14120B),
      onBackground = Color(0xFFE9E2D8),
      surface = Color(0xFF1A1710),
      onSurface = Color(0xFFE9E2D8),
      surfaceVariant = Color(0xFF2E2A20),
      onSurfaceVariant = Color(0xFFD0C6B5),
      outline = Color(0xFF999081),
      outlineVariant = Color(0xFF4C463A)
    )
    "rose" -> darkColorScheme(
      primary = Color(0xFFF43F5E),
      onPrimary = Color(0xFF4F0014),
      primaryContainer = Color(0xFF720022),
      onPrimaryContainer = Color(0xFFFFD9DF),
      secondary = Color(0xFFE4BDC2),
      onSecondary = Color(0xFF42292D),
      secondaryContainer = Color(0xFF5B3F43),
      onSecondaryContainer = Color(0xFFFFD9DF),
      tertiary = Color(0xFFDFBE9B),
      onTertiary = Color(0xFF3F2913),
      background = Color(0xFF140B0C),
      onBackground = Color(0xFFECE0E1),
      surface = Color(0xFF1C1011),
      onSurface = Color(0xFFECE0E1),
      surfaceVariant = Color(0xFF2D2021),
      onSurfaceVariant = Color(0xFFD5C2C4),
      outline = Color(0xFF9E8E90),
      outlineVariant = Color(0xFF514345)
    )
    "sky" -> darkColorScheme(
      primary = Color(0xFF0EA5E9),
      onPrimary = Color(0xFF003550),
      primaryContainer = Color(0xFF004D72),
      onPrimaryContainer = Color(0xFFCBE6FF),
      secondary = Color(0xFFBAC8DB),
      onSecondary = Color(0xFF243140),
      secondaryContainer = Color(0xFF3A4858),
      onSecondaryContainer = Color(0xFFD6E4F7),
      tertiary = Color(0xFFD3BEDF),
      onTertiary = Color(0xFF392943),
      background = Color(0xFF0B1216),
      onBackground = Color(0xFFE1E2E5),
      surface = Color(0xFF101B20),
      onSurface = Color(0xFFE1E2E5),
      surfaceVariant = Color(0xFF202A30),
      onSurfaceVariant = Color(0xFFC0C7CD),
      outline = Color(0xFF8B9297),
      outlineVariant = Color(0xFF40474C)
    )
    "orange" -> darkColorScheme(
      primary = Color(0xFFFF5722),
      onPrimary = Color(0xFF511000),
      primaryContainer = Color(0xFF731D00),
      onPrimaryContainer = Color(0xFFFFDBCE),
      secondary = Color(0xFFE5BDB0),
      onSecondary = Color(0xFF442A22),
      secondaryContainer = Color(0xFF5D4038),
      onSecondaryContainer = Color(0xFFFFDBCE),
      tertiary = Color(0xFFDEC38B),
      onTertiary = Color(0xFF3E2D05),
      background = Color(0xFF140F0B),
      onBackground = Color(0xFFEFE0DC),
      surface = Color(0xFF1C1410),
      onSurface = Color(0xFFEFE0DC),
      surfaceVariant = Color(0xFF2E221E),
      onSurfaceVariant = Color(0xFFD8C2BB),
      outline = Color(0xFF9F8E89),
      outlineVariant = Color(0xFF53433E)
    )
    "cyber_pink" -> darkColorScheme(
      primary = Color(0xFFFF007F),
      onPrimary = Color(0xFF510022),
      primaryContainer = Color(0xFF740034),
      onPrimaryContainer = Color(0xFFFFD9E4),
      secondary = Color(0xFFE4BDCB),
      onSecondary = Color(0xFF432934),
      secondaryContainer = Color(0xFF5A3F4B),
      onSecondaryContainer = Color(0xFFFFD9E4),
      tertiary = Color(0xFFDFC0A5),
      onTertiary = Color(0xFF3E2E1A),
      background = Color(0xFF140B10),
      onBackground = Color(0xFFEBE0E3),
      surface = Color(0xFF1C1017),
      onSurface = Color(0xFFEBE0E3),
      surfaceVariant = Color(0xFF2D2028),
      onSurfaceVariant = Color(0xFFD5C2C9),
      outline = Color(0xFF9E8E93),
      outlineVariant = Color(0xFF514348)
    )
    "toxic_green" -> darkColorScheme(
      primary = Color(0xFF39FF14),
      onPrimary = Color(0xFF003901),
      primaryContainer = Color(0xFF005304),
      onPrimaryContainer = Color(0xFF89FF7D),
      secondary = Color(0xFFBCCBB9),
      onSecondary = Color(0xFF263525),
      secondaryContainer = Color(0xFF3C4C3A),
      onSecondaryContainer = Color(0xFFD8E7D5),
      tertiary = Color(0xFFA5D8D3),
      onTertiary = Color(0xFF0A3734),
      background = Color(0xFF0C140B),
      onBackground = Color(0xFFE2ECE1),
      surface = Color(0xFF121C11),
      onSurface = Color(0xFFE2ECE1),
      surfaceVariant = Color(0xFF222D21),
      onSurfaceVariant = Color(0xFFC2CBC1),
      outline = Color(0xFF8C958B),
      outlineVariant = Color(0xFF424A41)
    )
    else -> darkColorScheme(
      primary = Color(0xFFD0BCFF),
      onPrimary = Color(0xFF381E72),
      primaryContainer = Color(0xFF4F378B),
      onPrimaryContainer = Color(0xFFEADDFF),
      secondary = Color(0xFFCCC2DC),
      onSecondary = Color(0xFF332D41),
      secondaryContainer = Color(0xFF4A4458),
      onSecondaryContainer = Color(0xFFE8DEF8),
      tertiary = Color(0xFFEFB8C8),
      onTertiary = Color(0xFF492532),
      tertiaryContainer = Color(0xFF633B48),
      onTertiaryContainer = Color(0xFFFFD8E4),
      background = Color(0xFF0F0E13),
      onBackground = Color(0xFFE6E1E5),
      surface = Color(0xFF1B1A1F),
      onSurface = Color(0xFFE6E1E5),
      surfaceVariant = Color(0xFF28262E),
      onSurfaceVariant = Color(0xFFCAC4D0),
      outline = Color(0xFF938F99),
      outlineVariant = Color(0xFF49454F)
    )
  }

  val customTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 57.sp, lineHeight = 64.sp, letterSpacing = (-0.25).sp),
    displayMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 45.sp, lineHeight = 52.sp, letterSpacing = 0.sp),
    displaySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 36.sp, lineHeight = 44.sp, letterSpacing = 0.sp),
    headlineLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 32.sp, lineHeight = 40.sp, letterSpacing = 0.sp),
    headlineMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 36.sp, letterSpacing = 0.sp),
    headlineSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 32.sp, letterSpacing = 0.sp),
    titleLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp),
    titleMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp),
    titleSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    bodyLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.5.sp),
    bodyMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.25.sp),
    bodySmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.4.sp),
    labelLarge = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp),
    labelSmall = TextStyle(fontFamily = fontFamily, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp)
  )

  MaterialTheme(
    colorScheme = selectedColorScheme,
    typography = customTypography,
    content = content
  )
}
