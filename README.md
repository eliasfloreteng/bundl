# Bundl - Social Media Notification Bundler

Bundl is an Android application that intercepts notifications from social media apps (Instagram, TikTok, Snapchat) and delivers them in bundles at scheduled times instead of immediately.

## Features

- **Notification Interception**: Automatically captures notifications from configured apps
- **Scheduled Delivery**: Set custom delivery times throughout the day
- **Notification History**: View all intercepted notifications with timestamps
- **App Configuration**: Enable/disable bundling per app
- **Exemption Rules**: Allow certain notification types (e.g., direct messages) to bypass bundling
- **On/Off Toggle**: Easily enable or disable bundling globally
- **Local Storage**: All data stored locally with Room database

## Architecture

### Core Components

1. **NotificationListenerService** (`BundlNotificationListenerService`)
   - Intercepts notifications from social media apps
   - Stores notifications in local database
   - Cancels original notifications to prevent duplicates
   - Checks exemption rules before bundling

2. **Room Database** (`BundlDatabase`)
   - Stores bundled notifications
   - Manages app configurations
   - Tracks delivery schedules
   - Stores exemption rules

3. **WorkManager** (`BundleDeliveryWorker`)
   - Schedules periodic notification delivery
   - Creates summary notifications per app
   - Marks delivered notifications

4. **Jetpack Compose UI**
   - Dashboard: Overview of pending notifications
   - History: View all notifications
   - Settings: Configure app behavior
   - App Config: Manage which apps to bundle
   - Schedules: Set delivery times
   - Exemptions: Configure instant delivery rules

### Database Schema

#### Notifications Table
- `id`: Primary key
- `appPackage`: Package name of the source app
- `appName`: Display name of the app
- `title`: Notification title
- `text`: Notification content
- `timestamp`: When notification was received
- `isDelivered`: Delivery status
- `deliveredAt`: Delivery timestamp
- `category`: Notification category

#### Schedules Table
- `id`: Primary key
- `hour`: Hour (0-23)
- `minute`: Minute (0-59)
- `daysOfWeek`: JSON array of active days
- `isEnabled`: Schedule status

#### App Config Table
- `appPackage`: Package name (primary key)
- `appName`: Display name
- `isEnabled`: Whether bundling is enabled for this app

#### Exemption Rules Table
- `id`: Primary key
- `appPackage`: Associated app package
- `ruleType`: Type of exemption (MESSAGE, CALL, MENTION)
- `keywords`: JSON array of keywords to match
- `categoryFilter`: Notification category to match
- `isEnabled`: Rule status

## Setup Instructions

### Prerequisites
- Android Studio Ladybug or newer
- Android SDK 24 (Android 7.0) or higher
- Kotlin 2.0+

### Build Instructions

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Run `./gradlew assembleDebug` or build from Android Studio

### Required Permissions

The app requires the following permissions:

- **Notification Access** (BIND_NOTIFICATION_LISTENER_SERVICE): Required to intercept notifications
- **Post Notifications** (POST_NOTIFICATIONS): For Android 13+ to show bundled notifications
- **Schedule Exact Alarms** (SCHEDULE_EXACT_ALARM): For precise delivery timing
- **Receive Boot Completed** (RECEIVE_BOOT_COMPLETED): To reschedule deliveries after reboot

### First-Time Setup

1. Install the app
2. Grant notification access permission:
   - Go to Settings → Notifications → Notification Access
   - Enable Bundl
3. Configure apps in the app settings
4. Set delivery schedules
5. (Optional) Configure exemption rules for instant delivery

## Usage

### Basic Usage

1. **Enable Bundling**: Toggle on the main dashboard
2. **Configure Apps**: Go to Settings → Managed Apps to select which apps to bundle
3. **Set Schedules**: Go to Settings → Delivery Schedules to add delivery times
4. **View History**: Tap "Pending Notifications" on the dashboard to see all intercepted notifications

### Exemption Rules

Exemption rules allow certain notifications to bypass bundling:

1. Go to Settings → Managed Apps
2. Select an app
3. Tap "Configure Exemptions"
4. Add rules for notification types that should be delivered instantly

**Example Rules**:
- **Direct Messages**: Deliver Snapchat/Instagram DMs immediately
- **Mentions**: Deliver @mentions instantly
- **Calls**: Deliver call notifications immediately

### Manual Delivery

You can trigger immediate delivery of all pending notifications by tapping "Deliver All Now" on the dashboard.

## Supported Apps

Default configuration includes:
- **Instagram** (`com.instagram.android`)
- **Snapchat** (`com.snapchat.android`)
- **TikTok** (`com.zhiliaoapp.musically`)

Additional apps can be added through the app configuration screen.

## Technical Details

### Dependencies

- **Room**: 2.6.1 - Local database
- **WorkManager**: 2.9.0 - Background scheduling
- **Navigation Compose**: 2.7.7 - Navigation
- **Material3**: Latest - UI components
- **Gson**: 2.10.1 - JSON parsing

### Minimum Requirements

- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)
- **Compile SDK**: 35

## Project Structure

```
app/src/main/java/se/floreteng/bundl/
├── BundlApplication.kt              # Application class
├── MainActivity.kt                  # Main activity with navigation
├── data/
│   ├── BundlDatabase.kt            # Room database
│   ├── dao/                        # Data Access Objects
│   │   ├── AppConfigDao.kt
│   │   ├── ExemptionRuleDao.kt
│   │   ├── NotificationDao.kt
│   │   └── ScheduleDao.kt
│   └── model/                      # Data models
│       ├── AppConfig.kt
│       ├── BundledNotification.kt
│       ├── ExemptionRule.kt
│       └── NotificationSchedule.kt
├── receiver/
│   └── BootReceiver.kt            # Boot completion receiver
├── service/
│   └── BundlNotificationListenerService.kt  # Notification listener
├── ui/
│   ├── navigation/
│   │   └── Screen.kt              # Navigation routes
│   ├── screen/                    # Compose screens
│   │   ├── AppConfigScreen.kt
│   │   ├── DashboardScreen.kt
│   │   ├── ExemptionsScreen.kt
│   │   ├── HistoryScreen.kt
│   │   ├── SchedulesScreen.kt
│   │   └── SettingsScreen.kt
│   └── theme/                     # Material3 theme
├── util/
│   ├── ExemptionChecker.kt       # Exemption rule logic
│   ├── PreferenceManager.kt      # SharedPreferences wrapper
│   └── ScheduleManager.kt        # WorkManager scheduling
├── viewmodel/
│   └── BundlViewModel.kt         # Main ViewModel
└── worker/
    └── BundleDeliveryWorker.kt   # Background delivery worker
```

## Known Limitations

1. **Notification Content**: Some apps may encrypt notification content, limiting filtering capabilities
2. **Battery Optimization**: Aggressive battery optimization may affect delivery timing
3. **App Updates**: Social media app package names may change with updates
4. **Android Restrictions**: Some manufacturers may restrict notification access

## Future Enhancements

- [ ] Add notification content preview in bundles
- [ ] Support for more social media apps
- [ ] Advanced filtering (by sender, keywords, etc.)
- [ ] Statistics and analytics
- [ ] Notification grouping by type
- [ ] Custom notification sounds
- [ ] Widget support
- [ ] Export/import settings

## Troubleshooting

### Notifications Not Being Intercepted

1. Check notification access permission is granted
2. Ensure bundling toggle is enabled
3. Verify the app is configured and enabled
4. Check if exemption rules are blocking all notifications

### Scheduled Delivery Not Working

1. Check if schedules are enabled
2. Disable battery optimization for Bundl
3. Verify exact alarm permission is granted
4. Check if pending notifications exist

### App Crashes

1. Clear app data and reconfigure
2. Check Android version compatibility
3. Review logcat for error details

## License

[Add your license here]

## Contributing

[Add contribution guidelines here]

## Support

For issues, questions, or feature requests, please open an issue on GitHub.
