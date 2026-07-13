package com.habiti.ti.presentation

import android.content.Context
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.habiti.core.ai.ModelType
import com.habiti.ti.R

@Composable
fun ModelTypeCard(
    type: ModelType,
    isSelected: Boolean,
    onClick: () -> Unit,
    isLocked: Boolean = false,
    dragonViewFactory: (Context) -> View,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable { onClick() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(
                containerColor = if (isLocked) Color.Black.copy(alpha = 0.6f) else
                    if (isSelected)
                        Color.White  // 👈 Белый при выборе
                    else
                        Color.White  // 👈 Белый всегда
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 8.dp else 2.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                AndroidView(
                    factory = {dragonViewFactory(it) },
                    Modifier.size(80.dp).background(Color.Red)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when(type) {
                        ModelType.DRAGON_GREEN -> stringResource(R.string.dragon_green)
                        ModelType.DRAGON_RED -> stringResource(R.string.dragon_red)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}