package com.example.ui

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
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

    LaunchedEffect(loginError) {
        if (loginError != null) {
            viewModel.triggerAudioFeedback("error")
        }
    }

    LaunchedEffect(loginSuccessMessage) {
        if (loginSuccessMessage != null) {
            viewModel.triggerAudioFeedback("success")
        }
    }

    var authModeIsRegister by remember { mutableStateOf(false) }
    var inputUsername by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("") }
    var inputPassword by remember { mutableStateOf("") }

    var infoMessage by remember { mutableStateOf<String?>(null) }
    var infoIsError by remember { mutableStateOf(false) }

    val isAuthLoading by viewModel.isAuthLoading.collectAsStateWithLifecycle()

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

    val themesList = listOf(
        ThemeStoreData("indigo", 0, if (currentLang == Language.RU) "ИНДИГО" else "INDIGO ACCENT", if (currentLang == Language.RU) "Классический космический индиго" else "Classic space indigo color theme", Color(0xFF6366F1)),
        ThemeStoreData("neon", 0, if (currentLang == Language.RU) "КИБЕР НЕОН" else "CYAN NEON", if (currentLang == Language.RU) "Высококонтрастный бирюзовый неон" else "High-contrast cyan neon accent theme", Color(0xFF00FFCC)),
        ThemeStoreData("red", 0, if (currentLang == Language.RU) "АЛЫЙ ИМПУЛЬС" else "RED PULSE", if (currentLang == Language.RU) "Агрессивный красный дизайн" else "Aggressive warning red design theme", Color(0xFFFF5555)),
        ThemeStoreData("emerald", 300, if (currentLang == Language.RU) "ИЗУМРУД" else "EMERALD GLOW", if (currentLang == Language.RU) "Спокойный зеленый оттенок" else "Calm high-tech green accent glow", Color(0xFF10B981)),
        ThemeStoreData("amber", 300, if (currentLang == Language.RU) "ЯНТАРЬ" else "AMBER TRANS", if (currentLang == Language.RU) "Янтарный оранжевый монохром" else "Amber orange digital dashboard feel", Color(0xFFF59E0B)),
        ThemeStoreData("rose", 500, if (currentLang == Language.RU) "РОЗОВЫЙ ЗАКАТ" else "ROSE SUNSET", if (currentLang == Language.RU) "Приятный малиново-розовый неон" else "Synthwave raspberry pink neon design", Color(0xFFF43F5E)),
        ThemeStoreData("sky", 500, if (currentLang == Language.RU) "НЕБЕСНАЯ СИНЕВА" else "SKY BREEZE", if (currentLang == Language.RU) "Светлый небесно-голубой цвет" else "Bright sky blue visual elements", Color(0xFF0EA5E9)),
        ThemeStoreData("orange", 600, if (currentLang == Language.RU) "АПЕЛЬСИН" else "ORANGE OVERDRIVE", if (currentLang == Language.RU) "Энергичный сочный оранжевый" else "Energetic juice orange outline layout", Color(0xFFFF5722)),
        ThemeStoreData("cyber_pink", 800, if (currentLang == Language.RU) "КИБЕР РОЗОВЫЙ" else "CYBER PINK", if (currentLang == Language.RU) "Элитный ядовитый розовый" else "Premium cyber pink neon highlight", Color(0xFFFF007F)),
        ThemeStoreData("toxic_green" , 700, if (currentLang == Language.RU) "ТОКСИЧНЫЙ ЗЕЛЕНЫЙ" else "TOXIC GREEN", if (currentLang == Language.RU) "Яркий радиоактивный зеленый" else "Hyper bright radioactive green look", Color(0xFF39FF14)),
        ThemeStoreData("gold", 1000, if (currentLang == Language.RU) "ЧИСТОЕ ЗОЛОТО" else "PURE GOLD MINE", if (currentLang == Language.RU) "Элитное премиум-золото" else "Ultra prestige golden matrix overlay theme", Color(0xFFFFD700))
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
            "chrono_gl" -> Brush.sweepGradient(listOf(Color(0xFFFF00FF), Color(0xFF00FFFF), Color(0xFFFF00FF)))
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

    fun selectSoundPack(packId: String, cost: Int) {
        if (purchasedSoundPacks.contains(packId)) {
            viewModel.setEquippedSoundPack(packId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            triggerMessage(if (currentLang == Language.RU) "Звуковой пак успешно выбран" else "Sound pack equipped.")
            return
        }
        if (credits >= cost) {
            val updated = purchasedSoundPacks.toMutableSet().apply { add(packId) }
            viewModel.setPurchasedSoundPacks(updated)
            viewModel.setEquippedSoundPack(packId)
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            triggerMessage(if (currentLang == Language.RU) "Звуковой пак куплен и активирован" else "Sound pack purchased and equipped.")
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            triggerMessage(if (currentLang == Language.RU) "Недостаточно средств" else "Insufficient funds", isError = true)
        }
    }

    fun selectTheme(themeId: String, cost: Int) {
        if (purchasedThemes.contains(themeId)) {
            viewModel.setThemeColor(themeId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            triggerMessage(if (currentLang == Language.RU) "Тема успешно выбрана" else "Color theme equipped.")
            return
        }
        if (credits >= cost) {
            val updated = purchasedThemes.toMutableSet().apply { add(themeId) }
            viewModel.setPurchasedThemes(updated)
            viewModel.setThemeColor(themeId)
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            triggerMessage(if (currentLang == Language.RU) "Тема куплена и активирована" else "Color theme purchased and equipped.")
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
    val pagerState = rememberPagerState(initialPage = if (initialTab == 99) 0 else initialTab) { 3 }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(top = 8.dp)
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = when (pagerState.currentPage) {
                        0 -> if (isLoggedIn) (if (currentLang == Language.RU) "ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ" else "USER PROFILE") else (if (currentLang == Language.RU) "АВТОРИЗАЦИЯ" else "AUTHORIZATION")
                        1 -> if (currentLang == Language.RU) "МАГАЗИН" else "STORE"
                        else -> if (currentLang == Language.RU) "ДОСТИЖЕНИЯ" else "ACHIEVEMENTS"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.align(Alignment.Center)
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Credits",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$credits 🪙",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.PrimaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                        )
                    }
                ) {
                    Tab(
                        selected = pagerState.currentPage == 0,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        },
                        text = {
                            Text(
                                text = if (currentLang == Language.RU) "ПРОФИЛЬ" else "PROFILE",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                maxLines = 1,
                                softWrap = false,
                                fontWeight = if (pagerState.currentPage == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = pagerState.currentPage == 1,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        },
                        text = {
                            Text(
                                text = if (currentLang == Language.RU) "МАГАЗИН" else "STORE",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                maxLines = 1,
                                softWrap = false,
                                fontWeight = if (pagerState.currentPage == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = pagerState.currentPage == 2,
                        onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(2)
                            }
                        },
                        text = {
                            Text(
                                text = if (currentLang == Language.RU) "ДОСТИЖЕНИЯ" else "ACHIEVEMENTS",
                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                                maxLines = 1,
                                softWrap = false,
                                fontWeight = if (pagerState.currentPage == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }



                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    userScrollEnabled = true
                ) { page ->
                    when (page) {
                        0 -> {
                        if (!isLoggedIn) {
                            // REGISTRATION & AUTHORIZATION VIEW
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    shape = RoundedCornerShape(28.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f)
                                    ),
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            )
                                        )
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth()) {
                                        Column(
                                            modifier = Modifier
                                                .padding(24.dp)
                                                .verticalScroll(rememberScrollState()),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = if (authModeIsRegister) {
                                                    if (currentLang == Language.RU) "РЕГИСТРАЦИЯ" else "REGISTRATION"
                                                } else {
                                                    if (currentLang == Language.RU) "ВХОД В АККАУНТ" else "ACCOUNT LOGIN"
                                                },
                                                style = MaterialTheme.typography.headlineSmall.copy(
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 1.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                textAlign = TextAlign.Center
                                            )
                                            
                                            Spacer(modifier = Modifier.height(6.dp))
                                            
                                            Text(
                                                text = if (authModeIsRegister) {
                                                    if (currentLang == Language.RU) "Создайте новый профиль для сохранения статистики" else "Create a new profile to save your stats"
                                                } else {
                                                    if (currentLang == Language.RU) "Войдите в сеть для синхронизации прогресса" else "Sign in to synchronize your progress"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 12.dp)
                                            )
                                            
                                            Spacer(modifier = Modifier.height(20.dp))

                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(32.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                                    .padding(4.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(28.dp))
                                                        .background(
                                                            if (!authModeIsRegister) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                            else Color.Transparent
                                                        )
                                                        .border(
                                                            width = if (!authModeIsRegister) 1.dp else 0.dp,
                                                            color = if (!authModeIsRegister) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                                                            shape = RoundedCornerShape(28.dp)
                                                        )
                                                        .clickable { 
                                                            authModeIsRegister = false
                                                            viewModel.clearLoginMessages()
                                                        }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = if (currentLang == Language.RU) "ВХОД" else "SIGN IN",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (!authModeIsRegister) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clip(RoundedCornerShape(28.dp))
                                                        .background(
                                                            if (authModeIsRegister) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                                            else Color.Transparent
                                                        )
                                                        .border(
                                                            width = if (authModeIsRegister) 1.dp else 0.dp,
                                                            color = if (authModeIsRegister) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else Color.Transparent,
                                                            shape = RoundedCornerShape(28.dp)
                                                        )
                                                        .clickable { 
                                                            authModeIsRegister = true
                                                            viewModel.clearLoginMessages()
                                                        }
                                                        .padding(vertical = 10.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = if (currentLang == Language.RU) "РЕГИСТРАЦИЯ" else "SIGN UP",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (authModeIsRegister) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(20.dp))

                                            OutlinedTextField(
                                                value = inputUsername,
                                                onValueChange = { inputUsername = it },
                                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                                label = { Text(if (authModeIsRegister) (if (currentLang == Language.RU) "Никнейм" else "Nickname") else (if (currentLang == Language.RU) "Никнейм или Email" else "Nickname or Email")) },
                                                singleLine = true,
                                                shape = RoundedCornerShape(16.dp),
                                                colors = OutlinedTextFieldDefaults.colors(
                                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                    focusedContainerColor = Color.Transparent,
                                                    unfocusedContainerColor = Color.Transparent
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            if (authModeIsRegister) {
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
                                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                        focusedContainerColor = Color.Transparent,
                                                        unfocusedContainerColor = Color.Transparent
                                                    ),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
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
                                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                                    focusedContainerColor = Color.Transparent,
                                                    unfocusedContainerColor = Color.Transparent
                                                ),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Spacer(modifier = Modifier.height(14.dp))

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
                                                
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f))
                                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = localizedErr,
                                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(14.dp))
                                            }

                                            loginSuccessMessage?.let { success ->
                                                val localizedSucc = if (currentLang == Language.RU) {
                                                    when {
                                                        success.contains("created") || success.contains("успешно создан") -> "Аккаунт успешно создан!"
                                                        success.contains("success") || success.contains("вход") -> "Успешный вход!"
                                                        else -> success
                                                    }
                                                } else success
                                                
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(Color(0xFFE8F5E9))
                                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = localizedSucc,
                                                        color = Color(0xFF1B5E20),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(14.dp))
                                            }

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
                                                    .height(50.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary
                                                )
                                            ) {
                                                Text(
                                                    text = if (authModeIsRegister) {
                                                        if (currentLang == Language.RU) "СОЗДАТЬ АККАУНТ" else "CREATE ACCOUNT"
                                                    } else {
                                                        if (currentLang == Language.RU) "ВОЙТИ В СИСТЕМУ" else "SIGN IN"
                                                    },
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 1.sp,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 15.sp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(18.dp))

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                HorizontalDivider(
                                                    modifier = Modifier.weight(1f),
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                                )
                                                Text(
                                                    text = if (currentLang == Language.RU) "ИЛИ" else "OR",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    modifier = Modifier.padding(horizontal = 12.dp)
                                                )
                                                HorizontalDivider(
                                                    modifier = Modifier.weight(1f),
                                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(18.dp))

                                            OutlinedButton(
                                                onClick = {
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    viewModel.clearLoginMessages()
                                                    val signInIntent = googleSignInClient.signInIntent
                                                    googleSignInLauncher.launch(signInIntent)
                                                },
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(50.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                                                colors = ButtonDefaults.outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.onSurface
                                                )
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    GoogleIcon(modifier = Modifier.size(18.dp))
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = if (currentLang == Language.RU) "Войти через Google" else "Continue with Google",
                                                        fontWeight = FontWeight.Bold,
                                                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 14.sp)
                                                    )
                                                }
                                            }
                                        }

                                        if (isAuthLoading) {
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize()
                                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.75f))
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
                                        // Custom Background Image if present
                                        val bgBitmap = remember(playerName, bgChangeCounter) {
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

                                        if (bgBitmap != null) {
                                            Image(
                                                bitmap = bgBitmap,
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

                                            // Avatar representation using custom photo or name initials (Material 3 style)
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier.size(112.dp)
                                            ) {
                                                // Glow background for neon, gold, chrono
                                                if (equippedAvatarFrame in listOf("neon_ae", "gold_ma", "chrono_gl")) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(96.dp)
                                                            .graphicsLayer {
                                                                scaleX = pulseScale
                                                                scaleY = pulseScale
                                                                alpha = 0.45f
                                                            }
                                                            .clip(RoundedCornerShape(50))
                                                            .background(
                                                                brush = avatarFrameBorderBrush
                                                            )
                                                    )
                                                }

                                                // Rotating Outer Border Ring for extra beauty
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

                                                // Static Inner Avatar Container
                                                Box(
                                                    modifier = Modifier
                                                        .size(90.dp)
                                                        .clip(RoundedCornerShape(50))
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(
                                                                    parsedAvatarBgColor,
                                                                    parsedAvatarBgColor.copy(alpha = 0.65f)
                                                                )
                                                            )
                                                        )
                                                        .border(
                                                            width = 1.dp,
                                                            color = Color.White.copy(alpha = 0.15f),
                                                            shape = RoundedCornerShape(50)
                                                        )
                                                        .clickable {
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            viewModel.triggerAudioFeedback("click")
                                                            showAvatarDialog = true
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    val customAvatarBitmap = remember(playerName, avatarChangeCounter) {
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

                                                    if (customAvatarBitmap != null) {
                                                        Image(
                                                            bitmap = customAvatarBitmap,
                                                            contentDescription = "Avatar",
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        Icon(
                                                            imageVector = Icons.Default.Person,
                                                            contentDescription = "Placeholder Avatar",
                                                            modifier = Modifier.size(56.dp),
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                        )
                                                    }


                                                }
                                            }
    
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
                                            Text(
                                                text = playerName.uppercase(),
                                                style = MaterialTheme.typography.titleLarge.copy(
                                                    fontSize = if (playerName.length > 15) 15.sp else if (playerName.length > 10) 18.sp else 22.sp,
                                                    brush = Brush.linearGradient(
                                                        colors = listOf(Color(0xFFE94560), Color(0xFFFF0055), Color(0xFFFF7B00))
                                                    )
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
    
                                        Text(
                                            text = Translations.getLocalizedRank(onlineTier, currentLang),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
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
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = if (currentLang == Language.RU) "УРОВЕНЬ МАСТЕРСТВА: $masteryLevel" else "MASTERY LEVEL: $masteryLevel",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "$currentLevelXp / $levelXpBound XP",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        LinearProgressIndicator(
                                            progress = { xpPercentage },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    }
                                }
    

                                // Stats telemetries
                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Column(modifier = Modifier.padding(20.dp)) {
                                        Text(
                                            text = if (currentLang == Language.RU) "ТЕЛЕМЕТРИЯ И СТАТИСТИКА" else "TELEMETRY AND STATISTICS",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                        ) {
                                            // Score High
                                            OutlinedCard(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = if (currentLang == Language.RU) "РЕКОРД ОЧКОВ" else "HIGH SCORE",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "$statsHighScore",
                                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                            // Lines Cleared
                                            OutlinedCard(
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = if (currentLang == Language.RU) "ЛИНИЙ ОЧИЩЕНО" else "LINES CLEARED",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "$statsClearedLines",
                                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
    
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        // Reset Button
                            OutlinedButton(
                                onClick = {
                                    viewModel.triggerAudioFeedback("click")
                                    viewModel.resetProfileStats()
                                    triggerMessage(if (currentLang == Language.RU) "Статистика сброшена." else "Stats reset.")
                                },
                                modifier = Modifier.align(Alignment.End),
                                shape = MaterialTheme.shapes.small,
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (currentLang == Language.RU) "Сбросить статистику" else "Reset stats",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Log out button
                    Button(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.switchAccount("Player 1", "BRONZE", 500)
                            inputUsername = ""
                            inputPassword = ""
                            viewModel.clearLoginMessages()
                            triggerMessage(if (currentLang == Language.RU) "Сессия завершена." else "Session ended.")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == Language.RU) "ВЫЙТИ ИЗ АККАУНТА" else "LOG OUT",
                            fontWeight = FontWeight.Bold
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
                    AvatarFrameStoreData("standard", 0, if (currentLang == Language.RU) "По умолчанию" else "Default Frame", if (currentLang == Language.RU) "Классическая тонкая рамка" else "Clean subtle indicator matching color", Color.Gray),
                    AvatarFrameStoreData("neon_ae", 350, if (currentLang == Language.RU) "Неоновая Эгида" else "Neon Aegis Pulse", if (currentLang == Language.RU) "Пульсирующий лазерный ореол" else "Dual-tone hyper bright glowing neon barrier", Color(0xFF00FFCC)),
                    AvatarFrameStoreData("gold_ma", 600, if (currentLang == Language.RU) "Золотая Матрица" else "Golden Matrix Aura", if (currentLang == Language.RU) "Элитное золотое обрамление" else "Prestige auric shell representing dominance", Color(0xFFFFD700)),
                    AvatarFrameStoreData("chrono_gl", 850, if (currentLang == Language.RU) "Глитч Спектр" else "Chrono Giga-Glitch", if (currentLang == Language.RU) "Сдвинутые цветовые каналы неона" else "Time distortion anomaly: desynchronized neon channels", Color(0xFFFF00FF)),
                    AvatarFrameStoreData("omega_ti", 1100, if (currentLang == Language.RU) "Металлический Титан" else "Titanium Singularity", if (currentLang == Language.RU) "Толстая броня из темного сплава" else "Thick titanium plating with deep space coating", Color(0xFF90A4AE))
                )
            }
            val playerBadgesList = remember {
                listOf(
                    PlayerBadgeStoreData("none", 0, if (currentLang == Language.RU) "Без титула" else "No Title", if (currentLang == Language.RU) "Стандартное имя" else "Standard name style", ""),
                    PlayerBadgeStoreData("node", 2500, if (currentLang == Language.RU) "РЕКРУТ" else "RECRUIT", if (currentLang == Language.RU) "Статус новобранца в системе" else "Rookie player title status", ""),
                    PlayerBadgeStoreData("lord", 5000, if (currentLang == Language.RU) "ВЕТЕРАН" else "VETERAN", if (currentLang == Language.RU) "Ветеран классических игр" else "Veteran player title status", ""),
                    PlayerBadgeStoreData("cosmic_overlord", 8000, if (currentLang == Language.RU) "ЭЛИТА" else "ELITE", if (currentLang == Language.RU) "Элитный статус мастера" else "Elite class master status", ""),
                    PlayerBadgeStoreData("ai_consensus", 12000, if (currentLang == Language.RU) "ЛЕГЕНДА" else "LEGEND", if (currentLang == Language.RU) "Легендарный чемпион сети" else "Legendary network champion status", "")
                )
            }
            val soundPacksList = remember {
                listOf(
                    SoundPackStoreData("arcade", 0, if (currentLang == Language.RU) "Классическая Аркада" else "Standard Arcade FX", if (currentLang == Language.RU) "Ностальгические щелчки оригинальной консоли" else "Vintage blips and laser chirps from original console", "1.2 MB"),
                    SoundPackStoreData("synthwave", 400, if (currentLang == Language.RU) "Синтвейв Хронология" else "Synthwave Retro 198X", if (currentLang == Language.RU) "Аналоговые пэды и неоновые басы" else "Chilled warm polyphonic synthesizer pads and sweeps", "4.8 MB"),
                    SoundPackStoreData("cyber_metal", 650, if (currentLang == Language.RU) "Тяжелый Бас" else "Industrial Cyber Bass", if (currentLang == Language.RU) "Резкие зажигания тяжелых металлических пластин" else "Grinding metal mechanics and heavy physical impacts", "6.2 MB"),
                    SoundPackStoreData("ai_voice", 900, if (currentLang == Language.RU) "Помощник Саманта" else "AI Vocal Assistant Samantha", if (currentLang == Language.RU) "Комментирование игрового процесса голосом ИИ" else "spoken tactical notifications and combo callouts", "8.9 MB")
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isLoggedIn) {
                    item {
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
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

                // 2. GRID SCHEMES
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

                // 3. CUBE STYLES
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

                // 4. GAME MODES
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

                // 5. AVATAR FRAMES
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

                // 6. PLAYER TITLES
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
                // 8. COLOR THEMES
                item {
                    StoreSectionHeader(
                        title = if (currentLang == Language.RU) "ТЕМА ОФОРМЛЕНИЯ" else "INTERFACE COLOR THEMES",
                        icon = Icons.Default.ColorLens,
                        purchasedCount = purchasedThemes.size,
                        totalCount = themesList.size,
                        currentLang = currentLang
                    )
                }
                items(themesList) { theme ->
                    val isEquipped = activeThemeKey == theme.id
                    val isOwned = purchasedThemes.contains(theme.id)
                    StoreItemCard(
                        icon = Icons.Default.ColorLens,
                        category = if (currentLang == Language.RU) "Тема" else "Color Theme",
                        title = theme.displayName,
                        description = theme.description,
                        isActive = isEquipped,
                        isOwned = isOwned,
                        cost = theme.cost,
                        currentLang = currentLang,
                        onAction = {
                            if (isEquipped) {
                                viewModel.setThemeColor("indigo")
                                triggerMessage(if (currentLang == Language.RU) "Тема сброшена" else "Color theme reset to indigo.")
                            } else {
                                selectTheme(theme.id, theme.cost)
                            }
                        }
                    )
                }

                // 10. CONTROL BUTTON STYLES
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
                
                // 11. CUSTOM LEADERBOARD TAG
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
                        cost = 200000,
                        currentLang = currentLang,
                        onAction = {
                            if (isOwned) {
                                triggerMessage(if (currentLang == Language.RU) "Уже приобретено! Настройте в настройках профиля." else "Already purchased! Edit it in profile settings.")
                            } else {
                                if (credits >= 200000) {
                                    viewModel.spendCredits(200000)
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
                    }
                }
                }

            infoMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (infoIsError) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.secondaryContainer
                        )
                        .border(
                            width = 1.dp,
                            color = (if (infoIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary).copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = msg,
                        color = if (infoIsError) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onSecondaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
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
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { 
                            showAvatarDialog = false 
                            viewModel.clearUpdateMessages()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentLang == Language.RU) "НАСТРОЙКИ ПРОФИЛЯ" else "PROFILE SETTINGS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 1. Avatar Section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(112.dp)
                            ) {
                                // Rotating Outer Border Ring for extra beauty
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

                                val customAvatarBitmap = remember(playerName, avatarChangeCounter) {
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

                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                        .border(
                                            width = 1.dp,
                                            color = Color.White.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(50)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (customAvatarBitmap != null) {
                                        Image(
                                            bitmap = customAvatarBitmap,
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
                                modifier = Modifier.fillMaxWidth(0.9f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { avatarPickerLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Portrait, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLang == Language.RU) "Аватар" else "Set Avatar",
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = { bgPickerLauncher.launch("image/*") },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (currentLang == Language.RU) "Фон карты" else "Set Card BG",
                                        fontSize = 12.sp
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
                                    modifier = Modifier.fillMaxWidth(0.9f),
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
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(if (currentLang == Language.RU) "Сбросить аватар" else "Clear Avatar", fontSize = 10.sp)
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
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(if (currentLang == Language.RU) "Сбросить фон" else "Clear BG", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // 2. Nickname Section
                        Column(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "СМЕНИТЬ НИКНЕЙМ" else "CHANGE NICKNAME",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            var editNickNameInput by remember { mutableStateOf(playerName) }

                            OutlinedTextField(
                                value = editNickNameInput,
                                onValueChange = { editNickNameInput = it },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                label = { Text(if (currentLang == Language.RU) "Новый никнейм" else "New Nickname") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
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
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(if (currentLang == Language.RU) "Сохранить ник" else "Save Nickname")
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // 3. Email Section
                        Column(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val currentUser = FirebaseAuth.getInstance().currentUser
                            val currentEmail = currentUser?.email ?: ""
                            
                            Text(
                                text = if (currentEmail.isEmpty()) {
                                    if (currentLang == Language.RU) "ПРИВЯЗАТЬ ПОЧТУ" else "BIND EMAIL"
                                } else {
                                    if (currentLang == Language.RU) "ИЗМЕНИТЬ ПОЧТУ" else "CHANGE EMAIL"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            var editEmailInput by remember { mutableStateOf(currentEmail) }

                            OutlinedTextField(
                                value = editEmailInput,
                                onValueChange = { editEmailInput = it },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                label = { Text(if (currentLang == Language.RU) "Электронная почта" else "Email Address") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
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
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    if (currentEmail.isEmpty()) {
                                        if (currentLang == Language.RU) "Привязать" else "Bind Email"
                                    } else {
                                        if (currentLang == Language.RU) "Обновить" else "Update Email"
                                    }
                                )
                            }
                        }



                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        // 5. Custom Leaderboard Tag Section
                        Column(
                            modifier = Modifier.fillMaxWidth(0.9f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) "ТЕГ В ТАБЛИЦЕ РЕКОРДОВ" else "LEADERBOARD CUSTOM TAG",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            val customTagUnlocked by viewModel.customTagUnlocked.collectAsStateWithLifecycle()
                            val customTagVal by viewModel.customTag.collectAsStateWithLifecycle()
                            
                            var editTagInput by remember(customTagVal) { mutableStateOf(customTagVal) }

                            if (!customTagUnlocked) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = if (currentLang == Language.RU) 
                                            "🔒 Функция заблокирована. Купите Кастомный тег в магазине за 200 000 🪙."
                                        else 
                                            "🔒 Feature locked. Purchase Custom Tag in the store for 200,000 🪙.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                OutlinedTextField(
                                    value = editTagInput,
                                    onValueChange = { if (it.length <= 6) editTagInput = it },
                                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) },
                                    label = { Text(if (currentLang == Language.RU) "Кастомный тег (макс. 6 симв.)" else "Custom Tag (max 6 chars)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    onClick = {
                                        viewModel.setCustomTag(editTagInput)
                                        viewModel.triggerAudioFeedback("success")
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(if (currentLang == Language.RU) "Сохранить тег" else "Save Tag")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                    }
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
            .padding(top = 16.dp, bottom = 8.dp)
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
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            if (purchasedCount >= 0 && totalCount >= 0) {
                Text(
                    text = if (currentLang == Language.RU) "Куплено $purchasedCount из $totalCount" else "Owned $purchasedCount of $totalCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
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

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                             else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left rarity border stripe
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(5.dp)
                    .background(rarityColor)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = category.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(rarityColor.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(BorderStroke(0.5.dp, rarityColor.copy(alpha = 0.5f)), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = rarityText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = rarityColor
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                val isRank = category.equals("Rank", ignoreCase = true) || category.equals("Ранг", ignoreCase = true)
                if (!(isRank && isOwned)) {
                    val buttonEnabled = when {
                        isRank -> !isOwned
                        else -> !isLocked
                    }
                    Button(
                        onClick = onAction,
                        enabled = buttonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRank && isOwned) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                             else if (isActive) MaterialTheme.colorScheme.primary
                                             else if (isOwned) MaterialTheme.colorScheme.secondaryContainer
                                             else if (isLocked) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                             else MaterialTheme.colorScheme.primary,
                            contentColor = if (isRank && isOwned) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                           else if (isActive) MaterialTheme.colorScheme.onPrimary
                                           else if (isOwned) MaterialTheme.colorScheme.onSecondaryContainer
                                           else if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                           else MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = MaterialTheme.shapes.small,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (isRank && isOwned) {
                                if (currentLang == Language.RU) "РАЗБЛОКИРОВАН" else "UNLOCKED"
                            } else if (isActive) {
                                if (currentLang == Language.RU) "ВКЛ" else "ON"
                            } else if (isOwned) {
                                if (currentLang == Language.RU) "ВЫКЛ" else "OFF"
                            } else if (isLocked) {
                                if (currentLang == Language.RU) "БЛОК" else "LOCKED"
                            } else {
                                if (currentLang == Language.RU) "$cost 🪙" else "$cost 🪙"
                            },
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 16.dp,
            end = 16.dp,
            bottom = 16.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (currentLang == Language.RU) "ЗАЛ СЛАВЫ" else "HALL OF FAME",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (currentLang == Language.RU) {
                            "Разблокировано наград: $unlockedCount из $totalCount"
                        } else {
                            "Unlocked achievements: $unlockedCount of $totalCount"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { progressFactor },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        item {
            StoreSectionHeader(
                title = if (currentLang == Language.RU) "СПИСОК ДОСТИЖЕНИЙ" else "ACHIEVEMENT LIST"
            )
        }

        items(achievements, key = { it.id }) { ach ->
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (ach.isUnlocked) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            ),
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
                                   else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentLang == Language.RU) ach.titleRu else ach.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "+${ach.pointsReward} 🪙",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }

                        val badgeTag = when (ach.iconType) {
                            "all_unlocked" -> if (currentLang == Language.RU) "АБСОЛЮТНЫЙ ЧЕМПИОН" else "OMNIPOTENT CHAMPION"
                            "lines" -> if (currentLang == Language.RU) "МАСТЕР ЛИНИЙ" else "LINE CLEAR MASTER"
                            "score" -> if (currentLang == Language.RU) "ЧЕМПИОН ПО ОЧКАМ" else "HIGH SCORE CHAMPION"
                            "crown" -> if (currentLang == Language.RU) "ГРОССМЕЙСТЕР ИГРЫ" else "GAME GRANDMASTER"
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
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${ach.currentValue} / ${ach.targetValue}",
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
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

data class RankData(val id: String, val cost: Int, val description: String)
data class SkinData(val id: String, val cost: Int, val displayName: String, val description: String)
data class GameModeStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val emoji: String)
data class CubeSkinStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val previewEmoji: String)
data class AvatarFrameStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val rarityColor: Color)
data class PlayerBadgeStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val emoji: String)
data class SoundPackStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val sizeStr: String)
data class ThemeStoreData(val id: String, val cost: Int, val displayName: String, val description: String, val themeColor: Color)
data class FontStoreData(val id: String, val cost: Int, val displayName: String, val description: String)
data class ControlButtonStyleStoreData(val id: String, val cost: Int, val displayName: String, val description: String)

@Composable
fun GoogleIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val strokeW = w * 0.22f
        val r = (w - strokeW) / 2f
        val cx = w / 2f
        val cy = w / 2f
        val arcSize = Size(r * 2, r * 2)
        val arcTopLeft = Offset(cx - r, cy - r)

        // Red (Top)
        drawArc(
            color = Color(0xFFEA4335),
            startAngle = 180f + 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeW)
        )
        // Yellow (Left)
        drawArc(
            color = Color(0xFFFBBC05),
            startAngle = 180f - 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeW)
        )
        // Green (Bottom)
        drawArc(
            color = Color(0xFF34A853),
            startAngle = 45f,
            sweepAngle = 90f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeW)
        )
        // Blue (Right)
        drawArc(
            color = Color(0xFF4285F4),
            startAngle = -45f, sweepAngle = 90f,
            useCenter = false,
            topLeft = arcTopLeft,
            size = arcSize,
            style = Stroke(width = strokeW)
        )
        // Horizontal bar
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(cx, cy - strokeW / 2f),
            size = Size(r + strokeW / 2f, strokeW)
        )
    }
}
