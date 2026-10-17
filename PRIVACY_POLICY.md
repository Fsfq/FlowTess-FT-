# FlowTess Privacy Policy / Политика конфиденциальности

Effective Date: October 17, 2026  
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
Оператор данных: FsFqX (разработчик FsFq, контакт: fsfqx77@gmail.com). Правила действуют для мобильных приложений FlowTess и FlowTess Lite на платформе Android. Игровой прогресс, рекорды и настройки хранятся локально на устройстве. Сетевые функции используют защищенные сервисы Firebase и Epic Online Services.

### 2. Обрабатываемые данные
• Локальные данные: параметры управления, личные рекорды, открытые темы и статистика хранятся локально на устройстве в базе данных SQLite (Room) и зашифрованных настройках.
• Данные учетной записи: при добровольной регистрации через Firebase или входе через Google обрабатываются идентификатор (UID), адрес электронной почты, отображаемый никнейм и токен аватара.
• Сетевая телеметрия: для многопользовательских матчей используются реле Epic Online Services (EOS) без раскрытия публичного IP-адреса оппоненту. Для локальных матчей по Wi-Fi используется сетевое обнаружение NSD mDNS исключительно внутри локальной подсети.
• Идентификаторы устройства и реклама: в сетевой версии может обрабатываться рекламный идентификатор (AAID / GAID) для показа релевантной рекламы и аналитики сбоев.
• Финансовые данные не собираются: приложение не запрашивает платежные реквизиты, все покупки осуществляются за виртуальную игровую валюту.

### 3. Цели и правовые основания обработки
Обработка данных осуществляется на законных основаниях (согласие пользователя и выполнение условий обслуживания): обеспечение игрового процесса, сохранение прогресса, синхронизация таблиц лидеров, детерминированная проверка честной игры (античит) и предотвращение мошенничества.

### 4. Сторонние сервисы и реклама
В сетевой версии приложения используются надежные сторонние платформы:
• Google Firebase: аутентификация учетных записей, облачное хранение прогресса (Cloud Firestore).
• Epic Games (EOS): P2P-матчмейкинг и облачные ретрансляторы трафика.
• Yandex Mobile Ads SDK: отображение рекламы. Пользователь может в любой момент ограничить сбор рекламного идентификатора в настройках Android: Настройки -> Google -> Реклама -> Сбросить или удалить рекламный идентификатор.
• Автономная версия FlowTess Lite: сетевой стек полностью отключен, приложение не выполняет сетевых запросов и не обращается к внешним SDK.

### 5. Безопасность данных и шифрование
Все внешние соединения выполняются по защищенным протоколам HTTPS и TLS 1.3. Конфиденциальные параметры шифруются аппаратно с использованием Android Keystore (AES-256-GCM). Локальные резервные копии .FTB защищены криптографической подписью HMAC-SHA256 для защиты от повреждения и модификации. Пароли пользователей никогда не хранятся в открытом виде.

### 6. Хранение и удаление учетной записи
Данные учетной записи хранятся до тех пор, пока аккаунт активен.
• Самостоятельное удаление в приложении: вы можете мгновенно и безвозвратно удалить свой аккаунт и все связанные облачные данные непосредственно в игре: Профиль -> Настройки (значок шестеренки) -> вкладка Аккаунт -> Опасная зона -> Удалить аккаунт.
• Удаление по запросу: вы также можете направить запрос на удаление учетной записи и всех связанных записей на адрес электронной почты: fsfqx77@gmail.com с темой 'Запрос на удаление аккаунта'. Запрос обрабатывается в течение 30 дней.
• При удалении аккаунта безвозвратно стираются: профиль игрока, запись в базе Firebase Auth, сохраненные таблицы лидеров, резервные данные и уникальный никнейм.

### 7. Защита данных несовершеннолетних
Приложение не предназначено для детей младше 13 лет (или младше 16 лет в странах Европейского Союза). Мы сознательно не собираем личную информацию несовершеннолетних. Если родителю или опекуну стало известно, что ребенок предоставил личные данные без согласия, свяжитесь с нами по адресу fsfqx77@gmail.com для немедленного удаления информации.

### 8. Права пользователя и контакты
В соответствии с законодательством о защите данных (GDPR, CCPA), пользователь имеет право на доступ к своим данным, их исправление, экспорт, ограничение обработки и полное удаление. По любым вопросам конфиденциальности и обращениям обращайтесь к разработчику по электронной почте: fsfqx77@gmail.com.

---

## English (EN)

### 1. Data Controller and Scope
Data controller: FsFqX studio (lead developer FsFq, contact: fsfqx77@gmail.com). These rules apply to FlowTess and FlowTess Lite applications on Android. Gameplay progress, high scores, and settings are stored locally on the device. Online services utilize encrypted Firebase and Epic Online Services infrastructure.

### 2. Categories of Processed Data
• Local Data: Control schemes, high scores, unlocked themes, and gameplay statistics are stored locally in the on-device SQLite database (Room) and encrypted preferences.
• Account Data: When voluntarily registering via Firebase or signing in with Google, we process unique user ID (UID), email address, player nickname, and avatar tokens.
• Network Telemetry: P2P multiplayer duels route through Epic Online Services (EOS) TURN relay servers without exposing public IP addresses to peers. Local Wi-Fi duels use NSD mDNS strictly within the local area network.
• Device Identifiers & Advertising: In the connected edition, advertising identifiers (AAID / GAID) may be processed for relevant ad delivery and crash telemetry.
• No Financial Data: We never collect payment card or banking data; all cosmetics and crates are unlocked strictly with in-game virtual currency.

### 3. Purposes and Legal Basis
Data processing relies on lawful grounds (user consent and service fulfillment): delivering core gameplay functionality, persisting progress, synchronizing global leaderboards, deterministic anti-cheat validation, and fraud prevention.

### 4. Third-Party Services and Advertising
The connected build integrates trusted third-party providers:
• Google Firebase: User authentication and secure cloud persistence (Cloud Firestore).
• Epic Games (EOS): Competitive matchmaking and cloud-relayed multiplayer connections.
• Yandex Mobile Ads SDK: In-app advertisement delivery. You can opt out of ad personalization via Android Settings: Settings -> Google -> Ads -> Reset or delete advertising ID.
• FlowTess Lite Edition: Completely isolated offline sandbox with zero network activity and zero external SDK telemetry.

### 5. Data Security and Encryption
All network traffic is encrypted in transit using HTTPS and TLS 1.3. Critical preferences are encrypted at rest using hardware-backed Android Keystore (AES-256-GCM). Portable .FTB backup archives require cryptographic HMAC-SHA256 integrity validation. User passwords are encrypted by Firebase Auth and never stored in plain text.

### 6. Data Retention and Account Deletion
Account data is retained for the duration of active account usage.
• In-App Account Deletion: You can permanently delete your account and all associated cloud progress directly inside the game: Profile -> Settings (gear icon) -> Account tab -> Danger Zone -> Delete Account.
• Deletion via Email Request: You may also request account erasure by sending an email to fsfqx77@gmail.com with the subject 'Account Deletion Request'. Inquiries are resolved within 30 days.
• Deletion permanently purges: user profile document, Firebase Auth credentials, cloud progress, leaderboard entries, and claimed nickname reservation.

### 7. Children's Privacy Protection
The application is not directed to children under 13 years of age (or under 16 in the European Union). We do not knowingly collect personal data from minors. If a parent or guardian discovers that a child has provided personal information without consent, contact us at fsfqx77@gmail.com for prompt deletion.

### 8. User Rights and Contact Details
Under applicable data protection laws (GDPR, CCPA), users retain rights of access, rectification, portability, processing restriction, and erasure of their personal data. For all privacy inquiries, data requests, or complaints, contact us at: fsfqx77@gmail.com.

---

## Українська (UA)

### 1. Оператор та сфера дії
Оператор даних: FsFqX (розробник FsFq, контакт: fsfqx77@gmail.com). Правила діють для додатків FlowTess та FlowTess Lite на платформі Android. Ігровий прогрес, рекорди та налаштування зберігаються локально на пристрої. Мережеві функції використовують захищені сервіси Firebase та Epic Online Services.

### 2. Категорії оброблюваних даних
• Локальні дані: налаштування керування, рекорди, відкриті теми та статистика зберігаються на пристрої в базі даних SQLite (Room) та системних налаштуваннях.
• Дані акаунта: при добровільній реєстрації через Firebase або Google обробляються ідентифікатор (UID), електронна пошта, ігровий нікнейм та аватар.
• Мережева телеметрія: для мережевих матчів використовуються реле Epic Online Services (EOS) без розкриття публічної IP-адреси опоненту. Для локальних дуелей через Wi-Fi задіяно протокол NSD mDNS виключно в межах домашньої мережі.
• Ідентифікатори пристрою та реклама: у мережевій версії може оброблятися рекламний ідентифікатор (AAID / GAID) для показу реклами та аналітики.
• Фінансові дані не збираються: додаток не запитує банківські дані, усі предмети купуються за віртуальну валюту.

### 3. Цілі та правові підстави обробки
Обробка здійснюється на законних підставах (згода користувача та виконання умов сервісу): забезпечення ігрового процесу, збереження прогресу, синхронізація таблиць лідерів, детермінована античит-перевірка та захист від шахрайства.

### 4. Сторонні сервіси та реклама
У мережевій версії використовуються перевірені сервіси:
• Google Firebase: автентифікація користувачів, хмарне збереження (Cloud Firestore).
• Epic Games (EOS): організація змагальних матчів через безпечні реле.
• Yandex Mobile Ads SDK: показ реклами. Користувач може вимкнути збір рекламного ідентифікатора в меню: Налаштування -> Google -> Реклама -> Скинути або видалити рекламний ідентифікатор.
• Автономна версія FlowTess Lite: мережевий модуль повністю вимкнено, додаток працює повністю офлайн.

### 5. Безпека даних та шифрування
Усі зовнішні з'єднання захищені сучасними протоколами HTTPS та TLS 1.3. Конфіденційні параметри шифруються апаратно через Android Keystore (AES-256-GCM). Файли резервних копій .FTB підписуються криптографічним ключем HMAC-SHA256. Паролі користувачів не зберігаються у відкритому вигляді.

### 6. Зберігання та видалення акаунта
Дані зберігаються, доки акаунт залишається активним.
• Самостійне видалення в додатку: ви можете безповоротно видалити свій акаунт та всі хмарні дані безпосередньо у грі: Профіль -> Налаштування (шестірня) -> вкладка Акаунт -> Небезпечна зона -> Видалити акаунт.
• Запит електронною поштою: ви також можете надіслати запит на видалення на fsfqx77@gmail.com із темою 'Account Deletion Request'. Запит виконується протягом 30 днів.
• При видаленні безповоротно стираються: дані профілю, обліковий запис Firebase Auth, рекорди, хмарні збереження та нікнейм.

### 7. Захист даних неповнолітніх
Додаток не орієнтований на дітей віком до 13 років (або до 16 років у країнах ЄС). Ми свідомо не збираємо персональні дані неповнолітніх. Якщо батьки або опікуни виявили факт передачі даних дитиною без їхньої згоди, зверніться на fsfqx77@gmail.com для негайного видалення таких відомостей.

### 8. Права користувача та контакти
Відповідно до законодавства (GDPR, CCPA), користувач має право на доступ до своїх даних, їх виправлення, експорт, обмеження обробки та повне видалення. З усіх питань конфіденційності звертайтеся за адресою: fsfqx77@gmail.com.

---

## Қазақша (KK)

### 1. Оператор және қолданылу аясы
Деректер операторы: FsFqX (әзірлеуші FsFq, байланыс: fsfqx77@gmail.com). Ережелер Android платформасындағы FlowTess және FlowTess Lite қолданбаларына қолданылады. Ойын барысы, рекордтар мен баптаулар құрылғыда жергілікті сақталады. Желілік қызметтер Firebase және Epic Online Services қорғалған серверлерін пайдаланады.

### 2. Өңделетін деректер санаттары
• Жергілікті деректер: басқару баптаулары, рекордтар, ашылған элементтер құрылғыдағы SQLite (Room) қорында сақталады.
• Тіркелгі деректері: Firebase немесе Google арқылы кірген кезде пайдаланушы коды (UID), электрондық пошта, ойыншы лақап аты және аватар өңделеді.
• Желілік телеметрия: онлайн матчтар үшін Epic Online Services (EOS) қауіпсіз релелері пайдаланылады, қарсыласқа жеке IP мекенжайы көрсетілмейді. Жергілікті Wi-Fi ойыны үшін үй желісі ішінде ғана NSD mDNS қолданылады.
• Жарнама идентификаторлары: желілік нұсқада жарнама көрсету үшін құрылғының жарнамалық коды (AAID) өңделуі мүмкін.
• Қаржылық деректер жиналмайды: төлем карталары сұралмайды, барлық элементтер ойын ішіндегі виртуалды монеталарға беріледі.

### 3. Өңдеу мақсаттары мен негіздері
Деректерді өңдеу заңды негіздерге сүйенеді (пайдаланушы келісімі және қызмет көрсету шарттары): ойын процесін жүргізу, жетістіктерді сақтау, көшбасшылар тақтасын жаңарту, әділ ойынды қорғау (античит) және алаяқтықтың алдын алу.

### 4. Үшінші тарап қызметтері мен жарнама
Желілік нұсқада сенімді қызметтер жұмыс істейді:
• Google Firebase: қауіпсіз тіркелу және бұлттық деректер қоры (Cloud Firestore).
• Epic Games (EOS): желілік ойын байланысы мен бұлттық релелер.
• Yandex Mobile Ads SDK: жарнама ұсыну. Пайдаланушы жарнамалық идентификаторды Android баптауларында өшіре алады: Баптаулар -> Google -> Жарнама.
• FlowTess Lite оффлайн нұсқасы: желі толық сөндірілген, сыртқы сервистерге ешқандай сұраныс жіберілмейді.

### 5. Деректер қауіпсіздігі және шифрлау
Барлық сыртқы байланыстар HTTPS және TLS 1.3 қауіпсіз хаттамаларымен жүргізіледі. Құпия деректер Android Keystore (AES-256-GCM) арқылы шифрланады. .FTB резервтік файлдары HMAC-SHA256 қолтаңбасымен қорғалған. Парольдер ашық мәтін түрінде сақталмайды.

### 6. Деректерді сақтау және тіркелгіні өшіру
Деректер тіркелгі белсенді болған уақыт бойы сақталады.
• Қосымша ішінде өшіру: ойыншы өз тіркелгісін және барлық бұлттық деректерін ойын ішінде өшіре алады: Профиль -> Баптаулар -> Тіркелгі қойындысы -> Қауіпті аймақ -> Тіркелгіні өшіру.
• Пошта арқылы сұраныс: сонымен қатар fsfqx77@gmail.com поштасына өшіру туралы сұраныс жіберуге болады. Сұраныс 30 күн ішінде толық орындалады.
• Өшіру барысында тіркелгі деректері, Firebase Auth жазбасы, рекордтар және ойыншы аты толықтай жойылады.

### 7. Кәмелетке толмағандар құпиялылығы
Қосымша 13 жасқа толмаған балаларға арналмаған (Еуропалық Одақ елдерінде 16 жасқа дейін). Біз кәмелетке толмағандардың жеке деректерін мақсатты түрде жинамаймыз. Егер баланың рұқсатсыз деректер бергені анықталса, fsfqx77@gmail.com поштасына хабарласу қажет, деректер дереу өшіріледі.

### 8. Пайдаланушы құқықтары және байланыс
Құпиялылық заңнамасына (GDPR, CCPA) сәйкес, пайдаланушы өз деректеріне қол жеткізуге, оларды түзетуге, көшіріп алуға және толық жоюға құқылы. Барлық сұрақтар бойынша мына поштаға хабарласыңыз: fsfqx77@gmail.com.

---

## Deutsch (DE)

### 1. Verantwortlicher und Geltungsbereich
Verantwortlicher: FsFqX (Entwickler FsFq, E-Mail: fsfqx77@gmail.com). Diese Bestimmungen gelten für FlowTess und FlowTess Lite unter Android. Spielstände, Rekorde und Einstellungen werden lokal auf dem Gerät gespeichert. Online-Dienste nutzen die verschlüsselte Infrastruktur von Firebase und Epic Online Services.

### 2. Kategorien verarbeiteter Daten
• Lokale Daten: Steuerungsoptionen, Rekorde, freigeschaltete Skins und Statistiken werden lokal in SQLite (Room) und geschützten App-Einstellungen gespeichert.
• Kontodaten: Bei freiwilliger Anmeldung via Firebase oder Google werden Benutzer-ID (UID), E-Mail-Adresse, Spielername und Avatar verarbeitet.
• Netzwerk-Telemetrie: Für Mehrspieler-Duelle werden Epic Online Services (EOS) Relay-Server genutzt, ohne Ihre öffentliche IP-Adresse für Mitspieler offenzulegen. Lokale Wi-Fi-Duelle nutzen NSD mDNS ausschließlich im lokalen Netzwerk.
• Geräte-IDs und Werbung: In der Online-Version kann die Werbe-ID (AAID) zur Anzeige von Werbung und Fehleranalyse verarbeitet werden.
• Keine Finanzdaten: Es werden keine Bank- oder Kreditkartendaten erhoben, alle Transaktionen erfolgen mit virtueller Spielwährung.

### 3. Zwecke und Rechtsgrundlagen
Die Verarbeitung erfolgt auf Basis berechtigter Interessen und Einwilligung: Bereitstellung von Spielfunktionen, Speichern des Fortschritts, globale Bestenlisten, deterministischer Anti-Cheat-Schutz und Betrugsprävention.

### 4. Drittanbieter-Dienste und Werbung
Die vernetzte Version nutzt folgende Drittanbieter:
• Google Firebase: Authentifizierung und Cloud-Speicherung (Cloud Firestore).
• Epic Games (EOS): Sichere P2P-Duelle über Cloud-Relays.
• Yandex Mobile Ads SDK: Einbindung von Werbeanzeigen. Nutzer können die Erfassung der Werbe-ID in den Systemeinstellungen deaktivieren: Einstellungen -> Google -> Werbung -> Werbe-ID löschen oder zurücksetzen.
• FlowTess Lite Edition: Vollständig isolierter Offline-Betrieb ohne jegliche Netzwerkverbindungen oder SDK-Aufrufe.

### 5. Datensicherheit und Verschlüsselung
Jegliche Datenübertragung erfolgt verschlüsselt über HTTPS und TLS 1.3. Sensible Konfigurationsdaten werden durch Android Keystore (AES-256-GCM) hardwarenah geschützt. Lokale .FTB-Backups sind mit HMAC-SHA256-Signaturen gegen Manipulation gesichert. Passwörter werden niemals unverschlüsselt gespeichert.

### 6. Speicherdauer und Kontolöschung
Daten werden gespeichert, solange das Benutzerkonto besteht.
• Direkte Kontolöschung in der App: Sie können Ihr Konto und alle Cloud-Daten jederzeit in der App löschen: Profil -> Einstellungen (Zahnrad-Symbol) -> Tab Konto -> Gefahrenbereich -> Konto löschen.
• Löschung per E-Mail: Alternativ können Sie eine Anfrage an fsfqx77@gmail.com mit dem Betreff 'Account Deletion Request' senden. Die Löschung wird innerhalb von 30 Tagen durchgeführt.
• Bei der Löschung werden Profil, Firebase Auth-Konto, Bestenlisten-Einträge und Cloud-Spielstände unwiderruflich entfernt.

### 7. Schutz von Minderjährigen
Die Anwendung richtet sich nicht an Kinder unter 13 Jahren (bzw. unter 16 Jahren in der EU). Wir erheben wissentlich keine personenbezogenen Daten von Minderjährigen. Sollten Erziehungsberechtigte feststellen, dass ein Kind ohne Einwilligung Daten übermittelt hat, bitten wir um Kontaktaufnahme unter fsfqx77@gmail.com zur umgehenden Löschung.

### 8. Betroffenenrechte und Kontakt
Gemäß DSGVO und geltendem Datenschutzrecht haben Sie das Recht auf Auskunft, Berichtigung, Datenübertragbarkeit, Einschränkung der Verarbeitung und Löschung Ihrer Daten. Für Datenschutzanfragen wenden Sie sich bitte an: fsfqx77@gmail.com.

---

## 中文 (ZH)

### 1. 控制者与适用范围
数据控制者：FsFqX 工作室（开发者 FsFq，联络邮箱：fsfqx77@gmail.com）。本准则适用于 Android 平台的 FlowTess 与 FlowTess Lite。游戏存档、单机记录与偏好设置均保存在本地设备中。在线服务采用 Firebase 与 Epic Online Services 加密通道。

### 2. 处理的数据类别
• 本地数据：操作设置、最高分、解锁皮肤和游戏统计均保存在设备本地 SQLite（Room）数据库和系统加密配置中。
• 账户数据：通过 Firebase 或 Google 登录时，处理用户唯一标识（UID）、电子邮箱、游戏昵称及头像标识。
• 网络通信：联机对战通过 Epic Online Services（EOS）云端中继进行，绝不向对手公开真实公网 IP 地址。本地 Wi-Fi 局域网对战仅在局域网内部使用 NSD mDNS 协议。
• 设备标识与广告：联机版可能处理广告标识符（AAID / GAID）以提供广告展示和崩溃诊断。
• 不收集金融数据：应用不索取任何银行卡或支付凭据，游戏道具仅支持虚拟游戏币兑换。

### 3. 处理目的与法律依据
数据处理基于合法基础（用户授权与服务履行）：保障游戏正常运行、保存关卡进度、同步全球排行榜、进行基于确定性算法的防作弊验证以及防范安全攻击。

### 4. 第三方服务与广告披露
标准联网版本集成以下可靠的第三方技术：
• Google Firebase：账户鉴权与云端存档同步（Cloud Firestore）。
• Epic Games（EOS）：多人竞技匹配与云端网络中继。
• Yandex Mobile Ads SDK：广告展示。用户可随时在系统设置中重置或关闭广告跟踪：设置 -> Google -> 广告 -> 重置或删除广告 ID。
• FlowTess Lite 纯单机版：网络组件已彻底停用，不发起任何网络连接，亦不调用任何外部分析或广告 SDK。

### 5. 数据安全与加密措施
所有外部网络通信均通过 HTTPS 及 TLS 1.3 加密协议传输。敏感本地配置由 Android Keystore（AES-256-GCM）硬件级密钥库加密保护。.FTB 备份文件通过 HMAC-SHA256 签名校验防篡改。用户密码绝不明文存储。

### 6. 数据留存与账户注销机制
账户数据在用户持有账户期间予以保留。
• 应用内自主注销：您可随时在游戏内即时且永久注销账户并清除云端数据：个人资料 -> 设置（齿轮图标） -> 账户选项卡 -> 危险区域 -> 注销账户。
• 邮件注销申请：您亦可发送邮件至 fsfqx77@gmail.com，邮件主题注明 'Account Deletion Request'，我们将在30天内核实并执行删除。
• 注销后将彻底清除：用户资料、Firebase Auth 登录凭证、云端存档记录、排行榜积分及唯一昵称占位。

### 7. 未成年人隐私保护
本应用不面向未满 13 周岁的儿童（或欧盟成员国未满 16 周岁的未成年人）。我们绝不故意收集未成年人的个人数据。若监护人发现儿童未经同意提交了个人信息，请联络 fsfqx77@gmail.com，我们将立即予以彻底删除。

### 8. 用户权利与联系方式
根据适用隐私法律法规（包括 GDPR 与 CCPA），用户对其个人数据享有查阅权、更正权、可携权、限制处理权及永久删除权。如有任何隐私疑虑或权益诉求，请随时联络：fsfqx77@gmail.com。

---
