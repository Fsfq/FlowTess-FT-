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
    DE("DE", "Deutsch"),
    ZH("ZH", "中文")
}

object Translations {
    private val en = mapOf(
        "app_name" to "FlowTess",
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
        "pieces" to "Pieces",
        "tap" to "TAP",
        "puzzle_stencil" to "BLUEPRINT",
        "moves" to "MOVES",
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
        "choose_game_mode" to "GAME MODES",
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
        "fast_drop_lock_speed_desc" to "Instantly locks the piece without slide delay",
        "profile" to "Profile",
        "store" to "Store",
        "achievements" to "Achievements",
        "cases" to "Crates",
        "inventory" to "Inventory",
        "fast_open" to "Fast Open (Instant)",
        "recycle_commons" to "Recycle Commons",
        "inventory_empty" to "Inventory is empty",
        "open_crates_hint" to "Open crates to obtain weapon-grade cosmetics",
        "crate_contents" to "CRATE CONTENTS",
        "open_for" to "OPEN FOR",
        "chances" to "Chances",
        "open" to "OPEN",
        "active" to "ACTIVE",
        "equip" to "EQUIP",
        "sell" to "SELL",
        "locked" to "Locked",
        "unlocked" to "Unlocked",
        "mass_recycle" to "Mass Item Recycling",
        "mass_recycle_confirm" to "Confirm Recycle",
        "cancel" to "Cancel",
        "zeta_arena" to "ZETA ARENA",
        "current_score" to "SCORE",
        "drag_drop_hint" to "Drag & drop figures to the grid",
        "retry" to "RETRY",
        "menu" to "MENU",
        "online" to "ONLINE",
        "offline" to "OFFLINE",
        "wins" to "Wins",
        "losses" to "Losses",
        "winrate" to "Winrate",
        "total_games" to "Total Matches",
        "pause" to "Pause",
        "game_paused" to "GAME PAUSED",
        "time" to "TIME",
        "final_score" to "FINAL SCORE",
        "new_record" to "NEW RECORD!",
        "incredible_performance" to "Incredible performance!",
        "tetrises" to "Tetrises",
        "play_again" to "PLAY AGAIN",
        "main_menu" to "MAIN MENU",
        "exit_to_menu" to "EXIT TO MENU",
        "done" to "Done",
        "ghost_piece" to "Ghost piece",
        "ghost_outline_only" to "Ghost outline only",
        "smooth_falling" to "Smooth falling",
        "line_challenge" to "Line challenge",
        "duel_invitation" to "Duel Invitation!",
        "accept_duel" to "Accept Duel",
        "decline" to "Decline",
        "quick_match_1v1" to "QUICK MATCH 1V1",
        "quick_match_desc" to "Instant matchmaking or auto-room host",
        "all_rooms" to "All Rooms",
        "open_rooms" to "Open",
        "locked_rooms" to "Locked",
        "no_rooms_found" to "No Battle Rooms Found",
        "no_rooms_desc" to "Create a new battle arena and invite your friend using the room code!",
        "create_room" to "Create Room",
        "room_name" to "ROOM NAME",
        "enter_room_name" to "Enter arena title...",
        "match_mode" to "MATCH GAME MODE",
        "battle_mode_title" to "Battle (Garbage Duel)",
        "battle_mode_desc" to "Knockout duel with garbage line attacks",
        "score_race_title" to "Pure Classic (Score Race)",
        "score_race_desc" to "Pure classic mode with no attacks, highest score wins",
        "room_code" to "ROOM CODE",
        "copy_code" to "Copy Code",
        "ready" to "READY",
        "not_ready" to "NOT READY",
        "start_match" to "START MATCH",
        "leave_room" to "LEAVE ROOM",
        "victory" to "VICTORY!",
        "defeat" to "DEFEAT",
        "surrender" to "Surrender",
        "rematch" to "REMATCH",
        "lines_sent" to "Lines Sent",
        "lines_received" to "Lines Received",
        "apm" to "APM",
        "insufficient_funds" to "Insufficient funds",
        "item_purchased" to "Item purchased and equipped.",
        "item_equipped" to "Item equipped successfully.",
        "already_unlocked" to "Already unlocked.",
        "unlock_prev_first" to "Unlock previous tier first.",
        "rank_promoted" to "Rank promoted successfully!",
        "cloud_sync_title" to "Cloud Sync",
        "sync_now" to "Sync Now",
        "edit_profile" to "Edit Profile",
        "change_nickname" to "Change Nickname",
        "enter_nickname" to "Enter new nickname...",
        "save" to "Save",
        "friends" to "Friends",
        "add_friend" to "Add Friend",
        "enter_friend_id" to "Enter friend UID...",
        "add" to "Add",
        "no_friends" to "No friends added yet"
    )

    private val ru = mapOf(
        "app_name" to "FlowTess",
        "play" to "Классический режим",
        "upgrades" to "Расширенный режим",
        "multiplayer" to "Мультиплеер",
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
        "pieces" to "Фигуры",
        "tap" to "НАЖМИ",
        "puzzle_stencil" to "ШАБЛОН",
        "moves" to "ХОДОВ",
        "game_over" to "Игра завершена",
        "restart" to "Перезапустить сессию",
        "back" to "Назад",
        "hold" to "ХОЛД",
        "next" to "СЛЕД.",
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
        "choose_game_mode" to "РЕЖИМЫ",
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
        "fast_drop_lock_speed_desc" to "Элемент немедленно фиксируется на опорной поверхности",
        "profile" to "Профиль",
        "store" to "Магазин",
        "achievements" to "Достижения",
        "cases" to "Кейсы",
        "inventory" to "Инвентарь",
        "fast_open" to "Быстрое открытие (без анимации)",
        "recycle_commons" to "Утилизировать ширпотреб",
        "inventory_empty" to "Инвентарь пуст",
        "open_crates_hint" to "Открывайте ящики для получения редких скинов",
        "crate_contents" to "ВОЗМОЖНЫЕ НАГРАДЫ",
        "open_for" to "ОТКРЫТЬ ЗА",
        "chances" to "Шансы",
        "open" to "ОТКРЫТЬ",
        "active" to "НАДЕТО",
        "equip" to "НАДЕТЬ",
        "sell" to "ПРОДАТЬ",
        "locked" to "Заблокировано",
        "unlocked" to "Разблокировано",
        "mass_recycle" to "Массовая утилизация",
        "mass_recycle_confirm" to "Утилизировать",
        "cancel" to "Отмена",
        "zeta_arena" to "ZETA АРЕНА",
        "current_score" to "ТЕКУЩИЙ СЧЕТ",
        "drag_drop_hint" to "Перетаскивайте фигуры на поле",
        "retry" to "ЗАНОВО",
        "menu" to "В МЕНЮ",
        "online" to "ОНЛАЙН",
        "offline" to "ОФФЛАЙН",
        "wins" to "Победы",
        "losses" to "Поражения",
        "winrate" to "Винрейт",
        "total_games" to "Всего матчей",
        "pause" to "Пауза",
        "game_paused" to "ИГРА НА ПАУЗЕ",
        "time" to "ВРЕМЯ",
        "final_score" to "ИТОГОВЫЙ СЧЕТ",
        "new_record" to "НОВЫЙ РЕКОРД!",
        "incredible_performance" to "Потрясающий результат!",
        "tetrises" to "Тетрисы",
        "play_again" to "ИГРАТЬ СНОВА",
        "main_menu" to "В ГЛАВНОЕ МЕНЮ",
        "exit_to_menu" to "ВЫЙТИ В МЕНЮ",
        "done" to "Готово",
        "ghost_piece" to "Призрачная фигура",
        "ghost_outline_only" to "Только контур призрака",
        "smooth_falling" to "Плавное падение",
        "line_challenge" to "Челлендж линий",
        "duel_invitation" to "Приглашение на дуэль!",
        "accept_duel" to "Принять бой",
        "decline" to "Отклонить",
        "quick_match_1v1" to "БЫСТРЫЙ БОЙ 1V1",
        "quick_match_desc" to "Мгновенный поиск или создание дуэли",
        "all_rooms" to "Все комнаты",
        "open_rooms" to "Открытые",
        "locked_rooms" to "С паролем",
        "no_rooms_found" to "Нет доступных комнат",
        "no_rooms_desc" to "Создайте новую комнату и пригласите друга по коду!",
        "create_room" to "Создать комнату",
        "room_name" to "НАЗВАНИЕ КОМНАТЫ",
        "enter_room_name" to "Введите имя комнаты...",
        "match_mode" to "РЕЖИМ СОРЕВНОВАНИЯ",
        "battle_mode_title" to "Битва (с атаками)",
        "battle_mode_desc" to "Дуэль на выбывание с атаками мусорными линиями",
        "score_race_title" to "Классика (на очки, без атак)",
        "score_race_desc" to "Классический тетрис без атак, победа по очкам/выживанию",
        "room_code" to "КОД КОМНАТЫ",
        "copy_code" to "Копировать код",
        "ready" to "ГОТОВ",
        "not_ready" to "НЕ ГОТОВ",
        "start_match" to "НАЧАТЬ МАТЧ",
        "leave_room" to "ПОКИНУТЬ КОМНАТУ",
        "victory" to "ПОБЕДА!",
        "defeat" to "ПОРАЖЕНИЕ",
        "surrender" to "Сдаться",
        "rematch" to "РЕВАНШ",
        "lines_sent" to "Линий отправлено",
        "lines_received" to "Линий получено",
        "apm" to "Атак в минуту (APM)",
        "insufficient_funds" to "Недостаточно средств",
        "item_purchased" to "Предмет успешно куплен и применен.",
        "item_equipped" to "Предмет успешно применен.",
        "already_unlocked" to "Уже разблокировано.",
        "unlock_prev_first" to "Сначала откройте предыдущий уровень.",
        "rank_promoted" to "Ранг успешно повышен!",
        "cloud_sync_title" to "Облачная синхронизация",
        "sync_now" to "Синхронизировать сейчас",
        "edit_profile" to "Редактировать профиль",
        "change_nickname" to "Сменить никнейм",
        "enter_nickname" to "Введите новый никнейм...",
        "save" to "Сохранить",
        "friends" to "Друзья",
        "add_friend" to "Добавить друга",
        "enter_friend_id" to "Введите UID друга...",
        "add" to "Добавить",
        "no_friends" to "Список друзей пуст"
    )

    private val ua = mapOf(
        "app_name" to "FlowTess",
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
        "pieces" to "Фігури",
        "tap" to "НАТИСНИ",
        "puzzle_stencil" to "ШАБЛОН",
        "moves" to "ХОДІВ",
        "game_over" to "Гра Закінчена",
        "restart" to "Заново",
        "back" to "Назад",
        "hold" to "ХОЛД",
        "next" to "НАСТ.",
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
        "choose_game_mode" to "РЕЖИМИ",
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
        "fast_drop_lock_speed_desc" to "Фігура миттєво фіксується при торканні поверхні",
        "profile" to "Профіль",
        "store" to "Магазин",
        "achievements" to "Досягнення",
        "cases" to "Кейси",
        "inventory" to "Інвентар",
        "fast_open" to "Швидке відкриття (без анімації)",
        "recycle_commons" to "Утилізувати непотріб",
        "inventory_empty" to "Інвентар порожній",
        "open_crates_hint" to "Відкривайте ящики для отримання рідкісних скінів",
        "crate_contents" to "МОЖЛИВІ НАГОРОДИ",
        "open_for" to "ВІДКРИТИ ЗА",
        "chances" to "Шанси",
        "open" to "ВІДКРИТИ",
        "active" to "АКТИВНО",
        "equip" to "ОДЯГНУТИ",
        "sell" to "ПРОДАТИ",
        "locked" to "Заблоковано",
        "unlocked" to "Розблоковано",
        "mass_recycle" to "Масова утилізація",
        "mass_recycle_confirm" to "Утилізувати",
        "cancel" to "Скасувати",
        "zeta_arena" to "ZETA АРЕНА",
        "current_score" to "ПОТОЧНИЙ РАХУНОК",
        "drag_drop_hint" to "Перетягуйте фігури на поле",
        "retry" to "ЗАНОВО",
        "menu" to "В МЕНЮ",
        "online" to "ОНЛАЙН",
        "offline" to "ОФЛАЙН",
        "wins" to "Перемоги",
        "losses" to "Поразки",
        "winrate" to "Вінрейт",
        "total_games" to "Всього матчів",
        "pause" to "Пауза",
        "game_paused" to "ГРА НА ПАУЗІ",
        "time" to "ЧАС",
        "final_score" to "ПІДСУМКОВИЙ РАХУНОК",
        "new_record" to "НОВИЙ РЕКОРД!",
        "incredible_performance" to "Неймовірний результат!",
        "tetrises" to "Тетріси",
        "play_again" to "ГРАТИ ЗНОВУ",
        "main_menu" to "ГОЛОВНЕ МЕНЮ",
        "exit_to_menu" to "ВИЙТИ В МЕНЮ",
        "done" to "Готово",
        "ghost_piece" to "Привидна фігура",
        "ghost_outline_only" to "Лише контур привида",
        "smooth_falling" to "Плавне падіння",
        "line_challenge" to "Випробування ліній",
        "duel_invitation" to "Запрошення на дуель!",
        "accept_duel" to "Прийняти бій",
        "decline" to "Відхилити",
        "quick_match_1v1" to "ШВИДКИЙ БІЙ 1V1",
        "quick_match_desc" to "Миттєвий пошук або створення дуелі",
        "all_rooms" to "Всі кімнати",
        "open_rooms" to "Відкриті",
        "locked_rooms" to "З паролем",
        "no_rooms_found" to "Немає доступних кімнат",
        "no_rooms_desc" to "Створіть нову кімнату та запросіть друга за кодом!",
        "create_room" to "Створити кімнату",
        "room_name" to "НАЗВА КІМНАТИ",
        "enter_room_name" to "Введіть назву кімнати...",
        "match_mode" to "РЕЖИМ ЗМАГАННЯ",
        "battle_mode_title" to "Битва (з атаками)",
        "battle_mode_desc" to "Дуель на вибування з атаками сміттєвими лініями",
        "score_race_title" to "Класика (на очки, без атак)",
        "score_race_desc" to "Класичний тетріс без атак, перемога за очками/виживанням",
        "room_code" to "КОД КІМНАТИ",
        "copy_code" to "Копіювати код",
        "ready" to "ГОТОВИЙ",
        "not_ready" to "НЕ ГОТОВИЙ",
        "start_match" to "ПОЧАТИ МАТЧ",
        "leave_room" to "ЗАЛИШИТИ КІМНАТУ",
        "victory" to "ПЕРЕМОГА!",
        "defeat" to "ПОРАЗКА",
        "surrender" to "Здатися",
        "rematch" to "РЕВАНШ",
        "lines_sent" to "Ліній відправлено",
        "lines_received" to "Ліній отримано",
        "apm" to "Атак за хвилину (APM)",
        "insufficient_funds" to "Недостатньо коштів",
        "item_purchased" to "Предмет успішно придбано та застосовано.",
        "item_equipped" to "Предмет успішно застосовано.",
        "already_unlocked" to "Вже розблоковано.",
        "unlock_prev_first" to "Спочатку відкрийте попередній рівень.",
        "rank_promoted" to "Ранг успішно підвищено!",
        "cloud_sync_title" to "Хмарна синхронізація",
        "sync_now" to "Синхронізувати зараз",
        "edit_profile" to "Редагувати профіль",
        "change_nickname" to "Змінити нікнейм",
        "enter_nickname" to "Введіть новий нікнейм...",
        "save" to "Зберегти",
        "friends" to "Друзі",
        "add_friend" to "Додати друга",
        "enter_friend_id" to "Введіть UID друга...",
        "add" to "Додати",
        "no_friends" to "Список друзів порожній"
    )

    private val kk = mapOf(
        "app_name" to "FlowTess",
        "play" to "Классикалық Ойын",
        "upgrades" to "Кеңейтілген Ойын",
        "multiplayer" to "Көп ойыншы",
        "left_handed_controls" to "Солақайларға арналған айна",
        "game_speed_multiplier" to "Ойын жылдамдығын арттыру:",
        "control_button_scale" to "Басқару батырмаларының өлшемі:",
        "control_button_style" to "Тактильді батырмалардың дизайны:",
        "grid_line_density" to "Ойын торының сызықтарының стилі:",
        "leaderboard" to "Рекордтар",
        "settings" to "Баптаулар",
        "score" to "Ұпай",
        "level" to "Деңгей",
        "lines" to "Жолдар",
        "pieces" to "Фигуралар",
        "tap" to "БАС",
        "puzzle_stencil" to "ҮЛГІ",
        "moves" to "ЖҮРІС",
        "game_over" to "Ойын Аяқталды",
        "restart" to "Қайталау",
        "back" to "Артқа",
        "hold" to "ХОЛД",
        "next" to "КЕЛЕСІ",
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
        "choose_game_mode" to "РЕЖИМДЕР",
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
        "fast_drop_lock_speed_desc" to "Фигура тірек бетіне тиген бойда лезде бекітіледі",
        "profile" to "Профиль",
        "store" to "Дүкен",
        "achievements" to "Жетістіктер",
        "cases" to "Кейстер",
        "inventory" to "Инвентарь",
        "fast_open" to "Жылдам ашу (анимациясыз)",
        "recycle_commons" to "Қоқысты өткізу",
        "inventory_empty" to "Инвентарь бос",
        "open_crates_hint" to "Сирек скин алу үшін кейстерді ашыңыз",
        "crate_contents" to "ЫҚТИМАЛ СЫЙЛЫҚТАР",
        "open_for" to "МЫНАҒАН АШУ",
        "chances" to "Шанстар",
        "open" to "АШУ",
        "active" to "КИІЛГЕН",
        "equip" to "КИЮ",
        "sell" to "САТУ",
        "locked" to "Құлыпталған",
        "unlocked" to "Ашылған",
        "mass_recycle" to "Жаппай утилизация",
        "mass_recycle_confirm" to "Утилизациялау",
        "cancel" to "Бас тарту",
        "zeta_arena" to "ZETA АРЕНА",
        "current_score" to "АҒЫМДАҒЫ ҰПАЙ",
        "drag_drop_hint" to "Фигураларды алаңға сүйреңіз",
        "retry" to "ҚАЙТАЛАУ",
        "menu" to "МӘЗІРГЕ",
        "online" to "ОНЛАЙН",
        "offline" to "ОФЛАЙН",
        "wins" to "Жеңістер",
        "losses" to "Жеңілістер",
        "winrate" to "Винрейт",
        "total_games" to "Барлық ойындар",
        "pause" to "Кідірту",
        "game_paused" to "ОЙЫН КІДІРТІЛДІ",
        "time" to "УАҚЫТ",
        "final_score" to "ҚОРЫТЫНДЫ ҰПАЙ",
        "new_record" to "ЖАҢА РЕКОРД!",
        "incredible_performance" to "Ғаламат нәтиже!",
        "tetrises" to "Тетристер",
        "play_again" to "ҚАЙТА ОЙНАУ",
        "main_menu" to "БАСТЫ МӘЗІРГЕ",
        "exit_to_menu" to "МӘЗІРГЕ ШЫҒУ",
        "done" to "Дайын",
        "ghost_piece" to "Елес фигура",
        "ghost_outline_only" to "Тек елес контуры",
        "smooth_falling" to "Бірқалыпты құлау",
        "line_challenge" to "Қатар сынағы",
        "duel_invitation" to "Дуэльге шақыру!",
        "accept_duel" to "Шайқасты қабылдау",
        "decline" to "Бас тарту",
        "quick_match_1v1" to "ЖЫЛДАМ БОЙ 1V1",
        "quick_match_desc" to "Лезде қарсылас табу немесе бөлме ашу",
        "all_rooms" to "Барлық бөлмелер",
        "open_rooms" to "Ашық",
        "locked_rooms" to "Құпия сөзді",
        "no_rooms_found" to "Қолжетімді бөлмелер жоқ",
        "no_rooms_desc" to "Жаңа бөлме жасап, досыңызды код арқылы шақырыңыз!",
        "create_room" to "Бөлме құру",
        "room_name" to "БӨЛМЕ АТАУЫ",
        "enter_room_name" to "Бөлме атауын жазыңыз...",
        "match_mode" to "ЖАРЫС РЕЖИМІ",
        "battle_mode_title" to "Шайқас (шабуылмен)",
        "battle_mode_desc" to "Қоқыс сызықтарымен шабуылдайтын дуэль",
        "score_race_title" to "Классика (ұпайға, шабуылсыз)",
        "score_race_desc" to "Шабуылсыз таза тетрис, ұпай бойынша жеңіс",
        "room_code" to "БӨЛМЕ КОДЫ",
        "copy_code" to "Кодты көшіру",
        "ready" to "ДАЙЫН",
        "not_ready" to "ДАЙЫН ЕМЕС",
        "start_match" to "МАТЧТЫ БАСТАУ",
        "leave_room" to "БӨЛМЕДЕН ШЫҒУ",
        "victory" to "ЖЕҢІС!",
        "defeat" to "ЖЕҢІЛІС",
        "surrender" to "Берілу",
        "rematch" to "РЕВАНШ",
        "lines_sent" to "Жіберілген жолдар",
        "lines_received" to "Қабылданған жолдар",
        "apm" to "Минутына шабуыл (APM)",
        "insufficient_funds" to "Қаражат жеткіліксіз",
        "item_purchased" to "Зат сәтті сатып алынды және қолданылды.",
        "item_equipped" to "Зат сәтті қолданылды.",
        "already_unlocked" to "Әлдеқашан ашылған.",
        "unlock_prev_first" to "Алдымен алдыңғы деңгейді ашыңыз.",
        "rank_promoted" to "Дәреже сәтті көтерілді!",
        "cloud_sync_title" to "Бұлттық синхрондау",
        "sync_now" to "Қазір синхрондау",
        "edit_profile" to "Профильді өңдеу",
        "change_nickname" to "Никнеймді өзгерту",
        "enter_nickname" to "Жаңа никнейм енгізіңіз...",
        "save" to "Сақтау",
        "friends" to "Достар",
        "add_friend" to "Дос қосу",
        "enter_friend_id" to "Досыңыздың UID енгізіңіз...",
        "add" to "Қосу",
        "no_friends" to "Достар тізімі бос"
    )

    private val de = mapOf(
        "app_name" to "FlowTess",
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
        "pieces" to "Steine",
        "tap" to "TIPPEN",
        "puzzle_stencil" to "SCHABLONE",
        "moves" to "ZÜGE",
        "game_over" to "Spiel vorbei",
        "restart" to "Neustart",
        "back" to "Zurück",
        "hold" to "HOLD",
        "next" to "NÄCHST.",
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
        "choose_game_mode" to "SPIELMODI",
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
        "fast_drop_lock_speed_desc" to "Sperrt den Block sofort bei Berührung des Bodens",
        "profile" to "Profil",
        "store" to "Shop",
        "achievements" to "Erfolge",
        "cases" to "Kisten",
        "inventory" to "Inventar",
        "fast_open" to "Schnellöffnung (ohne Animation)",
        "recycle_commons" to "Standard-Items verwerten",
        "inventory_empty" to "Inventar ist leer",
        "open_crates_hint" to "Öffne Kisten für seltene Skins",
        "crate_contents" to "MÖGLICHE BELOHNUNGEN",
        "open_for" to "ÖFFNEN FÜR",
        "chances" to "Chancen",
        "open" to "ÖFFNEN",
        "active" to "AKTIV",
        "equip" to "AUSRÜSTEN",
        "sell" to "VERKAUFEN",
        "locked" to "Gesperrt",
        "unlocked" to "Entsperrt",
        "mass_recycle" to "Massenverwertung",
        "mass_recycle_confirm" to "Verwerten",
        "cancel" to "Abbrechen",
        "zeta_arena" to "ZETA ARENA",
        "current_score" to "PUNKTESTAND",
        "drag_drop_hint" to "Blöcke auf das Spielfeld ziehen",
        "retry" to "NEUSTART",
        "menu" to "ZUM MENÜ",
        "online" to "ONLINE",
        "offline" to "OFFLINE",
        "wins" to "Siege",
        "losses" to "Niederlagen",
        "winrate" to "Siegquote",
        "total_games" to "Gesamte Spiele",
        "pause" to "Pause",
        "game_paused" to "SPIEL PAUSIERT",
        "time" to "ZEIT",
        "final_score" to "ENDERGEBNIS",
        "new_record" to "NEUER REKORD!",
        "incredible_performance" to "Hervorragende Leistung!",
        "tetrises" to "Tetrises",
        "play_again" to "NOCHMAL SPIELEN",
        "main_menu" to "HAUPTMENÜ",
        "exit_to_menu" to "ZUM MENÜ",
        "done" to "Fertig",
        "ghost_piece" to "Geister-Block",
        "ghost_outline_only" to "Nur Geister-Umriss",
        "smooth_falling" to "Weiches Fallen",
        "line_challenge" to "Linien-Herausforderung",
        "duel_invitation" to "Duell-Einladung!",
        "accept_duel" to "Duell annehmen",
        "decline" to "Ablehnen",
        "quick_match_1v1" to "SCHNELLES MATCH 1V1",
        "quick_match_desc" to "Sofortige Gegnersuche oder Raum erstellen",
        "all_rooms" to "Alle Räume",
        "open_rooms" to "Offen",
        "locked_rooms" to "Gesperrt",
        "no_rooms_found" to "Keine Räume gefunden",
        "no_rooms_desc" to "Erstelle einen neuen Raum und lade deinen Freund mit dem Code ein!",
        "create_room" to "Raum erstellen",
        "room_name" to "RAUMNAME",
        "enter_room_name" to "Raumtitel eingeben...",
        "match_mode" to "SPIELMODUS",
        "battle_mode_title" to "Schlacht (mit Angriffen)",
        "battle_mode_desc" to "K.-o.-Duell mit Mülllinien-Angriffen",
        "score_race_title" to "Klassisch (auf Punkte, ohne Angriffe)",
        "score_race_desc" to "Klassisches Tetris ohne Angriffe, Punktsieg",
        "room_code" to "RAUMCODE",
        "copy_code" to "Code kopieren",
        "ready" to "BEREIT",
        "not_ready" to "NICHT BEREIT",
        "start_match" to "SPIEL STARTEN",
        "leave_room" to "RAUM VERLASSEN",
        "victory" to "SIEG!",
        "defeat" to "NIEDERLAGE",
        "surrender" to "Aufgeben",
        "rematch" to "REVANCHE",
        "lines_sent" to "Linien gesendet",
        "lines_received" to "Linien empfangen",
        "apm" to "Angriffe pro Minute (APM)",
        "insufficient_funds" to "Nicht genügend Münzen",
        "item_purchased" to "Item erfolgreich gekauft und ausgerüstet.",
        "item_equipped" to "Item erfolgreich ausgerüstet.",
        "already_unlocked" to "Bereits freigeschaltet.",
        "unlock_prev_first" to "Schalte zuerst die vorherige Stufe frei.",
        "rank_promoted" to "Rang erfolgreich aufgestiegen!",
        "cloud_sync_title" to "Cloud-Synchronisierung",
        "sync_now" to "Jetzt synchronisieren",
        "edit_profile" to "Profil bearbeiten",
        "change_nickname" to "Spitznamen ändern",
        "enter_nickname" to "Neuen Namen eingeben...",
        "save" to "Speichern",
        "friends" to "Freunde",
        "add_friend" to "Freund hinzufügen",
        "enter_friend_id" to "Freundes-UID eingeben...",
        "add" to "Hinzufügen",
        "no_friends" to "Keine Freunde hinzugefügt"
    )

    private val zh = mapOf(
        "app_name" to "FlowTess",
        "play" to "经典模式",
        "upgrades" to "扩展模式",
        "multiplayer" to "多人对战",
        "left_handed_controls" to "左手镜像操控布局",
        "game_speed_multiplier" to "游戏下落速度倍率:",
        "control_button_scale" to "控制按钮缩放尺寸:",
        "control_button_style" to "触控按钮外观样式:",
        "grid_line_density" to "网格面板线条风格:",
        "leaderboard" to "排行榜",
        "settings" to "系统设置",
        "score" to "得分",
        "level" to "等级",
        "lines" to "消除行",
        "pieces" to "方块数",
        "tap" to "点击",
        "puzzle_stencil" to "全息模板",
        "moves" to "步数",
        "game_over" to "游戏结束",
        "restart" to "重新开始",
        "back" to "返回",
        "hold" to "暂存",
        "next" to "下一个",
        "chat" to "聊天",
        "save_game" to "保存进度",
        "resume" to "继续游戏",
        "language" to "界面语言",
        "high_score" to "最高纪录",
        "fps_mode" to "已解锁 144 FPS",
        "sound_effects" to "音效设置",
        "vibration" to "触觉震动反馈",
        "controls_style" to "触控控件样式",
        "save_success" to "当前游戏进度已成功保存！",
        "save_fail" to "没有可保存的活跃游戏会话。",
        "welcome" to "请选择游戏模式:",
        "play_modes" to "游戏模式",
        "type_msg" to "输入消息内容...",
        "controls_hint" to "点击下方方向键操控方块移动",
        "change_language" to "切换语言",
        "lobby_header" to "竞技对战大厅",
        "waiting_opponent" to "正在寻找对手...",
        "player_you" to "Alex_Swift (您)",
        "player_opponent" to "对战方",
        "send" to "发送",
        "gaming_league_profile" to "玩家联盟档案",
        "select_rank_badge" to "选择段位徽章:",
        "system_visual_themes" to "系统视觉主题与设计",
        "active_theme_accent" to "活动主题色彩:",
        "block_architecture_style" to "方块纹理架构风格:",
        "show_ghost_target" to "显示下落投影辅助线",
        "dynamic_line_sparkles" to "消除行动态火花效果",
        "tactical_gameplay_config" to "战术玩法配置",
        "control_buttons_layout" to "按键操控布局:",
        "next_pieces_preview" to "预览方块队列数量:",
        "extra_smooth_falling" to "极致平滑方块下落",
        "audio_haptics_online" to "音频、震动与在线服务器",
        "digital_music_theme" to "背景电子音乐:",
        "simulated_arena_region" to "竞技服务器区域:",
        "choose_game_mode" to "游戏模式",
        "logout" to "退出登录",
        "admin" to "管理员权限",
        "grid_transparency" to "游戏底盘背景透明度:",
        "vibration_strength" to "触觉震动反馈强度:",
        "weak" to "轻微",
        "medium" to "标准",
        "strong" to "强力",
        "starting_level_selector" to "初始挑战难度:",
        "line_clear_challenge" to "硬核行消除规则",
        "line_clear_challenge_desc" to "仅在双行或三行消除时才计分",
        "auto_save_highscore" to "自动同步最高纪录至数据库",
        "fast_drop_lock_speed" to "触底瞬间锁定方块",
        "fast_drop_lock_speed_desc" to "方块触底时立即固定无滑动延迟",
        "profile" to "个人档案",
        "store" to "游戏商城",
        "achievements" to "荣誉成就",
        "cases" to "战术军械箱",
        "inventory" to "物品仓库",
        "fast_open" to "极速开启 (跳过动画)",
        "recycle_commons" to "一键回收普通物品",
        "inventory_empty" to "物品仓库为空",
        "open_crates_hint" to "开启军械箱以获得稀有皮肤与特效",
        "crate_contents" to "箱内所有可能奖励",
        "open_for" to "开启价格",
        "chances" to "概率",
        "open" to "立即开启",
        "active" to "已装备",
        "equip" to "装备",
        "sell" to "回收出售",
        "locked" to "已锁定",
        "unlocked" to "已解锁",
        "mass_recycle" to "批量物品回收",
        "mass_recycle_confirm" to "确认回收",
        "cancel" to "取消",
        "zeta_arena" to "ZETA 竞技场",
        "current_score" to "当前积分",
        "drag_drop_hint" to "将方块拖拽至棋盘中放置",
        "retry" to "重新挑战",
        "menu" to "返回主页",
        "online" to "在线",
        "offline" to "离线",
        "wins" to "胜利",
        "losses" to "失败",
        "winrate" to "胜率",
        "total_games" to "总场次",
        "pause" to "暂停",
        "game_paused" to "游戏暂停中",
        "time" to "倒计时",
        "final_score" to "最终得分",
        "new_record" to "打破最高纪录！",
        "incredible_performance" to "令人惊叹的卓越表现！",
        "tetrises" to "四行消除",
        "play_again" to "再玩一局",
        "main_menu" to "返回主菜单",
        "exit_to_menu" to "退出到主菜单",
        "done" to "完成",
        "ghost_piece" to "投影辅助方块",
        "ghost_outline_only" to "仅显示投影轮廓",
        "smooth_falling" to "平滑下落动画",
        "line_challenge" to "消行挑战规则",
        "duel_invitation" to "收到对决邀请！",
        "accept_duel" to "接受对决",
        "decline" to "拒绝",
        "quick_match_1v1" to "快速匹配 1V1",
        "quick_match_desc" to "瞬间自动寻找对手或创建房间",
        "all_rooms" to "全部房间",
        "open_rooms" to "公开房间",
        "locked_rooms" to "加密房间",
        "no_rooms_found" to "未找到可用房间",
        "no_rooms_desc" to "立即创建专属战场，使用房间代码邀请好友加入！",
        "create_room" to "创建房间",
        "room_name" to "房间名称",
        "enter_room_name" to "请输入房间标题...",
        "match_mode" to "竞赛游戏模式",
        "battle_mode_title" to "攻防对决 (互送障碍行)",
        "battle_mode_desc" to "淘汰对抗赛，消除方块向对手发动垃圾行攻击",
        "score_race_title" to "纯净竞速 (高分比拼)",
        "score_race_desc" to "经典无攻击模式，以最高分或存活时间决胜",
        "room_code" to "房间代码",
        "copy_code" to "复制房号",
        "ready" to "已就绪",
        "not_ready" to "未就绪",
        "start_match" to "开始对决",
        "leave_room" to "离开房间",
        "victory" to "胜利！",
        "defeat" to "战败",
        "surrender" to "投降认输",
        "rematch" to "再战一局",
        "lines_sent" to "发起攻击行数",
        "lines_received" to "承受攻击行数",
        "apm" to "每分钟攻击频次 (APM)",
        "insufficient_funds" to "金币余额不足",
        "item_purchased" to "物品已成功购买并装备。",
        "item_equipped" to "物品已成功装备。",
        "already_unlocked" to "该项目已解锁。",
        "unlock_prev_first" to "请先解锁前置等级。",
        "rank_promoted" to "段位晋升成功！",
        "cloud_sync_title" to "云端数据同步",
        "sync_now" to "立即同步数据",
        "edit_profile" to "编辑个人资料",
        "change_nickname" to "修改玩家昵称",
        "enter_nickname" to "请输入新昵称...",
        "save" to "保存修改",
        "friends" to "好友列表",
        "add_friend" to "添加好友",
        "enter_friend_id" to "请输入好友UID...",
        "add" to "添加",
        "no_friends" to "好友列表为空"
    )

    fun get(key: String, lang: Language): String {
        val map = when (lang) {
            Language.EN -> en
            Language.RU -> ru
            Language.UA -> ua
            Language.KK -> kk
            Language.DE -> de
            Language.ZH -> zh
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
                Language.ZH -> "青铜"
                else -> "BRONZE"
            }
            "SILVER" -> when (lang) {
                Language.RU -> "СЕРЕБРО"
                Language.UA -> "СРІБЛО"
                Language.DE -> "SILBER"
                Language.KK -> "КҮМІС"
                Language.ZH -> "白银"
                else -> "SILVER"
            }
            "GOLD" -> when (lang) {
                Language.RU -> "ЗОЛОТО"
                Language.UA -> "ЗОЛОТО"
                Language.DE -> "GOLD"
                Language.KK -> "АЛТЫН"
                Language.ZH -> "黄金"
                else -> "GOLD"
            }
            "PLATINUM" -> when (lang) {
                Language.RU -> "ПЛАТИНА"
                Language.UA -> "ПЛАТИНА"
                Language.DE -> "PLATIN"
                Language.KK -> "ПЛАТИНА"
                Language.ZH -> "铂金"
                else -> "PLATINUM"
            }
            "DIAMOND" -> when (lang) {
                Language.RU -> "АЛМАЗ"
                Language.UA -> "АЛМАЗ"
                Language.DE -> "DIAMANT"
                Language.KK -> "АЛМАЗ"
                Language.ZH -> "钻石"
                else -> "DIAMOND"
            }
            "MASTER" -> when (lang) {
                Language.RU -> "МАСТЕР"
                Language.UA -> "МАЙСТЕР"
                Language.DE -> "MEISTER"
                Language.KK -> "ШЕБЕР"
                Language.ZH -> "大师"
                else -> "MASTER"
            }
            "GRANDMASTER" -> when (lang) {
                Language.RU -> "ГРАНДМАСТЕР"
                Language.UA -> "ГРАНДМАЙСТЕР"
                Language.DE -> "GROSSMEISTER"
                Language.KK -> "ГРОССМЕЙСТЕР"
                Language.ZH -> "宗师"
                else -> "GRANDMASTER"
            }
            "CHALLENGER" -> when (lang) {
                Language.RU -> "ЛЕГЕНДА"
                Language.UA -> "ЛЕГЕНДА"
                Language.DE -> "HERAUSFORDERER"
                Language.KK -> "АҢЫЗ"
                Language.ZH -> "王者传奇"
                else -> "CHALLENGER"
            }
            else -> rankId
        }
    }

    fun getGameModeTitle(modeCode: String, lang: Language): String {
        return when (modeCode.lowercase()) {
            "classic" -> when (lang) {
                Language.RU -> "Стандарт"
                Language.UA -> "Стандарт"
                Language.KK -> "Стандарт"
                Language.DE -> "Standard"
                Language.ZH -> "标准"
                else -> "Standard"
            }
            "extended" -> when (lang) {
                Language.RU -> "Расширенный"
                Language.UA -> "Розширений"
                Language.KK -> "Кеңейтілген"
                Language.DE -> "Erweitert"
                Language.ZH -> "扩展"
                else -> "Extended"
            }
            "fast_run" -> when (lang) {
                Language.RU -> "Спринт"
                Language.UA -> "Спринт"
                Language.KK -> "Спринт"
                Language.DE -> "Sprint"
                Language.ZH -> "冲刺"
                else -> "Sprint"
            }
            "reverse" -> when (lang) {
                Language.RU -> "Инверсия"
                Language.UA -> "Інверсія"
                Language.KK -> "Инверсия"
                Language.DE -> "Inversion"
                Language.ZH -> "反转"
                else -> "Inversion"
            }
            "block_blast" -> when (lang) {
                Language.RU -> "Арена"
                Language.UA -> "Арена"
                Language.KK -> "Арена"
                Language.DE -> "Arena"
                Language.ZH -> "擂台"
                else -> "Arena"
            }
            "time_attack" -> when (lang) {
                Language.RU -> "Блиц"
                Language.UA -> "Бліц"
                Language.KK -> "Блиц"
                Language.DE -> "Blitz"
                Language.ZH -> "闪击"
                else -> "Blitz"
            }
            "mirror" -> when (lang) {
                Language.RU -> "Зеркало"
                Language.UA -> "Дзеркало"
                Language.KK -> "Айна"
                Language.DE -> "Spiegel"
                Language.ZH -> "镜界"
                else -> "Mirror"
            }
            "relax" -> when (lang) {
                Language.RU -> "Песочница"
                Language.UA -> "Пісочниця"
                Language.KK -> "Құмсалғыш"
                Language.DE -> "Sandbox"
                Language.ZH -> "沙盒"
                else -> "Sandbox"
            }
            "perfectionist" -> when (lang) {
                Language.RU -> "Идеал"
                Language.UA -> "Ідеал"
                Language.KK -> "Мінсіз"
                Language.DE -> "Perfektion"
                Language.ZH -> "完美"
                else -> "Perfection"
            }
            "pattern" -> when (lang) {
                Language.RU -> "Шаблон"
                Language.UA -> "Шаблон"
                Language.KK -> "Үлгі"
                Language.DE -> "Muster"
                Language.ZH -> "图形模板"
                else -> "Blueprint"
            }
            "slide", "slide_puzzle" -> when (lang) {
                Language.RU -> "Слайдер"
                Language.UA -> "Слайдер"
                Language.KK -> "Слайдер"
                Language.DE -> "Schieberätsel"
                Language.ZH -> "滑块"
                else -> "Slide Puzzle"
            }
            "memory", "memory_puzzle" -> when (lang) {
                Language.RU -> "Память"
                Language.UA -> "Пам'ять"
                Language.KK -> "Жады"
                Language.DE -> "Gedächtnis"
                Language.ZH -> "记忆大师"
                else -> "Memory Puzzle"
            }
            else -> modeCode
        }
    }

    fun getLocalizedAchievementTitle(achId: String, lang: Language, fallbackEn: String = "", fallbackRu: String = ""): String {
        return when (achId) {
            "classic_novice" -> when (lang) {
                Language.RU -> "Мастер линий"
                Language.UA -> "Майстер ліній"
                Language.KK -> "Қатар шебері"
                Language.DE -> "Linien-Meister"
                Language.ZH -> "消行入门"
                else -> "Lines Master"
            }
            "score_tycoon" -> when (lang) {
                Language.RU -> "Набор очков"
                Language.UA -> "Набір очок"
                Language.KK -> "Ұпай жинау"
                Language.DE -> "Punkte-Sammler"
                Language.ZH -> "进阶得分"
                else -> "Sizable Score"
            }
            "extended_pioneer" -> when (lang) {
                Language.RU -> "Пионер пентамино"
                Language.UA -> "Піонер пентаміно"
                Language.KK -> "Пентамино пионері"
                Language.DE -> "Pentamino-Pionier"
                Language.ZH -> "五连方块先锋"
                else -> "Pentamino Pioneer"
            }
            "speed_runner" -> when (lang) {
                Language.RU -> "Гиперскорость"
                Language.UA -> "Гіпершвидкість"
                Language.KK -> "Гипержылдамдық"
                Language.DE -> "Hyper-Geschwindigkeit"
                Language.ZH -> "极速超频"
                else -> "Hyper Speed"
            }
            "blast_tactician" -> when (lang) {
                Language.RU -> "Тактик ZETA"
                Language.UA -> "Тактик ZETA"
                Language.KK -> "ZETA Тактигі"
                Language.DE -> "ZETA-Taktiker"
                Language.ZH -> "ZETA 战术家"
                else -> "ZETA Tactician"
            }
            "combo_king" -> when (lang) {
                Language.RU -> "Комбо 3x"
                Language.UA -> "Комбо 3x"
                Language.KK -> "Комбо 3x"
                Language.DE -> "Kombo 3x"
                Language.ZH -> "三连连击"
                else -> "Combo 3x"
            }
            "grandmaster" -> when (lang) {
                Language.RU -> "Гроссмейстер"
                Language.UA -> "Гросмейстер"
                Language.KK -> "Гроссмейстер"
                Language.DE -> "Großmeister"
                Language.ZH -> "特级大师"
                else -> "Grandmaster"
            }
            "blast_master" -> when (lang) {
                Language.RU -> "Ветеран ZETA"
                Language.UA -> "Ветеран ZETA"
                Language.KK -> "ZETA Ардагері"
                Language.DE -> "ZETA-Veteran"
                Language.ZH -> "ZETA 老兵"
                else -> "ZETA Veteran"
            }
            "rich_player" -> when (lang) {
                Language.RU -> "Элитный инвестор"
                Language.UA -> "Елітний інвестор"
                Language.KK -> "Элиталық инвестор"
                Language.DE -> "Elite-Investor"
                Language.ZH -> "精英投资家"
                else -> "Elite Investor"
            }
            "multiplayer_veteran" -> when (lang) {
                Language.RU -> "Ветеран лобби"
                Language.UA -> "Ветеран лобі"
                Language.KK -> "Лобби ардагері"
                Language.DE -> "Lobby-Veteran"
                Language.ZH -> "对战大厅元老"
                else -> "Lobby Veteran"
            }
            "color_skin_collector" -> when (lang) {
                Language.RU -> "Новая тема"
                Language.UA -> "Нова тема"
                Language.KK -> "Жаңа тақырып"
                Language.DE -> "Design-Sammler"
                Language.ZH -> "主题收藏家"
                else -> "New Theme"
            }
            "rank_conqueror" -> when (lang) {
                Language.RU -> "Новый ранг"
                Language.UA -> "Новий ранг"
                Language.KK -> "Жаңа дәреже"
                Language.DE -> "Neuer Rang"
                Language.ZH -> "段位突破"
                else -> "New Rank"
            }
            "games_played_5" -> when (lang) {
                Language.RU -> "Начинающий игрок"
                Language.UA -> "Початківець"
                Language.KK -> "Бастаушы ойыншы"
                Language.DE -> "Anfänger"
                Language.ZH -> "初入茅庐"
                else -> "Novice Player"
            }
            "games_played_25" -> when (lang) {
                Language.RU -> "Опытный игрок"
                Language.UA -> "Досвідчений гравець"
                Language.KK -> "Тәжірибелі ойыншы"
                Language.DE -> "Erfahrener Spieler"
                Language.ZH -> "经验老手"
                else -> "Experienced Player"
            }
            "games_played_100" -> when (lang) {
                Language.RU -> "Легенда Тетриса"
                Language.UA -> "Легенда Тетрісу"
                Language.KK -> "Тетрис аңызы"
                Language.DE -> "Tetris-Legende"
                Language.ZH -> "俄罗斯方块传奇"
                else -> "Tetris Legend"
            }
            "lines_50" -> when (lang) {
                Language.RU -> "Очиститель линий"
                Language.UA -> "Очищувач ліній"
                Language.KK -> "Қатар тазартқыш"
                Language.DE -> "Linien-Feger"
                Language.ZH -> "消行能手"
                else -> "Line Sweeper"
            }
            "lines_200" -> when (lang) {
                Language.RU -> "Уничтожитель линий"
                Language.UA -> "Знищувач ліній"
                Language.KK -> "Қатар жойғыш"
                Language.DE -> "Linien-Schredder"
                Language.ZH -> "消行专家"
                else -> "Line Shredder"
            }
            "lines_1000" -> when (lang) {
                Language.RU -> "Вихревой очиститель"
                Language.UA -> "Вихровий очищувач"
                Language.KK -> "Құйынды тазартқыш"
                Language.DE -> "Vortex-Feger"
                Language.ZH -> "千行湮灭者"
                else -> "Vortex Sweeper"
            }
            "score_single_8000" -> when (lang) {
                Language.RU -> "Сборщик очков"
                Language.UA -> "Збирач очок"
                Language.KK -> "Ұпай жинаушы"
                Language.DE -> "Punkte-Sammler"
                Language.ZH -> "积分达人"
                else -> "Point Collector"
            }
            "score_single_20000" -> when (lang) {
                Language.RU -> "Мастер очков"
                Language.UA -> "Майстер очок"
                Language.KK -> "Ұпай шебері"
                Language.DE -> "Punkte-Meister"
                Language.ZH -> "积分大师"
                else -> "Score Master"
            }
            "score_single_50000" -> when (lang) {
                Language.RU -> "Повелитель очков"
                Language.UA -> "Володар очок"
                Language.KK -> "Ұпай әміршісі"
                Language.DE -> "Punkte-Herrscher"
                Language.ZH -> "积分至尊"
                else -> "Score Overlord"
            }
            "credits_accumulated_5000" -> when (lang) {
                Language.RU -> "Сбережения"
                Language.UA -> "Заощадження"
                Language.KK -> "Жинақ"
                Language.DE -> "Sparer"
                Language.ZH -> "金币储蓄"
                else -> "Savings"
            }
            "credits_accumulated_10000" -> when (lang) {
                Language.RU -> "Крупный капитал"
                Language.UA -> "Великий капітал"
                Language.KK -> "Ірі капитал"
                Language.DE -> "Großes Kapital"
                Language.ZH -> "巨额财富"
                else -> "Big Capital"
            }
            "credits_spent_1000" -> when (lang) {
                Language.RU -> "Покупатель"
                Language.UA -> "Покупець"
                Language.KK -> "Сатып алушы"
                Language.DE -> "Käufer"
                Language.ZH -> "初次消费"
                else -> "Shopper"
            }
            "credits_spent_5000" -> when (lang) {
                Language.RU -> "Активный покупатель"
                Language.UA -> "Активний покупець"
                Language.KK -> "Белсенді сатып алушы"
                Language.DE -> "Aktiver Käufer"
                Language.ZH -> "热衷购物"
                else -> "Active Buyer"
            }
            "credits_spent_10000" -> when (lang) {
                Language.RU -> "Постоянный клиент"
                Language.UA -> "Постійний клієнт"
                Language.KK -> "Тұрақты клиент"
                Language.DE -> "VIP-Kunde"
                Language.ZH -> "VIP 贵宾"
                else -> "VIP Customer"
            }
            "tetrises_cleared_5" -> when (lang) {
                Language.RU -> "Любитель тетрисов"
                Language.UA -> "Шанувальник тетрісів"
                Language.KK -> "Тетрис әуесқойы"
                Language.DE -> "Tetris-Fan"
                Language.ZH -> "四行消除狂热"
                else -> "Tetris Fan"
            }
            "tetrises_cleared_25" -> when (lang) {
                Language.RU -> "Чемпион тетрисов"
                Language.UA -> "Чемпіон тетрісів"
                Language.KK -> "Тетрис чемпионы"
                Language.DE -> "Tetris-Meister"
                Language.ZH -> "四行消除大师"
                else -> "Tetris Champion"
            }
            "tetrises_cleared_100" -> when (lang) {
                Language.RU -> "Мастер тетрисов"
                Language.UA -> "Майстер тетрісів"
                Language.KK -> "Тетрис шебері"
                Language.DE -> "Tetris-Großmeister"
                Language.ZH -> "四行消除宗师"
                else -> "Tetris Master"
            }
            "blast_score_3000" -> when (lang) {
                Language.RU -> "Профи ZETA"
                Language.UA -> "Профі ZETA"
                Language.KK -> "ZETA Кәсіпқойы"
                Language.DE -> "ZETA-Profi"
                Language.ZH -> "ZETA 进阶专家"
                else -> "ZETA Pro"
            }
            "blast_score_10000" -> when (lang) {
                Language.RU -> "Мастер ZETA"
                Language.UA -> "Майстер ZETA"
                Language.KK -> "ZETA Шебері"
                Language.DE -> "ZETA-Meister"
                Language.ZH -> "ZETA 巅峰大师"
                else -> "ZETA Master"
            }
            "combo_multiplier_4" -> when (lang) {
                Language.RU -> "Комбо 4x"
                Language.UA -> "Комбо 4x"
                Language.KK -> "Комбо 4x"
                Language.DE -> "Kombo 4x"
                Language.ZH -> "四连连击"
                else -> "Combo 4x"
            }
            "combo_multiplier_5" -> when (lang) {
                Language.RU -> "Комбо 5x"
                Language.UA -> "Комбо 5x"
                Language.KK -> "Комбо 5x"
                Language.DE -> "Kombo 5x"
                Language.ZH -> "五连连击"
                else -> "Combo 5x"
            }
            "avatar_changes_5" -> when (lang) {
                Language.RU -> "Стилист"
                Language.UA -> "Стиліст"
                Language.KK -> "Стилист"
                Language.DE -> "Stylist"
                Language.ZH -> "时尚造型师"
                else -> "Stylist"
            }
            "title_purchases_3" -> when (lang) {
                Language.RU -> "Коллекционер титулов"
                Language.UA -> "Колекціонер титулів"
                Language.KK -> "Атақтар жинаушы"
                Language.DE -> "Titel-Sammler"
                Language.ZH -> "称号收藏家"
                else -> "Title Collector"
            }
            "frame_purchases_3" -> when (lang) {
                Language.RU -> "Коллекционер рамок"
                Language.UA -> "Колекціонер рамок"
                Language.KK -> "Жақтаулар жинаушы"
                Language.DE -> "Rahmen-Sammler"
                Language.ZH -> "头像框收藏家"
                else -> "Frame Collector"
            }
            "cosmetics_collector" -> when (lang) {
                Language.RU -> "Икона стиля"
                Language.UA -> "Ікона стилю"
                Language.KK -> "Стиль белгісі"
                Language.DE -> "Stil-Ikone"
                Language.ZH -> "潮流风向标"
                else -> "Style Icon"
            }
            "xp_earned_500" -> when (lang) {
                Language.RU -> "Опыт 500"
                Language.UA -> "Досвід 500"
                Language.KK -> "Тәжірибе 500"
                Language.DE -> "Erfahrung 500"
                Language.ZH -> "经验 500"
                else -> "Experience 500"
            }
            "xp_earned_2000" -> when (lang) {
                Language.RU -> "Опыт 2000"
                Language.UA -> "Досвід 2000"
                Language.KK -> "Тәжірибе 2000"
                Language.DE -> "Erfahrung 2000"
                Language.ZH -> "经验 2000"
                else -> "Experience 2000"
            }
            "xp_earned_10000" -> when (lang) {
                Language.RU -> "Опыт 10000"
                Language.UA -> "Досвід 10000"
                Language.KK -> "Тәжірибе 10000"
                Language.DE -> "Erfahrung 10000"
                Language.ZH -> "经验 10000"
                else -> "Experience 10000"
            }
            "player_level_5" -> when (lang) {
                Language.RU -> "Уровень 5"
                Language.UA -> "Рівень 5"
                Language.KK -> "5-деңгей"
                Language.DE -> "Stufe 5"
                Language.ZH -> "等级 5"
                else -> "Level 5"
            }
            "player_level_15" -> when (lang) {
                Language.RU -> "Уровень 15"
                Language.UA -> "Рівень 15"
                Language.KK -> "15-деңгей"
                Language.DE -> "Stufe 15"
                Language.ZH -> "等级 15"
                else -> "Level 15"
            }
            "player_level_30" -> when (lang) {
                Language.RU -> "Уровень 30"
                Language.UA -> "Рівень 30"
                Language.KK -> "30-деңгей"
                Language.DE -> "Stufe 30"
                Language.ZH -> "等级 30"
                else -> "Level 30"
            }
            "mode_time_attack" -> when (lang) {
                Language.RU -> "Тайм-атак"
                Language.UA -> "Тайм-атак"
                Language.KK -> "Тайм-атак"
                Language.DE -> "Zeitangriff"
                Language.ZH -> "限时挑战"
                else -> "Time Attack"
            }
            "mode_reverse" -> when (lang) {
                Language.RU -> "Инверсия"
                Language.UA -> "Інверсія"
                Language.KK -> "Инверсия"
                Language.DE -> "Inversion"
                Language.ZH -> "反转控制"
                else -> "Inversion Controls"
            }
            "mode_mirror" -> when (lang) {
                Language.RU -> "Зеркальный мир"
                Language.UA -> "Дзеркальний світ"
                Language.KK -> "Айна әлемі"
                Language.DE -> "Spiegelwelt"
                Language.ZH -> "镜像空间"
                else -> "Mirror World"
            }
            "mode_relax", "mode_pentary" -> when (lang) {
                Language.RU -> "Песочница"
                Language.UA -> "Пісочниця"
                Language.KK -> "Құмсалғыш"
                Language.DE -> "Sandbox"
                Language.ZH -> "沙盒模式"
                else -> "Sandbox Mode"
            }
            "mode_extended" -> when (lang) {
                Language.RU -> "Расширенный режим"
                Language.UA -> "Розширений режим"
                Language.KK -> "Кеңейтілген режим"
                Language.DE -> "Erweiterter Modus"
                Language.ZH -> "扩展模式"
                else -> "Extended Mode"
            }
            "speed_level_max" -> when (lang) {
                Language.RU -> "Мастер скорости"
                Language.UA -> "Майстер швидкості"
                Language.KK -> "Жылдамдық шебері"
                Language.DE -> "Geschwindigkeits-Meister"
                Language.ZH -> "极速大师"
                else -> "Speed Master"
            }
            "millionaire" -> when (lang) {
                Language.RU -> "Миллионер"
                Language.UA -> "Мільйонер"
                Language.KK -> "Миллионер"
                Language.DE -> "Millionär"
                Language.ZH -> "百万富翁"
                else -> "Millionaire"
            }
            "all_unlocked" -> when (lang) {
                Language.RU -> "Чемпион"
                Language.UA -> "Чемпіон"
                Language.KK -> "Чемпион"
                Language.DE -> "Champion"
                Language.ZH -> "全成就大满贯"
                else -> "Champion"
            }
            else -> when (lang) {
                Language.RU -> fallbackRu.ifEmpty { fallbackEn }
                Language.UA -> fallbackRu.ifEmpty { fallbackEn }
                Language.KK -> fallbackRu.ifEmpty { fallbackEn }
                Language.DE -> fallbackEn
                Language.ZH -> fallbackEn
                else -> fallbackEn
            }
        }
    }

    fun getLocalizedAchievementDesc(achId: String, lang: Language, fallbackEn: String = "", fallbackRu: String = ""): String {
        return when (achId) {
            "classic_novice" -> when (lang) {
                Language.RU -> "Уберите 10 линий в классическом режиме"
                Language.UA -> "Приберіть 10 ліній у класичному режимі"
                Language.KK -> "Классикалық режимде 10 қатарды тазалаңыз"
                Language.DE -> "Räume 10 Linien im klassischen Modus ab"
                Language.ZH -> "在经典模式中消除10行方块"
                else -> "Clear 10 lines in Classic Match"
            }
            "score_tycoon" -> when (lang) {
                Language.RU -> "Наберите 5 000 очков в одном матче"
                Language.UA -> "Наберіть 5 000 очок в одному матчі"
                Language.KK -> "Бір матчта 5 000 ұпай жинаңыз"
                Language.DE -> "Erziele 5.000 Punkte in einem einzigen Spiel"
                Language.ZH -> "单局比赛斩获5,000分"
                else -> "Score 5,000 points in a single match"
            }
            "extended_pioneer" -> when (lang) {
                Language.RU -> "Сыграйте матч в расширенном режиме"
                Language.UA -> "Зіграйте матч у розширеному режимі"
                Language.KK -> "Кеңейтілген режимде ойын ойнаңыз"
                Language.DE -> "Spiele ein Match im erweiterten Modus"
                Language.ZH -> "体验一局扩展方块模式"
                else -> "Play a match in Extended Shapes mode"
            }
            "speed_runner" -> when (lang) {
                Language.RU -> "Начните матч на 10-м уровне в гипер-режиме"
                Language.UA -> "Почніть матч на 10-му рівні в гіпер-режимі"
                Language.KK -> "Гипер-режимде 10-деңгейде матч бастаңыз"
                Language.DE -> "Starte ein Match auf Stufe 10 im Hyper-Modus"
                Language.ZH -> "在极速超频模式下以10级难度开启对局"
                else -> "Start a match at level 10 in Hyper Blast mode"
            }
            "blast_tactician" -> when (lang) {
                Language.RU -> "Наберите 1 000 очков в режиме ZETA"
                Language.UA -> "Наберіть 1 000 очок у режимі ZETA"
                Language.KK -> "ZETA режимінде 1 000 ұпай жинаңыз"
                Language.DE -> "Erziele 1.000 Punkte im ZETA-Modus"
                Language.ZH -> "在 ZETA 模式中获得 1,000 分"
                else -> "Score 1,000 points in ZETA mode"
            }
            "combo_king" -> when (lang) {
                Language.RU -> "Сделайте серию комбо 3x в мультиплеере"
                Language.UA -> "Зробіть серію комбо 3x у мультиплеєрі"
                Language.KK -> "Мультиплеерде 3x комбо сериясын орындаңыз"
                Language.DE -> "Erreiche eine 3-fach-Kombokette im Mehrspieler"
                Language.ZH -> "在多人对战中达成3连击"
                else -> "Reach a 3x combo chain in multiplayer"
            }
            "grandmaster" -> when (lang) {
                Language.RU -> "Наберите 15 000 очков в классическом режиме"
                Language.UA -> "Наберіть 15 000 очок у класичному режимі"
                Language.KK -> "Классикалық режимде 15 000 ұпай жинаңыз"
                Language.DE -> "Erziele 15.000 Punkte im klassischen Modus"
                Language.ZH -> "在经典模式中斩获 15,000 分"
                else -> "Score 15,000 points in Classic Match"
            }
            "blast_master" -> when (lang) {
                Language.RU -> "Наберите 5 000 очков в режиме ZETA"
                Language.UA -> "Наберіть 5 000 очок у режимі ZETA"
                Language.KK -> "ZETA режимінде 5 000 ұпай жинаңыз"
                Language.DE -> "Erziele 5.000 Punkte im ZETA-Modus"
                Language.ZH -> "在 ZETA 模式中获得 5,000 分"
                else -> "Score 5,000 points in ZETA mode"
            }
            "rich_player" -> when (lang) {
                Language.RU -> "Накопите 2 000 монет на балансе"
                Language.UA -> "Накопичіть 2 000 монет на балансі"
                Language.KK -> "Баланста 2 000 тиын жинаңыз"
                Language.DE -> "Spare 2.000 Münzen auf deinem Konto"
                Language.ZH -> "账户金币余额达到 2,000"
                else -> "Save 2,000 coins in your balance"
            }
            "multiplayer_veteran" -> when (lang) {
                Language.RU -> "Подключитесь к сетевому лобби 5 раз"
                Language.UA -> "Підключіться до мережевого лобі 5 разів"
                Language.KK -> "Желілік лоббиге 5 рет қосылыңыз"
                Language.DE -> "Tritt 5 Mal der Mehrspieler-Lobby bei"
                Language.ZH -> "进入多人对战大厅 5 次"
                else -> "Connect to multiplayer lobby 5 times"
            }
            "color_skin_collector" -> when (lang) {
                Language.RU -> "Приобретите свою первую тему игрового поля"
                Language.UA -> "Придбайте свою першу тему ігрового поля"
                Language.KK -> "Ойын алаңының алғашқы тақырыбын сатып алыңыз"
                Language.DE -> "Schalte dein erstes Spielfeld-Design frei"
                Language.ZH -> "购买并解锁首个棋盘网格主题"
                else -> "Unlock your first custom board theme"
            }
            "rank_conqueror" -> when (lang) {
                Language.RU -> "Повысьте свой ранг за монеты"
                Language.UA -> "Підвищіть свій ранг за монети"
                Language.KK -> "Тиындар арқылы дәрежеңізді көтеріңіз"
                Language.DE -> "Verbessere deinen Rang mit Münzen"
                Language.ZH -> "使用金币提升个人段位"
                else -> "Upgrade your profile rank with coins"
            }
            "games_played_5" -> when (lang) {
                Language.RU -> "Сыграйте 5 матчей в любом режиме"
                Language.UA -> "Зіграйте 5 матчів у будь-якому режимі"
                Language.KK -> "Кез келген режимде 5 ойын ойнаңыз"
                Language.DE -> "Spiele 5 Matches in einem beliebigen Modus"
                Language.ZH -> "在任意模式中完成 5 局游戏"
                else -> "Play 5 matches in any mode"
            }
            "games_played_25" -> when (lang) {
                Language.RU -> "Сыграйте 25 матчей в любом режиме"
                Language.UA -> "Зіграйте 25 матчів у будь-якому режимі"
                Language.KK -> "Кез келген режимде 25 ойын ойнаңыз"
                Language.DE -> "Spiele 25 Matches in einem beliebigen Modus"
                Language.ZH -> "在任意模式中完成 25 局游戏"
                else -> "Play 25 matches in any mode"
            }
            "games_played_100" -> when (lang) {
                Language.RU -> "Сыграйте 100 матчей в любом режиме"
                Language.UA -> "Зіграйте 100 матчів у будь-якому режимі"
                Language.KK -> "Кез келген режимде 100 ойын ойнаңыз"
                Language.DE -> "Spiele 100 Matches in einem beliebigen Modus"
                Language.ZH -> "在任意模式中完成 100 局游戏"
                else -> "Play 100 matches in any mode"
            }
            "lines_50" -> when (lang) {
                Language.RU -> "Уберите 50 линий за все игры"
                Language.UA -> "Приберіть 50 ліній за всі ігри"
                Language.KK -> "Барлық ойындарда 50 қатарды тазалаңыз"
                Language.DE -> "Räume insgesamt 50 Linien ab"
                Language.ZH -> "累计消除 50 行方块"
                else -> "Clear 50 total lines across all matches"
            }
            "lines_200" -> when (lang) {
                Language.RU -> "Уберите 200 линий за все игры"
                Language.UA -> "Приберіть 200 ліній за всі ігри"
                Language.KK -> "Барлық ойындарда 200 қатарды тазалаңыз"
                Language.DE -> "Räume insgesamt 200 Linien ab"
                Language.ZH -> "累计消除 200 行方块"
                else -> "Clear 200 total lines across all matches"
            }
            "lines_1000" -> when (lang) {
                Language.RU -> "Уберите 1 000 линий за все игры"
                Language.UA -> "Приберіть 1 000 ліній за всі ігри"
                Language.KK -> "Барлық ойындарда 1 000 қатарды тазалаңыз"
                Language.DE -> "Räume insgesamt 1.000 Linien ab"
                Language.ZH -> "累计消除 1,000 行方块"
                else -> "Clear 1,000 total lines across all matches"
            }
            "score_single_8000" -> when (lang) {
                Language.RU -> "Наберите 8 000 очков в одном матче"
                Language.UA -> "Наберіть 8 000 очок в одному матчі"
                Language.KK -> "Бір матчта 8 000 ұпай жинаңыз"
                Language.DE -> "Erziele 8.000 Punkte in einem einzigen Spiel"
                Language.ZH -> "单局比赛斩获 8,000 分"
                else -> "Score 8,000 points in a single match"
            }
            "score_single_20000" -> when (lang) {
                Language.RU -> "Наберите 20 000 очков в одном матче"
                Language.UA -> "Наберіть 20 000 очок в одному матчі"
                Language.KK -> "Бір матчта 20 000 ұпай жинаңыз"
                Language.DE -> "Erziele 20.000 Punkte in einem einzigen Spiel"
                Language.ZH -> "单局比赛斩获 20,000 分"
                else -> "Score 20,000 points in a single match"
            }
            "score_single_50000" -> when (lang) {
                Language.RU -> "Наберите 50 000 очков в одном матче"
                Language.UA -> "Наберіть 50 000 очок в одном матчі"
                Language.KK -> "Бір матчта 50 000 ұпай жинаңыз"
                Language.DE -> "Erziele 50.000 Punkte in einem einzigen Spiel"
                Language.ZH -> "单局比赛斩获 50,000 分"
                else -> "Score 50,000 points in a single match"
            }
            "credits_accumulated_5000" -> when (lang) {
                Language.RU -> "Накопите 5 000 монет на балансе"
                Language.UA -> "Накопичіть 5 000 монет на балансі"
                Language.KK -> "Баланста 5 000 тиын жинаңыз"
                Language.DE -> "Erreiche ein Guthaben von 5.000 Münzen"
                Language.ZH -> "金币余额累计达到 5,000"
                else -> "Reach a balance of 5,000 coins"
            }
            "credits_accumulated_10000" -> when (lang) {
                Language.RU -> "Накопите 10 000 монет на балансе"
                Language.UA -> "Накопичіть 10 000 монет на балансі"
                Language.KK -> "Баланста 10 000 тиын жинаңыз"
                Language.DE -> "Erreiche ein Guthaben von 10.000 Münzen"
                Language.ZH -> "金币余额累计达到 10,000"
                else -> "Reach a balance of 10,000 coins"
            }
            "credits_spent_1000" -> when (lang) {
                Language.RU -> "Потратьте 1 000 монет в магазине"
                Language.UA -> "Витратьте 1 000 монет у магазині"
                Language.KK -> "Дүкенде 1 000 тиын жұмсаңыз"
                Language.DE -> "Gib 1.000 Münzen im Shop aus"
                Language.ZH -> "在商店累计消费 1,000 金币"
                else -> "Spend 1,000 coins in the store"
            }
            "credits_spent_5000" -> when (lang) {
                Language.RU -> "Потратьте 5 000 монет в магазине"
                Language.UA -> "Витратьте 5 000 монет у магазині"
                Language.KK -> "Дүкенде 5 000 тиын жұмсаңыз"
                Language.DE -> "Gib 5.000 Münzen im Shop aus"
                Language.ZH -> "在商店累计消费 5,000 金币"
                else -> "Spend 5,000 coins in the store"
            }
            "credits_spent_10000" -> when (lang) {
                Language.RU -> "Потратьте 10 000 монет в магазине"
                Language.UA -> "Витратьте 10 000 монет у магазині"
                Language.KK -> "Дүкенде 10 000 тиын жұмсаңыз"
                Language.DE -> "Gib 10.000 Münzen im Shop aus"
                Language.ZH -> "在商店累计消费 10,000 金币"
                else -> "Spend 10,000 coins in the store"
            }
            "tetrises_cleared_5" -> when (lang) {
                Language.RU -> "Очистите 4 линии за раз 5 раз"
                Language.UA -> "Очистіть 4 лінії за раз 5 разів"
                Language.KK -> "Бірден 4 қатарды 5 рет тазалаңыз"
                Language.DE -> "Führe 5 Mal ein Tetris aus"
                Language.ZH -> "完成 5 次四行同时消除 (Tetris)"
                else -> "Perform 5 four-line clears (Tetrises)"
            }
            "tetrises_cleared_25" -> when (lang) {
                Language.RU -> "Очистите 4 линии за раз 25 раз"
                Language.UA -> "Очистіть 4 лінії за раз 25 разів"
                Language.KK -> "Бірден 4 қатарды 25 рет тазалаңыз"
                Language.DE -> "Führe 25 Mal ein Tetris aus"
                Language.ZH -> "完成 25 次四行同时消除 (Tetris)"
                else -> "Perform 25 four-line clears (Tetrises)"
            }
            "tetrises_cleared_100" -> when (lang) {
                Language.RU -> "Очистите 4 линии за раз 100 раз"
                Language.UA -> "Очистіть 4 лінії за раз 100 разів"
                Language.KK -> "Бірден 4 қатарды 100 рет тазалаңыз"
                Language.DE -> "Führe 100 Mal ein Tetris aus"
                Language.ZH -> "完成 100 次四行同时消除 (Tetris)"
                else -> "Perform 100 four-line clears (Tetrises)"
            }
            "blast_score_3000" -> when (lang) {
                Language.RU -> "Наберите 3 000 очков в режиме ZETA"
                Language.UA -> "Наберіть 3 000 очок у режимі ZETA"
                Language.KK -> "ZETA режимінде 3 000 ұпай жинаңыз"
                Language.DE -> "Erziele 3.000 Punkte im ZETA-Modus"
                Language.ZH -> "在 ZETA 模式中获得 3,000 分"
                else -> "Reach 3,000 points in ZETA mode"
            }
            "blast_score_10000" -> when (lang) {
                Language.RU -> "Наберите 10 000 очков в режиме ZETA"
                Language.UA -> "Наберіть 10 000 очок у режимі ZETA"
                Language.KK -> "ZETA режимінде 10 000 ұпай жинаңыз"
                Language.DE -> "Erziele 10.000 Punkte im ZETA-Modus"
                Language.ZH -> "在 ZETA 模式中获得 10,000 分"
                else -> "Reach 10,000 points in ZETA mode"
            }
            "combo_multiplier_4" -> when (lang) {
                Language.RU -> "Сделайте серию комбо 4x в мультиплеере"
                Language.UA -> "Зробіть серію комбо 4x у мультиплеєрі"
                Language.KK -> "Мультиплеерде 4x комбо сериясын орындаңыз"
                Language.DE -> "Erreiche eine 4-fach-Kombokette im Mehrspieler"
                Language.ZH -> "在多人对战中达成 4 连击"
                else -> "Reach a 4x combo chain in multiplayer"
            }
            "combo_multiplier_5" -> when (lang) {
                Language.RU -> "Сделайте серию комбо 5x в мультиплеере"
                Language.UA -> "Зробіть серію комбо 5x у мультиплеєрі"
                Language.KK -> "Мультиплеерде 5x комбо сериясын орындаңыз"
                Language.DE -> "Erreiche eine 5-fach-Kombokette im Mehrspieler"
                Language.ZH -> "在多人对战中达成 5 连击"
                else -> "Reach a 5x combo chain in multiplayer"
            }
            "avatar_changes_5" -> when (lang) {
                Language.RU -> "Смените эмодзи или цвет аватара 5 раз"
                Language.UA -> "Змініть емодзі або колір аватара 5 разів"
                Language.KK -> "Аватардың эмодзиін немесе түсін 5 рет өзгертіңіз"
                Language.DE -> "Ändere dein Avatar-Emoji oder die Farbe 5 Mal"
                Language.ZH -> "更换头像 Emoji 或背景颜色 5 次"
                else -> "Change your avatar emoji or color 5 times"
            }
            "title_purchases_3" -> when (lang) {
                Language.RU -> "Приобретите 3 разных титула в магазине"
                Language.UA -> "Придбайте 3 різних титули в магазині"
                Language.KK -> "Дүкеннен 3 түрлі атақ сатып алыңыз"
                Language.DE -> "Kaufe 3 verschiedene Titel im Shop"
                Language.ZH -> "在商店购买 3 个不同的玩家称号"
                else -> "Purchase 3 different profile titles in the store"
            }
            "frame_purchases_3" -> when (lang) {
                Language.RU -> "Приобретите 3 разных рамки в магазине"
                Language.UA -> "Придбайте 3 різних рамки в магазині"
                Language.KK -> "Дүкеннен 3 түрлі жақтау сатып алыңыз"
                Language.DE -> "Kaufe 3 verschiedene Avatar-Rahmen im Shop"
                Language.ZH -> "在商店购买 3 个不同的头像框"
                else -> "Purchase 3 different avatar frames in the store"
            }
            "cosmetics_collector" -> when (lang) {
                Language.RU -> "Приобретите 3 темы или стиля кнопок"
                Language.UA -> "Придбайте 3 теми або стилі кнопок"
                Language.KK -> "3 тақырып немесе батырма стилін сатып алыңыз"
                Language.DE -> "Schalte 3 Designs oder Button-Stile frei"
                Language.ZH -> "解锁 3 款网格主题或按键样式"
                else -> "Unlock 3 themes or control button styles"
            }
            "xp_earned_500" -> when (lang) {
                Language.RU -> "Наберите 500 очков опыта (XP) суммарно"
                Language.UA -> "Наберіть 500 очок досвіду (XP) сумарно"
                Language.KK -> "Барлығы 500 тәжірибе ұпайын (XP) жинаңыз"
                Language.DE -> "Sammle insgesamt 500 Erfahrungspunkte (XP)"
                Language.ZH -> "累计获取 500 点经验值 (XP)"
                else -> "Earn 500 total Experience Points (XP)"
            }
            "xp_earned_2000" -> when (lang) {
                Language.RU -> "Наберите 2 000 очков опыта (XP) суммарно"
                Language.UA -> "Наберіть 2 000 очок досвіду (XP) сумарно"
                Language.KK -> "Барлығы 2 000 тәжірибе ұпайын (XP) жинаңыз"
                Language.DE -> "Sammle insgesamt 2.000 Erfahrungspunkte (XP)"
                Language.ZH -> "累计获取 2,000 点经验值 (XP)"
                else -> "Earn 2,000 total Experience Points (XP)"
            }
            "xp_earned_10000" -> when (lang) {
                Language.RU -> "Наберите 10 000 очков опыта (XP) суммарно"
                Language.UA -> "Наберіть 10 000 очок досвіду (XP) сумарно"
                Language.KK -> "Барлығы 10 000 тәжірибе ұпайын (XP) жинаңыз"
                Language.DE -> "Sammle insgesamt 10.000 Erfahrungspunkte (XP)"
                Language.ZH -> "累计获取 10,000 点经验值 (XP)"
                else -> "Earn 10,000 total Experience Points (XP)"
            }
            "player_level_5" -> when (lang) {
                Language.RU -> "Достигните 5-го уровня игрока"
                Language.UA -> "Досягніть 5-го рівня гравця"
                Language.KK -> "Ойыншының 5-деңгейіне жетіңіз"
                Language.DE -> "Erreiche Spieler-Stufe 5"
                Language.ZH -> "玩家等级达到 5 级"
                else -> "Reach Player Level 5"
            }
            "player_level_15" -> when (lang) {
                Language.RU -> "Достигните 15-го уровня игрока"
                Language.UA -> "Досягніть 15-го рівня гравця"
                Language.KK -> "Ойыншының 15-деңгейіне жетіңіз"
                Language.DE -> "Erreiche Spieler-Stufe 15"
                Language.ZH -> "玩家等级达到 15 级"
                else -> "Reach Player Level 15"
            }
            "player_level_30" -> when (lang) {
                Language.RU -> "Достигните 30-го уровня игрока"
                Language.UA -> "Досягніть 30-го рівня гравця"
                Language.KK -> "Ойыншының 30-деңгейіне жетіңіз"
                Language.DE -> "Erreiche Spieler-Stufe 30"
                Language.ZH -> "玩家等级达到 30 级"
                else -> "Reach Player Level 30"
            }
            "mode_time_attack" -> when (lang) {
                Language.RU -> "Сыграйте матч в режиме Тайм-Атак"
                Language.UA -> "Зіграйте матч у режимі Тайм-Атак"
                Language.KK -> "Тайм-атак режимінде ойын ойнаңыз"
                Language.DE -> "Spiele ein Match im Zeitangriffs-Modus"
                Language.ZH -> "开启一局限时突袭模式对决"
                else -> "Play a game in Time Attack mode"
            }
            "mode_reverse" -> when (lang) {
                Language.RU -> "Сыграйте матч в режиме Инверсия"
                Language.UA -> "Зіграйте матч у режимі Інверсія"
                Language.KK -> "Инверсия режимінде ойын ойнаңыз"
                Language.DE -> "Spiele ein Match im Inversions-Modus"
                Language.ZH -> "开启一局反转控制模式对决"
                else -> "Play a game in Inversion mode"
            }
            "mode_mirror" -> when (lang) {
                Language.RU -> "Сыграйте матч в режиме Зеркальный Мир"
                Language.UA -> "Зіграйте матч у режимі Дзеркальний Світ"
                Language.KK -> "Айна әлемі режимінде ойын ойнаңыз"
                Language.DE -> "Spiele ein Match im Spiegel-Dimensions-Modus"
                Language.ZH -> "开启一局镜像异次元模式对决"
                else -> "Play a game in Mirror Dimension mode"
            }
            "mode_relax", "mode_pentary" -> when (lang) {
                Language.RU -> "Сыграйте матч в режиме Песочница"
                Language.UA -> "Зіграйте матч у режимі Пісочниця"
                Language.KK -> "Құмсалғыш режимінде ойын ойнаңыз"
                Language.DE -> "Spiele ein Match im Sandbox-Modus"
                Language.ZH -> "开启一局沙盒模式对决"
                else -> "Play a game in Sandbox mode"
            }
            "mode_extended" -> when (lang) {
                Language.RU -> "Сыграйте матч в Расширенном режиме"
                Language.UA -> "Зіграйте матч у Розширеному режимі"
                Language.KK -> "Кеңейтілген режимде ойын ойнаңыз"
                Language.DE -> "Spiele ein Match im erweiterten Modus"
                Language.ZH -> "开启一局扩展模式对决"
                else -> "Play a game in Extended mode"
            }
            "speed_level_max" -> when (lang) {
                Language.RU -> "Достигните 15-й скорости в обычном матче"
                Language.UA -> "Досягніть 15-ї швидкості у звичайному матчі"
                Language.KK -> "Қалыпты ойында 15-жылдамдыққа жетіңіз"
                Language.DE -> "Erreiche Geschwindigkeitsstufe 15 im Standard-Match"
                Language.ZH -> "在标准比赛中达到 15 级极速下落"
                else -> "Reach speed level 15 in standard match modes"
            }
            "millionaire" -> when (lang) {
                Language.RU -> "Накопите 1 000 000 монет (Открывает Престиж III: x8 доход и Личный Тег)"
                Language.UA -> "Накопичіть 1 000 000 монет (Відкриває Престиж III: x8 дохід та Особистий Тег)"
                Language.KK -> "1 000 000 тиын жинаңыз (Престиж III ашады: x8 табыс пен Жеке Тег)"
                Language.DE -> "Sammle 1.000.000 Münzen für Prestige III (x8 Multiplikator und Tag)"
                Language.ZH -> "累计积攒 1,000,000 金币以解锁声望 III (永久8倍金币加成与专属标签)"
                else -> "Accumulate 1,000,000 coins to unlock Prestige III"
            }
            "all_unlocked" -> when (lang) {
                Language.RU -> "Откройте все остальные достижения (Главная финальная награда)"
                Language.UA -> "Відкрийте всі інші досягнення (Головна фінальна нагорода)"
                Language.KK -> "Барлық қалған жетістіктерді ашыңыз (Басты финалдық сыйлық)"
                Language.DE -> "Schalte alle anderen Erfolge frei (Große Abschluss-Belohnung)"
                Language.ZH -> "解锁全部其余成就 (终极史诗通关奖励)"
                else -> "Unlock all other achievements (Epic Completion Reward)"
            }
            else -> when (lang) {
                Language.RU -> fallbackRu.ifEmpty { fallbackEn }
                Language.UA -> fallbackRu.ifEmpty { fallbackEn }
                Language.KK -> fallbackRu.ifEmpty { fallbackEn }
                Language.DE -> fallbackEn
                Language.ZH -> fallbackEn
                else -> fallbackEn
            }
        }
    }

    fun getLocalizedSkinTitle(skinId: String, lang: Language): String = when (skinId) {
        "cyberpunk" -> when (lang) {
            Language.RU -> "КВАНТ"
            Language.UA -> "КВАНТ"
            Language.KK -> "КВАНТ"
            Language.DE -> "QUANTUM"
            Language.ZH -> "量子"
            else -> "QUANTUM"
        }
        "retro_amber" -> when (lang) {
            Language.RU -> "ЗОЛОТО"
            Language.UA -> "ЗОЛОТО"
            Language.KK -> "АЛТЫН"
            Language.DE -> "GOLD"
            Language.ZH -> "黄金"
            else -> "GOLD"
        }
        "emerald_matrix" -> when (lang) {
            Language.RU -> "ИЗУМРУД"
            Language.UA -> "СМАРАГД"
            Language.KK -> "ЗҮБӘРЖАТ"
            Language.DE -> "SMARAGD"
            Language.ZH -> "翡翠"
            else -> "EMERALD"
        }
        "vaporwave_pink" -> when (lang) {
            Language.RU -> "СИНТВЕЙВ"
            Language.UA -> "СИНТВЕЙВ"
            Language.KK -> "СИНТВЕЙВ"
            Language.DE -> "SYNTHWAVE"
            Language.ZH -> "蒸汽波"
            else -> "SYNTHWAVE"
        }
        "midnight_gold" -> when (lang) {
            Language.RU -> "ОБСИДИАН"
            Language.UA -> "ОБСИДІАН"
            Language.KK -> "ОБСИДИАН"
            Language.DE -> "OBSIDIAN"
            Language.ZH -> "黑曜石"
            else -> "OBSIDIAN"
        }
        "carbon_neutral" -> when (lang) {
            Language.RU -> "ГРАФИТ"
            Language.UA -> "ГРАФІТ"
            Language.KK -> "ГРАФИТ"
            Language.DE -> "GRAPHIT"
            Language.ZH -> "石墨"
            else -> "GRAPHITE"
        }
        "plasma_storm" -> when (lang) {
            Language.RU -> "ПЛАЗМА"
            Language.UA -> "ПЛАЗМА"
            Language.KK -> "ПЛАЗМА"
            Language.DE -> "PLASMA"
            Language.ZH -> "等离子"
            else -> "PLASMA"
        }
        "glacial_frost" -> when (lang) {
            Language.RU -> "КРИСТАЛЛ"
            Language.UA -> "КРИСТАЛ"
            Language.KK -> "КРИСТАЛДАР"
            Language.DE -> "KRISTALL"
            Language.ZH -> "晶体"
            else -> "CRYSTAL"
        }
        else -> skinId.uppercase()
    }

    fun getLocalizedSkinDesc(skinId: String, lang: Language): String = when (skinId) {
        "cyberpunk" -> when (lang) {
            Language.RU -> "Высококонтрастный квантовый стиль"
            Language.UA -> "Висококонтрастний квантовий стиль"
            Language.KK -> "Жоғары контрастты кванттық стиль"
            Language.DE -> "Kontrastreiches Quanten-Design"
            Language.ZH -> "高对比度量子网格设计"
            else -> "High-contrast quantum grid design"
        }
        "retro_amber" -> when (lang) {
            Language.RU -> "Классический монохром теплого золота"
            Language.UA -> "Класичний монохром теплого золота"
            Language.KK -> "Жылы алтын монохромы"
            Language.DE -> "Klassisches warmes Gold-Monochrom"
            Language.ZH -> "经典温暖黄金单色"
            else -> "Vintage warm gold monochrome layout"
        }
        "emerald_matrix" -> when (lang) {
            Language.RU -> "Цифровой зеленый код терминала"
            Language.UA -> "Цифровий зелений код терміналу"
            Language.KK -> "Терминалдың цифрлық жасыл коды"
            Language.DE -> "Digitaler grüner Matrix-Code"
            Language.ZH -> "黑客绿色数字雨代码流"
            else -> "Cyberspace digital green code rain"
        }
        "vaporwave_pink" -> when (lang) {
            Language.RU -> "Розово-пурпурные закатные тона"
            Language.UA -> "Рожево-пурпурові західні тони"
            Language.KK -> "Қызғылт-күлгін күн бату реңктері"
            Language.DE -> "Rosa-lila Synthwave-Sonnenuntergang"
            Language.ZH -> "梦幻粉紫日落色调"
            else -> "Synthwave sunset accents"
        }
        "midnight_gold" -> when (lang) {
            Language.RU -> "Глубокий черный с золотыми акцентами"
            Language.UA -> "Глибокий чорний із золотими акцентами"
            Language.KK -> "Алтын реңкті терең қара стиль"
            Language.DE -> "Tiefschwarz mit goldenen Akzenten"
            Language.ZH -> "奢华深黑与曜金细节"
            else -> "Deep black with premium gold accents"
        }
        "carbon_neutral" -> when (lang) {
            Language.RU -> "Матовый промышленный титановый сплав"
            Language.UA -> "Матовий промисловий титановий сплав"
            Language.KK -> "Күңгірт өндірістік титан қорытпасы"
            Language.DE -> "Mattes Industrie-Titan"
            Language.ZH -> "工业质感磨砂钛合金纹理"
            else -> "Industrial brushed graphite metal texture"
        }
        "plasma_storm" -> when (lang) {
            Language.RU -> "Энергетическое плазменное поле"
            Language.UA -> "Енергетичне плазмове поле"
            Language.KK -> "Энергетикалық плазма өрісі"
            Language.DE -> "Hochenergetisches Plasmafeld"
            Language.ZH -> "高能等离子能量场"
            else -> "High-energy plasma static interference overlay"
        }
        "glacial_frost" -> when (lang) {
            Language.RU -> "Кристаллы арктического льда"
            Language.UA -> "Кристали арктичного льоду"
            Language.KK -> "Арктикалық мұз кристалдары"
            Language.DE -> "Subpolare Eiskristalle"
            Language.ZH -> "极地冰蓝结晶纹理"
            else -> "Sub-zero tundra blue crystal design"
        }
        else -> ""
    }

    fun getLocalizedCubeSkinTitle(cubeSkinId: String, lang: Language): String = when (cubeSkinId) {
        "neon" -> when (lang) {
            Language.RU -> "ФОТОН"
            Language.UA -> "ФОТОН"
            Language.KK -> "ФОТОН"
            Language.DE -> "PHOTON"
            Language.ZH -> "光子"
            else -> "PHOTON"
        }
        "glass" -> when (lang) {
            Language.RU -> "СТЕКЛО"
            Language.UA -> "СКЛО"
            Language.KK -> "ШЫНЫ"
            Language.DE -> "GLAS"
            Language.ZH -> "玻璃"
            else -> "GLASS"
        }
        "retro" -> when (lang) {
            Language.RU -> "РЕТРО"
            Language.UA -> "РЕТРО"
            Language.KK -> "РЕТРО"
            Language.DE -> "RETRO"
            Language.ZH -> "复古"
            else -> "RETRO"
        }
        "flat" -> when (lang) {
            Language.RU -> "ФЛЭТ"
            Language.UA -> "ФЛЕТ"
            Language.KK -> "ТЕГІС"
            Language.DE -> "FLAT"
            Language.ZH -> "极简"
            else -> "FLAT"
        }
        "material" -> when (lang) {
            Language.RU -> "МАТЕРИАЛ"
            Language.UA -> "МАТЕРІАЛ"
            Language.KK -> "МАТЕРИАЛ"
            Language.DE -> "MATERIAL"
            Language.ZH -> "质感"
            else -> "MATERIAL"
        }
        "glowing_jewel" -> when (lang) {
            Language.RU -> "САПФИР"
            Language.UA -> "САПФІР"
            Language.KK -> "САПФИР"
            Language.DE -> "SAPHIR"
            Language.ZH -> "蓝宝石"
            else -> "SAPPHIRE"
        }
        "steampunk" -> when (lang) {
            Language.RU -> "ЛАТУНЬ"
            Language.UA -> "ЛАТУНЬ"
            Language.KK -> "ЖЕЗ"
            Language.DE -> "MESSING"
            Language.ZH -> "黄铜"
            else -> "BRASS"
        }
        "red_gradient" -> when (lang) {
            Language.RU -> "КРАСНЫЙ ПУЛЬС"
            Language.UA -> "ЧЕРВОНИЙ ПУЛЬС"
            Language.KK -> "ҚЫЗЫЛ ПУЛЬС"
            Language.DE -> "ROTER IMPULS"
            Language.ZH -> "赤红脉冲"
            else -> "RED PULSE"
        }
        "green_gradient" -> when (lang) {
            Language.RU -> "ИЗУМРУДНЫЙ ПУЛЬС"
            Language.UA -> "СМАРАГДОВИЙ ПУЛЬС"
            Language.KK -> "ЗҮМРӘТ ПУЛЬС"
            Language.DE -> "SMARAGD IMPULS"
            Language.ZH -> "翡翠脉冲"
            else -> "EMERALD PULSE"
        }
        "blue_gradient" -> when (lang) {
            Language.RU -> "САПФИРОВЫЙ ПУЛЬС"
            Language.UA -> "САПФІРОВИЙ ПУЛЬС"
            Language.KK -> "САПФИР ПУЛЬС"
            Language.DE -> "SAPHIR IMPULS"
            Language.ZH -> "蓝宝石脉冲"
            else -> "SAPPHIRE PULSE"
        }
        "purple_gradient" -> when (lang) {
            Language.RU -> "АМЕТИСТОВЫЙ ПУЛЬС"
            Language.UA -> "АМЕТИСТОВИЙ ПУЛЬС"
            Language.KK -> "АМЕТИСТ ПУЛЬС"
            Language.DE -> "AMETHYST IMPULS"
            Language.ZH -> "紫晶脉冲"
            else -> "AMETHYST PULSE"
        }
        else -> cubeSkinId.uppercase()
    }

    fun getLocalizedCubeSkinDesc(cubeSkinId: String, lang: Language): String = when (cubeSkinId) {
        "neon" -> when (lang) {
            Language.RU -> "Яркие фотонные грани с белым контуром"
            Language.UA -> "Яскраві фотонні грані з білим контуром"
            Language.KK -> "Ақ контурлы жарқын фотон қырлары"
            Language.DE -> "Leuchtende Photon-Kanten mit weißem Umriss"
            Language.ZH -> "明亮白边高对比光子发光方块"
            else -> "Vibrant photon edges with white glow lines"
        }
        "glass" -> when (lang) {
            Language.RU -> "Стеклянные плитки с эффектом матового размытия"
            Language.UA -> "Скляні плитки з ефектом матового розмиття"
            Language.KK -> "Күңгірт бұлыңғыр шыны тақталар"
            Language.DE -> "Mattierte Kacheln im Weltraumglas-Stil"
            Language.ZH -> "半透明磨砂质感玻璃方块"
            else -> "Frosted tinted space glass with back-glare"
        }
        "retro" -> when (lang) {
            Language.RU -> "Контрастные концентрические узоры в стиле 80-х"
            Language.UA -> "Контрастні концентричні візерунки у стилі 80-х"
            Language.KK -> "80-жылдар стиліндегі контрасты өрнектер"
            Language.DE -> "80er-Jahre Konsolen-Muster"
            Language.ZH -> "80年代经典街机同心圆纹理"
            else -> "Concentric retro console styles from the 80s"
        }
        "flat" -> when (lang) {
            Language.RU -> "Минималистичный чистый плоский стиль блоков"
            Language.UA -> "Мінімалістичний чистий плоский стиль блоків"
            Language.KK -> "Блоктардың қарапайым таза тегіс стилі"
            Language.DE -> "Schlichtes minimalistisches Flachdesign"
            Language.ZH -> "利落明快的单色极简架构"
            else -> "Sleek low-footprint solid layout with sharp edges"
        }
        "material" -> when (lang) {
            Language.RU -> "Скругленные блоки Material с мягким градиентом"
            Language.UA -> "Скруглені блоки Material з м'яким градієнтом"
            Language.KK -> "Жұмсақ градиентті дөңгеленген блоктар"
            Language.DE -> "Abgerundete 3D-Material-Kacheln mit Farbverlauf"
            Language.ZH -> "自然柔和圆角与三维渐变质感"
            else -> "Organic rounded Material Design custom 3D tiles"
        }
        "glowing_jewel" -> when (lang) {
            Language.RU -> "Ограненные сапфировые плиты с внутренним свечением"
            Language.UA -> "Огранені сапфірові плити з внутрішнім світінням"
            Language.KK -> "Ішкі жарығы бар сапфир тақталары"
            Language.DE -> "Geschliffener Juwelen-Stil mit Innenleuchten"
            Language.ZH -> "晶莹剔透的奢华内发光宝石方块"
            else -> "Chiseled luxury jewel design with internal raytracing"
        }
        "steampunk" -> when (lang) {
            Language.RU -> "Латунные блоки с шестеренками и заклепками"
            Language.UA -> "Латунні блоки з шестернями та заклепками"
            Language.KK -> "Тісті доңғалақтар мен тойтармалары бар жез блоктар"
            Language.DE -> "Schwere Messingblöcke mit Zahnrädern und Nieten"
            Language.ZH -> "复古黄铜机械齿轮与铆钉美学"
            else -> "Heavy brass gears and rivets industrial aesthetic"
        }
        "red_gradient" -> when (lang) {
            Language.RU -> "Пульсирующий красный градиент реликвии"
            Language.UA -> "Пульсуючий червоний градієнт реліквії"
            Language.KK -> "Реликттің пульсацияланатын қызыл градиенті"
            Language.DE -> "Pulsierender roter Relikt-Farbverlauf"
            Language.ZH -> "动态脉冲赤红遗物光效"
            else -> "Pulsing red relic gradient glow"
        }
        "green_gradient" -> when (lang) {
            Language.RU -> "Пульсирующий изумрудный градиент реликвии"
            Language.UA -> "Пульсуючий смарагдовий градієнт реліквії"
            Language.KK -> "Реликттің пульсацияланатын зүмрәт градиенті"
            Language.DE -> "Pulsierender smaragdgrüner Relikt-Farbverlauf"
            Language.ZH -> "动态脉冲翡翠遗物光效"
            else -> "Pulsing emerald relic gradient glow"
        }
        "blue_gradient" -> when (lang) {
            Language.RU -> "Пульсирующий сапфировый градиент реликвии"
            Language.UA -> "Пульсуючий сапфіровий градієнт реліквії"
            Language.KK -> "Реликттің пульсацияланатын сапфир градиенті"
            Language.DE -> "Pulsierender saphirblauer Relikt-Farbverlauf"
            Language.ZH -> "动态脉冲蓝宝石遗物光效"
            else -> "Pulsing sapphire relic gradient glow"
        }
        "purple_gradient" -> when (lang) {
            Language.RU -> "Пульсирующий аметистовый градиент реликвии"
            Language.UA -> "Пульсуючий аметистовий градієнт реліквії"
            Language.KK -> "Реликттің пульсацияланатын аметист градиенті"
            Language.DE -> "Pulsierender amethystener Relikt-Farbverlauf"
            Language.ZH -> "动态脉冲紫晶遗物光效"
            else -> "Pulsing amethyst relic gradient glow"
        }
        else -> ""
    }

    fun getLocalizedAvatarFrameTitle(frameId: String, lang: Language): String = when (frameId.lowercase()) {
        "standard" -> when (lang) {
            Language.RU -> "Базовая"
            Language.UA -> "Базова"
            Language.KK -> "Негізгі"
            Language.DE -> "Standard"
            Language.ZH -> "默认"
            else -> "Standard"
        }
        "frame_white" -> when (lang) {
            Language.RU -> "Белая"
            Language.UA -> "Біла"
            Language.KK -> "Ақ"
            Language.DE -> "Weiß"
            Language.ZH -> "白色"
            else -> "White"
        }
        "frame_blue" -> when (lang) {
            Language.RU -> "Синяя"
            Language.UA -> "Синя"
            Language.KK -> "Көк"
            Language.DE -> "Blau"
            Language.ZH -> "蓝色"
            else -> "Blue"
        }
        "frame_green" -> when (lang) {
            Language.RU -> "Зеленая"
            Language.UA -> "Зелена"
            Language.KK -> "Жасыл"
            Language.DE -> "Grün"
            Language.ZH -> "绿色"
            else -> "Green"
        }
        "frame_yellow" -> when (lang) {
            Language.RU -> "Желтая"
            Language.UA -> "Жовта"
            Language.KK -> "Сары"
            Language.DE -> "Gelb"
            Language.ZH -> "黄色"
            else -> "Yellow"
        }
        "frame_orange" -> when (lang) {
            Language.RU -> "Оранжевая"
            Language.UA -> "Помаранчева"
            Language.KK -> "Қызғылт сары"
            Language.DE -> "Orange"
            Language.ZH -> "橙色"
            else -> "Orange"
        }
        "frame_red" -> when (lang) {
            Language.RU -> "Красная"
            Language.UA -> "Червона"
            Language.KK -> "Қызыл"
            Language.DE -> "Rot"
            Language.ZH -> "红色"
            else -> "Red"
        }
        "frame_purple" -> when (lang) {
            Language.RU -> "Фиолетовая"
            Language.UA -> "Фіолетова"
            Language.KK -> "Күлгін"
            Language.DE -> "Lila"
            Language.ZH -> "紫色"
            else -> "Purple"
        }
        "frame_dark" -> when (lang) {
            Language.RU -> "Темная"
            Language.UA -> "Темна"
            Language.KK -> "Қараңғы"
            Language.DE -> "Dunkel"
            Language.ZH -> "深暗"
            else -> "Dark"
        }
        "chrono_gl", "gradient_frame" -> when (lang) {
            Language.RU -> "Градиент"
            Language.UA -> "Градієнт"
            Language.KK -> "Градиент"
            Language.DE -> "Farbverlauf"
            Language.ZH -> "动态渐变"
            else -> "Gradient"
        }
        // Legacy fallbacks
        "neon_frame" -> "Бирюза"
        "gold_frame" -> "Золото"
        "cyber_frame" -> "Фиолет"
        "fire_frame" -> "Пламя"
        "ice_frame" -> "Ледник"
        "matrix_frame" -> "Матрица"
        "galaxy_frame" -> "Галактика"
        "rainbow_frame" -> "Радуга"
        else -> frameId
    }

    fun getLocalizedAvatarFrameDesc(frameId: String, lang: Language): String = when (frameId.lowercase()) {
        "standard" -> when (lang) {
            Language.RU -> "Базовая классическая рамка"
            Language.UA -> "Базова класична рамка"
            Language.KK -> "Классикалық негізгі жақтау"
            Language.DE -> "Klassischer dezenter Rahmen"
            Language.ZH -> "经典微质感相框"
            else -> "Classic subtle frame"
        }
        "frame_white" -> when (lang) {
            Language.RU -> "Минималистичный белый контур"
            Language.UA -> "Мінімалістичний білий контур"
            Language.KK -> "Минималистік ақ контур"
            Language.DE -> "Minimalistischer weißer Rahmen"
            Language.ZH -> "简约白色轮廓相框"
            else -> "Minimalist white outline"
        }
        "frame_blue" -> when (lang) {
            Language.RU -> "Классический синий контур"
            Language.UA -> "Класичний синій контур"
            Language.KK -> "Классикалық көк контур"
            Language.DE -> "Klassischer blauer Rahmen"
            Language.ZH -> "经典蓝色轮廓相框"
            else -> "Classic blue outline"
        }
        "frame_green" -> when (lang) {
            Language.RU -> "Свежий зеленый контур"
            Language.UA -> "Свіжий зелений контур"
            Language.KK -> "Жаңа жасыл контур"
            Language.DE -> "Frischer grüner Rahmen"
            Language.ZH -> "清新绿色轮廓相框"
            else -> "Fresh green outline"
        }
        "frame_yellow" -> when (lang) {
            Language.RU -> "Яркий желтый контур"
            Language.UA -> "Яскравий жовтий контур"
            Language.KK -> "Жарық сары контур"
            Language.DE -> "Heller gelber Rahmen"
            Language.ZH -> "明亮黄色轮廓相框"
            else -> "Bright yellow outline"
        }
        "frame_orange" -> when (lang) {
            Language.RU -> "Теплый оранжевый контур"
            Language.UA -> "Теплий помаранчевий контур"
            Language.KK -> "Жылы қызғылт сары контур"
            Language.DE -> "Warmer orangefarbener Rahmen"
            Language.ZH -> "温暖橙色轮廓相框"
            else -> "Warm orange outline"
        }
        "frame_red" -> when (lang) {
            Language.RU -> "Насыщенный красный контур"
            Language.UA -> "Насичений червоний контур"
            Language.KK -> "Қанық қызыл контур"
            Language.DE -> "Kräftiger roter Rahmen"
            Language.ZH -> "热烈红色轮廓相框"
            else -> "Vivid red outline"
        }
        "frame_purple" -> when (lang) {
            Language.RU -> "Глубокий фиолетовый контур"
            Language.UA -> "Глибокий фіолетовий контур"
            Language.KK -> "Терең күлгін контур"
            Language.DE -> "Tiefer violetter Rahmen"
            Language.ZH -> "深邃紫色轮廓相框"
            else -> "Deep purple outline"
        }
        "frame_dark" -> when (lang) {
            Language.RU -> "Строгий темный контур"
            Language.UA -> "Строгий темний контур"
            Language.KK -> "Қатал қараңғы контур"
            Language.DE -> "Eleganter dunkler Rahmen"
            Language.ZH -> "沉稳暗色轮廓相框"
            else -> "Sleek dark outline"
        }
        "chrono_gl", "gradient_frame", "neon_ae", "gold_ma", "omega_ti" -> when (lang) {
            Language.RU -> "Динамическая радужная рамка с переливом"
            Language.UA -> "Динамічна веселкова рамка з переливом"
            Language.KK -> "Динамикалық кемпірқосақ жақтау"
            Language.DE -> "Dynamischer Regenbogen-Farbverlauf"
            Language.ZH -> "动态全彩流光渐变相框"
            else -> "Dynamic rainbow gradient frame"
        }
        else -> ""
    }

    fun getLocalizedButtonTitle(btnStyleId: String, lang: Language): String = when (btnStyleId) {
        "classic" -> when (lang) {
            Language.RU -> "Классический"
            Language.UA -> "Класичний"
            Language.KK -> "Классикалық"
            Language.DE -> "Klassisch Solide"
            Language.ZH -> "经典质感"
            else -> "Classic Solid"
        }
        "neon" -> when (lang) {
            Language.RU -> "Фотонный"
            Language.UA -> "Фотонний"
            Language.KK -> "Фотон"
            Language.DE -> "Photon"
            Language.ZH -> "光子"
            else -> "Photon"
        }
        "glass" -> when (lang) {
            Language.RU -> "Матовое стекло"
            Language.UA -> "Матове скло"
            Language.KK -> "Күңгірт шыны"
            Language.DE -> "Mattiertes Glas"
            Language.ZH -> "磨砂玻璃"
            else -> "Frosted Glass"
        }
        "gold_legendary", "gold" -> when (lang) {
            Language.RU -> "Золото"
            Language.UA -> "Золото"
            Language.KK -> "Алтын"
            Language.DE -> "Gold"
            Language.ZH -> "黄金传奇"
            else -> "Gold"
        }
        "plasma_legendary", "plasma" -> when (lang) {
            Language.RU -> "Плазма"
            Language.UA -> "Плазма"
            Language.KK -> "Плазма"
            Language.DE -> "Plasma"
            Language.ZH -> "等离子体"
            else -> "Plasma"
        }
        else -> btnStyleId
    }

    fun getLocalizedButtonDesc(btnStyleId: String, lang: Language): String = when (btnStyleId) {
        "classic" -> when (lang) {
            Language.RU -> "Стандартные непрозрачные кнопки с тенью"
            Language.UA -> "Стандартні непрозорі кнопки з тінню"
            Language.KK -> "Көлеңкесі бар стандартты батырмалар"
            Language.DE -> "Solide Standardtasten mit Schatten"
            Language.ZH -> "带有实体阴影的经典按钮"
            else -> "Solid material-design buttons with shadow"
        }
        "neon" -> when (lang) {
            Language.RU -> "Кнопки с ярким светящимся фотонным контуром"
            Language.UA -> "Кнопки з яскравим світним фотонним контуром"
            Language.KK -> "Жарқыраған фотон контурлы батырмалар"
            Language.DE -> "Leuchtender Photon-Rand mit transparentem Hintergrund"
            Language.ZH -> "半透明背景搭配发光光子边缘"
            else -> "Glowing photon border with transparent background"
        }
        "glass" -> when (lang) {
            Language.RU -> "Полупрозрачные кнопки с эффектом размытия"
            Language.UA -> "Напівпрозорі кнопки з ефектом розмиття"
            Language.KK -> "Бұлыңғыр жартылай мөлдір батырмалар"
            Language.DE -> "Halbtransparente Tasten im modernen Glas-Look"
            Language.ZH -> "现代半透明磨砂质感玻璃按键"
            else -> "Semi-transparent modern glassmorphic look"
        }
        "gold_legendary", "gold" -> when (lang) {
            Language.RU -> "Золотой градиент и мягкое свечение"
            Language.UA -> "Золотий градієнт і м'яке свічення"
            Language.KK -> "Алтын градиент және жұмсақ жарқыл"
            Language.DE -> "Goldener Farbverlauf mit Glanz"
            Language.ZH -> "奢华流金渐变与柔和光效"
            else -> "Golden gradient with soft glow"
        }
        "plasma_legendary", "plasma" -> when (lang) {
            Language.RU -> "Фиолетово-голубой плазменный импульс"
            Language.UA -> "Фіолетово-блакитний плазмовий імпульс"
            Language.KK -> "Күлгін-көгілдір плазмалық импульс"
            Language.DE -> "Plasma-Impuls in Violett-Blau"
            Language.ZH -> "紫蓝等离子能量脉冲质感"
            else -> "Violet-blue plasma pulse"
        }
        else -> ""
    }

    fun getLocalizedBadgeDesc(badgeId: String, lang: Language): String = when (badgeId) {
        "none" -> when (lang) {
            Language.RU -> "Стандартный вид без звания"
            Language.UA -> "Стандартний вигляд без звання"
            Language.KK -> "Атақсыз стандартты көрініс"
            Language.DE -> "Standardanzeige ohne Titel"
            Language.ZH -> "标准状态，不佩戴任何称号"
            else -> "Standard appearance without title"
        }
        "node" -> when (lang) {
            Language.RU -> "Начальный статус бойца арены"
            Language.UA -> "Початковий статус бійця арени"
            Language.KK -> "Арена сарбазының бастапқы мәртебесі"
            Language.DE -> "Status als Arena-Rekrut"
            Language.ZH -> "初入战场的竞技新手称号"
            else -> "Starter rank status on the arena"
        }
        "lord" -> when (lang) {
            Language.RU -> "Опытный ветеран многих сражений"
            Language.UA -> "Досвідчений ветеран багатьох битв"
            Language.KK -> "Көптеген шайқастардың ардагері"
            Language.DE -> "Erfahrener Veteran vieler Schlachten"
            Language.ZH -> "经历多次激烈对战的资深老将"
            else -> "Battle-tested seasoned veteran"
        }
        "cosmic_overlord" -> when (lang) {
            Language.RU -> "Элитный мастер тактического боя"
            Language.UA -> "Елітний майстер тактичного бою"
            Language.KK -> "Тактикалық ұрыстың элиталық шебері"
            Language.DE -> "Elite-Meister taktischer Kämpfe"
            Language.ZH -> "顶尖高手专属精英战术称号"
            else -> "Elite master of tactical warfare"
        }
        "ai_consensus" -> when (lang) {
            Language.RU -> "Титул опытного профессионала"
            Language.UA -> "Титул досвідченого професіонала"
            Language.KK -> "Тәжірибелі кәсіпқой атағы"
            Language.DE -> "Titel für erfahrene Profis"
            Language.ZH -> "资深职业玩家称号"
            else -> "Title of experienced pro"
        }
        else -> ""
    }

    fun getLocalizedAchievementBadge(iconType: String, achId: String, lang: Language): String {
        return when (iconType) {
            "all_unlocked", "crown" -> when (lang) {
                Language.RU -> "ЧЕМПИОН"
                Language.UA -> "ЧЕМПІОН"
                Language.KK -> "ЧЕМПИОН"
                Language.DE -> "CHAMPION"
                Language.ZH -> "王者荣耀"
                else -> "CHAMPION"
            }
            "lines" -> when (lang) {
                Language.RU -> "МАСТЕР ЛИНИЙ"
                Language.UA -> "МАЙСТЕР ЛІНІЙ"
                Language.KK -> "ҚАТАР ШЕБЕРІ"
                Language.DE -> "LINIEN-MEISTER"
                Language.ZH -> "消行大师"
                else -> "LINE CLEAR MASTER"
            }
            "score" -> when (lang) {
                Language.RU -> "ЧЕМПИОН ПО ОЧКАМ"
                Language.UA -> "ЧЕМПІОН ЗА ОЧКАМИ"
                Language.KK -> "ҰПАЙ ЧЕМПИОНЫ"
                Language.DE -> "PUNKTE-CHAMPION"
                Language.ZH -> "积分之王"
                else -> "HIGH SCORE CHAMPION"
            }
            "speed" -> when (lang) {
                Language.RU -> "СКОРОСТНОЙ РЕКОРДСМЕН"
                Language.UA -> "ШВИДКІСНИЙ РЕКОРДСМЕН"
                Language.KK -> "ЖЫЛДАМДЫҚ ШЕБЕРІ"
                Language.DE -> "GESCHWINDIGKEITS-MEISTER"
                Language.ZH -> "极速极限先锋"
                else -> "SPEED MASTER"
            }
            "blast" -> when (lang) {
                Language.RU -> "ЭКСПЕРТ ZETA"
                Language.UA -> "ЕКСПЕРТ ZETA"
                Language.KK -> "ZETA САРАПШЫСЫ"
                Language.DE -> "ZETA-EXPERTE"
                Language.ZH -> "ZETA 战术专家"
                else -> "ZETA EXPERT"
            }
            "combo" -> when (lang) {
                Language.RU -> "КОМБО-ЭКСПЕРТ"
                Language.UA -> "КОМБО-ЕКСПЕРТ"
                Language.KK -> "КОМБО САРАПШЫСЫ"
                Language.DE -> "KOMBO-EXPERTE"
                Language.ZH -> "连击大师"
                else -> "COMBO EXPERT"
            }
            else -> when (lang) {
                Language.RU -> "ИГРОВОЙ АКТИВ"
                Language.UA -> "ІГРОВИЙ АКТИВ"
                Language.KK -> "ОЙЫН АКТИВІ"
                Language.DE -> "AKTIVES MITGLIED"
                Language.ZH -> "活跃精英"
                else -> "ACTIVE PEER"
            }
        }
    }

    fun getLobbyFilter(key: String, lang: Language): String = when (key.uppercase()) {
        "ALL" -> when (lang) {
            Language.RU -> "Все комнаты"
            Language.UA -> "Усі кімнати"
            Language.KK -> "Барлық бөлмелер"
            Language.DE -> "Alle Räume"
            Language.ZH -> "全部房间"
            else -> "All Rooms"
        }
        "OPEN" -> when (lang) {
            Language.RU -> "Открытые"
            Language.UA -> "Відкриті"
            Language.KK -> "Ашық"
            Language.DE -> "Offen"
            Language.ZH -> "公开房间"
            else -> "Open"
        }
        "LOCKED" -> when (lang) {
            Language.RU -> "С паролем"
            Language.UA -> "З паролем"
            Language.KK -> "Құпия сөзбен"
            Language.DE -> "Passwort"
            Language.ZH -> "带密码"
            else -> "Locked"
        }
        else -> key
    }

    fun getLobbyModeTitle(modeId: String, lang: Language): String = when (modeId.uppercase()) {
        "CLASSIC" -> when (lang) {
            Language.RU -> "Битва (с атаками)"
            Language.UA -> "Битва (з атаками)"
            Language.KK -> "Шайқас (шабуылдармен)"
            Language.DE -> "Kampf (mit Angriffen)"
            Language.ZH -> "垃圾行对决 (进攻模式)"
            else -> "Battle (Garbage Duel)"
        }
        "SCORE_RACE" -> when (lang) {
            Language.RU -> "Классика (на очки, без атак)"
            Language.UA -> "Класика (на очки, без атак)"
            Language.KK -> "Классика (ұпайға, шабуылсыз)"
            Language.DE -> "Klassisch (Punkterennen)"
            Language.ZH -> "纯净经典 (积分竞速)"
            else -> "Pure Classic (Score Race)"
        }
        "SPRINT" -> when (lang) {
            Language.RU -> "Спринт 40 линий"
            Language.UA -> "Спринт 40 ліній"
            Language.KK -> "40 қатар спринті"
            Language.DE -> "Sprint 40 Linien"
            Language.ZH -> "40行极限竞速"
            else -> "Sprint 40 Lines"
        }
        "BLITZ" -> when (lang) {
            Language.RU -> "Блиц 2 минуты"
            Language.UA -> "Бліц 2 хвилини"
            Language.KK -> "2 минуттық блиц"
            Language.DE -> "Blitz 2 Minuten"
            Language.ZH -> "2分钟闪电战"
            else -> "Blitz 2 Minutes"
        }
        "HYPER", "FAST_RUN" -> when (lang) {
            Language.RU -> "Спринт"
            Language.UA -> "Спринт"
            Language.KK -> "Спринт"
            Language.DE -> "Sprint"
            Language.ZH -> "冲刺"
            else -> "Sprint"
        }
        "MIRROR" -> when (lang) {
            Language.RU -> "Зеркало"
            Language.UA -> "Дзеркало"
            Language.KK -> "Айна"
            Language.DE -> "Spiegel"
            Language.ZH -> "镜界"
            else -> "Mirror"
        }
        "EXTENDED" -> when (lang) {
            Language.RU -> "Расширенный"
            Language.UA -> "Розширений"
            Language.KK -> "Кеңейтілген"
            Language.DE -> "Erweitert"
            Language.ZH -> "扩展"
            else -> "Extended"
        }
        "REVERSE" -> when (lang) {
            Language.RU -> "Инверсия"
            Language.UA -> "Інверсія"
            Language.KK -> "Инверсия"
            Language.DE -> "Inversion"
            Language.ZH -> "反转"
            else -> "Inversion"
        }
        "BLOCK_BLAST" -> when (lang) {
            Language.RU -> "Арена"
            Language.UA -> "Арена"
            Language.KK -> "Арена"
            Language.DE -> "Arena"
            Language.ZH -> "擂台"
            else -> "Arena"
        }
        "TIME_ATTACK" -> when (lang) {
            Language.RU -> "Блиц"
            Language.UA -> "Бліц"
            Language.KK -> "Блиц"
            Language.DE -> "Blitz"
            Language.ZH -> "闪击"
            else -> "Blitz"
        }
        "RELAX" -> when (lang) {
            Language.RU -> "Песочница"
            Language.UA -> "Пісочниця"
            Language.KK -> "Құмсалғыш"
            Language.DE -> "Sandbox"
            Language.ZH -> "沙盒"
            else -> "Sandbox"
        }
        "PERFECTIONIST" -> when (lang) {
            Language.RU -> "Идеал"
            Language.UA -> "Ідеал"
            Language.KK -> "Мінсіз"
            Language.DE -> "Perfektion"
            Language.ZH -> "完美"
            else -> "Perfection"
        }
        "PATTERN", "PATTERN_PUZZLE" -> when (lang) {
            Language.RU -> "Шаблон"
            Language.UA -> "Шаблон"
            Language.KK -> "Үлгі"
            Language.DE -> "Muster"
            Language.ZH -> "图形模板"
            else -> "Blueprint"
        }
        else -> modeId
    }

    fun getLocalizedControlPresetName(preset: String, lang: Language): String = when (preset) {
        "classic" -> when (lang) {
            Language.RU -> "Классика"
            Language.UA -> "Класика"
            Language.KK -> "Классика"
            Language.DE -> "Klassisch"
            Language.ZH -> "经典布局"
            else -> "Classic Bar"
        }
        "split" -> when (lang) {
            Language.RU -> "Сплит (2 пальца)"
            Language.UA -> "Спліт (2 пальці)"
            Language.KK -> "Сплит (2 саусақ)"
            Language.DE -> "Split (2 Daumen)"
            Language.ZH -> "双拇指分体"
            else -> "Split (Two Thumbs)"
        }
        "arcade" -> when (lang) {
            Language.RU -> "Аркада (D-Pad)"
            Language.UA -> "Аркада (D-Pad)"
            Language.KK -> "Аркада (D-Pad)"
            Language.DE -> "Arcade (D-Pad)"
            Language.ZH -> "街机十字键"
            else -> "Arcade D-Pad"
        }
        "one_hand_right" -> when (lang) {
            Language.RU -> "Правая рука"
            Language.UA -> "Права рука"
            Language.KK -> "Оң қол"
            Language.DE -> "Rechte Hand"
            Language.ZH -> "右手单手"
            else -> "One-Hand Right"
        }
        "one_hand_left" -> when (lang) {
            Language.RU -> "Левая рука"
            Language.UA -> "Ліва рука"
            Language.KK -> "Сол қол"
            Language.DE -> "Linke Hand"
            Language.ZH -> "左手单手"
            else -> "One-Hand Left"
        }
        "claw_pro" -> when (lang) {
            Language.RU -> "Claw Pro (6 клавиш)"
            Language.UA -> "Claw Pro (6 клавіш)"
            Language.KK -> "Claw Pro (6 перне)"
            Language.DE -> "Claw Pro (6 Tasten)"
            Language.ZH -> "竞技6键矩阵"
            else -> "Claw Pro (6-Key)"
        }
        else -> preset
    }

    fun getLobbyModeDesc(modeId: String, lang: Language): String = when (modeId.uppercase()) {
        "CLASSIC" -> when (lang) {
            Language.RU -> "Дуэль на выбывание с атаками мусорными линиями"
            Language.UA -> "Дуель на вибування з атаками сміттєвими лініями"
            Language.KK -> "Қоқыс қатарларымен шабуыл жасайтын дуэль"
            Language.DE -> "K.O.-Duell mit Müllzeilen-Angriffen"
            Language.ZH -> "消除行将向对手发送干扰行直至淘汰"
            else -> "Knockout duel with garbage line attacks"
        }
        "SCORE_RACE" -> when (lang) {
            Language.RU -> "Классический тетрис без атак, победа по очкам/выживанию"
            Language.UA -> "Класичний тетріс без атак, перемога за очками/виживанням"
            Language.KK -> "Шабуылсыз классикалық тетрис, ұпай немесе аман қалу арқылы жеңіс"
            Language.DE -> "Klassisches Spiel ohne Angriffe, Sieg nach Punkten"
            Language.ZH -> "无干扰纯净对决，依最高分或存活时间定胜负"
            else -> "Pure classic mode with no attacks, highest score wins"
        }
        "SPRINT" -> when (lang) {
            Language.RU -> "Кто быстрее очистит 40 линий"
            Language.UA -> "Хто швидше очистить 40 ліній"
            Language.KK -> "Кім 40 қатарды тезірек тазалайды"
            Language.DE -> "Wer zuerst 40 Linien abräumt"
            Language.ZH -> "率先消除40行的玩家获得胜利"
            else -> "First to clear 40 lines"
        }
        "BLITZ" -> when (lang) {
            Language.RU -> "Набор максимального счета за 120 сек"
            Language.UA -> "Набір максимального рахунку за 120 сек"
            Language.KK -> "120 секундта ең көп ұпай жинау"
            Language.DE -> "Höchste Punktzahl innerhalb von 120 Sek"
            Language.ZH -> "在120秒限时内尽可能斩获最高积分"
            else -> "Highest score within 120s"
        }
        "HYPER", "FAST_RUN" -> when (lang) {
            Language.RU -> "Экстремальное ускорение падения блоков"
            Language.UA -> "Екстремальне прискорення падіння блоків"
            Language.KK -> "Блоктардың құлау жылдамдығын шекті арттыру"
            Language.DE -> "Extreme Fallgeschwindigkeit der Blöcke"
            Language.ZH -> "极速重力坠落，考验极限反应力"
            else -> "High gravity drop speed"
        }
        "MIRROR" -> when (lang) {
            Language.RU -> "Инвертированное отражение игрового поля"
            Language.UA -> "Інвертоване відображення ігрового поля"
            Language.KK -> "Ойын алаңының инверттелген айналық көрінісі"
            Language.DE -> "Spiegelverkehrtes Spielfeld für Profis"
            Language.ZH -> "左右水平镜像对称的颠覆性视野挑战"
            else -> "Horizontally flipped gameplay challenge"
        }
        "PERFECTIONIST" -> when (lang) {
            Language.RU -> "Режим абсолютной точности и мастерства"
            Language.UA -> "Режим абсолютної точності та майстерності"
            Language.KK -> "Абсолютті дәлдік пен шеберлік режимі"
            Language.DE -> "Modus für absolute Präzision und Meisterschaft"
            Language.ZH -> "极致精准度与最高技巧要求的无瑕挑战模式"
            else -> "Ultimate precision and mastery mode"
        }
        "PATTERN", "PATTERN_PUZZLE" -> when (lang) {
            Language.RU -> "Головоломка: заполните неоновый шаблон фигурами"
            Language.UA -> "Головоломка: заповніть неоновий шаблон фігурами"
            Language.KK -> "Басқатырғыш: неонды үлгіні фигуралармен толтырыңыз"
            Language.DE -> "Puzzle: Fülle die Neonschablone mit Steinen"
            Language.ZH -> "全息拼图解密：用方块精准填满图形轮廓"
            else -> "Puzzle: Fill the neon blueprint stencil with pieces"
        }
        else -> ""
    }

    fun getLobbySeriesFormat(rounds: Int, lang: Language): String = when (rounds) {
        1 -> when (lang) {
            Language.RU -> "1 раунд"
            Language.UA -> "1 раунд"
            Language.KK -> "1 раунд"
            Language.DE -> "1 Runde"
            Language.ZH -> "单局决胜 (1局)"
            else -> "1 round"
        }
        3 -> when (lang) {
            Language.RU -> "До 2 побед"
            Language.UA -> "До 2 перемог"
            Language.KK -> "2 жеңіске дейін"
            Language.DE -> "Bis 2 Siege"
            Language.ZH -> "三局两胜 (BO3)"
            else -> "First to 2"
        }
        5 -> when (lang) {
            Language.RU -> "До 3 побед"
            Language.UA -> "До 3 перемог"
            Language.KK -> "3 жеңіске дейін"
            Language.DE -> "Bis 3 Siege"
            Language.ZH -> "五局三胜 (BO5)"
            else -> "First to 3"
        }
        else -> "BO$rounds"
    }

    fun getLobbyGarbageIntensity(intensity: Float, lang: Language): String = when {
        intensity <= 0.01f -> when (lang) {
            Language.RU -> "Выкл"
            Language.UA -> "Вимк"
            Language.KK -> "Өшірулі"
            Language.DE -> "Aus"
            Language.ZH -> "关闭"
            else -> "Off"
        }
        intensity <= 0.6f -> when (lang) {
            Language.RU -> "Слабо"
            Language.UA -> "Слабко"
            Language.KK -> "Баяу"
            Language.DE -> "Leicht"
            Language.ZH -> "轻度"
            else -> "Low"
        }
        intensity <= 1.1f -> when (lang) {
            Language.RU -> "Норма"
            Language.UA -> "Норма"
            Language.KK -> "Қалыпты"
            Language.DE -> "Normal"
            Language.ZH -> "标准"
            else -> "Norm"
        }
        intensity <= 1.6f -> when (lang) {
            Language.RU -> "Хард"
            Language.UA -> "Хард"
            Language.KK -> "Қиын"
            Language.DE -> "Schwer"
            Language.ZH -> "强力"
            else -> "Hard"
        }
        else -> when (lang) {
            Language.RU -> "Хаос"
            Language.UA -> "Хаос"
            Language.KK -> "Хаос"
            Language.DE -> "Chaos"
            Language.ZH -> "疯狂"
            else -> "Chaos"
        }
    }

    fun getStoreCategoryTitle(catKey: String, lang: Language): String = when (catKey.uppercase()) {
        "ALL" -> when (lang) {
            Language.RU -> "Все"
            Language.UA -> "Усі"
            Language.KK -> "Барлығы"
            Language.DE -> "Alle"
            Language.ZH -> "全部"
            else -> "All"
        }
        "FRAMES" -> when (lang) {
            Language.RU -> "Рамки"
            Language.UA -> "Рамки"
            Language.KK -> "Жектеулер"
            Language.DE -> "Rahmen"
            Language.ZH -> "头像框"
            else -> "Frames"
        }
        "TITLES" -> when (lang) {
            Language.RU -> "Титулы"
            Language.UA -> "Титули"
            Language.KK -> "Атақтар"
            Language.DE -> "Titel"
            Language.ZH -> "专属称号"
            else -> "Titles"
        }
        "SKINS" -> when (lang) {
            Language.RU -> "Сетка"
            Language.UA -> "Сітка"
            Language.KK -> "Тор"
            Language.DE -> "Gitter"
            Language.ZH -> "棋盘网格"
            else -> "Grid"
        }
        "BLOCKS" -> when (lang) {
            Language.RU -> "Блоки"
            Language.UA -> "Блоки"
            Language.KK -> "Блоктар"
            Language.DE -> "Blöcke"
            Language.ZH -> "方块质感"
            else -> "Blocks"
        }
        "BUTTONS" -> when (lang) {
            Language.RU -> "Кнопки"
            Language.UA -> "Кнопки"
            Language.KK -> "Батырмалар"
            Language.DE -> "Tasten"
            Language.ZH -> "按键外观"
            else -> "Buttons"
        }
        "MODES" -> when (lang) {
            Language.RU -> "Режимы"
            Language.UA -> "Режими"
            Language.KK -> "Режимдер"
            Language.DE -> "Modi"
            Language.ZH -> "特殊模式"
            else -> "Modes"
        }
        "RANKS" -> when (lang) {
            Language.RU -> "Ранги"
            Language.UA -> "Ранги"
            Language.KK -> "Дәрежелер"
            Language.DE -> "Ränge"
            Language.ZH -> "天梯段位"
            else -> "Ranks"
        }
        "TAGS" -> when (lang) {
            Language.RU -> "Теги"
            Language.UA -> "Теги"
            Language.KK -> "Тегтер"
            Language.DE -> "Tags"
            Language.ZH -> "个性标签"
            else -> "Tags"
        }
        "PRESTIGE" -> when (lang) {
            Language.RU -> "Престиж"
            Language.UA -> "Престиж"
            Language.KK -> "Престиж"
            Language.DE -> "Prestige"
            Language.ZH -> "声望转生"
            else -> "Prestige"
        }
        else -> catKey
    }

    fun getStoreRarity(cost: Int, lang: Language): String = when {
        cost <= 0 -> when (lang) {
            Language.RU -> "БАЗОВЫЙ"
            Language.UA -> "БАЗОВИЙ"
            Language.KK -> "НЕГІЗГІ"
            Language.DE -> "GEWÖHNLICH"
            Language.ZH -> "基础"
            else -> "COMMON"
        }
        cost <= 400 -> when (lang) {
            Language.RU -> "РЕДКИЙ"
            Language.UA -> "РІДКІСНИЙ"
            Language.KK -> "СИРЕК"
            Language.DE -> "SELTEN"
            Language.ZH -> "稀有"
            else -> "RARE"
        }
        cost <= 800 -> when (lang) {
            Language.RU -> "ЭПИЧЕСКИЙ"
            Language.UA -> "ЕПІЧНИЙ"
            Language.KK -> "ЭПИКАЛЫҚ"
            Language.DE -> "EPISCH"
            Language.ZH -> "史诗"
            else -> "EPIC"
        }
        else -> when (lang) {
            Language.RU -> "ЛЕГЕНДАРНЫЙ"
            Language.UA -> "ЛЕГЕНДАРНИЙ"
            Language.KK -> "АҢЫЗҒА АЙНАЛҒАН"
            Language.DE -> "LEGENDÄR"
            Language.ZH -> "传说"
            else -> "LEGENDARY"
        }
    }

    fun getLocalizedTitle(titleId: String, lang: Language): String = when (titleId) {
        "node" -> when (lang) {
            Language.RU -> "РЕКРУТ"
            Language.UA -> "РЕКРУТ"
            Language.KK -> "РЕКРУТ"
            Language.DE -> "REKRUT"
            Language.ZH -> "新兵"
            else -> "RECRUIT"
        }
        "lord" -> when (lang) {
            Language.RU -> "ВЕТЕРАН"
            Language.UA -> "ВЕТЕРАН"
            Language.KK -> "АРДАГЕР"
            Language.DE -> "VETERAN"
            Language.ZH -> "老兵"
            else -> "VETERAN"
        }
        "cosmic_overlord" -> when (lang) {
            Language.RU -> "ЭЛИТА"
            Language.UA -> "ЕЛІТА"
            Language.KK -> "ЭЛИТА"
            Language.DE -> "ELITE"
            Language.ZH -> "精英"
            else -> "ELITE"
        }
        "ai_consensus" -> when (lang) {
            Language.RU -> "ПРОФИ"
            Language.UA -> "ПРОФІ"
            Language.KK -> "ПРОФИ"
            Language.DE -> "PROFI"
            Language.ZH -> "职业专家"
            else -> "PRO"
        }
        else -> ""
    }

    fun getAuthError(err: String, lang: Language): String {
        return when (lang) {
            Language.RU -> when {
                err.contains("empty", true) || err.contains("пустые", true) || err.contains("пустой", true) -> "Заполните все поля!"
                err.contains("not found", true) || err.contains("найден", true) -> "Пользователь не найден!"
                err.contains("wrong", true) || err.contains("пароль", true) -> "Неверный пароль!"
                err.contains("already in use", true) || err.contains("занят", true) -> "Этот никнейм или почта уже используются!"
                err.contains("weak", true) -> "Слишком простой пароль (минимум 6 символов)!"
                err.contains("network", true) -> "Ошибка сети! Проверьте подключение."
                else -> err
            }
            Language.UA -> when {
                err.contains("empty", true) || err.contains("пустые", true) || err.contains("пустой", true) -> "Заповніть усі поля!"
                err.contains("not found", true) || err.contains("найден", true) -> "Користувача не знайдено!"
                err.contains("wrong", true) || err.contains("пароль", true) -> "Невірний пароль!"
                err.contains("already in use", true) || err.contains("занят", true) -> "Цей нікнейм або пошта вже використовуються!"
                err.contains("weak", true) -> "Занадто простий пароль (мінімум 6 символів)!"
                err.contains("network", true) -> "Помилка мережі! Перевірте з'єднання."
                else -> err
            }
            Language.KK -> when {
                err.contains("empty", true) || err.contains("пустые", true) || err.contains("пустой", true) -> "Барлық өрістерді толтырыңыз!"
                err.contains("not found", true) || err.contains("найден", true) -> "Пайдаланушы табылмады!"
                err.contains("wrong", true) || err.contains("пароль", true) -> "Құпия сөз қате!"
                err.contains("already in use", true) || err.contains("занят", true) -> "Бұл бүркеншік ат немесе пошта қолданыста!"
                err.contains("weak", true) -> "Құпия сөз тым қарапайым (кемінде 6 таңба)!"
                err.contains("network", true) -> "Желі қатесі! Байланысты тексеріңіз."
                else -> err
            }
            Language.DE -> when {
                err.contains("empty", true) || err.contains("пустые", true) || err.contains("пустой", true) -> "Bitte alle Felder ausfüllen!"
                err.contains("not found", true) || err.contains("найден", true) -> "Benutzer nicht gefunden!"
                err.contains("wrong", true) || err.contains("пароль", true) -> "Falsches Passwort!"
                err.contains("already in use", true) || err.contains("занят", true) -> "Name oder E-Mail bereits vergeben!"
                err.contains("weak", true) -> "Passwort zu schwach (mindestens 6 Zeichen)!"
                err.contains("network", true) -> "Netzwerkfehler! Verbindung prüfen."
                else -> err
            }
            Language.ZH -> when {
                err.contains("empty", true) || err.contains("пустые", true) || err.contains("пустой", true) -> "请填写所有必填项！"
                err.contains("not found", true) || err.contains("найден", true) -> "用户不存在！"
                err.contains("wrong", true) || err.contains("пароль", true) -> "密码错误！"
                err.contains("already in use", true) || err.contains("занят", true) -> "该昵称或邮箱已被使用！"
                err.contains("weak", true) -> "密码强度过低（至少6位）！"
                err.contains("network", true) -> "网络连接异常，请检查网络！"
                else -> err
            }
            else -> err
        }
    }

    fun getAuthSuccess(success: String, lang: Language): String {
        return when (lang) {
            Language.RU -> when {
                success.contains("created", true) || success.contains("создан", true) -> "Аккаунт успешно создан!"
                success.contains("success", true) || success.contains("вход", true) -> "Успешный вход!"
                else -> success
            }
            Language.UA -> when {
                success.contains("created", true) || success.contains("создан", true) -> "Акаунт успішно створено!"
                success.contains("success", true) || success.contains("вход", true) -> "Успішний вхід!"
                else -> success
            }
            Language.KK -> when {
                success.contains("created", true) || success.contains("создан", true) -> "Тіркелгі сәтті құрылды!"
                success.contains("success", true) || success.contains("вход", true) -> "Сәтті кіру!"
                else -> success
            }
            Language.DE -> when {
                success.contains("created", true) || success.contains("создан", true) -> "Konto erfolgreich erstellt!"
                success.contains("success", true) || success.contains("вход", true) -> "Erfolgreich eingeloggt!"
                else -> success
            }
            Language.ZH -> when {
                success.contains("created", true) || success.contains("создан", true) -> "账号注册成功！"
                success.contains("success", true) || success.contains("вход", true) -> "登录成功！"
                else -> success
            }
            else -> success
        }
    }
}

