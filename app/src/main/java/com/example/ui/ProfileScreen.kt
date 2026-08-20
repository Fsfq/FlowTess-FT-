package com.example.ui

import com.example.MasterySystem
import com.example.LevelReward
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
import androidx.compose.material.icons.filled.Diamond
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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Bolt
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
    val onlineRating by viewModel.onlineRating.collectAsStateWithLifecycle()
    val winStreak by viewModel.winStreak.collectAsStateWithLifecycle()
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

    val ranksList = remember(currentLang) {
        listOf(
            RankData("BRONZE", ShopPrices.RANK_BRONZE, when (currentLang) {
                Language.RU -> "Начальный ранг игрока"
                Language.UA -> "Початковий ранг гравця"
                Language.KK -> "Бастапқы ойыншы дәрежесі"
                Language.DE -> "Anfänger-Rang"
                Language.ZH -> "初始段位"
                else -> "Starting player rank"
            }),
            RankData("SILVER", ShopPrices.RANK_SILVER, when (currentLang) {
                Language.RU -> "Серебряная лига игрока"
                Language.UA -> "Срібна ліга гравця"
                Language.KK -> "Күміс лига ойыншысы"
                Language.DE -> "Silber-Liga"
                Language.ZH -> "白银联赛"
                else -> "Silver tier member"
            }),
            RankData("GOLD", ShopPrices.RANK_GOLD, when (currentLang) {
                Language.RU -> "Золотая лига опытных бойцов"
                Language.UA -> "Золота ліга досвідчених бійців"
                Language.KK -> "Тәжірибелі ойыншылардың алтын лигасы"
                Language.DE -> "Gold-Liga"
                Language.ZH -> "黄金精英联赛"
                else -> "Gold league experienced tier"
            }),
            RankData("PLATINUM", ShopPrices.RANK_PLATINUM, when (currentLang) {
                Language.RU -> "Платиновый мастер комбинаций"
                Language.UA -> "Платиновий майстер комбінацій"
                Language.KK -> "Платина комбинациялар шебері"
                Language.DE -> "Platin-Kombinationsmeister"
                Language.ZH -> "铂金连击大师"
                else -> "Platinum combination master"
            }),
            RankData("DIAMOND", ShopPrices.RANK_DIAMOND, when (currentLang) {
                Language.RU -> "Алмазная лига консенсуса"
                Language.UA -> "Діамантова ліга консенсусу"
                Language.KK -> "Гауһар консенсус лигасы"
                Language.DE -> "Diamant-Liga"
                Language.ZH -> "璀璨钻石联赛"
                else -> "Diamond consensus league"
            }),
            RankData("MASTER", ShopPrices.RANK_MASTER, when (currentLang) {
                Language.RU -> "Магистр пространственной сетки"
                Language.UA -> "Магістр просторової сітки"
                Language.KK -> "Кеңістіктік тор магистрі"
                Language.DE -> "Meister des Gitters"
                Language.ZH -> "网格空间宗师"
                else -> "Master of spatial matrix grid"
            }),
            RankData("GRANDMASTER", ShopPrices.RANK_GRANDMASTER, when (currentLang) {
                Language.RU -> "Гроссмейстер тактических дуэлей"
                Language.UA -> "Гросмейстер тактичних дуелей"
                Language.KK -> "Тактикалық дуэль гроссмейстері"
                Language.DE -> "Großmeister der Duelle"
                Language.ZH -> "战术对决大宗师"
                else -> "Grandmaster of tactical gameplay"
            }),
            RankData("CHALLENGER", ShopPrices.RANK_CHALLENGER, when (currentLang) {
                Language.RU -> "Легенда абсолютного топа"
                Language.UA -> "Легенда абсолютного топу"
                Language.KK -> "Абсолютті топтың аңызы"
                Language.DE -> "Legende der Rangliste"
                Language.ZH -> "巅峰至尊传奇"
                else -> "Legendary top challenger status"
            })
        )
    }

    val skinsList = remember(currentLang) {
        listOf(
            SkinData("cyberpunk", ShopPrices.getSkinCost("cyberpunk"), Translations.getLocalizedSkinTitle("cyberpunk", currentLang), Translations.getLocalizedSkinDesc("cyberpunk", currentLang)),
            SkinData("retro_amber", ShopPrices.getSkinCost("retro_amber"), Translations.getLocalizedSkinTitle("retro_amber", currentLang), Translations.getLocalizedSkinDesc("retro_amber", currentLang)),
            SkinData("emerald_matrix", ShopPrices.getSkinCost("emerald_matrix"), Translations.getLocalizedSkinTitle("emerald_matrix", currentLang), Translations.getLocalizedSkinDesc("emerald_matrix", currentLang)),
            SkinData("vaporwave_pink", ShopPrices.getSkinCost("vaporwave_pink"), Translations.getLocalizedSkinTitle("vaporwave_pink", currentLang), Translations.getLocalizedSkinDesc("vaporwave_pink", currentLang)),
            SkinData("midnight_gold", ShopPrices.getSkinCost("midnight_gold"), Translations.getLocalizedSkinTitle("midnight_gold", currentLang), Translations.getLocalizedSkinDesc("midnight_gold", currentLang)),
            SkinData("carbon_neutral", ShopPrices.getSkinCost("carbon_neutral"), Translations.getLocalizedSkinTitle("carbon_neutral", currentLang), Translations.getLocalizedSkinDesc("carbon_neutral", currentLang)),
            SkinData("plasma_storm", ShopPrices.getSkinCost("plasma_storm"), Translations.getLocalizedSkinTitle("plasma_storm", currentLang), Translations.getLocalizedSkinDesc("plasma_storm", currentLang)),
            SkinData("glacial_frost", ShopPrices.getSkinCost("glacial_frost"), Translations.getLocalizedSkinTitle("glacial_frost", currentLang), Translations.getLocalizedSkinDesc("glacial_frost", currentLang))
        )
    }

    val cubeSkinsList = remember(currentLang) {
        listOf(
            CubeSkinStoreData("neon", ShopPrices.getCubeSkinCost("neon"), Translations.getLocalizedCubeSkinTitle("neon", currentLang), Translations.getLocalizedCubeSkinDesc("neon", currentLang), ""),
            CubeSkinStoreData("glass", ShopPrices.getCubeSkinCost("glass"), Translations.getLocalizedCubeSkinTitle("glass", currentLang), Translations.getLocalizedCubeSkinDesc("glass", currentLang), ""),
            CubeSkinStoreData("retro", ShopPrices.getCubeSkinCost("retro"), Translations.getLocalizedCubeSkinTitle("retro", currentLang), Translations.getLocalizedCubeSkinDesc("retro", currentLang), ""),
            CubeSkinStoreData("flat", ShopPrices.getCubeSkinCost("flat"), Translations.getLocalizedCubeSkinTitle("flat", currentLang), Translations.getLocalizedCubeSkinDesc("flat", currentLang), ""),
            CubeSkinStoreData("material", ShopPrices.getCubeSkinCost("material"), Translations.getLocalizedCubeSkinTitle("material", currentLang), Translations.getLocalizedCubeSkinDesc("material", currentLang), ""),
            CubeSkinStoreData("glowing_jewel", ShopPrices.getCubeSkinCost("glowing_jewel"), Translations.getLocalizedCubeSkinTitle("glowing_jewel", currentLang), Translations.getLocalizedCubeSkinDesc("glowing_jewel", currentLang), ""),
            CubeSkinStoreData("steampunk", ShopPrices.getCubeSkinCost("steampunk"), Translations.getLocalizedCubeSkinTitle("steampunk", currentLang), Translations.getLocalizedCubeSkinDesc("steampunk", currentLang), "")
        )
    }

    val controlButtonStylesList = remember(currentLang) {
        listOf(
            ControlButtonStyleStoreData("classic", ShopPrices.getButtonCost("classic"), Translations.getLocalizedButtonTitle("classic", currentLang), Translations.getLocalizedButtonDesc("classic", currentLang)),
            ControlButtonStyleStoreData("neon", ShopPrices.getButtonCost("neon"), Translations.getLocalizedButtonTitle("neon", currentLang), Translations.getLocalizedButtonDesc("neon", currentLang)),
            ControlButtonStyleStoreData("glass", ShopPrices.getButtonCost("glass"), Translations.getLocalizedButtonTitle("glass", currentLang), Translations.getLocalizedButtonDesc("glass", currentLang))
        )
    }

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

    val purchasedRanks by viewModel.purchasedRanks.collectAsStateWithLifecycle()

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
            viewModel.saveCurrentProfileToDb()
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
        val isMonochrome = (themeColor.red < 0.22f && themeColor.green < 0.22f && themeColor.blue < 0.22f) ||
                (themeColor.red > 0.80f && themeColor.green > 0.80f && themeColor.blue > 0.80f)

        if (equippedAvatarFrame != "standard") {
            if (isMonochrome) {
                Brush.sweepGradient(listOf(Color(0xFFFFFFFF), Color(0xFFA0A5B5), Color(0xFFE8EDF8), Color(0xFF656A7A), Color(0xFFFFFFFF)))
            } else {
                val hsv = FloatArray(3)
                android.graphics.Color.colorToHSV(
                    android.graphics.Color.argb(
                        (themeColor.alpha * 255).toInt(),
                        (themeColor.red * 255).toInt(),
                        (themeColor.green * 255).toInt(),
                        (themeColor.blue * 255).toInt()
                    ),
                    hsv
                )
                val baseHue = hsv[0]
                val sat = hsv[1].coerceIn(0.70f, 0.98f)
                val value = hsv[2].coerceIn(0.85f, 1f)

                val c1 = Color(android.graphics.Color.HSVToColor(floatArrayOf(baseHue, sat, value)))
                val c2 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 35f) % 360f, sat, value)))
                val c3 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 70f) % 360f, (sat * 0.85f).coerceIn(0.55f, 1f), value)))
                val c4 = Color(android.graphics.Color.HSVToColor(floatArrayOf((baseHue + 35f) % 360f, sat, value)))
                val c5 = c1

                Brush.sweepGradient(listOf(c1, c2, c3, c4, c5))
            }
        } else {
            Brush.sweepGradient(listOf(themeColor, secondaryColor, themeColor))
        }
    }
    val avatarFrameThickness = if (equippedAvatarFrame != "standard") 3.5.dp else 2.dp


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
            val msg = when (currentLang) {
                Language.RU -> "Оформление блоков успешно применено"
                Language.UA -> "Оформлення блоків успішно застосовано"
                Language.KK -> "Блоктер дизайны сәтті қолданылды"
                Language.DE -> "Block-Stil erfolgreich ausgerüstet."
                Language.ZH -> "方块样式已成功应用"
                else -> "Cube style equipped."
            }
            triggerMessage(msg)
            return
        }
        if (credits >= cost) {
            val updated = purchasedCubeSkinsSet.toMutableSet().apply { add(styleId) }
            sharedPrefs.edit().putStringSet("purchased_cube_skins", updated).apply()
            viewModel.spendCredits(cost)
            viewModel.setBlockStyle(styleId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            val msg = when (currentLang) {
                Language.RU -> "Стиль блоков успешно приобретен и применен"
                Language.UA -> "Стиль блоків успішно придбано та застосовано"
                Language.KK -> "Блок стилі сәтті сатып алынды және қолданылды"
                Language.DE -> "Block-Stil gekauft und ausgerüstet."
                Language.ZH -> "方块样式已购买并装备"
                else -> "Cube style purchased and equipped."
            }
            triggerMessage(msg)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            val msg = when (currentLang) {
                Language.RU -> "Недостаточно средств"
                Language.UA -> "Недостатньо коштів"
                Language.KK -> "Қаражат жеткіліксіз"
                Language.DE -> "Nicht genügend Guthaben"
                Language.ZH -> "余额不足"
                else -> "Insufficient funds"
            }
            triggerMessage(msg, isError = true)
        }
    }

    val purchasedSkinsSet = remember(boardSkin, credits) {
        sharedPrefs.getStringSet("purchased_skins", setOf("cyberpunk")) ?: setOf("cyberpunk")
    }

    val premiumModesList = remember(currentLang) {
        listOf(
            GameModeStoreData("zen", ShopPrices.getModeCost("zen"), Translations.getLobbyModeTitle("zen", currentLang), Translations.getLobbyModeDesc("zen", currentLang), ""),
            GameModeStoreData("pulse_extreme", ShopPrices.getModeCost("pulse_extreme"), Translations.getLobbyModeTitle("pulse_extreme", currentLang), Translations.getLobbyModeDesc("pulse_extreme", currentLang), ""),
            GameModeStoreData("mirror", ShopPrices.getModeCost("mirror"), Translations.getLobbyModeTitle("mirror", currentLang), Translations.getLobbyModeDesc("mirror", currentLang), ""),
            GameModeStoreData("penta", ShopPrices.getModeCost("penta"), Translations.getLobbyModeTitle("penta", currentLang), Translations.getLobbyModeDesc("penta", currentLang), "")
        )
    }

    val purchasedModesSet = remember(credits) {
        sharedPrefs.getStringSet("purchased_modes", setOf("classic", "extended", "fast_run", "reverse", "block_blast")) 
            ?: setOf("classic", "extended", "fast_run", "reverse", "block_blast")
    }

    fun purchaseMode(modeId: String, cost: Int) {
        if (purchasedModesSet.contains(modeId)) {
            val msg = when (currentLang) {
                Language.RU -> "Режим уже разблокирован"
                Language.UA -> "Режим вже розблоковано"
                Language.KK -> "Режим әлдеқашан ашылған"
                Language.DE -> "Modus bereits freigeschaltet"
                Language.ZH -> "游戏模式已解锁"
                else -> "Game mode already unlocked"
            }
            triggerMessage(msg)
            return
        }
        if (credits >= cost) {
            val updated = purchasedModesSet.toMutableSet().apply { add(modeId) }
            sharedPrefs.edit().putStringSet("purchased_modes", updated).apply()
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            val msg = when (currentLang) {
                Language.RU -> "Премиум-режим разблокирован"
                Language.UA -> "Преміум-режим розблоковано"
                Language.KK -> "Премиум режим ашылды"
                Language.DE -> "Premium-Modus erfolgreich freigeschaltet."
                Language.ZH -> "高级模式解锁成功"
                else -> "Premium game mode unlocked successfully."
            }
            triggerMessage(msg)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            val msg = when (currentLang) {
                Language.RU -> "Недостаточно средств"
                Language.UA -> "Недостатньо коштів"
                Language.KK -> "Қаражат жеткіліксіз"
                Language.DE -> "Nicht genügend Guthaben"
                Language.ZH -> "余额不足"
                else -> "Insufficient funds"
            }
            triggerMessage(msg, isError = true)
        }
    }

    fun purchaseSkin(skinId: String, cost: Int) {
        if (purchasedSkinsSet.contains(skinId)) {
            viewModel.setBoardColorSkin(skinId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            val msg = when (currentLang) {
                Language.RU -> "Оформление успешно применилось"
                Language.UA -> "Оформлення успішно застосовано"
                Language.KK -> "Дизайн сәтті қолданылды"
                Language.DE -> "Design erfolgreich ausgerüstet."
                Language.ZH -> "网格主题已应用"
                else -> "Grid scheme equipped."
            }
            triggerMessage(msg)
            return
        }
        if (credits >= cost) {
            val updated = purchasedSkinsSet.toMutableSet().apply { add(skinId) }
            sharedPrefs.edit().putStringSet("purchased_skins", updated).apply()
            viewModel.spendCredits(cost)
            viewModel.setBoardColorSkin(skinId)
            sharedPrefs.edit().putBoolean("ach_color_skin_collector_unlocked", true).apply()
            viewModel.evaluateAchievements()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            val msg = when (currentLang) {
                Language.RU -> "Оформление успешно приобретено и применилось"
                Language.UA -> "Оформлення успішно придбано та застосовано"
                Language.KK -> "Дизайн сәтті сатып алынды және қолданылды"
                Language.DE -> "Design gekauft und ausgerüstet."
                Language.ZH -> "主题已购买并装备"
                else -> "Skin purchased and equipped."
            }
            triggerMessage(msg)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            val msg = when (currentLang) {
                Language.RU -> "Недостаточно средств"
                Language.UA -> "Недостатньо коштів"
                Language.KK -> "Қаражат жеткіліксіз"
                Language.DE -> "Nicht genügend Guthaben"
                Language.ZH -> "余额不足"
                else -> "Insufficient funds"
            }
            triggerMessage(msg, isError = true)
        }
    }

    fun purchaseRank(rankId: String, cost: Int) {
        if (purchasedRanks.contains(rankId)) {
            val msg = when (currentLang) {
                Language.RU -> "Этот ранг уже разблокирован!"
                Language.UA -> "Цей ранг вже розблоковано!"
                Language.KK -> "Бұл дәреже әлдеқашан ашылған!"
                Language.DE -> "Dieser Rang ist bereits freigeschaltet!"
                Language.ZH -> "该段位已解锁！"
                else -> "This rank is already unlocked."
            }
            triggerMessage(msg)
            return
        }
        val rankIndex = ranksList.indexOfFirst { it.id == rankId }
        val prevRankId = if (rankIndex > 0) ranksList[rankIndex - 1].id else null
        if (prevRankId != null && !purchasedRanks.contains(prevRankId)) {
            val msg = when (currentLang) {
                Language.RU -> "Нужно купить предыдущий ранг!"
                Language.UA -> "Потрібно купити попередній ранг!"
                Language.KK -> "Алдымен алдыңғы дәрежені сатып алу керек!"
                Language.DE -> "Schalte zuerst den vorherigen Rang frei!"
                Language.ZH -> "请先解锁前置段位！"
                else -> "Unlock previous rank first."
            }
            triggerMessage(msg, isError = true)
            return
        }
        if (credits >= cost) {
            viewModel.spendCredits(cost)
            viewModel.addPurchasedRank(rankId)
            viewModel.setOnlineTier(rankId)
            sharedPrefs.edit().putBoolean("ach_rank_conqueror_unlocked", true).apply()
            viewModel.evaluateAchievements()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            val msg = when (currentLang) {
                Language.RU -> "Ранг успешно повышен"
                Language.UA -> "Ранг успішно підвищено"
                Language.KK -> "Дәреже сәтті көтерілді"
                Language.DE -> "Rang erfolgreich aufgewertet."
                Language.ZH -> "段位提升成功"
                else -> "Rank updated successfully."
            }
            triggerMessage(msg)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            val msg = when (currentLang) {
                Language.RU -> "Недостаточно средств для повышения"
                Language.UA -> "Недостатньо коштів для підвищення"
                Language.KK -> "Дәрежені көтеруге қаражат жеткіліксіз"
                Language.DE -> "Nicht genügend Guthaben für Rang-Upgrade."
                Language.ZH -> "晋升所需余额不足"
                else -> "Insufficient funds for rank raise."
            }
            triggerMessage(msg, isError = true)
        }
    }

    fun selectAvatarFrame(frameId: String, cost: Int) {
        if (purchasedAvatarFrames.contains(frameId)) {
            viewModel.setEquippedAvatarFrame(frameId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            val msg = when (currentLang) {
                Language.RU -> "Рамка успешно выбрана"
                Language.UA -> "Рамку успішно обрано"
                Language.KK -> "Жақтау сәтті таңдалды"
                Language.DE -> "Avatar-Rahmen ausgerüstet."
                Language.ZH -> "头像框已装备"
                else -> "Avatar frame equipped."
            }
            triggerMessage(msg)
            return
        }
        if (credits >= cost) {
            val updated = purchasedAvatarFrames.toMutableSet().apply { add(frameId) }
            viewModel.setPurchasedAvatarFrames(updated)
            viewModel.setEquippedAvatarFrame(frameId)
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            val msg = when (currentLang) {
                Language.RU -> "Рамка куплена и надета"
                Language.UA -> "Рамку куплено та вдягнено"
                Language.KK -> "Жақтау сатып алынды және тағылды"
                Language.DE -> "Avatar-Rahmen gekauft und ausgerüstet."
                Language.ZH -> "头像框已购买并装备"
                else -> "Avatar frame purchased and equipped."
            }
            triggerMessage(msg)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            val msg = when (currentLang) {
                Language.RU -> "Недостаточно средств"
                Language.UA -> "Недостатньо коштів"
                Language.KK -> "Қаражат жеткіліксіз"
                Language.DE -> "Nicht genügend Guthaben"
                Language.ZH -> "余额不足"
                else -> "Insufficient funds"
            }
            triggerMessage(msg, isError = true)
        }
    }

    fun selectTitle(titleId: String, cost: Int) {
        if (purchasedTitles.contains(titleId)) {
            viewModel.setEquippedTitle(titleId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            val msg = when (currentLang) {
                Language.RU -> "Титул успешно выбран"
                Language.UA -> "Титул успішно обрано"
                Language.KK -> "Атақ сәтті таңдалды"
                Language.DE -> "Titel ausgerüstet."
                Language.ZH -> "称号已佩戴"
                else -> "Title equipped."
            }
            triggerMessage(msg)
            return
        }
        if (credits >= cost) {
            val updated = purchasedTitles.toMutableSet().apply { add(titleId) }
            viewModel.setPurchasedTitles(updated)
            viewModel.setEquippedTitle(titleId)
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            val msg = when (currentLang) {
                Language.RU -> "Титул куплен и активирован"
                Language.UA -> "Титул куплено та активовано"
                Language.KK -> "Атақ сатып алынды және белсендірілді"
                Language.DE -> "Titel gekauft und aktiviert."
                Language.ZH -> "称号已购买并佩戴"
                else -> "Title purchased and equipped."
            }
            triggerMessage(msg)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            val msg = when (currentLang) {
                Language.RU -> "Недостаточно средств"
                Language.UA -> "Недостатньо коштів"
                Language.KK -> "Қаражат жеткіліксіз"
                Language.DE -> "Nicht genügend Guthaben"
                Language.ZH -> "余额不足"
                else -> "Insufficient funds"
            }
            triggerMessage(msg, isError = true)
        }
    }

    fun selectFont(fontId: String, cost: Int) {
        if (purchasedFonts.contains(fontId)) {
            viewModel.setCustomFontKey(fontId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            val msg = when (currentLang) {
                Language.RU -> "Шрифт успешно выбран"
                Language.UA -> "Шрифт успішно обрано"
                Language.KK -> "Қаріп сәтті таңдалды"
                Language.DE -> "Schriftart ausgerüstet."
                Language.ZH -> "界面字体已应用"
                else -> "Interface font equipped."
            }
            triggerMessage(msg)
            return
        }
        if (credits >= cost) {
            val updated = purchasedFonts.toMutableSet().apply { add(fontId) }
            viewModel.setPurchasedFonts(updated)
            viewModel.setCustomFontKey(fontId)
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            val msg = when (currentLang) {
                Language.RU -> "Шрифт куплен и активирован"
                Language.UA -> "Шрифт куплено та активовано"
                Language.KK -> "Қаріп сатып алынды және белсендірілді"
                Language.DE -> "Schriftart gekauft und aktiviert."
                Language.ZH -> "界面字体已购买并应用"
                else -> "Interface font purchased and equipped."
            }
            triggerMessage(msg)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            val msg = when (currentLang) {
                Language.RU -> "Недостаточно средств"
                Language.UA -> "Недостатньо коштів"
                Language.KK -> "Қаражат жеткіліксіз"
                Language.DE -> "Nicht genügend Guthaben"
                Language.ZH -> "余额不足"
                else -> "Insufficient funds"
            }
            triggerMessage(msg, isError = true)
        }
    }

    fun selectControlButtonStyle(styleId: String, cost: Int) {
        if (purchasedControlButtonStyles.contains(styleId)) {
            viewModel.setControlButtonStyle(styleId)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("equip")
            val msg = when (currentLang) {
                Language.RU -> "Стиль кнопок успешно выбран"
                Language.UA -> "Стиль кнопок успішно обрано"
                Language.KK -> "Батырмалар стилі сәтті таңдалды"
                Language.DE -> "Button-Stil ausgerüstet."
                Language.ZH -> "按键样式已应用"
                else -> "Button style equipped."
            }
            triggerMessage(msg)
            return
        }
        if (credits >= cost) {
            val updated = purchasedControlButtonStyles.toMutableSet().apply { add(styleId) }
            viewModel.setPurchasedControlButtonStyles(updated)
            viewModel.setControlButtonStyle(styleId)
            viewModel.spendCredits(cost)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("buy")
            val msg = when (currentLang) {
                Language.RU -> "Стиль кнопок куплен и активирован"
                Language.UA -> "Стиль кнопок куплено та активовано"
                Language.KK -> "Батырмалар стилі сатып алынды және белсендірілді"
                Language.DE -> "Button-Stil gekauft und aktiviert."
                Language.ZH -> "按键样式已购买并应用"
                else -> "Button style purchased and equipped."
            }
            triggerMessage(msg)
        } else {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.triggerAudioFeedback("error")
            val msg = when (currentLang) {
                Language.RU -> "Недостаточно средств"
                Language.UA -> "Недостатньо коштів"
                Language.KK -> "Қаражат жеткіліксіз"
                Language.DE -> "Nicht genügend Guthaben"
                Language.ZH -> "余额不足"
                else -> "Insufficient funds"
            }
            triggerMessage(msg, isError = true)
        }
    }


    val isLoggedIn = playerName != "Player 1"
    val pageCount = 3
    val pagerState = rememberPagerState(initialPage = if (initialTab == 99) 0 else initialTab.coerceAtMost(pageCount - 1)) { pageCount }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)),
                        tonalElevation = 3.dp,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Primary Currency (Credits / Coins)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Credits",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(18.dp)
                                )
                                AdaptiveText(
                                    text = "$credits",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }

                            // Elegant Gradient Vertical Divider
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 10.dp)
                                    .height(18.dp)
                                    .width(1.5.dp)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color.Transparent,
                                                MaterialTheme.colorScheme.outlineVariant,
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            // Secondary Visual Cosmetic Currency (Gems / Crystals)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Diamond,
                                    contentDescription = "Gems",
                                    tint = Color(0xFF00E5FF),
                                    modifier = Modifier.size(17.dp)
                                )
                                AdaptiveText(
                                    text = "0",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        letterSpacing = 0.5.sp
                                    )
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.size(48.dp))
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
                val profileTabTitle = when (currentLang) {
                    Language.RU -> "Профиль"
                    Language.UA -> "Профіль"
                    Language.KK -> "Профиль"
                    Language.DE -> "Profil"
                    Language.ZH -> "个人资料"
                    else -> "Profile"
                }
                val storeTabTitle = when (currentLang) {
                    Language.RU -> "Магазин"
                    Language.UA -> "Магазин"
                    Language.KK -> "Дүкен"
                    Language.DE -> "Shop"
                    Language.ZH -> "商店"
                    else -> "Store"
                }
                val achievementsTabTitle = when (currentLang) {
                    Language.RU -> "Достижения"
                    Language.UA -> "Досягнення"
                    Language.KK -> "Жетістіктер"
                    Language.DE -> "Erfolge"
                    Language.ZH -> "成就"
                    else -> "Achievements"
                }
                val tabsList = listOf(
                    Triple(0, profileTabTitle, Icons.Default.Person),
                    Triple(1, storeTabTitle, Icons.Default.ShoppingBag),
                    Triple(2, achievementsTabTitle, Icons.Default.EmojiEvents)
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
                                                val authTitle = if (isRegister) {
                                                    when (currentLang) {
                                                        Language.RU -> "РЕГИСТРАЦИЯ"
                                                        Language.UA -> "РЕЄСТРАЦІЯ"
                                                        Language.KK -> "ТІРКЕЛУ"
                                                        Language.DE -> "REGISTRIERUNG"
                                                        Language.ZH -> "注册账号"
                                                        else -> "REGISTRATION"
                                                    }
                                                } else {
                                                    when (currentLang) {
                                                        Language.RU -> "ВХОД В АККАУНТ"
                                                        Language.UA -> "ВХІД В АКАУНТ"
                                                        Language.KK -> "АККАУНТҚА КІРУ"
                                                        Language.DE -> "ANMELDUNG"
                                                        Language.ZH -> "登录账号"
                                                        else -> "ACCOUNT LOGIN"
                                                    }
                                                }
                                                val authSubtitle = if (isRegister) {
                                                    when (currentLang) {
                                                        Language.RU -> "Создайте профиль для сохранения статистики"
                                                        Language.UA -> "Створіть профіль для збереження статистики"
                                                        Language.KK -> "Статистиканы сақтау үшін профиль жасаңыз"
                                                        Language.DE -> "Erstelle ein Profil, um deinen Fortschritt zu speichern"
                                                        Language.ZH -> "创建账号以保存游戏进度与成就"
                                                        else -> "Create a profile to save your stats"
                                                    }
                                                } else {
                                                    when (currentLang) {
                                                        Language.RU -> "Войдите для синхронизации прогресса"
                                                        Language.UA -> "Увійдіть для синхронізації прогресу"
                                                        Language.KK -> "Прогресті синхрондау үшін кіріңіз"
                                                        Language.DE -> "Melde dich an, um deinen Fortschritt zu synchronisieren"
                                                        Language.ZH -> "登录以同步您的云端游戏数据"
                                                        else -> "Sign in to sync your progress"
                                                    }
                                                }
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = authTitle,
                                                        style = MaterialTheme.typography.titleLarge,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                        textAlign = TextAlign.Center
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = authSubtitle,
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
                                            val signInTabLabel = when (currentLang) {
                                                Language.RU -> "ВХОД"
                                                Language.UA -> "ВХІД"
                                                Language.KK -> "КІРУ"
                                                Language.DE -> "ANMELDEN"
                                                Language.ZH -> "登录"
                                                else -> "SIGN IN"
                                            }
                                            val signUpTabLabel = when (currentLang) {
                                                Language.RU -> "РЕГИСТРАЦИЯ"
                                                Language.UA -> "РЕЄСТРАЦІЯ"
                                                Language.KK -> "ТІРКЕЛУ"
                                                Language.DE -> "REGISTRIEREN"
                                                Language.ZH -> "注册"
                                                else -> "SIGN UP"
                                            }
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
                                                                text = signInTabLabel,
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
                                                                text = signUpTabLabel,
                                                                style = MaterialTheme.typography.labelLarge,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = textColor
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(22.dp))

                                            val nicknameLabel = when (currentLang) {
                                                Language.RU -> "Никнейм"
                                                Language.UA -> "Нікнейм"
                                                Language.KK -> "Лақап ат"
                                                Language.DE -> "Benutzername"
                                                Language.ZH -> "昵称"
                                                else -> "Nickname"
                                            }
                                            val nicknameOrEmailLabel = when (currentLang) {
                                                Language.RU -> "Никнейм или Email"
                                                Language.UA -> "Нікнейм або Email"
                                                Language.KK -> "Лақап ат немесе Email"
                                                Language.DE -> "Benutzername oder E-Mail"
                                                Language.ZH -> "昵称或电子邮箱"
                                                else -> "Nickname or Email"
                                            }
                                            val emailLabel = when (currentLang) {
                                                Language.RU -> "Электронная почта"
                                                Language.UA -> "Електронна пошта"
                                                Language.KK -> "Электрондық пошта"
                                                Language.DE -> "E-Mail-Adresse"
                                                Language.ZH -> "电子邮箱"
                                                else -> "Email Address"
                                            }
                                            val passwordLabel = when (currentLang) {
                                                Language.RU -> "Пароль"
                                                Language.UA -> "Пароль"
                                                Language.KK -> "Құпиясөз"
                                                Language.DE -> "Passwort"
                                                Language.ZH -> "密码"
                                                else -> "Password"
                                            }

                                            // Input fields
                                            OutlinedTextField(
                                                value = inputUsername,
                                                onValueChange = { inputUsername = it },
                                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                                label = { Text(if (authModeIsRegister) nicknameLabel else nicknameOrEmailLabel) },
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
                                                        label = { Text(emailLabel) },
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
                                                label = { Text(passwordLabel) },
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
                                                val localizedErr = Translations.getAuthError(err, currentLang)

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
                                                val localizedSucc = Translations.getAuthSuccess(success, currentLang)

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
                                                val verifyAccountTitle = when (currentLang) {
                                                    Language.RU -> "Проверьте почту и подтвердите аккаунт!"
                                                    Language.UA -> "Перевірте пошту та підтвердіть акаунт!"
                                                    Language.KK -> "Поштаңызды тексеріп, аккаунтты растаңыз!"
                                                    Language.DE -> "Prüfe deine E-Mails und bestätige dein Konto!"
                                                    Language.ZH -> "请查看您的邮箱并完成账号验证！"
                                                    else -> "Check your email and verify your account!"
                                                }
                                                val resendBtnText = when (currentLang) {
                                                    Language.RU -> "Отправить"
                                                    Language.UA -> "Надіслати"
                                                    Language.KK -> "Жіберу"
                                                    Language.DE -> "Erneut senden"
                                                    Language.ZH -> "重新发送"
                                                    else -> "Resend"
                                                }
                                                val verifiedBtnText = when (currentLang) {
                                                    Language.RU -> "Я подтвердил"
                                                    Language.UA -> "Я підтвердив"
                                                    Language.KK -> "Мен растадым"
                                                    Language.DE -> "Ich habe bestätigt"
                                                    Language.ZH -> "我已完成验证"
                                                    else -> "I verified"
                                                }
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
                                                            text = verifyAccountTitle,
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
                                                                text = resendBtnText,
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
                                                                text = verifiedBtnText,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(14.dp))
                                            }

                                            // Submit Button
                                            val submitBtnText = if (authModeIsRegister) {
                                                when (currentLang) {
                                                    Language.RU -> "СОЗДАТЬ АККАУНТ"
                                                    Language.UA -> "СТВОРИТИ АКАУНТ"
                                                    Language.KK -> "АККАУНТ ЖАСАУ"
                                                    Language.DE -> "KONTO ERSTELLEN"
                                                    Language.ZH -> "创建新账号"
                                                    else -> "CREATE ACCOUNT"
                                                }
                                            } else {
                                                when (currentLang) {
                                                    Language.RU -> "ВОЙТИ В СИСТЕМУ"
                                                    Language.UA -> "УВІЙТИ В СИСТЕМУ"
                                                    Language.KK -> "ЖҮЙЕГЕ КІРУ"
                                                    Language.DE -> "ANMELDEN"
                                                    Language.ZH -> "立即登录"
                                                    else -> "SIGN IN"
                                                }
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
                                                    .height(52.dp),
                                                shape = RoundedCornerShape(16.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            ) {
                                                Text(
                                                    text = submitBtnText,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    letterSpacing = 0.5.sp,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    maxLines = 1
                                                )
                                            }

                                            Spacer(modifier = Modifier.height(20.dp))

                                            // Divider
                                            val orDividerText = when (currentLang) {
                                                Language.RU -> "ИЛИ"
                                                Language.UA -> "АБО"
                                                Language.KK -> "НЕМЕСЕ"
                                                Language.DE -> "ODER"
                                                Language.ZH -> "或"
                                                else -> "OR"
                                            }
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                                Text(
                                                    text = orDividerText,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                                    modifier = Modifier.padding(horizontal = 14.dp)
                                                )
                                                HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                            }

                                            Spacer(modifier = Modifier.height(20.dp))

                                            // Google Sign-In Button
                                            val googleBtnText = when (currentLang) {
                                                Language.RU -> "Войти через Google"
                                                Language.UA -> "Увійти через Google"
                                                Language.KK -> "Google арқылы кіру"
                                                Language.DE -> "Mit Google anmelden"
                                                Language.ZH -> "使用 Google 账号登录"
                                                else -> "Sign in with Google"
                                            }
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
                                                GoogleLogoIcon(
                                                    modifier = Modifier.size(22.dp)
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Text(
                                                    text = googleBtnText,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = Color(0xFF1F1F1F),
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        // Loading overlay
                                        if (isAuthLoading) {
                                            val waitText = when (currentLang) {
                                                Language.RU -> "Пожалуйста, подождите..."
                                                Language.UA -> "Будь ласка, зачекайте..."
                                                Language.KK -> "Күте тұрыңыз..."
                                                Language.DE -> "Bitte warten..."
                                                Language.ZH -> "请稍候..."
                                                else -> "Please wait..."
                                            }
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
                                                        text = waitText,
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
    
                                        val titleString = Translations.getLocalizedTitle(equippedTitle, currentLang)
    
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
                                            val nicknameBrush = rememberAnimatedNicknameBrush(baseColor = themeColor)
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
                                                        val copyMsg = when (currentLang) {
                                                            Language.RU -> "ID скопирован в буфер!"
                                                            Language.UA -> "ID скопійовано в буфер!"
                                                            Language.KK -> "ID алмасу буферіне көшірілді!"
                                                            Language.DE -> "ID in Zwischenablage kopiert!"
                                                            Language.ZH -> "ID 已复制到剪贴板！"
                                                            else -> "ID copied to clipboard!"
                                                        }
                                                        triggerMessage(copyMsg)
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
    
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
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

                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFFFFD700).copy(alpha = 0.18f)
                                            ) {
                                                Text(
                                                    text = "ELO $onlineRating",
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                                    color = Color(0xFFFFD700),
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    maxLines = 1
                                                )
                                            }

                                            if (winStreak > 0) {
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = Color(0xFFFF5722).copy(alpha = 0.18f)
                                                ) {
                                                    Text(
                                                        text = "x$winStreak",
                                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                                        color = Color(0xFFFF5722),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                        maxLines = 1
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        val editProfileBtnText = when (currentLang) {
                                            Language.RU -> "Настройки профиля"
                                            Language.UA -> "Налаштування профілю"
                                            Language.KK -> "Профиль баптаулары"
                                            Language.DE -> "Profil bearbeiten"
                                            Language.ZH -> "编辑资料"
                                            else -> "Edit Profile"
                                        }
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
                                                text = editProfileBtnText,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
    
                                // Mastery Progress & Leveling System
                                val bonusXpVal by viewModel.bonusXp.collectAsStateWithLifecycle()
                                val claimedLevelRewards by viewModel.claimedLevelRewards.collectAsStateWithLifecycle()
                                val totalLines = statsClearedLines
                                val highscoreVal = statsHighScore
                                val totalXp = (totalLines * 25) + (highscoreVal / 10) + bonusXpVal
                                
                                val (masteryLevel, currentLevelXp, xpPercentage) = remember(totalXp) {
                                    viewModel.getPlayerLevelProgress(totalXp)
                                }
                                val levelXpBound = remember(masteryLevel) { viewModel.getXpRequiredForLevel(masteryLevel) }
                                val xpNeeded = (levelXpBound - currentLevelXp).coerceAtLeast(0)
                                val masteryTitle = remember(masteryLevel, currentLang) { 
                                    MasterySystem.getMasteryTitle(masteryLevel, currentLang) 
                                }
                                val unclaimedRewards = remember(claimedLevelRewards, masteryLevel) { 
                                    viewModel.getUnclaimedLevelRewards() 
                                }

                                ElevatedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(22.dp),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(18.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        val levelSectionTitle = when (currentLang) {
                                            Language.RU -> "УРОВЕНЬ"
                                            Language.UA -> "РІВЕНЬ"
                                            Language.KK -> "ДЕҢГЕЙ"
                                            Language.DE -> "LEVEL"
                                            Language.ZH -> "等级"
                                            else -> "LEVEL"
                                        }

                                        // HEADER: Icon + Section Title + Title Badge + Level Badge
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
                                                    text = levelSectionTitle,
                                                    style = MaterialTheme.typography.titleSmall.copy(
                                                        fontWeight = FontWeight.ExtraBold,
                                                        letterSpacing = 0.5.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                // Mastery Title Badge
                                                Surface(
                                                    shape = RoundedCornerShape(10.dp),
                                                    color = Color(masteryTitle.color).copy(alpha = 0.15f),
                                                    border = BorderStroke(1.dp, Color(masteryTitle.color).copy(alpha = 0.5f))
                                                ) {
                                                    Text(
                                                        text = "${masteryTitle.iconEmoji} ${masteryTitle.title}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = Color(masteryTitle.color),
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }

                                                // Level Number Badge
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
                                        }

                                        // PROGRESS BAR & XP INFO
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
                                                val toNextLvlText = when (currentLang) {
                                                    Language.RU -> "До след. уровня: $xpNeeded XP"
                                                    Language.UA -> "До наст. рівня: $xpNeeded XP"
                                                    Language.KK -> "Келесі деңгейге дейін: $xpNeeded XP"
                                                    Language.DE -> "Bis zum nächsten Level: $xpNeeded XP"
                                                    Language.ZH -> "升级还需: $xpNeeded XP"
                                                    else -> "To next level: $xpNeeded XP"
                                                }
                                                Text(
                                                    text = toNextLvlText,
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

                                        // PASSIVE PERKS CHIPS
                                        val coinBonusPercent = masteryLevel / 2
                                        val xpBonusPercent = (masteryLevel / 5) * 2
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Surface(
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(text = "🪙", fontSize = 13.sp)
                                                    val coinBuffLabel = when (currentLang) {
                                                        Language.RU -> "Бонус монет: +$coinBonusPercent%"
                                                        Language.UA -> "Бонус монет: +$coinBonusPercent%"
                                                        Language.KK -> "Монета бонусы: +$coinBonusPercent%"
                                                        Language.DE -> "Münzbonus: +$coinBonusPercent%"
                                                        Language.ZH -> "金币加成: +$coinBonusPercent%"
                                                        else -> "Coin boost: +$coinBonusPercent%"
                                                    }
                                                    Text(
                                                        text = coinBuffLabel,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Surface(
                                                modifier = Modifier.weight(1f),
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(text = "⚡", fontSize = 13.sp)
                                                    val xpBuffLabel = when (currentLang) {
                                                        Language.RU -> "Бонус опыта: +$xpBonusPercent%"
                                                        Language.UA -> "Бонус досвіду: +$xpBonusPercent%"
                                                        Language.KK -> "Тәжірибе бонусы: +$xpBonusPercent%"
                                                        Language.DE -> "XP-Bonus: +$xpBonusPercent%"
                                                        Language.ZH -> "经验加成: +$xpBonusPercent%"
                                                        else -> "XP boost: +$xpBonusPercent%"
                                                    }
                                                    Text(
                                                        text = xpBuffLabel,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        // LEVEL UP REWARDS SECTION
                                        if (unclaimedRewards.isNotEmpty()) {
                                            val totalRewardCredits = unclaimedRewards.sumOf { it.credits }
                                            val keyRewardsCount = unclaimedRewards.filter { it.keyType != null }.sumOf { it.keyCount }
                                            val claimRewardTitle = when (currentLang) {
                                                Language.RU -> "ДОСТУПНЫ НАГРАДЫ (${unclaimedRewards.size})"
                                                Language.UA -> "ДОСТУПНІ НАГОРОДИ (${unclaimedRewards.size})"
                                                Language.KK -> "СЫЙЛЫҚТАР ҚОЛЖЕТІМДІ (${unclaimedRewards.size})"
                                                Language.DE -> "BELOHNUNGEN VERFÜGBAR (${unclaimedRewards.size})"
                                                Language.ZH -> "可领取等级奖励 (${unclaimedRewards.size})"
                                                else -> "REWARDS AVAILABLE (${unclaimedRewards.size})"
                                            }
                                            val claimBtnLabel = when (currentLang) {
                                                Language.RU -> "ЗАБРАТЬ (+$totalRewardCredits 🪙${if (keyRewardsCount > 0) " +$keyRewardsCount 🔑" else ""})"
                                                Language.UA -> "ЗАБРАТИ (+$totalRewardCredits 🪙${if (keyRewardsCount > 0) " +$keyRewardsCount 🔑" else ""})"
                                                Language.KK -> "АЛУ (+$totalRewardCredits 🪙${if (keyRewardsCount > 0) " +$keyRewardsCount 🔑" else ""})"
                                                Language.DE -> "EINSAMMELN (+$totalRewardCredits 🪙${if (keyRewardsCount > 0) " +$keyRewardsCount 🔑" else ""})"
                                                Language.ZH -> "立即领取 (+$totalRewardCredits 🪙${if (keyRewardsCount > 0) " +$keyRewardsCount 🔑" else ""})"
                                                else -> "CLAIM (+$totalRewardCredits 🪙${if (keyRewardsCount > 0) " +$keyRewardsCount 🔑" else ""})"
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.CardGiftcard,
                                                            contentDescription = null,
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text(
                                                            text = claimRewardTitle,
                                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                    Button(
                                                        onClick = {
                                                            viewModel.triggerAudioFeedback("success")
                                                            viewModel.claimAllLevelRewards()
                                                        },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = MaterialTheme.colorScheme.primary
                                                        )
                                                    ) {
                                                        Text(
                                                            text = claimBtnLabel,
                                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                                        )
                                                    }
                                                }
                                            }
                                        } else {
                                            // NEXT MILESTONE PREVIEW
                                            val nextMilestoneLvl = ((masteryLevel / 5) + 1) * 5
                                            val nextReward = viewModel.getLevelReward(nextMilestoneLvl)
                                            val nextMilestoneDesc = when (currentLang) {
                                                Language.RU -> "Следующая веха: LVL $nextMilestoneLvl (+${nextReward.credits} 🪙${if (nextReward.keyType != null) " + ${nextReward.keyCount} 🔑" else ""})"
                                                Language.UA -> "Наступна віха: LVL $nextMilestoneLvl (+${nextReward.credits} 🪙${if (nextReward.keyType != null) " + ${nextReward.keyCount} 🔑" else ""})"
                                                Language.KK -> "Келесі кезең: LVL $nextMilestoneLvl (+${nextReward.credits} 🪙${if (nextReward.keyType != null) " + ${nextReward.keyCount} 🔑" else ""})"
                                                Language.DE -> "Nächster Meilenstein: LVL $nextMilestoneLvl (+${nextReward.credits} 🪙${if (nextReward.keyType != null) " + ${nextReward.keyCount} 🔑" else ""})"
                                                Language.ZH -> "下一里程碑: LVL $nextMilestoneLvl (+${nextReward.credits} 🪙${if (nextReward.keyType != null) " + ${nextReward.keyCount} 🔑" else ""})"
                                                else -> "Next milestone: LVL $nextMilestoneLvl (+${nextReward.credits} 🪙${if (nextReward.keyType != null) " + ${nextReward.keyCount} 🔑" else ""})"
                                            }
                                            Text(
                                                text = nextMilestoneDesc,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
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
                                        val statsTitle = when (currentLang) {
                                            Language.RU -> "СТАТИСТИКА"
                                            Language.UA -> "СТАТИСТИКА"
                                            Language.KK -> "СТАТИСТИКА"
                                            Language.DE -> "STATISTIKEN"
                                            Language.ZH -> "数据统计"
                                            else -> "STATISTICS"
                                        }
                                        val resetBtnText = when (currentLang) {
                                            Language.RU -> "Сброс"
                                            Language.UA -> "Скидання"
                                            Language.KK -> "Қайтару"
                                            Language.DE -> "Reset"
                                            Language.ZH -> "重置"
                                            else -> "Reset"
                                        }
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
                                                    text = statsTitle,
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
                                                    val resetMsg = when (currentLang) {
                                                        Language.RU -> "Статистика сброшена."
                                                        Language.UA -> "Статистику скинуто."
                                                        Language.KK -> "Статистика нөлденді."
                                                        Language.DE -> "Statistiken zurückgesetzt."
                                                        Language.ZH -> "数据已重置。"
                                                        else -> "Stats reset."
                                                    }
                                                    triggerMessage(resetMsg)
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
                                                    text = resetBtnText,
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            val highScoreLabel = when (currentLang) {
                                                Language.RU -> "РЕКОРД ОЧКОВ"
                                                Language.UA -> "РЕКОРД ОЧОК"
                                                Language.KK -> "РЕКОРД ҰПАЙ"
                                                Language.DE -> "REKORD"
                                                Language.ZH -> "最高得分"
                                                else -> "HIGH SCORE"
                                            }
                                            val linesClearedLabel = when (currentLang) {
                                                Language.RU -> "ЛИНИЙ ОЧИЩЕНО"
                                                Language.UA -> "ЛІНІЙ ОЧИЩЕНО"
                                                Language.KK -> "ЖОЙЫЛҒАН СЫЗЫҚТАР"
                                                Language.DE -> "REIHEN GELÖSCHT"
                                                Language.ZH -> "消除行数"
                                                else -> "LINES CLEARED"
                                            }
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
                                                        text = highScoreLabel,
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
                                                        text = linesClearedLabel,
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
                                            val emailStatusTitle = if (isEmailVerified) {
                                                when (currentLang) {
                                                    Language.RU -> "ПОЧТА ПОДТВЕРЖДЕНА"
                                                    Language.UA -> "ПОШТА ПІДТВЕРДЖЕНА"
                                                    Language.KK -> "ПОШТА РАСТАЛДЫ"
                                                    Language.DE -> "E-MAIL BESTÄTIGT"
                                                    Language.ZH -> "邮箱已验证"
                                                    else -> "EMAIL VERIFIED"
                                                }
                                            } else {
                                                when (currentLang) {
                                                    Language.RU -> "ПОЧТА НЕ ПОДТВЕРЖДЕНА"
                                                    Language.UA -> "ПОШТА НЕ ПІДТВЕРДЖЕНА"
                                                    Language.KK -> "ПОШТА РАСТАЛМАДЫ"
                                                    Language.DE -> "E-MAIL NICHT BESTÄTIGT"
                                                    Language.ZH -> "邮箱未验证"
                                                    else -> "EMAIL NOT VERIFIED"
                                                }
                                            }
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
                                                        text = emailStatusTitle,
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
                                                val unverifiedDesc = when (currentLang) {
                                                    Language.RU -> "Для безопасности аккаунта и восстановления доступа подтвердите адрес электронной почты."
                                                    Language.UA -> "Для безпеки акаунта та відновлення доступу підтвердіть адресу електронної пошти."
                                                    Language.KK -> "Аккаунт қауіпсіздігі және қолжетімділікті қалпына келтіру үшін электрондық поштаны растаңыз."
                                                    Language.DE -> "Bitte bestätige deine E-Mail-Adresse, um dein Konto zu sichern und Wiederherstellung zu aktivieren."
                                                    Language.ZH -> "为了账号安全及密码找回，请尽快完成邮箱验证。"
                                                    else -> "Please verify your email address to secure your account and enable account recovery."
                                                }
                                                val sendEmailBtnText = when (currentLang) {
                                                    Language.RU -> "Отправить письмо"
                                                    Language.UA -> "Надіслати лист"
                                                    Language.KK -> "Хат жіберу"
                                                    Language.DE -> "E-Mail senden"
                                                    Language.ZH -> "发送验证邮件"
                                                    else -> "Send Email"
                                                }
                                                val checkStatusBtnText = when (currentLang) {
                                                    Language.RU -> "Проверить"
                                                    Language.UA -> "Перевірити"
                                                    Language.KK -> "Тексеру"
                                                    Language.DE -> "Status prüfen"
                                                    Language.ZH -> "检查状态"
                                                    else -> "Check Status"
                                                }
                                                Text(
                                                    text = unverifiedDesc,
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
                                                            text = sendEmailBtnText,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                                        )
                                                    }

                                                    Button(
                                                        onClick = {
                                                            viewModel.checkEmailVerificationStatus { verified ->
                                                                if (verified) {
                                                                    val verifiedSuccessMsg = when (currentLang) {
                                                                        Language.RU -> "Почта успешно подтверждена!"
                                                                        Language.UA -> "Пошту успішно підтверджено!"
                                                                        Language.KK -> "Пошта сәтті расталды!"
                                                                        Language.DE -> "E-Mail erfolgreich bestätigt!"
                                                                        Language.ZH -> "邮箱验证成功！"
                                                                        else -> "Email verified successfully!"
                                                                    }
                                                                    triggerMessage(verifiedSuccessMsg)
                                                                } else {
                                                                    val notVerifiedMsg = when (currentLang) {
                                                                        Language.RU -> "Почта ещё не подтверждена. Проверьте входящие!"
                                                                        Language.UA -> "Пошту ще не підтверджено. Перевірте вхідні!"
                                                                        Language.KK -> "Пошта әлі расталмады. Кіріс хаттарды тексеріңіз!"
                                                                        Language.DE -> "E-Mail noch nicht bestätigt. Prüfe deinen Posteingang!"
                                                                        Language.ZH -> "邮箱尚未验证，请检查您的收件箱！"
                                                                        else -> "Email not verified yet. Check your inbox!"
                                                                    }
                                                                    triggerMessage(notVerifiedMsg, isError = true)
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(12.dp)
                                                    ) {
                                                        Text(
                                                            text = checkStatusBtnText,
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
                                    val signOutTitle = when (currentLang) {
                                        Language.RU -> "Выход из аккаунта"
                                        Language.UA -> "Вихід з акаунта"
                                        Language.KK -> "Аккаунттан шығу"
                                        Language.DE -> "Vom Konto abmelden"
                                        Language.ZH -> "退出登录"
                                        else -> "Log Out Confirmation"
                                    }
                                    val signOutDesc = when (currentLang) {
                                        Language.RU -> "Вы уверены, что хотите выйти из аккаунта? Все синхронизированные данные сохранены в облаке."
                                        Language.UA -> "Ви впевнені, що хочете вийти з акаунта? Усі синхронізовані дані збережено в хмарі."
                                        Language.KK -> "Аккаунттан шыққыңыз келетініне сенімдісіз бе? Барлық синхрондалған деректер бұлтта сақталған."
                                        Language.DE -> "Möchtest du dich wirklich abmelden? Alle synchronisierten Daten sind sicher in der Cloud gespeichert."
                                        Language.ZH -> "确定要退出当前账号吗？所有已同步的进度都已安全保存在云端。"
                                        else -> "Are you sure you want to log out? All synced data is safely stored in the cloud."
                                    }
                                    val signOutConfirmBtn = when (currentLang) {
                                        Language.RU -> "ВЫЙТИ"
                                        Language.UA -> "ВИЙТИ"
                                        Language.KK -> "ШЫҒУ"
                                        Language.DE -> "ABMELDEN"
                                        Language.ZH -> "退出"
                                        else -> "LOG OUT"
                                    }
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
                                                text = signOutTitle,
                                                fontWeight = FontWeight.Bold,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        },
                                        text = {
                                            Text(
                                                text = signOutDesc,
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
                                                Text(signOutConfirmBtn, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(
                                                onClick = { showSignOutConfirmDialog = false },
                                                shape = RoundedCornerShape(14.dp)
                                            ) {
                                                Text(Translations.get("cancel", currentLang).uppercase(), fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    )
                                }

                                val logOutBtnText = when (currentLang) {
                                    Language.RU -> "ВЫЙТИ ИЗ АККАУНТА"
                                    Language.UA -> "ВИЙТИ З АКАУНТА"
                                    Language.KK -> "АККАУНТТАН ШЫҒУ"
                                    Language.DE -> "VOM KONTO ABMELDEN"
                                    Language.ZH -> "退出当前账号"
                                    else -> "LOG OUT"
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
                                        text = logOutBtnText,
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
            val avatarFramesList = remember(currentLang) {
                listOf(
                    AvatarFrameStoreData(
                        "standard", ShopPrices.getAvatarFrameCost("standard"),
                        Translations.getLocalizedAvatarFrameTitle("standard", currentLang),
                        Translations.getLocalizedAvatarFrameDesc("standard", currentLang),
                        Color.Gray
                    ),
                    AvatarFrameStoreData(
                        "chrono_gl", ShopPrices.getAvatarFrameCost("chrono_gl"),
                        Translations.getLocalizedAvatarFrameTitle("chrono_gl", currentLang),
                        Translations.getLocalizedAvatarFrameDesc("chrono_gl", currentLang),
                        Color(0xFFFF0077)
                    )
                )
            }
            val playerBadgesList = remember(currentLang) {
                listOf(
                    PlayerBadgeStoreData(
                        "none", ShopPrices.getTitleCost("none"),
                        Translations.getLocalizedTitle("none", currentLang),
                        Translations.getLocalizedBadgeDesc("none", currentLang),
                        ""
                    ),
                    PlayerBadgeStoreData(
                        "node", ShopPrices.getTitleCost("node"),
                        Translations.getLocalizedTitle("node", currentLang),
                        Translations.getLocalizedBadgeDesc("node", currentLang),
                        ""
                    ),
                    PlayerBadgeStoreData(
                        "lord", ShopPrices.getTitleCost("lord"),
                        Translations.getLocalizedTitle("lord", currentLang),
                        Translations.getLocalizedBadgeDesc("lord", currentLang),
                        ""
                    ),
                    PlayerBadgeStoreData(
                        "cosmic_overlord", ShopPrices.getTitleCost("cosmic_overlord"),
                        Translations.getLocalizedTitle("cosmic_overlord", currentLang),
                        Translations.getLocalizedBadgeDesc("cosmic_overlord", currentLang),
                        ""
                    ),
                    PlayerBadgeStoreData(
                        "ai_consensus", ShopPrices.getTitleCost("ai_consensus"),
                        Translations.getLocalizedTitle("ai_consensus", currentLang),
                        Translations.getLocalizedBadgeDesc("ai_consensus", currentLang),
                        ""
                    )
                )
            }
            var selectedStoreCategory by remember { mutableStateOf("ALL") }
            val storeCategories = remember(currentLang) {
                listOf(
                    "ALL" to Translations.getStoreCategoryTitle("ALL", currentLang),
                    "FRAMES" to Translations.getStoreCategoryTitle("FRAMES", currentLang),
                    "TITLES" to Translations.getStoreCategoryTitle("TITLES", currentLang),
                    "SKINS" to Translations.getStoreCategoryTitle("SKINS", currentLang),
                    "BLOCKS" to Translations.getStoreCategoryTitle("BLOCKS", currentLang),
                    "BUTTONS" to Translations.getStoreCategoryTitle("BUTTONS", currentLang),
                    "MODES" to Translations.getStoreCategoryTitle("MODES", currentLang),
                    "RANKS" to Translations.getStoreCategoryTitle("RANKS", currentLang),
                    "TAGS" to Translations.getStoreCategoryTitle("TAGS", currentLang)
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
                        val guestModeText = when (currentLang) {
                            Language.RU -> "Режим гостя: Приобретения сохраняются только локально. Зарегистрируйтесь в профиле."
                            Language.UA -> "Режим гостя: Придбання зберігаються лише локально. Зареєструйтесь у профілі."
                            Language.KK -> "Қонақ режимі: Сатып алулар тек жергілікті сақталады. Профильде тіркеліңіз."
                            Language.DE -> "Gastmodus: Einkäufe werden nur lokal gespeichert. Bitte im Profil registrieren."
                            Language.ZH -> "访客模式：购买项仅保存在本地。请在个人资料页注册账号。"
                            else -> "Guest Mode: Purchases are saved locally. Please register in the profile tab."
                        }
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
                                    text = guestModeText,
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
                        val ownedRanksCount = ranksList.count { purchasedRanks.contains(it.id) }
                        val ranksSectionTitle = when (currentLang) {
                            Language.RU -> "РАНГИ"
                            Language.UA -> "РАНГИ"
                            Language.KK -> "РАНГТАР"
                            Language.DE -> "RÄNGE"
                            Language.ZH -> "段位"
                            else -> "RANKS"
                        }
                        StoreSectionHeader(
                            title = ranksSectionTitle,
                            icon = Icons.Default.MilitaryTech,
                            purchasedCount = ownedRanksCount,
                            totalCount = ranksList.size,
                            currentLang = currentLang
                        )
                    }
                    items(ranksList) { rank ->
                        val rankIndex = ranksList.indexOfFirst { it.id == rank.id }
                        val isCurrent = rank.id == onlineTier
                        val isOwned = purchasedRanks.contains(rank.id)
                        val prevRankId = if (rankIndex > 0) ranksList[rankIndex - 1].id else null
                        val isNext = !isOwned && (prevRankId == null || purchasedRanks.contains(prevRankId))
                        val isLocked = !isOwned && !isNext
                        val rankCategoryLabel = when (currentLang) {
                            Language.RU -> "Ранг"
                            Language.UA -> "Ранг"
                            Language.KK -> "Ранг"
                            Language.DE -> "Rang"
                            Language.ZH -> "段位"
                            else -> "Rank"
                        }

                        StoreItemCard(
                            icon = Icons.Default.MilitaryTech,
                            category = rankCategoryLabel,
                            title = Translations.getLocalizedRank(rank.id, currentLang),
                            description = rank.description,
                            isActive = isCurrent,
                            isOwned = isOwned,
                            cost = rank.cost,
                            currentLang = currentLang,
                            isLocked = isLocked,
                            onAction = {
                                if (isCurrent) {
                                    viewModel.setOnlineTier("BRONZE")
                                    val resetMsg = when (currentLang) {
                                        Language.RU -> "Ранг сброшен до базового"
                                        Language.UA -> "Ранг скинуто до базового"
                                        Language.KK -> "Ранг негізгіге қайтарылды"
                                        Language.DE -> "Rang auf BRONZE zurückgesetzt."
                                        Language.ZH -> "段位已重置为青铜。"
                                        else -> "Rank reset to BRONZE."
                                    }
                                    triggerMessage(resetMsg)
                                } else if (isOwned) {
                                    viewModel.setOnlineTier(rank.id)
                                    val selectedMsg = when (currentLang) {
                                        Language.RU -> "Ранг успешно выбран"
                                        Language.UA -> "Ранг успішно обрано"
                                        Language.KK -> "Ранг сәтті таңдалды"
                                        Language.DE -> "Rang erfolgreich aktualisiert"
                                        Language.ZH -> "段位更新成功"
                                        else -> "Rank updated to ${rank.id}."
                                    }
                                    triggerMessage(selectedMsg)
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
                        val gridThemesTitle = when (currentLang) {
                            Language.RU -> "ОФОРМЛЕНИЕ СЕТКИ"
                            Language.UA -> "ОФОРМЛЕННЯ СІТКИ"
                            Language.KK -> "ТОР ДИЗАЙНЫ"
                            Language.DE -> "RASTER-DESIGNS"
                            Language.ZH -> "网格主题"
                            else -> "GRID THEMES"
                        }
                        StoreSectionHeader(
                            title = gridThemesTitle,
                            icon = Icons.Default.Palette,
                            purchasedCount = purchasedSkinsSet.size,
                            totalCount = skinsList.size,
                            currentLang = currentLang
                        )
                    }
                    items(skinsList) { skin ->
                        val isEquipped = boardSkin == skin.id
                        val isOwned = purchasedSkinsSet.contains(skin.id)
                        val themeCategoryLabel = when (currentLang) {
                            Language.RU -> "Оформление"
                            Language.UA -> "Оформлення"
                            Language.KK -> "Дизайн"
                            Language.DE -> "Design"
                            Language.ZH -> "主题"
                            else -> "Theme"
                        }
                        StoreItemCard(
                            icon = Icons.Default.Palette,
                            category = themeCategoryLabel,
                            title = skin.displayName,
                            description = skin.description,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            cost = skin.cost,
                            currentLang = currentLang,
                            onAction = {
                                if (isEquipped) {
                                    viewModel.setBoardColorSkin("cyberpunk")
                                    val resetMsg = when (currentLang) {
                                        Language.RU -> "Оформление сброшено"
                                        Language.UA -> "Оформлення скинуто"
                                        Language.KK -> "Дизайн қалпына келтірілді"
                                        Language.DE -> "Raster-Design zurückgesetzt."
                                        Language.ZH -> "网格主题已重置"
                                        else -> "Grid theme reset to cyberpunk."
                                    }
                                    triggerMessage(resetMsg)
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
                        val blockStylesTitle = when (currentLang) {
                            Language.RU -> "СТИЛИ БЛОКОВ"
                            Language.UA -> "СТИЛІ БЛОКІВ"
                            Language.KK -> "БЛОК СТИЛЬДЕРІ"
                            Language.DE -> "BLOCK-DESIGNS"
                            Language.ZH -> "方块样式"
                            else -> "BLOCK STYLES"
                        }
                        StoreSectionHeader(
                            title = blockStylesTitle,
                            icon = Icons.Default.Category,
                            purchasedCount = purchasedCubeSkinsSet.size,
                            totalCount = cubeSkinsList.size,
                            currentLang = currentLang
                        )
                    }
                    items(cubeSkinsList) { cSkin ->
                        val isEquipped = blockStyle == cSkin.id
                        val isOwned = purchasedCubeSkinsSet.contains(cSkin.id)
                        val blockStyleCategoryLabel = when (currentLang) {
                            Language.RU -> "Стиль блоков"
                            Language.UA -> "Стиль блоків"
                            Language.KK -> "Блок стилі"
                            Language.DE -> "Block-Stil"
                            Language.ZH -> "方块样式"
                            else -> "Block Style"
                        }
                        StoreItemCard(
                            icon = Icons.Default.Category,
                            category = blockStyleCategoryLabel,
                            title = cSkin.displayName,
                            description = cSkin.description,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            cost = cSkin.cost,
                            currentLang = currentLang,
                            onAction = {
                                if (isEquipped) {
                                    viewModel.setBlockStyle("glass")
                                    val resetMsg = when (currentLang) {
                                        Language.RU -> "Стиль блоков сброшен"
                                        Language.UA -> "Стиль блоків скинуто"
                                        Language.KK -> "Блок стилі қалпына келтірілді"
                                        Language.DE -> "Block-Design zurückgesetzt."
                                        Language.ZH -> "方块样式已重置"
                                        else -> "Cube style reset to glass."
                                    }
                                    triggerMessage(resetMsg)
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
                        val gameModesTitle = when (currentLang) {
                            Language.RU -> "ИГРОВЫЕ РЕЖИМЫ"
                            Language.UA -> "ІГРОВІ РЕЖИМИ"
                            Language.KK -> "ОЙЫН РЕЖИМДЕРІ"
                            Language.DE -> "SPIELMODI"
                            Language.ZH -> "游戏模式"
                            else -> "GAME MODES"
                        }
                        StoreSectionHeader(
                            title = gameModesTitle,
                            icon = Icons.Default.PlayCircleOutline,
                            purchasedCount = purchasedModesSet.size,
                            totalCount = premiumModesList.size,
                            currentLang = currentLang
                        )
                    }
                    items(premiumModesList) { pMode ->
                        val isOwned = purchasedModesSet.contains(pMode.id)
                        val modeCategoryLabel = when (currentLang) {
                            Language.RU -> "Режим"
                            Language.UA -> "Режим"
                            Language.KK -> "Режим"
                            Language.DE -> "Modus"
                            Language.ZH -> "模式"
                            else -> "Game Mode"
                        }
                        StoreItemCard(
                            icon = Icons.Default.PlayCircleOutline,
                            category = modeCategoryLabel,
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
                        val avatarFramesTitle = when (currentLang) {
                            Language.RU -> "РАМКИ АВАТАРА"
                            Language.UA -> "РАМКИ АВАТАРА"
                            Language.KK -> "АВАТАР ЖАҚТАУЛАРЫ"
                            Language.DE -> "AVATAR-RAHMEN"
                            Language.ZH -> "头像相框"
                            else -> "AVATAR FRAMES"
                        }
                        StoreSectionHeader(
                            title = avatarFramesTitle,
                            icon = Icons.Default.Portrait,
                            purchasedCount = purchasedAvatarFrames.size,
                            totalCount = avatarFramesList.size,
                            currentLang = currentLang
                        )
                    }
                    items(avatarFramesList) { frame ->
                        val isEquipped = equippedAvatarFrame == frame.id
                        val isOwned = purchasedAvatarFrames.contains(frame.id)
                        val frameCategoryLabel = when (currentLang) {
                            Language.RU -> "Рамка"
                            Language.UA -> "Рамка"
                            Language.KK -> "Жақтау"
                            Language.DE -> "Rahmen"
                            Language.ZH -> "相框"
                            else -> "Avatar Frame"
                        }
                        StoreItemCard(
                            icon = Icons.Default.Portrait,
                            category = frameCategoryLabel,
                            title = frame.displayName,
                            description = frame.description,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            cost = frame.cost,
                            currentLang = currentLang,
                            onAction = {
                                if (isEquipped) {
                                    viewModel.setEquippedAvatarFrame("standard")
                                    val resetMsg = when (currentLang) {
                                        Language.RU -> "Рамка аватара сброшена"
                                        Language.UA -> "Рамку аватара скинуто"
                                        Language.KK -> "Аватар жақтауы қалпына келтірілді"
                                        Language.DE -> "Avatar-Rahmen zurückgesetzt."
                                        Language.ZH -> "头像相框已重置"
                                        else -> "Avatar frame reset to standard."
                                    }
                                    triggerMessage(resetMsg)
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
                        val playerTitlesTitle = when (currentLang) {
                            Language.RU -> "ТИТУЛЫ ИГРОКА"
                            Language.UA -> "ТИТУЛИ ГРАВЦЯ"
                            Language.KK -> "ОЙЫНШЫ АТАҚТАРЫ"
                            Language.DE -> "SPIELERTITEL"
                            Language.ZH -> "玩家称号"
                            else -> "PLAYER TITLES"
                        }
                        StoreSectionHeader(
                            title = playerTitlesTitle,
                            icon = Icons.Default.WorkspacePremium,
                            purchasedCount = purchasedTitles.size,
                            totalCount = playerBadgesList.size,
                            currentLang = currentLang
                        )
                    }
                    items(playerBadgesList) { title ->
                        val isEquipped = equippedTitle == title.id
                        val isOwned = purchasedTitles.contains(title.id)
                        val titleCategoryLabel = when (currentLang) {
                            Language.RU -> "Титул"
                            Language.UA -> "Титул"
                            Language.KK -> "Атақ"
                            Language.DE -> "Titel"
                            Language.ZH -> "称号"
                            else -> "Player Title"
                        }
                        StoreItemCard(
                            icon = Icons.Default.WorkspacePremium,
                            category = titleCategoryLabel,
                            title = title.displayName,
                            description = title.description,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            cost = title.cost,
                            currentLang = currentLang,
                            onAction = {
                                if (isEquipped) {
                                    viewModel.setEquippedTitle("none")
                                    val resetMsg = when (currentLang) {
                                        Language.RU -> "Титул сброшен"
                                        Language.UA -> "Титул скинуто"
                                        Language.KK -> "Атақ қалпына келтірілді"
                                        Language.DE -> "Titel zurückgesetzt."
                                        Language.ZH -> "称号已重置"
                                        else -> "Title reset to none."
                                    }
                                    triggerMessage(resetMsg)
                                } else {
                                    selectTitle(title.id, title.cost)
                                }
                            }
                        )
                    }
                }

                // 9. CONTROL BUTTON STYLES
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "BUTTONS") {
                    item {
                        val controlButtonsTitle = when (currentLang) {
                            Language.RU -> "ДИЗАЙН КНОПОК"
                            Language.UA -> "ДИЗАЙН КНОПОК"
                            Language.KK -> "БАТЫРМАЛАР ДИЗАЙНЫ"
                            Language.DE -> "TASTEN-DESIGNS"
                            Language.ZH -> "按键样式"
                            else -> "CONTROL BUTTON STYLES"
                        }
                        StoreSectionHeader(
                            title = controlButtonsTitle,
                            icon = Icons.Default.Extension,
                            purchasedCount = purchasedControlButtonStyles.size,
                            totalCount = controlButtonStylesList.size,
                            currentLang = currentLang
                        )
                    }
                    items(controlButtonStylesList) { btnStyle ->
                        val isEquipped = controlButtonStyle == btnStyle.id
                        val isOwned = purchasedControlButtonStyles.contains(btnStyle.id)
                        val buttonCategoryLabel = when (currentLang) {
                            Language.RU -> "Кнопки"
                            Language.UA -> "Кнопки"
                            Language.KK -> "Батырмалар"
                            Language.DE -> "Tasten"
                            Language.ZH -> "按键"
                            else -> "Button Design"
                        }
                        StoreItemCard(
                            icon = Icons.Default.Extension,
                            category = buttonCategoryLabel,
                            title = btnStyle.displayName,
                            description = btnStyle.description,
                            isActive = isEquipped,
                            isOwned = isOwned,
                            cost = btnStyle.cost,
                            currentLang = currentLang,
                            onAction = {
                                if (isEquipped) {
                                    viewModel.setControlButtonStyle("classic")
                                    val resetMsg = when (currentLang) {
                                        Language.RU -> "Стиль кнопок сброшен"
                                        Language.UA -> "Стиль кнопок скинуто"
                                        Language.KK -> "Батырмалар стилі қалпына келтірілді"
                                        Language.DE -> "Tasten-Design zurückgesetzt."
                                        Language.ZH -> "按键样式已重置"
                                        else -> "Button style reset to classic."
                                    }
                                    triggerMessage(resetMsg)
                                } else {
                                    selectControlButtonStyle(btnStyle.id, btnStyle.cost)
                                }
                            }
                        )
                    }
                }
                
                // 10. PRESTIGE SYSTEM (I, II, III)
                if (selectedStoreCategory == "ALL" || selectedStoreCategory == "PRESTIGE") {
                    item {
                        val prestigeLvl by viewModel.prestigeLevel.collectAsStateWithLifecycle()
                        val prestigeSectionTitle = when (currentLang) {
                            Language.RU -> "СИСТЕМА ПРЕСТИЖА"
                            Language.UA -> "СИСТЕМА ПРЕСТИЖУ"
                            Language.KK -> "ПРЕСТИЖ ЖҮЙЕСІ"
                            Language.DE -> "PRESTIGE-SYSTEM"
                            Language.ZH -> "声望系统"
                            else -> "PRESTIGE SYSTEM"
                        }
                        StoreSectionHeader(
                            title = prestigeSectionTitle,
                            icon = Icons.Default.AutoAwesome,
                            purchasedCount = prestigeLvl.coerceIn(0, 3),
                            totalCount = 3,
                            currentLang = currentLang
                        )
                    }

                    item {
                        val prestigeLvl by viewModel.prestigeLevel.collectAsStateWithLifecycle()
                        var confirmPrestigeTarget by remember { mutableStateOf<Int?>(null) }

                        val prestigeCategoryLabel = when (currentLang) {
                            Language.RU -> "Престиж"
                            Language.UA -> "Престиж"
                            Language.KK -> "Престиж"
                            Language.DE -> "Prestige"
                            Language.ZH -> "声望"
                            else -> "Prestige"
                        }

                        // ── PRESTIGE I (x2) ──
                        val isP1Active = prestigeLvl >= 1
                        val p1Title = when (currentLang) {
                            Language.RU -> "Престиж I (Множитель x2)"
                            Language.UA -> "Престиж I (Множник x2)"
                            Language.KK -> "Престиж I (Көбейткіш x2)"
                            Language.DE -> "Prestige I (x2 Multiplikator)"
                            Language.ZH -> "声望 I (2倍金币加成)"
                            else -> "Prestige I (x2 Multiplier)"
                        }
                        val p1Desc = if (isP1Active) {
                            when (currentLang) {
                                Language.RU -> "Престиж I активен! Постоянный множитель x2 ко всем заработкам монет."
                                Language.UA -> "Престиж I активний! Постійний множник x2 до всіх заробітків монет."
                                Language.KK -> "Престиж I белсенді! Барлық тиын табысына тұрақты x2 көбейткіш."
                                Language.DE -> "Prestige I aktiv! Dauerhafter 2-fach-Multiplikator auf alle Münzbelohnungen."
                                Language.ZH -> "声望 I 已激活！所有金币获取享受永久 2 倍加成。"
                                else -> "Prestige I is active! Permanent x2 multiplier to all coin rewards."
                            }
                        } else {
                            when (currentLang) {
                                Language.RU -> "Требуется 100,000 🪙. Добровольный сброс баланса даёт вечный x2 множитель ко всем доходам!"
                                Language.UA -> "Потрібно 100,000 🪙. Добровільне скидання балансу дає вічний x2 множник до всіх доходів!"
                                Language.KK -> "100,000 🪙 қажет. Балансты нөлдеу барлық табысқа мәңгілік x2 көбейткіш береді!"
                                Language.DE -> "Erfordert 100.000 🪙. Setze Guthaben auf 0 zurück für dauerhaften 2-fachen Multiplikator!"
                                Language.ZH -> "需要 100,000 🪙。自愿重置金币为0，即可获得全收益永久2倍加成！"
                                else -> "Requires 100,000 🪙. Reset balance to 0 to unlock permanent x2 earnings multiplier!"
                            }
                        }

                        StoreItemCard(
                            icon = Icons.Default.AutoAwesome,
                            category = prestigeCategoryLabel,
                            title = p1Title,
                            description = p1Desc,
                            isActive = isP1Active,
                            isOwned = isP1Active,
                            cost = ShopPrices.PRESTIGE_I_REQUIREMENT,
                            currentLang = currentLang,
                            onAction = {
                                if (isP1Active) {
                                    val activeMsg = when (currentLang) {
                                        Language.RU -> "Престиж I уже активирован!"
                                        Language.UA -> "Престиж I вже активовано!"
                                        Language.KK -> "Престиж I белсендірілген!"
                                        Language.DE -> "Prestige I ist bereits aktiv!"
                                        Language.ZH -> "声望 I 已经处于激活状态！"
                                        else -> "Prestige I is already active!"
                                    }
                                    triggerMessage(activeMsg)
                                } else {
                                    if (credits >= ShopPrices.PRESTIGE_I_REQUIREMENT) {
                                        confirmPrestigeTarget = 1
                                    } else {
                                        viewModel.triggerAudioFeedback("error")
                                        val needMsg = when (currentLang) {
                                            Language.RU -> "Необходимо накопить 100,000 🪙"
                                            Language.UA -> "Необхідно накопичити 100,000 🪙"
                                            Language.KK -> "100,000 🪙 жинау қажет"
                                            Language.DE -> "Es müssen zuerst 100.000 🪙 gespart werden"
                                            Language.ZH -> "需要先攒够 100,000 🪙"
                                            else -> "Need to save 100,000 🪙 first"
                                        }
                                        triggerMessage(needMsg, isError = true)
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── PRESTIGE II (x4) ──
                        val isP2Active = prestigeLvl >= 2
                        val p2Title = when (currentLang) {
                            Language.RU -> "Престиж II (Множитель x4)"
                            Language.UA -> "Престиж II (Множник x4)"
                            Language.KK -> "Престиж II (Көбейткіш x4)"
                            Language.DE -> "Prestige II (x4 Multiplikator)"
                            Language.ZH -> "声望 II (4倍金币加成)"
                            else -> "Prestige II (x4 Multiplier)"
                        }
                        val p2Desc = if (isP2Active) {
                            when (currentLang) {
                                Language.RU -> "Престиж II активен! Постоянный множитель x4 ко всем заработкам монет."
                                Language.UA -> "Престиж II активний! Постійний множник x4 до всіх заробітків монет."
                                Language.KK -> "Престиж II белсенді! Барлық тиын табысына тұрақты x4 көбейткіш."
                                Language.DE -> "Prestige II aktiv! Dauerhafter 4-fach-Multiplikator auf alle Münzbelohnungen."
                                Language.ZH -> "声望 II 已激活！所有金币获取享受永久 4 倍加成。"
                                else -> "Prestige II is active! Permanent x4 multiplier to all coin rewards."
                            }
                        } else {
                            when (currentLang) {
                                Language.RU -> "Требуется 400,000 🪙 и Престиж I. Добровольный сброс баланса даёт вечный x4 множитель ко всем доходам!"
                                Language.UA -> "Потрібно 400,000 🪙 та Престиж I. Добровільне скидання балансу дає вічний x4 множник до всіх доходів!"
                                Language.KK -> "400,000 🪙 және Престиж I қажет. Балансты нөлдеу барлық табысқа мәңгілік x4 көбейткіш береді!"
                                Language.DE -> "Erfordert 400.000 🪙 und Prestige I. Setze Guthaben auf 0 zurück für dauerhaften 4-fachen Multiplikator!"
                                Language.ZH -> "需要 400,000 🪙 及声望 I。自愿重置金币为0，即可获得全收益永久4倍加成！"
                                else -> "Requires 400,000 🪙 and Prestige I. Reset balance to 0 to unlock permanent x4 earnings multiplier!"
                            }
                        }

                        StoreItemCard(
                            icon = Icons.Default.AutoAwesome,
                            category = prestigeCategoryLabel,
                            title = p2Title,
                            description = p2Desc,
                            isActive = isP2Active,
                            isOwned = isP2Active,
                            cost = ShopPrices.PRESTIGE_II_REQUIREMENT,
                            currentLang = currentLang,
                            onAction = {
                                if (isP2Active) {
                                    val activeMsg = when (currentLang) {
                                        Language.RU -> "Престиж II уже активирован!"
                                        Language.UA -> "Престиж II вже активовано!"
                                        Language.KK -> "Престиж II белсендірілген!"
                                        Language.DE -> "Prestige II ist bereits aktiv!"
                                        Language.ZH -> "声望 II 已经处于激活状态！"
                                        else -> "Prestige II is already active!"
                                    }
                                    triggerMessage(activeMsg)
                                } else if (prestigeLvl < 1) {
                                    viewModel.triggerAudioFeedback("error")
                                    val reqMsg = when (currentLang) {
                                        Language.RU -> "Сначала активируйте Престиж I!"
                                        Language.UA -> "Спочатку активуйте Престиж I!"
                                        Language.KK -> "Алдымен Престиж I белсендіріңіз!"
                                        Language.DE -> "Aktiviere zuerst Prestige I!"
                                        Language.ZH -> "请先激活声望 I！"
                                        else -> "Activate Prestige I first!"
                                    }
                                    triggerMessage(reqMsg, isError = true)
                                } else {
                                    if (credits >= ShopPrices.PRESTIGE_II_REQUIREMENT) {
                                        confirmPrestigeTarget = 2
                                    } else {
                                        viewModel.triggerAudioFeedback("error")
                                        val needMsg = when (currentLang) {
                                            Language.RU -> "Необходимо накопить 400,000 🪙"
                                            Language.UA -> "Необхідно накопичити 400,000 🪙"
                                            Language.KK -> "400,000 🪙 жинау қажет"
                                            Language.DE -> "Es müssen zuerst 400.000 🪙 gespart werden"
                                            Language.ZH -> "需要先攒够 400,000 🪙"
                                            else -> "Need to save 400,000 🪙 first"
                                        }
                                        triggerMessage(needMsg, isError = true)
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── PRESTIGE III (x8 + Tag) ──
                        val isP3Active = prestigeLvl >= 3
                        val p3Title = when (currentLang) {
                            Language.RU -> "Престиж III (Множитель x8 + Тег)"
                            Language.UA -> "Престиж III (Множник x8 + Тег)"
                            Language.KK -> "Престиж III (Көбейткіш x8 + Тег)"
                            Language.DE -> "Prestige III (x8 Multiplikator + Tag)"
                            Language.ZH -> "声望 III (8倍金币加成 + 专属标签)"
                            else -> "Prestige III (x8 Multiplier + Tag)"
                        }
                        val p3Desc = if (isP3Active) {
                            when (currentLang) {
                                Language.RU -> "Престиж III активен! Постоянный множитель x8 ко всем заработкам монет и Личный Тег."
                                Language.UA -> "Престиж III активний! Постійний множник x8 до всіх заробітків монет та Особистий Тег."
                                Language.KK -> "Престиж III белсенді! Барлық тиын табысына тұрақты x8 көбейткіш пен Жеке Тег."
                                Language.DE -> "Prestige III aktiv! Dauerhafter 8-fach-Multiplikator und Bestenlisten-Tag."
                                Language.ZH -> "声望 III 已激活！所有金币获取享受永久 8 倍加成及专属标签。"
                                else -> "Prestige III is active! Permanent x8 multiplier to all coin rewards and Custom Tag."
                            }
                        } else {
                            when (currentLang) {
                                Language.RU -> "Требуется 1,000,000 🪙 и Престиж II. Добровольный сброс баланса даёт Личный Тег и вечный x8 множитель ко всем доходам!"
                                Language.UA -> "Потрібно 1,000,000 🪙 та Престиж II. Добровільне скидання балансу дає Особистий Тег та вічний x8 множник до всіх доходів!"
                                Language.KK -> "1,000,000 🪙 және Престиж II қажет. Балансты нөлдеу Жеке Тег пен барлық табысқа мәңгілік x8 көбейткіш береді!"
                                Language.DE -> "Erfordert 1.000.000 🪙 und Prestige II. Setze Guthaben auf 0 zurück für Bestenlisten-Tag und dauerhaften 8-fachen Multiplikator!"
                                Language.ZH -> "需要 1,000,000 🪙 及声望 II。自愿重置金币为0，即可获得专属标签及全收益永久8倍加成！"
                                else -> "Requires 1,000,000 🪙 and Prestige II. Reset balance to 0 to unlock Leaderboard Tag and permanent x8 earnings multiplier!"
                            }
                        }

                        StoreItemCard(
                            icon = Icons.Default.AutoAwesome,
                            category = prestigeCategoryLabel,
                            title = p3Title,
                            description = p3Desc,
                            isActive = isP3Active,
                            isOwned = isP3Active,
                            cost = ShopPrices.PRESTIGE_III_REQUIREMENT,
                            currentLang = currentLang,
                            onAction = {
                                if (isP3Active) {
                                    val activeMsg = when (currentLang) {
                                        Language.RU -> "Престиж III уже активирован! Множитель x8 активен."
                                        Language.UA -> "Престиж III вже активовано! Множник x8 активний."
                                        Language.KK -> "Престиж III белсендірілген! x8 көбейткіш жұмыс істеп тұр."
                                        Language.DE -> "Prestige III ist bereits aktiv! Multiplikator x8 wird angewendet."
                                        Language.ZH -> "声望 III 已经处于激活状态！8倍加成生效中。"
                                        else -> "Prestige III is already active! Multiplier x8 is applied."
                                    }
                                    triggerMessage(activeMsg)
                                } else if (prestigeLvl < 2) {
                                    viewModel.triggerAudioFeedback("error")
                                    val reqMsg = when (currentLang) {
                                        Language.RU -> "Сначала активируйте Престиж II!"
                                        Language.UA -> "Спочатку активуйте Престиж II!"
                                        Language.KK -> "Алдымен Престиж II белсендіріңіз!"
                                        Language.DE -> "Aktiviere zuerst Prestige II!"
                                        Language.ZH -> "请先激活声望 II！"
                                        else -> "Activate Prestige II first!"
                                    }
                                    triggerMessage(reqMsg, isError = true)
                                } else {
                                    if (credits >= ShopPrices.PRESTIGE_III_REQUIREMENT) {
                                        confirmPrestigeTarget = 3
                                    } else {
                                        viewModel.triggerAudioFeedback("error")
                                        val needMsg = when (currentLang) {
                                            Language.RU -> "Необходимо накопить 1,000,000 🪙"
                                            Language.UA -> "Необхідно накопичити 1,000,000 🪙"
                                            Language.KK -> "1,000,000 🪙 жинау қажет"
                                            Language.DE -> "Es müssen zuerst 1.000.000 🪙 gespart werden"
                                            Language.ZH -> "需要先攒够 1,000,000 🪙"
                                            else -> "Need to save 1,000,000 🪙 first"
                                        }
                                        triggerMessage(needMsg, isError = true)
                                    }
                                }
                            }
                        )

                        confirmPrestigeTarget?.let { targetLevel ->
                            val romanNumeral = when (targetLevel) {
                                1 -> "I"
                                2 -> "II"
                                else -> "III"
                            }
                            val multiplierText = when (targetLevel) {
                                1 -> "x2"
                                2 -> "x4"
                                else -> "x8"
                            }
                            val dialogTitle = when (currentLang) {
                                Language.RU -> "Активировать Престиж $romanNumeral?"
                                Language.UA -> "Активувати Престиж $romanNumeral?"
                                Language.KK -> "Престиж $romanNumeral белсендіру керек пе?"
                                Language.DE -> "Prestige $romanNumeral aktivieren?"
                                Language.ZH -> "激活声望 $romanNumeral？"
                                else -> "Activate Prestige $romanNumeral?"
                            }
                            val extraBenefit = if (targetLevel >= 3) {
                                when (currentLang) {
                                    Language.RU -> "\n• Бесплатный Личный Тег для таблицы рекордов"
                                    Language.UA -> "\n• Безкоштовний Особистий Тег для таблиці рекордів"
                                    Language.KK -> "\n• Рекордтар кестесі үшін тегін Жеке Тег"
                                    Language.DE -> "\n• Kostenloser Bestenlisten-Tag"
                                    Language.ZH -> "\n• 免费排行榜专属标签"
                                    else -> "\n• Free Custom Leaderboard Tag"
                                }
                            } else ""
                            val dialogText = when (currentLang) {
                                Language.RU -> "Внимание! Ваш баланс монет будет сброшен до 0.\n\nВы получите навсегда:\n• Постоянный множитель $multiplierText ко всем заработкам монет$extraBenefit\n• Знак Престижа $romanNumeral"
                                Language.UA -> "Увага! Ваш баланс монет буде скинуто до 0.\n\nВи отримаєте назавжди:\n• Постійний множник $multiplierText до всіх заробітків монет$extraBenefit\n• Знак Престижу $romanNumeral"
                                Language.KK -> "Назар аударыңыз! Сіздің тиын балансыңыз 0-ге дейін нөлденеді.\n\nСіз мәңгілікке аласыз:\n• Барлық тиын табысына тұрақты $multiplierText көбейткіш$extraBenefit\n• Престиж $romanNumeral белгісі"
                                Language.DE -> "Achtung! Dein Münzguthaben wird auf 0 zurückgesetzt.\n\nDu erhältst dauerhaft:\n• Dauerhafter $multiplierText-Multiplikator auf alle Münzbelohnungen$extraBenefit\n• Prestige $romanNumeral-Abzeichen"
                                Language.ZH -> "注意！您的金币余额将被重置为 0。\n\n您将永久获得：\n• 所有金币获取永久 $multiplierText 倍加成$extraBenefit\n• 声望 $romanNumeral 专属勋章"
                                else -> "Attention! Your coin balance will be reset to 0.\n\nYou will permanently receive:\n• Permanent $multiplierText multiplier to all coin rewards$extraBenefit\n• Prestige $romanNumeral Badge"
                            }
                            val confirmBtn = when (currentLang) {
                                Language.RU -> "СБРОСИТЬ И АКТИВИРОВАТЬ"
                                Language.UA -> "СКИНУТИ ТА АКТИВУВАТИ"
                                Language.KK -> "НӨЛДЕП БЕЛСЕНДІРУ"
                                Language.DE -> "ZURÜCKSETZEN & AKTIVIEREN"
                                Language.ZH -> "重置并激活"
                                else -> "RESET & ACTIVATE"
                            }
                            AlertDialog(
                                onDismissRequest = { confirmPrestigeTarget = null },
                                icon = { Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(36.dp)) },
                                title = {
                                    Text(
                                        text = dialogTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                },
                                text = {
                                    Text(
                                        text = dialogText,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            confirmPrestigeTarget = null
                                            viewModel.activatePrestige(targetLevel)
                                            viewModel.triggerAudioFeedback("success")
                                            val activatedMsg = when (currentLang) {
                                                Language.RU -> "Престиж $romanNumeral активирован! Множитель $multiplierText получен!"
                                                Language.UA -> "Престиж $romanNumeral активовано! Множник $multiplierText отримано!"
                                                Language.KK -> "Престиж $romanNumeral белсендірілді! $multiplierText көбейткіш алынды!"
                                                Language.DE -> "Prestige $romanNumeral aktiviert! $multiplierText-Multiplikator freigeschaltet!"
                                                Language.ZH -> "声望 $romanNumeral 激活成功！已获得 $multiplierText 倍金币加成！"
                                                else -> "Prestige $romanNumeral activated! $multiplierText Multiplier unlocked!"
                                            }
                                            triggerMessage(activatedMsg)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700), contentColor = Color.Black)
                                    ) {
                                        Text(confirmBtn, fontWeight = FontWeight.Bold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { confirmPrestigeTarget = null }) {
                                        Text(Translations.get("cancel", currentLang))
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
                    val profileSettingsTitle = when (currentLang) {
                        Language.RU -> "НАСТРОЙКИ ПРОФИЛЯ"
                        Language.UA -> "НАЛАШТУВАННЯ ПРОФІЛЮ"
                        Language.KK -> "ПРОФИЛЬ БАПТАУЛАРЫ"
                        Language.DE -> "PROFIL-EINSTELLUNGEN"
                        Language.ZH -> "个人资料设置"
                        else -> "PROFILE SETTINGS"
                    }
                    CenterAlignedTopAppBar(
                        title = {
                            Text(
                                text = profileSettingsTitle,
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
                            val avatarAndBgTitle = when (currentLang) {
                                Language.RU -> "АВАТАР И ФОН"
                                Language.UA -> "АВАТАР ТА ФОН"
                                Language.KK -> "АВАТАР ЖӘНЕ ФОН"
                                Language.DE -> "AVATAR & HINTERGRUND"
                                Language.ZH -> "头像与背景"
                                else -> "AVATAR & BACKGROUND"
                            }
                            Text(
                                text = avatarAndBgTitle,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Live Avatar Preview
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
                                isOnline = true
                            )

                            // Photo Picker Buttons
                            val pickPhotoBtnText = when (currentLang) {
                                Language.RU -> "Выбрать фото"
                                Language.UA -> "Обрати фото"
                                Language.KK -> "Фото таңдау"
                                Language.DE -> "Foto wählen"
                                Language.ZH -> "选择头像"
                                else -> "Pick Photo"
                            }
                            val pickBgBtnText = when (currentLang) {
                                Language.RU -> "Фон карты"
                                Language.UA -> "Фон карти"
                                Language.KK -> "Карта фоны"
                                Language.DE -> "Karten-Hintergrund"
                                Language.ZH -> "卡片背景"
                                else -> "Pick BG"
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
                                        text = pickPhotoBtnText,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
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
                                        text = pickBgBtnText,
                                        fontWeight = FontWeight.Bold,
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
                                val resetPhotoText = when (currentLang) {
                                    Language.RU -> "Сбросить фото"
                                    Language.UA -> "Скинути фото"
                                    Language.KK -> "Фотоны қалпына келтіру"
                                    Language.DE -> "Foto zurücksetzen"
                                    Language.ZH -> "重置头像"
                                    else -> "Reset Photo"
                                }
                                val resetBgText = when (currentLang) {
                                    Language.RU -> "Сбросить фон"
                                    Language.UA -> "Скинути фон"
                                    Language.KK -> "Фонды қалпына келтіру"
                                    Language.DE -> "Hintergrund zurücksetzen"
                                    Language.ZH -> "重置背景"
                                    else -> "Reset BG"
                                }
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
                                                viewModel.saveCurrentProfileToDb()
                                                viewModel.triggerAudioFeedback("click")
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(resetPhotoText, fontSize = 11.sp)
                                        }
                                    }
                                    if (hasCustomBg) {
                                        OutlinedButton(
                                            onClick = {
                                                val file = File(context.filesDir, "custom_background_${playerName}.jpg")
                                                if (file.exists()) file.delete()
                                                sharedPrefs.edit().putBoolean("has_custom_background_${playerName}", false).apply()
                                                bgChangeCounter++
                                                viewModel.saveCurrentProfileToDb()
                                                viewModel.triggerAudioFeedback("click")
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(resetBgText, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // Emoji Avatar Picker
                            val chooseEmojiTitle = when (currentLang) {
                                Language.RU -> "ВЫБЕРИТЕ СМАЙЛИК / ИКОНКУ"
                                Language.UA -> "ОБЕРІТЬ СМАЙЛИК / ІКОНКУ"
                                Language.KK -> "СМАЙЛИК / БЕЛГІШЕНІ ТАҢДАҢЫЗ"
                                Language.DE -> "EMOJI / SYMBOL WÄHLEN"
                                Language.ZH -> "选择表情 / 图标"
                                else -> "CHOOSE EMOJI / ICON"
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = chooseEmojiTitle,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val emojiPresets = listOf(
                                    "🤖", "🐱", "👑", "🔥", "⚡", "👾", "💎", "🌟",
                                    "🚀", "🛡️", "🎯", "🎮", "🦊", "🐯", "🦁", "🐉",
                                    "💀", "🏆", "😎", "👻", "⭐", "🍕", "🦄", "🔮"
                                )

                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                                ) {
                                    items(emojiPresets) { emoji ->
                                        val isSelected = customAvatarEmoji == emoji
                                        Surface(
                                            onClick = {
                                                viewModel.setCustomAvatarEmoji(emoji)
                                                viewModel.triggerAudioFeedback("click")
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) themeColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                                            border = if (isSelected) BorderStroke(2.dp, themeColor) else null,
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(text = emoji, fontSize = 20.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Avatar Background Color Swatches
                            val avatarBgColorTitle = when (currentLang) {
                                Language.RU -> "ЦВЕТ ФОНА АВАТАРА"
                                Language.UA -> "КОЛІР ФОНУ АВАТАРА"
                                Language.KK -> "АВАТАР ФОНЫНЫҢ ТҮСІ"
                                Language.DE -> "AVATAR-HINTERGRUNDFARBE"
                                Language.ZH -> "头像背景颜色"
                                else -> "AVATAR BACKGROUND COLOR"
                            }
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = avatarBgColorTitle,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val colorSwatches = listOf(
                                    "3A3C44", "6C63FF", "00B4D8", "06D6A0", "FFB703", "FB5607",
                                    "FF006E", "8338EC", "3A86FF", "2EC4B6", "E71D36", "1A1A24",
                                    "2D3748", "D97706", "059669"
                                )

                                androidx.compose.foundation.lazy.LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
                                ) {
                                    items(colorSwatches) { hex ->
                                        val swatchColor = try {
                                            Color(android.graphics.Color.parseColor("#$hex"))
                                        } catch (e: Exception) {
                                            themeColor
                                        }
                                        val isSelected = customAvatarBgColor.equals(hex, ignoreCase = true)
                                        Surface(
                                            onClick = {
                                                viewModel.setCustomAvatarBgColor(hex)
                                                viewModel.triggerAudioFeedback("click")
                                            },
                                            shape = CircleShape,
                                            color = swatchColor,
                                            border = if (isSelected) BorderStroke(3.dp, Color.White) else BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                            modifier = Modifier.size(38.dp)
                                        ) {
                                            if (isSelected) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
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
                            val changeNickTitle = when (currentLang) {
                                Language.RU -> "СМЕНИТЬ НИКНЕЙМ"
                                Language.UA -> "ЗМІНИТИ НІКНЕЙМ"
                                Language.KK -> "НИКНЕЙМДІ ӨЗГЕРТУ"
                                Language.DE -> "BENUTZERNAME ÄNDERN"
                                Language.ZH -> "更改昵称"
                                else -> "CHANGE NICKNAME"
                            }
                            val newNickLabel = when (currentLang) {
                                Language.RU -> "Новый никнейм"
                                Language.UA -> "Новий нікнейм"
                                Language.KK -> "Жаңа никнейм"
                                Language.DE -> "Neuer Benutzername"
                                Language.ZH -> "新昵称"
                                else -> "New Nickname"
                            }
                            val saveNickBtn = when (currentLang) {
                                Language.RU -> "Сохранить ник"
                                Language.UA -> "Зберегти нік"
                                Language.KK -> "Никті сақтау"
                                Language.DE -> "Name speichern"
                                Language.ZH -> "保存昵称"
                                else -> "Save Nickname"
                            }
                            Text(
                                text = changeNickTitle,
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
                                label = { Text(newNickLabel) },
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
                                Text(saveNickBtn, fontWeight = FontWeight.Bold)
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
                            val emailCardTitle = if (currentEmail.isEmpty()) {
                                when (currentLang) {
                                    Language.RU -> "ПРИВЯЗАТЬ ПОЧТУ"
                                    Language.UA -> "ПРИВ'ЯЗАТИ ПОШТУ"
                                    Language.KK -> "ПОШТАНЫ БАЙЛАНЫСТЫРУ"
                                    Language.DE -> "E-MAIL VERKNÜPFEN"
                                    Language.ZH -> "绑定邮箱"
                                    else -> "BIND EMAIL"
                                }
                            } else {
                                when (currentLang) {
                                    Language.RU -> "ИЗМЕНИТЬ ПОЧТУ"
                                    Language.UA -> "ЗМІНИТИ ПОШТУ"
                                    Language.KK -> "ПОШТАНЫ ӨЗГЕРТУ"
                                    Language.DE -> "E-MAIL ÄNDERN"
                                    Language.ZH -> "更改邮箱"
                                    else -> "CHANGE EMAIL"
                                }
                            }
                            val emailInputLabel = when (currentLang) {
                                Language.RU -> "Электронная почта"
                                Language.UA -> "Електронна пошта"
                                Language.KK -> "Электрондық пошта"
                                Language.DE -> "E-Mail-Adresse"
                                Language.ZH -> "电子邮箱"
                                else -> "Email Address"
                            }
                            val emailActionBtn = if (currentEmail.isEmpty()) {
                                when (currentLang) {
                                    Language.RU -> "Привязать"
                                    Language.UA -> "Прив'язати"
                                    Language.KK -> "Байланыстыру"
                                    Language.DE -> "Verknüpfen"
                                    Language.ZH -> "绑定"
                                    else -> "Bind Email"
                                }
                            } else {
                                when (currentLang) {
                                    Language.RU -> "Обновить"
                                    Language.UA -> "Оновити"
                                    Language.KK -> "Жаңарту"
                                    Language.DE -> "Aktualisieren"
                                    Language.ZH -> "更新"
                                    else -> "Update Email"
                                }
                            }
                            Text(
                                text = emailCardTitle,
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
                                label = { Text(emailInputLabel) },
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
                                Text(emailActionBtn, fontWeight = FontWeight.Bold)
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
                            val customTagCardTitle = when (currentLang) {
                                Language.RU -> "ТЕГ В ТАБЛИЦЕ РЕКОРДОВ"
                                Language.UA -> "ТЕГ У ТАБЛИЦІ РЕКОРДІВ"
                                Language.KK -> "РЕКОРДТАР КЕСТЕСІНДЕГІ ТЕГ"
                                Language.DE -> "BESTENLISTEN-TAG"
                                Language.ZH -> "排行榜专属标签"
                                else -> "LEADERBOARD CUSTOM TAG"
                            }
                            Text(
                                text = customTagCardTitle,
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
                                val tagLockedMsg = when (currentLang) {
                                    Language.RU -> "Функция заблокирована. Приобретите «Личный Тег» в магазине."
                                    Language.UA -> "Функція заблокована. Придбайте «Особистий Тег» у магазині."
                                    Language.KK -> "Функция бұғатталған. Дүкеннен «Жеке Тег» сатып алыңыз."
                                    Language.DE -> "Funktion gesperrt. Kaufe «Persönlicher Tag» im Shop."
                                    Language.ZH -> "功能已锁定。请在商店中购买「专属标签」。"
                                    else -> "Feature locked. Purchase «Leaderboard Tag» in the store."
                                }
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
                                            text = tagLockedMsg,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            } else {
                                val customTagInputLabel = when (currentLang) {
                                    Language.RU -> "Кастомный тег (макс. 6 симв.)"
                                    Language.UA -> "Кастомний тег (макс. 6 симв.)"
                                    Language.KK -> "Арнайы тег (макс. 6 таңба)"
                                    Language.DE -> "Eigener Tag (max. 6 Zeichen)"
                                    Language.ZH -> "自定义标签（最多6个字符）"
                                    else -> "Custom Tag (max 6 chars)"
                                }
                                val saveTagBtnText = when (currentLang) {
                                    Language.RU -> "Сохранить тег"
                                    Language.UA -> "Зберегти тег"
                                    Language.KK -> "Тегті сақтау"
                                    Language.DE -> "Tag speichern"
                                    Language.ZH -> "保存标签"
                                    else -> "Save Tag"
                                }
                                OutlinedTextField(
                                    value = editTagInput,
                                    onValueChange = { if (it.length <= 6) editTagInput = it },
                                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null) },
                                    label = { Text(customTagInputLabel) },
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
                                    Text(saveTagBtnText, fontWeight = FontWeight.Bold)
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
                val countText = when (currentLang) {
                    Language.RU -> "$purchasedCount из $totalCount"
                    Language.UA -> "$purchasedCount з $totalCount"
                    Language.KK -> "$totalCount ішінен $purchasedCount"
                    Language.DE -> "$purchasedCount von $totalCount"
                    Language.ZH -> "$purchasedCount / $totalCount"
                    else -> "$purchasedCount of $totalCount"
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest
                ) {
                    Text(
                        text = countText,
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
    val rarityText = Translations.getStoreRarity(cost, currentLang)

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

            val isRank = category.equals("Rank", ignoreCase = true) || 
                         category.equals("Ранг", ignoreCase = true) ||
                         category.equals("Rang", ignoreCase = true) ||
                         category.equals("段位", ignoreCase = true)
            if (!(isRank && isOwned)) {
                val buttonEnabled = when {
                    isRank -> !isOwned
                    else -> !isLocked
                }
                if (isOwned || isActive) {
                    val onOffText = if (isActive) {
                        when (currentLang) {
                            Language.RU -> "ВКЛ"
                            Language.UA -> "УВІМК"
                            Language.KK -> "ҚОС"
                            Language.DE -> "EIN"
                            Language.ZH -> "已开启"
                            else -> "ON"
                        }
                    } else {
                        when (currentLang) {
                            Language.RU -> "ВЫКЛ"
                            Language.UA -> "ВИМК"
                            Language.KK -> "ӨШІР"
                            Language.DE -> "AUS"
                            Language.ZH -> "已关闭"
                            else -> "OFF"
                        }
                    }
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
                            text = onOffText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                } else {
                    val lockBtnText = when (currentLang) {
                        Language.RU -> "БЛОК"
                        Language.UA -> "БЛОК"
                        Language.KK -> "ҚҰЛЫП"
                        Language.DE -> "GESPERRT"
                        Language.ZH -> "未解锁"
                        else -> "LOCKED"
                    }
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
                            text = if (isLocked) lockBtnText else "$cost 🪙",
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
            "ALL" to when (currentLang) {
                Language.RU -> "Все"
                Language.UA -> "Всі"
                Language.KK -> "Барлығы"
                Language.DE -> "Alle"
                Language.ZH -> "全部"
                else -> "All"
            },
            "UNLOCKED" to when (currentLang) {
                Language.RU -> "Открытые"
                Language.UA -> "Відкриті"
                Language.KK -> "Ашылғандар"
                Language.DE -> "Freigeschaltet"
                Language.ZH -> "已解锁"
                else -> "Unlocked"
            },
            "LOCKED" to when (currentLang) {
                Language.RU -> "В процессе"
                Language.UA -> "В процесі"
                Language.KK -> "Орындалуда"
                Language.DE -> "In Bearbeitung"
                Language.ZH -> "进行中"
                else -> "In Progress"
            }
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
            val hallOfFameTitle = when (currentLang) {
                Language.RU -> "ЗАЛ СЛАВЫ"
                Language.UA -> "ЗАЛА СЛАВИ"
                Language.KK -> "ДАҢҚ ЗАЛЫ"
                Language.DE -> "RUHMESHALLE"
                Language.ZH -> "荣誉殿堂"
                else -> "HALL OF FAME"
            }
            val unlockedProgressText = when (currentLang) {
                Language.RU -> "Разблокировано наград: $unlockedCount из $totalCount (${(progressFactor * 100).toInt()}%)"
                Language.UA -> "Розблоковано нагород: $unlockedCount з $totalCount (${(progressFactor * 100).toInt()}%)"
                Language.KK -> "Ашылған жетістіктер: $totalCount ішінен $unlockedCount (${(progressFactor * 100).toInt()}%)"
                Language.DE -> "Freigeschaltete Erfolge: $unlockedCount von $totalCount (${(progressFactor * 100).toInt()}%)"
                Language.ZH -> "已解锁成就：$unlockedCount / $totalCount (${(progressFactor * 100).toInt()}%)"
                else -> "Unlocked achievements: $unlockedCount of $totalCount (${(progressFactor * 100).toInt()}%)"
            }
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
                        text = hallOfFameTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = unlockedProgressText,
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
                                text = Translations.getLocalizedAchievementTitle(ach.id, currentLang, ach.titleEn, ach.titleRu),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            val rewardLabel = when {
                                ach.crateKeyReward != null && ach.crateKeyCount > 0 && ach.pointsReward > 0 -> {
                                    val keyName = when (ach.crateKeyReward) {
                                        "wooden" -> if (currentLang == Language.RU) "Деревянный Ключ" else "Wooden Key"
                                        "iron" -> if (currentLang == Language.RU) "Железный Ключ" else "Iron Key"
                                        "golden" -> if (currentLang == Language.RU) "Золотой Ключ" else "Golden Key"
                                        "platinum" -> if (currentLang == Language.RU) "Платиновый Ключ" else "Platinum Key"
                                        "legendary" -> if (currentLang == Language.RU) "Легендарный Ключ" else "Legendary Key"
                                        "diamond" -> if (currentLang == Language.RU) "Алмазный Ключ" else "Diamond Key"
                                        "red_crate", "red_crate_lite" -> if (currentLang == Language.RU) "Красный Ключ" else "Red Key"
                                        else -> "Key"
                                    }
                                    "+${ach.pointsReward} 🪙 + 🔑 $keyName"
                                }
                                ach.crateKeyReward != null && ach.crateKeyCount > 0 -> {
                                    val keyName = when (ach.crateKeyReward) {
                                        "wooden" -> if (currentLang == Language.RU) "Деревянный Ключ" else "Wooden Key"
                                        "iron" -> if (currentLang == Language.RU) "Железный Ключ" else "Iron Key"
                                        "golden" -> if (currentLang == Language.RU) "Золотой Ключ" else "Golden Key"
                                        "platinum" -> if (currentLang == Language.RU) "Платиновый Ключ" else "Platinum Key"
                                        "legendary" -> if (currentLang == Language.RU) "Легендарный Ключ" else "Legendary Key"
                                        "diamond" -> if (currentLang == Language.RU) "Алмазный Ключ" else "Diamond Key"
                                        "red_crate", "red_crate_lite" -> if (currentLang == Language.RU) "Красный Ключ" else "Red Key"
                                        else -> "Key"
                                    }
                                    "🔑 $keyName"
                                }
                                ach.pointsReward > 0 -> "+${ach.pointsReward} 🪙"
                                else -> if (currentLang == Language.RU) "⭐ Престиж" else "⭐ Prestige"
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                       else MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Text(
                                    text = rewardLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                                    color = if (ach.isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        val badgeTag = Translations.getLocalizedAchievementBadge(ach.iconType, ach.id, currentLang)
                        
                        Text(
                            text = badgeTag,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )

                        val achDesc = Translations.getLocalizedAchievementDesc(ach.id, currentLang, ach.descriptionEn, ach.descriptionRu)

                        Text(
                            text = achDesc,
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
                val aboutAppTitle = when (currentLang) {
                    Language.RU -> "О ПРИЛОЖЕНИИ"
                    Language.UA -> "ПРО ДОДАТОК"
                    Language.KK -> "ҚОСЫМША ТУРАЛЫ"
                    Language.DE -> "ÜBER DIE APP"
                    Language.ZH -> "关于应用"
                    else -> "ABOUT APP"
                }
                val aboutAppDesc = when (currentLang) {
                    Language.RU -> "FlowTess — это продвинутая версия классической головоломки, сочетающая в себе традиционный игровой процесс и инновационный режим ZETA Arena (Block Blast). В игре доступны кастомизация интерфейса, система скинов, звуковые паки, глобальные рекорды, синхронизация прогресса с облаком и полноценный мультиплеер с чатом."
                    Language.UA -> "FlowTess — це просунута версія класичної головоломки, що поєднує традиційний ігровий процес та інноваційний режим ZETA Arena (Block Blast). У грі доступні кастомізація інтерфейсу, система скінів, звукові паки, глобальні рекорди, синхронізація з хмарою та мультиплеєр з чатом."
                    Language.KK -> "FlowTess — бұл дәстүрлі ойын процесі мен инновациялық ZETA Arena (Block Blast) режимін біріктіретін классикалық басқатырғыштың заманауи нұсқасы. Ойында интерфейсті баптау, мұқабалар жүйесі, дыбыстар жиынтығы, ғаламдық рекордтар, бұлтты синхрондау және чаты бар толық мультиплеер қолжетімді."
                    Language.DE -> "FlowTess ist eine moderne Weiterentwicklung des klassischen Puzzlespiels. Es verbindet traditionelles Gameplay nahtlos mit der innovativen ZETA Arena (Block Blast). Zu den Features gehören Oberflächenanpassung, Skin-Pakete, Soundboards, globale Bestenlisten, Cloud-Synchronisierung und ein Mehrspieler-Modus mit Live-Chat."
                    Language.ZH -> "FlowTess 是经典方块消除游戏的全新进化版。它将传统玩法与创新的 ZETA Arena (Block Blast) 模式完美融合。支持全界面深度自定义、皮肤包、音效包、全球排行榜、云端存档同步以及带实时聊天的多人联机对战。"
                    else -> "FlowTess is an advanced evolution of the classic block puzzle game. It seamlessly blends traditional gameplay with the innovative ZETA Arena (Block Blast). Features include full interface customization, skin packs, unique soundboards, global high scores, secure cloud sync, and a multiplayer match lobby with live chat."
                }
                val versionLabel = when (currentLang) {
                    Language.RU -> "Версия"
                    Language.UA -> "Версія"
                    Language.KK -> "Нұсқа"
                    Language.DE -> "Version"
                    Language.ZH -> "版本"
                    else -> "Version"
                }
                val developerLabel = when (currentLang) {
                    Language.RU -> "Разработчик"
                    Language.UA -> "Розробник"
                    Language.KK -> "Әзірлеуші"
                    Language.DE -> "Entwickler"
                    Language.ZH -> "开发者"
                    else -> "Developer"
                }
                Text(
                    text = aboutAppTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = aboutAppDesc,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = versionLabel,
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
                        text = developerLabel,
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
                val changelogTitle = when (currentLang) {
                    Language.RU -> "СПИСОК ИЗМЕНЕНИЙ"
                    Language.UA -> "СПИСОК ЗМІН"
                    Language.KK -> "ӨЗГЕРІСТЕР ТІЗІМІ"
                    Language.DE -> "ÄNDERUNGSPROTOKOLL"
                    Language.ZH -> "更新日志"
                    else -> "CHANGE LOG"
                }
                Text(
                    text = changelogTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                // Version 2.0 Card Content
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    val v20Header = when (currentLang) {
                        Language.RU -> "Версия 2.0 (Текущая)"
                        Language.UA -> "Версія 2.0 (Поточна)"
                        Language.KK -> "Нұсқа 2.0 (Ағымдағы)"
                        Language.DE -> "Version 2.0 (Aktuell)"
                        Language.ZH -> "版本 2.0 (当前)"
                        else -> "Version 2.0 (Current)"
                    }
                    Text(
                        text = v20Header,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = themeColor
                    )
                    
                    val changes = when (currentLang) {
                        Language.RU -> listOf(
                            "• Добавлена строгая верификация почты через Firebase (защита онлайн-функций)",
                            "• Добавлен переключатель видимости вкладки 'Новое' в настройках (раздел 'Система')",
                            "• Кэширование FirebaseAuth для оптимизации скорости работы",
                            "• Полная реструктуризация кода с добавлением двуязычных неформальных комментариев",
                            "• Балансировка экономики: стоимость открытия кейсов увеличена на 25%, награды скорректированы",
                            "• Переименованы научно-фантастические и ИИ-достижения в строгие классические названия",
                            "• Ужесточена проверка проигрыша (строго по ряду 2 или выше)",
                            "• Немедленное удаление сессии при поражении для предотвращения дюпа монет"
                        )
                        Language.UA -> listOf(
                            "• Додана сувора верифікація пошти через Firebase (захист онлайн-функцій)",
                            "• Додано перемикач видимості вкладки 'Нове' в налаштуваннях (розділ 'Система')",
                            "• Кешування FirebaseAuth для оптимізації швидкодії",
                            "• Повна реструктуризація коду та оптимізація продуктивності",
                            "• Балансування економіки: вартість кейсів та нагороди скориговано",
                            "• Оновлено назви та опис досягнень на класичні",
                            "• Посилено перевірку завершення гри",
                            "• Негайне видалення збереженої гри при поразці"
                        )
                        Language.KK -> listOf(
                            "• Firebase арқылы қатаң пошта растауы қосылды (онлайн-функцияларды қорғау)",
                            "• Баптауларда 'Жаңа' қойындысының көріну қосқышы қосылды",
                            "• Жұмыс жылдамдығын оңтайландыру үшін FirebaseAuth кэштеу",
                            "• Кодты толық қайта құрылымдау және оңтайландыру",
                            "• Экономика балансы: кейстер құны мен сыйақылар түзетілді",
                            "• Жетістіктердің классикалық атаулары жаңартылды",
                            "• Ойынның аяқталуын тексеру күшейтілді",
                            "• Жеңіліс кезінде сақталған ойынды бірден өшіру"
                        )
                        Language.DE -> listOf(
                            "• Strikte Firebase-E-Mail-Verifizierung hinzugefügt (schützt Online-Funktionen)",
                            "• Umschalter für die Sichtbarkeit des 'Neu'-Reiters in den Einstellungen hinzugefügt",
                            "• Globale FirebaseAuth-Instanz-Optimierung",
                            "• Vollständiges Codebase-Refactoring zur Performanceverbesserung",
                            "• Wirtschafts-Balancing: Kistenpreise und Belohnungen angepasst",
                            "• Klassische Namen für alle Errungenschaften",
                            "• Strengere Game-Over-Erkennung",
                            "• Sofortiges Löschen von Spielständen bei Niederlage"
                        )
                        Language.ZH -> listOf(
                            "• 新增 Firebase 严格邮箱验证系统（保障在线对战及联机功能安全）",
                            "• 设置界面新增「新特性」选项卡显隐开关",
                            "• 优化 FirebaseAuth 全局缓存，提升加载速度",
                            "• 代码结构全面重构与性能深度调优",
                            "• 经济平衡调整：优化宝箱开启成本与奖励倍率",
                            "• 重构成就系统名称与图标，风格更经典专业",
                            "• 优化游戏结束检测判定",
                            "• 失败时立即销毁临时进度以防止金币漏洞"
                        )
                        else -> listOf(
                            "• Added strict Firebase Email Verification system (verifying email gates online play and chat)",
                            "• Added toggle switch for the 'New' tab visibility in settings (System tab)",
                            "• Cached FirebaseAuth instance globally to eliminate redundant service queries",
                            "• Full codebase review with helpful, informal RU/EN comments",
                            "• Economy balancing: increased crate costs by 25% and adjusted reward rates",
                            "• Rebranded space/AI achievements into professional, classic names",
                            "• Implemented strict game over rules (immediately triggers when block reaches row 2)",
                            "• Added immediate saved game destruction on defeat to fix coin exploits"
                        )
                    }

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
                    val v10Header = when (currentLang) {
                        Language.RU -> "Версия 1.0"
                        Language.UA -> "Версія 1.0"
                        Language.KK -> "Нұсқа 1.0"
                        Language.DE -> "Version 1.0"
                        Language.ZH -> "版本 1.0"
                        else -> "Version 1.0"
                    }
                    val v10Desc = when (currentLang) {
                        Language.RU -> "• Первый релиз: режимы FlowTess и ZETA, лобби мультиплеера, аватары, рамки, теги и профиль."
                        Language.UA -> "• Перший реліз: режими FlowTess і ZETA, лобі мультиплеєра, аватари, рамки, теги та профіль."
                        Language.KK -> "• Алғашқы шығарылым: FlowTess және ZETA режимдері, мультиплеер лоббиі, аватарлар, жақтаулар, тегтер және профиль."
                        Language.DE -> "• Erstveröffentlichung: FlowTess- & ZETA-Modi, Mehrspieler-Lobby, Avatare, Rahmen, Tags und Profil."
                        Language.ZH -> "• 初始版本发布：FlowTess 与 ZETA 双模式、多人对战大厅、个性头像、相框商店、排行榜标签与个人资料。"
                        else -> "• Initial launch: FlowTess & ZETA gameplay modes, multiplayer matchmaking lobbies, custom avatars, frame store, and tags."
                    }
                    Text(
                        text = v10Header,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Text(
                        text = v10Desc,
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

