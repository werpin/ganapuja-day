# ganapuja-day

Android-приложение с домашним виджетом для отслеживания тибетского лунного календаря (традиция **Phugpa**). Подсвечивает особые дни практики (Ганапуджа / Цог), работает оффлайн, открывает описание дня на tibetastromed.ru.

## Возможности

- Текущий тибетский лунный день на главном экране и в домашнем виджете.
- Подсветка особых дней практики: **10, 15, 25, 30** лунные дни.
- Месячный вид: григорианский календарь с подписями лунных дней.
- Переход на описание дня: `https://tibetastromed.ru/docom.php?tdat=M/d/yyyy&tipv=0&type=new&lang=ru`.
- Полностью оффлайн: **нет разрешения `INTERNET`**, все расчёты локальные.
- Виджет с обновлением в полночь.

## Стек

| Слой | Технология |
|---|---|
| Язык | Kotlin (JVM) |
| UI | Jetpack Compose + Material 3 |
| Виджет | Jetpack Glance |
| Календарь | `cn.6tail:tyme4kt:1.5.0` (Phugpa) |
| Планировщик | `AlarmManager.setAndAllowWhileIdle` + `WorkManager` fallback |
| Кэш | `LruCache(32)` + `Mutex` (in-memory, без БД) |
| Сборка | Gradle Kotlin DSL |
| Дистрибуция | F-Droid + APK |
| Min SDK | API 24 (Android 7.0) |

## Требования к системе

- Debian 13 (trixie), 64-bit (`amd64`)
- RAM: минимум 8 ГБ, рекомендуется **16 ГБ+** (эмулятор)
- Диск: ~15 ГБ (IDE + SDK + эмулятор + Gradle cache)
- CPU: поддержка аппаратной виртуализации (Intel VT-x / AMD-V)
- Разрешение экрана: рекомендуется 1920×1080

## Установка окружения (Debian 13 + XFCE)

### 1. JDK

```bash
sudo apt update
sudo apt install -y default-jdk
java -version
# Ожидается: openjdk version "21.x"
```

> Debian 13 поставляет OpenJDK 21. Если проект потребует строго JDK 17 — установить Eclipse Temurin вручную.

### 2. KVM (критично для эмулятора)

```bash
sudo apt install -y qemu-kvm libvirt-daemon-system libvirt-clients \
                    bridge-utils cpu-checker
sudo adduser "$USER" kvm
sudo adduser "$USER" libvirt
# ВАЖНО: выйти из сессии и войти заново
kvm-ok
# Ожидается: INFO: /dev/kvm exists / KVM acceleration can be used
```

### 3. Android Studio 2026.2.1

```bash
cd ~/Downloads
wget https://redirector.gvt1.com/edgedl/android/studio/ide-zips/2026.2.1.20/android-studio-2026.2.1.20-linux.tar.gz
sudo tar -xzf android-studio-*.tar.gz -C /opt/
/opt/android-studio/bin/studio.sh
```

Setup Wizard: принять значения по умолчанию (SDK, Platform Tools, эмулятор).

### 4. Переменные окружения

Добавить в `~/.bashrc`:

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$PATH:$ANDROID_HOME/emulator"
export PATH="$PATH:$ANDROID_HOME/platform-tools"
```

Применить: `source ~/.bashrc`.

### 5. Настройки XFCE

- **Горячие клавиши:** `Настройки → Клавиатура → Сочетания клавиш`. Убрать конфликты с `Ctrl+Alt+...`, которые использует Android Studio.
- **Сессия:** при нестабильности эмулятора на Wayland — зайти в X11-сессию XFCE.

### 6. Ярлык в меню (опционально)

```bash
cat > ~/.local/share/applications/android-studio.desktop <<'EOF'
[Desktop Entry]
Version=1.0
Type=Application
Name=Android Studio
Exec=/opt/android-studio/bin/studio.sh %f
Icon=/opt/android-studio/icons/studio.svg
Categories=Development;IDE;
Terminal=false
StartupNotify=true
EOF
```

## Создание проекта (первый раз)

В Android Studio: **New Project → Empty Activity**.

| Поле | Значение |
|---|---|
| Name | `ganapuja-day` |
| Package name | `com.ganapujaday` |
| Save location | `~/hdd/projects/ganapuja-day` |
| Language | Kotlin |
| Minimum SDK | API 24 |

Затем инициализация Git:

```bash
cd ~/hdd/projects/ganapuja-day
git init -b main
git add .
git commit -m "initial commit: Empty Activity scaffold"
```

Создание GitHub-репозитория:

```bash
sudo apt install gh -y
gh auth login   # GitHub.com → HTTPS → browser
gh repo create ganapuja-day --public --source=. --remote=origin --push
```

## Сборка и запуск

```bash
# Debug
./gradlew :app:assembleDebug
./gradlew :app:installDebug

# Release
./gradlew :app:assembleRelease
# APK: app/build/outputs/apk/release/app-release-unsigned.apk

# Проверка зависимостей
./gradlew :app:dependencies --configuration releaseRuntimeClasspath | grep -i tyme
# Ожидается:
# +--- cn.6tail:tyme4kt:1.5.0
# |    \--- cn.6tail:tyme4kt-android:1.5.0
```

## Тесты

```bash
# Unit
./gradlew :app:testDebugUnitTest

# Только бенчмарк
./gradlew :app:testDebugUnitTest --tests "com.ganapujaday.Tyme4ktBenchmarkTest.benchmarkTibetanDayCalculation" -i

# Только сверка дат
./gradlew :app:testDebugUnitTest --tests "com.ganapujaday.Tyme4ktBenchmarkTest.printTibetanDatesForCrossCheck" -i

# Instrumented (нужен эмулятор / устройство)
./gradlew :app:connectedDebugAndroidTest
```

## Performance

Бенчмарк `tyme4kt 1.5.0` на JVM (100 вызовов, 8 дат включая граничные):

| Метрика | Значение |
|---------|----------|
| avg     | 6.40 ms  |
| p50     | 6.18 ms  |
| p95     | 7.77 ms  |
| max     | 11.32 ms |

Расчёт одной даты — **~6 мс**. Кэш `LruCache(32)` сокращает повторные вызовы до 0 мс, но не критичен для производительности. Месячный вид (30 дат) без кэша ≈ 190 мс, с кэшем ≈ 0 мс.

Измерено: unit test, warm JIT.

## Validation

Сверка 10 дат с эталонным Phugpa-калькулятором (Lotsawa House / Merigar / Xinhua):

**Losar 2026 = 2026-02-18** (подтверждено Xinhua, CGTN, Merigar/Melong, Indiana University).

| Date | tyme4kt output | Reference |
|------|----------------|-----------|
| 2026-02-18 | 第十七饶迥**火马年正月初一** (17-й Rabjung, Огненная Лошадь, 1-й месяц, 1-й день) | Losar = 18 Feb 2026, Fire Horse |
| 2026-02-17 | 木蛇年十二月三十 (Wood Snake, 12-й месяц, 30-й день) | Канун Losar |
| 2026-02-19 | 火马年正月初二 (Fire Horse, 1-й месяц, 2-й день) | 2-й день нового года |

**Результат: 10/10 совпадений.** `tyme4kt` корректно реализует Phugpa.

## Структура проекта (целевая)

```
app/
├── src/main/
│   ├── java/com/ganapujaday/
│   │   ├── core/           # TibetanDayRepository, Tyme4KtRepository
│   │   ├── cache/          # TibetanDayCache (LruCache + Mutex)
│   │   ├── url/            # TibetAstroMedUrl (валидация, форматирование)
│   │   ├── ui/             # Compose-экраны: DayScreen, MonthScreen
│   │   ├── widget/         # GlanceAppWidget, GlanceAppWidgetReceiver
│   │   └── scheduler/      # AlarmScheduler, BootReceiver
│   ├── test/java/com/ganapujaday/   # Unit-тесты, бенчмарки
│   └── AndroidManifest.xml
└── build.gradle.kts
```

## Разрешения в манифесте

Только эти:

```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>
```

Никаких `INTERNET`, `WAKE_LOCK`, `SCHEDULE_EXACT_ALARM`, `FOREGROUND_SERVICE`.

## Ограничения и нюансы

- **Традиция:** только **Phugpa**. Расхождения с Tsurphu-калькуляторами на 1–2 дня в месяц — ожидаемое поведение, не баг.
- **Точность обновления виджета:** ±10 минут от полуночи (inexact alarm).
- **Формат URL:** `M/d/yyyy` (месяц/день/год), подтверждён вручную.
- **Не использовать Docker** для dev-окружения — накладные расходы превышают выгоду. Docker оправдан только в CI/CD.

## Документация

- `CONTEXT.md` — структурированный контекст для AI-моделей и нового разработчика (состояние, ADR, ограничения, следующие шаги).

## Лицензия

[TBD]