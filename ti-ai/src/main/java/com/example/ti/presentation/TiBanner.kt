package com.example.ti.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ti.TiMessage
import com.example.ti.TiMessageType

@Composable
fun TiBanner(
    message: TiMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (message == null) return

    val backgroundColor = when (message.type) {
        TiMessageType.INFO -> MaterialTheme.colorScheme.primaryContainer
        TiMessageType.SUCCESS -> MaterialTheme.colorScheme.tertiaryContainer
        TiMessageType.WARNING -> MaterialTheme.colorScheme.errorContainer
        TiMessageType.MOTIVATION -> MaterialTheme.colorScheme.secondaryContainer
    }

    val textColor = when (message.type) {
        TiMessageType.INFO -> MaterialTheme.colorScheme.onPrimaryContainer
        TiMessageType.SUCCESS -> MaterialTheme.colorScheme.onTertiaryContainer
        TiMessageType.WARNING -> MaterialTheme.colorScheme.onErrorContainer
        TiMessageType.MOTIVATION -> MaterialTheme.colorScheme.onSecondaryContainer
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Аватарка Ти
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "🤖",
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Текст сообщения
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                modifier = Modifier.weight(1f)
            )

            // Кнопка закрытия
            IconButton(onClick = onDismiss) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Закрыть",
                    tint = textColor
                )
            }
        }
    }
}