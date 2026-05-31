# 🍔 FoodApp — Zomato Clone

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange?style=for-the-badge)
![Tests](https://img.shields.io/badge/Tests-145%2B%20Passing-brightgreen?style=for-the-badge)
![Architecture](https://img.shields.io/badge/MVVM%20%2B%20Clean-E23744?style=for-the-badge)

**A production-style food ordering app built with Jetpack Compose, Clean Architecture, and a test-driven workflow (Kotest BehaviorSpec).**

[Architecture](#-architecture) • [Tech stack](#-tech-stack) • [Engineering decisions](#-engineering-decisions) • [Testing](#-testing) • [Setup](#-setup)

</div>

---

## Overview

FoodApp is a fully functional Zomato-style food ordering app for Android, built end-to-end as a self-directed engineering project. It covers the full flow — phone-based authentication, restaurant and menu browsing, search, cart, and checkout — backed by an offline-first data layer and a mock REST API.

The goal wasn't just a working app, but a demonstration of how I build production Android software: strict Clean Architecture boundaries, a test-driven workflow with 145+ tests written before or alongside the code, and deliberate, defensible choices at each layer.

> **Stack at a glance:** Kotlin · Jetpack Compose · MVVM + Clean Architecture · Hilt · Coroutines & Flow · Retrofit · Room · Firebase Phone Auth · Kotest (TDD) · MockK · Turbine

---

## ✨ Features

- **Phone authentication** — Firebase Phone Auth with OTP, modelled as a sealed-class state machine so impossible UI states can't occur.
- **Offline-first browsing** — Room emits cached data instantly while the network refreshes in the background; the UI updates reactively via Flow.
- **Debounced search** — `flatMapLatest` cancels stale in-flight queries on each keystroke, so only the latest search resolves.
- **Cart & checkout** — add/remove items, live total calculation, persisted across sessions.
- **Reactive UI** — single source of truth via `StateFlow`, unidirectional data flow, Compose throughout.

---

## 🏗️ Architecture

Clean Architecture with three layers and a strict dependency rule: **the domain layer has zero Android dependencies** and depends on nothing outward. The data and presentation layers depend inward on domain, never on each other.

```
┌─────────────────────────────────────────────────────────┐
│  PRESENTATION   Compose screens + ViewModels (StateFlow)  │
│                 auth · home · search · restaurant ·       │
│                 product · cart · profile                  │
└───────────────────────────┬─────────────────────────────┘
                            │  depends on
┌───────────────────────────▼─────────────────────────────┐
│  DOMAIN         Pure Kotlin — no Android imports          │
│                 models · repository interfaces · usecases │
└───────────────────────────▲─────────────────────────────┘
                            │  implements
┌───────────────────────────┴─────────────────────────────┐
│  DATA           Room (local) · Retrofit (remote) ·        │
│                 mappers · repository implementations      │
└─────────────────────────────────────────────────────────┘
```

**Data flow:** Compose UI → ViewModel → UseCase → Repository interface (domain) → Repository implementation (data) → Room + Retrofit. Results flow back as `Flow`, so the UI reacts to a single source of truth.

### Project structure

```
app/src/main/java/com/swapna/foodapp/
├── data/
│   ├── local/          ← Room DAOs + entities
│   ├── remote/         ← Retrofit service + DTOs
│   ├── mapper/         ← Entity ↔ domain mapping
│   └── repository/     ← Repository implementations
├── domain/
│   ├── model/          ← Pure Kotlin data classes
│   ├── repository/     ← Repository interfaces
│   └── usecase/        ← Business logic, one responsibility each
├── presentation/
│   ├── auth/           ← Login / OTP
│   ├── home/           ← Home
│   ├── search/         ← Search
│   ├── restaurant/     ← Restaurant + menu
│   ├── product/        ← Product detail
│   ├── cart/           ← Cart
│   └── profile/        ← Profile
├── di/                 ← Hilt modules
├── navigation/         ← NavGraph + routes
└── utils/              ← Constants + helpers
```

---

## 🛠️ Tech stack

| Category | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| Dependency injection | Hilt |
| Concurrency | Coroutines + Flow (`StateFlow`, `SharedFlow`) |
| Networking | Retrofit + OkHttp |
| Local persistence | Room |
| Authentication | Firebase Phone Auth |
| Image loading | Coil |
| Testing | Kotest (BehaviorSpec / DescribeSpec / FunSpec) |
| Mocking | MockK |
| Flow testing | Turbine |

---

## 💡 Engineering decisions

These are the "why" choices behind the code — the kind of reasoning the project was built to demonstrate.

**Sealed-class state for auth.** Login state is modelled as a sealed hierarchy (`Idle`, `Loading`, `OtpSent`, `Success`, `Error`) so the compiler enforces that only one state exists at a time — no nullable-flag soup, no impossible combinations.

**Offline-first, not network-first.** The splash screen checks Room (instant, works offline) rather than waiting on a network call to decide navigation. Cached content renders in well under the time a cold network request takes, and a fresh fetch updates the UI reactively once it returns.

**`flatMapLatest` for search.** Typing "p → pi → piz → pizza" fires four queries; `flatMapLatest` cancels each previous coroutine on the next keystroke so only the final query runs to completion — no stale results racing to the screen.

**Domain layer with no Android dependencies.** Use cases and models are pure Kotlin, which keeps business logic fast to unit-test on the JVM with no instrumentation.

---

## 🧪 Testing

Built with a test-driven workflow using **Kotest BehaviorSpec** (Given/When/Then), with **145+ tests** covering ViewModels, use cases, and repositories, plus Compose UI tests for the screens.

| Spec style | Layer under test |
|---|---|
| `BehaviorSpec` | ViewModels |
| `DescribeSpec` | Use cases |
| `FunSpec` | Repositories |
| Compose UI tests | Screens |

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run instrumented Compose UI tests (device/emulator required)
./gradlew connectedDebugAndroidTest
```

### A few testing problems worth noting

These were real failures solved during the build — included because the fixes are the interesting part:

- **`android.util.Log` crashing JVM unit tests.** `Log` is an Android framework stub that throws under plain JUnit. Resolved by stubbing it with `mockkStatic(Log::class)` so domain/ViewModel tests stay on the fast JVM path.
- **Flaky `LoginScreenTest` on API 36.** Stabilised using `createComposeRule()` with explicit `setContent`, dispatching work on the UI thread, and proper `@After` teardown to avoid state leaking between tests.
- **Firebase Play Integrity errors in test.** Resolved by registering a test phone number so OTP flows run without a real integrity challenge.

---

## ⚙️ Setup

### Prerequisites

- Android Studio (latest stable)
- JDK 17+
- Android SDK 34
- Minimum device: Android 7.0 (API 24)

### Steps

```bash
# 1. Clone
git clone https://github.com/SwapnaPrakash/FoodApp.git
cd FoodApp
```

2. Add your `google-services.json` to the `app/` directory (Firebase console → project settings).

3. Set the mock API base URL in `app/build.gradle.kts`:

```kotlin
buildConfigField("String", "BASE_URL", "\"https://swapnaprakash.github.io/zomato-mock-api/\"")
```

```bash
# 4. Build
./gradlew assembleDebug

# 5. Run on a connected device / emulator
./gradlew installDebug
```

### Mock API

The app is backed by a static mock REST API hosted on GitHub Pages, so it runs without a live backend. Endpoints return restaurants, menus, and related data as JSON.

---

## 📸 Screenshots

> Add screenshots to a `screenshots/` folder and they'll render here.

| Splash | Login | Home | Restaurant |
|---|---|---|---|
| _add image_ | _add image_ | _add image_ | _add image_ |

| Search | Product | Cart | Profile |
|---|---|---|---|
| _add image_ | _add image_ | _add image_ | _add image_ |

---

## 👤 Author

**Swapna Prakash** — Senior Android Developer

[LinkedIn](https://www.linkedin.com/in/swapna-prakash-3170b380/) · [GitHub](https://github.com/SwapnaPrakash)
