package com.habiti.ti.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.*
import com.habiti.core.ai.MentorType
import com.habiti.ti.R
import kotlin.random.Random

@Composable
fun MentorAvatar(
    mentorType: MentorType,
    modifier: Modifier = Modifier
) {
    // Запоминаем выбранный вариант анимации при первом создании
    val animationVariant = remember {
        Random.nextInt(1, 5)  // возвращает 1 или 2
    }

    val animationRes = when (mentorType) {
        MentorType.MALE -> when (animationVariant) {
            1 -> R.raw.animation1_man
            2 -> R.raw.animation2_man
            3 -> R.raw.animation3_man
            else -> R.raw.animation4_man
        }
        MentorType.FEMALE -> when (animationVariant) {
            1 -> R.raw.animation1_woman
            else -> R.raw.animation2_woman
        }

        MentorType.ANONYMOUS -> when (animationVariant)
        {
            1 -> R.raw.finance
            else -> R.raw.finance
        }

        MentorType.CAT -> when (animationVariant) {
            1 -> R.raw.cat
            else -> R.raw.cat_2
        }

        MentorType.MR_STRICK -> R.raw.bear
        MentorType.DANCING_WOMAN -> R.raw.data5
    }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(animationRes)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )

    LottieAnimation(
        composition = composition,
        progress = progress,
        modifier = modifier
    )
}