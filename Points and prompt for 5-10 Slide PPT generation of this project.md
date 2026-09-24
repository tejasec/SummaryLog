# Points and Prompt for 5-10 Slide PPT Generation

## Slide-by-Slide Content Outline

---

### Slide 1: Title Slide
- **Title:** SummaryLog — AI-Powered Notification Logger for Android
- **Subtitle:** Never lose an important notification again
- **Tagline:** Capture. Organize. Ask. Summarize.
- **Details:** AI System Design Project | [Your Name] | [Date]

---

### Slide 2: The Problem
- **Heading:** Why Notifications Get Lost
- **Points:**
  - Android notifications are optimized for clearing, not recovering
  - Accidental swipe, "Clear all," or notification overflow loses critical info
  - OTPs, delivery updates, calendar reminders — gone in seconds
  - Android's built-in Notification History is limited (no search, no grouping, no AI)
  - Users experience anxiety: "Did I miss something important?"
- **Visual idea:** Icon showing a notification being swiped away / trash bin

---

### Slide 3: The Solution
- **Heading:** SummaryLog — Your Notification Safety Net
- **Points:**
  - Automatically captures every notification in the background
  - Stores everything locally on-device (100% private)
  - Organizes notifications into conversation threads (WhatsApp-style)
  - AI chat assistant answers natural-language questions about your history
  - Example: "What OTPs did I receive today?" → Instant streaming answer
- **Visual idea:** Phone mockup showing the app's main screen

---

### Slide 4: System Architecture
- **Heading:** How SummaryLog Works
- **Architecture flow:**
  - `NotificationListenerService` (background) → Captures notifications
  - `Room Database` (local) → Stores notifications, app whitelist, chat history
  - `Jetpack Compose UI` → Home feed, conversation threads, AI chat
  - `Gemini 1.5 Flash` (or fallback engine) → AI summarization & querying
- **Visual idea:** Architecture diagram (Service → DB → UI → AI)

---

### Slide 5: Key Features (Implemented)
- **Heading:** What SummaryLog Does Today
- **Features (✅ in code):**
  - Background notification capture via `NotificationListenerService`
  - Local Room database with 3 tables (notifications, monitored_apps, chat_messages)
  - Chat-style conversation threads grouped by app + title
  - AI-powered chat with streaming responses (Gemini 1.5 Flash)
  - Offline fallback engine (keyword matching, OTP extraction, full-text search)
  - App whitelist — choose which apps to monitor
  - Markdown rendering in AI responses (headers, bold, code blocks, bullets)
  - AMOLED dark theme (pure black for OLED screens)
  - Adaptive navigation (bottom bar on phones, rail on tablets)

---

### Slide 6: Tech Stack
- **Heading:** Built With
- **Stack:**
  - **Language:** Kotlin 2.2.10
  - **UI:** Jetpack Compose + Material 3
  - **Database:** Room 2.6.1 (with KSP)
  - **AI:** Google Gemini 1.5 Flash (Generative AI SDK 0.9.0)
  - **Background:** NotificationListenerService
  - **Build:** Gradle 9.3.2, Compose BOM 2025.12.00
  - **Min SDK:** 24 (Android 7.0) | **Target SDK:** 37

---

### Slide 7: AI Integration Deep Dive
- **Heading:** How AI Summarization Works
- **Points:**
  - User types a natural-language question
  - App injects last 50 notifications as context into the prompt
  - Gemini 1.5 Flash streams response token-by-token
  - UI renders in real-time with blinking cursor animation
  - Markdown-formatted output: headers, bullets, bold, code blocks
  - **Fallback mode:** If no API key → PrototypeChatEngine handles queries offline
    - "summarize" → top 5 notifications
    - "otp" / "code" → regex `\b\d{4,6}\b` extraction
    - Otherwise → full-text search through all notifications

---

### Slide 8: Privacy & Local-First Design
- **Heading:** Your Data Never Leaves Your Device
- **Points:**
  - No accounts, no cloud storage, no external servers
  - All data stored in local Room database
  - Only network call: Gemini API (optional, when user asks a question)
  - User controls exactly which apps are monitored
  - No analytics, no tracking, no ads
  - Open-source AI model option for full offline operation

---

### Slide 9: Demo / Screenshots
- **Heading:** See SummaryLog in Action
- **Content (placeholder for screenshots):**
  - Screenshot 1: Home feed showing captured notifications
  - Screenshot 2: Conversation threads view (WhatsApp-style)
  - Screenshot 3: AI chat with streaming response
  - Screenshot 4: App whitelist toggle screen
  - Screenshot 5: AMOLED dark theme

---

### Slide 10: Future Scope & Conclusion
- **Heading:** What's Next
- **Future features:**
  - Search notifications by keyword
  - Pin important conversations
  - Export conversations as PDF
  - Notification insights & statistics
  - Custom rules (auto-dismiss, remind, alarm, auto-reply)
  - Auto-delete old notifications (30/60/90 days)
  - Deep-linking to source apps (open in WhatsApp, Instagram, etc.)
- **Conclusion:** SummaryLog demonstrates how AI can enhance everyday Android utility apps — turning a passive notification stream into an intelligent, queryable knowledge base.
- **Call to action:** "Take control of your notifications with AI."

---

## AI PPT Generator Prompt

Use the following prompt with tools like Gamma, Beautiful.ai, SlidesGo, or any AI presentation generator:

---

```
Create a 10-slide professional presentation for an academic AI System Design project called "SummaryLog".

Project: SummaryLog — An AI-Powered Notification Logger for Android
Course: AI System Design

Slide 1 (Title):
- Title: "SummaryLog"
- Subtitle: "AI-Powered Notification Logger for Android"
- Tagline: "Capture. Organize. Ask. Summarize."
- Add: "AI System Design Project" and placeholder for student name

Slide 2 (Problem):
- Title: "The Problem: Lost Notifications"
- Content: Android notifications are optimized for clearing, not recovering. Important notifications (OTPs, delivery updates, reminders) are lost when swiped away or cleared. Android's built-in Notification History is limited — no search, no grouping, no AI. Users experience anxiety about missing critical information.
- Visual: Show a notification being dismissed/lost

Slide 3 (Solution):
- Title: "Our Solution: SummaryLog"
- Content: Automatically captures every notification in the background. Stores everything locally on-device with 100% privacy. Organizes into conversation threads (WhatsApp-style). AI chat assistant answers natural-language questions like "What OTPs did I receive today?" with streaming, real-time responses.
- Visual: Phone mockup of the app

Slide 4 (Architecture):
- Title: "System Architecture"
- Content: Show the flow: NotificationListenerService (captures) → Room Database (stores) → Jetpack Compose UI (displays) → Gemini 1.5 Flash AI (summarizes). All running on-device with no server required.
- Visual: Architecture diagram with boxes and arrows

Slide 5 (Features):
- Title: "Key Features"
- Content (bullet list): Background notification capture, Local database storage, Chat-style conversation threads, AI-powered summarization with streaming, Offline fallback engine, App whitelist control, Markdown rendering in AI responses, AMOLED dark theme, Adaptive navigation

Slide 6 (Tech Stack):
- Title: "Technology Stack"
- Content: Kotlin 2.2.10, Jetpack Compose + Material 3, Room 2.6.1 with KSP, Google Gemini 1.5 Flash, NotificationListenerService, Gradle 9.3.2, minSdk 24 / targetSdk 37

Slide 7 (AI Integration):
- Title: "AI Integration"
- Content: User asks a question in natural language. App sends last 50 notifications as context to Gemini 1.5 Flash. Response streams token-by-token with blinking cursor. Markdown-formatted output (headers, bullets, code blocks). Offline fallback uses keyword matching and regex for OTP extraction.

Slide 8 (Privacy):
- Title: "Privacy-First Design"
- Content: No accounts. No cloud. No servers. 100% local storage. User controls which apps are monitored. Only network call is optional Gemini API query. No analytics, no tracking, no ads.

Slide 9 (Demo):
- Title: "Demo"
- Content: Placeholder for 4-5 screenshots: Home feed, Conversation threads, AI chat with streaming response, App whitelist screen, AMOLED dark theme

Slide 10 (Future & Conclusion):
- Title: "Future Scope & Conclusion"
- Content: Planned features — Search, Pin, Export PDF, Statistics, Custom rules, Auto-delete, Deep-linking. Conclusion: SummaryLog demonstrates how AI enhances everyday Android apps, turning passive notifications into an intelligent, queryable knowledge base.

Design style: Modern, clean, dark theme preferred (matching the app's AMOLED dark UI). Use purple accent colors. Professional academic presentation style.
```

---

## Key Talking Points for Each Slide

| Slide | Time | Key Point |
|---|---|---|
| 1 | 30s | Introduce yourself and the project name |
| 2 | 1 min | Emphasize the pain point — everyone has lost an important notification |
| 3 | 1 min | Show the core value prop — capture + AI query |
| 4 | 1.5 min | Walk through the architecture, emphasize no server needed |
| 5 | 1.5 min | Highlight the most impressive implemented features |
| 6 | 1 min | Briefly mention the modern tech stack choices |
| 7 | 2 min | Deep dive into AI — this is the "AI" part of "AI System Design" |
| 8 | 1 min | Privacy is a major differentiator — no cloud, no accounts |
| 9 | 2 min | Live demo or walkthrough of screenshots |
| 10 | 1 min | Show vision for the future, wrap up with conclusion |
