package com.habiti.ti.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.habiti.core.ai.MentorType
import com.habiti.core.ai.TiMessage

@Composable
fun TiBanner(
    message: TiMessage?,
    mentorType: MentorType,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (message == null) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onDismiss() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MentorAvatar(
                mentorType = mentorType,
                modifier = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Закрыть")
            }
        }
    }
}