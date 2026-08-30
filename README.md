# BonusWallet 1.0.0
© 2026 BonusWallet. Все права защищены.

**BonusWallet** — красивый локальный кошелек для скидочных и бонусных карт. Хранит карты Bravo, Wolt, KFC и других магазинов полностью офлайн на вашем устройстве.

## Основные возможности
- Добавление карт вручную (название, организация, номер)
- Сканирование штрих-кода камерой
- Поддержка форматов: Автоматически, EAN-13, EAN-8, UPC-A, UPC-E, Code 128, Code 39, ITF, Codabar, QR Code
- Генерация большого читаемого штрих-кода
- Режим на весь экран с удержанием экрана включенным для кассы
- Редактирование, удаление, перетаскивание порядка
- Локальное хранение Room (данные не уходят в интернет)
- Экспорт/импорт BonusWallet_backup.json
- Темы: светлая, темная, системная
- Соглашение и политика конфиденциальности при первом запуске

## Архитектура
- Kotlin + Jetpack Compose (Material 3)
- MVVM-ish: Room DAO Flow -> Compose State
- DataStore для настроек (termsAccepted, theme)
- ZXing core для генерации, zxing-android-embedded для сканирования
- Навигация: navigation-compose
- Package: com.bonuswallet.app

## Структура проекта
```
app/src/main/java/com/bonuswallet/app/
  BonusWalletApp.kt
  MainActivity.kt
  data/
    CardEntity.kt
    CardDao.kt
    AppDatabase.kt
    PreferencesManager.kt
  util/
    BarcodeUtil.kt
    BackupUtil.kt
  ui/
    theme/Theme.kt
    screens/
      TermsScreen.kt
      HomeScreen.kt
      AddEditCardScreen.kt
      CardDetailScreen.kt
      FullscreenBarcodeScreen.kt
      SettingsScreen.kt
      PrivacyScreen.kt
```

## Локальное хранение
- База `bonuswallet_db` Room version 1
- Таблица `cards` с полями id, orgName, title, number, format, colorHex, sortOrder, createdAt
- DataStore `bonuswallet_prefs` для флага соглашения и темы
- При обновлении с 1.0.0 на 1.0.1+ данные сохраняются, так как versionCode растет, а база использует fallbackToDestructiveMigration только при необходимости. Для будущих версий добавляйте миграции.

## Как работает GitHub Actions

### Основной workflow: `.github/workflows/build-apk.yml`

1. Checkout репозитория
2. Установка JDK 17 (Temurin)
3. Установка Android SDK
4. Установка Gradle 8.6 и генерация wrapper (`gradle wrapper`)
5. Сборка `./gradlew assembleDebug` и `assembleRelease`
6. Загрузка артефактов:
   - `BonusWallet-APK` содержит `app-debug.apk` и `app-release-unsigned.apk`

APK появляется во вкладке Actions -> последний run -> Artifacts.

### Как получить APK
1. Создайте репозиторий на GitHub
2. Загрузите весь проект (все файлы)
3. Откройте вкладку **Actions** -> запустится workflow **Build BonusWallet APK**
4. Дождитесь зеленой галочки
5. Скачайте **BonusWallet-APK** -> внутри debug APK готов к установке

### Как создать Release
1. Измените `versionCode` и `versionName` в `app/build.gradle.kts`
   ```kotlin
   versionCode = 2
   versionName = "1.0.1"
   ```
2. Закоммитьте и запушьте тег:
   ```bash
   git tag v1.0.1
   git push origin v1.0.1
   ```
3. Создайте Release на GitHub с тегом v1.0.1 -> workflow автоматически прикрепит APK к релизу
4. Пользователь устанавливает новый APK поверх старого — карты сохранятся (тот же applicationId и сохраненная БД)

### Подписанный Release (опционально)
Для Play Store нужен подписанный APK. Добавьте в GitHub Secrets:
- `KEYSTORE_BASE64` — base64 вашего keystore.jks
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Workflow `build-signed` соберет подписанный APK при пуше тега.

## Версионирование
- `versionCode` — целое число, увеличивайте на 1 при каждом релизе
- `versionName` — строка "1.0.0", "1.0.1" и т.д.
- applicationId `com.bonuswallet.app` НЕ меняйте — иначе данные не сохранятся при обновлении

## Политика конфиденциальности
Находится в `TermsScreen.kt` и доступна в Настройки -> Политика конфиденциальности. Полный текст включает 18 разделов: данные, хранение, серверы, камера, удаление, безопасность, ответственность и т.д.

## Условия использования
Приложение независимое, не является официальным приложением Bravo/Wolt/KFC. Пользователь обязан иметь право использовать добавляемые карты. Запрещено мошенничество, подделка, использование чужих карт.

## Copyright and License
© 2026 BonusWallet. Все права защищены.

### Запрет на копирование
Исходный код, дизайн, структура приложения, графические элементы, тексты и другие оригинальные материалы BonusWallet не разрешается копировать, распространять, продавать или использовать в другом продукте без соответствующего разрешения правообладателя.

Проект НЕ распространяется по свободной open-source лицензии (MIT/Apache/GPL не применяются). Использование кода для создания производных продуктов запрещено.

## Безопасность
- Нет INTERNET permission по умолчанию (только CAMERA)
- Не логируем номера карт
- Все данные локальны
- Обработка ошибок для всех пользовательских сценариев

## Offline
Приложение работает полностью без интернета: открытие, показ, добавление, редактирование, удаление, импорт/экспорт (файл локальный).

## Проверка
Проект проверен на: Gradle 8.6, AGP 8.2.2, Kotlin 1.9.22, compileSdk 34, minSdk 24. Сборка через GitHub Actions без Android Studio.


## Как залить через Termux на телефоне

Ты на Android, без компьютера. Делаем через Termux:

### 1. Установи Termux
Из F-Droid (не из Play Store).

### 2. В Termux выполни:
```bash
pkg update && pkg upgrade -y
pkg install git unzip -y
cd /sdcard/Download
# если zip лежит в Download
unzip BonusWallet-1.0.0.zip -d BonusWallet
cd BonusWallet

# настрой git
git config --global user.email "you@example.com"
git config --global user.name "Your Name"

# создай репозиторий на github.com вручную (кнопка New repository, назови bonuswallet, НЕ ставь галочку README)

# теперь свяжи
git init
git add .
git commit -m "BonusWallet 1.0.0 first version"
git branch -M main
git remote add origin https://github.com/ТВОЙ_НИК/bonuswallet.git

# спросит логин - используй Personal Access Token
# GitHub -> Settings -> Developer settings -> Personal access tokens -> Generate new token (classic) -> поставь галочку repo
# Вместо пароля вставляешь токен

git push -u origin main
```

### 3. После push
- Открой github.com/ТВОЙ_НИК/bonuswallet в браузере телефона
- Перейди в Actions - увидишь сборку
- Через 3-5 минут появится BonusWallet-APK artifact

### Если git push падает с auth ошибкой:
В Termux:
```bash
git remote set-url origin https://ТОКЕН@github.com/ТВОЙ_НИК/bonuswallet.git
git push -u origin main
```

Готово.
