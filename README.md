#   Feet 

<div align="center">

![Feet Logo](https://img.shields.io/badge/Feet-Fitness%20App-4CAF50?style=for-the-badge&logo=android&logoColor=white)

**Track your steps, hydration, and workouts with a beautiful glassmorphic UI**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpack-compose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Personal-orange?style=flat-square)](LICENSE)

[Features](#-features) • [Tech Stack](#-tech-stack) • [Installation](#-installation) • [Architecture](#-architecture)

</div>

---

## About

 Modern, Activity tracking application built with Jetpack Compose and Material Design 3 which features a glassmorphic UI.

---

## Features

### Step Tracking
- **Live Step Counter** - Real-time step counting using device sensors
- **Daily Goals** - Set and track your daily step targets
- **Historical Data** - View last 30 days of step history with calendar view
- **Progress Visualization** - Reactive progress bars and charts
- **Distance & Calories** - Automatic calculation based on steps
- **Simulation Mode** - Manual step addition for testing or non-sensor devices

### Running Tracer
- **Live Run details** - Real-time tracking of distance, time, pace, and elevation gain
- **GPS Path Tracing** - Live route visualization on a dark-themed Google Map
- **Run Analytics** - Detailed details modal with traced path drawing and interactive zoom
- **Performance Charts** - Dynamic Speed and Pace charts for every run session
- **Historical Records** - View and manage your past running activities

### Water Tracking
- **Glass-Based Tracking** - Add/remove water by glass
- **Customizable Glass Size** - Set your preferred glass size (ml)
- **Daily Goal Management** - Customize your hydration targets
- **Glass Animation** - Fluid liquid fill animations
- **Quick Actions** - Preset buttons for common glass sizes (100ml, 250ml, 500ml)
- **History View** - Track last 10 days of hydration

### Workout Management
- **Automatic Persistence** - Daily workouts automatically carry over from the previous day
- **Workout History** - Quick-view cards for the last 3 days and an all-time history viewer
- **Custom Workouts** - Create personalized workout plans with reps or duration goals
- **Edit & Delete** - Refined management with icon-based quick actions
- **Progress Tracking** - Real-time percentage completion for daily targets

### Security & Polish
- **API Key Hiding** - API keys are protected using the Plugins
- **Frosted Blur Edges** - Custom fading edges on all scrollable lists for a premium glass look
- **Bouncy Interactions** - Physics-based spring animations on all clickable elements
- **Glassmorphic UI** - High-fidelity translucent components and vibrant gradients

### Weather Integration
- **Live Weather Data** - Real-time weather using GPS location
- **Automatic Location** - GPS-based city detection
- **Weather Icons** - Beautiful weather condition icons (☀️ ⛅ ☁️ 🌧️ ⛈️ ❄️ 🌫️)
- **Temperature Display** - Current temperature in Celsius
- **Privacy Option** - Phone-cached weather for offline tracking

### Media Integration
- **Now Playing** - Display currently playing music
- **Artist Information** - Shows track and artist name
- **Media Controls** - Quick access to music player
- **Notification Listener** - Seamless integration with music apps

### Home Screen Widgets
- **Steps Widget** - Quick step tracking from home screen
  - Current step count
  - Progress bar
  - +100 steps button
  - Reset functionality
- **Water Widget** - Hydration tracking widget
  - Current water intake
  - Glass counter
  - Add/Remove glass buttons
  - Progress visualization

### UI/UX
- **Glassmorphic Design** - Modern frosted glass aesthetic
- **Animated Transitions** - Smooth animations throughout
- **Color Bends** - Dynamic gradient backgrounds
- **Custom Components** - Liquid glass buttons and cards
- **Dark Theme** - Eye-friendly dark mode design
- **Responsive Layout** - Adapts to different screen sizes

### Data Persistence
- **Room Database** - Local SQLite database for data storage
- **Automatic Sync** - Real-time data synchronization
- **90-Day History** - Automatic retention of 3 months data
- **User Preferences** - Saved settings and customizations
- **Data Backup** - Reliable data persistence across sessions

---

## Tech Stack

### Core Technologies
- **Language:** Kotlin 1.9+
- **UI Framework:** Jetpack Compose
- **Minimum SDK:** Android 8.0 (API 26)
- **Target SDK:** Android 14 (API 34)

### Architecture & Components
- **Architecture Pattern:** MVVM (Model-View-ViewModel)
- **Dependency Injection:** Manual DI with Repository pattern
- **Database:** Room (SQLite wrapper)
- **Coroutines:** Kotlin Coroutines for asynchronous operations
- **State Management:** StateFlow & MutableState

### Jetpack Libraries
```kotlin
// UI & Compose
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.activity:activity-compose")

// Navigation
implementation("androidx.navigation:navigation-compose")

// Lifecycle & ViewModel
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx")
implementation("androidx.lifecycle:lifecycle-runtime-ktx")
implementation("androidx.lifecycle:lifecycle-runtime-compose")

// Room Database
implementation("androidx.room:room-runtime")
implementation("androidx.room:room-ktx")
kapt("androidx.room:room-compiler")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android")

// Sensors
implementation("androidx.core:core-ktx")

// Widgets
implementation("androidx.glance:glance-appwidget")
```

### APIs & Services
- **Weather API:** wttr.in (free, no API key required)
- **Location Services:** Android Location Manager
- **Sensors:** Android Step Counter Sensor
- **Media:** Notification Listener Service

### Build Tools
- **Gradle:** 8.0+
- **Kotlin Plugin:** 1.9.0
- **Kapt:** For Room annotation processing

---

## Project Structure

```
com.example.feet/
├── data/
│   ├── database/
│   │   ├── Entities.kt          # Room entities (Steps, Water, Workouts, Runs, Timers)
│   │   ├── Daos.kt              # Data Access Objects
│   │   └── AppDatabase.kt       # Database instance
│   └── repository/
│       └── FitnessRepository.kt # Repository layer
│
├── ui/
│   ├── screens/
│   │   ├── StepsScreen.kt       # Steps tracking UI
│   │   ├── Enhancedwaterscreen.kt # Hydration tracking UI
│   │   ├── WorkoutScreen.kt     # Workout management & History UI
│   │   ├── RunningSection.kt    # Running tracker & Map details UI
│   │   ├── TimeScreen.kt        # Timers & Stopwatch UI
│   │   └── MainScreen.kt        # Navigation container
│   │
│   ├── components/
│   │   ├── LiquidGlassButton.kt # Premium glass button
│   │   ├── TranslucentBox.kt    # Frosted glass container
│   │   ├── GlassDialogBox.kt    # Modal glass container
│   │   ├── GlassAccentColors.kt # Color palette provider
│   │   └── Modifiers.kt         # Custom modifiers (fadingEdges, etc.)
│   │
│   ├── theme/
│   │   ├── Color.kt             # Design system colors
│   │   ├── Theme.kt             # Glassmorphic M3 theme
│   │   └── Type.kt              # Typography
│   │
│   └── viewmodels/
│       ├── SharedViewModel.kt    # Core app state
│       └── RunningViewModel.kt   # GPS & Running state
│
├── services/
│   ├── StepTrackerService.kt     # Foreground tracking
│   └── MediaNotificationListener.kt # Music sync
│
└── widgets/
    ├── StepsWidgetSynced.kt      # Persistent step widget
    └── WaterWidgetSynced.kt      # Persistent water widget
```

---

## Installation

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Android SDK 34
- An Android device or emulator (API 26+)

### Setup Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/feet.git
   cd feet
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. **Sync Gradle**
   - Click "Sync Now" when prompted
   - Wait for dependencies to download

4. **Configure Permissions**
   
   Ensure these permissions are in `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
   <uses-permission android:name="android.permission.INTERNET" />
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
   ```

5. **Build & Run**
   - Connect your Android device or start an emulator
   - Click the "Run" button (▶️) or press Shift+F10
   - Grant necessary permissions when prompted

### First Launch Setup

1. **Grant Permissions:**
   - Activity Recognition (for step counting)
   - Location (for weather)
   - Notification Access (for media tracking)

2. **Set Your Goals:**
   - Configure daily step goal
   - Set hydration target
   - Customize glass size

3. **Add Widgets (Optional):**
   - Long press home screen
   - Select "Widgets"
   - Find "Feet" app
   - Add Steps or Water widget

---

## Architecture

### MVVM Pattern

```
┌─────────────┐
│     View    │  (Composables)
│  (UI Layer) │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│  ViewModel  │  (SharedViewModel)
│ (Logic Layer)│
└──────┬──────┘
       │
       ↓
┌─────────────┐
│ Repository  │  (FitnessRepository)
│ (Data Layer) │
└──────┬──────┘
       │
       ↓
┌─────────────┐
│  Database   │  (Room)
│   (DAO)     │
└──────┴──────┘
```

### Data Flow

1. **User Interaction** → Composable UI
2. **UI Events** → ViewModel
3. **Business Logic** → Repository
4. **Data Operations** → Room Database
5. **Data Updates** → StateFlow
6. **UI Recomposition** → Updated UI

---

## Design System

### Color Palette

```kotlin
// Dark Backgrounds
val DarkBg = Color(0xFF0A0A0F)
val DeepSpace = Color(0xFF080810)

// Glass Tokens
val GlassWhite = Color(0x1AFFFFFF) // Frosted overlay
val GlassBorder = Color(0x33FFFFFF) // Subtle edge

// Accent Palette (Activity Specific)
val PacePurple = Color(0xFF7B61FF)
val DistanceCyan = Color(0xFF00BCD4)
val TimeGreen = Color(0xFF00E676)
val SpeedPink = Color(0xFFE91E63)
val ElevationAmber = Color(0xFFFFC107)
```

### Typography

```kotlin
// Display Text
displayLarge: 57sp, Bold
displayMedium: 45sp, SemiBold

// Title Text
titleLarge: 22sp, Medium
titleMedium: 16sp, Medium

// Body Text
bodyLarge: 16sp, Regular
bodyMedium: 14sp, Regular
```

### Components

- **TranslucentBox:** Glassmorphic container with blur effect
- **LiquidGlassButton:** Animated button with ripple effect
- **ColorBendsBackground:** Dynamic gradient backgrounds
- **Custom Progress Bars:** Animated progress indicators

---

## Database Schema

### Entities

| Entity | Description |
| :--- | :--- |
| **StepRecord** | Daily step count, goal, and timestamps. |
| **WaterRecord** | Hydration tracking (ml) and daily targets. |
| **WorkoutEntity** | Exercise names, rep/distance goals, and completion status. |
| **RunRecordEntity** | Detailed run metrics: duration, distance, pace, elevation, and GPS path points. |
| **TimerEntity** | State and remaining time for custom countdown timers. |
| **StopwatchEntity** | Persistent state for the stopwatch across app restarts. |
| **LapRecordEntity** | Individual lap timestamps for running sessions. |
| **UserPreferences** | Global settings: goals, glass sizes, and user vitals (weight/height). |

### UserPreferences Schema

| Column | Type | Description |
|--------|------|-------------|
| id | Int (PK) | Always 1 (single row) |
| dailyWaterGoalMl | Int | Daily water goal |
| dailyStepGoal | Int | Daily step goal |
| glassSize | Float | Default glass size |

---

## Privacy & Permissions

### Required Permissions

1. **ACTIVITY_RECOGNITION**
   - Purpose: Count steps using device sensor
   - When: Only when app is active
   - Privacy: Data stays local, never shared

2. **INTERNET**
   - Purpose: Fetch weather data
   - When: On app launch
   - Privacy: Only weather API calls

3. **ACCESS_COARSE_LOCATION**
   - Purpose: Detect city for weather
   - When: On app launch
   - Privacy: Approximate location only, not tracked

4. **BIND_NOTIFICATION_LISTENER_SERVICE** (Optional)
   - Purpose: Display currently playing music
   - When: When media is playing
   - Privacy: Only reads media metadata

### Data Storage

-  **All data stored locally** on device
-  **No cloud sync** or external servers
-  **No analytics** or tracking
-  **No ads** or third-party SDKs
-  **Complete privacy** - your data is yours

---

**Last Updated:** November 2024

**Version:** 1.0.0

**Status:** Active Development 
