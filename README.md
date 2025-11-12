# 👟 Feet - Your Personal Fitness Companion

<div align="center">

![Feet Logo](https://img.shields.io/badge/Feet-Fitness%20App-4CAF50?style=for-the-badge&logo=android&logoColor=white)

**Track your steps, hydration, and workouts with a beautiful glassmorphic UI**

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?style=flat-square&logo=android&logoColor=white)](https://www.android.com/)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?style=flat-square&logo=jetpack-compose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Personal-orange?style=flat-square)](LICENSE)



</div>

---

## 📱 About

**Feet** is a modern, beautiful fitness tracking application built with Jetpack Compose and Material Design 3. It features a stunning glassmorphic UI that makes tracking your daily fitness goals a delightful experience.

Whether you're counting steps, tracking water intake, or managing workouts, Feet provides an intuitive and visually appealing interface to keep you motivated on your fitness journey.

---

## ✨ Features

### 🚶 Step Tracking
- **Live Step Counter** - Real-time step counting using device sensors
- **Daily Goals** - Set and track your daily step targets
- **Historical Data** - View last 30 days of step history with calendar view
- **Progress Visualization** - Beautiful progress bars and charts
- **Distance & Calories** - Automatic calculation based on steps
- **Simulation Mode** - Manual step addition for testing or non-sensor devices

### 💧 Water Tracking
- **Glass-Based Tracking** - Add/remove water by glass
- **Customizable Glass Size** - Set your preferred glass size (ml)
- **Daily Goal Management** - Customize your hydration targets
- **Visual Progress** - Animated water fill with wave effects
- **Liters & Glasses Display** - Dual unit tracking
- **Quick Actions** - Preset buttons for common glass sizes (100ml, 250ml, 500ml)
- **History View** - Track last 10 days of hydration

### 🏋️ Workout Management
- **Custom Workouts** - Create personalized workout plans
- **Goal Types** - Support for reps-based and distance-based goals
- **Duration Tracking** - Optional time tracking for each workout
- **Completion Status** - Mark workouts as complete with visual feedback
- **Today's Workouts** - Quick view of daily workout plans
- **Delete & Edit** - Full CRUD operations for workout management

### 🌤️ Weather Integration
- **Live Weather Data** - Real-time weather using GPS location
- **Automatic Location** - GPS-based city detection
- **Weather Icons** - Beautiful weather condition icons (☀️ ⛅ ☁️ 🌧️ ⛈️ ❄️ 🌫️)
- **Temperature Display** - Current temperature in Celsius
- **Privacy Option** - Phone-cached weather for offline tracking

### 🎵 Media Integration
- **Now Playing** - Display currently playing music
- **Artist Information** - Shows track and artist name
- **Media Controls** - Quick access to music player
- **Notification Listener** - Seamless integration with music apps

### 📱 Home Screen Widgets
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

### 🎨 Beautiful UI/UX
- **Glassmorphic Design** - Modern frosted glass aesthetic
- **Animated Transitions** - Smooth animations throughout
- **Color Bends** - Dynamic gradient backgrounds
- **Custom Components** - Liquid glass buttons and cards
- **Dark Theme** - Eye-friendly dark mode design
- **Responsive Layout** - Adapts to different screen sizes

### 💾 Data Persistence
- **Room Database** - Local SQLite database for data storage
- **Automatic Sync** - Real-time data synchronization
- **90-Day History** - Automatic retention of 3 months data
- **User Preferences** - Saved settings and customizations
- **Data Backup** - Reliable data persistence across sessions

---

## 🖼️ Screenshots

<div align="center">

### Steps Tracking
*Track your daily steps with beautiful visual progress*

### Hydration Monitor
*Stay hydrated with easy glass tracking*

### Workout Planner
*Plan and complete your daily workouts*

### Weather Widget
*Stay informed with live weather updates*

</div>

---

## 🛠️ Tech Stack

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

## 📦 Project Structure

```
com.example.feet/
├── data/
│   ├── database/
│   │   ├── Entities.kt          # Room entities
│   │   ├── DAOs.kt              # Data Access Objects
│   │   ├── FitnessDatabase.kt   # Database instance
│   │   └── FitnessRepository.kt # Repository layer
│   └── models/                   # Data models
│
├── ui/
│   ├── screens/
│   │   ├── StepsScreen.kt       # Steps tracking UI
│   │   ├── WaterScreen.kt       # Hydration tracking UI
│   │   ├── WorkoutScreen.kt     # Workout management UI
│   │   └── MainScreen.kt        # Navigation container
│   │
│   ├── components/
│   │   ├── LiquidGlassButton.kt # Custom button component
│   │   ├── TranslucentBox.kt    # Glassmorphic container
│   │   ├── ColorBends.kt        # Gradient backgrounds
│   │   └── ProgressIndicator.kt # Custom progress bars
│   │
│   ├── theme/
│   │   ├── Color.kt             # Color definitions
│   │   ├── Theme.kt             # Material3 theme
│   │   └── Type.kt              # Typography
│   │
│   └── viewmodels/
│       └── SharedViewModel.kt    # Main ViewModel
│
├── widgets/
│   ├── StepsWidget.kt           # Steps home screen widget
│   └── WaterWidget.kt           # Water home screen widget
│
├── services/
│   └── MediaNotificationListener.kt # Music integration
│
└── MainActivity.kt               # Entry point
```

---

## 🚀 Installation

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

## 🏗️ Architecture

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
└─────────────┘
```

### Data Flow

1. **User Interaction** → Composable UI
2. **UI Events** → ViewModel
3. **Business Logic** → Repository
4. **Data Operations** → Room Database
5. **Data Updates** → StateFlow
6. **UI Recomposition** → Updated UI

---

## 🎨 Design System

### Color Palette

```kotlin
// Primary Colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

// Glassmorphic Colors
val GlassWhite = Color(0x30FFFFFF)
val GlassBorder = Color(0x40FFFFFF)
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

## 📊 Database Schema

### Tables

#### **water_records**
| Column | Type | Description |
|--------|------|-------------|
| date | String (PK) | Date in YYYY-MM-DD format |
| totalMl | Int | Total water in milliliters |
| glassSize | Float | Size of each glass |
| timestamp | Long | Record timestamp |

#### **step_records**
| Column | Type | Description |
|--------|------|-------------|
| date | String (PK) | Date in YYYY-MM-DD format |
| steps | Int | Total steps |
| goal | Int | Step goal |
| timestamp | Long | Record timestamp |

#### **workout_records**
| Column | Type | Description |
|--------|------|-------------|
| id | Long (PK) | Auto-generated ID |
| date | String | Date in YYYY-MM-DD format |
| name | String | Workout name |
| duration | Int? | Duration in minutes |
| goalValue | Int | Goal value |
| goalType | String | "REPS" or "KM" |
| completed | Boolean | Completion status |
| timestamp | Long | Record timestamp |

#### **user_preferences**
| Column | Type | Description |
|--------|------|-------------|
| id | Int (PK) | Always 1 (single row) |
| dailyWaterGoalMl | Int | Daily water goal |
| dailyStepGoal | Int | Daily step goal |
| glassSize | Float | Default glass size |
| timestamp | Long | Last updated |

---

## 🔒 Privacy & Permissions

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

- ✅ **All data stored locally** on device
- ✅ **No cloud sync** or external servers
- ✅ **No analytics** or tracking
- ✅ **No ads** or third-party SDKs
- ✅ **Complete privacy** - your data is yours

---

## 🎯 Roadmap

### Planned Features

- [ ] **Google Fit Integration** - Sync with Google Fit
- [ ] **Export Data** - CSV/JSON export functionality
- [ ] **Streaks** - Track consecutive days of goal achievement
- [ ] **Achievements** - Unlock badges for milestones
- [ ] **Social Features** - Share progress (optional)
- [ ] **Apple Health Sync** - iOS companion app
- [ ] **Wear OS App** - Smartwatch companion
- [ ] **Advanced Analytics** - Weekly/monthly insights
- [ ] **Custom Themes** - User-selectable color schemes
- [ ] **Backup & Restore** - Cloud backup option

---

## 🤝 Contributing

Contributions are welcome! However, this is a personal project with a personal license. If you'd like to contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

Please note: By contributing, you agree that your contributions will be licensed under the same Personal License as this project.

---

## 🐛 Bug Reports

Found a bug? Please open an issue with:
- Device information (model, Android version)
- Steps to reproduce
- Expected vs actual behavior
- Screenshots (if applicable)

---

## 📝 License

**Personal License**

Copyright (c) 2024 [Your Name]

This software is personal property and is protected by copyright law. 

### ✅ You MAY:
- Use this software for personal, non-commercial purposes
- Study the code for educational purposes
- Fork the repository for personal learning

### ❌ You MAY NOT:
- Distribute this software commercially
- Redistribute this software (modified or unmodified) without explicit permission
- Use this software in commercial products or services
- Remove or modify copyright notices
- Claim this work as your own

### 📜 Detailed Terms:

1. **Personal Use:** You are granted permission to use this software for personal, non-commercial purposes only.

2. **Educational Use:** You may study and learn from the source code for educational purposes.

3. **No Commercial Use:** You may not use this software, in whole or in part, for commercial purposes without explicit written permission from the copyright holder.

4. **No Redistribution:** You may not redistribute this software, modified or unmodified, without explicit written permission from the copyright holder.

5. **Attribution:** If you reference this work in educational materials, projects, or publications, you must provide appropriate attribution to the original author.

6. **No Warranty:** This software is provided "as is", without warranty of any kind, express or implied.

7. **Liability:** The copyright holder shall not be liable for any claims, damages, or other liability arising from the use of this software.

For permissions beyond the scope of this license, please contact: [your.email@example.com]

---

## 👨‍💻 Author

**[Your Name]**

- GitHub: [@yourusername](https://github.com/yourusername)
- LinkedIn: [Your Name](https://linkedin.com/in/yourprofile)
- Email: your.email@example.com
- Portfolio: [yourwebsite.com](https://yourwebsite.com)

---

## 🙏 Acknowledgments

- **Jetpack Compose** team for the amazing UI toolkit
- **Material Design 3** for design guidelines
- **wttr.in** for free weather API
- **Android Developer Community** for inspiration and support
- **Kotlin** team for the beautiful language

---

## 📸 Demo

### Video Demo
[Link to demo video]

### Live App
[Link to app on Play Store - if published]

---

## 💬 Support

Need help? Have questions?

- 📧 Email: your.email@example.com
- 💬 Discussions: [GitHub Discussions](https://github.com/yourusername/feet/discussions)
- 🐛 Issues: [GitHub Issues](https://github.com/yourusername/feet/issues)

---

## ⭐ Show Your Support

If you find this project helpful or interesting, please consider giving it a star! ⭐

It helps the project gain visibility and encourages further development.

---

<div align="center">

**Made with ❤️ and Kotlin**

*Stay fit, stay hydrated, stay awesome!* 💪💧

[![Star on GitHub](https://img.shields.io/github/stars/yourusername/feet?style=social)](https://github.com/yourusername/feet)
[![Follow on GitHub](https://img.shields.io/github/followers/yourusername?style=social)](https://github.com/yourusername)

</div>

---

## 📄 Additional Documentation

- [Widget Setup Guide](WIDGETS_SETUP_GUIDE.md)
- [Room Database Integration](ROOM_INTEGRATION_GUIDE.md)
- [Weather API Setup](GPS_WEATHER_GUIDE.md)
- [App Icon Setup](CHANGE_APP_ICON_GUIDE.md)

---

**Last Updated:** November 2024

**Version:** 1.0.0

**Status:** Active Development 🚀
