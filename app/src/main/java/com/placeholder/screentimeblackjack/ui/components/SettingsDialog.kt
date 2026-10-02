package com.placeholder.screentimeblackjack.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.placeholder.screentimeblackjack.data.BlockedApp
import com.placeholder.screentimeblackjack.service.MonitorForegroundService
import com.placeholder.screentimeblackjack.ui.GameViewModel
import com.placeholder.screentimeblackjack.ui.theme.*
import com.placeholder.screentimeblackjack.util.PermissionHelper

/**
 * Settings and Blocked Apps management modal dialog.
 * Enables user to configure blocked apps, grant accessibility permissions,
 * and request battery optimization exemption.
 */
@Composable
fun SettingsDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val blockedApps by viewModel.blockedApps.collectAsState()
    val isAccessibilityEnabled by viewModel.isAccessibilityEnabled.collectAsState()
    val isBatteryOptimizationIgnored by viewModel.isBatteryOptimizationIgnored.collectAsState()

    var newPackageInput by remember { mutableStateOf("") }
    var newAppNameInput by remember { mutableStateOf("") }
    var inputError by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.88f),
            shape = RoundedCornerShape(20.dp),
            color = CasinoSurfaceDark,
            tonalElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, CasinoGoldDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = CasinoGold,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "APP GATEKEEPER",
                            color = CasinoGoldLight,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 1.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }

                HorizontalDivider(
                    color = CasinoGoldDark.copy(alpha = 0.4f),
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // System Permissions Section
                    item {
                        Text(
                            text = "SYSTEM PERMISSIONS",
                            color = CasinoGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Accessibility Service Status & Action
                        PermissionCard(
                            title = "Accessibility Gatekeeper",
                            description = if (isAccessibilityEnabled) {
                                "Active: Real-time window detection enabled"
                            } else {
                                "REQUIRED: Must be enabled in Settings to detect foreground apps and enforce lock."
                            },
                            isGranted = isAccessibilityEnabled,
                            actionLabel = if (isAccessibilityEnabled) "Settings" else "Enable Service",
                            onActionClick = {
                                PermissionHelper.openAccessibilitySettings(context)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Battery Optimization Exemption
                        PermissionCard(
                            title = "Unrestricted Battery Execution",
                            description = if (isBatteryOptimizationIgnored) {
                                "Exempt from battery optimization (durable)"
                            } else {
                                "Recommended: Prevents Android from killing the background monitoring service."
                            },
                            isGranted = isBatteryOptimizationIgnored,
                            actionLabel = if (isBatteryOptimizationIgnored) "Configured" else "Request Exemption",
                            onActionClick = {
                                PermissionHelper.requestIgnoreBatteryOptimizations(context)
                            }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Persistent Foreground Service Keepalive toggle
                        PersistentServiceCard(context = context)
                    }

                    // Add New App Section
                    item {
                        Text(
                            text = "ADD MONITORED APP",
                            color = CasinoGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CasinoGreenCardBg, RoundedCornerShape(12.dp))
                                .border(1.dp, CasinoGoldDark.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newPackageInput,
                                onValueChange = {
                                    newPackageInput = it
                                    inputError = null
                                },
                                label = { Text("Package Name (e.g. com.reddit.frontpage)") },
                                placeholder = { Text("com.example.app") },
                                singleLine = true,
                                isError = inputError != null,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CasinoGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = CasinoGold,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            OutlinedTextField(
                                value = newAppNameInput,
                                onValueChange = { newAppNameInput = it },
                                label = { Text("Display Name (optional)") },
                                placeholder = { Text("Reddit") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (newPackageInput.isNotBlank()) {
                                        viewModel.addBlockedApp(newPackageInput, newAppNameInput)
                                        newPackageInput = ""
                                        newAppNameInput = ""
                                    }
                                }),
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CasinoGold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedLabelColor = CasinoGold,
                                    unfocusedLabelColor = Color.White.copy(alpha = 0.6f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )

                            if (inputError != null) {
                                Text(
                                    text = inputError!!,
                                    color = FeltRed,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = {
                                    if (newPackageInput.isBlank()) {
                                        inputError = "Please enter a valid package name"
                                    } else {
                                        viewModel.addBlockedApp(newPackageInput, newAppNameInput)
                                        newPackageInput = ""
                                        newAppNameInput = ""
                                        inputError = null
                                    }
                                },
                                modifier = Modifier.align(Alignment.End),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CasinoGold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add App", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Monitored Apps List Section
                    item {
                        Text(
                            text = "MONITORED APPS (${blockedApps.size})",
                            color = CasinoGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (blockedApps.isEmpty()) {
                        item {
                            Text(
                                text = "No apps monitored yet. Add an app above to gate screen time.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        items(blockedApps, key = { it.packageName }) { app ->
                            BlockedAppItem(
                                app = app,
                                onToggle = { viewModel.toggleAppBlocked(app) },
                                onDelete = { viewModel.removeBlockedApp(app.packageName) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isGranted) CasinoGreenCardBg else Color(0xFF2A1515)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isGranted) CasinoGoldDark.copy(alpha = 0.5f) else FeltRed.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isGranted) Color(0xFF1B5E20) else Color(0xFF7F0000)
                ) {
                    Text(
                        text = if (isGranted) "READY" else "DISABLED",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = description,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            Button(
                onClick = onActionClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) CasinoSurfaceDark else CasinoGold,
                    contentColor = if (isGranted) CasinoGold else Color.Black
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(actionLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun PersistentServiceCard(context: Context) {
    var isRunning by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CasinoGreenCardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, CasinoGoldDark.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Keep-Alive Background Service",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = "Runs a low-priority foreground notification to keep the accessibility service active.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp
                )
            }

            Spacer(Modifier.width(8.dp))

            Switch(
                checked = isRunning,
                onCheckedChange = { checked ->
                    isRunning = checked
                    if (checked) {
                        MonitorForegroundService.start(context)
                    } else {
                        MonitorForegroundService.stop(context)
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CasinoGold,
                    checkedTrackColor = CasinoGoldDark,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = CasinoSurfaceDark
                )
            )
        }
    }
}

@Composable
private fun BlockedAppItem(
    app: BlockedApp,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CasinoGreenCardBg)
            .border(1.dp, CasinoGoldDark.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.appName,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = app.packageName,
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Switch(
                checked = app.isBlocked,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = CasinoGold,
                    checkedTrackColor = CasinoGoldDark,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = CasinoSurfaceDark
                )
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = FeltRed.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
