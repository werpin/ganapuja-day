# Project

**Name:** Ganapuja Day
**Repo:** github.com/<user>/ganapuja-day
**Version:** 1.0.0
**License:** MIT
**Goal:** Android-приложение с домашним виджетом для отображения тибетского лунного дня (традиция Phugpa), подсветки особых дней практики (10, 15, 25, 30 — Ганапуджа / Цог), месячного вида с навигацией и иконками фаз луны, переключателя темы и перехода на описание дня на `tibetastromed.ru`.

**Stack:**
- Kotlin (JVM), Android, min SDK 26, target SDK 36
- Jetpack Compose + Material 3
- Jetpack Glance 1.1.1 (виджет)
- `cn.6tail:tyme4kt:1.5.0` (тибетский календарь, Phugpa)
- `androidx.navigation:navigation-compose:2.8.4`
- `AlarmManager.setAndAllowWhileIdle` + `BootReceiver` + `WidgetUpdateReceiver`
- `LinkedHashMap(accessOrder=true) + Mutex` (in-memory кэш, без БД)
- R8: `isMinifyEnabled = true`, `isShrinkResources = true`
- Gradle Kotlin DSL

**Key modules:**
- `core/` — `TibetanDay`, `TibetanDayRepository`, `Tyme4KtRepository`, `TibetanDateFormatter`, `SpecialDays`, `MoonIcons`
- `cache/` — `TibetanDayCache` (LruCache-эквивалент + Mutex)
- `url/` — `TibetAstroMedUrl`
- `ui/` — `DayScreen`, `DayViewModel`, `MonthScreen`, `MonthViewModel`, `ThemeMenuButton`, `AppTheme`
- `widget/` — `GanapujaDayWidget`, `GanapujaDayWidgetReceiver`, `WidgetScheduler`, `WidgetUpdateReceiver`, `BootReceiver`
- `MainActivity` — точка входа, `NavHost`

**Dev environment:** Debian 13 (trixie) + XFCE, JDK 21, Android Studio 2026.2.1, KVM, нативная установка (без Docker).

# State

**Done (Фазы 0–4):**

**Фаза 0 — Спайк и замеры:**
- ✅ Формат URL подтверждён: `M/d/yyyy` (месяц/день/год).
- ✅ Окружение: Debian 13 + XFCE, JDK 21, KVM, Android Studio 2026.2.1.
- ✅ Зависимости: `tyme4kt:1.5.0` → `tyme4kt-android:1.5.0` (без iOS/Wasm/JS).
- ✅ Бенчмарк: p50 = 6.18 мс, p95 = 7.77 мс (гипотеза 50–200 мс опровергнута).
- ✅ Сверка 10 дат с эталоном: 10/10.

**Фаза 1 — Ядро:**
- ✅ `TibetanDay`, `TibetanDayRepository`, `Tyme4KtRepository`.
- ✅ `TibetanDayCache` (LinkedHashMap + Mutex).
- ✅ `TibetAstroMedUrl` (валидация через `require()`).
- ✅ `TibetanDateFormatter` (парсинг raw, без склонений).
- ✅ `SpecialDays.findNext` (поиск вперёд до 60 дней).
- ✅ `MoonIcons.moonIconFor` (4 фазы).
- ✅ Unit-тесты: 22 (core, cache, url, formatter, special).

**Фаза 2 — UI:**
- ✅ `DayScreen`: «Сегодня: <дата>», лунный день крупно, бейдж «Ганапуджа», кнопка «Описание», блок «Ближайший день Ганапуджи» (скрыт, если сегодня особый).
- ✅ `MonthScreen`: `LazyVerticalGrid(7)`, навигация по месяцам (←/→), иконки фаз луны в ячейках, обводка сегодняшнего дня.
- ✅ Навигация через `epochDay` (long), клик по дню → `DayScreen`.
- ✅ Переключатель темы (system/light/dark) через `LocalThemeController` (`CompositionLocalProvider`).
- ✅ Тема сохраняется в `SharedPreferences`.

**Фаза 3 — Виджет:**
- ✅ `GlanceAppWidget` 2×1, прозрачный фон, число + иконка фазы луны + «Ганапуджа».
- ✅ `AlarmManager.setAndAllowWhileIdle` на полночь.
- ✅ `BootReceiver` — перепланирование после reboot.
- ✅ `WidgetUpdateReceiver` — реакция на `DATE_CHANGED`, `TIMEZONE_CHANGED`, `TIME_SET`.
- ✅ Клик по виджету → `MainActivity`.

**Фаза 4 — Релиз (в работе):**
- ✅ R8: `isMinifyEnabled = true`, `isShrinkResources = true`. APK 6.3 → 2.4 МБ.
- ✅ ProGuard-правила для `tyme4kt` и Glance.
- ⚠️ `signingConfig` временно = debug для тестирования release. **Заменить на настоящий keystore перед публикацией.** `[DEBT-high]`.
- ⏳ `README.md`, `CONTEXT.md` — обновлены.
- ⏳ `CHANGELOG.md` — создан.
- ⏳ APK на GitHub Releases — не опубликован.

**In progress:**
- Финальная подпись APK (настоящий keystore).
- Публикация APK на GitHub Releases.

**Broken / Unknown:**
- `[?]` Високосные дни/месяцы в `RabByungDay`: как `tyme4kt` маркирует skipped/duplicate дни. `[DEBT-low]`.
- `[?]` `RabByungDay` не экспонирует признак високосного месяца отдельным геттером — `isLeapMonth` может быть всегда `false`. `[DEBT-low]`.

# Decisions

**ADR-001:** Kotlin + Jetpack Compose + Jetpack Glance. Почему: виджет нативный, `tyme4kt` — Kotlin-native, оффлайн прост. Альтернативы (Flutter, KMP, React Native) дают кроссплатформенность вне скоупа и усложняют виджет.

**ADR-002:** `AlarmManager.setAndAllowWhileIdle` (inexact) вместо `setExactAndAllowWhileIdle`. Почему: `SCHEDULE_EXACT_ALARM` не гарантирован на Android 14+, а ±9 мин для суточного календаря приемлемо. `[DEBT-low]`: WorkManager fallback, если появятся жалобы.

**ADR-003:** `TibetanDayCache` = `LinkedHashMap(accessOrder=true)` + `Mutex`, `suspend fun get(date)`. Почему: `android.util.LruCache` не работает в JVM unit-тестах (stub-методы бросают RuntimeException). Весь доступ через `Mutex`, включая быстрый путь. Плата — потеря lock-free чтения.

**ADR-004:** URL `tibetastromed.ru` формируется только из `Int`-компонентов с `require()`-валидацией диапазонов 1900..2100. Формат `M/d/yyyy` в константе. Почему: защита от SSRF-подобных атак через deep links / шаринг.

**ADR-005:** План работ — 5 фаз. Фазы 0.1 и 0.3 — жёсткие блокеры (пройдены). Фаза 0.4 — верификация гипотезы о производительности (пройдена).

**ADR-006:** Не использовать Docker для dev-окружения. Почему: GUI-проброс X11, `/dev/kvm`, USB создают накладные расходы; нативная установка в `/opt` проще. Docker оправдан только в CI/CD.

**ADR-007:** Не добавлять отдельный `tyme4kt-jvm`. Почему: артефакт не существует в Maven Central; `tyme4kt-android:1.5.0` уже JVM-совместимый таргет.

**ADR-008:** `isMinifyEnabled = true` включать только в Фазе 4. Почему: усложняет отладку. APK 6.3 МБ укладывался в критерий.

**ADR-009:** `RabByungDay.year` — **грегорианский год**, не номер Rabjung-цикла. Для отображения цикла (17-й, Огненная Лошадь) парсить `raw` (`第十七饶迥火马年正月初一`). Для логики `SPECIAL_DAYS` используются только `month` и `day`. Проверено эмпирически: для 2026-02-18 `year=2026`, `month=1`, `day=1`.

**ADR-010:** `TibetanDayCache` использует `LinkedHashMap(accessOrder=true) + removeEldestEntry` вместо `android.util.LruCache`. См. ADR-003.

**ADR-011:** `TibetAstroMedUrl` — объект-утилита с единственным методом `from(date): String`. Диапазон года 1900..2100. Валидация через `require()` на Int-полях `LocalDate`.

**ADR-012:** Навигация через `epochDay` (Long), не ISO-строку. Почему: проще парсить, нет проблем с локалью, компактнее. `LocalDate.ofEpochDay(...)` и `LocalDate.toEpochDay()` — симметричны.

**ADR-013:** `LocalThemeController` через `CompositionLocalProvider` (`staticCompositionLocalOf`) вместо `ThemeSetterHolder`. Почему: идиоматичный Compose-способ шаринга состояния, не требует проброса параметров через все экраны.

**ADR-014:** Тема хранится в `SharedPreferences`, не DataStore. Почему: DataStore требует ещё одну зависимость ради одного enum. `[DEBT-low]`: перейти при первой реконфигурации хранилища.

**ADR-015:** `MonthViewModel.load` использует `while`-цикл, не `Sequence.map`. Почему: `suspend fun cache.get()` нельзя вызвать из не-suspend лямбды `Sequence.map`.

**ADR-016:** Виджет без WorkManager fallback. Почему: `AlarmManager` + `BootReceiver` + `WidgetUpdateReceiver` покрывают 95% устройств. `[DEBT-low]`: добавить `WorkManager` при жалобах на MIUI/EMUI.

**ADR-017:** Виджет: `updatePeriodMillis="0"`. Почему: не полагаемся на системное обновление (< 30 минут игнорируется, точность не гарантирована).

**ADR-018:** Виджет: `Intent` создаётся в `provideGlance`, передаётся в `WidgetContent` параметром. Почему: Glance не наследует `context` в `@Composable`, а `actionStartActivity` принимает только `Intent` в версии 1.1.1.

**ADR-019:** Иконки фаз луны: 4 vector-drawable (waxing/full/waning/new), tinted через `ColorFilter.tint(ColorProvider)`. `moonIconFor` вынесен в `core`, шарится между UI и виджетом. Почему: единая логика выбора иконки для обоих контекстов.

**ADR-020:** R8: `isMinifyEnabled = true`, `isShrinkResources = true`, ProGuard-правила для `tyme4kt` и Glance. Почему: APK 6.3 → 2.4 МБ (62% уменьшение). Проверено: ничего не сломалось.

**ADR-021:** `signingConfig = signingConfigs.debug` для release временно. Почему: позволяет тестировать release-сборку на эмуляторе. **Заменить на настоящий keystore перед публикацией.** `[DEBT-high]`.

**ADR-023:** Двойной механизм обновления виджета для MIUI/EMUI/Oppo:
- `AlarmManager.setAndAllowWhileIdle` — точное срабатывание в полночь на «чистом» Android.
- `WorkManager.PeriodicWorkRequest(24h)` — гарантированное обновление в течение суток, даже если alarm заблокирован.
- `scheduleNext()` (оба) — только из `onEnabled` и `BootReceiver`.
- `rescheduleAlarm()` (только alarm) — из `WidgetUpdateReceiver` и `WidgetUpdateWorker`.
- `Result.success()` вместо `Result.retry()` в Worker — не останавливаем 24-часовой цикл при разовом сбое.

# Constraints

- **Традиция:** только **Phugpa**. Tsurphu-расхождения — не баг. Указано в README.
- **Разрешения:** только `RECEIVE_BOOT_COMPLETED`. Запрещено: `INTERNET`, `WAKE_LOCK`, `SCHEDULE_EXACT_ALARM`, `FOREGROUND_SERVICE`.
- **Не использовать:** Room (до обоснованной необходимости), Foreground Service, `updatePeriodMillis` в XML виджета.
- **`tyme4kt`:** версия `1.5.0`. Обёрнут в `TibetanDayRepository` — при расхождениях подменить движок без переписывания UI.
- **URL:** валидация через `Int`, `require()` на диапазоны, константа формата в одном месте. Формат `M/d/yyyy` подтверждён вручную.
- **Котлин API `tyme4kt`:**
  - `SolarDay.fromYmd(y, m, d).getRabByungDay()` — метод, не свойство.
  - `rab.year`, `rab.month`, `rab.day` — Kotlin properties, не Java getters (`getYear()` не существует).
  - `GlanceTheme` — из `androidx.glance`, не из `androidx.glance.material3`.
  - `dp` / `sp` — из `androidx.compose.ui.unit`, не из `androidx.glance.unit` (в Glance 1.1.1 их там нет).
  - `ColorFilter` — из `androidx.glance`, не из `androidx.glance.color`.
- **Имена:** не использовать `LunarCache` / `LunarDay` (двусмысленно с астрономией). Использовать `TibetanDay*`.
- **Дев-окружение:** нативная установка, не Docker.
- **Локализация:** русский. Английский — `[DEBT-low]`.
- **Виджет:** только `home_screen`, не `keyguard`.

# Next

**Фаза 4 — оставшиеся шаги:**

1. **Настоящая подпись APK** (`[DEBT-high]` ADR-021):
   - Создать keystore: `keytool -genkey -v -keystore ganapuja-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias ganapuja`
   - Настроить `signingConfigs` в `build.gradle.kts` с чтением пароля из `local.properties` или env.
   - Заменить `signingConfig = signingConfigs.debug` на `signingConfig = signingConfigs.release`.
   - `.gitignore`: `*.jks`, `local.properties`.
2. **Публикация APK на GitHub Releases:**
   - `git tag v1.0.0`
   - `git push origin v1.0.0`
   - На GitHub: Releases → New release → загрузить `app-release.apk`.
3. **Финальная проверка:**
   - Пройти чек-лист на чистом устройстве / эмуляторе с чистой установкой.
   - Проверить, что виджет добавляется, обновляется в полночь, перепланируется после reboot.

**Реестр `[DEBT]`:**

- `[DEBT-high]` Настоящая подпись APK (keystore, `signingConfigs.release`).
- `[DEBT-low]` WorkManager fallback для виджета (MIUI/EMUI).
- `[DEBT-low]` DataStore вместо `SharedPreferences` для темы.
- `[DEBT-low]` Английская локализация.
- `[DEBT-low]` Параметризация маршрута `month/{year}/{month}` (сейчас месяц не сохраняется при возврате из `DayScreen`).
- `[DEBT-low]` Ограничение диапазона навигации по месяцам (1900..2100).
- `[DEBT-low]` `SavedStateHandle` в `MonthViewModel` для сохранения месяца.
- `[DEBT-low]` Обработка високосных дней/месяцев в `RabByungDay`.
- `[DEBT-low]` Instrumented-тест для `MonthScreen`, виджета, `SpecialDays`.

# Reference data sources

- **Svante Janson, "Tibetan Calendar Mathematics"** — arXiv:1401.6285. Формулы Phugpa и Tsurphu.
- **`tyme4kt`** — https://github.com/6tail/tyme4kt — Kotlin-реализация.
- **`tibetan_calendar`** (Dart/Flutter) — референс-реализация Phugpa.
- **`tibetan-date-calculator`** (JS/NPM) — для скриптовой сверки.
- **`calcal`** (R) — независимая реализация Phugpa (Dershowitz & Reingold).
- **Lotsawa House Phugpa Calculator** — https://www.lotsawahouse.org/Static/tools/phugpa.html — веб-эталон.
- **Merigar / Melong Phugpa Calendar** — эталон для Losar.
- **`tibetastromed.ru`** — источник описаний дней, целевой сайт для URL.
- **Material Design Icons** — https://materialdesignicons.com — иконки фаз луны.