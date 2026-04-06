package com.habiti.app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habiti.core.ai.MentorType
import com.habiti.core.ai.UserPreferences
import com.habiti.ti.presentation.MentorTypeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (UserPreferences) -> Unit
) {
    var step by remember { mutableStateOf(0) }

    var userName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf<MentorType?>(null) }
    var mentorName by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    val chooseMentorError = stringResource(R.string.choose_mentor)
    val enterNameError = stringResource(R.string.enter_user_name)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (step == 0) stringResource(R.string.who_are_y) else stringResource(R.string.choose_mentor),
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (step == 0) {
                Text(
                    text = stringResource(R.string.nice_meet_y),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(R.string.how_should_y),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = userName,
                    onValueChange = {
                        userName = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.user_name)) },
                    placeholder = { Text(stringResource(R.string.who_are_y)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = {
                        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error)
                    }
                )

                Button(
                    onClick = {
                        if (userName.isBlank()) {
                            error = enterNameError
                        } else {
                            step = 1
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(stringResource(R.string.next), fontWeight = FontWeight.Bold)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    Text(
                        text = stringResource(R.string.choose_mentor),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.who_y_teach),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MentorTypeCard(
                            type = MentorType.MALE,
                            isSelected = selectedType == MentorType.MALE,
                            onClick = { selectedType = MentorType.MALE; error = null },
                            modifier = Modifier.weight(1f)
                        )
                        MentorTypeCard(
                            type = MentorType.FEMALE,
                            isSelected = selectedType == MentorType.FEMALE,
                            onClick = { selectedType = MentorType.FEMALE; error = null },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        MentorTypeCard(
                            type = MentorType.CAT,
                            isSelected = selectedType == MentorType.CAT,
                            onClick = {selectedType = MentorType.CAT; error = null  },
                            modifier = Modifier.weight(1f)
                        )
                        MentorTypeCard(
                            type = MentorType.ANONYMOUS,
                            isSelected = selectedType == MentorType.ANONYMOUS,
                            onClick = { },
                            true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = mentorName,
                        onValueChange = { mentorName = it; error = null },
                        label = { Text(stringResource(R.string.mentor_name)) },
                        placeholder = { Text(stringResource(R.string.mentor_hold)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        isError = error != null,
                        supportingText = {
                            if (error != null) Text(
                                error!!,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                    var errorVar = stringResource(R.string.enter_mentor_name)
                    Button(
                        onClick = {
                            when {
                                selectedType == null -> error = chooseMentorError
                                mentorName.isBlank() -> error = errorVar

                                else -> {
                                    onComplete(
                                        UserPreferences(
                                            mentorType = selectedType!!,
                                            mentorName = mentorName.trim(),
                                            userName = userName.trim(),
                                            isOnboardingCompleted = true
                                        )
                                    )
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        enabled = selectedType != null && mentorName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.start_y_way), fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}