package com.example.ui

import androidx.compose.material.icons.filled.AutoAwesome

import androidx.compose.animation.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.MainViewModel
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Check
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Portrait
import androidx.compose.material.icons.filled.WorkspacePremium
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import android.content.Context
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    initialTab: Int = 0,
    onBack: () -> Unit
) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val activeThemeKey by viewModel.themeColor.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()
    val playerName by viewModel.playerName.collectAsStateWithLifecycle()
    val hasNicknameGradient by viewModel.hasNicknameGradient.collectAsStateWithLifecycle()
    val onlineTier by viewModel.onlineTier.collectAsStateWithLifecycle()
    val nicknameUpdateError by viewModel.nicknameUpdateError.collectAsStateWithLifecycle()
    val nicknameUpdateSuccess by viewModel.nicknameUpdateSuccess.collectAsStateWithLifecycle()
    val emailUpdateError by viewModel.emailUpdateError.collectAsStateWithLifecycle()
    val emailUpdateSuccess by viewModel.emailUpdateSuccess.collectAsStateWithLifecycle()
    val boardSkin by viewModel.boardColorSkin.collectAsStateWithLifecycle()
    
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    val loginSuccessMessage by viewModel.loginSuccessMessage.collectAsStateWithLifecycle()

    val vibrationEnabled by viewModel.vibrationEnabled.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.soundEnabled.collectAsStateWithLifecycle()
    val ghostVisible by viewModel.ghostVisible.collectAsStateWithLifecycle()
    val smoothFallingEnabled by viewModel.smoothFallingEnabled.collectAsStateWithLifecycle()
    val gridOpacity by viewModel.gridOpacity.collectAsStateWithLifecycle()
    val autoSaveHighscore by viewModel.autoSaveHighscore.collectAsStateWithLifecycle()
    val leftHandedControls by viewModel.leftHandedControls.collectAsStateWithLifecycle()
    val controlButtonStyle by viewModel.controlButtonStyle.collectAsStateWithLifecycle()
    val gridLineDensity by viewModel.gridLineDensity.collectAsStateWithLifecycle()
    val screenShakeIntensity by viewModel.screenShakeIntensity.collectAsStateWithLifecycle()
    val scanlinesFilter by viewModel.scanlinesFilter.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    
    val themeColor = MaterialTheme.colorScheme.primary

    var authModeIsRegister by remember { mutableStateOf(false) }
    var inputUsername by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }

    var infoMessage by remember { mutableStateOf<String?>(null) }
    var infoIsError by remember { mutableStateOf(false) }
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }

    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()
    val isEmailVerified by viewModel.isEmailVerified.collectAsStateWithLifecycle()
    val showVerificationBanner by viewModel.showVerificationBanner.collectAsStateWithLifecycle()
    val showNewSection by viewModel.showNewSection.collectAsStateWithLifecycle()

    val context = androidx.compose.ui.platform.LocalContext.current
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("509266226335-m7n4s1m9a9q2otjsm4vh6r552nheomgl.apps.googleusercontent.com")
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    viewModel.signInWithGoogle(idToken)
                }
            } catch (e: ApiException) {
                e.printStackTrace()
            }
        }
    }

    fun triggerMessage(msg: String, isError: Boolean = false) {
        infoMessage = msg
        infoIsError = isError
        coroutineScope.launch {
            delay(3000)
            if (infoMessage == msg) {
                infoMessage = null
            }
        }
    }

    val ranksList = listOf(
        RankData("BRONZE", 0, if (currentLang == Language.RU) "Начальный ранг игрока" else "Starting player rank"),
        RankData("SILVER", 1500, if (currentLang == Language.RU) "Серебряная лига игрока" else "Silver tier member"),
        RankData("GOLD", 3000, if (currentLang == Language.RU) "Золотая лига опытных бойцов" else "Gold league experienced tier"),
        RankData("PLATINUM", 4500, if (currentLang == Language.RU) "Платиновый мастер комбинаций" else "Platinum combination master"),
        RankData("DIAMOND", 7500, if (currentLang == Language.RU) "Алмазная лига консенсуса" else "Diamond consensus league"),
        RankData("MASTER", 12000, if (currentLang == Language.RU) "Магистр пространственной сетки" else "Master of spatial matrix grid"),
        RankData("GRANDMASTER", 16500, if (currentLang == Language.RU) "Гроссмейстер тактических дуэлей" else "Grandmaster of tactical gameplay"),
        RankData("CHALLENGER", 24000, if (currentLang == Language.RU) "Легенда абсолютного топа" else "Legendary top challenger status")
    )

    val skinsList = listOf(
        SkinData("cyberpunk", 0, "NEON VIOLET", if (currentLang == Language.RU) "Стандартный неоново-фиолетовый стиль" else "Standard neon purple high-contrast grid"),
        SkinData("retro_amber", 300, "AMBER GOLD", if (currentLang == Language.RU) "Классический янтарный терминальный монохром" else "Vintage monochrome amber-orange layout"),
        SkinData("emerald_matrix", 400, "MATRIX GREEN", if (currentLang == Language.RU) "Высокотехнологичный зеленый шифр" else "Deep cyberspace digital green code rain"),
        SkinData("vaporwave_pink", 600, "VAPORWAVE PINK", if (currentLang == Language.RU) "Розово-пурпурный закат" else "Synthwave dream sunset accents"),
        SkinData("midnight_gold", 1000, "MIDNIGHT GOLD", if (currentLang == Language.RU) "Элитный золотой глянец на темном фоне" else "Premium carbon black with gold details"),
        SkinData("carbon_neutral", 1200, "TITAN SLATE", if (currentLang == Language.RU) "Промышленный матовый титановый сплав" else "Industrial brushed graphite metal texture"),
        SkinData("plasma_storm", 1500, "PLASMA BLAST", if (currentLang == Language.RU) "Импульсное плазменное поле со вспышками" else "High-energy plasma static interference overlay"),
        SkinData("glacial_frost", 1850, "GLACIAL ZERO", if (currentLang == Language.RU) "Кристальная текстура глубокого арктического льда" else "Sub-zero deep tundra thermal blue crystal design")
    )

    val cubeSkinsList = listOf(
        CubeSkinStoreData("neon", 0, if (currentLang == Language.RU) "ГИПЕР НЕОН" else "HYPER NEON", if (currentLang == Language.RU) "Яркие неоновые грани с белым контуром" else "Vibrant neon edges with custom white glow lines", ""),
        CubeSkinStoreData("glass", 200, if (currentLang == Language.RU) "ГЛАССМОРФИЗМ" else "GLASSMORPHISM", if (currentLang == Language.RU) "Стеклянные плитки с эффектом матового размытия" else "Frosted tinted space glass with back-glare", ""),
        CubeSkinStoreData("retro", 300, if (currentLang == Language.RU) "РЕТРО-КОНЦЕНТРИК" else "CONCENTRIC RETRO", if (currentLang == Language.RU) "Ретро-стиль контрастных узоров" else "Concentric retro console styles from the 80s", ""),
        CubeSkinStoreData("flat", 400, if (currentLang == Language.RU) "ПРОСТОЙ ПЛОСКИЙ" else "MINIMAL FLAT", if (currentLang == Language.RU) "Минималистичный чистый плоский стиль блоков" else "Sleek low-footprint solid layout with sharp edges", ""),
        CubeSkinStoreData("material", 500, if (currentLang == Language.RU) "ANDROID MATERIAL 3" else "ANDROID MATERIAL 3", if (currentLang == Language.RU) "Скругленные блоки Material со сложным градиентом" else "Organic rounded Material Design custom 3D tiles", ""),
        CubeSkinStoreData("glowing_jewel", 700, if (currentLang == Language.RU) "ДРАГОЦЕННЫЙ САПФИР" else "GLOWING GEMSTONE", if (currentLang == Language.RU) "Ограненные сапфировые плиты с внутренним свечением" else "Chiseled luxury jewel design with internal raytracing", ""),
        CubeSkinStoreData("steampunk", 900, if (currentLang == Language.RU) "СТИМПАНК И МЕДЬ" else "STEAM_BRASS", if (currentLang == Language.RU) "Тяжелые латунные блоки с шестеренками и заклепками" else "Heavy brass gears and rivets industrial aesthetic", "")
    )

    val fontsList = listOf(
        FontStoreData("default", 0, if (currentLang == Language.RU) "Системный" else "System default", if (currentLang == Language.RU) "Классический шрифт системы" else "Default clean sans-serif typeface"),
        FontStoreData("monospace", 0, if (currentLang == Language.RU) "Консоль" else "Terminal Monospace", if (currentLang == Language.RU) "Ретро консольный моноширинный" else "Concentric terminal styling grid font"),
        FontStoreData("serif", 200, if (currentLang == Language.RU) "Элегантный засечки" else "Sleek Serif", if (currentLang == Language.RU) "Книжный стиль с засечками" else "Elegant book style typeface with serifs"),
        FontStoreData("sans-serif", 300, if (currentLang == Language.RU) "Космический" else "Space Clean Sans", if (currentLang == Language.RU) "Геометрический чистый без засечек" else "Sleek modern geometric clean layout font"),
        FontStoreData("cursive", 500, if (currentLang == Language.RU) "Пиксельный" else "Arcade Cursive", if (currentLang == Language.RU) "Игровой пиксельный ретро-стиль" else "Retro 8-bit cursive pixel art style font"),
        FontStoreData("condensed", 400, if (currentLang == Language.RU) "Кибер Сжатый" else "Cyberpunk Condensed", if (currentLang == Language.RU) "Плотный киберпанк-шрифт" else "High-density cyber condensed text style"),
        FontStoreData("black", 600, if (currentLang == Language.RU) "Тяжелый Титан" else "Heavy Titan Black", if (currentLang == Language.RU) "Супер-жирный толстый шрифт" else "Max weight industrial black presentation font"),
        FontStoreData("thin", 450, if (currentLang == Language.RU) "Минимал Тонкий" else "Sleek Thin", if (currentLang == Language.RU) "Сверхлегкий утонченный минимализм" else "Ultra light modern space minimal aesthetic")
    )

    val controlButtonStylesList = listOf(
        ControlButtonStyleStoreData("classic", 0, if (currentLang == Language.RU) "Классический" else "Classic Solid", if (currentLang == Language.RU) "Стандартный заполненный стиль" else "Solid material-design buttons with shadow"),
        ControlButtonStyleStoreData("neon", 0, if (currentLang == Language.RU) "Неоновое свечение" else "Neon Glow", if (currentLang == Language.RU) "Кнопки с неоновым контуром" else "Glowing neon border with transparent background"),
        ControlButtonStyleStoreData("glass", 600, if (currentLang == Language.RU) "Матовое стекло" else "Frosted Glass", if (currentLang == Language.RU) "Эффект полупрозрачного стекла" else "Semi-transparent modern glassmorphic look")
    )

    val blockStyle by viewModel.blockStyle.collectAsStateWithLifecycle()
    val customFontKey by viewModel.customFontKey.collectAsStateWithLifecycle()
    val statsClearedLines by viewModel.statsClearedLines.collectAsStateWithLifecycle()
    val statsHighScore by viewModel.statsHighScore.collectAsStateWithLifecycle()

    val sharedPrefs = remember {
        viewModel.getApplication<android.app.Application>()
            .getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
    }

    val equippedAvatarFrame by viewModel.equippedAvatarFrame.collectAsStateWithLifecycle()
    val purchasedAvatarFrames by viewModel.purchasedAvatarFrames.collectAsStateWithLifecycle()

    val equippedTitle by viewModel.equippedTitle.collectAsStateWithLifecycle()
    val purchasedTitles by viewModel.purchasedTitles.collectAsStateWithLifecycle()

    val equippedSoundPack by viewModel.equippedSoundPack.collectAsStateWithLifecycle()
    val purchasedSoundPacks by viewModel.purchasedSoundPacks.collectAsStateWithLifecycle()

    val purchasedThemes by viewModel.purchasedThemes.collectAsStateWithLifecycle()
    val purchasedFonts by viewModel.purchasedFonts.collectAsStateWithLifecycle()
    val purchasedControlButtonStyles by viewModel.purchasedControlButtonStyles.collectAsStateWithLifecycle()
    val customTagUnlocked by viewModel.customTagUnlocked.collectAsStateWithLifecycle()
    val customTag by viewModel.customTag.collectAsStateWithLifecycle()

    val customAvatarEmoji by viewModel.customAvatarEmoji.collectAsStateWithLifecycle()
    val customAvatarBgColor by viewModel.customAvatarBgColor.collectAsStateWithLifecycle()

    val infiniteTransition = rememberInfiniteTransition(label = "AvatarFrameGlow")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RotationAngle"
    )

    var avatarChangeCounter by remember { mutableStateOf(0) }
    var bgChangeCounter by remember { mutableStateOf(0) }

    var customAvatarBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var bgBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    LaunchedEffect(playerName, avatarChangeCounter) {
        val bmp = withContext(Dispatchers.IO) {
            val file = File(context.filesDir, "custom_avatar_${playerName}.jpg")
            if (file.exists() && sharedPrefs.getBoolean("has_custom_avatar_${playerName}", false)) {
                try {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
        customAvatarBitmap = bmp
    }

    LaunchedEffect(playerName, bgChangeCounter) {
        val bmp = withContext(Dispatchers.IO) {
            val file = File(context.filesDir, "custom_background_${playerName}.jpg")
            if (file.exists() && sharedPrefs.getBoolean("has_custom_background_${playerName}", false)) {
                try {
                    BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } catch (e: Exception) {
                    null
                }
            } else {
                null
            }
        }
        bgBitmap = bmp
    }

    fun saveCustomImage(ctx: Context, uri: Uri, type: String) {
        try {
            val inputStream = ctx.contentResolver.openInputStream(uri) ?: return
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            if (originalBitmap == null) return

            val maxDim = if (type == "avatar") 256 else 1024
            val width = originalBitmap.width
            val height = originalBitmap.height
            val scaledBitmap = if (width > maxDim || height > maxDim) {
                val ratio = width.toFloat() / height.toFloat()
                val newWidth = if (ratio > 1) maxDim else (maxDim * ratio).toInt()
                val newHeight = if (ratio > 1) (maxDim / ratio).toInt() else maxDim
                Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            } else {
                originalBitmap
            }

            val file = File(ctx.filesDir, "custom_${type}_${playerName}.jpg")
            val outputStream = FileOutputStream(file)
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()

            if (type == "avatar") {
                sharedPrefs.edit().putBoolean("has_custom_avatar_${playerName}", true).apply()
                avatarChangeCounter++
            } else {
                sharedPrefs.edit().putBoolean("has_custom_background_${playerName}", true).apply()
                bgChangeCounter++
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { saveCustomImage(context, it, "avatar") }
    }

    val bgPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { saveCustomImage(context, it, "background") }
    }

    val secondaryColor = MaterialTheme.colorScheme.secondary
    val avatarFrameBorderBrush = remember(equippedAvatarFrame, themeColor, secondaryColor) {
        when (equippedAvatarFrame) {
            "neon_ae" -> Brush.sweepGradient(listOf(Color(0xFF00FFCC), Color(0xFF0099FF), Color(0xFF00FFCC)))
            "gold_ma" -> Brush.sweepGradient(listOf(Color(0xFFFFD700), Color(0xFFFFA500), Color(0xFFFFD700)))
            "chrono_gl" -> Brush.sweepGradient(listOf(Color(0xFFFF0055), Color(0xFFFF5252), Color(0xFFFF7A00), Color(0xFFFF0055)))
            "omega_ti" -> Brush.linearGradient(listOf(Color(0xFF90A4AE), Color(0xFF37474F)))
            else -> Brush.sweepGradient(
                listOf(
                    themeColor,
                    secondaryColor,
                    themeColor
                )
            )
        }
    }
    val avatarFrameThickness = when (equippedAvatarFrame) {
        "neon_ae", "gold_ma", "chrono_gl" -> 3.5.dp
        "omega_ti" -> 5.dp
        else -> 2.dp
    }


    var userId by remember {
        val stored = sharedPrefs.getString("profile_user_id", "") ?: ""
        val resolved = if (stored.isEmpty()) {
            val generated = "ID-${(100000..999999).random()}"
            sharedPrefs.edit().putString("profile_user_id", generated).apply()
            generated
        } else {
            stored
        }
        mutableStateOf(resolved)
    }
    var showAvatarDialog by remember { mutableStateOf(false) }

    val purchasedCubeSkinsSet = remember(blockStyle, credits) {
        sharedPrefs.getStringSet("purchased_cube_skins", setOf("neon")) ?: setOf("neon")
    }

    fun purchaseCubeSkin(styleId: String, cost: Int) {
        if (purchasedCubeSkinsSet.contains(styleId)) {
            viewModel.setBlockStyle(styleId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            triggerMessage(if (currentLang == Language.RU) "Оформление блоков успешно применено" else "Cube style equipped.")
            return
        }
        if (credits >= cost) {
            val updated = purchasedCubeSkinsSet.toMutableSet().apply { add(styleId) }
            sharedPrefs.edit().putStringSet("purchased_cube_skins", updated).apply()
            viewModel.spendCredits(cost)
            viewModel.setBlockStyle(styleId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            triggerMessage(if (currentLang == Language.RU) "Стиль блоков успешно приобретен и применен" else "Cube style purchased and equipped.")
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            triggerMessage(if (currentLang == Language.RU) "Недостаточно средств" else "Insufficient funds", isError = true)
        }
    }

    val purchasedSkinsSet = remember(boardSkin, credits) {
        sharedPrefs.getStringSet("purchased_skins", setOf("cyberpunk")) ?: setOf("cyberpunk")
    }

    val premiumModesList = listOf(
        GameModeStoreData("zen", 400, if (currentLang == Language.RU) "Дзен" else "Zen", if (currentLang == Language.RU) "Бесконечный режим: автоматическое очищение поля при переполнении, отключено поражение." else "Endless game mode: clears the board on overflow, defeat is disabled.", ""),
        GameModeStoreData("pulse_extreme", 800, if (currentLang == Language.RU) "Вихрь" else "Vortex Pulse", if (currentLang == Language.RU) "Повышенная сложность: каждые 4 установленные фигуры снизу поля добавляется случайная заполненная линия." else "Increased difficulty: a random garbage line is added at the bottom every 4 placed pieces.", ""),
        GameModeStoreData("mirror", 1000, if (currentLang == Language.RU) "Зеркальный режим" else "Mirror Mode", if (currentLang == Language.RU) "Игровое поле зеркально отражается по горизонтальной оси во время игрового процесса." else "The game field is mirrored horizontally during gameplay.", ""),
        GameModeStoreData("penta", 1200, if (currentLang == Language.RU) "Пентатрис" else "Pentatris", if (currentLang == Language.RU) "Режим повышенной сложности: все падающие фигуры состоят из пяти блоков." else "High difficulty mode: all falling pieces consist of five blocks.", "")
    )

    val purchasedModesSet = remember(credits) {
        sharedPrefs.getStringSet("purchased_modes", setOf("classic", "extended", "fast_run", "reverse", "block_blast")) 
            ?: setOf("classic", "extended", "fast_run", "reverse", "block_blast")
    }

    fun purchaseMode(modeId: String, cost: Int) {
        if (purchasedModesSet.contains(modeId)) {
            triggerMessage(if (currentLang == Language.RU) "Режим уже разблокирован" else "Game mode already unlocked")
            return
        }
        if (credits >= cost) {
            val updated = purchasedModesSet.toMutableSet().apply { add(modeId) }
            sharedPrefs.edit().putStringSet("purchased_modes", updated).apply()
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            triggerMessage(if (currentLang == Language.RU) "Премиум-режим разблокирован" else "Premium game mode unlocked successfully.")
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            triggerMessage(if (currentLang == Language.RU) "Недостаточно средств" else "Insufficient funds", isError = true)
        }
    }

    fun purchaseSkin(skinId: String, cost: Int) {
        if (purchasedSkinsSet.contains(skinId)) {
            viewModel.setBoardColorSkin(skinId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            triggerMessage(if (currentLang == Language.RU) "Оформление успешно применилось" else "Grid scheme equipped.")
            return
        }
        if (credits >= cost) {
            val updated = purchasedSkinsSet.toMutableSet().apply { add(skinId) }
            sharedPrefs.edit().putStringSet("purchased_skins", updated).apply()
            viewModel.spendCredits(cost)
            viewModel.setBoardColorSkin(skinId)
            viewModel.unlockAchievement("color_skin_collector", 200)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            triggerMessage(if (currentLang == Language.RU) "Оформление успешно приобретено и применилось" else "Skin purchased and equipped.")
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            triggerMessage(if (currentLang == Language.RU) "Недостаточно средств" else "Insufficient funds", isError = true)
        }
    }

    fun purchaseRank(rankId: String, cost: Int) {
        val rankIndex = ranksList.indexOfFirst { it.id == rankId }
        val currentRankIndex = ranksList.indexOfFirst { it.id == onlineTier }
        if (rankIndex <= currentRankIndex) {
            triggerMessage(if (currentLang == Language.RU) "Этот ранг уже разблокирован!" else "This rank is already unlocked.")
            return
        }
        if (rankIndex > currentRankIndex + 1) {
            triggerMessage(if (currentLang == Language.RU) "Нужно купить предыдущий ранг!" else "Unlock previous rank first.", isError = true)
            return
        }
        if (credits >= cost) {
            viewModel.spendCredits(cost)
            viewModel.setOnlineTier(rankId)
            viewModel.unlockAchievement("rank_conqueror", 350)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            triggerMessage(if (currentLang == Language.RU) "Ранг успешно повышен" else "Rank updated to $rankId successfully.")
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            triggerMessage(if (currentLang == Language.RU) "Недостаточно средств для повышения" else "Insufficient funds for rank raise.", isError = true)
        }
    }

    fun selectAvatarFrame(frameId: String, cost: Int) {
        if (purchasedAvatarFrames.contains(frameId)) {
            viewModel.setEquippedAvatarFrame(frameId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            triggerMessage(if (currentLang == Language.RU) "Рамка успешно выбрана" else "Avatar frame equipped.")
            return
        }
        if (credits >= cost) {
            val updated = purchasedAvatarFrames.toMutableSet().apply { add(frameId) }
            viewModel.setPurchasedAvatarFrames(updated)
            viewModel.setEquippedAvatarFrame(frameId)
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            triggerMessage(if (currentLang == Language.RU) "Рамка куплена и надета" else "Avatar frame purchased and equipped.")
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            triggerMessage(if (currentLang == Language.RU) "Недостаточно средств" else "Insufficient funds", isError = true)
        }
    }

    fun selectTitle(titleId: String, cost: Int) {
        if (purchasedTitles.contains(titleId)) {
            viewModel.setEquippedTitle(titleId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            triggerMessage(if (currentLang == Language.RU) "Титул успешно выбран" else "Title equipped.")
            return
        }
        if (credits >= cost) {
            val updated = purchasedTitles.toMutableSet().apply { add(titleId) }
            viewModel.setPurchasedTitles(updated)
            viewModel.setEquippedTitle(titleId)
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            triggerMessage(if (currentLang == Language.RU) "Титул куплен и активирован" else "Title purchased and equipped.")
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            triggerMessage(if (currentLang == Language.RU) "Недостаточно средств" else "Insufficient funds", isError = true)
        }
    }

    fun selectFont(fontId: String, cost: Int) {
        if (purchasedFonts.contains(fontId)) {
            viewModel.setCustomFontKey(fontId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            triggerMessage(if (currentLang == Language.RU) "Шрифт успешно выбран" else "Interface font equipped.")
            return
        }
        if (credits >= cost) {
            val updated = purchasedFonts.toMutableSet().apply { add(fontId) }
            viewModel.setPurchasedFonts(updated)
            viewModel.setCustomFontKey(fontId)
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            triggerMessage(if (currentLang == Language.RU) "Шрифт куплен и активирован" else "Interface font purchased and equipped.")
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            triggerMessage(if (currentLang == Language.RU) "Недостаточно средств" else "Insufficient funds", isError = true)
        }
    }

    fun selectControlButtonStyle(styleId: String, cost: Int) {
        if (purchasedControlButtonStyles.contains(styleId)) {
            viewModel.setControlButtonStyle(styleId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            triggerMessage(if (currentLang == Language.RU) "Стиль кнопок успешно выбран" else "Button style equipped.")
            return
        }
        if (credits >= cost) {
            val updated = purchasedControlButtonStyles.toMutableSet().apply { add(styleId) }
            viewModel.setPurchasedControlButtonStyles(updated)
            viewModel.setControlButtonStyle(styleId)
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            triggerMessage(if (currentLang == Language.RU) "Стиль кнопок куплен и активирован" else "Button style purchased and equipped.")
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            triggerMessage(if (currentLang == Language.RU) "Недостаточно средств" else "Insufficient funds", isError = true)
        }
    }


    val isLoggedIn = playerName != "Player 1"
    val pageCount = 3
    val pagerState = rememberPagerState(initialPage = if (initialTab == 99) 0 else initialTab.coerceAtMost(pageCount - 1)) { pageCount }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    AdaptiveText(
                        text = when (pagerState.currentPage) {
                            0 -> if (isLoggedIn) (if (currentLang == Language.RU) "ПРОФИЛЬ" else "PROFILE") else (if (currentLang == Language.RU) "АВТОРИЗАЦИЯ" else "AUTHORIZATION")
                            1 -> if (currentLang == Language.RU) "МАГАЗИН" else "STORE"
                            else -> if (currentLang == Language.RU) "ДОСТИЖЕНИЯ" else "ACHIEVEMENTS"
                        },
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                },
                actions = {
                    Surface(
                        modifier = Modifier.padding(end = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        tonalElevation = 2.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Credits",
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            AdaptiveText(
                                text = "$credits",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                val tabsList = listOf(
                    Triple(0, if (currentLang == Language.RU) "Профиль" else "Profile", Icons.Default.Person),
                    Triple(1, if (currentLang == Language.RU) "Магазин" else "Store", Icons.Default.ShoppingBag),
                    Triple(2, if (currentLang == Language.RU) "Достижения" else "Achievements", Icons.Default.EmojiEvents)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val tabWidth = maxWidth / tabsList.size
                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * pagerState.currentPage,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                            label = "tabIndicator"
                        )

                        // Smooth animated sliding pill
                        Box(
                            modifier = Modifier
                                .width(tabWidth)
                                .height(44.dp)
                                .offset(x = indicatorOffset)
                                .clip(RoundedCornerShape(18.dp))
                                .background(themeColor)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            tabsList.forEach { (index, title, icon) ->
                                val isSelected = pagerState.currentPage == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = title,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(17.dp)
                                        )
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    userScrollEnabled = true
                ) { page ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).let { if (it < 0) -it else it }
                                alpha = (1f - pageOffset * 0.45f).coerceIn(0f, 1f)
                                val s = (1f - pageOffset * 0.04f).coerceIn(0.92f, 1f)
                                scaleX = s
                                scaleY = s
                            }
                    ) {
                        when (page) {
                            0 -> {
                            if (!isLoggedIn) {
                            // REGISTRATION & AUTHORIZATION VIEW
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Column(
                                            modifier = Modifier
                                                .padding(horizontal = 24.dp, vertical = 28.dp)
                                                .verticalScroll(rememberScrollState())
                                                .animateContentSize(
                                                    animationSpec = spring(
                                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                                        stiffness = Spring.StiffnessLow
                                                    )
                                                ),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            // Title with crossfade
                                            androidx.compose.animation.AnimatedContent(
                                                targetState = authModeIsRegister,
                                                transitionSpec = {
                                                    fadeIn(animationSpec = tween(300)) togetherWith
                                                            fadeOut(animationSpec = tween(200))
                                                },
                                                label = "titleAnim"
                                            ) { isRegister ->
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = if (isRegister) {
                                                            if (currentLang == Language.RU) "РЕГИСТРАЦИЯ" else "REGISTRATION"
                                                        } else {
                                                            if (currentLang == Language.RU) "ВХОД В АККАУНТ" else "ACCOUNT LOGIN"
                                                        },
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = if (isRegister) {
                                                            if (currentLang == Language.RU) "Создайте профиль для сохранения статистики" else "Create a profile to save your stats"
                                                        } else {
                                                            if (currentLang == Language.RU) "Войдите для синхронизации прогресса" else "Sign in to sync your progress"
                                                        },
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        textAlign = TextAlign.Center,
                                                        modifier = Modifier.padding(horizontal = 8.dp)
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(22.dp))

                                            // Animated Segmented Control
                                            val primaryColor = MaterialTheme.colorScheme.primary
                                            val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
                                            val onSurfVariantColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(48.dp)
                                                    .clip(RoundedCornerShape(24.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                            ) {
                                                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                                                    val halfWidth = maxWidth / 2
                                                    val animOffset by animateDpAsState(
                                                        targetValue = if (!authModeIsRegister) 0.dp else halfWidth,
                                                        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
                                                        label = "segSlide"
                                                    )

                                                    // Sliding fill indicator
                                                    Box(
                                                        modifier = Modifier
                                                            .offset(x = animOffset + 3.dp)
                                                            .width(halfWidth - 6.dp)
                                                            .fillMaxHeight()
                                                            .padding(vertical = 3.dp)
                                                            .clip(RoundedCornerShape(21.dp))
                                                            .background(primaryColor)
                                                    )

                                                    Row(modifier = Modifier.fillMaxSize()) {
                                                        // Sign In tab
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxHeight()
                                                                .clip(RoundedCornerShape(24.dp))
                                                                .clickable {
                                                                    authModeIsRegister = false
                                                                    viewModel.clearLoginMessages()
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            val textColor by animateColorAsState(
                                                                targetValue = if (!authModeIsRegister) onPrimaryColor else onSurfVariantColor,
                                                                animationSpec = tween(250),
                                                                label = "signInColor"
                                                            )
                                                            Text(
                                                                text = if (currentLang == Language.RU) "ВХОД" else "SIGN IN",
                                                                style = MaterialTheme.typography.labelLarge,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = textColor
                                                            )
                                                        }

                                                        // Sign Up tab
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .fillMaxHeight()
                                                                .clip(RoundedCornerShape(24.dp))
                                                                .clickable {
                                                                    authModeIsRegister = true
                                                                    viewModel.clearLoginMessages()
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            val textColor by animateColorAsState(
                                                                targetValue = if (authModeIsRegister) onPrimaryColor else onSurfVariantColor,
                                                                animationSpec = tween(250),
                                                                label = "signUpColor"
                                                            )
                                                            Text(
                                                                text = if (currentLang == Language.RU) "РЕГИСТРАЦИЯ" else "SIGN UP",
                                                                style = MaterialTheme.typography.labelLarge,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = textColor
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(22.dp))

                                            // Input fields
                                            OutlinedTextField(
                                                value = inputUsername,
                                                onValueChange = { inputUsername = it },
                                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                                label = { Text(if (authModeIsRegister) (if (currentLang == Language.RU) "Никнейм" else "Nickname") else (if (currentLang == Language.RU) "Никнейм или Email" else "Nickname or Email")) },
                                                singleLine = true,
                                                shape = RoundedCornerShape(16.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            androidx.compose.animation.AnimatedVisibility(
                                                visible = authModeIsRegister,
                                                enter = fadeIn(tween(300)) + expandVertically(tween(300)),
                                                exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                                            ) {
                                                Column {
                                                    Spacer(modifier = Modifier.height(14.dp))
                                                    OutlinedTextField(
                                                        value = inputEmail,
                                                        onValueChange = { inputEmail = it },
                                                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                                        label = { Text(if (currentLang == Language.RU) "Электронная почта" else "Email Address") },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(16.dp),
                                                        colors = OutlinedTextFieldDefaults.colors(
                                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                                        ),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(14.dp))

                                            OutlinedTextField(
                                                value = inputPassword,
                                                onValueChange = { inputPassword = it },
                                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                                label = { Text(if (currentLang == Language.RU) "Пароль" else "Password") },
                                                singleLine = true,
                                                shape = RoundedCornerShape(16.dp),
                                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Spacer(modifier = Modifier.height(16.dp))

                                            // Error message
                                            loginError?.let { err ->
                                                val localizedErr = if (currentLang == Language.RU) {
                                                    when {
                                                        err.contains("empty") || err.contains("пустые") || err.contains("пустой") -> "Заполните все поля!"
                                                        err.contains("not found") || err.contains("найден") -> "Пользователь не найден!"
                                                        err.contains("wrong") || err.contains("пароль") -> "Неверный пароль!"
                                                        err.contains("already in use") || err.contains("занят") -> "Этот никнейм или почта уже используются!"
                                                        err.contains("weak") -> "Слишком простой пароль (минимум 6 символов)!"
                                                        err.contains("network") -> "Ошибка сети! Проверьте подключение."
                                                        else -> err
                                                    }
                                                } else err

                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = MaterialTheme.colorScheme.errorContainer
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = localizedErr,
                                                            color = MaterialTheme.colorScheme.onErrorContainer,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(14.dp))
                                            }

                                            // Success message
                                            loginSuccessMessage?.let { success ->
                                                val localizedSucc = if (currentLang == Language.RU) {
                                                    when {
                                                        success.contains("created") || success.contains("успешно создан") -> "Аккаунт успешно создан!"
                                                        success.contains("success") || success.contains("вход") -> "Успешный вход!"
                                                        else -> success
                                                    }
                                                } else success

                                                Surface(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = Color(0xFFE8F5E9)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = localizedSucc,
                                                            color = Color(0xFF1B5E20),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(14.dp))
                                            }

                                            // Email verification banner
                                            if (showVerificationBanner) {
                                                Column(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(14.dp))
                                                        .background(Color(0xFFFFF3E0))
                                                        .padding(14.dp)
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(20.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text(
                                                            text = if (currentLang == Language.RU) "Проверьте почту и подтвердите аккаунт!" else "Check your email and verify your account!",
                                                            color = Color(0xFFBF360C),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(10.dp))
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        OutlinedButton(
                                                            onClick = { viewModel.resendVerificationEmail() },
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(12.dp),
                                                            border = BorderStroke(1.dp, Color(0xFFE65100)),
                                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE65100))
                                                        ) {
                                                            Text(
                                                                text = if (currentLang == Language.RU) "Отправить" else "Resend",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                        Button(
                                                            onClick = { viewModel.checkEmailVerification() },
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(12.dp),
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                                                        ) {
                                                            Text(
                                                                text = if (currentLang == Language.RU) "Я подтвердил" else "I verified",
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(14.dp))
                                            }

                                            // Submit Button
                                            Button(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    if (authModeIsRegister) {
                                                        viewModel.registerAccount(inputUsername, inputEmail, inputPassword)
                                                    } else {
                                                        viewModel.loginAccount(inputUsername, inputPassword)
                                                    }
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            ) {
                                                Text(
                                                    text = if (authModeIsRegister) {
                                                        if (currentLang == Language.RU) "СОЗДАТЬ АККАУНТ" else "CREATE ACCOUNT"
                                                    } else {
                                                        if (currentLang == Language.RU) "ВОЙТИ В СИСТЕМУ" else "SIGN IN"
                                                    },
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 0.5.sp,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    maxLines = 1
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(20.dp))

                                            // Divider
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                                Text(
                                                    text = if (currentLang == Language.RU) "ИЛИ" else "OR",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    modifier = Modifier.padding(horizontal = 14.dp)
                                                )
                                                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                            }

                                            Spacer(modifier = Modifier.height(20.dp))

                                            // Google Sign-In Button
                                            OutlinedButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    viewModel.clearLoginMessages()
                                                    val signInIntent = googleSignInClient.signInIntent
                                                    googleSignInLauncher.launch(signInIntent)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(52.dp),
                                                shape = RoundedCornerShape(26.dp),
                                                border = BorderStroke(1.dp, Color(0xFF747775)),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    containerColor = Color.White,
                                                    contentColor = Color(0xFF1F1F1F)
                                                )
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = "Google",
                                                    modifier = Modifier.size(22.dp),
                                                    tint = Color(0xFF4285F4)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = if (currentLang == Language.RU) "Войти через Google" else "Sign in with Google",
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF1F1F1F),
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        // Loading overlay
                                        if (isAuthLoading) {
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                                    .clickable(enabled = false) {},
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    CircularProgressIndicator(
                                                        color = MaterialTheme.colorScheme.primary,
                                                        strokeWidth = 4.dp
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        text = if (currentLang == Language.RU) "Пожалуйста, подождите..." else "Please wait...",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // LOGGED IN USER PROFILE VIEW
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(24.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        val bgBmp = bgBitmap
                                        if (bgBmp != null) {
                                            Image(
                                                bitmap = bgBmp,
                                                contentDescription = null,
                                                modifier = Modifier.matchParentSize(),
                                                contentScale = ContentScale.Crop,
                                                alpha = 0.55f
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(
                                                        Brush.verticalGradient(
                                                            colors = listOf(
                                                                Color.Black.copy(alpha = 0.4f),
                                                                Color.Black.copy(alpha = 0.75f)
                                                            )
                                                        )
                                                    )
                                            )
                                        }

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            val defaultBgColor = MaterialTheme.colorScheme.surfaceVariant
                                            val parsedAvatarBgColor = remember(customAvatarBgColor, defaultBgColor) {
                                                try {
                                                    Color(android.graphics.Color.parseColor("#$customAvatarBgColor"))
                                                } catch (e: Exception) {
                                                    defaultBgColor
                                                }
                                            }

                                            // Avatar representation with strictly circular PlayerAvatarView
                                            PlayerAvatarView(
                                                playerName = playerName,
                                                avatarEmoji = customAvatarEmoji,
                                                avatarBgColorHex = customAvatarBgColor,
                                                avatarFrame = equippedAvatarFrame,
                                                customBitmap = customAvatarBitmap,
                                                size = 100.dp,
                                                themeColor = themeColor,
                                                secondaryColor = secondaryColor,
                                                showOnlineDot = true,
                                                isOnline = true,
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    viewModel.triggerAudioFeedback("click")
                                                    showAvatarDialog = true
                                                }
                                            )
    
                                        Spacer(modifier = Modifier.height(12.dp))
    
                                        val titleString = when (equippedTitle) {
                                            "none" -> ""
                                            "node" -> if (currentLang == Language.RU) "РЕКРУТ" else "RECRUIT"
                                            "lord" -> if (currentLang == Language.RU) "ВЕТЕРАН" else "VETERAN"
                                            "cosmic_overlord" -> if (currentLang == Language.RU) "ЭЛИТА" else "ELITE"
                                            "ai_consensus" -> if (currentLang == Language.RU) "ЛЕГЕНДА" else "LEGEND"
                                            else -> ""
                                        }
    
                                        if (titleString.isNotEmpty()) {
                                            Text(
                                                text = titleString,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.secondary,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                modifier = Modifier
                                                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                        }
    
                                        if (hasNicknameGradient) {
                                            val myAvatarColor = parseHexColor(customAvatarBgColor, themeColor)
                                            val nicknameBrush = rememberAnimatedNicknameBrush(baseColor = myAvatarColor)
                                            Text(
                                                text = playerName.uppercase(),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontSize = if (playerName.length > 15) 15.sp else if (playerName.length > 10) 18.sp else 22.sp,
                                                    brush = nicknameBrush
                                                ),
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        } else {
                                            Text(
                                                text = playerName.uppercase(),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontSize = if (playerName.length > 15) 15.sp else if (playerName.length > 10) 18.sp else 22.sp
                                                ),
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
    
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier
                                                .clickable {
                                                    try {
                                                        val clipboard = viewModel.getApplication<android.app.Application>()
                                                            .getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                        val clip = android.content.ClipData.newPlainText("User ID", userId)
                                                        clipboard.setPrimaryClip(clip)
                                                        triggerMessage(if (currentLang == Language.RU) "ID скопирован в буфер!" else "ID copied to clipboard!")
                                                    } catch (e: Exception) {
                                                        triggerMessage("Error copying ID", isError = true)
                                                    }
                                                }
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "ID: $userId",
                                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy",
                                                modifier = Modifier.size(12.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
    
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        ) {
                                            Text(
                                                text = Translations.getLocalizedRank(onlineTier, currentLang),
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        FilledTonalButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                viewModel.triggerAudioFeedback("click")
                                                showAvatarDialog = true
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (currentLang == Language.RU) "Настройки профиля" else "Edit Profile",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
    
                                // Mastery Progress
                                val totalLines = statsClearedLines
                                val highscoreVal = statsHighScore
                                val totalXp = (totalLines * 25) + (highscoreVal / 10)
                                val levelXpBound = 500
                                val masteryLevel = (totalXp / levelXpBound) + 1
                                val currentLevelXp = totalXp % levelXpBound
                                val xpPercentage = if (currentLevelXp > 0) currentLevelXp.toFloat() / levelXpBound.toFloat() else 0.01f
    
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // ПЛАНКА 1: Заголовок + Бейдж уровня
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.MilitaryTech,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Text(
                                                    text = if (currentLang == Language.RU) "УРОВЕНЬ" else "LEVEL",
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Text(
                                                    text = "LVL $masteryLevel",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                                )
                                            }
                                        }

                                        // ПЛАНКА 2: Прогресс-бар + инфо о XP до след. уровня
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            LinearProgressIndicator(
                                                progress = { xpPercentage },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(8.dp)
                                                    .clip(RoundedCornerShape(4.dp)),
                                                color = MaterialTheme.colorScheme.primary,
                                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                val xpNeeded = (levelXpBound - currentLevelXp).coerceAtLeast(0)
                                                Text(
                                                    text = if (currentLang == Language.RU) "До след. уровня: $xpNeeded XP" else "To next level: $xpNeeded XP",
                                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                                )
                                                Text(
                                                    text = "$currentLevelXp / $levelXpBound XP",
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
    
                                // Stats telemetries
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(18.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    text = if (currentLang == Language.RU) "СТАТИСТИКА" else "STATISTICS",
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.triggerAudioFeedback("click")
                                                    viewModel.resetProfileStats()
                                                    triggerMessage(if (currentLang == Language.RU) "Статистика сброшена." else "Stats reset.")
                                                },
                                                shape = RoundedCornerShape(12.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (currentLang == Language.RU) "Сброс" else "Reset",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Score High
                                            Surface(
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(14.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = if (currentLang == Language.RU) "РЕКОРД ОЧКОВ" else "HIGH SCORE",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 9.sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "$statsHighScore",
                                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                            // Lines Cleared
                                            Surface(
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(14.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = if (currentLang == Language.RU) "ЛИНИЙ ОЧИЩЕНО" else "LINES CLEARED",
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            fontWeight = FontWeight.Bold,
                                                            fontSize = 9.sp
                                                        ),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "$statsClearedLines",
                                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                // Real Email Verification Status Banner (Only visible when unverified)
                                if (playerName != "Player 1" && !isEmailVerified) {
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .border(1.dp, if (isEmailVerified) Color(0xFF00E676).copy(alpha = 0.4f) else Color(0xFFFF9100).copy(alpha = 0.4f), RoundedCornerShape(22.dp)),
                                        shape = RoundedCornerShape(22.dp),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = if (isEmailVerified) Color(0xFF1B5E20).copy(alpha = 0.12f) else Color(0xFFE65100).copy(alpha = 0.12f)
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isEmailVerified) Icons.Default.Check else Icons.Default.Email,
                                                        contentDescription = null,
                                                        tint = if (isEmailVerified) Color(0xFF00E676) else Color(0xFFFF9100),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Text(
                                                        text = if (isEmailVerified) {
                                                            if (currentLang == Language.RU) "ПОЧТА ПОДТВЕРЖДЕНА" else "EMAIL VERIFIED"
                                                        } else {
                                                            if (currentLang == Language.RU) "ПОЧТА НЕ ПОДТВЕРЖДЕНА" else "EMAIL NOT VERIFIED"
                                                        },
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.ExtraBold),
                                                        color = if (isEmailVerified) Color(0xFF00E676) else Color(0xFFFF9100)
                                                    )
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = if (isEmailVerified) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFFFF9100).copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = if (isEmailVerified) "VERIFIED" else "PENDING",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                                        color = if (isEmailVerified) Color(0xFF00E676) else Color(0xFFFF9100),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }

                                            if (!isEmailVerified) {
                                                Text(
                                                    text = if (currentLang == Language.RU) 
                                                        "Для безопасности аккаунта и восстановления доступа подтвердите адрес электронной почты." 
                                                        else "Please verify your email address to secure your account and enable account recovery.",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.resendVerificationEmail { success, msg ->
                                                                triggerMessage(msg, isError = !success)
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Text(
                                                            text = if (currentLang == Language.RU) "Отправить письмо" else "Send Email",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                                        )
                                                    }

                                                    Button(
                                                        onClick = {
                                                            viewModel.checkEmailVerificationStatus { verified ->
                                                                if (verified) {
                                                                    triggerMessage(if (currentLang == Language.RU) "Почта успешно подтверждена!" else "Email verified successfully!")
                                                                } else {
                                                                    triggerMessage(if (currentLang == Language.RU) "Почта ещё не подтверждена. Проверьте входящие!" else "Email not verified yet. Check your inbox!", isError = true)
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Text(
                                                            text = if (currentLang == Language.RU) "Проверить" else "Check Status",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Log out button with confirmation dialog
                                if (showSignOutConfirmDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showSignOutConfirmDialog = false },
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        tonalElevation = 6.dp,
                                        shape = RoundedCornerShape(28.dp),
                                        icon = {
                                            Surface(
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.errorContainer,
                                                modifier = Modifier.size(48.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.ExitToApp,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                            }
                                        },
                                        title = {
                                            Text(
                                                text = if (currentLang == Language.RU) "Выход из аккаунта" else "Log Out Confirmation",
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = if (currentLang == Language.RU) 
                                                    "Вы уверены, что хотите выйти из аккаунта? Все синхронизированные данные сохранены в облаке." 
                                                    else "Are you sure you want to log out? All synced data is safely stored in the cloud.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    showSignOutConfirmDialog = false
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    viewModel.switchAccount("Player 1", "BRONZE", 500)
                                                    inputUsername = ""
                                                    inputPassword = ""
                                                    viewModel.clearLoginMessages()
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error,
                                                    contentColor = MaterialTheme.colorScheme.onError
                                                ),
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                Text(if (currentLang == Language.RU) "ВЫЙТИ" else "LOG OUT", fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(
                                                onClick = { showSignOutConfirmDialog = false },
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                Text(if (currentLang == Language.RU) "ОТМЕНА" else "CANCEL", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    )
                                }

                                FilledTonalButton(
                                    onClick = {
                                        showSignOutConfirmDialog = true
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer,
                                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp),
                                    shape = RoundedCornerShape(18.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (currentLang == Language.RU) "ВЫЙТИ ИЗ АККАУНТА" else "LOG OUT",
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                            }
                        }
                    }
                    1 -> {
            // STORE SHOP VIEW
            val avatarFramesList = remember {
                listOf(
                    AvatarFrameStoreData("standard", 0, if (currentLang == Language.RU) "По умолчанию" else "Default", if (currentLang == Language.RU) "Классическая рамка" else "Classic subtle frame", Color.Gray),
                    AvatarFrameStoreData("neon_ae", 350, if (currentLang == Language.RU) "Неон" else "Neon", if (currentLang == Language.RU) "Неоновое свечение" else "Glowing neon frame", Color(0xFF00FFCC)),
                    AvatarFrameStoreData("gold_ma", 600, if (currentLang == Language.RU) "Золото" else "Gold", if (currentLang == Language.RU) "Золотое обрамление" else "Prestige gold frame", Color(0xFFFFD700)),
                    AvatarFrameStoreData("omega_ti", 1100, if (currentLang == Language.RU) "Титан" else "Titan", if (currentLang == Language.RU) "Титановая броня" else "Thick titanium frame", Color(0xFF90A4AE))
                )
            }
            val playerBadgesList = remember {
                listOf(
                    PlayerBadgeStoreData("none", 0, if (currentLang == Language.RU) "Без титула" else "No Title", if (currentLang == Language.RU) "Стандартный вид" else "Standard title", ""),
                    PlayerBadgeStoreData("node", 2500, if (currentLang == Language.RU) "РЕКРУТ" else "RECRUIT", if (currentLang == Language.RU) "Статус новобранца" else "Recruit title status", ""),
                    PlayerBadgeStoreData("lord", 5000, if (currentLang == Language.RU) "ВЕТЕРАН" else "VETERAN", if (currentLang == Language.RU) "Опытный игрок" else "Veteran title status", ""),
                    PlayerBadgeStoreData("cosmic_overlord", 8000, if (currentLang == Language.RU) "ЭЛИТА" else "ELITE", if (currentLang == Language.RU) "Мастер игры" else "Elite title status", ""),
                    PlayerBadgeStoreData("ai_consensus", 12000, if (currentLang == Language.RU) "ЛЕГЕНДА" else "LEGEND", if (currentLang == Language.RU) "Легенда арены" else "Legend title status", "")
                )
            }
            var selectedStoreCategory by remember { mutableStateOf("ALL") }
            val storeCategories = remember(currentLang) {
                listOf(
                    "ALL" to (if (currentLang == Language.RU) "Все" else "All"),
                    "FRAMES" to (if (currentLang == Language.RU) "Рамки" else "Frames"),
                    "TITLES" to (if (currentLang == Language.RU) "Титулы" else "Titles"),
                    "SKINS" to (if (currentLang == Language.RU) "Сетка" else "Grid"),
                    "BLOCKS" to (if (currentLang == Language.RU) "Блоки" else "Blocks"),
                    "BUTTONS" to (if (currentLang == Language.RU) "Кнопки" else "Buttons"),
                    "FONTS" to (if (currentLang == Language.RU) "Шрифты" else "Fonts"),
                    "MODES" to (if (currentLang == Language.RU) "Режимы" else "Modes"),
                    "RANKS" to (if (currentLang == Language.RU) "Ранги" else "Ranks"),
                    "TAGS" to (if (currentLang == Language.RU) "Теги" else "Tags")
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 12.dp,
                    end = 16.dp,
                    bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Category Filter Chips
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(storeCategories) { (catKey, catLabel) ->
                            val isSelected = selectedStoreCategory == catKey
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    selectedStoreCategory = catKey
                                },
                                label = {
                                    Text(
                                        text = catLabel,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = themeColor,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null
                            )
                        }
                    }
                }

                if (!isLoggedIn) {
                    item {
                        ElevatedCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (currentLang == Language.RU) {
                                        "Режим гостя: Приобретения сохраняются только локально. Зарегистрируйтесь в профиле."
                                    } else {
                                        "Guest Mode: Purchases are saved locally. Please register in the profile tab."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
                
                // 1. RANKS
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "RANKS") {
                    item {
                        val rankIndex = ranksList.indexOfFirst { it.id == onlineTier }
                        val ownedRanksCount = if (rankIndex >= 0) rankIndex + 1 else 1
                        StoreSectionHeader(
                            title = if (currentLang == Language.RU) "СЕТЕВЫЕ РАНГИ" else "CONSENSUS RANKS",
                            icon = Icons.Default.MilitaryTech,
                            purchasedCount = ownedRanksCount,
                            totalCount = ranksList.size,
                            currentLang = currentLang
                        )
                    }
                    items(ranksList) { rank ->
                        val rankIndex = ranksList.indexOfFirst { it.id == rank.id }
                        val currentRankIndex = ranksList.indexOfFirst { it.id == onlineTier }
                        val isCurrent = rank.id == onlineTier
                        val isOwned = rankIndex <= currentRankIndex
                        val isLocked = rankIndex > currentRankIndex + 1
                        val isNext = rankIndex == currentRankIndex + 1

                        StoreItemCard(
                            icon = Icons.Default.MilitaryTech,
                            category = if (currentLang == Language.RU) "Ранг" else "Rank",
                            title = rank.id,
                            description = rank.description,
                            isActive = isCurrent,
                            isOwned = isOwned,
                            cost = rank.cost,
                            currentLang = currentLang,
                            isLocked = isLocked,
                            onAction = {
                                if (isCurrent) {
                                    viewModel.setOnlineTier("BRONZE")
                                    triggerMessage(if (currentLang == Language.RU) "Ранг сброшен до базового" else "Rank reset to BRONZE.")
                                } else if (isOwned) {
                                    viewModel.setOnlineTier(rank.id)
                                    triggerMessage(if (currentLang == Language.RU) "Ранг успешно выбран" else "Rank updated to ${rank.id}.")
                                } else if (isNext) {
                                    purchaseRank(rank.id, rank.cost)
                                }
                            }
                        )
                    }
                }

                // 2. GRID SCHEMES
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "SKINS") {
                    item {
                        StoreSectionHeader(
                            title = if (currentLang == Language.RU) "ОФОРМЛЕНИЕ СЕТКИ" else "GRID THEMES",
                            icon = Icons.Default.Palette,
                            purchasedCount = purchasedSkinsSet.size,
                            totalCount = skinsList.size,
                            currentLang = currentLang
                        )
                    }
                    items(skinsList) { skin ->
                        val isEquipped = boardSkin == skin.id
                        val isOwned = purchasedSkinsSet.contains(skin.id)
                        StoreItemCard(
                            icon = Icons.Default.Palette,
                            category = if (currentLang == Language.RU) "Оформление" else "Theme",
                            title = skin.displayName,
                            description = skin.description,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            cost = skin.cost,
                            currentLang = currentLang,
                            onAction = {
                                if (isEquipped) {
                                    viewModel.setBoardColorSkin("cyberpunk")
                                    triggerMessage(if (currentLang == Language.RU) "Оформление сброшено" else "Grid theme reset to cyberpunk.")
                                } else {
                                    purchaseSkin(skin.id, skin.cost)
                                }
                            }
                        )
                    }
                }

                // 3. CUBE STYLES
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "BLOCKS") {
                    item {
                        StoreSectionHeader(
                            title = if (currentLang == Language.RU) "СТИЛИ БЛОКОВ" else "BLOCK STYLES",
                            icon = Icons.Default.Category,
                            purchasedCount = purchasedCubeSkinsSet.size,
                            totalCount = cubeSkinsList.size,
                            currentLang = currentLang
                        )
                    }
                    items(cubeSkinsList) { cSkin ->
                        val isEquipped = blockStyle == cSkin.id
                        val isOwned = purchasedCubeSkinsSet.contains(cSkin.id)
                        StoreItemCard(
                            icon = Icons.Default.Category,
                            category = if (currentLang == Language.RU) "Стиль блоков" else "Block Style",
                            title = cSkin.displayName,
                            description = cSkin.description,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            cost = cSkin.cost,
                            currentLang = currentLang,
                            onAction = {
                                if (isEquipped) {
                                    viewModel.setBlockStyle("glass")
                                    triggerMessage(if (currentLang == Language.RU) "Стиль блоков сброшен" else "Cube style reset to glass.")
                                } else {
                                    purchaseCubeSkin(cSkin.id, cSkin.cost)
                                }
                            }
                        )
                    }
                }

                // 4. GAME MODES
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "MODES") {
                    item {
                        StoreSectionHeader(
                            title = if (currentLang == Language.RU) "ИГРОВЫЕ РЕЖИМЫ" else "GAME MODES",
                            icon = Icons.Default.PlayCircleOutline,
                            purchasedCount = purchasedModesSet.size,
                            totalCount = premiumModesList.size,
                            currentLang = currentLang
                        )
                    }
                    items(premiumModesList) { pMode ->
                        val isOwned = purchasedModesSet.contains(pMode.id)
                        StoreItemCard(
                            icon = Icons.Default.PlayCircleOutline,
                            category = if (currentLang == Language.RU) "Режим" else "Game Mode",
                            title = pMode.displayName,
                            description = pMode.description,
                            isActive = false,
                            isOwned = isOwned,
                            cost = pMode.cost,
                            currentLang = currentLang,
                            onAction = { purchaseMode(pMode.id, pMode.cost) }
                        )
                    }
                }

                // 5. AVATAR FRAMES
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "FRAMES") {
                    item {
                        StoreSectionHeader(
                            title = if (currentLang == Language.RU) "РАМКИ АВАТАРА" else "AVATAR FRAMES",
                            icon = Icons.Default.Portrait,
                            purchasedCount = purchasedAvatarFrames.size,
                            totalCount = avatarFramesList.size,
                            currentLang = currentLang
                        )
                    }
                    items(avatarFramesList) { frame ->
                        val isEquipped = equippedAvatarFrame == frame.id
                        val isOwned = purchasedAvatarFrames.contains(frame.id)
                        StoreItemCard(
                            icon = Icons.Default.Portrait,
                            category = if (currentLang == Language.RU) "Рамка" else "Avatar Frame",
                            title = frame.displayName,
                            description = frame.description,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            cost = frame.cost,
                            currentLang = currentLang,
                            onAction = {
                                if (isEquipped) {
                                    viewModel.setEquippedAvatarFrame("standard")
                                    triggerMessage(if (currentLang == Language.RU) "Рамка аватара сброшена" else "Avatar frame reset to standard.")
                                } else {
                                    selectAvatarFrame(frame.id, frame.cost)
                                }
                            }
                        )
                    }
                }

                // 6. PLAYER TITLES
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "TITLES") {
                    item {
                        StoreSectionHeader(
                            title = if (currentLang == Language.RU) "ТИТУЛЫ ИГРОКА" else "PLAYER TITLES",
                            icon = Icons.Default.WorkspacePremium,
                            purchasedCount = purchasedTitles.size,
                            totalCount = playerBadgesList.size,
                            currentLang = currentLang
                        )
                    }
                    items(playerBadgesList) { title ->
                        val isEquipped = equippedTitle == title.id
                        val isOwned = purchasedTitles.contains(title.id)
                        StoreItemCard(
                            icon = Icons.Default.WorkspacePremium,
                            category = if (currentLang == Language.RU) "Титул" else "Player Title",
                            title = title.displayName,
                            description = title.description,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            cost = title.cost,
                            currentLang = currentLang,
                            onAction = {
                                if (isEquipped) {
                                    viewModel.setEquippedTitle("none")
                                    triggerMessage(if (currentLang == Language.RU) "Титул сброшен" else "Title reset to none.")
                                } else {
                                    selectTitle(title.id, title.cost)
                                }
                            }
                        )
                    }
                }


                // 8. FONTS
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "FONTS") {
                    item {
                        StoreSectionHeader(
                            title = if (currentLang == Language.RU) "ШРИФТЫ ТЕКСТА" else "TYPOGRAPHY FONTS",
                            icon = Icons.Default.Edit,
                            purchasedCount = purchasedFonts.size,
                            totalCount = fontsList.size,
                            currentLang = currentLang
                        )
                    }
                    items(fontsList) { fontItem ->
                        val isEquipped = customFontKey == fontItem.id
                        val isOwned = purchasedFonts.contains(fontItem.id)
                        StoreItemCard(
                            icon = Icons.Default.Edit,
                            category = if (currentLang == Language.RU) "Шрифт" else "Font",
                            title = fontItem.displayName,
                            description = fontItem.description,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            cost = fontItem.cost,
                            currentLang = currentLang,
                            onAction = {
                                if (isEquipped) {
                                    viewModel.setCustomFontKey("default")
                                    triggerMessage(if (currentLang == Language.RU) "Шрифт сброшен" else "Font reset to default.")
                                } else {
                                    selectFont(fontItem.id, fontItem.cost)
                                }
                            }
                        )
                    }
                }

                // 9. CONTROL BUTTON STYLES
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "BUTTONS") {
                    item {
                        StoreSectionHeader(
                            title = if (currentLang == Language.RU) "ДИЗАЙН КНОПОК" else "CONTROL BUTTON STYLES",
                            icon = Icons.Default.Extension,
                            purchasedCount = purchasedControlButtonStyles.size,
                            totalCount = controlButtonStylesList.size,
                            currentLang = currentLang
                        )
                    }
                    items(controlButtonStylesList) { btnStyle ->
                        val isEquipped = controlButtonStyle == btnStyle.id
                        val isOwned = purchasedControlButtonStyles.contains(btnStyle.id)
                        StoreItemCard(
                            icon = Icons.Default.Extension,
                            category = if (currentLang == Language.RU) "Кнопки" else "Button Design",
                            title = btnStyle.displayName,
                            description = btnStyle.description,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            cost = btnStyle.cost,
                            currentLang = currentLang,
                            onAction = {
                                if (isEquipped) {
                                    viewModel.setControlButtonStyle("classic")
                                    triggerMessage(if (currentLang == Language.RU) "Стиль кнопок сброшен" else "Button style reset to classic.")
                                } else {
                                    selectControlButtonStyle(btnStyle.id, btnStyle.cost)
                                }
                            }
                        )
                    }
                }
                
                // 10. CUSTOM LEADERBOARD TAG
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "TAGS") {
                    item {
                        val isOwned = customTagUnlocked
                        StoreSectionHeader(
                            title = if (currentLang == Language.RU) "КАСТОМНЫЙ ТЕГ" else "CUSTOM TAG",
                            icon = Icons.Default.Shield,
                            purchasedCount = if (isOwned) 1 else 0,
                            totalCount = 1,
                            currentLang = currentLang
                        )
                    }
                    item {
                        val isOwned = customTagUnlocked
                        StoreItemCard(
                            icon = Icons.Default.Shield,
                            category = if (currentLang == Language.RU) "Тег" else "Custom Tag",
                            title = if (currentLang == Language.RU) "Личный Тег" else "Leaderboard Tag",
                            description = if (currentLang == Language.RU) "Позволяет установить свой тег в глобальной таблице рекордов" else "Unlocks custom tag customization in profile settings",
                            isActive = isOwned,
                            isOwned = isOwned,
                            cost = 500000,
                            currentLang = currentLang,
                            onAction = {
                                if (isOwned) {
                                    triggerMessage(if (currentLang == Language.RU) "Уже приобретено! Настройте в настройках профиля." else "Already purchased! Edit it in profile settings.")
                                } else {
                                    if (credits >= 500000) {
                                        viewModel.spendCredits(500000)
                                        viewModel.setCustomTagUnlocked(true)
                                        viewModel.triggerAudioFeedback("buy")
                                        triggerMessage(if (currentLang == Language.RU) "Тег успешно куплен! Установите его в настройках." else "Custom tag purchased successfully! Set it in settings.")
                                    } else {
                                        viewModel.triggerAudioFeedback("error")
                                        triggerMessage(if (currentLang == Language.RU) "Недостаточно средств" else "Insufficient funds", isError = true)
                                    }
                                }
                            }
                        )
                    }
                }

                // 11. PRESTIGE II
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "PRESTIGE") {
                    item {
                        val prestigeLvl by viewModel.prestigeLevel.collectAsStateWithLifecycle()
                        val isPrestigeActive = prestigeLvl >= 2

                        StoreSectionHeader(
                            title = if (currentLang == Language.RU) "ПРЕСТИЖ II" else "PRESTIGE II",
                            icon = Icons.Default.AutoAwesome,
                            purchasedCount = if (isPrestigeActive) 1 else 0,
                            totalCount = 1,
                            currentLang = currentLang
                        )
                    }
                    item {
                        val prestigeLvl by viewModel.prestigeLevel.collectAsStateWithLifecycle()
                        val isPrestigeActive = prestigeLvl >= 2
                        var showPrestigeConfirm by remember { mutableStateOf(false) }

                        StoreItemCard(
                            icon = Icons.Default.AutoAwesome,
                            category = if (currentLang == Language.RU) "Престиж" else "Prestige",
                            title = if (currentLang == Language.RU) "Престиж II (Множитель x8)" else "Prestige II (x8 Multiplier)",
                            description = if (currentLang == Language.RU)
                                if (isPrestigeActive) "Престиж II активен! Постоянный множитель x8 ко всем заработкам монет."
                                else "Требуется 1,000,000 🪙. Добровольный сброс баланса даёт Личный Тег и вечный x8 множитель ко всем доходам!"
                            else
                                if (isPrestigeActive) "Prestige II is active! Permanent x8 multiplier to all coin rewards."
                                else "Requires 1,000,000 🪙. Reset balance to 0 to unlock Leaderboard Tag and permanent x8 earnings multiplier!",
                            isActive = isPrestigeActive,
                            isOwned = isPrestigeActive,
                            cost = 1000000,
                            currentLang = currentLang,
                            onAction = {
                                if (isPrestigeActive) {
                                    triggerMessage(if (currentLang == Language.RU) "Престиж II уже активирован! Множитель x8 активен." else "Prestige II is already active! Multiplier x8 is applied.")
                                } else {
                                    if (credits >= 1000000) {
                                        showPrestigeConfirm = true
                                    } else {
                                        viewModel.triggerAudioFeedback("error")
                                        triggerMessage(if (currentLang == Language.RU) "Необходимо накопить 1,000,000 🪙" else "Need to save 1,000,000 🪙 first", isError = true)
                                    }
                                }
                            }
                        )

                        if (showPrestigeConfirm) {
                            AlertDialog(
                                onDismissRequest = { showPrestigeConfirm = false },
                                icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(36.dp)) },
                                title = {
                                    Text(
                                        text = if (currentLang == Language.RU) "Активировать Престиж II?" else "Activate Prestige II?",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                },
                                text = {
                                    Text(
                                        text = if (currentLang == Language.RU)
                                            "Внимание! Ваш баланс монет будет сброшен до 0.\n\nВы получите навсегда:\n✨ Постоянный множитель x8 ко всем заработкам монет\n🛡️ Бесплатный Личный Тег для таблицы рекордов\n👑 Знак Престижа II"
                                        else
                                            "Attention! Your coin balance will be reset to 0.\n\nYou will permanently receive:\n✨ Permanent x8 multiplier to all coin rewards\n🛡️ Free Custom Leaderboard Tag\n👑 Prestige II Badge",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            showPrestigeConfirm = false
                                            viewModel.activatePrestige2()
                                            viewModel.triggerAudioFeedback("success")
                                            triggerMessage(if (currentLang == Language.RU) "Престиж II активирован! Множитель x8 получен!" else "Prestige II activated! x8 Multiplier unlocked!")
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                                    ) {
                                        Text(if (currentLang == Language.RU) "СБРОСИТЬ И АКТИВИРОВАТЬ" else "RESET & ACTIVATE", fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showPrestigeConfirm = false }) {
                                        Text(if (currentLang == Language.RU) "Отмена" else "Cancel")
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
        2 -> {
            // ACHIEVEMENTS VIEW
            AchievementsTabContent(
                viewModel = viewModel,
                themeColor = themeColor,
                currentLang = currentLang
            )
        }
        3 -> {
            if (showNewSection) {
                NewTabContent(
                    currentLang = currentLang,
                    themeColor = themeColor
                )
            }
        }
    }
}
}
}
}
}

    if (showAvatarDialog) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { 
                showAvatarDialog = false 
                viewModel.clearUpdateMessages()
            },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = if (currentLang == Language.RU) "НАСТРОЙКИ ПРОФИЛЯ" else "PROFILE SETTINGS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = { 
                                showAvatarDialog = false 
                                viewModel.clearUpdateMessages()
                            }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                }
            ) { dialogPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dialogPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Avatar & Background Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "АВАТАР И ФОН" else "AVATAR & BACKGROUND",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(112.dp)
                            ) {
                                if (equippedAvatarFrame in listOf("neon_ae", "gold_ma", "chrono_gl", "omega_ti")) {
                                    Box(
                                        modifier = Modifier
                                            .size(98.dp)
                                            .graphicsLayer {
                                                rotationZ = rotationAngle
                                            }
                                            .border(
                                                width = avatarFrameThickness + 0.5.dp,
                                                brush = avatarFrameBorderBrush,
                                                shape = RoundedCornerShape(50)
                                            )
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(50)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val avatarBmp = customAvatarBitmap
                                    if (avatarBmp != null) {
                                        Image(
                                            bitmap = avatarBmp,
                                            contentDescription = "Avatar",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Placeholder Avatar",
                                            modifier = Modifier.size(52.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FilledTonalButton(
                                    onClick = { avatarPickerLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.Portrait, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLang == Language.RU) "Аватар" else "Set Avatar",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { bgPickerLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (currentLang == Language.RU) "Фон карты" else "Set Card BG",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            val hasCustomAvatar = remember(playerName, avatarChangeCounter) {
                                sharedPrefs.getBoolean("has_custom_avatar_${playerName}", false)
                            }
                            val hasCustomBg = remember(playerName, bgChangeCounter) {
                                sharedPrefs.getBoolean("has_custom_background_${playerName}", false)
                            }

                            if (hasCustomAvatar || hasCustomBg) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (hasCustomAvatar) {
                                        OutlinedButton(
                                            onClick = {
                                                val file = File(context.filesDir, "custom_avatar_${playerName}.jpg")
                                                if (file.exists()) file.delete()
                                                sharedPrefs.edit().putBoolean("has_custom_avatar_${playerName}", false).apply()
                                                avatarChangeCounter++
                                                viewModel.triggerAudioFeedback("click")
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(if (currentLang == Language.RU) "Сбросить аватар" else "Clear Avatar", fontSize = 11.sp)
                                        }
                                    }
                                    if (hasCustomBg) {
                                        OutlinedButton(
                                            onClick = {
                                                val file = File(context.filesDir, "custom_background_${playerName}.jpg")
                                                if (file.exists()) file.delete()
                                                sharedPrefs.edit().putBoolean("has_custom_background_${playerName}", false).apply()
                                                bgChangeCounter++
                                                viewModel.triggerAudioFeedback("click")
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(if (currentLang == Language.RU) "Сбросить фон" else "Clear BG", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Nickname Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "СМЕНИТЬ НИКНЕЙМ" else "CHANGE NICKNAME",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            var editNickNameInput by remember { mutableStateOf(playerName) }

                            OutlinedTextField(
                                value = editNickNameInput,
                                onValueChange = { editNickNameInput = it },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                label = { Text(if (currentLang == Language.RU) "Новый никнейм" else "New Nickname") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            nicknameUpdateError?.let { err ->
                                Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                            nicknameUpdateSuccess?.let { msg ->
                                Text(text = msg, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.updateNickname(editNickNameInput)
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(if (currentLang == Language.RU) "Сохранить ник" else "Save Nickname", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // 3. Email Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        val currentEmail = currentUser?.email ?: ""
                        var editEmailInput by remember(currentEmail) { mutableStateOf(currentEmail) }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (currentEmail.isEmpty()) {
                                    if (currentLang == Language.RU) "ПРИВЯЗАТЬ ПОЧТУ" else "BIND EMAIL"
                                } else {
                                    if (currentLang == Language.RU) "ИЗМЕНИТЬ ПОЧТУ" else "CHANGE EMAIL"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedTextField(
                                value = editEmailInput,
                                onValueChange = { editEmailInput = it },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                label = { Text(if (currentLang == Language.RU) "Электронная почта" else "Email Address") },
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            emailUpdateError?.let { err ->
                                Text(text = err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                            }
                            emailUpdateSuccess?.let { msg ->
                                Text(text = msg, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    viewModel.updateEmail(editEmailInput)
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    if (currentEmail.isEmpty()) {
                                        if (currentLang == Language.RU) "Привязать" else "Bind Email"
                                    } else {
                                        if (currentLang == Language.RU) "Обновить" else "Update Email"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // 4. Custom Leaderboard Tag Card
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "ТЕГ В ТАБЛИЦЕ РЕКОРДОВ" else "LEADERBOARD CUSTOM TAG",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            val customTagUnlocked by viewModel.customTagUnlocked.collectAsStateWithLifecycle()
                            val customTagVal by viewModel.customTag.collectAsStateWithLifecycle()
                            
                            var editTagInput by remember(customTagVal) { mutableStateOf(customTagVal) }

                            if (!customTagUnlocked) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = if (currentLang == Language.RU) 
                                                "Функция заблокирована. Приобретите «Личный Тег» в магазине."
                                            else 
                                                "Feature locked. Purchase «Leaderboard Tag» in the store.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            } else {
                                OutlinedTextField(
                                    value = editTagInput,
                                    onValueChange = { if (it.length <= 6) editTagInput = it },
                                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) },
                                    label = { Text(if (currentLang == Language.RU) "Кастомный тег (макс. 6 симв.)" else "Custom Tag (max 6 chars)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        viewModel.setCustomTag(editTagInput)
                                        viewModel.triggerAudioFeedback("success")
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(if (currentLang == Language.RU) "Сохранить тег" else "Save Tag", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun StoreSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    purchasedCount: Int = -1,
    totalCount: Int = -1,
    currentLang: Language = Language.EN
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            if (purchasedCount >= 0 && totalCount >= 0) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = if (currentLang == Language.RU) "$purchasedCount из $totalCount" else "$purchasedCount of $totalCount",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StoreItemCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    category: String,
    title: String,
    description: String,
    isActive: Boolean,
    isOwned: Boolean,
    cost: Int,
    currentLang: Language,
    isLocked: Boolean = false,
    onAction: () -> Unit
) {
    val rarityText = if (currentLang == Language.RU) {
        if (cost <= 0) "БАЗОВЫЙ"
        else if (cost <= 400) "РЕДКИЙ"
        else if (cost <= 800) "ЭПИЧЕСКИЙ"
        else "ЛЕГЕНДАРНЫЙ"
    } else {
        if (cost <= 0) "COMMON"
        else if (cost <= 400) "RARE"
        else if (cost <= 800) "EPIC"
        else "LEGENDARY"
    }

    val rarityColor = when {
        cost <= 0 -> Color(0xFF9E9E9E)
        cost <= 400 -> Color(0xFF2196F3)
        cost <= 800 -> Color(0xFF9C27B0)
        else -> Color(0xFFFF9800)
    }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.surfaceContainerHighest
                             else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = if (isActive) 3.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        else rarityColor.copy(alpha = 0.12f),
                tonalElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isActive) MaterialTheme.colorScheme.primary else rarityColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Text(
                            text = category.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = rarityColor.copy(alpha = 0.14f)
                    ) {
                        AdaptiveText(
                            text = rarityText,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            ),
                            color = rarityColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            val isRank = category.equals("Rank", ignoreCase = true) || category.equals("Ранг", ignoreCase = true)
            if (!(isRank && isOwned)) {
                val buttonEnabled = when {
                    isRank -> !isOwned
                    else -> !isLocked
                }
                if (isOwned || isActive) {
                    FilledTonalButton(
                        onClick = onAction,
                        enabled = buttonEnabled,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isActive) MaterialTheme.colorScheme.primary
                                             else MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = if (isActive) MaterialTheme.colorScheme.onPrimary
                                           else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            text = if (isActive) {
                                if (currentLang == Language.RU) "ВКЛ" else "ON"
                            } else {
                                if (currentLang == Language.RU) "ВЫКЛ" else "OFF"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    Button(
                        onClick = onAction,
                        enabled = buttonEnabled,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLocked) MaterialTheme.colorScheme.surfaceContainerHighest
                                             else MaterialTheme.colorScheme.primary,
                            contentColor = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                           else MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(
                            text = if (isLocked) {
                                if (currentLang == Language.RU) "БЛОК" else "LOCKED"
                            } else {
                                "$cost 🪙"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementsTabContent(
    viewModel: MainViewModel,
    themeColor: Color,
    currentLang: Language
) {
    val achievements by viewModel.achievementsList.collectAsStateWithLifecycle()
    val unlockedCount = remember(achievements) { achievements.count { it.isUnlocked } }
    val totalCount = remember(achievements) { achievements.size }
    val progressFactor = if (totalCount > 0) unlockedCount.toFloat() / totalCount else 0f

    var achievementFilter by remember { mutableStateOf("ALL") }
    val filterOptions = remember(currentLang) {
        listOf(
            "ALL" to (if (currentLang == Language.RU) "Все" else "All"),
            "UNLOCKED" to (if (currentLang == Language.RU) "Открытые" else "Unlocked"),
            "LOCKED" to (if (currentLang == Language.RU) "В процессе" else "In Progress")
        )
    }

    val filteredAchievements = remember(achievements, achievementFilter) {
        when (achievementFilter) {
            "UNLOCKED" -> achievements.filter { it.isUnlocked }
            "LOCKED" -> achievements.filter { !it.isUnlocked }
            else -> achievements
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 12.dp,
            end = 16.dp,
            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hall of Fame Hero Card
        item {
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (currentLang == Language.RU) "ЗАЛ СЛАВЫ" else "HALL OF FAME",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentLang == Language.RU) {
                            "Разблокировано наград: $unlockedCount из $totalCount (${(progressFactor * 100).toInt()}%)"
                        } else {
                            "Unlocked achievements: $unlockedCount of $totalCount (${(progressFactor * 100).toInt()}%)"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { progressFactor },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                }
            }
        }

        // Filter Chips Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { (filterKey, filterLabel) ->
                    val isSelected = achievementFilter == filterKey
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            achievementFilter = filterKey
                        },
                        label = {
                            Text(
                                text = filterLabel,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColor,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = null
                    )
                }
            }
        }

        items(filteredAchievements, key = { it.id }) { ach ->
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (ach.isUnlocked) MaterialTheme.colorScheme.primaryContainer
                               else MaterialTheme.colorScheme.surfaceContainerHighest,
                        tonalElevation = 1.dp
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (ach.iconType) {
                                    "lines" -> Icons.Default.Extension
                                    "score" -> Icons.Default.Star
                                    "crown" -> Icons.Default.Shield
                                    "speed" -> Icons.Default.Settings
                                    "blast" -> Icons.Default.Extension
                                    "combo" -> Icons.Default.VolumeUp
                                    else -> Icons.Default.EmojiEvents
                                },
                                contentDescription = null,
                                tint = if (ach.isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer
                                       else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) ach.titleRu else ach.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                       else MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Text(
                                    text = "+${ach.pointsReward} 🪙",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        val badgeTag = when (ach.iconType) {
                            "all_unlocked", "crown" -> if (currentLang == Language.RU) "ЧЕМПИОН" else "CHAMPION"
                            "lines" -> if (currentLang == Language.RU) "МАСТЕР ЛИНИЙ" else "LINE CLEAR MASTER"
                            "score" -> if (currentLang == Language.RU) "ЧЕМПИОН ПО ОЧКАМ" else "HIGH SCORE CHAMPION"
                            "speed" -> if (currentLang == Language.RU) "СКОРОСТНОЙ РЕКОРДСМЕН" else "SPEED MASTER"
                            "blast" -> if (currentLang == Language.RU) "ЭКСПЕРТ ОЧИСТКИ" else "BLOCK BLAST EXPERT"
                            "combo" -> if (currentLang == Language.RU) "КОМБО-ЭКСПЕРТ" else "COMBO EXPERT"
                            else -> {
                                when (ach.id) {
                                    "rich_player" -> if (currentLang == Language.RU) "НАКОПИТЕЛЬ БАЛЛОВ" else "CREDITS COLLECTOR"
                                    "color_skin_collector" -> if (currentLang == Language.RU) "ДИЗАЙНЕР ИНТЕРФЕЙСА" else "INTERFACE DESIGNER"
                                    "rank_conqueror" -> if (currentLang == Language.RU) "ЛИДЕР РЕЙТИНГА" else "LEADERBOARD LEGEND"
                                    "extended_pioneer" -> if (currentLang == Language.RU) "ПЕРСПЕКТИВНЫЙ ИГРОК" else "PIONEERING OBSERVER"
                                    "speed_runner" -> if (currentLang == Language.RU) "СКОРОСТНОЙ АНАЛИТИК" else "SPEED RUNNER"
                                    else -> if (currentLang == Language.RU) "ИГРОВОЙ АКТИВ" else "ACTIVE PEER"
                                }
                            }
                        }
                        
                        Text(
                            text = badgeTag,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        Text(
                            text = if (currentLang == Language.RU) ach.descriptionRu else ach.descriptionEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        val fraction = if (ach.targetValue > 0) {
                            (ach.currentValue.toFloat() / ach.targetValue).coerceIn(0f, 1f)
                        } else 0f

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            LinearProgressIndicator(
                                progress = { fraction },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${ach.currentValue} / ${ach.targetValue}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NewTabContent(
    currentLang: Language,
    themeColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // О приложении / About App Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (currentLang == Language.RU) "О ПРИЛОЖЕНИИ" else "ABOUT APP",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = if (currentLang == Language.RU) 
                        "ZETA Tetris — это продвинутая версия классической головоломки, сочетающая в себе традиционный игровой процесс и инновационный режим ZETA Arena (Block Blast). В игре доступны кастомизация интерфейса, система скинов, звуковые паки, глобальные рекорды, синхронизация прогресса с облаком и полноценный мультиплеер с чатом."
                    else
                        "ZETA Tetris is an advanced evolution of the classic block puzzle game. It seamlessly blends traditional gameplay with the innovative ZETA Arena (Block Blast). Features include full interface customization, skin packs, unique soundboards, global high scores, secure cloud sync, and a multiplayer match lobby with live chat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (currentLang == Language.RU) "Версия" else "Version",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "2.0",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (currentLang == Language.RU) "Разработчик" else "Developer",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "FsFq",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Список изменений / Change Log Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (currentLang == Language.RU) "СПИСОК ИЗМЕНЕНИЙ" else "CHANGE LOG",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Version 2.0 Card Content
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Версия 2.0 (Текущая) / Version 2.0 (Current)",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    
                    val changes = if (currentLang == Language.RU) listOf(
                        "• Добавлена строгая верификация почты через Firebase (защита онлайн-функций)",
                        "• Добавлен переключатель видимости вкладки 'Новое' в настройках (раздел 'Система')",
                        "• Кэширование FirebaseAuth для оптимизации скорости работы",
                        "• Полная реструктуризация кода с добавлением двуязычных неформальных комментариев",
                        "• Балансировка экономики: стоимость открытия кейсов увеличена на 25%, награды скорректированы",
                        "• Переименованы научно-фантастические и ИИ-достижения в строгие классические названия",
                        "• Ужесточена проверка проигрыша (строго по ряду 2 или выше)",
                        "• Немедленное удаление сессии при поражении для предотвращения дюпа монет"
                    ) else listOf(
                        "• Added strict Firebase Email Verification system (verifying email gates online play and chat)",
                        "• Added toggle switch for the 'New' tab visibility in settings (System tab)",
                        "• Cached FirebaseAuth instance globally to eliminate redundant service queries",
                        "• Full codebase review with helpful, informal RU/EN comments",
                        "• Economy balancing: increased crate costs by 25% and adjusted reward rates",
                        "• Rebranded space/AI achievements into professional, classic names",
                        "• Implemented strict game over rules (immediately triggers when block reaches row 2)",
                        "• Added immediate saved game destruction on defeat to fix coin exploits"
                    )

                    changes.forEach { change ->
                        Text(
                            text = change,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Version 1.0 Card Content
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Версия 1.0 / Version 1.0",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = if (currentLang == Language.RU) 
                            "• Первый релиз: режимы Тетрис и ZETA, лобби мультиплеера, аватары, рамки, теги и профиль."
                        else
                            "• Initial launch: Tetris & ZETA gameplay modes, multiplayer matchmaking lobbies, custom avatars, frame store, and tags.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class RankData(val id: String, val cost: Int, val description: String)
data class SkinData(val id: String, val cost: Int, val displayName: String, val description: String)
data class GameModeStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val emoji: String)
data class CubeSkinStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val previewEmoji: String)
data class AvatarFrameStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val rarityColor: Color)
data class PlayerBadgeStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val emoji: String)
data class ThemeStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val themeColor: Color)
data class FontStoreData(val id: String, val cost: Int, val displayName: String, val description: String)
data class ControlButtonStyleStoreData(val id: String, val cost: Int, val displayName: String, val description: String)

