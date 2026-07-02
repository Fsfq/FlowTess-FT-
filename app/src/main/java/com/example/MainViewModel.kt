package com.example

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.db.AppDatabase
import com.example.db.HighScore
import com.example.db.ScoreRepository
import com.example.game.GameEngine
import com.example.game.GameMode
import com.example.game.Position
import com.example.game.BlockBlastEngine
import com.example.ui.Language
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.example.db.FirebaseLobbyManager

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "tetris-db"
    ).fallbackToDestructiveMigration(false).build()

    private val scoreRepo = ScoreRepository(db.highScoreDao())
    val topScores = scoreRepo.topScores

    private val accountRepo = com.example.db.AccountRepository(db.userAccountDao())
    val allAccounts = accountRepo.allAccounts

    val gameEngine = GameEngine()
    val blockBlastEngine = BlockBlastEngine()
    val lobbyManager = FirebaseLobbyManager(viewModelScope)

    private var soundPool: android.media.SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    private var lobbyMusicPlayer: android.media.MediaPlayer? = null

    // Relax/Sandbox Mode settings
    private val _relaxImmortal = MutableStateFlow(true)
    val relaxImmortal = _relaxImmortal.asStateFlow()

    private val _relaxSpeed = MutableStateFlow("slow")
    val relaxSpeed = _relaxSpeed.asStateFlow()

    private val _relaxBlockSet = MutableStateFlow("ideal")
    val relaxBlockSet = _relaxBlockSet.asStateFlow()

    private val _relaxGhostEnabled = MutableStateFlow(true)
    val relaxGhostEnabled = _relaxGhostEnabled.asStateFlow()

    // Lobby music separate toggle
    private val _lobbyMusicEnabled = MutableStateFlow(true)
    val lobbyMusicEnabled = _lobbyMusicEnabled.asStateFlow()

    // Custom Tag customization
    private val _customTag = MutableStateFlow("")
    val customTag = _customTag.asStateFlow()

    private val _customTagUnlocked = MutableStateFlow(false)
    val customTagUnlocked = _customTagUnlocked.asStateFlow()

    // Sound and music volume levels
    private val _soundVolume = MutableStateFlow(1.0f)
    val soundVolume = _soundVolume.asStateFlow()

    private val _lobbyMusicVolume = MutableStateFlow(0.8f)
    val lobbyMusicVolume = _lobbyMusicVolume.asStateFlow()

    private var gameLoopJob: Job? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _playerName = MutableStateFlow("Player 1")
    val playerName = _playerName.asStateFlow()

    private val prefs = application.getSharedPreferences("tetris_prefs", Context.MODE_PRIVATE)
    private val profilePrefs = application.getSharedPreferences("block_tetris_prefs", Context.MODE_PRIVATE)

    // Customization variables
    private val _equippedAvatarFrame = MutableStateFlow("standard")
    val equippedAvatarFrame = _equippedAvatarFrame.asStateFlow()

    private val _purchasedAvatarFrames = MutableStateFlow(setOf("standard"))
    val purchasedAvatarFrames = _purchasedAvatarFrames.asStateFlow()

    private val _equippedTitle = MutableStateFlow("none")
    val equippedTitle = _equippedTitle.asStateFlow()

    private val _purchasedTitles = MutableStateFlow(setOf("none"))
    val purchasedTitles = _purchasedTitles.asStateFlow()

    private val _equippedSoundPack = MutableStateFlow("arcade")
    val equippedSoundPack = _equippedSoundPack.asStateFlow()

    private val _purchasedSoundPacks = MutableStateFlow(setOf("arcade"))
    val purchasedSoundPacks = _purchasedSoundPacks.asStateFlow()

    private val _purchasedThemes = MutableStateFlow(setOf("indigo", "neon", "red"))
    val purchasedThemes = _purchasedThemes.asStateFlow()

    private val _purchasedFonts = MutableStateFlow(setOf("default", "monospace"))
    val purchasedFonts = _purchasedFonts.asStateFlow()

    private val _purchasedControlButtonStyles = MutableStateFlow(setOf("classic", "neon"))
    val purchasedControlButtonStyles = _purchasedControlButtonStyles.asStateFlow()

    private val _customAvatarEmoji = MutableStateFlow("")
    val customAvatarEmoji = _customAvatarEmoji.asStateFlow()

    private val _customAvatarBgColor = MutableStateFlow("3A3C44")
    val customAvatarBgColor = _customAvatarBgColor.asStateFlow()

    private val _language = MutableStateFlow(Language.EN)
    val language = _language.asStateFlow()

    private val _hasSavedGame = MutableStateFlow(false)
    val hasSavedGame = _hasSavedGame.asStateFlow()

    private val _themeColor = MutableStateFlow("indigo")
    val themeColor = _themeColor.asStateFlow()

    private val _blockStyle = MutableStateFlow("glass")
    val blockStyle = _blockStyle.asStateFlow()

    private val _nextCount = MutableStateFlow(3)
    val nextCount = _nextCount.asStateFlow()

    private val _ghostVisible = MutableStateFlow(true)
    val ghostVisible = _ghostVisible.asStateFlow()

    private val _controlStyle = MutableStateFlow("split")
    val controlStyle = _controlStyle.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled = _vibrationEnabled.asStateFlow()

    private val _onlineTier = MutableStateFlow("BRONZE")
    val onlineTier = _onlineTier.asStateFlow()

    private val _credits = MutableStateFlow(750)
    val credits = _credits.asStateFlow()

    private val _smoothFallingEnabled = MutableStateFlow(true)
    val smoothFallingEnabled = _smoothFallingEnabled.asStateFlow()

    private val _gridOpacity = MutableStateFlow(0.6f)
    val gridOpacity = _gridOpacity.asStateFlow()

    private val _customStartLevel = MutableStateFlow(1)
    val customStartLevel = _customStartLevel.asStateFlow()

    private val _lineClearChallenge = MutableStateFlow(false)
    val lineClearChallenge = _lineClearChallenge.asStateFlow()

    private val _autoSaveHighscore = MutableStateFlow(true)
    val autoSaveHighscore = _autoSaveHighscore.asStateFlow()

    private val _fastDropLockSpeed = MutableStateFlow(false)
    val fastDropLockSpeed = _fastDropLockSpeed.asStateFlow()

    private val _leftHandedControls = MutableStateFlow(false)
    val leftHandedControls = _leftHandedControls.asStateFlow()

    private val _gameSpeedMultiplier = MutableStateFlow(1.0f)
    val gameSpeedMultiplier = _gameSpeedMultiplier.asStateFlow()

    private val _controlButtonScale = MutableStateFlow(1.0f)
    val controlButtonScale = _controlButtonScale.asStateFlow()

    private val _controlButtonAlpha = MutableStateFlow(1.0f)
    val controlButtonAlpha = _controlButtonAlpha.asStateFlow()

    private val _controlButtonStyle = MutableStateFlow("neon")
    val controlButtonStyle = _controlButtonStyle.asStateFlow()

    private val _customFontKey = MutableStateFlow("default")
    val customFontKey = _customFontKey.asStateFlow()

    private val _gridLineDensity = MutableStateFlow("standard")
    val gridLineDensity = _gridLineDensity.asStateFlow()

    private val _controlVerticalPosition = MutableStateFlow("bottom")
    val controlVerticalPosition = _controlVerticalPosition.asStateFlow()

    private val _screenShakeIntensity = MutableStateFlow(1.0f)
    val screenShakeIntensity = _screenShakeIntensity.asStateFlow()

    private val _scanlinesFilter = MutableStateFlow(false)
    val scanlinesFilter = _scanlinesFilter.asStateFlow()

    private val _boardColorSkin = MutableStateFlow("cyberpunk")
    val boardColorSkin = _boardColorSkin.asStateFlow()

    private val _statsClearedLines = MutableStateFlow(0)
    val statsClearedLines = _statsClearedLines.asStateFlow()

    private val _statsHighScore = MutableStateFlow(0)
    val statsHighScore = _statsHighScore.asStateFlow()

    private val _achievementsList = MutableStateFlow<List<com.example.ui.Achievement>>(emptyList())
    val achievementsList = _achievementsList.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError = _loginError.asStateFlow()

    private val _loginSuccessMessage = MutableStateFlow<String?>(null)
    val loginSuccessMessage = _loginSuccessMessage.asStateFlow()

    private val _nicknameUpdateError = MutableStateFlow<String?>(null)
    val nicknameUpdateError = _nicknameUpdateError.asStateFlow()

    private val _nicknameUpdateSuccess = MutableStateFlow<String?>(null)
    val nicknameUpdateSuccess = _nicknameUpdateSuccess.asStateFlow()

    private val _emailUpdateError = MutableStateFlow<String?>(null)
    val emailUpdateError = _emailUpdateError.asStateFlow()

    private val _emailUpdateSuccess = MutableStateFlow<String?>(null)
    val emailUpdateSuccess = _emailUpdateSuccess.asStateFlow()

    private val _isAuthLoading = MutableStateFlow(false)
    val isAuthLoading = _isAuthLoading.asStateFlow()

    private val _globalScores = MutableStateFlow<List<HighScore>>(emptyList())
    val globalScores = _globalScores.asStateFlow()

    private val _firebaseUsers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val firebaseUsers = _firebaseUsers.asStateFlow()

    private val _hasNicknameGradient = MutableStateFlow(false)
    val hasNicknameGradient = _hasNicknameGradient.asStateFlow()

    private val _bonusXp = MutableStateFlow(0)
    val bonusXp = _bonusXp.asStateFlow()


    private class AchievementDef(
        val id: String,
        val titleEn: String,
        val titleRu: String,
        val descEn: String,
        val descRu: String,
        val target: Int,
        val icon: String,
        val reward: Int,
        val getCurrentVal: (MainViewModel) -> Int
    )

    private val achievementDefs = listOf(
        AchievementDef("classic_novice", "Lines Master", "Мастер линий", "Clear 10 or more total lines in Classic Match mode", "Уберите 10 или более линий в классическом режиме", 10, "lines", 100) { vm -> vm.prefs.getInt("stats_cleared_lines", 0) },
        AchievementDef("score_tycoon", "Sizable Score", "Финансовый магнат", "Score 5,000 points or more in a single Tetris match", "Наберите 5000 или более очков в одном матче", 5000, "score", 150) { vm -> vm.prefs.getInt("stats_high_score", 0) },
        AchievementDef("extended_pioneer", "Pentamino Integrator", "Пионер Пентамино", "Launch a match in Extended Shapes mode to embrace 5-element blocks", "Начните хотя бы одну игру в расширенном режиме", 1, "crown", 75) { vm -> if (vm.prefs.getBoolean("ach_extended_pioneer_unlocked", false)) 1 else 0 },
        AchievementDef("speed_runner", "Hyper-Speed", "Гиперскорость", "Survive a match under extreme starting speed in Hyper Blast mode", "Начните хотя бы один матч на уровне 10 в гипер-режиме", 1, "speed", 120) { vm -> if (vm.prefs.getBoolean("ach_speed_runner_unlocked", false)) 1 else 0 },
        AchievementDef("blast_tactician", "ZETA Expert", "Эксперт ZETA", "Earn 1,000 score points in ZETA mode", "Наберите 1000 очков в режиме ZETA", 1000, "blast", 200) { vm -> vm.prefs.getInt("block_blast_high_score", 0) },
        AchievementDef("combo_king", "Combo 3x", "Комбо 3x", "Achieve a combo chain multiplier of 3x or higher in multiplayer simulator", "Достигните комбо-множителя 3x или выше в мультиплеере", 3, "combo", 180) { vm -> if (vm.prefs.getBoolean("ach_combo_king_unlocked", false)) 1 else 0 },
        AchievementDef("grandmaster", "Grandmaster Tactician", "Гроссмейстер", "Score 15,000 points or more in Classic Match", "Наберите 15000 или более очков в классическом матче", 15000, "crown", 500) { vm -> vm.prefs.getInt("stats_high_score", 0) },
        AchievementDef("blast_master", "ZETA Veteran", "Ветеран ZETA", "Earn 5,000 score points in ZETA mode", "Наберите 5000 очков в режиме ZETA", 5000, "blast", 300) { vm -> vm.prefs.getInt("block_blast_high_score", 0) },
        AchievementDef("rich_player", "Elite Investor", "Элитный инвестор", "Save 2,000 credits in your balance", "Накопите не менее 2000 кредитов на балансе", 2000, "crown", 250) { vm -> vm.credits.value },
        AchievementDef("multiplayer_veteran", "Lobby Veteran", "Ветеран Лобби", "Initiate connection in PeerJS lobby 5 times", "Запустите подключение в лобби PeerJS не менее 5 раз", 5, "combo", 150) { vm -> vm.prefs.getInt("multiplayer_launches", 0) },
        AchievementDef("color_skin_collector", "Theme Collector", "Новая Тема", "Purchase your first custom board visual theme skin", "Приобретите свою первую уникальную тему оформления", 1, "crown", 200) { vm -> if (vm.prefs.getBoolean("ach_color_skin_collector_unlocked", false)) 1 else 0 },
        AchievementDef("rank_conqueror", "New Rank", "Новый Ранг", "Upgrade your security node rank tier using credits", "Повысьте категорию своего узла за кредиты", 1, "crown", 350) { vm -> if (vm.prefs.getBoolean("ach_rank_conqueror_unlocked", false)) 1 else 0 },
        
        // 37 new ones
        AchievementDef("games_played_5", "Novice Player", "Начинающий игрок", "Play 5 games total in any mode", "Сыграйте 5 игр в любом режиме", 5, "lines", 100) { vm -> vm.prefs.getInt("stats_games_played", 0) },
        AchievementDef("games_played_25", "Experienced Player", "Опытный игрок", "Play 25 games total in any mode", "Сыграйте 25 игр в любом режиме", 25, "lines", 150) { vm -> vm.prefs.getInt("stats_games_played", 0) },
        AchievementDef("games_played_100", "Tetris Legend", "Легенда Тетриса", "Play 100 games total in any mode", "Сыграйте 100 игр в любом режиме", 100, "lines", 300) { vm -> vm.prefs.getInt("stats_games_played", 0) },
        AchievementDef("lines_50", "Line Sweeper", "Очиститель линий", "Clear 50 total lines across all matches", "Уберите 50 линий суммарно во всех играх", 50, "lines", 120) { vm -> vm.prefs.getInt("stats_cleared_lines", 0) },
        AchievementDef("lines_200", "Line Shredder", "Уничтожитель линий", "Clear 200 total lines across all matches", "Уберите 200 линий суммарно во всех играх", 200, "lines", 200) { vm -> vm.prefs.getInt("stats_cleared_lines", 0) },
        AchievementDef("lines_1000", "Vortex Sweeper", "Вихревой очиститель", "Clear 1000 total lines across all matches", "Уберите 1000 линий суммарно во всех играх", 1000, "lines", 500) { vm -> vm.prefs.getInt("stats_cleared_lines", 0) },
        AchievementDef("score_single_8000", "Point Collector", "Сборщик очков", "Score 8,000 points or more in a single match", "Наберите 8000 или более очков в одном матче", 8000, "score", 180) { vm -> vm.prefs.getInt("stats_high_score", 0) },
        AchievementDef("score_single_20000", "Score Master", "Мастер очков", "Score 20,000 points or more in a single match", "Наберите 20000 или более очков в одном матче", 20000, "score", 300) { vm -> vm.prefs.getInt("stats_high_score", 0) },
        AchievementDef("score_single_50000", "Score Overlord", "Повелитель очков", "Score 50,000 points or more in a single match", "Наберите 50000 или более очков в одном матче", 50000, "score", 600) { vm -> vm.prefs.getInt("stats_high_score", 0) },
        AchievementDef("credits_accumulated_5000", "Credits Saver", "Сбережения", "Reach a balance of 5,000 credits", "Накопите баланс в 5000 кредитов", 5000, "crown", 300) { vm -> vm.credits.value },
        AchievementDef("credits_accumulated_10000", "Credits Capitalist", "Крупный капитал", "Reach a balance of 10,000 credits", "Накопите баланс в 10000 кредитов", 10000, "crown", 500) { vm -> vm.credits.value },
        AchievementDef("credits_spent_1000", "Shop Spender", "Покупатель", "Spend 1,000 credits in the store", "Потратьте 1000 кредитов в магазине", 1000, "crown", 100) { vm -> vm.prefs.getInt("stats_spent_credits", 0) },
        AchievementDef("credits_spent_5000", "Store Spender", "Активный покупатель", "Spend 5,000 credits in the store", "Потратьте 5000 кредитов в магазине", 5000, "crown", 250) { vm -> vm.prefs.getInt("stats_spent_credits", 0) },
        AchievementDef("credits_spent_10000", "VIP Customer", "Постоянный клиент", "Spend 10,000 credits in the store", "Потратьте 10000 кредитов в магазине", 10000, "crown", 500) { vm -> vm.prefs.getInt("stats_spent_credits", 0) },
        AchievementDef("tetrises_cleared_5", "Tetris Enthusiast", "Энтузиаст Тетрисов", "Perform 4-line clears (Tetrises) 5 times", "Выполните очистку 4-х линий (Тетрис) 5 раз", 5, "lines", 150) { vm -> vm.prefs.getInt("stats_tetrises_count", 0) },
        AchievementDef("tetrises_cleared_25", "Tetris Champion", "Чемпион Тетрисов", "Perform 4-line clears (Tetrises) 25 times", "Выполните очистку 4-х линий (Тетрис) 25 раз", 25, "lines", 300) { vm -> vm.prefs.getInt("stats_tetrises_count", 0) },
        AchievementDef("tetrises_cleared_100", "Tetris Master", "Мастер Тетрисов", "Perform 4-line clears (Tetrises) 100 times", "Выполните очистку 4-х линий (Тетрис) 100 раз", 100, "lines", 600) { vm -> vm.prefs.getInt("stats_tetrises_count", 0) },
        AchievementDef("blast_score_3000", "ZETA Professional", "Профессионал ZETA", "Reach 3,000 score in ZETA mode", "Наберите 3000 очков в режиме ZETA", 3000, "blast", 220) { vm -> vm.prefs.getInt("block_blast_high_score", 0) },
        AchievementDef("blast_score_10000", "ZETA Master", "Мастер ZETA", "Reach 10,000 score in ZETA mode", "Наберите 10000 очков в режиме ZETA", 10000, "blast", 400) { vm -> vm.prefs.getInt("block_blast_high_score", 0) },
        AchievementDef("combo_multiplier_4", "Combo 4x", "Комбо 4x", "Achieve a combo chain multiplier of 4x in multiplayer simulator", "Достигните комбо-множителя 4x в мультиплеере", 4, "combo", 220) { vm -> if (vm.prefs.getBoolean("ach_combo_multiplier_4_unlocked", false)) 1 else 0 },
        AchievementDef("combo_multiplier_5", "Combo 5x", "Комбо 5x", "Achieve a combo chain multiplier of 5x in multiplayer simulator", "Достигните комбо-множителя 5x в мультиплеере", 5, "combo", 350) { vm -> if (vm.prefs.getBoolean("ach_combo_multiplier_5_unlocked", false)) 1 else 0 },
        AchievementDef("avatar_changes_5", "Fashion Stylist", "Модный стилист", "Change your avatar emoji or color 5 times", "Измените эмодзи или цвет аватара 5 раз", 5, "crown", 100) { vm -> vm.prefs.getInt("stats_avatar_changes", 0) },
        AchievementDef("title_purchases_3", "Title Collector", "Коллекционер титулов", "Purchase 3 different profile titles in store", "Приобретите 3 разных титула в магазине", 3, "crown", 150) { vm -> vm.purchasedTitles.value.size },
        AchievementDef("frame_purchases_3", "Frame Collector", "Коллекционер рамок", "Purchase 3 different avatar frames in store", "Приобретите 3 разных рамки в магазине", 3, "crown", 150) { vm -> vm.purchasedAvatarFrames.value.size },
        AchievementDef("sound_pack_purchased", "Audio Collector", "Коллекционер звуков", "Purchase any custom sound synthesizer pack in store", "Приобретите любой синтезатор звука в магазине", 1, "speed", 150) { vm -> vm.purchasedSoundPacks.value.filter { it != "arcade" && it != "default" }.size },
        AchievementDef("xp_earned_500", "Experience 500", "Опыт 500", "Earn 500 total Experience Points (XP)", "Наберите 500 очков опыта (XP) суммарно", 500, "speed", 100) { vm -> (vm.prefs.getInt("stats_cleared_lines", 0) * 25) + (vm.prefs.getInt("stats_high_score", 0) / 10) + vm.bonusXp.value },
        AchievementDef("xp_earned_2000", "Experience 2000", "Опыт 2000", "Earn 2,000 total Experience Points (XP)", "Наберите 2000 очков опыта (XP) суммарно", 2000, "speed", 200) { vm -> (vm.prefs.getInt("stats_cleared_lines", 0) * 25) + (vm.prefs.getInt("stats_high_score", 0) / 10) + vm.bonusXp.value },
        AchievementDef("xp_earned_10000", "Experience 10000", "Опыт 10000", "Earn 10,000 total Experience Points (XP)", "Наберите 10000 очков опыта (XP) суммарно", 10000, "speed", 500) { vm -> (vm.prefs.getInt("stats_cleared_lines", 0) * 25) + (vm.prefs.getInt("stats_high_score", 0) / 10) + vm.bonusXp.value },
        AchievementDef("player_level_5", "Level 5", "Уровень 5", "Reach Player Mastery Level 5", "Достигните 5-го уровня мастерства", 5, "speed", 100) { vm -> ((vm.prefs.getInt("stats_cleared_lines", 0) * 25) + (vm.prefs.getInt("stats_high_score", 0) / 10) + vm.bonusXp.value) / 500 + 1 },
        AchievementDef("player_level_15", "Level 15", "Уровень 15", "Reach Player Mastery Level 15", "Достигните 15-го уровня мастерства", 15, "speed", 250) { vm -> ((vm.prefs.getInt("stats_cleared_lines", 0) * 25) + (vm.prefs.getInt("stats_high_score", 0) / 10) + vm.bonusXp.value) / 500 + 1 },
        AchievementDef("player_level_30", "Level 30", "Уровень 30", "Reach Player Mastery Level 30", "Достигните 30-го уровня мастерства", 30, "speed", 500) { vm -> ((vm.prefs.getInt("stats_cleared_lines", 0) * 25) + (vm.prefs.getInt("stats_high_score", 0) / 10) + vm.bonusXp.value) / 500 + 1 },
        AchievementDef("mode_time_attack", "Time Attack Mode", "Режим Тайм-Атак", "Launch a game in Time Attack mode", "Начните игру в режиме Тайм-Атак", 1, "speed", 100) { vm -> if (vm.prefs.getBoolean("ach_mode_time_attack_unlocked", false)) 1 else 0 },
        AchievementDef("mode_reverse", "Reverse Mode", "Режим Реверс", "Launch a game in Chaos Controls mode", "Начните игру в режиме Хаос-Управление", 1, "speed", 100) { vm -> if (vm.prefs.getBoolean("ach_mode_reverse_unlocked", false)) 1 else 0 },
        AchievementDef("mode_mirror", "Mirror Mode", "Зеркальный режим", "Launch a game in Mirror Dimension mode", "Начните игру в режиме Зеркальный Мир", 1, "speed", 100) { vm -> if (vm.prefs.getBoolean("ach_mode_mirror_unlocked", false)) 1 else 0 },
        AchievementDef("mode_pentary", "Pentary Chaos Mode", "Режим Пента-Хаос", "Launch a game in Pentary Chaos mode", "Начните игру в режиме Пента-Хаос", 1, "speed", 100) { vm -> if (vm.prefs.getBoolean("ach_mode_pentary_unlocked", false)) 1 else 0 },
        AchievementDef("mode_pulse", "Vortex Mode", "Вихревой режим", "Launch a game in Vortex Pulse mode", "Начните игру в режиме Импульсный Вихрь", 1, "speed", 100) { vm -> if (vm.prefs.getBoolean("ach_mode_pulse_unlocked", false)) 1 else 0 },
        AchievementDef("speed_level_max", "Speed Master", "Мастер скорости", "Reach game level speed 15 in standard match modes", "Достигните 15-го игрового уровня скорости в матче", 15, "speed", 250) { vm -> vm.prefs.getInt("stats_max_speed_reached", 0) },
        
        // Final epic 50th achievement
        AchievementDef("all_unlocked", "Champion", "Абсолютный чемпион", "Obtain all 49 other achievements (Epic Completion Reward)", "Откройте все 49 других достижений (Эпическая финальная награда)", 1, "crown", 1000) { vm -> if (vm.prefs.getBoolean("ach_all_unlocked_unlocked", false)) 1 else 0 }
    )

    init {
        val savedLangCode = prefs.getString("lang_code", Language.EN.code) ?: Language.EN.code
        _language.update { Language.entries.firstOrNull { it.code == savedLangCode } ?: Language.EN }
        _playerName.update { prefs.getString("player_name", "Player 1") ?: "Player 1" }
        _hasSavedGame.update { prefs.getBoolean("has_saved_game", false) }
        _themeColor.update { prefs.getString("theme_color", "indigo") ?: "indigo" }
        _blockStyle.update { prefs.getString("block_style", "neon") ?: "neon" }
        _nextCount.update { prefs.getInt("next_count", 3) }
        _ghostVisible.update { prefs.getBoolean("ghost_visible", true) }
        _controlStyle.update { prefs.getString("control_style", "split") ?: "split" }
        _soundEnabled.update { prefs.getBoolean("sound_enabled", true) }
        _vibrationEnabled.update { prefs.getBoolean("vibration_enabled", true) }
        _credits.update { prefs.getInt("credits", 750) }
        _smoothFallingEnabled.update { prefs.getBoolean("smooth_falling_enabled", true) }
        _gridOpacity.update { prefs.getFloat("grid_opacity", 0.6f) }
        _customStartLevel.update { prefs.getInt("custom_start_level", 1) }
        _lineClearChallenge.update { prefs.getBoolean("line_clear_challenge", false) }
        _autoSaveHighscore.update { prefs.getBoolean("auto_save_highscore", true) }
        _fastDropLockSpeed.update { prefs.getBoolean("fast_drop_lock_speed", false) }
        _leftHandedControls.update { prefs.getBoolean("left_handed_controls", false) }
        _gameSpeedMultiplier.update { prefs.getFloat("game_speed_multiplier", 1.0f) }
        _controlButtonScale.update { prefs.getFloat("control_button_scale", 1.0f) }
        _controlButtonAlpha.update { prefs.getFloat("control_button_alpha", 1.0f) }
        _controlButtonStyle.update { prefs.getString("control_button_style", "neon") ?: "neon" }
        _customFontKey.update { prefs.getString("custom_font_key", "default") ?: "default" }
        _gridLineDensity.update { prefs.getString("grid_line_density", "standard") ?: "standard" }
        _controlVerticalPosition.update { prefs.getString("control_vertical_position", "bottom") ?: "bottom" }
        
        _screenShakeIntensity.update { prefs.getFloat("screen_shake_intensity", 1.0f) }
        _statsClearedLines.update { prefs.getInt("stats_cleared_lines", 0) }
        _statsHighScore.update { prefs.getInt("stats_high_score", 0) }
        _scanlinesFilter.update { prefs.getBoolean("scanlines_filter", false) }
        _boardColorSkin.update { prefs.getString("board_color_skin", "cyberpunk") ?: "cyberpunk" }

        // Relax settings
        _relaxImmortal.update { prefs.getBoolean("relax_immortal", true) }
        _relaxSpeed.update { prefs.getString("relax_speed", "slow") ?: "slow" }
        _relaxBlockSet.update { prefs.getString("relax_block_set", "ideal") ?: "ideal" }
        _relaxGhostEnabled.update { prefs.getBoolean("relax_ghost_enabled", true) }
        _lobbyMusicEnabled.update { prefs.getBoolean("lobby_music_enabled", true) }

        // Custom Tag settings
        _customTag.update { profilePrefs.getString("custom_tag", "") ?: "" }
        _customTagUnlocked.update { profilePrefs.getBoolean("custom_tag_unlocked", false) }

        // Volume settings
        _soundVolume.update { prefs.getFloat("sound_volume", 1.0f) }
        _lobbyMusicVolume.update { prefs.getFloat("lobby_music_volume", 0.8f) }

        // Load customization values
        _equippedAvatarFrame.update { profilePrefs.getString("equipped_avatar_frame", "standard") ?: "standard" }
        _purchasedAvatarFrames.update { profilePrefs.getStringSet("purchased_avatar_frames", setOf("standard")) ?: setOf("standard") }
        _equippedTitle.update { profilePrefs.getString("equipped_title", "none") ?: "none" }
        _purchasedTitles.update { profilePrefs.getStringSet("purchased_titles", setOf("none")) ?: setOf("none") }
        _equippedSoundPack.update { profilePrefs.getString("equipped_sound_pack", "arcade") ?: "arcade" }
        _purchasedSoundPacks.update { profilePrefs.getStringSet("purchased_sound_packs", setOf("arcade")) ?: setOf("arcade") }
        _purchasedThemes.update { profilePrefs.getStringSet("purchased_themes", setOf("indigo", "neon", "red")) ?: setOf("indigo", "neon", "red") }
        _purchasedFonts.update { profilePrefs.getStringSet("purchased_fonts", setOf("default", "monospace")) ?: setOf("default", "monospace") }
        _purchasedControlButtonStyles.update { profilePrefs.getStringSet("purchased_control_button_styles", setOf("classic", "neon")) ?: setOf("classic", "neon") }
        _customAvatarEmoji.update { profilePrefs.getString("custom_avatar_emoji", "") ?: "" }
        _customAvatarBgColor.update { profilePrefs.getString("custom_avatar_bg_color", "3A3C44") ?: "3A3C44" }
        _onlineTier.update { profilePrefs.getString("online_tier", "BRONZE") ?: "BRONZE" }
        _hasNicknameGradient.update { profilePrefs.getBoolean("has_nickname_gradient", false) }
        _bonusXp.update { profilePrefs.getInt("bonus_xp", 0) }

        loadAchievements()
        
        gameEngine.lineClearChallenge = _lineClearChallenge.value
        gameEngine.fastDropLockSpeed = _fastDropLockSpeed.value
        
        viewModelScope.launch {
            try {
                val adminExist = accountRepo.getAccount("FsFq")
                if (adminExist == null) {
                    accountRepo.insert(
                        com.example.db.UserAccount(
                            username = "FsFq",
                            password = "1111333322",
                            onlineTier = "PRO GOLD",
                            credits = 1000
                        )
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                try {
                    _isAuthLoading.value = true
                    val data = com.example.db.FirebaseSync.pullUserData(getApplication())
                    val resolvedUsername = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "FirebaseUser"
                    val resolvedCredits = (data?.get("credits") as? Long)?.toInt() ?: 750
                    val resolvedTier = (data?.get("online_tier") as? String) ?: "BRONZE"
                    val resolvedHasGradient = data?.get("has_nickname_gradient") as? Boolean ?: false
                    val resolvedBonusXp = (data?.get("bonus_xp") as? Long)?.toInt() ?: 0
                    val resolvedTag = (data?.get("custom_tag") as? String) ?: ""
                    val resolvedTagUnlocked = (data?.get("custom_tag_unlocked") as? Boolean) ?: false
                    _customTag.update { resolvedTag }
                    _customTagUnlocked.update { resolvedTagUnlocked }
                    switchAccount(resolvedUsername, resolvedTier, resolvedCredits, resolvedHasGradient, resolvedBonusXp)
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    _isAuthLoading.value = false
                }
            } else {
                saveCurrentProfileToDb()
            }
        }
        initSoundPool()
        viewModelScope.launch {
            isPlaying.collect { playing ->
                if (playing) {
                    stopLobbyMusic()
                } else {
                    startLobbyMusic()
                }
            }
        }
    }

    fun setSoundVolume(volume: Float) {
        _soundVolume.value = volume
        prefs.edit().putFloat("sound_volume", volume).apply()
    }

    fun setLobbyMusicVolume(volume: Float) {
        _lobbyMusicVolume.value = volume
        prefs.edit().putFloat("lobby_music_volume", volume).apply()
        lobbyMusicPlayer?.let {
            try {
                it.setVolume(volume, volume)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onAppResume() {
        if (!_isPlaying.value) {
            startLobbyMusic()
        }
    }

    fun onAppPause() {
        stopLobbyMusic()
        pauseGame() // Pause match if app minimized
    }

    fun resetSettingsToDefault() {
        // Reset local preferences
        prefs.edit()
            .putString("theme_color", "indigo")
            .putString("block_style", "glass")
            .putInt("next_count", 3)
            .putBoolean("ghost_visible", true)
            .putString("control_style", "buttons")
            .putBoolean("sound_enabled", true)
            .putBoolean("vibration_enabled", true)
            .putBoolean("smooth_falling", true)
            .putFloat("grid_opacity", 0.35f)
            .putInt("custom_start_level", 1)
            .putFloat("screen_shake_intensity", 1.0f)
            .putBoolean("scanlines_filter", false)
            .putBoolean("line_clear_challenge", false)
            .putBoolean("auto_save_highscore", true)
            .putBoolean("fast_drop_lock_speed", false)
            .putBoolean("left_handed_controls", false)
            .putFloat("game_speed_multiplier", 1.0f)
            .putFloat("control_button_scale", 1.0f)
            .putFloat("control_button_alpha", 1.0f)
            .putString("control_button_style", "neon")
            .putString("grid_line_density", "dashed")
            .putString("custom_font_key", "default")
            .putString("control_vertical_position", "bottom")
            .putFloat("sound_volume", 1.0f)
            .putFloat("lobby_music_volume", 0.8f)
            .putBoolean("lobby_music_enabled", true)
            .apply()

        // Update active StateFlows
        _themeColor.value = "indigo"
        _blockStyle.value = "glass"
        _nextCount.value = 3
        _ghostVisible.value = true
        _controlStyle.value = "buttons"
        _soundEnabled.value = true
        _vibrationEnabled.value = true
        _smoothFallingEnabled.value = true
        _gridOpacity.value = 0.35f
        _customStartLevel.value = 1
        _screenShakeIntensity.value = 1.0f
        _scanlinesFilter.value = false
        _lineClearChallenge.value = false
        _autoSaveHighscore.value = true
        _fastDropLockSpeed.value = false
        _leftHandedControls.value = false
        _gameSpeedMultiplier.value = 1.0f
        _controlButtonScale.value = 1.0f
        _controlButtonAlpha.value = 1.0f
        _controlButtonStyle.value = "neon"
        _gridLineDensity.value = "dashed"
        _customFontKey.value = "default"
        _controlVerticalPosition.value = "bottom"
        _soundVolume.value = 1.0f
        _lobbyMusicVolume.value = 0.8f
        _lobbyMusicEnabled.value = true

        // Sync changes to gameEngine
        gameEngine.lineClearChallenge = false
        gameEngine.fastDropLockSpeed = false

        // Restart/refresh music if enabled
        stopLobbyMusic()
        startLobbyMusic()
    }

    fun setRelaxImmortal(enabled: Boolean) {
        _relaxImmortal.value = enabled
        prefs.edit().putBoolean("relax_immortal", enabled).apply()
        // Sync to engine
        gameEngine.relaxImmortal = enabled
    }

    fun setRelaxSpeed(speed: String) {
        _relaxSpeed.value = speed
        prefs.edit().putString("relax_speed", speed).apply()
    }

    fun setRelaxBlockSet(blockSet: String) {
        _relaxBlockSet.value = blockSet
        prefs.edit().putString("relax_block_set", blockSet).apply()
        gameEngine.relaxBlockSet = blockSet
    }

    fun setRelaxGhostEnabled(enabled: Boolean) {
        _relaxGhostEnabled.value = enabled
        prefs.edit().putBoolean("relax_ghost_enabled", enabled).apply()
    }

    fun setLobbyMusicEnabled(enabled: Boolean) {
        _lobbyMusicEnabled.value = enabled
        prefs.edit().putBoolean("lobby_music_enabled", enabled).apply()
        if (enabled) {
            startLobbyMusic()
        } else {
            stopLobbyMusic()
        }
    }

    fun setCustomTag(tag: String) {
        _customTag.value = tag
        profilePrefs.edit().putString("custom_tag", tag).apply()
        saveCurrentProfileToDb()
    }

    fun setCustomTagUnlocked(unlocked: Boolean) {
        _customTagUnlocked.value = unlocked
        profilePrefs.edit().putBoolean("custom_tag_unlocked", unlocked).apply()
        saveCurrentProfileToDb()
    }

    fun clearRelaxBoard() {
        gameEngine.clearBoard()
    }

    fun setEquippedAvatarFrame(frameId: String) {
        _equippedAvatarFrame.value = frameId
        profilePrefs.edit().putString("equipped_avatar_frame", frameId).apply()
        saveCurrentProfileToDb()
    }

    fun setPurchasedAvatarFrames(frames: Set<String>) {
        _purchasedAvatarFrames.value = frames
        profilePrefs.edit().putStringSet("purchased_avatar_frames", frames).apply()
        saveCurrentProfileToDb()
    }

    fun setEquippedTitle(titleId: String) {
        _equippedTitle.value = titleId
        profilePrefs.edit().putString("equipped_title", titleId).apply()
        saveCurrentProfileToDb()
    }

    fun setPurchasedTitles(titles: Set<String>) {
        _purchasedTitles.value = titles
        profilePrefs.edit().putStringSet("purchased_titles", titles).apply()
        saveCurrentProfileToDb()
    }

    fun setEquippedSoundPack(packId: String) {
        _equippedSoundPack.value = packId
        profilePrefs.edit().putString("equipped_sound_pack", packId).apply()
        saveCurrentProfileToDb()
    }

    fun setPurchasedSoundPacks(packs: Set<String>) {
        _purchasedSoundPacks.value = packs
        profilePrefs.edit().putStringSet("purchased_sound_packs", packs).apply()
        saveCurrentProfileToDb()
    }

    fun setPurchasedThemes(themes: Set<String>) {
        _purchasedThemes.value = themes
        profilePrefs.edit().putStringSet("purchased_themes", themes).apply()
        saveCurrentProfileToDb()
    }

    fun setPurchasedFonts(fonts: Set<String>) {
        _purchasedFonts.value = fonts
        profilePrefs.edit().putStringSet("purchased_fonts", fonts).apply()
        saveCurrentProfileToDb()
    }

    fun setPurchasedControlButtonStyles(styles: Set<String>) {
        _purchasedControlButtonStyles.value = styles
        profilePrefs.edit().putStringSet("purchased_control_button_styles", styles).apply()
        saveCurrentProfileToDb()
    }

    fun setCustomAvatarEmoji(emoji: String) {
        _customAvatarEmoji.value = emoji
        profilePrefs.edit().putString("custom_avatar_emoji", emoji).apply()
        val changes = prefs.getInt("stats_avatar_changes", 0) + 1
        prefs.edit().putInt("stats_avatar_changes", changes).apply()
        evaluateAchievements()
        saveCurrentProfileToDb()
    }

    fun setCustomAvatarBgColor(hexColor: String) {
        _customAvatarBgColor.value = hexColor
        profilePrefs.edit().putString("custom_avatar_bg_color", hexColor).apply()
        val changes = prefs.getInt("stats_avatar_changes", 0) + 1
        prefs.edit().putInt("stats_avatar_changes", changes).apply()
        evaluateAchievements()
        saveCurrentProfileToDb()
    }

    fun setSmoothFallingEnabled(enabled: Boolean) {
        _smoothFallingEnabled.value = enabled
        prefs.edit().putBoolean("smooth_falling_enabled", enabled).apply()
    }


    fun setThemeColor(color: String) {
        _themeColor.value = color
        prefs.edit().putString("theme_color", color).apply()
    }

    fun setBlockStyle(style: String) {
        _blockStyle.value = style
        prefs.edit().putString("block_style", style).apply()
    }

    fun setNextCount(count: Int) {
        _nextCount.value = count
        prefs.edit().putInt("next_count", count).apply()
    }

    fun setGhostVisible(visible: Boolean) {
        _ghostVisible.value = visible
        prefs.edit().putBoolean("ghost_visible", visible).apply()
    }


    fun setControlStyle(style: String) {
        _controlStyle.value = style
        prefs.edit().putString("control_style", style).apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
        if (enabled) {
            startLobbyMusic()
        } else {
            stopLobbyMusic()
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        _vibrationEnabled.value = enabled
        prefs.edit().putBoolean("vibration_enabled", enabled).apply()
    }

    fun setOnlineTier(tier: String) {
        _onlineTier.value = tier
        prefs.edit().putString("online_tier", tier).apply()
        profilePrefs.edit().putString("online_tier", tier).apply()
        saveCurrentProfileToDb()
    }


    fun setGridOpacity(opacity: Float) {
        _gridOpacity.value = opacity
        prefs.edit().putFloat("grid_opacity", opacity).apply()
    }


    fun setCustomStartLevel(level: Int) {
        _customStartLevel.value = level
        prefs.edit().putInt("custom_start_level", level).apply()
    }

    fun setLineClearChallenge(enabled: Boolean) {
        _lineClearChallenge.value = enabled
        gameEngine.lineClearChallenge = enabled
        prefs.edit().putBoolean("line_clear_challenge", enabled).apply()
    }

    fun setAutoSaveHighscore(enabled: Boolean) {
        _autoSaveHighscore.value = enabled
        prefs.edit().putBoolean("auto_save_highscore", enabled).apply()
    }

    fun setFastDropLockSpeed(enabled: Boolean) {
        _fastDropLockSpeed.value = enabled
        gameEngine.fastDropLockSpeed = enabled
        prefs.edit().putBoolean("fast_drop_lock_speed", enabled).apply()
    }

    fun setLeftHandedControls(enabled: Boolean) {
        _leftHandedControls.value = enabled
        prefs.edit().putBoolean("left_handed_controls", enabled).apply()
    }

    fun setGameSpeedMultiplier(mult: Float) {
        _gameSpeedMultiplier.value = mult
        prefs.edit().putFloat("game_speed_multiplier", mult).apply()
    }

    fun setControlButtonScale(scale: Float) {
        _controlButtonScale.value = scale
        prefs.edit().putFloat("control_button_scale", scale).apply()
    }

    fun setControlButtonAlpha(alpha: Float) {
        _controlButtonAlpha.value = alpha
        prefs.edit().putFloat("control_button_alpha", alpha).apply()
    }

    fun setControlButtonStyle(style: String) {
        _controlButtonStyle.value = style
        prefs.edit().putString("control_button_style", style).apply()
    }

    fun setGridLineDensity(density: String) {
        _gridLineDensity.value = density
        prefs.edit().putString("grid_line_density", density).apply()
    }

    fun setControlVerticalPosition(pos: String) {
        _controlVerticalPosition.value = pos
        prefs.edit().putString("control_vertical_position", pos).apply()
    }

    fun setScreenShakeIntensity(v: Float) {
        _screenShakeIntensity.value = v
        prefs.edit().putFloat("screen_shake_intensity", v).apply()
    }

    fun setScanlinesFilter(enabled: Boolean) {
        _scanlinesFilter.value = enabled
        prefs.edit().putBoolean("scanlines_filter", enabled).apply()
    }

    fun setCustomFontKey(key: String) {
        _customFontKey.value = key
        prefs.edit().putString("custom_font_key", key).apply()
    }

    fun resetProfileStats() {
        prefs.edit()
            .putInt("stats_cleared_lines", 0)
            .putInt("stats_high_score", 0)
            .apply()
        _statsClearedLines.value = 0
        _statsHighScore.value = 0
    }

    fun setBoardColorSkin(skin: String) {
        _boardColorSkin.value = skin
        prefs.edit().putString("board_color_skin", skin).apply()
        saveCurrentProfileToDb()
    }

    fun loadAchievements() {
        val list = achievementDefs.map { def ->
            val isUnlocked = prefs.getBoolean("ach_${def.id}_unlocked", false)
            val curVal = if (isUnlocked) def.target else def.getCurrentVal(this)
            com.example.ui.Achievement(
                id = def.id,
                titleEn = def.titleEn,
                titleRu = def.titleRu,
                descriptionEn = def.descEn,
                descriptionRu = def.descRu,
                targetValue = def.target,
                iconType = def.icon,
                pointsReward = def.reward,
                isUnlocked = isUnlocked,
                currentValue = curVal
            )
        }

        // Evaluate all regular ones to unlock the epic bonus automatically
        val regulars = list.filter { it.id != "all_unlocked" }
        val allRegularUnlocked = regulars.all { it.isUnlocked }
        var allUnlocked = prefs.getBoolean("ach_all_unlocked_unlocked", false)
        
        if (allRegularUnlocked && !allUnlocked) {
            prefs.edit().putBoolean("ach_all_unlocked_unlocked", true).apply()
            allUnlocked = true
            viewModelScope.launch {
                addCredits(1000)
            }
        }

        val finalUnlockedList = list.map { ach ->
            if (ach.id == "all_unlocked") {
                ach.copy(isUnlocked = allUnlocked, currentValue = if (allUnlocked) 1 else 0)
            } else {
                ach
            }
        }

        _achievementsList.value = finalUnlockedList
    }

    fun evaluateAchievements() {
        for (def in achievementDefs) {
            if (def.id == "all_unlocked") continue
            val key = "ach_${def.id}_unlocked"
            if (!prefs.getBoolean(key, false)) {
                val currentVal = def.getCurrentVal(this)
                if (currentVal >= def.target) {
                    unlockAchievement(def.id, def.reward)
                }
            }
        }
        loadAchievements()
    }

    fun unlockAchievement(id: String, reward: Int) {
        val key = "ach_${id}_unlocked"
        if (!prefs.getBoolean(key, false)) {
            prefs.edit().putBoolean(key, true).apply()
            addCredits((reward * 1.5f).toInt())
            loadAchievements()
        }
    }

    fun checkMultiplayerCombo(combo: Int) {
        if (combo >= 3) {
            unlockAchievement("combo_king", 180)
        }
        if (combo >= 4) {
            prefs.edit().putBoolean("ach_combo_multiplier_4_unlocked", true).apply()
        }
        if (combo >= 5) {
            prefs.edit().putBoolean("ach_combo_multiplier_5_unlocked", true).apply()
        }
        evaluateAchievements()
    }

    fun onBlockBlastPlacement(score: Int) {
        if (score >= 1000) {
            unlockAchievement("blast_tactician", 200)
        }
        if (score >= 5000) {
            unlockAchievement("blast_master", 300)
        }
        evaluateAchievements()
    }

    fun addCredits(amount: Int) {
        _credits.update { it + amount }
        prefs.edit().putInt("credits", _credits.value).apply()
        saveCurrentProfileToDb()
        evaluateAchievements()
    }

    fun spendCredits(amount: Int): Boolean {
        if (_credits.value >= amount) {
            _credits.update { it - amount }
            prefs.edit().putInt("credits", _credits.value).apply()
            val totalSpent = prefs.getInt("stats_spent_credits", 0) + amount
            prefs.edit().putInt("stats_spent_credits", totalSpent).apply()
            saveCurrentProfileToDb()
            evaluateAchievements()
            return true
        }
        return false
    }

    fun setLanguage(lang: Language) {
        _language.update { lang }
        prefs.edit().putString("lang_code", lang.code).apply()
    }

    fun setPlayerName(name: String) {
        _playerName.update { name }
        prefs.edit().putString("player_name", name).apply()
        saveCurrentProfileToDb()
    }

    fun saveCurrentProfileToDb() {
        viewModelScope.launch {
            val pName = _playerName.value.trim()
            if (pName.isNotEmpty()) {
                val existing = accountRepo.getAccount(pName)
                val pass = existing?.password ?: "1234"
                val acc = com.example.db.UserAccount(
                    username = pName,
                    password = pass,
                    onlineTier = _onlineTier.value,
                    credits = _credits.value,
                    hasGradient = _hasNicknameGradient.value,
                    bonusXp = _bonusXp.value
                )
                accountRepo.insert(acc)
                // Sync data with Cloud Firestore
                com.example.db.FirebaseSync.pushUserData(getApplication())
            }
        }
    }

    fun clearLoginMessages() {
        _loginError.value = null
        _loginSuccessMessage.value = null
    }

    fun loginAccount(usernameEntered: String, passwordEntered: String) {
        viewModelScope.launch {
            clearLoginMessages()
            val trimmedName = usernameEntered.trim()
            val trimmedPass = passwordEntered.trim()
            if (trimmedName.isEmpty() || trimmedPass.isEmpty()) {
                _loginError.value = "Никнейм или пароль пустые!"
                return@launch
            }
            _isAuthLoading.value = true
            val email = if (trimmedName.contains("@")) {
                trimmedName
            } else {
                val hexName = trimmedName.toByteArray(Charsets.UTF_8).joinToString("") { "%02x".format(it) }
                "${hexName}@blocktetris.com"
            }
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, trimmedPass)
                .addOnSuccessListener { authResult ->
                    viewModelScope.launch {
                        val user = authResult.user
                        if (user != null) {
                            val data = com.example.db.FirebaseSync.pullUserData(getApplication())
                            val resolvedUsername = user.displayName ?: user.email?.substringBefore("@") ?: trimmedName
                            val resolvedCredits = (data?.get("credits") as? Long)?.toInt()
                                ?: if (_playerName.value == "Player 1") _credits.value else 750
                            val resolvedTier = (data?.get("online_tier") as? String)
                                ?: if (_playerName.value == "Player 1") _onlineTier.value else "BRONZE"
                            val resolvedHasGradient = data?.get("has_nickname_gradient") as? Boolean ?: false
                            val resolvedBonusXp = (data?.get("bonus_xp") as? Long)?.toInt() ?: 0
                            
                            val resolvedHighScore = (data?.get("stats_high_score") as? Long)?.toInt() ?: 0
                            if (resolvedHighScore > 0) {
                                scoreRepo.insert(HighScore(playerName = resolvedUsername, score = resolvedHighScore))
                            }
                            
                            switchAccount(resolvedUsername, resolvedTier, resolvedCredits, resolvedHasGradient, resolvedBonusXp)
                            _loginSuccessMessage.value = "Успешный вход!"
                        } else {
                            _loginError.value = "Ошибка авторизации!"
                        }
                        _isAuthLoading.value = false
                    }
                }
                .addOnFailureListener { e ->
                    _loginError.value = e.localizedMessage ?: "Неверные учетные данные!"
                    _isAuthLoading.value = false
                }
        }
    }

    fun clearUpdateMessages() {
        _nicknameUpdateError.value = null
        _nicknameUpdateSuccess.value = null
        _emailUpdateError.value = null
        _emailUpdateSuccess.value = null
    }

    fun registerAccount(usernameEntered: String, emailEntered: String, passwordEntered: String, initialTier: String? = null, initialCredits: Int? = null) {
        viewModelScope.launch {
            clearLoginMessages()
            val trimmedName = usernameEntered.trim()
            val trimmedEmail = emailEntered.trim()
            val trimmedPass = passwordEntered.trim()
            if (trimmedName.isEmpty()) {
                _loginError.value = "Никнейм пустой!"
                return@launch
            }
            if (trimmedEmail.isEmpty() || !trimmedEmail.contains("@")) {
                _loginError.value = "Почта обязательна и должна содержать @!"
                return@launch
            }
            if (trimmedPass.isEmpty()) {
                _loginError.value = "Пароль пустой!"
                return@launch
            }
            if (trimmedPass.length < 6) {
                _loginError.value = "Пароль должен быть не менее 6 символов!"
                return@launch
            }
            _isAuthLoading.value = true
            
            // Preserve guest progress: if active user is "Player 1", keep their stats!
            val finalCredits = initialCredits ?: if (_playerName.value == "Player 1") _credits.value else 750
            val finalTier = initialTier ?: if (_playerName.value == "Player 1") _onlineTier.value else "BRONZE"

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(trimmedEmail, trimmedPass)
                .addOnSuccessListener { authResult ->
                    viewModelScope.launch {
                        val user = authResult.user
                        if (user != null) {
                            val profileUpdates = userProfileChangeRequest {
                                displayName = trimmedName
                            }
                            user.updateProfile(profileUpdates)
                            
                            val acc = com.example.db.UserAccount(
                                username = trimmedName,
                                password = trimmedPass,
                                onlineTier = finalTier,
                                credits = finalCredits
                            )
                            accountRepo.insert(acc)
                            switchAccount(trimmedName, finalTier, finalCredits)
                            
                            // Initialize Firestore record
                            com.example.db.FirebaseSync.pushUserData(getApplication())
                            
                            _loginSuccessMessage.value = "Аккаунт успешно создан!"
                        } else {
                            _loginError.value = "Ошибка создания аккаунта!"
                        }
                        _isAuthLoading.value = false
                    }
                }
                .addOnFailureListener { e ->
                    _loginError.value = e.localizedMessage ?: "Ошибка при регистрации!"
                    _isAuthLoading.value = false
                }
        }
    }

    fun updateNickname(newNick: String) {
        val trimmed = newNick.trim()
        if (trimmed.isEmpty() || trimmed.length < 2 || trimmed.length > 20) {
            _nicknameUpdateError.value = "Никнейм должен быть от 2 до 20 символов!"
            return
        }
        viewModelScope.launch {
            _nicknameUpdateError.value = null
            _nicknameUpdateSuccess.value = null
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val profileUpdates = userProfileChangeRequest {
                    displayName = trimmed
                }
                user.updateProfile(profileUpdates)
                    .addOnSuccessListener {
                        viewModelScope.launch {
                            switchAccount(trimmed, _onlineTier.value, _credits.value, _hasNicknameGradient.value, _bonusXp.value)
                            com.example.db.FirebaseSync.pushUserData(getApplication())
                            _nicknameUpdateSuccess.value = "Никнейм успешно изменен!"
                        }
                    }
                    .addOnFailureListener { e ->
                        _nicknameUpdateError.value = e.localizedMessage ?: "Ошибка при изменении никнейма!"
                    }
            } else {
                switchAccount(trimmed, _onlineTier.value, _credits.value, _hasNicknameGradient.value, _bonusXp.value)
                _nicknameUpdateSuccess.value = "Никнейм успешно изменен!"
            }
        }
    }

    fun updateEmail(newEmail: String) {
        val trimmed = newEmail.trim()
        if (trimmed.isEmpty() || !trimmed.contains("@")) {
            _emailUpdateError.value = "Неверный формат почты!"
            return
        }
        viewModelScope.launch {
            _emailUpdateError.value = null
            _emailUpdateSuccess.value = null
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                user.updateEmail(trimmed)
                    .addOnSuccessListener {
                        _emailUpdateSuccess.value = "Почта успешно обновлена!"
                        viewModelScope.launch {
                            com.example.db.FirebaseSync.pushUserData(getApplication())
                        }
                    }
                    .addOnFailureListener { e ->
                        _emailUpdateError.value = e.localizedMessage ?: "Ошибка при обновлении почты!"
                    }
            } else {
                _emailUpdateError.value = "Войдите в аккаунт, чтобы изменить почту!"
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            clearLoginMessages()
            _isAuthLoading.value = true
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    viewModelScope.launch {
                        val user = authResult.user
                        if (user != null) {
                            val resolvedUsername = user.displayName ?: user.email?.substringBefore("@") ?: "GoogleUser"
                            val data = com.example.db.FirebaseSync.pullUserData(getApplication())
                            val resolvedCredits = (data?.get("credits") as? Long)?.toInt()
                                ?: if (_playerName.value == "Player 1") _credits.value else 750
                            val resolvedTier = (data?.get("online_tier") as? String)
                                ?: if (_playerName.value == "Player 1") _onlineTier.value else "BRONZE"
                            val resolvedHasGradient = data?.get("has_nickname_gradient") as? Boolean ?: false
                            val resolvedBonusXp = (data?.get("bonus_xp") as? Long)?.toInt() ?: 0
                            
                            val resolvedHighScore = (data?.get("stats_high_score") as? Long)?.toInt() ?: 0
                            if (resolvedHighScore > 0) {
                                scoreRepo.insert(HighScore(playerName = resolvedUsername, score = resolvedHighScore))
                            }
                            
                            switchAccount(resolvedUsername, resolvedTier, resolvedCredits, resolvedHasGradient, resolvedBonusXp)
                            
                            if (data == null) {
                                com.example.db.FirebaseSync.pushUserData(getApplication())
                            }
                            _loginSuccessMessage.value = "Успешный вход через Google!"
                        } else {
                            _loginError.value = "Ошибка входа через Google!"
                        }
                        _isAuthLoading.value = false
                    }
                }
                .addOnFailureListener { e ->
                    _loginError.value = e.localizedMessage ?: "Ошибка аутентификации Google!"
                    _isAuthLoading.value = false
                }
        }
    }

    fun switchAccount(username: String, tier: String, creditsAmount: Int, hasGradient: Boolean = false, bonusXpAmount: Int = 0) {
        _playerName.update { username }
        _onlineTier.update { tier }
        _credits.update { creditsAmount }
        _hasNicknameGradient.update { hasGradient }
        _bonusXp.update { bonusXpAmount }
        prefs.edit().putString("player_name", username).apply()
        prefs.edit().putString("online_tier", tier).apply()
        profilePrefs.edit().putString("online_tier", tier).apply()
        prefs.edit().putInt("credits", creditsAmount).apply()
        profilePrefs.edit().putBoolean("has_nickname_gradient", hasGradient).apply()
        profilePrefs.edit().putInt("bonus_xp", bonusXpAmount).apply()
        
        saveCurrentProfileToDb()
        reloadAllCustomizationsAndStats()
    }

    private fun resetPrefsToGuestDefaults(guestAcc: com.example.db.UserAccount?) {
        val editorProfile = profilePrefs.edit()
        val editorTetris = prefs.edit()
        
        editorTetris.putString("player_name", "Player 1")
        editorTetris.putInt("credits", guestAcc?.credits ?: 750)
        editorTetris.putString("online_tier", guestAcc?.onlineTier ?: "BRONZE")
        
        editorProfile.putString("online_tier", guestAcc?.onlineTier ?: "BRONZE")
        editorProfile.putBoolean("has_nickname_gradient", guestAcc?.hasGradient ?: false)
        editorProfile.putInt("bonus_xp", guestAcc?.bonusXp ?: 0)
        
        editorProfile.putString("equipped_avatar_frame", "standard")
        editorProfile.putStringSet("purchased_avatar_frames", setOf("standard"))
        editorProfile.putString("equipped_title", "none")
        editorProfile.putStringSet("purchased_titles", setOf("none"))
        editorProfile.putString("equipped_sound_pack", "arcade")
        editorProfile.putStringSet("purchased_sound_packs", setOf("arcade"))
        editorProfile.putStringSet("purchased_themes", setOf("indigo", "neon", "red"))
        editorProfile.putStringSet("purchased_fonts", setOf("default", "monospace"))
        editorProfile.putStringSet("purchased_control_button_styles", setOf("classic", "neon"))
        editorProfile.putString("custom_avatar_emoji", "")
        editorProfile.putString("custom_avatar_bg_color", "3A3C44")
        
        editorTetris.putInt("stats_games_played", 0)
        editorTetris.putInt("stats_spent_credits", 0)
        editorTetris.putInt("stats_cleared_lines", 0)
        editorTetris.putInt("stats_high_score", 0)
        editorTetris.putInt("stats_max_speed_reached", 0)
        editorTetris.putInt("stats_tetrises_count", 0)
        editorTetris.putInt("block_blast_high_score", 0)
        
        editorProfile.apply()
        editorTetris.apply()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val guestAcc = accountRepo.getAccount("Player 1")
            resetPrefsToGuestDefaults(guestAcc)
            reloadAllCustomizationsAndStats()
            clearLoginMessages()
        }
    }

    fun reloadAllCustomizationsAndStats() {
        _playerName.update { prefs.getString("player_name", "Player 1") ?: "Player 1" }
        _onlineTier.update { profilePrefs.getString("online_tier", "BRONZE") ?: "BRONZE" }
        _credits.update { prefs.getInt("credits", 750) }
        _hasNicknameGradient.update { profilePrefs.getBoolean("has_nickname_gradient", false) }
        _bonusXp.update { profilePrefs.getInt("bonus_xp", 0) }
        
        _equippedAvatarFrame.update { profilePrefs.getString("equipped_avatar_frame", "standard") ?: "standard" }
        _purchasedAvatarFrames.update { profilePrefs.getStringSet("purchased_avatar_frames", setOf("standard")) ?: setOf("standard") }
        _equippedTitle.update { profilePrefs.getString("equipped_title", "none") ?: "none" }
        _purchasedTitles.update { profilePrefs.getStringSet("purchased_titles", setOf("none")) ?: setOf("none") }
        _equippedSoundPack.update { profilePrefs.getString("equipped_sound_pack", "arcade") ?: "arcade" }
        _purchasedSoundPacks.update { profilePrefs.getStringSet("purchased_sound_packs", setOf("arcade")) ?: setOf("arcade") }
        _purchasedThemes.update { profilePrefs.getStringSet("purchased_themes", setOf("indigo", "neon", "red")) ?: setOf("indigo", "neon", "red") }
        _purchasedFonts.update { profilePrefs.getStringSet("purchased_fonts", setOf("default", "monospace")) ?: setOf("default", "monospace") }
        _purchasedControlButtonStyles.update { profilePrefs.getStringSet("purchased_control_button_styles", setOf("classic", "neon")) ?: setOf("classic", "neon") }
        _customAvatarEmoji.update { profilePrefs.getString("custom_avatar_emoji", "") ?: "" }
        _customAvatarBgColor.update { profilePrefs.getString("custom_avatar_bg_color", "3A3C44") ?: "3A3C44" }
        
        _themeColor.update { prefs.getString("theme_color", "indigo") ?: "indigo" }
        _blockStyle.update { prefs.getString("block_style", "neon") ?: "neon" }
        _boardColorSkin.update { prefs.getString("board_color_skin", "cyberpunk") ?: "cyberpunk" }

        // Relax settings
        _relaxImmortal.update { prefs.getBoolean("relax_immortal", true) }
        _relaxSpeed.update { prefs.getString("relax_speed", "slow") ?: "slow" }
        _relaxBlockSet.update { prefs.getString("relax_block_set", "ideal") ?: "ideal" }
        _relaxGhostEnabled.update { prefs.getBoolean("relax_ghost_enabled", true) }
        _lobbyMusicEnabled.update { prefs.getBoolean("lobby_music_enabled", true) }

        // Custom Tag settings
        _customTag.update { profilePrefs.getString("custom_tag", "") ?: "" }
        _customTagUnlocked.update { profilePrefs.getBoolean("custom_tag_unlocked", false) }

        // Volume settings
        _soundVolume.update { prefs.getFloat("sound_volume", 1.0f) }
        _lobbyMusicVolume.update { prefs.getFloat("lobby_music_volume", 0.8f) }
        _controlButtonStyle.update { prefs.getString("control_button_style", "neon") ?: "neon" }
        
        _statsClearedLines.update { prefs.getInt("stats_cleared_lines", 0) }
        _statsHighScore.update { prefs.getInt("stats_high_score", 0) }
        
        evaluateAchievements()
    }

    fun addBonusXp(amount: Int) {
        val current = _bonusXp.value
        val updated = current + amount
        _bonusXp.value = updated
        profilePrefs.edit().putInt("bonus_xp", updated).apply()
        saveCurrentProfileToDb()
    }

    fun setHasNicknameGradient(hasGradient: Boolean) {
        _hasNicknameGradient.value = hasGradient
        profilePrefs.edit().putBoolean("has_nickname_gradient", hasGradient).apply()
        saveCurrentProfileToDb()
    }

    fun deleteAccount(username: String) {
        viewModelScope.launch {
            accountRepo.delete(username)
        }
    }

    fun fetchGlobalLeaderboard() {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("high_scores")
            .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("playerName") ?: ""
                    val score = doc.getLong("score")?.toInt() ?: 0
                    val ts = doc.getLong("timestamp") ?: 0L
                    val hasGrad = doc.getBoolean("hasGradient") ?: false
                    val tag = doc.getString("customTag") ?: ""
                    HighScore(playerName = name, score = score, timestamp = ts, hasGradient = hasGrad).apply {
                        customTag = tag
                    }
                }
                _globalScores.value = list
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun fetchFirebaseUsersForAdmin() {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    data + mapOf("uid" to doc.id)
                }
                _firebaseUsers.value = list
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun adminClearAllScores() {
        viewModelScope.launch {
            scoreRepo.clearAll()
        }
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("high_scores").get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit().addOnSuccessListener {
                    fetchGlobalLeaderboard()
                }
            }
    }

    fun adminClearAllNonAdminAccounts() {
        viewModelScope.launch {
            accountRepo.clearAllNonAdmin()
        }
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users").get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    val pName = doc.getString("player_name")
                    if (pName != "FsFq") {
                        batch.delete(doc.reference)
                        firestore.collection("high_scores").document(doc.id).delete()
                    }
                }
                batch.commit().addOnSuccessListener {
                    fetchFirebaseUsersForAdmin()
                    fetchGlobalLeaderboard()
                }
            }
    }

    fun adminUpdateAccountCredits(username: String, amount: Int) {
        viewModelScope.launch {
            val account = accountRepo.getAccount(username)
            if (account != null) {
                val updated = account.copy(credits = amount)
                accountRepo.insert(updated)
            }
        }
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users")
            .whereEqualTo("player_name", username)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "credits", amount)
                }
                batch.commit().addOnSuccessListener {
                    fetchFirebaseUsersForAdmin()
                }
            }
    }

    fun adminUpdateAccountRank(username: String, rank: String) {
        viewModelScope.launch {
            val account = accountRepo.getAccount(username)
            if (account != null) {
                val updated = account.copy(onlineTier = rank)
                accountRepo.insert(updated)
            }
        }
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users")
            .whereEqualTo("player_name", username)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "online_tier", rank)
                }
                batch.commit().addOnSuccessListener {
                    fetchFirebaseUsersForAdmin()
                }
            }
    }

    fun adminUpdateFirebaseUserDirect(uid: String, credits: Int, rank: String) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val updates = mapOf(
            "credits" to credits,
            "online_tier" to rank
        )
        firestore.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                fetchFirebaseUsersForAdmin()
            }
    }

    fun adminUpdateFirebaseUserFull(uid: String, credits: Int, rank: String, bonusXp: Int, hasGradient: Boolean) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val updates = mapOf(
            "credits" to credits,
            "online_tier" to rank,
            "bonus_xp" to bonusXp,
            "has_nickname_gradient" to hasGradient
        )
        firestore.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                fetchFirebaseUsersForAdmin()
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null && currentUser.uid == uid) {
                    viewModelScope.launch {
                        com.example.db.FirebaseSync.pullUserData(getApplication())
                        _credits.update { prefs.getInt("credits", 750) }
                        _onlineTier.update { profilePrefs.getString("online_tier", "BRONZE") ?: "BRONZE" }
                        _hasNicknameGradient.update { profilePrefs.getBoolean("has_nickname_gradient", false) }
                        _bonusXp.update { profilePrefs.getInt("bonus_xp", 0) }
                    }
                }
            }
    }

    fun adminUnlockAllFirebase(uid: String) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val updates = mapOf(
            "credits" to 99999,
            "has_nickname_gradient" to true,
            "bonus_xp" to 50000,
            "purchased_avatar_frames" to listOf("standard", "neon_ae", "gold_ma", "chrono_gl", "omega_ti"),
            "purchased_sound_packs" to listOf("arcade", "synthwave", "cyber_metal", "ai_voice"),
            "purchased_themes" to listOf("indigo", "neon", "red", "emerald", "amber", "rose", "sky", "orange", "toxic_green", "cyber_pink", "gold"),
            "purchased_fonts" to listOf("default", "monospace", "serif", "sans-serif", "cursive", "condensed", "black", "thin"),
            "purchased_control_button_styles" to listOf("classic", "neon", "glass"),
            "purchased_cube_skins" to listOf("neon", "glass", "retro", "flat", "material", "glowing_jewel", "steampunk", "red_gradient", "green_gradient", "blue_gradient", "purple_gradient"),
            "purchased_skins" to listOf("cyberpunk", "retro_amber", "emerald_matrix", "vaporwave_pink", "midnight_gold", "carbon_neutral", "plasma_storm", "glacial_frost")
        )
        firestore.collection("users").document(uid).update(updates)
            .addOnSuccessListener {
                fetchFirebaseUsersForAdmin()
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null && currentUser.uid == uid) {
                    viewModelScope.launch {
                        com.example.db.FirebaseSync.pullUserData(getApplication())
                        _credits.update { prefs.getInt("credits", 750) }
                        _hasNicknameGradient.update { profilePrefs.getBoolean("has_nickname_gradient", false) }
                        _bonusXp.update { profilePrefs.getInt("bonus_xp", 0) }
                        _purchasedAvatarFrames.update { profilePrefs.getStringSet("purchased_avatar_frames", setOf("standard")) ?: setOf("standard") }
                        _purchasedSoundPacks.update { profilePrefs.getStringSet("purchased_sound_packs", setOf("arcade")) ?: setOf("arcade") }
                        _purchasedThemes.update { profilePrefs.getStringSet("purchased_themes", setOf("indigo", "neon", "red")) ?: setOf("indigo", "neon", "red") }
                        _purchasedFonts.update { profilePrefs.getStringSet("purchased_fonts", setOf("default", "monospace")) ?: setOf("default", "monospace") }
                        _purchasedControlButtonStyles.update { profilePrefs.getStringSet("purchased_control_button_styles", setOf("classic", "neon")) ?: setOf("classic", "neon") }
                    }
                }
            }
    }

    fun adminWipeStatsFirebase(uid: String) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val updates = mapOf(
            "credits" to 750,
            "online_tier" to "BRONZE",
            "has_nickname_gradient" to false,
            "bonus_xp" to 0,
            "equipped_avatar_frame" to "standard",
            "purchased_avatar_frames" to listOf("standard"),
            "equipped_title" to "none",
            "purchased_titles" to listOf("none"),
            "equipped_sound_pack" to "arcade",
            "purchased_sound_packs" to listOf("arcade"),
            "purchased_themes" to listOf("indigo", "neon", "red"),
            "purchased_fonts" to listOf("default", "monospace"),
            "purchased_control_button_styles" to listOf("classic", "neon"),
            "purchased_cube_skins" to listOf("neon"),
            "purchased_skins" to listOf("cyberpunk"),
            "stats_games_played" to 0,
            "stats_spent_credits" to 0,
            "stats_cleared_lines" to 0,
            "stats_high_score" to 0,
            "stats_max_speed_reached" to 0,
            "stats_tetrises_count" to 0,
            "block_blast_high_score" to 0,
            "stats_avatar_changes" to 0,
            "multiplayer_launches" to 0,
            "board_color_skin" to "cyberpunk",
            "block_style" to "glass",
            "custom_avatar_base64" to "",
            "custom_background_base64" to ""
        )
        firestore.collection("users").document(uid).set(updates)
            .addOnSuccessListener {
                fetchFirebaseUsersForAdmin()
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null && currentUser.uid == uid) {
                    viewModelScope.launch {
                        com.example.db.FirebaseSync.pullUserData(getApplication())
                        _credits.update { prefs.getInt("credits", 750) }
                        _onlineTier.update { profilePrefs.getString("online_tier", "BRONZE") ?: "BRONZE" }
                        _hasNicknameGradient.update { profilePrefs.getBoolean("has_nickname_gradient", false) }
                        _bonusXp.update { profilePrefs.getInt("bonus_xp", 0) }
                        _statsClearedLines.update { prefs.getInt("stats_cleared_lines", 0) }
                        _statsHighScore.update { prefs.getInt("stats_high_score", 0) }
                    }
                }
            }
    }

    fun adminUpdateAccountGradient(username: String, hasGradient: Boolean) {
        viewModelScope.launch {
            val account = accountRepo.getAccount(username)
            if (account != null) {
                val updated = account.copy(hasGradient = hasGradient)
                accountRepo.insert(updated)
            }
            if (_playerName.value == username) {
                _hasNicknameGradient.update { hasGradient }
                profilePrefs.edit().putBoolean("has_nickname_gradient", hasGradient).apply()
            }
        }
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users")
            .whereEqualTo("player_name", username)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "has_nickname_gradient", hasGradient)
                }
                batch.commit().addOnSuccessListener {
                    fetchFirebaseUsersForAdmin()
                }
            }
    }

    fun adminUpdateAccountBonusXp(username: String, xp: Int) {
        viewModelScope.launch {
            val account = accountRepo.getAccount(username)
            if (account != null) {
                val updated = account.copy(bonusXp = xp)
                accountRepo.insert(updated)
            }
            if (_playerName.value == username) {
                _bonusXp.update { xp }
                profilePrefs.edit().putInt("bonus_xp", xp).apply()
            }
        }
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users")
            .whereEqualTo("player_name", username)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.update(doc.reference, "bonus_xp", xp)
                }
                batch.commit().addOnSuccessListener {
                    fetchFirebaseUsersForAdmin()
                }
            }
    }

    fun adminUnlockAllLocal(username: String) {
        viewModelScope.launch {
            val account = accountRepo.getAccount(username)
            if (account != null) {
                val updated = account.copy(
                    credits = 99999,
                    hasGradient = true,
                    bonusXp = 50000
                )
                accountRepo.insert(updated)
            }
            if (_playerName.value == username) {
                _credits.value = 99999
                _hasNicknameGradient.value = true
                _bonusXp.value = 50000
                prefs.edit().putInt("credits", 99999).apply()
                profilePrefs.edit()
                    .putBoolean("has_nickname_gradient", true)
                    .putInt("bonus_xp", 50000)
                    .putStringSet("purchased_skins", setOf("cyberpunk", "retro_amber", "emerald_matrix", "vaporwave_pink", "midnight_gold", "carbon_neutral", "plasma_storm", "glacial_frost"))
                    .putStringSet("purchased_cube_skins", setOf("neon", "glass", "retro", "flat", "material", "glowing_jewel", "steampunk", "red_gradient", "green_gradient", "blue_gradient", "purple_gradient"))
                    .putStringSet("purchased_avatar_frames", setOf("standard", "neon_ae", "gold_ma", "chrono_gl", "omega_ti"))
                    .putStringSet("purchased_sound_packs", setOf("arcade", "synthwave", "cyber_metal", "ai_voice"))
                    .putStringSet("purchased_themes", setOf("indigo", "neon", "red", "emerald", "amber", "rose", "sky", "orange", "toxic_green", "cyber_pink", "gold"))
                    .putStringSet("purchased_fonts", setOf("default", "monospace", "serif", "sans-serif", "cursive", "condensed", "black", "thin"))
                    .putStringSet("purchased_control_button_styles", setOf("classic", "neon", "glass"))
                    .apply()
                
                _equippedAvatarFrame.value = "standard"
                _purchasedAvatarFrames.value = setOf("standard", "neon_ae", "gold_ma", "chrono_gl", "omega_ti")
                _equippedSoundPack.value = "arcade"
                _purchasedSoundPacks.value = setOf("arcade", "synthwave", "cyber_metal", "ai_voice")
                _purchasedThemes.value = setOf("indigo", "neon", "red", "emerald", "amber", "rose", "sky", "orange", "toxic_green", "cyber_pink", "gold")
                _purchasedFonts.value = setOf("default", "monospace", "serif", "sans-serif", "cursive", "condensed", "black", "thin")
                _purchasedControlButtonStyles.value = setOf("classic", "neon", "glass")
                
                saveCurrentProfileToDb()
            } else {
                val matchingUser = _firebaseUsers.value.find { (it["player_name"] as? String) == username }
                matchingUser?.let {
                    val uid = it["uid"] as String
                    adminUnlockAllFirebase(uid)
                }
            }
        }
    }

    fun adminWipeStatsLocal(username: String) {
        viewModelScope.launch {
            val account = accountRepo.getAccount(username)
            if (account != null) {
                val updated = account.copy(
                    credits = 750,
                    onlineTier = "BRONZE",
                    hasGradient = false,
                    bonusXp = 0
                )
                accountRepo.insert(updated)
            }
            if (_playerName.value == username) {
                _credits.value = 750
                _onlineTier.value = "BRONZE"
                _hasNicknameGradient.value = false
                _bonusXp.value = 0
                
                prefs.edit()
                    .putInt("credits", 750)
                    .putInt("stats_games_played", 0)
                    .putInt("stats_spent_credits", 0)
                    .putInt("stats_cleared_lines", 0)
                    .putInt("stats_high_score", 0)
                    .putInt("stats_max_speed_reached", 0)
                    .putInt("stats_tetrises_count", 0)
                    .putInt("block_blast_high_score", 0)
                    .putInt("stats_avatar_changes", 0)
                    .putInt("multiplayer_launches", 0)
                    .putString("board_color_skin", "cyberpunk")
                    .putString("block_style", "glass")
                    .apply()
                    
                profilePrefs.edit()
                    .putString("online_tier", "BRONZE")
                    .putBoolean("has_nickname_gradient", false)
                    .putInt("bonus_xp", 0)
                    .putString("equipped_avatar_frame", "standard")
                    .putStringSet("purchased_avatar_frames", setOf("standard"))
                    .putString("equipped_title", "none")
                    .putStringSet("purchased_titles", setOf("none"))
                    .putString("equipped_sound_pack", "arcade")
                    .putStringSet("purchased_sound_packs", setOf("arcade"))
                    .putStringSet("purchased_themes", setOf("indigo", "neon", "red"))
                    .putStringSet("purchased_fonts", setOf("default", "monospace"))
                    .putStringSet("purchased_control_button_styles", setOf("classic", "neon"))
                    .putStringSet("purchased_skins", setOf("cyberpunk"))
                    .putStringSet("purchased_cube_skins", setOf("neon"))
                    .apply()
                
                _statsClearedLines.value = 0
                _statsHighScore.value = 0
                
                saveCurrentProfileToDb()
            } else {
                val matchingUser = _firebaseUsers.value.find { (it["player_name"] as? String) == username }
                matchingUser?.let {
                    val uid = it["uid"] as String
                    adminWipeStatsFirebase(uid)
                }
            }
        }
    }

    fun adminDeleteFirebaseUserDirect(uid: String) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users").document(uid).delete()
            .addOnSuccessListener {
                fetchFirebaseUsersForAdmin()
            }
        firestore.collection("high_scores").document(uid).delete()
            .addOnSuccessListener {
                fetchGlobalLeaderboard()
            }
    }

    fun adminUnlockAllAchievements() {
        viewModelScope.launch {
            _achievementsList.update { list ->
                list.map { ach ->
                    ach.copy(isUnlocked = true, currentValue = ach.targetValue)
                }
            }
            addCredits(5000)
        }
    }

    fun startGame(mode: GameMode = GameMode.CLASSIC) {
        gameEngine.startGame(mode, startingLevel = _customStartLevel.value)
        
        // Track games played and starting modes
        val totalGames = prefs.getInt("stats_games_played", 0) + 1
        prefs.edit().putInt("stats_games_played", totalGames).apply()
        
        when (mode) {
            GameMode.EXTENDED -> prefs.edit().putBoolean("ach_extended_pioneer_unlocked", true).apply()
            GameMode.FAST_RUN -> prefs.edit().putBoolean("ach_speed_runner_unlocked", true).apply()
            GameMode.TIME_ATTACK -> prefs.edit().putBoolean("ach_mode_time_attack_unlocked", true).apply()
            GameMode.REVERSE_CONTROLS -> prefs.edit().putBoolean("ach_mode_reverse_unlocked", true).apply()
            GameMode.MIRROR_DIMENSION -> prefs.edit().putBoolean("ach_mode_mirror_unlocked", true).apply()
            GameMode.PENTARY_CHAOS -> prefs.edit().putBoolean("ach_mode_pentary_unlocked", true).apply()
            GameMode.PULSE_EXTREME -> prefs.edit().putBoolean("ach_mode_pulse_unlocked", true).apply()
            else -> {}
        }
        
        evaluateAchievements()
        
        _isPlaying.update { true }
        startGameLoop()
    }

    fun startBlockBlast() {
        blockBlastEngine.startGame(prefs.getInt("block_blast_high_score", 0))
        val totalGames = prefs.getInt("stats_games_played", 0) + 1
        prefs.edit().putInt("stats_games_played", totalGames).apply()
        evaluateAchievements()
    }

    fun placeBlockBlastFigure(idx: Int, r: Int, c: Int): Boolean {
        val ok = blockBlastEngine.placeFigure(idx, r, c) { linesCleared ->
            addCredits(linesCleared * 11)
        }
        if (ok) {
            val finalScore = blockBlastEngine.state.value.score
            prefs.edit().putInt("block_blast_high_score", blockBlastEngine.state.value.highScore).apply()
            onBlockBlastPlacement(finalScore)
            
            // Tiered gameover coins for Block Blast match completion
            if (blockBlastEngine.state.value.isGameOver) {
                val basePassCoins = 22
                val perfCoins = finalScore / 133
                val modeBonusCoins = 45
                addCredits(basePassCoins + perfCoins + modeBonusCoins)
            }
        }
        return ok
    }

    fun pauseGame() {
        _isPlaying.update { false }
        gameLoopJob?.cancel()
    }

    fun resumeGame() {
        if (!gameEngine.gameState.value.isGameOver) {
            _isPlaying.update { true }
            startGameLoop()
        }
    }

    fun saveCurrentGame(): Boolean {
        val state = gameEngine.gameState.value
        if (state.isGameOver || state.currentPiece == null) {
            return false
        }
        val editor = prefs.edit()
        
        editor.putInt("saved_score", state.score)
        editor.putInt("saved_lines", state.lines)
        editor.putInt("saved_level", state.level)
        editor.putBoolean("saved_extended", state.isExtendedMode)
        editor.putBoolean("saved_game_over", state.isGameOver)
        
        editor.putInt("saved_current_color", state.currentPiece.colorIndex)
        editor.putInt("saved_pos_x", state.currentPos.x)
        editor.putInt("saved_pos_y", state.currentPos.y)
        
        editor.putInt("saved_hold_color", state.holdPiece?.colorIndex ?: -1)
        
        val nextIndices = state.nextPieces.map { it.colorIndex }.joinToString(",")
        editor.putString("saved_next_indices", nextIndices)
        
        val gridStr = state.grid.joinToString(";") { row -> row.joinToString(",") }
        editor.putString("saved_grid", gridStr)
        
        // Save upgraded game parameters
        editor.putString("saved_game_mode", state.gameMode.name)
        editor.putInt("saved_time_remaining", state.timeRemainingSeconds)
        editor.putInt("saved_pieces_placed", state.piecesPlaced)
        
        editor.putBoolean("has_saved_game", true)
        editor.apply()
        
        _hasSavedGame.update { true }
        return true
    }

    fun loadSavedGame(): Boolean {
        if (!prefs.getBoolean("has_saved_game", false)) return false
        
        try {
            val score = prefs.getInt("saved_score", 0)
            val lines = prefs.getInt("saved_lines", 0)
            val level = prefs.getInt("saved_level", 1)
            val isExtended = prefs.getBoolean("saved_extended", false)
            val isGameOver = prefs.getBoolean("saved_game_over", false)
            
            val currentColorIdx = prefs.getInt("saved_current_color", 1)
            val posX = prefs.getInt("saved_pos_x", 4)
            val posY = prefs.getInt("saved_pos_y", 0)
            
            val holdColorIdxIdx = prefs.getInt("saved_hold_color", -1)
            val holdColorIdx = if (holdColorIdxIdx == -1) null else holdColorIdxIdx
            
            val nextStr = prefs.getString("saved_next_indices", "") ?: ""
            val nextIndices = if (nextStr.isEmpty()) emptyList() else nextStr.split(",").mapNotNull { it.toIntOrNull() }
            
            val gridStr = prefs.getString("saved_grid", null)
            val grid = if (gridStr != null) {
                val parsed = gridStr.split(";").map { rowStr ->
                    rowStr.split(",").mapNotNull { it.toIntOrNull() }.toIntArray()
                }
                if (parsed.size == 22 && parsed.all { it.size == 10 }) {
                    parsed
                } else {
                    List(22) { IntArray(10) }
                }
            } else {
                List(22) { IntArray(10) }
            }
            
            // Load upgraded game parameters
            val savedModeStr = prefs.getString("saved_game_mode", GameMode.CLASSIC.name) ?: GameMode.CLASSIC.name
            val savedMode = try {
                com.example.game.GameMode.valueOf(savedModeStr)
            } catch (e: Exception) {
                com.example.game.GameMode.CLASSIC
            }
            val savedTimeRemaining = prefs.getInt("saved_time_remaining", 60)
            val savedPiecesPlaced = prefs.getInt("saved_pieces_placed", 0)
            
            gameEngine.restoreState(
                grid = grid,
                currentPieceColorIndex = currentColorIdx,
                currentPos = Position(posX, posY),
                holdPieceColorIndex = holdColorIdx,
                nextPiecesColorIndices = nextIndices,
                score = score,
                lines = lines,
                level = level,
                isExtendedMode = isExtended,
                isGameOver = isGameOver,
                gameMode = savedMode,
                timeRemainingSeconds = savedTimeRemaining,
                piecesPlaced = savedPiecesPlaced
            )
            
            _isPlaying.update { !isGameOver }
            if (!isGameOver) {
                startGameLoop()
            }
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            prefs.edit().putBoolean("has_saved_game", false).apply()
            _hasSavedGame.update { false }
            return false
        }
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            try {
                var lastTimeTick = System.currentTimeMillis()
                while (_isPlaying.value) {
                    val delayTime = if (gameEngine.gameState.value.gameMode == com.example.game.GameMode.RELAX) {
                        when (_relaxSpeed.value) {
                            "static" -> 1000L
                            "slow" -> 1500L
                            "normal" -> 800L
                            "fast" -> 300L
                            else -> 800L
                        }
                    } else {
                        val level = gameEngine.gameState.value.level
                        val baseDelay = when {
                            level <= 11 -> 850L - (level * 50L)
                            level == 12 -> 260L
                            level == 13 -> 220L
                            level == 14 -> 180L
                            level >= 15 -> 150L
                            else -> 800L
                        }
                        val speedMult = _gameSpeedMultiplier.value
                        (baseDelay / speedMult).toLong().coerceAtLeast(30L)
                    }
                    delay(delayTime)
                    if (!_isPlaying.value) break
                    
                    if (gameEngine.gameState.value.gameMode == com.example.game.GameMode.RELAX && _relaxSpeed.value == "static") {
                        continue
                    }
                    
                    val now = System.currentTimeMillis()
                    if (gameEngine.gameState.value.gameMode == com.example.game.GameMode.TIME_ATTACK) {
                        if (now - lastTimeTick >= 1000) {
                            val elapsedSec = ((now - lastTimeTick) / 1000).toInt()
                            lastTimeTick = now
                            gameEngine.decrementTime(elapsedSec)
                        }
                    }
                    
                    if (gameEngine.gameState.value.isGameOver) {
                        triggerAudioFeedback("gameover")
                        saveHighScore()
                        _isPlaying.update { false }
                        prefs.edit().putBoolean("has_saved_game", false).apply()
                        _hasSavedGame.update { false }
                        break
                    }
                    val prevScore = gameEngine.gameState.value.score
                    val prevPieces = gameEngine.gameState.value.piecesPlaced
                    val prevLines = gameEngine.gameState.value.lines
                    gameEngine.tick()
                    val stateAfter = gameEngine.gameState.value
                    if (stateAfter.score != prevScore || stateAfter.piecesPlaced != prevPieces) {
                        saveCurrentGame()
                    }
                    if (stateAfter.isGameOver) {
                        triggerAudioFeedback("gameover")
                        saveHighScore()
                        _isPlaying.update { false }
                        prefs.edit().putBoolean("has_saved_game", false).apply()
                        _hasSavedGame.update { false }
                        break
                    }
                    if (stateAfter.lines > prevLines) {
                        val cleared = stateAfter.lines - prevLines
                        when (cleared) {
                            1 -> triggerAudioFeedback("clear_1")
                            2 -> triggerAudioFeedback("clear_2")
                            3 -> triggerAudioFeedback("clear_3")
                            4 -> triggerAudioFeedback("clear_4")
                            else -> triggerAudioFeedback("clear")
                        }
                    } else if (stateAfter.piecesPlaced > prevPieces) {
                        triggerAudioFeedback("land")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun awardMultiplayerCredits(playerScore: Int, opponentScore: Int, won: Boolean) {
        val basePlayCoins = 50
        val performanceCoins = playerScore / 5
        val winBonus = if (won) 150 else 0
        val total = ((basePlayCoins + performanceCoins + winBonus) * 1.5f).toInt()
        addCredits(total)
    }

    private fun saveHighScore() {
        val mode = gameEngine.gameState.value.gameMode
        if (mode == com.example.game.GameMode.RELAX) {
            // Relax mode scores are not saved to databases or leaderboards!
            return
        }
        val score = gameEngine.gameState.value.score
        val lines = gameEngine.gameState.value.lines
        
        // Track stats
        val totalClearedLines = prefs.getInt("stats_cleared_lines", 0) + lines
        prefs.edit().putInt("stats_cleared_lines", totalClearedLines).apply()
        _statsClearedLines.value = totalClearedLines
        
        val maxScore = maxOf(prefs.getInt("stats_high_score", 0), score)
        prefs.edit().putInt("stats_high_score", maxScore).apply()
        _statsHighScore.value = maxScore

        val level = gameEngine.gameState.value.level
        val maxSpeed = maxOf(prefs.getInt("stats_max_speed_reached", 0), level)
        prefs.edit().putInt("stats_max_speed_reached", maxSpeed).apply()
        
        val tetrisesInGame = gameEngine.gameState.value.tetrisesCleared
        val totalTetrises = prefs.getInt("stats_tetrises_count", 0) + tetrisesInGame
        prefs.edit().putInt("stats_tetrises_count", totalTetrises).apply()

        // Tiered coin reward calculation for playing standard modes
        if (score > 100) {
            val basePlayCoins = 22
            val performanceCoins = score / 133
            val modeBonus = when (mode) {
                com.example.game.GameMode.CLASSIC, com.example.game.GameMode.ZEN_FLOW -> 0
                com.example.game.GameMode.EXTENDED, com.example.game.GameMode.FAST_RUN, 
                com.example.game.GameMode.TIME_ATTACK, com.example.game.GameMode.REVERSE_CONTROLS,
                com.example.game.GameMode.MIRROR_DIMENSION -> 37
                com.example.game.GameMode.PENTARY_CHAOS -> 60
                com.example.game.GameMode.BLOCK_BLAST -> 45
                com.example.game.GameMode.PULSE_EXTREME -> 90
                else -> 0
            }
            val total = ((basePlayCoins + performanceCoins + modeBonus) * 1.125f).toInt()
            addCredits(total)
        }

        if (score > 0) {
            viewModelScope.launch {
                scoreRepo.insert(HighScore(playerName = _playerName.value, score = score))
            }
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                
                // 1. All-time high score
                firestore.collection("high_scores").document(currentUser.uid).get()
                    .addOnSuccessListener { doc ->
                        val existingScore = doc.getLong("score") ?: 0
                        if (score > existingScore) {
                            val scoreMap = mapOf(
                                "uid" to currentUser.uid,
                                "playerName" to _playerName.value,
                                "score" to score,
                                "timestamp" to System.currentTimeMillis(),
                                "hasGradient" to _hasNicknameGradient.value,
                                "customTag" to _customTag.value
                            )
                            firestore.collection("high_scores").document(currentUser.uid).set(scoreMap)
                        }
                    }
                
                // 2. Mode-specific high score
                val modeCode = mode.code
                firestore.collection("high_scores_by_mode").document(modeCode)
                    .collection("scores").document(currentUser.uid).get()
                    .addOnSuccessListener { doc ->
                        val existingScore = doc.getLong("score") ?: 0
                        if (score > existingScore) {
                            val scoreMap = mapOf(
                                "uid" to currentUser.uid,
                                "playerName" to _playerName.value,
                                "score" to score,
                                "timestamp" to System.currentTimeMillis(),
                                "mode" to modeCode,
                                "hasGradient" to _hasNicknameGradient.value,
                                "customTag" to _customTag.value
                            )
                            firestore.collection("high_scores_by_mode").document(modeCode)
                                .collection("scores").document(currentUser.uid).set(scoreMap)
                        }
                    }
            }
        }
        
        // Evaluate achievements
        evaluateAchievements()
    }

    fun triggerAudioFeedback(type: String) {
        if (!_soundEnabled.value) return
        if (playSoundEffect(type)) return
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.Default) {
            try {
                val sampleRate = 44100
                val durationMs = when (type) {
                    "click" -> 50
                    "rotate" -> 80
                    "land" -> 100
                    "clear" -> 250
                    "gameover" -> 600
                    "buy" -> 300
                    "equip" -> 150
                    "success" -> 400
                    "error" -> 350
                    else -> 50
                }
                val numSamples = (durationMs * sampleRate / 1000)
                val samples = FloatArray(numSamples)
                val pack = _equippedSoundPack.value

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val progress = i.toDouble() / numSamples

                    samples[i] = when (pack) {
                        "synthwave" -> {
                            when (type) {
                                "click" -> {
                                    val freq = 220.0
                                    kotlin.math.sin(2.0 * kotlin.math.PI * freq * t).toFloat() * 0.4f * (1.0f - progress).toFloat()
                                }
                                "rotate" -> {
                                    val freq = 200.0 + (300.0 * Math.sin(Math.PI * progress))
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.4f
                                }
                                "land" -> {
                                    val freq = 80.0
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.5f * (1.0f - progress).toFloat()
                                }
                                "clear" -> {
                                    val noteIndex = (progress * 4).toInt().coerceIn(0, 3)
                                    val freq = when (noteIndex) {
                                        0 -> 261.63
                                        1 -> 329.63
                                        2 -> 392.00
                                        else -> 493.88
                                    }
                                    kotlin.math.sin(2.0 * kotlin.math.PI * freq * t).toFloat() * 0.4f * (1.0f - progress).toFloat()
                                }
                                "gameover" -> {
                                    val freq = 196.00 * (1.0 - progress * 0.4)
                                    (Math.sin(2.0 * Math.PI * freq * t) + 0.3 * Math.sin(4.0 * Math.PI * freq * t)).toFloat() * 0.4f * (1.0f - progress).toFloat()
                                }
                                "buy" -> {
                                    val freq1 = 880.0
                                    val freq2 = 1318.51
                                    (Math.sin(2.0 * Math.PI * freq1 * t) + 0.5 * Math.sin(2.0 * Math.PI * freq2 * t)).toFloat() * 0.3f * (1.0f - progress).toFloat()
                                }
                                "equip" -> {
                                    val freq = 440.0 + 220.0 * progress
                                    kotlin.math.sin(2.0 * kotlin.math.PI * freq * t).toFloat() * 0.4f * (1.0f - progress).toFloat()
                                }
                                "success" -> {
                                    val freq = 329.63 + 329.63 * progress
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.4f
                                }
                                "error" -> {
                                    val carrier = 110.0
                                    val modulator = 8.0
                                    val index = 5.0
                                    Math.sin(2.0 * Math.PI * carrier * t + index * Math.sin(2.0 * Math.PI * modulator * t)).toFloat() * 0.4f
                                }
                                else -> Math.sin(2.0 * Math.PI * 300.0 * t).toFloat() * 0.4f
                            }
                        }
                        "cyber_metal" -> {
                            val noise = (Math.sin(t * 123456.7) * 0.2).toFloat()
                            when (type) {
                                "click" -> {
                                    val saw = ((t * 800.0) % 1.0 * 2.0 - 1.0).toFloat()
                                    (saw * 0.3f + noise) * (1.0f - progress).toFloat()
                                }
                                "rotate" -> {
                                    val freq = 400.0 - 150.0 * progress
                                    val saw = ((t * freq) % 1.0 * 2.0 - 1.0).toFloat()
                                    saw.coerceIn(-0.3f, 0.3f) * 1.5f
                                }
                                "land" -> {
                                    val freq = 90.0
                                    val saw = ((t * freq) % 1.0 * 2.0 - 1.0).toFloat()
                                    (saw * 0.4f + noise * 0.6f) * (1.0f - progress).toFloat()
                                }
                                "clear" -> {
                                    val freq1 = 293.66
                                    val freq2 = 440.00
                                    val saw1 = ((t * freq1) % 1.0 * 2.0 - 1.0)
                                    val saw2 = ((t * freq2) % 1.0 * 2.0 - 1.0)
                                    val mix = (saw1 + saw2).toFloat() * 0.3f + noise * 0.4f
                                    mix.coerceIn(-0.4f, 0.4f) * 2.2f * (1.0f - progress).toFloat()
                                }
                                "gameover" -> {
                                    val freq = 120.0 - 80.0 * progress
                                    val saw = ((t * freq) % 1.0 * 2.0 - 1.0).toFloat()
                                    (saw + noise).coerceIn(-0.4f, 0.4f) * 1.8f * (1.0f - progress).toFloat()
                                }
                                "buy" -> {
                                    val freq = 2000.0 - 1500.0 * progress
                                    val sq = if (((t * freq) % 1.0) > 0.5) 1.0f else -1.0f
                                    (sq * 0.2f + noise * 0.8f) * (1.0f - progress).toFloat()
                                }
                                "equip" -> {
                                    val sq = if (((t * 900.0) % 1.0) > 0.5) 0.3f else -0.3f
                                    (sq + noise * 0.7f) * (1.0f - progress).toFloat()
                                }
                                "success" -> {
                                    val noteIdx = (progress * 3).toInt().coerceIn(0, 2)
                                    val freq = when (noteIdx) {
                                        0 -> 196.00
                                        1 -> 261.63
                                        else -> 293.66
                                    }
                                    val saw = ((t * freq) % 1.0 * 2.0 - 1.0).toFloat()
                                    saw.coerceIn(-0.4f, 0.4f) * 2.0f
                                }
                                "error" -> {
                                    val saw = ((t * 85.0) % 1.0 * 2.0 - 1.0).toFloat()
                                    (saw + noise * 0.5f).coerceIn(-0.4f, 0.4f) * 2.0f
                                }
                                else -> noise
                            }
                        }
                        "ai_voice" -> {
                            when (type) {
                                "click" -> {
                                    val freq = if (progress < 0.5) 1200.0 else 1800.0
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.3f * (1.0f - progress).toFloat()
                                }
                                "rotate" -> {
                                    val carrier = 600.0 - 200.0 * progress
                                    val modulator = 120.0
                                    val index = 3.0
                                    Math.sin(2.0 * Math.PI * carrier * t + index * Math.sin(2.0 * Math.PI * modulator * t)).toFloat() * 0.4f
                                }
                                "land" -> {
                                    val carrier = 150.0
                                    val modulator = 30.0
                                    val index = 2.0
                                    Math.sin(2.0 * Math.PI * carrier * t + index * Math.sin(2.0 * Math.PI * modulator * t)).toFloat() * 0.4f * (1.0f - progress).toFloat()
                                }
                                "clear" -> {
                                    val carrier = 440.0 + 880.0 * progress
                                    val modulator = 220.0
                                    val index = 4.0
                                    Math.sin(2.0 * Math.PI * carrier * t + index * Math.sin(2.0 * Math.PI * modulator * t)).toFloat() * 0.4f * (1.0f - progress).toFloat()
                                }
                                "gameover" -> {
                                    val wave = Math.sin(2.0 * Math.PI * 4.0 * t)
                                    val carrier = 330.0 + 100.0 * wave
                                    Math.sin(2.0 * Math.PI * carrier * t).toFloat() * 0.4f * (1.0f - progress).toFloat()
                                }
                                "buy" -> {
                                    val step = (progress * 6).toInt()
                                    val freq = 800.0 + step * 200.0
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.3f
                                }
                                "equip" -> {
                                    val freq = 1500.0 - 1000.0 * progress
                                    kotlin.math.sin(2.0 * kotlin.math.PI * freq * t).toFloat() * 0.4f * (1.0f - progress).toFloat()
                                }
                                "success" -> {
                                    val step = (progress * 3).toInt()
                                    val freq = when (step) {
                                        0 -> 523.25
                                        1 -> 659.25
                                        else -> 1046.50
                                    }
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.4f
                                }
                                "error" -> {
                                    val step = (progress * 2).toInt()
                                    val freq = if (step == 0) 220.0 else 180.0
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.4f
                                }
                                else -> Math.sin(2.0 * Math.PI * 800.0 * t).toFloat() * 0.3f
                            }
                        }
                        else -> {
                            when (type) {
                                "click" -> {
                                    val freq = 400.0
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.5f
                                }
                                "rotate" -> {
                                    val freq = 300.0 + (500.0 * progress)
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.5f
                                }
                                "land" -> {
                                    val freq = 120.0
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.6f * (1.0f - progress).toFloat()
                                }
                                "clear" -> {
                                    val freq1 = 600.0 - (300.0 * progress)
                                    val freq2 = 800.0 - (400.0 * progress)
                                    (Math.sin(2.0 * Math.PI * freq1 * t) + Math.sin(2.0 * Math.PI * freq2 * t)).toFloat() * 0.4f * (1.0f - progress).toFloat()
                                }
                                "gameover" -> {
                                    val freq = 220.0 * Math.pow(2.0, -1.0 * progress)
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.5f * (1.1f - progress).toFloat()
                                }
                                "buy" -> {
                                    val step = (progress * 2).toInt()
                                    val freq = if (step == 0) 987.77 else 1318.51
                                    kotlin.math.sin(2.0 * kotlin.math.PI * freq * t).toFloat() * 0.4f * (1.0f - progress).toFloat()
                                }
                                "equip" -> {
                                    val freq = 659.25 + 329.63 * progress
                                    kotlin.math.sin(2.0 * kotlin.math.PI * freq * t).toFloat() * 0.4f * (1.0f - progress).toFloat()
                                }
                                "success" -> {
                                    val noteIndex = (progress * 4).toInt().coerceIn(0, 3)
                                    val freq = when (noteIndex) {
                                        0 -> 523.25
                                        1 -> 659.25
                                        2 -> 783.99
                                        else -> 1046.50
                                    }
                                    Math.sin(2.0 * Math.PI * freq * t).toFloat() * 0.5f
                                }
                                "error" -> {
                                    val freq = 130.0
                                    val sq = if (((t * freq) % 1.0) > 0.5) 0.4f else -0.4f
                                    sq * (1.0f - progress).toFloat()
                                }
                                else -> {
                                    Math.sin(2.0 * Math.PI * 440.0 * t).toFloat() * 0.5f
                                }
                            }
                        }
                    }
                }

                val buffer = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    buffer[i] = (samples[i].coerceIn(-1.0f, 1.0f) * 32767.0).toInt().toShort()
                }

                val audioTrack = android.media.AudioTrack(
                    android.media.AudioManager.STREAM_MUSIC,
                    sampleRate,
                    android.media.AudioFormat.CHANNEL_OUT_MONO,
                    android.media.AudioFormat.ENCODING_PCM_16BIT,
                    numSamples * 2,
                    android.media.AudioTrack.MODE_STATIC
                )

                audioTrack.write(buffer, 0, numSamples)
                audioTrack.play()
                delay(durationMs.toLong() + 50)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun triggerLocalVibration(type: String) {
        if (!_vibrationEnabled.value) return
        try {
            val vibrator = getApplication<Application>().getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val effect = when (type) {
                        "click" -> android.os.VibrationEffect.createOneShot(20, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
                        "heavy" -> android.os.VibrationEffect.createOneShot(80, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
                        "tick" -> android.os.VibrationEffect.createOneShot(8, 80)
                        "double" -> {
                            val timings = longArrayOf(0, 20, 40, 20)
                            val amplitudes = intArrayOf(0, 180, 0, 180)
                            android.os.VibrationEffect.createWaveform(timings, amplitudes, -1)
                        }
                        else -> android.os.VibrationEffect.createOneShot(15, android.os.VibrationEffect.DEFAULT_AMPLITUDE)
                    }
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    when (type) {
                        "click" -> vibrator.vibrate(20)
                        "heavy" -> vibrator.vibrate(80)
                        "tick" -> vibrator.vibrate(8)
                        "double" -> vibrator.vibrate(longArrayOf(0, 20, 40, 20), -1)
                        else -> vibrator.vibrate(15)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCleared() {
        super.onCleared()
        lobbyManager.cleanUpAllListeners()
        try {
            lobbyMusicPlayer?.stop()
            lobbyMusicPlayer?.release()
            lobbyMusicPlayer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            soundPool?.release()
            soundPool = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initSoundPool() {
        val context = getApplication<Application>()
        val attributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_GAME)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = android.media.SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(attributes)
            .build()

        soundPool?.let { pool ->
            val defaultBtnId = pool.load(context, R.raw.default_button, 1)
            soundMap["click"] = defaultBtnId
            soundMap["tap"] = defaultBtnId
            soundMap["buy"] = pool.load(context, R.raw.buy_button, 1)
            soundMap["equip"] = pool.load(context, R.raw.button_on_off, 1)
            soundMap["error"] = pool.load(context, R.raw.error_no_money, 1)
            soundMap["gameover"] = pool.load(context, R.raw.fail, 1)
            soundMap["case_spin"] = pool.load(context, R.raw.case_spin, 1)
            soundMap["selling"] = pool.load(context, R.raw.selling_item, 1)
            soundMap["online_ready"] = pool.load(context, R.raw.online_readiness, 1)
            soundMap["clear_1"] = pool.load(context, R.raw.one_line, 1)
            soundMap["clear_2"] = pool.load(context, R.raw.two_line, 1)
            soundMap["clear_3"] = pool.load(context, R.raw.three_line, 1)
            soundMap["clear_4"] = pool.load(context, R.raw.four_line, 1)
            soundMap["drop_common"] = pool.load(context, R.raw.drop_common_item, 1)
            soundMap["drop_uncommon"] = pool.load(context, R.raw.drop_uncommon_item, 1)
            soundMap["drop_rare"] = pool.load(context, R.raw.drop_rare_item, 1)
            soundMap["drop_epic"] = pool.load(context, R.raw.drop_epic_item, 1)
            soundMap["drop_legendary"] = pool.load(context, R.raw.drop_legendary_item, 1)
            soundMap["drop_red"] = pool.load(context, R.raw.drop_red_item, 1)
        }
    }

    private fun playSoundEffect(type: String): Boolean {
        val soundId = soundMap[type] ?: return false
        val vol = _soundVolume.value
        soundPool?.play(soundId, vol, vol, 1, 0, 1.0f)
        return true
    }

    private fun startLobbyMusic() {
        if (!_soundEnabled.value || !_lobbyMusicEnabled.value || _isPlaying.value) {
            stopLobbyMusic()
            return
        }
        if (lobbyMusicPlayer == null) {
            try {
                lobbyMusicPlayer = android.media.MediaPlayer.create(getApplication(), R.raw.main_menu_ft).apply {
                    isLooping = true
                    val vol = _lobbyMusicVolume.value
                    setVolume(vol, vol)
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else if (lobbyMusicPlayer?.isPlaying == false) {
            try {
                lobbyMusicPlayer?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun stopLobbyMusic() {
        lobbyMusicPlayer?.let { player ->
            try {
                if (player.isPlaying) {
                    player.pause()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchGlobalLeaderboardByMode(modeCode: String) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("high_scores_by_mode").document(modeCode)
            .collection("scores")
            .orderBy("score", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(50)
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val name = doc.getString("playerName") ?: ""
                    val score = doc.getLong("score")?.toInt() ?: 0
                    val ts = doc.getLong("timestamp") ?: 0L
                    val hasGrad = doc.getBoolean("hasGradient") ?: false
                    val tag = doc.getString("customTag") ?: ""
                    HighScore(playerName = name, score = score, timestamp = ts, hasGradient = hasGrad).apply {
                        customTag = tag
                    }
                }
                _globalScores.value = list
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }
}
