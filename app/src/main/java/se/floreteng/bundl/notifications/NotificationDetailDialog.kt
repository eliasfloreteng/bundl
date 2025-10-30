package se.floreteng.bundl.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotificationDetailDialog(
    notification: Notification,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Notification Details",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Package Name
                DetailRow(
                    label = "Package",
                    value = notification.packageName
                )

                // Title
                notification.title?.let { title ->
                    DetailRow(
                        label = "Title",
                        value = title
                    )
                }

                // Text
                notification.text?.let { text ->
                    DetailRow(
                        label = "Text",
                        value = text
                    )
                }

                // SubText
                notification.subText?.let { subText ->
                    DetailRow(
                        label = "Sub Text",
                        value = subText
                    )
                }

                // Category
                DetailRow(
                    label = "Category",
                    value = notification.category
                )

                // Tag
                notification.tag?.let { tag ->
                    DetailRow(
                        label = "Tag",
                        value = tag
                    )
                }

                // Notification Time
                DetailRow(
                    label = "Time",
                    value = formatTimestamp(notification.notificationTime)
                )

                // Post Time
                DetailRow(
                    label = "Posted",
                    value = formatTimestamp(notification.postTime)
                )

                // Key
                DetailRow(
                    label = "Key",
                    value = notification.key,
                    isMonospace = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    isMonospace: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value,
            style = if (isMonospace) {
                MaterialTheme.typography.bodySmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            } else {
                MaterialTheme.typography.bodyMedium
            },
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(date)
}
