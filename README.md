# Ganapuja Day

Android-приложение с домашним виджетом для отслеживания тибетского лунного календаря (традиция **Phugpa**). Подсвечивает особые дни практики (Ганапуджа / Цог), показывает фазы луны, работает оффлайн и открывает описание дня на tibetastromed.ru.

**Версия:** 1.0.0
**Лицензия:** MIT

## Возможности

- Текущий тибетский лунный день на главном экране и в домашнем виджете.
- Индикатор особых дней практики: **10, 15, 25, 30** лунные дни (Ганапуджа / Цог).
- Блок «Ближайший день Ганапуджи» с датой и лунным днём (скрывается, если сегодня уже особый день).
- Месячный вид: григорианский календарь с подписями лунных дней, иконками фаз луны и навигацией по месяцам.
- Переключатель темы: системная / светлая / тёмная.
- Переход на описание дня: `https://tibetastromed.ru/docom.php?tdat=M/d/yyyy&tipv=0&type=new&lang=ru`.
- Полностью оффлайн: **нет разрешения `INTERNET`**, все расчёты локальные.
- Виджет 2×1 с обновлением в полночь и перепланированием после перезагрузки.

## Стек

| Слой | Технология |
|---|---|
| Язык | Kotlin (JVM) |
| UI | Jetpack Compose + Material 3 |
| Виджет | Jetpack Glance 1.1.1 |
| Календарь | `cn.6tail:tyme4kt:1.5.0` (Phugpa) |
| Навигация | `androidx.navigation:navigation-compose:2.8.4` |
| Планировщик | `AlarmManager.setAndAllowWhileIdle` + `BootReceiver` |
| Кэш | `LinkedHashMap(accessOrder) + Mutex` (in-memory, без БД) |
| Сборка | Gradle Kotlin DSL, R8 (minify + shrink) |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 36 |

## Установка

Скачать APK из [Releases](https://github.com/<user>/ganapuja-day/releases) и установить на устройство с Android 8.0+.

Виджет: долгое нажатие на домашнем экране → **Виджеты** → **Ganapuja Day** → перетащить на экран.

## Сборка из исходников

### Требования

- Debian 13 (trixie), 64-bit (`amd64`) — или другая Linux-система
- JDK 21
- Android SDK (API 36)
- Android Studio 2026.2.1 (опционально, но рекомендуется)
- 8 ГБ RAM минимум, 16 ГБ рекомендуется для эмулятора

### Установка окружения

```bash
# JDK
sudo apt update
sudo apt install -y default-jdk
java -version  # openjdk version "21.x"

# KVM для эмулятора
sudo apt install -y qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils cpu-checker
sudo adduser "$USER" kvm
sudo adduser "$USER" libvirt
# ВАЖНО: выйти из сессии и войти заново
kvm-ok  # INFO: KVM acceleration can be used
```
Android Studio: скачать с https://developer.android.com/studio, распаковать в /opt, запустить Setup Wizard.
Сборка
```bash

git clone https://github.com/<user>/ganapuja-day.git
cd ganapuja-day

# Debug
./gradlew :app:assembleDebug
./gradlew :app:installDebug

# Release (подписанный)
./gradlew :app:assembleRelease
```
APK появится в app/build/outputs/apk/release/.
Тесты
```bash

./gradlew :app:testDebugUnitTest
```
Покрытие: 22 теста (репозиторий, кэш, URL, форматтер, особые дни).
Структура проекта
```text

app/src/main/java/com/ganapujaday/
├── core/
│   ├── TibetanDay.kt              # Модель дня
│   ├── TibetanDayRepository.kt    # Интерфейс
│   ├── Tyme4KtRepository.kt       # Реализация на tyme4kt
│   ├── TibetanDateFormatter.kt    # Форматирование для UI
│   ├── SpecialDays.kt             # Поиск ближайшего особого дня
│   └── MoonIcons.kt               # Выбор иконки фазы луны
├── cache/
│   └── TibetanDayCache.kt         # LRU + Mutex
├── url/
│   └── TibetAstroMedUrl.kt        # Формирование URL
├── ui/
│   ├── DayScreen.kt               # Главный экран
│   ├── DayViewModel.kt
│   ├── MonthScreen.kt             # Месячный календарь
│   ├── MonthViewModel.kt
│   ├── ThemeMenuButton.kt         # Переключатель темы
│   └── AppTheme.kt                # Material 3 + CompositionLocal
├── widget/
│   ├── GanapujaDayWidget.kt       # GlanceAppWidget
│   ├── GanapujaDayWidgetReceiver.kt
│   ├── WidgetScheduler.kt         # AlarmManager
│   ├── WidgetUpdateReceiver.kt    # DATE_CHANGED / TIME_CHANGED
│   └── BootReceiver.kt            # BOOT_COMPLETED
└── MainActivity.kt
```
Виджет

    Размер: 2×1 (горизонтальный).

    Содержимое: лунный день + иконка фазы луны. В особые дни — «Ганапуджа».

    Обновление: AlarmManager в полночь + BootReceiver для перепланирования после перезагрузки + WidgetUpdateReceiver на смену даты / часового пояса.

    Клик: открывает приложение.

    Ограничения:

        Только домашний экран (home_screen), не локскрин.

        Точность обновления: ±9 минут в Doze.

Производительность

Бенчмарк tyme4kt 1.5.0 на JVM (100 вызовов, 8 дат):
Метрика ↕
Значение ↕
avg	6.40 ms
p50	6.18 ms
p95	7.77 ms
max	11.32 ms

Расчёт одной даты — ~6 мс. Кэш LruCache(32) сокращает повторные вызовы до 0 мс.

Размер APK: 2.4 МБ (release с R8), 6.3 МБ (debug без R8).
Валидация

Сверка 10 дат с эталонным Phugpa-калькулятором (Lotsawa House / Merigar / Xinhua):

Losar 2026 = 2026-02-18 (подтверждено Xinhua, CGTN, Merigar, Indiana University).
Дата ↕
tyme4kt ↕Эталон ↕
2026-02-18	第十七饶迥火马年正月初一	Losar = 18 Feb 2026, Fire Horse
2026-02-17	木蛇年十二月三十	Канун Losar
2026-02-19	火马年正月初二	2-й день нового года

Результат: 10/10 совпадений. tyme4kt корректно реализует Phugpa.
Разрешения

Только RECEIVE_BOOT_COMPLETED для перепланирования виджета после перезагрузки. Нет INTERNET — все расчёты локальные, ссылка на tibetastromed.ru открывается через браузер.
Ограничения

    Традиция: только Phugpa. Расхождения с Tsurphu-калькуляторами на 1–2 дня в месяц — ожидаемое поведение, не баг.

    Особые дни: 10, 15, 25, 30. Логика задана в TibetanDay.SPECIAL_DAYS.

    Диапазон дат: 1900–2100 (ограничение TibetAstroMedUrl).

    Локализация: русский язык. Английский — [DEBT-low].

Документация

    CONTEXT.md — структурированный контекст для AI и нового разработчика.

    CHANGELOG.md — история версий.

Лицензия

MIT License. Автор: deepseek. Идея: werpin.
Благодарности

    Svante Janson, "Tibetan Calendar Mathematics" — математика Phugpa.

    6tail/tyme4kt — Kotlin-реализация тибетского календаря.

    tibetastromed.ru — описания дней.

    Material Design Icons — иконки фаз луны.