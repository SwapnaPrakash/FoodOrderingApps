# 🍔 Food Ordering App — Zomato Clone

<div align="center">

![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue?logo=jetpackcompose)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange)
![Tests](https://img.shields.io/badge/Tests-145%20Passing-brightgreen)
![Architecture](https://img.shields.io/badge/Architecture-MVVM%20Clean-red)

**A fully functional food ordering Android app built with Jetpack Compose, Clean Architecture, and Test-Driven Development using Kotest BehaviorSpec.**

[Features](#-features) • [Architecture](#-architecture) • [Tech Stack](#-tech-stack) • [Setup](#-setup) • [Tests](#-tests) • [API](#-mock-api)

</div>

## 🏗️ Architecture

Clean Architecture with MVVM — strictly 3 layers:

```
┌─────────────────────────────────────────┐
│         PRESENTATION LAYER              │
│   Jetpack Compose + ViewModels          │
│   HomeScreen  SearchScreen  LoginScreen │
└──────────────────┬──────────────────────┘
                   │ observes / calls
┌──────────────────▼──────────────────────┐
│           DOMAIN LAYER                  │
│   Pure Kotlin — ZERO Android imports    │
│   Repository Interfaces  UseCases       │
│   GetHomeDataUseCase  AddToCartUseCase  │
└──────────────────┬──────────────────────┘
                   │ implemented by
┌──────────────────▼──────────────────────┐
│             DATA LAYER                  │
│   Retrofit (GitHub Pages API)           │
│   Room Database (offline cache)         │
│   DTOs  Mappers  DAOs  Entities         │
└─────────────────────────────────────────┘
```

### Key Decisions

**Why sealed class for state?**
```kotlin
// Only ONE state at a time — compiler enforces it
sealed class AuthState {
    object Idle    : AuthState()
    object Loading : AuthState()
    data class OtpSent(val phone: String) : AuthState()
    data class Success(val user: User)    : AuthState()
    data class Error(val message: String) : AuthState()
}
```

**Why offline-first?**
```kotlin
// 1. Room emits cache instantly (<100ms) → user sees content
// 2. Network fetches fresh data in background
// 3. Room updates → UI refreshes via Flow
// 4. If offline: banner shown, cached data still visible
```

**Why flatMapLatest for search?**
```kotlin
// User types "p" → "pi" → "piz" → "pizza" fast
// flatMapLatest cancels previous coroutine on each keystroke
// Only "pizza" runs to completion — no stale results
_query.debounce(300).flatMapLatest { searchUseCase(it) }
```

---

## 🛠️ Tech Stack

| Category | Technology | Version |
|---|---|---|
| **Language** | Kotlin | 1.9.22 |
| **UI** | Jetpack Compose | BOM 2024.02.00 |
| **DI** | Hilt | 2.50 |
| **Navigation** | Navigation Compose | 2.7.7 |
| **Network** | Retrofit + OkHttp | 2.9.0 / 4.12.0 |
| **Local DB** | Room | 2.6.1 |
| **Images** | Coil | 2.5.0 |
| **State** | StateFlow + SharedFlow | Coroutines 1.7.3 |
| **Testing** | Kotest BehaviorSpec | 5.8.0 |
| **Mocking** | MockK | 1.13.9 |
| **Flow Testing** | Turbine | 1.0.0 |

---

## ⚙️ Setup

### Prerequisites
- Android Studio Hedgehog+
- JDK 17+
- Android SDK 34
- Min device: Android 7.0 (API 24)

### Step 1 — Clone
```bash
git clone https://github.com/YOUR_USERNAME/FoodAppOrdering.git
cd FoodAppOrdering
```

### Step 2 — Set Base URL
In `app/build.gradle.kts`:
```kotlin
buildConfigField(
    "String", "BASE_URL",
    "\"https://YOUR_USERNAME.github.io/zomato-mock-api/\""
)
```

### Step 3 — Build
```bash
./gradlew assembleDebug
```

### Step 4 — Run Tests
```bash
# All tests
./gradlew testDebugUnitTest --tests "*Spec"

# Coverage report
./gradlew koverHtmlReport
# View: app/build/reports/kover/html/index.html
```

---

## 🧪 Tests — 145 Passing, 0 Failures

| Spec | Style | Tests | Layer |
|---|---|---|---|
| `AuthViewModelSpec` | BehaviorSpec | 15 | Presentation |
| `HomeViewModelSpec` | BehaviorSpec | 19 | Presentation |
| `HomeViewModelLocationSpec` | BehaviorSpec | 10 | Presentation |
| `SearchViewModelSpec` | BehaviorSpec | 32 | Presentation |
| `CartViewModelSpec` | BehaviorSpec | 18 | Presentation |
| `RestaurantRepositorySpec` | FunSpec | 13 | Data |
| `GetHomeDataUseCaseSpec` | DescribeSpec | 6 | Domain |
| `SearchRestaurantsUseCaseSpec` | DescribeSpec | 16 | Domain |
| `CalculateCartTotalUseCaseSpec` | DescribeSpec | 16 | Domain |
| **TOTAL** | | **145** | **0 failures** |

### Test Style
```kotlin
// BehaviorSpec — reads like documentation
given("phone number is valid 10 digits") {
    `when`("sendOtp succeeds") {
        then("state should be OtpSent") {
            coEvery { userRepository.sendOtp("9876543210") } returns
                Result.success(Unit)
            viewModel.sendOtp("9876543210")
            viewModel.state.value shouldBe
                AuthViewModel.AuthState.OtpSent("9876543210")
        }
    }
}
```

```bash
# Run live — expected output:
# 145 tests completed, 0 failed
# BUILD SUCCESSFUL
./gradlew testDebugUnitTest --tests "*Spec"
```

---

## 📁 Project Structure

```
com.swapna.foodapp/
├── di/                      # Hilt modules
│   ├── NetworkModule.kt     # Retrofit, OkHttp, FoodApi
│   ├── DatabaseModule.kt    # Room, all DAOs
│   ├── DispatcherModule.kt  # @IoDispatcher qualifiers
│   └── RepositoryModule.kt  # @Binds interfaces → impls
│
├── domain/                  # Pure Kotlin — no Android
│   ├── model/               # Restaurant, MenuItem, CartItem
│   ├── repository/          # Interfaces only (contracts)
│   └── usecase/             # Business logic
│       ├── home/GetHomeDataUseCase.kt
│       ├── cart/AddToCartUseCase.kt
│       ├── cart/CalculateCartTotalUseCase.kt
│       └── search/SearchRestaurantsUseCase.kt
│
├── data/                    # Repository implementations
│   ├── remote/dto/          # JSON shapes from API
│   ├── remote/api/FoodApi.kt
│   ├── local/entity/        # Room entities
│   ├── local/dao/           # Room DAOs
│   ├── mapper/              # DTO/Entity → Domain
│   └── repository/          # Offline-first impls
│
├── presentation/
│   ├── auth/                # Login + AuthViewModel
│   ├── home/                # Home + HomeViewModel
│   ├── search/              # Search + SearchViewModel
│   └── common/              # RatingBadge, VegBadge, ErrorScreen
│
├── navigation/
│   ├── AppRoutes.kt         # All route constants
│   └── AppNavGraph.kt       # NavHost
│
└── utils/
    ├── AppConstants.kt      # All app constants
    ├── ConnectivityObserver.kt
    └── Resource.kt

ui/theme/
    ├── AppColors.kt         # All color constants
    ├── AppDimensions.kt     # All dp/sp values
    ├── AppTypography.kt     # Font scale
    ├── AppShapes.kt         # Corner radii
    └── AppAnimations.kt     # Durations
```

---

## 🌐 Mock API

GitHub Pages serves static JSON matching exact Zomato API structure.

| Endpoint | Description |
|---|---|
| `GET /geocode.json` | Nearby restaurants |
| `GET /categories.json` | Food categories |
| `GET /collections.json` | Offer collections |
| `GET /search.json` | Search results |
| `GET /dailymenu.json` | Restaurant menu items |
| `GET /reviews.json` | Restaurant reviews |
| `GET /user.json` | User profile |
| `GET /orders.json` | Order history |

> **Important:** API returns `latitude` as String `"12.9352"`, rating as String `"4.6"`, price as `"249 Rs."` — all conversions handled in mappers.

```kotlin
// Mapper handles all type conversions safely:
rating = dto.rating.rating.toDoubleOrNull() ?: 0.0  // "4.6" → 4.6
price  = dto.price.parsePriceString()               // "249 Rs." → 249.0
votes  = dto.votes.replace(",","").toIntOrNull() ?: 0 // "12,547" → 12547
```

---

## 📐 Constants — Zero Magic Numbers

```kotlin
// ❌ Never this
Button(modifier = Modifier.height(52.dp))
val tax = subtotal * 0.05

// ✅ Always this  
Button(modifier = Modifier.height(Dimens.ButtonHeight))
val tax = subtotal * AppBusinessRules.GST_RATE
```

| File | Contains |
|---|---|
| `AppConstants.kt` | Timeouts, DB name, OTP length, cache duration |
| `AppBusinessRules.kt` | GST rate, delivery fee, cart limits, ratings |
| `AppDimensions.kt` | Every dp/sp value used in UI |
| `AppColors.kt` | Brand + semantic + rating colors |
| `AppAnimations.kt` | Shimmer, fade, debounce durations |
| `AppTestConstants.kt` | Test-only constants (never in production) |

---

## 🔐 Authentication

```
Demo Mode (current):
  Phone: any 10-digit number
  OTP:   any 6-digit number
  → Mock 1s delay, always succeeds

Production (Firebase Phone Auth):
  1. Enable Phone Auth in Firebase Console
  2. Add google-services.json → app/ folder
  3. Add SHA-1: ./gradlew signingReport
  4. Switch UserRepositoryImpl to FirebaseAuthManager
  (AuthViewModel + all tests unchanged — only impl swaps)
```
## 👩‍💻 Author

**Swapna** — Android Developer — PIP Assessment Project

Built with Jetpack Compose + Clean Architecture + Kotest TDD

---

<div align="center">

`Jetpack Compose` • `MVVM Clean` • `Kotest BDD` • `Room` • `Hilt` • `Retrofit` • `TDD`

</div>
