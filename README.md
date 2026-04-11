# SPOTit - Android Anti-Theft Security Application

<div align="center">

![SPOTit Logo](app/src/main/res/mipmap-mdpi/ic_launcher.webp)

**A modern Android anti-theft solution with real-time motion detection, GPS tracking, and instant SMS alerts.**

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)
[![MinSDK](https://img.shields.io/badge/MinSDK-26%20(Android%208.0)-blue.svg)](https://developer.android.com/about/versions/oreo)
[![TargetSDK](https://img.shields.io/badge/TargetSDK-35%20(Android%2015)-blue.svg)](https://developer.android.com/about/versions)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-orange.svg)](https://developer.android.com/jetpack/compose)

</div>

---

## 📱 Overview

SPOTit is a comprehensive anti-theft security application designed to protect your Android device against unauthorized access and theft. When activated, it continuously monitors your device using advanced motion detection algorithms and instantly alerts you through multiple channels when suspicious movement is detected.

### Key Features

- **🔍 Real-Time Motion Detection** - Utilizes the device's accelerometer sensors with configurable sensitivity levels to detect unauthorized movement
- **📍 GPS Location Tracking** - Captures and continuously updates the device's precise location during alerts
- **📩 Instant SMS Alerts** - Automatically sends detailed alert messages with location coordinates to your designated emergency contact every 5 seconds during an active alarm
- **🔊 Multi-Modal Alarm System** - Combines maximum volume alarm sounds and vibration patterns for immediate attention
- **🔐 Pattern Lock Security** - Prevents unauthorized disabling of protection with a customizable unlock pattern
- **📊 Alert History** - Maintains a chronological log of all security events with timestamps and locations
- **⚙️ Customizable Settings** - Configure sensitivity, emergency contact, and alert preferences to suit your needs

---

## 🏗️ Architecture

SPOTit follows modern Android development best practices with a clean, layered architecture:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ HomeScreen  │  │SettingsScr. │  │ HistoryScr. │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
│                         │                                    │
│              ┌──────────┴──────────┐                         │
│              │   SpotItViewModel   │                         │
│              └─────────────────────┘                         │
├─────────────────────────────────────────────────────────────┤
│                     Domain Layer                             │
│              ┌─────────────────────┐                         │
│              │  SpotItRepository   │                         │
│              └─────────────────────┘                         │
├─────────────────────────────────────────────────────────────┤
│                      Data Layer                              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │SettingsStore│  │ AlertEvent  │  │ AppSettings │          │
│  │ (DataStore) │  │   Model     │  │   Model     │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
├─────────────────────────────────────────────────────────────┤
│                    Service Layer                             │
│  ┌─────────────────────┐  ┌─────────────────────┐           │
│  │SpotItForegroundSvc  │  │  MotionEventManager │           │
│  └─────────────────────┘  └─────────────────────┘           │
├─────────────────────────────────────────────────────────────┤
│                    Utility Layer                             │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │MotionDetect.│  │LocationHelper│  │  SmsHelper  │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

### Technology Stack

| Layer | Technologies |
|-------|-------------|
| **UI** | Jetpack Compose, Material Design 3 |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Dependency Injection** | Manual DI (Constructor Injection) |
| **Asynchronous** | Kotlin Coroutines, Flow |
| **Persistence** | DataStore Preferences |
| **Location** | Google Play Services FusedLocationProvider |
| **Sensors** | Android SensorManager (LINEAR_ACCELERATION) |

---

## 🎨 Design System

SPOTit features a carefully crafted visual identity following Material Design 3 guidelines:

### Color Palette

| Color Name | Hex Code | Usage |
|------------|----------|-------|
| Primary Blue | `#1A237E` | Headers, primary buttons, active states |
| Accent Blue | `#283593` | Highlights, borders, secondary elements |
| Surface Gray | `#F5F5F5` | Card backgrounds, unselected states |
| Border Gray | `#BDBDBD` | Dividers, card borders |
| Text Dark | `#212121` | Primary text content |
| Muted Text | `#616161` | Secondary text, hints, labels |
| Status Green | `#2E7D32` | Active/ON status indicators |
| Alarm Red | `#B71C1C` | Alerts, OFF status, error states |

### Accessibility

- All text maintains WCAG AA contrast ratios
- Touch targets meet 48dp minimum size guidelines
- Color coding is supplemented with text labels for accessibility
- Screen reader compatible with descriptive content

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or later
- **JDK**: Java 17 or higher
- **Android Device**: Physical device recommended (API 26+)
- **Google Play Services**: Required for location features

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/PanosBiazis/spotit-project.git
   cd spotit-project
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory and click "OK"

3. **Sync Gradle**
   - Android Studio will prompt to sync Gradle files
   - Click "Sync Now" to download dependencies

4. **Configure your device**
   - Enable Developer Options and USB Debugging on your device
   - Connect your device via USB
   - Accept any permission prompts on the device

5. **Run the application**
   - Click the "Run" button in Android Studio (green play icon)
   - Select your connected device
   - Wait for installation and launch

### First-Time Setup

1. **Grant Permissions** - The app will request the following permissions:
   - 📍 Location (Fine & Coarse) - For GPS tracking
   - 📩 SMS - For sending alert messages
   - 🔔 Notifications - For foreground service notification (Android 13+)

2. **Configure Settings**
   - Enter your emergency contact's phone number
   - Set your preferred unlock pattern (default: `1-2-3-6`)
   - Adjust motion sensitivity as needed
   - Enable/disable SMS and siren alerts

3. **Test the System**
   - Start monitoring
   - Move the device to test motion detection
   - Verify SMS alerts are received at the configured number

---

## 📖 User Guide

### Starting Monitoring

1. Open SPOTit and navigate to the **Home** tab
2. Tap the **"START MONITORING"** button
3. The status will change to "Monitoring: ON" (green)
4. A persistent notification will appear indicating active protection

### When Motion is Detected

The system automatically triggers the following:

1. **Alarm Sound** - Maximum volume alert ringtone
2. **Vibration Pattern** - Repeating vibration for immediate attention
3. **SMS Alerts** - Location-based messages sent every 5 seconds
4. **Notification Update** - Shows "ALARM" status with current location
5. **History Entry** - Event logged with timestamp and location

### Stopping Monitoring (Security Pattern Required)

1. Tap the **"STOP MONITORING"** button
2. Enter your security pattern by tapping the numbered dots
3. Press **"Unlock"** to confirm
4. If correct, monitoring stops and the alarm ceases

### Adjusting Settings

Navigate to the **Settings** tab to configure:

| Setting | Description | Range/Options |
|---------|-------------|---------------|
| Notification Contact | Emergency contact phone number | Any valid phone number |
| Pattern | Security unlock pattern | Dot sequence (e.g., `1-2-3-6`) |
| Sensitivity | Motion detection threshold | 0.1 (very sensitive) to 5.0 (less sensitive) |
| SMS | Enable/disable SMS alerts | ON/OFF toggle |
| Siren | Enable/disable alarm sound | ON/OFF toggle |

### Viewing Alert History

Navigate to the **History** tab to see:
- All recorded security events
- Timestamp of each alert
- Location at time of detection
- Event details and descriptions

---

## 🔧 Technical Details

### Motion Detection Algorithm

```
Accelerometer Data → Magnitude Calculation → Threshold Comparison
                                                            ↓
                                            Motion Detected? → Yes → Trigger Alert
                                                    ↓
                                                   No → Continue Monitoring
```

- **Sensor**: TYPE_LINEAR_ACCELERATION (preferred) or TYPE_ACCELEROMETER (fallback)
- **Sampling Rate**: SENSOR_DELAY_UI (~60Hz)
- **Cooldown Period**: 1.5 seconds between alerts
- **Threshold Range**: 0.1 - 5.0 m/s²

### Foreground Service

The monitoring service runs as a foreground service to ensure:

- Continuous operation even when the app is backgrounded
- Survival through system memory pressure
- Proper notification display for user awareness
- Compliance with Android background execution limits

### SMS Alert Format

```
SPOTit ALERT: Movement Detected. Lat: 37.98765, Lng: 23.76543
```

- Sent immediately upon motion detection
- Repeated every 5 seconds during active alarm
- Includes current GPS coordinates
- Continues until monitoring is stopped

---

## 📋 Project Structure

```
spotit-project/
├── app/
│   ├── src/main/
│   │   ├── kotlin/com/spotit/
│   │   │   ├── MainActivity.kt              # Main entry point
│   │   │   ├── SpotItViewModel.kt           # UI state management
│   │   │   ├── data/
│   │   │   │   ├── Models.kt                # Data classes
│   │   │   │   └── SettingsStore.kt         # DataStore persistence
│   │   │   ├── domain/
│   │   │   │   └── SpotItRepository.kt      # Repository layer
│   │   │   ├── service/
│   │   │   │   ├── MotionEventManager.kt    # Event bus
│   │   │   │   └── SpotItForegroundService.kt # Background monitoring
│   │   │   ├── ui/
│   │   │   │   ├── components/              # Reusable UI components
│   │   │   │   ├── screens/                 # Screen composables
│   │   │   │   └── theme/                   # Theme configuration
│   │   │   └── util/
│   │   │       ├── LocationHelper.kt        # GPS utilities
│   │   │       ├── MotionDetector.kt        # Sensor handling
│   │   │       └── SmsHelper.kt             # SMS utilities
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── README.md
```

---

## 🔐 Security Considerations

### Permissions Justification

| Permission | Purpose |
|------------|---------|
| `VIBRATE` | Alarm vibration feedback |
| `WAKE_LOCK` | Keep device awake during monitoring |
| `POST_NOTIFICATIONS` | Foreground service notification (Android 13+) |
| `SEND_SMS` | Emergency alert messages |
| `ACCESS_FINE_LOCATION` | Precise GPS coordinates for alerts |
| `ACCESS_COARSE_LOCATION` | Fallback network location |
| `ACCESS_BACKGROUND_LOCATION` | Location access during monitoring |
| `FOREGROUND_SERVICE` | Run background monitoring service |
| `FOREGROUND_SERVICE_LOCATION` | Service type declaration (Android 14+) |

### Pattern Lock Security

- Prevents unauthorized stopping of monitoring
- Pattern is stored locally (never transmitted)
- No lockout mechanism (user convenience)
- Consider adding biometric authentication in future versions

### Privacy

- Location data is only used for security alerts
- No data is transmitted to external servers
- All settings stored locally using DataStore
- SMS messages are sent directly from device

---

## 🐛 Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Motion not detected | Increase sensitivity; ensure device is on stable surface |
| SMS not sending | Check SMS permission; verify contact number format |
| Location shows "Unknown" | Grant location permission; enable GPS |
| Service stops unexpectedly | Check battery optimization settings; disable for SPOTit |
| Pattern not working | Verify pattern format (e.g., `1-2-3-6`); check for typos |

### Testing Recommendations

- **Physical Device Required**: Emulators have limited sensor/GPS support
- **Test in Safe Environment**: Avoid triggering alerts in public
- **Use Your Own Number**: For SMS testing, use your own phone
- **Battery Considerations**: Extended monitoring may drain battery

---

## 🗺️ Roadmap

### Version 2.0 (Current)
- ✅ Motion detection with configurable sensitivity
- ✅ GPS location tracking
- ✅ SMS alerts with location
- ✅ Pattern lock security
- ✅ Alert history
- ✅ Material Design 3 UI

### Version 2.1 (Planned)
- [ ] Biometric authentication option
- [ ] Photo capture on alert
- [ ] Cloud backup for settings
- [ ] Multi-contact support
- [ ] Custom alarm sounds

### Version 3.0 (Future)
- [ ] Wear OS companion app
- [ ] Web dashboard
- [ ] Machine learning motion classification
- [ ] Geofencing capabilities
- [ ] Remote management

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Maintain MVVM architecture patterns

---

## 📄 License

```
Copyright (c) 2026 Panos Biazis

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

---

## 👤 Author

**Panos Biazis**

- Project Creator & Lead Developer
- Date: April 10, 2026
- Version: 2.0

---

## 🙏 Acknowledgments

- [Jetpack Compose](https://developer.android.com/jetpack/compose) - Modern UI toolkit
- [Material Design 3](https://m3.material.io/) - Design system
- [Google Play Services](https://developers.google.com/android) - Location APIs
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Async programming

---

<div align="center">

**⭐ If this project helped you, please consider giving it a star! ⭐**

[Report Bug](https://github.com/PanosBiazis/spotit-project/issues) · [Request Feature](https://github.com/PanosBiazis/spotit-project/issues)

</div>
