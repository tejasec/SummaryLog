# Questions That Could Be Asked in the Presentation

---

## Practical Explanations

**Q: What problem does SummaryLog solve?**  
A: Android notifications are optimized for dismissal, not recovery. Once swiped away or cleared via "Clear all," important information (OTPs, delivery updates, messages) becomes irretrievable. SummaryLog creates a persistent, AI-queryable notification archive that runs entirely on-device.

**Q: How does the app capture notifications without the user doing anything?**  
A: SummaryLog uses Android's `NotificationListenerService` API. Once the user grants notification access permission, the system automatically delivers every incoming notification to our background service. The service extracts the title and text, resolves the app name, and stores everything in a local Room database — all without any user interaction.

**Q: What happens if the user kills the app or it gets removed from memory?**  
A: The `NotificationListenerService` is managed by the Android system, not the app's process lifecycle. It continues running even when the app UI is closed. Notifications are captured and stored in the Room database regardless of whether the app is open.

**Q: How does the AI summarization work?**  
A: When the user types a question, the app constructs a prompt containing the last 50 captured notifications (as a bulleted list) and sends it to Google Gemini 1.5 Flash via the Generative AI SDK. The response is streamed back token-by-token and rendered in real-time with a blinking cursor animation. If no API key is configured, a built-in keyword-match fallback engine handles basic queries locally.

**Q: What is the fallback engine?**  
A: `PrototypeChatEngine` is a lightweight offline assistant that uses regex and keyword matching. It can summarize the top 5 most recent notifications, extract OTP codes (4-6 digit numbers), and perform full-text search through notification titles and body text — all without any API call.

**Q: How are notifications organized?**  
A: Notifications are grouped by `(packageName, title)` into conversation threads. For example, all WhatsApp messages from "John" appear as one thread, all OTP messages from "BankApp" as another. This mimics a messaging app's conversation list.

**Q: Can the user choose which apps to monitor?**  
A: Yes. The app whitelist screen lists all installed launchable apps with toggle switches. If any apps are whitelisted, only notifications from those apps are captured. If no apps are whitelisted, all notifications are captured.

**Q: How much battery does the app consume?**  
A: Minimal. The `NotificationListenerService` is a system-managed component that only activates when a notification arrives. There is no polling, no background threads, and no network activity (except when the user actively asks the AI a question).

---

## Tools Used

**Q: What database does the app use?**  
A: Room (version 2.6.1), which is Android's recommended abstraction layer over SQLite. It provides compile-time SQL verification, coroutine support, and Flow-based reactive queries. The database has three tables: `notifications`, `monitored_apps`, and `chat_messages`.

**Q: What AI model is used?**  
A: Google Gemini 1.5 Flash, accessed via the `com.google.ai.client.generativeai` SDK (version 0.9.0). It's a lightweight, fast model suitable for real-time streaming on mobile. The model receives notification context and responds with markdown-formatted answers.

**Q: Why Gemini and not another LLM?**  
A: Gemini 1.5 Flash offers a good balance of speed, cost, and quality for mobile use. It supports streaming (token-by-token output), has a generous free tier, and the official Android SDK makes integration straightforward. The fallback engine ensures the app works even without an API key.

**Q: What UI framework is used?**  
A: Jetpack Compose with Material 3 design. The app uses `NavigationSuiteScaffold` for adaptive navigation (bottom bar on phones, rail on tablets), `LazyColumn` for scrollable lists, and custom composables for markdown rendering, code blocks, and the blinking cursor animation.

**Q: What is KSP?**  
A: Kotlin Symbol Processing — a build-time tool that processes Room's `@Database`, `@Entity`, and `@Dao` annotations to generate the implementation code. It replaces the older KAPT compiler and is significantly faster.

---

## Languages Used

**Q: What programming language is the app written in?**  
A: Kotlin (version 2.2.10). All application logic — database entities, DAOs, services, AI helpers, UI composables, and navigation — is written in Kotlin. XML is used only for Android resources (strings, colors, themes, drawable icons).

**Q: Why Kotlin over Java?**  
A: Kotlin is Google's recommended language for Android development. It offers coroutines for async operations, null safety, concise syntax, and first-class support for Jetpack Compose. Room and the Generative AI SDK both have superior Kotlin APIs.

---

## Android Specific Questions

**Q: What permissions does the app require?**  
A: The primary permission is `BIND_NOTIFICATION_LISTENER_SERVICE`, which is a special system permission granted through Android Settings (not the normal permission dialog). The app also needs `INTERNET` (for Gemini API calls) and standard storage/network permissions. No camera, location, or contacts permissions are required.

**Q: How does `NotificationListenerService` differ from a regular service?**  
A: `NotificationListenerService` is a system-managed component that receives callbacks when notifications are posted or removed. It runs in its own process and persists even when the app is closed. Unlike a regular `Service`, it cannot be started/stopped by the app — it's controlled by the user through Android's "Notification access" settings.

**Q: Why Room instead of raw SQLite?**  
A: Room provides compile-time SQL verification (catches query errors at build time), automatic object mapping (entities ↔ Kotlin objects), Flow-based reactive queries (UI updates automatically when data changes), and coroutine support. Raw SQLite would require manual cursor management and is error-prone.

**Q: How does the app handle notification grouping?**  
A: The `NotificationDao.getConversationThreads()` query uses `GROUP BY packageName, title` with `COUNT(*)` and `MAX(timestamp)` to create conversation summaries. This groups all notifications from the same app with the same title into a single thread, showing the message count and most recent timestamp.

**Q: What is `fallbackToDestructiveMigration()`?**  
A: A Room configuration option that destroys and recreates the database when the schema version changes (currently version 3). This is a prototype shortcut — in production, proper `Migration` objects should be used to preserve user data across schema upgrades.

**Q: Why compileSdk 37?**  
A: compileSdk 37 targets the latest Android API level, giving access to the newest platform APIs and ensuring compatibility with the latest Android versions. minSdk 24 (Android 7.0) ensures broad device coverage.

---

## Frontend / Backend

**Q: Is there a backend server?**  
A: No. SummaryLog is a fully offline, client-side application. All data is stored in a local Room database on the device. The only network call is to the Google Gemini API when the user actively asks the AI a question — and even that is optional (the fallback engine works without any network).

**Q: How does the UI stay reactive?**  
A: Room DAOs return `Flow<List<T>>` objects. Composables collect these flows using `collectAsState()`, which automatically triggers recomposition whenever the underlying data changes. When a new notification arrives or a chat message is updated, the UI refreshes instantly without manual refresh logic.

**Q: How is the AI response streamed to the UI?**  
A: `GeminiChatHelper.streamQueryWithNotifications()` returns a `Flow<String>` that emits tokens as they arrive from the Gemini API. The composable collects this flow and progressively appends each token to the chat message in the database. The `BlinkingCursor` composable shows an animated cursor during streaming and disappears when the response is complete.

**Q: What is the chat bubble rendering pipeline?**  
A: AI responses go through `RichMarkdownContent`, which parses the raw markdown text into `MarkdownSegment` objects (either `Text` or `Code`). Text segments are rendered by `MarkdownText` (handles `##` headers, `**bold**`, `*italic*`, `` `code` ``, bullet points). Code segments are rendered by `CodeBlockView` (dark card, monospace font, language label, copy button, horizontal scroll).

**Q: How does the adaptive navigation work?**  
A: `NavigationSuiteScaffold` automatically switches between a bottom navigation bar (on phones) and a navigation rail (on tablets/wide screens) based on window size class. The app has two destinations: HOME (notification feed + AI chat) and THREADS (conversation list).
