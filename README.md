# Bundl - Smart Notification Bundling for Android

Bundl is a modern Android application that helps you take control of your notifications by intelligently bundling them and delivering them on your schedule.

## Features

### 🔔 Notification Bundling

- **Smart Rule-Based Filtering**: Create custom rules to bundle notifications from specific apps
- **Blacklist & Whitelist Modes**: Choose whether to bundle specific notifications or allow only certain ones
- **Filter Strings**: Add optional text filters to match specific notification content
- **App Name Display**: View friendly app names instead of technical package names

### 📅 Scheduled Delivery

- **Custom Schedules**: Set multiple times throughout the day for automatic notification delivery
- **24-Hour Time Picker**: Native Material 3 time picker for easy schedule management
- **Enable/Disable Toggles**: Control individual schedules without deleting them
- **Boot Persistence**: Schedules automatically restore after device reboot

### 📊 Notification History

- **View All Bundled Notifications**: See all notifications that have been intercepted
- **App Icons & Names**: Beautiful display with app icons and human-readable names
- **Detailed Information**: View full notification details including title, text, and timestamps
- **Search & Filter**: Find notifications by app name or package name

### 🎯 App Rule Management

- **Easy App Selection**: Pick apps from a searchable list with icons
- **Visual Rule Cards**: See rules displayed with app names and icons
- **Edit & Delete**: Manage your rules with intuitive controls
- **System & User Apps**: Access all installed applications

### 🚀 Manual Delivery

- **Deliver Now Button**: Instantly deliver all bundled notifications at any time
- **Grouped by App**: Notifications are intelligently grouped by application
- **Expandable Summaries**: Inbox-style notifications with up to 5 items visible
- **Mark as Read**: Persistent notifications with action button to dismiss

## Screenshots

[Add screenshots here]

## Requirements

- **Minimum SDK**: Android 8.0 (API 26)
- **Target SDK**: Android 14 (API 36)
- **Permissions Required**:
  - `POST_NOTIFICATIONS` - For delivering bundled notifications (Android 13+)
  - `RECEIVE_BOOT_COMPLETED` - For rescheduling alarms after reboot
  - `SCHEDULE_EXACT_ALARM` - For precise scheduled delivery
  - `WAKE_LOCK` - For reliable alarm triggers
  - `QUERY_ALL_PACKAGES` - For viewing all installed apps
  - Notification Listener Access - For intercepting notifications (granted via Settings)

## Installation

### From Source

1. Clone the repository:

```bash
git clone https://github.com/eliasfloreteng/bundl.git
cd bundl
```

2. Open the project in Android Studio

3. Build and run:

```bash
./gradlew assembleDebug
```

4. Install the APK on your device:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

<details>
<summary><b>Building & Publishing Production APK (Click to expand)</b></summary>

### Building a Signed Production APK

#### Option 1: Using Android Studio (Recommended for beginners)

1. **Generate Signed Bundle/APK**:
   - Click `Build` → `Generate Signed Bundle / APK`
   - Select `Android App Bundle` (for Google Play) or `APK`
   - Click `Next`

2. **Create or Select Keystore**:

   **If you don't have a keystore (first time)**:
   - Click `Create new...`
   - **Key store path**: Choose location (e.g., `bundl-release-key.jks`)
   - **Password**: Enter a strong password
   - **Key alias**: `bundl` (or your preferred name)
   - **Key password**: Enter a strong password (can be same or different)
   - **Validity**: 25 years (default)
   - **Certificate**: Fill in your details
   - Click `OK`

   ⚠️ **IMPORTANT**: Save these passwords securely! You cannot update your app without them.

   **If you already have a keystore**:
   - Click `Choose existing...`
   - Select your `.jks` file
   - Enter passwords and alias
   - Click `Next`

3. **Build Configuration**:
   - **Destination folder**: Where to save the output file
   - **Build Variants**: Select `release`
   - For AAB: Check `Export encrypted key` (required for Play App Signing)
   - Click `Finish`

4. **Wait for Build**:
   - Android Studio will build your signed APK/AAB
   - A notification will appear when complete
   - Click `locate` to find the file

5. **Output Locations**:
   - **AAB**: `app/release/app-release.aab`
   - **APK**: `app/release/app-release.apk`

#### Option 2: Using Command Line

1. **Create a keystore** (one-time setup):

```bash
keytool -genkey -v -keystore bundl-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias bundl
```

Follow the prompts to set:
- Keystore password
- Key password
- Your name and organization details

⚠️ **IMPORTANT**: Store the keystore file and passwords securely. You cannot update your app without them!

2. **Create `keystore.properties`** in the project root:

```properties
storePassword=YOUR_KEYSTORE_PASSWORD
keyPassword=YOUR_KEY_PASSWORD
keyAlias=bundl
storeFile=../bundl-release-key.jks
```

⚠️ **Never commit this file to version control!** Add it to `.gitignore`.

3. **Update `app/build.gradle.kts`** to load signing config:

```kotlin
// Add at the top of the file
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    // ... existing config ...

    signingConfigs {
        create("release") {
            storeFile = file(keystoreProperties["storeFile"] as String)
            storePassword = keystoreProperties["storePassword"] as String
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

4. **Build the release APK**:

```bash
./gradlew assembleRelease
```

The signed APK will be at: `app/build/outputs/apk/release/app-release.apk`

5. **Or build an AAB (Android App Bundle)** for Google Play:

```bash
./gradlew bundleRelease
```

The AAB will be at: `app/build/outputs/bundle/release/app-release.aab`

### Publishing to Google Play

#### Prerequisites

1. **Google Play Developer Account** ($25 one-time fee)
2. **Signed AAB file** (from step 5 above)
3. **App assets**:
   - App icon (512x512 PNG)
   - Feature graphic (1024x500 PNG)
   - Screenshots (at least 2, up to 8)
   - Privacy policy URL (if app collects data)

#### Steps to Publish

1. **Go to Google Play Console**: https://play.google.com/console

2. **Create a new app**:
   - Click "Create app"
   - Enter app name, default language, app/game type
   - Declare if it's free or paid

3. **Complete the Store Listing**:
   - App name and short description
   - Full description (up to 4000 characters)
   - App icon and feature graphic
   - Screenshots for phone, tablet, etc.
   - Categorization and tags
   - Contact details and privacy policy

4. **Set up Content Rating**:
   - Complete the questionnaire
   - Get rating certificates (required before publishing)

5. **Set Target Audience and Content**:
   - Age ranges
   - Content declarations
   - Ads presence

6. **Declare Data Safety**:
   - For Bundl, you should declare:
     - ✅ No data collected and shared with third parties
     - ✅ Data stored locally on device
     - ⚠️ App accesses notifications (for functionality)

7. **Complete App Access**:
   - Add instructions for testing notification access
   - Provide test credentials if needed

8. **Set up Pricing and Distribution**:
   - Select countries
   - Pricing (free recommended)
   - Distribution consent

9. **Upload the AAB**:
   - Go to "Production" → "Create new release"
   - Upload `app-release.aab`
   - Write release notes
   - Review and rollout

10. **Handle Special Permissions**:

⚠️ **IMPORTANT**: `QUERY_ALL_PACKAGES` requires justification:
- Go to "App content" → "Sensitive app permissions"
- Declare usage of `QUERY_ALL_PACKAGES`
- Provide justification:
  > "Bundl is a notification management app that requires QUERY_ALL_PACKAGES to display a list of installed apps to the user. Users need to select which apps' notifications should be bundled. This permission is essential for the core functionality of the app."
- Submit a screen recording showing:
  - App picker feature in action
  - User selecting apps from the list
  - How it's used for notification bundling

11. **Submit for Review**:
    - Review summary
    - Submit app for review
    - Wait for approval (typically 1-7 days)

#### After Approval

- **Monitor**: Check crash reports and user reviews
- **Update**: Use same signing key for all future updates
- **Respond**: Reply to user reviews promptly

#### Tips for Faster Approval

- Write clear, detailed feature descriptions
- Provide comprehensive privacy policy
- Include good quality screenshots showing all features
- Record a demo video showing permission usage
- Test thoroughly before submitting
- Respond quickly to review questions

</details>

## Setup

### First Launch

1. **Grant Notification Permission**: On first launch (Android 13+), you'll be prompted to allow notification posting
2. **Enable Notification Access**:
   - Toggle "Bundling" on the Home screen
   - You'll be redirected to Settings
   - Enable notification access for Bundl
3. **Create Rules**: Navigate to the Rules tab and add apps you want to bundle
4. **Set Schedules** (Optional): Go to the Schedule tab to set automatic delivery times

### Creating App Rules

1. Navigate to the **Rules** tab
2. Tap the **+ (Add)** button
3. Click the list icon to pick an app from the searchable list
4. Choose a mode:
   - **BLACKLIST**: Bundle all notifications from this app (or only those matching the filter)
   - **WHITELIST**: Allow only notifications matching the filter
5. Add an optional filter string to match specific notification content
6. Tap **Save**

### Setting Up Schedules

1. Navigate to the **Schedule** tab
2. Tap the **+ (Add)** button
3. Use the time picker to select a delivery time
4. Tap **Add**
5. Toggle schedules on/off as needed
6. Delete schedules with the trash icon

## Architecture

Bundl follows modern Android development best practices:

- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room with Flow-based reactive queries
- **State Management**: Kotlin StateFlow and Coroutines
- **Preferences**: DataStore (type-safe)
- **Image Loading**: Coil 3.x
- **Navigation**: NavigationSuiteScaffold with adaptive layouts

### Project Structure

```
app/src/main/java/se/floreteng/bundl/
├── MainActivity.kt                      # Entry point
├── HomeScreen.kt                        # Main screen with bundling toggle
├── HistoryScreen.kt                     # Notification history view
├── ScheduleScreen.kt                    # Schedule management
├── SettingsScreen.kt                    # App rules management
├── BundlDatabase.kt                     # Room database
├── BundlNotificationListenerService.kt  # Notification interception
├── BootReceiver.kt                      # Reboot handling
├── NotificationActionReceiver.kt        # Notification actions
├── ScheduledDeliveryReceiver.kt         # Scheduled delivery
├── apprule/                             # App rule feature
│   ├── AppRule.kt
│   ├── AppRuleDao.kt
│   ├── AppRuleRepository.kt
│   ├── AppRuleViewModel.kt
│   ├── AppRuleDialog.kt
│   └── ...
├── notifications/                       # Notification feature
│   ├── Notification.kt
│   ├── NotificationDao.kt
│   ├── NotificationRepository.kt
│   ├── NotificationViewModel.kt
│   └── ...
├── schedule/                            # Schedule feature
│   ├── Schedule.kt
│   ├── ScheduleDao.kt
│   ├── ScheduleRepository.kt
│   ├── ScheduleViewModel.kt
│   └── ...
├── preferences/                         # App preferences
│   └── PreferencesManager.kt
└── utils/                               # Utilities
    ├── AppInfoUtil.kt
    ├── NotificationAccessUtil.kt
    ├── NotificationDeliveryUtil.kt
    └── ScheduleAlarmUtil.kt
```

## Technologies Used

- **Language**: Kotlin
- **UI**: Jetpack Compose with Material 3
- **Database**: Room 2.8.3 with KSP
- **Async**: Kotlin Coroutines & Flow
- **DI**: Manual ViewModelFactory
- **Image Loading**: Coil 3.0.4
- **Build System**: Gradle with Kotlin DSL

## Key Components

### NotificationListenerService

- Intercepts all system notifications
- Checks against user-defined rules
- Cancels matching notifications
- Stores cancelled notifications in database

### AlarmManager Integration

- Schedules daily repeating alarms
- Handles device reboot restoration
- Delivers notifications at scheduled times
- Clears database after delivery

### App Picker Dialog

- Searchable list of all installed apps
- App icons loaded with Coil
- Filter by app name or package name
- Real-time app name resolution

## Privacy & Permissions

Bundl takes privacy seriously:

- **Local Storage Only**: All data is stored locally on your device
- **No Internet**: The app requires no internet connection
- **No Analytics**: No tracking or analytics are collected
- **No Ads**: Completely ad-free
- **Open Source**: Code is available for review

### Why We Need Permissions

- **Notification Access**: Required to intercept notifications from other apps
- **Post Notifications**: Needed to deliver bundled notification summaries
- **Query All Packages**: Allows you to see all installed apps in the app picker
- **Alarm Permissions**: For scheduling automatic delivery at specified times
- **Boot Completed**: To restore schedules after device restart

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Setup

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Add comments for complex logic
- Keep functions small and focused

## Known Issues

- On Android 14+, some system dialogs may appear on top of notification permission requests
- App list may take a moment to load on first opening the app picker

## Roadmap

- [ ] Notification templates for custom formats
- [ ] Export/import rules and schedules
- [ ] Notification statistics and analytics
- [ ] Dark/Light theme customization
- [ ] Widget support
- [ ] Backup to cloud storage

## License

MIT License

## Acknowledgments

- Built with [Jetpack Compose](https://developer.android.com/jetpack/compose)
- Icons from [Material Icons](https://fonts.google.com/icons)
- Image loading by [Coil](https://coil-kt.github.io/coil/)

## Support

For issues, questions, or suggestions, please open an issue on GitHub.

## Disclaimer

This app intercepts and manages notifications from other apps on your device. By using this app, you acknowledge that:

- The app requires extensive permissions to function properly
- Notifications are stored locally and processed on-device
- The app may not work perfectly with all notification types
- Use at your own risk

---

**Made with ❤️ using Kotlin and Jetpack Compose**
