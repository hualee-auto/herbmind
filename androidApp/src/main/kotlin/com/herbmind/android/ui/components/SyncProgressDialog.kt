package com.herbmind.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.herbmind.android.ui.viewmodel.SyncUiState

/**
 * 数据同步进度对话框
 * 
 * 在应用启动时显示，展示数据同步进度
 */
@Composable
fun SyncProgressDialog(
    syncState: SyncUiState,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 只有在非 Idle 状态下显示
    if (syncState is SyncUiState.Idle) return

    Dialog(
        onDismissRequest = { 
            // 只有在成功、无需更新或错误时才允许关闭
            if (syncState is SyncUiState.Success || 
                syncState is SyncUiState.NoUpdate ||
                syncState is SyncUiState.Error) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = syncState !is SyncUiState.Progress &&
                                syncState !is SyncUiState.Checking,
            dismissOnClickOutside = syncState !is SyncUiState.Progress &&
                                   syncState !is SyncUiState.Checking
        )
    ) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (syncState) {
                    is SyncUiState.Checking -> {
                        CheckingContent()
                    }
                    is SyncUiState.Progress -> {
                        ProgressContent(
                            percent = syncState.percent,
                            message = syncState.message
                        )
                    }
                    is SyncUiState.Success -> {
                        SuccessContent(
                            version = syncState.version,
                            herbCount = syncState.herbCount,
                            isFirstSync = syncState.isFirstSync,
                            onConfirm = onDismiss
                        )
                    }
                    is SyncUiState.NoUpdate -> {
                        NoUpdateContent(onConfirm = onDismiss)
                    }
                    is SyncUiState.Error -> {
                        ErrorContent(
                            message = syncState.message,
                            onRetry = onRetry,
                            onDismiss = onDismiss
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun CheckingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "正在检查数据",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "请稍候...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProgressContent(percent: Int, message: String) {
    val animatedProgress by animateFloatAsState(
        targetValue = percent / 100f,
        label = "progress"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "正在同步数据",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // 进度条
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SuccessContent(
    version: Int,
    herbCount: Int,
    isFirstSync: Boolean,
    onConfirm: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = if (isFirstSync) "数据初始化完成" else "数据更新完成",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "当前版本: $version",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "共 $herbCount 味药材",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("开始使用")
        }
    }
}

@Composable
private fun NoUpdateContent(onConfirm: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "数据已是最新",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "无需同步更新",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("确定")
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Text(
            text = "同步失败",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("跳过")
            }

            Button(
                onClick = onRetry,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }
        }
    }
}

/**
 * 全屏同步进度覆盖层（用于首次启动时）
 */
@Composable
fun FullScreenSyncOverlay(
    syncState: SyncUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (syncState is SyncUiState.Idle || 
        syncState is SyncUiState.NoUpdate) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (syncState) {
                    is SyncUiState.Checking -> CheckingContent()
                    is SyncUiState.Progress -> ProgressContent(
                        percent = syncState.percent,
                        message = syncState.message
                    )
                    is SyncUiState.Success -> SuccessContent(
                        version = syncState.version,
                        herbCount = syncState.herbCount,
                        isFirstSync = syncState.isFirstSync,
                        onConfirm = {}
                    )
                    is SyncUiState.Error -> ErrorContent(
                        message = syncState.message,
                        onRetry = onRetry,
                        onDismiss = {}
                    )
                    else -> {}
                }
            }
        }
    }
}
