# SummaryLog 📱

[![Android](https://img.shields.io/badge/Platform-Android_24+-3DDC84.svg?style=flat&logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin_2.0-7F52FF.svg?style=flat&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack_Compose_Material_3-4285F4.svg?style=flat&logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Database](https://img.shields.io/badge/Database-Room_2.6.1-4285F4.svg?style=flat&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![AI Engine](https://img.shields.io/badge/AI-Google_Gemini_2.5_Flash-FF6F00.svg?style=flat&logo=google&logoColor=white)](https://ai.google.dev)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

An offline-first Android notification manager, conversation thread organizer, and AI-powered notification assistant. SummaryLog automatically archives incoming notifications, groups alerts into chat-like threads, enables natural-language queries via Google Gemini (with an offline regex fallback), and exports conversation logs to PDF, keeping your data strictly on your device.

---

## Features

- **Local Notification Capture**: Intercepts title, text, app package, timestamps, and notification images via `NotificationListenerService`.
- **Conversation Threads**: Automatically groups incoming alerts into chat-style threads by contact and application.
- **AI-Powered Queries**: Ask natural-language questions about your notifications using Google Gemini (`gemini-2.5-flash`) with token streaming.
- **Offline Fallback Engine**: Instantly extracts 4–6 digit OTPs, verification codes, and keywords with zero internet or API key required.
- **PDF Export**: Generate formatted, paginated PDF transcripts of any conversation thread directly to device storage.
- **App Whitelist**: Filter which installed apps to log with granular toggles, including batch Select All / Deselect All controls.
- **Activity & Insights**: Track daily alert volumes and identify top notification sources.
- **Automated Retention**: Built-in background cleanup via `WorkManager` prunes alerts older than 30, 60, or 90 days.
- **Material 3 UI**: Responsive Jetpack Compose interface featuring a resizable split-screen layout and AMOLED dark mode support.

---

## Tech Stack

| Component | Technology | Description |
|---|---|---|
| **Language** | [Kotlin](https://kotlinlang.org/) | Modern Kotlin with Coroutines & Flow |
| **UI Framework** | [Jetpack Compose](https://developer.android.com/jetpack/compose) | Material 3 design system, Edge-to-Edge UI |
| **Local Storage** | [Room](https://developer.android.com/training/data-storage/room) | SQLite persistence with KSP compiler |
| **AI Integration** | [Google Generative AI SDK](https://ai.google.dev/) | `gemini-2.5-flash` client with token streaming |
| **Offline Search** | Pattern Matching Engine | Local regex parser for OTPs and keywords |
| **Background Tasks** | [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) | Periodic worker for automated retention cleanup |
| **Document Export** | Android `PdfDocument` | Native multi-page PDF generation |

---

## Getting Started

### Prerequisites

- **Android Studio**: Ladybug (2024.2.1+) or newer
- **Java Development Kit**: JDK 17 recommended
- **Android SDK**: Compile SDK `37`, Target SDK `35`, Minimum SDK `24` (Android 7.0+)
- **Device/Emulator**: Running Android 7.0 or higher
- **(Optional) Gemini API Key**: Free API key from [Google AI Studio](https://aistudio.google.com/)

### Installation & Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/tejasec/SummaryLog.git
   cd SummaryLog
   ```

2. **Configure `local.properties`**:
   Add your Android SDK location and optional Gemini API key to `local.properties`:
   ```properties
   sdk.dir=/path/to/your/Android/Sdk
   GEMINI_API_KEY=your_gemini_api_key_here
   ```
   > **Note**: If `GEMINI_API_KEY` is omitted, SummaryLog defaults to its built-in offline engine (`PrototypeChatEngine`) for local regex matching and OTP extraction.

3. **Build the debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install onto a connected device**:
   ```bash
   ./gradlew installDebug
   ```

---

## Permissions & Setup

SummaryLog requires the following system permissions:

- **Notification Access (`BIND_NOTIFICATION_LISTENER_SERVICE`)**: Needed to intercept and archive incoming notifications.
  1. Open SummaryLog and tap **Grant Notification Access**.
  2. Toggle **SummaryLog** ON in Android Settings > Notification Access.
  3. Confirm the system prompt.
- **Internet (`android.permission.INTERNET`)**: Used only for outbound Google Gemini API requests when prompting the AI assistant.

---

## Usage

### Natural-Language Search & Summaries
Ask questions in plain English in the bottom chat bar:  ### Summary of Accomplishments:

- *"Summarize my alerts from the last 2 hours"*
- *"Did I receive any messages about the meeting time?"*
- *"Show me my latest OTP"*

### Reviewing & Pinning Threads
- Switch to the **Chats** tab to browse alerts clustered by app and contact.
- Pin key conversations to the top of the feed for fast access.
- Tap any thread to view its full chronological message log.

### Exporting Logs to PDF
1. Open a conversation thread.
2. Tap the **PDF icon** in the top app bar.
3. Select a destination file name via the Android Storage Access Framework.

### Managing Monitored Apps
Navigate to the **Track Apps** tab to toggle notification capture per application or use batch Select/Deselect buttons to filter out noise.

---

## Privacy & Security

- **100% Local Archiving**: All notification records, extracted pictures, and conversation metadata are stored on-device in a sandboxed SQLite Room database (`/data/data/com.ts.summarylog/`).
- **No Third-Party Trackers**: No analytics SDKs, advertising libraries, or telemetry services.
- **Controlled AI Context**: SummaryLog only contacts Gemini when you explicitly submit a prompt in the chat interface, passing only the most recent notification snippets required to fulfill your query.
- **Air-Gapped Mode**: If no Gemini API key is configured, all search and extraction operations run completely offline on your device.

---

## 🗺️ Roadmap

- [ ] **Custom Notification Rules**: Auto-dismiss, trigger alarms, or generate custom vibrations based on regex triggers.
- [ ] **On-Device Embeddings & Vector Search**: Local semantic search across thousands of notifications without cloud APIs.
- [ ] **Biometric App Lock**: Fingerprint and Face Unlock authentication to secure access to the notification archive.
- [ ] **Interactive Notification Actions**: Trigger reply or mark-as-read actions directly from the SummaryLog conversation view.
- [ ] **Backup & Restore**: Encrypted local database export and import.
- [ ] **Self-healing pipeline with continual learning**: Drift detection (KS/MMD tests), stateful retraining from a checkpoint, and a champion-challenger gate.


## Contributing

Contributions, bug reports, and feature suggestions are welcome!

1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/amazing-feature`).
3. Commit your changes (`git commit -m 'feat: add amazing feature'`).
4. Push to the branch (`git push origin feature/amazing-feature`).
5. Open a Pull Request.

---

## License

Distributed under the Apache License, Version 2.0. See [`LICENSE`](LICENSE) for more information.


A Ton of features for Android power users and the open-source community.
