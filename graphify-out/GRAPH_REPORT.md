# Graph Report - SummaryLog2  (2026-09-07)

## Corpus Check
- 40 files · ~15,253 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 158 nodes · 190 edges · 18 communities detected
- Extraction: 74% EXTRACTED · 26% INFERRED · 0% AMBIGUOUS · INFERRED: 49 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Community Hubs (Navigation)
- [[_COMMUNITY_Community 0|Community 0]]
- [[_COMMUNITY_Community 1|Community 1]]
- [[_COMMUNITY_Community 2|Community 2]]
- [[_COMMUNITY_Community 3|Community 3]]
- [[_COMMUNITY_Community 4|Community 4]]
- [[_COMMUNITY_Community 5|Community 5]]
- [[_COMMUNITY_Community 6|Community 6]]
- [[_COMMUNITY_Community 7|Community 7]]
- [[_COMMUNITY_Community 8|Community 8]]
- [[_COMMUNITY_Community 9|Community 9]]
- [[_COMMUNITY_Community 10|Community 10]]
- [[_COMMUNITY_Community 11|Community 11]]
- [[_COMMUNITY_Community 12|Community 12]]
- [[_COMMUNITY_Community 13|Community 13]]
- [[_COMMUNITY_Community 14|Community 14]]
- [[_COMMUNITY_Community 15|Community 15]]
- [[_COMMUNITY_Community 16|Community 16]]
- [[_COMMUNITY_Community 17|Community 17]]

## God Nodes (most connected - your core abstractions)
1. `Text` - 21 edges
2. `NotificationDao` - 20 edges
3. `SummaryLogMainScreen()` - 12 edges
4. `SummaryLogRootScreen()` - 9 edges
5. `SummaryLogApp()` - 9 edges
6. `MonitoredAppDao` - 8 edges
7. `RichMarkdownContent()` - 7 edges
8. `AppDatabase` - 7 edges
9. `AppWhitelistScreen()` - 6 edges
10. `ConversationDetailScreen()` - 6 edges

## Surprising Connections (you probably didn't know these)
- `SummaryLogMainScreen()` --calls--> `ChatMessageEntity`  [INFERRED]
  app/src/main/java/com/ts/summarylog/SummaryLogMainScreen.kt → app/src/main/java/com/ts/summarylog/data/AppDatabase.kt
- `SummaryLogMainScreen()` --calls--> `isNotificationAccessGranted()`  [INFERRED]
  app/src/main/java/com/ts/summarylog/SummaryLogMainScreen.kt → app/src/main/java/com/ts/summarylog/MainActivity.kt
- `SummaryLogMainScreen()` --calls--> `openNotificationAccessSettings()`  [INFERRED]
  app/src/main/java/com/ts/summarylog/SummaryLogMainScreen.kt → app/src/main/java/com/ts/summarylog/MainActivity.kt
- `SummaryLogRootScreen()` --calls--> `ConversationDetailScreen()`  [INFERRED]
  app/src/main/java/com/ts/summarylog/MainActivity.kt → app/src/main/java/com/ts/summarylog/ui/conversations/ConversationDetailScreen.kt
- `SummaryLogRootScreen()` --calls--> `Text`  [INFERRED]
  app/src/main/java/com/ts/summarylog/MainActivity.kt → app/src/main/java/com/ts/summarylog/ui/RichMarkdownContent.kt

## Communities (25 total, 11 thin omitted)

### Community 0 - "Community 0"
Cohesion: 0.16
Nodes (14): NotificationSearchBar(), ConversationItemRow(), ConversationListScreen(), InsightsScreen(), StatMetricCard(), TopAppProgressRow(), BlinkingCursor(), CodeBlockView() (+6 more)

### Community 2 - "Community 2"
Cohesion: 0.11
Nodes (5): AppStat, ChatDao, ChatMessageEntity, ConversationSummary, MonitoredAppDao

### Community 3 - "Community 3"
Cohesion: 0.2
Nodes (12): ConversationFeedScreen(), AppDestinations, AppTab, Greeting(), GreetingPreview(), isNotificationAccessGranted(), MainActivity, NotificationLogScreen() (+4 more)

### Community 4 - "Community 4"
Cohesion: 0.16
Nodes (7): GeminiChatHelper, ChatMessage, ChatBubble(), ResizableSummaryLogsWindow(), SummaryLogMainScreen(), ChatSectionHeader(), ClearChatConfirmationDialog()

### Community 5 - "Community 5"
Cohesion: 0.15
Nodes (5): NotificationEntity, NotificationCollectorService, NotificationItem, NotificationItem, NotificationRepository

### Community 6 - "Community 6"
Cohesion: 0.38
Nodes (5): ConversationDetailScreen(), MessageBubbleItem(), openSourceApp(), ConversationThreadsContainerScreen(), ConversationMetaEntity

### Community 9 - "Community 9"
Cohesion: 0.6
Nodes (4): MonitoredAppEntity, AppWhitelistScreen(), InstalledAppItem, loadInstalledApps()

## Knowledge Gaps
- **6 isolated node(s):** `AppTab`, `MarkdownSegment`, `AppStat`, `ConversationSummary`, `AppDestinations` (+1 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **11 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `SummaryLogMainScreen()` connect `Community 4` to `Community 0`, `Community 2`, `Community 3`?**
  _High betweenness centrality (0.195) - this node is a cross-community bridge._
- **Why does `Text` connect `Community 0` to `Community 9`, `Community 3`, `Community 4`, `Community 6`?**
  _High betweenness centrality (0.176) - this node is a cross-community bridge._
- **Why does `NotificationDao` connect `Community 1` to `Community 2`?**
  _High betweenness centrality (0.171) - this node is a cross-community bridge._
- **Are the 19 inferred relationships involving `Text` (e.g. with `SummaryLogRootScreen()` and `SummaryLogMainScreen()`) actually correct?**
  _`Text` has 19 INFERRED edges - model-reasoned connections that need verification._
- **Are the 9 inferred relationships involving `SummaryLogMainScreen()` (e.g. with `SummaryLogRootScreen()` and `isNotificationAccessGranted()`) actually correct?**
  _`SummaryLogMainScreen()` has 9 INFERRED edges - model-reasoned connections that need verification._
- **Are the 6 inferred relationships involving `SummaryLogRootScreen()` (e.g. with `ConversationDetailScreen()` and `Text`) actually correct?**
  _`SummaryLogRootScreen()` has 6 INFERRED edges - model-reasoned connections that need verification._
- **Are the 4 inferred relationships involving `SummaryLogApp()` (e.g. with `Text` and `SummaryLogMainScreen()`) actually correct?**
  _`SummaryLogApp()` has 4 INFERRED edges - model-reasoned connections that need verification._