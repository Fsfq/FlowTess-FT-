# FlowTess Privacy Policy / Политика конфиденциальности

Effective Date: October 19, 2026  
Data Controller: FsFqX studio (lead developer FsFq, contact: fsfqx77@gmail.com)  
Applications: FlowTess and FlowTess Lite for Android  

---

## Языки / Languages
- [Русский (RU)](#русский-ru)
- [English (EN)](#english-en)
- [Українська (UA)](#українська-ua)
- [Қазақша (KK)](#қазақша-kk)
- [Deutsch (DE)](#deutsch-de)
- [中文 (ZH)](#中文-zh)

---

## Русский (RU)

### 1. Оператор и сфера действия
Оператор данных: FsFqX (разработчик FsFq, контакт: fsfqx77@gmail.com). Обработка персональных данных регулируется Федеральным законом РФ от 27.07.2006 № 152-ФЗ "О персональных данных", а также международными регламентами GDPR (ЕС) и CCPA (США). Правила действуют для мобильных приложений FlowTess и FlowTess Lite на платформе Android. Игровой прогресс, рекорды и локальные настройки записываются и хранятся на устройстве пользователя (ч. 5 ст. 18 ФЗ-152). Сетевые службы работают через защищенную инфраструктуру Firebase и Epic Online Services.

### 2. Обрабатываемые данные
• Локальные данные: раскладка управления, рекорды, открытые темы и статистика матчей хранятся на устройстве в базе данных SQLite (Room) и системных настройках.
• Данные профиля: при регистрации через Firebase или Google обрабатываются идентификатор пользователя (UID), адрес электронной почты, никнейм и токен аватара.
• Сетевая телеметрия: многопользовательские дуэли маршрутизируются через защищенные реле Epic Online Services (EOS) без раскрытия публичного IP-адреса сопернику. Локальные матчи по Wi-Fi используют сетевое обнаружение NSD mDNS внутри домашней подсети.
• Идентификаторы устройства и реклама: в сетевой версии обрабатывается рекламный идентификатор (AAID / GAID) для аналитики стабильности и показа рекламы.
• Платежные данные: приложение не запрашивает и не хранит платежные реквизиты. Все внутриигровые предметы приобретаются за виртуальную валюту.

### 3. Цели и правовые основания обработки
Обработка данных выполняется на основании согласия пользователя и исполнения условий обслуживания (ст. 6 Федерального закона № 152-ФЗ, ст. 6 GDPR). Цели: обеспечение игрового процесса, сохранение прогресса, синхронизация глобальных таблиц лидеров, детерминированная проверка честной игры (античит) и предотвращение модификации сетевых пакетов.

### 4. Сторонние сервисы и реклама
Для работы сетевой версии задействованы внешние платформы:
• Google Firebase: аутентификация учетных записей, облачное хранилище Cloud Firestore.
• Epic Games (EOS): организация P2P-матчей и сквозная ретрансляция сетевого трафика.
• Yandex Mobile Ads SDK: показ рекламы. Сброс или отключение рекламного идентификатора доступны в настройках Android: Настройки -> Google -> Реклама.
• FlowTess Lite: полностью автономная сборка. Сетевой стек отключен, приложение не отправляет внешних запросов и не содержит рекламных SDK.

### 5. Безопасность данных и шифрование
Внешний трафик передается по защищенным протоколам HTTPS и TLS 1.3. Локальные параметры шифруются аппаратным модулем Android Keystore (алгоритм AES-256-GCM). Резервные файлы .FTB подписываются криптографическим хэшем HMAC-SHA256 для защиты от повреждений и подделки. Пароли пользователей обрабатываются сервисом Firebase Auth и не хранятся в открытом виде.

### 6. Хранение и удаление учетной записи
Данные учетной записи хранятся до момента ее удаления пользователем.
• Удаление в приложении: мгновенно очистить учетную запись и все связанные облачные данные можно по пути: Главное меню -> Профиль -> Опасная зона -> Удалить аккаунт. Действие необратимо.
• Запрос по почте: запрос на удаление можно направить на адрес fsfqx77@gmail.com с темой 'Запрос на удаление аккаунта'. Срок обработки: до 30 дней.
• При удалении стираются: профиль игрока, запись в Firebase Auth, позиции в таблицах лидеров, резервные копии и привязка никнейма.

### 7. Защита данных несовершеннолетних
Приложение не предназначено для детей младше 13 лет (в странах Европейского Союза: младше 16 лет). Профиль и сбор сетевых идентификаторов для детской аудитории не предусмотрены. Если родитель или опекун обнаружил факт создания учетной записи ребенком, направьте обращение на fsfqx77@gmail.com для немедленной очистки данных.

### 8. Права пользователя и контакты
Пользователь имеет право на получение сведений об обработке своих данных, их выгрузку, исправление неточностей, блокирование и отзыв согласия (ст. 14, 15 Федерального закона № 152-ФЗ, Chapter 3 GDPR). По вопросам реализации прав и защите персональных данных обращайтесь к разработчику: fsfqx77@gmail.com.

---

## English (EN)

### 1. Data Controller and Scope
Data controller: FsFqX studio (lead developer FsFq, contact: fsfqx77@gmail.com). Personal data processing complies with Federal Law No. 152-FZ 'On Personal Data' (Russian Federation) as well as GDPR (EU) and CCPA (USA) standards. These rules apply to FlowTess and FlowTess Lite on Android. Primary storage of gameplay progress, high scores, and local settings is performed directly on the user's device in accordance with Art. 18 Part 5 of Law No. 152-FZ. Online services operate through encrypted Firebase and Epic Online Services infrastructure.

### 2. Categories of Processed Data
• Local Data: Control schemes, high scores, unlocked themes, and gameplay statistics are stored locally in the on-device SQLite database (Room) and encrypted preferences.
• Account Data: When registering via Firebase or signing in with Google, we process unique user ID (UID), email address, player nickname, and avatar tokens.
• Network Telemetry: Multiplayer duels route through Epic Online Services (EOS) TURN relay servers without exposing public IP addresses to opponents. Local Wi-Fi duels use NSD mDNS strictly within the local area network.
• Device Identifiers & Advertising: In the connected edition, advertising identifiers (AAID / GAID) may be processed for stability telemetry and relevant ad delivery.
• No Financial Data: We never collect payment card or banking information. All in-game items are unlocked strictly with virtual currency.

### 3. Purposes and Legal Basis
Data processing relies on lawful grounds of user consent and service fulfillment (Article 6 of Federal Law No. 152-FZ, Article 6 GDPR): delivering gameplay functionality, persisting progress, synchronizing global leaderboards, deterministic anti-cheat validation, and packet tampering prevention.

### 4. Third-Party Services and Advertising
The connected build integrates the following external platforms:
• Google Firebase: User authentication and cloud storage (Cloud Firestore).
• Epic Games (EOS): P2P matchmaking and cloud-relayed multiplayer connections.
• Yandex Mobile Ads SDK: In-app advertisement delivery. You can opt out of ad personalization via Android Settings: Settings -> Google -> Ads.
• FlowTess Lite: Completely isolated offline sandbox with zero network activity and zero external SDK calls.

### 5. Data Security and Encryption
All external network traffic is encrypted in transit using HTTPS and TLS 1.3. Local sensitive configurations are encrypted at rest using hardware-backed Android Keystore (AES-256-GCM). Portable .FTB backup archives require cryptographic HMAC-SHA256 signature validation. Passwords are authenticated by Firebase Auth and never stored in plain text.

### 6. Data Retention and Account Deletion
Account data is retained until the user deletes the account.
• In-App Deletion: You can permanently erase your account and associated cloud data at any time via: Main Menu -> Profile -> Danger Zone -> Delete Account. This action is irreversible.
• Email Request: You can also request complete account erasure by contacting fsfqx77@gmail.com with the subject 'Account Deletion Request'. Processing takes up to 30 days.
• Purged Data: Deletion permanently wipes user profile documents, Firebase Auth records, leaderboard ranks, backup files, and reserved nicknames.

### 7. Children's Privacy Protection
The application is not directed to children under 13 years of age (or under 16 in the European Union). Profiles and network identifier collection are not available for children. If a parent or guardian discovers that a child has created an account, contact fsfqx77@gmail.com for immediate data erasure.

### 8. User Rights and Contact Details
Under Federal Law No. 152-FZ (Articles 14, 15) and GDPR (Chapter 3), users retain full rights to access, rectify, restrict, export, and revoke consent for their data. For inquiries regarding data rights and privacy inquiries, contact the developer at: fsfqx77@gmail.com.

---

## Українська (UA)

### 1. Оператор та сфера дії
Оператор даних: FsFqX (розробник FsFq, контакт: fsfqx77@gmail.com). Обробка даних регулюється Законом № 152-ФЗ "Про персональні дані" за місцем реєстрації оператора, а також регламентами GDPR (ЄС) та CCPA (США). Правила діють для додатків FlowTess та FlowTess Lite на платформі Android. Збереження ігрового прогресу, рекордів та налаштувань здійснюється локально на пристрої користувача (ч. 5 ст. 18 № 152-ФЗ). Мережеві служби працюють через захищену інфраструктуру Firebase та Epic Online Services.

### 2. Категорії оброблюваних даних
• Локальні дані: налаштування керування, рекорди, відкриті теми та статистика зберігаються на пристрої в базі даних SQLite (Room) та системних налаштуваннях.
• Дані профілю: при добровільній реєстрації через Firebase або Google обробляються ідентифікатор (UID), електронна пошта, нікнейм та токен аватара.
• Мережева телеметрія: мережеві матчі маршрутизуються через захищені реле Epic Online Services (EOS) без розкриття публічної IP-адреси опоненту. Локальні дуелі через Wi-Fi використовують протокол NSD mDNS виключно в межах домашньої мережі.
• Ідентифікатори пристрою та реклама: у мережевій версії обробляється рекламний ідентифікатор (AAID / GAID) для показу реклами та аналітики стабільності.
• Фінансові дані: додаток не збирає банківські дані. Усі предмети відкриваються за віртуальну валюту.

### 3. Цілі та правові підстави обробки
Обробка здійснюється на законних підставах згоди користувача та виконання умов сервісу (ст. 6 Закону № 152-ФЗ, ст. 6 GDPR): забезпечення гри, збереження прогресу, синхронізація таблиць лідерів, детермінована античит-перевірка та захист від шахрайства.

### 4. Сторонні сервіси та реклама
Для роботи мережевої версії задіяно зовнішні сервіси:
• Google Firebase: автентифікація облікових записів, хмарна база даних Cloud Firestore.
• Epic Games (EOS): змагальні матчі та ретрансляція трафіку.
• Yandex Mobile Ads SDK: показ реклами. Користувач може скинути рекламний ідентифікатор у системних налаштуваннях: Налаштування -> Google -> Реклама.
• FlowTess Lite: автономна версія без мережевого модуля, додаток працює повністю офлайн і не звертається до зовнішніх SDK.

### 5. Безпека даних та шифрування
Зовнішні з'єднання захищені протоколами HTTPS та TLS 1.3. Конфіденційні параметри шифруються апаратно через Android Keystore (AES-256-GCM). Резервні копії .FTB підписуються криптографічним ключем HMAC-SHA256. Паролі користувачів обробляються Firebase Auth і не зберігаються у відкритому вигляді.

### 6. Зберігання та видалення акаунта
Дані облікового запису зберігаються до моменту їх видалення користувачем.
• Видалення у додатку: повністю стерти свій акаунт та хмарні дані можна за шляхом: Головне меню -> Профіль -> Небезпечна зона -> Видалити акаунт. Дія незворотна.
• Запит електронною поштою: запит на видалення можна надіслати на адресу fsfqx77@gmail.com із темою 'Запит на видалення акаунта'. Термін обробки: до 30 днів.
• При видаленні стираються: профіль, запис Firebase Auth, таблиці рекордів, резервні файли та закріплений нікнейм.

### 7. Захист даних неповнолітніх
Додаток не орієнтований на дітей віком до 13 років (або до 16 років у країнах ЄС). Профілі та збір мережевих даних для дітей не підтримуються. Якщо батьки або опікуни виявили реєстрацію дитиною, зверніться на fsfqx77@gmail.com для негайного видалення даних.

### 8. Права користувача та контакти
Користувач має право на отримання інформації про обробку даних, їх вивантаження, виправлення, блокування та відкликання згоди (ст. 14, 15 Закону № 152-ФЗ, GDPR). З усіх питань звертайтеся до розробника: fsfqx77@gmail.com.

---

## Қазақша (KK)

### 1. Оператор және қолданылу аясы
Деректер операторы: FsFqX (әзірлеуші FsFq, байланыс: fsfqx77@gmail.com). Деректерді өңдеу оператор орналасқан елдің № 152-ФЗ "Дербес деректер туралы" Заңына және халықаралық GDPR (ЕО) мен CCPA (АҚШ) талаптарына сәйкес реттеледі. Ережелер Android жүйесіндегі FlowTess және FlowTess Lite қолданбаларына қолданылады. Ойын барысы, рекордтар мен баптаулар құрылғыда жергілікті сақталады (№ 152-ФЗ 18-бап 5-бөлім). Желілік қызметтер Firebase және Epic Online Services қорғалған серверлері арқылы жұмыс істейді.

### 2. Өңделетін деректер санаттары
• Жергілікті деректер: басқару баптаулары, рекордтар, ашылған элементтер құрылғыдағы SQLite (Room) қорында сақталады.
• Тіркелгі деректері: Firebase немесе Google арқылы кірген кезде пайдаланушы коды (UID), электрондық пошта, ойыншы лақап аты және аватар өңделеді.
• Желілік телеметрия: онлайн матчтар үшін Epic Online Services (EOS) қауіпсіз релелері пайдаланылады, қарсыласқа жеке IP мекенжайы көрсетілмейді. Жергілікті Wi-Fi ойыны үшін үй желісі ішінде ғана NSD mDNS қолданылады.
• Жарнама идентификаторлары: желілік нұсқада жарнама көрсету үшін құрылғының жарнамалық коды (AAID / GAID) өңделуі мүмкін.
• Қаржылық деректер: төлем карталары сұралмайды, барлық элементтер виртуалды монеталарға беріледі.

### 3. Өңдеу мақсаттары мен негіздері
№ 152-ФЗ Заңының 6-бабы мен GDPR нормаларына сәйкес өңдеу пайдаланушы келісімі мен қызмет көрсету шарттарына негізделеді: ойын процесін жүргізу, жетістіктерді сақтау, көшбасшылар тақтасын жаңарту, әділ ойынды қорғау (античит) және қауіпсіздікті қамтамасыз ету.

### 4. Үшінші тарап қызметтері мен жарнама
Желілік нұсқа үшін сыртқы сервистер жұмыс істейді:
• Google Firebase: қауіпсіз тіркелу және бұлттық деректер қоры (Cloud Firestore).
• Epic Games (EOS): желілік ойын байланысы мен бұлттық релелер.
• Yandex Mobile Ads SDK: жарнама ұсыну. Пайдаланушы жарнамалық идентификаторды Android баптауларында өшіре алады: Баптаулар -> Google -> Жарнама.
• FlowTess Lite: желі толық сөндірілген автономды нұсқа, сыртқы сервистерге ешқандай сұраныс жіберілмейді.

### 5. Деректер қауіпсіздігі және шифрлау
Барлық байланыстар HTTPS және TLS 1.3 қауіпсіз хаттамаларымен жүргізіледі. Құпия деректер Android Keystore (AES-256-GCM) арқылы шифрланады. .FTB резервтік файлдары HMAC-SHA256 қолтаңбасымен қорғалған. Парольдер ашық мәтін түрінде сақталмайды.

### 6. Деректерді сақтау және тіркелгіні өшіру
Деректер тіркелгі белсенді болған уақыт бойы сақталады.
• Қосымша ішінде өшіру: ойыншы өз тіркелгісін және барлық бұлттық деректерін мына жол арқылы өшіре алады: Басты мәзір -> Профиль -> Қауіпті аймақ -> Тіркелгіні өшіру. Әрекет қайтарылмайды.
• Пошта арқылы сұраныс: fsfqx77@gmail.com поштасына өшіру туралы сұраныс жіберуге болады. Сұраныс 30 күн ішінде орындалады.
• Өшіру барысында тіркелгі деректері, Firebase Auth жазбасы, рекордтар және ойыншы аты толықтай жойылады.

### 7. Кәмелетке толмағандар құпиялылығы
Қосымша 13 жасқа толмаған балаларға арналмаған (Еуропалық Одақ елдерінде: 16 жасқа дейін). Балаларға арналған профиль немесе желілік деректерді жинау қарастырылмаған. Егер баланың рұқсатсыз тіркелгені анықталса, fsfqx77@gmail.com поштасына хабарласу қажет, деректер дереу жойылады.

### 8. Пайдаланушы құқықтары және байланыс
№ 152-ФЗ Заңы (14, 15-баптар) мен GDPR талаптарына сәйкес, пайдаланушы өз деректеріне қол жеткізуге, оларды түзетуге, бұғаттауға, келісімді қайтарып алуға және толық жоюға құқылы. Барлық сұрақтар бойынша мына поштаға хабарласыңыз: fsfqx77@gmail.com.

---

## Deutsch (DE)

### 1. Verantwortlicher und Geltungsbereich
Verantwortlicher: FsFqX (Entwickler FsFq, E-Mail: fsfqx77@gmail.com). Die Datenverarbeitung erfolgt auf Grundlage des russischen Bundesgesetzes Nr. 152-FZ 'Über personenbezogene Daten' sowie der DSGVO (EU) und des CCPA (USA). Diese Bestimmungen gelten für FlowTess und FlowTess Lite unter Android. Spielstände, Rekorde und lokale Konfigurationen werden primär auf dem Gerät gespeichert (Art. 18 Abs. 5 Nr. 152-FZ). Online-Dienste nutzen die verschlüsselte Infrastruktur von Firebase und Epic Online Services.

### 2. Kategorien verarbeiteter Daten
• Lokale Daten: Steuerungsbelegung, Rekorde, freigeschaltete Skins und Spielstatistiken werden lokal in SQLite (Room) und geschützten App-Einstellungen gespeichert.
• Kontodaten: Bei freiwilliger Registrierung via Firebase oder Google werden Benutzer-ID (UID), E-Mail-Adresse, Spielername und Avatar-Token verarbeitet.
• Netzwerk-Telemetrie: Mehrspieler-Duelle werden über Epic Online Services (EOS) Relay-Server vermittelt, ohne Mitspielern Ihre öffentliche IP-Adresse offenzulegen. Lokale Wi-Fi-Duelle nutzen NSD mDNS ausschließlich im Heimnetzwerk.
• Geräte-IDs und Werbung: In der vernetzten Version kann die Werbe-ID (AAID / GAID) zur Stabilitätsanalyse und Werbeeinblendung verarbeitet werden.
• Keine Finanzdaten: Es werden keine Bank- oder Kartendaten erhoben. Alle virtuellen Gegenstände werden mit Spielwährung erworben.

### 3. Zwecke und Rechtsgrundlagen
Die Datenverarbeitung stützt sich auf Einwilligung und Vertragserfüllung (Art. 6 des Gesetzes Nr. 152-FZ, Art. 6 DSGVO): Spielfunktionen, Speichern des Fortschritts, globale Bestenlisten, deterministischer Anti-Cheat-Schutz und Betrugsprävention.

### 4. Drittanbieter-Dienste und Werbung
Für die Online-Funktionen werden externe Plattformen genutzt:
• Google Firebase: Authentifizierung und Cloud-Datenbank (Cloud Firestore).
• Epic Games (EOS): P2P-Matchmaking und verschlüsselte Netzwerk-Relays.
• Yandex Mobile Ads SDK: Bereitstellung von Werbung. Die Werbe-ID kann in den Android-Einstellungen zurückgesetzt oder deaktiviert werden: Einstellungen -> Google -> Werbung.
• FlowTess Lite: Vollständig isolierte Offline-Version ohne Netzwerkzugriff oder Analyse-SDKs.

### 5. Datensicherheit und Verschlüsselung
Die Netzwerkkommunikation erfolgt geschützt über HTTPS und TLS 1.3. Lokale Schlüssel werden über Android Keystore (AES-256-GCM) hardwarenah verschlüsselt. .FTB-Backupdateien sind durch HMAC-SHA256-Signaturen manipulationssicher. Passwörter werden über Firebase Auth verarbeitet und niemals im Klartext gespeichert.

### 6. Speicherdauer und Kontolöschung
Kontodaten bleiben bis zur Löschung durch den Nutzer gespeichert.
• Löschung in der App: Sie können Ihr Konto und alle Cloud-Daten jederzeit löschen über: Hauptmenü -> Profil -> Gefahrenbereich -> Konto löschen. Dieser Vorgang ist unwiderruflich.
• Löschung per E-Mail: Alternativ können Sie einen Löschantrag an fsfqx77@gmail.com mit dem Betreff 'Account Deletion Request' senden. Die Bearbeitungszeit beträgt bis zu 30 Tage.
• Gelöschte Daten: Das Spielerprofil, der Firebase Auth-Eintrag, Bestenlisten-Einträge, Backups und der reservierte Nickname werden dauerhaft entfernt.

### 7. Schutz von Minderjährigen
Die App ist nicht für Kinder unter 13 Jahren (in der EU: unter 16 Jahren) bestimmt. Profile und die Erhebung von Online-Daten für Kinder finden nicht statt. Sollten Erziehungsberechtigte feststellen, dass ein Kind ein Konto erstellt hat, wenden Sie sich bitte an fsfqx77@gmail.com zur sofortigen Löschung.

### 8. Betroffenenrechte und Kontakt
Gemäß Gesetz Nr. 152-FZ (Art. 14, 15) und DSGVO (Kapitel 3) haben Nutzer das Recht auf Auskunft, Berichtigung, Einschränkung, Datenübertragbarkeit und Löschung. Bei Datenschutzanfragen wenden Sie sich bitte an: fsfqx77@gmail.com.

---

## 中文 (ZH)

### 1. 控制者与适用范围
数据控制者：FsFqX 工作室（主开发者 FsFq，联络邮箱：fsfqx77@gmail.com）。个人数据处理依据控制者所在地俄罗斯联邦第 152-FZ 号《个人数据法》，并遵循国际 GDPR（欧盟）与 CCPA（美国）隐私标准。本准则适用于 Android 平台的 FlowTess 与 FlowTess Lite。本地游戏进度、最高分与设置优先存储于用户本地设备（第 152-FZ 号法律第18条第5款）。在线服务运行于 Firebase 与 Epic Online Services 加密通道。

### 2. 处理的数据类别
• 本地数据：操作设置、最高分、解锁皮肤和游戏统计均保存在设备本地 SQLite（Room）数据库和系统加密配置中。
• 账户数据：通过 Firebase 或 Google 登录时，处理用户唯一标识（UID）、电子邮箱、游戏昵称及头像标识。
• 网络通信：联机对战通过 Epic Online Services（EOS）云端中继进行，绝不向对手公开真实公网 IP 地址。本地 Wi-Fi 局域网对战仅在局域网内部使用 NSD mDNS 协议。
• 设备标识与广告：联机版可能处理广告标识符（AAID / GAID）以提供广告展示和稳定性分析。
• 不收集金融数据：应用不索取任何银行卡或支付凭据，游戏道具仅支持虚拟游戏币兑换。

### 3. 处理目的与法律依据
数据处理基于用户同意与服务协议履行（第 152-FZ 号法律第 6 条、GDPR 第 6 条）：提供核心游戏功能、保存关卡进度、同步全球排行榜、进行基于确定性算法的反作弊验证以及防范安全攻击。

### 4. 第三方服务与广告披露
联网版本集成以下外部技术支持：
• Google Firebase：账户鉴权与云端存档同步（Cloud Firestore）。
• Epic Games（EOS）：多人竞技匹配与云端网络中继。
• Yandex Mobile Ads SDK：广告展示。用户可随时在系统设置中重置或关闭广告跟踪：设置 -> Google -> 广告。
• FlowTess Lite 纯单机版：网络组件已彻底停用，不发起任何网络连接，亦不调用任何外部分析或广告 SDK。

### 5. 数据安全与加密措施
所有外部网络通信均通过 HTTPS 及 TLS 1.3 加密协议传输。敏感本地配置由 Android Keystore（AES-256-GCM）硬件级密钥库加密保护。.FTB 备份文件通过 HMAC-SHA256 签名校验防篡改。用户密码由 Firebase Auth 处理，绝不明文存储。

### 6. 数据留存与账户注销机制
账户数据在用户注销前持续保留。
• 应用内自主注销：您可随时在游戏内即时且永久注销账户并清除云端数据：主菜单 -> 个人资料 -> 危险区域 -> 注销账户。此操作不可逆。
• 邮件注销申请：您亦可发送邮件至 fsfqx77@gmail.com，邮件主题注明 'Account Deletion Request'，我们将在30天内核实并执行删除。
• 注销后清除：用户资料、Firebase Auth 登录凭证、云端存档记录、排行榜积分及唯一昵称占位。

### 7. 未成年人隐私保护
本应用不面向未满 13 周岁的儿童（或欧盟成员国未满 16 周岁的未成年人）。应用不支持儿童个人资料及网络标识符收集。若监护人发现儿童创建了账户，请联络 fsfqx77@gmail.com，我们将立即予以彻底删除。

### 8. 用户权利与联系方式
根据第 152-FZ 号法律（第14、15条）以及 GDPR（第3章）规范，用户拥有查询、更正、限制处理、导出以及撤回同意的完整权利。如有隐私权益相关需求，请联络开发者：fsfqx77@gmail.com。
