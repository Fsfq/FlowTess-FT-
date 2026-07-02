package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class Language(val code: String, val displayName: String) {
    EN("EN", "English"),
    RU("RU", "Русский"),
    UA("UA", "Українська"),
    KK("KK", "Қазақша"),
    DE("DE", "Deutsch")
}

object Translations {
    private val en = mapOf(
        "app_name" to "Tetris",
        "play" to "Classic Play",
        "upgrades" to "Extended Play",
        "multiplayer" to "Multiplayer",
        "left_handed_controls" to "Left-Handed Mirror Controls Layout",
        "game_speed_multiplier" to "Game Engine Tick Speed Multiplier:",
        "control_button_scale" to "Control Buttons Scale Size:",
        "control_button_style" to "Interactive Control Button Style:",
        "grid_line_density" to "Grid Board Lines Architecture Style:",
        "leaderboard" to "Leaderboard",
        "settings" to "Settings",
        "score" to "Score",
        "level" to "Level",
        "lines" to "Lines",
        "game_over" to "Game Over",
        "restart" to "Restart",
        "back" to "Back",
        "hold" to "Hold",
        "next" to "Next",
        "chat" to "Chat",
        "save_game" to "Save Game",
        "resume" to "Resume Game",
        "language" to "Language",
        "high_score" to "HIGH SCORE",
        "fps_mode" to "144 FPS UNLOCKED",
        "sound_effects" to "Sound Effects",
        "vibration" to "Haptic Feedback",
        "controls_style" to "Tactile Controls Styles",
        "save_success" to "Current game progress saved successfully!",
        "save_fail" to "No active game to save.",
        "welcome" to "Select game mode:",
        "play_modes" to "Modes",
        "type_msg" to "Type a message...",
        "controls_hint" to "Tap arrow buttons below to control blocks",
        "change_language" to "Change Language",
        "lobby_header" to "Battle Arena Lobby",
        "waiting_opponent" to "Waiting for second player...",
        "player_you" to "Alex_Swift (You)",
        "player_opponent" to "Opponent",
        "send" to "Send",
        "gaming_league_profile" to "Gaming League Profile",
        "select_rank_badge" to "Select Rank Badge:",
        "system_visual_themes" to "System Visual Themes & Design",
        "active_theme_accent" to "Active Theme Accent:",
        "block_architecture_style" to "Block Architecture Style:",
        "show_ghost_target" to "Show Ghost Target Helper",
        "dynamic_line_sparkles" to "Dynamic Line Sparkles",
        "tactical_gameplay_config" to "Tactical Gameplay Config",
        "control_buttons_layout" to "Control Buttons Layout Style:",
        "next_pieces_preview" to "Next Pieces Preview Count:",
        "extra_smooth_falling" to "Extra Smooth Piece Falling",
        "audio_haptics_online" to "Audio, Haptics & Online Servers",
        "digital_music_theme" to "Digital Music Theme Track:",
        "simulated_arena_region" to "Simulated Arena Server Region:",
        "choose_game_mode" to "CHOOSE SYSTEM GAME MODE",
        "logout" to "LOG OUT",
        "admin" to "ADMIN",
        "grid_transparency" to "Grid Board Background Opacity:",
        "vibration_strength" to "Vibration Intensity Level:",
        "weak" to "Gentle Buzz",
        "medium" to "Standard Core",
        "strong" to "High-Frequency",
        "starting_level_selector" to "Starting Difficulty:",
        "line_clear_challenge" to "Hardcore Line Clear Rule",
        "line_clear_challenge_desc" to "Requires double or triple line clears to gain points",
        "auto_save_highscore" to "Auto-Save Match Highscore",
        "fast_drop_lock_speed" to "Instant Touch Ground Lock-In",
        "fast_drop_lock_speed_desc" to "Instantly locks the piece without slide delay"
    )

    private val ru = mapOf(
        "app_name" to "Тетрис",
        "play" to "Классический режим",
        "upgrades" to "Расширенный режим",
        "multiplayer" to "Сетевой режим",
        "left_handed_controls" to "Зеркальная раскладка для левшей",
        "game_speed_multiplier" to "Коэффициент скорости игры:",
        "control_button_scale" to "Масштаб клавиш управления:",
        "control_button_style" to "Внешний стиль тактильных кнопок:",
        "grid_line_density" to "Тип разметки игрового поля:",
        "leaderboard" to "Таблица лидеров",
        "settings" to "Параметры",
        "score" to "Счет",
        "level" to "Уровень",
        "lines" to "Линии",
        "game_over" to "Игра завершена",
        "restart" to "Перезапустить сессию",
        "back" to "Назад",
        "hold" to "Удержание",
        "next" to "Следующая",
        "chat" to "Чат",
        "save_game" to "Сохранить прогресс",
        "resume" to "Возобновить сессию",
        "language" to "Язык интерфейса",
        "high_score" to "ЛУЧШИЙ РЕЗУЛЬТАТ",
        "fps_mode" to "Режим 144 FPS активен",
        "sound_effects" to "Звуковое сопровождение",
        "vibration" to "Тактильная отдача",
        "controls_style" to "Стиль элементов управления",
        "save_success" to "Прогресс сессии успешно сохранен.",
        "save_fail" to "Активная игровая сессия отсутствует.",
        "welcome" to "Выберите игровой режим:",
        "play_modes" to "Режимы игры",
        "type_msg" to "Введите текст сообщения...",
        "controls_hint" to "Используйте клавиши направления для навигации фигур",
        "change_language" to "Сменить язык",
        "lobby_header" to "Зал ожидания сессии",
        "waiting_opponent" to "Поиск доступного подключения...",
        "player_you" to "Игрок 1 (Вы)",
        "player_opponent" to "Оппонент",
        "send" to "Отправить",
        "gaming_league_profile" to "ПРОФИЛЬ ПОЛЬЗОВАТЕЛЯ",
        "select_rank_badge" to "Выбор квалификационного ранга:",
        "system_visual_themes" to "ВИЗУАЛЬНЫЕ ПАРАМЕТРЫ СИСТЕМЫ",
        "active_theme_accent" to "Цветовая схема интерфейса:",
        "block_architecture_style" to "Визуальный стиль элементов:",
        "show_ghost_target" to "Отображать проекцию фигуры",
        "dynamic_line_sparkles" to "Анимация удаления сегментов поля",
        "tactical_gameplay_config" to "КОНФИГУРАЦИЯ ИГРОВОГО ПРОЦЕССА",
        "control_buttons_layout" to "Расположение органов управления:",
        "next_pieces_preview" to "Очередь отображения следующих фигур:",
        "extra_smooth_falling" to "Сглаженное позиционирование элементов",
        "audio_haptics_online" to "ПАРАМЕТРЫ ПЕРИФЕРИИ И СВЯЗИ",
        "digital_music_theme" to "Музыкальное сопровождение:",
        "simulated_arena_region" to "Регион игрового сервера:",
        "choose_game_mode" to "ВЫБОР ИГРОВОГО РЕЖИМА",
        "logout" to "ВЫХОД ИЗ СИСТЕМЫ",
        "admin" to "СТАТУС: АДМИНИСТРАТОР",
        "grid_transparency" to "Интенсивность фоновой сетки:",
        "vibration_strength" to "Интенсивность тактильной отдачи:",
        "weak" to "Минимальная",
        "medium" to "Средняя",
        "strong" to "Высокая",
        "starting_level_selector" to "Начальная сложность:",
        "line_clear_challenge" to "Задачи очистки линий",
        "line_clear_challenge_desc" to "Параметры начисления очков заблокированы для одиночных линий",
        "auto_save_highscore" to "Автоматическая регистрация результатов в БД",
        "fast_drop_lock_speed" to "Форсированное закрепление элементов",
        "fast_drop_lock_speed_desc" to "Элемент немедленно фиксируется на опорной поверхности"
    )

    private val ua = mapOf(
        "app_name" to "Тетріс",
        "play" to "Класична Гра",
        "upgrades" to "Розширена Гра",
        "multiplayer" to "Мультиплеєр",
        "left_handed_controls" to "Дзеркальне керування для шульг",
        "game_speed_multiplier" to "Множник швидкості падіння фігур:",
        "control_button_scale" to "Розмір кнопок керування:",
        "control_button_style" to "Зовнішній стиль тактильних кнопок:",
        "grid_line_density" to "Візуальний стиль сітки поля:",
        "leaderboard" to "Рекорди",
        "settings" to "Налаштування",
        "score" to "Рахунок",
        "level" to "Рівень",
        "lines" to "Лінії",
        "game_over" to "Гра Закінчена",
        "restart" to "Заново",
        "back" to "Назад",
        "hold" to "Запас",
        "next" to "Наступний",
        "chat" to "Чат",
        "save_game" to "Зберегти",
        "resume" to "Продовжити гру",
        "language" to "Мова",
        "high_score" to "РЕКОРД",
        "fps_mode" to "144 FPS РОЗБЛОКОВАНО",
        "sound_effects" to "Звукові ефекти",
        "vibration" to "Віброведіння",
        "controls_style" to "Тактильне керування",
        "save_success" to "Поточний прогрес успішно збережено!",
        "save_fail" to "Немає активної гри для збереження.",
        "welcome" to "Оберіть режим гри:",
        "play_modes" to "Режими",
        "type_msg" to "Введіть повідомлення...",
        "controls_hint" to "Натискайте кнопки зі стрілками внизу для керування",
        "change_language" to "Змінити Мову",
        "lobby_header" to "Лобі бойової арени",
        "waiting_opponent" to "Очікування другого гравця...",
        "player_you" to "Alex_Swift (Ви)",
        "player_opponent" to "Супротивник",
        "send" to "Надіслати",
        "gaming_league_profile" to "ПРОФІЛЬ ІГРОВОЇ ЛІГИ",
        "select_rank_badge" to "Оберіть ранг профілю:",
        "system_visual_themes" to "ВІЗУАЛЬНІ ТЕМИ ТА ОФОРМЛЕННЯ",
        "active_theme_accent" to "Колірний акцент системи:",
        "block_architecture_style" to "Текстурний стиль блоків:",
        "show_ghost_target" to "Показувати привид-помічник",
        "dynamic_line_sparkles" to "Ефекти іскор при очищенні",
        "tactical_gameplay_config" to "НАЛАШТУВАННЯ ІГРОВОГО ПРОЦЕСУ",
        "control_buttons_layout" to "Розташування кнопок керування:",
        "next_pieces_preview" to "Кількість наступних фігур:",
        "extra_smooth_falling" to "Плавне падіння фігур",
        "audio_haptics_online" to "ЗВУК, ВІБРОВІДДАЧА ТА СЕРВЕРИ",
        "digital_music_theme" to "Фоновий музичний трек:",
        "simulated_arena_region" to "Регіон ігрового сервера:",
        "choose_game_mode" to "ВИБІР РЕЖИМУ ГРИ",
        "logout" to "ВИЙТИ З АКАУНТА",
        "admin" to "АДМІНІСТРАТОР",
        "grid_transparency" to "Прозорість фону ігрового поля:",
        "vibration_strength" to "Сила тактильної вібровіддачі:",
        "weak" to "Легкий",
        "medium" to "Стандартний",
        "strong" to "Максимальний",
        "starting_level_selector" to "Початковий рівень складності:",
        "line_clear_challenge" to "Завдання очищення ліній",
        "line_clear_challenge_desc" to "Очки нараховуються лише за очищення двох або більше ліній",
        "auto_save_highscore" to "Автоматичне збереження рекордів",
        "fast_drop_lock_speed" to "Миттєве закріплення фігур",
        "fast_drop_lock_speed_desc" to "Фігура миттєво фіксується при торканні поверхні"
    )

    private val kk = mapOf(
        "app_name" to "Тетрис",
        "play" to "Классикалық Ойын",
        "upgrades" to "Кеңейтілген Ойын",
        "multiplayer" to "Көп ойыншы",
        "left_handed_controls" to "Солақайларға арналған дзеркало",
        "game_speed_multiplier" to "Ойын жылдамдығын арттыру:",
        "control_button_scale" to "Басқару батырмаларының өлшемі:",
        "control_button_style" to "Тактильді батырмалардың дизайны:",
        "grid_line_density" to "Ойын торының сызықтарының стилі:",
        "leaderboard" to "Рекордтар",
        "settings" to "Баптаулар",
        "score" to "Ұпай",
        "level" to "Деңгей",
        "lines" to "Жолдар",
        "game_over" to "Ойын Аяқталды",
        "restart" to "Қайталау",
        "back" to "Артқа",
        "hold" to "Ұстау",
        "next" to "Келесі",
        "chat" to "Чат",
        "save_game" to "Сақтау",
        "resume" to "Ойынды Жалғастыру",
        "language" to "Тіл",
        "high_score" to "РЕКОРДТАР",
        "fps_mode" to "144 FPS ҚОСЫЛҒАН",
        "sound_effects" to "Дыбыстық әсерлер",
        "vibration" to "Діріл әсері",
        "controls_style" to "Тактильді басқару стилі",
        "save_success" to "Ағымдағы ойын процесі сәтті сақталды!",
        "save_fail" to "Сақталатын белсенді ойын жоқ.",
        "welcome" to "Ойын режимін таңдаңыз:",
        "play_modes" to "Режимдер",
        "type_msg" to "Хабарлама жазыңыз...",
        "controls_hint" to "Блоктарды басқару үшін төмендегі көрсеткілерді басыңыз",
        "change_language" to "Тілді өзгерту",
        "lobby_header" to "Дүбірлі шайқас бөлмесі",
        "waiting_opponent" to "Екінші ойыншыны күту...",
        "player_you" to "Alex_Swift (Сіз)",
        "player_opponent" to "Қарсылас",
        "send" to "Жіберу",
        "gaming_league_profile" to "ОЙЫН ЛИГАСЫНЫҢ ПРОФИЛІ",
        "select_rank_badge" to "Профиль дәрежесін таңдаңыз:",
        "system_visual_themes" to "ВИЗУАЛДЫ ТАҚЫРЫПТАР МЕН ДИЗАЙН",
        "active_theme_accent" to "Жүйенің түс екпіні:",
        "block_architecture_style" to "Блоктардың текстура стилі:",
        "show_ghost_target" to "Көмекші елесті көрсету",
        "dynamic_line_sparkles" to "Жол тазарту ұшқындары",
        "tactical_gameplay_config" to "ОЙЫН БАРЫСЫН РЕТТЕУ",
        "control_buttons_layout" to "Басқару батырмаларының стилі:",
        "next_pieces_preview" to "Келесі фигураларды көрсету:",
        "extra_smooth_falling" to "Фигуралардың бірқалыпты құлауы",
        "audio_haptics_online" to "ДЫБЫС, ДІРІЛ ЖӘНЕ СЕРВЕРЛЕР",
        "digital_music_theme" to "Фондық музыкалық трек:",
        "simulated_arena_region" to "Ойын серверінің аймағы:",
        "choose_game_mode" to "ОЙЫН РЕЖИМІН ТАҢДАУ",
        "logout" to "АККАУНТТАН ШЫҒУ",
        "admin" to "ӘКІМШІ",
        "grid_transparency" to "Ойын алаңы фонының мөлдірлігі:",
        "vibration_strength" to "Діріл әсерінің күші:",
        "weak" to "Жеңіл",
        "medium" to "Қалыпты",
        "strong" to "Максимум",
        "starting_level_selector" to "Бастапқы қиындық деңгейі:",
        "line_clear_challenge" to "Қатарды қиын тазарту ережесі",
        "line_clear_challenge_desc" to "Ұпайлар тек екі немесе одан көп қатарды жойғанда беріледі",
        "auto_save_highscore" to "Рекордтарды автоматты түрде сақтау",
        "fast_drop_lock_speed" to "Фигураны лезде бекіту",
        "fast_drop_lock_speed_desc" to "Фигура тірек бетіне тиген бойда лезде бекітіледі"
    )

    private val de = mapOf(
        "app_name" to "Tetris",
        "play" to "Klassisches Spiel",
        "upgrades" to "Erweitertes Spiel",
        "multiplayer" to "Mehrspieler",
        "left_handed_controls" to "Linkshänder-Layout spiegeln",
        "game_speed_multiplier" to "Takt-Geschwindigkeitsfaktor:",
        "control_button_scale" to "Größe Steuerungs-Knöpfe:",
        "control_button_style" to "Stil taktiler Knöpfe:",
        "grid_line_density" to "Gitterlinien-Architektur:",
        "leaderboard" to "Bestenliste",
        "settings" to "Einstellungen",
        "score" to "Punkte",
        "level" to "Level",
        "lines" to "Linien",
        "game_over" to "Spiel vorbei",
        "restart" to "Neustart",
        "back" to "Zurück",
        "hold" to "Speicher",
        "next" to "Nächster",
        "chat" to "Chat",
        "save_game" to "Spiel speichern",
        "resume" to "Spiel fortsetzen",
        "language" to "Sprache",
        "high_score" to "REKORD",
        "fps_mode" to "144 FPS FREIGESCHALTET",
        "sound_effects" to "Soundeffekte",
        "vibration" to "Haptisches Feedback",
        "controls_style" to "Taktile Steuerung",
        "save_success" to "Spielstand erfolgreich gespeichert!",
        "save_fail" to "Kein aktives Spiel zum Speichern.",
        "welcome" to "Spielmodus wählen:",
        "play_modes" to "Modi",
        "type_msg" to "Nachricht schreiben...",
        "controls_hint" to "Pfeiltasten unten drücken, um Blöcke zu steuern",
        "change_language" to "Sprache ändern",
        "lobby_header" to "Kampfarena Lobby",
        "waiting_opponent" to "Warten auf zweiten Spieler...",
        "player_you" to "Alex_Swift (Du)",
        "player_opponent" to "Gegner",
        "send" to "Senden",
        "gaming_league_profile" to "Spielerliga-Profil",
        "select_rank_badge" to "Rang-Abzeichen wählen:",
        "system_visual_themes" to "Visuelle Themen & Design",
        "active_theme_accent" to "Aktiver Farbakzent:",
        "block_architecture_style" to "Block-Stil:",
        "show_ghost_target" to "Geist-Hilfslinie anzeigen",
        "dynamic_line_sparkles" to "Dynamische Linien-Funken",
        "tactical_gameplay_config" to "Taktische Spiel-Konfiguration",
        "control_buttons_layout" to "Tasten-Layout:",
        "next_pieces_preview" to "Vorschau nächste Blöcke:",
        "extra_smooth_falling" to "Extra weiches Fallen",
        "audio_haptics_online" to "Audio, Haptik & Online-Server",
        "digital_music_theme" to "Musik-Track:",
        "simulated_arena_region" to "Spiel-Serverregion:",
        "choose_game_mode" to "SPIELMODUS WÄHLEN",
        "logout" to "ABMELDEN",
        "admin" to "ADMIN",
        "grid_transparency" to "Feld-Hintergrund Deckkraft:",
        "vibration_strength" to "Vibrationsintensität:",
        "weak" to "Sanft",
        "medium" to "Standard",
        "strong" to "Intensiv",
        "starting_level_selector" to "Anfangs-Schwierigkeitsgrad:",
        "line_clear_challenge" to "Hardcore Linien-Schnitt",
        "line_clear_challenge_desc" to "Punkte werden nur bei zwei- oder dreifachen Linienlöschungen vergeben",
        "auto_save_highscore" to "Punkte automatisch speichern",
        "fast_drop_lock_speed" to "Sofortige Bodenfixierung",
        "fast_drop_lock_speed_desc" to "Sperrt den Block sofort bei Berührung des Bodens"
    )

    fun get(key: String, lang: Language): String {
        val map = when (lang) {
            Language.EN -> en
            Language.RU -> ru
            Language.UA -> ua
            Language.KK -> kk
            Language.DE -> de
        }
        return map[key] ?: en[key] ?: key
    }

    fun getLocalizedRank(rankId: String, lang: Language): String {
        return when (rankId.uppercase()) {
            "BRONZE" -> when (lang) {
                Language.RU -> "БРОНЗА"
                Language.UA -> "БРОНЗА"
                Language.DE -> "BRONZE"
                Language.KK -> "ҚОЛА"
                else -> "BRONZE"
            }
            "SILVER" -> when (lang) {
                Language.RU -> "СЕРЕБРО"
                Language.UA -> "СРІБЛО"
                Language.DE -> "SILBER"
                Language.KK -> "КҮМІС"
                else -> "SILVER"
            }
            "GOLD" -> when (lang) {
                Language.RU -> "ЗОЛОТО"
                Language.UA -> "ЗОЛОТО"
                Language.DE -> "GOLD"
                Language.KK -> "АЛТЫН"
                else -> "GOLD"
            }
            "PLATINUM" -> when (lang) {
                Language.RU -> "ПЛАТИНА"
                Language.UA -> "ПЛАТИНА"
                Language.DE -> "PLATIN"
                Language.KK -> "ПЛАТИНА"
                else -> "PLATINUM"
            }
            "DIAMOND" -> when (lang) {
                Language.RU -> "АЛМАЗ"
                Language.UA -> "АЛМАЗ"
                Language.DE -> "DIAMANT"
                Language.KK -> "АЛМАЗ"
                else -> "DIAMOND"
            }
            "MASTER" -> when (lang) {
                Language.RU -> "МАСТЕР"
                Language.UA -> "МАЙСТЕР"
                Language.DE -> "MEISTER"
                Language.KK -> "ШЕБЕР"
                else -> "MASTER"
            }
            "GRANDMASTER" -> when (lang) {
                Language.RU -> "ГРАНДМАСТЕР"
                Language.UA -> "ГРАНДМАЙСТЕР"
                Language.DE -> "GROSSMEISTER"
                Language.KK -> "ГРОССМЕЙСТЕР"
                else -> "GRANDMASTER"
            }
            "CHALLENGER" -> when (lang) {
                Language.RU -> "ЛЕГЕНДА"
                Language.UA -> "ЛЕГЕНДА"
                Language.DE -> "HERAUSFORDERER"
                Language.KK -> "АҢЫЗ"
                else -> "CHALLENGER"
            }
            else -> rankId
        }
    }
}
