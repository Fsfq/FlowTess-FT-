package com.example.ui

import androidx.activity.compose.BackHandler

import androidx.compose.animation.*
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Leaderboard
import java.util.Locale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.VideogameAsset
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import com.example.db.FriendUser
import com.example.db.PublicUserProfile
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.MainViewModel
import com.example.db.HighScore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.TileMode


@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier.size(20.dp)) {
    androidx.compose.foundation.Image(
        painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_google),
        contentDescription = "Google Logo",
        modifier = modifier
    )
}

@Composable
fun PlayerAvatarView(
    playerName: String,
    avatarEmoji: String = "",
    avatarBgColorHex: String = "",
    avatarFrame: String = "standard",
    customBitmap: androidx.compose.ui.graphics.ImageBitmap? = null,
    avatarBase64: String? = null,
    size: androidx.compose.ui.unit.Dp = 40.dp,
    themeColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.secondary,
    showOnlineDot: Boolean = false,
    isOnline: Boolean = true,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val infiniteTransition = rememberInfiniteTransition(label = "AvatarFrameAnim")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "frameRotation"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "framePulse"
    )

    val parsedBgColor = remember(avatarBgColorHex, playerName) {
        parsePlayerColor(avatarBgColorHex, playerName, themeColor)
    }

    val hasAnimatedFrame = avatarFrame != "standard" && avatarFrame != "none" && avatarFrame.isNotBlank()

    val frameBrush = remember(parsedBgColor, secondaryColor) {
        val isMetallic = (parsedBgColor.red < 0.22f && parsedBgColor.green < 0.22f && parsedBgColor.blue < 0.22f) ||
                (parsedBgColor.red > 0.80f && parsedBgColor.green > 0.80f && parsedBgColor.blue > 0.80f)

        if (isMetallic) {
            androidx.compose.ui.graphics.Brush.sweepGradient(
                listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFA0A5B5),
                    Color(0xFFE8EDF8),
                    Color(0xFF656A7A),
                    Color(0xFFFFFFFF)
                )
            )
        } else {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(
                android.graphics.Color.argb(255, (parsedBgColor.red * 255).toInt(), (parsedBgColor.green * 255).toInt(), (parsedBgColor.blue * 255).toInt()),
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

            androidx.compose.ui.graphics.Brush.sweepGradient(listOf(c1, c2, c3, c4, c5))
        }
    }

    val resolvedBitmap: androidx.compose.ui.graphics.ImageBitmap? = remember(customBitmap, avatarBase64, playerName) {
        if (customBitmap != null) {
            customBitmap
        } else if (!avatarBase64.isNullOrBlank()) {
            try {
                val clean = avatarBase64.substringAfter("base64,")
                val bytes = android.util.Base64.decode(clean, android.util.Base64.NO_WRAP)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else if (playerName.isNotBlank()) {
            try {
                val file = java.io.File(context.filesDir, "custom_avatar_${playerName}.jpg")
                if (file.exists() && file.length() > 0) {
                    android.graphics.BitmapFactory.decodeFile(file.absolutePath)?.asImageBitmap()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .aspectRatio(1f, matchHeightConstraintsFirst = true)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        // Outer Glow
        if (hasAnimatedFrame) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.92f)
                    .aspectRatio(1f)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = 0.35f
                    }
                    .clip(CircleShape)
                    .background(frameBrush)
            )
        }

        // Rotating Frame Ring
        if (hasAnimatedFrame) {
            Box(
                modifier = Modifier
                    .fillMaxSize(0.98f)
                    .aspectRatio(1f)
                    .graphicsLayer {
                        rotationZ = rotationAngle
                    }
                    .border(
                        width = if (size > 60.dp) 2.5.dp else 1.5.dp,
                        brush = frameBrush,
                        shape = CircleShape
                    )
            )
        }

        // Circular Avatar Content
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize(if (hasAnimatedFrame) 0.84f else 1f)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(
                    androidx.compose.ui.graphics.Brush.linearGradient(
                        colors = listOf(parsedBgColor, parsedBgColor.copy(alpha = 0.75f))
                    )
                )
                .border(
                    width = 1.dp,
                    color = if (hasAnimatedFrame) Color.Transparent else themeColor.copy(alpha = 0.35f),
                    shape = CircleShape
                )
        ) {
            if (resolvedBitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = resolvedBitmap,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else if (avatarEmoji.isNotBlank()) {
                Text(
                    text = avatarEmoji,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (size.value * 0.45f).sp
                    ),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else {
                val initial = if (playerName.isNotBlank()) playerName.take(1).uppercase() else "?"
                Text(
                    text = initial,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (size.value * 0.45f).sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
                    ),
                    color = Color.White,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        // Online Status Dot
        if (showOnlineDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(if (size > 50.dp) 12.dp else 9.dp)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(if (isOnline) Color(0xFF00E676) else Color(0xFFFF9100))
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

private data class ProfileStatItem(
    val label: String,
    val value: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtherUserProfileDialog(
    profile: PublicUserProfile,
    currentLang: Language,
    themeColor: Color,
    isFriend: Boolean = false,
    isSelf: Boolean = false,
    onDismiss: () -> Unit,
    onAddFriend: () -> Unit,
    onInviteToDuel: () -> Unit
) {
    var friendAddedState by remember { mutableStateOf(false) }

    // Decode player's custom background image if present
    val bgBitmap = remember(profile.customBackgroundBase64) {
        if (profile.customBackgroundBase64.isNotBlank()) {
            try {
                val bytes = android.util.Base64.decode(profile.customBackgroundBase64, android.util.Base64.DEFAULT)
                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    val playerAccentColor = parseHexColor(profile.avatarBgColor, themeColor)

    val tierColor = when (profile.onlineTier.ifBlank { "BRONZE" }.uppercase()) {
        "BRONZE" -> Color(0xFFCD7F32)
        "SILVER" -> Color(0xFFC0C0C0)
        "GOLD" -> Color(0xFFFFD700)
        "PLATINUM" -> Color(0xFF00E5FF)
        "DIAMOND" -> Color(0xFF7C4DFF)
        "MASTER" -> Color(0xFFFF1744)
        "GRANDMASTER" -> Color(0xFFFF5252)
        "LEGEND" -> Color(0xFFFF9100)
        else -> themeColor
    }

    BackHandler(onBack = onDismiss)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                topBar = {
                    CenterAlignedTopAppBar(
                        title = {
                            val profileTitle = when (currentLang) {
                                Language.RU -> "ПРОФИЛЬ"
                                Language.UA -> "ПРОФІЛЬ"
                                Language.KK -> "ПРОФИЛЬ"
                                Language.DE -> "PROFIL"
                                Language.ZH -> "个人资料"
                                else -> "PROFILE"
                            }
                            Text(
                                text = profileTitle,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.5.sp
                                )
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close"
                                )
                            }
                        },
                        actions = {
                            // Online / Offline Status Badge
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (profile.isOnline) Color(0xFF00E676).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, if (profile.isOnline) Color(0xFF00E676).copy(alpha = 0.5f) else Color.Gray.copy(alpha = 0.35f)),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .background(if (profile.isOnline) Color(0xFF00E676) else Color.Gray, CircleShape)
                                    )
                                    Text(
                                        text = if (profile.isOnline) {
                                            when (currentLang) {
                                                Language.RU -> "В СЕТИ"
                                                Language.UA -> "В МЕРЕЖІ"
                                                Language.KK -> "ЖЕЛІДЕ"
                                                Language.DE -> "ONLINE"
                                                Language.ZH -> "在线"
                                                else -> "ONLINE"
                                            }
                                        } else {
                                            when (currentLang) {
                                                Language.RU -> "ОФЛАЙН"
                                                Language.UA -> "ОФЛАЙН"
                                                Language.KK -> "ОФЛАЙН"
                                                Language.DE -> "OFFLINE"
                                                Language.ZH -> "离线"
                                                else -> "OFFLINE"
                                            }
                                        },
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black, fontSize = 9.sp),
                                        color = if (profile.isOnline) Color(0xFF00E676) else Color.LightGray
                                    )
                                }
                            }
                        },
                        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background
                        )
                    )
                },
                bottomBar = {
                    val configuration = LocalConfiguration.current
                    val isCompactHeight = configuration.screenHeightDp < 680
                    val isNarrowWidth = configuration.screenWidthDp < 360

                    val btnHeight = if (isCompactHeight) 40.dp else 46.dp
                    val btnCorner = if (isCompactHeight) 12.dp else 16.dp
                    val btnPaddingV = if (isCompactHeight) 6.dp else 10.dp
                    val btnPaddingH = if (isNarrowWidth) 10.dp else 16.dp
                    val fontSize = if (isNarrowWidth || isCompactHeight) 11.sp else 12.sp
                    val iconSize = if (isCompactHeight) 16.dp else 18.dp

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        shadowElevation = 8.dp
                    ) {
                        val requestSentText = when (currentLang) {
                            Language.RU -> "Запрос отправлен"
                            Language.UA -> "Запит надіслано"
                            Language.KK -> "Сұраныс жіберілді"
                            Language.DE -> "Anfrage gesendet"
                            Language.ZH -> "已发送申请"
                            else -> "Request Sent"
                        }
                        val addFriendText = when (currentLang) {
                            Language.RU -> "В друзья"
                            Language.UA -> "У друзі"
                            Language.KK -> "Дос қосу"
                            Language.DE -> "Freund hinzufügen"
                            Language.ZH -> "加为好友"
                            else -> "Add Friend"
                        }
                        val alreadyFriendsText = when (currentLang) {
                            Language.RU -> "В друзьях"
                            Language.UA -> "У друзях"
                            Language.KK -> "Достарда"
                            Language.DE -> "Befreundet"
                            Language.ZH -> "已是好友"
                            else -> "Friends"
                        }
                        val duelBtnText = when (currentLang) {
                            Language.RU -> "Вызвать на дуэль"
                            Language.UA -> "Викликати на дуель"
                            Language.KK -> "Дуэльге шақыру"
                            Language.DE -> "1v1 Duell"
                            Language.ZH -> "发起对战"
                            else -> "1v1 Duel"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = btnPaddingH, vertical = btnPaddingV)
                        ) {
                            if (isSelf) {
                                Button(
                                    onClick = onDismiss,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(btnHeight),
                                    shape = RoundedCornerShape(btnCorner),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = themeColor,
                                        contentColor = if (themeColor.red * 0.299f + themeColor.green * 0.587f + themeColor.blue * 0.114f > 0.6f) Color(0xFF111111) else Color.White
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    val selfProfileText = when (currentLang) {
                                        Language.RU -> "Ваш профиль (Закрыть)"
                                        Language.UA -> "Ваш профіль (Закрити)"
                                        Language.KK -> "Сіздің профиліңіз (Жабу)"
                                        Language.DE -> "Dein Profil (Schließen)"
                                        Language.ZH -> "你的资料 (关闭)"
                                        else -> "Your Profile (Close)"
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        modifier = Modifier.size(iconSize)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = selfProfileText,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = fontSize
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val isActionDisabled = isFriend || friendAddedState
                                    val friendText = when {
                                        isFriend -> alreadyFriendsText
                                        friendAddedState -> requestSentText
                                        else -> addFriendText
                                    }
                                    val friendIcon = if (isActionDisabled) Icons.Default.Check else Icons.Default.PersonAdd

                                    Button(
                                        onClick = {
                                            friendAddedState = true
                                            onAddFriend()
                                        },
                                        enabled = !isActionDisabled,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(btnHeight),
                                        shape = RoundedCornerShape(btnCorner),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeColor,
                                            contentColor = if (themeColor.red * 0.299f + themeColor.green * 0.587f + themeColor.blue * 0.114f > 0.6f) Color(0xFF111111) else Color.White,
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = friendIcon,
                                                contentDescription = null,
                                                modifier = Modifier.size(iconSize)
                                            )
                                            Text(
                                                text = friendText,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = fontSize
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    FilledTonalButton(
                                        onClick = onInviteToDuel,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(btnHeight),
                                        shape = RoundedCornerShape(btnCorner),
                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                        border = BorderStroke(1.dp, Color(0xFFFF5722).copy(alpha = 0.35f)),
                                        colors = ButtonDefaults.filledTonalButtonColors(
                                            containerColor = Color(0xFFFF5722).copy(alpha = 0.18f),
                                            contentColor = Color(0xFFFF7043)
                                        )
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.SportsEsports,
                                                contentDescription = null,
                                                modifier = Modifier.size(iconSize)
                                            )
                                            Text(
                                                text = duelBtnText,
                                                style = MaterialTheme.typography.labelMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = fontSize
                                                ),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // HERO CARD (Matches Player Profile background styling)
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            if (bgBitmap != null) {
                                Image(
                                    bitmap = bgBitmap,
                                    contentDescription = null,
                                    modifier = Modifier.matchParentSize(),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                    alpha = 0.55f
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.40f),
                                                    Color.Black.copy(alpha = 0.75f)
                                                )
                                            )
                                        )
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PlayerAvatarView(
                                    playerName = profile.username,
                                    avatarEmoji = profile.avatarEmoji,
                                    avatarBgColorHex = profile.avatarBgColor,
                                    avatarFrame = profile.avatarFrame,
                                    avatarBase64 = profile.avatarBase64,
                                    size = 84.dp,
                                    themeColor = playerAccentColor,
                                    showOnlineDot = true,
                                    isOnline = profile.isOnline
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                // Player Nickname & Tag
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (profile.customTag.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = playerAccentColor.copy(alpha = 0.2f),
                                            border = BorderStroke(1.dp, playerAccentColor.copy(alpha = 0.5f)),
                                            modifier = Modifier.padding(end = 6.dp)
                                        ) {
                                            Text(
                                                text = "[${profile.customTag}]",
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
                                                color = playerAccentColor,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    val nicknameBrush = rememberAnimatedNicknameBrush(baseColor = playerAccentColor)
                                    Text(
                                        text = profile.username.ifBlank { "Player" },
                                        style = if (profile.hasGradient) {
                                            MaterialTheme.typography.titleLarge.copy(
                                                brush = nicknameBrush,
                                                fontWeight = FontWeight.Black
                                            )
                                        } else {
                                            MaterialTheme.typography.titleLarge.copy(
                                                color = MaterialTheme.colorScheme.onSurface,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    )
                                }

                                // Online Tier & Rating Badge
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = tierColor.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, tierColor.copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocalFireDepartment,
                                            contentDescription = null,
                                            tint = tierColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "${Translations.getLocalizedRank(profile.onlineTier.ifBlank { "BRONZE" }, currentLang).uppercase()} • ${profile.rating} ELO",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                            color = tierColor
                                        )
                                    }
                                }

                                if (profile.title.isNotBlank() && profile.title != "none") {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = Translations.getLocalizedTitle(profile.title, currentLang).uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.5.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // COMPREHENSIVE STATS GRID (2x3) with Vector Icons (No Text Emojis)
                    val statsData = listOf(
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Рекорд"
                                Language.UA -> "Рекорд"
                                Language.KK -> "Рекорд"
                                Language.DE -> "Rekord"
                                Language.ZH -> "最高分"
                                else -> "High Score"
                            },
                            String.format(Locale.getDefault(), "%,d", profile.highScore),
                            playerAccentColor,
                            Icons.Default.EmojiEvents
                        ),
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Линии"
                                Language.UA -> "Лінії"
                                Language.KK -> "Сызықтар"
                                Language.DE -> "Linien"
                                Language.ZH -> "消除行数"
                                else -> "Lines"
                            },
                            String.format(Locale.getDefault(), "%,d", profile.clearedLines),
                            Color(0xFF00E5FF),
                            Icons.Default.FlashOn
                        ),
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Матчи"
                                Language.UA -> "Матчі"
                                Language.KK -> "Матчтар"
                                Language.DE -> "Spiele"
                                Language.ZH -> "对战场次"
                                else -> "Matches"
                            },
                            "${profile.gamesPlayed}",
                            Color(0xFFB388FF),
                            Icons.Default.SportsEsports
                        ),
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Тетрисы"
                                Language.UA -> "Тетріси"
                                Language.KK -> "Тетристер"
                                Language.DE -> "Tetrise"
                                Language.ZH -> "四行全消"
                                else -> "Tetrises"
                            },
                            "${profile.tetrisesCount}",
                            Color(0xFFFF4081),
                            Icons.Default.CheckCircle
                        ),
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Уровень"
                                Language.UA -> "Рівень"
                                Language.KK -> "Деңгей"
                                Language.DE -> "Level"
                                Language.ZH -> "等级"
                                else -> "Level"
                            },
                            "LVL ${profile.userLevel}",
                            Color(0xFFFFD700),
                            Icons.Default.Star
                        ),
                        ProfileStatItem(
                            when (currentLang) {
                                Language.RU -> "Баланс"
                                Language.UA -> "Баланс"
                                Language.KK -> "Баланс"
                                Language.DE -> "Münzen"
                                Language.ZH -> "金币"
                                else -> "Coins"
                            },
                            String.format(Locale.getDefault(), "%,d", profile.credits),
                            Color(0xFFFFD700),
                            Icons.Default.MonetizationOn
                        )
                    )

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        statsData.chunked(2).forEach { rowPair ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for ((label, value, color, icon) in rowPair) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(16.dp),
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f),
                                        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Surface(
                                                modifier = Modifier.size(32.dp),
                                                shape = RoundedCornerShape(10.dp),
                                                color = color.copy(alpha = 0.15f)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = null,
                                                        tint = color,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                            Column {
                                                Text(
                                                    text = label.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = value,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black, fontSize = 15.sp),
                                                    color = color
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ACHIEVEMENTS SUMMARY CARD (Only Achievements displayed)
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.70f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(44.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.35f))
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.WorkspacePremium,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "ДОСТИЖЕНИЯ"
                                        Language.UA -> "ДОСЯГНЕННЯ"
                                        Language.KK -> "ЖЕТІСТІКТЕР"
                                        Language.DE -> "ERFOLGE"
                                        Language.ZH -> "成就荣誉"
                                        else -> "ACHIEVEMENTS"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = when (currentLang) {
                                        Language.RU -> "Разблокировано: ${profile.unlockedAchievements.size}"
                                        Language.UA -> "Розблоковано: ${profile.unlockedAchievements.size}"
                                        Language.KK -> "Ашылғандар: ${profile.unlockedAchievements.size}"
                                        Language.DE -> "Freigeschaltet: ${profile.unlockedAchievements.size}"
                                        Language.ZH -> "已解锁：${profile.unlockedAchievements.size}"
                                        else -> "Unlocked: ${profile.unlockedAchievements.size}"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsDialog(
    viewModel: MainViewModel,
    currentLang: Language,
    themeColor: Color,
    onDismiss: () -> Unit,
    onOpenLobby: () -> Unit
) {
    val friendsList by viewModel.friendsList.collectAsStateWithLifecycle()
    val friendRequests by viewModel.friendRequests.collectAsStateWithLifecycle()
    val haptic = LocalHapticFeedback.current

    var activeTab by remember { mutableIntStateOf(0) } // 0: Friends, 1: Requests, 2: Search
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FriendUser>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var actionToast by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(actionToast) {
        if (actionToast != null) {
            delay(2000)
            actionToast = null
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top App Bar
                val friendsDialogTitle = when (currentLang) {
                    Language.RU -> "ДРУЗЬЯ И СОЮЗНИКИ"
                    Language.UA -> "ДРУЗІ ТА СОЮЗНИКИ"
                    Language.KK -> "ДОСТАР ЖӘНЕ ОДАҚТАСТАР"
                    Language.DE -> "FREUNDE & VERBÜNDETE"
                    Language.ZH -> "好友与盟友"
                    else -> "FRIENDS & ALLIES"
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Text(
                        text = friendsDialogTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Box(modifier = Modifier.size(48.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Sliding Pill Tabs
                val tabFriendsLabel = when (currentLang) {
                    Language.RU -> "Друзья"
                    Language.UA -> "Друзі"
                    Language.KK -> "Достар"
                    Language.DE -> "Freunde"
                    Language.ZH -> "好友"
                    else -> "Friends"
                }
                val tabRequestsLabel = when (currentLang) {
                    Language.RU -> "Запросы"
                    Language.UA -> "Запити"
                    Language.KK -> "Сұраныстар"
                    Language.DE -> "Anfragen"
                    Language.ZH -> "好友申请"
                    else -> "Requests"
                }
                val tabSearchLabel = when (currentLang) {
                    Language.RU -> "Поиск"
                    Language.UA -> "Пошук"
                    Language.KK -> "Іздеу"
                    Language.DE -> "Suche"
                    Language.ZH -> "搜索"
                    else -> "Search"
                }
                val tabs = listOf(
                    Triple(0, "$tabFriendsLabel (${friendsList.size})", Icons.Default.People),
                    Triple(1, "$tabRequestsLabel (${friendRequests.size})", Icons.Default.Mail),
                    Triple(2, tabSearchLabel, Icons.Default.Search)
                )

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                ) {
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp)
                    ) {
                        val tabWidth = maxWidth / tabs.size
                        val indicatorOffset by animateDpAsState(
                            targetValue = tabWidth * activeTab,
                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                            label = "friendsTabIndicator"
                        )

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
                            tabs.forEach { (index, title, icon) ->
                                val isSelected = activeTab == index
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            activeTab = index
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = title,
                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Toast notification
                actionToast?.let { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Tab Contents with Directional Transitions
                AnimatedContent(
                    targetState = activeTab,
                    transitionSpec = {
                        val direction = if (targetState > initialState) 1 else -1
                        (slideInHorizontally(
                            initialOffsetX = { fullWidth -> (fullWidth * 0.35f * direction).toInt() },
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                        ) + fadeIn(tween(220))).togetherWith(
                            slideOutHorizontally(
                                targetOffsetX = { fullWidth -> (fullWidth * 0.35f * -direction).toInt() },
                                animationSpec = spring(dampingRatio = 0.85f, stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(tween(180))
                        )
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    label = "FriendsTabContent"
                ) { tab ->
                    when (tab) {
                        0 -> {
                            // My Friends List
                            if (friendsList.isEmpty()) {
                                val emptyFriendsText = when (currentLang) {
                                    Language.RU -> "У вас пока нет добавленных друзей"
                                    Language.UA -> "У вас ще немає доданих друзів"
                                    Language.KK -> "Сізде әзірше достар жоқ"
                                    Language.DE -> "Noch keine Freunde hinzugefügt"
                                    Language.ZH -> "暂无好友"
                                    else -> "No friends added yet"
                                }
                                val findPlayersBtnText = when (currentLang) {
                                    Language.RU -> "Найти игроков"
                                    Language.UA -> "Знайти гравців"
                                    Language.KK -> "Ойыншыларды табу"
                                    Language.DE -> "Spieler suchen"
                                    Language.ZH -> "寻找玩家"
                                    else -> "Find Players"
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.People,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(54.dp)
                                        )
                                        Text(
                                            text = emptyFriendsText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        FilledTonalButton(
                                            onClick = { activeTab = 2 },
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text(findPlayersBtnText)
                                        }
                                    }
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(friendsList) { friend ->
                                        ElevatedCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.elevatedCardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .clickable {
                                                            viewModel.openUserProfile(friend.uid, friend.username)
                                                        }
                                                ) {
                                                    PlayerAvatarView(
                                                        playerName = friend.username,
                                                        avatarEmoji = friend.avatarEmoji,
                                                        avatarBgColorHex = friend.avatarBgColor,
                                                        avatarFrame = friend.avatarFrame,
                                                        avatarBase64 = friend.avatarBase64,
                                                        showOnlineDot = true,
                                                        isOnline = friend.isOnline,
                                                        size = 46.dp,
                                                        themeColor = themeColor
                                                    )

                                                    Column {
                                                        val friendColor = parseHexColor(friend.avatarBgColor, themeColor)
                                                        val nicknameBrush = rememberAnimatedNicknameBrush(baseColor = friendColor)
                                                        Text(
                                                            text = friend.username,
                                                            style = if (friend.hasGradient) {
                                                                MaterialTheme.typography.bodyLarge.copy(
                                                                    brush = nicknameBrush,
                                                                    fontWeight = FontWeight.ExtraBold
                                                                )
                                                            } else {
                                                                MaterialTheme.typography.bodyLarge.copy(
                                                                    color = MaterialTheme.colorScheme.onSurface,
                                                                    fontWeight = FontWeight.ExtraBold
                                                                )
                                                            },
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                        val tierWord = when (currentLang) {
                                                            Language.RU -> "Лига"
                                                            Language.UA -> "Ліга"
                                                            Language.KK -> "Лига"
                                                            Language.DE -> "Liga"
                                                            Language.ZH -> "段位"
                                                            else -> "Tier"
                                                        }
                                                        Text(
                                                            text = "${String.format(Locale.getDefault(), "%,d", friend.credits)} монет • $tierWord: ${Translations.getLocalizedRank(friend.onlineTier, currentLang)}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                val currentRoom by viewModel.lobbyManager.currentRoom.collectAsStateWithLifecycle()
                                                val customAvatarEmoji by viewModel.customAvatarEmoji.collectAsStateWithLifecycle()
                                                val customAvatarBgColor by viewModel.customAvatarBgColor.collectAsStateWithLifecycle()
                                                val equippedAvatarFrame by viewModel.equippedAvatarFrame.collectAsStateWithLifecycle()
                                                val localPlayerName by viewModel.playerName.collectAsStateWithLifecycle()
                                                val localTier by viewModel.onlineTier.collectAsStateWithLifecycle()

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    if (friend.isOnline && currentRoom != null && currentRoom!!.status == "waiting") {
                                                        val inviteBtnText = when (currentLang) {
                                                            Language.RU -> "Позвать"
                                                            Language.UA -> "Запросити"
                                                            Language.KK -> "Шақыру"
                                                            Language.DE -> "Einladen"
                                                            Language.ZH -> "邀请"
                                                            else -> "Invite"
                                                        }
                                                        val inviteSentToastText = when (currentLang) {
                                                            Language.RU -> "Приглашение отправлено!"
                                                            Language.UA -> "Запрошення надіслано!"
                                                            Language.KK -> "Шақыру жіберілді!"
                                                            Language.DE -> "Einladung gesendet!"
                                                            Language.ZH -> "邀请已发送！"
                                                            else -> "Invite sent!"
                                                        }
                                                        FilledTonalButton(
                                                            onClick = {
                                                                viewModel.triggerAudioFeedback("click")
                                                                viewModel.lobbyManager.sendRoomInvite(
                                                                    targetUid = friend.uid,
                                                                    roomId = currentRoom!!.roomId,
                                                                    roomName = currentRoom!!.name,
                                                                    hostName = localPlayerName,
                                                                    avatarEmoji = customAvatarEmoji,
                                                                    avatarBgColor = customAvatarBgColor,
                                                                    avatarFrame = equippedAvatarFrame,
                                                                    hostTier = localTier
                                                                )
                                                                actionToast = inviteSentToastText
                                                            },
                                                            shape = RoundedCornerShape(12.dp),
                                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                                        ) {
                                                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(13.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text(inviteBtnText, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                        }
                                                    } else {
                                                        IconButton(
                                                            onClick = {
                                                                onOpenLobby()
                                                            },
                                                            modifier = Modifier.size(36.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.SportsEsports,
                                                                contentDescription = "Duel",
                                                                tint = themeColor,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            viewModel.removeFriend(friend) { ok ->
                                                                if (ok) {
                                                                    actionToast = when (currentLang) {
                                                                        Language.RU -> "Друг удален"
                                                                        Language.UA -> "Друга видалено"
                                                                        Language.KK -> "Дос өшірілді"
                                                                        Language.DE -> "Freund entfernt"
                                                                        Language.ZH -> "已删除好友"
                                                                        else -> "Friend removed"
                                                                    }
                                                                }
                                                            }
                                                        },
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Delete,
                                                            contentDescription = "Remove",
                                                            tint = MaterialTheme.colorScheme.error,
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

                        // Tab 1: Friend Requests
                        1 -> {
                            if (friendRequests.isEmpty()) {
                                val emptyReqText = when (currentLang) {
                                    Language.RU -> "Входящих запросов в друзья нет"
                                    Language.UA -> "Вхідних запитів у друзі немає"
                                    Language.KK -> "Кіріс дос сұраныстары жоқ"
                                    Language.DE -> "Keine eingehenden Freundschaftsanfragen"
                                    Language.ZH -> "暂无好友申请"
                                    else -> "No incoming friend requests"
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emptyReqText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                val wantsToBeFriendsText = when (currentLang) {
                                    Language.RU -> "Хочет добавить вас в друзья"
                                    Language.UA -> "Хоче додати вас у друзі"
                                    Language.KK -> "Сізді дос ретінде қосқысы келеді"
                                    Language.DE -> "Möchte dein Freund sein"
                                    Language.ZH -> "申请添加你为好友"
                                    else -> "Wants to be friends"
                                }
                                val reqAcceptedText = when (currentLang) {
                                    Language.RU -> "Запрос принят!"
                                    Language.UA -> "Запит прийнято!"
                                    Language.KK -> "Сұраныс қабылданды!"
                                    Language.DE -> "Anfrage angenommen!"
                                    Language.ZH -> "已同意申请！"
                                    else -> "Request accepted!"
                                }
                                val reqDeclinedText = when (currentLang) {
                                    Language.RU -> "Запрос отклонен"
                                    Language.UA -> "Запит відхилено"
                                    Language.KK -> "Сұраныс қабылданбады"
                                    Language.DE -> "Anfrage abgelehnt"
                                    Language.ZH -> "已拒绝申请"
                                    else -> "Request declined"
                                }
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(friendRequests) { req ->
                                        ElevatedCard(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            colors = CardDefaults.elevatedCardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    PlayerAvatarView(
                                                        playerName = req.username,
                                                        avatarEmoji = req.avatarEmoji,
                                                        avatarBgColorHex = req.avatarBgColor,
                                                        avatarFrame = req.avatarFrame,
                                                        size = 46.dp,
                                                        themeColor = themeColor
                                                    )

                                                    Column {
                                                        Text(
                                                            text = req.username,
                                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        Text(
                                                            text = wantsToBeFriendsText,
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    FilledIconButton(
                                                        onClick = {
                                                            viewModel.acceptFriendRequest(req) { ok ->
                                                                if (ok) {
                                                                    actionToast = reqAcceptedText
                                                                }
                                                            }
                                                        },
                                                        colors = IconButtonDefaults.filledIconButtonColors(
                                                            containerColor = Color(0xFF00E676),
                                                            contentColor = Color.Black
                                                        ),
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(Icons.Default.Check, contentDescription = "Accept", modifier = Modifier.size(18.dp))
                                                    }

                                                    FilledIconButton(
                                                        onClick = {
                                                            viewModel.declineFriendRequest(req) { ok ->
                                                                if (ok) {
                                                                    actionToast = reqDeclinedText
                                                                }
                                                            }
                                                        },
                                                        colors = IconButtonDefaults.filledIconButtonColors(
                                                            containerColor = MaterialTheme.colorScheme.errorContainer,
                                                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                                                        ),
                                                        modifier = Modifier.size(36.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Decline", modifier = Modifier.size(18.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        2 -> {
                            // Player Search
                            val searchPlaceholder = when (currentLang) {
                                Language.RU -> "Введите никнейм игрока..."
                                Language.UA -> "Введіть нікнейм гравця..."
                                Language.KK -> "Ойыншының никнеймін енгізіңіз..."
                                Language.DE -> "Spielernamen eingeben..."
                                Language.ZH -> "输入玩家昵称搜索..."
                                else -> "Search player by nickname..."
                            }
                            val noPlayersFoundText = when (currentLang) {
                                Language.RU -> "Игроки не найдены"
                                Language.UA -> "Гравців не знайдено"
                                Language.KK -> "Ойыншылар табылмады"
                                Language.DE -> "Keine Spieler gefunden"
                                Language.ZH -> "未找到玩家"
                                else -> "No players found"
                            }
                            val addBtnText = when (currentLang) {
                                Language.RU -> "Добавить"
                                Language.UA -> "Додати"
                                Language.KK -> "Қосу"
                                Language.DE -> "Hinzufügen"
                                Language.ZH -> "添加"
                                else -> "Add"
                            }
                            val tierLabel = when (currentLang) {
                                Language.RU -> "Лига"
                                Language.UA -> "Ліга"
                                Language.KK -> "Лига"
                                Language.DE -> "Liga"
                                Language.ZH -> "段位"
                                else -> "Tier"
                            }
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = {
                                        searchQuery = it
                                        if (it.length >= 2) {
                                            isSearching = true
                                            viewModel.searchPlayers(it) { results ->
                                                searchResults = results
                                                isSearching = false
                                            }
                                        } else {
                                            searchResults = emptyList()
                                        }
                                    },
                                    placeholder = {
                                        Text(searchPlaceholder)
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = null, tint = themeColor)
                                    },
                                    trailingIcon = {
                                        if (searchQuery.isNotEmpty()) {
                                            IconButton(onClick = {
                                                searchQuery = ""
                                                searchResults = emptyList()
                                            }) {
                                                Icon(Icons.Default.Close, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    singleLine = true
                                )

                                if (isSearching) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = themeColor)
                                    }
                                } else if (searchResults.isEmpty() && searchQuery.length >= 2) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = noPlayersFoundText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                } else {
                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(searchResults) { user ->
                                            ElevatedCard(
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(20.dp),
                                                colors = CardDefaults.elevatedCardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        PlayerAvatarView(
                                                            playerName = user.username,
                                                            avatarEmoji = user.avatarEmoji,
                                                            avatarBgColorHex = user.avatarBgColor,
                                                            avatarFrame = user.avatarFrame,
                                                            size = 46.dp,
                                                            themeColor = themeColor
                                                        )

                                                        Column {
                                                            Text(
                                                                text = user.username,
                                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            Text(
                                                                text = "$tierLabel: ${Translations.getLocalizedRank(user.onlineTier, currentLang)}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }
                                                    }

                                                    Button(
                                                        onClick = {
                                                            viewModel.sendFriendRequest(user.username) { ok, msg ->
                                                                actionToast = msg
                                                            }
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = themeColor,
                                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                                        )
                                                    ) {
                                                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(addBtnText, fontSize = 12.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
