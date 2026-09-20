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


// ── Full-Screen Mode Selection Screen ──
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSelectionScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onPlayMode: (com.example.game.GameMode) -> Unit
) {
    val currentLang by viewModel.language.collectAsStateWithLifecycle()
    val credits by viewModel.credits.collectAsStateWithLifecycle()
    var lockedModeWarning by remember { mutableStateOf<String?>(null) }
    var lastPurchaseTimestamp by remember { mutableLongStateOf(0L) }
    fun canPurchase(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastPurchaseTimestamp < 400L) return false
        lastPurchaseTimestamp = now
        return true
    }

    val purchasedModesSet = remember(credits) {
        val sharedPrefs = viewModel.getApplication<android.app.Application>()
            .getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
        sharedPrefs.getStringSet("purchased_modes", setOf("classic", "extended", "fast_run", "slide", "block_blast"))
            ?: setOf("classic", "extended", "fast_run", "slide", "block_blast")
    }

    data class ModeInfo(
        val mode: com.example.game.GameMode,
        val modeId: String,
        val title: String,
        val description: String,
        val howToPlay: String,
        val cost: Int,
        val icon: androidx.compose.ui.graphics.vector.ImageVector,
        val categoryTag: String // "FREE", "PAID", "HARD", "FUN"
    )

    val modes = listOf(
        ModeInfo(
            com.example.game.GameMode.CLASSIC, "classic",
            when (currentLang) {
                Language.RU -> "Стандарт"
                Language.UA -> "Стандарт"
                Language.KK -> "Стандарт"
                Language.DE -> "Standard"
                Language.ZH -> "标准"
                else -> "Standard"
            },
            when (currentLang) {
                Language.RU -> "Оригинальный режим с нарастающей сложностью."
                Language.UA -> "Оригінальний режим із наростаючою складністю."
                Language.KK -> "Өсіп келе жатқан қиындығы бар түпнұсқа режим."
                Language.DE -> "Das originale Erlebnis mit steigender Geschwindigkeit."
                Language.ZH -> "速度逐渐递增的经典原始模式。"
                else -> "The original experience with increasing speed."
            },
            when (currentLang) {
                Language.RU -> "Укладывайте фигурки и заполняйте горизонтальные линии. Каждые 10 очищенных линий повышают уровень и скорость игры."
                Language.UA -> "Укладайте фігурки та заповнюйте горизонтальні лінії. Кожні 10 очищених ліній підвищують рівень і швидкість гри."
                Language.KK -> "Фигураларды орналастырып, көлденең сызықтарды толтырыңыз. Әрбір 10 тазартылған сызық ойын деңгейі мен жылдамдығын арттырады."
                Language.DE -> "Platziere herabfallende Blöcke, um Linien zu vervollständigen. Alle 10 Linien steigen Level und Tempo."
                Language.ZH -> "旋转摆放掉落的方块以消除整行。每消除10行即可提升等级与下落速度。"
                else -> "Fit falling blocks to clear horizontal lines. Every 10 cleared lines increases game level and falling speed."
            },
            0, Icons.Default.VideogameAsset, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.TIME_ATTACK, "time_attack",
            when (currentLang) {
                Language.RU -> "Блиц"
                Language.UA -> "Бліц"
                Language.KK -> "Блиц"
                Language.DE -> "Blitz"
                Language.ZH -> "闪击"
                else -> "Blitz"
            },
            when (currentLang) {
                Language.RU -> "Режим с ограничением времени: старт с 60 секунд."
                Language.UA -> "Режим з обмеженням часу: старт із 60 секунд."
                Language.KK -> "Уақыт шектеуі бар режим: 60 секундтан басталады."
                Language.DE -> "Zeitbegrenzter Modus: Startet bei 60 Sekunden."
                Language.ZH -> "限时竞速模式：从60秒倒计时开始。"
                else -> "Time-limited mode: starts at 60 seconds."
            },
            when (currentLang) {
                Language.RU -> "Игра начинается с 60 секундами на таймере. Очищайте линии, чтобы прибавлять по 10 секунд за каждую линию. Время стремительно иссекает!"
                Language.UA -> "Гра починається з 60 секундами на таймері. Очищайте лінії, щоб додавати по 10 секунд за кожну лінію. Час стрімко спливає!"
                Language.KK -> "Ойын таймерде 60 секундпен басталады. Әр сызық үшін 10 секунд қосу мақсатында сызықтарды тазартыңыз. Уақыт тез таусылады!"
                Language.DE -> "Startet mit 60s auf der Uhr. Jede gelöschte Zeile bringt +10s. Sammle Punkte, bevor die Zeit abläuft!"
                Language.ZH -> "初始计时器为60秒。每消除一行增加10秒时间奖励，在时间耗尽前尽可能斩获高分！"
                else -> "Starts with 60s on the clock. Clear lines to gain +10 seconds per line. Score as much as possible before time runs out!"
            },
            ShopPrices.getModeCost("time_attack"), Icons.Default.Schedule, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.EXTENDED, "extended",
            when (currentLang) {
                Language.RU -> "Спектр"
                Language.UA -> "Спектр"
                Language.KK -> "Спектр"
                Language.DE -> "Spektrum"
                Language.ZH -> "光谱"
                else -> "Spectrum"
            },
            when (currentLang) {
                Language.RU -> "Игра фигурами из пяти блоков для повышенной сложности."
                Language.UA -> "Гра фігурами з п'яти блоків для підвищеної складності."
                Language.KK -> "Жоғары қиындық үшін бес блоктан тұратын фигуралармен ойнау."
                Language.DE -> "Gameplay mit 5-Block-Steinen für zusätzliche Herausforderung."
                Language.ZH -> "使用五格骨牌方块提升挑战难度。"
                else -> "Gameplay using five-block pieces for an extra challenge."
            },
            when (currentLang) {
                Language.RU -> "Режим классического тетриса, но с использованием 5-блочных фигур (пентамино). Заполняйте линии в условиях высокой плотности фигур."
                Language.UA -> "Режим класичного тетрісу, але з використанням 5-блокових фігур (пентаміно). Заповнюйте лінії в умовах високої щільності фігур."
                Language.KK -> "Классикалық тетрис режимі, бірақ 5 блокты фигураларды (пентамино) қолданумен. Тығыз фигуралар жағдайында сызықтарды толтырыңыз."
                Language.DE -> "Klassische Tetris-Regeln mit 5-Block-Pentaminos. Fülle Reihen mit komplexen Formen."
                Language.ZH -> "经典下落消除玩法，但使用复杂的五联块（Pentamino）。在复杂的方块几何中考验空间规划。"
                else -> "Classic tetris rules, but played with 5-block pentamino pieces. Fill rows with complex shapes."
            },
            ShopPrices.getModeCost("extended"), Icons.Default.Star, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.FAST_RUN, "fast_run",
            when (currentLang) {
                Language.RU -> "Спринт"
                Language.UA -> "Спринт"
                Language.KK -> "Спринт"
                Language.DE -> "Sprint"
                Language.ZH -> "冲刺"
                else -> "Sprint"
            },
            when (currentLang) {
                Language.RU -> "Начало игры с 10-го уровня сложности."
                Language.UA -> "Початок гри з 10-го рівня складності."
                Language.KK -> "Ойынды 10-деңгейдегі қиындықтан бастау."
                Language.DE -> "Startet direkt bei Schwierigkeitsgrad Level 10."
                Language.ZH -> "从难度等级10直接开始极速挑战。"
                else -> "Starts the game at Level 10 difficulty."
            },
            when (currentLang) {
                Language.RU -> "Игра сразу начинается на 10 уровне скорости! Требует молниеносной реакции и быстрого принятия решений."
                Language.UA -> "Гра одразу починається на 10 рівні швидкості! Вимагає блискавичної реакції та швидкого прийняття рішень."
                Language.KK -> "Ойын бірден 10 жылдамдық деңгейінде басталады! Жылдам реакция мен жылдам шешім қабылдауды талап етеді."
                Language.DE -> "Startet direkt auf Geschwindigkeitsstufe 10! Erfordert blitzschnelle Reflexe."
                Language.ZH -> "直接以极高下落速度开始！极度考验临场反应与瞬间决策能力。"
                else -> "Game starts immediately at speed level 10! Requires lightning reflexes and fast placement decisions."
            },
            ShopPrices.getModeCost("fast_run"), Icons.Default.FlashOn, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.BLOCK_BLAST, "block_blast",
            when (currentLang) {
                Language.RU -> "Zeta"
                Language.UA -> "Zeta"
                Language.KK -> "Zeta"
                Language.DE -> "Zeta"
                Language.ZH -> "Zeta"
                else -> "Zeta"
            },
            when (currentLang) {
                Language.RU -> "Свободное размещение фигурок на игровом поле."
                Language.UA -> "Вільне розміщення фігурок на ігровому полі."
                Language.KK -> "Ойын алаңында фигураларды еркін орналастыру."
                Language.DE -> "Freies Platzieren von Blöcken auf dem Spielfeld."
                Language.ZH -> "在网格中自由拖拽摆放方块。"
                else -> "Free placement of polyominos on the board."
            },
            when (currentLang) {
                Language.RU -> "Перетаскивайте и ставьте процедурно сгенерированные блоки в любую свободную область поля для сбора линий."
                Language.UA -> "Перетягуйте та ставте процедурно згенеровані блоки в будь-яку вільну область поля для збору ліній."
                Language.KK -> "Сызықтарды жинау үшін процедуралық түрде жасалған блоктарды өрістің кез келген бос аймағына сүйреп апарыңыз."
                Language.DE -> "Platziere zufällig generierte Blöcke auf dem Gitter, um Zeilen und Spalten zu leeren."
                Language.ZH -> "将随机生成的方块组合拖放至棋盘任意空位，填满整行或整列进行消除并获取连击积分。"
                else -> "Drag and place procedurally generated blocks anywhere on the grid to clear rows and columns."
            },
            ShopPrices.getModeCost("block_blast"), Icons.Default.Computer, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.PATTERN_PUZZLE, "pattern",
            when (currentLang) {
                Language.RU -> "Шаблон"
                Language.UA -> "Шаблон"
                Language.KK -> "Үлгі"
                Language.DE -> "Muster"
                Language.ZH -> "图腾拼图"
                else -> "Pattern Puzzle"
            },
            when (currentLang) {
                Language.RU -> "Заполните светящийся шаблон на поле заданными блоками."
                Language.UA -> "Заповніть сяючий шаблон на полі заданими блоками."
                Language.KK -> "Берілген блоктармен өрістегі жарқыраған үлгіні толтырыңыз."
                Language.DE -> "Fülle die markierte Schablone mit fallenden Steinen."
                Language.ZH -> "用掉落方块精准填充棋盘上的目标轮廓。"
                else -> "Fill the target outline pattern on the board with falling blocks."
            },
            when (currentLang) {
                Language.RU -> "На поле отображается контур фигуры. Заполните каждую подсвеченную клетку, чтобы пройти уровень!"
                Language.UA -> "На полі відображається контур фігури. Заповніть кожну підсвічену клітинку, щоб пройти рівень!"
                Language.KK -> "Өрісте фигураның контуры көрсетіледі. Деңгейден өту үшін әрбір бөлектелген ұяшықты толтырыңыз!"
                Language.DE -> "Auf dem Feld erscheint eine Schablone. Fülle alle markierten Zellen, um das Level abzuschließen!"
                Language.ZH -> "棋盘上投射出目标轮廓。精准摆放方块填满所有高亮方格即可通关！"
                else -> "A target outline is shown on the grid. Fill every highlighted cell to complete the stage!"
            },
            ShopPrices.getModeCost("pattern"), Icons.Default.AutoAwesome, "HARD"
        ),
        ModeInfo(
            com.example.game.GameMode.MIRROR_DIMENSION, "mirror",
            when (currentLang) {
                Language.RU -> "Зеркало"
                Language.UA -> "Дзеркало"
                Language.KK -> "Айна"
                Language.DE -> "Spiegel"
                Language.ZH -> "镜界"
                else -> "Mirror"
            },
            when (currentLang) {
                Language.RU -> "Поле отражается по горизонтали во время игры."
                Language.UA -> "Поле відображається по горизонталі під час гри."
                Language.KK -> "Ойын барысында алаң көлденеңінен шағылысады."
                Language.DE -> "Das Spielfeld spiegelt sich horizontal im Spiel."
                Language.ZH -> "游戏过程中棋盘会周期性水平翻转镜像。"
                else -> "The game field is mirrored horizontally during gameplay."
            },
            when (currentLang) {
                Language.RU -> "Игровое поле и падающие блоки периодически отражаются зеркально по горизонтали!"
                Language.UA -> "Ігрове поле та падаючі блоки періодично відображаються дзеркально по горизонталі!"
                Language.KK -> "Ойын алаңы мен құлаған блоктар мезгіл-мезгіл көлденеңінен айнадай шағылысады!"
                Language.DE -> "Das Spielfeld dreht sich periodisch horizontal um und fordert dein räumliches Denken heraus."
                Language.ZH -> "整个棋盘和下落方块会突然水平镜面翻转，颠覆你的空间知觉与操控直觉！"
                else -> "The board periodically flips horizontally, challenging your spatial orientation."
            },
            ShopPrices.getModeCost("mirror"), Icons.Default.SwapHoriz, "FUN"
        ),
        ModeInfo(
            com.example.game.GameMode.MEMORY_PUZZLE, "memory",
            when (currentLang) {
                Language.RU -> "Память"
                Language.UA -> "Пам'ять"
                Language.KK -> "Жады"
                Language.DE -> "Gedächtnis"
                Language.ZH -> "记忆大师"
                else -> "Memory Puzzle"
            },
            when (currentLang) {
                Language.RU -> "Запомните шаблон за 3 секунды до того, как он исчезнет!"
                Language.UA -> "Запам'ятайте шаблон за 3 секунди до того, як він зникне!"
                Language.KK -> "Үлгіні жоғалғанға дейін 3 секунд ішінде есте сақтаңыз!"
                Language.DE -> "Präge dir das Muster in 3 Sekunden ein, bevor es verschwindet!"
                Language.ZH -> "在目标图案隐形前的3秒内记住其位置！"
                else -> "Memorize the pattern in 3 seconds before it disappears!"
            },
            when (currentLang) {
                Language.RU -> "Шаблон показывается всего 3 секунды в начале каждого раунда, после чего исчезает! Соберите его по памяти."
                Language.UA -> "Шаблон показується лише 3 секунди на початку кожного раунду, після чого зникає! Зберіть його по пам'яті."
                Language.KK -> "Үлгі әр раундтың басында тек 3 секунд көрсетіледі, содан кейін жоғалады! Оны жадыңыздан жинаңыз."
                Language.DE -> "Das Muster wird nur 3 Sekunden lang eingeblendet und verschwindet dann! Setze es aus dem Gedächtnis zusammen."
                Language.ZH -> "目标图案仅展示3秒随即隐形！全凭瞬间记忆在脑海中复原并完成方块拼合。"
                else -> "The target pattern is visible for only 3 seconds before vanishing! Fill it entirely from memory."
            },
            ShopPrices.getModeCost("memory"), Icons.Default.Visibility, "HARD"
        ),
        ModeInfo(
            com.example.game.GameMode.RELAX, "relax",
            when (currentLang) {
                Language.RU -> "Песочница"
                Language.UA -> "Пісочниця"
                Language.KK -> "Құмсалғыш"
                Language.DE -> "Sandbox"
                Language.ZH -> "沙盒"
                else -> "Sandbox"
            },
            when (currentLang) {
                Language.RU -> "Настраиваемый режим с выбором блоков и скоростей."
                Language.UA -> "Налаштовуваний режим з вибором блоків і швидкостей."
                Language.KK -> "Блоктар мен жылдамдықтарды таңдау мүмкіндігі бар бапталатын режим."
                Language.DE -> "Anpassbare Sandbox: Wähle Steine und Geschwindigkeit."
                Language.ZH -> "自由配置方块与速度的自定义沙盒。"
                else -> "Customizable sandbox: select blocks and speed."
            },
            when (currentLang) {
                Language.RU -> "Песочница с гибкой настройкой параметров игры под ваше настроение."
                Language.UA -> "Пісочниця з гнучким налаштуванням параметрів гри під ваш настрій."
                Language.KK -> "Көңіл-күйіңізге сай ойын параметрлерін еркін реттейтін құмсалғыш."
                Language.DE -> "Ein Sandbox-Modus для vollkommen individuelle Spielregeln nach Lust und Laune."
                Language.ZH -> "完全自由定义的沙盒模式，随心定制专属游戏规则和方块类型。"
                else -> "A sandbox mode allowing full customization of gameplay rules to match your preference."
            },
            ShopPrices.getModeCost("relax"), Icons.Default.Spa, "FREE"
        ),
        ModeInfo(
            com.example.game.GameMode.PERFECTIONIST, "perfectionist",
            when (currentLang) {
                Language.RU -> "Идеал"
                Language.UA -> "Ідеал"
                Language.KK -> "Мінсіз"
                Language.DE -> "Perfektion"
                Language.ZH -> "完美"
                else -> "Perfection"
            },
            when (currentLang) {
                Language.RU -> "Идеальные фигуры и ИИ-подсветка лучшей позиции."
                Language.UA -> "Ідеальні фігури та ШІ-підсвічування кращої позиції."
                Language.KK -> "Мінсіз фигуралар және ЖИ ең жақсы орын нұсқауы."
                Language.DE -> "Ideale Steine & KI-Leitstrahl für perfekte Platzierung."
                Language.ZH -> "极致顺滑方块序列与AI黄金最优落点辅助。"
                else -> "Ideal piece bags & holographic AI optimal placement guide."
            },
            when (currentLang) {
                Language.RU -> "Режим абсолютной гармонии: игра выдает идеальные фигуры для непрерывных линий, а золотой голографический гид подсказывает математически точную позицию сброса! (Награда в 15 раз меньше)."
                Language.UA -> "Режим абсолютної гармонії: ідеальні фігури та золотий голографічний гід! (Нагорода в 15 разів менше)."
                Language.KK -> "Үйлесімділік режимі: мінсіз фигуралар мен алтын голографиялық көмекші! (Сыйақы 15 есе аз)."
                Language.DE -> "Modus der absoluten Harmonie: Perfekte Figuren und goldener KI-Leitstrahl für fehlerfreies Stapeln! (15-fach reduzierte Belohnung)."
                Language.ZH -> "完美秩序模式：系统持续生成最易拼合的方块，全息黄金光标实时指引AI最优摆放位置！（收益为经典模式的1/15）。"
                else -> "Mode of absolute harmony: Ideal piece sequence with real-time golden holographic optimal drop guide! (Rewards scaled 1/15th)."
            },
            ShopPrices.getModeCost("perfectionist"), Icons.Default.Star, "PAID"
        )
    )

    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedInfoMode by remember { mutableStateOf<ModeInfo?>(null) }

    val filterChips = listOf(
        "ALL" to when (currentLang) {
            Language.RU -> "Все"
            Language.UA -> "Всі"
            Language.KK -> "Барлығы"
            Language.DE -> "Alle"
            Language.ZH -> "全部"
            else -> "All"
        },
        "FREE" to when (currentLang) {
            Language.RU -> "Бесплатные"
            Language.UA -> "Безкоштовні"
            Language.KK -> "Тегін"
            Language.DE -> "Kostenlos"
            Language.ZH -> "免费"
            else -> "Free"
        },
        "PAID" to when (currentLang) {
            Language.RU -> "Платные"
            Language.UA -> "Платні"
            Language.KK -> "Ақылы"
            Language.DE -> "Kaufbar"
            Language.ZH -> "付费"
            else -> "Paid"
        },
        "HARD" to when (currentLang) {
            Language.RU -> "Сложные"
            Language.UA -> "Складні"
            Language.KK -> "Күрделі"
            Language.DE -> "Schwer"
            Language.ZH -> "高难"
            else -> "Hard"
        },
        "FUN" to when (currentLang) {
            Language.RU -> "Развлечение"
            Language.UA -> "Розваги"
            Language.KK -> "Көңілді"
            Language.DE -> "Casual / Spaß"
            Language.ZH -> "休闲趣味"
            else -> "Casual / Fun"
        }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets(top = 8.dp),
                title = {
                    val selectModeTitle = when (currentLang) {
                        Language.RU -> "РЕЖИМЫ"
                        Language.UA -> "РЕЖИМИ"
                        Language.KK -> "РЕЖИМДЕР"
                        Language.DE -> "MODI"
                        Language.ZH -> "游戏模式"
                        else -> "MODES"
                    }
                    AdaptiveText(
                        text = selectModeTitle,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
        val themeColorVal = MaterialTheme.colorScheme.primary

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Category Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filterChips) { (key, label) ->
                    val isSelected = selectedCategoryFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.triggerAudioFeedback("click")
                            selectedCategoryFilter = key
                        },
                        label = {
                            AdaptiveText(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = themeColorVal,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                        )
                    )
                }
            }

            val filteredModes = remember(selectedCategoryFilter, modes) {
                when (selectedCategoryFilter) {
                    "FREE" -> modes.filter { it.cost == 0 }
                    "PAID" -> modes.filter { it.cost > 0 }
                    "HARD" -> modes.filter { it.categoryTag == "HARD" }
                    "FUN" -> modes.filter { it.categoryTag == "FUN" }
                    else -> modes
                }
            }

            run {

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (filteredModes.isEmpty()) {
                        item {
                            val emptyCategoryMsg = when (currentLang) {
                                Language.RU -> "Нет режимов в этой категории"
                                Language.UA -> "Немає режимів у цій категорії"
                                Language.KK -> "Бұл санатта режимдер жоқ"
                                Language.DE -> "Keine Modi in dieser Kategorie"
                                Language.ZH -> "该分类下暂无游戏模式"
                                else -> "No modes found in this category"
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = emptyCategoryMsg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    items(filteredModes.size) { index ->
                        val modeInfo = filteredModes[index]
                        val isPurchased = purchasedModesSet.contains(modeInfo.modeId)
                        val isPlayable = isPurchased || modeInfo.cost == 0


                        val modeAcquiredToast = when (currentLang) {
                            Language.RU -> "Режим разблокирован!"
                            Language.UA -> "Режим розблоковано!"
                            Language.KK -> "Режим ашылды!"
                            Language.DE -> "Spielmodus freigeschaltet!"
                            Language.ZH -> "模式解锁成功！"
                            else -> "Game Mode acquired!"
                        }
                        val insufficientCreditsToast = when (currentLang) {
                            Language.RU -> "Недостаточно кредитов!"
                            Language.UA -> "Недостатньо кредитів!"
                            Language.KK -> "Кредит жеткіліксіз!"
                            Language.DE -> "Nicht genügend Credits!"
                            Language.ZH -> "金币余额不足！"
                            else -> "Insufficient credits!"
                        }

                        ElevatedCard(
                            onClick = {
                                if (isPlayable) {
                                    viewModel.triggerAudioFeedback("click")
                                    onPlayMode(modeInfo.mode)
                                } else {
                                    if (credits >= modeInfo.cost && canPurchase() && viewModel.spendCredits(modeInfo.cost)) {
                                        val updated = purchasedModesSet.toMutableSet().apply { add(modeInfo.modeId) }
                                        val sharedPrefs = viewModel.getApplication<android.app.Application>()
                                            .getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
                                        sharedPrefs.edit().putStringSet("purchased_modes", updated).apply()
                                        viewModel.saveCurrentProfileToDb()
                                        viewModel.triggerAudioFeedback("buy")
                                        lockedModeWarning = modeAcquiredToast
                                    } else if (credits < modeInfo.cost) {
                                        viewModel.triggerAudioFeedback("error")
                                        lockedModeWarning = insufficientCreditsToast
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (isPlayable) MaterialTheme.colorScheme.surfaceContainerHigh
                                                 else MaterialTheme.colorScheme.surfaceContainer
                            ),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.elevatedCardElevation(
                                defaultElevation = if (isPlayable) 3.dp else 1.dp
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Mode Icon Avatar
                                    Surface(
                                        modifier = Modifier.size(56.dp),
                                        shape = RoundedCornerShape(18.dp),
                                        color = if (isPlayable) themeColorVal.copy(alpha = 0.15f)
                                                else MaterialTheme.colorScheme.surfaceContainerHighest,
                                        tonalElevation = 1.dp
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            if (isPlayable) {
                                                Icon(
                                                    imageVector = modeInfo.icon,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(28.dp),
                                                    tint = themeColorVal
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp),
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }

                                    // Title & 2nd line tags
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        AdaptiveText(
                                            text = modeInfo.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isPlayable) MaterialTheme.colorScheme.onSurface
                                                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )

                                        // Second line tags chips for all modes
                                        val modeTags = remember(modeInfo, isPurchased, currentLang) {
                                            val list = mutableListOf<Pair<String, Color>>()
                                            val freeTag = when (currentLang) {
                                                Language.RU -> "БЕСПЛАТНО"
                                                Language.UA -> "БЕЗКОШТОВНО"
                                                Language.KK -> "ТЕГІН"
                                                Language.DE -> "GRATIS"
                                                Language.ZH -> "免费"
                                                else -> "FREE"
                                            }
                                            val ownedTag = when (currentLang) {
                                                Language.RU -> "КУПЛЕНО"
                                                Language.UA -> "КУПЛЕНО"
                                                Language.KK -> "САТЫП АЛЫНДЫ"
                                                Language.DE -> "GEKAUFT"
                                                Language.ZH -> "已拥有"
                                                else -> "OWNED"
                                            }
                                            val crSuffix = when (currentLang) {
                                                Language.RU -> "КР."
                                                Language.UA -> "КР."
                                                Language.KK -> "КР."
                                                Language.DE -> "CR"
                                                Language.ZH -> "代币"
                                                else -> "CR"
                                            }
                                            val hardTag = when (currentLang) {
                                                Language.RU -> "СЛОЖНЫЙ"
                                                Language.UA -> "СКЛАДНИЙ"
                                                Language.KK -> "КҮРДЕЛІ"
                                                Language.DE -> "SCHWER"
                                                Language.ZH -> "困难"
                                                else -> "HARD"
                                            }
                                            val casualTag = when (currentLang) {
                                                Language.RU -> "ФАН"
                                                Language.UA -> "ФАН"
                                                Language.KK -> "КӨҢІЛДІ"
                                                Language.DE -> "CASUAL"
                                                Language.ZH -> "休闲"
                                                else -> "CASUAL"
                                            }
                                            val standardTag = when (currentLang) {
                                                Language.RU -> "СТАНДАРТ"
                                                Language.UA -> "СТАНДАРТ"
                                                Language.KK -> "СТАНДАРТ"
                                                Language.DE -> "STANDARD"
                                                Language.ZH -> "标准"
                                                else -> "STANDARD"
                                            }

                                            if (modeInfo.cost == 0) {
                                                list.add(freeTag to themeColorVal)
                                            } else if (isPurchased) {
                                                list.add(ownedTag to Color(0xFF4CAF50))
                                            } else {
                                                list.add("${modeInfo.cost} $crSuffix" to Color(0xFFFFB300))
                                            }

                                            when (modeInfo.categoryTag) {
                                                "HARD" -> list.add(hardTag to Color(0xFFFF5252))
                                                "FUN" -> list.add(casualTag to Color(0xFF00E5FF))
                                                else -> list.add(standardTag to themeColorVal)
                                            }
                                            list
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            modeTags.forEach { (tagText, tagColor) ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = tagColor.copy(alpha = 0.14f)
                                                ) {
                                                    Text(
                                                        text = tagText,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = tagColor,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Info "i" button without grey border/background
                                    IconButton(
                                        onClick = {
                                            viewModel.triggerAudioFeedback("click")
                                            selectedInfoMode = modeInfo
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Mode Info",
                                            tint = themeColorVal,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Bottom action bar inside card
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isPlayable) {
                                        val playLabel = when (currentLang) {
                                            Language.RU -> "ИГРАТЬ"
                                            Language.UA -> "ГРАТИ"
                                            Language.KK -> "ОЙНАУ"
                                            Language.DE -> "SPIELEN"
                                            Language.ZH -> "开始游戏"
                                            else -> "PLAY"
                                        }
                                        Button(
                                            onClick = {
                                                viewModel.triggerAudioFeedback("click")
                                                onPlayMode(modeInfo.mode)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = themeColorVal,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            AdaptiveText(
                                                text = playLabel,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    } else {
                                        val unlockPrefix = when (currentLang) {
                                            Language.RU -> "ОТКРЫТЬ ЗА"
                                            Language.UA -> "ВІДКРИТИ ЗА"
                                            Language.KK -> "МЫНАҒАН АШУ"
                                            Language.DE -> "KAUFEN FÜR"
                                            Language.ZH -> "解锁花费"
                                            else -> "UNLOCK FOR"
                                        }
                                        FilledTonalButton(
                                            onClick = {
                                                if (credits >= modeInfo.cost && canPurchase() && viewModel.spendCredits(modeInfo.cost)) {
                                                    val updated = purchasedModesSet.toMutableSet().apply { add(modeInfo.modeId) }
                                                    val sharedPrefs = viewModel.getApplication<android.app.Application>()
                                                        .getSharedPreferences("block_tetris_prefs", android.content.Context.MODE_PRIVATE)
                                                    sharedPrefs.edit().putStringSet("purchased_modes", updated).apply()
                                                    viewModel.saveCurrentProfileToDb()
                                                    viewModel.triggerAudioFeedback("buy")
                                                    lockedModeWarning = modeAcquiredToast
                                                } else if (credits < modeInfo.cost) {
                                                    viewModel.triggerAudioFeedback("error")
                                                    lockedModeWarning = insufficientCreditsToast
                                                }
                                            },
                                            colors = ButtonDefaults.filledTonalButtonColors(
                                                containerColor = Color(0xFFFFB300).copy(alpha = 0.18f),
                                                contentColor = Color(0xFFFF8F00)
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ShoppingCart,
                                                contentDescription = null,
                                                tint = Color(0xFFFF8F00),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            AdaptiveText(
                                                text = "$unlockPrefix ${modeInfo.cost}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Info Dialog showing detailed rules/explanation for the selected mode
    selectedInfoMode?.let { info ->
        val freeModeLabel = when (currentLang) {
            Language.RU -> "Бесплатный режим"
            Language.UA -> "Безкоштовний режим"
            Language.KK -> "Тегін режим"
            Language.DE -> "Kostenloser Modus"
            Language.ZH -> "免费模式"
            else -> "Free Mode"
        }
        val costPrefix = when (currentLang) {
            Language.RU -> "Стоимость:"
            Language.UA -> "Вартість:"
            Language.KK -> "Құны:"
            Language.DE -> "Kosten:"
            Language.ZH -> "价格："
            else -> "Cost:"
        }
        val gotItLabel = when (currentLang) {
            Language.RU -> "ПОНЯТНО"
            Language.UA -> "ЗРОЗУМІЛО"
            Language.KK -> "ТҮСІНІКТІ"
            Language.DE -> "VERSTANDEN"
            Language.ZH -> "我知道了"
            else -> "GOT IT"
        }
        AlertDialog(
            onDismissRequest = { selectedInfoMode = null },
            icon = {
                Icon(
                    imageVector = info.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text(
                    text = info.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (info.cost == 0) freeModeLabel else "$costPrefix ${info.cost} монет",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (info.cost == 0) MaterialTheme.colorScheme.primary else Color(0xFFFF8F00)
                            )
                        }
                    }
                    Text(
                        text = info.howToPlay,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Start
                    )
                }
            },
            confirmButton = {
                val gotItLabel = when (currentLang) {
                    Language.RU -> "ПОНЯТНО"
                    Language.UA -> "ЗРОЗУМІЛО"
                    Language.KK -> "ТҮСІНІКТІ"
                    Language.DE -> "VERSTANDEN"
                    Language.ZH -> "我知道了"
                    else -> "GOT IT"
                }
                Button(
                    onClick = { selectedInfoMode = null },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(gotItLabel)
                }
            }
        )
    }

    if (lockedModeWarning != null) {
        val systemAlertTitle = when (currentLang) {
            Language.RU -> "Внимание"
            Language.UA -> "Увага"
            Language.KK -> "Назар аударыңыз"
            Language.DE -> "Hinweis"
            Language.ZH -> "系统提示"
            else -> "System Alert"
        }
        AlertDialog(
            onDismissRequest = { lockedModeWarning = null },
            title = { AdaptiveText(systemAlertTitle) },
            text = { AdaptiveText(lockedModeWarning ?: "", maxLines = 3) },
            confirmButton = {
                TextButton(onClick = { lockedModeWarning = null }) {
                    AdaptiveText("OK")
                }
            }
        )
    }
}

