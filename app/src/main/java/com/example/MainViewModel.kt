package com.example

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import org.json.JSONObject
import com.example.db.AppDatabase
import com.example.db.HighScore
import com.example.db.ScoreRepository
import com.example.game.GameEngine
import com.example.game.GameMode
import com.example.game.BlockBlastEngine
import com.example.ui.Language
import com.example.ui.ShopPrices
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.example.db.FirebaseLobbyManager
import com.example.db.FriendUser
import com.example.db.PublicUserProfile

data class LevelReward(
    val level: Int,
    val credits: Int,
    val keyType: String? = null,
    val keyCount: Int = 0
)

data class MasteryTitle(
    val title: String,
    val color: Long,
    val iconEmoji: String
)

object MasterySystem {
    fun getMasteryTitle(level: Int, lang: Language): MasteryTitle {
        return when {
            level >= 50 -> MasteryTitle(
                title = when (lang) {
                    Language.RU -> "Легенда Матрицы"
                    Language.UA -> "Легенда Матриці"
                    Language.KK -> "Матрица Аңызы"
                    Language.DE -> "Matrix-Legende"
                    Language.ZH -> "矩阵传奇"
                    else -> "Matrix Legend"
                },
                color = 0xFFE040FB,
                iconEmoji = ""
            )
            level >= 35 -> MasteryTitle(
                title = when (lang) {
                    Language.RU -> "Повелитель Блоков"
                    Language.UA -> "Володар Блоків"
                    Language.KK -> "Блок Әміршісі"
                    Language.DE -> "Block-Beherrscher"
                    Language.ZH -> "方块霸主"
                    else -> "Block Overlord"
                },
                color = 0xFFFF3D00,
                iconEmoji = ""
            )
            level >= 20 -> MasteryTitle(
                title = when (lang) {
                    Language.RU -> "Гроссмейстер"
                    Language.UA -> "Гросмейстер"
                    Language.KK -> "Гроссмейстер"
                    Language.DE -> "Großmeister"
                    Language.ZH -> "特级大师"
                    else -> "Grandmaster"
                },
                color = 0xFF00E5FF,
                iconEmoji = ""
            )
            level >= 10 -> MasteryTitle(
                title = when (lang) {
                    Language.RU -> "Мастер Блоков"
                    Language.UA -> "Майстер Блоків"
                    Language.KK -> "Блок Шебері"
                    Language.DE -> "Block-Meister"
                    Language.ZH -> "方块大师"
                    else -> "Block Master"
                },
                color = 0xFFFFD700,
                iconEmoji = ""
            )
            level >= 5 -> MasteryTitle(
                title = when (lang) {
                    Language.RU -> "Адепт"
                    Language.UA -> "Адепт"
                    Language.KK -> "Адепт"
                    Language.DE -> "Lehrling"
                    Language.ZH -> "学徒"
                    else -> "Apprentice"
                },
                color = 0xFF80DEEA,
                iconEmoji = ""
            )
            else -> MasteryTitle(
                title = when (lang) {
                    Language.RU -> "Новичок"
                    Language.UA -> "Новачок"
                    Language.KK -> "Жаңадан бастаушы"
                    Language.DE -> "Neuling"
                    Language.ZH -> "新手"
                    else -> "Novice"
                },
                color = 0xFFCD7F32,
                iconEmoji = ""
            )
        }
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java, "tetris-db"
    ).fallbackToDestructiveMigration(true).build()

    private val scoreRepo = ScoreRepository(db.highScoreDao())
    val topScores = scoreRepo.topScores

    private val accountRepo = com.example.db.AccountRepository(db.userAccountDao())
    val allAccounts = accountRepo.allAccounts

    // Кэшируем FirebaseAuth — не дёргаем getInstance() каждый раз / cached auth instance
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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

    fun setPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

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

    private val _purchasedThemes = MutableStateFlow(ALL_THEMES)
    val purchasedThemes = _purchasedThemes.asStateFlow()

    private val _purchasedFonts = MutableStateFlow(setOf("default", "monospace"))
    val purchasedFonts = _purchasedFonts.asStateFlow()

    private val _purchasedControlButtonStyles = MutableStateFlow(setOf("classic", "neon"))
    val purchasedControlButtonStyles = _purchasedControlButtonStyles.asStateFlow()

    private val _purchasedRanks = MutableStateFlow(setOf("BRONZE"))
    val purchasedRanks = _purchasedRanks.asStateFlow()

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

    private val _blockStyle = MutableStateFlow("material")
    val blockStyle = _blockStyle.asStateFlow()

    private val _nextCount = MutableStateFlow(3)
    val nextCount = _nextCount.asStateFlow()

    private val _ghostVisible = MutableStateFlow(true)
    val ghostVisible = _ghostVisible.asStateFlow()

    private val _ghostOutlineOnly = MutableStateFlow(true)
    val ghostOutlineOnly = _ghostOutlineOnly.asStateFlow()

    private val _controlStyle = MutableStateFlow("split")
    val controlStyle = _controlStyle.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled = _vibrationEnabled.asStateFlow()

    private val _onlineTier = MutableStateFlow("BRONZE")
    val onlineTier = _onlineTier.asStateFlow()

    private val _onlineRating = MutableStateFlow(1000)
    val onlineRating = _onlineRating.asStateFlow()

    private val _winStreak = MutableStateFlow(0)
    val winStreak = _winStreak.asStateFlow()

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

    private val _controlDas = MutableStateFlow(160)
    val controlDas = _controlDas.asStateFlow()

    private val _controlArr = MutableStateFlow(35)
    val controlArr = _controlArr.asStateFlow()

    private val _controlBottomPadding = MutableStateFlow(16)
    val controlBottomPadding = _controlBottomPadding.asStateFlow()

    private val _relaxAmbientSound = MutableStateFlow("cosmic")
    val relaxAmbientSound = _relaxAmbientSound.asStateFlow()

    private val _screenShakeIntensity = MutableStateFlow(1.0f)
    val screenShakeIntensity = _screenShakeIntensity.asStateFlow()

    private val _scanlinesFilter = MutableStateFlow(false)
    val scanlinesFilter = _scanlinesFilter.asStateFlow()

    private val _graphicsQuality = MutableStateFlow("medium")
    val graphicsQuality = _graphicsQuality.asStateFlow()

    private val _showNewSection = MutableStateFlow(true)
    val showNewSection = _showNewSection.asStateFlow()

    private val _newGameUiEnabled = MutableStateFlow(false)
    val newGameUiEnabled = _newGameUiEnabled.asStateFlow()

    private val _boardColorSkin = MutableStateFlow("cyberpunk")
    val boardColorSkin = _boardColorSkin.asStateFlow()

    private val _statsClearedLines = MutableStateFlow(0)
    val statsClearedLines = _statsClearedLines.asStateFlow()

    private val _statsHighScore = MutableStateFlow(0)
    val statsHighScore = _statsHighScore.asStateFlow()

    private val _statsGamesPlayed = MutableStateFlow(0)
    val statsGamesPlayed = _statsGamesPlayed.asStateFlow()

    private var hasAwardedCurrentGameReward = false
    private var hasAwardedBlockBlastReward = false

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

    // Подтверждена ли почта / is email verified flag
    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified = _isEmailVerified.asStateFlow()

    // Показать баннер "проверьте почту" / show "check inbox" banner after registration
    private val _showVerificationBanner = MutableStateFlow(false)
    val showVerificationBanner = _showVerificationBanner.asStateFlow()

    private val _globalScores = MutableStateFlow<List<HighScore>>(emptyList())
    val globalScores = _globalScores.asStateFlow()

    private val _firebaseUsers = MutableStateFlow<List<Map<String, Any>>>(emptyList())
    val firebaseUsers = _firebaseUsers.asStateFlow()

    private val _isLoadingFirebaseUsers = MutableStateFlow(false)
    val isLoadingFirebaseUsers = _isLoadingFirebaseUsers.asStateFlow()

    private val _adminErrorMessage = MutableStateFlow<String?>(null)
    val adminErrorMessage = _adminErrorMessage.asStateFlow()

    fun isCurrentUserAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        val uid = user.uid
        val email = user.email?.lowercase() ?: ""
        return uid == "ge9Lzx5EkCfbINDZEG6I8vYcJCd2" || 
               (user.isEmailVerified && email == "ezik02021@gmail.com")
    }

    fun isReservedAdminNickname(name: String): Boolean {
        val clean = name.trim().lowercase()
        if (clean.isEmpty()) return false
        val reserved = setOf(
            "fsfq", "admin", "administrator", "админ", "администратор",
            "system", "система", "moderator", "модератор", "developer",
            "разработчик", "support", "поддержка", "owner", "владелец", "создатель"
        )
        if (clean in reserved) return true
        if (clean.contains("fsfq") || clean.contains("админ") || clean.contains("admin")) {
            return true
        }
        return false
    }

    private val _isAdminSessionAuthenticated = MutableStateFlow(
        prefs.getBoolean("admin_session_persisted", false)
    )
    val isAdminSessionAuthenticated = _isAdminSessionAuthenticated.asStateFlow()

    fun setAdminSessionAuthenticated(value: Boolean) {
        if (value && !isCurrentUserAdmin()) {
            _isAdminSessionAuthenticated.value = false
            prefs.edit().putBoolean("admin_session_persisted", false).apply()
            return
        }
        _isAdminSessionAuthenticated.value = value
        prefs.edit().putBoolean("admin_session_persisted", value).apply()
    }

    private val _isMultiplayerUnlocked = MutableStateFlow(
        prefs.getString("multiplayer_access_key_v2", "") == "FsFq"
    )
    val isMultiplayerUnlocked = _isMultiplayerUnlocked.asStateFlow()

    fun unlockMultiplayerWithPassword(password: String): Boolean {
        val clean = password.trim()
        if (clean.isEmpty()) return false
        if (clean == "FsFq" || clean.equals("FsFq", ignoreCase = true)) {
            _isMultiplayerUnlocked.value = true
            prefs.edit()
                .putString("multiplayer_access_key_v2", "FsFq")
                .remove("multiplayer_beta_unlocked")
                .apply()
            return true
        }
        return false
    }

    /**
     * Records an intentional room exit. If player exits > 3 times within 1 hour,
     * imposes a 100 coin penalty to prevent room spam / dodging.
     * Returns true if penalty was applied, false otherwise.
     */
    fun recordRoomExitAndApplyPenaltyIfNeeded(): Boolean {
        val now = System.currentTimeMillis()
        val oneHourAgo = now - 3600_000L
        val historyStr = prefs.getString("mp_room_exit_timestamps", "") ?: ""
        val timestamps = historyStr.split(",")
            .mapNotNull { it.toLongOrNull() }
            .filter { it > oneHourAgo }
            .toMutableList()

        timestamps.add(now)
        val updatedHistory = timestamps.joinToString(",")
        prefs.edit().putString("mp_room_exit_timestamps", updatedHistory).apply()

        // If more than 3 exits in the past hour -> apply 100 coin penalty
        if (timestamps.size > 3) {
            val penalty = 100
            deductCreditsForPenalty(penalty)
            return true
        }
        return false
    }

    fun deductCreditsForPenalty(amount: Int) {
        if (amount <= 0) return
        if ((_credits.value xor CHECKSUM_MASK) != _creditsChecksum) {
            val restored = prefs.getInt("credits", 0)
            _credits.value = restored
            _creditsChecksum = restored xor CHECKSUM_MASK
        }
        val newCredits = maxOf(0, _credits.value - amount)
        setCreditsInternal(newCredits, syncToCloud = true)
        saveCurrentProfileToDb()
    }

    private val _hasNicknameGradient = MutableStateFlow(false)
    val hasNicknameGradient = _hasNicknameGradient.asStateFlow()

    private val _bonusXp = MutableStateFlow(0)
    val bonusXp = _bonusXp.asStateFlow()

    private val _prestigeLevel = MutableStateFlow(0)
    val prestigeLevel = _prestigeLevel.asStateFlow()

    // Anti-cheat memory protection & Real-time sync listener
    private val CHECKSUM_MASK = 0x5A5A5A5A
    private var _creditsChecksum: Int = 0
    private var userDocSnapshotListener: com.google.firebase.firestore.ListenerRegistration? = null
    private var lastLocalCreditsChangeTime: Long = 0L

    val incomeMultiplier: Float
        get() = when {
            _prestigeLevel.value >= 3 -> 8.0f
            _prestigeLevel.value == 2 -> 4.0f
            _prestigeLevel.value == 1 -> 2.0f
            else -> 1.0f
        }

    // Cloud DB sync indicator
    private val _isSyncingDb = MutableStateFlow(false)
    val isSyncingDb = _isSyncingDb.asStateFlow()

    // Public Profile state
    private val _selectedPublicProfile = MutableStateFlow<PublicUserProfile?>(null)
    val selectedPublicProfile = _selectedPublicProfile.asStateFlow()

    private val _isLoadingPublicProfile = MutableStateFlow(false)
    val isLoadingPublicProfile = _isLoadingPublicProfile.asStateFlow()

    // Friends system state
    private val _friendsList = MutableStateFlow<List<FriendUser>>(emptyList())
    val friendsList = _friendsList.asStateFlow()

    private val _friendRequests = MutableStateFlow<List<FriendUser>>(emptyList())
    val friendRequests = _friendRequests.asStateFlow()

    private val _showFriendsDialog = MutableStateFlow(false)
    val showFriendsDialog = _showFriendsDialog.asStateFlow()

    private val _showRewardedAdDialog = MutableStateFlow(false)
    val showRewardedAdDialog = _showRewardedAdDialog.asStateFlow()

    // Ads SDK connection state (Yandex Mobile Ads SDK connected)
    val isAdSdkConnected: Boolean = true
    val isRewardedAdLoaded = com.example.ads.YandexAdsManager.isAdLoaded
    val isAdLoading = com.example.ads.YandexAdsManager.isLoading

    fun openRewardedAdDialog() {
        com.example.ads.YandexAdsManager.loadRewardedAd()
        _showRewardedAdDialog.value = true
    }

    fun closeRewardedAdDialog() {
        _showRewardedAdDialog.value = false
    }

    fun loadRewardedAd() {
        com.example.ads.YandexAdsManager.loadRewardedAd()
    }

    fun showRewardedAd(
        activity: android.app.Activity,
        onRewarded: (amount: Int, type: String) -> Unit,
        onDismissed: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        com.example.ads.YandexAdsManager.showRewardedAd(activity, onRewarded, onDismissed, onError)
    }

    private val _showSignOutConfirmDialog = MutableStateFlow(false)
    val showSignOutConfirmDialog = _showSignOutConfirmDialog.asStateFlow()

    private val _crateKeys = MutableStateFlow<Map<String, Int>>(emptyMap())
    val crateKeys = _crateKeys.asStateFlow()

    fun getCrateKeyCount(crateId: String): Int {
        return _crateKeys.value[crateId.lowercase()] ?: 0
    }

    fun addCrateKeys(crateId: String, count: Int = 1) {
        val cid = crateId.lowercase()
        val current = _crateKeys.value.toMutableMap()
        current[cid] = (current[cid] ?: 0) + count
        _crateKeys.value = current
        saveCrateKeysToPrefs()
    }

    fun useCrateKey(crateId: String): Boolean {
        val cid = crateId.lowercase()
        val current = _crateKeys.value.toMutableMap()
        val count = current[cid] ?: 0
        if (count > 0) {
            current[cid] = count - 1
            _crateKeys.value = current
            saveCrateKeysToPrefs()
            return true
        }
        return false
    }

    private fun loadCrateKeysFromPrefs() {
        val map = mutableMapOf<String, Int>()
        val crates = listOf("wooden", "iron", "golden", "platinum", "legendary", "diamond", "red_crate_lite", "red_crate")
        for (c in crates) {
            val k = prefs.getInt("crate_key_$c", 0)
            if (k > 0) map[c] = k
        }
        _crateKeys.value = map
    }

    private fun saveCrateKeysToPrefs() {
        val editor = prefs.edit()
        val crates = listOf("wooden", "iron", "golden", "platinum", "legendary", "diamond", "red_crate_lite", "red_crate")
        for (c in crates) {
            val count = _crateKeys.value[c] ?: 0
            editor.putInt("crate_key_$c", count)
        }
        editor.apply()
        saveCurrentProfileToDb()
    }

    // --- Account Mastery & Leveling System ---
    private val _claimedLevelRewards = MutableStateFlow<Set<Int>>(emptySet())
    val claimedLevelRewards = _claimedLevelRewards.asStateFlow()

    fun getXpRequiredForLevel(level: Int): Int {
        return 500 + (level - 1) * 250
    }

    fun addMatchXp(score: Int, lines: Int, modeBonus: Int = 40) {
        val earnedXp = maxOf(15, (score / 40) + (lines * 12) + modeBonus)
        val currentAccXp = prefs.getInt("stats_accumulated_xp", 0) + earnedXp
        prefs.edit().putInt("stats_accumulated_xp", currentAccXp).apply()
        evaluateAchievements()
    }

    fun getPlayerTotalXp(): Int {
        val accumulatedXp = prefs.getInt("stats_accumulated_xp", 0)
        val totalLines = prefs.getInt("stats_cleared_lines", 0)
        val highscore = prefs.getInt("stats_high_score", 0)
        return accumulatedXp + (totalLines * 25) + (highscore / 10) + _bonusXp.value
    }

    fun getPlayerLevel(totalXp: Int = getPlayerTotalXp()): Int {
        var lvl = 1
        var remaining = totalXp
        while (remaining >= getXpRequiredForLevel(lvl)) {
            remaining -= getXpRequiredForLevel(lvl)
            lvl++
        }
        return lvl
    }

    fun getPlayerLevelProgress(totalXp: Int = getPlayerTotalXp()): Triple<Int, Int, Float> {
        var lvl = 1
        var remaining = totalXp
        while (remaining >= getXpRequiredForLevel(lvl)) {
            remaining -= getXpRequiredForLevel(lvl)
            lvl++
        }
        val needed = getXpRequiredForLevel(lvl)
        val progress = if (needed > 0) (remaining.toFloat() / needed.toFloat()).coerceIn(0f, 1f) else 0f
        return Triple(lvl, remaining, progress)
    }

    fun getLevelCreditMultiplier(): Float {
        val lvl = getPlayerLevel()
        return 1.0f + (lvl / 2) * 0.01f
    }

    fun getLevelReward(level: Int): LevelReward {
        return when {
            level == 5 -> LevelReward(5, 300, "wooden", 2)
            level == 10 -> LevelReward(10, 500, "iron", 1)
            level == 15 -> LevelReward(15, 750, "golden", 1)
            level == 20 -> LevelReward(20, 1000, "platinum", 1)
            level == 25 -> LevelReward(25, 1500, "platinum", 2)
            level == 30 -> LevelReward(30, 2000, "diamond", 1)
            level == 35 -> LevelReward(35, 2500, "diamond", 2)
            level == 40 -> LevelReward(40, 3000, "legendary", 1)
            level == 45 -> LevelReward(45, 4000, "legendary", 2)
            level >= 50 && level % 5 == 0 -> LevelReward(level, 5000, "legendary", 3)
            else -> LevelReward(level, 150 + (level * 15))
        }
    }

    fun getUnclaimedLevelRewards(): List<LevelReward> {
        val currentLvl = getPlayerLevel()
        val claimed = _claimedLevelRewards.value
        val list = mutableListOf<LevelReward>()
        for (lvl in 2..currentLvl) {
            if (!claimed.contains(lvl)) {
                list.add(getLevelReward(lvl))
            }
        }
        return list
    }

    fun claimLevelReward(level: Int): LevelReward? {
        val currentLvl = getPlayerLevel()
        if (level > currentLvl || level < 2 || _claimedLevelRewards.value.contains(level)) {
            return null
        }
        val reward = getLevelReward(level)
        addCredits(reward.credits)
        if (reward.keyType != null && reward.keyCount > 0) {
            addCrateKeys(reward.keyType, reward.keyCount)
        }
        val updated = _claimedLevelRewards.value + level
        _claimedLevelRewards.value = updated
        saveClaimedLevelRewardsToPrefs()
        triggerAudioFeedback("success")
        return reward
    }

    fun claimAllLevelRewards(): List<LevelReward> {
        val unclaimed = getUnclaimedLevelRewards()
        if (unclaimed.isEmpty()) return emptyList()
        var totalCoins = 0
        val keysToAdd = mutableMapOf<String, Int>()
        val claimedLevels = mutableSetOf<Int>()

        for (reward in unclaimed) {
            totalCoins += reward.credits
            if (reward.keyType != null && reward.keyCount > 0) {
                keysToAdd[reward.keyType] = (keysToAdd[reward.keyType] ?: 0) + reward.keyCount
            }
            claimedLevels.add(reward.level)
        }

        if (totalCoins > 0) addCredits(totalCoins)
        for ((k, count) in keysToAdd) {
            addCrateKeys(k, count)
        }

        val updated = _claimedLevelRewards.value + claimedLevels
        _claimedLevelRewards.value = updated
        saveClaimedLevelRewardsToPrefs()
        triggerAudioFeedback("success")
        return unclaimed
    }

    fun loadClaimedLevelRewardsFromPrefs() {
        val setStr = profilePrefs.getStringSet("claimed_level_rewards", emptySet()) ?: emptySet()
        _claimedLevelRewards.value = setStr.mapNotNull { it.toIntOrNull() }.toSet()
    }

    private fun saveClaimedLevelRewardsToPrefs() {
        val setStr = _claimedLevelRewards.value.map { it.toString() }.toSet()
        profilePrefs.edit().putStringSet("claimed_level_rewards", setStr).apply()
    }

    private class AchievementDef(
        val id: String,
        val titleEn: String,
        val titleRu: String,
        val descEn: String,
        val descRu: String,
        val target: Int,
        val icon: String,
        val rewardCredits: Int = 0,
        val crateKeyReward: String? = null,
        val crateKeyCount: Int = 0,
        val getCurrentVal: (MainViewModel) -> Int
    )

    private val achievementDefs = listOf(
        AchievementDef("classic_novice", "Lines Master", "Мастер линий", "Clear 10 or more total lines in Classic Match mode", "Уберите 10 или более линий в классическом режиме", 10, "lines", rewardCredits = 150) { vm -> vm.prefs.getInt("stats_cleared_lines", 0) },
        AchievementDef("score_tycoon", "Sizable Score", "Финансовый магнат", "Score 5,000 points or more in a single Tetris match", "Наберите 5000 или более очков в одном матче", 5000, "score", crateKeyReward = "iron", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_high_score", 0) },
        AchievementDef("extended_pioneer", "Pentamino Integrator", "Пионер Пентамино", "Clear 20 lines or score 2,000 points in Extended mode", "Очистите 20 линий или наберите 2000 очков в расширенном режиме", 1, "crown", rewardCredits = 250, crateKeyReward = "iron", crateKeyCount = 1) { vm -> if (vm.prefs.getBoolean("ach_extended_pioneer_unlocked", false)) 1 else 0 },
        AchievementDef("speed_runner", "Hyper-Speed Ace", "Ас Гиперскорости", "Score 3,000 points starting at speed 10 in Sprint mode", "Наберите 3000 очков на 10-й скорости в режиме Спринт", 1, "speed", rewardCredits = 250, crateKeyReward = "iron", crateKeyCount = 1) { vm -> if (vm.prefs.getBoolean("ach_speed_runner_unlocked", false)) 1 else 0 },
        AchievementDef("blast_tactician", "ZETA Expert", "Эксперт ZETA", "Earn 1,000 score points in ZETA mode", "Наберите 1000 очков в режиме ZETA", 1000, "blast", rewardCredits = 250) { vm -> vm.prefs.getInt("block_blast_high_score", 0) },
        AchievementDef("combo_king", "Combo 3x", "Комбо 3x", "Achieve a combo chain multiplier of 3x or higher in multiplayer simulator", "Достигните комбо-множителя 3x или выше в мультиплеере", 3, "combo", rewardCredits = 300) { vm -> if (vm.prefs.getBoolean("ach_combo_king_unlocked", false)) 1 else 0 },
        AchievementDef("grandmaster", "Grandmaster Tactician", "Гроссмейстер", "Score 15,000 points or more in Classic Match", "Наберите 15000 или более очков в классическом матче", 15000, "crown", rewardCredits = 500, crateKeyReward = "platinum", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_high_score", 0) },
        AchievementDef("blast_master", "ZETA Veteran", "Ветеран ZETA", "Earn 5,000 score points in ZETA mode", "Наберите 5000 очков в режиме ZETA", 5000, "blast", crateKeyReward = "platinum", crateKeyCount = 1) { vm -> vm.prefs.getInt("block_blast_high_score", 0) },
        AchievementDef("rich_player", "Elite Investor", "Элитный инвестор", "Save 2,000 credits in your balance", "Накопите не менее 2000 кредитов на балансе", 2000, "crown", crateKeyReward = "golden", crateKeyCount = 1) { vm -> vm.credits.value },
        AchievementDef("multiplayer_veteran", "Arena Fighter", "Боец Арены", "Win 3 online multiplayer matches", "Победите в 3 онлайн-матчах мультиплеера", 3, "combo", crateKeyReward = "golden", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_multiplayer_wins", 0) },
        AchievementDef("color_skin_collector", "Theme Collector", "Новая Тема", "Purchase your first custom board visual theme skin", "Приобретите свою первую уникальную тему оформления", 1, "crown", rewardCredits = 300) { vm -> if (vm.prefs.getBoolean("ach_color_skin_collector_unlocked", false)) 1 else 0 },
        AchievementDef("rank_conqueror", "New Rank", "Новый Ранг", "Upgrade your security node rank tier using credits", "Повысьте категорию своего узла за кредиты", 1, "crown", crateKeyReward = "platinum", crateKeyCount = 1) { vm -> if (vm.prefs.getBoolean("ach_rank_conqueror_unlocked", false)) 1 else 0 },
        
        // Progression achievements with balanced rewards and crate keys
        AchievementDef("games_played_5", "Novice Player", "Начинающий игрок", "Play 5 games total in any mode", "Сыграйте 5 игр в любом режиме", 5, "lines", crateKeyReward = "wooden", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_games_played", 0) },
        AchievementDef("games_played_25", "Experienced Player", "Опытный игрок", "Play 25 games total in any mode", "Сыграйте 25 игр в любом режиме", 25, "lines", crateKeyReward = "iron", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_games_played", 0) },
        AchievementDef("games_played_100", "FlowTess Legend", "Легенда FlowTess", "Play 100 games total in any mode", "Сыграйте 100 игр в любом режиме", 100, "lines", crateKeyReward = "legendary", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_games_played", 0) },
        AchievementDef("lines_50", "Line Sweeper", "Очиститель линий", "Clear 50 total lines across all matches", "Уберите 50 линий суммарно во всех играх", 50, "lines", crateKeyReward = "wooden", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_cleared_lines", 0) },
        AchievementDef("lines_200", "Line Shredder", "Уничтожитель линий", "Clear 200 total lines across all matches", "Уберите 200 линий суммарно во всех играх", 200, "lines", crateKeyReward = "iron", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_cleared_lines", 0) },
        AchievementDef("lines_1000", "Vortex Sweeper", "Вихревой очиститель", "Clear 1000 total lines across all matches", "Уберите 1000 линий суммарно во всех играх", 1000, "lines", rewardCredits = 500, crateKeyReward = "legendary", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_cleared_lines", 0) },
        AchievementDef("score_single_8000", "Point Collector", "Сборщик очков", "Score 8,000 points or more in a single match", "Наберите 8000 или более очков в одном матче", 8000, "score", crateKeyReward = "golden", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_high_score", 0) },
        AchievementDef("score_single_20000", "Score Master", "Мастер очков", "Score 20,000 points or more in a single match", "Наберите 20000 или более очков в одном матче", 20000, "score", crateKeyReward = "legendary", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_high_score", 0) },
        AchievementDef("score_single_50000", "Score Overlord", "Повелитель очков", "Score 50,000 points or more in a single match", "Наберите 50000 или более очков в одном матче", 50000, "score", rewardCredits = 1000, crateKeyReward = "diamond", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_high_score", 0) },
        AchievementDef("credits_accumulated_5000", "Credits Saver", "Сбережения", "Reach a balance of 5,000 credits", "Накопите баланс в 5000 кредитов", 5000, "crown", crateKeyReward = "platinum", crateKeyCount = 1) { vm -> vm.credits.value },
        AchievementDef("credits_accumulated_10000", "Credits Capitalist", "Крупный капитал", "Reach a balance of 10,000 credits", "Накопите баланс в 10000 кредитов", 10000, "crown", crateKeyReward = "legendary", crateKeyCount = 1) { vm -> vm.credits.value },
        AchievementDef("credits_spent_1000", "Shop Spender", "Покупатель", "Spend 1,000 credits in the store", "Потратьте 1000 кредитов в магазине", 1000, "crown", crateKeyReward = "iron", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_spent_credits", 0) },
        AchievementDef("credits_spent_5000", "Store Spender", "Активный покупатель", "Spend 5,000 credits in the store", "Потратьте 5000 кредитов в магазине", 5000, "crown", crateKeyReward = "platinum", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_spent_credits", 0) },
        AchievementDef("credits_spent_10000", "VIP Customer", "Постоянный клиент", "Spend 10,000 credits in the store", "Потратьте 10000 кредитов в магазине", 10000, "crown", crateKeyReward = "legendary", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_spent_credits", 0) },
        AchievementDef("tetrises_cleared_5", "Quadro Enthusiast", "Энтузиаст Квадро", "Perform 4-line clears (Quadro) 5 times", "Выполните очистку 4-х линий (Квадро) 5 раз", 5, "lines", rewardCredits = 300) { vm -> vm.prefs.getInt("stats_tetrises_count", 0) },
        AchievementDef("tetrises_cleared_25", "Quadro Champion", "Чемпион Квадро", "Perform 4-line clears (Quadro) 25 times", "Выполните очистку 4-х линий (Квадро) 25 раз", 25, "lines", crateKeyReward = "golden", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_tetrises_count", 0) },
        AchievementDef("tetrises_cleared_100", "Quadro Master", "Мастер Квадро", "Perform 4-line clears (Quadro) 100 times", "Выполните очистку 4-х линий (Квадро) 100 раз", 100, "lines", rewardCredits = 500, crateKeyReward = "legendary", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_tetrises_count", 0) },
        AchievementDef("blast_score_3000", "ZETA Professional", "Профессионал ZETA", "Reach 3,000 score in ZETA mode", "Наберите 3000 очков в режиме ZETA", 3000, "blast", crateKeyReward = "golden", crateKeyCount = 1) { vm -> vm.prefs.getInt("block_blast_high_score", 0) },
        AchievementDef("blast_score_10000", "ZETA Master", "Мастер ZETA", "Reach 10,000 score in ZETA mode", "Наберите 10000 очков в режиме ZETA", 10000, "blast", crateKeyReward = "legendary", crateKeyCount = 1) { vm -> vm.prefs.getInt("block_blast_high_score", 0) },
        AchievementDef("combo_multiplier_4", "Combo 4x", "Комбо 4x", "Achieve a combo chain multiplier of 4x in multiplayer simulator", "Достигните комбо-множителя 4x в мультиплеере", 4, "combo", crateKeyReward = "golden", crateKeyCount = 1) { vm -> if (vm.prefs.getBoolean("ach_combo_multiplier_4_unlocked", false)) 1 else 0 },
        AchievementDef("combo_multiplier_5", "Combo 5x", "Комбо 5x", "Achieve a combo chain multiplier of 5x in multiplayer simulator", "Достигните комбо-множителя 5x в мультиплеере", 5, "combo", crateKeyReward = "platinum", crateKeyCount = 1) { vm -> if (vm.prefs.getBoolean("ach_combo_multiplier_5_unlocked", false)) 1 else 0 },
        AchievementDef("avatar_changes_5", "Fashion Stylist", "Модный стилист", "Change your avatar emoji or color 5 times", "Измените эмодзи или цвет аватара 5 раз", 5, "crown", rewardCredits = 150) { vm -> vm.prefs.getInt("stats_avatar_changes", 0) },
        AchievementDef("title_purchases_3", "Title Collector", "Коллекционер титулов", "Purchase 3 different profile titles in store", "Приобретите 3 разных титула в магазине", 3, "crown", crateKeyReward = "golden", crateKeyCount = 1) { vm -> vm.purchasedTitles.value.size },
        AchievementDef("frame_purchases_3", "Frame Collector", "Коллекционер рамок", "Purchase 3 different avatar frames in store", "Приобретите 3 разных рамки в магазине", 3, "crown", crateKeyReward = "golden", crateKeyCount = 1) { vm -> vm.purchasedAvatarFrames.value.size },
        AchievementDef("cosmetics_collector", "Cosmetics Fan", "Икона стиля", "Purchase or unlock 3 themes or button styles", "Приобретите 3 темы или стиля кнопок", 3, "crown", crateKeyReward = "platinum", crateKeyCount = 1) { vm -> vm.purchasedThemes.value.size + vm.purchasedControlButtonStyles.value.size },
        AchievementDef("xp_earned_500", "Experience 500", "Опыт 500", "Earn 500 total Experience Points (XP)", "Наберите 500 очков опыта (XP) суммарно", 500, "speed", rewardCredits = 200) { vm -> vm.getPlayerTotalXp() },
        AchievementDef("xp_earned_2000", "Experience 2000", "Опыт 2000", "Earn 2,000 total Experience Points (XP)", "Наберите 2000 очков опыта (XP) суммарно", 2000, "speed", crateKeyReward = "golden", crateKeyCount = 1) { vm -> vm.getPlayerTotalXp() },
        AchievementDef("xp_earned_10000", "Experience 10000", "Опыт 10000", "Earn 10,000 total Experience Points (XP)", "Наберите 10000 очков опыта (XP) суммарно", 10000, "speed", crateKeyReward = "legendary", crateKeyCount = 1) { vm -> vm.getPlayerTotalXp() },
        AchievementDef("player_level_5", "Level 5", "Уровень 5", "Reach Player Level 5", "Достигните 5-го уровня", 5, "speed", rewardCredits = 250) { vm -> vm.getPlayerLevel() },
        AchievementDef("player_level_15", "Level 15", "Уровень 15", "Reach Player Level 15", "Достигните 15-го уровня", 15, "speed", crateKeyReward = "platinum", crateKeyCount = 1) { vm -> vm.getPlayerLevel() },
        AchievementDef("player_level_30", "Level 30", "Уровень 30", "Reach Player Level 30", "Достигните 30-го уровня", 30, "speed", crateKeyReward = "legendary", crateKeyCount = 1) { vm -> vm.getPlayerLevel() },
        AchievementDef("mode_time_attack", "Blitz Ace", "Ас Блица", "Score 2,500 points in Time Attack mode", "Наберите 2500 очков в режиме Блиц", 1, "speed", rewardCredits = 250, crateKeyReward = "iron", crateKeyCount = 1) { vm -> if (vm.prefs.getBoolean("ach_mode_time_attack_unlocked", false)) 1 else 0 },
        AchievementDef("mode_reverse", "Inversion Tactician", "Тактик Инверсии", "Clear 15 lines in Inversion mode", "Очистите 15 линий в режиме Инверсия", 1, "speed", rewardCredits = 250, crateKeyReward = "iron", crateKeyCount = 1) { vm -> if (vm.prefs.getBoolean("ach_mode_reverse_unlocked", false)) 1 else 0 },
        AchievementDef("mode_mirror", "Mirror Mind", "Зеркальный Разум", "Score 3,000 points in Mirror Dimension mode", "Наберите 3000 очков в Зеркальном Мире", 1, "speed", rewardCredits = 250, crateKeyReward = "iron", crateKeyCount = 1) { vm -> if (vm.prefs.getBoolean("ach_mode_mirror_unlocked", false)) 1 else 0 },
        AchievementDef("mode_relax", "Zen Master", "Мастер Дзен", "Clear 30 lines in Sandbox mode", "Очистите 30 линий в режиме Песочница", 1, "speed", rewardCredits = 200, crateKeyReward = "wooden", crateKeyCount = 2) { vm -> if (vm.prefs.getBoolean("ach_mode_relax_unlocked", false)) 1 else 0 },
        AchievementDef("mode_extended", "Blueprint Architect", "Архитектор Схем", "Solve 5 stages in Pattern Puzzle mode", "Пройдите 5 этапов в режиме Шаблон", 1, "speed", rewardCredits = 300, crateKeyReward = "golden", crateKeyCount = 1) { vm -> if (vm.prefs.getBoolean("ach_pattern_solver_unlocked", false) || vm.prefs.getInt("stats_pattern_solved", 0) >= 5) 1 else 0 },
        AchievementDef("speed_level_max", "Speed Master", "Мастер скорости", "Reach game level speed 15 in standard match modes", "Достигните 15-го игрового уровня скорости в матче", 15, "speed", crateKeyReward = "platinum", crateKeyCount = 1) { vm -> vm.prefs.getInt("stats_max_speed_reached", 0) },
        
        // Final epic 50th achievement
        AchievementDef("millionaire", "Millionaire", "Миллионер", "Save 1,000,000 credits to unlock Prestige III (Voluntary reset for x8 Multiplier + Custom Tag)", "Накопите 1,000,000 кредитов (Открывает Престиж III: добровольный сброс за x8 доход и Личный Тег)", ShopPrices.PRESTIGE_III_REQUIREMENT, "crown", rewardCredits = 0) { vm -> vm.credits.value },
        AchievementDef("all_unlocked", "Champion", "Чемпион", "Obtain all 49 other achievements (Epic Completion Reward)", "Откройте все 49 других достижений (Эпическая финальная награда)", 1, "crown", rewardCredits = 5000, crateKeyReward = "red_crate", crateKeyCount = 1) { vm -> if (vm.prefs.getBoolean("ach_all_unlocked_unlocked", false)) 1 else 0 }
    )

    init {
        val savedLangCode = prefs.getString("lang_code", null)
        val resolvedLang = if (savedLangCode == null) {
            val sysLang = getSystemDefaultLanguage()
            prefs.edit().putString("lang_code", sysLang.code).apply()
            sysLang
        } else {
            Language.entries.firstOrNull { it.code == savedLangCode } ?: Language.EN
        }
        _language.update { resolvedLang }
        _playerName.update { prefs.getString("player_name", "Player 1") ?: "Player 1" }
        _hasSavedGame.update { prefs.getBoolean("has_saved_game", false) }
        _themeColor.update { prefs.getString("theme_color", "indigo") ?: "indigo" }
        _blockStyle.update { prefs.getString("block_style", "neon") ?: "neon" }
        _nextCount.update { prefs.getInt("next_count", 3) }
        _ghostVisible.update { prefs.getBoolean("ghost_visible", true) }
        _ghostOutlineOnly.update { prefs.getBoolean("ghost_outline_only", true) }
        _controlStyle.update {
            val s = prefs.getString("control_style", "split") ?: "split"
            if (s == "swipe_hybrid") "split" else s
        }
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
        _controlDas.update { prefs.getInt("control_das", 160) }
        _controlArr.update { prefs.getInt("control_arr", 35) }
        _controlBottomPadding.update { prefs.getInt("control_bottom_padding", 16) }
        
        _screenShakeIntensity.update { prefs.getFloat("screen_shake_intensity", 1.0f) }
        _statsClearedLines.update { prefs.getInt("stats_cleared_lines", 0) }
        _statsHighScore.update { prefs.getInt("stats_high_score", 0) }
        _statsGamesPlayed.update { prefs.getInt("stats_games_played", 0) }
        _scanlinesFilter.update { prefs.getBoolean("scanlines_filter", false) }
        _graphicsQuality.update { prefs.getString("graphics_quality", "medium") ?: "medium" }
        _boardColorSkin.update { prefs.getString("board_color_skin", "cyberpunk") ?: "cyberpunk" }
        _showNewSection.update { prefs.getBoolean("show_new_section", true) }
        _newGameUiEnabled.update { prefs.getBoolean("new_game_ui_enabled", false) }

        // Relax settings
        _relaxImmortal.update { prefs.getBoolean("relax_immortal", true) }
        _relaxSpeed.update { prefs.getString("relax_speed", "slow") ?: "slow" }
        _relaxBlockSet.update { prefs.getString("relax_block_set", "ideal") ?: "ideal" }
        _relaxGhostEnabled.update { prefs.getBoolean("relax_ghost_enabled", true) }
        _relaxAmbientSound.update { prefs.getString("relax_ambient_sound", "cosmic") ?: "cosmic" }
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
        _purchasedThemes.update { ALL_THEMES }
        _purchasedFonts.update { profilePrefs.getStringSet("purchased_fonts", setOf("default", "monospace")) ?: setOf("default", "monospace") }
        _purchasedControlButtonStyles.update { profilePrefs.getStringSet("purchased_control_button_styles", setOf("classic", "neon")) ?: setOf("classic", "neon") }
        _customAvatarEmoji.update { profilePrefs.getString("custom_avatar_emoji", "") ?: "" }
        _customAvatarBgColor.update { profilePrefs.getString("custom_avatar_bg_color", "3A3C44") ?: "3A3C44" }
        _onlineTier.update { profilePrefs.getString("online_tier", "BRONZE") ?: "BRONZE" }
        _hasNicknameGradient.update { profilePrefs.getBoolean("has_nickname_gradient", false) }
        _bonusXp.update { profilePrefs.getInt("bonus_xp", 0) }
        _prestigeLevel.update { profilePrefs.getInt("prestige_level", 0) }
        _creditsChecksum = _credits.value xor CHECKSUM_MASK

        // Migration: Ensure only "classic" is unlocked by default, all other modes paid
        if (!profilePrefs.getBoolean("migrated_paid_modes_v1", false)) {
            val curModes = profilePrefs.getStringSet("purchased_modes", null)
            if (curModes != null) {
                val preserved = curModes.filter { it == "classic" || it == "mirror" || it == "perfectionist" }.toSet()
                profilePrefs.edit()
                    .putStringSet("purchased_modes", if (preserved.isEmpty()) setOf("classic") else preserved)
                    .putBoolean("migrated_paid_modes_v1", true)
                    .apply()
            } else {
                profilePrefs.edit()
                    .putStringSet("purchased_modes", setOf("classic"))
                    .putBoolean("migrated_paid_modes_v1", true)
                    .apply()
            }
        }

        loadCrateKeysFromPrefs()
        loadClaimedLevelRewardsFromPrefs()
        loadPurchasedRanksFromPrefs()
        loadAchievements()
        
        gameEngine.lineClearChallenge = _lineClearChallenge.value
        gameEngine.fastDropLockSpeed = _fastDropLockSpeed.value
        
        auth.addAuthStateListener { firebaseAuth ->
            val u = firebaseAuth.currentUser
            if (u != null) {
                startUserDocListener(u.uid)
            } else {
                userDocSnapshotListener?.remove()
                userDocSnapshotListener = null
            }
        }

        viewModelScope.launch {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                try {
                    _isAuthLoading.value = true
                    // Перезагружаем профиль — проверяем верификацию почты / reload to check email verification
                    try { currentUser.reload() } catch (_: Exception) {}
                    val data = com.example.db.FirebaseSync.pullUserData(getApplication())
                    val isVerified = currentUser.isEmailVerified ||
                        (data?.get("email_verified") as? Boolean == true) ||
                        (data?.get("is_verified") as? Boolean == true) ||
                        (data?.get("emailVerified") as? Boolean == true) ||
                        profilePrefs.getBoolean("is_email_verified", false)
                    _isEmailVerified.value = isVerified
                    if (isVerified) {
                        _showVerificationBanner.value = false
                        profilePrefs.edit().putBoolean("is_email_verified", true).apply()
                    }
                    val resolvedUsername = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "FirebaseUser"
                    val localCredits = prefs.getInt("credits", 750)
                    val cloudCredits = (data?.get("credits") as? Number)?.toInt()
                    val resolvedCredits = cloudCredits ?: localCredits
                    val resolvedTier = (data?.get("online_tier") as? String) ?: profilePrefs.getString("online_tier", "BRONZE") ?: "BRONZE"
                    val resolvedHasGradient = data?.get("has_nickname_gradient") as? Boolean ?: profilePrefs.getBoolean("has_nickname_gradient", false)
                    val resolvedBonusXp = (data?.get("bonus_xp") as? Number)?.toInt() ?: profilePrefs.getInt("bonus_xp", 0)
                    val resolvedTag = (data?.get("custom_tag") as? String) ?: profilePrefs.getString("custom_tag", "") ?: ""
                    val resolvedTagUnlocked = (data?.get("custom_tag_unlocked") as? Boolean) ?: profilePrefs.getBoolean("custom_tag_unlocked", false)
                    _customTag.update { resolvedTag }
                    _customTagUnlocked.update { resolvedTagUnlocked }
                    switchAccount(resolvedUsername, resolvedTier, resolvedCredits, resolvedHasGradient, resolvedBonusXp)
                    startUserDocListener(currentUser.uid)
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

    private fun startUserDocListener(uid: String) {
        if (uid.isEmpty()) return
        userDocSnapshotListener?.remove()
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        userDocSnapshotListener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                // Ignore local pending writes to prevent rollback jitter
                if (snapshot.metadata.hasPendingWrites()) return@addSnapshotListener
                val data = snapshot.data ?: return@addSnapshotListener

                // 1. Credits from server (authoritative sync)
                val cloudCredits = (data["credits"] as? Number)?.toInt()
                if (cloudCredits != null && cloudCredits != _credits.value) {
                    // Do not overwrite if local credit transaction occurred recently (within 20 seconds)
                    if (System.currentTimeMillis() - lastLocalCreditsChangeTime < 20000L) {
                        return@addSnapshotListener
                    }
                    setCreditsInternal(cloudCredits, syncToCloud = false)
                }

                // 2. Prestige level
                val cloudPrestige = (data["prestige_level"] as? Number)?.toInt()
                if (cloudPrestige != null && cloudPrestige != _prestigeLevel.value) {
                    _prestigeLevel.value = cloudPrestige
                    profilePrefs.edit().putInt("prestige_level", cloudPrestige).apply()
                    prefs.edit().putInt("prestige_level", cloudPrestige).apply()
                }

                // 3. Online Tier / Rank
                val cloudTier = data["online_tier"] as? String
                if (cloudTier != null && cloudTier != _onlineTier.value) {
                    _onlineTier.value = cloudTier
                    profilePrefs.edit().putString("online_tier", cloudTier).apply()
                    prefs.edit().putString("online_tier", cloudTier).apply()
                }

                // 4. Bonus XP
                val cloudBonusXp = (data["bonus_xp"] as? Number)?.toInt()
                if (cloudBonusXp != null && cloudBonusXp != _bonusXp.value) {
                    _bonusXp.value = cloudBonusXp
                    profilePrefs.edit().putInt("bonus_xp", cloudBonusXp).apply()
                }

                // 5. Custom Tag
                val cloudTag = data["custom_tag"] as? String
                if (cloudTag != null && cloudTag != _customTag.value) {
                    _customTag.value = cloudTag
                    profilePrefs.edit().putString("custom_tag", cloudTag).apply()
                }
                val cloudTagUnlocked = data["custom_tag_unlocked"] as? Boolean
                if (cloudTagUnlocked != null && cloudTagUnlocked != _customTagUnlocked.value) {
                    _customTagUnlocked.value = cloudTagUnlocked
                    profilePrefs.edit().putBoolean("custom_tag_unlocked", cloudTagUnlocked).apply()
                }

                // 6. Nickname Gradient
                val cloudGradient = data["has_nickname_gradient"] as? Boolean
                if (cloudGradient != null && cloudGradient != _hasNicknameGradient.value) {
                    _hasNicknameGradient.value = cloudGradient
                    profilePrefs.edit().putBoolean("has_nickname_gradient", cloudGradient).apply()
                }

                // 7. Email Verification status from Firestore
                val cloudVerified = (data["email_verified"] as? Boolean)
                    ?: (data["is_verified"] as? Boolean)
                    ?: (data["emailVerified"] as? Boolean)
                if (cloudVerified != null && cloudVerified != _isEmailVerified.value) {
                    _isEmailVerified.value = cloudVerified
                    profilePrefs.edit().putBoolean("is_email_verified", cloudVerified).apply()
                    if (cloudVerified) {
                        _showVerificationBanner.value = false
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
            .putString("block_style", "material")
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
            .putBoolean("show_new_section", true)
            .putBoolean("new_game_ui_enabled", false)
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
        _blockStyle.value = "material"
        _nextCount.value = 3
        _ghostVisible.value = true
        _ghostOutlineOnly.value = true
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
        _showNewSection.value = true
        _newGameUiEnabled.value = false
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

    fun setNewGameUiEnabled(enabled: Boolean) {
        _newGameUiEnabled.value = enabled
        prefs.edit().putBoolean("new_game_ui_enabled", enabled).apply()
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

    fun clearRelaxLowerRows() {
        gameEngine.clearLowerRows(8)
    }

    fun setRelaxAmbientSound(sound: String) {
        _relaxAmbientSound.value = sound
        prefs.edit().putString("relax_ambient_sound", sound).apply()
    }

    fun setControlDas(das: Int) {
        _controlDas.value = das
        prefs.edit().putInt("control_das", das).apply()
    }

    fun setControlArr(arr: Int) {
        _controlArr.value = arr
        prefs.edit().putInt("control_arr", arr).apply()
    }

    fun setControlBottomPadding(padding: Int) {
        _controlBottomPadding.value = padding
        prefs.edit().putInt("control_bottom_padding", padding).apply()
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

    fun setPurchasedRanks(ranks: Set<String>) {
        _purchasedRanks.value = ranks
        profilePrefs.edit().putStringSet("purchased_ranks", ranks).apply()
        saveCurrentProfileToDb()
    }

    fun addPurchasedRank(rankId: String) {
        val updated = _purchasedRanks.value + rankId
        _purchasedRanks.value = updated
        profilePrefs.edit().putStringSet("purchased_ranks", updated).apply()
        saveCurrentProfileToDb()
    }

    fun loadPurchasedRanksFromPrefs() {
        val rankOrder = listOf("BRONZE", "SILVER", "GOLD", "PLATINUM", "DIAMOND", "MASTER", "GRANDMASTER", "CHALLENGER")
        val currentTier = profilePrefs.getString("online_tier", "BRONZE") ?: "BRONZE"
        val currentIdx = rankOrder.indexOf(currentTier)
        val defaultRanks = if (currentIdx >= 0) {
            rankOrder.subList(0, currentIdx + 1).toSet()
        } else {
            setOf("BRONZE")
        }
        val saved = profilePrefs.getStringSet("purchased_ranks", defaultRanks) ?: defaultRanks
        _purchasedRanks.value = saved + defaultRanks
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

    fun setGhostOutlineOnly(outlineOnly: Boolean) {
        _ghostOutlineOnly.value = outlineOnly
        prefs.edit().putBoolean("ghost_outline_only", outlineOnly).apply()
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

    fun setShowNewSection(enabled: Boolean) {
        _showNewSection.value = enabled
        prefs.edit().putBoolean("show_new_section", enabled).apply()
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

    fun exportControlConfigCode(): String {
        return try {
            val json = JSONObject().apply {
                put("v", 1)
                put("style", _controlStyle.value)
                put("scale", (_controlButtonScale.value * 100).toInt())
                put("alpha", (_controlButtonAlpha.value * 100).toInt())
                put("btn", _controlButtonStyle.value)
                put("pos", _controlVerticalPosition.value)
                put("left", _leftHandedControls.value)
                put("das", _controlDas.value)
                put("arr", _controlArr.value)
                put("pad", _controlBottomPadding.value)
            }
            val base64 = Base64.encodeToString(
                json.toString().toByteArray(Charsets.UTF_8),
                Base64.NO_WRAP
            )
            "TTR-CTRL:$base64"
        } catch (e: Exception) {
            ""
        }
    }

    fun importControlConfigCode(rawCode: String): Boolean {
        return try {
            val trimmed = rawCode.trim()
            if (trimmed.isEmpty()) return false

            val payload = when {
                trimmed.startsWith("TTR-CTRL:", ignoreCase = true) -> trimmed.substring("TTR-CTRL:".length).trim()
                trimmed.startsWith("TETRIS-CTRL:", ignoreCase = true) -> trimmed.substring("TETRIS-CTRL:".length).trim()
                trimmed.startsWith("TTR:", ignoreCase = true) -> trimmed.substring("TTR:".length).trim()
                else -> trimmed
            }

            val jsonStr = if (payload.startsWith("{") && payload.endsWith("}")) {
                payload
            } else {
                String(Base64.decode(payload, Base64.DEFAULT), Charsets.UTF_8)
            }

            val json = JSONObject(jsonStr)

            if (json.has("style")) {
                val style = json.getString("style")
                val validStyles = setOf("split", "classic", "arcade", "one_hand_right", "one_hand_left", "claw_pro")
                setControlStyle(if (style in validStyles) style else "split")
            }
            if (json.has("scale")) {
                val rawScale = json.getDouble("scale")
                val scale = if (rawScale > 10.0) (rawScale / 100.0).toFloat() else rawScale.toFloat()
                setControlButtonScale(scale.coerceIn(0.5f, 1.5f))
            }
            if (json.has("alpha")) {
                val rawAlpha = json.getDouble("alpha")
                val alpha = if (rawAlpha > 1.0) (rawAlpha / 100.0).toFloat() else rawAlpha.toFloat()
                setControlButtonAlpha(alpha.coerceIn(0.1f, 1.0f))
            }
            if (json.has("btn")) {
                val btn = json.getString("btn")
                val validBtnStyles = setOf("neon", "classic", "glass", "gold", "plasma")
                setControlButtonStyle(if (btn in validBtnStyles) btn else "neon")
            }
            if (json.has("pos")) {
                val pos = json.getString("pos")
                val validPos = setOf("bottom", "middle", "top")
                setControlVerticalPosition(if (pos in validPos) pos else "bottom")
            }
            if (json.has("left")) {
                setLeftHandedControls(json.getBoolean("left"))
            }
            if (json.has("das")) {
                setControlDas(json.getInt("das").coerceIn(60, 350))
            }
            if (json.has("arr")) {
                setControlArr(json.getInt("arr").coerceIn(10, 100))
            }
            if (json.has("pad")) {
                setControlBottomPadding(json.getInt("pad").coerceIn(0, 80))
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun resetControlConfigToDefaults() {
        setControlStyle("split")
        setControlButtonScale(1.0f)
        setControlButtonAlpha(1.0f)
        setControlButtonStyle("neon")
        setControlVerticalPosition("bottom")
        setLeftHandedControls(false)
        setControlDas(160)
        setControlArr(35)
        setControlBottomPadding(16)
    }

    fun setScreenShakeIntensity(v: Float) {
        _screenShakeIntensity.value = v
        prefs.edit().putFloat("screen_shake_intensity", v).apply()
    }

    fun setScanlinesFilter(enabled: Boolean) {
        _scanlinesFilter.value = enabled
        prefs.edit().putBoolean("scanlines_filter", enabled).apply()
    }

    fun setGraphicsQuality(quality: String) {
        _graphicsQuality.value = quality
        prefs.edit().putString("graphics_quality", quality).apply()
        if (quality == "low") {
            _scanlinesFilter.value = false
            prefs.edit().putBoolean("scanlines_filter", false).apply()
            _screenShakeIntensity.value = 0f
            prefs.edit().putFloat("screen_shake_intensity", 0f).apply()
        }
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
                pointsReward = def.rewardCredits,
                crateKeyReward = def.crateKeyReward,
                crateKeyCount = def.crateKeyCount,
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
                addCredits(5000)
                addCrateKeys("red_crate", 1)
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
                    unlockAchievement(def.id, def.rewardCredits, def.crateKeyReward, def.crateKeyCount)
                }
            }
        }
        loadAchievements()
    }

    fun unlockAchievement(id: String, rewardCredits: Int = 0, crateKeyReward: String? = null, crateKeyCount: Int = 0) {
        val key = "ach_${id}_unlocked"
        if (!prefs.getBoolean(key, false)) {
            prefs.edit().putBoolean(key, true).apply()
            if (rewardCredits > 0) {
                addCredits(rewardCredits)
            }
            if (!crateKeyReward.isNullOrBlank() && crateKeyCount > 0) {
                addCrateKeys(crateKeyReward, crateKeyCount)
            }
            loadAchievements()
        }
    }

    fun checkMultiplayerCombo(combo: Int) {
        if (combo >= 3) {
            prefs.edit().putBoolean("ach_combo_king_unlocked", true).apply()
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
        evaluateAchievements()
    }

    fun setCreditsInternal(amount: Int, syncToCloud: Boolean = true) {
        val safeAmount = amount.coerceAtLeast(0)
        lastLocalCreditsChangeTime = System.currentTimeMillis()
        _credits.value = safeAmount
        _creditsChecksum = safeAmount xor CHECKSUM_MASK
        prefs.edit().putInt("credits", safeAmount).apply()
        if (syncToCloud) {
            syncCreditsToCloud(safeAmount)
        }
    }

    private fun syncCreditsToCloud(newCredits: Int) {
        val uid = auth.currentUser?.uid ?: return
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users").document(uid)
            .update("credits", newCredits)
            .addOnFailureListener {
                firestore.collection("users").document(uid)
                    .set(mapOf("credits" to newCredits), com.google.firebase.firestore.SetOptions.merge())
            }
    }

    fun addCredits(amount: Int) {
        if (amount <= 0) return
        // Anti-cheat client validation: memory integrity check
        if ((_credits.value xor CHECKSUM_MASK) != _creditsChecksum) {
            val restored = prefs.getInt("credits", 0)
            _credits.value = restored
            _creditsChecksum = restored xor CHECKSUM_MASK
        }
        // Clamped single increment to block external parameter injection
        val safeAmount = amount.coerceIn(1, 100000)
        val multiplied = (safeAmount * incomeMultiplier).toInt().coerceAtLeast(safeAmount)
        setCreditsInternal(_credits.value + multiplied, syncToCloud = true)
        saveCurrentProfileToDb()
        evaluateAchievements()
    }

    fun addRawCredits(amount: Int) {
        if (amount <= 0) return
        if ((_credits.value xor CHECKSUM_MASK) != _creditsChecksum) {
            val restored = prefs.getInt("credits", 0)
            _credits.value = restored
            _creditsChecksum = restored xor CHECKSUM_MASK
        }
        val safeAmount = amount.coerceIn(1, 1000000)
        setCreditsInternal(_credits.value + safeAmount, syncToCloud = true)
        saveCurrentProfileToDb()
        evaluateAchievements()
    }

    fun activatePrestige(targetLevel: Int) {
        val req = when (targetLevel) {
            1 -> com.example.ui.ShopPrices.PRESTIGE_I_REQUIREMENT
            2 -> com.example.ui.ShopPrices.PRESTIGE_II_REQUIREMENT
            3 -> com.example.ui.ShopPrices.PRESTIGE_III_REQUIREMENT
            else -> Int.MAX_VALUE
        }
        if (_credits.value < req) return

        setCreditsInternal(0, syncToCloud = true)
        if (targetLevel >= 3) {
            setCustomTagUnlocked(true)
        }
        profilePrefs.edit().putInt("prestige_level", targetLevel).apply()
        prefs.edit().putInt("prestige_level", targetLevel).apply()
        _prestigeLevel.value = targetLevel
        saveCurrentProfileToDb()
        viewModelScope.launch {
            com.example.db.FirebaseSync.pushUserData(getApplication())
        }
    }

    fun activatePrestige1() = activatePrestige(1)
    fun activatePrestige2() = activatePrestige(2)
    fun activatePrestige3() = activatePrestige(3)

    fun spendCredits(amount: Int): Boolean {
        if (amount <= 0) return false
        // Anti-cheat client validation: memory integrity check
        if ((_credits.value xor CHECKSUM_MASK) != _creditsChecksum) {
            val restored = prefs.getInt("credits", 0)
            _credits.value = restored
            _creditsChecksum = restored xor CHECKSUM_MASK
        }
        if (_credits.value >= amount) {
            setCreditsInternal(_credits.value - amount, syncToCloud = true)
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
        val trimmed = name.trim()
        val safeName = if (isReservedAdminNickname(trimmed) && !isCurrentUserAdmin()) {
            "Player 1"
        } else {
            trimmed
        }
        _playerName.update { safeName }
        prefs.edit().putString("player_name", safeName).apply()
        saveCurrentProfileToDb()
    }

    fun saveCurrentProfileToDb() {
        viewModelScope.launch {
            _isSyncingDb.value = true
            val pName = _playerName.value.trim()
            val currentUser = auth.currentUser
            if (pName.isNotEmpty()) {
                val existing = accountRepo.getAccount(pName)
                val pass = existing?.password ?: "1234"
                val acc = com.example.db.UserAccount(
                    username = pName,
                    password = pass,
                    avatarColor = _customAvatarBgColor.value,
                    avatarEmoji = _customAvatarEmoji.value,
                    avatarFrame = _equippedAvatarFrame.value,
                    onlineTier = _onlineTier.value,
                    title = _equippedTitle.value,
                    customTag = _customTag.value,
                    credits = _credits.value,
                    hasGradient = _hasNicknameGradient.value,
                    bonusXp = _bonusXp.value,
                    uid = currentUser?.uid ?: ""
                )
                accountRepo.insert(acc)
                // Sync data with Cloud Firestore
                com.example.db.FirebaseSync.pushUserData(getApplication())

                // Also update user profile in high_scores document if present
                if (currentUser != null) {
                    val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val avatarFile = java.io.File(getApplication<android.app.Application>().filesDir, "custom_avatar_${pName}.jpg")
                    val avatarBase64 = com.example.db.FirebaseSync.fileToBase64(avatarFile)
                    val updates = mapOf(
                        "playerName" to pName,
                        "hasGradient" to _hasNicknameGradient.value,
                        "customTag" to _customTag.value,
                        "avatarEmoji" to _customAvatarEmoji.value,
                        "avatarBgColor" to _customAvatarBgColor.value,
                        "avatarFrame" to _equippedAvatarFrame.value,
                        "avatarBase64" to (avatarBase64 ?: ""),
                        "onlineTier" to _onlineTier.value,
                        "title" to _equippedTitle.value,
                        "credits" to _credits.value,
                        "last_synced_timestamp" to System.currentTimeMillis(),
                        "is_online" to true,
                        "app_version_code" to com.example.BuildConfig.VERSION_CODE
                    )
                    firestore.collection("high_scores").document(currentUser.uid).set(updates, com.google.firebase.firestore.SetOptions.merge())
                }
            }
            delay(500)
            _isSyncingDb.value = false
        }
    }

    fun manualCloudSync(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _isSyncingDb.value = true
            val user = auth.currentUser
            if (user == null) {
                _isSyncingDb.value = false
                val msg = when (_language.value) {
                    Language.RU -> "Войдите в аккаунт для облачной синхронизации"
                    Language.UA -> "Увійдіть в акаунт для хмарної синхронізації"
                    Language.KK -> "Бұлттық синхрондау үшін аккаунтқа кіріңіз"
                    Language.DE -> "Melden Sie sich an, um Cloud-Sync zu nutzen"
                    Language.ZH -> "请登录账户以使用云端同步"
                    else -> "Log in to use cloud sync"
                }
                onResult(false, msg)
                return@launch
            }
            try {
                user.reload()
                _isEmailVerified.value = user.isEmailVerified
                val pullResult = com.example.db.FirebaseSync.pullUserData(getApplication())
                reloadAllCustomizationsAndStats()
                loadAchievements()
                val pushResult = com.example.db.FirebaseSync.pushUserData(getApplication())
                saveCurrentProfileToDb()
                fetchGlobalLeaderboard()
                loadFriends()
                _isSyncingDb.value = false
                if (pushResult || pullResult != null) {
                    val successMsg = when (_language.value) {
                        Language.RU -> "Синхронизация с облаком успешно завершена!"
                        Language.UA -> "Хмарна синхронізація успішно завершена!"
                        Language.KK -> "Бұлтпен синхрондау сәтті аяқталды!"
                        Language.DE -> "Cloud-Synchronisierung erfolgreich abgeschlossen!"
                        Language.ZH -> "云端数据同步成功完成！"
                        else -> "Cloud synchronization completed successfully!"
                    }
                    onResult(true, successMsg)
                } else {
                    val failMsg = when (_language.value) {
                        Language.RU -> "Ошибка синхронизации с облаком"
                        Language.UA -> "Помилка хмарної синхронізації"
                        Language.KK -> "Бұлтпен синхрондау қатесі"
                        Language.DE -> "Cloud-Synchronisierungsfehler"
                        Language.ZH -> "云端数据同步失败"
                        else -> "Cloud synchronization failed"
                    }
                    onResult(false, failMsg)
                }
            } catch (e: Exception) {
                _isSyncingDb.value = false
                onResult(false, e.localizedMessage ?: "Sync Error")
            }
        }
    }

    fun clearLoginMessages() {
        _loginError.value = null
        _loginSuccessMessage.value = null
    }

    // Вход — проверяем верификацию почты перед допуском / login with email verification gate
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

            // Local DB login check (support hashed and legacy plain)
            val localAcc = accountRepo.getAccount(trimmedName)
            val inputHashed = com.example.db.PasswordHasher.hash(trimmedPass)
            
            if (localAcc != null && (localAcc.password == inputHashed || localAcc.password == trimmedPass)) {
                if (localAcc.password == trimmedPass) {
                    accountRepo.insert(localAcc.copy(password = inputHashed))
                }
                _isEmailVerified.value = true
                _showVerificationBanner.value = false
                switchAccount(localAcc.username, localAcc.onlineTier, localAcc.credits, localAcc.hasGradient, localAcc.bonusXp)
                _loginSuccessMessage.value = "Успешный вход!"
                _isAuthLoading.value = false
                return@launch
            }

            // Resolve email: if user entered an email, use it directly. Otherwise query nicknames collection for registered email.
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val attemptAuthWithEmail = { targetEmail: String ->
                auth.signInWithEmailAndPassword(targetEmail, trimmedPass)
                    .addOnSuccessListener { authResult ->
                        val user = authResult.user
                        if (user != null) {
                            try { user.reload() } catch (_: Exception) {}
                            firestore.collection("users").document(user.uid).get()
                                .addOnSuccessListener { doc ->
                                    val cloudVerified = (doc.getBoolean("email_verified") == true) ||
                                            (doc.getBoolean("is_verified") == true) ||
                                            (doc.getBoolean("emailVerified") == true)
                                    val isVerified = user.isEmailVerified || cloudVerified

                                    if (!isVerified) {
                                        _isEmailVerified.value = false
                                        _showVerificationBanner.value = true
                                        _loginError.value = "Подтвердите почту! Проверьте входящие."
                                        auth.signOut()
                                        _isAuthLoading.value = false
                                        return@addOnSuccessListener
                                    }

                                    viewModelScope.launch {
                                        _isEmailVerified.value = true
                                        _showVerificationBanner.value = false
                                        profilePrefs.edit().putBoolean("is_email_verified", true).apply()

                                        firestore.collection("users").document(user.uid).set(
                                            mapOf(
                                                "email" to (user.email ?: ""),
                                                "email_verified" to true,
                                                "is_verified" to true,
                                                "emailVerified" to true
                                            ),
                                            com.google.firebase.firestore.SetOptions.merge()
                                        )

                                        val data = com.example.db.FirebaseSync.pullUserData(getApplication())
                                        val resolvedUsername = user.displayName ?: user.email?.substringBefore("@") ?: trimmedName
                                        val resolvedCredits = (data?.get("credits") as? Number)?.toInt()
                                            ?: if (_playerName.value == "Player 1") _credits.value else prefs.getInt("credits", 750)
                                        val resolvedTier = (data?.get("online_tier") as? String)
                                            ?: if (_playerName.value == "Player 1") _onlineTier.value else "BRONZE"
                                        val resolvedHasGradient = data?.get("has_nickname_gradient") as? Boolean ?: false
                                        val resolvedBonusXp = (data?.get("bonus_xp") as? Number)?.toInt() ?: 0

                                        val resolvedHighScore = (data?.get("stats_high_score") as? Number)?.toInt() ?: 0
                                        if (resolvedHighScore > 0) {
                                            scoreRepo.insert(HighScore(playerName = resolvedUsername, score = resolvedHighScore))
                                        }

                                        switchAccount(resolvedUsername, resolvedTier, resolvedCredits, resolvedHasGradient, resolvedBonusXp)
                                        startUserDocListener(user.uid)
                                        _loginSuccessMessage.value = "Успешный вход!"
                                        _isAuthLoading.value = false
                                    }
                                }
                                .addOnFailureListener {
                                    if (!user.isEmailVerified) {
                                        _isEmailVerified.value = false
                                        _showVerificationBanner.value = true
                                        _loginError.value = "Подтвердите почту! Проверьте входящие."
                                        auth.signOut()
                                        _isAuthLoading.value = false
                                    } else {
                                        viewModelScope.launch {
                                            _isEmailVerified.value = true
                                            _showVerificationBanner.value = false
                                            profilePrefs.edit().putBoolean("is_email_verified", true).apply()
                                            val data = com.example.db.FirebaseSync.pullUserData(getApplication())
                                            val resolvedUsername = user.displayName ?: user.email?.substringBefore("@") ?: trimmedName
                                            val resolvedCredits = (data?.get("credits") as? Number)?.toInt()
                                                ?: if (_playerName.value == "Player 1") _credits.value else prefs.getInt("credits", 750)
                                            val resolvedTier = (data?.get("online_tier") as? String)
                                                ?: if (_playerName.value == "Player 1") _onlineTier.value else "BRONZE"
                                            val resolvedHasGradient = data?.get("has_nickname_gradient") as? Boolean ?: false
                                            val resolvedBonusXp = (data?.get("bonus_xp") as? Number)?.toInt() ?: 0

                                            switchAccount(resolvedUsername, resolvedTier, resolvedCredits, resolvedHasGradient, resolvedBonusXp)
                                            startUserDocListener(user.uid)
                                            _loginSuccessMessage.value = "Успешный вход!"
                                            _isAuthLoading.value = false
                                        }
                                    }
                                }
                        } else {
                            _loginError.value = "Ошибка авторизации!"
                            _isAuthLoading.value = false
                        }
                    }
                    .addOnFailureListener { e ->
                        _loginError.value = e.localizedMessage ?: "Неверные учетные данные!"
                        _isAuthLoading.value = false
                    }
            }

            if (trimmedName.contains("@")) {
                attemptAuthWithEmail(trimmedName.lowercase())
            } else {
                val nickLower = trimmedName.lowercase()
                firestore.collection("nicknames").document(nickLower).get()
                    .addOnSuccessListener { nickDoc ->
                        val emailFromNick = nickDoc.getString("email")
                        if (!emailFromNick.isNullOrEmpty()) {
                            attemptAuthWithEmail(emailFromNick)
                        } else {
                            // Fallback to legacy hex email
                            val hexName = trimmedName.toByteArray(Charsets.UTF_8).joinToString("") { "%02x".format(it) }
                            attemptAuthWithEmail("${hexName}@blocktetris.com")
                        }
                    }
                    .addOnFailureListener {
                        val hexName = trimmedName.toByteArray(Charsets.UTF_8).joinToString("") { "%02x".format(it) }
                        attemptAuthWithEmail("${hexName}@blocktetris.com")
                    }
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
            val trimmedEmail = emailEntered.trim().lowercase()
            val trimmedPass = passwordEntered.trim()
            if (trimmedName.isEmpty()) {
                _loginError.value = "Никнейм пустой!"
                return@launch
            }
            val isTargetEmailAdmin = (trimmedEmail == "ezik02021@gmail.com")
            if (isReservedAdminNickname(trimmedName) && !isTargetEmailAdmin) {
                _loginError.value = "Этот никнейм защищен и зарезервирован администрацией!"
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
            val nameLower = trimmedName.lowercase()
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

            // 1. Проверяем занятость никнейма перед регистрацией
            firestore.collection("nicknames").document(nameLower).get()
                .addOnSuccessListener { nickDoc ->
                    if (nickDoc.exists()) {
                        _loginError.value = "Никнейм '$trimmedName' уже занят другим игроком!"
                        _isAuthLoading.value = false
                        return@addOnSuccessListener
                    }

                    firestore.collection("users").whereEqualTo("player_name", trimmedName).get()
                        .addOnSuccessListener { userDocs ->
                            if (!userDocs.isEmpty) {
                                _loginError.value = "Никнейм '$trimmedName' уже занят другим игроком!"
                                _isAuthLoading.value = false
                                return@addOnSuccessListener
                            }

                            // Никнейм свободен -> регистрируем в Firebase Auth
                            auth.createUserWithEmailAndPassword(trimmedEmail, trimmedPass)
                                .addOnSuccessListener { authResult ->
                                    viewModelScope.launch {
                                        val user = authResult.user
                                        if (user != null) {
                                            val profileUpdates = userProfileChangeRequest {
                                                displayName = trimmedName
                                            }
                                            user.updateProfile(profileUpdates)
                                            
                                            // Отправляем письмо подтверждения / send verification email
                                            try {
                                                user.sendEmailVerification()
                                            } catch (_: Exception) {}
                                            
                                            // Бронируем уникальный никнейм в коллекции nicknames
                                            val nickData = mapOf(
                                                "uid" to user.uid,
                                                "email" to trimmedEmail,
                                                "player_name" to trimmedName,
                                                "nick_lower" to nameLower,
                                                "created_at" to System.currentTimeMillis()
                                            )
                                            firestore.collection("nicknames").document(nameLower).set(nickData)

                                            val acc = com.example.db.UserAccount(
                                                username = trimmedName,
                                                password = com.example.db.PasswordHasher.hash(trimmedPass),
                                                onlineTier = finalTier,
                                                credits = finalCredits
                                            )
                                            accountRepo.insert(acc)
                                            switchAccount(trimmedName, finalTier, finalCredits)
                                            
                                            // Инициализируем Firestore запись / init Firestore record
                                            com.example.db.FirebaseSync.pushUserData(getApplication())
                                            
                                            _isEmailVerified.value = false
                                            _showVerificationBanner.value = true
                                            _loginSuccessMessage.value = "Письмо подтверждения отправлено! Проверьте почту."
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
                        .addOnFailureListener { e ->
                            _loginError.value = "Ошибка проверки доступности ника: ${e.localizedMessage}"
                            _isAuthLoading.value = false
                        }
                }
                .addOnFailureListener { e ->
                    _loginError.value = "Ошибка проверки доступности ника: ${e.localizedMessage}"
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
        if (isReservedAdminNickname(trimmed) && !isCurrentUserAdmin()) {
            _nicknameUpdateError.value = "Этот никнейм защищен и зарезервирован разработчиком!"
            return
        }

        val user = auth.currentUser
        if (user == null) {
            _nicknameUpdateError.value = "Войдите в подтвержденный аккаунт для смены никнейма!"
            return
        }

        // Проверка: реальный человек с подтвержденной почтой
        val isVerified = user.isEmailVerified || _isEmailVerified.value
        if (!isVerified && !isCurrentUserAdmin()) {
            _nicknameUpdateError.value = "Смена ника доступна только для подтвержденной почты! Подтвердите почту."
            return
        }

        val oldNick = _playerName.value.trim()
        if (oldNick.equals(trimmed, ignoreCase = true)) {
            _nicknameUpdateError.value = "Этот никнейм уже установлен!"
            return
        }

        val newNickLower = trimmed.lowercase()
        val oldNickLower = oldNick.lowercase()
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        _nicknameUpdateError.value = null
        _nicknameUpdateSuccess.value = null

        // 1. Проверяем коллекцию уникальных никнеймов
        firestore.collection("nicknames").document(newNickLower).get()
            .addOnSuccessListener { nickDoc ->
                if (nickDoc.exists()) {
                    val ownerUid = nickDoc.getString("uid") ?: ""
                    if (ownerUid.isNotEmpty() && ownerUid != user.uid) {
                        _nicknameUpdateError.value = "Этот никнейм уже занят другим игроком!"
                        return@addOnSuccessListener
                    }
                }

                // 2. Дополнительная проверка по коллекции users
                firestore.collection("users").whereEqualTo("player_name", trimmed).get()
                    .addOnSuccessListener { userDocs ->
                        val hasOtherOwner = userDocs.documents.any { it.id != user.uid }
                        if (hasOtherOwner) {
                            _nicknameUpdateError.value = "Этот никнейм уже занят другим игроком!"
                            return@addOnSuccessListener
                        }

                        // 3. Ник свободен — резервируем и привязываем к пользователю и почте
                        val nickData = mapOf(
                            "uid" to user.uid,
                            "email" to (user.email ?: ""),
                            "player_name" to trimmed,
                            "nick_lower" to newNickLower,
                            "updated_at" to System.currentTimeMillis()
                        )

                        firestore.collection("nicknames").document(newNickLower).set(nickData)
                            .addOnSuccessListener {
                                // Удаляем старый ник из брони, если он отличался и был зарегистрирован
                                if (oldNickLower.isNotEmpty() && oldNickLower != "player 1" && oldNickLower != newNickLower) {
                                    firestore.collection("nicknames").document(oldNickLower).get()
                                        .addOnSuccessListener { oldDoc ->
                                            if (oldDoc.exists() && oldDoc.getString("uid") == user.uid) {
                                                oldDoc.reference.delete()
                                            }
                                        }
                                }

                                // Обновляем Firebase Auth профиль
                                val profileUpdates = userProfileChangeRequest {
                                    displayName = trimmed
                                }
                                user.updateProfile(profileUpdates)
                                    .addOnSuccessListener {
                                        viewModelScope.launch {
                                            switchAccount(trimmed, _onlineTier.value, _credits.value, _hasNicknameGradient.value, _bonusXp.value)
                                            com.example.db.FirebaseSync.pushUserData(getApplication())
                                            _nicknameUpdateSuccess.value = "Никнейм успешно изменен и привязан к вашей почте!"
                                        }
                                    }
                                    .addOnFailureListener { e ->
                                        _nicknameUpdateError.value = e.localizedMessage ?: "Ошибка при обновлении профиля!"
                                    }
                            }
                            .addOnFailureListener { e ->
                                _nicknameUpdateError.value = "Ошибка привязки никнейма: ${e.localizedMessage}"
                            }
                    }
                    .addOnFailureListener { e ->
                        _nicknameUpdateError.value = "Ошибка проверки никнейма: ${e.localizedMessage}"
                    }
            }
            .addOnFailureListener { e ->
                _nicknameUpdateError.value = "Ошибка проверки никнейма: ${e.localizedMessage}"
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
            val user = auth.currentUser
            if (user != null) {
                user.verifyBeforeUpdateEmail(trimmed)
                    .addOnSuccessListener {
                        _emailUpdateSuccess.value = "Письмо подтверждения отправлено на новый адрес! Подтвердите его для смены."
                        viewModelScope.launch {
                            com.example.db.FirebaseSync.pushUserData(getApplication())
                        }
                    }
                    .addOnFailureListener { e ->
                        // Fallback to updateEmail if verifyBeforeUpdateEmail not supported or fails
                        @Suppress("DEPRECATION")
                        user.updateEmail(trimmed)
                            .addOnSuccessListener {
                                _emailUpdateSuccess.value = "Почта успешно обновлена!"
                                viewModelScope.launch {
                                    com.example.db.FirebaseSync.pushUserData(getApplication())
                                }
                            }
                            .addOnFailureListener { fallbackErr ->
                                _emailUpdateError.value = fallbackErr.localizedMessage ?: e.localizedMessage ?: "Ошибка при обновлении почты!"
                            }
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
            // Google вход — почта уже верифицирована Google'ом / Google = already verified
            auth.signInWithCredential(credential)
                .addOnSuccessListener { authResult ->
                    viewModelScope.launch {
                        val user = authResult.user
                        if (user != null) {
                            val resolvedUsername = user.displayName ?: user.email?.substringBefore("@") ?: "GoogleUser"
                            val data = com.example.db.FirebaseSync.pullUserData(getApplication())
                            val resolvedCredits = (data?.get("credits") as? Number)?.toInt()
                                ?: if (_playerName.value == "Player 1") _credits.value else prefs.getInt("credits", 750)
                            val resolvedTier = (data?.get("online_tier") as? String)
                                ?: if (_playerName.value == "Player 1") _onlineTier.value else "BRONZE"
                            val resolvedHasGradient = data?.get("has_nickname_gradient") as? Boolean ?: false
                            val resolvedBonusXp = (data?.get("bonus_xp") as? Number)?.toInt() ?: 0
                            
                            val resolvedHighScore = (data?.get("stats_high_score") as? Number)?.toInt() ?: 0
                            if (resolvedHighScore > 0) {
                                scoreRepo.insert(HighScore(playerName = resolvedUsername, score = resolvedHighScore))
                            }
                            
                            _isEmailVerified.value = true
                            _showVerificationBanner.value = false
                            profilePrefs.edit().putBoolean("is_email_verified", true).apply()
                            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                .collection("users").document(user.uid).set(
                                    mapOf(
                                        "email" to (user.email ?: ""),
                                        "email_verified" to true,
                                        "is_verified" to true,
                                        "emailVerified" to true
                                    ),
                                    com.google.firebase.firestore.SetOptions.merge()
                                )
                            switchAccount(resolvedUsername, resolvedTier, resolvedCredits, resolvedHasGradient, resolvedBonusXp)
                            startUserDocListener(user.uid)
                            
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
        val safeName = if (isReservedAdminNickname(username) && !isCurrentUserAdmin()) {
            "Player 1"
        } else {
            username
        }
        _playerName.update { safeName }
        _onlineTier.update { tier }
        setCreditsInternal(creditsAmount, syncToCloud = false)
        _hasNicknameGradient.update { hasGradient }
        _bonusXp.update { bonusXpAmount }
        prefs.edit().putString("player_name", safeName).apply()
        prefs.edit().putString("online_tier", tier).apply()
        profilePrefs.edit().putString("online_tier", tier).apply()
        profilePrefs.edit().putBoolean("has_nickname_gradient", hasGradient).apply()
        profilePrefs.edit().putInt("bonus_xp", bonusXpAmount).apply()
        
        saveCurrentProfileToDb()
        reloadAllCustomizationsAndStats()
    }

    private fun resetPrefsToGuestDefaults(guestAcc: com.example.db.UserAccount?) {
        val guestCredits = guestAcc?.credits ?: 750
        setCreditsInternal(guestCredits, syncToCloud = false)
        val editorProfile = profilePrefs.edit()
        val editorTetris = prefs.edit()
        
        editorTetris.putString("player_name", "Player 1")
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
        userDocSnapshotListener?.remove()
        userDocSnapshotListener = null
        viewModelScope.launch {
            try {
                auth.signOut()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            _isEmailVerified.value = false
            _showVerificationBanner.value = false
            profilePrefs.edit().putBoolean("is_email_verified", false).apply()
            val guestAcc = accountRepo.getAccount("Player 1")
            resetPrefsToGuestDefaults(guestAcc)
            reloadAllCustomizationsAndStats()
            clearLoginMessages()
        }
    }

    // Переотправить письмо верификации / resend verification email
    fun resendVerificationEmail() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null && !user.isEmailVerified) {
                try {
                    user.sendEmailVerification()
                    _loginSuccessMessage.value = "Письмо отправлено повторно!"
                } catch (e: Exception) {
                    _loginError.value = e.localizedMessage ?: "Ошибка отправки письма!"
                }
            } else {
                _loginError.value = "Нет аккаунта для верификации"
            }
        }
    }

    // Проверить статус верификации / check if email was verified (user clicks "I verified")
    fun checkEmailVerification() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user != null) {
                try {
                    user.reload()
                    val isVerified = user.isEmailVerified || profilePrefs.getBoolean("is_email_verified", false)
                    if (isVerified) {
                        _isEmailVerified.value = true
                        _showVerificationBanner.value = false
                        profilePrefs.edit().putBoolean("is_email_verified", true).apply()
                        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("users").document(user.uid).set(
                                mapOf(
                                    "email" to (user.email ?: ""),
                                    "email_verified" to true,
                                    "is_verified" to true,
                                    "emailVerified" to true
                                ),
                                com.google.firebase.firestore.SetOptions.merge()
                            )
                        _loginSuccessMessage.value = "Почта подтверждена!"
                    } else {
                        _loginError.value = "Почта ещё не подтверждена"
                    }
                } catch (e: Exception) {
                    _loginError.value = e.localizedMessage ?: "Ошибка проверки"
                }
            }
        }
    }

    fun reloadAllCustomizationsAndStats() {
        _playerName.update { prefs.getString("player_name", "Player 1") ?: "Player 1" }
        _onlineTier.update { profilePrefs.getString("online_tier", "BRONZE") ?: "BRONZE" }
        setCreditsInternal(prefs.getInt("credits", 750), syncToCloud = false)
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
        _statsGamesPlayed.update { prefs.getInt("stats_games_played", 0) }
        
        loadCrateKeysFromPrefs()
        loadClaimedLevelRewardsFromPrefs()
        loadPurchasedRanksFromPrefs()
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
                    val name = (doc.getString("playerName") ?: doc.getString("username") ?: doc.getString("name") ?: "Игрок").ifEmpty { "Игрок" }
                    val score = doc.getLong("score")?.toInt() ?: 0
                    val ts = doc.getLong("timestamp") ?: 0L
                    val hasGrad = doc.getBoolean("hasGradient") ?: false
                    val tag = doc.getString("customTag") ?: ""
                    val emoji = doc.getString("avatarEmoji") ?: doc.getString("custom_avatar_emoji") ?: ""
                    val bg = doc.getString("avatarBgColor") ?: doc.getString("custom_avatar_bg_color") ?: ""
                    val frame = doc.getString("avatarFrame") ?: doc.getString("equipped_avatar_frame") ?: "standard"
                    val tier = doc.getString("onlineTier") ?: "BRONZE"
                    val title = doc.getString("title") ?: doc.getString("equipped_title") ?: ""
                    val uid = doc.getString("uid") ?: doc.id
                    val avatarBase64 = doc.getString("avatarBase64") ?: doc.getString("custom_avatar_base64") ?: ""
                    val rawSynced = doc.getLong("last_synced_timestamp") ?: doc.getLong("lastActive") ?: 0L
                    val rawOnline = doc.getBoolean("is_online") ?: doc.getBoolean("isOnline") ?: false
                    val isOnline = rawOnline && (rawSynced == 0L || (System.currentTimeMillis() - rawSynced) < 120_000L)

                    HighScore(
                        playerName = name,
                        score = score,
                        timestamp = ts,
                        hasGradient = hasGrad,
                        avatarEmoji = emoji,
                        avatarBgColorHex = bg,
                        avatarFrame = frame,
                        onlineTier = tier,
                        title = title,
                        uid = uid
                    ).apply {
                        customTag = tag
                        this.avatarBase64 = avatarBase64
                        this.isOnline = isOnline
                    }
                }
                _globalScores.value = list
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    fun fetchFirebaseUsersForAdmin() {
        _isLoadingFirebaseUsers.value = true
        _adminErrorMessage.value = null
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users")
            .get()
            .addOnSuccessListener { snapshot ->
                val list = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    data + mapOf("uid" to doc.id)
                }
                viewModelScope.launch {
                    val localAccounts = accountRepo.allAccounts.firstOrNull() ?: emptyList()
                    val cloudUsernames = list.mapNotNull { it["player_name"] as? String }.toSet()
                    val mergedLocal = localAccounts
                        .filter { it.username !in cloudUsernames }
                        .map { acc ->
                            mapOf<String, Any>(
                                "uid" to "local_${acc.username}",
                                "player_name" to acc.username,
                                "email" to "${acc.username}@local.db",
                                "email_verified" to true,
                                "is_verified" to true,
                                "emailVerified" to true,
                                "credits" to acc.credits,
                                "online_tier" to acc.onlineTier,
                                "has_nickname_gradient" to acc.hasGradient,
                                "bonus_xp" to acc.bonusXp,
                                "is_local_only" to true,
                                "stats_high_score" to 0,
                                "stats_cleared_lines" to 0,
                                "stats_games_played" to 0,
                                "last_synced_timestamp" to 0L,
                                "is_online" to false
                            )
                        }
                    _firebaseUsers.value = list + mergedLocal
                    _isLoadingFirebaseUsers.value = false
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                _adminErrorMessage.value = "Ошибка облака: ${e.localizedMessage ?: "Сетевая ошибка"}"
                viewModelScope.launch {
                    val localAccounts = accountRepo.allAccounts.firstOrNull() ?: emptyList()
                    val localList = localAccounts.map { acc ->
                        mapOf<String, Any>(
                            "uid" to "local_${acc.username}",
                            "player_name" to acc.username,
                            "credits" to acc.credits,
                            "online_tier" to acc.onlineTier,
                            "has_nickname_gradient" to acc.hasGradient,
                            "bonus_xp" to acc.bonusXp,
                            "is_local_only" to true,
                            "stats_high_score" to 0,
                            "stats_cleared_lines" to 0,
                            "stats_games_played" to 0,
                            "last_synced_timestamp" to 0L,
                            "is_online" to false
                        )
                    }
                    _firebaseUsers.value = localList
                    _isLoadingFirebaseUsers.value = false
                }
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
                    val uid = doc.id
                    val email = doc.getString("email")?.lowercase() ?: ""
                    val isProtectedAdminDoc = (uid == "ge9Lzx5EkCfbINDZEG6I8vYcJCd2" || 
                        email == "ezik02021@gmail.com")
                    if (!isProtectedAdminDoc) {
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

    fun adminGiveCredits(uid: String = "", username: String = "", amount: Int, isDelta: Boolean = false) {
        val currentUid = auth.currentUser?.uid ?: ""
        val currentPName = _playerName.value
        val isTargetMe = (uid.isNotEmpty() && uid == currentUid) || (username.isNotEmpty() && username == currentPName && isCurrentUserAdmin())
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()

        if (isTargetMe) {
            val newAmount = if (isDelta) (_credits.value + amount).coerceAtLeast(0) else amount.coerceAtLeast(0)
            setCreditsInternal(newAmount, syncToCloud = false)
            viewModelScope.launch {
                val account = accountRepo.getAccount(currentPName)
                if (account != null) {
                    accountRepo.insert(account.copy(credits = newAmount))
                }
                if (currentUid.isNotEmpty()) {
                    val updateOp: Map<String, Any> = if (isDelta) mapOf("credits" to com.google.firebase.firestore.FieldValue.increment(amount.toLong())) else mapOf("credits" to newAmount)
                    firestore.collection("users").document(currentUid).set(updateOp, com.google.firebase.firestore.SetOptions.merge())
                }
                fetchFirebaseUsersForAdmin()
            }
        } else {
            val resolvedName = username.ifEmpty {
                _firebaseUsers.value.find { it["uid"] == uid }?.get("player_name") as? String ?: ""
            }
            if (resolvedName.isNotEmpty()) {
                viewModelScope.launch {
                    val account = accountRepo.getAccount(resolvedName)
                    if (account != null) {
                        val newBal = if (isDelta) (account.credits + amount).coerceAtLeast(0) else amount.coerceAtLeast(0)
                        accountRepo.insert(account.copy(credits = newBal))
                    }
                }
            }
            if (uid.isNotEmpty() && !uid.startsWith("local_")) {
                val updateOp: Map<String, Any> = if (isDelta) mapOf("credits" to com.google.firebase.firestore.FieldValue.increment(amount.toLong())) else mapOf("credits" to amount)
                firestore.collection("users").document(uid).set(updateOp, com.google.firebase.firestore.SetOptions.merge())
                    .addOnSuccessListener { fetchFirebaseUsersForAdmin() }
            } else if (username.isNotEmpty() && !uid.startsWith("local_")) {
                firestore.collection("users").whereEqualTo("player_name", username).get()
                    .addOnSuccessListener { snapshot ->
                        val batch = firestore.batch()
                        for (doc in snapshot.documents) {
                            val updateOp: Map<String, Any> = if (isDelta) mapOf("credits" to com.google.firebase.firestore.FieldValue.increment(amount.toLong())) else mapOf("credits" to amount)
                            batch.set(doc.reference, updateOp, com.google.firebase.firestore.SetOptions.merge())
                        }
                        batch.commit().addOnSuccessListener { fetchFirebaseUsersForAdmin() }
                    }
            } else {
                fetchFirebaseUsersForAdmin()
            }
        }
    }

    fun adminGiveCreditsToAll(amount: Int) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users").get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.set(doc.reference, mapOf("credits" to com.google.firebase.firestore.FieldValue.increment(amount.toLong())), com.google.firebase.firestore.SetOptions.merge())
                }
                batch.commit().addOnSuccessListener {
                    addCredits(amount)
                    fetchFirebaseUsersForAdmin()
                }
            }
    }

    fun adminUpdateAccountCredits(username: String, amount: Int) {
        adminGiveCredits(username = username, amount = amount, isDelta = false)
    }

    fun adminUpdateAccountRank(username: String, rank: String) {
        viewModelScope.launch {
            val account = accountRepo.getAccount(username)
            if (account != null) {
                val updated = account.copy(onlineTier = rank)
                accountRepo.insert(updated)
            }
            if (_playerName.value == username) {
                _onlineTier.value = rank
                prefs.edit().putString("online_tier", rank).apply()
                profilePrefs.edit().putString("online_tier", rank).apply()
            }
        }
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users")
            .whereEqualTo("player_name", username)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.set(doc.reference, mapOf("online_tier" to rank), com.google.firebase.firestore.SetOptions.merge())
                }
                batch.commit().addOnSuccessListener {
                    fetchFirebaseUsersForAdmin()
                }
            }
    }

    fun adminUpdateFirebaseUserDirect(uid: String, credits: Int, rank: String) {
        adminGiveCredits(uid = uid, amount = credits, isDelta = false)
        if (!uid.startsWith("local_")) {
            val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            firestore.collection("users").document(uid).set(mapOf("online_tier" to rank), com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    fetchFirebaseUsersForAdmin()
                }
        } else {
            fetchFirebaseUsersForAdmin()
        }
    }

    fun adminUpdateUserStats(
        uid: String,
        credits: Int,
        highScore: Int,
        linesCleared: Int,
        gamesPlayed: Int,
        bonusXp: Int,
        onlineTier: String,
        hasGradient: Boolean,
        customTagUnlocked: Boolean,
        customTag: String
    ) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val updates = mapOf(
            "credits" to credits,
            "stats_high_score" to highScore,
            "stats_cleared_lines" to linesCleared,
            "stats_games_played" to gamesPlayed,
            "bonus_xp" to bonusXp,
            "online_tier" to onlineTier,
            "has_nickname_gradient" to hasGradient,
            "custom_tag_unlocked" to customTagUnlocked,
            "custom_tag" to customTag
        )
        val targetName = _firebaseUsers.value.find { it["uid"] == uid }?.get("player_name") as? String ?: ""
        if (targetName.isNotEmpty()) {
            viewModelScope.launch {
                val acc = accountRepo.getAccount(targetName)
                if (acc != null) {
                    accountRepo.insert(acc.copy(
                        credits = credits,
                        onlineTier = onlineTier,
                        hasGradient = hasGradient,
                        bonusXp = bonusXp
                    ))
                }
            }
        }
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.uid == uid) {
            setCreditsInternal(credits, syncToCloud = false)
            _onlineTier.value = onlineTier
            _hasNicknameGradient.value = hasGradient
            _bonusXp.value = bonusXp
            _customTagUnlocked.value = customTagUnlocked
            _customTag.value = customTag
            _statsHighScore.value = highScore
            _statsClearedLines.value = linesCleared
            _statsGamesPlayed.value = gamesPlayed

            prefs.edit()
                .putInt("stats_high_score", highScore)
                .putInt("stats_cleared_lines", linesCleared)
                .putInt("stats_games_played", gamesPlayed)
                .putString("online_tier", onlineTier)
                .apply()

            profilePrefs.edit()
                .putString("online_tier", onlineTier)
                .putBoolean("has_nickname_gradient", hasGradient)
                .putInt("bonus_xp", bonusXp)
                .putBoolean("custom_tag_unlocked", customTagUnlocked)
                .putString("custom_tag", customTag)
                .apply()
        }

        if (!uid.startsWith("local_")) {
            firestore.collection("users").document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge())
                .addOnSuccessListener {
                    fetchFirebaseUsersForAdmin()
                }
        } else {
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
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.uid == uid) {
                    viewModelScope.launch {
                        com.example.db.FirebaseSync.pullUserData(getApplication())
                        setCreditsInternal(prefs.getInt("credits", 750), syncToCloud = false)
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
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.uid == uid) {
                    viewModelScope.launch {
                        com.example.db.FirebaseSync.pullUserData(getApplication())
                        setCreditsInternal(prefs.getInt("credits", 750), syncToCloud = false)
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
            "block_style" to "material",
            "custom_avatar_base64" to "",
            "custom_background_base64" to ""
        )
        firestore.collection("users").document(uid).set(updates)
            .addOnSuccessListener {
                fetchFirebaseUsersForAdmin()
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.uid == uid) {
                    viewModelScope.launch {
                        com.example.db.FirebaseSync.pullUserData(getApplication())
                        setCreditsInternal(prefs.getInt("credits", 750), syncToCloud = false)
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
                setCreditsInternal(99999, syncToCloud = true)
                _hasNicknameGradient.value = true
                _bonusXp.value = 50000
                profilePrefs.edit()
                    .putBoolean("has_nickname_gradient", true)
                    .putInt("bonus_xp", 50000)
                    .putStringSet("purchased_skins", setOf("cyberpunk", "retro_amber", "emerald_matrix", "vaporwave_pink", "midnight_gold", "carbon_neutral", "plasma_storm", "glacial_frost"))
                    .putStringSet("purchased_cube_skins", setOf("neon", "glass", "retro", "flat", "material", "glowing_jewel", "steampunk", "red_gradient", "green_gradient", "blue_gradient", "purple_gradient"))
                    .putStringSet("purchased_avatar_frames", setOf("standard", "frame_white", "frame_blue", "chrono_gl"))
                    .putStringSet("purchased_sound_packs", setOf("arcade", "synthwave", "cyber_metal", "ai_voice"))
                    .putStringSet("purchased_themes", setOf("indigo", "neon", "red", "emerald", "amber", "rose", "sky", "orange", "toxic_green", "cyber_pink", "gold"))
                    .putStringSet("purchased_fonts", setOf("default", "monospace", "serif", "sans-serif", "cursive", "condensed", "black", "thin"))
                    .putStringSet("purchased_control_button_styles", setOf("classic", "neon", "glass"))
                    .apply()
                
                _equippedAvatarFrame.value = "standard"
                _purchasedAvatarFrames.value = setOf("standard", "frame_white", "frame_blue", "chrono_gl")
                _equippedSoundPack.value = "arcade"
                _purchasedSoundPacks.value = setOf("arcade", "synthwave", "cyber_metal", "ai_voice")
                _purchasedThemes.value = setOf("indigo", "neon", "red", "emerald", "amber", "rose", "sky", "orange", "toxic_green", "cyber_pink", "gold")
                _purchasedFonts.value = setOf("default", "monospace", "serif", "sans-serif", "cursive", "condensed", "black", "thin")
                _purchasedControlButtonStyles.value = setOf("classic", "neon", "glass", "gold_legendary", "plasma_legendary")
                
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
                setCreditsInternal(750, syncToCloud = true)
                _onlineTier.value = "BRONZE"
                _hasNicknameGradient.value = false
                _bonusXp.value = 0
                
                prefs.edit()
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
                    .putString("block_style", "material")
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
        if (uid.startsWith("local_")) {
            val uName = uid.removePrefix("local_")
            viewModelScope.launch {
                val acc = accountRepo.getAccount(uName)
                if (acc != null) {
                    accountRepo.delete(uName)
                }
                fetchFirebaseUsersForAdmin()
            }
            return
        }
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users").document(uid).delete()
            .addOnSuccessListener {
                fetchFirebaseUsersForAdmin()
            }
        firestore.collection("high_scores").document(uid).delete()
            .addOnSuccessListener {
                fetchGlobalLeaderboard()
            }
        try {
            com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("presence/$uid").removeValue()
        } catch (e: Exception) {
            e.printStackTrace()
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

    fun adminBanUser(uid: String, isBanned: Boolean) {
        if (uid.startsWith("local_")) return
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users").document(uid).set(mapOf("is_banned" to isBanned), com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                fetchFirebaseUsersForAdmin()
            }
    }

    fun adminSendGlobalBroadcast(title: String, message: String) {
        if (message.isBlank()) return
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val broadcastData = mapOf(
            "title" to title.trim(),
            "message" to message.trim(),
            "sender" to "ADMIN",
            "timestamp" to System.currentTimeMillis()
        )
        firestore.collection("global_broadcasts").add(broadcastData)
        // Also broadcast to lobby chat as system alert
        val systemChatMessage = mapOf(
            "senderId" to "system_broadcast",
            "senderName" to "[СИСТЕМА / SYSTEM]",
            "text" to if (title.isNotBlank()) "$title: $message" else message,
            "timestamp" to System.currentTimeMillis(),
            "hasGradient" to true,
            "senderTier" to "ADMIN",
            "senderAvatarEmoji" to "",
            "senderAvatarBgColor" to "FFD700",
            "senderAvatarFrame" to "gold_ma"
        )
        firestore.collection("lobby_chat").add(systemChatMessage)
    }

    fun adminGiveAllCosmetics(username: String) {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val matchingUser = _firebaseUsers.value.find { (it["player_name"] as? String) == username }
        val allFrames = setOf("standard", "frame_white", "frame_blue", "frame_green", "frame_yellow", "frame_orange", "frame_red", "frame_purple", "frame_dark", "chrono_gl")
        val allTitles = setOf("none", "node", "lord", "cosmic_overlord", "ai_consensus")
        val allThemes = ALL_THEMES
        val allFonts = setOf("default", "monospace", "serif", "sans-serif", "cursive", "condensed", "black", "thin")
        val allButtons = setOf("classic", "neon", "glass", "gold_legendary", "plasma_legendary")

        if (matchingUser != null) {
            val uid = matchingUser["uid"] as? String ?: ""
            if (uid.isNotEmpty()) {
                val updates = mapOf<String, Any>(
                    "purchased_avatar_frames" to allFrames.toList(),
                    "purchased_titles" to allTitles.toList(),
                    "purchased_themes" to allThemes.toList(),
                    "purchased_fonts" to allFonts.toList(),
                    "purchased_control_button_styles" to allButtons.toList(),
                    "has_nickname_gradient" to true,
                    "custom_tag_unlocked" to true
                )
                firestore.collection("users").document(uid).update(updates)
                    .addOnSuccessListener {
                        fetchFirebaseUsersForAdmin()
                    }
            }
        }
        if (username == _playerName.value) {
            setPurchasedAvatarFrames(allFrames)
            setPurchasedTitles(allTitles)
            setPurchasedThemes(allThemes)
            setPurchasedFonts(allFonts)
            setPurchasedControlButtonStyles(allButtons)
            setHasNicknameGradient(true)
            setCustomTagUnlocked(true)
        }
    }

    fun adminSetEmailVerified(uid: String, isVerified: Boolean) {
        if (uid.startsWith("local_")) {
            val uName = uid.removePrefix("local_")
            _firebaseUsers.update { list ->
                list.map { if (it["uid"] == uid) it + mapOf("email_verified" to isVerified, "is_verified" to isVerified, "emailVerified" to isVerified) else it }
            }
            if (_playerName.value == uName) {
                _isEmailVerified.value = isVerified
                profilePrefs.edit().putBoolean("is_email_verified", isVerified).apply()
                if (isVerified) _showVerificationBanner.value = false
            }
            return
        }
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users").document(uid).set(
            mapOf(
                "email_verified" to isVerified,
                "is_verified" to isVerified,
                "emailVerified" to isVerified
            ),
            com.google.firebase.firestore.SetOptions.merge()
        ).addOnSuccessListener {
            fetchFirebaseUsersForAdmin()
            val currentUser = auth.currentUser
            if (currentUser != null && currentUser.uid == uid) {
                _isEmailVerified.value = isVerified
                profilePrefs.edit().putBoolean("is_email_verified", isVerified).apply()
                if (isVerified) {
                    _showVerificationBanner.value = false
                }
            }
        }
    }

    fun adminFullDatabaseWipe() {
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        viewModelScope.launch {
            scoreRepo.clearAll()
            accountRepo.clearAllNonAdmin()
        }
        val collections = listOf("high_scores", "high_scores_by_mode", "global_broadcasts", "lobby_chat", "rooms", "lobbies", "friends")
        for (col in collections) {
            firestore.collection(col).get().addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (doc in snapshot.documents) {
                    batch.delete(doc.reference)
                }
                batch.commit()
            }
        }
        firestore.collection("users").get().addOnSuccessListener { snapshot ->
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                val uid = doc.id
                val email = doc.getString("email")?.lowercase() ?: ""
                val isProtectedAdminDoc = (uid == "ge9Lzx5EkCfbINDZEG6I8vYcJCd2" || 
                    email == "ezik02021@gmail.com")
                if (!isProtectedAdminDoc) {
                    batch.delete(doc.reference)
                }
            }
            batch.commit().addOnSuccessListener {
                fetchFirebaseUsersForAdmin()
                fetchGlobalLeaderboard()
            }
        }
    }

    fun startGame(mode: GameMode = GameMode.CLASSIC) {
        hasAwardedCurrentGameReward = false
        gameEngine.startGame(mode, startingLevel = _customStartLevel.value)
        _isPlaying.update { true }
        startGameLoop()
    }

    fun startBlockBlast() {
        hasAwardedBlockBlastReward = false
        blockBlastEngine.startGame(prefs.getInt("block_blast_high_score", 0))
    }

    fun placeBlockBlastFigure(idx: Int, r: Int, c: Int): Boolean {
        var linesClearedCount = 0
        val ok = blockBlastEngine.placeFigure(idx, r, c) { linesCleared ->
            linesClearedCount = linesCleared
        }
        if (ok) {
            if (linesClearedCount > 0) {
                when (linesClearedCount) {
                    1 -> triggerAudioFeedback("clear_1")
                    2 -> triggerAudioFeedback("clear_2")
                    3 -> triggerAudioFeedback("clear_3")
                    4 -> triggerAudioFeedback("clear_4")
                    else -> triggerAudioFeedback("clear")
                }
            } else {
                triggerAudioFeedback("land")
            }
            val finalScore = blockBlastEngine.state.value.score
            prefs.edit().putInt("block_blast_high_score", blockBlastEngine.state.value.highScore).apply()
            onBlockBlastPlacement(finalScore)
            
            // Rebalanced gameover coins & XP for Block Blast (equal with Classic match)
            if (blockBlastEngine.state.value.isGameOver && !hasAwardedBlockBlastReward) {
                hasAwardedBlockBlastReward = true
                triggerAudioFeedback("gameover")
                val finalScore = blockBlastEngine.state.value.score
                val blastLines = blockBlastEngine.state.value.linesClearedTotal
                
                val totalGames = prefs.getInt("stats_games_played", 0) + 1
                prefs.edit().putInt("stats_games_played", totalGames).apply()
                _statsGamesPlayed.value = totalGames

                val totalClearedLines = prefs.getInt("stats_cleared_lines", 0) + blastLines
                prefs.edit().putInt("stats_cleared_lines", totalClearedLines).apply()
                _statsClearedLines.value = totalClearedLines
                
                val rewardCoins = calculateBlockBlastReward(finalScore, blastLines)
                if (rewardCoins > 0) {
                    addCredits(rewardCoins)
                }
                addMatchXp(score = finalScore, lines = blastLines, modeBonus = 40)
                evaluateAchievements()
            }
        }
        return ok
    }

    fun calculateBlockBlastReward(finalScore: Int, blastLines: Int): Int {
        if (finalScore >= 500) {
            val basePlayCoins = 45
            val performanceCoins = (finalScore / 120).coerceAtMost(120)
            val linesBonus = blastLines * 4
            val baseTotal = ((basePlayCoins + performanceCoins + linesBonus) * 1.25f).toInt()
            val levelBonusPercent = (getPlayerLevel() / 2)
            return (baseTotal * (1f + levelBonusPercent / 100f)).toInt()
        } else if (finalScore >= 200) {
            return 20
        }
        return 0
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

    fun exitGameToMenu(onExit: () -> Unit) {
        pauseGame()
        _isPlaying.update { false }
        prefs.edit().putBoolean("has_saved_game", false).apply()
        _hasSavedGame.update { false }
        onExit()
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
                    val curMode = gameEngine.gameState.value.gameMode
                    if (curMode == com.example.game.GameMode.TIME_ATTACK || curMode == com.example.game.GameMode.MEMORY_PUZZLE) {
                        if (now - lastTimeTick >= 1000) {
                            val elapsedSec = ((now - lastTimeTick) / 1000).toInt()
                            lastTimeTick = now
                            val prevMem = gameEngine.gameState.value.memoryCountdownSeconds
                            gameEngine.decrementTime(elapsedSec)
                            val postMem = gameEngine.gameState.value.memoryCountdownSeconds
                            if (curMode == com.example.game.GameMode.MEMORY_PUZZLE) {
                                if (prevMem > 0 && postMem > 0) {
                                    triggerAudioFeedback("select")
                                } else if (prevMem > 0 && postMem == 0) {
                                    triggerAudioFeedback("start")
                                }
                            }
                        }
                    }

                    // During Memory mode memorization countdown, do not drop pieces
                    if (curMode == com.example.game.GameMode.MEMORY_PUZZLE && gameEngine.gameState.value.memoryCountdownSeconds > 0) {
                        continue
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
                    val prevPuzzleLvl = gameEngine.gameState.value.puzzleLevel
                    gameEngine.tick()
                    val stateAfter = gameEngine.gameState.value
                    if (stateAfter.isGameOver) {
                        triggerAudioFeedback("gameover")
                        saveHighScore()
                        _isPlaying.update { false }
                        prefs.edit().putBoolean("has_saved_game", false).apply()
                        _hasSavedGame.update { false }
                        break
                    }
                    if (stateAfter.puzzleLevel > prevPuzzleLvl) {
                        triggerAudioFeedback("clear_4")
                        addCredits(30)
                        val curSolved = prefs.getInt("stats_pattern_solved", 0) + 1
                        prefs.edit().putInt("stats_pattern_solved", curSolved).apply()
                        addMatchXp(score = 150, lines = 1, modeBonus = 25)
                    } else if (stateAfter.lines > prevLines) {
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

    fun awardMultiplayerCredits(playerScore: Int, opponentScore: Int, won: Boolean, isDraw: Boolean = false) {
        val levelBonusPercent = (getPlayerLevel() / 2)
        val levelMultiplier = 1f + (levelBonusPercent / 100f)
        
        if (won) {
            val currentStreak = _winStreak.value + 1
            _winStreak.value = currentStreak
            val newRating = _onlineRating.value + 25
            _onlineRating.value = newRating
            val totalWins = prefs.getInt("stats_multiplayer_wins", 0) + 1
            prefs.edit().putInt("win_streak", currentStreak)
                .putInt("online_rating", newRating)
                .putInt("stats_multiplayer_wins", totalWins)
                .apply()

            // Minimum score requirement: 1000 points to prevent forfeit farming
            if (playerScore >= 1000) {
                val basePlayCoins = 40
                val performanceCoins = (playerScore / 100).coerceAtMost(80)
                val winBonus = 60
                val streakCoins = (currentStreak * 10).coerceAtMost(100)
                val total = (((basePlayCoins + performanceCoins + winBonus) * 1.2f).toInt() + streakCoins)
                addCredits((total * levelMultiplier).toInt())
            }
            addMatchXp(score = playerScore, lines = (playerScore / 200).coerceAtLeast(1), modeBonus = 60)
        } else if (isDraw) {
            if (playerScore >= 1000) {
                val basePlayCoins = 20
                val performanceCoins = (playerScore / 120).coerceAtMost(50)
                val total = ((basePlayCoins + performanceCoins + 20) * 1.1f).toInt()
                addCredits((total * levelMultiplier).toInt())
            }
            addMatchXp(score = playerScore, lines = (playerScore / 250).coerceAtLeast(1), modeBonus = 40)
        } else {
            _winStreak.value = 0
            val newRating = maxOf(100, _onlineRating.value - 15)
            _onlineRating.value = newRating
            prefs.edit().putInt("win_streak", 0).putInt("online_rating", newRating).apply()

            if (playerScore >= 1000) {
                val basePlayCoins = 15
                val performanceCoins = (playerScore / 150).coerceAtMost(40)
                val total = (basePlayCoins + performanceCoins + 10)
                addCredits((total * levelMultiplier).toInt())
            }
            addMatchXp(score = playerScore, lines = (playerScore / 300).coerceAtLeast(1), modeBonus = 30)
        }

        // Sync rating & winStreak to Firestore if logged in
        val user = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (user != null) {
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users").document(user.uid)
                .set(mapOf("online_rating" to _onlineRating.value, "win_streak" to _winStreak.value), com.google.firebase.firestore.SetOptions.merge())
        }
        evaluateAchievements()
    }

    private fun saveHighScore() {
        val mode = gameEngine.gameState.value.gameMode
        val score = gameEngine.gameState.value.score
        val lines = gameEngine.gameState.value.lines
        
        // Track stats (only if user actually played)
        if (score >= 200 || lines >= 1) {
            val totalGames = prefs.getInt("stats_games_played", 0) + 1
            prefs.edit().putInt("stats_games_played", totalGames).apply()
            _statsGamesPlayed.value = totalGames
        }

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

        if (mode == com.example.game.GameMode.PATTERN_PUZZLE) {
            val pLvl = gameEngine.gameState.value.puzzleLevel
            val maxPLvl = maxOf(prefs.getInt("stats_pattern_level", 1), pLvl)
            prefs.edit().putInt("stats_pattern_level", maxPLvl).apply()
            val maxPScore = maxOf(prefs.getInt("stats_pattern_score", 0), score)
            prefs.edit().putInt("stats_pattern_score", maxPScore).apply()
        }

        // Mode specific achievement evaluations
        when (mode) {
            com.example.game.GameMode.EXTENDED -> {
                if (lines >= 20 || score >= 2000) {
                    prefs.edit().putBoolean("ach_extended_pioneer_unlocked", true).apply()
                }
            }
            com.example.game.GameMode.FAST_RUN -> {
                if (score >= 3000 || lines >= 15) {
                    prefs.edit().putBoolean("ach_speed_runner_unlocked", true).apply()
                }
            }
            com.example.game.GameMode.TIME_ATTACK -> {
                if (score >= 2500 || lines >= 15) {
                    prefs.edit().putBoolean("ach_mode_time_attack_unlocked", true).apply()
                }
            }
            com.example.game.GameMode.MIRROR_DIMENSION -> {
                if (score >= 3000 || lines >= 20) {
                    prefs.edit().putBoolean("ach_mode_mirror_unlocked", true).apply()
                }
            }
            com.example.game.GameMode.RELAX -> {
                if (lines >= 30) {
                    prefs.edit().putBoolean("ach_mode_relax_unlocked", true).apply()
                }
            }
            com.example.game.GameMode.PATTERN_PUZZLE -> {
                val pLvl = gameEngine.gameState.value.puzzleLevel
                if (pLvl >= 5 || prefs.getInt("stats_pattern_solved", 0) >= 5) {
                    prefs.edit().putBoolean("ach_pattern_solver_unlocked", true).apply()
                }
            }
            com.example.game.GameMode.MEMORY_PUZZLE -> {
                val pLvl = gameEngine.gameState.value.puzzleLevel
                if (pLvl >= 5 || prefs.getInt("stats_memory_solved", 0) >= 5) {
                    prefs.edit().putBoolean("ach_memory_master_unlocked", true).apply()
                }
            }
            else -> {}
        }

        saveCurrentProfileToDb()

        // Equal and balanced payout
        if (score >= 500 && !hasAwardedCurrentGameReward) {
            hasAwardedCurrentGameReward = true
            val basePlayCoins = 50
            val performanceCoins = score / 120
            val linesBonus = lines * 4
            val tetrisBonus = tetrisesInGame * 25
            val modeBonus = when (mode) {
                com.example.game.GameMode.CLASSIC, com.example.game.GameMode.RELAX -> 25
                com.example.game.GameMode.EXTENDED, com.example.game.GameMode.FAST_RUN, 
                com.example.game.GameMode.TIME_ATTACK, com.example.game.GameMode.MIRROR_DIMENSION -> 75
                com.example.game.GameMode.BLOCK_BLAST -> 80
                com.example.game.GameMode.PATTERN_PUZZLE, com.example.game.GameMode.MEMORY_PUZZLE -> 90
                else -> 25
            }
            val baseTotal = ((basePlayCoins + performanceCoins + linesBonus + tetrisBonus + modeBonus) * 1.25f).toInt()
            val levelBonusPercent = (getPlayerLevel() / 2)
            val total = (baseTotal * (1f + levelBonusPercent / 100f)).toInt()
            val finalCredits = if (mode == com.example.game.GameMode.PERFECTIONIST) maxOf(1, total / 15) else total
            addCredits(finalCredits)
        } else if (score >= 200 && !hasAwardedCurrentGameReward) {
            hasAwardedCurrentGameReward = true
            addCredits(20)
        }

        addMatchXp(score = score, lines = lines, modeBonus = 40)
        evaluateAchievements()

        if (mode == com.example.game.GameMode.RELAX) {
            // Relax mode scores are not saved to databases or leaderboards!
            return
        }

        if (score > 0) {
            val currentUser = auth.currentUser
            viewModelScope.launch {
                scoreRepo.insert(
                    HighScore(
                        playerName = _playerName.value,
                        score = score,
                        timestamp = System.currentTimeMillis(),
                        hasGradient = _hasNicknameGradient.value,
                        avatarEmoji = _customAvatarEmoji.value,
                        avatarBgColorHex = _customAvatarBgColor.value,
                        avatarFrame = _equippedAvatarFrame.value,
                        onlineTier = _onlineTier.value,
                        title = _equippedTitle.value,
                        uid = currentUser?.uid ?: ""
                    ).apply {
                        customTag = _customTag.value
                    }
                )
            }
            if (currentUser != null) {
                val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                val avatarFile = java.io.File(getApplication<android.app.Application>().filesDir, "custom_avatar_${_playerName.value}.jpg")
                val avatarBase64 = com.example.db.FirebaseSync.fileToBase64(avatarFile) ?: ""
                
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
                                "customTag" to _customTag.value,
                                "avatarEmoji" to _customAvatarEmoji.value,
                                "avatarBgColor" to _customAvatarBgColor.value,
                                "avatarFrame" to _equippedAvatarFrame.value,
                                "avatarBase64" to avatarBase64,
                                "onlineTier" to _onlineTier.value,
                                "title" to _equippedTitle.value,
                                "is_online" to true,
                                "last_synced_timestamp" to System.currentTimeMillis(),
                                "app_version_code" to com.example.BuildConfig.VERSION_CODE
                            )
                            firestore.collection("high_scores").document(currentUser.uid).set(scoreMap, com.google.firebase.firestore.SetOptions.merge())
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
                                "customTag" to _customTag.value,
                                "avatarEmoji" to _customAvatarEmoji.value,
                                "avatarBgColor" to _customAvatarBgColor.value,
                                "avatarFrame" to _equippedAvatarFrame.value,
                                "avatarBase64" to avatarBase64,
                                "onlineTier" to _onlineTier.value,
                                "title" to _equippedTitle.value,
                                "is_online" to true,
                                "last_synced_timestamp" to System.currentTimeMillis(),
                                "app_version_code" to com.example.BuildConfig.VERSION_CODE
                            )
                            firestore.collection("high_scores_by_mode").document(modeCode)
                                .collection("scores").document(currentUser.uid).set(scoreMap, com.google.firebase.firestore.SetOptions.merge())
                        }
                    }
            }
        }
        
        // Evaluate achievements
        evaluateAchievements()
    }

    fun triggerAudioFeedback(type: String) {
        // Sounds completely disabled per user request
        return
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
        userDocSnapshotListener?.remove()
        userDocSnapshotListener = null
        lobbyManager.cleanup()
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
        // Audio resources removed - will be reloaded when non-copyrighted sounds are added
        soundMap.clear()
    }

    private fun playSoundEffect(type: String): Boolean {
        return false
    }

    private fun startLobbyMusic() {
        // Lobby music removed until non-copyrighted music is provided
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
                    val name = (doc.getString("playerName") ?: doc.getString("username") ?: doc.getString("name") ?: "Игрок").ifEmpty { "Игрок" }
                    val score = doc.getLong("score")?.toInt() ?: 0
                    val ts = doc.getLong("timestamp") ?: 0L
                    val hasGrad = doc.getBoolean("hasGradient") ?: false
                    val tag = doc.getString("customTag") ?: ""
                    val emoji = doc.getString("avatarEmoji") ?: doc.getString("custom_avatar_emoji") ?: ""
                    val bg = doc.getString("avatarBgColor") ?: doc.getString("custom_avatar_bg_color") ?: ""
                    val frame = doc.getString("avatarFrame") ?: doc.getString("equipped_avatar_frame") ?: "standard"
                    val tier = doc.getString("onlineTier") ?: "BRONZE"
                    val title = doc.getString("title") ?: doc.getString("equipped_title") ?: ""
                    val uid = doc.getString("uid") ?: doc.id
                    val avatarBase64 = doc.getString("avatarBase64") ?: doc.getString("custom_avatar_base64") ?: ""
                    val rawSynced = doc.getLong("last_synced_timestamp") ?: doc.getLong("lastActive") ?: 0L
                    val rawOnline = doc.getBoolean("is_online") ?: doc.getBoolean("isOnline") ?: false
                    val isOnline = rawOnline && (rawSynced == 0L || (System.currentTimeMillis() - rawSynced) < 120_000L)

                    HighScore(
                        playerName = name,
                        score = score,
                        timestamp = ts,
                        hasGradient = hasGrad,
                        avatarEmoji = emoji,
                        avatarBgColorHex = bg,
                        avatarFrame = frame,
                        onlineTier = tier,
                        title = title,
                        uid = uid
                    ).apply {
                        customTag = tag
                        this.avatarBase64 = avatarBase64
                        this.isOnline = isOnline
                    }
                }
                _globalScores.value = list
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    // --- Friends & Public Profile & Email Verification Helpers ---
    fun setShowFriendsDialog(show: Boolean) {
        _showFriendsDialog.value = show
        if (show) {
            loadFriends()
        }
    }

    fun setShowSignOutConfirmDialog(show: Boolean) {
        _showSignOutConfirmDialog.value = show
    }

    fun openUserProfile(uid: String, fallbackUsername: String = "") {
        _isLoadingPublicProfile.value = true
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val userData = if (userDoc != null && userDoc.exists()) userDoc.data ?: emptyMap() else emptyMap()
                firestore.collection("high_scores").document(uid).get()
                    .addOnSuccessListener { scoreDoc ->
                        val scoreData = if (scoreDoc != null && scoreDoc.exists()) scoreDoc.data ?: emptyMap() else emptyMap()
                        val merged = userData + scoreData
                        val name = (merged["playerName"] as? String)
                            ?: (merged["username"] as? String)
                            ?: fallbackUsername.ifEmpty { "Игрок" }
                        _selectedPublicProfile.value = PublicUserProfile.fromMap(uid, merged + mapOf("username" to name))
                        _isLoadingPublicProfile.value = false
                    }
                    .addOnFailureListener {
                        val name = (userData["playerName"] as? String)
                            ?: (userData["username"] as? String)
                            ?: fallbackUsername.ifEmpty { "Игрок" }
                        _selectedPublicProfile.value = PublicUserProfile.fromMap(uid, userData + mapOf("username" to name))
                        _isLoadingPublicProfile.value = false
                    }
            }
            .addOnFailureListener {
                firestore.collection("high_scores").document(uid).get()
                    .addOnSuccessListener { scoreDoc ->
                        val scoreData = if (scoreDoc != null && scoreDoc.exists()) scoreDoc.data ?: emptyMap() else emptyMap()
                        val name = (scoreData["playerName"] as? String)
                            ?: (scoreData["username"] as? String)
                            ?: fallbackUsername.ifEmpty { "Игрок" }
                        _selectedPublicProfile.value = PublicUserProfile.fromMap(uid, scoreData + mapOf("username" to name))
                        _isLoadingPublicProfile.value = false
                    }
                    .addOnFailureListener {
                        _selectedPublicProfile.value = PublicUserProfile(
                            uid = uid,
                            username = fallbackUsername.ifEmpty { "Игрок" },
                            onlineTier = "BRONZE"
                        )
                        _isLoadingPublicProfile.value = false
                    }
            }
    }

    fun closeUserProfile() {
        _selectedPublicProfile.value = null
    }

    fun loadFriends() {
        val myUid = auth.currentUser?.uid ?: return
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users").document(myUid).collection("friends")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val list = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    FriendUser.fromMap(data + mapOf("uid" to doc.id))
                } ?: emptyList()
                _friendsList.value = list.filter { it.status == "accepted" }
                _friendRequests.value = list.filter { it.status == "pending_incoming" }
            }
    }

    fun sendFriendRequest(targetUsername: String, onResult: (Boolean, String) -> Unit) {
        val myUser = auth.currentUser ?: run {
            onResult(false, "Войдите в аккаунт для добавления друзей")
            return
        }
        val trimmed = targetUsername.trim()
        if (trimmed.isEmpty()) {
            onResult(false, "Введите никнейм игрока")
            return
        }
        if (trimmed.equals(_playerName.value, ignoreCase = true)) {
            onResult(false, "Нельзя добавить самого себя")
            return
        }
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users").whereEqualTo("player_name", trimmed).limit(1).get()
            .addOnSuccessListener { snap ->
                if (snap.isEmpty) {
                    onResult(false, "Игрок '$trimmed' не найден")
                    return@addOnSuccessListener
                }
                val targetDoc = snap.documents[0]
                val targetUid = targetDoc.id
                val targetName = targetDoc.getString("player_name") ?: trimmed
                val targetTier = targetDoc.getString("online_tier") ?: "BRONZE"
                val targetEmoji = targetDoc.getString("custom_avatar_emoji") ?: ""
                val targetBg = targetDoc.getString("custom_avatar_bg_color") ?: ""
                val targetFrame = targetDoc.getString("equipped_avatar_frame") ?: "standard"
                val targetGradient = targetDoc.getBoolean("has_nickname_gradient") ?: false

                val outgoing = FriendUser(
                    uid = targetUid,
                    username = targetName,
                    onlineTier = targetTier,
                    avatarEmoji = targetEmoji,
                    avatarBgColor = targetBg,
                    avatarFrame = targetFrame,
                    hasGradient = targetGradient,
                    status = "pending_outgoing",
                    lastSeen = System.currentTimeMillis()
                )
                firestore.collection("users").document(myUser.uid).collection("friends").document(targetUid).set(outgoing.toMap())

                val incoming = FriendUser(
                    uid = myUser.uid,
                    username = _playerName.value,
                    onlineTier = _onlineTier.value,
                    avatarEmoji = _customAvatarEmoji.value,
                    avatarBgColor = _customAvatarBgColor.value,
                    avatarFrame = _equippedAvatarFrame.value,
                    hasGradient = _hasNicknameGradient.value,
                    status = "pending_incoming",
                    lastSeen = System.currentTimeMillis()
                )
                firestore.collection("users").document(targetUid).collection("friends").document(myUser.uid).set(incoming.toMap())
                    .addOnSuccessListener {
                        onResult(true, "Запрос дружбы отправлен игроку $targetName!")
                    }
                    .addOnFailureListener { e ->
                        onResult(false, e.localizedMessage ?: "Ошибка отправки запроса")
                    }
            }
            .addOnFailureListener { e ->
                onResult(false, e.localizedMessage ?: "Ошибка поиска игрока")
            }
    }

    fun acceptFriendRequest(friend: FriendUser, onResult: (Boolean) -> Unit) {
        val myUser = auth.currentUser ?: return
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val batch = firestore.batch()
        val myRef = firestore.collection("users").document(myUser.uid).collection("friends").document(friend.uid)
        batch.update(myRef, "status", "accepted", "isOnline", true)
        val friendRef = firestore.collection("users").document(friend.uid).collection("friends").document(myUser.uid)
        batch.update(friendRef, "status", "accepted", "isOnline", true)
        batch.commit()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun declineFriendRequest(friend: FriendUser, onResult: (Boolean) -> Unit) {
        val myUser = auth.currentUser ?: return
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val batch = firestore.batch()
        val myRef = firestore.collection("users").document(myUser.uid).collection("friends").document(friend.uid)
        batch.delete(myRef)
        val friendRef = firestore.collection("users").document(friend.uid).collection("friends").document(myUser.uid)
        batch.delete(friendRef)
        batch.commit()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun removeFriend(friend: FriendUser, onResult: (Boolean) -> Unit) {
        declineFriendRequest(friend, onResult)
    }

    fun searchPlayers(query: String, onResult: (List<FriendUser>) -> Unit) {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) {
            onResult(emptyList())
            return
        }
        val firestore = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        firestore.collection("users")
            .whereGreaterThanOrEqualTo("player_name", trimmed)
            .whereLessThanOrEqualTo("player_name", trimmed + "\uf8ff")
            .limit(15)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { doc ->
                    val name = doc.getString("player_name") ?: return@mapNotNull null
                    val tier = doc.getString("online_tier") ?: "BRONZE"
                    val emoji = doc.getString("custom_avatar_emoji") ?: ""
                    val bg = doc.getString("custom_avatar_bg_color") ?: ""
                    val frame = doc.getString("equipped_avatar_frame") ?: "standard"
                    val grad = doc.getBoolean("has_nickname_gradient") ?: false
                    val isOnline = doc.getBoolean("is_online") ?: false
                    FriendUser(
                        uid = doc.id,
                        username = name,
                        onlineTier = tier,
                        avatarEmoji = emoji,
                        avatarBgColor = bg,
                        avatarFrame = frame,
                        hasGradient = grad,
                        isOnline = isOnline,
                        status = "none"
                    )
                }
                onResult(list)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun resendVerificationEmail(onResult: (Boolean, String) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.sendEmailVerification()
                .addOnSuccessListener {
                    onResult(true, "Письмо с подтверждением успешно отправлено!")
                }
                .addOnFailureListener { e ->
                    onResult(false, e.localizedMessage ?: "Ошибка отправки письма")
                }
        } else {
            onResult(false, "Пользователь не авторизован")
        }
    }

    fun checkEmailVerificationStatus(onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            user.reload().addOnCompleteListener {
                val isVerified = user.isEmailVerified || profilePrefs.getBoolean("is_email_verified", false)
                _isEmailVerified.value = isVerified
                if (isVerified) {
                    _showVerificationBanner.value = false
                    profilePrefs.edit().putBoolean("is_email_verified", true).apply()
                    com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        .collection("users").document(user.uid).set(
                            mapOf(
                                "email" to (user.email ?: ""),
                                "email_verified" to true,
                                "is_verified" to true,
                                "emailVerified" to true
                            ),
                            com.google.firebase.firestore.SetOptions.merge()
                        )
                }
                onResult(isVerified)
            }
        } else {
            onResult(false)
        }
    }

    companion object {
        val ALL_THEMES = setOf(
            "monet", "indigo", "black", "neon", "red", "emerald", "amber",
            "rose", "sky", "orange", "cyber_pink", "toxic_green", "gold"
        )

        fun getSystemDefaultLanguage(): Language {
            val sysLang = java.util.Locale.getDefault().language.lowercase()
            return when {
                sysLang.startsWith("ru") || sysLang.startsWith("be") || sysLang.startsWith("ky") -> Language.RU
                sysLang.startsWith("uk") -> Language.UA
                sysLang.startsWith("kk") -> Language.KK
                sysLang.startsWith("de") -> Language.DE
                sysLang.startsWith("zh") -> Language.ZH
                else -> Language.EN
            }
        }
    }
}
