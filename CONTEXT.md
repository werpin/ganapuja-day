# Project

**Name:** ganapuja-day
**Repo:** github.com/<user>/ganapuja-day
**Goal:** Android-приложение с домашним виджетом для отображения текущего тибетского лунного дня (традиция Phugpa), подсветки особых дней практики (10, 15, 25, 30 — Ганапуджа / Цог), месячного вида с подписями лунных дней и перехода на описание дня на `tibetastromed.ru`.

**Stack:**
- Kotlin (JVM), Android, min SDK 24
- Jetpack Compose + Material 3
- Jetpack Glance (виджет)
- `cn.6tail:tyme4kt:1.5.0` (тибетский календарь, Phugpa)
- `AlarmManager.setAndAllowWhileIdle` + `WorkManager` fallback
- `LruCache(32)` + `Mutex` (in-memory, без БД)
- Gradle Kotlin DSL

**Key modules (целевые):**
- `core` — `TibetanDayRepository` (interface), `Tyme4KtRepository` (impl)
- `cache` — `TibetanDayCache` (LruCache + Mutex)
- `url` — `TibetAstroMedUrl` (формирование и валидация)
- `ui` — `DayScreen`, `MonthScreen`
- `widget` — `GlanceAppWidget`, `GlanceAppWidgetReceiver`
- `scheduler` — `AlarmScheduler`, `BootReceiver`

**Dev environment:** Debian 13 (trixie) + XFCE, Android Studio 2026.2.1, нативная установка (без Docker).

# State

**Done (Фаза 1):**
- ✅ `TibetanDay` — data-класс (year, month, day, isLeapMonth, isLeapDay, raw, isSpecial)
- ✅ `TibetanDayRepository` — интерфейс с `suspend fun get(date): TibetanDay`
- ✅ `Tyme4KtRepository` — реализация через `SolarDay.fromYmd().getRabByungDay()`
- ✅ `TibetanDayCache` — LinkedHashMap(accessOrder) + Mutex, maxSize=32
- ✅ Unit-тесты: 5/5 зелёные (losar, special day, leap year, cache hit, concurrency)

**In progress:**
- Фаза 2 (UI) — не начата.

**Broken / Unknown:**
- `[?]` Точный набор getter-методов у `RabByungDay` (для программного извлечения day/month/year вместо парсинга строки `第十七饶迥火马年正月初一`).
- `[?]` API `tyme4kt` для определения признака високосного дня/месяца (`isLeapMonth`, `isLeapDay`).

# Decisions

- **ADR-001:** Kotlin + Jetpack Compose + Jetpack Glance. Почему: виджет нативный, `tyme4kt` — Kotlin-native, оффлайн прост. Альтернативы (Flutter, KMP, React Native) дают кроссплатформенность вне скоупа и усложняют виджет.
- **ADR-002:** `AlarmManager.setAndAllowWhileIdle` (inexact) + `WorkManager.PeriodicWorkRequest(24h)` fallback вместо `setExactAndAllowWhileIdle`. Почему: `SCHEDULE_EXACT_ALARM` не гарантирован на Android 14+, а ±10 мин для суточного календаря приемлемо. `[DEBT-low]`: опциональный exact в настройках при жалобах.
- **ADR-003:** `TibetanDayCache` = `LruCache(32)` + `Mutex`, `suspend fun get(date)`. Почему: потокобезопасность для виджета и UI; БД не нужна. После 0.4 — кэш не критичен для производительности (p95 7.77 мс), но полезен для UI-плавности месячного вида. `[DEBT-med]`: Room только если понадобится навигация по годам.
- **ADR-004:** URL `tibetastromed.ru` формируется только из `Int`-компонентов (year, month, day) с `require()`-валидацией диапазонов. Формат `M/d/yyyy` в константе `URL_DATE_FORMAT`. Почему: защита от SSRF-подобных атак через deep links / шаринг в будущем.
- **ADR-005:** План работ — 5 фаз. Фазы 0.1 и 0.3 — жёсткие блокеры (пройдены). Фаза 0.4 — верификация гипотезы о производительности (пройдена).
- **ADR-006:** Не использовать Docker для dev-окружения. Почему: GUI-проброс X11, `/dev/kvm`, USB создают накладные расходы; нативная установка в `/opt` проще. Docker оправдан только в CI/CD.
- **ADR-007:** Не добавлять отдельный `tyme4kt-jvm`. Почему: артефакт не существует в Maven Central; `tyme4kt-android:1.5.0` уже JVM-совместимый таргет, APK 6.3 МБ.
- **ADR-008:** Не включать `isMinifyEnabled = true` на ранних фазах. Почему: усложняет отладку; APK 6.3 МБ укладывается в критерий. `[DEBT-low]` — включить в Фазе 4 перед публикацией.
- **ADR-009:** `RabByungDay.year` — **грегорианский год**, не номер Rabjung-цикла. Для отображения «17-й цикл, Огненная Лошадь» парсить `raw` (`第十七饶迥火马年正月初一`). Для логики `SPECIAL_DAYS` используются только `month` и `day`. Проверено эмпирически: для 2026-02-18 `year=2026`, `month=1`, `day=1`.
- **ADR-010:** `TibetanDayCache` использует `LinkedHashMap(accessOrder=true)` + `removeEldestEntry` вместо `android.util.LruCache`. Почему: `android.util.LruCache` не работает в JVM unit-тестах (stub-методы бросают RuntimeException). Весь доступ — через `Mutex`, включая быстрый путь. Плата — потеря «lock-free» чтения, выигрыш — детерминированные тесты и одинаковое поведение в JVM/Android.

# Constraints

- **Традиция:** только **Phugpa**. Tsurphu-расхождения — не баг. Указать в README и в описании на F-Droid.
- **Разрешения:** только `RECEIVE_BOOT_COMPLETED`. Запрещено: `INTERNET`, `WAKE_LOCK`, `SCHEDULE_EXACT_ALARM`, `FOREGROUND_SERVICE`.
- **Не использовать:** Room (до обоснованной необходимости), Foreground Service, `updatePeriodMillis` в XML виджета (игнорируется для значений < 30 мин).
- **`tyme4kt`:** зафиксировать версию `1.5.0`. Реализацию обернуть в `TibetanDayRepository` — при расхождениях подменить движок без переписывания UI.
- **URL:** валидация через `Int`, `require()` на диапазоны, константа `URL_DATE_FORMAT = "%d/%d/%d"` (month, day, year) в одном месте.
- **Формат URL подтверждён:** `M/d/yyyy`. Пример: 13 сентября 2026 → `tdat=9/13/2026`.
- **Дев-окружение:** нативная установка, не Docker.
- **Имена:** не использовать `LunarCache` / `LunarDay` (двусмысленно с астрономией). Использовать `TibetanDay*`.
- **Kotlin API `tyme4kt`:** метод называется `getRabByungDay()` (не свойство `rabByungDay`). Пример: `SolarDay.fromYmd(y, m, d).getRabByungDay()`.
- **`RabByungDay.month` и `day`** — использовать для логики. `year` — только грегорианский, для сортировки/отладки.
- **Високосный месяц:** `RabByungDay` не имеет отдельного `isLeapMonth()`. Отрицательный `month` означает високосный месяц (аналогично `day`). Проверить при первом попадании високосного месяца: `[?]`.
- **`android.util.LruCache`** не использовать — заменён на `LinkedHashMap` (см. ADR-010).

# Next

**Фаза 2: UI (5–7 дней) — следующая**

1. **Главный экран (DayScreen):**
   - Григорианская дата крупно.
   - Тибетская дата: `month` и `day` из `TibetanDay`, плюс `raw` для цикла/животного/элемента.
   - Индикатор особого дня (10, 15, 25, 30) — цветной бейдж.
   - Кнопка «Описание дня» → `Intent.ACTION_VIEW` с URL из `TibetAstroMedUrl` (Фаза 2 также).
2. **Месячный экран (MonthScreen):**
   - `LazyVerticalGrid(7)` — сетка календаря.
   - В ячейке — григорианское число + тибетский день (мелко).
   - Особые дни подсвечены.
   - Клик по дню → DayScreen для этой даты.
3. **Навигация:** `androidx.navigation:navigation-compose` (одна зависимость).
4. **Тема:** Material 3, цветовая схема — финальная (сейчас дефолтная из шаблона).
5. **Локализация:** `values/strings.xml` (en), `values-ru/strings.xml` (ru).
6. **URL-модуль (Фаза 1, отложен):** `TibetAstroMedUrl.from(date): String` с `require()`-валидацией. Тесты: 5 дат → ожидаемый URL.

**`[?]` уточнить перед Фазой 2:**
- Формат отображения тибетской даты: только `day` + `month` (например, «1 день 1 месяца») или с циклом/животным из `raw`?

**Фаза 3: Виджет (4–5 дней)**
- `GlanceAppWidget` + `GlanceAppWidgetReceiver`
- `AlarmScheduler` (`setAndAllowWhileIdle`) + `WorkManager` fallback
- `BootReceiver` на `RECEIVE_BOOT_COMPLETED`
- Клик по виджету → deep link в Activity или `ACTION_VIEW` на tibetastromed.ru
- Замер холодного старта < 500 мс

**Фаза 4: Публикация (2–3 дня)**
- `isMinifyEnabled = true`, `isShrinkResources = true` (закрыть `[DEBT-low]` ADR-008)
- F-Droid metadata
- README с дисклеймером о Phugpa
- Подписанный APK, воспроизводимая сборка

**Реестр `[DEBT]`:**
- `[DEBT-low]` Опциональный `SCHEDULE_EXACT_ALARM` в настройках (если жалобы на точность).
- `[DEBT-low]` `isMinifyEnabled = true` для release (Фаза 4).
- `[DEBT-med]` Room-кэш (если понадобится навигация по годам).
- `[DEBT-med]` Рефактор планировщика при проблемах с inexact alarm.

# Reference data sources

- **Svante Janson, "Tibetan Calendar Mathematics"** — arXiv:1401.6285. Формулы Phugpa и Tsurphu.
- **`tyme4kt`** — https://klibs.io/project/6tail/tyme4kt — официальная Kotlin-обёртка.
- **`tibetan_calendar`** (Dart/Flutter) — референс-реализация Phugpa.
- **`tibetan-date-calculator`** (JS/NPM) — для скриптовой сверки.
- **`calcal`** (R) — независимая реализация Phugpa (Dershowitz & Reingold).
- **Lotsawa House Phugpa Calculator** — https://www.lotsawahouse.org/Static/tools/phugpa.html — веб-эталон.
- **Merigar / Melong Phugpa Calendar** — эталон для Losar.
- **`tibetastromed.ru`** — источник описаний дней, целевой сайт для URL.

# Open questions (`[?]`)

1. **`RabByungDay` API:** какие getter-методы доступны для извлечения `year`, `month`, `day`, `isLeapMonth`, `isLeapDay` без парсинга строки `第十七饶迥火马年正月初一`? Проверить через `Ctrl+Space` в IDE или исходники `tyme4kt`.
2. **Високосные дни/месяцы:** как `tyme4kt` маркирует skipped/duplicate дни? Есть ли метод типа `isLeapDay()` у `RabByungDay`?