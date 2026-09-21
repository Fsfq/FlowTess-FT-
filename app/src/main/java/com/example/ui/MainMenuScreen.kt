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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(
    viewModel: MainViewModel,
    onLeaderboard: () -> Unit,
    onProfile: () -> Unit,
    onModeSelection: () -> Unit,
    onPlayMode: (com.example.game.GameMode) -> Unit,
    onMultiplayer: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val playerName by viewModel.playerName.collectAsStateWithLifecycle()
    val allAccounts by viewModel.allAccounts.collectAsStateWithLifecycle(initialValue = emptyList())
    val isAdminSessionAuthenticated by viewModel.isAdminSessionAuthenticated.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()

    var showAdminPanelDialog by remember { mutableStateOf(false) }
    var showAdminPassDialog by remember { mutableStateOf(false) }
    var adminPassInput by remember { mutableStateOf("") }
    var adminPassError by remember { mutableStateOf(false) }
    val adminPassHashTarget = "c864cdb36ad4b4ea933ebec535a78dd5e427967092b6493941c3dba2eca951b5"
    var showAuthGuardDialog by remember { mutableStateOf(false) }


    val themeColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth >= 600.dp
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
            if (showAuthGuardDialog) {
                AlertDialog(
                    onDismissRequest = { showAuthGuardDialog = false },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shape = RoundedCornerShape(28.dp),
                    icon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    title = {
                        val authReqTitle = when (currentLang) {
                            Language.RU -> "Требуется авторизация"
                            Language.UA -> "Потрібна авторизація"
                            Language.KK -> "Авторизация қажет"
                            Language.DE -> "Anmeldung erforderlich"
                            Language.ZH -> "需要登录账号"
                            else -> "Authentication Required"
                        }
                        Text(
                            text = authReqTitle,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        val authReqBody = when (currentLang) {
                            Language.RU -> "Для игры в сетевом режиме необходимо зарегистрироваться или войти в свой аккаунт. Хотите перейти в профиль?"
                            Language.UA -> "Для гри в мережевому режимі необхідно зареєструватися або увійти до свого акаунта. Бажаєте перейти до профілю?"
                            Language.KK -> "Желілік режимде ойнау үшін тіркелу немесе аккаунтқа кіру қажет. Профильге өткіңіз келе ме?"
                            Language.DE -> "Für den Online-Mehrspielermodus musst du dich anmelden oder ein Konto erstellen. Möchtest du zum Profil wechseln?"
                            Language.ZH -> "进行在线联机对战需要先登录或注册账号。是否前往个人资料页？"
                            else -> "To play online multiplayer, you need to sign in or create an account. Would you like to go to your profile?"
                        }
                        Text(
                            text = authReqBody,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    confirmButton = {
                        val authConfirmText = when (currentLang) {
                            Language.RU -> "ВХОД / РЕГИСТРАЦИЯ"
                            Language.UA -> "ВХІД / РЕЄСТРАЦІЯ"
                            Language.KK -> "КІРУ / ТІРКЕЛУ"
                            Language.DE -> "ANMELDEN / REGISTRIEREN"
                            Language.ZH -> "登录 / 注册"
                            else -> "LOGIN / REGISTER"
                        }
                        Button(
                            onClick = {
                                showAuthGuardDialog = false
                                onProfile()
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(authConfirmText, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { 
                                showAuthGuardDialog = false 
                                viewModel.triggerAudioFeedback("click")
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(Translations.get("cancel", currentLang).uppercase(), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (showAdminPassDialog) {
                val adminGuardTitle = when (currentLang) {
                    Language.RU -> "Защита Админ-Панели"
                    Language.UA -> "Захист Адмін-Панелі"
                    Language.KK -> "Әкімші панелін қорғау"
                    Language.DE -> "Admin-Panel-Schutz"
                    Language.ZH -> "管理控制台安全验证"
                    else -> "Admin Panel Guard"
                }
                val adminGuardDesc = when (currentLang) {
                    Language.RU -> "Введите секретный пароль доступа к консоли администратора:"
                    Language.UA -> "Введіть секретний пароль доступу до консолі адміністратора:"
                    Language.KK -> "Әкімші консоліне кіру үшін құпия сөзді енгізіңіз:"
                    Language.DE -> "Geheimes Passwort für den Zugriff auf die Administratorkonsole eingeben:"
                    Language.ZH -> "请输入访问管理员控制台的安全密码："
                    else -> "Enter secret password to access administrator console:"
                }
                val adminPassLabel = when (currentLang) {
                    Language.RU -> "Пароль доступа"
                    Language.UA -> "Пароль доступу"
                    Language.KK -> "Қолжетімділік құпия сөзі"
                    Language.DE -> "Zugangspasswort"
                    Language.ZH -> "访问密码"
                    else -> "Access Password"
                }
                val adminPassErrorText = when (currentLang) {
                    Language.RU -> "Неверный пароль доступа!"
                    Language.UA -> "Невірний пароль доступу!"
                    Language.KK -> "Құпия сөз қате!"
                    Language.DE -> "Falsches Zugangspasswort!"
                    Language.ZH -> "访问密码错误！"
                    else -> "Incorrect access password!"
                }
                val adminEnterBtn = when (currentLang) {
                    Language.RU -> "ВОЙТИ"
                    Language.UA -> "УВІЙТИ"
                    Language.KK -> "КІРУ"
                    Language.DE -> "EINTRETEN"
                    Language.ZH -> "进入"
                    else -> "ENTER"
                }
                AlertDialog(
                    onDismissRequest = {
                        showAdminPassDialog = false
                        adminPassInput = ""
                        adminPassError = false
                    },
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    tonalElevation = 6.dp,
                    shape = RoundedCornerShape(28.dp),
                    icon = { Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp)) },
                    title = {
                        Text(
                            text = adminGuardTitle,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .imePadding(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = adminGuardDesc,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = adminPassInput,
                                onValueChange = {
                                    adminPassInput = it
                                    adminPassError = false
                                },
                                label = { Text(adminPassLabel) },
                                singleLine = true,
                                isError = adminPassError,
                                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (adminPassError) {
                                Text(
                                    text = adminPassErrorText,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (viewModel.isCurrentUserAdmin() && com.example.db.PasswordHasher.hash(adminPassInput.trim()) == adminPassHashTarget) {
                                    viewModel.setAdminSessionAuthenticated(true)
                                    showAdminPassDialog = false
                                    adminPassInput = ""
                                    adminPassError = false
                                    showAdminPanelDialog = true
                                } else {
                                    adminPassError = true
                                }
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(adminEnterBtn, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showAdminPassDialog = false
                                adminPassInput = ""
                                adminPassError = false
                            },
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(Translations.get("cancel", currentLang).uppercase())
                        }
                    }
                )
            }

            if (showAdminPanelDialog) {
                var editUsername by remember { mutableStateOf(playerName) }
                var editCredits by remember { mutableStateOf("") }
                var editRank by remember { mutableStateOf("BRONZE") }
                var editBonusXp by remember { mutableStateOf("0") }
                var editHasGradient by remember { mutableStateOf(false) }
                var broadcastTitle by remember { mutableStateOf("") }
                var broadcastText by remember { mutableStateOf("") }
                var broadcastSentFeedback by remember { mutableStateOf<String?>(null) }
                var userSearchQuery by remember { mutableStateOf("") }
                var selectedUserFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Online, 2: Verified, 3: Unverified, 4: Banned, 5: Rich
                var selectedAdminTab by remember { mutableIntStateOf(0) }
                val firebaseUsers by viewModel.firebaseUsers.collectAsStateWithLifecycle(initialValue = emptyList())
                var editingAdminUser by remember { mutableStateOf<Map<String, Any>?>(null) }
                var grantingCoinsUser by remember { mutableStateOf<Map<String, Any>?>(null) }
                var grantCoinsCustomAmount by remember { mutableStateOf("") }
                var editUserCredits by remember { mutableStateOf("") }
                var editUserHighScore by remember { mutableStateOf("") }
                var editUserLines by remember { mutableStateOf("") }
                var editUserGames by remember { mutableStateOf("") }
                var editUserXp by remember { mutableStateOf("") }
                var editUserTier by remember { mutableStateOf("BRONZE") }
                var editUserGradient by remember { mutableStateOf(false) }
                var editUserTagUnlocked by remember { mutableStateOf(false) }
                var editUserTag by remember { mutableStateOf("") }
                var editUserEmailVerified by remember { mutableStateOf(false) }
                var showWipeConfirmDialog by remember { mutableStateOf(false) }
                var adminToastMessage by remember { mutableStateOf<String?>(null) }
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

                LaunchedEffect(adminToastMessage) {
                    if (adminToastMessage != null) {
                        delay(2500)
                        adminToastMessage = null
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.fetchFirebaseUsersForAdmin()
                }

                LaunchedEffect(selectedAdminTab) {
                    if (selectedAdminTab == 1 || selectedAdminTab == 3) {
                        viewModel.fetchFirebaseUsersForAdmin()
                    }
                }

                AnimatedVisibility(
                    visible = showAdminPanelDialog,
                    enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { it / 4 }, animationSpec = tween(250)),
                    exit = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { it / 4 }, animationSpec = tween(200))
                ) {
                    BackHandler { showAdminPanelDialog = false }
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Scaffold(
                            containerColor = MaterialTheme.colorScheme.background,
                            topBar = {
                                @OptIn(ExperimentalMaterial3Api::class)
                                CenterAlignedTopAppBar(
                                    modifier = Modifier.statusBarsPadding(),
                                    title = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Shield,
                                                contentDescription = null,
                                                tint = themeColor,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Админ-панель FsFq"
                                                    Language.UA -> "Адмін-панель FsFq"
                                                    Language.KK -> "FsFq Әкімші панелі"
                                                    Language.DE -> "FsFq Admin-Konsole"
                                                    Language.ZH -> "FsFq 管理控制台"
                                                    else -> "Admin Panel FsFq"
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { showAdminPanelDialog = false }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Close",
                                                tint = MaterialTheme.colorScheme.onBackground
                                            )
                                        }
                                    },
                                    actions = {
                                        IconButton(onClick = {
                                            viewModel.fetchFirebaseUsersForAdmin()
                                            viewModel.triggerAudioFeedback("click")
                                            adminToastMessage = when (currentLang) {
                                                Language.RU -> "Данные обновлены"
                                                Language.UA -> "Дані оновлено"
                                                Language.KK -> "Деректер жаңартылды"
                                                Language.DE -> "Daten aktualisiert"
                                                Language.ZH -> "数据已刷新"
                                                else -> "Data refreshed"
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = "Refresh",
                                                tint = themeColor
                                            )
                                        }
                                        IconButton(onClick = {
                                            viewModel.setAdminSessionAuthenticated(false)
                                            showAdminPanelDialog = false
                                            viewModel.triggerAudioFeedback("gameover")
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Lock",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    },
                                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                        containerColor = MaterialTheme.colorScheme.background
                                    )
                                )
                            }
                        ) { paddingValues ->
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValues)
                                    .consumeWindowInsets(paddingValues)
                                    .imePadding()
                            ) {
                                // Server Stats Summary Bar
                                val totalUsers = firebaseUsers.size
                                val onlineCount = firebaseUsers.count { (it["is_online"] as? Boolean) == true }
                                val verifiedCount = firebaseUsers.count { 
                                    (it["email_verified"] as? Boolean) == true || 
                                    (it["is_verified"] as? Boolean) == true || 
                                    (it["emailVerified"] as? Boolean) == true 
                                }
                                val bannedCount = firebaseUsers.count { (it["is_banned"] as? Boolean) == true }
                                val totalEconomy = firebaseUsers.sumOf { (it["credits"] as? Number)?.toLong() ?: 0L }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainerHigh
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceAround,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$totalUsers",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = themeColor
                                            )
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Игроки"
                                                    Language.UA -> "Гравці"
                                                    Language.KK -> "Ойыншылар"
                                                    Language.DE -> "Spieler"
                                                    Language.ZH -> "玩家总数"
                                                    else -> "Users"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$onlineCount",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF00E676)
                                            )
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Онлайн"
                                                    Language.UA -> "Онлайн"
                                                    Language.KK -> "Желіде"
                                                    Language.DE -> "Online"
                                                    Language.ZH -> "当前在线"
                                                    else -> "Online"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "$verifiedCount",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF40C4FF)
                                            )
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Вериф."
                                                    Language.UA -> "Вериф."
                                                    Language.KK -> "Тексерілді"
                                                    Language.DE -> "Verifiziert"
                                                    Language.ZH -> "已认证"
                                                    else -> "Verified"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = if (totalEconomy >= 1_000_000) String.format(Locale.US, "%.1fM", totalEconomy / 1_000_000.0)
                                                       else if (totalEconomy >= 1_000) String.format(Locale.US, "%.1fk", totalEconomy / 1_000.0)
                                                       else "$totalEconomy",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFFFD54F)
                                            )
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Экономика"
                                                    Language.UA -> "Економіка"
                                                    Language.KK -> "Экономика"
                                                    Language.DE -> "Wirtschaft"
                                                    Language.ZH -> "经济流通"
                                                    else -> "Economy"
                                                },
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        if (bannedCount > 0) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                Text(
                                                    text = "$bannedCount",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.error
                                                )
                                                Text(
                                                    text = when (currentLang) {
                                                        Language.RU -> "Бан"
                                                        Language.UA -> "Бан"
                                                        Language.KK -> "Бұғаттау"
                                                        Language.DE -> "Gesperrt"
                                                        Language.ZH -> "封禁用户"
                                                        else -> "Banned"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                adminToastMessage?.let { msg ->
                                    Surface(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF00E676).copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = msg,
                                            color = Color(0xFF00E676),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp)
                                        )
                                    }
                                }

                                // MD3 Segmented Tab Selector
                                val tabAdminProfile = when (currentLang) {
                                    Language.RU -> "Профиль"
                                    Language.UA -> "Профіль"
                                    Language.KK -> "Профиль"
                                    Language.DE -> "Profil"
                                    Language.ZH -> "管理员"
                                    else -> "Profile"
                                }
                                val tabAdminPlayers = when (currentLang) {
                                    Language.RU -> "Игроки"
                                    Language.UA -> "Гравці"
                                    Language.KK -> "Ойыншылар"
                                    Language.DE -> "Spieler"
                                    Language.ZH -> "玩家列表"
                                    else -> "Players"
                                }
                                val tabAdminBroadcast = when (currentLang) {
                                    Language.RU -> "Оповещения"
                                    Language.UA -> "Оповіщення"
                                    Language.KK -> "Хабарландыру"
                                    Language.DE -> "Broadcast"
                                    Language.ZH -> "全服广播"
                                    else -> "Broadcast"
                                }
                                val tabAdminSystem = when (currentLang) {
                                    Language.RU -> "Система"
                                    Language.UA -> "Система"
                                    Language.KK -> "Жүйе"
                                    Language.DE -> "System"
                                    Language.ZH -> "系统维护"
                                    else -> "System"
                                }
                                val adminTabs = listOf(
                                    Triple(0, tabAdminProfile, Icons.Default.Person),
                                    Triple(1, tabAdminPlayers, Icons.Default.People),
                                    Triple(2, tabAdminBroadcast, Icons.Default.Send),
                                    Triple(3, tabAdminSystem, Icons.Default.Settings)
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
                                        val tabWidth = maxWidth / adminTabs.size
                                        val indicatorOffset by animateDpAsState(
                                            targetValue = tabWidth * selectedAdminTab,
                                            animationSpec = spring(dampingRatio = 0.75f, stiffness = 450f),
                                            label = "adminTabIndicator"
                                        )

                                        Box(
                                            modifier = Modifier
                                                .width(tabWidth)
                                                .height(42.dp)
                                                .offset(x = indicatorOffset)
                                                .clip(RoundedCornerShape(18.dp))
                                                .background(themeColor)
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceEvenly
                                        ) {
                                            adminTabs.forEach { (index, title, icon) ->
                                                val isSelected = selectedAdminTab == index
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(42.dp)
                                                        .clip(RoundedCornerShape(18.dp))
                                                        .clickable {
                                                            viewModel.triggerAudioFeedback("click")
                                                            selectedAdminTab = index
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = icon,
                                                            contentDescription = null,
                                                            tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Text(
                                                            text = title,
                                                            style = MaterialTheme.typography.labelSmall,
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

                                Spacer(modifier = Modifier.height(4.dp))

                                AnimatedContent(
                                    targetState = selectedAdminTab,
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
                                    label = "AdminTabContentAnim"
                                ) { tab ->
                                    when (tab) {
                                        0 -> {
                                            // TAB 0: PROFILE & QUICK COIN GENERATOR
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                // Live Profile Status Card
                                                val liveCredits by viewModel.credits.collectAsStateWithLifecycle()
                                                val liveRating by viewModel.onlineRating.collectAsStateWithLifecycle()
                                                val liveTier by viewModel.onlineTier.collectAsStateWithLifecycle()
                                                val liveGradient by viewModel.hasNicknameGradient.collectAsStateWithLifecycle()
                                                val liveTag by viewModel.customTag.collectAsStateWithLifecycle()
                                                val liveBonusXp by viewModel.bonusXp.collectAsStateWithLifecycle()

                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = when (currentLang) {
                                                                    Language.RU -> "МОЙ ПРОФИЛЬ: $playerName"
                                                                    Language.UA -> "МІЙ ПРОФІЛЬ: $playerName"
                                                                    Language.KK -> "МЕНІҢ ПРОФИЛІМ: $playerName"
                                                                    Language.DE -> "MEIN PROFIL: $playerName"
                                                                    Language.ZH -> "我的个人档案：$playerName"
                                                                    else -> "MY PROFILE: $playerName"
                                                                },
                                                                style = MaterialTheme.typography.titleSmall,
                                                                fontWeight = FontWeight.ExtraBold,
                                                                color = themeColor
                                                            )
                                                            Surface(
                                                                shape = RoundedCornerShape(8.dp),
                                                                color = MaterialTheme.colorScheme.primaryContainer
                                                            ) {
                                                                Text(
                                                                    text = "ADMIN",
                                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.ExtraBold),
                                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                )
                                                            }
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                        ) {
                                                            Surface(
                                                                modifier = Modifier.weight(1f),
                                                                shape = RoundedCornerShape(14.dp),
                                                                color = Color(0xFFFFD54F).copy(alpha = 0.15f)
                                                            ) {
                                                                Column(modifier = Modifier.padding(10.dp)) {
                                                                    Text(
                                                                        text = when (currentLang) {
                                                                            Language.RU -> "Баланс монет"
                                                                            Language.UA -> "Баланс монет"
                                                                            Language.KK -> "Монета балансы"
                                                                            Language.DE -> "Münzenbestand"
                                                                            Language.ZH -> "代币余额"
                                                                            else -> "Live Balance"
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                                        color = Color(0xFFFFB300)
                                                                    )
                                                                    Text(
                                                                        text = "$liveCredits монет",
                                                                        style = MaterialTheme.typography.titleMedium,
                                                                        fontWeight = FontWeight.Black,
                                                                        color = Color(0xFFFFB300)
                                                                    )
                                                                }
                                                            }

                                                            Surface(
                                                                modifier = Modifier.weight(1f),
                                                                shape = RoundedCornerShape(14.dp),
                                                                color = themeColor.copy(alpha = 0.15f)
                                                            ) {
                                                                Column(modifier = Modifier.padding(10.dp)) {
                                                                    Text(
                                                                        text = when (currentLang) {
                                                                            Language.RU -> "Ранг / ELO"
                                                                            Language.UA -> "Ранг / ELO"
                                                                            Language.KK -> "Дәреже / ELO"
                                                                            Language.DE -> "Rang / ELO"
                                                                            Language.ZH -> "段位积分 / ELO"
                                                                            else -> "Tier / ELO"
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                                        color = themeColor
                                                                    )
                                                                    Text(
                                                                        text = "$liveTier ($liveRating)",
                                                                        style = MaterialTheme.typography.titleMedium,
                                                                        fontWeight = FontWeight.Black,
                                                                        color = themeColor
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                // INSTANT COIN GENERATOR & PRESETS
                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "ВЫДАЧА МОНЕТ И РЕСУРСОВ"
                                                                Language.UA -> "ВИДАЧА МОНЕТ ТА РЕСУРСІВ"
                                                                Language.KK -> "МОНЕТА МЕН РЕСУРС БЕРУ"
                                                                Language.DE -> "MÜNZEN-GENERATOR & PRESETS"
                                                                Language.ZH -> "代币生成与快捷预设"
                                                                else -> "COIN GENERATOR & PRESETS"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color(0xFFFFB300)
                                                        )

                                                        OutlinedTextField(
                                                            value = editUsername,
                                                            onValueChange = { editUsername = it },
                                                            label = {
                                                                val targetUserLabel = when (currentLang) {
                                                                    Language.RU -> "Целевой никнейм (по умолчанию $playerName)"
                                                                    Language.UA -> "Цільовий нікнейм (за замовчуванням $playerName)"
                                                                    Language.KK -> "Мақсатты бүркеншік ат (әдепкі $playerName)"
                                                                    Language.DE -> "Ziel-Benutzername (Standard $playerName)"
                                                                    Language.ZH -> "目标玩家昵称（默认 $playerName）"
                                                                    else -> "Target Username"
                                                                }
                                                                Text(targetUserLabel)
                                                            },
                                                            singleLine = true,
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "Быстрое начисление (+ к текущему балансу):"
                                                                Language.UA -> "Швидке нарахування (+ до поточного балансу):"
                                                                Language.KK -> "Жылдам қосу (+ ағымдағы балансқа):"
                                                                Language.DE -> "Schnellaufladung (+ zum Guthaben):"
                                                                Language.ZH -> "快速充值（+ 追加至现有余额）："
                                                                else -> "Quick add (+ to balance):"
                                                            },
                                                            style = MaterialTheme.typography.bodySmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )

                                                        // Quick Add Grid
                                                        val quickCoins = listOf(1_000, 10_000, 50_000, 100_000, 500_000, 1_000_000)
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            quickCoins.take(3).forEach { amount ->
                                                                FilledTonalButton(
                                                                    onClick = {
                                                                        viewModel.adminGiveCredits(username = editUsername.ifBlank { playerName }, amount = amount, isDelta = true)
                                                                        viewModel.triggerAudioFeedback("success")
                                                                        adminToastMessage = "+$amount монет успешно начислено!"
                                                                    },
                                                                    modifier = Modifier.weight(1f),
                                                                    shape = RoundedCornerShape(12.dp),
                                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                                                ) {
                                                                    Text(
                                                                        text = "+${amount / 1000}k",
                                                                        fontWeight = FontWeight.ExtraBold,
                                                                        style = MaterialTheme.typography.labelMedium
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            quickCoins.drop(3).forEach { amount ->
                                                                FilledTonalButton(
                                                                    onClick = {
                                                                        viewModel.adminGiveCredits(username = editUsername.ifBlank { playerName }, amount = amount, isDelta = true)
                                                                        viewModel.triggerAudioFeedback("success")
                                                                        adminToastMessage = "+$amount монет успешно начислено!"
                                                                    },
                                                                    modifier = Modifier.weight(1f),
                                                                    shape = RoundedCornerShape(12.dp),
                                                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                                                                ) {
                                                                    Text(
                                                                        text = if (amount >= 1_000_000) "+1M" else "+${amount / 1000}k",
                                                                        fontWeight = FontWeight.ExtraBold,
                                                                        style = MaterialTheme.typography.labelMedium
                                                                    )
                                                                }
                                                            }
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Button(
                                                                onClick = {
                                                                    viewModel.adminGiveCredits(username = editUsername.ifBlank { playerName }, amount = 999_999, isDelta = false)
                                                                    viewModel.triggerAudioFeedback("success")
                                                                    adminToastMessage = "Баланс установлен на 999,999 монет"
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                shape = RoundedCornerShape(12.dp),
                                                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                                                            ) {
                                                                Text("MAX (999k)", color = Color.Black, fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium)
                                                            }

                                                            OutlinedButton(
                                                                onClick = {
                                                                    viewModel.adminGiveCredits(username = editUsername.ifBlank { playerName }, amount = 0, isDelta = false)
                                                                    viewModel.triggerAudioFeedback("gameover")
                                                                    adminToastMessage = "Баланс сброшен в 0 монет"
                                                                },
                                                                modifier = Modifier.weight(1f),
                                                                shape = RoundedCornerShape(12.dp)
                                                            ) {
                                                                val resetZeroLabel = when (currentLang) {
                                                                        Language.RU -> "Сброс (0)"
                                                                        Language.UA -> "Скидання (0)"
                                                                        Language.KK -> "Қалпына келтіру (0)"
                                                                        Language.DE -> "Zurücksetzen (0)"
                                                                        Language.ZH -> "归零 (0)"
                                                                        else -> "Reset (0)"
                                                                    }
                                                                    Text(resetZeroLabel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                            }
                                                        }

                                                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                                        // Exact manual input
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            OutlinedTextField(
                                                                value = editCredits,
                                                                onValueChange = { editCredits = it },
                                                                label = {
                                                                    val exactCreditsLabel = when (currentLang) {
                                                                        Language.RU -> "Точная сумма"
                                                                        Language.UA -> "Точна сума"
                                                                        Language.KK -> "Дәл сома"
                                                                        Language.DE -> "Exakter Betrag"
                                                                        Language.ZH -> "精确代币数值"
                                                                        else -> "Exact Credits"
                                                                    }
                                                                    Text(exactCreditsLabel)
                                                                },
                                                                singleLine = true,
                                                                shape = RoundedCornerShape(14.dp),
                                                                modifier = Modifier.weight(1f)
                                                            )

                                                            Button(
                                                                onClick = {
                                                                    val creds = editCredits.toIntOrNull()
                                                                    if (creds != null) {
                                                                        viewModel.adminGiveCredits(username = editUsername.ifBlank { playerName }, amount = creds, isDelta = false)
                                                                        viewModel.triggerAudioFeedback("success")
                                                                        adminToastMessage = "Баланс установлен на $creds монет"
                                                                        editCredits = ""
                                                                    }
                                                                },
                                                                shape = RoundedCornerShape(14.dp)
                                                            ) {
                                                                val setCoinsBtnLabel = when (currentLang) {
                                                                        Language.RU -> "ЗАДАТЬ"
                                                                        Language.UA -> "ЗАДАТИ"
                                                                        Language.KK -> "ОРНАТУ"
                                                                        Language.DE -> "FESTLEGEN"
                                                                        Language.ZH -> "设置"
                                                                        else -> "SET"
                                                                    }
                                                                    Text(setCoinsBtnLabel, fontWeight = FontWeight.Bold)
                                                            }
                                                        }
                                                    }
                                                }

                                                // TIER & COSMETICS EDITOR
                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "РАНГ И СТАТУС"
                                                                Language.UA -> "РАНГ ТА СТАТУС"
                                                                Language.KK -> "ДӘРЕЖЕ МЕН МӘРТЕБЕ"
                                                                Language.DE -> "RANG & STATUS"
                                                                Language.ZH -> "段位与状态配置"
                                                                else -> "TIER & STATUS"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        val tiers = listOf("BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "GRANDMASTER", "LEGEND")
                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .horizontalScroll(rememberScrollState()),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            tiers.forEach { tier ->
                                                                FilterChip(
                                                                    selected = editRank == tier,
                                                                    onClick = {
                                                                        editRank = tier
                                                                        viewModel.adminUpdateAccountRank(editUsername.ifBlank { playerName }, tier)
                                                                        viewModel.triggerAudioFeedback("click")
                                                                        adminToastMessage = "Ранг изменен на $tier"
                                                                    },
                                                                    label = { Text(tier, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall) },
                                                                    shape = RoundedCornerShape(10.dp)
                                                                )
                                                            }
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            OutlinedTextField(
                                                                value = editBonusXp,
                                                                onValueChange = { editBonusXp = it },
                                                                label = {
                                                                    val bonusXpLabel = when (currentLang) {
                                                                        Language.RU -> "Бонус XP"
                                                                        Language.UA -> "Бонус XP"
                                                                        Language.KK -> "Бонус XP"
                                                                        Language.DE -> "Bonus-XP"
                                                                        Language.ZH -> "经验加成 XP"
                                                                        else -> "Bonus XP"
                                                                    }
                                                                    Text(bonusXpLabel)
                                                                },
                                                                singleLine = true,
                                                                shape = RoundedCornerShape(14.dp),
                                                                modifier = Modifier.weight(1f)
                                                            )

                                                            Button(
                                                                onClick = {
                                                                    val xp = editBonusXp.toIntOrNull() ?: 0
                                                                    viewModel.adminUpdateAccountBonusXp(editUsername.ifBlank { playerName }, xp)
                                                                    viewModel.triggerAudioFeedback("success")
                                                                    adminToastMessage = "Опыт обновлен: $xp XP"
                                                                },
                                                                shape = RoundedCornerShape(14.dp)
                                                            ) {
                                                                val setXpBtnLabel = when (currentLang) {
                                                                        Language.RU -> "ЗАДАТЬ XP"
                                                                        Language.UA -> "ЗАДАТИ XP"
                                                                        Language.KK -> "XP ОРНАТУ"
                                                                        Language.DE -> "XP FESTLEGEN"
                                                                        Language.ZH -> "设置 XP"
                                                                        else -> "SET XP"
                                                                    }
                                                                    Text(setXpBtnLabel, fontWeight = FontWeight.Bold)
                                                            }
                                                        }

                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            Text(
                                                                text = when (currentLang) {
                                                                    Language.RU -> "Градиент никнейма"
                                                                    Language.UA -> "Градієнт нікнейму"
                                                                    Language.KK -> "Бүркеншік ат градиенті"
                                                                    Language.DE -> "Farbverlauf-Name"
                                                                    Language.ZH -> "昵称渐变特效"
                                                                    else -> "Nickname Gradient"
                                                                },
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                            Switch(
                                                                checked = editHasGradient,
                                                                onCheckedChange = {
                                                                    editHasGradient = it
                                                                    viewModel.adminUpdateAccountGradient(editUsername.ifBlank { playerName }, it)
                                                                    viewModel.triggerAudioFeedback("click")
                                                                    adminToastMessage = if (it) "Градиент включен" else "Градиент отключен"
                                                                }
                                                            )
                                                        }
                                                    }
                                                }

                                                // Quick Action Cheats Card
                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "РАЗБЛОКИРОВКА И ЧИТЫ"
                                                                Language.UA -> "РОЗБЛОКУВАННЯ ТА ЧІТИ"
                                                                Language.KK -> "ҚҰЛЫПТЫ АШУ ЖӘНЕ ЧИТТЕР"
                                                                Language.DE -> "FREISCHALTUNGEN & CHEATS"
                                                                Language.ZH -> "全量解锁与测试指令"
                                                                else -> "UNLOCKS & CHEATS"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        FilledTonalButton(
                                                            onClick = {
                                                                viewModel.adminGiveAllCosmetics(editUsername.ifBlank { playerName })
                                                                viewModel.triggerAudioFeedback("success")
                                                                adminToastMessage = "Вся косметика разблокирована!"
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val grantCosmeticsLabel = when (currentLang) {
                                                                    Language.RU -> "ВЫДАТЬ ВСЮ КОСМЕТИКУ (Скины, Рамки, Шрифты)"
                                                                    Language.UA -> "ВИДАТИ ВСЮ КОСМЕТИКУ (Скіни, Рамки, Шрифти)"
                                                                    Language.KK -> "БАРЛЫҚ КОСМЕТИКАНЫ БЕРУ (Скиндер, Жақтаулар, Қаріптер)"
                                                                    Language.DE -> "ALLE KOSMETIK FREISCHALTEN (Skins, Rahmen, Schriftarten)"
                                                                    Language.ZH -> "解锁全部装扮道具（皮肤、头像框、特效字体）"
                                                                    else -> "GRANT ALL COSMETICS & SKINS"
                                                                }
                                                                Text(grantCosmeticsLabel, fontWeight = FontWeight.Bold)
                                                        }

                                                        FilledTonalButton(
                                                            onClick = {
                                                                viewModel.adminUnlockAllAchievements()
                                                                viewModel.triggerAudioFeedback("success")
                                                                adminToastMessage = "Все достижения открыты + 5000 монет"
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val unlockAchLabel = when (currentLang) {
                                                                    Language.RU -> "ОТКРЫТЬ ВСЕ ДОСТИЖЕНИЯ (+5000 МОНЕТ)"
                                                                    Language.UA -> "ВІДКРИТИ ВСІ ДОСЯГНЕННЯ (+5000 МОНЕТ)"
                                                                    Language.KK -> "БАРЛЫҚ ЖЕТІСТІКТЕРДІ АШУ (+5000 МОНЕТ)"
                                                                    Language.DE -> "ALLE ERFOLGE FREISCHALTEN (+5000 MÜNZEN)"
                                                                    Language.ZH -> "解锁所有成就并获取 +5000 代币"
                                                                    else -> "UNLOCK ALL ACHIEVEMENTS (+5000 CREDITS)"
                                                                }
                                                                Text(unlockAchLabel, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        1 -> {
                                            // TAB 1: FIREBASE USERS & DETAILED INSPECTION
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = userSearchQuery,
                                                    onValueChange = { userSearchQuery = it },
                                                    label = {
                                                        val userSearchLabel = when (currentLang) {
                                                            Language.RU -> "Поиск (по нику, UID, почте)..."
                                                            Language.UA -> "Пошук (за ніком, UID, поштою)..."
                                                            Language.KK -> "Іздеу (ник, UID, пошта бойынша)..."
                                                            Language.DE -> "Suche (Name, UID, E-Mail)..."
                                                            Language.ZH -> "搜索玩家（按昵称、UID 或邮箱）..."
                                                            else -> "Search (name, UID, email)..."
                                                        }
                                                        Text(userSearchLabel)
                                                    },
                                                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(14.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )

                                                // Filter Chips
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .horizontalScroll(rememberScrollState()),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    val verifiedCount = firebaseUsers.count { 
                                                        (it["email_verified"] as? Boolean) == true || 
                                                        (it["is_verified"] as? Boolean) == true || 
                                                        (it["emailVerified"] as? Boolean) == true 
                                                    }
                                                    val unverifiedCount = firebaseUsers.size - verifiedCount
                                                    val filterLabels = listOf(
                                                        "Все (${firebaseUsers.size})",
                                                        "Онлайн (${firebaseUsers.count { (it["is_online"] as? Boolean) == true }})",
                                                        "Вериф. ($verifiedCount)",
                                                        "Невериф. ($unverifiedCount)",
                                                        "Бан (${firebaseUsers.count { (it["is_banned"] as? Boolean) == true }})",
                                                        "Богатые (${firebaseUsers.count { ((it["credits"] as? Number)?.toLong() ?: 0L) >= 50_000 }})"
                                                    )
                                                    filterLabels.forEachIndexed { index, label ->
                                                        FilterChip(
                                                            selected = selectedUserFilter == index,
                                                            onClick = { selectedUserFilter = index },
                                                            label = { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold) },
                                                            shape = RoundedCornerShape(10.dp)
                                                        )
                                                    }
                                                }

                                                val filteredUsers = remember(firebaseUsers, userSearchQuery, selectedUserFilter) {
                                                    var list = firebaseUsers
                                                    if (userSearchQuery.isNotBlank()) {
                                                        list = list.filter {
                                                            val pName = (it["player_name"] as? String) ?: ""
                                                            val email = (it["email"] as? String) ?: ""
                                                            val uid = (it["uid"] as? String) ?: ""
                                                            pName.contains(userSearchQuery, ignoreCase = true) ||
                                                            email.contains(userSearchQuery, ignoreCase = true) ||
                                                            uid.contains(userSearchQuery, ignoreCase = true)
                                                        }
                                                    }
                                                    when (selectedUserFilter) {
                                                        1 -> list.filter {
                                                            val rawSynced = (it["last_synced_timestamp"] as? Number)?.toLong() ?: 0L
                                                            (it["is_online"] as? Boolean) == true && (rawSynced == 0L || (System.currentTimeMillis() - rawSynced) < 120_000L)
                                                        }
                                                        2 -> list.filter { (it["email_verified"] as? Boolean) == true || (it["is_verified"] as? Boolean) == true || (it["emailVerified"] as? Boolean) == true }
                                                        3 -> list.filter { !((it["email_verified"] as? Boolean) == true || (it["is_verified"] as? Boolean) == true || (it["emailVerified"] as? Boolean) == true) }
                                                        4 -> list.filter { (it["is_banned"] as? Boolean) == true }
                                                        5 -> list.filter { ((it["credits"] as? Number)?.toLong() ?: 0L) >= 50_000 }
                                                        else -> list
                                                    }
                                                }

                                                LazyColumn(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .weight(1f),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                                    contentPadding = PaddingValues(bottom = 24.dp)
                                                ) {
                                                    items(filteredUsers) { user ->
                                                        val uName = (user["player_name"] as? String) ?: "Unknown"
                                                        val uEmail = (user["email"] as? String) ?: "No email"
                                                        val uUid = (user["uid"] as? String) ?: ""
                                                        val uCredits = (user["credits"] as? Number)?.toLong() ?: 0L
                                                        val isBanned = (user["is_banned"] as? Boolean) == true
                                                        val rawLastSynced = (user["last_synced_timestamp"] as? Number)?.toLong() ?: 0L
                                                        val isRecentlyActive = (System.currentTimeMillis() - rawLastSynced) < 120_000L
                                                        val isOnline = ((user["is_online"] as? Boolean) == true) && (rawLastSynced == 0L || isRecentlyActive)
                                                        val isEmailVerified = (user["email_verified"] as? Boolean) == true || (user["is_verified"] as? Boolean) == true || (user["emailVerified"] as? Boolean) == true
                                                        val uTier = (user["online_tier"] as? String) ?: "BRONZE"
                                                        val uRating = (user["online_rating"] as? Number)?.toInt() ?: 1000
                                                        val uHighScore = (user["stats_high_score"] as? Number)?.toInt() ?: 0
                                                        val uLines = (user["stats_cleared_lines"] as? Number)?.toInt() ?: 0
                                                        val uGames = (user["stats_games_played"] as? Number)?.toInt() ?: 0
                                                        val uBonusXp = (user["bonus_xp"] as? Number)?.toInt() ?: 0
                                                        val uCustomTag = (user["custom_tag"] as? String) ?: ""
                                                        val uFrame = (user["equipped_avatar_frame"] as? String) ?: "standard"
                                                        val uAvatarEmoji = (user["custom_avatar_emoji"] as? String) ?: (user["avatarEmoji"] as? String) ?: ""
                                                        val uAvatarBgColor = (user["custom_avatar_bg_color"] as? String) ?: (user["avatarBgColor"] as? String) ?: ""
                                                        val uAvatarBase64 = (user["custom_avatar_base64"] as? String) ?: ""

                                                        ElevatedCard(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(20.dp),
                                                            colors = CardDefaults.elevatedCardColors(
                                                                containerColor = if (isBanned) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                                                                else MaterialTheme.colorScheme.surfaceContainerHigh
                                                            )
                                                        ) {
                                                            Column(
                                                                modifier = Modifier.padding(14.dp),
                                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                // Header Row: Avatar + Name + Badges
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                                    ) {
                                                                        PlayerAvatarView(
                                                                            playerName = uName,
                                                                            avatarEmoji = uAvatarEmoji,
                                                                            avatarBgColorHex = uAvatarBgColor,
                                                                            avatarFrame = uFrame,
                                                                            avatarBase64 = uAvatarBase64,
                                                                            size = 40.dp,
                                                                            showOnlineDot = true,
                                                                            isOnline = isOnline
                                                                        )

                                                                        Row(
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                                        ) {
                                                                            Text(
                                                                                text = uName,
                                                                                style = MaterialTheme.typography.titleMedium,
                                                                                fontWeight = FontWeight.Black
                                                                            )

                                                                            val isTargetRealAdmin = uEmail.equals("ezik02021@gmail.com", ignoreCase = true)

                                                                            if (isTargetRealAdmin) {
                                                                                Surface(
                                                                                    shape = RoundedCornerShape(6.dp),
                                                                                    color = MaterialTheme.colorScheme.primaryContainer
                                                                                ) {
                                                                                    Text(
                                                                                        text = "ADMIN",
                                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                                        fontWeight = FontWeight.Bold,
                                                                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                                                    )
                                                                                }
                                                                            }

                                                                            if (uCustomTag.isNotBlank()) {
                                                                                Surface(
                                                                                    shape = RoundedCornerShape(6.dp),
                                                                                    color = themeColor.copy(alpha = 0.2f)
                                                                                ) {
                                                                                    Text(
                                                                                        text = "[$uCustomTag]",
                                                                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                                                        fontWeight = FontWeight.Bold,
                                                                                        color = themeColor,
                                                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                                                    )
                                                                                }
                                                                            }
                                                                        }
                                                                    }

                                                                    Surface(
                                                                        shape = RoundedCornerShape(8.dp),
                                                                        color = if (isBanned) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) 
                                                                                else if (isEmailVerified) Color(0xFF00E676).copy(alpha = 0.2f) 
                                                                                else Color(0xFFFF9100).copy(alpha = 0.2f),
                                                                        modifier = Modifier.clickable {
                                                                            if (!uUid.startsWith("local_")) {
                                                                                viewModel.adminSetEmailVerified(uUid, !isEmailVerified)
                                                                                viewModel.triggerAudioFeedback("click")
                                                                                adminToastMessage = if (!isEmailVerified) "Почта $uName подтверждена" else "Подтверждение почты $uName снято"
                                                                            }
                                                                        }
                                                                    ) {
                                                                        Row(
                                                                            verticalAlignment = Alignment.CenterVertically,
                                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = if (isBanned) Icons.Default.Block else if (isEmailVerified) Icons.Default.CheckCircle else Icons.Default.MarkEmailUnread,
                                                                                contentDescription = null,
                                                                                modifier = Modifier.size(11.dp),
                                                                                tint = if (isBanned) MaterialTheme.colorScheme.error else if (isEmailVerified) Color(0xFF00E676) else Color(0xFFFF9100)
                                                                            )
                                                                            Text(
                                                                                text = if (isBanned) "BANNED" else if (isEmailVerified) "VERIFIED" else "PENDING",
                                                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                                                                color = if (isBanned) MaterialTheme.colorScheme.error else if (isEmailVerified) Color(0xFF00E676) else Color(0xFFFF9100)
                                                                            )
                                                                        }
                                                                    }
                                                                }

                                                                // UID + Email Row (with copy UID & copy Email buttons)
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = "UID: ${uUid.take(12)}...",
                                                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                        IconButton(
                                                                            onClick = {
                                                                                clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(uUid))
                                                                                viewModel.triggerAudioFeedback("click")
                                                                                adminToastMessage = "UID скопирован: $uUid"
                                                                            },
                                                                            modifier = Modifier.size(18.dp)
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = Icons.Default.ContentCopy,
                                                                                contentDescription = "Copy UID",
                                                                                modifier = Modifier.size(12.dp),
                                                                                tint = themeColor
                                                                            )
                                                                        }
                                                                    }

                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                    ) {
                                                                        Text(
                                                                            text = if (uEmail.length > 22) uEmail.take(20) + "..." else uEmail,
                                                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                        )
                                                                        if (uEmail != "No email" && uEmail.isNotBlank()) {
                                                                            IconButton(
                                                                                onClick = {
                                                                                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(uEmail))
                                                                                    viewModel.triggerAudioFeedback("click")
                                                                                    adminToastMessage = "Email скопирован: $uEmail"
                                                                                },
                                                                                modifier = Modifier.size(18.dp)
                                                                            ) {
                                                                                Icon(
                                                                                    imageVector = Icons.Default.ContentCopy,
                                                                                    contentDescription = "Copy Email",
                                                                                    modifier = Modifier.size(12.dp),
                                                                                    tint = themeColor
                                                                                )
                                                                            }
                                                                        }
                                                                    }
                                                                }

                                                                // Stats Grid: Credits, ELO/Tier, Score, Lines, Games
                                                                Surface(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    shape = RoundedCornerShape(12.dp),
                                                                    color = MaterialTheme.colorScheme.surfaceContainer
                                                                ) {
                                                                    Row(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .padding(8.dp),
                                                                        horizontalArrangement = Arrangement.SpaceAround,
                                                                        verticalAlignment = Alignment.CenterVertically
                                                                    ) {
                                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                            Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                                ) {
                                                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(13.dp))
                                                    Text("$uCredits", fontWeight = FontWeight.Black, style = MaterialTheme.typography.labelMedium, color = Color(0xFFFFB300))
                                                }
                                                                            Text("Монеты", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                        }
                                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                            Text("$uTier ($uRating)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium, color = themeColor)
                                                                            Text("Лига / ELO", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                        }
                                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                            Text("$uHighScore", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                                            Text("Рекорд", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                        }
                                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                            Text("$uLines", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                                            Text("Линии", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                        }
                                                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                            Text("$uGames", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                                                                            Text("Игры", style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                                        }
                                                                    }
                                                                }

                                                                // Action Buttons Row
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    // + Coins Quick Action Button
                                                                    FilledTonalButton(
                                                                        onClick = {
                                                                            grantingCoinsUser = user
                                                                            grantCoinsCustomAmount = ""
                                                                            viewModel.triggerAudioFeedback("click")
                                                                        },
                                                                        shape = RoundedCornerShape(10.dp),
                                                                        modifier = Modifier.weight(1f),
                                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                                                    ) {
                                                                        Icon(imageVector = Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB300))
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        Text("+ Монеты", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                                                    }

                                                                    // Full Edit Button
                                                                    OutlinedButton(
                                                                        onClick = {
                                                                            editingAdminUser = user
                                                                            editUserCredits = uCredits.toString()
                                                                            editUserHighScore = uHighScore.toString()
                                                                            editUserLines = uLines.toString()
                                                                            editUserGames = uGames.toString()
                                                                            editUserXp = uBonusXp.toString()
                                                                            editUserTier = uTier
                                                                            editUserGradient = (user["has_nickname_gradient"] as? Boolean) == true
                                                                            editUserTagUnlocked = (user["custom_tag_unlocked"] as? Boolean) == true
                                                                            editUserTag = uCustomTag
                                                                            editUserEmailVerified = isEmailVerified
                                                                            viewModel.triggerAudioFeedback("click")
                                                                        },
                                                                        shape = RoundedCornerShape(10.dp),
                                                                        modifier = Modifier.weight(1f),
                                                                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                                                    ) {
                                                                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                                                        Spacer(modifier = Modifier.width(4.dp))
                                                                        val editBtnLabel = when (currentLang) {
                                                                            Language.RU -> "Правка"
                                                                            Language.UA -> "Редагувати"
                                                                            Language.KK -> "Өңдеу"
                                                                            Language.DE -> "Bearbeiten"
                                                                            Language.ZH -> "编辑"
                                                                            else -> "Edit"
                                                                        }
                                                                        Text(editBtnLabel, style = MaterialTheme.typography.labelSmall)
                                                                    }

                                                                    val isTargetProtectedAdmin = uEmail.equals("ezik02021@gmail.com", ignoreCase = true)

                                                                    if (!isTargetProtectedAdmin) {
                                                                        OutlinedButton(
                                                                            onClick = {
                                                                                viewModel.adminBanUser(uUid, !isBanned)
                                                                                viewModel.triggerAudioFeedback("click")
                                                                                adminToastMessage = if (!isBanned) "Игрок $uName забанен" else "Игрок $uName разбанен"
                                                                            },
                                                                            shape = RoundedCornerShape(10.dp),
                                                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                                                        ) {
                                                                            val banBtnLabel = if (isBanned) {
                                                                                when (currentLang) {
                                                                                    Language.RU -> "Разбан"
                                                                                    Language.UA -> "Розбан"
                                                                                    Language.KK -> "Бұғаттан шығару"
                                                                                    Language.DE -> "Entbannen"
                                                                                    Language.ZH -> "解封"
                                                                                    else -> "Unban"
                                                                                }
                                                                            } else {
                                                                                when (currentLang) {
                                                                                    Language.RU -> "Бан"
                                                                                    Language.UA -> "Бан"
                                                                                    Language.KK -> "Бұғаттау"
                                                                                    Language.DE -> "Bannen"
                                                                                    Language.ZH -> "封禁"
                                                                                    else -> "Ban"
                                                                                }
                                                                            }
                                                                            Text(
                                                                                text = banBtnLabel,
                                                                                style = MaterialTheme.typography.labelSmall,
                                                                                color = if (isBanned) Color(0xFF00E676) else MaterialTheme.colorScheme.error
                                                                            )
                                                                        }

                                                                        IconButton(
                                                                            onClick = {
                                                                                viewModel.adminDeleteFirebaseUserDirect(uUid)
                                                                                viewModel.triggerAudioFeedback("gameover")
                                                                                adminToastMessage = "Аккаунт $uName удален из базы"
                                                                            },
                                                                            modifier = Modifier.size(32.dp)
                                                                        ) {
                                                                            Icon(
                                                                                imageVector = Icons.Default.Delete,
                                                                                contentDescription = "Delete",
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
                                        }
                                        2 -> {
                                            // TAB 2: GLOBAL BROADCAST ANNOUNCEMENTS
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "ОТПРАВКА СИСТЕМНОГО ОПОВЕЩЕНИЯ"
                                                                Language.UA -> "ВІДПРАВКА СИСТЕМНОГО ОПОВІЩЕННЯ"
                                                                Language.KK -> "ЖҮЙЕЛІК ХАБАРЛАНДЫРУ ЖІБЕРУ"
                                                                Language.DE -> "SYSTEMWEITE BENACHRICHTIGUNG"
                                                                Language.ZH -> "发布全服系统公告"
                                                                else -> "GLOBAL SERVER BROADCAST"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "Сообщение отобразится у всех онлайн-игроков в чате лобби и будет сохранено в базе данных."
                                                                Language.UA -> "Повідомлення відобразиться у всіх онлайн-гравців у чаті лобі та буде збережено в базі даних."
                                                                Language.KK -> "Хабарлама лобби чатындағы барлық онлайн ойыншыларға көрсетіледі және дерекқорда сақталады."
                                                                Language.DE -> "Die Nachricht wird allen Online-Spielern im Lobby-Chat angezeigt und in der Datenbank gespeichert."
                                                                Language.ZH -> "公告将即时广播给所有在全服大厅聊天的在线玩家，并存入数据库记录。"
                                                                else -> "Message will be broadcast to all connected players in the multiplayer lobby chat and saved to global database."
                                                            },
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )

                                                        // Quick template buttons
                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "Быстрые шаблоны:"
                                                                Language.UA -> "Швидкі шаблони:"
                                                                Language.KK -> "Жылдам үлгілер:"
                                                                Language.DE -> "Schnellvorlagen:"
                                                                Language.ZH -> "快捷模板："
                                                                else -> "Quick templates:"
                                                            },
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )

                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .horizontalScroll(rememberScrollState()),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            SuggestionChip(
                                                                onClick = {
                                                                    broadcastTitle = "Технические работы"
                                                                    broadcastText = "Серверные работы завершены. Все сетевые режимы работают штатно!"
                                                                },
                                                                label = { Text("Техработы", style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                            SuggestionChip(
                                                                onClick = {
                                                                    broadcastTitle = "Бонусные награды"
                                                                    broadcastText = "Администрация начислила бонусные монеты всем активным игрокам! Проверьте свой баланс."
                                                                },
                                                                label = { Text("Награды", style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                            SuggestionChip(
                                                                onClick = {
                                                                    broadcastTitle = "Обновление 0.94.9"
                                                                    broadcastText = "Вышло обновление клиента: улучшена стабильность мультиплеера и синхронизация монет!"
                                                                },
                                                                label = { Text("Обновление", style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                            SuggestionChip(
                                                                onClick = {
                                                                    broadcastTitle = "Турнир начался"
                                                                    broadcastText = "Запущен турнирный сезон! Играйте в сетевых дуэлях и занимайте топ в лидерборде."
                                                                },
                                                                label = { Text("Турнир", style = MaterialTheme.typography.labelSmall) }
                                                            )
                                                        }

                                                        OutlinedTextField(
                                                            value = broadcastTitle,
                                                            onValueChange = { broadcastTitle = it },
                                                            label = {
                                                                val broadcastTitleLabel = when (currentLang) {
                                                                    Language.RU -> "Заголовок оповещения"
                                                                    Language.UA -> "Заголовок оповіщення"
                                                                    Language.KK -> "Хабарландыру тақырыбы"
                                                                    Language.DE -> "Titel der Nachricht"
                                                                    Language.ZH -> "公告标题"
                                                                    else -> "Broadcast Title"
                                                                }
                                                                Text(broadcastTitleLabel)
                                                            },
                                                            singleLine = true,
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )

                                                        OutlinedTextField(
                                                            value = broadcastText,
                                                            onValueChange = { broadcastText = it },
                                                            label = {
                                                                val broadcastContentLabel = when (currentLang) {
                                                                    Language.RU -> "Текст сообщения"
                                                                    Language.UA -> "Текст повідомлення"
                                                                    Language.KK -> "Хабарлама мәтіні"
                                                                    Language.DE -> "Nachrichteninhalt"
                                                                    Language.ZH -> "公告正文"
                                                                    else -> "Message Content"
                                                                }
                                                                Text(broadcastContentLabel)
                                                            },
                                                            minLines = 3,
                                                            shape = RoundedCornerShape(14.dp),
                                                            modifier = Modifier.fillMaxWidth()
                                                        )

                                                        Button(
                                                            onClick = {
                                                                if (broadcastText.isNotBlank()) {
                                                                    viewModel.adminSendGlobalBroadcast(broadcastTitle, broadcastText)
                                                                    viewModel.triggerAudioFeedback("success")
                                                                    broadcastSentFeedback = when (currentLang) {
                                                                        Language.RU -> "Оповещение успешно отправлено всем игрокам!"
                                                                        Language.UA -> "Оповіщення успішно надіслано всім гравцям!"
                                                                        Language.KK -> "Хабарландыру барлық ойыншыларға сәтті жіберілді!"
                                                                        Language.DE -> "Benachrichtigung erfolgreich an alle Spieler gesendet!"
                                                                        Language.ZH -> "全服系统公告已成功下发！"
                                                                        else -> "Broadcast dispatched successfully!"
                                                                    }
                                                                    broadcastTitle = ""
                                                                    broadcastText = ""
                                                                }
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val sendAllBtnLabel = when (currentLang) {
                                                                    Language.RU -> "ОТПРАВИТЬ ВСЕМ"
                                                                    Language.UA -> "НАДІСЛАТИ ВСІМ"
                                                                    Language.KK -> "БАРЛЫҒЫНА ЖІБЕРУ"
                                                                    Language.DE -> "AN ALLE SENDEN"
                                                                    Language.ZH -> "全员下发"
                                                                    else -> "BROADCAST TO ALL"
                                                                }
                                                                Text(sendAllBtnLabel, fontWeight = FontWeight.Bold)
                                                        }

                                                        broadcastSentFeedback?.let { msg ->
                                                            Text(
                                                                text = msg,
                                                                color = Color(0xFF00E676),
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        3 -> {
                                            // TAB 3: DATABASE & SYSTEM TOOLS
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .verticalScroll(rememberScrollState())
                                                    .padding(16.dp),
                                                verticalArrangement = Arrangement.spacedBy(14.dp)
                                            ) {
                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "ГЛОБАЛЬНАЯ ЭКОНОМИКА И НАГРАДЫ"
                                                                Language.UA -> "ГЛОБАЛЬНА ЕКОНОМІКА ТА НАГОРОДИ"
                                                                Language.KK -> "ЖАҺАНДЫҚ ЭКОНОМИКА ЖӘНЕ СЫЙЛЫҚТАР"
                                                                Language.DE -> "GLOBALE WIRTSCHAFT & BELOHNUNGEN"
                                                                Language.ZH -> "全服经济与奖励分配"
                                                                else -> "GLOBAL ECONOMY & REWARDS"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = themeColor
                                                        )

                                                        Button(
                                                            onClick = {
                                                                viewModel.adminGiveCreditsToAll(10_000)
                                                                viewModel.triggerAudioFeedback("success")
                                                                adminToastMessage = "Всем игрокам начислено по +10,000 монет"
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            shape = RoundedCornerShape(14.dp),
                                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300))
                                                        ) {
                                                            Icon(imageVector = Icons.Default.CardGiftcard, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Black)
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val grantAll10kLabel = when (currentLang) {
                                                                    Language.RU -> "ВЫДАТЬ ВСЕМ ИГРОКАМ ПО +10,000 МОНЕТ"
                                                                    Language.UA -> "ВИДАТИ ВСІМ ГРАВЦЯМ ПО +10,000 МОНЕТ"
                                                                    Language.KK -> "БАРЛЫҚ ОЙЫНШЫЛАРҒА +10,000 МОНЕТА БЕРУ"
                                                                    Language.DE -> "ALLEN SPIELERN +10.000 MÜNZEN GEBEN"
                                                                    Language.ZH -> "向全服所有玩家发放 +10,000 代币"
                                                                    else -> "GRANT +10,000 CREDITS TO ALL USERS"
                                                                }
                                                                Text(grantAll10kLabel, color = Color.Black, fontWeight = FontWeight.Black)
                                                        }
                                                    }
                                                }

                                                ElevatedCard(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(24.dp),
                                                    colors = CardDefaults.elevatedCardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(18.dp),
                                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                                    ) {
                                                        Text(
                                                            text = when (currentLang) {
                                                                Language.RU -> "УПРАВЛЕНИЕ БАЗОЙ ДАННЫХ"
                                                                Language.UA -> "УПРАВЛІННЯ БАЗОЮ ДАНИХ"
                                                                Language.KK -> "ДЕРЕКҚОРДЫ БАСҚАРУ"
                                                                Language.DE -> "DATENBANK-VERWALTUNG"
                                                                Language.ZH -> "数据库核心管理"
                                                                else -> "DATABASE MANAGEMENT"
                                                            },
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = MaterialTheme.colorScheme.error
                                                        )

                                                        Button(
                                                            onClick = {
                                                                viewModel.adminClearAllScores()
                                                                viewModel.triggerAudioFeedback("gameover")
                                                                adminToastMessage = "Все рекорды в БД сброшены"
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val resetScoresLabel = when (currentLang) {
                                                                    Language.RU -> "СБРОСИТЬ ВСЕ РЕКОРДЫ В БД"
                                                                    Language.UA -> "СКИНУТИ ВСІ РЕКОРДИ В БД"
                                                                    Language.KK -> "ДЕРЕКҚОРДАҒЫ БАРЛЫҚ РЕКОРДТАРДЫ ҚАЛПЫНА КЕЛТІРУ"
                                                                    Language.DE -> "ALLE HIGHSCORES IN DB ZURÜCKSETZEN"
                                                                    Language.ZH -> "重置数据库所有历史最高得分"
                                                                    else -> "RESET ALL DATABASE SCORES"
                                                                }
                                                                Text(resetScoresLabel, fontWeight = FontWeight.Bold)
                                                        }

                                                        Button(
                                                            onClick = {
                                                                viewModel.adminClearAllNonAdminAccounts()
                                                                viewModel.triggerAudioFeedback("gameover")
                                                                adminToastMessage = "Все сторонние аккаунты удалены"
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            val deleteUsersLabel = when (currentLang) {
                                                                    Language.RU -> "УДАЛИТЬ ВСЕ АККАУНТЫ КРОМЕ АДМИНА"
                                                                    Language.UA -> "ВИДАЛИТИ ВСІ АКАУНТИ КРІМ АДМІНА"
                                                                    Language.KK -> "ӘКІМШІДЕН БАСҚА БАРЛЫҚ АККАУНТТАРДЫ ЖОЮ"
                                                                    Language.DE -> "ALLE BENUTZER AUSSER ADMIN LÖSCHEN"
                                                                    Language.ZH -> "清空所有非管理员注册账号"
                                                                    else -> "DELETE ALL NON-ADMIN USERS"
                                                                }
                                                                Text(deleteUsersLabel, fontWeight = FontWeight.Bold)
                                                        }

                                                        Button(
                                                            onClick = {
                                                                showWipeConfirmDialog = true
                                                                viewModel.triggerAudioFeedback("click")
                                                            },
                                                            modifier = Modifier.fillMaxWidth(),
                                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                            shape = RoundedCornerShape(14.dp)
                                                        ) {
                                                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Text("ПОЛНЫЙ ВАЙП БД (FIRESTORE + БАЗА)", fontWeight = FontWeight.Black)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Quick Coins Grant Modal for specific user
                                if (grantingCoinsUser != null) {
                                    val target = grantingCoinsUser!!
                                    val tUid = (target["uid"] as? String) ?: ""
                                    val tName = (target["player_name"] as? String) ?: "Player"
                                    val tCurrentCoins = (target["credits"] as? Number)?.toLong() ?: 0L

                                    AlertDialog(
                                        onDismissRequest = { grantingCoinsUser = null },
                                        title = {
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Выдача монет: $tName"
                                                    Language.UA -> "Видача монет: $tName"
                                                    Language.KK -> "Монета беру: $tName"
                                                    Language.DE -> "Münzen vergeben: $tName"
                                                    Language.ZH -> "发放代币：$tName"
                                                    else -> "Grant Coins: $tName"
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        },
                                        text = {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .imePadding(),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text(
                                                    text = when (currentLang) {
                                                        Language.RU -> "Текущий баланс: $tCurrentCoins монет"
                                                        Language.UA -> "Поточний баланс: $tCurrentCoins монет"
                                                        Language.KK -> "Ағымдағы баланс: $tCurrentCoins монета"
                                                        Language.DE -> "Aktueller Stand: $tCurrentCoins Münzen"
                                                        Language.ZH -> "当前持有余额：$tCurrentCoins 代币"
                                                        else -> "Current Balance: $tCurrentCoins credits"
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFFB300)
                                                )

                                                Text(
                                                    text = when (currentLang) {
                                                        Language.RU -> "Быстрое добавление:"
                                                        Language.UA -> "Швидке додавання:"
                                                        Language.KK -> "Жылдам қосу:"
                                                        Language.DE -> "Schnellauswahl:"
                                                        Language.ZH -> "快捷增发："
                                                        else -> "Quick Add Presets:"
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    listOf(5_000, 25_000, 100_000).forEach { amount ->
                                                        FilledTonalButton(
                                                            onClick = {
                                                                viewModel.adminGiveCredits(uid = tUid, username = tName, amount = amount, isDelta = true)
                                                                viewModel.triggerAudioFeedback("success")
                                                                adminToastMessage = "+$amount монет начислено пользователю $tName"
                                                                grantingCoinsUser = null
                                                            },
                                                            modifier = Modifier.weight(1f),
                                                            shape = RoundedCornerShape(10.dp)
                                                        ) {
                                                            Text("+${amount / 1000}k", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                                        }
                                                    }
                                                }

                                                OutlinedTextField(
                                                    value = grantCoinsCustomAmount,
                                                    onValueChange = { grantCoinsCustomAmount = it },
                                                    label = {
                                                        val customAmountLabel = when (currentLang) {
                                                            Language.RU -> "Своя сумма (+)"
                                                            Language.UA -> "Своя сума (+)"
                                                            Language.KK -> "Өз сомасы (+)"
                                                            Language.DE -> "Eigener Betrag (+)"
                                                            Language.ZH -> "自定义充值数额 (+)"
                                                            else -> "Custom amount (+)"
                                                        }
                                                        Text(customAmountLabel)
                                                    },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            }
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    val delta = grantCoinsCustomAmount.toIntOrNull()
                                                    if (delta != null && delta > 0) {
                                                        viewModel.adminGiveCredits(uid = tUid, username = tName, amount = delta, isDelta = true)
                                                        viewModel.triggerAudioFeedback("success")
                                                        adminToastMessage = "+$delta монет начислено пользователю $tName"
                                                        grantingCoinsUser = null
                                                    }
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                val grantBtnLabel = when (currentLang) {
                                                    Language.RU -> "НАЧИСЛИТЬ"
                                                    Language.UA -> "НАРАХУВАТИ"
                                                    Language.KK -> "ЕСЕПТЕУ"
                                                    Language.DE -> "VERGEBEN"
                                                    Language.ZH -> "确认充值"
                                                    else -> "GRANT"
                                                }
                                                Text(grantBtnLabel, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { grantingCoinsUser = null }) {
                                                val cancelBtnLabel = when (currentLang) {
                                                    Language.RU -> "Отмена"
                                                    Language.UA -> "Скасувати"
                                                    Language.KK -> "Бас тарту"
                                                    Language.DE -> "Abbrechen"
                                                    Language.ZH -> "取消"
                                                    else -> "Cancel"
                                                }
                                                Text(cancelBtnLabel)
                                            }
                                        }
                                    )
                                }

                                // Full Edit User Modal
                                if (editingAdminUser != null) {
                                    val target = editingAdminUser!!
                                    val tUid = (target["uid"] as? String) ?: ""
                                    val tName = (target["player_name"] as? String) ?: "Player"

                                    AlertDialog(
                                        onDismissRequest = { editingAdminUser = null },
                                        title = {
                                            Text(
                                                text = when (currentLang) {
                                                    Language.RU -> "Редактирование: $tName"
                                                    Language.UA -> "Редагування: $tName"
                                                    Language.KK -> "Өңдеу: $tName"
                                                    Language.DE -> "Bearbeiten: $tName"
                                                    Language.ZH -> "编辑玩家数据：$tName"
                                                    else -> "Edit Stats: $tName"
                                                },
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        },
                                        text = {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .imePadding()
                                                    .verticalScroll(rememberScrollState()),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                OutlinedTextField(
                                                    value = editUserCredits,
                                                    onValueChange = { editUserCredits = it },
                                                    label = {
                                                    val creditsLabel = when (currentLang) {
                                                        Language.RU -> "Монеты"
                                                        Language.UA -> "Монети"
                                                        Language.KK -> "Монеталар"
                                                        Language.DE -> "Münzen"
                                                        Language.ZH -> "代币"
                                                        else -> "Credits"
                                                    }
                                                    Text(creditsLabel)
                                                },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(
                                                        value = editUserHighScore,
                                                        onValueChange = { editUserHighScore = it },
                                                        label = {
                                                        val highScoreLabel = when (currentLang) {
                                                            Language.RU -> "Рекорд"
                                                            Language.UA -> "Рекорд"
                                                            Language.KK -> "Рекорд"
                                                            Language.DE -> "Highscore"
                                                            Language.ZH -> "最高分"
                                                            else -> "High Score"
                                                        }
                                                        Text(highScoreLabel)
                                                    },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = editUserLines,
                                                        onValueChange = { editUserLines = it },
                                                        label = {
                                                        val linesLabel = when (currentLang) {
                                                            Language.RU -> "Линии"
                                                            Language.UA -> "Лінії"
                                                            Language.KK -> "Жолдар"
                                                            Language.DE -> "Linien"
                                                            Language.ZH -> "消除行数"
                                                            else -> "Lines"
                                                        }
                                                        Text(linesLabel)
                                                    },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                    OutlinedTextField(
                                                        value = editUserGames,
                                                        onValueChange = { editUserGames = it },
                                                        label = {
                                                        val gamesLabel = when (currentLang) {
                                                            Language.RU -> "Игры"
                                                            Language.UA -> "Ігри"
                                                            Language.KK -> "Ойындар"
                                                            Language.DE -> "Spiele"
                                                            Language.ZH -> "对局总数"
                                                            else -> "Games"
                                                        }
                                                        Text(gamesLabel)
                                                    },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    OutlinedTextField(
                                                        value = editUserXp,
                                                        onValueChange = { editUserXp = it },
                                                        label = {
                                                        val xpLabel = when (currentLang) {
                                                            Language.RU -> "Опыт"
                                                            Language.UA -> "Досвід"
                                                            Language.KK -> "Тәжірибе"
                                                            Language.DE -> "Bonus-XP"
                                                            Language.ZH -> "经验值"
                                                            else -> "Bonus XP"
                                                        }
                                                        Text(xpLabel)
                                                    },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                                OutlinedTextField(
                                                    value = editUserTier,
                                                    onValueChange = { editUserTier = it },
                                                    label = {
                                                    val tierLabel = when (currentLang) {
                                                        Language.RU -> "Ранг (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER)"
                                                        Language.UA -> "Ранг (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER)"
                                                        Language.KK -> "Дәреже (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER)"
                                                        Language.DE -> "Rang (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER)"
                                                        Language.ZH -> "段位 (BRONZE, SILVER, GOLD, PLATINUM, DIAMOND, MASTER, GRANDMASTER)"
                                                        else -> "Tier"
                                                    }
                                                    Text(tierLabel)
                                                },
                                                    singleLine = true,
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val gradientNickLabel = when (currentLang) {
                                                        Language.RU -> "Градиент ника"
                                                        Language.UA -> "Градієнт ніка"
                                                        Language.KK -> "Бүркеншік ат градиенті"
                                                        Language.DE -> "Farbverlauf-Name"
                                                        Language.ZH -> "昵称渐变特效"
                                                        else -> "Gradient Nickname"
                                                    }
                                                    Text(gradientNickLabel, style = MaterialTheme.typography.bodyMedium)
                                                    Switch(checked = editUserGradient, onCheckedChange = { editUserGradient = it })
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    val customTagUnlockedLabel = when (currentLang) {
                                                        Language.RU -> "Личный Тег разблокирован"
                                                        Language.UA -> "Особистий Тег розблоковано"
                                                        Language.KK -> "Жеке тег ашылды"
                                                        Language.DE -> "Individueller Tag freigeschaltet"
                                                        Language.ZH -> "个性称号已解锁"
                                                        else -> "Custom Tag Unlocked"
                                                    }
                                                    Text(customTagUnlockedLabel, style = MaterialTheme.typography.bodyMedium)
                                                    Switch(checked = editUserTagUnlocked, onCheckedChange = { editUserTagUnlocked = it })
                                                }
                                                if (editUserTagUnlocked) {
                                                    OutlinedTextField(
                                                        value = editUserTag,
                                                        onValueChange = { editUserTag = it },
                                                        label = {
                                                        val customTagTextLabel = when (currentLang) {
                                                            Language.RU -> "Текст личного тега"
                                                            Language.UA -> "Текст особистого тега"
                                                            Language.KK -> "Жеке тег мәтіні"
                                                            Language.DE -> "Tag-Text"
                                                            Language.ZH -> "个性称号文本"
                                                            else -> "Custom Tag Text"
                                                        }
                                                        Text(customTagTextLabel)
                                                    },
                                                        singleLine = true,
                                                        shape = RoundedCornerShape(12.dp),
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (editUserEmailVerified) Icons.Default.CheckCircle else Icons.Default.MarkEmailUnread,
                                                            contentDescription = null,
                                                            tint = if (editUserEmailVerified) Color(0xFF00E676) else Color(0xFFFF9100),
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Text("Подтверждение почты (Verified)", style = MaterialTheme.typography.bodyMedium)
                                                    }
                                                    Switch(checked = editUserEmailVerified, onCheckedChange = { editUserEmailVerified = it })
                                                }

                                                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                                                // Quick action buttons for this user
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    OutlinedButton(
                                                        onClick = {
                                                            viewModel.adminGiveAllCosmetics(tName)
                                                            viewModel.triggerAudioFeedback("success")
                                                            adminToastMessage = "Косметика выдана игроку $tName"
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Text("Вся косметика", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                    OutlinedButton(
                                                        onClick = {
                                                            if (!tUid.startsWith("local_")) {
                                                                viewModel.adminWipeStatsFirebase(tUid)
                                                            } else {
                                                                viewModel.adminWipeStatsLocal(tName)
                                                            }
                                                            viewModel.triggerAudioFeedback("gameover")
                                                            adminToastMessage = "Статистика $tName сброшена"
                                                            editUserCredits = "750"
                                                            editUserHighScore = "0"
                                                            editUserLines = "0"
                                                            editUserGames = "0"
                                                            editUserXp = "0"
                                                            editUserTier = "BRONZE"
                                                            editUserGradient = false
                                                            editUserTagUnlocked = false
                                                            editUserTag = ""
                                                        },
                                                        modifier = Modifier.weight(1f),
                                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                                        shape = RoundedCornerShape(10.dp)
                                                    ) {
                                                        Text("Сброс статы", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    viewModel.adminSetEmailVerified(tUid, editUserEmailVerified)
                                                    viewModel.adminUpdateUserStats(
                                                        uid = tUid,
                                                        credits = editUserCredits.toIntOrNull() ?: 0,
                                                        highScore = editUserHighScore.toIntOrNull() ?: 0,
                                                        linesCleared = editUserLines.toIntOrNull() ?: 0,
                                                        gamesPlayed = editUserGames.toIntOrNull() ?: 0,
                                                        bonusXp = editUserXp.toIntOrNull() ?: 0,
                                                        onlineTier = editUserTier.trim().uppercase(),
                                                        hasGradient = editUserGradient,
                                                        customTagUnlocked = editUserTagUnlocked,
                                                        customTag = editUserTag.trim()
                                                    )
                                                    viewModel.triggerAudioFeedback("success")
                                                    adminToastMessage = "Данные $tName сохранены!"
                                                    editingAdminUser = null
                                                },
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                val saveUserBtnLabel = when (currentLang) {
                                                    Language.RU -> "СОХРАНИТЬ"
                                                    Language.UA -> "ЗБЕРЕГТИ"
                                                    Language.KK -> "САҚТАУ"
                                                    Language.DE -> "SPEICHERN"
                                                    Language.ZH -> "保存修改"
                                                    else -> "SAVE"
                                                }
                                                Text(saveUserBtnLabel, fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { editingAdminUser = null }) {
                                                val cancelBtnLabel = when (currentLang) {
                                                    Language.RU -> "Отмена"
                                                    Language.UA -> "Скасувати"
                                                    Language.KK -> "Бас тарту"
                                                    Language.DE -> "Abbrechen"
                                                    Language.ZH -> "取消"
                                                    else -> "Cancel"
                                                }
                                                Text(cancelBtnLabel)
                                            }
                                        }
                                    )
                                }

                                if (showWipeConfirmDialog) {
                                    AlertDialog(
                                        onDismissRequest = { showWipeConfirmDialog = false },
                                        title = { Text("Внимание: Полный вайп базы данных", fontWeight = FontWeight.Black) },
                                        text = { 
                                            Text(
                                                "Вы действительно хотите полностью очистить всю базу данных Firestore (пользователи кроме FsFq, рекорды, лобби, чаты) и локальные аккаунты?",
                                                style = MaterialTheme.typography.bodyMedium
                                            ) 
                                        },
                                        confirmButton = {
                                            Button(
                                                onClick = {
                                                    viewModel.adminFullDatabaseWipe()
                                                    viewModel.triggerAudioFeedback("gameover")
                                                    adminToastMessage = "Полный вайп базы данных выполнен!"
                                                    showWipeConfirmDialog = false
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("УДАЛИТЬ ВСЁ", fontWeight = FontWeight.Bold)
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showWipeConfirmDialog = false }) {
                                                Text("Отмена")
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                if (isWideScreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(0.45f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Text(
                                    text = "FLOWTESS",
                                    style = MaterialTheme.typography.displayMedium,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 6.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    textAlign = TextAlign.Center
                                )
                                if (viewModel.isCurrentUserAdmin()) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    FilledTonalIconButton(
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            if (!viewModel.isCurrentUserAdmin()) {
                                                viewModel.setAdminSessionAuthenticated(false)
                                            } else if (isAdminSessionAuthenticated) {
                                                showAdminPanelDialog = true
                                            } else {
                                                adminPassInput = ""
                                                adminPassError = false
                                                showAdminPassDialog = true
                                            }
                                        },
                                        modifier = Modifier.size(42.dp),
                                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = "Admin Console",
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                            }

                            // Coins Badge
                            Row(
                                modifier = Modifier
                                    .padding(bottom = 16.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.triggerAudioFeedback("click")
                                        viewModel.openRewardedAdDialog()
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MonetizationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = java.text.NumberFormat.getIntegerInstance().format(credits),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Watch Ad",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(0.55f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val classicBtnText = when (currentLang) {
                                Language.RU -> "КЛАССИЧЕСКИЙ РЕЖИМ"
                                Language.UA -> "КЛАСИЧНИЙ РЕЖИМ"
                                Language.KK -> "КЛАССИКАЛЫҚ РЕЖИМ"
                                Language.DE -> "KLASSISCHER MODUS"
                                Language.ZH -> "经典模式"
                                else -> "CLASSIC MODE"
                            }
                            val gameModesBtnText = when (currentLang) {
                                Language.RU -> "РЕЖИМЫ ИГРЫ"
                                Language.UA -> "РЕЖИМИ ГРИ"
                                Language.KK -> "ОЙЫН РЕЖИМДЕРІ"
                                Language.DE -> "SPIELMODI"
                                Language.ZH -> "游戏模式"
                                else -> "GAME MODES"
                            }
                            val multiplayerBtnText = when (currentLang) {
                                Language.RU -> "МУЛЬТИПЛЕЕР"
                                Language.UA -> "МУЛЬТИПЛЕЄР"
                                Language.KK -> "МУЛЬТИПЛЕЕР"
                                Language.DE -> "MEHRSPIELER"
                                Language.ZH -> "多人联机"
                                else -> "MULTIPLAYER"
                            }
                            val friendsBtnText = when (currentLang) {
                                Language.RU -> "ДРУЗЬЯ"
                                Language.UA -> "ДРУЗІ"
                                Language.KK -> "ДОСТАР"
                                Language.DE -> "FREUNDE"
                                Language.ZH -> "好友"
                                else -> "FRIENDS"
                            }

                            MenuButton(
                                text = classicBtnText,
                                iconType = "play",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = { onPlayMode(com.example.game.GameMode.CLASSIC) }
                            )

                            MenuButton(
                                text = gameModesBtnText,
                                iconType = "upgrades",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = { onModeSelection() }
                            )

                            MenuButton(
                                text = Translations.get("leaderboard", currentLang),
                                iconType = "leaderboard",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = onLeaderboard
                            )

                            MenuButton(
                                text = multiplayerBtnText,
                                iconType = "multiplayer",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    if (playerName == "Player 1") {
                                        viewModel.triggerAudioFeedback("error")
                                        showAuthGuardDialog = true
                                    } else {
                                        viewModel.triggerAudioFeedback("click")
                                        onMultiplayer()
                                    }
                                }
                            )

                            MenuButton(
                                text = friendsBtnText,
                                iconType = "friends",
                                themeColor = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    viewModel.setShowFriendsDialog(true)
                                }
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
                        ) {
                            Text(
                                text = "FLOWTESS",
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 6.sp,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            if (viewModel.isCurrentUserAdmin()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                FilledTonalIconButton(
                                    onClick = {
                                        viewModel.triggerAudioFeedback("click")
                                        if (!viewModel.isCurrentUserAdmin()) {
                                            viewModel.setAdminSessionAuthenticated(false)
                                        } else if (isAdminSessionAuthenticated) {
                                            showAdminPanelDialog = true
                                        } else {
                                            adminPassInput = ""
                                            adminPassError = false
                                            showAdminPassDialog = true
                                        }
                                    },
                                    modifier = Modifier.size(38.dp),
                                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = "Admin Console",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Coins Chip
                        Row(
                            modifier = Modifier
                                .padding(bottom = 16.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.triggerAudioFeedback("click")
                                    viewModel.openRewardedAdDialog()
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = java.text.NumberFormat.getIntegerInstance().format(credits),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Watch Ad",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        val classicBtnText = when (currentLang) {
                            Language.RU -> "КЛАССИЧЕСКИЙ РЕЖИМ"
                            Language.UA -> "КЛАСИЧНИЙ РЕЖИМ"
                            Language.KK -> "КЛАССИКАЛЫҚ РЕЖИМ"
                            Language.DE -> "KLASSISCHER MODUS"
                            Language.ZH -> "经典模式"
                            else -> "CLASSIC MODE"
                        }
                        val gameModesBtnText = when (currentLang) {
                            Language.RU -> "РЕЖИМЫ ИГРЫ"
                            Language.UA -> "РЕЖИМИ ГРИ"
                            Language.KK -> "ОЙЫН РЕЖИМДЕРІ"
                            Language.DE -> "SPIELMODI"
                            Language.ZH -> "游戏模式"
                            else -> "GAME MODES"
                        }
                        val multiplayerBtnText = when (currentLang) {
                            Language.RU -> "МУЛЬТИПЛЕЕР"
                            Language.UA -> "МУЛЬТИПЛЕЄР"
                            Language.KK -> "МУЛЬТИПЛЕЕР"
                            Language.DE -> "MEHRSPIELER"
                            Language.ZH -> "多人联机"
                            else -> "MULTIPLAYER"
                        }
                        val friendsBtnText = when (currentLang) {
                            Language.RU -> "ДРУЗЬЯ"
                            Language.UA -> "ДРУЗІ"
                            Language.KK -> "ДОСТАР"
                            Language.DE -> "FREUNDE"
                            Language.ZH -> "好友"
                            else -> "FRIENDS"
                        }

                        MenuButton(
                            text = classicBtnText,
                            iconType = "play",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = { onPlayMode(com.example.game.GameMode.CLASSIC) }
                        )

                        MenuButton(
                            text = gameModesBtnText,
                            iconType = "upgrades",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = { onModeSelection() }
                        )

                        MenuButton(text = Translations.get("leaderboard", currentLang), iconType = "leaderboard", themeColor = MaterialTheme.colorScheme.primary, onClick = onLeaderboard)

                        MenuButton(
                            text = multiplayerBtnText,
                            iconType = "multiplayer",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                if (playerName == "Player 1") {
                                    viewModel.triggerAudioFeedback("error")
                                    showAuthGuardDialog = true
                                } else {
                                    viewModel.triggerAudioFeedback("click")
                                    onMultiplayer()
                                }
                            }
                        )

                        MenuButton(
                            text = friendsBtnText,
                            iconType = "friends",
                            themeColor = MaterialTheme.colorScheme.primary,
                            onClick = {
                                viewModel.setShowFriendsDialog(true)
                            }
                        )

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }
            }
        }
    }
}
}

