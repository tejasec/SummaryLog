package com.ts.summarylog.ui.settings

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.ts.summarylog.data.AppDatabase
import com.ts.summarylog.data.MonitoredAppEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class InstalledAppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isMonitored: Boolean
)

suspend fun loadInstalledApps(context: Context, database: AppDatabase): List<InstalledAppItem> =
    withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val launchableApps = pm.queryIntentActivities(mainIntent, 0)

        launchableApps.mapNotNull { resolveInfo ->
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == context.packageName) return@mapNotNull null // Don't list SummaryLog

            val name = resolveInfo.loadLabel(pm).toString()
            val icon = resolveInfo.loadIcon(pm)
            InstalledAppItem(
                packageName = pkg,
                appName = name,
                icon = icon,
                isMonitored = false
            )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.appName.lowercase() }
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppWhitelistScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val coroutineScope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var rawInstalledApps by remember { mutableStateOf<List<InstalledAppItem>>(emptyList()) }
    val monitoredEntities by database.monitoredAppDao().getAllMonitoredApps().collectAsState(initial = emptyList())
    val monitoredSet = remember(monitoredEntities) {
        monitoredEntities.filter { it.isMonitored }.map { it.packageName }.toSet()
    }

    LaunchedEffect(Unit) {
        rawInstalledApps = loadInstalledApps(context, database)
    }

    val filteredApps = remember(rawInstalledApps, monitoredSet, searchQuery) {
        rawInstalledApps
            .map { app -> app.copy(isMonitored = app.packageName in monitoredSet) }
            .filter {
                it.appName.contains(searchQuery, ignoreCase = true) ||
                it.packageName.contains(searchQuery, ignoreCase = true)
            }
    }

    val allSelected = filteredApps.isNotEmpty() && filteredApps.all { it.isMonitored }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitored Apps") },
                actions = {
                    if (filteredApps.isNotEmpty()) {
                        TextButton(onClick = {
                            val targetState = !allSelected
                            coroutineScope.launch(Dispatchers.IO) {
                                val entities = filteredApps.map { app ->
                                    MonitoredAppEntity(app.packageName, app.appName, targetState)
                                }
                                database.monitoredAppDao().setAppMonitoringBatch(entities)
                            }
                        }) {
                            Text(if (allSelected) "Deselect All" else "Select All")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search installed apps...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            if (filteredApps.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val monitoredCount = filteredApps.count { it.isMonitored }
                    Text(
                        text = "$monitoredCount of ${filteredApps.size} monitored",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (allSelected) "Deselect All" else "Select All",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = allSelected,
                            onCheckedChange = { isChecked ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    val entities = filteredApps.map { app ->
                                        MonitoredAppEntity(app.packageName, app.appName, isChecked)
                                    }
                                    database.monitoredAppDao().setAppMonitoringBatch(entities)
                                }
                            }
                        )
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (app.icon != null) {
                            Image(
                                painter = rememberDrawablePainter(drawable = app.icon),
                                contentDescription = null,
                                modifier = Modifier.size(40.dp)
                            )
                        } else {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {}
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.appName,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = app.isMonitored,
                            onCheckedChange = { isChecked ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    database.monitoredAppDao().setAppMonitoring(
                                        MonitoredAppEntity(
                                            app.packageName,
                                            app.appName,
                                            isChecked
                                        )
                                    )
                                }
                            }
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}
