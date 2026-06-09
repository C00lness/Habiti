package com.habiti.ti.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.habiti.core.ai.MentorType
import com.habiti.ti.R

@Composable
fun MentorTypeCard(
    type: MentorType,
    isSelected: Boolean,
    onClick: () -> Unit,
    isLocked: Boolean = false,
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
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
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
                Text(
                    text = when(type) {
                        MentorType.MALE -> stringResource(R.string.mentor_man_logo)
                        MentorType.FEMALE -> stringResource(R.string.mentor_woman_logo)
                        MentorType.ANONYMOUS -> stringResource(R.string.mentor_finance_logo)
                        MentorType.CAT -> stringResource(R.string.mentor_cat_logo)
                        MentorType.MR_STRICK -> stringResource(R.string.mentor_mr_strick_logo)
                    },
                    fontSize = MaterialTheme.typography.displayMedium.fontSize)
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when(type) {
                        MentorType.MALE -> stringResource(R.string.mentor_man)
                        MentorType.FEMALE -> stringResource(R.string.mentor_woman)
                        MentorType.ANONYMOUS -> stringResource(R.string.mentor_finance)
                        MentorType.CAT -> stringResource(R.string.mentor_cat)
                        MentorType.MR_STRICK -> stringResource(R.string.mentor_mr_strick)
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = when(type) {
                        MentorType.MALE -> stringResource(R.string.mentor_man_desc)
                        MentorType.FEMALE -> stringResource(R.string.mentor_woman_desc)
                        MentorType.ANONYMOUS -> stringResource(R.string.mentor_finance_desc)
                        MentorType.CAT -> stringResource(R.string.mentor_cat_desc)
                        MentorType.MR_STRICK -> stringResource(R.string.mentor_mr_strick_desc)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}