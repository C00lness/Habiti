import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.habiti.habits.impl.domain.Habit
import com.habiti.habits.impl.presentation.CorrelationCard
import com.habiti.habits.impl.R
import com.habiti.habits.impl.cpp.HabitCubeView
@Composable
fun HabitCard(
    habit: Habit,
    onAnalyze: (String) -> Unit,
    onClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    correlation: Double? = null,
    modifier: Modifier = Modifier
) {
    var hideCorrelation by remember { mutableStateOf(true) }
    val progress = remember(habit.streak, habit.targetCount) {
        if (habit.targetCount > 0) {
            (habit.streak.toFloat() / habit.targetCount).coerceIn(0f, 1f)
        } else 0f
    }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Первая строка: иконка + чекбокс
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            Color(habit.color).copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(habit.icon, fontSize = 20.sp)
                }

                Checkbox(
                    checked = habit.isCompletedToday,
                    onCheckedChange = onCheckedChange,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Название привычки
            Text(
                text = habit.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.padding(vertical = 2.dp)
            )

            // Стрик
            Text(
                text = "Стрик: ${habit.streak} ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 2.dp)
            )

            Box(
                modifier = Modifier
                    .width(if (progress == 1.toFloat()) 50.dp else if (progress >= 0.3.toFloat()) 40.dp else 35.dp)
                    .height(if (progress == 1.toFloat()) 50.dp else if (progress >= 0.3.toFloat()) 40.dp else 35.dp)
                    .padding(5.dp)
            ) {
                AndroidView(
                    factory = { context ->
                        HabitCubeView(context).also {
                            it.updateProgress(progress)
                        }
                    },
                    update = { view ->
                        view.updateProgress(progress)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Вторая строка: кнопки (редактировать, удалить, аналитика)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = {
                        onAnalyze(habit.id)
                        hideCorrelation = !hideCorrelation
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = stringResource(R.string.analyzer),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Корреляция (если есть и не скрыта)
            if (correlation != null && !hideCorrelation) {
                CorrelationCard(
                    correlation = correlation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }

            // Прогресс
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${habit.currentCount}/${habit.targetCount}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                LinearProgressIndicator(
                    progress = { habit.currentCount.toFloat() / habit.targetCount },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(4.dp),
                    color = Color(habit.color),
                    trackColor = Color(habit.color).copy(alpha = 0.2f)
                )
            }
        }
    }
}