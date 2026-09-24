AI System Design Project Details
Project should be Unique 
Presentation on Monday 5-10 Sildes PPT and its submission in print out
Prototype of working on monday



# SummaryLog

## Project Overview

**SummaryLog** is an Android application that automatically captures, stores, and AI-summarizes every notification received on a user's device. It solves the real-world problem of notifications being accidentally dismissed, cleared, or buried — making important information irretrievable. The app provides a persistent, searchable notification archive with a built-in AI chat assistant powered by Google Gemini 1.5 Flash that can answer natural-language questions about the user's notification history.

**Category:** AI System Design  
**Platform:** Android (minSdk 24, targetSdk 37)  
**Language:** Kotlin  
**Package:** `com.ts.summarylog`

---

## Problem Statement

Android's default notification system is optimized for **clearing** notifications, not **recovering** from mistakes. Once a notification is swiped away, cleared via "Clear all," or replaced by newer alerts, the information it contained becomes difficult or impossible to retrieve. Users frequently wonder whether they dismissed something important — an OTP, a delivery update, a calendar reminder, or a message from a key contact.

While Android 11+ introduced a basic Notification History toggle, it provides limited context, no search, no organization, and no AI-powered summarization.

---

## Proposed Solution

SummaryLog acts as a **persistent notification buffer** with AI-powered querying:

1. A background `NotificationListenerService` passively captures every notification as it arrives.
2. Notifications are stored in a local Room database, organized by app and conversation thread.
3. Users can browse notifications in a flat feed or grouped conversation threads (WhatsApp-style).
4. An AI chat assistant lets users ask natural-language questions like *"Summarize my notifications from today"*, *"Find my OTP codes"*, or *"What messages did I get from WhatsApp?"* and receive streaming, markdown-formatted answers.

---

## Architecture

```
┌─────────────────────────────────────────────────────┐
│                   Android OS                        │
│                                                     │
│  ┌──────────────────┐    ┌───────────────────────┐  │
│  │  Notification     │    │   Jetpack Compose UI  │  │
│  │  Listener Service │    │                       │  │
│  │  (Background)     │    │  ┌─────────────────┐  │  │
│  │                   │    │  │ Home Feed       │  │  │
│  │  onNotification-  │    │  │ (flat list)     │  │  │
│  │  Posted()         │    │  ├─────────────────┤  │  │
│  │       │           │    │  │ Threads View    │  │  │
│  │       ▼           │    │  │ (grouped by     │  │  │
│  │  ┌──────────┐     │    │  │  app+title)     │  │  │
│  │  │ Extract  │     │    │  ├─────────────────┤  │  │
│  │  │ title +  │     │    │  │ AI Chat         │  │  │
│  │  │ text     │     │    │  │ (Gemini +       │  │  │
│  │  └────┬─────┘     │    │  │  fallback)      │  │  │
│  │       │           │    │  └─────────────────┘  │  │
│  └───────┼───────────┘    └───────────┬───────────┘  │
│          │                            │               │
│          ▼                            ▼               │
│  ┌─────────────────────────────────────────────────┐  │
│  │              Room Database                       │  │
│  │  ┌──────────────┬──────────────┬──────────────┐  │  │
│  │  │ notifications│monitored_apps│chat_messages  │  │  │
│  │  └──────────────┴──────────────┴──────────────┘  │  │
│  └─────────────────────────────────────────────────┘  │
│          │                                            │
│          ▼                                            │
│  ┌──────────────────┐                                 │
│  │ Gemini 1.5 Flash │  (or PrototypeChatEngine)       │
│  │ (Google GenAI    │  (keyword-match fallback)       │
│  │  SDK)            │                                 │
│  └──────────────────┘                                 │
└─────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Layer | Technology | Details |
|---|---|---|
| **Language** | Kotlin | Primary language for all application logic |
| **UI Framework** | Jetpack Compose | Material 3, adaptive `NavigationSuiteScaffold` |
| **Database** | Room 2.6.1 | Local SQLite with KSP annotation processing |
| **AI Backend** | Google Generative AI SDK | `gemini-1.5-flash` model, streaming responses |
| **Background Service** | `NotificationListenerService` | System-level notification capture |
| **Build System** | Gradle (Kotlin DSL) | AGP 9.3.2, Compose BOM 2025.12.00 |
| **Architecture** | Flat Compose + DAO | Composables access Room DAOs directly via coroutines |

---

## Database Schema

### `notifications` table
| Column | Type | Description |
|---|---|---|
| `id` | Long (PK, auto) | Unique notification ID |
| `packageName` | String (indexed) | Source app package name |
| `appName` | String | Human-readable app name |
| `title` | String (indexed) | Notification title |
| `text` | String | Notification body text |
| `timestamp` | Long (indexed) | Arrival time (epoch millis) |

### `monitored_apps` table
| Column | Type | Description |
|---|---|---|
| `packageName` | String (PK) | App to monitor |
| `appName` | String | Display name |
| `isMonitored` | Boolean | Whether monitoring is active |

### `chat_messages` table
| Column | Type | Description |
|---|---|---|
| `id` | String (PK, UUID) | Message ID |
| `text` | String | Message content |
| `isUser` | Boolean | true = user, false = AI |
| `timestamp` | Long | Message time |

---

## Key Components

| Component | File | Role |
|---|---|---|
| `NotificationCollectorService` | `NotificationCollectorService.kt` | Background service that captures notifications via `onNotificationPosted()` |
| `AppDatabase` | `data/AppDatabase.kt` | Room database singleton with 3 entities and 3 DAOs |
| `GeminiChatHelper` | `ai/GeminiChatHelper.kt` | Streams queries to Gemini 1.5 Flash with notification context |
| `PrototypeChatEngine` | `ai/PrototypeChatEngine.kt` | Offline fallback: keyword matching, OTP extraction, full-text search |
| `SummaryLogMainScreen` | `SummaryLogMainScreen.kt` | Main UI: split-screen with notification feed + AI chat |
| `ConversationListScreen` | `ui/conversations/ConversationListScreen.kt` | WhatsApp-style conversation thread list |
| `ConversationDetailScreen` | `ui/conversations/ConversationDetailScreen.kt` | Individual conversation message view |
| `AppWhitelistScreen` | `ui/settings/AppWhitelistScreen.kt` | Toggle monitoring per installed app |
| `MarkdownText` / `RichMarkdownContent` | `ui/MarkdownText.kt`, `ui/RichMarkdownContent.kt` | Render AI responses with headers, bullets, bold, code blocks |

---

## How It Works (User Flow)

1. **Install** SummaryLog from the Play Store (or sideload the APK).
2. **Grant** notification access permission when prompted by the app.
3. **Select** which apps to monitor via the whitelist toggle screen.
4. **Notifications are automatically captured** in the background by `NotificationCollectorService` — no user action required.
5. **Browse** notifications in the Home feed (flat list) or Threads view (grouped by conversation).
6. **Ask the AI assistant** natural-language questions about your notification history and get streaming, markdown-formatted answers.

---

## Privacy & Security

- **100% local storage** — no cloud, no servers, no accounts.
- **No data leaves the device** except when querying the Gemini API (only the last 50 notification summaries are sent, never raw personal data).
- **User-controlled whitelist** — only apps the user selects are monitored.
- **No tracking, no analytics, no ads.**

---

## Unique Selling Points

1. **AI-powered notification querying** — Ask "What OTPs did I receive today?" in natural language.
2. **Streaming response with markdown rendering** — Real-time AI output with blinking cursor, code blocks, and formatted text.
3. **Offline fallback engine** — Works without an API key using keyword matching and regex extraction.
4. **Conversation threading** — Notifications grouped by app+title into WhatsApp-style threads.
5. **AMOLED-optimized dark theme** — Pure black background designed for OLED screens.
6. **Zero cloud dependency** — Complete privacy by design.

---

## Current Limitations (v1 Prototype)

- No ViewModel layer — composables access database directly.
- No `onNotificationRemoved` handling — dismissed notifications are not tracked in real-time.
- Gemini prompt sends only the last 50 notifications with no conversation context.
- `AppWhitelistScreen` exists but is not yet reachable from navigation.
- Many advertised features (search, pin, export PDF, privacy mode, auto-delete, statistics) are planned but not yet implemented.
- No unit or integration tests beyond boilerplate placeholders.
